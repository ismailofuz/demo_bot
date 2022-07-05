package mike.com.demo_bot.entity;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.sql.Timestamp;


@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Entity
public class Target {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private  BotUser user;

    private String text;

    @CreationTimestamp
    private Timestamp createdAt;

    private String results; // null ga tekshirish oson bo'ladi nasib bo'lsa

    private Timestamp finishedAt;

    private boolean isDone;
}
