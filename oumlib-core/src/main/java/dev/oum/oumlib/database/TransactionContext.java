package dev.oum.oumlib.database;

import dev.oum.oumlib.OumLib;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.RecordComponent;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class TransactionContext {

    private final Connection connection;
    private final long slowQueryThresholdMs;

    public TransactionContext(@NonNull Connection connection, long slowQueryThresholdMs) {
        this.connection = connection;
        this.slowQueryThresholdMs = slowQueryThresholdMs;
    }

    private static @Nullable Object getValueFromResultSet(@NonNull ResultSet rs, @NonNull String label,
                                                          @NonNull Class<?> type) throws SQLException {
        Object val = rs.getObject(label);
        if (val == null) return null;
        if (type == int.class || type == Integer.class) {
            return rs.getInt(label);
        } else if (type == double.class || type == Double.class) {
            return rs.getDouble(label);
        } else if (type == float.class || type == Float.class) {
            return rs.getFloat(label);
        } else if (type == long.class || type == Long.class) {
            return rs.getLong(label);
        } else if (type == boolean.class || type == Boolean.class) {
            return rs.getBoolean(label);
        } else if (type == String.class) {
            return rs.getString(label);
        }
        return val;
    }

    private static @NonNull String toCamelCase(@NonNull String snake) {
        StringBuilder sb = new StringBuilder();
        boolean nextUpper = false;
        for (int i = 0; i < snake.length(); i++) {
            char c = snake.charAt(i);
            if (c == '_') {
                nextUpper = true;
            } else {
                if (nextUpper) {
                    sb.append(Character.toUpperCase(c));
                    nextUpper = false;
                } else {
                    sb.append(Character.toLowerCase(c));
                }
            }
        }
        return sb.toString();
    }

    public @NonNull Connection connection() {
        return connection;
    }

    public int executeUpdate(@NonNull String sql, Object... params) throws SQLException {
        long start = System.currentTimeMillis();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }
            int affected = stmt.executeUpdate();
            long elapsed = System.currentTimeMillis() - start;
            if (elapsed > slowQueryThresholdMs) {
                OumLib.logger().warning("SLOW TRANSACTION UPDATE QUERY (" + elapsed + "ms): " + sql);
            }
            return affected;
        }
    }

    public @NonNull List<Map<String, Object>> executeQuery(@NonNull String sql, Object... params) throws SQLException {
        long start = System.currentTimeMillis();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                ResultSetMetaData md = rs.getMetaData();
                int columns = md.getColumnCount();
                List<Map<String, Object>> list = new ArrayList<>();
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>(columns);
                    for (int i = 1; i <= columns; i++) {
                        row.put(md.getColumnLabel(i), rs.getObject(i));
                    }
                    list.add(row);
                }
                long elapsed = System.currentTimeMillis() - start;
                if (elapsed > slowQueryThresholdMs) {
                    OumLib.logger().warning("SLOW TRANSACTION SELECT QUERY (" + elapsed + "ms): " + sql);
                }
                return list;
            }
        }
    }

    public <T> @NonNull List<T> executeQuery(@NonNull String sql, @NonNull RowMapper<T> mapper, Object... params) throws SQLException {
        long start = System.currentTimeMillis();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                List<T> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(mapper.map(rs));
                }
                long elapsed = System.currentTimeMillis() - start;
                if (elapsed > slowQueryThresholdMs) {
                    OumLib.logger().warning("SLOW TRANSACTION SELECT QUERY (" + elapsed + "ms): " + sql);
                }
                return list;
            }
        }
    }

    public <T> @NonNull List<T> executeQuery(@NonNull String sql, @NonNull Class<T> type, Object... params) throws SQLException {
        return executeQuery(sql, rs -> {
            try {
                if (type.isRecord()) {
                    RecordComponent[] components = type.getRecordComponents();
                    Class<?>[] paramTypes = new Class<?>[components.length];
                    Object[] args = new Object[components.length];
                    for (int i = 0; i < components.length; i++) {
                        RecordComponent comp = components[i];
                        paramTypes[i] = comp.getType();
                        args[i] = getValueFromResultSet(rs, comp.getName(), comp.getType());
                    }
                    Constructor<T> ctor = type.getDeclaredConstructor(paramTypes);
                    ctor.setAccessible(true);
                    return ctor.newInstance(args);
                } else {
                    T instance = type.getDeclaredConstructor().newInstance();
                    ResultSetMetaData md = rs.getMetaData();
                    int columns = md.getColumnCount();
                    for (int i = 1; i <= columns; i++) {
                        String label = md.getColumnLabel(i);
                        try {
                            Field field = type.getDeclaredField(label);
                            field.setAccessible(true);
                            field.set(instance, getValueFromResultSet(rs, label, field.getType()));
                        } catch (NoSuchFieldException ignored) {
                            String camel = toCamelCase(label);
                            try {
                                Field field = type.getDeclaredField(camel);
                                field.setAccessible(true);
                                field.set(instance, getValueFromResultSet(rs, label, field.getType()));
                            } catch (NoSuchFieldException ignored2) {
                            }
                        }
                    }
                    return instance;
                }
            } catch (Exception e) {
                throw new SQLException("Failed to map transaction row to class " + type.getName(), e);
            }
        }, params);
    }

    public int[] executeBatch(@NonNull String sql, @NonNull List<Object[]> parameterBatch) throws SQLException {
        long start = System.currentTimeMillis();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            for (Object[] params : parameterBatch) {
                for (int i = 0; i < params.length; i++) {
                    stmt.setObject(i + 1, params[i]);
                }
                stmt.addBatch();
            }
            int[] results = stmt.executeBatch();
            long elapsed = System.currentTimeMillis() - start;
            if (elapsed > slowQueryThresholdMs) {
                OumLib.logger().warning("SLOW TRANSACTION BATCH QUERY (" + elapsed + "ms): " + sql);
            }
            return results;
        }
    }
}
