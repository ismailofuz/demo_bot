package mike.com.demo_bot.repository;

import mike.com.demo_bot.entity.Target;
import org.springframework.data.jpa.repository.JpaRepository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

public interface TargetRepository extends JpaRepository<Target, Long> {
    Optional<Target> findByUser_IdAndIsDoneFalse(Long id);

    List<Target> findAllByCreatedAtAfter(Timestamp timestamp);
    List<Target> findAllByIsDoneFalse();

}