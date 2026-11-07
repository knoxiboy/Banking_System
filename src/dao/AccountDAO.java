package Projects_java.Banking_System.dao;

import Projects_java.Banking_System.model.Account;
import Projects_java.Banking_System.model.Transaction;
import Projects_java.Banking_System.util.PasswordUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) for Bank Accounts.
 * Handles account creation, queries, balance operations (credit/debit/transfer),
 * transaction auditing, and admin account status controls.
 */
public class AccountDAO {

    private final Connection connection;
    private final TransactionDAO transactionDAO;

    public AccountDAO(Connection connection, TransactionDAO transactionDAO) {
        this.connection = connection;
        this.transactionDAO = transactionDAO;
    }

    /**
     * Opens a new bank account with initial balance.
     * PIN is hashed using SHA-256.
     * Generates a new unique account number and records the initial credit transaction.
     */
    public long openAccount(Account account) throws SQLException {
        if (accountExists(account.getUserEmail())) {
            throw new IllegalArgumentException("An account already exists for email: " + account.getUserEmail());
        }

        long accountNumber = generateAccountNumber();
        account.setAccountNumber(accountNumber);

        String query = "INSERT INTO accounts (account_number, email, full_name, balance, security_pin, account_type, status) " +
                       "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setLong(1, accountNumber);
            ps.setString(2, account.getUserEmail());
            ps.setString(3, account.getFullName());
            ps.setDouble(4, account.getBalance());
            ps.setString(5, PasswordUtil.hash(account.getSecurityPin()));
            ps.setString(6, account.getAccountType() != null ? account.getAccountType() : "SAVINGS");
            ps.setString(7, "ACTIVE");

            int rows = ps.executeUpdate();
            if (rows > 0) {
                // Record initial deposit in transaction audit table
                if (account.getBalance() > 0 && transactionDAO != null) {
                    Transaction tx = new Transaction(
                            accountNumber,
                            "CREDIT",
                            account.getBalance(),
                            "Initial Account Opening Deposit",
                            account.getBalance(),
                            null
                    );
                    transactionDAO.logTransaction(tx);
                }
                return accountNumber;
            } else {
                throw new SQLException("Account creation failed, no rows inserted.");
            }
        }
    }

    /**
     * Checks if an account exists for the given user email.
     */
    public boolean accountExists(String email) throws SQLException {
        String query = "SELECT 1 FROM accounts WHERE email = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * Retrieves account by user email.
     */
    public Account getAccountByEmail(String email) throws SQLException {
        String query = "SELECT account_number, email, full_name, balance, security_pin, account_type, status, created_at " +
                       "FROM accounts WHERE email = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToAccount(rs);
                }
            }
        }
        return null;
    }

    /**
     * Retrieves account by account number.
     */
    public Account getAccountByNumber(long accountNumber) throws SQLException {
        String query = "SELECT account_number, email, full_name, balance, security_pin, account_type, status, created_at " +
                       "FROM accounts WHERE account_number = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setLong(1, accountNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToAccount(rs);
                }
            }
        }
        return null;
    }

    /**
     * Verifies the security PIN for an account.
     */
    public boolean verifyPin(long accountNumber, String plainPin) throws SQLException {
        String query = "SELECT security_pin FROM accounts WHERE account_number = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setLong(1, accountNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String storedPin = rs.getString("security_pin");
                    if (PasswordUtil.verify(plainPin, storedPin)) {
                        return true;
                    } else if (storedPin.equals(plainPin)) {
                        // Legacy plaintext PIN upgrade
                        upgradePinHash(accountNumber, plainPin);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Credits money into an account.
     */
    public boolean creditMoney(long accountNumber, double amount, String plainPin) throws SQLException {
        if (amount <= 0) {
            throw new IllegalArgumentException("Credit amount must be greater than zero.");
        }
        if (!verifyPin(accountNumber, plainPin)) {
            throw new SecurityException("Invalid Security PIN.");
        }

        Account acc = getAccountByNumber(accountNumber);
        if (acc == null) {
            throw new IllegalArgumentException("Account not found.");
        }
        if (!acc.isActive()) {
            throw new IllegalStateException("Account is FROZEN. Transactions are temporarily disabled.");
        }

        try {
            connection.setAutoCommit(false);

            String query = "UPDATE accounts SET balance = balance + ? WHERE account_number = ?";
            try (PreparedStatement ps = connection.prepareStatement(query)) {
                ps.setDouble(1, amount);
                ps.setLong(2, accountNumber);
                int updated = ps.executeUpdate();

                if (updated > 0) {
                    double newBalance = acc.getBalance() + amount;
                    if (transactionDAO != null) {
                        Transaction tx = new Transaction(
                                accountNumber,
                                "CREDIT",
                                amount,
                                "Cash/Online Deposit",
                                newBalance,
                                null
                        );
                        transactionDAO.logTransaction(tx);
                    }
                    connection.commit();
                    return true;
                } else {
                    connection.rollback();
                    return false;
                }
            }
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }

    /**
     * Debits money from an account.
     */
    public boolean debitMoney(long accountNumber, double amount, String plainPin) throws SQLException {
        if (amount <= 0) {
            throw new IllegalArgumentException("Debit amount must be greater than zero.");
        }
        if (!verifyPin(accountNumber, plainPin)) {
            throw new SecurityException("Invalid Security PIN.");
        }

        Account acc = getAccountByNumber(accountNumber);
        if (acc == null) {
            throw new IllegalArgumentException("Account not found.");
        }
        if (!acc.isActive()) {
            throw new IllegalStateException("Account is FROZEN. Transactions are temporarily disabled.");
        }
        if (acc.getBalance() < amount) {
            throw new IllegalArgumentException("Insufficient balance. Available: ₹" + acc.getBalance());
        }

        try {
            connection.setAutoCommit(false);

            String query = "UPDATE accounts SET balance = balance - ? WHERE account_number = ?";
            try (PreparedStatement ps = connection.prepareStatement(query)) {
                ps.setDouble(1, amount);
                ps.setLong(2, accountNumber);
                int updated = ps.executeUpdate();

                if (updated > 0) {
                    double newBalance = acc.getBalance() - amount;
                    if (transactionDAO != null) {
                        Transaction tx = new Transaction(
                                accountNumber,
                                "DEBIT",
                                amount,
                                "Withdrawal",
                                newBalance,
                                null
                        );
                        transactionDAO.logTransaction(tx);
                    }
                    connection.commit();
                    return true;
                } else {
                    connection.rollback();
                    return false;
                }
            }
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }

    /**
     * Transfers money atomically from sender account to receiver account.
     * Demonstrates ACID Transactions: both balances update or none does.
     */
    public boolean transferMoney(long senderAccountNum, long receiverAccountNum, double amount, String plainPin)
            throws SQLException {
        if (senderAccountNum == receiverAccountNum) {
            throw new IllegalArgumentException("Cannot transfer money to the same account.");
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("Transfer amount must be greater than zero.");
        }
        if (!verifyPin(senderAccountNum, plainPin)) {
            throw new SecurityException("Invalid Security PIN.");
        }

        Account sender = getAccountByNumber(senderAccountNum);
        Account receiver = getAccountByNumber(receiverAccountNum);

        if (sender == null) throw new IllegalArgumentException("Sender account not found.");
        if (receiver == null) throw new IllegalArgumentException("Receiver account not found.");
        if (!sender.isActive()) throw new IllegalStateException("Sender account is FROZEN.");
        if (!receiver.isActive()) throw new IllegalStateException("Receiver account is FROZEN.");
        if (sender.getBalance() < amount) {
            throw new IllegalArgumentException("Insufficient balance. Available: ₹" + sender.getBalance());
        }

        try {
            connection.setAutoCommit(false);

            String debitQuery = "UPDATE accounts SET balance = balance - ? WHERE account_number = ?";
            String creditQuery = "UPDATE accounts SET balance = balance + ? WHERE account_number = ?";

            try (PreparedStatement psDebit = connection.prepareStatement(debitQuery);
                 PreparedStatement psCredit = connection.prepareStatement(creditQuery)) {

                psDebit.setDouble(1, amount);
                psDebit.setLong(2, senderAccountNum);
                int dRows = psDebit.executeUpdate();

                psCredit.setDouble(1, amount);
                psCredit.setLong(2, receiverAccountNum);
                int cRows = psCredit.executeUpdate();

                if (dRows > 0 && cRows > 0) {
                    double senderNewBalance = sender.getBalance() - amount;
                    double receiverNewBalance = receiver.getBalance() + amount;

                    if (transactionDAO != null) {
                        // Log Sender's TRANSFER_OUT
                        Transaction txSender = new Transaction(
                                senderAccountNum,
                                "TRANSFER_OUT",
                                amount,
                                "Transfer to A/C " + receiverAccountNum + " (" + receiver.getFullName() + ")",
                                senderNewBalance,
                                receiverAccountNum
                        );
                        transactionDAO.logTransaction(txSender);

                        // Log Receiver's TRANSFER_IN
                        Transaction txReceiver = new Transaction(
                                receiverAccountNum,
                                "TRANSFER_IN",
                                amount,
                                "Transfer from A/C " + senderAccountNum + " (" + sender.getFullName() + ")",
                                receiverNewBalance,
                                senderAccountNum
                        );
                        transactionDAO.logTransaction(txReceiver);
                    }

                    connection.commit();
                    return true;
                } else {
                    connection.rollback();
                    return false;
                }
            }
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }

    /**
     * Checks balance with PIN verification.
     */
    public double getBalance(long accountNumber, String plainPin) throws SQLException {
        if (!verifyPin(accountNumber, plainPin)) {
            throw new SecurityException("Invalid Security PIN.");
        }
        Account acc = getAccountByNumber(accountNumber);
        if (acc == null) throw new IllegalArgumentException("Account not found.");
        return acc.getBalance();
    }

    /**
     * Updates account status to FROZEN (Admin action).
     */
    public boolean freezeAccount(long accountNumber) throws SQLException {
        String query = "UPDATE accounts SET status = 'FROZEN' WHERE account_number = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setLong(1, accountNumber);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Updates account status to ACTIVE (Admin action).
     */
    public boolean unfreezeAccount(long accountNumber) throws SQLException {
        String query = "UPDATE accounts SET status = 'ACTIVE' WHERE account_number = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setLong(1, accountNumber);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Retrieves all accounts in the bank (used by Admin Panel).
     */
    public List<Account> getAllAccounts() throws SQLException {
        List<Account> list = new ArrayList<>();
        String query = "SELECT account_number, email, full_name, balance, security_pin, account_type, status, created_at " +
                       "FROM accounts ORDER BY account_number ASC";
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(query)) {
            while (rs.next()) {
                list.add(mapResultSetToAccount(rs));
            }
        }
        return list;
    }

    private synchronized long generateAccountNumber() throws SQLException {
        String query = "SELECT MAX(account_number) AS max_num FROM accounts";
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(query)) {
            if (rs.next()) {
                long maxNum = rs.getLong("max_num");
                if (maxNum >= 10000100) {
                    return maxNum + 1;
                }
            }
        }
        return 10000100L;
    }

    private void upgradePinHash(long accountNumber, String plainPin) {
        String query = "UPDATE accounts SET security_pin = ? WHERE account_number = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, PasswordUtil.hash(plainPin));
            ps.setLong(2, accountNumber);
            ps.executeUpdate();
        } catch (SQLException ignored) {
        }
    }

    private Account mapResultSetToAccount(ResultSet rs) throws SQLException {
        Account a = new Account();
        a.setAccountNumber(rs.getLong("account_number"));
        a.setUserEmail(rs.getString("email"));
        a.setFullName(rs.getString("full_name"));
        a.setBalance(rs.getDouble("balance"));
        a.setSecurityPin(rs.getString("security_pin"));
        a.setAccountType(rs.getString("account_type"));
        a.setStatus(rs.getString("status"));
        a.setCreatedAt(rs.getTimestamp("created_at"));
        return a;
    }
}
