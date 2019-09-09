package com.mycompany.chatbot;
/**
 *
 * @author saif
 */
import java.net.*;
import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.speech.AudioException;
import javax.speech.EngineException;


public class Client  {

    private ObjectInputStream sInput;       // to read from the socket
    private ObjectOutputStream sOutput;     // to write on the socket
    private Socket socket;
 
    // For the GUI
    private ClientGUI cg;
    // the server, the port and the username
    private String server, username;
    private int port;
    /*
     *  Constructor called by console mode
     *  server: the server address
     *  port: the port number
     *  username: the username
     */
    Client(String server, int port, String username) {
        // which calls the common constructor with the GUI set to null
        this(server, port, username, null);
    }
    /*
     * Constructor call when used from a GUI
     * in console mode the ClienGUI parameter is null
     */

    Client(String server, int port, String username, ClientGUI cg) {
        this.server = server;
        this.port = port;
        this.username = username;
        // save if we are in GUI mode or not
        this.cg = cg;
    }
     /*
     * To start the dialog
     */
    public boolean start() throws EngineException, InterruptedException, AudioException {

        // try to connect to the server
        try {
            socket = new Socket(server, port);
        }
        // if it failed catch exception
        catch(Exception ec) {
            display("Error connectiong to server:" + ec);
            return false;
        }
         
        String msg = "Connection accepted " + socket.getInetAddress() + ":" + socket.getPort();
        display(msg);     

        /* Creating both Data Stream */
        try
        {
            sInput  = new ObjectInputStream(socket.getInputStream());
            sOutput = new ObjectOutputStream(socket.getOutputStream());
        }

        catch (IOException eIO) {
            display("Exception creating new Input/output Streams: " + eIO);
            return false;
        }

        // creates the Thread to listen from the server
        new ListenFromServer().start();
        // Send our username to the server this is the only message that we

        // will send as a String. All other messages will be ChatMessage objects

        try

        {

            sOutput.writeObject(username);

        }

        catch (IOException eIO) {

            display("Exception doing login : " + eIO);

            disconnect();

            return false;

        }

        // success we inform the caller that it worked

        return true;

    }

 
    /*
     * * * * * * To send a message to the console or the GUI
     */
    private void display(String msg) throws EngineException, InterruptedException, AudioException {

        if(cg == null)

            System.out.println(msg);      // println in console mode

        else
            //If the message is not null then it send to the Client Gui
            cg.append(msg+"\n");      // append to the ClientGUI JTextArea

    }

    /*
     * * * * * To send a message to the server
     */
    void sendMessage(ChatMessage msg) throws EngineException, InterruptedException, AudioException {

        try {
            sOutput.writeObject(msg);
        }
        catch(IOException e) {
            display("Exception writing to server: " + e);
        }
    } 

    /*
     * When something goes wrong
     * we have to close the Input/Output streams and disconnect not much to do in the catch clause
     */

    private void disconnect() {
        try {
            if(sInput != null) sInput.close();
        }
        catch(Exception e) {} 
        try {
            if(sOutput != null) sOutput.close();
        }
        catch(Exception e) {} 
        try{
            if(socket != null) socket.close();
        }
        catch(Exception e) {} 
        // Tell user about connection failure 
        if(cg != null)
            cg.connectionFailed();
    }    

    /*
     * This class waits for the message from the server and append them to the JTextArea
     */
    class ListenFromServer extends Thread {
        public void run() {
            
            while(true) {
                try {
                    String msg = (String) sInput.readObject();
                    // add the message to the JTextArea
                    if(cg != null) {
                        cg.append(msg);
                    }
                }

                catch(IOException e) {

                    try {
                        display("Server has close the connection: " + e);
                    } catch (EngineException ex) {
                        Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (AudioException ex) {
                        Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
                    }

                    if(cg != null)
                        //Rollback to log in position because of the failure
                        cg.connectionFailed();
                    break;

                }

                catch(ClassNotFoundException e2) {

                } catch (EngineException ex) {
                    Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
                } catch (InterruptedException ex) {
                    Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
                } catch (AudioException ex) {
                    Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
                }

            }

        }

    }

}


