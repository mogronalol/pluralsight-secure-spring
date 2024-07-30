package pluralsight.m10;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import pluralsight.m10.repository.GenericRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class DefaultMostSecureProfileTest {

    @Autowired
    private List<GenericRepository<?, ?>> allRepositories;

    @Test
    public void assertNoTestDataIsGenerated() {
        assertThat(allRepositories)
                .flatExtracting(GenericRepository::findAll)
                .isEmpty();
    }
}
