package Projects_java.Banking_System.model;

import java.sql.Timestamp;

/**
 * POJO representing a single banking transaction (audit trail entry).
 *
 * Every debit, credit, and transfer generates one or more Transaction records.
 * This enables complete transaction history and balance reconstruction.
 */
public class Transaction {

    private int       id;
    private long      accountNumber;
    private String    type;          // DEBIT | CREDIT | TRANSFER_IN | TRANSFER_OUT
    private double    amount;
    private String    description;
    private double    balanceAfter;  // account balance immediately after this transaction
    private Long      refAccount;   // for transfers: the counterpart account number
    private Timestamp timestamp;

    /** Default constructor (required for DAO mapping). */
    public Transaction() {}

    /**
     * Convenience constructor for creating a new transaction log entry.
     */
    public Transaction(long accountNumber, String type, double amount,
                       String description, double balanceAfter, Long refAccount) {
        this.accountNumber = accountNumber;
        this.type          = type;
        this.amount        = amount;
        this.description   = description;
        this.balanceAfter  = balanceAfter;
        this.refAccount    = refAccount;
    }

    // ─── Getters ─────────────────────────────────────────────────────────────

    public int       getId()            { return id; }
    public long      getAccountNumber() { return accountNumber; }
    public String    getType()          { return type; }
    public double    getAmount()        { return amount; }
    public String    getDescription()   { return description; }
    public double    getBalanceAfter()  { return balanceAfter; }
    public Long      getRefAccount()    { return refAccount; }
    public Timestamp getTimestamp()     { return timestamp; }

    // ─── Setters ─────────────────────────────────────────────────────────────

    public void setId(int id)                       { this.id            = id; }
    public void setAccountNumber(long accountNumber){ this.accountNumber  = accountNumber; }
    public void setType(String type)                { this.type           = type; }
    public void setAmount(double amount)            { this.amount         = amount; }
    public void setDescription(String description)  { this.description    = description; }
    public void setBalanceAfter(double balanceAfter){ this.balanceAfter   = balanceAfter; }
    public void setRefAccount(Long refAccount)      { this.refAccount     = refAccount; }
    public void setTimestamp(Timestamp timestamp)   { this.timestamp      = timestamp; }

    @Override
    public String toString() {
        return "Transaction{id=" + id + ", type='" + type + "', amount=" + amount +
               ", balanceAfter=" + balanceAfter + ", time=" + timestamp + "}";
    }
}

