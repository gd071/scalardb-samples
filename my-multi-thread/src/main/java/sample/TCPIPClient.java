package sample;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

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
        setSize(800, 450);
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
        JLabel idLabel = new JLabel("Your ID");
        idField = new JTextField(30);
        idField.setBounds(340, 280, 200, 50);
        JButton loginButton = createTransparentButton("", 320, 360, 160, 30);

        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                userId = idField.getText();
                if (userId != null && !userId.isEmpty()) {
                    cardLayout.show(mainPanel, "gamePanel");
                }
            }
        });

        BackgroundPanel loginPanel = new BackgroundPanel("src/main/java/sample/images/login.png");
        loginPanel.add(idLabel);
        loginPanel.add(idField);
        loginPanel.add(new JLabel()); // Empty label for spacing
        loginPanel.add(loginButton);

        mainPanel.add(loginPanel, "loginPanel");
    }

    private void initGamePanel() {
        BackgroundPanel gamePanel = new BackgroundPanel("src/main/java/sample/images/game.png");
        gamePanel.setLayout(null); // Absolute layout for positioning buttons

        betField = new JTextField(10);
        JLabel betLabel = new JLabel("BET");

        betLabel.setBounds(50, 10, 50, 30);
        betField.setBounds(110, 10, 100, 30);

        gamePanel.add(betLabel);
        gamePanel.add(betField);

        JButton choButton = createTransparentButton("", 50, 50, 100, 50);
        choButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendGameMessage(1);
            }
        });

        JButton hanButton = createTransparentButton("", 200, 50, 100, 50);
        hanButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendGameMessage(0);
            }
        });

        gamePanel.add(choButton);
        gamePanel.add(hanButton);

        outputArea = new JTextArea();
        outputArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(outputArea);
        scrollPane.setBounds(10, 120, 380, 150);

        gamePanel.add(scrollPane);

        mainPanel.add(gamePanel, "gamePanel");
    }

    private JButton createTransparentButton(String text, int x, int y, int width, int height) {
        JButton button = new JButton(text);
        button.setBounds(x, y, width, height);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setOpaque(false);
        button.setForeground(Color.WHITE);
        return button;
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

class BackgroundPanel extends JPanel {
    private BufferedImage image;

    public BackgroundPanel(String imagePath) {
        try {
            image = ImageIO.read(new File(imagePath));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        setLayout(null); // Absolute layout to position components based on coordinates
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (image != null) {
            g.drawImage(image, 0, 0, getWidth(), getHeight(), this);
        }
    }
}
