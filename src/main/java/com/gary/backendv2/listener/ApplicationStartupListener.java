package com.gary.backendv2.listener;

import com.gary.backendv2.exception.AdminAccountExistsException;
import com.gary.backendv2.model.User;
import com.gary.backendv2.model.enums.RoleName;
import com.gary.backendv2.model.security.Role;
import com.gary.backendv2.repository.RoleRepository;
import com.gary.backendv2.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;

@Slf4j
@Component
public class ApplicationStartupListener implements ApplicationListener<ContextRefreshedEvent> {
    @Autowired
    UserRepository userRepository;
    @Autowired
    RoleRepository roleRepository;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    ApplicationContext applicationContext;

    @Value("${gary.app.admin.credentials.email}")
    private String adminEmail;
    @Value("${gary.app.admin.credentials.password}")
    private String adminPassword;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
       try {
           createAdminAccount();
       } catch (AdminAccountExistsException e) {
           log.info(e.getMessage());
       }
    }

    private void createAdminAccount() throws AdminAccountExistsException {
        Optional<User> adminUser = Optional.ofNullable(userRepository.getByEmail("admin@gary.com"));
        if (adminUser.isEmpty()) {
            Optional<Role> adminRole = Optional.ofNullable(roleRepository.findByName(RoleName.ADMIN));
            if (adminRole.isEmpty()) {
                log.error("ADMIN ROLE DOESN'T EXISTS!!! SHUTTING DOWN APPLICATION IMMEDIATELY!");
                SpringApplication.exit(applicationContext, () -> -1);
                return;
            }

            User user = new User();
            user.setPassword(passwordEncoder.encode(adminPassword));
            user.setEmail(adminEmail);
            user.setRoles(Set.of(adminRole.get()));

            userRepository.save(user);

            log.info("Admin account created: email: {}, password: {}", adminEmail, adminPassword);
        } else throw new AdminAccountExistsException("Admin account already exists.");
    }
}
