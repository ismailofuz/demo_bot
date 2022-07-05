package mike.com.demo_bot.entity;

import lombok.*;

import javax.persistence.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Entity(name = "my_users")
public class DefaultUsers {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String name;

    private String phoneNumber;

    public DefaultUsers(String name, String phone_number) {
        this.name = name;
        this.phoneNumber = phone_number;
    }
}
