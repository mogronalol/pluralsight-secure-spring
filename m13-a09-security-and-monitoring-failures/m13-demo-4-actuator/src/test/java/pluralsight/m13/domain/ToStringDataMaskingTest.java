package pluralsight.m13.domain;

import org.junit.jupiter.api.Test;
import pluralsight.m13.model.TransferModel;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ToStringDataMaskingTest {

    public static final LocalDateTime NOW = LocalDateTime.now();

    @Test
    void testAccountToString() {
        Account account = Account.builder()
                .username("user123")
                .accountCode("AC123456")
                .index(1)
                .displayName("John Doe")
                .build();

        String accountString = account.toString();
        assertThat(accountString)
                .doesNotContain("user123", "AC123456", "John Doe")
                .contains("1");
    }

    @Test
    void testEmployeeToString() {
        UUID employeeId = UUID.fromString("f47ac10b-58cc-4372-a567-0e02b2c3d479");
        Employee employee = Employee.builder()
                .employeeId(employeeId)
                .name("Alice")
                .department("HR")
                .build();

        String employeeString = employee.toString();
        assertThat(employeeString)
                .doesNotContain("Alice", employeeId.toString())
                .contains("HR");
    }

    @Test
    void testTransactionToString() {
        LocalDateTime date = LocalDateTime.of(2023, 10, 15, 14, 30, 0);
        Transaction transaction = Transaction.builder()
                .id(1001L)
                .date(date)
                .description("Payment received")
                .amount(new BigDecimal("150.00"))
                .build();

        String transactionString = transaction.toString();
        assertThat(transactionString)
                .contains("1001")
                .doesNotContain("Payment received", "150.00", date.toString());
    }

    @Test
    void testTransferModelToString() {
        TransferModel transfer = TransferModel.builder()
                .fromAccountCode("ACC1001")
                .toAccountCode("ACC1002")
                .amount(new BigDecimal("200.00"))
                .build();

        String transferString = transfer.toString();
        assertThat(transferString)
                .doesNotContain("ACC1001", "ACC1002", "200.00");
    }
}