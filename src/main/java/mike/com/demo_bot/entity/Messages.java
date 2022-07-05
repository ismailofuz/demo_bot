package mike.com.demo_bot.entity;

import lombok.*;
import mike.com.demo_bot.entity.constants.MessageType;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.sql.Timestamp;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Entity(name = "all_messages")
public class Messages {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private BotUser sender;

     @ManyToOne
    private BotUser receiver;

     @Enumerated(EnumType.STRING)
    private MessageType type; // type is crucial

    @CreationTimestamp
    private Timestamp createdAt;

    private String text;

    private boolean isSent ;
}

// notice
// comment : receiver is null
// public : receiver is null