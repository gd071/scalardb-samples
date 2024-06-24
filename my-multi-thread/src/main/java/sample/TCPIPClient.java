package sample;

import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class TCPIPClient extends JFrame {
    private JTextField inputField;
    private JTextArea outputArea;
    private PrintWriter writer;
    private BufferedReader serverReader;

    public TCPIPClient() {
        setTitle("TCP/IP Client");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        initComponents();
        connectToServer();
    }

    private void initComponents() {
        inputField = new JTextField(30);
        JButton sendButton = new JButton("Send");
        outputArea = new JTextArea();
        outputArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(outputArea);

        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(inputField, BorderLayout.CENTER);
        panel.add(sendButton, BorderLayout.EAST);
        

        getContentPane().add(panel, BorderLayout.NORTH);
        getContentPane().add(scrollPane, BorderLayout.CENTER);
    }

    private void connectToServer() {
        String hostname = "127.0.0.1";
        int port = 12345;

        try {
            Socket socket = new Socket(hostname, port);
            outputArea.append("Connected to the server\n");

            OutputStream output = socket.getOutputStream();
            writer = new PrintWriter(output, true);

            InputStream input = socket.getInputStream();
            serverReader = new BufferedReader(new InputStreamReader(input));
            
            // Thread to listen for server messages
            new Thread(() -> {
                try {
                    String response;
                    while ((response = serverReader.readLine()) != null) {
                        outputArea.append("Received from server: " + response + "\n");
                    }
                } catch (IOException e) {
                    outputArea.append("Connection closed\n");
                }
            }).start();

        } catch (IOException ex) {
            ex.printStackTrace();
            outputArea.append("Error connecting to the server\n");
        }
    }

    private void sendMessage() {
        String text = inputField.getText();
        if (text != null && !text.equals("bye")) {
            writer.println(text);
            outputArea.append("Sent to server: " + text + "\n");
            inputField.setText("");
        } else if (text.equals("bye")) {
            writer.println(text);
            outputArea.append("Connection closed by client\n");
            System.exit(0);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new TCPIPClient().setVisible(true);
            }
        });
    }
}
