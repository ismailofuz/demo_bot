package mike.com.demo_bot.repository;

import mike.com.demo_bot.entity.BotUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface BotUserRepository extends JpaRepository<BotUser, Long> {
    Optional<BotUser> findByChatId(String chatId);

//    public static void main(String[] args) {
//
//        LocalDateTime now = LocalDateTime.now();
//        System.out.println("now.plusDays(7) = " + now.plusDays(7));
//        String time = LocalDateTime.now().toString();
//        System.out.println("Time : "+time.substring(0,10)+" "+time.substring(11,19));
//    }
}