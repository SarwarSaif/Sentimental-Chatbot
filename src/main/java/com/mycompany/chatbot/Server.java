package com.mycompany.chatbot;

/**
 *
 * @author saif
 */
import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;

/*
 * The server run as a console application
 */

public class Server {

    // a unique ID for each connection
    private static int uniqueId;

    // an ArrayList to keep the list of the Client
    private ArrayList<ClientThread> al;

    private SimpleDateFormat sdf;

    // the port number to listen for connection
    private int port;

    // the boolean that will be turned of to stop the server
    private boolean keepGoing;


    /*
     *  server constructor that receive the port to listen to for connection as parameter
     *  in console
     */

    public Server(int port) {

        this.port = port;
        // to display hh:mm:ss

        sdf = new SimpleDateFormat("HH:mm:ss");

        // ArrayList for the Client list

        al = new ArrayList<ClientThread>();

    }

  

    public void start() {

        keepGoing = true;

        /* create socket server and wait for connection requests */
        try
        {

            // the socket used by the server
            ServerSocket serverSocket = new ServerSocket(port);

            // infinite loop to wait for connections
            while(keepGoing)
            {

                // format message saying we are waiting
                display("Server waiting for Clients on port " + port + ".");

                Socket socket = serverSocket.accept();      // accept connection
                // if I was asked to stop
                if(!keepGoing)
                    break;
                ClientThread t = new ClientThread(socket);  // make a thread of it
                al.add(t);                                  // save it in the ArrayList
                t.start();

            }

            // I was asked to stop
            try {

                serverSocket.close();
                for(int i = 0; i < al.size(); ++i) {

                    ClientThread tc = al.get(i);

                    try {
                    tc.sInput.close();
                    tc.sOutput.close();
                    tc.socket.close();
                    }

                    catch(IOException ioE) {
                        // not much I can do
                    }

                }

            }

            catch(Exception e) {

                display("Exception closing the server and clients: " + e);

            }

        }

        // something went bad
        catch (IOException e) {
            String msg = sdf.format(new Date()) + " Exception on new ServerSocket: " + e + "\n";
            display(msg);

        }

    }      

    /*
     * For the GUI to stop the server
     */

    protected void stop() {

        keepGoing = false;
        // connect to myself as Client to exit statement
        // Socket socket = serverSocket.accept();

        try {
            new Socket("localhost", port);
        }

        catch(Exception e) {
            
        }

    }

    /*
     * Display an event (not a message) to the console or the GUI
     */

    private void display(String msg) {

        String time = sdf.format(new Date()) + " " + msg;
        System.out.println(time);

    }


    // for a client who logoff using the LOGOUT message
    synchronized void remove(int id) {

        // scan the array list until we found the Id
        for(int i = 0; i < al.size(); ++i) {
            ClientThread ct = al.get(i);
            // found it
            if(ct.id == id) {
                al.remove(i);
                return;
            }

        }

    }

     

    /*
     *  To run as a console application just open a console window and:
     * > java Server
     * > java Server portNumber
     * If the port number is not specified 1500 is used
     */

    public static void main(String[] args) {

        // start server on port 1500 unless a PortNumber is specified
        int portNumber = 1500;
        
        // create a server object and start it
        Server server = new Server(portNumber);
        server.start();

    }

 

    /** One instance of this thread will run for each client */
    class ClientThread extends Thread {

        // the socket where to listen/talk
        Socket socket;
        ObjectInputStream sInput;
        ObjectOutputStream sOutput;

        // my unique id (easier for deconnection)
        int id;

        // the Username of the Client
        String username;

        // the only type of message a will receive
        ChatMessage cm;

        // the date I connect
        String date;

        // Constructore
        ClientThread(Socket socket) {

            // a unique id
            id = ++uniqueId;

            this.socket = socket;
            /* Creating both Data Stream */
            System.out.println("Thread trying to create Object Input/Output Streams");

            try
            {
                // create output first
                sOutput = new ObjectOutputStream(socket.getOutputStream());
                sInput  = new ObjectInputStream(socket.getInputStream());

                // read the username
                username = (String) sInput.readObject();
                display(username + " just connected.");

            }

            catch (IOException e) {
                display("Exception creating new Input/output Streams: " + e);

                return;
            }

            // have to catch ClassNotFoundException
            // but I read a String, I am sure it will work

            catch (ClassNotFoundException e) {
            }

            date = new Date().toString() + "\n";

        }

 

        // what will run forever
        public void run() {

            // to loop until LOGOUT
            boolean keepGoing = true;
            while(keepGoing) {

                // read a String (which is an object)
                try {

                    cm = (ChatMessage) sInput.readObject();

                }

                catch (IOException e) {

                    display(username + " Exception reading Streams: " + e);

                    break;             

                }

                catch(ClassNotFoundException e2) {

                    break;

                }

                // the messaage part of the ChatMessage
                String message = cm.getMessage();

                // Switch on the type of message receive
                switch(cm.getType()) {

                case ChatMessage.MESSAGE:

                    //broadcast(username + ": " + message);
                    
                    /* Here we'll add the chatbot
                    1111111111111111111111111111111111111111111111111
                    1111111111111111111111111111111111111111111111111111
                    11111111111111111111111111111111111111111
                    */
                    Chatbot myBot = new Chatbot();
                    String response = myBot.getResponse(message);
                    writeMsg(message+"\n"+response+"\n");
                    
                    break;

                case ChatMessage.LOGOUT:

                    display(username + " disconnected with a LOGOUT message.");

                    keepGoing = false;

                    break;
              
                }

            }

            // remove myself from the arrayList containing the list of the
            // connected Clients

            remove(id);
            close();

        }

         

        // try to close everything
        private void close() {

            // try to close the connection

            try {

                if(sOutput != null) sOutput.close();

            }

            catch(Exception e) {}

            try {

                if(sInput != null) sInput.close();

            }

            catch(Exception e) {};

            try {

                if(socket != null) socket.close();

            }

            catch (Exception e) {}

        }

 

        /*
         * Write a String to the Client output stream
         */

        private boolean writeMsg(String msg) {

            // if Client is still connected send the message to it
            if(!socket.isConnected()) {

                close();

                return false;

            }

            // write the message to the stream
            try {
                sOutput.writeObject(msg);
            }

            // if an error occurs, do not abort just inform the user
            catch(IOException e) {

                display("Error sending message to " + username);

                display(e.toString());

            }

            return true;

        }

    }

}

