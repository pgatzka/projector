package io.github.pgatzka.projector.data.repository;

import io.github.pgatzka.projector.jooq.tables.pojos.Account;
import io.github.pgatzka.projector.jooq.tables.records.AccountRecord;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static io.github.pgatzka.projector.jooq.tables.Account.ACCOUNT;

@Repository
public class AccountRepository {

    private final DSLContext dsl;

    public AccountRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    public long countAll() {
        return dsl.fetchCount(ACCOUNT);
    }

    public Optional<Account> findByEmailIgnoreCase(String email) {
        return dsl.selectFrom(ACCOUNT)
            .where(ACCOUNT.EMAIL.equalIgnoreCase(email.trim()))
            .fetchOptionalInto(Account.class);
    }

    public Account insert(Account account) {
        AccountRecord record = dsl.newRecord(ACCOUNT, account);
        record.store();
        return record.into(Account.class);
    }

    public int updateLastLoginAt(UUID id, OffsetDateTime when) {
        return dsl.update(ACCOUNT)
            .set(ACCOUNT.LAST_LOGIN_AT, when)
            .where(ACCOUNT.ID.eq(id))
            .execute();
    }
}
