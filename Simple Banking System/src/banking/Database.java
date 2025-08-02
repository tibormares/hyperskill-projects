package banking;

import org.sqlite.SQLiteDataSource;

import java.sql.*;

public class Database {

    private Connection connection;

    public Database(String fileName) {
        String baseUrl = "jdbc:sqlite:";
        String url = baseUrl + fileName;
        SQLiteDataSource dataSource = new SQLiteDataSource();
        dataSource.setUrl(url);

        try {
            Connection con = dataSource.getConnection();
            try (Statement statement = con.createStatement()) {
                statement.executeUpdate("CREATE TABLE IF NOT EXISTS card ("
                        + "id INTEGER,"
                        + "number TEXT,"
                        + "pin TEXT,"
                        + "balance INTEGER DEFAULT 0);");
            }
            this.connection = con;
            this.connection.setAutoCommit(true);
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }

    public boolean transferMoney(String sendersNumber, String receiversNumber, int amount) {
        try {
            connection.setAutoCommit(false);

            int senderBalance = getBalance(sendersNumber);
            if (senderBalance < amount) {
                connection.rollback();
                return false;
            }

            String sender = "UPDATE card SET balance = balance - ? WHERE number = ?";
            try (PreparedStatement senderStatement = connection.prepareStatement(sender)) {
                senderStatement.setInt(1, amount);
                senderStatement.setString(2, sendersNumber);
                senderStatement.executeUpdate();
            }

            String receiver = "UPDATE card SET balance = balance + ? WHERE number = ?";
            try (PreparedStatement receiverStatement = connection.prepareStatement(receiver)) {
                receiverStatement.setInt(1, amount);
                receiverStatement.setString(2, receiversNumber);
                receiverStatement.executeUpdate();
            }

            connection.commit();
            return true;
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException rollbackException) {
                System.out.println("Error during rollback: " + rollbackException.getMessage());
            }
            System.out.println("Error during transfer: " + e.getMessage());
            return false;
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                System.out.println("Error setting auto-commit: " + e.getMessage());
            }
        }
    }

    public boolean checkAccountNumber(String number) {
        String select = "SELECT * FROM card WHERE number = ?";
        try (PreparedStatement statement = connection.prepareStatement(select)) {
            statement.setString(1, number);
            try (ResultSet set  = statement.executeQuery()) {
                if (set.next()) {
                    return true;
                }
            } catch (SQLException e) {
                System.out.println("Error when checking account:" + e.getMessage());
            }
        } catch (SQLException e) {
            System.out.println("Exception: " + e);
        }
        return false;
    }

    public void addIncome(int income, String cardNumber) {
        String update = "UPDATE card SET balance = balance + ? WHERE number = ?";
        try (PreparedStatement statement = connection.prepareStatement(update)) {
            statement.setInt(1, income);
            statement.setString(2, cardNumber);
            statement.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error when adding income:" + e.getMessage());
        }
    }

    public void delete(String cardNumber) {
        String delete = "DELETE FROM card WHERE number = ?";
        try (PreparedStatement statement = connection.prepareStatement(delete)) {
            statement.setString(1, cardNumber);
            statement.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error when deleting account:" + e.getMessage());
        }
    }

    public void insert(CreditCard card) {
        String insert = "INSERT INTO card (number, pin) VALUES (?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(insert)) {
            statement.setString(1, card.getCardNumber());
            statement.setString(2, card.getPinCode());
            statement.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error inserting card: " + e.getMessage());
        }
    }

    public CreditCard verifyLogin(String cardNumber, String pinCode) {
        CreditCard creditCard = null;
        String select = "SELECT * FROM card WHERE number = ? AND pin = ?";
        try (PreparedStatement statement = connection.prepareStatement(select)) {
            statement.setString(1, cardNumber);
            statement.setString(2, pinCode);
            try (ResultSet card = statement.executeQuery()) {
                if (card.next()) {
                    creditCard = new CreditCard(card.getString("number"),
                            card.getString("pin"),
                            card.getInt("balance"));
                }
            }
        } catch (SQLException e) {
            System.out.println("Error verifying login: " + e.getMessage());
        }
        return creditCard;
    }

    public int getBalance(String cardNumber) {
        String select = "SELECT balance FROM card WHERE number = ?";
        try (PreparedStatement statement = connection.prepareStatement(select)) {
            statement.setString(1, cardNumber);
            try (ResultSet set = statement.executeQuery()) {
                if (set.next()) {
                    return set.getInt("balance");
                }
            }
        } catch (SQLException e) {
            System.out.println("Error getting balance: " + e.getMessage());
        }
        return 0;
    }

}
