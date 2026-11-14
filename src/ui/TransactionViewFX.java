package Projects_java.Banking_System.ui;

import Projects_java.Banking_System.BankingFXApp;
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

/**
 * TransactionViewFX — JavaFX Passbook & Transaction Ledger screen.
 * Displays color-coded transactions, type filtering, and balance audits.
 */
public class TransactionViewFX extends BorderPane {

    private final BankingFXApp app;
    private final TransactionDAO transactionDAO;

    private Account currentAccount;
    private User currentViewer;
    private TableView<Transaction> table;
    private ObservableList<Transaction> masterList = FXCollections.observableArrayList();
    private ObservableList<Transaction> filteredList = FXCollections.observableArrayList();

    private ComboBox<String> filterBox;
    private Label countLabel;
    private Button backBtn;

    public TransactionViewFX(BankingFXApp app, TransactionDAO transactionDAO) {
        this.app = app;
        this.transactionDAO = transactionDAO;

        setStyle("-fx-background-color: " + UIThemeFX.BG_LIGHT_HEX + ";");

        // Top Header
        HBox topHeader = new HBox(15);
        topHeader.setAlignment(Pos.CENTER_LEFT);
        topHeader.setPadding(new Insets(16, 24, 16, 24));
        topHeader.setStyle("-fx-background-color: " + UIThemeFX.PRIMARY_HEX + ";");

        Label title = new Label("Account Passbook & Transaction Ledger");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        title.setTextFill(Color.WHITE);
        HBox.setHgrow(title, Priority.ALWAYS);

        backBtn = UIThemeFX.createButton("← Back to Dashboard", UIThemeFX.PRIMARY_HOVER_HEX, "#1E88E5");
        backBtn.setOnAction(e -> app.showDashboardAfterTx());

        topHeader.getChildren().addAll(title, backBtn);
        setTop(topHeader);

        // Center Content: Toolbar + Table
        VBox centerBox = new VBox(15);
        centerBox.setPadding(new Insets(20, 24, 20, 24));

        // Toolbar
        HBox toolbar = new HBox(15);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setPadding(new Insets(12, 16, 12, 16));
        toolbar.setStyle(
                "-fx-background-color: #FFFFFF; " +
                "-fx-background-radius: 8px; " +
                "-fx-border-color: " + UIThemeFX.BORDER_HEX + "; " +
                "-fx-border-radius: 8px;"
        );

        Label filterLbl = new Label("Filter by Type:");
        filterLbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));

        filterBox = new ComboBox<>();
        filterBox.getItems().addAll("ALL TRANSACTIONS", "CREDIT ONLY", "DEBIT ONLY", "TRANSFERS ONLY");
        filterBox.setValue("ALL TRANSACTIONS");
        filterBox.setOnAction(e -> applyFilter());

        Button refreshBtn = UIThemeFX.createButton("🔄 Refresh", UIThemeFX.PRIMARY_HEX, UIThemeFX.PRIMARY_HOVER_HEX);
        refreshBtn.setOnAction(e -> loadTransactions());

        countLabel = new Label("Showing 0 transactions");
        countLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 13));
        countLabel.setTextFill(Color.web(UIThemeFX.TEXT_MUTED_HEX));

        toolbar.getChildren().addAll(filterLbl, filterBox, refreshBtn, countLabel);

        // Table
        table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<Transaction, String> idCol = new TableColumn<>("Txn ID");
        idCol.setCellValueFactory(d -> new SimpleStringProperty("#" + d.getValue().getId()));
        idCol.setPrefWidth(70);

        TableColumn<Transaction, String> timeCol = new TableColumn<>("Date & Time");
        timeCol.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getTimestamp() != null ? d.getValue().getTimestamp().toString().substring(0, 19) : "N/A"));
        timeCol.setPrefWidth(160);

        TableColumn<Transaction, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getType()));
        typeCol.setPrefWidth(130);

        // Custom Cell styling for Type column (Green for credits, Red for debits)
        typeCol.setCellFactory(column -> new TableCell<Transaction, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
                    if ("CREDIT".equalsIgnoreCase(item) || "TRANSFER_IN".equalsIgnoreCase(item)) {
                        setStyle("-fx-text-fill: #2E7D32; -fx-alignment: CENTER-LEFT;");
                    } else if ("DEBIT".equalsIgnoreCase(item) || "TRANSFER_OUT".equalsIgnoreCase(item)) {
                        setStyle("-fx-text-fill: #C62828; -fx-alignment: CENTER-LEFT;");
                    } else {
                        setStyle("-fx-text-fill: #212121; -fx-alignment: CENTER-LEFT;");
                    }
                }
            }
        });

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

        table.getColumns().addAll(idCol, timeCol, typeCol, amtCol, descCol, balCol, refCol);
        table.setItems(filteredList);

        centerBox.getChildren().addAll(toolbar, table);
        setCenter(centerBox);
    }

    public void setAccount(Account account) {
        setAccount(account, null);
    }

    public void setAccount(Account account, User viewer) {
        this.currentAccount = account;
        this.currentViewer = viewer;

        if (viewer != null && "ADMIN".equalsIgnoreCase(viewer.getRole())) {
            backBtn.setText("← Back to Admin Console");
        } else {
            backBtn.setText("← Back to Dashboard");
        }

        loadTransactions();
    }

    public void loadTransactions() {
        if (currentAccount == null) return;
        try {
            List<Transaction> list = transactionDAO.getHistory(currentAccount.getAccountNumber(), 100);
            masterList.setAll(list);
            applyFilter();
        } catch (SQLException e) {
            UIThemeFX.showAlert(Alert.AlertType.ERROR, "System Error", "Error Loading Transactions", e.getMessage());
        }
    }

    private void applyFilter() {
        String filter = filterBox.getValue();
        List<Transaction> res = new ArrayList<>();

        for (Transaction t : masterList) {
            String type = t.getType();
            if ("CREDIT ONLY".equals(filter)) {
                if ("CREDIT".equalsIgnoreCase(type) || "TRANSFER_IN".equalsIgnoreCase(type)) res.add(t);
            } else if ("DEBIT ONLY".equals(filter)) {
                if ("DEBIT".equalsIgnoreCase(type) || "TRANSFER_OUT".equalsIgnoreCase(type)) res.add(t);
            } else if ("TRANSFERS ONLY".equals(filter)) {
                if (type != null && type.toUpperCase().contains("TRANSFER")) res.add(t);
            } else {
                res.add(t);
            }
        }

        filteredList.setAll(res);
        countLabel.setText(String.format("Showing %d of %d transactions", res.size(), masterList.size()));
    }
}

