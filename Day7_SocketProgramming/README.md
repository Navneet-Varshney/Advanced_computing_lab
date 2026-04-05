# Day 7 - Socket Programming

## Project Overview
This project demonstrates **Client-Server Socket Communication** in Java. The server hosts a CSV database of student records and allows multiple clients to query it concurrently.

## Features
- **Server** (`Server.java`): 
  - Listens on PORT 5000
  - Loads student records from `students.csv`
  - Handles multiple client connections simultaneously using threading
  - Logs all connections with timestamps

- **Client** (`Client.java`):
  - Connects to the server
  - Allows searching student records by:
    - Name
    - Enrollment Number
    - Gender
    - Branch
  - Validates user input for field selection

## Files
- `Server.java` - Server implementation
- `Client.java` - Client implementation
- `students.csv` - Database of student records

## How to Run

### Step 1: Start the Server
```bash
javac Day7_SocketProgramming/Server.java
java Day7_SocketProgramming.Server
```
The server will start and wait for client connections on port 5000.

### Step 2: Connect a Client (in a new terminal)
```bash
javac Day7_SocketProgramming/Client.java
java Day7_SocketProgramming.Client
```

The client will prompt you to:
1. Select a search field (name, enrollment, gender, or branch)
2. Enter the search value
3. Receive matching student records from the server

## Example Usage
```
Search by: name / enrollment / gender / branch
Field: name
Value: John
```

## Technical Details
- **Protocol**: TCP/IP Sockets
- **Concurrency**: Multi-threaded server handling multiple clients
- **Language**: Java
- **Port**: 5000

## Key Concepts Demonstrated
1. Socket Programming (ServerSocket, Socket)
2. Client-Server Architecture
3. Multi-threading
4. File I/O (CSV reading)
5. Network Communication
6. Timestamp logging
