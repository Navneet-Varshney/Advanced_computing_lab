package Day5_NFA_Creation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

public class NFAConsole {

    public static void main(String[] args) {
        String filePath = args.length > 0 ? args[0] : Paths.get("Day5_NFA_Creation", "file.txt").toString();
        String input = args.length > 1 ? args[1] : null;

        try (Scanner scanner = new Scanner(System.in)) {
            if (args.length == 0) {
                System.out.print("Enter NFA file path (press Enter for default Day5_NFA_Creation/file.txt): ");
                String entered = scanner.nextLine().trim();
                if (!entered.isEmpty()) {
                    filePath = entered;
                }
            }

            NFAModel model = NFAParser.parse(Paths.get(filePath));
            System.out.println("\nNFA loaded successfully from: " + Paths.get(filePath).toAbsolutePath());
            System.out.println(model.summary());

            if (input == null) {
                System.out.print("Enter input string: ");
                input = scanner.nextLine();
            }

            validateInputSymbols(model, input);

            SimulationResult result = NFASimulator.run(model, input);
            printSimulation(result, input);
        } catch (Exception ex) {
            System.err.println("Error: " + ex.getMessage());
        }
    }

    private static void validateInputSymbols(NFAModel model, String input) {
        for (int i = 0; i < input.length(); i++) {
            String symbol = String.valueOf(input.charAt(i));
            if (!model.alphabet.contains(symbol)) {
                throw new IllegalArgumentException(
                    "Input contains symbol '" + symbol + "' which is not in alphabet " + model.alphabet
                );
            }
        }
    }

    private static void printSimulation(SimulationResult result, String input) {
        System.out.println("\n=== Simulation Start ===");
        System.out.println("Input string: '" + input + "'");
        System.out.println("Initial states(start): " + result.initialStates);

        for (SimulationStep step : result.steps) {
            System.out.print(step.toLog());
        }

        System.out.println("Final states after input: " + result.finalStatesAfterInput);
        System.out.println("Result: " + (result.accepted ? "ACCEPTED" : "REJECTED"));
        System.out.println("========================");
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

        SimulationStep(int stepNo, String consumedSymbol, Set<String> beforeStates, Set<String> afterStates) {
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

        SimulationResult(
            List<SimulationStep> steps,
            Set<String> initialStates,
            Set<String> finalStatesAfterInput,
            boolean accepted
        ) {
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
            return symbol.equals("e")
                || symbol.equalsIgnoreCase("eps")
                || symbol.equalsIgnoreCase("epsilon");
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
}
