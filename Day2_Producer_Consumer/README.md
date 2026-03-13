# Day 2 – Producer Consumer Problem

This experiment demonstrates the classic **Producer–Consumer synchronization problem** using:

- Shared Memory (mmap)
- POSIX Semaphores
- Multiple Processes using fork()

## Concepts Used

- Process Synchronization
- Critical Section
- Mutual Exclusion
- Semaphore operations (wait / signal)
- Circular Buffer

## Compilation

g++ producer_consumer.cpp -o producer_consumer -pthread

## Run

./producer_consumer