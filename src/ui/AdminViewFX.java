package Projects_java.Banking_System.ui;

import Projects_java.Banking_System.BankingFXApp;
import Projects_java.Banking_System.dao.AccountDAO;
import Projects_java.Banking_System.dao.TransactionDAO;
import Projects_java.Banking_System.model.Account;
import Projects_java.Banking_System.model.Transaction;
import Projects_java.Banking_System.model.User;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * AdminViewFX — JavaFX Administrative & Audit Console for Apex Digital Banking.
 * Allows bank personnel to monitor accounts, freeze/unfreeze accounts for fraud prevention,
 * view individual account ledgers, and audit system-wide transaction streams.
 */
public class AdminViewFX extends BorderPane {

    private final BankingFXApp app;
    private final AccountDAO accountDAO;
    private final TransactionDAO transactionDAO;

    private User currentAdmin;
    private TableView<Account> accountsTable;
    private ObservableList<Account> masterAccounts = FXCollections.observableArrayList();
    private ObservableList<Account> filteredAccounts = FXCollections.observableArrayList();

    private TableView<Transaction> auditTable;
    private ObservableList<Transaction> auditTransactions = FXCollections.observableArrayList();

    private TextField searchField;

    public AdminViewFX(BankingFXApp app, AccountDAO accountDAO, TransactionDAO transactionDAO) {
        this.app = app;
        this.accountDAO = accountDAO;
        this.transactionDAO = transactionDAO;

        setStyle("-fx-background-color: " + UIThemeFX.BG_LIGHT_HEX + ";");

        // Top Header
        HBox topHeader = new HBox(15);
        topHeader.setAlignment(Pos.CENTER_LEFT);
        topHeader.setPadding(new Insets(16, 24, 16, 24));
        topHeader.setStyle("-fx-background-color: #212121;"); // Slate Dark for Admin

        Label title = new Label("🛡 Bank Administration & Audit Console");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        title.setTextFill(Color.WHITE);
        HBox.setHgrow(title, Priority.ALWAYS);

        HBox controls = new HBox(10);
        controls.setAlignment(Pos.CENTER_RIGHT);

        Button refreshBtn = UIThemeFX.createButton("🔄 Refresh Data", "#424242", "#616161");
        refreshBtn.setOnAction(e -> refreshAll());

        Button logoutBtn = UIThemeFX.createButton("Admin Logout", UIThemeFX.DANGER_HEX, UIThemeFX.DANGER_HOVER_HEX);
        logoutBtn.setOnAction(e -> app.showLogin());

        controls.getChildren().addAll(refreshBtn, logoutBtn);
        topHeader.getChildren().addAll(title, controls);
        setTop(topHeader);

        // Center Tabs
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        Tab accountsTab = new Tab("👤 Customer Accounts Management", buildAccountsTab());
        Tab auditTab    = new Tab("📜 System-wide Transaction Audit", buildAuditTab());

        tabPane.getTabs().addAll(accountsTab, auditTab);
        setCenter(tabPane);
    }

    public void setAdmin(User admin) {
        this.currentAdmin = admin;
        refreshAll();
    }

    private VBox buildAccountsTab() {
        VBox box = new VBox(14);
        box.setPadding(new Insets(18, 22, 18, 22));

        // Toolbar
        HBox toolbar = new HBox(12);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setPadding(new Insets(12, 16, 12, 16));
        toolbar.setStyle(
                "-fx-background-color: #FFFFFF; " +
                "-fx-background-radius: 8px; " +
                "-fx-border-color: " + UIThemeFX.BORDER_HEX + "; " +
                "-fx-border-radius: 8px;"
        );

        Label searchLbl = new Label("Search Account / Name / Email:");
        searchLbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));

        searchField = UIThemeFX.createTextField("Type to filter...");
        searchField.setPrefWidth(220);
        searchField.textProperty().addListener((obs, oldVal, newVal) -> filterAccounts());

        Button freezeBtn = UIThemeFX.createButton("❄ Freeze Account", UIThemeFX.DANGER_HEX, UIThemeFX.DANGER_HOVER_HEX);
        freezeBtn.setOnAction(e -> handleFreeze());

        Button unfreezeBtn = UIThemeFX.createButton("✔ Unfreeze Account", UIThemeFX.SUCCESS_HEX, UIThemeFX.SUCCESS_HOVER_HEX);
        unfreezeBtn.setOnAction(e -> handleUnfreeze());

        Button viewLedgerBtn = UIThemeFX.createButton("🔍 View Account Ledger", UIThemeFX.PRIMARY_HEX, UIThemeFX.PRIMARY_HOVER_HEX);
        viewLedgerBtn.setOnAction(e -> handleViewAccountLedger());

        toolbar.getChildren().addAll(searchLbl, searchField, freezeBtn, unfreezeBtn, viewLedgerBtn);

        // Table
        accountsTable = new TableView<>();
        accountsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        VBox.setVgrow(accountsTable, Priority.ALWAYS);

        TableColumn<Account, String> numCol = new TableColumn<>("A/C Number");
        numCol.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getAccountNumber())));
        numCol.setPrefWidth(120);

        TableColumn<Account, String> nameCol = new TableColumn<>("Account Holder");
        nameCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getFullName()));
        nameCol.setPrefWidth(180);

        TableColumn<Account, String> emailCol = new TableColumn<>("Email Address");
        emailCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getUserEmail()));
        emailCol.setPrefWidth(200);

        TableColumn<Account, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getAccountType()));
        typeCol.setPrefWidth(110);

        TableColumn<Account, String> balCol = new TableColumn<>("Balance (₹)");
        balCol.setCellValueFactory(d -> new SimpleStringProperty(String.format("₹ %.2f", d.getValue().getBalance())));
        balCol.setPrefWidth(120);

        TableColumn<Account, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStatus()));
        statusCol.setPrefWidth(110);

        statusCol.setCellFactory(column -> new TableCell<Account, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
                    if ("FROZEN".equalsIgnoreCase(item)) {
                        setStyle("-fx-text-fill: #C62828; -fx-alignment: CENTER-LEFT;");
                    } else {
                        setStyle("-fx-text-fill: #2E7D32; -fx-alignment: CENTER-LEFT;");
                    }
                }
            }
        });

        TableColumn<Account, String> dateCol = new TableColumn<>("Opened On");
        dateCol.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getCreatedAt() != null ? d.getValue().getCreatedAt().toString().substring(0, 19) : "N/A"));
        dateCol.setPrefWidth(160);

        accountsTable.getColumns().addAll(numCol, nameCol, emailCol, typeCol, balCol, statusCol, dateCol);
        accountsTable.setItems(filteredAccounts);

        box.getChildren().addAll(toolbar, accountsTable);
        return box;
    }

    private VBox buildAuditTab() {
        VBox box = new VBox(14);
        box.setPadding(new Insets(18, 22, 18, 22));

        auditTable = new TableView<>();
        auditTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        VBox.setVgrow(auditTable, Priority.ALWAYS);

        TableColumn<Transaction, String> idCol = new TableColumn<>("Txn ID");
        idCol.setCellValueFactory(d -> new SimpleStringProperty("#" + d.getValue().getId()));
        idCol.setPrefWidth(70);

        TableColumn<Transaction, String> timeCol = new TableColumn<>("Date & Time");
        timeCol.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getTimestamp() != null ? d.getValue().getTimestamp().toString().substring(0, 19) : "N/A"));
        timeCol.setPrefWidth(160);

        TableColumn<Transaction, String> accCol = new TableColumn<>("A/C Number");
        accCol.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getAccountNumber())));
        accCol.setPrefWidth(120);

        TableColumn<Transaction, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getType()));
        typeCol.setPrefWidth(130);

        TableColumn<Transaction, String> amtCol = new TableColumn<>("Amount (₹)");
        amtCol.setCellValueFactory(d -> new SimpleStringProperty(String.format("₹ %.2f", d.getValue().getAmount())));
        amtCol.setPrefWidth(120);

        TableColumn<Transaction, String> descCol = new TableColumn<>("Description");
        descCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDescription()));
        descCol.setPrefWidth(240);

        TableColumn<Transaction, String> balCol = new TableColumn<>("Balance After (₹)");
        balCol.setCellValueFactory(d -> new SimpleStringProperty(String.format("₹ %.2f", d.getValue().getBalanceAfter())));
        balCol.setPrefWidth(130);

        TableColumn<Transaction, String> refCol = new TableColumn<>("Counterpart A/C");
        refCol.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getRefAccount() != null && d.getValue().getRefAccount() > 0 ? String.valueOf(d.getValue().getRefAccount()) : "-"));
        refCol.setPrefWidth(120);

        auditTable.getColumns().addAll(idCol, timeCol, accCol, typeCol, amtCol, descCol, balCol, refCol);
        auditTable.setItems(auditTransactions);

        box.getChildren().add(auditTable);
        return box;
    }

    public void refreshAll() {
        loadAccounts();
        loadAuditLog();
    }

    private void loadAccounts() {
        try {
            List<Account> list = accountDAO.getAllAccounts();
            masterAccounts.setAll(list);
            filterAccounts();
        } catch (SQLException e) {
            UIThemeFX.showAlert(Alert.AlertType.ERROR, "System Error", "Failed to Load Accounts", e.getMessage());
        }
    }

    private void filterAccounts() {
        String search = searchField != null ? searchField.getText().trim().toLowerCase() : "";
        List<Account> res = new ArrayList<>();

        for (Account a : masterAccounts) {
            String accNumStr = String.valueOf(a.getAccountNumber());
            String name = a.getFullName().toLowerCase();
            String email = a.getUserEmail().toLowerCase();

            if (search.isEmpty() || accNumStr.contains(search) || name.contains(search) || email.contains(search)) {
                res.add(a);
            }
        }
        filteredAccounts.setAll(res);
    }

    private void loadAuditLog() {
        try {
            List<Transaction> list = transactionDAO.getAllTransactions(200);
            auditTransactions.setAll(list);
        } catch (SQLException e) {
            UIThemeFX.showAlert(Alert.AlertType.ERROR, "System Error", "Failed to Load Audit Log", e.getMessage());
        }
    }

    private void handleFreeze() {
        Account selected = accountsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            UIThemeFX.showAlert(Alert.AlertType.WARNING, "Selection Required", "No Account Selected", "Please select an account row from the table first.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Account Freeze");
        confirm.setHeaderText("Freeze Account " + selected.getAccountNumber());
        confirm.setContentText("Are you sure you want to FREEZE Account " + selected.getAccountNumber() + "?\nAll debit, credit, and transfer transactions will be immediately blocked.");

        Optional<ButtonType> res = confirm.showAndWait();
        if (res.isPresent() && res.get() == ButtonType.OK) {
            try {
                boolean ok = accountDAO.freezeAccount(selected.getAccountNumber());
                if (ok) {
                    UIThemeFX.showAlert(Alert.AlertType.INFORMATION, "Account Frozen", "Status Updated", "Account " + selected.getAccountNumber() + " is now FROZEN.");
                    loadAccounts();
                }
            } catch (SQLException e) {
                UIThemeFX.showAlert(Alert.AlertType.ERROR, "Freeze Failed", "Database Error", e.getMessage());
            }
        }
    }

    private void handleUnfreeze() {
        Account selected = accountsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            UIThemeFX.showAlert(Alert.AlertType.WARNING, "Selection Required", "No Account Selected", "Please select an account row from the table first.");
            return;
        }

        try {
            boolean ok = accountDAO.unfreezeAccount(selected.getAccountNumber());
            if (ok) {
                UIThemeFX.showAlert(Alert.AlertType.INFORMATION, "Account Unfrozen", "Status Updated", "Account " + selected.getAccountNumber() + " is now ACTIVE.");
                loadAccounts();
            }
        } catch (SQLException e) {
            UIThemeFX.showAlert(Alert.AlertType.ERROR, "Unfreeze Failed", "Database Error", e.getMessage());
        }
    }

    private void handleViewAccountLedger() {
        Account selected = accountsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            UIThemeFX.showAlert(Alert.AlertType.WARNING, "Selection Required", "No Account Selected", "Please select an account row from the table first.");
            return;
        }

        app.showTransactions(selected);
    }
}

