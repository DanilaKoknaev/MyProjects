import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class ConcurrentBankExample {
    public static void main(String[] args) throws InterruptedException {
        ConcurrentBank bank = new ConcurrentBank();

        // Создание счетов
        BankAccount account1 = bank.createAccount(1000);
        BankAccount account2 = bank.createAccount(500);

        System.out.println("До переводов: " + bank.getAccounts() + ", всего " + bank.getTotalBalance());

        // Перевод между счетами
        Thread transferThread1 = new Thread(() -> bank.transfer(account1, account2, 200));
        Thread transferThread2 = new Thread(() -> bank.transfer(account2, account1, 100));

        transferThread1.start();
        transferThread2.start();

        transferThread1.join();
        transferThread2.join();

        System.out.println("После переводов: " + bank.getAccounts() + ", всего " + bank.getTotalBalance());

        stressTest();
    }

    private static void stressTest() throws InterruptedException {
        ConcurrentBank bank = new ConcurrentBank();
        for (int i = 0; i < 5; i++) {
            bank.createAccount(1000);
        }

        double expected = bank.getTotalBalance();
        List<BankAccount> accounts = bank.getAccounts();
        List<Thread> threads = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            Thread thread = new Thread(() -> {
                for (int j = 0; j < 1000; j++) {
                    int fromIndex = ThreadLocalRandom.current().nextInt(accounts.size());
                    int toIndex = ThreadLocalRandom.current().nextInt(accounts.size());
                    if (fromIndex != toIndex) {
                        bank.transfer(accounts.get(fromIndex), accounts.get(toIndex), 10);
                    }
                }
            });
            threads.add(thread);
            thread.start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        System.out.println();
        System.out.println("Нагрузочный тест: 10 потоков по 1000 переводов");
        System.out.println("Ожидаемый общий баланс: " + expected);
        System.out.println("Фактический общий баланс: " + bank.getTotalBalance());
        System.out.println("Счета: " + bank.getAccounts());
    }
}
