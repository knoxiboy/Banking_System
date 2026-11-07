package Projects_java.Banking_System.ui;

import Projects_java.Banking_System.BankingGUI;
import Projects_java.Banking_System.dao.TransactionDAO;
import Projects_java.Banking_System.model.Account;
import Projects_java.Banking_System.model.Transaction;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

/**
 * Transaction History and Passbook Ledger view.
 * Features an interactive JTable with color-coded transaction types (green for credit, red for debit),
 * category filtering, and balance progression audit.
 */
public class TransactionPanel extends JPanel {

    private final BankingGUI app;
    private final TransactionDAO transactionDAO;

    private Account currentAccount;
    private JTable transactionTable;
    private DefaultTableModel tableModel;
    private JComboBox<String> filterBox;
    private JLabel countLabel;

    private List<Transaction> cachedTransactions;

    public TransactionPanel(BankingGUI app, TransactionDAO transactionDAO) {
        this.app = app;
        this.transactionDAO = transactionDAO;

        setLayout(new BorderLayout());
        setBackground(UITheme.BG_LIGHT);

        // Header
        JPanel topHeader = new JPanel(new BorderLayout());
        topHeader.setBackground(UITheme.PRIMARY);
        topHeader.setBorder(new EmptyBorder(16, 24, 16, 24));

        JLabel title = new JLabel("Account Passbook & Transaction Ledger");
        title.setFont(UITheme.TITLE_FONT);
        title.setForeground(Color.WHITE);

        JButton backBtn = UITheme.createButton("← Back to Dashboard", UITheme.PRIMARY_HOVER, Color.WHITE);
        backBtn.addActionListener(e -> app.showDashboardAfterTx());

        topHeader.add(title, BorderLayout.WEST);
        topHeader.add(backBtn, BorderLayout.EAST);
        add(topHeader, BorderLayout.NORTH);

        // Filter / Controls Toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 12));
        toolbar.setBackground(Color.WHITE);
        toolbar.setBorder(new CompoundBorder(
                new LineBorder(UITheme.BORDER_COLOR, 1, false),
                new EmptyBorder(5, 15, 5, 15)
        ));

        toolbar.add(new JLabel("Filter by Type:"));
        filterBox = new JComboBox<>(new String[]{"ALL TRANSACTIONS", "CREDIT ONLY", "DEBIT ONLY", "TRANSFERS ONLY"});
        filterBox.setFont(UITheme.BODY_FONT);
        filterBox.addActionListener(e -> applyFilter());
        toolbar.add(filterBox);

        JButton refreshBtn = UITheme.createButton("🔄 Refresh", UITheme.PRIMARY, Color.WHITE);
        refreshBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        refreshBtn.addActionListener(e -> loadTransactions());
        toolbar.add(refreshBtn);

        countLabel = new JLabel("Total Transactions: 0");
        countLabel.setFont(UITheme.LABEL_FONT);
        countLabel.setForeground(UITheme.TEXT_MUTED);
        toolbar.add(countLabel);

        add(toolbar, BorderLayout.AFTER_LAST_LINE);

        // Table Setup
        String[] columnNames = {"Txn ID", "Date & Time", "Type", "Amount (₹)", "Description", "Balance After (₹)", "Counterpart A/C"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // read-only
            }
        };

        transactionTable = new JTable(tableModel);
        transactionTable.setRowHeight(28);
        transactionTable.setFont(UITheme.BODY_FONT);
        transactionTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        transactionTable.setShowVerticalLines(false);
        transactionTable.setGridColor(new Color(230, 235, 240));

        JTableHeader th = transactionTable.getTableHeader();
        th.setFont(new Font("Segoe UI", Font.BOLD, 13));
        th.setBackground(new Color(236, 239, 241));
        th.setForeground(UITheme.TEXT_DARK);
        th.setPreferredSize(new Dimension(th.getWidth(), 34));

        // Color-coded cell renderer
        transactionTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                String type = (String) table.getValueAt(row, 2);
                if (!isSelected) {
                    if ("CREDIT".equalsIgnoreCase(type) || "TRANSFER_IN".equalsIgnoreCase(type)) {
                        c.setForeground(new Color(27, 94, 32));
                        c.setBackground(row % 2 == 0 ? new Color(241, 248, 241) : Color.WHITE);
                    } else if ("DEBIT".equalsIgnoreCase(type) || "TRANSFER_OUT".equalsIgnoreCase(type)) {
                        c.setForeground(new Color(183, 28, 28));
                        c.setBackground(row % 2 == 0 ? new Color(253, 242, 242) : Color.WHITE);
                    } else {
                        c.setForeground(UITheme.TEXT_DARK);
                        c.setBackground(row % 2 == 0 ? UITheme.BG_LIGHT : Color.WHITE);
                    }
                }
                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(transactionTable);
        scrollPane.setBorder(new EmptyBorder(15, 20, 15, 20));
        scrollPane.getViewport().setBackground(Color.WHITE);

        add(scrollPane, BorderLayout.CENTER);
    }

    public void setAccount(Account account) {
        this.currentAccount = account;
        loadTransactions();
    }

    public void loadTransactions() {
        if (currentAccount == null) return;
        try {
            cachedTransactions = transactionDAO.getHistory(currentAccount.getAccountNumber(), 100);
            applyFilter();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Failed to load transactions: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void applyFilter() {
        if (cachedTransactions == null) return;

        tableModel.setRowCount(0);
        String selectedFilter = (String) filterBox.getSelectedItem();

        int displayedCount = 0;
        for (Transaction t : cachedTransactions) {
            String type = t.getType();
            boolean matches = true;

            if ("CREDIT ONLY".equals(selectedFilter)) {
                matches = "CREDIT".equalsIgnoreCase(type) || "TRANSFER_IN".equalsIgnoreCase(type);
            } else if ("DEBIT ONLY".equals(selectedFilter)) {
                matches = "DEBIT".equalsIgnoreCase(type) || "TRANSFER_OUT".equalsIgnoreCase(type);
            } else if ("TRANSFERS ONLY".equals(selectedFilter)) {
                matches = type.toUpperCase().contains("TRANSFER");
            }

            if (matches) {
                tableModel.addRow(new Object[]{
                        "#" + t.getId(),
                        t.getTimestamp() != null ? t.getTimestamp().toString().substring(0, 19) : "N/A",
                        t.getType(),
                        String.format("₹ %.2f", t.getAmount()),
                        t.getDescription(),
                        String.format("₹ %.2f", t.getBalanceAfter()),
                        t.getRefAccount() != null && t.getRefAccount() > 0 ? t.getRefAccount() : "-"
                });
                displayedCount++;
            }
        }

        countLabel.setText(String.format("Showing %d of %d transactions", displayedCount, cachedTransactions.size()));
    }
}
