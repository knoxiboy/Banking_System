package Projects_java.Banking_System.ui;

import Projects_java.Banking_System.BankingGUI;
import Projects_java.Banking_System.dao.AccountDAO;
import Projects_java.Banking_System.dao.TransactionDAO;
import Projects_java.Banking_System.model.Account;
import Projects_java.Banking_System.model.Transaction;
import Projects_java.Banking_System.model.User;
import Projects_java.Banking_System.util.PasswordUtil;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

/**
 * Main customer dashboard for the Banking System.
 * Displays account status, hidden balance with PIN toggle, quick operations
 * (Debit, Credit, Transfer, Mini-statement, Full History), and account opening flow.
 */
public class DashboardPanel extends JPanel {

    private final BankingGUI app;
    private final AccountDAO accountDAO;
    private final TransactionDAO transactionDAO;

    private User currentUser;
    private Account currentAccount;

    private JLabel welcomeLabel;
    private JLabel accountNumLabel;
    private JLabel accountTypeLabel;
    private JLabel statusBadge;
    private JLabel balanceValueLabel;
    private JButton balanceToggleBtn;

    private boolean isBalanceRevealed = false;
    private JPanel mainContentPanel;
    private CardLayout contentCardLayout;

    public DashboardPanel(BankingGUI app, AccountDAO accountDAO, TransactionDAO transactionDAO) {
        this.app = app;
        this.accountDAO = accountDAO;
        this.transactionDAO = transactionDAO;

        setLayout(new BorderLayout());
        setBackground(UITheme.BG_LIGHT);

        contentCardLayout = new CardLayout();
        mainContentPanel = new JPanel(contentCardLayout);
        mainContentPanel.setOpaque(false);

        // Sub-panels
        mainContentPanel.add(buildNoAccountPanel(), "NO_ACCOUNT");
        mainContentPanel.add(buildActiveAccountPanel(), "ACTIVE_ACCOUNT");

        add(mainContentPanel, BorderLayout.CENTER);
    }

    /**
     * Initializes the dashboard for the currently logged-in user and account.
     */
    public void setUserAndAccount(User user, Account account) {
        this.currentUser = user;
        this.currentAccount = account;
        this.isBalanceRevealed = false;

        if (account == null) {
            contentCardLayout.show(mainContentPanel, "NO_ACCOUNT");
        } else {
            refreshAccountData();
            contentCardLayout.show(mainContentPanel, "ACTIVE_ACCOUNT");
        }
    }

    // ─── Panel 1: No Account Yet ─────────────────────────────────────────────

    private JPanel buildNoAccountPanel() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);

        JPanel topHeader = new JPanel(new BorderLayout());
        topHeader.setBackground(UITheme.PRIMARY);
        topHeader.setBorder(new EmptyBorder(16, 24, 16, 24));
        JLabel title = new JLabel("Welcome to Your Digital Bank");
        title.setFont(UITheme.TITLE_FONT);
        title.setForeground(Color.WHITE);
        JButton logoutBtn = UITheme.createButton("Logout", UITheme.DANGER, Color.WHITE);
        logoutBtn.addActionListener(e -> app.showLogin());
        topHeader.add(title, BorderLayout.WEST);
        topHeader.add(logoutBtn, BorderLayout.EAST);
        wrapper.add(topHeader, BorderLayout.NORTH);

        JPanel center = new JPanel(new GridBagLayout());
        center.setOpaque(false);

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(UITheme.CARD_BG);
        card.setBorder(new CompoundBorder(
                new LineBorder(UITheme.BORDER_COLOR, 1, true),
                new EmptyBorder(40, 40, 40, 40)
        ));
        card.setPreferredSize(new Dimension(460, 260));

        JLabel iconLbl = new JLabel("🏦");
        iconLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
        iconLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel msgLbl = new JLabel("No Bank Account Found");
        msgLbl.setFont(new Font("Segoe UI", Font.BOLD, 20));
        msgLbl.setForeground(UITheme.TEXT_DARK);
        msgLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subLbl = new JLabel("You haven't opened a bank account with us yet.");
        subLbl.setFont(UITheme.BODY_FONT);
        subLbl.setForeground(UITheme.TEXT_MUTED);
        subLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton openBtn = UITheme.createButton("Open New Bank Account", UITheme.SUCCESS, Color.WHITE);
        openBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        openBtn.addActionListener(e -> promptOpenAccount());

        card.add(iconLbl);
        card.add(Box.createVerticalStrut(10));
        card.add(msgLbl);
        card.add(Box.createVerticalStrut(6));
        card.add(subLbl);
        card.add(Box.createVerticalStrut(25));
        card.add(openBtn);

        center.add(card);
        wrapper.add(center, BorderLayout.CENTER);
        return wrapper;
    }

    // ─── Panel 2: Active Account Dashboard ───────────────────────────────────

    private JPanel buildActiveAccountPanel() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);

        // Top Navigation Bar
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(UITheme.PRIMARY);
        topBar.setBorder(new EmptyBorder(16, 24, 16, 24));

        welcomeLabel = new JLabel("Welcome Back, User");
        welcomeLabel.setFont(UITheme.TITLE_FONT);
        welcomeLabel.setForeground(Color.WHITE);

        JPanel rightControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightControls.setOpaque(false);

        JButton refreshBtn = UITheme.createButton("🔄 Refresh", UITheme.PRIMARY_HOVER, Color.WHITE);
        refreshBtn.addActionListener(e -> refreshAccountData());

        JButton logoutBtn = UITheme.createButton("Logout", UITheme.DANGER, Color.WHITE);
        logoutBtn.addActionListener(e -> {
            isBalanceRevealed = false;
            app.showLogin();
        });

        rightControls.add(refreshBtn);
        rightControls.add(logoutBtn);

        topBar.add(welcomeLabel, BorderLayout.WEST);
        topBar.add(rightControls, BorderLayout.EAST);
        wrapper.add(topBar, BorderLayout.NORTH);

        // Center Content Area
        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setOpaque(false);
        center.setBorder(new EmptyBorder(20, 24, 20, 24));

        // ─── Account Summary Card ───
        JPanel summaryCard = new JPanel(new BorderLayout(15, 10));
        summaryCard.setBackground(UITheme.CARD_BG);
        summaryCard.setBorder(new CompoundBorder(
                new LineBorder(UITheme.BORDER_COLOR, 1, true),
                new EmptyBorder(20, 24, 20, 24)
        ));
        summaryCard.setMaximumSize(new Dimension(800, 160));

        JPanel leftSummary = new JPanel(new GridLayout(3, 1, 0, 5));
        leftSummary.setOpaque(false);

        accountNumLabel = new JLabel("A/C Number: 10000100");
        accountNumLabel.setFont(UITheme.MONO_FONT);
        accountNumLabel.setForeground(UITheme.PRIMARY);

        accountTypeLabel = new JLabel("Type: SAVINGS ACCOUNT");
        accountTypeLabel.setFont(UITheme.BODY_FONT);
        accountTypeLabel.setForeground(UITheme.TEXT_MUTED);

        statusBadge = new JLabel("● ACTIVE");
        statusBadge.setFont(UITheme.LABEL_FONT);
        statusBadge.setForeground(UITheme.SUCCESS);

        leftSummary.add(accountNumLabel);
        leftSummary.add(accountTypeLabel);
        leftSummary.add(statusBadge);

        // Balance Card (Right side)
        JPanel balanceCard = new JPanel();
        balanceCard.setLayout(new BoxLayout(balanceCard, BoxLayout.Y_AXIS));
        balanceCard.setBackground(new Color(237, 247, 237));
        balanceCard.setBorder(new CompoundBorder(
                new LineBorder(new Color(165, 214, 167), 1, true),
                new EmptyBorder(12, 20, 12, 20)
        ));

        JLabel balTitle = new JLabel("AVAILABLE BALANCE");
        balTitle.setFont(new Font("Segoe UI", Font.BOLD, 11));
        balTitle.setForeground(new Color(46, 125, 50));

        balanceValueLabel = new JLabel("₹ ••••••••");
        balanceValueLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        balanceValueLabel.setForeground(new Color(27, 94, 32));

        balanceToggleBtn = UITheme.createButton("👁 Show Balance", UITheme.SUCCESS, Color.WHITE);
        balanceToggleBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        balanceToggleBtn.addActionListener(e -> toggleBalanceVisibility());

        balanceCard.add(balTitle);
        balanceCard.add(Box.createVerticalStrut(4));
        balanceCard.add(balanceValueLabel);
        balanceCard.add(Box.createVerticalStrut(8));
        balanceCard.add(balanceToggleBtn);

        summaryCard.add(leftSummary, BorderLayout.WEST);
        summaryCard.add(balanceCard, BorderLayout.EAST);
        center.add(summaryCard);
        center.add(Box.createVerticalStrut(25));

        // ─── Actions Section ───
        JLabel actionsTitle = new JLabel("Banking Services & Operations");
        actionsTitle.setFont(UITheme.HEADER_FONT);
        actionsTitle.setForeground(UITheme.TEXT_DARK);
        actionsTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        center.add(actionsTitle);
        center.add(Box.createVerticalStrut(12));

        // 2x3 Grid of Action Cards
        JPanel actionsGrid = new JPanel(new GridLayout(2, 3, 15, 15));
        actionsGrid.setOpaque(false);
        actionsGrid.setMaximumSize(new Dimension(800, 220));

        JButton debitBtn = createActionCard("💳 Debit Money", "Withdraw funds securely with PIN", new Color(198, 40, 40));
        JButton creditBtn = createActionCard("💰 Credit Money", "Deposit funds into your account", new Color(46, 125, 50));
        JButton transferBtn = createActionCard("🔄 Transfer Funds", "Send money instantly to any account", UITheme.PRIMARY);
        JButton miniStmtBtn = createActionCard("📄 Mini Statement", "View last 5 recent transactions", new Color(239, 108, 0));
        JButton historyBtn = createActionCard("📊 Passbook / History", "Comprehensive transaction audit ledger", new Color(106, 27, 154));
        JButton accountDetailsBtn = createActionCard("ℹ Account Info", "View your registration profile details", new Color(69, 90, 100));

        debitBtn.addActionListener(e -> handleDebit());
        creditBtn.addActionListener(e -> handleCredit());
        transferBtn.addActionListener(e -> handleTransfer());
        miniStmtBtn.addActionListener(e -> showMiniStatement());
        historyBtn.addActionListener(e -> app.showTransactions(currentAccount));
        accountDetailsBtn.addActionListener(e -> showAccountDetails());

        actionsGrid.add(debitBtn);
        actionsGrid.add(creditBtn);
        actionsGrid.add(transferBtn);
        actionsGrid.add(miniStmtBtn);
        actionsGrid.add(historyBtn);
        actionsGrid.add(accountDetailsBtn);

        center.add(actionsGrid);

        JScrollPane scrollPane = new JScrollPane(center);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        wrapper.add(scrollPane, BorderLayout.CENTER);

        return wrapper;
    }

    private JButton createActionCard(String title, String subtitle, Color accentColor) {
        JButton card = new JButton();
        card.setLayout(new BorderLayout(5, 5));
        card.setBackground(UITheme.CARD_BG);
        card.setBorder(new CompoundBorder(
                new LineBorder(UITheme.BORDER_COLOR, 1, true),
                new EmptyBorder(14, 16, 14, 16)
        ));
        card.setFocusPainted(false);
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel tLbl = new JLabel(title);
        tLbl.setFont(new Font("Segoe UI", Font.BOLD, 15));
        tLbl.setForeground(accentColor);

        JLabel sLbl = new JLabel("<html><small>" + subtitle + "</small></html>");
        sLbl.setFont(UITheme.SUBTITLE_FONT);
        sLbl.setForeground(UITheme.TEXT_MUTED);

        card.add(tLbl, BorderLayout.NORTH);
        card.add(sLbl, BorderLayout.CENTER);

        card.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                card.setBackground(new Color(240, 244, 248));
                card.setBorder(new CompoundBorder(
                        new LineBorder(accentColor, 1, true),
                        new EmptyBorder(14, 16, 14, 16)
                ));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                card.setBackground(UITheme.CARD_BG);
                card.setBorder(new CompoundBorder(
                        new LineBorder(UITheme.BORDER_COLOR, 1, true),
                        new EmptyBorder(14, 16, 14, 16)
                ));
            }
        });

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
                    statusBadge.setForeground(UITheme.SUCCESS);
                } else {
                    statusBadge.setText("● FROZEN");
                    statusBadge.setForeground(UITheme.DANGER);
                }

                if (isBalanceRevealed) {
                    balanceValueLabel.setText(String.format("₹ %.2f", currentAccount.getBalance()));
                } else {
                    balanceValueLabel.setText("₹ ••••••••");
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Failed to refresh account: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void toggleBalanceVisibility() {
        if (isBalanceRevealed) {
            isBalanceRevealed = false;
            balanceValueLabel.setText("₹ ••••••••");
            balanceToggleBtn.setText("👁 Show Balance");
            balanceToggleBtn.setBackground(UITheme.SUCCESS);
        } else {
            String pin = promptPin("Enter Security PIN to View Balance:");
            if (pin == null) return;

            try {
                if (accountDAO.verifyPin(currentAccount.getAccountNumber(), pin)) {
                    isBalanceRevealed = true;
                    balanceValueLabel.setText(String.format("₹ %.2f", currentAccount.getBalance()));
                    balanceToggleBtn.setText("🙈 Hide Balance");
                    balanceToggleBtn.setBackground(UITheme.WARNING);
                } else {
                    JOptionPane.showMessageDialog(this, "Incorrect Security PIN!", "Access Denied", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Database Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleDebit() {
        if (!checkAccountActive()) return;

        JPanel panel = new JPanel(new GridLayout(2, 2, 8, 8));
        JTextField amountFld = UITheme.createTextField();
        JPasswordField pinFld = UITheme.createPasswordField();

        panel.add(new JLabel("Amount to Debit (₹):"));
        panel.add(amountFld);
        panel.add(new JLabel("Security PIN:"));
        panel.add(pinFld);

        int result = JOptionPane.showConfirmDialog(this, panel, "Debit Money", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) return;

        try {
            double amount = Double.parseDouble(amountFld.getText().trim());
            String pin = new String(pinFld.getPassword()).trim();

            boolean ok = accountDAO.debitMoney(currentAccount.getAccountNumber(), amount, pin);
            if (ok) {
                JOptionPane.showMessageDialog(this,
                        String.format("₹ %.2f debited successfully!", amount),
                        "Transaction Successful", JOptionPane.INFORMATION_MESSAGE);
                refreshAccountData();
            } else {
                JOptionPane.showMessageDialog(this, "Transaction failed.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid numeric amount.", "Invalid Input", JOptionPane.WARNING_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Transaction Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleCredit() {
        if (!checkAccountActive()) return;

        JPanel panel = new JPanel(new GridLayout(2, 2, 8, 8));
        JTextField amountFld = UITheme.createTextField();
        JPasswordField pinFld = UITheme.createPasswordField();

        panel.add(new JLabel("Amount to Credit (₹):"));
        panel.add(amountFld);
        panel.add(new JLabel("Security PIN:"));
        panel.add(pinFld);

        int result = JOptionPane.showConfirmDialog(this, panel, "Credit / Deposit Money", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) return;

        try {
            double amount = Double.parseDouble(amountFld.getText().trim());
            String pin = new String(pinFld.getPassword()).trim();

            boolean ok = accountDAO.creditMoney(currentAccount.getAccountNumber(), amount, pin);
            if (ok) {
                JOptionPane.showMessageDialog(this,
                        String.format("₹ %.2f credited successfully!", amount),
                        "Deposit Successful", JOptionPane.INFORMATION_MESSAGE);
                refreshAccountData();
            } else {
                JOptionPane.showMessageDialog(this, "Transaction failed.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid numeric amount.", "Invalid Input", JOptionPane.WARNING_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Deposit Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleTransfer() {
        if (!checkAccountActive()) return;

        JPanel panel = new JPanel(new GridLayout(3, 2, 8, 8));
        JTextField receiverFld = UITheme.createTextField();
        JTextField amountFld = UITheme.createTextField();
        JPasswordField pinFld = UITheme.createPasswordField();

        panel.add(new JLabel("Receiver A/C Number:"));
        panel.add(receiverFld);
        panel.add(new JLabel("Amount to Transfer (₹):"));
        panel.add(amountFld);
        panel.add(new JLabel("Your Security PIN:"));
        panel.add(pinFld);

        int result = JOptionPane.showConfirmDialog(this, panel, "Fund Transfer (NEFT/IMPS)", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) return;

        try {
            long receiverAcc = Long.parseLong(receiverFld.getText().trim());
            double amount = Double.parseDouble(amountFld.getText().trim());
            String pin = new String(pinFld.getPassword()).trim();

            boolean ok = accountDAO.transferMoney(currentAccount.getAccountNumber(), receiverAcc, amount, pin);
            if (ok) {
                JOptionPane.showMessageDialog(this,
                        String.format("₹ %.2f transferred to A/C %d successfully!", amount, receiverAcc),
                        "Transfer Complete", JOptionPane.INFORMATION_MESSAGE);
                refreshAccountData();
            } else {
                JOptionPane.showMessageDialog(this, "Transfer failed.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter valid numeric values for Account and Amount.", "Invalid Input", JOptionPane.WARNING_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Transfer Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showMiniStatement() {
        String pin = promptPin("Enter Security PIN to view Mini Statement:");
        if (pin == null) return;

        try {
            if (!accountDAO.verifyPin(currentAccount.getAccountNumber(), pin)) {
                JOptionPane.showMessageDialog(this, "Invalid Security PIN!", "Access Denied", JOptionPane.ERROR_MESSAGE);
                return;
            }

            List<Transaction> list = transactionDAO.getHistory(currentAccount.getAccountNumber(), 5);
            if (list.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No transactions recorded yet.", "Mini Statement", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            String[] columns = {"Time", "Type", "Amount", "Description", "Balance After"};
            Object[][] data = new Object[list.size()][5];
            for (int i = 0; i < list.size(); i++) {
                Transaction t = list.get(i);
                data[i][0] = t.getTimestamp() != null ? t.getTimestamp().toString().substring(0, 19) : "N/A";
                data[i][1] = t.getType();
                data[i][2] = String.format("₹ %.2f", t.getAmount());
                data[i][3] = t.getDescription();
                data[i][4] = String.format("₹ %.2f", t.getBalanceAfter());
            }

            JTable table = new JTable(data, columns);
            table.setRowHeight(24);
            table.setEnabled(false);
            JScrollPane sp = new JScrollPane(table);
            sp.setPreferredSize(new Dimension(600, 160));

            JOptionPane.showMessageDialog(this, sp, "Mini Statement — Last 5 Transactions", JOptionPane.PLAIN_MESSAGE);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error fetching transactions: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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
        JOptionPane.showMessageDialog(this, info, "Account Profile", JOptionPane.INFORMATION_MESSAGE);
    }

    private void promptOpenAccount() {
        JPanel panel = new JPanel(new GridLayout(4, 2, 8, 8));
        JTextField nameFld = UITheme.createTextField();
        nameFld.setText(currentUser.getFullName());
        JTextField balanceFld = UITheme.createTextField();
        balanceFld.setText("1000.00");
        JPasswordField pinFld = UITheme.createPasswordField();
        JComboBox<String> typeBox = new JComboBox<>(new String[]{"SAVINGS", "CURRENT"});

        panel.add(new JLabel("Full Name:"));
        panel.add(nameFld);
        panel.add(new JLabel("Initial Deposit (₹ min. 500):"));
        panel.add(balanceFld);
        panel.add(new JLabel("Security PIN (4-6 digits):"));
        panel.add(pinFld);
        panel.add(new JLabel("Account Type:"));
        panel.add(typeBox);

        int res = JOptionPane.showConfirmDialog(this, panel, "Open Bank Account", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res != JOptionPane.OK_OPTION) return;

        try {
            String name = nameFld.getText().trim();
            double balance = Double.parseDouble(balanceFld.getText().trim());
            String pin = new String(pinFld.getPassword()).trim();
            String type = (String) typeBox.getSelectedItem();

            if (balance < 500) {
                JOptionPane.showMessageDialog(this, "Minimum opening deposit is ₹ 500.", "Invalid Deposit", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!PasswordUtil.isValidPin(pin)) {
                JOptionPane.showMessageDialog(this, "Security PIN must be 4 to 6 numeric digits.", "Invalid PIN", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Account newAcc = new Account(0, currentUser.getEmail(), name, balance, pin, type);
            long accNum = accountDAO.openAccount(newAcc);

            JOptionPane.showMessageDialog(this,
                    "Congratulations!\nYour Bank Account has been opened successfully!\n\nAccount Number: " + accNum,
                    "Account Created", JOptionPane.INFORMATION_MESSAGE);

            setUserAndAccount(currentUser, accountDAO.getAccountByNumber(accNum));

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid numeric deposit amount.", "Invalid Input", JOptionPane.WARNING_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error opening account: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean checkAccountActive() {
        if (currentAccount == null) return false;
        if (!currentAccount.isActive()) {
            JOptionPane.showMessageDialog(this,
                    "Your account is FROZEN by Bank Administration.\nTransactions cannot be performed.",
                    "Account Frozen", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

    private String promptPin(String message) {
        JPasswordField pf = UITheme.createPasswordField();
        int ok = JOptionPane.showConfirmDialog(this, pf, message, JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (ok == JOptionPane.OK_OPTION) {
            return new String(pf.getPassword()).trim();
        }
        return null;
    }
}
