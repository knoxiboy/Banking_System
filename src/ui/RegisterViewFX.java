package Projects_java.Banking_System.ui;

import Projects_java.Banking_System.BankingFXApp;
import Projects_java.Banking_System.dao.AccountDAO;
import Projects_java.Banking_System.dao.UserDAO;
import Projects_java.Banking_System.model.Account;
import Projects_java.Banking_System.model.User;
import Projects_java.Banking_System.util.PasswordUtil;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.sql.SQLException;

/**
 * RegisterViewFX — JavaFX-powered registration view for new banking customers.
 * Performs client-side format checks and secure SHA-256 password hashing.
 */
public class RegisterViewFX extends BorderPane {

    private final BankingFXApp app;
    private final UserDAO userDAO;
    private final AccountDAO accountDAO;

    private TextField nameField;
    private TextField emailField;
    private PasswordField passField;
    private PasswordField confirmPassField;
    private PasswordField pinField;

    public RegisterViewFX(BankingFXApp app, UserDAO userDAO, AccountDAO accountDAO) {
        this.app = app;
        this.userDAO = userDAO;
        this.accountDAO = accountDAO;

        setStyle("-fx-background-color: " + UIThemeFX.BG_LIGHT_HEX + ";");

        // Top Header
        setTop(UIThemeFX.createHeader("Create Banking Account", "Register for a new digital banking profile"));

        // Center Form Card
        StackPane centerContainer = new StackPane();
        centerContainer.setPadding(new Insets(20));

        VBox card = new VBox(12);
        card.setMaxWidth(440);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(25, 35, 25, 35));
        card.setStyle(
                "-fx-background-color: #FFFFFF; " +
                "-fx-background-radius: 10px; " +
                "-fx-border-color: " + UIThemeFX.BORDER_HEX + "; " +
                "-fx-border-radius: 10px; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.08), 12, 0, 0, 4);"
        );

        Label titleLbl = new Label("Register New User");
        titleLbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        titleLbl.setTextFill(Color.web(UIThemeFX.TEXT_DARK_HEX));

        Label subLbl = new Label("All fields are mandatory");
        subLbl.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 13));
        subLbl.setTextFill(Color.web(UIThemeFX.TEXT_MUTED_HEX));

        nameField = UIThemeFX.createTextField("John Doe");
        VBox nameBox = UIThemeFX.createFormField("Full Name", nameField);

        emailField = UIThemeFX.createTextField("john.doe@example.com");
        VBox emailBox = UIThemeFX.createFormField("Email Address", emailField);

        passField = UIThemeFX.createPasswordField("At least 8 characters");
        VBox passBox = UIThemeFX.createFormField("Password (min. 8 characters)", passField);

        confirmPassField = UIThemeFX.createPasswordField("Re-enter password");
        VBox confirmPassBox = UIThemeFX.createFormField("Confirm Password", confirmPassField);

        pinField = UIThemeFX.createPasswordField("4 to 6 digit numeric PIN");
        VBox pinBox = UIThemeFX.createFormField("Security PIN (4-6 digits for transactions)", pinField);

        Button registerBtn = UIThemeFX.createButton("Create Profile & Open Account", UIThemeFX.SUCCESS_HEX, UIThemeFX.SUCCESS_HOVER_HEX);
        registerBtn.setMaxWidth(Double.MAX_VALUE);
        registerBtn.setPrefHeight(40);

        HBox backBox = new HBox(6);
        backBox.setAlignment(Pos.CENTER);
        Label haveAccLbl = new Label("Already have an account?");
        haveAccLbl.setFont(Font.font("Segoe UI", 13));
        haveAccLbl.setTextFill(Color.web(UIThemeFX.TEXT_MUTED_HEX));

        Hyperlink loginLink = new Hyperlink("Back to Login");
        loginLink.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        loginLink.setStyle("-fx-text-fill: " + UIThemeFX.PRIMARY_HEX + "; -fx-underline: false;");
        backBox.getChildren().addAll(haveAccLbl, loginLink);

        card.getChildren().addAll(
                titleLbl, subLbl,
                new Region() {{ setPrefHeight(5); }},
                nameBox, emailBox, passBox, confirmPassBox, pinBox,
                new Region() {{ setPrefHeight(8); }},
                registerBtn, backBox
        );

        ScrollPane scroll = new ScrollPane(card);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-border-color: transparent;");
        StackPane.setAlignment(card, Pos.CENTER);
        centerContainer.getChildren().add(card);

        setCenter(centerContainer);

        // ─── Event Handling ───────────────────────────────────────────────────
        registerBtn.setOnAction(e -> performRegistration());

        confirmPassField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                performRegistration();
            }
        });

        loginLink.setOnAction(e -> {
            clearFields();
            app.showLogin();
        });
    }

    private void performRegistration() {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String pass = passField.getText().trim();
        String confirm = confirmPassField.getText().trim();
        String pin = pinField.getText().trim();

        if (name.isEmpty() || email.isEmpty() || pass.isEmpty() || confirm.isEmpty() || pin.isEmpty()) {
            UIThemeFX.showAlert(Alert.AlertType.WARNING, "Validation Error", "Missing Fields", "All fields are required. Please fill in all details.");
            return;
        }

        if (!PasswordUtil.isValidEmail(email)) {
            UIThemeFX.showAlert(Alert.AlertType.WARNING, "Validation Error", "Invalid Email", "Please enter a valid email address (e.g. user@example.com).");
            return;
        }

        if (!PasswordUtil.isStrongPassword(pass)) {
            UIThemeFX.showAlert(Alert.AlertType.WARNING, "Validation Error", "Weak Password", "Password must be at least 8 characters long.");
            return;
        }

        if (!pass.equals(confirm)) {
            UIThemeFX.showAlert(Alert.AlertType.WARNING, "Validation Error", "Password Mismatch", "Passwords do not match. Please verify.");
            return;
        }

        if (!PasswordUtil.isValidPin(pin)) {
            UIThemeFX.showAlert(Alert.AlertType.WARNING, "Validation Error", "Invalid PIN", "Security PIN must be 4 to 6 numeric digits.");
            return;
        }

        try {
            if (userDAO.userExists(email)) {
                UIThemeFX.showAlert(Alert.AlertType.ERROR, "Registration Error", "User Exists", "An account with this email address already exists!");
                return;
            }

            User newUser = new User(name, email, pass);
            boolean ok = userDAO.register(newUser);
            if (ok) {
                // Provision initial bank account automatically with ₹1,000 welcome deposit
                Account newAcc = new Account(0, email, name, 1000.00, pin, "SAVINGS");
                long accNum = accountDAO.openAccount(newAcc);

                UIThemeFX.showAlert(Alert.AlertType.INFORMATION, "Registration Successful", "Profile & Bank Account Created!",
                        "Congratulations, " + name + "!\n\nYour digital profile and bank account have been created successfully!\n" +
                        "• Account Number: " + accNum + "\n" +
                        "• Opening Balance: ₹ 1,000.00\n\n" +
                        "You can now log in using your email and password.");
                clearFields();
                app.showLogin();
            } else {
                UIThemeFX.showAlert(Alert.AlertType.ERROR, "Registration Failed", "System Error", "Registration could not be completed. Please try again.");
            }

        } catch (SQLException ex) {
            UIThemeFX.showAlert(Alert.AlertType.ERROR, "Database Error", "System Error", ex.getMessage());
        }
    }

    public void clearFields() {
        nameField.clear();
        emailField.clear();
        passField.clear();
        confirmPassField.clear();
        pinField.clear();
    }
}

