package mike.com.demo_bot.repository;

import mike.com.demo_bot.entity.Messages;
import mike.com.demo_bot.entity.constants.MessageType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MessagesRepository extends JpaRepository<Messages, Long> {

    Optional<Messages> findBySender_IdAndIsSentIsFalse(Long id);

    List<Messages> findAllByIsSentTrueAndType(MessageType type);
}