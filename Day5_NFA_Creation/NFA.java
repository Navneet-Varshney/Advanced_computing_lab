

import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;

import javax.swing.*;
import java.awt.BasicStroke;

public class NFA {

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			setLookAndFeel();
			new NFAGui().setVisible(true);
		});
	}

	private static void setLookAndFeel() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception ignored) {
			// Falling back to default look-and-feel is acceptable.
		}
	}

	private static final class NFAModel {
		private final Set<String> states = new LinkedHashSet<>();
		private final Set<String> alphabet = new LinkedHashSet<>();
		private String startState;
		private final Set<String> finalStates = new LinkedHashSet<>();
		private final Map<String, Map<String, Set<String>>> transitions = new LinkedHashMap<>();

		void addTransition(String from, String symbol, String to) {
			transitions
				.computeIfAbsent(from, key -> new LinkedHashMap<>())
				.computeIfAbsent(symbol, key -> new LinkedHashSet<>())
				.add(to);
		}

		Set<String> move(Set<String> inputStates, String symbol) {
			Set<String> result = new LinkedHashSet<>();
			for (String state : inputStates) {
				result.addAll(getTargets(state, symbol));
			}
			return result;
		}

		private Set<String> getTargets(String from, String symbol) {
			Map<String, Set<String>> bySymbol = transitions.get(from);
			if (bySymbol == null) {
				return Collections.emptySet();
			}
			return bySymbol.getOrDefault(symbol, Collections.emptySet());
		}

		String summary() {
			return "States      : " + states + "\n"
				+ "Alphabet    : " + alphabet + "\n"
				+ "Start State : " + startState + "\n"
				+ "Final States: " + finalStates + "\n"
				+ "Transitions :\n" + transitionLines();
		}

		String transitionLines() {
			StringBuilder sb = new StringBuilder();
			for (Map.Entry<String, Map<String, Set<String>>> fromEntry : transitions.entrySet()) {
				for (Map.Entry<String, Set<String>> symbolEntry : fromEntry.getValue().entrySet()) {
					sb.append("  ")
						.append(fromEntry.getKey())
						.append(" --")
						.append(symbolEntry.getKey())
						.append("--> ")
						.append(symbolEntry.getValue())
						.append("\n");
				}
			}
			return sb.length() == 0 ? "  (none)\n" : sb.toString();
		}
	}

	private static final class SimulationStep {
		private final int stepNo;
		private final String consumedSymbol;
		private final Set<String> beforeStates;
		private final Set<String> afterStates;

		SimulationStep(int stepNo, String consumedSymbol, Set<String> beforeStates,
					   Set<String> afterStates) {
			this.stepNo = stepNo;
			this.consumedSymbol = consumedSymbol;
			this.beforeStates = new LinkedHashSet<>(beforeStates);
			this.afterStates = new LinkedHashSet<>(afterStates);
		}

		String toLog() {
			return "Step " + stepNo + " | symbol='" + consumedSymbol + "'\n"
				+ "  Current states         : " + beforeStates + "\n"
				+ "  After transition(move) : " + afterStates + "\n";
		}
	}

	private static final class SimulationResult {
		private final List<SimulationStep> steps;
		private final Set<String> initialStates;
		private final Set<String> finalStatesAfterInput;
		private final boolean accepted;

		SimulationResult(List<SimulationStep> steps, Set<String> initialStates,
						 Set<String> finalStatesAfterInput, boolean accepted) {
			this.steps = steps;
			this.initialStates = new LinkedHashSet<>(initialStates);
			this.finalStatesAfterInput = new LinkedHashSet<>(finalStatesAfterInput);
			this.accepted = accepted;
		}
	}

	private static final class NFAParser {
		static NFAModel parse(Path path) throws IOException {
			List<String> lines = Files.readAllLines(path);
			NFAModel model = new NFAModel();

			boolean readingTransitions = false;
			int lineNo = 0;
			for (String rawLine : lines) {
				lineNo++;
				String line = cleanLine(rawLine);
				if (line.isEmpty()) {
					continue;
				}

				if (line.equalsIgnoreCase("transitions:")) {
					readingTransitions = true;
					continue;
				}

				if (readingTransitions) {
					parseTransitionLine(model, line, lineNo);
				} else {
					parseHeaderLine(model, line, lineNo);
				}
			}

			validateModel(model);
			return model;
		}

		private static String cleanLine(String line) {
			return line.split("#", 2)[0].trim();
		}

		private static void parseHeaderLine(NFAModel model, String line, int lineNo) {
			String[] parts = line.split("=", 2);
			if (parts.length != 2) {
				throw new IllegalArgumentException("Line " + lineNo + ": expected key=value format.");
			}

			String key = parts[0].trim().toLowerCase(Locale.ROOT);
			String value = parts[1].trim();

			switch (key) {
				case "states":
					model.states.addAll(splitCsv(value));
					break;
				case "alphabet":
					model.alphabet.addAll(splitCsv(value));
					break;
				case "start":
				case "startstate":
					model.startState = value;
					break;
				case "final":
				case "finalstates":
					model.finalStates.addAll(splitCsv(value));
					break;
				default:
					throw new IllegalArgumentException("Line " + lineNo + ": unknown key '" + key + "'.");
			}
		}

		private static void parseTransitionLine(NFAModel model, String line, int lineNo) {
			String[] side = line.split("->", 2);
			if (side.length != 2) {
				throw new IllegalArgumentException("Line " + lineNo + ": transition should be from,symbol->to.");
			}

			String left = side[0].trim();
			String right = side[1].trim();

			String[] leftParts = left.split(",", 2);
			if (leftParts.length != 2) {
				throw new IllegalArgumentException("Line " + lineNo + ": left side should be from,symbol.");
			}

			String from = leftParts[0].trim();
			String symbol = leftParts[1].trim();
			List<String> targets = splitCsv(right);
			if (targets.isEmpty()) {
				throw new IllegalArgumentException("Line " + lineNo + ": target states missing.");
			}
			if (symbol.isBlank()) {
				throw new IllegalArgumentException("Line " + lineNo + ": transition symbol cannot be empty.");
			}
			if (isEpsilonSymbol(symbol)) {
				throw new IllegalArgumentException("Line " + lineNo + ": epsilon transitions are not allowed in this NFA.");
			}

			for (String to : targets) {
				model.addTransition(from, symbol, to);
			}
		}

		private static boolean isEpsilonSymbol(String symbol) {
			return symbol.equals("e") || symbol.equalsIgnoreCase("eps")
				|| symbol.equalsIgnoreCase("epsilon") || symbol.equals("ε");
		}

		private static List<String> splitCsv(String csv) {
			if (csv.isBlank()) {
				return Collections.emptyList();
			}
			List<String> items = new ArrayList<>();
			for (String token : csv.split(",")) {
				String value = token.trim();
				if (!value.isEmpty()) {
					items.add(value);
				}
			}
			return items;
		}

		private static void validateModel(NFAModel model) {
			if (model.states.isEmpty()) {
				throw new IllegalArgumentException("states list is empty.");
			}
			if (model.startState == null || model.startState.isBlank()) {
				throw new IllegalArgumentException("start state is missing.");
			}
			if (!model.states.contains(model.startState)) {
				throw new IllegalArgumentException("start state '" + model.startState + "' not in states list.");
			}
			if (model.finalStates.isEmpty()) {
				throw new IllegalArgumentException("final states are missing.");
			}
			for (String fs : model.finalStates) {
				if (!model.states.contains(fs)) {
					throw new IllegalArgumentException("final state '" + fs + "' not in states list.");
				}
			}

			for (String ch : model.alphabet) {
				if (isEpsilonSymbol(ch)) {
					throw new IllegalArgumentException("alphabet cannot include epsilon symbol: " + ch);
				}
			}

			for (Map.Entry<String, Map<String, Set<String>>> fromEntry : model.transitions.entrySet()) {
				String from = fromEntry.getKey();
				if (!model.states.contains(from)) {
					throw new IllegalArgumentException("transition has unknown source state: " + from);
				}
				for (Map.Entry<String, Set<String>> symbolEntry : fromEntry.getValue().entrySet()) {
					String symbol = symbolEntry.getKey();
					if (!model.alphabet.contains(symbol)) {
						throw new IllegalArgumentException("transition uses symbol not in alphabet: " + symbol);
					}
					for (String to : symbolEntry.getValue()) {
						if (!model.states.contains(to)) {
							throw new IllegalArgumentException("transition has unknown target state: " + to);
						}
					}
				}
			}
		}
	}

	private static final class NFASimulator {
		static SimulationResult run(NFAModel model, String input) {
			Set<String> current = new LinkedHashSet<>(Collections.singleton(model.startState));
			Set<String> initial = new LinkedHashSet<>(current);
			List<SimulationStep> steps = new ArrayList<>();

			for (int i = 0; i < input.length(); i++) {
				String symbol = String.valueOf(input.charAt(i));
				Set<String> moved = model.move(current, symbol);
				SimulationStep step = new SimulationStep(i + 1, symbol, current, moved);
				steps.add(step);
				current = moved;
			}

			boolean accepted = !Collections.disjoint(current, model.finalStates);
			return new SimulationResult(steps, initial, current, accepted);
		}
	}

	private static final class NFAGui extends JFrame {
		private final JTextField fileField = new JTextField();
		private final JTextArea nfaDetailsArea = new JTextArea();
		private final JTextArea logsArea = new JTextArea();
		private final JTextField inputField = new JTextField();
		private final JLabel statusLabel = new JLabel("Load NFA to begin.");
		private final GraphPanel graphPanel = new GraphPanel();

		private NFAModel model;
		private SimulationResult activeResult;
		private int nextStepIndex;
		private javax.swing.Timer autoTimer;

		NFAGui() {
			setTitle("NFA Simulator (File + Step Trace)");
			setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			setSize(new Dimension(1050, 680));
			setLocationRelativeTo(null);
			setLayout(new BorderLayout(10, 10));

			JPanel topPanel = buildTopPanel();
			JSplitPane centerPane = buildCenterPane();
			JPanel bottomPanel = buildBottomPanel();

			add(topPanel, BorderLayout.NORTH);
			add(centerPane, BorderLayout.CENTER);
			add(bottomPanel, BorderLayout.SOUTH);

			String defaultPath = Paths.get("Day5_NFA_Creation", "file.txt").toString();
			fileField.setText(defaultPath);

			nfaDetailsArea.setText("NFA details will appear here after loading file.");
			logsArea.setText("Step logs will appear here.\n");
		}

		private JPanel buildTopPanel() {
			JPanel topPanel = new JPanel(new BorderLayout(8, 8));
			topPanel.setBorder(BorderFactory.createTitledBorder("Input File"));

			JButton browseBtn = new JButton("Browse");
			browseBtn.addActionListener(this::onBrowse);

			JButton loadBtn = new JButton("Load NFA");
			loadBtn.addActionListener(this::onLoadNfa);

			JPanel btnPanel = new JPanel(new GridLayout(1, 2, 6, 6));
			btnPanel.add(browseBtn);
			btnPanel.add(loadBtn);

			topPanel.add(fileField, BorderLayout.CENTER);
			topPanel.add(btnPanel, BorderLayout.EAST);
			return topPanel;
		}

		private JSplitPane buildCenterPane() {
			nfaDetailsArea.setEditable(false);
			nfaDetailsArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
			logsArea.setEditable(false);
			logsArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
			graphPanel.setBorder(BorderFactory.createTitledBorder("NFA Graph"));
			graphPanel.setPreferredSize(new Dimension(450, 360));

			JScrollPane detailsScroll = new JScrollPane(nfaDetailsArea);
			detailsScroll.setBorder(BorderFactory.createTitledBorder("Parsed NFA"));

			JPanel left = new JPanel(new BorderLayout(6, 6));
			left.add(detailsScroll, BorderLayout.NORTH);
			left.add(graphPanel, BorderLayout.CENTER);

			JScrollPane right = new JScrollPane(logsArea);
			right.setBorder(BorderFactory.createTitledBorder("Simulation Logs"));

			JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, right);
			splitPane.setResizeWeight(0.45);
			return splitPane;
		}

		private JPanel buildBottomPanel() {
			JPanel bottomPanel = new JPanel(new BorderLayout(8, 8));
			bottomPanel.setBorder(BorderFactory.createTitledBorder("Simulation Controls"));

			JPanel controls = new JPanel(new GridLayout(1, 5, 6, 6));
			JButton startBtn = new JButton("Start Simulation");
			startBtn.addActionListener(this::onStartSimulation);

			JButton nextBtn = new JButton("Next Step");
			nextBtn.addActionListener(this::onNextStep);

			JButton autoBtn = new JButton("Auto Run");
			autoBtn.addActionListener(evt -> onAutoRun());

			JButton stopBtn = new JButton("Stop");
			stopBtn.addActionListener(evt -> stopAutoRun());

			JButton clearBtn = new JButton("Clear Logs");
			clearBtn.addActionListener(evt -> logsArea.setText(""));

			controls.add(startBtn);
			controls.add(nextBtn);
			controls.add(autoBtn);
			controls.add(stopBtn);
			controls.add(clearBtn);

			JPanel inputPanel = new JPanel(new BorderLayout(8, 8));
			inputPanel.add(new JLabel("Input String:"), BorderLayout.WEST);
			inputPanel.add(inputField, BorderLayout.CENTER);

			statusLabel.setOpaque(true);
			statusLabel.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
			setStatusNeutral("Load NFA and run simulation.");

			JPanel upper = new JPanel(new BorderLayout(8, 8));
			upper.add(inputPanel, BorderLayout.CENTER);
			upper.add(statusLabel, BorderLayout.EAST);

			bottomPanel.add(upper, BorderLayout.NORTH);
			bottomPanel.add(controls, BorderLayout.SOUTH);
			return bottomPanel;
		}

		private void onBrowse(ActionEvent ignored) {
			JFileChooser chooser = new JFileChooser();
			if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
				fileField.setText(chooser.getSelectedFile().getAbsolutePath());
			}
		}

		private void onLoadNfa(ActionEvent ignored) {
			stopAutoRun();
			String inputPath = fileField.getText().trim();
			if (inputPath.isEmpty()) {
				showError("Please provide a valid NFA file path.");
				return;
			}

			try {
				Path path = Paths.get(inputPath);
				model = NFAParser.parse(path);
				activeResult = null;
				nextStepIndex = 0;
				nfaDetailsArea.setText(model.summary());
				graphPanel.setModel(model);
				graphPanel.setActiveStates(Collections.singleton(model.startState));
				graphPanel.setStepContext(Collections.emptySet(), "");
				logsArea.append("Loaded NFA from: " + path.toAbsolutePath() + "\n");
				setStatusNeutral("NFA loaded successfully.");
			} catch (Exception ex) {
				model = null;
				graphPanel.setModel(null);
				showError("Failed to load NFA: " + ex.getMessage());
			}
		}

		private void onStartSimulation(ActionEvent ignored) {
			stopAutoRun();
			if (model == null) {
				showError("Load NFA first.");
				return;
			}

			String input = inputField.getText();
			if (!isInputValid(input)) {
				return;
			}

			activeResult = NFASimulator.run(model, input);
			nextStepIndex = 0;

			logsArea.append("\n=== New Simulation ===\n");
			logsArea.append("Input string: '" + input + "'\n");
			logsArea.append("Initial states(start): " + activeResult.initialStates + "\n");
			graphPanel.setActiveStates(activeResult.initialStates);
			graphPanel.setStepContext(Collections.emptySet(), "");

			if (activeResult.steps.isEmpty()) {
				finishSimulation();
			} else {
				setStatusNeutral("Simulation started. Use Next Step or Auto Run.");
			}
		}

		private void onNextStep(ActionEvent ignored) {
			if (activeResult == null) {
				showError("Start a simulation first.");
				return;
			}

			if (nextStepIndex >= activeResult.steps.size()) {
				finishSimulation();
				return;
			}

			SimulationStep step = activeResult.steps.get(nextStepIndex);
			logsArea.append(step.toLog());
			graphPanel.setStepContext(step.beforeStates, step.consumedSymbol);
			graphPanel.setActiveStates(step.afterStates);
			nextStepIndex++;

			if (nextStepIndex >= activeResult.steps.size()) {
				finishSimulation();
			}
		}

		private void onAutoRun() {
			if (activeResult == null) {
				showError("Start simulation first, then use Auto Run.");
				return;
			}

			stopAutoRun();
			autoTimer = new javax.swing.Timer(700, evt -> {
				if (activeResult == null || nextStepIndex >= activeResult.steps.size()) {
					stopAutoRun();
					finishSimulation();
					return;
				}
				SimulationStep step = activeResult.steps.get(nextStepIndex);
				logsArea.append(step.toLog());
				graphPanel.setStepContext(step.beforeStates, step.consumedSymbol);
				graphPanel.setActiveStates(step.afterStates);
				nextStepIndex++;
				if (nextStepIndex >= activeResult.steps.size()) {
					stopAutoRun();
					finishSimulation();
				}
			});
			autoTimer.start();
			setStatusNeutral("Auto run in progress...");
		}

		private void stopAutoRun() {
			if (autoTimer != null) {
				autoTimer.stop();
				autoTimer = null;
			}
		}

		private void finishSimulation() {
			if (activeResult == null) {
				return;
			}

			logsArea.append("Final states after input: " + activeResult.finalStatesAfterInput + "\n");
			logsArea.append("Result: " + (activeResult.accepted ? "ACCEPTED" : "REJECTED") + "\n");
			logsArea.append("======================\n");

			if (activeResult.accepted) {
				statusLabel.setBackground(new Color(205, 244, 205));
				statusLabel.setForeground(new Color(20, 90, 20));
				statusLabel.setText("ACCEPTED");
			} else {
				statusLabel.setBackground(new Color(255, 222, 222));
				statusLabel.setForeground(new Color(140, 20, 20));
				statusLabel.setText("REJECTED");
			}
		}

		private boolean isInputValid(String input) {
			if (model == null) {
				return false;
			}
			for (int i = 0; i < input.length(); i++) {
				String symbol = String.valueOf(input.charAt(i));
				if (!model.alphabet.contains(symbol)) {
					showError("Input contains symbol '" + symbol + "' which is not in alphabet " + model.alphabet);
					return false;
				}
			}
			return true;
		}

		private void setStatusNeutral(String message) {
			statusLabel.setBackground(new Color(230, 235, 240));
			statusLabel.setForeground(new Color(45, 45, 45));
			statusLabel.setText(message);
		}

		private void showError(String message) {
			setStatusNeutral("Error occurred. Check dialog/logs.");
			logsArea.append("ERROR: " + message + "\n");
			JOptionPane.showMessageDialog(this, message, "NFA Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	private static final class GraphPanel extends JPanel {
		private NFAModel model;
		private Set<String> activeStates = new LinkedHashSet<>();
		private Set<String> edgeFromStates = new LinkedHashSet<>();
		private String edgeSymbol = "";

		void setModel(NFAModel model) {
			this.model = model;
			repaint();
		}

		void setActiveStates(Set<String> activeStates) {
			this.activeStates = new LinkedHashSet<>(activeStates);
			repaint();
		}

		void setStepContext(Set<String> fromStates, String symbol) {
			this.edgeFromStates = new LinkedHashSet<>(fromStates);
			this.edgeSymbol = symbol == null ? "" : symbol;
			repaint();
		}

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D g2 = (Graphics2D) g.create();
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			if (model == null || model.states.isEmpty()) {
				g2.setColor(new Color(110, 110, 110));
				g2.drawString("Load an NFA to see graph.", 18, 26);
				g2.dispose();
				return;
			}

			List<String> stateList = new ArrayList<>(model.states);
			Map<String, Point> points = layoutStates(stateList, getWidth(), getHeight());

			for (Map.Entry<String, Map<String, Set<String>>> fromEntry : model.transitions.entrySet()) {
				String from = fromEntry.getKey();
				Point pFrom = points.get(from);
				if (pFrom == null) {
					continue;
				}
				for (Map.Entry<String, Set<String>> symbolEntry : fromEntry.getValue().entrySet()) {
					String symbol = symbolEntry.getKey();
					for (String to : symbolEntry.getValue()) {
						Point pTo = points.get(to);
						if (pTo == null) {
							continue;
						}
						boolean highlight = edgeFromStates.contains(from) && symbol.equals(edgeSymbol);
						drawTransition(g2, pFrom, pTo, symbol, highlight);
					}
				}
			}

			for (String state : stateList) {
				Point p = points.get(state);
				if (p == null) {
					continue;
				}
				drawState(g2, state, p, model.startState.equals(state), model.finalStates.contains(state), activeStates.contains(state));
			}

			g2.dispose();
		}

		private Map<String, Point> layoutStates(List<String> states, int width, int height) {
			Map<String, Point> map = new LinkedHashMap<>();
			int n = states.size();
			int cx = Math.max(120, width / 2);
			int cy = Math.max(120, height / 2);
			int radius = Math.max(80, Math.min(width, height) / 2 - 70);
			for (int i = 0; i < n; i++) {
				double angle = (2.0 * Math.PI * i / n) - (Math.PI / 2.0);
				int x = cx + (int) (radius * Math.cos(angle));
				int y = cy + (int) (radius * Math.sin(angle));
				map.put(states.get(i), new Point(x, y));
			}
			return map;
		}

		private void drawState(Graphics2D g2, String name, Point p, boolean isStart, boolean isFinal, boolean isActive) {
			int r = 28;
			Color fill = isActive ? new Color(203, 245, 214) : new Color(245, 245, 248);
			g2.setColor(fill);
			g2.fillOval(p.x - r, p.y - r, 2 * r, 2 * r);

			g2.setColor(new Color(40, 40, 50));
			g2.setStroke(new BasicStroke(2.0f));
			g2.drawOval(p.x - r, p.y - r, 2 * r, 2 * r);

			if (isFinal) {
				g2.drawOval(p.x - (r - 5), p.y - (r - 5), 2 * (r - 5), 2 * (r - 5));
			}

			if (isStart) {
				g2.drawLine(p.x - 55, p.y, p.x - r, p.y);
				drawArrowHead(g2, p.x - 55, p.y, p.x - r, p.y);
			}

			g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
			int textWidth = g2.getFontMetrics().stringWidth(name);
			g2.drawString(name, p.x - textWidth / 2, p.y + 4);
		}

		private void drawTransition(Graphics2D g2, Point from, Point to, String symbol, boolean highlight) {
			Color edgeColor = highlight ? new Color(0, 115, 217) : new Color(90, 95, 110);
			Stroke oldStroke = g2.getStroke();
			g2.setColor(edgeColor);
			g2.setStroke(new BasicStroke(highlight ? 2.8f : 1.8f));

			if (from.equals(to)) {
				int loopR = 20;
				int x = from.x - loopR;
				int y = from.y - 48;
				g2.drawArc(x, y, 2 * loopR, 2 * loopR, 20, 320);
				drawArrowHead(g2, from.x + loopR - 5, from.y - 29, from.x + loopR - 1, from.y - 22);
				g2.drawString(symbol, from.x, from.y - 50);
				g2.setStroke(oldStroke);
				return;
			}

			int nodeR = 28;
			double dx = to.x - from.x;
			double dy = to.y - from.y;
			double len = Math.sqrt(dx * dx + dy * dy);
			if (len < 0.001) {
				g2.setStroke(oldStroke);
				return;
			}

			int x1 = (int) (from.x + (dx / len) * nodeR);
			int y1 = (int) (from.y + (dy / len) * nodeR);
			int x2 = (int) (to.x - (dx / len) * nodeR);
			int y2 = (int) (to.y - (dy / len) * nodeR);

			g2.drawLine(x1, y1, x2, y2);
			drawArrowHead(g2, x1, y1, x2, y2);

			int mx = (x1 + x2) / 2;
			int my = (y1 + y2) / 2;
			g2.drawString(symbol, mx + 4, my - 4);
			g2.setStroke(oldStroke);
		}

		private void drawArrowHead(Graphics2D g2, int x1, int y1, int x2, int y2) {
			double phi = Math.toRadians(24);
			int barb = 11;
			double theta = Math.atan2(y2 - y1, x2 - x1);

			for (int i = 0; i < 2; i++) {
				double rho = theta + (i == 0 ? phi : -phi);
				int x = (int) (x2 - barb * Math.cos(rho));
				int y = (int) (y2 - barb * Math.sin(rho));
				g2.drawLine(x2, y2, x, y);
			}
		}
	}
}
