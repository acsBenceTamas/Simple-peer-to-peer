package com.codecool.peer_to_peer;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerClient {
    public static void main(String[] args) {
//        ServerThread serverThread = new ServerThread(Integer.parseInt(args[0]));
//        serverThread.start();
        ClientConnection clientConnection = new ClientConnection("localhost", Integer.parseInt(args[1]));
        clientConnection.start();
    }

    private static class ServerThread implements Runnable {
        int port;
        Thread thread;

        ServerThread(int port) {
            this.port = port;
        }

        @Override
        public void run() {
            ExecutorService pool = Executors.newFixedThreadPool(1);
            System.out.println("Starting server now");
            try (ServerSocket listener = new ServerSocket(port)) {
                System.out.println("Running ServerClient");
                while (true) {
                    System.out.println("Before pool.execute");
                    pool.execute(new ServerHandler(listener.accept()));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void start () {
            if (thread == null) {
                thread = new Thread (this, "Server");
                thread.start ();
            }
        }
    }

    private static class ServerHandler implements Runnable {
        private Socket socket;
        private Scanner in;
        private PrintWriter out;


        public ServerHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            System.out.println("Inside pool.execute");
            try {
                System.out.println("Running Server");
                in = new Scanner(socket.getInputStream());
                out = new PrintWriter(socket.getOutputStream(), true);

                while(true) {
                    System.out.println("Sending Input request");
                    out.println("GET_ANY_INPUT");
                    System.out.println("Input Request Sent");
                    System.out.println("Awaiting response");
                    String input = in.nextLine();
                    System.out.println("Response received:");
                    System.out.println(input);
                    out.println("INPUT_GOT");

                    if (input == null) {
                        System.out.println("Response is empty");
                        return;
                    }
                }
            } catch (Exception e) {
                System.out.println(e);
            }
        }
    }

    private static class ClientConnection implements Runnable {
        Thread thread;
        String serverAddress;
        int serverPort;
        Scanner in;
        PrintWriter out;

        public ClientConnection(String serverAddress, int serverPort) {
            System.out.println("Connection Created");
            this.serverAddress = serverAddress;
            this.serverPort = serverPort;
        }

        @Override
        public void run() {
            System.out.println("Running client");
            boolean running = true;
            while (running) {
                try (Socket socket = new Socket(serverAddress, 56969);){
                    System.out.println("Socket established");
                    in = new Scanner(socket.getInputStream());
                    out = new PrintWriter(socket.getOutputStream(), true);
                    int i = 1;
                    while (in.hasNextLine() && running) {
                        System.out.println("Try: " + i);
                        i++;
                        String line = in.nextLine();
                        System.out.println(line);
                        if (line.startsWith("GET_ANY_INPUT")) {
                            System.out.println("Input anything");
                            Scanner scanner = new Scanner(System.in);
                            String response = scanner.nextLine();
                            System.out.println("Your response was: " + response);
                            out.println("RESPONSE " + response);
                        } else if (line.startsWith("INPUT_GOT")){
                            System.out.println("Server received the response");
//                            running = false;
//                            System.out.println("Running set to false");
                        }
                        System.out.println("End of inner while");
                    }
                    System.out.println("After inner while");
                } catch (ConnectException e) {
                    try {
                        e.printStackTrace();
                        System.out.println("Connection attempt failed. Retrying in 2000 milliseconds");
                        Thread.sleep(2000);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("Client Connection Done");
        }

        public void start () {
            if (thread == null) {
                thread = new Thread (this, "Client");
                thread.start ();
            }
        }
    }
}
