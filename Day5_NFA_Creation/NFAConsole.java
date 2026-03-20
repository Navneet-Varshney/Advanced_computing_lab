package Day5_NFA_Creation;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

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
                    "Input contains symbol '" + symbol + "' which is not in alphabet " + model.alphabet.toDisplayString()
                );
            }
        }
    }

    private static void printSimulation(SimulationResult result, String input) {
        System.out.println("\n=== Simulation Start ===");
        System.out.println("Input string: '" + input + "'");
        System.out.println("Initial states(start): " + result.initialStates.toDisplayString());

        for (int i = 0; i < result.stepCount; i++) {
            System.out.print(result.steps[i].toLog());
        }

        System.out.println("Final states after input: " + result.finalStatesAfterInput.toDisplayString());
        System.out.println("Result: " + (result.accepted ? "ACCEPTED" : "REJECTED"));
        System.out.println("========================");
    }

    private static final class StringArray {
        private String[] data;
        private int size;

        StringArray() {
            this(4);
        }

        StringArray(int capacity) {
            data = new String[Math.max(1, capacity)];
            size = 0;
        }

        void add(String value) {
            ensureCapacity(size + 1);
            data[size++] = value;
        }

        void addUnique(String value) {
            if (!contains(value)) {
                add(value);
            }
        }

        boolean contains(String value) {
            for (int i = 0; i < size; i++) {
                if (data[i].equals(value)) {
                    return true;
                }
            }
            return false;
        }

        String get(int index) {
            return data[index];
        }

        int size() {
            return size;
        }

        boolean isEmpty() {
            return size == 0;
        }

        StringArray copy() {
            StringArray clone = new StringArray(size);
            for (int i = 0; i < size; i++) {
                clone.add(data[i]);
            }
            return clone;
        }

        String toDisplayString() {
            StringBuilder sb = new StringBuilder();
            sb.append("[");
            for (int i = 0; i < size; i++) {
                if (i > 0) {
                    sb.append(", ");
                }
                sb.append(data[i]);
            }
            sb.append("]");
            return sb.toString();
        }

        private void ensureCapacity(int minCapacity) {
            if (minCapacity <= data.length) {
                return;
            }
            int newCapacity = data.length * 2;
            if (newCapacity < minCapacity) {
                newCapacity = minCapacity;
            }
            String[] newData = new String[newCapacity];
            for (int i = 0; i < size; i++) {
                newData[i] = data[i];
            }
            data = newData;
        }
    }

    private static final class Transition {
        private final String from;
        private final String symbol;
        private final String to;

        Transition(String from, String symbol, String to) {
            this.from = from;
            this.symbol = symbol;
            this.to = to;
        }
    }

    private static final class NFAModel {
        private final StringArray states = new StringArray();
        private final StringArray alphabet = new StringArray();
        private String startState;
        private final StringArray finalStates = new StringArray();
        private Transition[] transitions = new Transition[8];
        private int transitionCount;

        void addTransition(String from, String symbol, String to) {
            ensureTransitionCapacity(transitionCount + 1);
            transitions[transitionCount++] = new Transition(from, symbol, to);
        }

        StringArray move(StringArray inputStates, String symbol) {
            StringArray result = new StringArray();
            for (int i = 0; i < inputStates.size(); i++) {
                String state = inputStates.get(i);
                for (int j = 0; j < transitionCount; j++) {
                    Transition t = transitions[j];
                    if (t.from.equals(state) && t.symbol.equals(symbol)) {
                        result.addUnique(t.to);
                    }
                }
            }
            return result;
        }

        String summary() {
            return "States      : " + states.toDisplayString() + "\n"
                + "Alphabet    : " + alphabet.toDisplayString() + "\n"
                + "Start State : " + startState + "\n"
                + "Final States: " + finalStates.toDisplayString() + "\n"
                + "Transitions :\n" + transitionLines();
        }

        String transitionLines() {
            StringBuilder sb = new StringBuilder();
            boolean[] grouped = new boolean[transitionCount];
            for (int i = 0; i < transitionCount; i++) {
                if (grouped[i]) {
                    continue;
                }

                Transition t = transitions[i];
                StringArray targets = new StringArray();
                targets.addUnique(t.to);
                grouped[i] = true;

                for (int j = i + 1; j < transitionCount; j++) {
                    if (grouped[j]) {
                        continue;
                    }
                    Transition other = transitions[j];
                    if (t.from.equals(other.from) && t.symbol.equals(other.symbol)) {
                        targets.addUnique(other.to);
                        grouped[j] = true;
                    }
                }

                sb.append("  ")
                    .append(t.from)
                    .append(" --")
                    .append(t.symbol)
                    .append("--> ")
                    .append(targets.toDisplayString())
                    .append("\n");
            }
            return sb.length() == 0 ? "  (none)\n" : sb.toString();
        }

        private void ensureTransitionCapacity(int minCapacity) {
            if (minCapacity <= transitions.length) {
                return;
            }
            int newCapacity = transitions.length * 2;
            if (newCapacity < minCapacity) {
                newCapacity = minCapacity;
            }
            Transition[] newTransitions = new Transition[newCapacity];
            for (int i = 0; i < transitionCount; i++) {
                newTransitions[i] = transitions[i];
            }
            transitions = newTransitions;
        }
    }

    private static final class SimulationStep {
        private final int stepNo;
        private final String consumedSymbol;
        private final StringArray beforeStates;
        private final StringArray afterStates;

        SimulationStep(int stepNo, String consumedSymbol, StringArray beforeStates, StringArray afterStates) {
            this.stepNo = stepNo;
            this.consumedSymbol = consumedSymbol;
            this.beforeStates = beforeStates.copy();
            this.afterStates = afterStates.copy();
        }

        String toLog() {
            return "Step " + stepNo + " | symbol='" + consumedSymbol + "'\n"
                + "  Current states         : " + beforeStates.toDisplayString() + "\n"
                + "  After transition(move) : " + afterStates.toDisplayString() + "\n";
        }
    }

    private static final class SimulationResult {
        private final SimulationStep[] steps;
        private final int stepCount;
        private final StringArray initialStates;
        private final StringArray finalStatesAfterInput;
        private final boolean accepted;

        SimulationResult(
            SimulationStep[] steps,
            int stepCount,
            StringArray initialStates,
            StringArray finalStatesAfterInput,
            boolean accepted
        ) {
            this.steps = steps;
            this.stepCount = stepCount;
            this.initialStates = initialStates.copy();
            this.finalStatesAfterInput = finalStatesAfterInput.copy();
            this.accepted = accepted;
        }
    }

    private static final class NFAParser {
        static NFAModel parse(Path path) throws IOException {
            NFAModel model = new NFAModel();

            boolean readingTransitions = false;
            int lineNo = 0;
            try (Scanner fileScanner = new Scanner(path)) {
                while (fileScanner.hasNextLine()) {
                    String rawLine = fileScanner.nextLine();
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

            String key = parts[0].trim().toLowerCase();
            String value = parts[1].trim();
            StringArray tokens = splitCsv(value);

            switch (key) {
                case "states":
                    addUniqueAll(model.states, tokens);
                    break;
                case "alphabet":
                    addUniqueAll(model.alphabet, tokens);
                    break;
                case "start":
                case "startstate":
                    model.startState = value;
                    break;
                case "final":
                case "finalstates":
                    addUniqueAll(model.finalStates, tokens);
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
            StringArray targets = splitCsv(right);
            if (targets.isEmpty()) {
                throw new IllegalArgumentException("Line " + lineNo + ": target states missing.");
            }
            if (symbol.isBlank()) {
                throw new IllegalArgumentException("Line " + lineNo + ": transition symbol cannot be empty.");
            }
            if (isEpsilonSymbol(symbol)) {
                throw new IllegalArgumentException("Line " + lineNo + ": epsilon transitions are not allowed in this NFA.");
            }

            for (int i = 0; i < targets.size(); i++) {
                model.addTransition(from, symbol, targets.get(i));
            }
        }

        private static boolean isEpsilonSymbol(String symbol) {
            return symbol.equals("e")
                || symbol.equalsIgnoreCase("eps")
                || symbol.equalsIgnoreCase("epsilon");
        }

        private static StringArray splitCsv(String csv) {
            StringArray items = new StringArray();
            if (csv.isBlank()) {
                return items;
            }
            for (String token : csv.split(",")) {
                String value = token.trim();
                if (!value.isEmpty()) {
                    items.add(value);
                }
            }
            return items;
        }

        private static void addUniqueAll(StringArray target, StringArray source) {
            for (int i = 0; i < source.size(); i++) {
                target.addUnique(source.get(i));
            }
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
            for (int i = 0; i < model.finalStates.size(); i++) {
                String fs = model.finalStates.get(i);
                if (!model.states.contains(fs)) {
                    throw new IllegalArgumentException("final state '" + fs + "' not in states list.");
                }
            }

            for (int i = 0; i < model.alphabet.size(); i++) {
                String ch = model.alphabet.get(i);
                if (isEpsilonSymbol(ch)) {
                    throw new IllegalArgumentException("alphabet cannot include epsilon symbol: " + ch);
                }
            }

            for (int i = 0; i < model.transitionCount; i++) {
                Transition t = model.transitions[i];
                if (!model.states.contains(t.from)) {
                    throw new IllegalArgumentException("transition has unknown source state: " + t.from);
                }
                if (!model.alphabet.contains(t.symbol)) {
                    throw new IllegalArgumentException("transition uses symbol not in alphabet: " + t.symbol);
                }
                if (!model.states.contains(t.to)) {
                    throw new IllegalArgumentException("transition has unknown target state: " + t.to);
                }
            }
        }
    }

    private static final class NFASimulator {
        static SimulationResult run(NFAModel model, String input) {
            StringArray current = new StringArray();
            current.add(model.startState);
            StringArray initial = current.copy();
            SimulationStep[] steps = new SimulationStep[Math.max(1, input.length())];
            int stepCount = 0;

            for (int i = 0; i < input.length(); i++) {
                String symbol = String.valueOf(input.charAt(i));
                StringArray moved = model.move(current, symbol);
                SimulationStep step = new SimulationStep(i + 1, symbol, current, moved);
                steps[stepCount++] = step;
                current = moved;
            }

            boolean accepted = hasCommonState(current, model.finalStates);
            return new SimulationResult(steps, stepCount, initial, current, accepted);
        }

        private static boolean hasCommonState(StringArray a, StringArray b) {
            for (int i = 0; i < a.size(); i++) {
                if (b.contains(a.get(i))) {
                    return true;
                }
            }
            return false;
        }
    }
}
