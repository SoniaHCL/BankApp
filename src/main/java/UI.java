import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;

public class UI { 

    static Account account = new Account();

    // High-Fidelity Color Palette & Geometry
    private static final Color GRADIENT_TOP = new Color(133, 79, 196);   // Vibrant Violet
    private static final Color GRADIENT_BOTTOM = new Color(74, 34, 142); // Deep Royal Purple
    private static final Color ACCENT_ORANGE = new Color(255, 158, 11);  // Accent Color
    private static final Color WHITE_CARD = new Color(255, 255, 255);
    private static final Color MUTED_PURPLE_TEXT = new Color(185, 153, 224);
    private static final int CORNER_RADIUS = 16;
    private static final int PADDING = 20;

    // Currency configuration locked to USD
    private static final String DOLLAR_SYMBOL = "$"; 

    public static void main(String[] args) {
        JFrame frame = new JFrame("iLedger Dashboard");
        frame.setSize(960, 720); 
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // --- ICON RESOLVER (100% Fail-Safe Dynamic Vector Renderer) ---
        try {
            BufferedImage dynamicIcon = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = dynamicIcon.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            g.setColor(ACCENT_ORANGE);
            g.fillOval(4, 4, 56, 56);
            
            g.setColor(Color.WHITE);
            g.setStroke(new BasicStroke(5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.drawLine(24, 18, 24, 44); 
            g.drawLine(24, 44, 42, 44); 
            g.drawLine(24, 31, 36, 31); 
            g.dispose();
            
            frame.setIconImage(dynamicIcon);
        } catch (Exception ex) {
            System.err.println("Note: Native OS window icon initialization bypassed.");
        }

        // --- Custom Gradient Background Panel ---
        JPanel backgroundPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, GRADIENT_TOP, 0, getHeight(), GRADIENT_BOTTOM);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        backgroundPanel.setLayout(null);
        frame.setContentPane(backgroundPanel);

        // --- MAIN APPLICATION HEADER ---
        JLabel titleLabel = new JLabel("iLedger DASHBOARD");
        titleLabel.setBounds(PADDING, 15, 300, 30);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(Color.WHITE);
        backgroundPanel.add(titleLabel);

        // --- 1. LEFT SIDE CONTENT: Balance & Analytics ---
        JPanel leftColumnPanel = new JPanel();
        leftColumnPanel.setBounds(PADDING, 60, 440, 580);
        leftColumnPanel.setOpaque(false);
        leftColumnPanel.setLayout(null);
        backgroundPanel.add(leftColumnPanel);

        // Balance Section
        JLabel balanceTitle = new JLabel("Current Balance");
        balanceTitle.setBounds(10, 10, 150, 20);
        balanceTitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        balanceTitle.setForeground(MUTED_PURPLE_TEXT);
        leftColumnPanel.add(balanceTitle);

        JLabel balanceLabel = new JLabel("Stream: " + DOLLAR_SYMBOL + "0.00");
        balanceLabel.setBounds(10, 35, 420, 45);
        balanceLabel.setFont(new Font("Segoe UI", Font.BOLD, 36));
        balanceLabel.setForeground(Color.WHITE);
        leftColumnPanel.add(balanceLabel);

        // Analytics Trend Chart Card
        JPanel chartCard = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                g2d.setColor(WHITE_CARD);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), CORNER_RADIUS, CORNER_RADIUS);

                g2d.setColor(Color.DARK_GRAY);
                g2d.setFont(new Font("Segoe UI", Font.BOLD, 12));
                g2d.drawString("Analytics", 15, 22);

                g2d.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                g2d.setColor(Color.GRAY);
                String[] months = {"Apr", "Jun", "Aug", "Oct", "Feb"};
                int startX = 15;
                int endX = getWidth() - 25;
                int step = (endX - startX) / (months.length - 1);

                for (int i = 0; i < months.length; i++) {
                    g2d.drawString(months[i], startX + (i * step), getHeight() - 10);
                }

                g2d.setStroke(new BasicStroke(2.8f));
                g2d.setColor(GRADIENT_BOTTOM);
                GeneralPath path = new GeneralPath();
                path.moveTo(15, 120);
                path.lineTo(40, 95);
                path.lineTo(65, 110);
                path.lineTo(110, 70);
                path.lineTo(145, 85);
                path.lineTo(185, 75);
                path.lineTo(getWidth() * 0.65, 110);
                path.lineTo(getWidth() * 0.78, 60); 
                path.lineTo(getWidth() * 0.90, 115);
                path.lineTo(getWidth() * 0.95, 90);
                path.lineTo(getWidth() * 0.98, 60);
                g2d.draw(path);

                float octX = getWidth() * 0.78f;
                g2d.setStroke(new BasicStroke(1.2f));
                g2d.setColor(Color.LIGHT_GRAY);
                g2d.drawLine((int)octX, 40, (int)octX, getHeight() - 40);
                
                g2d.setColor(ACCENT_ORANGE);
                g2d.fillOval((int)octX - 6, 54, 12, 12);

                g2d.setFont(new Font("Segoe UI", Font.BOLD, 12));
                g2d.drawString("$7,306", octX + 15, 45);
            }
        };
        chartCard.setBounds(10, 100, 420, 240);
        chartCard.setOpaque(false);
        leftColumnPanel.add(chartCard);

        // --- 2. RIGHT SIDE CONTENT: Calendar, Status, Controls ---
        JPanel rightColumnPanel = new JPanel();
        rightColumnPanel.setBounds(PADDING + 440 + PADDING, 60, 440, 580);
        rightColumnPanel.setOpaque(false);
        rightColumnPanel.setLayout(null);
        backgroundPanel.add(rightColumnPanel);

        // High-Contrast Transaction Ledger Calendar Card
        JPanel calendarCard = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2d.setColor(new Color(242, 242, 242));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), CORNER_RADIUS, CORNER_RADIUS);

                g2d.setColor(new Color(100, 80, 140));
                g2d.setFont(new Font("Segoe UI", Font.BOLD, 14));
                FontMetrics fm = g2d.getFontMetrics();
                int headerX = (getWidth() - fm.stringWidth("MARCH")) / 2;
                g2d.drawString("MARCH", headerX, 25);
                
                g2d.setColor(new Color(225, 225, 225));
                g2d.drawLine(20, 38, getWidth() - 20, 38);

                g2d.setFont(new Font("Segoe UI", Font.BOLD, 11));
                g2d.setColor(new Color(140, 110, 190));
                String[] days = {"SU", "MO", "TU", "WE", "TH", "FR", "SA"};
                int colStep = (getWidth() - 50) / 6;
                for (int i = 0; i < days.length; i++) {
                    g2d.drawString(days[i], 25 + (i * colStep), 58);
                }

                g2d.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                String[][] dates = {
                    {"30", "31", "1", "2", "3", "4", "5"},
                    {"6", "7", "8", "9", "10", "11", "12"},
                    {"13", "14", "15", "16", "17", "18", "19"},
                    {"20", "21", "22", "23", "24", "25", "26"},
                    {"27", "28", "29", "30", "31", "1", "2"}
                };

                int rowStep = (getHeight() - 85) / 4;
                for (int row = 0; row < 5; row++) {
                    for (int col = 0; col < 7; col++) {
                        int x = 25 + (col * colStep);
                        int y = 85 + (row * rowStep);

                        boolean isSpecialDay = false;

                        if (dates[row][col].equals("14") && row == 2) {
                            g2d.setColor(ACCENT_ORANGE);
                            g2d.fillOval(x - 5, y - 12, 22, 22);
                            g2d.setColor(Color.WHITE); 
                            isSpecialDay = true;
                        } else if (dates[row][col].equals("23") && row == 3) {
                            g2d.setColor(ACCENT_ORANGE);
                            g2d.setStroke(new BasicStroke(1.8f));
                            g2d.drawOval(x - 5, y - 12, 22, 22);
                            g2d.setColor(new Color(80, 80, 80)); 
                            isSpecialDay = true;
                        }

                        if (!isSpecialDay) {
                            if ((row == 0 && col < 2) || (row == 4 && col > 4)) {
                                g2d.setColor(new Color(185, 165, 215));
                            } else {
                                g2d.setColor(new Color(100, 100, 100)); 
                            }
                        }
                        g2d.drawString(dates[row][col], x, y);
                    }
                }
            }
        };
        calendarCard.setBounds(10, 10, 420, 200);
        calendarCard.setOpaque(false);
        rightColumnPanel.add(calendarCard);

        // --- INTERACTIVE BANKING CONTROLS SUBPANEL ---
        JLabel amountLabel = new JLabel("Enter Amount (" + DOLLAR_SYMBOL + ")");
        amountLabel.setBounds(20, 225, 180, 20);
        amountLabel.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        amountLabel.setForeground(Color.WHITE);
        rightColumnPanel.add(amountLabel);

        JTextField inputField = new JTextField();
        inputField.setBounds(20, 250, 400, 38);
        inputField.setFont(new Font("Segoe UI", Font.BOLD, 16));
        inputField.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(255, 255, 255, 50)), 
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        rightColumnPanel.add(inputField);

        JLabel countLabel = new JLabel("Transactions Logged: 0");
        countLabel.setBounds(20, 298, 300, 20);
        countLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        countLabel.setForeground(MUTED_PURPLE_TEXT);
        rightColumnPanel.add(countLabel);

        JLabel statusLabel = new JLabel("");
        statusLabel.setBounds(20, 322, 400, 20);
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        statusLabel.setForeground(ACCENT_ORANGE);
        rightColumnPanel.add(statusLabel);

        // --- Streamlined 2-Button Stack Layout ---
        JButton depositBtn = new JButton("DEPOSIT FUNDS");
        depositBtn.setBounds(20, 355, 400, 45);
        styleSystemButton(depositBtn, ACCENT_ORANGE);
        rightColumnPanel.add(depositBtn);

        JButton withdrawBtn = new JButton("WITHDRAW FUNDS");
        withdrawBtn.setBounds(20, 415, 400, 45); // Sits perfectly below Deposit with strict padding
        styleSystemButton(withdrawBtn, ACCENT_ORANGE);
        rightColumnPanel.add(withdrawBtn);

        // --- LISTENERS ---
        depositBtn.addActionListener(e -> {
            try {
                double amt = Double.parseDouble(inputField.getText());
                account.deposit(amt);
                updateUI(balanceLabel, countLabel, statusLabel, "[Success] Deposit Applied");
                inputField.setText("");
            } catch (Exception ex) {
                statusLabel.setText("[!] Error: " + (ex.getMessage().contains("For input string") ? "Invalid Number Entry" : ex.getMessage()));
            }
        });

        withdrawBtn.addActionListener(e -> {
            try {
                double amt = Double.parseDouble(inputField.getText());
                account.withdraw(amt);
                updateUI(balanceLabel, countLabel, statusLabel, "[Success] Withdrawal executed");
                inputField.setText("");
            } catch (Exception ex) {
                statusLabel.setText("[!] Error: " + (ex.getMessage().contains("For input string") ? "Invalid Number Entry" : ex.getMessage()));
            }
        });

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private static void styleSystemButton(JButton btn, Color bg) {
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    static void updateUI(JLabel balanceLabel, JLabel transactionLabel, JLabel statusLabel, String message) {
        balanceLabel.setText(String.format("Stream: " + DOLLAR_SYMBOL + "%,.2f", account.getBalance()));
        transactionLabel.setText("Transactions Logged: " + account.getTransactionCount());
        statusLabel.setText(message);
    }
}