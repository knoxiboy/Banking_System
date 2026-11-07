package Projects_java.Banking_System.ui;

import Projects_java.Banking_System.BankingGUI;
import Projects_java.Banking_System.dao.UserDAO;
import Projects_java.Banking_System.model.User;
import Projects_java.Banking_System.util.PasswordUtil;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.sql.SQLException;

/**
 * Modern Swing Registration screen for the Banking System.
 * Handles user sign-up with real-time format validation and SHA-256 password hashing.
 */
public class RegisterPanel extends JPanel {

    private final BankingGUI app;
    private final UserDAO userDAO;

    private JTextField nameField;
    private JTextField emailField;
    private JPasswordField passField;
    private JPasswordField confirmPassField;

    public RegisterPanel(BankingGUI app, UserDAO userDAO) {
        this.app = app;
        this.userDAO = userDAO;

        setLayout(new BorderLayout());
        setBackground(UITheme.BG_LIGHT);

        // Header
        add(UITheme.createHeader("Create Banking Account", "Register for a new digital banking profile"), BorderLayout.NORTH);

        // Center card form
        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.setOpaque(false);
        centerWrapper.setBorder(new EmptyBorder(15, 20, 15, 20));

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(UITheme.CARD_BG);
        card.setBorder(new CompoundBorder(
                new LineBorder(UITheme.BORDER_COLOR, 1, true),
                new EmptyBorder(25, 35, 25, 35)
        ));
        card.setPreferredSize(new Dimension(440, 520));

        JLabel titleLbl = new JLabel("Register New User");
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLbl.setForeground(UITheme.TEXT_DARK);
        titleLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subLbl = new JLabel("All fields are mandatory");
        subLbl.setFont(UITheme.SUBTITLE_FONT);
        subLbl.setForeground(UITheme.TEXT_MUTED);
        subLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(titleLbl);
        card.add(Box.createVerticalStrut(4));
        card.add(subLbl);
        card.add(Box.createVerticalStrut(20));

        // Full Name
        nameField = UITheme.createTextField();
        JPanel nameP = UITheme.createFormField("Full Name", nameField);
        nameP.setMaximumSize(new Dimension(370, 55));
        card.add(nameP);
        card.add(Box.createVerticalStrut(12));

        // Email
        emailField = UITheme.createTextField();
        JPanel emailP = UITheme.createFormField("Email Address", emailField);
        emailP.setMaximumSize(new Dimension(370, 55));
        card.add(emailP);
        card.add(Box.createVerticalStrut(12));

        // Password
        passField = UITheme.createPasswordField();
        JPanel passP = UITheme.createFormField("Password (min. 8 characters)", passField);
        passP.setMaximumSize(new Dimension(370, 55));
        card.add(passP);
        card.add(Box.createVerticalStrut(12));

        // Confirm Password
        confirmPassField = UITheme.createPasswordField();
        JPanel confirmP = UITheme.createFormField("Confirm Password", confirmPassField);
        confirmP.setMaximumSize(new Dimension(370, 55));
        card.add(confirmP);
        card.add(Box.createVerticalStrut(22));

        // Register Button
        JButton registerBtn = UITheme.createButton("Create Profile", UITheme.SUCCESS, Color.WHITE);
        registerBtn.setMaximumSize(new Dimension(370, 42));
        registerBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(registerBtn);
        card.add(Box.createVerticalStrut(14));

        // Back to Login Link
        JPanel backPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        backPanel.setOpaque(false);
        JLabel haveAccountLbl = new JLabel("Already have an account?");
        haveAccountLbl.setFont(UITheme.BODY_FONT);
        haveAccountLbl.setForeground(UITheme.TEXT_MUTED);

        JButton loginLink = new JButton("Back to Login");
        loginLink.setFont(new Font("Segoe UI", Font.BOLD, 14));
        loginLink.setForeground(UITheme.PRIMARY);
        loginLink.setBorderPainted(false);
        loginLink.setContentAreaFilled(false);
        loginLink.setCursor(new Cursor(Cursor.HAND_CURSOR));

        backPanel.add(haveAccountLbl);
        backPanel.add(loginLink);
        backPanel.setMaximumSize(new Dimension(370, 30));
        card.add(backPanel);

        centerWrapper.add(card);
        add(centerWrapper, BorderLayout.CENTER);

        // Actions
        registerBtn.addActionListener(e -> performRegistration());
        loginLink.addActionListener(e -> {
            clearFields();
            app.showLogin();
        });
    }

    private void performRegistration() {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String pass = new String(passField.getPassword()).trim();
        String confirm = new String(confirmPassField.getPassword()).trim();

        if (name.isEmpty() || email.isEmpty() || pass.isEmpty() || confirm.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "All fields are required. Please fill in all details.",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!PasswordUtil.isValidEmail(email)) {
            JOptionPane.showMessageDialog(this,
                    "Please enter a valid email address (e.g. user@example.com).",
                    "Invalid Email", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!PasswordUtil.isStrongPassword(pass)) {
            JOptionPane.showMessageDialog(this,
                    "Password must be at least 8 characters long.",
                    "Weak Password", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!pass.equals(confirm)) {
            JOptionPane.showMessageDialog(this,
                    "Passwords do not match. Please verify.",
                    "Password Mismatch", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            if (userDAO.userExists(email)) {
                JOptionPane.showMessageDialog(this,
                        "An account with this email address already exists!",
                        "User Already Exists", JOptionPane.ERROR_MESSAGE);
                return;
            }

            User newUser = new User(name, email, pass);
            boolean registered = userDAO.register(newUser);

            if (registered) {
                JOptionPane.showMessageDialog(this,
                        "Registration Successful!\nYou can now log in with your email and password.",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                clearFields();
                app.showLogin();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Registration failed. Please try again.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Database Error: " + ex.getMessage(),
                    "System Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void clearFields() {
        nameField.setText("");
        emailField.setText("");
        passField.setText("");
        confirmPassField.setText("");
    }
}
