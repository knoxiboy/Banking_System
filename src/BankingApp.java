package Projects_java.Banking_System;

import Projects_java.Banking_System.dao.AccountDAO;
import Projects_java.Banking_System.dao.DBConnection;
import Projects_java.Banking_System.dao.TransactionDAO;
import Projects_java.Banking_System.dao.UserDAO;
import Projects_java.Banking_System.model.Account;
import Projects_java.Banking_System.model.Transaction;
import Projects_java.Banking_System.model.User;
import Projects_java.Banking_System.util.PasswordUtil;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

/**
 * BankingApp — Command-Line Interface (CLI) Entry Point.
 *
 * Fully integrated with the unified Layered Architecture (Model-DAO-Util)
 * sharing the same database connection, SHA-256 security, and ACID transactions
 * as the BankingGUI application.
 */
public class BankingApp {

    private static final String URL  = "jdbc:mysql://localhost:3306/banking_system";
    private static final String USER = "root";
    private static final String PASS = "knox";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Connection connection = null;

        try {
            connection = DBConnection.getConnection(URL, USER, PASS);
            TransactionDAO transactionDAO = new TransactionDAO(connection);
            AccountDAO accountDAO         = new AccountDAO(connection, transactionDAO);
            UserDAO userDAO               = new UserDAO(connection);

            System.out.println("==========================================");
            System.out.println("   WELCOME TO APEX DIGITAL BANKING (CLI)  ");
            System.out.println("==========================================");

            while (true) {
                System.out.println("\n--- MAIN MENU ---");
                System.out.println("1. Register");
                System.out.println("2. Login");
                System.out.println("3. Exit");
                System.out.print("Enter your choice: ");

                if (!scanner.hasNextInt()) {
                    System.out.println("Invalid input. Please enter a number.");
                    scanner.nextLine();
                    continue;
                }
                int choice1 = scanner.nextInt();
                scanner.nextLine(); // consume newline

                switch (choice1) {
                    case 1:
                        handleRegistration(scanner, userDAO);
                        break;
                    case 2:
                        handleLoginAndSession(scanner, userDAO, accountDAO, transactionDAO);
                        break;
                    case 3:
                        System.out.println("\nTHANK YOU FOR USING APEX BANKING SYSTEM!");
                        System.out.println("Exiting System. Goodbye!");
                        DBConnection.closeConnection();
                        return;
                    default:
                        System.out.println("Enter a valid choice (1-3).");
                        break;
                }
            }
        } catch (SQLException e) {
            System.err.println("Database Error: " + e.getMessage());
        } finally {
            DBConnection.closeConnection();
        }
    }

    private static void handleRegistration(Scanner scanner, UserDAO userDAO) {
        System.out.println("\n--- USER REGISTRATION ---");
        System.out.print("Full Name: ");
        String name = scanner.nextLine().trim();

        System.out.print("Email Address: ");
        String email = scanner.nextLine().trim();

        if (!PasswordUtil.isValidEmail(email)) {
            System.out.println("Invalid email format. Registration cancelled.");
            return;
        }

        System.out.print("Password (min 8 characters): ");
        String password = scanner.nextLine().trim();

        if (!PasswordUtil.isStrongPassword(password)) {
            System.out.println("Password must be at least 8 characters long.");
            return;
        }

        try {
            if (userDAO.userExists(email)) {
                System.out.println("An account with this email address already exists!");
                return;
            }

            User newUser = new User(name, email, password);
            boolean ok = userDAO.register(newUser);
            if (ok) {
                System.out.println("Registration Successful! You can now log in.");
            } else {
                System.out.println("Registration failed. Please try again.");
            }
        } catch (SQLException e) {
            System.out.println("Error during registration: " + e.getMessage());
        }
    }

    private static void handleLoginAndSession(Scanner scanner, UserDAO userDAO,
                                              AccountDAO accountDAO, TransactionDAO transactionDAO) {
        System.out.println("\n--- USER LOGIN ---");
        System.out.print("Email: ");
        String email = scanner.nextLine().trim();

        System.out.print("Password: ");
        String password = scanner.nextLine().trim();

        try {
            User user = userDAO.login(email, password);
            if (user == null) {
                System.out.println("Incorrect Email or Password!");
                return;
            }

            System.out.println("\nLogin Successful! Welcome, " + user.getFullName());

            // Check if user has an account
            Account account = accountDAO.getAccountByEmail(email);
            if (account == null) {
                System.out.println("\nYou do not have an active Bank Account yet.");
                System.out.println("1. Open a new Bank Account");
                System.out.println("2. Return to Main Menu");
                System.out.print("Enter choice: ");

                int accChoice = scanner.nextInt();
                scanner.nextLine();

                if (accChoice == 1) {
                    System.out.print("Enter Initial Deposit (min. ₹500): ");
                    double deposit = scanner.nextDouble();
                    scanner.nextLine();

                    if (deposit < 500) {
                        System.out.println("Minimum opening deposit is ₹500.");
                        return;
                    }

                    System.out.print("Set Security PIN (4-6 digits): ");
                    String pin = scanner.nextLine().trim();

                    if (!PasswordUtil.isValidPin(pin)) {
                        System.out.println("PIN must be 4 to 6 numeric digits.");
                        return;
                    }

                    System.out.print("Account Type (1 for SAVINGS, 2 for CURRENT): ");
                    int typeChoice = scanner.nextInt();
                    scanner.nextLine();
                    String accType = (typeChoice == 2) ? "CURRENT" : "SAVINGS";

                    Account newAcc = new Account(0, email, user.getFullName(), deposit, pin, accType);
                    long accNum = accountDAO.openAccount(newAcc);
                    System.out.println("Account Created Successfully!");
                    System.out.println("Your New Account Number is: " + accNum);
                    account = accountDAO.getAccountByNumber(accNum);
                } else {
                    return;
                }
            }

            // Customer Banking Operations Session
            long accNum = account.getAccountNumber();
            int userChoice = 0;

            while (userChoice != 6) {
                System.out.println("\n==========================================");
                System.out.println("   ACCOUNT OPERATIONS: A/C " + accNum);
                System.out.println("==========================================");
                System.out.println("1. Debit Money (Withdrawal)");
                System.out.println("2. Credit Money (Deposit)");
                System.out.println("3. Transfer Money (P2P)");
                System.out.println("4. Check Balance");
                System.out.println("5. View Mini Statement");
                System.out.println("6. Log Out");
                System.out.print("Enter your choice: ");

                if (!scanner.hasNextInt()) {
                    System.out.println("Invalid input.");
                    scanner.nextLine();
                    continue;
                }
                userChoice = scanner.nextInt();
                scanner.nextLine();

                switch (userChoice) {
                    case 1:
                        handleDebit(scanner, accountDAO, accNum);
                        break;
                    case 2:
                        handleCredit(scanner, accountDAO, accNum);
                        break;
                    case 3:
                        handleTransfer(scanner, accountDAO, accNum);
                        break;
                    case 4:
                        handleCheckBalance(scanner, accountDAO, accNum);
                        break;
                    case 5:
                        handleMiniStatement(scanner, accountDAO, transactionDAO, accNum);
                        break;
                    case 6:
                        System.out.println("Logged out successfully.");
                        break;
                    default:
                        System.out.println("Invalid option! Choose between 1 and 6.");
                        break;
                }
            }

        } catch (SQLException e) {
            System.out.println("Database Error: " + e.getMessage());
        }
    }

    private static void handleDebit(Scanner scanner, AccountDAO accountDAO, long accNum) {
        System.out.print("Enter Amount to Debit (₹): ");
        double amount = scanner.nextDouble();
        scanner.nextLine();

        System.out.print("Enter Security PIN: ");
        String pin = scanner.nextLine().trim();

        try {
            boolean ok = accountDAO.debitMoney(accNum, amount, pin);
            if (ok) {
                System.out.printf("Rs. %.2f debited successfully!\n", amount);
            } else {
                System.out.println("Debit transaction failed.");
            }
        } catch (Exception e) {
            System.out.println("Transaction Failed: " + e.getMessage());
        }
    }

    private static void handleCredit(Scanner scanner, AccountDAO accountDAO, long accNum) {
        System.out.print("Enter Amount to Credit (₹): ");
        double amount = scanner.nextDouble();
        scanner.nextLine();

        System.out.print("Enter Security PIN: ");
        String pin = scanner.nextLine().trim();

        try {
            boolean ok = accountDAO.creditMoney(accNum, amount, pin);
            if (ok) {
                System.out.printf("Rs. %.2f credited successfully!\n", amount);
            } else {
                System.out.println("Credit transaction failed.");
            }
        } catch (Exception e) {
            System.out.println("Deposit Failed: " + e.getMessage());
        }
    }

    private static void handleTransfer(Scanner scanner, AccountDAO accountDAO, long senderAccNum) {
        System.out.print("Enter Receiver Account Number: ");
        long receiverAccNum = scanner.nextLong();

        System.out.print("Enter Amount to Transfer (₹): ");
        double amount = scanner.nextDouble();
        scanner.nextLine();

        System.out.print("Enter Security PIN: ");
        String pin = scanner.nextLine().trim();

        try {
            boolean ok = accountDAO.transferMoney(senderAccNum, receiverAccNum, amount, pin);
            if (ok) {
                System.out.printf("Rs. %.2f transferred to A/C %d successfully!\n", amount, receiverAccNum);
            } else {
                System.out.println("Transfer transaction failed.");
            }
        } catch (Exception e) {
            System.out.println("Transfer Failed: " + e.getMessage());
        }
    }

    private static void handleCheckBalance(Scanner scanner, AccountDAO accountDAO, long accNum) {
        System.out.print("Enter Security PIN: ");
        String pin = scanner.nextLine().trim();

        try {
            double balance = accountDAO.getBalance(accNum, pin);
            System.out.printf("Current Balance for A/C %d: Rs. %.2f\n", accNum, balance);
        } catch (Exception e) {
            System.out.println("Failed to fetch balance: " + e.getMessage());
        }
    }

    private static void handleMiniStatement(Scanner scanner, AccountDAO accountDAO,
                                            TransactionDAO transactionDAO, long accNum) {
        System.out.print("Enter Security PIN: ");
        String pin = scanner.nextLine().trim();

        try {
            if (!accountDAO.verifyPin(accNum, pin)) {
                System.out.println("Invalid Security PIN!");
                return;
            }

            List<Transaction> list = transactionDAO.getHistory(accNum, 5);
            if (list.isEmpty()) {
                System.out.println("No transactions found.");
                return;
            }

            System.out.println("\n----------------------------------------------------------------------------------");
            System.out.printf("%-6s | %-19s | %-12s | %-12s | %-12s | %s\n",
                    "Txn ID", "Date & Time", "Type", "Amount", "Balance", "Description");
            System.out.println("----------------------------------------------------------------------------------");
            for (Transaction t : list) {
                String timeStr = t.getTimestamp() != null ? t.getTimestamp().toString().substring(0, 19) : "N/A";
                System.out.printf("#%-5d | %-19s | %-12s | Rs. %-8.2f | Rs. %-8.2f | %s\n",
                        t.getId(), timeStr, t.getType(), t.getAmount(), t.getBalanceAfter(), t.getDescription());
            }
            System.out.println("----------------------------------------------------------------------------------");
        } catch (Exception e) {
            System.out.println("Error fetching transactions: " + e.getMessage());
        }
    }
}
