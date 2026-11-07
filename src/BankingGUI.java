package Projects_java.Banking_System;

import Projects_java.Banking_System.dao.AccountDAO;
import Projects_java.Banking_System.dao.DBConnection;
import Projects_java.Banking_System.dao.TransactionDAO;
import Projects_java.Banking_System.dao.UserDAO;
import Projects_java.Banking_System.model.Account;
import Projects_java.Banking_System.model.User;
import Projects_java.Banking_System.ui.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * BankingGUI — Main Entry Point for the Graphical Banking System.
 *
 * Implements a complete Model-View-Controller (MVC) and DAO-driven
 * desktop GUI application built using Java Swing and JDBC/MySQL.
 *
 * Features:
 *  - Secure SHA-256 Hashing for Passwords & PINs
 *  - Event Handling & Responsive CardLayout navigation
 *  - Atomic ACID Transaction management (debit/credit/transfer with rollback)
 *  - Comprehensive Transaction Ledger & Passbook with color coding
 *  - Administrative Console for Fraud Prevention (Account Freeze / Unfreeze)
 */
public class BankingGUI extends JFrame {

    public static final String DB_URL  = "jdbc:mysql://localhost:3306/banking_system";
    public static final String DB_USER = "root";
    public static final String DB_PASS = "knox";

    private Connection connection;
    private UserDAO userDAO;
    private TransactionDAO transactionDAO;
    private AccountDAO accountDAO;

    private CardLayout cardLayout;
    private JPanel cardsPanel;

    private LoginPanel loginPanel;
    private RegisterPanel registerPanel;
    private DashboardPanel dashboardPanel;
    private TransactionPanel transactionPanel;
    private AdminPanel adminPanel;

    private User currentLoggedInUser;
    private Account currentLoggedInAccount;

    public BankingGUI() {
        super("Apex Digital Banking System");
        initDatabase();
        initUI();
    }

    private void initDatabase() {
        try {
            connection = DBConnection.getConnection(DB_URL, DB_USER, DB_PASS);
            transactionDAO = new TransactionDAO(connection);
            accountDAO     = new AccountDAO(connection, transactionDAO);
            userDAO        = new UserDAO(connection);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "⚠️ Database Connection Warning:\n" + e.getMessage() +
                    "\n\nPlease ensure MySQL is running on port 3306 and banking_schema.sql has been executed.",
                    "Database Notice", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void initUI() {
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(920, 680);
        setMinimumSize(new Dimension(800, 600));
        setLocationRelativeTo(null);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                int confirm = JOptionPane.showConfirmDialog(BankingGUI.this,
                        "Are you sure you want to exit the Banking System?",
                        "Exit Confirmation", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                if (confirm == JOptionPane.YES_OPTION) {
                    DBConnection.closeConnection();
                    dispose();
                    System.exit(0);
                }
            }
        });

        cardLayout = new CardLayout();
        cardsPanel = new JPanel(cardLayout);

        // Instantiate Screen Panels
        loginPanel        = new LoginPanel(this, userDAO, accountDAO);
        registerPanel     = new RegisterPanel(this, userDAO);
        dashboardPanel    = new DashboardPanel(this, accountDAO, transactionDAO);
        transactionPanel  = new TransactionPanel(this, transactionDAO);
        adminPanel        = new AdminPanel(this, accountDAO, transactionDAO);

        // Register cards
        cardsPanel.add(loginPanel, "LOGIN");
        cardsPanel.add(registerPanel, "REGISTER");
        cardsPanel.add(dashboardPanel, "DASHBOARD");
        cardsPanel.add(transactionPanel, "TRANSACTIONS");
        cardsPanel.add(adminPanel, "ADMIN");

        add(cardsPanel, BorderLayout.CENTER);

        showLogin();
    }

    // ─── Screen Navigation Controls ──────────────────────────────────────────

    public void showLogin() {
        currentLoggedInUser = null;
        currentLoggedInAccount = null;
        if (loginPanel != null) loginPanel.clearFields();
        cardLayout.show(cardsPanel, "LOGIN");
    }

    public void showRegister() {
        if (registerPanel != null) registerPanel.clearFields();
        cardLayout.show(cardsPanel, "REGISTER");
    }

    public void showDashboard(User user, Account account) {
        this.currentLoggedInUser = user;
        this.currentLoggedInAccount = account;
        dashboardPanel.setUserAndAccount(user, account);
        cardLayout.show(cardsPanel, "DASHBOARD");
    }

    public void showDashboardAfterTx() {
        if (currentLoggedInUser != null) {
            dashboardPanel.setUserAndAccount(currentLoggedInUser, currentLoggedInAccount);
            cardLayout.show(cardsPanel, "DASHBOARD");
        } else {
            showLogin();
        }
    }

    public void showTransactions(Account account) {
        transactionPanel.setAccount(account);
        cardLayout.show(cardsPanel, "TRANSACTIONS");
    }

    public void showAdmin(User admin) {
        this.currentLoggedInUser = admin;
        adminPanel.setAdmin(admin);
        cardLayout.show(cardsPanel, "ADMIN");
    }

    // ─── Application Bootstrap ───────────────────────────────────────────────

    public static void main(String[] args) {
        // Apply modern system look and feel
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignored) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored2) {
            }
        }

        SwingUtilities.invokeLater(() -> {
            BankingGUI gui = new BankingGUI();
            gui.setVisible(true);
        });
    }
}
