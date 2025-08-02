package budget;

public class Product {

    private String name;
    private double price;
    private Category category;

    public Product(String name, double price, String category) {
        this.name = name;
        this.price = price;
        this.category = Category.valueOf(category);
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public Category getCategory() {
        return category;
    }

    @Override
    public String toString() {
        return name + " $" + String.format("%.2f", price);
    }
}
