package pluralsight.m7.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pluralsight.m7.domain.Secret;

@Repository
public interface SecretRepository extends JpaRepository<Secret, Long>{

}
