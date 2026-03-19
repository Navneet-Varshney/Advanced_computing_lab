# Day5: NFA Creation (Without Epsilon)

This project reads an NFA definition from a text file, simulates input strings, and shows step-by-step execution in a Java Swing GUI with logs and graph view.

## Files

- `NFA.java` : Main Java program (parser + simulator + GUI)
- `file.txt` : Sample NFA input file
- `README.md` : This guide

## NFA Input Format

Use this format in `file.txt`:

```txt
states=q0,q1,q2
alphabet=a,b
start=q0
final=q2
transitions:
q0,a->q0
q0,b->q1
q1,b->q2
q2,b->q2
```

## Important Rules

- Epsilon transitions are **not allowed**.
- `alphabet` must contain all symbols used in transitions.
- `start` must be one of the declared states.
- `final` states must be from declared states.

## How To Compile And Run

From workspace root (`computing_lab`):

```bash
javac Day5_NFA_Creation/NFA.java
java Day5_NFA_Creation.NFA
```

## GUI Features

- Load NFA from file path (or browse)
- Enter input string
- Start simulation
- Next Step (manual step-by-step)
- Auto Run and Stop
- Detailed logs panel
- Graph-style NFA visualization
- Active states highlighted during simulation

## Acceptance Logic

After consuming the full input string, if any current state is a final state, string is ACCEPTED, otherwise REJECTED.
