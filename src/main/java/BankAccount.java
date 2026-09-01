import java.util.concurrent.atomic.AtomicInteger;

public class BankAccount {
    private static final AtomicInteger ID_GENERATOR = new AtomicInteger(1);

    private final int id;
    private double balance;

    public BankAccount(double initialBalance) {
        if (initialBalance < 0) {
            throw new IllegalArgumentException("Начальный баланс не может быть отрицательным");
        }
        this.id = ID_GENERATOR.getAndIncrement();
        this.balance = initialBalance;
    }

    public int getId() {
        return id;
    }

    public synchronized void deposit(double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Сумма пополнения должна быть положительной");
        }
        balance += amount;
    }

    public synchronized boolean withdraw(double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Сумма снятия должна быть положительной");
        }
        if (balance < amount) {
            return false;
        }
        balance -= amount;
        return true;
    }

    public synchronized double getBalance() {
        return balance;
    }

    @Override
    public String toString() {
        return "BankAccount{id=" + id + ", balance=" + getBalance() + '}';
    }
}
