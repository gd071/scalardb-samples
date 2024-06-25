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
    // private JTextArea outputArea;
    private PrintWriter writer;
    private BufferedReader serverReader;
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private JLabel coinLabel;
    private JLabel resultCoinLabel;

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
        initResultPanel("src/main/java/sample/images/gameResult/cho_lose.png", "resultCL");
        initResultPanel("src/main/java/sample/images/gameResult/cho_win.png", "resultCW");
        initResultPanel("src/main/java/sample/images/gameResult/han_lose.png", "resultHL");
        initResultPanel("src/main/java/sample/images/gameResult/han_win.png", "resultHW");
        
        getContentPane().add(mainPanel);
    }

    private void initLoginPanel() {
        JLabel idLabel = new JLabel("Your ID");
        idField = new JTextField(30);
        idField.setBounds(340, 280, 200, 50);
        JButton loginButton = createTransparentButton("", 320, 340, 160, 30);

        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                userId = idField.getText();
                if (userId != null && !userId.isEmpty()) {
                    if (writer != null) {
                        writer.println("LOGIN " + userId);
                        cardLayout.show(mainPanel, "gamePanel");
                    } else {
                        // outputArea.append("Error: Not connected to server.\n");
                    }
                }
            }
        });

        BackgroundPanel loginPanel = new BackgroundPanel("src/main/java/sample/images/login.png");
        loginPanel.setLayout(null);
        loginPanel.add(idLabel);
        loginPanel.add(idField);
        loginPanel.add(loginButton);

        mainPanel.add(loginPanel, "loginPanel");
    }

    private void initGamePanel() {
        BackgroundPanel gamePanel = new BackgroundPanel("src/main/java/sample/images/game.png");
        gamePanel.setLayout(null); // Absolute layout for positioning buttons

        betField = new JTextField(10);
        betField.setBounds(330, 330, 200, 40);
        gamePanel.add(betField);

        coinLabel = new JLabel("Your Coin: x"); // coinLabel の初期化
        coinLabel.setBounds(220, 15, 200, 40);
        gamePanel.add(coinLabel);

        JButton choButton = createTransparentButton("", 0, 100, 200, 250);
        choButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendGameMessage(0);
            }
        });

        JButton hanButton = createTransparentButton("", 600, 100, 200, 250);
        hanButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendGameMessage(1);
            }
        });

        gamePanel.add(choButton);
        gamePanel.add(hanButton);

        // outputArea = new JTextArea();
        // outputArea.setEditable(false);
        // JScrollPane scrollPane = new JScrollPane(outputArea);
        // scrollPane.setBounds(10, 120, 380, 150);
        // gamePanel.add(scrollPane);

        mainPanel.add(gamePanel, "gamePanel");
    }

    private void initResultPanel(String imagePath, String panelName) {
        resultCoinLabel = new JLabel("Your Coin");
        resultCoinLabel.setBounds(350, 265, 200, 50);
        JButton retryButton = createTransparentButton("", 280, 325, 100, 30);
        retryButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                userId = idField.getText();
                if (userId != null && !userId.isEmpty()) {
                    cardLayout.show(mainPanel, "gamePanel");
                }
            }
        });

        JButton exitButton = createTransparentButton("", 420, 325, 100, 30);
        exitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });

        BackgroundPanel resultPanel = new BackgroundPanel(imagePath);
        resultPanel.setLayout(null);
        resultPanel.add(resultCoinLabel);
        resultPanel.add(retryButton);
        resultPanel.add(exitButton);

        mainPanel.add(resultPanel, panelName);
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
            // outputArea.append("Connected to the server\n");

            OutputStream output = socket.getOutputStream();
            writer = new PrintWriter(output, true);

            InputStream input = socket.getInputStream();
            serverReader = new BufferedReader(new InputStreamReader(input));
            
            // Thread to listen for server messages
            new Thread(() -> {
                try {
                    String response;
                    while ((response = serverReader.readLine()) != null) {
                        String[] parts = response.split(" ");
                        if (parts[0].equals("LOGIN")) {
                            if (parts[1].equals("FAIL")) {
                                cardLayout.show(mainPanel, "loginPanel");
                            } else {
                                coinLabel.setText("Your Coin: " + parts[2]);
                                resultCoinLabel.setText("Your Coin: " + parts[2]);
                            }
                        }

                        if (parts[0].equals("GAME")) {
                            if (parts[1].equals("WIN") && (Integer.parseInt(parts[2]) + Integer.parseInt(parts[3])) % 2 == 0 ) {
                                coinLabel.setText("Your Coin: " + parts[4]);
                                resultCoinLabel.setText("Your Coin: " + parts[4]);
                                cardLayout.show(mainPanel, "resultCW");
                            } else if (parts[1].equals("WIN") && (Integer.parseInt(parts[2]) + Integer.parseInt(parts[3])) % 2 == 1 ) {
                                coinLabel.setText("Your Coin: " + parts[4]);
                                resultCoinLabel.setText("Your Coin: " + parts[4]);
                                cardLayout.show(mainPanel, "resultHW");
                            } else if (parts[1].equals("LOSE") && (Integer.parseInt(parts[2])+ Integer.parseInt(parts[3])) % 2 == 0 ) {
                                coinLabel.setText("Your Coin: " + parts[4]);
                                resultCoinLabel.setText("Your Coin: " + parts[4]);
                                cardLayout.show(mainPanel, "resultCL");
                            } else if (parts[1].equals("LOSE") && (Integer.parseInt(parts[2])+ Integer.parseInt(parts[3])) % 2 == 1 ) {
                                coinLabel.setText("Your Coin: " + parts[4]);
                                resultCoinLabel.setText("Your Coin: " + parts[4]);
                                cardLayout.show(mainPanel, "resultHL");
                            }
                        }

                        // outputArea.append("Received from server: " + response + "\n");
                    }
                } catch (IOException e) {
                    // ex.printStackTrace();
                    // outputArea.append("Connection closed\n");
                }
            }).start();

        } catch (IOException ex) {
            // ex.printStackTrace();
            // outputArea.append("Error connecting to the server\n");
        }
    }

    private void sendGameMessage(int choice) {
        String coin = betField.getText();
        if (coin != null && !coin.isEmpty()) {
            String message = "GAME " + userId + " " + coin + " " + choice;
            writer.println(message);
            // outputArea.append("Sent to server: " + message + "\n");
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
            // ex.printStackTrace();
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
