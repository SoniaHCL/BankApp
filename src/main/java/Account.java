public class Account {

    private double balance;
    private int transactionCount;

    public Account() {
        balance = 0;
        transactionCount = 0;
    }

    public void deposit(double amount) {
        validateAmount(amount);
        balance += amount;
        transactionCount++;
    }

    public void withdraw(double amount) {
        validateAmount(amount);
        if (amount > balance) {
            throw new IllegalStateException("Insufficient balance");
        }
        balance -= amount;
        transactionCount++;
    }

    private void validateAmount(double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be greater than 0");
        }
    }

    public double getBalance() {
        return balance;
    }

    public int getTransactionCount() {
        return transactionCount;
    }

    public void resetAccount() {
        balance = 0;
        transactionCount = 0;
    }
}