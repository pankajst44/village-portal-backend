package com.village.portal.config;

import com.village.portal.entity.User;
import com.village.portal.enums.Role;
import com.village.portal.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Runs once on every application startup.
 *
 * Creates default system users ONLY if they do not already exist.
 * Safe to run in production — it never overwrites existing records.
 *
 * DEFAULT CREDENTIALS (change immediately after first login):
 * ┌─────────────────┬──────────────────┬────────────┬──────────────────┐
 * │ Role            │ Username         │ Password   │ Purpose          │
 * ├─────────────────┼──────────────────┼────────────┼──────────────────┤
 * │ ADMIN           │ admin            │ Admin@1234 │ Full access       │
 * │ OFFICER         │ officer          │ Officer@123│ Field officer     │
 * │ AUDITOR         │ auditor          │ Auditor@123│ Read-only audit   │
 * └─────────────────┴──────────────────┴────────────┴──────────────────┘
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UserRepository  userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository,
                      PasswordEncoder passwordEncoder) {
        this.userRepository  = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        log.info("──────────────────────────────────────────");
        log.info("  Village Portal — Running Data Seeder");
        log.info("──────────────────────────────────────────");

        seedUser("admin",   "Admin@1234",   "System Administrator", "admin@village.gov.in",   Role.ADMIN);
        seedUser("officer", "Officer@123",  "Village Officer",       "officer@village.gov.in", Role.OFFICER);
        seedUser("auditor", "Auditor@123",  "Village Auditor",       "auditor@village.gov.in", Role.AUDITOR);

        log.info("  Data seeding complete.");
        log.info("──────────────────────────────────────────");
    }

    // ── Creates the user only if the username does not exist ──
    private void seedUser(String username, String rawPassword,
                          String fullName, String email, Role role) {

        if (userRepository.existsByUsername(username)) {
            log.info("  [SKIP] User '{}' already exists.", username);
            return;
        }

        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setFullName(fullName);
        user.setEmail(email);
        user.setRole(role);
        user.setIsActive(true);

        userRepository.save(user);
        log.info("  [CREATED] User '{}' with role '{}'.", username, role);
    }
}
