package com.project.demo.logic.entity.role;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

@Component
@Order(1)
public class RoleSeeder implements ApplicationListener<ContextRefreshedEvent> {

    private final TblRoleRepository roleRepository;


    public RoleSeeder(TblRoleRepository roleRepository) {

        this.roleRepository = roleRepository;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        this.loadRoles();
    }

    private void loadRoles() {
        RoleEnum[] roleTitles = new RoleEnum[] { RoleEnum.SELLER, RoleEnum.BUYER, RoleEnum.ADMIN, RoleEnum.SUPER_ADMIN, RoleEnum.PENDING };
        Map<RoleEnum, String> roleDescriptionMap = Map.of(
                RoleEnum.SELLER, "Seller user role",
                RoleEnum.BUYER, "Buyer user role",
                RoleEnum.ADMIN, "Administrator role",
                RoleEnum.SUPER_ADMIN, "Super Administrator role",
                RoleEnum.PENDING,"Pending"
        );


        Arrays.stream(roleTitles).forEach((roleTitle) -> {
            Optional<TblRole> optionalRole = roleRepository.findByTitle(roleTitle);


            optionalRole.ifPresentOrElse(System.out::println, () -> {
                TblRole roleToCreate = new TblRole();


                roleToCreate.setTitle(roleTitle);
                roleToCreate.setDescription(roleDescriptionMap.get(roleTitle));


                roleRepository.save(roleToCreate);
            });
        });
    }
}