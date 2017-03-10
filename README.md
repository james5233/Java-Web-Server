# Sandip Nath Tiwari - 20162032

## General Architecture
```
                                 +--------------+
                                 | File Handler |
                                 +--------------+
                                      *  |
                                      *  |
                              spawns  *  | comm.
                               per    *  | over
                              request *  | I/O
                                      *  | streams
                                      *  |
                                      *  |
+--------+     over HTTP/1.0      +------------+  spawns  +-----------------+
| Client | <--------------------> | Web Server |**********| Session Manager |
+--------+                        +------------+          +-----------------+
                                      *  |               /
                                      *  | comm.        /
                                      *  | over        / 
                              spawns  *  | I/O        /
                               per    *  | streams   / via a
                              request *  |          / Transaction
                                      *  |         / Class
                                      *  |        /
                                  +--------------+                         ,,,,,,,,,,,,,
                                  | Java Handler |<------------------------| Stock.jar |
                                  +--------------+    Code gets loaded     `````````````
                                                        on demand and 
                                                          Executed

```
## Usage

1. Make
```
make all
```
2. Start Server
```
java MyServer
```
   (Note: If the server declines to start due to port conflict,
    it might be that a previous instance of session-manager is still
    running. Kill it and then retry starting the server.)

3. Start Client with host, port, and username as paramters
```
java MyClient localhost 8080 "James Wilkins Booth"
```
4. Read Client usage and perform transactions

## Files
.
├── Readme.txt                   (README FILE)
└── src
    ├── config                   (HANDLER NAME SPECIFICATION)
    ├── file_handler.class       (FILE-REQUESTS HANDLER)
    ├── file_handler.java              .
    ├── java_handler.class       (JAVA-REQUESTS HANDLER)
    ├── java_handler.java              .
    ├── Makefile                 (MAKEFILE)
    ├── MyClient.class           (CLIENT PROGRAM)
    ├── MyClient.java                  .
    ├── MyServer.class           (SERVER PROGRAM)
    ├── MyServer.java                  .
    ├── sample_audio.mp3         (SAMPLE FILES)
    ├── sample_image.jpg               .
    ├── sample_video.mp4               .
    ├── session_manager.class    (SESSION MANAGER)
    ├── session_manager.java
    ├── sessmgr.conf             (SESSION MANAGER LOCAL LISTEN IP/PORT)
    ├── sess_transactor.class    (SESSION-TRANSACTIONS INTERMEDIARY)
    ├── sess_transactor.java
    ├── Stock.jar                (STOCKS.JAR - APPLICATION SPEC)
    └── Stock_src                (SOURCE CODE FOR STOCK.JAR)
        ├── mainfest.mf                       .
        ├── Stock.class                       .
        └── Stock.java                        .

2 directories, 23 files


## Challenges Faced
--> Client/Server Comm. using HTTP Protocol, parsing header and body
--> Getting Reflections right
--> Java
