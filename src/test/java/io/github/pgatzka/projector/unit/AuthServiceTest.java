package io.github.pgatzka.projector.unit;

import io.github.pgatzka.projector.audit.AccountPrincipal;
import io.github.pgatzka.projector.data.service.AccountDataService;
import io.github.pgatzka.projector.jooq.tables.pojos.Account;
import io.github.pgatzka.projector.rest.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthServiceTest {

    private AccountDataService accountDataService;
    private PasswordEncoder passwordEncoder;
    private AuthService service;

    @BeforeEach
    void setUp() {
        accountDataService = mock(AccountDataService.class);
        passwordEncoder = mock(PasswordEncoder.class);
        service = new AuthService(accountDataService, passwordEncoder);
    }

    @Test
    void authenticatesValidCredentials() {
        UUID id = UUID.fromString("01933f2a-0000-7000-8000-000000000010");
        Account account = new Account();
        account.setId(id);
        account.setEmail("admin@example.test");
        account.setDisplayName("Admin");
        account.setPasswordHash("$2a$10$hashed");

        when(accountDataService.findByEmail("admin@example.test")).thenReturn(Optional.of(account));
        when(passwordEncoder.matches("hunter22!", "$2a$10$hashed")).thenReturn(true);

        AccountPrincipal principal = service.authenticate("admin@example.test", "hunter22!");

        assertThat(principal.id()).isEqualTo(id);
        assertThat(principal.email()).isEqualTo("admin@example.test");
        assertThat(principal.displayName()).isEqualTo("Admin");
        verify(accountDataService).recordLogin(eq(id), any());
    }

    @Test
    void rejectsWrongPassword() {
        Account account = new Account();
        account.setId(UUID.randomUUID());
        account.setEmail("admin@example.test");
        account.setPasswordHash("$2a$10$hashed");

        when(accountDataService.findByEmail("admin@example.test")).thenReturn(Optional.of(account));
        when(passwordEncoder.matches("wrong", "$2a$10$hashed")).thenReturn(false);

        assertThatThrownBy(() -> service.authenticate("admin@example.test", "wrong"))
            .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void rejectsUnknownEmail() {
        when(accountDataService.findByEmail("nobody@example.test")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.authenticate("nobody@example.test", "anything"))
            .isInstanceOf(BadCredentialsException.class);
    }
}
