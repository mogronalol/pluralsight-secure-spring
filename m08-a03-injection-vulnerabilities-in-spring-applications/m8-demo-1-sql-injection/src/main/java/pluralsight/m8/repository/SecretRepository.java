package pluralsight.m8.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pluralsight.m8.domain.Secret;

@Repository
public interface SecretRepository extends JpaRepository<Secret, Long>{

}
