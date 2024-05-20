package pluralsight.m7.domain;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@Builder
@NoArgsConstructor  // Required by JPA for creating entities via reflection
@AllArgsConstructor // Required for @Builder to work with @NoArgsConstructor
public class Transaction implements Comparable<Transaction> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id; // Assuming an auto-generated ID

    @Column(nullable = false)
    private LocalDateTime date;

    @Column(nullable = false, length = 200)
    private String description;

    @Column(nullable = false)
    private BigDecimal amount;

    @Override
    public int compareTo(final Transaction o) {
        return o.date.compareTo(this.date);
    }
}
