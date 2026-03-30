#  Day6 : Binary Tree Farthest Descendants (C++)

This C++ program builds a Binary Tree using recursive input and allows the user to find the **farthest descendants (deepest level nodes)** of any given target node.

---

## Features

- Build Binary Tree using recursive input
- Inorder traversal of the tree
- Search any node in the tree
- Find **farthest descendants** of a given node
- Interactive loop to query multiple nodes

---

## How the Program Works

1. User creates the tree using preorder input.
2. Program prints the inorder traversal.
3. User enters a target node.
4. Program finds and prints the **deepest descendants** of that node.
5. Repeat until user enters `-1`.

---

## Compilation & Run

```bash
g++ BinaryTree.cpp -o tree
./tree