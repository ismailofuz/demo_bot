package mike.com.demo_bot.entity;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;


import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.sql.Timestamp;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Entity
public class BotUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String chatId;

    private String state ;

    private String email;

    private String fullName;

    private String phoneNumber;

    private String position;

    @CreationTimestamp
    private Timestamp createdAt;

    private String role;

}
