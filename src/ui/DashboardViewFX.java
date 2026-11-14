package Projects_java.Banking_System.ui;

import Projects_java.Banking_System.BankingFXApp;
import Projects_java.Banking_System.dao.AccountDAO;
import Projects_java.Banking_System.dao.TransactionDAO;
import Projects_java.Banking_System.model.Account;
import Projects_java.Banking_System.model.Transaction;
import Projects_java.Banking_System.model.User;
import Projects_java.Banking_System.util.PasswordUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * DashboardViewFX — JavaFX Customer Dashboard for Apex Digital Banking.
 * Provides rich banking operations: balance masking, debit/credit/transfer with ACID guarantees,
 * mini-statement, account opening, and transaction navigation.
 */
public class DashboardViewFX extends BorderPane {

    private final BankingFXApp app;
    private final AccountDAO accountDAO;
    private final TransactionDAO transactionDAO;

    private User currentUser;
    private Account currentAccount;

    private Label welcomeLabel;
    private Label accountNumLabel;
    private Label accountTypeLabel;
    private Label statusBadge;
    private Label balanceValueLabel;
    private Button balanceToggleBtn;

    private boolean isBalanceRevealed = false;
    private StackPane contentArea;

    public DashboardViewFX(BankingFXApp app, AccountDAO accountDAO, TransactionDAO transactionDAO) {
        this.app = app;
        this.accountDAO = accountDAO;
        this.transactionDAO = transactionDAO;

        setStyle("-fx-background-color: " + UIThemeFX.BG_LIGHT_HEX + ";");
        contentArea = new StackPane();
        setCenter(contentArea);
    }

    public void setUserAndAccount(User user, Account account) {
        this.currentUser = user;
        this.currentAccount = account;
        this.isBalanceRevealed = false;

        contentArea.getChildren().clear();
        if (account == null) {
            setTop(buildTopBar("Welcome to Apex Digital Bank", false));
            contentArea.getChildren().add(buildNoAccountView());
        } else {
            setTop(buildTopBar("Welcome Back, " + account.getFullName(), true));
            contentArea.getChildren().add(buildActiveDashboardView());
            refreshAccountData();
        }
    }

    private HBox buildTopBar(String titleText, boolean hasAccount) {
        HBox topBar = new HBox(15);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(16, 24, 16, 24));
        topBar.setStyle("-fx-background-color: " + UIThemeFX.PRIMARY_HEX + ";");

        welcomeLabel = new Label(titleText);
        welcomeLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        welcomeLabel.setTextFill(Color.WHITE);
        HBox.setHgrow(welcomeLabel, Priority.ALWAYS);

        HBox controls = new HBox(10);
        controls.setAlignment(Pos.CENTER_RIGHT);

        if (hasAccount) {
            Button refreshBtn = UIThemeFX.createButton("🔄 Refresh", UIThemeFX.PRIMARY_HOVER_HEX, "#1E88E5");
            refreshBtn.setOnAction(e -> refreshAccountData());
            controls.getChildren().add(refreshBtn);
        }

        Button logoutBtn = UIThemeFX.createButton("Logout", UIThemeFX.DANGER_HEX, UIThemeFX.DANGER_HOVER_HEX);
        logoutBtn.setOnAction(e -> {
            isBalanceRevealed = false;
            app.showLogin();
        });
        controls.getChildren().add(logoutBtn);

        topBar.getChildren().addAll(welcomeLabel, controls);
        return topBar;
    }

    // ─── No Account View ──────────────────────────────────────────────────────
    private VBox buildNoAccountView() {
        VBox box = new VBox(15);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(40));

        VBox card = new VBox(14);
        card.setMaxWidth(460);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(35, 40, 35, 40));
        card.setStyle(
                "-fx-background-color: #FFFFFF; " +
                "-fx-background-radius: 10px; " +
                "-fx-border-color: " + UIThemeFX.BORDER_HEX + "; " +
                "-fx-border-radius: 10px; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.08), 10, 0, 0, 3);"
        );

        Label iconLbl = new Label("🏦");
        iconLbl.setFont(Font.font(48));

        Label title = new Label("No Bank Account Found");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        title.setTextFill(Color.web(UIThemeFX.TEXT_DARK_HEX));

        Label desc = new Label("You haven't opened a bank account with us yet.");
        desc.setFont(Font.font("Segoe UI", 13));
        desc.setTextFill(Color.web(UIThemeFX.TEXT_MUTED_HEX));

        Button openBtn = UIThemeFX.createButton("Open New Bank Account", UIThemeFX.SUCCESS_HEX, UIThemeFX.SUCCESS_HOVER_HEX);
        openBtn.setPrefHeight(40);
        openBtn.setOnAction(e -> promptOpenAccount());

        card.getChildren().addAll(iconLbl, title, desc, new Region() {{ setPrefHeight(10); }}, openBtn);
        box.getChildren().add(card);
        return box;
    }

    // ─── Active Account Dashboard ─────────────────────────────────────────────
    private ScrollPane buildActiveDashboardView() {
        VBox content = new VBox(22);
        content.setPadding(new Insets(24, 28, 24, 28));

        // 1. Account Summary Card
        HBox summaryCard = new HBox(20);
        summaryCard.setAlignment(Pos.CENTER_LEFT);
        summaryCard.setPadding(new Insets(20, 24, 20, 24));
        summaryCard.setStyle(
                "-fx-background-color: #FFFFFF; " +
                "-fx-background-radius: 10px; " +
                "-fx-border-color: " + UIThemeFX.BORDER_HEX + "; " +
                "-fx-border-radius: 10px; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.06), 8, 0, 0, 2);"
        );

        VBox leftInfo = new VBox(6);
        accountNumLabel = new Label("A/C Number: 10000100");
        accountNumLabel.setFont(Font.font("Consolas", FontWeight.BOLD, 17));
        accountNumLabel.setTextFill(Color.web(UIThemeFX.PRIMARY_HEX));

        accountTypeLabel = new Label("Type: SAVINGS ACCOUNT");
        accountTypeLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 13));
        accountTypeLabel.setTextFill(Color.web(UIThemeFX.TEXT_MUTED_HEX));

        statusBadge = new Label("● ACTIVE");
        statusBadge.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        statusBadge.setTextFill(Color.web(UIThemeFX.SUCCESS_HEX));

        leftInfo.getChildren().addAll(accountNumLabel, accountTypeLabel, statusBadge);
        HBox.setHgrow(leftInfo, Priority.ALWAYS);

        // Balance Card on the right
        VBox balanceCard = new VBox(6);
        balanceCard.setAlignment(Pos.CENTER_LEFT);
        balanceCard.setPadding(new Insets(14, 20, 14, 20));
        balanceCard.setStyle(
                "-fx-background-color: #EDF7ED; " +
                "-fx-background-radius: 8px; " +
                "-fx-border-color: #A5D6A7; " +
                "-fx-border-radius: 8px;"
        );

        Label balTitle = new Label("AVAILABLE BALANCE");
        balTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
        balTitle.setTextFill(Color.web("#2E7D32"));

        balanceValueLabel = new Label("₹ ••••••••");
        balanceValueLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        balanceValueLabel.setTextFill(Color.web("#1B5E20"));

        balanceToggleBtn = UIThemeFX.createButton("👁 Show Balance", UIThemeFX.SUCCESS_HEX, UIThemeFX.SUCCESS_HOVER_HEX);
        balanceToggleBtn.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
        balanceToggleBtn.setOnAction(e -> toggleBalanceVisibility());

        balanceCard.getChildren().addAll(balTitle, balanceValueLabel, balanceToggleBtn);

        summaryCard.getChildren().addAll(leftInfo, balanceCard);
        content.getChildren().add(summaryCard);

        // 2. Services Section Header
        Label servicesHeader = new Label("Banking Services & Operations");
        servicesHeader.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        servicesHeader.setTextFill(Color.web(UIThemeFX.TEXT_DARK_HEX));
        content.getChildren().add(servicesHeader);

        // 3. Grid of Action Cards
        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(16);

        Button debitBtn    = createActionCard("💳 Debit Money", "Withdraw funds securely with PIN", UIThemeFX.DANGER_HEX);
        Button creditBtn   = createActionCard("💰 Credit Money", "Deposit funds into your account", UIThemeFX.SUCCESS_HEX);
        Button transferBtn = createActionCard("🔄 Transfer Funds", "Send money instantly to any account", UIThemeFX.PRIMARY_HEX);
        Button miniStmtBtn = createActionCard("📄 Mini Statement", "View last 5 recent transactions", UIThemeFX.WARNING_HEX);
        Button historyBtn  = createActionCard("📊 Passbook / History", "Comprehensive transaction audit ledger", "#6A1B9A");
        Button profileBtn  = createActionCard("ℹ Account Profile", "View your registration profile details", "#455A64");

        debitBtn.setOnAction(e -> handleDebit());
        creditBtn.setOnAction(e -> handleCredit());
        transferBtn.setOnAction(e -> handleTransfer());
        miniStmtBtn.setOnAction(e -> showMiniStatement());
        historyBtn.setOnAction(e -> app.showTransactions(currentAccount));
        profileBtn.setOnAction(e -> showAccountDetails());

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(33.33);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(33.33);
        ColumnConstraints col3 = new ColumnConstraints();
        col3.setPercentWidth(33.33);
        grid.getColumnConstraints().addAll(col1, col2, col3);

        grid.add(debitBtn, 0, 0);
        grid.add(creditBtn, 1, 0);
        grid.add(transferBtn, 2, 0);
        grid.add(miniStmtBtn, 0, 1);
        grid.add(historyBtn, 1, 1);
        grid.add(profileBtn, 2, 1);

        content.getChildren().add(grid);

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-border-color: transparent;");
        return scroll;
    }

    private Button createActionCard(String title, String subtitle, String accentHex) {
        Button card = new Button();
        card.setMaxWidth(Double.MAX_VALUE);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(16, 18, 16, 18));

        VBox box = new VBox(4);
        Label tLbl = new Label(title);
        tLbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
        tLbl.setTextFill(Color.web(accentHex));

        Label sLbl = new Label(subtitle);
        sLbl.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));
        sLbl.setTextFill(Color.web(UIThemeFX.TEXT_MUTED_HEX));
        sLbl.setWrapText(true);

        box.getChildren().addAll(tLbl, sLbl);
        card.setGraphic(box);

        String baseStyle =
                "-fx-background-color: #FFFFFF; " +
                "-fx-background-radius: 8px; " +
                "-fx-border-color: " + UIThemeFX.BORDER_HEX + "; " +
                "-fx-border-radius: 8px; " +
                "-fx-cursor: hand; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.04), 6, 0, 0, 2);";

        String hoverStyle =
                "-fx-background-color: #F8FAFC; " +
                "-fx-background-radius: 8px; " +
                "-fx-border-color: " + accentHex + "; " +
                "-fx-border-radius: 8px; " +
                "-fx-cursor: hand; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 4);";

        card.setStyle(baseStyle);
        card.setOnMouseEntered(e -> card.setStyle(hoverStyle));
        card.setOnMouseExited(e -> card.setStyle(baseStyle));

        return card;
    }

    // ─── Actions & Dialogs ───────────────────────────────────────────────────
    private void refreshAccountData() {
        if (currentUser == null) return;
        try {
            currentAccount = accountDAO.getAccountByEmail(currentUser.getEmail());
            if (currentAccount != null) {
                welcomeLabel.setText("Welcome Back, " + currentAccount.getFullName());
                accountNumLabel.setText("A/C Number: " + currentAccount.getAccountNumber());
                accountTypeLabel.setText("Type: " + currentAccount.getAccountType() + " ACCOUNT");

                if (currentAccount.isActive()) {
                    statusBadge.setText("● ACTIVE");
                    statusBadge.setTextFill(Color.web(UIThemeFX.SUCCESS_HEX));
                } else {
                    statusBadge.setText("● FROZEN");
                    statusBadge.setTextFill(Color.web(UIThemeFX.DANGER_HEX));
                }

                if (isBalanceRevealed) {
                    balanceValueLabel.setText(String.format("₹ %.2f", currentAccount.getBalance()));
                } else {
                    balanceValueLabel.setText("₹ ••••••••");
                }
            }
        } catch (SQLException e) {
            UIThemeFX.showAlert(Alert.AlertType.ERROR, "Refresh Failed", "Database Error", e.getMessage());
        }
    }

    private void toggleBalanceVisibility() {
        if (isBalanceRevealed) {
            isBalanceRevealed = false;
            balanceValueLabel.setText("₹ ••••••••");
            balanceToggleBtn.setText("👁 Show Balance");
            balanceToggleBtn.setStyle("-fx-background-color: " + UIThemeFX.SUCCESS_HEX + "; -fx-text-fill: white; -fx-background-radius: 6px; -fx-cursor: hand;");
        } else {
            Optional<String> pinOpt = UIThemeFX.promptPinDialog("Security Check", "Enter Security PIN to View Balance:");
            if (!pinOpt.isPresent() || pinOpt.get().isEmpty()) return;

            try {
                if (accountDAO.verifyPin(currentAccount.getAccountNumber(), pinOpt.get())) {
                    isBalanceRevealed = true;
                    balanceValueLabel.setText(String.format("₹ %.2f", currentAccount.getBalance()));
                    balanceToggleBtn.setText("🙈 Hide Balance");
                    balanceToggleBtn.setStyle("-fx-background-color: " + UIThemeFX.WARNING_HEX + "; -fx-text-fill: white; -fx-background-radius: 6px; -fx-cursor: hand;");
                } else {
                    UIThemeFX.showAlert(Alert.AlertType.ERROR, "Access Denied", "Incorrect PIN", "The security PIN entered is incorrect.");
                }
            } catch (SQLException e) {
                UIThemeFX.showAlert(Alert.AlertType.ERROR, "Database Error", "Verification Failed", e.getMessage());
            }
        }
    }

    private void handleDebit() {
        if (!checkAccountActive()) return;

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Debit Money");
        dialog.setHeaderText("Withdraw funds securely from Account " + currentAccount.getAccountNumber());
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(12);
        grid.setPadding(new Insets(20));

        TextField amountField = UIThemeFX.createTextField("Amount in ₹");
        PasswordField pinField = UIThemeFX.createPasswordField("Security PIN");

        grid.add(new Label("Amount to Debit (₹):"), 0, 0);
        grid.add(amountField, 1, 0);
        grid.add(new Label("Security PIN:"), 0, 1);
        grid.add(pinField, 1, 1);
        dialog.getDialogPane().setContent(grid);

        Optional<ButtonType> res = dialog.showAndWait();
        if (res.isPresent() && res.get() == ButtonType.OK) {
            try {
                double amount = Double.parseDouble(amountField.getText().trim());
                String pin = pinField.getText().trim();

                boolean ok = accountDAO.debitMoney(currentAccount.getAccountNumber(), amount, pin);
                if (ok) {
                    UIThemeFX.showAlert(Alert.AlertType.INFORMATION, "Transaction Successful", "Debit Completed",
                            String.format("₹ %.2f debited successfully from your account.", amount));
                    refreshAccountData();
                } else {
                    UIThemeFX.showAlert(Alert.AlertType.ERROR, "Transaction Failed", "Debit Failed", "Transaction could not be completed.");
                }
            } catch (NumberFormatException e) {
                UIThemeFX.showAlert(Alert.AlertType.WARNING, "Invalid Input", "Invalid Amount", "Please enter a valid numeric amount.");
            } catch (Exception e) {
                UIThemeFX.showAlert(Alert.AlertType.ERROR, "Transaction Failed", "Error", e.getMessage());
            }
        }
    }

    private void handleCredit() {
        if (!checkAccountActive()) return;

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Credit / Deposit Money");
        dialog.setHeaderText("Deposit funds into Account " + currentAccount.getAccountNumber());
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(12);
        grid.setPadding(new Insets(20));

        TextField amountField = UIThemeFX.createTextField("Amount in ₹");
        PasswordField pinField = UIThemeFX.createPasswordField("Security PIN");

        grid.add(new Label("Amount to Credit (₹):"), 0, 0);
        grid.add(amountField, 1, 0);
        grid.add(new Label("Security PIN:"), 0, 1);
        grid.add(pinField, 1, 1);
        dialog.getDialogPane().setContent(grid);

        Optional<ButtonType> res = dialog.showAndWait();
        if (res.isPresent() && res.get() == ButtonType.OK) {
            try {
                double amount = Double.parseDouble(amountField.getText().trim());
                String pin = pinField.getText().trim();

                boolean ok = accountDAO.creditMoney(currentAccount.getAccountNumber(), amount, pin);
                if (ok) {
                    UIThemeFX.showAlert(Alert.AlertType.INFORMATION, "Deposit Successful", "Credit Completed",
                            String.format("₹ %.2f deposited successfully into your account.", amount));
                    refreshAccountData();
                } else {
                    UIThemeFX.showAlert(Alert.AlertType.ERROR, "Transaction Failed", "Deposit Failed", "Deposit could not be completed.");
                }
            } catch (NumberFormatException e) {
                UIThemeFX.showAlert(Alert.AlertType.WARNING, "Invalid Input", "Invalid Amount", "Please enter a valid numeric amount.");
            } catch (Exception e) {
                UIThemeFX.showAlert(Alert.AlertType.ERROR, "Deposit Failed", "Error", e.getMessage());
            }
        }
    }

    private void handleTransfer() {
        if (!checkAccountActive()) return;

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Fund Transfer (NEFT/IMPS)");
        dialog.setHeaderText("Send money instantly from Account " + currentAccount.getAccountNumber());
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(12);
        grid.setPadding(new Insets(20));

        TextField receiverField = UIThemeFX.createTextField("Receiver Account Number");
        TextField amountField = UIThemeFX.createTextField("Amount in ₹");
        PasswordField pinField = UIThemeFX.createPasswordField("Your Security PIN");

        grid.add(new Label("Receiver A/C Number:"), 0, 0);
        grid.add(receiverField, 1, 0);
        grid.add(new Label("Amount to Transfer (₹):"), 0, 1);
        grid.add(amountField, 1, 1);
        grid.add(new Label("Your Security PIN:"), 0, 2);
        grid.add(pinField, 1, 2);
        dialog.getDialogPane().setContent(grid);

        Optional<ButtonType> res = dialog.showAndWait();
        if (res.isPresent() && res.get() == ButtonType.OK) {
            try {
                long receiverAcc = Long.parseLong(receiverField.getText().trim());
                double amount = Double.parseDouble(amountField.getText().trim());
                String pin = pinField.getText().trim();

                boolean ok = accountDAO.transferMoney(currentAccount.getAccountNumber(), receiverAcc, amount, pin);
                if (ok) {
                    UIThemeFX.showAlert(Alert.AlertType.INFORMATION, "Transfer Complete", "Funds Sent Successfully",
                            String.format("₹ %.2f transferred to Account %d successfully!", amount, receiverAcc));
                    refreshAccountData();
                } else {
                    UIThemeFX.showAlert(Alert.AlertType.ERROR, "Transfer Failed", "Error", "Fund transfer failed.");
                }
            } catch (NumberFormatException e) {
                UIThemeFX.showAlert(Alert.AlertType.WARNING, "Invalid Input", "Invalid Account or Amount", "Please enter valid numeric values for Account and Amount.");
            } catch (Exception e) {
                UIThemeFX.showAlert(Alert.AlertType.ERROR, "Transfer Failed", "Error", e.getMessage());
            }
        }
    }

    private void showMiniStatement() {
        Optional<String> pinOpt = UIThemeFX.promptPinDialog("Security Verification", "Enter Security PIN to view Mini Statement:");
        if (!pinOpt.isPresent() || pinOpt.get().isEmpty()) return;

        try {
            if (!accountDAO.verifyPin(currentAccount.getAccountNumber(), pinOpt.get())) {
                UIThemeFX.showAlert(Alert.AlertType.ERROR, "Access Denied", "Incorrect PIN", "Invalid Security PIN!");
                return;
            }

            List<Transaction> list = transactionDAO.getHistory(currentAccount.getAccountNumber(), 5);
            if (list.isEmpty()) {
                UIThemeFX.showAlert(Alert.AlertType.INFORMATION, "Mini Statement", "No Transactions", "No transactions recorded yet.");
                return;
            }

            Dialog<Void> dialog = new Dialog<>();
            dialog.setTitle("Mini Statement");
            dialog.setHeaderText("Last 5 Transactions — A/C " + currentAccount.getAccountNumber());
            dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

            TableView<Transaction> table = new TableView<>();
            table.setPrefWidth(650);
            table.setPrefHeight(220);

            TableColumn<Transaction, String> timeCol = new TableColumn<>("Time");
            timeCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTimestamp() != null ? d.getValue().getTimestamp().toString().substring(0, 19) : "N/A"));
            timeCol.setPrefWidth(140);

            TableColumn<Transaction, String> typeCol = new TableColumn<>("Type");
            typeCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getType()));
            typeCol.setPrefWidth(110);

            TableColumn<Transaction, String> amtCol = new TableColumn<>("Amount");
            amtCol.setCellValueFactory(d -> new SimpleStringProperty(String.format("₹ %.2f", d.getValue().getAmount())));
            amtCol.setPrefWidth(100);

            TableColumn<Transaction, String> descCol = new TableColumn<>("Description");
            descCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDescription()));
            descCol.setPrefWidth(180);

            TableColumn<Transaction, String> balCol = new TableColumn<>("Balance After");
            balCol.setCellValueFactory(d -> new SimpleStringProperty(String.format("₹ %.2f", d.getValue().getBalanceAfter())));
            balCol.setPrefWidth(110);

            table.getColumns().addAll(timeCol, typeCol, amtCol, descCol, balCol);
            table.getItems().addAll(list);

            dialog.getDialogPane().setContent(table);
            dialog.showAndWait();

        } catch (SQLException e) {
            UIThemeFX.showAlert(Alert.AlertType.ERROR, "System Error", "Error", e.getMessage());
        }
    }

    private void showAccountDetails() {
        if (currentAccount == null) return;
        String info = String.format(
                "Account Holder : %s\n" +
                "Account Number : %d\n" +
                "Registered Email : %s\n" +
                "Account Type   : %s\n" +
                "Account Status : %s\n" +
                "Opened On      : %s",
                currentAccount.getFullName(),
                currentAccount.getAccountNumber(),
                currentAccount.getUserEmail(),
                currentAccount.getAccountType(),
                currentAccount.getStatus(),
                currentAccount.getCreatedAt() != null ? currentAccount.getCreatedAt().toString() : "N/A"
        );
        UIThemeFX.showAlert(Alert.AlertType.INFORMATION, "Account Profile", "Account Details", info);
    }

    private void promptOpenAccount() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Open Bank Account");
        dialog.setHeaderText("Create a new digital banking account");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(12);
        grid.setPadding(new Insets(20));

        TextField nameFld = UIThemeFX.createTextField("Holder Name");
        nameFld.setText(currentUser.getFullName());

        TextField balanceFld = UIThemeFX.createTextField("Initial Deposit (min ₹ 500)");
        balanceFld.setText("1000.00");

        PasswordField pinFld = UIThemeFX.createPasswordField("4-6 digit PIN");

        ComboBox<String> typeBox = new ComboBox<>();
        typeBox.getItems().addAll("SAVINGS", "CURRENT");
        typeBox.setValue("SAVINGS");

        grid.add(new Label("Full Name:"), 0, 0);
        grid.add(nameFld, 1, 0);
        grid.add(new Label("Initial Deposit (₹ min. 500):"), 0, 1);
        grid.add(balanceFld, 1, 1);
        grid.add(new Label("Security PIN (4-6 digits):"), 0, 2);
        grid.add(pinFld, 1, 2);
        grid.add(new Label("Account Type:"), 0, 3);
        grid.add(typeBox, 1, 3);
        dialog.getDialogPane().setContent(grid);

        Optional<ButtonType> res = dialog.showAndWait();
        if (res.isPresent() && res.get() == ButtonType.OK) {
            try {
                String name = nameFld.getText().trim();
                double balance = Double.parseDouble(balanceFld.getText().trim());
                String pin = pinFld.getText().trim();
                String type = typeBox.getValue();

                if (balance < 500) {
                    UIThemeFX.showAlert(Alert.AlertType.WARNING, "Invalid Deposit", "Minimum Balance Required", "Minimum opening deposit is ₹ 500.");
                    return;
                }
                if (!PasswordUtil.isValidPin(pin)) {
                    UIThemeFX.showAlert(Alert.AlertType.WARNING, "Invalid PIN", "Format Error", "Security PIN must be 4 to 6 numeric digits.");
                    return;
                }

                Account newAcc = new Account(0, currentUser.getEmail(), name, balance, pin, type);
                long accNum = accountDAO.openAccount(newAcc);

                UIThemeFX.showAlert(Alert.AlertType.INFORMATION, "Account Created", "Welcome to Apex Digital Banking!",
                        "Congratulations!\nYour Bank Account has been opened successfully!\n\nAccount Number: " + accNum);

                Account createdAcc = accountDAO.getAccountByNumber(accNum);
                app.showDashboard(currentUser, createdAcc);

            } catch (NumberFormatException e) {
                UIThemeFX.showAlert(Alert.AlertType.WARNING, "Invalid Input", "Numeric Amount Required", "Please enter a valid numeric deposit amount.");
            } catch (Exception e) {
                UIThemeFX.showAlert(Alert.AlertType.ERROR, "Creation Failed", "Error", e.getMessage());
            }
        }
    }

    private boolean checkAccountActive() {
        if (currentAccount == null) return false;
        if (!currentAccount.isActive()) {
            UIThemeFX.showAlert(Alert.AlertType.WARNING, "Account Frozen", "Action Blocked",
                    "Your account is FROZEN by Bank Administration.\nDebit, credit, and transfer operations are locked.");
            return false;
        }
        return true;
    }
}

