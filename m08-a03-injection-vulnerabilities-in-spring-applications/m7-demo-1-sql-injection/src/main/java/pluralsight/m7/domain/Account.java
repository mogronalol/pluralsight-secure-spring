package pluralsight.m7.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.SortedSet;
import java.util.TreeSet;

@Entity
@Data
@Builder
@NoArgsConstructor  // Required by JPA for creating entities via reflection
@AllArgsConstructor // Required for @Builder to work with @NoArgsConstructor
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Assuming an auto-generated ID

    @Column(nullable = false)
    private String username;

    @Column(nullable = false, unique = true)
    private String accountCode;

    @Column(nullable = false)
    private int index;

    @Column(nullable = false)
    private String displayName;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "account_id") // Maps the foreign key in the Transaction table
    @Builder.Default
    private SortedSet<Transaction> transactions = new TreeSet<>();

    @Transient // Excludes this field from JPA persistence
    public BigDecimal getBalance() {
        return transactions.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
