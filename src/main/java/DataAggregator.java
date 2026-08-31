import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;

public class DataAggregator {
    ProductInfo aggregateProductInfo(String productName) {
        CompletableFuture<Double> priceFuture =
                CompletableFuture.supplyAsync(() ->
                                fetchPrice(productName))
                        .exceptionally(ex -> {
                            System.out.println("Сбой источника:" + ex.getCause().getMessage());
                            return 0.0;
                        });

        CompletableFuture<String> descriptionFuture =
                CompletableFuture.supplyAsync(() ->
                                fetchDescription(productName))
                        .exceptionally(ex -> {
                            System.out.println("Сбой источника:" + ex.getCause().getMessage());
                            return "Нет данных";
                        });

        CompletableFuture<Double> ratingFuture =
                CompletableFuture.supplyAsync(() ->
                                fetchRating(productName))
                        .exceptionally(ex -> {
                            System.out.println("Сбой источника:" + ex.getCause().getMessage());
                            return 0.0;
                        });

        CompletableFuture.allOf(priceFuture, descriptionFuture,
                ratingFuture).join();

        return new ProductInfo(
                productName,
                priceFuture.join(),
                descriptionFuture.join(),
                ratingFuture.join());
    }

    private double fetchPrice(String productName) {
        simulateWork("цена");
        return 899.99;
    }

    private String fetchDescription(String productName) {
        simulateWork("описание");
        return "Мощный игровой ноутбук";
    }

    private double fetchRating(String productName) {
        simulateWork("рейтинг");
        return 4.7;
    }

    private void simulateWork(String source) {
        try {
            Thread.sleep(1000 +
                    ThreadLocalRandom.current().nextInt(2000));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        if (ThreadLocalRandom.current().nextDouble() < 0.2) {
            throw new RuntimeException("источник \"" + source + "\"недоступен");
        }
    }


}



