package com.project.demo.logic.entity.role;

import com.project.demo.logic.entity.user.TblUser;
import com.project.demo.logic.entity.user.UserRepository;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Optional;

@Component
@Order(3)
public class TestUserSeeder implements ApplicationListener<ContextRefreshedEvent> {
    private final TblRoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public TestUserSeeder(TblRoleRepository roleRepository, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        this.createTestUsers();
    }

    private void createTestUsers() {
        String[][] testUsers = {
                {"Carlos",    "Rodriguez", "201234567", "carlos.test@gmail.com",    "61234567", "2000-01-15"},
                {"Maria",     "Gonzalez",  "302345678", "maria.test@gmail.com",     "62345678", "1999-03-20"},
                {"Juan",      "Perez",     "103456789", "juan.test@gmail.com",      "63456789", "1998-07-10"},
                {"Ana",       "Martinez",  "404567890", "ana.test@gmail.com",       "64567890", "1997-11-25"},
                {"Pedro",     "Lopez",     "505678901", "pedro.test@gmail.com",     "65678901", "2001-02-14"},
                {"Sofia",     "Hernandez", "206789012", "sofia.test@gmail.com",     "66789012", "2000-05-30"},
                {"Diego",     "Ramirez",   "307890123", "diego.test@gmail.com",     "67890123", "1999-09-05"},
                {"Laura",     "Torres",    "108901234", "laura.test@gmail.com",     "68901234", "1998-12-20"},
                {"Miguel",    "Sanchez",   "409012345", "miguel.test@gmail.com",    "69012345", "2002-04-08"},
                {"Valentina", "Castro",    "500123456", "valentina.test@gmail.com", "60123456", "2001-08-17"},
        };

        String testPassword = "Test123!@";

        // Alternar entre BUYER y SELLER
        RoleEnum[] roles = {RoleEnum.BUYER, RoleEnum.SELLER};

        for (int i = 0; i < testUsers.length; i++) {
            String[] data = testUsers[i];
            String email = data[3];

            Optional<TblUser> optionalUser = userRepository.findByEmail(email);
            if (optionalUser.isPresent()) {
                continue;
            }

            RoleEnum roleEnum = roles[i % roles.length];
            Optional<TblRole> optionalRole = roleRepository.findByTitle(roleEnum);
            if (optionalRole.isEmpty()) {
                continue;
            }

            TblUser user = new TblUser();
            user.setName(data[0]);
            user.setLastName1(data[1]);
            user.setIdentification(data[2]);
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode(testPassword));
            user.setBirthDate(LocalDate.parse(data[5]));
            user.setPhoneNumber(data[4]);
            user.setState("Active");
            user.setRole(optionalRole.get());

            userRepository.save(user);
        }
    }
}
