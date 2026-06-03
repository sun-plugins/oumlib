package dev.oum.oumlib.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import dev.oum.oumlib.scheduler.Promise;
import org.jspecify.annotations.NonNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public final class Database {

    private final HikariDataSource dataSource;

    private Database(@NonNull HikariConfig config) {
        this.dataSource = new HikariDataSource(config);
    }

    public static @NonNull Database sqlite(@NonNull File file) {
        return sqlite(file, _ -> {
        });
    }

    public static @NonNull Database sqlite(@NonNull File file, @NonNull Consumer<HikariConfig> configCustomizer) {
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:sqlite:" + file.getAbsolutePath());
        config.setDriverClassName("org.sqlite.JDBC");

        config.setMaximumPoolSize(1);
        config.setConnectionTestQuery("SELECT 1");

        configCustomizer.accept(config);
        return new Database(config);
    }

    public static @NonNull Database mysql(
            @NonNull String host,
            int port,
            @NonNull String database,
            @NonNull String username,
            @NonNull String password
    ) {
        return mysql(host, port, database, username, password, _ -> {
        });
    }

    public static @NonNull Database mysql(
            @NonNull String host,
            int port,
            @NonNull String database,
            @NonNull String username,
            @NonNull String password,
            @NonNull Consumer<HikariConfig> configCustomizer
    ) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database);
        config.setUsername(username);
        config.setPassword(password);

        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useServerPrepStmts", "true");
        config.addDataSourceProperty("useLocalSessionState", "true");
        config.addDataSourceProperty("rewriteBatchedStatements", "true");
        config.addDataSourceProperty("cacheResultSetMetadata", "true");
        config.addDataSourceProperty("cacheServerConfiguration", "true");
        config.addDataSourceProperty("elideSetAutoCommits", "true");
        config.addDataSourceProperty("maintainTimeStats", "false");

        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);
        config.setConnectionTimeout(5000);

        configCustomizer.accept(config);
        return new Database(config);
    }

    public static @NonNull Database custom(@NonNull HikariConfig config) {
        return new Database(config);
    }

    public @NonNull Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public void close() {
        if (!dataSource.isClosed()) {
            dataSource.close();
        }
    }

    public @NonNull HikariDataSource dataSource() {
        return dataSource;
    }

    public @NonNull Promise<Integer> executeUpdate(@NonNull String sql, Object... params) {
        return Promise.supplyVirtual(() -> {
            try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                for (int i = 0; i < params.length; i++) {
                    stmt.setObject(i + 1, params[i]);
                }
                return stmt.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException("SQL update execution failed: " + sql, e);
            }
        });
    }

    /**
     * Executes a SELECT query asynchronously using virtual threads and returns mapped results.
     */
    public @NonNull Promise<List<Map<String, Object>>> executeQuery(@NonNull String sql, Object... params) {
        return Promise.supplyVirtual(() -> {
            try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
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
                    return list;
                }
            } catch (SQLException e) {
                throw new RuntimeException("SQL query execution failed: " + sql, e);
            }
        });
    }

    public <T> @NonNull Promise<List<T>> executeQuery(@NonNull String sql, @NonNull RowMapper<T> mapper, Object... params) {
        return Promise.supplyVirtual(() -> {
            try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                for (int i = 0; i < params.length; i++) {
                    stmt.setObject(i + 1, params[i]);
                }
                try (ResultSet rs = stmt.executeQuery()) {
                    List<T> list = new ArrayList<>();
                    while (rs.next()) {
                        list.add(mapper.map(rs));
                    }
                    return list;
                }
            } catch (SQLException e) {
                throw new RuntimeException("SQL query execution failed: " + sql, e);
            }
        });
    }

    public @NonNull Promise<int[]> executeBatch(@NonNull String sql, @NonNull List<Object[]> parameterBatch) {
        return Promise.supplyVirtual(() -> {
            try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                for (Object[] params : parameterBatch) {
                    for (int i = 0; i < params.length; i++) {
                        stmt.setObject(i + 1, params[i]);
                    }
                    stmt.addBatch();
                }
                return stmt.executeBatch();
            } catch (SQLException e) {
                throw new RuntimeException("SQL batch execution failed: " + sql, e);
            }
        });
    }

    public <R> @NonNull Promise<R> transaction(@NonNull TransactionCallback<R> callback) {
        return Promise.supplyVirtual(() -> {
            try (Connection conn = getConnection()) {
                boolean wasAutoCommit = conn.getAutoCommit();
                try {
                    conn.setAutoCommit(false);
                    R result = callback.execute(conn);
                    conn.commit();
                    return result;
                } catch (Throwable t) {
                    try {
                        conn.rollback();
                    } catch (SQLException ex) {
                        t.addSuppressed(ex);
                    }
                    throw t;
                } finally {
                    try {
                        conn.setAutoCommit(wasAutoCommit);
                    } catch (SQLException ignored) {
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException("SQL transaction execution failed", e);
            }
        });
    }

    public @NonNull Promise<Void> executeScript(@NonNull String sqlScript) {
        return Promise.runVirtual(() -> {
            try (Connection conn = getConnection()) {
                String[] statements = sqlScript.split(";");
                try (Statement stmt = conn.createStatement()) {
                    for (String sql : statements) {
                        String trimmed = sql.replaceAll("(?m)^\\s*--.*$", "") // remove line comments
                                .replaceAll("(?s)/\\*.*?\\*/", "") // remove block comments
                                .trim();
                        if (!trimmed.isEmpty()) {
                            stmt.execute(trimmed);
                        }
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException("SQL script execution failed", e);
            }
        });
    }

    public @NonNull Promise<Void> executeScript(@NonNull InputStream stream) {
        return Promise.runVirtual(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append("\n");
                }
                executeScript(sb.toString()).toCompletableFuture().join();
            } catch (Exception e) {
                throw new RuntimeException("SQL script loading failed", e);
            }
        });
    }
}
