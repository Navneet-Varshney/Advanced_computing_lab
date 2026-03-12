package Day1_EdmondsKarp;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class DrawPanel extends JPanel {

    ArrayList<Node> nodes;
    ArrayList<Edge> edges;
    ArrayList<Edge> highlightedEdges;
    int source;
    int sink;
    boolean isConverted;

    public DrawPanel(ArrayList<Node> nodes, ArrayList<Edge> edges,
                     ArrayList<Edge> highlightedEdges,
                     int source, int sink, boolean isConverted) {
        this.nodes = nodes;
        this.edges = edges;
        this.highlightedEdges = highlightedEdges;
        this.source = source;
        this.sink = sink;
        this.isConverted = isConverted;
        setBackground(Color.BLACK);
    }

    public void updateState(int source, int sink, boolean isConverted) {
        this.source = source;
        this.sink = sink;
        this.isConverted = isConverted;
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        int r = 25;

        for (Edge e : edges) {
            Node n1 = nodes.get(e.src);
            Node n2 = nodes.get(e.dest);

            if (highlightedEdges.contains(e)) {
                g2.setColor(Color.YELLOW);
                g2.setStroke(new BasicStroke(4));
            } else {
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(2));
            }

            if (!isConverted) {
                g2.drawLine(n1.x, n1.y, n2.x, n2.y);
                drawEdgeText(g2, n1, n2, String.valueOf(e.capacity), 20);
            } else {
                drawOffsetArrow(g2, n1, n2, 25, 8);
                String flowText = Math.max(0, e.flow) + " / " + e.capacity;
                drawEdgeText(g2, n1, n2, flowText, 25);
            }
        }

        for (Node n : nodes) {

            if (n.id == source)
                g2.setColor(Color.ORANGE);
            else if (n.id == sink)
                g2.setColor(Color.RED);
            else
                g2.setColor(new Color(30, 144, 255));

            g2.fillOval(n.x - r, n.y - r, 2 * r, 2 * r);
            g2.setColor(Color.WHITE);
            g2.drawOval(n.x - r, n.y - r, 2 * r, 2 * r);

            g2.setFont(new Font("Segoe UI", Font.BOLD, 18));
            String id = String.valueOf(n.id);

            g2.drawString(id,
                    n.x - g2.getFontMetrics().stringWidth(id) / 2,
                    n.y + 7);
        }
    }

    private void drawOffsetArrow(Graphics2D g2, Node n1, Node n2,
                                 int r, int shift) {

        double angle = Math.atan2(n2.y - n1.y, n2.x - n1.x);
        double pAngle = angle - Math.PI / 2;

        int dx = (int) (shift * Math.cos(pAngle));
        int dy = (int) (shift * Math.sin(pAngle));

        int sx = (int) (n1.x + dx + r * Math.cos(angle));
        int sy = (int) (n1.y + dy + r * Math.sin(angle));

        int ex = (int) (n2.x + dx - r * Math.cos(angle));
        int ey = (int) (n2.y + dy - r * Math.sin(angle));

        g2.drawLine(sx, sy, ex, ey);

        int aSize = 12;

        g2.drawLine(ex, ey,
                (int) (ex - aSize * Math.cos(angle - Math.PI / 7)),
                (int) (ey - aSize * Math.sin(angle - Math.PI / 7)));

        g2.drawLine(ex, ey,
                (int) (ex - aSize * Math.cos(angle + Math.PI / 7)),
                (int) (ey - aSize * Math.sin(angle + Math.PI / 7)));
    }

    private void drawEdgeText(Graphics2D g2, Node n1, Node n2,
                              String text, int offset) {

        int midX = (n1.x + n2.x) / 2;
        int midY = (n1.y + n2.y) / 2;

        double angle = Math.atan2(n2.y - n1.y, n2.x - n1.x);

        int tx = midX + (int) (offset * Math.sin(angle));
        int ty = midY - (int) (offset * Math.cos(angle));

        g2.setFont(new Font("Segoe UI", Font.BOLD, 18));
        g2.setColor(new Color(50, 255, 50));

        FontMetrics fm = g2.getFontMetrics();

        g2.drawString(text,
                tx - fm.stringWidth(text) / 2,
                ty + fm.getAscent() / 4);
    }
}
