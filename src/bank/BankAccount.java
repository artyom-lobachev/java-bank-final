package bank;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

class InsufficientFundsException extends RuntimeException {
    public InsufficientFundsException(String msg) { super(msg); }
}

class BankAccount implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String id;
    private final String iban;
    private final String bic;
    private final String bankName;
    private final String ownerName;
    private BigDecimal balance = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    private final List<Transaction> transactions = new ArrayList<>();

    public BankAccount(String iban, String bic, String bankName, String ownerName) {
        this.id = UUID.randomUUID().toString();
        this.iban = Objects.requireNonNull(iban);
        this.bic = Objects.requireNonNull(bic);
        this.bankName = Objects.requireNonNull(bankName);
        this.ownerName = Objects.requireNonNull(ownerName);
    }

    public String id() { return id; }
    public String iban() { return iban; }
    public String bic() { return bic; }
    public String bankName() { return bankName; }
    public String ownerName() { return ownerName; }
    public BigDecimal balance() { return balance; }
    public List<Transaction> transactions() { return Collections.unmodifiableList(transactions); }

    public void deposit(BigDecimal amount, String description) {
        checkPositive(amount);
        balance = balance.add(amount);
        transactions.add(new Transaction(LocalDateTime.now(), amount, TransactionType.DEPOSIT, description));
    }

    public void withdraw(BigDecimal amount, String description) {
        checkPositive(amount);
        if (balance.compareTo(amount) < 0) throw new InsufficientFundsException("Недостаточно средств: " + balance);
        balance = balance.subtract(amount);
        transactions.add(new Transaction(LocalDateTime.now(), amount, TransactionType.WITHDRAWAL, description));
    }

    private static void checkPositive(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Сумма должна быть положительной");
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BankAccount)) return false;
        return iban.equals(((BankAccount) o).iban);
    }
    @Override public int hashCode() { return Objects.hash(iban); }

    @Override public String toString() {
        return ownerName + " — " + bankName + " — " + iban + " [" + balance + "]";
    }
}
