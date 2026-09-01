import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ConcurrentBank {
    private final List<BankAccount> accounts = new CopyOnWriteArrayList<>();

    public BankAccount createAccount(double initialBalance) {
        BankAccount account = new BankAccount(initialBalance);
        accounts.add(account);
        return account;
    }

    public boolean transfer(BankAccount from, BankAccount to, double amount) {
        if (from == to) {
            throw new IllegalArgumentException("Нельзя переводить деньги на тот же счёт");
        }
        BankAccount first = from.getId() < to.getId() ? from : to;
        BankAccount second = from.getId() < to.getId() ? to : from;

        synchronized (first) {
            synchronized (second) {
                if (!from.withdraw(amount)) {
                    return false;
                }
                to.deposit(amount);
                return true;
            }
        }
    }

    public double getTotalBalance() {
        double total = 0;
        for (BankAccount account : accounts) {
            total += account.getBalance();
        }
        return total;
    }

    public List<BankAccount> getAccounts() {
        return List.copyOf(accounts);
    }
}
