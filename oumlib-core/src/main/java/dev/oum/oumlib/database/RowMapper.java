package dev.oum.oumlib.database;

import org.jspecify.annotations.NonNull;

import java.sql.ResultSet;
import java.sql.SQLException;

@FunctionalInterface
public interface RowMapper<T> {
    T map(@NonNull ResultSet rs) throws SQLException;
}
