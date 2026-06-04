import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class AccountTest {

    @Test
    void testDeposit() {
        Account acc = new Account();
        acc.deposit(1000);
        assertEquals(1000, acc.getBalance());
    }

    @Test
    void testWithdraw() {
        Account acc = new Account();
        acc.deposit(1000);
        acc.withdraw(300);
        assertEquals(700, acc.getBalance());
    }

    @Test
    void testInsufficientBalance() {
        Account acc = new Account();
        acc.deposit(500);

        assertThrows(IllegalStateException.class, () -> acc.withdraw(1000));
    }

    @Test
    void testInvalidDeposit() {
        Account acc = new Account();
        assertThrows(IllegalArgumentException.class, () -> acc.deposit(-10));
    }

    @Test
    void testTransactionCount() {
        Account acc = new Account();
        acc.deposit(100);
        acc.withdraw(50);
        assertEquals(2, acc.getTransactionCount());
    }

    @Test
    void testResetAccount() {
        Account acc = new Account();
        acc.deposit(500);
        acc.resetAccount();
        assertEquals(0, acc.getBalance());
    }
}