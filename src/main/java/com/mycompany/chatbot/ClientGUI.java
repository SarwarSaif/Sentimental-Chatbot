package com.mycompany.chatbot;
/**
 *
 * @author saif
 */
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.speech.AudioException;
import javax.speech.Central;
import javax.speech.EngineException;
import javax.speech.synthesis.Synthesizer;
import javax.speech.synthesis.SynthesizerModeDesc;
import javax.speech.synthesis.Voice;
import static javax.swing.JFrame.EXIT_ON_CLOSE;



public class ClientGUI extends JFrame implements ActionListener {

    private static final long serialVersionUID = 1L;

    // will first hold "Username:", later on "Enter message"
    private JLabel label;

    // to hold the Username and later on the messages
    private JTextField tf;

    // to hold the server address an the port number
    private JTextField tfServer, tfPort;

    // to Logout and get the list of the users
    private JButton login, logout;

    // for the conversation
    private JTextArea ta;

    // it is for connection
    private boolean connected;

    // the Client object
    private Client client;

    // the default port number
    private int defaultPort;
    private String defaultHost;


    // Constructor connection receiving a socket number
    ClientGUI(String host, int port) { 

        super("Chat Client");
        defaultPort = port;
        defaultHost = host;
        
        // The NorthPanel with:
        JPanel northPanel = new JPanel(new GridLayout(3,1));

        // the server name anmd the port number
        JPanel serverAndPort = new JPanel(new GridLayout(1,5, 1, 3));

        // the two JTextField with default value for server address and port number
        tfServer = new JTextField(host);
        tfPort = new JTextField("" + port);
        tfPort.setHorizontalAlignment(SwingConstants.RIGHT);

 

        serverAndPort.add(new JLabel("Server Address:  "));
        serverAndPort.add(tfServer);
        serverAndPort.add(new JLabel("Port Number:  "));
        serverAndPort.add(tfPort);
        serverAndPort.add(new JLabel(""));

        // adds the Server an port field to the GUI
        northPanel.add(serverAndPort); 

        // the Label and the TextField
        label = new JLabel("Enter your username below", SwingConstants.CENTER);
        northPanel.add(label);
        
        tf = new JTextField("Anonymous");
        tf.setBackground(Color.WHITE);

        northPanel.add(tf);
        add(northPanel, BorderLayout.NORTH);
 
        // The CenterPanel which is the chat room
        ta = new JTextArea("Welcome to the Chat room\n", 80, 80);
        ta.setBackground(Color.DARK_GRAY);
        ta.setForeground(Color.WHITE); //Add the colors
        
        JPanel centerPanel = new JPanel(new GridLayout(1,1));               
        centerPanel.add(new JScrollPane(ta));
        ta.setEditable(false);
        add(centerPanel, BorderLayout.CENTER);
 
        // the 2 buttons

        login = new JButton("Login");
        login.addActionListener(this);

        logout = new JButton("Logout");
        logout.addActionListener(this);
        logout.setEnabled(false);       // you have to login before being able to logout

        JPanel southPanel = new JPanel();
        southPanel.add(login);
        southPanel.add(logout);
        add(southPanel, BorderLayout.SOUTH);

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(600, 600);
        setVisible(true);
        tf.requestFocus();
 

    }


    // called by the Client to append text in the TextArea
    void append(String str) throws EngineException, InterruptedException, AudioException {

        // ta.append(str);
        String [] mod = str.split(":");
        // System.out.println(mod[0]);
        
        // set property as Kevin Dictionary 
	System.setProperty("freetts.voices", "com.sun.speech.freetts.en.us.cmu_us_kal.KevinVoiceDirectory"); 
	// Register Engine 
	Central.registerEngineCentral ("com.sun.speech.freetts.jsapi.FreeTTSEngineCentral"); 
        // required.setLocale(Locale.ENGLISH);
        Synthesizer synthesizer = Central.createSynthesizer(new SynthesizerModeDesc(Locale.US));	
        // Allocate synthesizer 
	synthesizer.allocate();	 
        // speaks the given text until queue is empty. 
        // textToSpeech(response);
        // Resume Synthesizer 
        synthesizer.resume();	 
        // speaks the given text until queue is empty.
        // System.out.println(mod.length);
        if(mod.length>2)
            synthesizer.speakPlainText(mod[2], null);	
        else
            synthesizer.speakPlainText(str, null);	
            //synthesizer.pause();
            synthesizer.waitEngineState(Synthesizer.QUEUE_NOT_EMPTY); 
            //Deallocate the Synthesizer. 
            //synthesizer.deallocate();  
        
            /*
            * If it is not the connected message then append the conversation between the user and the server
            * else just append the connection message with voice synthesizer            
            */
        if(mod.length>2)
            ta.append("Saif: "+mod[0]+"\n    >>"+mod[1]+"\nRobot: "+mod[2]+"\n    >>"+mod[3]);
        else
            ta.append(str);
        ta.setCaretPosition(ta.getText().length() - 1);

    }

    // called by the GUI is the connection failed
    // we reset our buttons, label, textfield
    // Rollback
    void connectionFailed() {

        login.setEnabled(true);
        logout.setEnabled(false);
        label.setText("Enter your username below");
        tf.setText("Anonymous");

        // reset port number and host name as a construction time
        tfPort.setText("" + defaultPort);
        tfServer.setText(defaultHost);

        // let the user change them
        tfServer.setEditable(false);
        tfPort.setEditable(false);

        // don't react to a <CR> after the username
        tf.removeActionListener(this);
        connected = false;
    }

    /*
    * Button or JTextField clicked
    */
    public void actionPerformed(ActionEvent e) {

        Object o = e.getSource();
        // if it is the Logout button
        if(o == logout) {

            try {
                client.sendMessage(new ChatMessage(ChatMessage.LOGOUT, ""));
            } catch (EngineException ex) {
                Logger.getLogger(ClientGUI.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InterruptedException ex) {
                Logger.getLogger(ClientGUI.class.getName()).log(Level.SEVERE, null, ex);
            } catch (AudioException ex) {
                Logger.getLogger(ClientGUI.class.getName()).log(Level.SEVERE, null, ex);
            }

            return;

        }

        // ok it is coming from the JTextField
        if(connected) {

            try {
                // just have to send the message
            
                client.sendMessage(new ChatMessage(ChatMessage.MESSAGE, tf.getText()));
                
            } catch (EngineException ex) {
                Logger.getLogger(ClientGUI.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InterruptedException ex) {
                Logger.getLogger(ClientGUI.class.getName()).log(Level.SEVERE, null, ex);
            } catch (AudioException ex) {
                Logger.getLogger(ClientGUI.class.getName()).log(Level.SEVERE, null, ex);
            }
            tf.setText("");

            return;

        }
        
        if(o == login) {

            // ok it is a connection request
            String username = tf.getText().trim();
            // empty username ignore it
            if(username.length() == 0)
                return;

            // empty serverAddress ignore it
            String server = tfServer.getText().trim();
            if(server.length() == 0)
                return;

            // empty or invalid port numer, ignore it
            String portNumber = tfPort.getText().trim();
            if(portNumber.length() == 0)
                return;

            int port = 0;
            // Parsed the port number from string to int
            try {
                port = Integer.parseInt(portNumber);
            }
            catch(Exception en) {
                return;   // nothing I can do if port number is not valid
            }

            // try creating a new Client with GUI
            client = new Client(server, port, username, this);
            try {
                // test if we can start the Client               
                if(!client.start())
                    
                    return;
            } catch (EngineException ex) {
                Logger.getLogger(ClientGUI.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InterruptedException ex) {
                Logger.getLogger(ClientGUI.class.getName()).log(Level.SEVERE, null, ex);
            } catch (AudioException ex) {
                Logger.getLogger(ClientGUI.class.getName()).log(Level.SEVERE, null, ex);
            }
            tf.setText("");
            label.setText("Enter your message below");
            connected = true;
            // disable login button
            login.setEnabled(false);
            // enable the 2 buttons
            logout.setEnabled(true);
            
            // disable the Server and Port JTextField
            tfServer.setEditable(false);
            tfPort.setEditable(false);
            // Action listener for when the user enter a message
            tf.addActionListener(this);
        }
    }
 
    // to start the whole thing the server
    public static void main(String[] args) {
        new ClientGUI("localhost", 1500);
    }
}
