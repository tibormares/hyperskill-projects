package banking;

import java.util.Scanner;

public class Main {
    private static Database database;

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            if (args.length < 2 || !args[0].equals("-fileName") || args[1].isEmpty()) {
                System.out.println("""
                    Invalid arguments.
                    -fileName filename""");
                return;
            }
            database = new Database(args[1]);
            menu(scanner);
        }
    }

    public static void menu(Scanner scanner) {
        while (true) {
            System.out.println("""
                    1. Create an account
                    2. Log into account
                    0. Exit""");
            String input = scanner.next();
            System.out.println();
            switch (input) {
                case "1" -> createCreditCard();
                case "2" -> loginMenu(scanner);
                case "0" -> {
                    System.out.print("Bye!");
                    return;
                }
            }
        }
    }

    public static void loginMenu(Scanner scanner) {
        System.out.println("Enter your card number:");
        String inputCardNumber = scanner.next();
        System.out.println("Enter your PIN:");
        String inputPinCode = scanner.next();
        System.out.println();

        CreditCard creditCard = database.verifyLogin(inputCardNumber, inputPinCode);
        if (creditCard != null) {
            System.out.println("You have successfully logged in!");
            System.out.println();
            userMenu(scanner, creditCard);
        } else {
            System.out.println("Wrong card number or PIN!");
            System.out.println();
        }
    }

    public static void userMenu(Scanner scanner, CreditCard creditCard) {
        while (true) {
            System.out.println("""
                    1. Balance
                    2. Add income
                    3. Do transfer
                    4. Close account
                    5. Log out
                    0. Exit""");
            String input = scanner.next();
            System.out.println();
            switch (input) {
                case "1" -> {
                    int currentBalance = database.getBalance(creditCard.getCardNumber());
                    System.out.println("Balance: " + currentBalance);
                    creditCard.setBalance(currentBalance);
                }
                case "2" -> {
                    System.out.println("Enter income:");
                    int income = scanner.nextInt();
                    database.addIncome(income, creditCard.getCardNumber());
                    creditCard.setBalance(creditCard.getBalance() + income);
                    System.out.println("Income was added!");
                }
                case "3" -> {
                    System.out.println("Transfer");
                    System.out.println("Enter card number:");
                    String receiversNumber = scanner.next();

                    if (receiversNumber.equals(creditCard.getCardNumber())) {
                        System.out.println("You can't transfer money to the same account!");
                        System.out.println();
                        continue;
                    }

                    if (!CreditCard.isValidLuhn(receiversNumber)) {
                        System.out.println("Probably you made a mistake in the card number. Please try again!");
                        System.out.println();
                        continue;
                    }

                    if (!database.checkAccountNumber(receiversNumber)) {
                        System.out.println("Such a card does not exist.");
                        System.out.println();
                        continue;
                    }

                    System.out.println("Enter how much money you want to transfer:");
                    int amount = scanner.nextInt();

                    if (creditCard.getBalance() < amount) {
                        System.out.println("Not enough money!");
                        System.out.println();
                        continue;
                    }

                    if (database.transferMoney(creditCard.getCardNumber(), receiversNumber, amount)) {
                        System.out.println("Success!");
                        creditCard.setBalance(creditCard.getBalance() - amount);
                    } else {
                        System.out.println("Transfer failed. Please try again.");
                    }
                }
                case "4" -> {
                    database.delete(creditCard.getCardNumber());
                    System.out.println("The account has been closed!");
                    System.out.println();
                    return;
                }
                case "5" -> {
                    System.out.println("You have successfully logged out!");
                    System.out.println();
                    return;
                }
                case "0" -> {
                    System.out.print("Bye!");
                    System.exit(0);
                }
            }
            System.out.println();
        }
    }

    public static void createCreditCard() {
        CreditCard card = new CreditCard();
        database.insert(card);
        System.out.println("Your card has been created");
        System.out.println("Your card number:");
        System.out.println(card.getCardNumber());
        System.out.println("Your card PIN:");
        System.out.println(card.getPinCode());
        System.out.println();
    }
}