package mike.com.demo_bot.loader;

import lombok.RequiredArgsConstructor;
import mike.com.demo_bot.entity.DefaultUsers;
import mike.com.demo_bot.repository.DefaultUsersRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    @Value("${spring.jpa.hibernate.ddl-auto}")
    private String ddl;


private final DefaultUsersRepository defaultUsersRepository;
    @Override
    public void run(String... args)  {
        if (ddl.equals("create")) {
//            adding new default users

            defaultUsersRepository.save(new DefaultUsers("Utkirjon","+998909741228"));
            defaultUsersRepository.save(new DefaultUsers("UX/UI designer","+998332002022"));
            defaultUsersRepository.save(new DefaultUsers("UX/UI designer","+998998359015"));

        }
    }
}
