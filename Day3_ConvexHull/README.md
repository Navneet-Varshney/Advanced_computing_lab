# Day 3 – Convex Hull Visualization (Graham Scan)

## 📌 Experiment

This experiment demonstrates the **Convex Hull problem** using the **Graham Scan Algorithm** with graphical visualization.

The program allows the user to place points on a canvas and visually observe how the convex hull is constructed step by step.

The implementation uses the **SFML graphics library** to render the points, edges, and algorithm progress.

---

## 🧠 Concepts Used

* Computational Geometry
* Convex Hull Problem
* Graham Scan Algorithm
* Orientation of Points
* Sorting by Polar Angle
* Stack based hull construction

---

## 🖥 Features

* Interactive point placement using mouse clicks
* Step-by-step convex hull construction
* Visual scanning line during algorithm execution
* Reset option to clear all points
* Real-time graphical rendering

---

## 📂 Files

```text
ConvexHull.cpp   → Implementation of Graham Scan with visualization
```

---

## ▶️ Compilation

This program requires the **SFML library**.

Example compilation command:

```
g++ ConvexHull.cpp -o convex_hull -lsfml-graphics -lsfml-window -lsfml-system
```

---

## ▶️ Run

```
./convex_hull
```

---

## 🎯 Objective

The objective of this experiment is to understand how the **Convex Hull of a set of points** can be computed efficiently using the Graham Scan algorithm and to visualize the geometric process interactively.
