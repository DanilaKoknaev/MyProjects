public class ProductInfo {
    private final String name;
    private final double price;
    private final String description;
    private final double rating;

    public ProductInfo(String name, double price, String description, double rating) {
        this.name = name;
        this.price = price;
        this.description = description;
        this.rating = rating;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public String getDescription() {
        return description;
    }

    public double getRating() {
        return rating;
    }

    @Override
    public String toString() {
        return "ProductInfo{" +
                "name='" + name + '\'' +
                ", price=" + price +
                ", description='" + description + '\'' +
                ", rating=" + rating +
                '}';
    }
}
