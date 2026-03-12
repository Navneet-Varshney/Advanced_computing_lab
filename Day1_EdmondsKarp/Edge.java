package Day1_EdmondsKarp;

public class Edge {
    int src;
    int dest;
    int capacity;
    int flow;

    public Edge(int s, int d, int c) {
        src = s;
        dest = d;
        capacity = c;
        flow = 0;
    }
}
