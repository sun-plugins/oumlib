package dev.oum.oumlib.database;

import org.jspecify.annotations.NonNull;

import java.sql.Connection;
import java.sql.SQLException;

@FunctionalInterface
public interface TransactionCallback<R> {
    R execute(@NonNull Connection connection) throws SQLException;
}
