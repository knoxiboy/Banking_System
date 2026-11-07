package Projects_java.Banking_System.ui;

import Projects_java.Banking_System.BankingGUI;
import Projects_java.Banking_System.dao.AccountDAO;
import Projects_java.Banking_System.dao.TransactionDAO;
import Projects_java.Banking_System.model.Account;
import Projects_java.Banking_System.model.Transaction;
import Projects_java.Banking_System.model.User;

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
 * Administrative Control Console.
 * Allows bank staff to review all customer accounts, freeze/unfreeze accounts to prevent fraud,
 * inspect individual account ledgers, and audit complete system-wide transaction streams.
 */
public class AdminPanel extends JPanel {

    private final BankingGUI app;
    private final AccountDAO accountDAO;
    private final TransactionDAO transactionDAO;

    private User currentAdmin;
    private JTable accountsTable;
    private DefaultTableModel accountsModel;

    private JTable auditTable;
    private DefaultTableModel auditModel;

    private JTextField searchField;
    private List<Account> cachedAccounts;

    public AdminPanel(BankingGUI app, AccountDAO accountDAO, TransactionDAO transactionDAO) {
        this.app = app;
        this.accountDAO = accountDAO;
        this.transactionDAO = transactionDAO;

        setLayout(new BorderLayout());
        setBackground(UITheme.BG_LIGHT);

        // Header
        JPanel topHeader = new JPanel(new BorderLayout());
        topHeader.setBackground(new Color(33, 33, 33)); // Slate black for Admin
        topHeader.setBorder(new EmptyBorder(16, 24, 16, 24));

        JLabel title = new JLabel("🛡 Bank Administration & Audit Console");
        title.setFont(UITheme.TITLE_FONT);
        title.setForeground(Color.WHITE);

        JPanel rightControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightControls.setOpaque(false);

        JButton refreshBtn = UITheme.createButton("🔄 Refresh Data", new Color(66, 66, 66), Color.WHITE);
        refreshBtn.addActionListener(e -> refreshAll());

        JButton logoutBtn = UITheme.createButton("Admin Logout", UITheme.DANGER, Color.WHITE);
        logoutBtn.addActionListener(e -> app.showLogin());

        rightControls.add(refreshBtn);
        rightControls.add(logoutBtn);

        topHeader.add(title, BorderLayout.WEST);
        topHeader.add(rightControls, BorderLayout.EAST);
        add(topHeader, BorderLayout.NORTH);

        // Tabs: Accounts Management vs System Audit
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(UITheme.BUTTON_FONT);

        tabbedPane.addTab("👤 Customer Accounts Management", buildAccountsTab());
        tabbedPane.addTab("📜 System-wide Transaction Audit", buildAuditTab());

        add(tabbedPane, BorderLayout.CENTER);
    }

    public void setAdmin(User admin) {
        this.currentAdmin = admin;
        refreshAll();
    }

    private JPanel buildAccountsTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(UITheme.BG_LIGHT);

        // Toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        toolbar.setBackground(Color.WHITE);
        toolbar.setBorder(new CompoundBorder(
                new LineBorder(UITheme.BORDER_COLOR, 1, false),
                new EmptyBorder(5, 15, 5, 15)
        ));

        toolbar.add(new JLabel("Search Account / Name / Email:"));
        searchField = UITheme.createTextField();
        searchField.setPreferredSize(new Dimension(200, 32));
        searchField.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                filterAccounts();
            }
        });
        toolbar.add(searchField);

        JButton freezeBtn = UITheme.createButton("❄ Freeze Account", UITheme.DANGER, Color.WHITE);
        freezeBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        freezeBtn.addActionListener(e -> handleFreeze());
        toolbar.add(freezeBtn);

        JButton unfreezeBtn = UITheme.createButton("✔ Unfreeze Account", UITheme.SUCCESS, Color.WHITE);
        unfreezeBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        unfreezeBtn.addActionListener(e -> handleUnfreeze());
        toolbar.add(unfreezeBtn);

        JButton viewTxBtn = UITheme.createButton("🔍 View Account Ledger", UITheme.PRIMARY, Color.WHITE);
        viewTxBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        viewTxBtn.addActionListener(e -> handleViewAccountLedger());
        toolbar.add(viewTxBtn);

        panel.add(toolbar, BorderLayout.NORTH);

        // Table
        String[] columns = {"A/C Number", "Account Holder", "Email Address", "Type", "Balance (₹)", "Status", "Opened On"};
        accountsModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        accountsTable = new JTable(accountsModel);
        accountsTable.setRowHeight(28);
        accountsTable.setFont(UITheme.BODY_FONT);
        accountsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        accountsTable.setShowVerticalLines(false);
        accountsTable.setGridColor(new Color(230, 235, 240));

        JTableHeader th = accountsTable.getTableHeader();
        th.setFont(new Font("Segoe UI", Font.BOLD, 13));
        th.setBackground(new Color(236, 239, 241));
        th.setForeground(UITheme.TEXT_DARK);
        th.setPreferredSize(new Dimension(th.getWidth(), 34));

        // Highlight status column
        accountsTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    String status = (String) table.getValueAt(row, 5);
                    if ("FROZEN".equalsIgnoreCase(status)) {
                        c.setBackground(new Color(255, 235, 238));
                    } else {
                        c.setBackground(row % 2 == 0 ? UITheme.BG_LIGHT : Color.WHITE);
                    }
                }
                return c;
            }
        });

        JScrollPane sp = new JScrollPane(accountsTable);
        sp.setBorder(new EmptyBorder(10, 15, 10, 15));
        panel.add(sp, BorderLayout.CENTER);

        return panel;
    }

    private JPanel buildAuditTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(UITheme.BG_LIGHT);

        String[] columns = {"Txn ID", "Date & Time", "A/C Number", "Type", "Amount (₹)", "Description", "Balance After (₹)", "Counterpart A/C"};
        auditModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        auditTable = new JTable(auditModel);
        auditTable.setRowHeight(28);
        auditTable.setFont(UITheme.BODY_FONT);
        auditTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        auditTable.setShowVerticalLines(false);

        JTableHeader th = auditTable.getTableHeader();
        th.setFont(new Font("Segoe UI", Font.BOLD, 13));
        th.setBackground(new Color(236, 239, 241));
        th.setForeground(UITheme.TEXT_DARK);

        JScrollPane sp = new JScrollPane(auditTable);
        sp.setBorder(new EmptyBorder(10, 15, 10, 15));
        panel.add(sp, BorderLayout.CENTER);

        return panel;
    }

    public void refreshAll() {
        loadAccounts();
        loadAuditLog();
    }

    private void loadAccounts() {
        try {
            cachedAccounts = accountDAO.getAllAccounts();
            filterAccounts();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Failed to load accounts: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void filterAccounts() {
        if (cachedAccounts == null) return;
        accountsModel.setRowCount(0);
        String search = searchField != null ? searchField.getText().trim().toLowerCase() : "";

        for (Account a : cachedAccounts) {
            String accNumStr = String.valueOf(a.getAccountNumber());
            String name = a.getFullName().toLowerCase();
            String email = a.getUserEmail().toLowerCase();

            if (search.isEmpty() || accNumStr.contains(search) || name.contains(search) || email.contains(search)) {
                accountsModel.addRow(new Object[]{
                        a.getAccountNumber(),
                        a.getFullName(),
                        a.getUserEmail(),
                        a.getAccountType(),
                        String.format("₹ %.2f", a.getBalance()),
                        a.getStatus(),
                        a.getCreatedAt() != null ? a.getCreatedAt().toString().substring(0, 19) : "N/A"
                });
            }
        }
    }

    private void loadAuditLog() {
        try {
            List<Transaction> list = transactionDAO.getAllTransactions(200);
            auditModel.setRowCount(0);
            for (Transaction t : list) {
                auditModel.addRow(new Object[]{
                        "#" + t.getId(),
                        t.getTimestamp() != null ? t.getTimestamp().toString().substring(0, 19) : "N/A",
                        t.getAccountNumber(),
                        t.getType(),
                        String.format("₹ %.2f", t.getAmount()),
                        t.getDescription(),
                        String.format("₹ %.2f", t.getBalanceAfter()),
                        t.getRefAccount() != null && t.getRefAccount() > 0 ? t.getRefAccount() : "-"
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Failed to load audit transactions: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleFreeze() {
        int selectedRow = accountsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an account row from the table first.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        long accNum = (Long) accountsTable.getValueAt(selectedRow, 0);
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to FREEZE Account " + accNum + "?\nAll debit, credit, and transfer operations will be locked.",
                "Confirm Account Freeze", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                boolean ok = accountDAO.freezeAccount(accNum);
                if (ok) {
                    JOptionPane.showMessageDialog(this, "Account " + accNum + " is now FROZEN.", "Status Updated", JOptionPane.INFORMATION_MESSAGE);
                    loadAccounts();
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error freezing account: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleUnfreeze() {
        int selectedRow = accountsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an account row from the table first.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        long accNum = (Long) accountsTable.getValueAt(selectedRow, 0);
        try {
            boolean ok = accountDAO.unfreezeAccount(accNum);
            if (ok) {
                JOptionPane.showMessageDialog(this, "Account " + accNum + " is now ACTIVE.", "Status Updated", JOptionPane.INFORMATION_MESSAGE);
                loadAccounts();
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error unfreezing account: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleViewAccountLedger() {
        int selectedRow = accountsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an account row first.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        long accNum = (Long) accountsTable.getValueAt(selectedRow, 0);
        try {
            Account acc = accountDAO.getAccountByNumber(accNum);
            if (acc != null) {
                app.showTransactions(acc);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
