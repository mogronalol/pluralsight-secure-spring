package pluralsight.m13.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferModel {

    private String fromAccountCode;
    private String toAccountCode;
    private BigDecimal amount;
}

