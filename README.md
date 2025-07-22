<body style="font-family:Arial, sans-serif; line-height:1.6;">

  <h1> Java Console Banking System</h1>
  <p>A simple console-based banking system developed in Java using JDBC and MySQL. This project allows users to register, log in, and perform basic banking operations like debit, credit, balance check, and fund transfer.</p>

  <hr>

  <h2> Features</h2>
  <ul>
    <li>User Registration & Login</li>
    <li>Account Creation with Initial Balance</li>
    <li>Secure Transactions using Security PIN</li>
    <li>Debit and Credit Money</li>
    <li>Balance Inquiry</li>
    <li>Fund Transfer (with error handling)</li>
    <li>Session Logout</li>
  </ul>

  <hr>

  <h2> Skills Gained</h2>
  <ul>
    <li>Core Java Programming (OOPs, Exception Handling, Loops, etc.)</li>
    <li>Working with JDBC for database operations</li>
    <li>MySQL database design and SQL query writing</li>
    <li>Console-based UI development and user interaction</li>
    <li>Modular design and code organization using classes</li>
    <li>Basic transaction handling and logic implementation</li>
    <li>Debugging and testing of Java applications</li>
    <li>Understanding of secure input (e.g., PIN verification)</li>
  </ul>

  <hr

  <h2> Demo Screenshots</h2>

  <h4>1. Registration and Login</h4>
  <img src="Screenshots/Screenshot 2025-07-22 084853.png" width="600" alt="Registration and Login">

  <h4>2. Banking Operations(Debit, Credit & Balance)</h4>
  <img src="Screenshots/Screenshot 2025-07-22 084944.png" width="600" alt="Account Creation & Debit">

  <h4>3. Fund Transfer Attempt</h4>
  <img src="Screenshots/Screenshot 2025-07-22 085102.png" width="600" alt="Transfer Money">

  <h4>4. MySQL Database </h4>
  <img src="Screenshots/Screenshot 2025-07-22 085455.png" width="600" alt="Main Java File">

  <hr>

  <h2> Technologies Used</h2>
  <ul>
    <li>Java 21</li>
    <li>MySQL</li>
    <li>JDBC API</li>
    <li>VS Code / Terminal</li>
  </ul>

  <hr>

  <h2> Getting Started</h2>

  <h3> Prerequisites</h3>
  <ul>
    <li>Java JDK 21+</li>
    <li>MySQL Server</li>
    <li>MySQL JDBC Driver</li>
    <li>IDE or text editor (e.g., VS Code, IntelliJ)</li>
  </ul>

  <h3> Setup</h3>

  <ol>
    <li><b>Clone the Repository</b>
      <pre><code>git clone https://github.com/yourusername/java-banking-system.git
cd java-banking-system</code></pre>
    </li>

    <li><b>Configure MySQL Database</b>
      <pre><code>CREATE DATABASE banking_system;

-- Create `users` and `accounts` tables accordingly</code></pre>
    </li>

    <li><b>Edit Database Credentials</b>
      <pre><code>private static final String url = "jdbc:mysql://localhost:3306/banking_system";
private static final String username = "root";
private static final String password = "your_mysql_password";</code></pre>
    </li>

    <li><b>Compile and Run</b>
      <pre><code>javac -d . *.java
java Projects_java.Banking_System.BankingApp</code></pre>
    </li>
  </ol>

  <hr>

  <h2> Security Notes</h2>
  <ul>
    <li>Passwords and security PINs are stored in plaintext (for demo purposes). Encrypt these for production use.</li>
    <li>Ensure MySQL is securely configured in your system.</li>
  </ul>

  <hr>

  <h2> TODO</h2>
  <ul>
    <li>Encrypt user credentials</li>
    <li>Add admin functionality</li>
    <li>Improve exception handling</li>
    <li>GUI implementation (Swing or JavaFX)</li>
  </ul>


