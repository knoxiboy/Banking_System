package Projects_java.Banking_System.ui;

import Projects_java.Banking_System.BankingGUI;
import Projects_java.Banking_System.dao.AccountDAO;
import Projects_java.Banking_System.dao.UserDAO;
import Projects_java.Banking_System.model.Account;
import Projects_java.Banking_System.model.User;
import Projects_java.Banking_System.util.PasswordUtil;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.SQLException;

/**
 * Modern Swing Login screen for the Banking System.
 * Supports User and Administrator authentication with validation and error alerts.
 */
public class LoginPanel extends JPanel {

    private final BankingGUI app;
    private final UserDAO userDAO;
    private final AccountDAO accountDAO;

    private JTextField emailField;
    private JPasswordField passwordField;

    public LoginPanel(BankingGUI app, UserDAO userDAO, AccountDAO accountDAO) {
        this.app = app;
        this.userDAO = userDAO;
        this.accountDAO = accountDAO;

        setLayout(new BorderLayout());
        setBackground(UITheme.BG_LIGHT);

        // Header
        add(UITheme.createHeader("Secure Banking Portal", "Sign in to access your account & transactions"), BorderLayout.NORTH);

        // Center card form
        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.setOpaque(false);
        centerWrapper.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(UITheme.CARD_BG);
        card.setBorder(new CompoundBorder(
                new LineBorder(UITheme.BORDER_COLOR, 1, true),
                new EmptyBorder(30, 35, 30, 35)
        ));
        card.setPreferredSize(new Dimension(420, 430));

        JLabel loginTitle = new JLabel("Welcome Back");
        loginTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        loginTitle.setForeground(UITheme.TEXT_DARK);
        loginTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel loginSub = new JLabel("Please enter your login details to continue");
        loginSub.setFont(UITheme.SUBTITLE_FONT);
        loginSub.setForeground(UITheme.TEXT_MUTED);
        loginSub.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(loginTitle);
        card.add(Box.createVerticalStrut(4));
        card.add(loginSub);
        card.add(Box.createVerticalStrut(25));

        // Form fields
        emailField = UITheme.createTextField();
        JPanel emailPanel = UITheme.createFormField("Email Address", emailField);
        emailPanel.setMaximumSize(new Dimension(360, 60));
        card.add(emailPanel);
        card.add(Box.createVerticalStrut(15));

        passwordField = UITheme.createPasswordField();
        JPanel passPanel = UITheme.createFormField("Password", passwordField);
        passPanel.setMaximumSize(new Dimension(360, 60));
        card.add(passPanel);
        card.add(Box.createVerticalStrut(25));

        // Login Button
        JButton loginBtn = UITheme.createButton("Sign In", UITheme.PRIMARY, Color.WHITE);
        loginBtn.setMaximumSize(new Dimension(360, 42));
        loginBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(loginBtn);
        card.add(Box.createVerticalStrut(18));

        // Register link
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        footerPanel.setOpaque(false);
        JLabel noAccountLbl = new JLabel("Don't have an account?");
        noAccountLbl.setFont(UITheme.BODY_FONT);
        noAccountLbl.setForeground(UITheme.TEXT_MUTED);

        JButton registerLink = new JButton("Register Here");
        registerLink.setFont(new Font("Segoe UI", Font.BOLD, 14));
        registerLink.setForeground(UITheme.PRIMARY);
        registerLink.setBorderPainted(false);
        registerLink.setContentAreaFilled(false);
        registerLink.setCursor(new Cursor(Cursor.HAND_CURSOR));

        footerPanel.add(noAccountLbl);
        footerPanel.add(registerLink);
        footerPanel.setMaximumSize(new Dimension(360, 30));
        card.add(footerPanel);

        // Hint for evaluator / demo credentials
        card.add(Box.createVerticalStrut(15));
        JLabel hintLbl = new JLabel("<html><center><small style='color:#757575;'>Admin Login: admin@bank.com / Admin@123</small></center></html>");
        hintLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(hintLbl);

        centerWrapper.add(card);
        add(centerWrapper, BorderLayout.CENTER);

        // Event Handling
        loginBtn.addActionListener(e -> performLogin());

        // Press Enter on password field to submit
        passwordField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    performLogin();
                }
            }
        });

        registerLink.addActionListener(e -> {
            clearFields();
            app.showRegister();
        });
    }

    private void performLogin() {
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if (email.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please fill in both Email and Password.",
                    "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!PasswordUtil.isValidEmail(email)) {
            JOptionPane.showMessageDialog(this,
                    "Please enter a valid email address.",
                    "Invalid Format", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            User user = userDAO.login(email, password);
            if (user == null) {
                JOptionPane.showMessageDialog(this,
                        "Invalid Email or Password! Please try again.",
                        "Authentication Failed", JOptionPane.ERROR_MESSAGE);
                return;
            }

            clearFields();

            if ("ADMIN".equalsIgnoreCase(user.getRole())) {
                app.showAdmin(user);
            } else {
                Account account = accountDAO.getAccountByEmail(user.getEmail());
                app.showDashboard(user, account);
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Database Connection Error: " + ex.getMessage(),
                    "System Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void clearFields() {
        emailField.setText("");
        passwordField.setText("");
    }
}
