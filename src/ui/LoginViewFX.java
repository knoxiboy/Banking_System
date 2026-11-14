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
 * LoginViewFX — JavaFX-powered authentication screen for Apex Digital Banking.
 * Supports User and Admin sign-in with full validation and error reporting.
 */
public class LoginViewFX extends BorderPane {

    private final BankingFXApp app;
    private final UserDAO userDAO;
    private final AccountDAO accountDAO;

    private TextField emailField;
    private PasswordField passwordField;

    public LoginViewFX(BankingFXApp app, UserDAO userDAO, AccountDAO accountDAO) {
        this.app = app;
        this.userDAO = userDAO;
        this.accountDAO = accountDAO;

        setStyle("-fx-background-color: " + UIThemeFX.BG_LIGHT_HEX + ";");

        // Top Header
        setTop(UIThemeFX.createHeader("Secure Banking Portal", "Sign in to access your digital account & transactions"));

        // Center Login Card
        StackPane centerContainer = new StackPane();
        centerContainer.setPadding(new Insets(30));

        VBox card = new VBox(15);
        card.setMaxWidth(420);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(30, 35, 30, 35));
        card.setStyle(
                "-fx-background-color: #FFFFFF; " +
                "-fx-background-radius: 10px; " +
                "-fx-border-color: " + UIThemeFX.BORDER_HEX + "; " +
                "-fx-border-radius: 10px; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.08), 12, 0, 0, 4);"
        );

        Label welcomeLbl = new Label("Welcome Back");
        welcomeLbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        welcomeLbl.setTextFill(Color.web(UIThemeFX.TEXT_DARK_HEX));

        Label subLbl = new Label("Please enter your login details to continue");
        subLbl.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 13));
        subLbl.setTextFill(Color.web(UIThemeFX.TEXT_MUTED_HEX));

        emailField = UIThemeFX.createTextField("name@example.com");
        VBox emailBox = UIThemeFX.createFormField("Email Address", emailField);

        passwordField = UIThemeFX.createPasswordField("Enter your password");
        VBox passBox = UIThemeFX.createFormField("Password", passwordField);

        Button loginBtn = UIThemeFX.createButton("Sign In", UIThemeFX.PRIMARY_HEX, UIThemeFX.PRIMARY_HOVER_HEX);
        loginBtn.setMaxWidth(Double.MAX_VALUE);
        loginBtn.setPrefHeight(40);

        HBox footerBox = new HBox(6);
        footerBox.setAlignment(Pos.CENTER);
        Label noAccLbl = new Label("Don't have an account?");
        noAccLbl.setFont(Font.font("Segoe UI", 13));
        noAccLbl.setTextFill(Color.web(UIThemeFX.TEXT_MUTED_HEX));

        Hyperlink registerLink = new Hyperlink("Register Here");
        registerLink.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        registerLink.setStyle("-fx-text-fill: " + UIThemeFX.PRIMARY_HEX + "; -fx-underline: false;");
        footerBox.getChildren().addAll(noAccLbl, registerLink);

        Label hintLbl = new Label("Admin Login: admin@bank.com / Admin@123");
        hintLbl.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 11));
        hintLbl.setTextFill(Color.web("#9E9E9E"));

        card.getChildren().addAll(
                welcomeLbl, subLbl,
                new Region() {{ setPrefHeight(10); }},
                emailBox, passBox,
                new Region() {{ setPrefHeight(5); }},
                loginBtn, footerBox,
                new Region() {{ setPrefHeight(5); }},
                hintLbl
        );

        centerContainer.getChildren().add(card);
        setCenter(centerContainer);

        // ─── Event Handling ───────────────────────────────────────────────────
        loginBtn.setOnAction(e -> performLogin());

        passwordField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                performLogin();
            }
        });

        emailField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                passwordField.requestFocus();
            }
        });

        registerLink.setOnAction(e -> {
            clearFields();
            app.showRegister();
        });
    }

    private void performLogin() {
        String email = emailField.getText().trim();
        String pass = passwordField.getText().trim();

        if (email.isEmpty() || pass.isEmpty()) {
            UIThemeFX.showAlert(Alert.AlertType.WARNING, "Input Error", "Missing Credentials", "Please enter both Email and Password.");
            return;
        }

        if (!PasswordUtil.isValidEmail(email)) {
            UIThemeFX.showAlert(Alert.AlertType.WARNING, "Invalid Format", "Invalid Email Format", "Please provide a valid email address (e.g. user@example.com).");
            return;
        }

        try {
            User user = userDAO.login(email, pass);
            if (user == null) {
                UIThemeFX.showAlert(Alert.AlertType.ERROR, "Authentication Failed", "Invalid Credentials", "Invalid Email or Password! Please verify and try again.");
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
            UIThemeFX.showAlert(Alert.AlertType.ERROR, "System Error", "Database Error", "Error connecting to database: " + ex.getMessage());
        }
    }

    public void clearFields() {
        emailField.clear();
        passwordField.clear();
    }
}

