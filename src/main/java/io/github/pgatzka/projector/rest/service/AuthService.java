package io.github.pgatzka.projector.rest.service;

import io.github.pgatzka.projector.audit.AccountPrincipal;
import io.github.pgatzka.projector.data.service.AccountDataService;
import io.github.pgatzka.projector.jooq.tables.pojos.Account;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.Optional;

@Service
public class AuthService {

    private final AccountDataService accountDataService;
    private final PasswordEncoder passwordEncoder;

    public AuthService(AccountDataService accountDataService, PasswordEncoder passwordEncoder) {
        this.accountDataService = accountDataService;
        this.passwordEncoder = passwordEncoder;
    }

    public AccountPrincipal authenticate(String email, String rawPassword) {
        Optional<Account> maybe = accountDataService.findByEmail(email.trim());
        Account account = maybe.orElseThrow(() -> new BadCredentialsException("Invalid email or password"));
        if (!passwordEncoder.matches(rawPassword, account.getPasswordHash())) {
            throw new BadCredentialsException("Invalid email or password");
        }
        accountDataService.recordLogin(account.getId(), OffsetDateTime.now());
        return new AccountPrincipal(account.getId(), account.getEmail(), account.getDisplayName());
    }
}
