public class Main {
    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        DataAggregator dataAggregator = new DataAggregator();
        ProductInfo info = dataAggregator.aggregateProductInfo("ноутбук");
        System.out.println(info);
        System.out.println((System.currentTimeMillis() - start));
    }
}
