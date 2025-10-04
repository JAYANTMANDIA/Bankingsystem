package bankingsystem;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class BankApp extends JFrame {
    private static final String URL = "jdbc:mysql://localhost:3306/bankdb";
    private static final String USER = "root";
    private static final String PASSWORD = "jayant";

    private Connection conn;
    private JTextField accNoField, nameField, amountField;
    private JTextArea outputArea;

    public BankApp() {
        // Frame setup
        setTitle("Bank Account Management System");
        setSize(900, 550);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(240, 248, 255));

        // Connect to database
        try {
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Database connection failed: " + e.getMessage());
        }

        // Title label
        JLabel title = new JLabel("Bank Account Management", JLabel.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 28));
        title.setForeground(new Color(25, 25, 112));
        title.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        add(title, BorderLayout.NORTH);

        // Input panel
        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBorder(BorderFactory.createTitledBorder("Account Details"));
        inputPanel.setBackground(new Color(230, 240, 255));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;

        JLabel accLabel = new JLabel("Account No:");
        JLabel nameLabel = new JLabel("Name:");
        JLabel amountLabel = new JLabel("Amount:");

        accLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        nameLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        amountLabel.setFont(new Font("Arial", Font.PLAIN, 16));

        accNoField = new JTextField();
        accNoField.setPreferredSize(new Dimension(200, 30));

        nameField = new JTextField();
        nameField.setPreferredSize(new Dimension(300, 25));

        amountField = new JTextField();
        amountField.setPreferredSize(new Dimension(200, 25));

        gbc.gridx = 0; gbc.gridy = 0;
        inputPanel.add(accLabel, gbc);
        gbc.gridx = 1;
        inputPanel.add(accNoField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        inputPanel.add(nameLabel, gbc);
        gbc.gridx = 1;
        inputPanel.add(nameField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        inputPanel.add(amountLabel, gbc);
        gbc.gridx = 1;
        inputPanel.add(amountField, gbc);

        add(inputPanel, BorderLayout.WEST);

        // Output area
        outputArea = new JTextArea();
        outputArea.setFont(new Font("Consolas", Font.PLAIN, 14));
        outputArea.setEditable(false);
        outputArea.setBorder(BorderFactory.createTitledBorder("System Logs"));
        outputArea.setBackground(Color.BLACK);
        outputArea.setForeground(Color.GREEN);
        add(new JScrollPane(outputArea), BorderLayout.CENTER);

        // Buttons panel
        JPanel buttonPanel = new JPanel(new GridLayout(1, 5, 15, 15));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        buttonPanel.setBackground(new Color(240, 248, 255));

        JButton createBtn = new JButton("Create");
        JButton depositBtn = new JButton("Deposit");
        JButton withdrawBtn = new JButton("Withdraw");
        JButton checkBtn = new JButton("Check Balance");
        JButton viewBtn = new JButton("View All");

        JButton[] buttons = {createBtn, depositBtn, withdrawBtn, checkBtn, viewBtn};
        for (JButton btn : buttons) {
            btn.setFont(new Font("Arial", Font.BOLD, 14));
            btn.setBackground(new Color(30, 144, 255));
            btn.setForeground(Color.WHITE);
            btn.setFocusPainted(false);
            btn.setBorder(BorderFactory.createLineBorder(new Color(0, 0, 128), 2));
            buttonPanel.add(btn);
        }

        add(buttonPanel, BorderLayout.SOUTH);

        // Button actions
        createBtn.addActionListener(e -> createAccount());
        depositBtn.addActionListener(e -> depositMoney());
        withdrawBtn.addActionListener(e -> withdrawMoney());
        checkBtn.addActionListener(e -> checkBalance());
        viewBtn.addActionListener(e -> viewAllAccounts());
    }

    // Create account
    private void createAccount() {
        try {
            int accNo = Integer.parseInt(accNoField.getText());
            String name = nameField.getText().trim();
            double balance = Double.parseDouble(amountField.getText());

            if (name.isEmpty() || balance < 0) {
                JOptionPane.showMessageDialog(this, "Please enter valid account details.");
                return;
            }

            PreparedStatement pst = conn.prepareStatement("INSERT INTO accounts VALUES (?, ?, ?)");
            pst.setInt(1, accNo);
            pst.setString(2, name);
            pst.setDouble(3, balance);
            pst.executeUpdate();

            outputArea.append("Account created successfully: " + accNo + " (" + name + ")\n");
            clearFields();
        } catch (Exception ex) {
            outputArea.append("Error creating account: " + ex.getMessage() + "\n");
        }
    }

    // Deposit money
    private void depositMoney() {
        try {
            int accNo = Integer.parseInt(accNoField.getText());
            double amount = Double.parseDouble(amountField.getText());

            if (amount <= 0) {
                JOptionPane.showMessageDialog(this, "Enter a positive amount.");
                return;
            }

            PreparedStatement pst = conn.prepareStatement("UPDATE accounts SET balance = balance + ? WHERE accNo = ?");
            pst.setDouble(1, amount);
            pst.setInt(2, accNo);
            int rows = pst.executeUpdate();

            if (rows > 0) {
                outputArea.append("Deposit successful for account " + accNo + "\n");
            } else {
                outputArea.append("Account not found.\n");
            }

            clearFields();
        } catch (Exception ex) {
            outputArea.append("Error during deposit: " + ex.getMessage() + "\n");
        }
    }

    // Withdraw money
    private void withdrawMoney() {
        try {
            int accNo = Integer.parseInt(accNoField.getText());
            double amount = Double.parseDouble(amountField.getText());

            if (amount <= 0) {
                JOptionPane.showMessageDialog(this, "Enter a positive amount.");
                return;
            }

            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("SELECT balance FROM accounts WHERE accNo=" + accNo);

            if (rs.next()) {
                double balance = rs.getDouble(1);
                if (balance >= amount) {
                    PreparedStatement pst = conn.prepareStatement("UPDATE accounts SET balance = balance - ? WHERE accNo=?");
                    pst.setDouble(1, amount);
                    pst.setInt(2, accNo);
                    pst.executeUpdate();
                    outputArea.append("Withdrawal of " + amount + " successful.\n");
                } else {
                    outputArea.append("Insufficient balance.\n");
                }
            } else {
                outputArea.append("Account not found.\n");
            }

            clearFields();
        } catch (Exception ex) {
            outputArea.append("Error during withdrawal: " + ex.getMessage() + "\n");
        }
    }

    // Check balance
    private void checkBalance() {
        try {
            int accNo = Integer.parseInt(accNoField.getText());
            PreparedStatement pst = conn.prepareStatement("SELECT balance FROM accounts WHERE accNo=?");
            pst.setInt(1, accNo);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                outputArea.append("Balance for account " + accNo + ": " + rs.getDouble(1) + "\n");
            } else {
                outputArea.append("Account not found.\n");
            }

            clearFields();
        } catch (Exception ex) {
            outputArea.append("Error checking balance: " + ex.getMessage() + "\n");
        }
    }

    // View all accounts in a table
    private void viewAllAccounts() {
        try {
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM accounts");

            DefaultTableModel model = new DefaultTableModel(new String[]{"Account No", "Name", "Balance"}, 0);

            while (rs.next()) {
                int accNo = rs.getInt("accNo");
                String name = rs.getString("name");
                double balance = rs.getDouble("balance");
                model.addRow(new Object[]{accNo, name, balance});
            }

            JTable table = new JTable(model);
            table.setFillsViewportHeight(true);
            table.setFont(new Font("Arial", Font.PLAIN, 14));
            table.setRowHeight(25);

            JFrame tableFrame = new JFrame("All Accounts");
            tableFrame.setSize(600, 400);
            tableFrame.setLayout(new BorderLayout());
            tableFrame.add(new JScrollPane(table), BorderLayout.CENTER);
            tableFrame.setLocationRelativeTo(null);
            tableFrame.setVisible(true);

        } catch (Exception ex) {
            outputArea.append("Error fetching accounts: " + ex.getMessage() + "\n");
        }
    }

    // Clear input fields
    private void clearFields() {
        accNoField.setText("");
        nameField.setText("");
        amountField.setText("");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new BankApp().setVisible(true));
    }
}
