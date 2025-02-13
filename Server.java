import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.io.*;

public class Server {
    ServerSocket server;
    Socket socket;

    BufferedReader br;
    PrintWriter out;

    // GUI Components
    private JFrame frame;
    private JTextArea textArea;
    private JTextField textField;
    private JButton sendButton;

    public Server() {
        try {
            // Creating server socket at port 7777
            server = new ServerSocket(7777);
            System.out.println("Server is ready to accept connection");
            System.out.println("Waiting...");
            socket = server.accept(); // Accepting client connection
            br = new BufferedReader(new InputStreamReader(socket.getInputStream())); // Input stream for reading from client
            out = new PrintWriter(socket.getOutputStream(), true); // Output stream to send messages to client

            // Initializing GUI components
            initializeGUI();

            // Start the reading and writing threads
            startReading();
            startWriting();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Method to handle reading from the client
    public void startReading() {
        Runnable r1 = () -> {
            System.out.println("Reader started");
            try {
                while (true) {
                    String msg = br.readLine(); // Reading message from client
                    if (msg == null || msg.equals("exit")) { // If client sends "exit" message
                        displayMessage("Client terminated the chat");
                        socket.close();
                        break;
                    }
                    displayMessage("Client: " + msg); // Display client message in the text area
                }
            } catch (Exception e) {
                displayMessage("Connection is closed");
            }
        };
        new Thread(r1).start(); // Starting the reading thread
    }

    // Method to handle writing to the client
    public void startWriting() {
        // Adding ActionListener for the sendButton to send messages from server to client
        sendButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String content = textField.getText(); // Get message from the text field
                if (content != null && !content.isEmpty()) {
                    out.println(content); // Send message to client
                    out.flush();
                    displayMessage("You: " + content); // Display the sent message in the text area
                    textField.setText(""); // Clear the text field after sending the message
                    if (content.equals("exit")) {
                        try {
                            socket.close(); // Close socket when exit message is sent
                        } catch (IOException ioException) {
                            ioException.printStackTrace();
                        }
                    }
                }
            }
        });
    }

    // Method to initialize GUI components
    private void initializeGUI() {
        // Create JFrame to hold the components
        frame = new JFrame("Server Chat");
        frame.setSize(400, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Create a JTextArea for displaying messages
        textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(textArea);
        frame.add(scrollPane, BorderLayout.CENTER);

        // Create a JTextField for typing messages
        textField = new JTextField();
        frame.add(textField, BorderLayout.SOUTH);

        // Create a JButton for sending messages
        sendButton = new JButton("Send");
        frame.add(sendButton, BorderLayout.EAST);

        // Set the frame to be visible
        frame.setVisible(true);
    }

    // Method to display messages in the JTextArea
    private void displayMessage(String message) {
        textArea.append(message + "\n");
    }

    public static void main(String[] args) {
        System.out.println("This is going to start the server");
        new Server(); // Starting the server
    }
}
