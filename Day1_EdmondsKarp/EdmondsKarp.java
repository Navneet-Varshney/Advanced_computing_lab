package Day1_EdmondsKarp;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class EdmondsKarp extends JFrame {

    private JLabel statusLabel;
    private boolean isConverted = false;

    private ArrayList<Node> nodes = new ArrayList<>();
    private ArrayList<Edge> edges = new ArrayList<>();
    private ArrayList<Edge> highlightedEdges = new ArrayList<>();

    private DrawPanel panel;

    private int source = -1;
    private int sink = -1;

    private JButton addNodeBtn;
    private JButton addEdgeBtn;
    private JButton clearGraphBtn;
    private JButton runAlgoBtn;
    private JButton convertBtn;

    public EdmondsKarp() {

        setTitle("Dynamic Flow Network Visualizer");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        panel = new DrawPanel(nodes, edges, highlightedEdges,
                source, sink, isConverted);

        add(panel, BorderLayout.CENTER);

        JPanel topPanel = new JPanel();
        topPanel.setBackground(Color.BLACK);

        addNodeBtn = createButton("Add Node");
        addEdgeBtn = createButton("Add Edge");
        clearGraphBtn = createButton("Clear Graph");
        runAlgoBtn = createButton("Run Edmonds-Karp");
        convertBtn = createButton("Convert to Flow Network");

        topPanel.add(addNodeBtn);
        topPanel.add(addEdgeBtn);
        topPanel.add(convertBtn);
        topPanel.add(runAlgoBtn);
        topPanel.add(clearGraphBtn);

        runAlgoBtn.setVisible(false);

        add(topPanel, BorderLayout.NORTH);

        statusLabel = new JLabel("Total Max Flow: 0",
                SwingConstants.CENTER);

        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 25));
        statusLabel.setForeground(Color.WHITE);
        statusLabel.setOpaque(true);
        statusLabel.setBackground(Color.BLACK);

        add(statusLabel, BorderLayout.SOUTH);

        setVisible(true);

        SwingUtilities.invokeLater(this::askInitialNodes);
    }

    private JButton createButton(String text) {

        JButton btn = new JButton(text);

        btn.setBackground(new Color(40, 40, 40));
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));

        return btn;
    }

    private void askInitialNodes() {

        String input = JOptionPane.showInputDialog(this,
                "Enter Number of Nodes:");

        try {

            int n = Integer.parseInt(input);

            for (int i = 0; i < n; i++)
                nodes.add(new Node(i));

            panel.repaint();

        } catch (Exception ignored) {
        }
    }

    public static void main(String[] args) {
        new EdmondsKarp();
    }
}
