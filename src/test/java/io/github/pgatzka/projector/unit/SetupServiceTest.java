package io.github.pgatzka.projector.unit;

import io.github.pgatzka.projector.audit.SetupActorOverride;
import io.github.pgatzka.projector.data.service.AccountDataService;
import io.github.pgatzka.projector.jooq.tables.pojos.Account;
import io.github.pgatzka.projector.rest.dto.SetupRequest;
import io.github.pgatzka.projector.rest.exception.SetupAlreadyCompletedException;
import io.github.pgatzka.projector.rest.service.SetupService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SetupServiceTest {

    private AccountDataService accountDataService;
    private PasswordEncoder passwordEncoder;
    private SetupService service;

    @BeforeEach
    void setUp() {
        accountDataService = mock(AccountDataService.class);
        passwordEncoder = mock(PasswordEncoder.class);
        service = new SetupService(accountDataService, passwordEncoder);
    }

    @AfterEach
    void clearOverride() {
        SetupActorOverride.clear();
    }

    @Test
    void completesSetupWhenNoAccountsYet() {
        when(accountDataService.countAll()).thenReturn(0L);
        when(passwordEncoder.encode("hunter22!")).thenReturn("$2a$10$hashed");
        when(accountDataService.create(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));

        Account created = service.completeSetup(
            new SetupRequest("admin@example.test", "hunter22!", "Admin")
        );

        assertThat(created.getEmail()).isEqualTo("admin@example.test");
        assertThat(created.getPasswordHash()).isEqualTo("$2a$10$hashed");
        assertThat(created.getDisplayName()).isEqualTo("Admin");
    }

    @Test
    void rejectsSetupWhenAccountAlreadyExists() {
        when(accountDataService.countAll()).thenReturn(1L);

        assertThatThrownBy(() -> service.completeSetup(
            new SetupRequest("admin@example.test", "hunter22!", "Admin")
        )).isInstanceOf(SetupAlreadyCompletedException.class);
    }
}
