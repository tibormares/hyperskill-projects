package budget;

import java.io.*;
import java.util.*;

public class Main {

    double balance = 0;
    ArrayList<Product> products = new ArrayList<>();

    public static void main(String[] args) {
        Main obj = new Main();
        Scanner scanner = new Scanner(System.in);
        obj.processCommandNumber(scanner);
    }

    public void printMenu() {
        System.out.print("""
                Choose your action:
                1) Add income
                2) Add purchase
                3) Show list of purchases
                4) Balance
                5) Save
                6) Load
                7) Analyze (Sort)
                0) Exit
                """);
    }

    public void savePurchasesIntoFile() {
        try (FileWriter writer = new FileWriter("purchases.txt")) {
            if (!products.isEmpty()) {
                writer.write(balance + "\n");
                for (Product p : products) {
                    writer.write(p.getName() + "," + p.getPrice() + "," + p.getCategory() + "\n");
                }
                System.out.println("Purchases were saved!");
            } else {
                System.out.println("None purchases to save.");
            }
        } catch (IOException e) {
            System.out.println("Could not be saved: " + e);
        }
        printLine();
    }

    public void loadPurchasesFromFile() {
        try (Scanner scanner = new Scanner(new FileReader("purchases.txt"))) {
            if (!scanner.hasNextLine()) {
                System.out.println("Nothing to load.");
                printLine();
                return;
            } else {
                String balance = scanner.nextLine();
                this.balance = Double.parseDouble(balance);
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    String[] parts = line.split(",");
                    Product p = new Product(parts[0], Double.parseDouble(parts[1]), parts[2]);
                    products.add(p);
                }
                System.out.println("Purchases were loaded!");
            }
        } catch (IOException e) {
            System.out.println("File could not be loaded: " + e);
        }
        printLine();
    }

    public void processCommandNumber(Scanner scanner) {
        while (true) {
            printMenu();
            int number = scanner.nextInt();
            scanner.nextLine();
            printLine();
            switch (number) {
                case 1 -> {
                    addIncome(scanner);
                    printLine();
                }
                case 2 -> addPurchase(scanner);
                case 3 -> showPurchasedProducts(scanner);
                case 4 -> {
                    printBalance();
                    printLine();
                }
                case 5 -> savePurchasesIntoFile();
                case 6 -> loadPurchasesFromFile();
                case 7 -> printSortMenu(scanner);
                case 0 -> {
                    System.out.println("Bye!");
                    return;
                }
            }
        }
    }

    public void printSortMenu(Scanner scanner) {
        while (true) {
            System.out.println("""
                    How do you want to sort?
                    1) Sort all purchases
                    2) Sort by type
                    3) Sort certain type
                    4) Back""");
            int number = scanner.nextInt();
            scanner.nextLine();
            printLine();
            switch (number) {
                case 1 -> sortAll();
                case 2 -> sortByType();
                case 3 -> sortCategory(scanner);
                case 4 -> {
                    return;
                }
            }
        }
    }

    public void sortAll() {
        // sort the entire shopping list and display it so that the most expensive purchases are at the top of the list
        products.sort(Comparator.comparing(Product::getPrice).reversed());
        if (products.isEmpty()) {
            System.out.println("The purchase list is empty!");
            printLine();
        } else {
            double sum = printProductsAndGetSum("All");
            System.out.println("Total: $" + sum);
            printLine();
        }
    }

    public void sortByType() {
        Map<String, Double> sums = new HashMap<>();
        sums.put("Food", 0.0);
        sums.put("Entertainment", 0.0);
        sums.put("Clothes", 0.0);
        sums.put("Other", 0.0);

        for (Product p : products) {
            String category = p.getCategory().name();
            sums.put(category, sums.get(category) + p.getPrice());
        }

        List<Map.Entry<String, Double>> sortedList = new ArrayList<>(sums.entrySet());
        sortedList.sort((category1, category2) ->
            Double.compare(category2.getValue(), category1.getValue()));

        System.out.println("Types:");
        for (Map.Entry<String, Double> entry : sortedList) {
            System.out.printf("%s - $%.2f\n", entry.getKey(), entry.getValue());
        }

        double total = sums.values().stream().mapToDouble(Double::doubleValue).sum();
        System.out.printf("Total sum: $%.2f\n", total);
        printLine();
    }

    public void sortCategory(Scanner scanner) {
        System.out.println("Choose the type of purchase");
        printTypes();
        int number = scanner.nextInt();
        scanner.nextLine();
        printLine();
        String category = "";
        switch (number) {
            case 1 -> category = "Food";
            case 2 -> category = "Clothes";
            case 3 -> category = "Entertainment";
            case 4 -> category = "Other";
        }
        double sum = 0;
        List<Product> filteredProducts = new ArrayList<>();
        for (Product p : this.products) {
            if (p.getCategory().name().equals(category)) {
                filteredProducts.add(p);
                sum += p.getPrice();
            }
        }
        if (sum == 0) {
            System.out.println("The purchase list is empty!");
        } else {
            filteredProducts.sort(Comparator.comparing(Product::getPrice).reversed());
            for (Product p : filteredProducts) {
                System.out.println(p);
            }
            System.out.printf("Total sum: $%.2f\n", sum);
        }
        printLine();
    }

    public void addIncome(Scanner scanner) {
        System.out.println("Enter income:");
        setBalance(scanner.nextDouble() + getBalance());
        System.out.println("Income was added!");
    }

    public void addPurchase(Scanner scanner) {
        while (true) {
            printPurchaseCategoryMenu(false);
            String category = readCategoryChoice(scanner, false);

            if (category.equals("Back")) {
                break;
            }

            if (!category.isEmpty()) {
                System.out.println("Enter purchase name:");
                String name = scanner.nextLine();
                System.out.println("Enter its price:");
                double price = scanner.nextDouble();
                products.add(new Product(name, price, category));
                setBalance(getBalance() - price);
                System.out.println("Purchase was added!");
            }
            printLine();
        }
    }

    public void printTypes() {
        System.out.println("""
                1) Food
                2) Clothes
                3) Entertainment
                4) Other""");
    }

    public void printPurchaseCategoryMenu(Boolean plural) {
        System.out.println("Choose the type of " + (plural ? "purchases" : "purchase"));
        printTypes();
        if (plural) {
            System.out.println("5) All");
            System.out.println("6) Back");
        } else {
            System.out.println("5) Back");
        }
    }

    public String readCategoryChoice(Scanner scanner, boolean plural) {
        int number = scanner.nextInt();
        scanner.nextLine();
        printLine();
        switch (number) {
            case 1 -> {
                return "Food";
            }
            case 2 -> {
                return "Clothes";
            }
            case 3 -> {
                return "Entertainment";
            }
            case 4 -> {
                return "Other";
            }
            case 5 -> {
                if (plural) return "All";
                return "Back";
            }
            case 6 -> {
                if (plural) return "Back";
            }
        }
        return "";
    }

    public void showPurchasedProducts(Scanner scanner) {
        if (products.isEmpty()) {
            System.out.println("The purchase list is empty!");
            printLine();
            return;
        }
        while (true) {
            printPurchaseCategoryMenu(true);
            String category = readCategoryChoice(scanner, true);

            if (category.equals("Back")) {
                break;
            }

            double sum = printProductsAndGetSum(category);

            if (sum != 0) {
                System.out.printf("Total sum: $%.2f\n", sum);
            }
            printLine();
        }
    }

    public double printProductsAndGetSum(String category) {
        double sum = 0;
        System.out.println(category + ":");
        for (Product p : this.products) {
            if (category.equals("All") || p.getCategory().name().equals(category)) {
                System.out.println(p);
                sum += p.getPrice();
            }
        }
        if (sum == 0) {
            System.out.println("The purchase list is empty!");
        }
        return sum;
    }

    public double getBalance() {
        return this.balance;
    }

    public void setBalance(double income) {
        this.balance = income;
    }

    public void printBalance() {
        System.out.printf("Balance: $%.2f\n", getBalance());
    }

    public void printLine() {
        System.out.println();
    }

}
