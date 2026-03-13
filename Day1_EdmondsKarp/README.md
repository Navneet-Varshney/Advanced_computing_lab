# Day 1 – Edmonds–Karp Algorithm

## 📌 Experiment

Implementation and visualization of the **Edmonds–Karp Algorithm** for computing the **Maximum Flow** in a flow network.

The Edmonds–Karp algorithm is a specific implementation of the Ford–Fulkerson method that uses **Breadth First Search (BFS)** to find augmenting paths in the residual graph.

---

## 🧠 Key Concepts

* Maximum Flow Problem
* Residual Graph
* Breadth First Search (BFS)
* Augmenting Paths
* Flow Networks

---

## 🛠 Implementation Details

The program is implemented using **Java** and structured into multiple classes for better modularity.

**Files included:**

```
EdmondsKarp.java   → Main algorithm implementation
Node.java          → Representation of graph nodes
Edge.java          → Representation of graph edges
DrawPanel.java     → Graph visualization component
```

---

## ▶️ Compilation and Execution

Compile the program:

```
javac *.java
```

Run the main program:

```
java EdmondsKarp
```

---

## 🎯 Objective

The objective of this experiment is to understand how maximum flow algorithms work and how BFS can be used to systematically find augmenting paths in a flow network.
