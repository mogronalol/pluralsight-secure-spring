package pluralsight.m13.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pluralsight.m13.security.MaskedToString;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferModel extends MaskedToString {

    private String fromAccountCode;

    private String toAccountCode;

    private BigDecimal amount;
}

