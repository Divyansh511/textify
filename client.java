import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;

public class client {  // Class name matches the filename (client.java)
    Socket socket;
    BufferedReader br;
    PrintWriter out;

    // GUI Components
    private JFrame frame;
    private JTextArea textArea;
    private JTextField textField;
    private JButton sendButton;

    public client() {
        try {
            // Connect to the server
            System.out.println("Sending request to server...");
            socket = new Socket("127.0.0.1", 7777); // Connecting to localhost and port 7777
            System.out.println("Connection established.");

            // Initialize input/output streams
            br = new BufferedReader(new InputStreamReader(socket.getInputStream())); // Input stream
            out = new PrintWriter(socket.getOutputStream(), true); // Output stream

            // Initialize GUI components
            SwingUtilities.invokeLater(this::initializeGUI);

            // Start the reading and writing threads
            startReading();
            startWriting();

        } catch (IOException e) {
            e.printStackTrace();
            displayMessage("Error: " + e.getMessage()); // Show detailed error message in the GUI
        }
    }

    // Method to initialize GUI components
    private void initializeGUI() {
        // Create the main JFrame
        frame = new JFrame("Client Chat");
        frame.setSize(400, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Create a JTextArea for displaying chat messages
        textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setLineWrap(true); 
        textArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(textArea);
        frame.add(scrollPane, BorderLayout.CENTER);

        // Create a JTextField for user to type messages
        textField = new JTextField();
        frame.add(textField, BorderLayout.SOUTH);

        // Create a JButton for sending messages
        sendButton = new JButton("Send");
        frame.add(sendButton, BorderLayout.EAST);

        // Set button action to send the typed message
        sendButton.addActionListener(e -> sendMessage());

        // Set the frame to be visible
        frame.setVisible(true);
    }

    // Method to handle reading messages from the server
    public void startReading() {
        Runnable r1 = () -> {
            System.out.println("Reader started");
            try {
                while (true) {
                    String msg = br.readLine(); // Reading message from the server
                    if (msg == null || msg.equals("exit")) { // If server sends "exit" message
                        displayMessage("Server terminated the chat");
                        socket.close();
                        break;
                    }
                    displayMessage("Server: " + msg); // Display server message in the text area
                }
            } catch (IOException e) {
                displayMessage("Error: " + e.getMessage()); // Show detailed error message
            }
        };
        new Thread(r1).start(); // Starting the reading thread
    }

    // Method to handle sending messages to the server
    public void startWriting() {
        Runnable r2 = () -> {
            System.out.println("Writer started");
            try {
                while (!socket.isClosed()) {
                    String content = textField.getText(); // Get the text typed by the user
                    if (!content.isEmpty()) {
                        out.println(content); // Send message to the server
                        out.flush();
                        textField.setText(""); // Clear the text field after sending the message
                        displayMessage("You: " + content); // Display the sent message in the text area
                        if (content.equals("exit")) {
                            socket.close();
                            break;
                        }
                    }
                }
                System.out.println("Connection is closed");
            } catch (IOException e) {
                displayMessage("Error: " + e.getMessage()); // Show detailed error message
            }
        };
        new Thread(r2).start(); // Starting the writing thread
    }

    // Method to display messages in the JTextArea
    private void displayMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            textArea.append(message + "\n");
        });
    }

    // Send the message when the send button is clicked
    private void sendMessage() {
        String content = textField.getText(); // Get the message from the text field
        if (!content.isEmpty()) {
            out.println(content); // Send message to server
            out.flush();
            textField.setText(""); // Clear the text field after sending the message
            displayMessage("You: " + content); // Display sent message in the text area
            if (content.equals("exit")) {
                try {
                    socket.close(); // Close the socket if "exit" is typed
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) {
        System.out.println("Starting client...");
        new client(); // Starting the client
    }
}
