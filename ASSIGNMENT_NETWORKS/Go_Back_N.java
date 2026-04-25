import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.Scanner;

public class Go_Back_N {

    private static LocalTime currentTime = LocalTime.of(10, 0, 0, 0);
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

    private static String advanceAndGetTime(int ms) {
        currentTime = currentTime.plusNanos((long) ms * 1_000_000);
        return "[" + currentTime.format(TIME_FORMATTER) + "]";
    }

    private static String getWindowString(int base, int windowSize, int totalPackets) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < windowSize; i++) {
            if (base + i < totalPackets) {
                sb.append(base + i).append(i == windowSize - 1 ? "" : " ");
            }
        }
        sb.append("]");
        return sb.toString().replace(" ]", "]");
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.println("===========================================");
        System.out.print("Enter Total Number of Packets : ");
        int totalPackets = sc.nextInt();
        int[] pktBuffer = new int[totalPackets]; 
        int pktCount = 0;
        System.out.print("Enter Size of Window : ");
        int windowSize = sc.nextInt();
        System.out.print("Enter Loss Probability of Packets (in %) : ");
        double dropProbability = sc.nextDouble() / 100.0;
        System.out.print("Enter Timeout Time for a Packet (in ms, e.g., 2000) : ");
        int timeoutMs = sc.nextInt();
        System.out.println("===========================================\n");

        int base = 0;
        int nextSeqNum = 0;
        int expectedSeqNum = 0;
        int lastAcked = -1;
        int totalTransmissions = 0;
        
        int[] ackBuffer = new int[totalPackets]; 
        int ackCount = 0;

        Random rand = new Random();

        while (base < totalPackets) {

            boolean sentAnything = false;
            while (nextSeqNum < base + windowSize && nextSeqNum < totalPackets) {
                System.out.printf("%s Sender: Window %-10s -> send PACKET%d%n", 
                                  advanceAndGetTime(100), getWindowString(base, windowSize, totalPackets), nextSeqNum);
                pktBuffer[pktCount++] = nextSeqNum; 
                nextSeqNum++;
                totalTransmissions++;
                sentAnything = true;
            }
            if (sentAnything) {
                System.out.println(advanceAndGetTime(50) + " Sender: (wait)");
                System.out.println();
            }
            if (pktCount == 0 && ackCount == 0 && base < totalPackets) {
                System.out.println(advanceAndGetTime(timeoutMs) + " TIMER EXPIRED for PACKET" + base + " timeout!");
                System.out.println(advanceAndGetTime(50) + " Sender: GO BACK and retransmit window\n");
                nextSeqNum = base; 
                continue; 
            }

            boolean receiverProcessed = false;
            int currentPktCount = pktCount;
            pktCount = 0; 

            for (int i = 0; i < currentPktCount; i++) {
                int pkt = pktBuffer[i];
                receiverProcessed = true;

                if (rand.nextDouble() < dropProbability) {
                    System.out.println(advanceAndGetTime(150) + "        Network: PACKET" + pkt + " LOSS");
                } else {
                    if (pkt == expectedSeqNum) {
                        System.out.println(advanceAndGetTime(150) + "        Receiver: receive PACKET" + pkt + ", send ACK" + pkt);
                        expectedSeqNum++;
                        lastAcked = pkt;
                        ackBuffer[ackCount++] = pkt;
                    } else {
                        System.out.println(advanceAndGetTime(150) + "        Receiver: receive PACKET" + pkt + ", discard, (re)send ACK" + Math.max(0, expectedSeqNum - 1));
                        ackBuffer[ackCount++] = expectedSeqNum - 1;
                    }
                }
            }
            if (receiverProcessed) System.out.println();

            boolean senderProcessedAcks = false;
            int currentAckCount = ackCount;
            ackCount = 0;

            for (int i = 0; i < currentAckCount; i++) {
                int ack = ackBuffer[i];
                senderProcessedAcks = true;

                if (ack >= base) { 
                    base = ack + 1;
                    System.out.printf("%s Sender: receive ACK%d -> Window slides to %s%n", 
                                      advanceAndGetTime(150), ack, getWindowString(base, windowSize, totalPackets));
                    while (nextSeqNum < base + windowSize && nextSeqNum < totalPackets) {
                        System.out.printf("%s Sender: Window %-10s -> send PACKET%d%n", 
                                          advanceAndGetTime(100), getWindowString(base, windowSize, totalPackets), nextSeqNum);
                        pktBuffer[pktCount++] = nextSeqNum; 
                        nextSeqNum++;
                        totalTransmissions++;
                    }
                } else {
                    System.out.println(advanceAndGetTime(150) + " Sender: receive ACK" + ack + " -> ignore duplicate ACK");
                }
            }
            if (senderProcessedAcks) System.out.println();

        }

        System.out.println(advanceAndGetTime(200) + " ===== ALL PACKETS SUCCESSFULLY DELIVERED =====");
        System.out.println("Total number oftransmissions (including retransmissions) = " + totalTransmissions);
        sc.close();
    }
}