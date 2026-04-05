package Day7_SocketProgramming;

import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Server {
    static int clientCounter = 0;
    static ServerSocket ss;

    static String time() {
        return new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(new Date());
    }

    static int countRecords() {
        int count = 0;
        try {
            BufferedReader br = new BufferedReader(new FileReader("students.csv"));
            br.readLine();
            while (br.readLine() != null)
                count++;
            br.close();
        } catch (Exception e) {
        }
        return count;
    }

    public static void main(String[] args) {
        try {
            int total = countRecords();
            System.out.println("[" + time() + "] CSV Loaded. Records = " + total);
            ss = new ServerSocket(5000);
            System.out.println("[" + time() + "] SERVER STARTED on PORT 5000");
            System.out.println("[" + time() + "] Waiting for client...");
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    System.out.println("\n[" + time() + "] Server shutting down...");
                    if (ss != null && !ss.isClosed()) {
                        ss.close();
                    }
                    System.out.println("[" + time() + "] Server stopped successfully.");
                } catch (Exception e) {
                }
            }));
            while (true) {
                Socket s = ss.accept();

                clientCounter++;
                int clientId = clientCounter;

                System.out.println("\n[" + time() + "] CLIENT-" + clientId + " CONNECTED");

                new ClientHandler(s, clientId).start();
            }

        }
        catch (SocketException e) {
        }
        catch (Exception e) {
            System.out.println("Server Error: " + e);
        }
    }
}

class ClientHandler extends Thread {

    Socket s;
    int clientId;

    ClientHandler(Socket s, int clientId) {
        this.s = s;
        this.clientId = clientId;
    }

    public void run() {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
            PrintWriter out = new PrintWriter(s.getOutputStream(), true);

            String field, value;

            while (true) {

                field = in.readLine();
                if (field == null)
                    break;
                value = in.readLine();

                System.out.println("\n[" + Server.time() + "] ==============================");
                System.out.println("[" + Server.time() + "] CLIENT-" + clientId + " Query: " + field + ":" + value);
                System.out.println("[" + Server.time() + "] Searching database...");

                BufferedReader file = new BufferedReader(new FileReader("students.csv"));

                file.readLine();

                String line;
                boolean found = false;
                int recordNo = 0;

                while ((line = file.readLine()) != null) {

                    String data[] = line.split(",");

                    boolean match = (field.equalsIgnoreCase("name") && data[0].equalsIgnoreCase(value)) ||
                            (field.equalsIgnoreCase("enrollment") && data[1].equalsIgnoreCase(value)) ||
                            (field.equalsIgnoreCase("faculty") && data[2].equalsIgnoreCase(value)) ||
                            (field.equalsIgnoreCase("residence") && data[3].equalsIgnoreCase(value)) ||
                            (field.equalsIgnoreCase("course") && data[4].equalsIgnoreCase(value)) ||
                            (field.equalsIgnoreCase("year") && data[5].equalsIgnoreCase(value)) ||
                            (field.equalsIgnoreCase("phone") && data[6].equalsIgnoreCase(value)) ||
                            (field.equalsIgnoreCase("email") && data[7].equalsIgnoreCase(value)) ||
                            (field.equalsIgnoreCase("gender") && data[8].equalsIgnoreCase(value)) ||
                            (field.equalsIgnoreCase("branch") && data[9].equalsIgnoreCase(value));

                    if (match) {
                        recordNo++;
                        found = true;

                        out.println("Record " + recordNo);
                        out.println("Name: " + data[0]);
                        out.println("Enrollment: " + data[1]);
                        out.println("Faculty No: " + data[2]);
                        out.println("Residence: " + data[3]);
                        out.println("Course: " + data[4]);
                        out.println("Year: " + data[5]);
                        out.println("Phone: " + data[6]);
                        out.println("Email: " + data[7]);
                        out.println("Gender: " + data[8]);
                        out.println("Branch: " + data[9]);
                        out.println("CGPA: " + data[10]);
                        out.println("--------------------");
                    }
                }

                if (!found)
                    out.println("No Record Found");
                else
                    out.println(recordNo + " Record(s) Found");

                out.println("END");
                file.close();

                System.out.println("[" + Server.time() + "] Response sent to CLIENT-" + clientId);
                System.out.println("[" + Server.time() + "] ==============================");
            }

            s.close();
            System.out.println("[" + Server.time() + "] CLIENT-" + clientId + " DISCONNECTED\n");

        } catch (SocketException e) {
            System.out.println("[" + Server.time() + "] CLIENT-" + clientId + " disconnected unexpectedly.");
        } catch (Exception e) {
            System.out.println("[" + Server.time() + "] Error handling CLIENT-" + clientId);
        }
    }
}