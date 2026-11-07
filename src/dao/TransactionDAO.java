package Projects_java.Banking_System.dao;

import Projects_java.Banking_System.model.Transaction;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) for Transaction entity.
 * Provides audit logging and retrieval for all debit, credit, and transfer operations.
 */
public class TransactionDAO {

    private final Connection connection;

    public TransactionDAO(Connection connection) {
        this.connection = connection;
    }

    /**
     * Records a new transaction into the database audit trail.
     */
    public boolean logTransaction(Transaction tx) throws SQLException {
        String query = "INSERT INTO transactions (account_number, type, amount, description, balance_after, ref_account) " +
                       "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setLong(1, tx.getAccountNumber());
            ps.setString(2, tx.getType());
            ps.setDouble(3, tx.getAmount());
            ps.setString(4, tx.getDescription());
            ps.setDouble(5, tx.getBalanceAfter());
            if (tx.getRefAccount() != null && tx.getRefAccount() > 0) {
                ps.setLong(6, tx.getRefAccount());
            } else {
                ps.setNull(6, Types.BIGINT);
            }
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Retrieves the transaction history for a specific account, ordered by newest first.
     */
    public List<Transaction> getHistory(long accountNumber, int limit) throws SQLException {
        List<Transaction> list = new ArrayList<>();
        String query = "SELECT id, account_number, type, amount, description, balance_after, ref_account, timestamp " +
                       "FROM transactions WHERE account_number = ? ORDER BY timestamp DESC LIMIT ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setLong(1, accountNumber);
            ps.setInt(2, limit > 0 ? limit : 50);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Transaction tx = new Transaction();
                    tx.setId(rs.getInt("id"));
                    tx.setAccountNumber(rs.getLong("account_number"));
                    tx.setType(rs.getString("type"));
                    tx.setAmount(rs.getDouble("amount"));
                    tx.setDescription(rs.getString("description"));
                    tx.setBalanceAfter(rs.getDouble("balance_after"));
                    long ref = rs.getLong("ref_account");
                    tx.setRefAccount(rs.wasNull() ? null : ref);
                    tx.setTimestamp(rs.getTimestamp("timestamp"));
                    list.add(tx);
                }
            }
        }
        return list;
    }

    /**
     * Retrieves all transactions in the system (used by Admin Panel).
     */
    public List<Transaction> getAllTransactions(int limit) throws SQLException {
        List<Transaction> list = new ArrayList<>();
        String query = "SELECT id, account_number, type, amount, description, balance_after, ref_account, timestamp " +
                       "FROM transactions ORDER BY timestamp DESC LIMIT ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, limit > 0 ? limit : 200);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Transaction tx = new Transaction();
                    tx.setId(rs.getInt("id"));
                    tx.setAccountNumber(rs.getLong("account_number"));
                    tx.setType(rs.getString("type"));
                    tx.setAmount(rs.getDouble("amount"));
                    tx.setDescription(rs.getString("description"));
                    tx.setBalanceAfter(rs.getDouble("balance_after"));
                    long ref = rs.getLong("ref_account");
                    tx.setRefAccount(rs.wasNull() ? null : ref);
                    tx.setTimestamp(rs.getTimestamp("timestamp"));
                    list.add(tx);
                }
            }
        }
        return list;
    }
}
