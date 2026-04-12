package Day7_SocketProgramming;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client {

    static boolean validField(String f) {
        if (f.equalsIgnoreCase("name"))
            return true;
        if (f.equalsIgnoreCase("enrollment"))
            return true;
        if (f.equalsIgnoreCase("gender"))
            return true;
        if (f.equalsIgnoreCase("branch"))
            return true;
        if (f.equalsIgnoreCase("faculty"))
            return true;
        if (f.equalsIgnoreCase("course"))
            return true;
        return false;
    }

    public static void main(String[] args) {
        try {
            Socket s = new Socket("localhost", 8000);
            System.out.println("Connected to Server\n");

            BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
            PrintWriter out = new PrintWriter(s.getOutputStream(), true);
            Scanner sc = new Scanner(System.in);

            while (true) {

                String field;
                while (true) {
                    System.out.println("Search by: name / enrollment / gender / branch / faculty / course");
                    System.out.println("Type 'exit' to disconnect.");
                    System.out.print("Field: ");
                    field = sc.nextLine();
                    if (field.equalsIgnoreCase("exit")) {
                        System.out.println("\nDisconnecting from server...");
                        s.close();
                        sc.close();
                        System.out.println("Client closed.");
                        return;
                    }
                    if (validField(field))
                        break;
                    else
                        System.out.println("Invalid Field! Please try again.\n");
                }

                System.out.print("Value: ");
                String value = sc.nextLine();

                out.println(field);
                out.println(value);

                System.out.println("\nResult from Server:");

                String line;

                try {
                    while (true) {
                        line = in.readLine();

                        if (line.equals("END"))
                            break;

                        System.out.println(line);
                    }
                } catch (SocketException e) {
                    System.out.println("\nConnection lost! Server might be down.");
                    System.out.println("Please start Server first, then run Client.");
                    s.close();
                    sc.close();
                    return;
                }

                System.out.println();
            }

        }

        catch (ConnectException e) {
            System.out.println("\n Unable to connect to Server!");
            System.out.println(" Possible reasons:");
            System.out.println("   1. Server program is NOT started");
            System.out.println("   2. Wrong port number (5000)");
            System.out.println("\n Please start Server first, then run Client.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}