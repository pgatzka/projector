package io.github.pgatzka.projector.data.service;

import io.github.pgatzka.projector.data.repository.AccountRepository;
import io.github.pgatzka.projector.jooq.tables.pojos.Account;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class AccountDataService {

    private final AccountRepository repository;

    public AccountDataService(AccountRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public long countAll() {
        return repository.countAll();
    }

    @Transactional(readOnly = true)
    public Optional<Account> findByEmail(String email) {
        return repository.findByEmailIgnoreCase(email);
    }

    public Account create(Account account) {
        return repository.insert(account);
    }

    public void recordLogin(UUID accountId, OffsetDateTime when) {
        repository.updateLastLoginAt(accountId, when);
    }
}
