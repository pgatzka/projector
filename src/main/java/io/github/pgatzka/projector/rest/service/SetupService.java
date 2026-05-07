package io.github.pgatzka.projector.rest.service;

import io.github.pgatzka.projector.audit.SetupActorOverride;
import io.github.pgatzka.projector.data.service.AccountDataService;
import io.github.pgatzka.projector.jooq.tables.pojos.Account;
import io.github.pgatzka.projector.rest.dto.SetupRequest;
import io.github.pgatzka.projector.rest.exception.SetupAlreadyCompletedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class SetupService {

    private final AccountDataService accountDataService;
    private final PasswordEncoder passwordEncoder;

    public SetupService(AccountDataService accountDataService, PasswordEncoder passwordEncoder) {
        this.accountDataService = accountDataService;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Atomically check that no accounts exist, then create the first admin.
     * Uses SERIALIZABLE so a concurrent setup attempt can't race past the count.
     * In practice contention is zero for a solo tool, but the invariant is cheap to enforce.
     */
    @Transactional(readOnly = true)
    public boolean isRequired() {
        return accountDataService.countAll() == 0;
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Account completeSetup(SetupRequest request) {
        if (accountDataService.countAll() > 0) {
            throw new SetupAlreadyCompletedException();
        }
        UUID newId = UUID.randomUUID();
        SetupActorOverride.set("user:" + newId);
        try {
            Account toInsert = new Account();
            toInsert.setId(newId);
            toInsert.setEmail(request.email().trim().toLowerCase());
            toInsert.setPasswordHash(passwordEncoder.encode(request.password()));
            toInsert.setDisplayName(request.displayName());
            return accountDataService.create(toInsert);
        } finally {
            SetupActorOverride.clear();
        }
    }
}
