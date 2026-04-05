package Day7_SocketProgramming;

import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Server {
    static int clientCounter = 0;

    // time stamp
    static String time() {
        return new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(new Date());
    }

    // ⭐ CSV RECORD COUNT
    static int countRecords() {
        int count = 0;
        try {
            BufferedReader br = new BufferedReader(new FileReader("students.csv"));
            while (br.readLine() != null)
                count++;
            br.close();
        } catch (Exception e) {
        }
        return count;
    }

    public static void main(String[] args) {
        try {
            // ⭐ CSV LOAD LOG
            int total = countRecords();
            System.out.println("[" + time() + "] CSV Loaded. Records = " + total);

            ServerSocket ss = new ServerSocket(5000);
            System.out.println("[" + time() + "] SERVER STARTED on PORT 5000");
            System.out.println("[" + time() + "] Waiting for client...");

            // ⭐ infinite server loop
            while (true) {
                Socket s = ss.accept();

                clientCounter++;
                int clientId = clientCounter;

                System.out.println("\n[" + time() + "] CLIENT-" + clientId + " CONNECTED");

                new ClientHandler(s, clientId).start();
            }

        } catch (Exception e) {
            System.out.println("Server Error: " + e);
        }
    }
}

// ⭐ THREAD FOR EACH CLIENT
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
                String line;
                boolean found = false;

                while ((line = file.readLine()) != null) {
                    String data[] = line.split(",");

                    if ((field.equalsIgnoreCase("name") && data[0].equalsIgnoreCase(value)) ||
                            (field.equalsIgnoreCase("enrollment") && data[1].equalsIgnoreCase(value)) ||
                            (field.equalsIgnoreCase("faculty") && data[2].equalsIgnoreCase(value)) ||
                            (field.equalsIgnoreCase("residence") && data[3].equalsIgnoreCase(value)) ||
                            (field.equalsIgnoreCase("course") && data[4].equalsIgnoreCase(value)) ||
                            (field.equalsIgnoreCase("year") && data[5].equalsIgnoreCase(value)) ||
                            (field.equalsIgnoreCase("phone") && data[6].equalsIgnoreCase(value)) ||
                            (field.equalsIgnoreCase("email") && data[7].equalsIgnoreCase(value)) ||
                            (field.equalsIgnoreCase("gender") && data[8].equalsIgnoreCase(value)) ||
                            (field.equalsIgnoreCase("branch") && data[9].equalsIgnoreCase(value))) {
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
                        found = true;
                    }
                }

                if (!found)
                    out.println("No Record Found");

                out.println("END");
                file.close();

                // ⭐ LOG BLOCK END
                System.out.println("[" + Server.time() + "] Response sent to CLIENT-" + clientId);
                System.out.println("[" + Server.time() + "] ==============================");
            }

            s.close();
            System.out.println("[" + Server.time() + "] CLIENT-" + clientId + " DISCONNECTED\n");

        } catch (SocketException e) {
            System.out.println("[" + Server.time() + "] CLIENT-" + clientId + " disconnected unexpectedly.");
        }

        catch (Exception e) {
            System.out.println("[" + Server.time() + "] Error handling CLIENT-" + clientId);
        }
    }
}