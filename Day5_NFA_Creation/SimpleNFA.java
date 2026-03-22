package Day5_NFA_Creation;

import java.io.*;
import java.util.Scanner;

public class SimpleNFA {

    static String[] states = new String[20];
    static String[] alphabet = new String[10];
    static String startState;
    static String[] finalStates = new String[10];

    static String[][][] transitions = new String[20][10][20];
    static int[] transCount = new int[200];

    static int stateCount = 0, alphaCount = 0, finalCount = 0;

    // 🔹 Find index
    static int indexOf(String[] arr, int size, String key) {
        for (int i = 0; i < size; i++) {
            if (arr[i].equals(key))
                return i;
        }
        return -1;
    }

    // 🔹 Add transition
    static void addTransition(String from, String symbol, String to) {
        int i = indexOf(states, stateCount, from);
        int j = indexOf(alphabet, alphaCount, symbol);

        if (i == -1 || j == -1)
            return;

        int index = i * 10 + j;
        transitions[i][j][transCount[index]++] = to;
    }

    // 🔹 Print array nicely
    static String printArray(String[] arr, int size) {
        String s = "[";
        for (int i = 0; i < size; i++) {
            s += arr[i];
            if (i != size - 1)
                s += ", ";
        }
        s += "]";
        return s;
    }

    static void printTransitions() {
        System.out.println("Transitions :");

        for (int i = 0; i < stateCount; i++) {
            for (int j = 0; j < alphaCount; j++) {

                int index = i * 10 + j;

                if (transCount[index] > 0) {
                    System.out.print("  " + states[i] + " --" + alphabet[j] + "--> ");

                    String[] temp = new String[20];
                    int size = 0;

                    for (int k = 0; k < transCount[index]; k++) {
                        String t = transitions[i][j][k];

                        // avoid duplicate
                        if (indexOf(temp, size, t) == -1) {
                            temp[size++] = t;
                        }
                    }

                    System.out.println(printArray(temp, size));
                }
            }
        }
    }

    // 🔹 Move function
    static String[] move(String[] current, int currSize, String symbol, int[] newSize) {
        String[] result = new String[20];
        newSize[0] = 0;

        int symIndex = indexOf(alphabet, alphaCount, symbol);

        for (int i = 0; i < currSize; i++) {
            int stateIndex = indexOf(states, stateCount, current[i]);

            int index = stateIndex * 10 + symIndex;

            for (int k = 0; k < transCount[index]; k++) {
                String next = transitions[stateIndex][symIndex][k];

                if (indexOf(result, newSize[0], next) == -1) {
                    result[newSize[0]++] = next;
                }
            }
        }

        return result;
    }

    // 🔹 Validate symbol
    static boolean isValidSymbol(String symbol) {
        return indexOf(alphabet, alphaCount, symbol) != -1;
    }

    static boolean validateInputString(String str) {
        for (int i = 0; i < str.length(); i++) {
            String symbol = String.valueOf(str.charAt(i));

            if (indexOf(alphabet, alphaCount, symbol) == -1) {
                System.out.println("\n Error: Symbol '" + symbol + "' not in alphabet !!!");
                return false;
            }
        }
        return true;
    }

    // 🔹 File read
    static void readFile(String path) throws Exception {
        Scanner sc = new Scanner(new File(path));

        boolean readingTrans = false;

        while (sc.hasNextLine()) {
            String line = sc.nextLine().trim();
            if (line.isEmpty())
                continue;

            if (line.equalsIgnoreCase("transitions:")) {
                readingTrans = true;
                continue;
            }

            if (!readingTrans) {
                String[] parts = line.split("=");

                String key = parts[0].trim();
                String val = parts[1].trim();

                String[] tokens = val.split(",");

                if (key.equalsIgnoreCase("states")) {
                    for (String t : tokens)
                        states[stateCount++] = t.trim();
                } else if (key.equalsIgnoreCase("alphabet")) {
                    for (String t : tokens)
                        alphabet[alphaCount++] = t.trim();
                } else if (key.equalsIgnoreCase("start")) {
                    startState = val;
                } else if (key.equalsIgnoreCase("final")) {
                    for (String t : tokens)
                        finalStates[finalCount++] = t.trim();
                }
            } else {
                String[] parts = line.split("->");
                String[] left = parts[0].split(",");

                String from = left[0].trim();
                String symbol = left[1].trim();

                String[] targets = parts[1].split(",");

                for (String t : targets) {
                    addTransition(from, symbol, t.trim());
                }
            }
        }

        sc.close();
    }

    public static void main(String[] args) throws Exception {

        Scanner input = new Scanner(System.in);

        System.out.print("Enter file path: ");
        String path = input.nextLine();
        System.out.println("\nReading NFA from given file..... \n");
        readFile(path);
        try {
            Thread.sleep(2000); // 🔥 2 second pause
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("File read successfully!\n");
        
    

        // 🔹 Print NFA Summary
        System.out.println("\n========== NFA ==========");
        System.out.println("States      : " + printArray(states, stateCount));
        System.out.println("Alphabet    : " + printArray(alphabet, alphaCount));
        System.out.println("Start State : " + startState);
        System.out.println("Final States: " + printArray(finalStates, finalCount));
        printTransitions();
        System.out.println("=========================");

        // 🔹 Input
        System.out.print("\nEnter input string: ");
        String str = input.nextLine();
        // 🔥 INPUT VALIDATION (yahin add karna hai)
        if (!validateInputString(str)) {
            return;
        }

        String[] current = new String[20];
        int currSize = 1;
        current[0] = startState;

        System.out.println("\n===== Simulation =====");
        System.out.println("Input: " + str);
        System.out.println("Start: " + printArray(current, currSize));

        // 🔹 Simulation
        for (int i = 0; i < str.length(); i++) {
            String symbol = String.valueOf(str.charAt(i));

            int[] newSize = new int[1];
            String[] next = move(current, currSize, symbol, newSize);

            System.out.println("--------------------------------");
            System.out.println("Step " + (i + 1) + " | Symbol: " + symbol);
            System.out.println("Current States : " + printArray(current, currSize));
            System.out.println("Next States    : " + printArray(next, newSize[0]));

            current = next;
            currSize = newSize[0];
            try {
                Thread.sleep(3000); // 🔥 3 second pause
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // 🔹 Final check
        boolean accepted = false;
        for (int i = 0; i < currSize; i++) {
            if (indexOf(finalStates, finalCount, current[i]) != -1) {
                accepted = true;
                break;
            }
        }

        System.out.println("--------------------------------");
        System.out.println("Final States : " + printArray(current, currSize));
        System.out.println("Result       : " + (accepted ? "ACCEPTED" : "REJECTED"));
        System.out.println("===============================");
    }
    
}