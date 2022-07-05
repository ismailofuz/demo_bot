package mike.com.demo_bot.repository;

import mike.com.demo_bot.entity.DefaultUsers;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DefaultUsersRepository extends JpaRepository<DefaultUsers, Integer> {

    Optional<DefaultUsers> findDefaultUsersByPhoneNumber(String phone_number);
}