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
@Order(2)
public class SuperAdminSeeder implements ApplicationListener<ContextRefreshedEvent> {
    private final TblRoleRepository roleRepository;

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;


    public SuperAdminSeeder(

            TblRoleRepository roleRepository,

            UserRepository  userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        this.createSuperAdministrator();
    }

    private void createSuperAdministrator() {
        TblUser superAdmin = new TblUser();
        superAdmin.setName("Super");
        superAdmin.setLastName1("Admin");
        superAdmin.setIdentification("111111111");
        superAdmin.setEmail("super.admin@gmail.com");
        superAdmin.setPassword("superadmin123");
        superAdmin.setBirthDate(LocalDate.of(2000, 1, 1));
        superAdmin.setPhoneNumber("22222222");
        superAdmin.setState("Active");

        Optional<TblRole> optionalRole = roleRepository.findByTitle(RoleEnum.SUPER_ADMIN);
        Optional<TblUser> optionalUser = userRepository.findByEmail(superAdmin.getEmail());

        if (optionalRole.isEmpty() || optionalUser.isPresent()) {
            return;
        }

        var user = new TblUser();
        user.setName(superAdmin.getName());
        user.setLastName1(superAdmin.getLastName1());
        user.setIdentification(superAdmin.getIdentification());
        user.setEmail(superAdmin.getEmail());
        user.setPassword(passwordEncoder.encode(superAdmin.getPassword()));
        user.setBirthDate(superAdmin.getBirthDate());
        user.setPhoneNumber(superAdmin.getPhoneNumber());
        user.setState(superAdmin.getState());
        user.setRole(optionalRole.get());

        userRepository.save(user);
    }
}