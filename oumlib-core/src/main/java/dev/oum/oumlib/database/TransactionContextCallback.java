package dev.oum.oumlib.database;

import org.jspecify.annotations.NonNull;

import java.sql.SQLException;

@FunctionalInterface
public interface TransactionContextCallback<R> {
    R execute(@NonNull TransactionContext context) throws SQLException;
}
