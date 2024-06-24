package sample;

import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class TCPIPClient extends JFrame {
    private JTextField idField;
    private JTextField betField;
    private String userId;
    private JTextArea outputArea;
    private PrintWriter writer;
    private BufferedReader serverReader;
    private CardLayout cardLayout;
    private JPanel mainPanel;

    public TCPIPClient() {
        setTitle("TCP/IP Client");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        initComponents();
        connectToServer();
    }

    private void initComponents() {
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        
        initLoginPanel();
        initGamePanel();
        
        getContentPane().add(mainPanel);
    }

    private void initLoginPanel() {
        JPanel loginPanel = new JPanel(new GridLayout(2, 2));
        JLabel idLabel = new JLabel("Your ID");
        idField = new JTextField(30);
        JButton loginButton = new JButton("LOGIN");

        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                userId = idField.getText();
                if (userId != null && !userId.isEmpty()) {
                    cardLayout.show(mainPanel, "gamePanel");
                }
            }
        });

        loginPanel.add(idLabel);
        loginPanel.add(idField);
        loginPanel.add(new JLabel()); // Empty label for spacing
        loginPanel.add(loginButton);

        mainPanel.add(loginPanel, "loginPanel");
    }

    private void initGamePanel() {
        JPanel gamePanel = new JPanel(new BorderLayout());
        betField = new JTextField(10);
        JLabel betLabel = new JLabel("BET");
        JButton choButton = new JButton("丁");
        JButton hanButton = new JButton("半");
        
        choButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendGameMessage(1);
            }
        });

        hanButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendGameMessage(0);
            }
        });

        JPanel inputPanel = new JPanel();
        inputPanel.add(betLabel);
        inputPanel.add(betField);

        JPanel buttonPanel = new JPanel(new GridLayout(1, 2));
        buttonPanel.add(choButton);
        buttonPanel.add(hanButton);

        gamePanel.add(inputPanel, BorderLayout.NORTH);
        gamePanel.add(buttonPanel, BorderLayout.CENTER);

        outputArea = new JTextArea();
        outputArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(outputArea);

        gamePanel.add(scrollPane, BorderLayout.SOUTH);

        mainPanel.add(gamePanel, "gamePanel");
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

    private void sendGameMessage(int choice) {
        String coin = betField.getText();
        if (coin != null && !coin.isEmpty()) {
            String message = "GAME " + userId + " " + coin + " " + choice;
            writer.println(message);
            outputArea.append("Sent to server: " + message + "\n");
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
