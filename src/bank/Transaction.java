package bank;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Objects;

enum TransactionType { DEPOSIT, WITHDRAWAL }

class Transaction implements Serializable {
    private static final long serialVersionUID = 1L;

    private final LocalDateTime timestamp;
    private final BigDecimal amount;
    private final TransactionType type;
    private final String description;

    public Transaction(LocalDateTime timestamp, BigDecimal amount, TransactionType type, String description) {
        this.timestamp = Objects.requireNonNull(timestamp);
        this.amount = amount.setScale(2, RoundingMode.HALF_UP);
        this.type = Objects.requireNonNull(type);
        this.description = description == null ? "" : description;
    }
    public LocalDateTime timestamp() { return timestamp; }
    public BigDecimal amount() { return amount; }
    public TransactionType type() { return type; }
    public String description() { return description; }
}
