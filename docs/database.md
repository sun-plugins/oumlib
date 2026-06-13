# Database Tools

OumLib includes a high-performance SQL database wrapper powered by **HikariCP 7.0.2** supporting both **SQLite** and **MySQL**. It utilizes Java 25 virtual threads via `Promise` for non-blocking asynchronous operations, preventing server thread stalling.

---

## 1. Connecting to a Database

OumLib automatically establishes a Hikari connection pool with sensible defaults (like statement caching, performance optimizations, and correct write-concurrency limits).

### SQLite Connection (Single-threaded file lock safety)
For SQLite, OumLib enforces a `maximumPoolSize` of `1` by default. This is the industry-standard best practice to prevent concurrent write locks on the SQLite file.
```java
import dev.oum.oumlib.database.Database;
import java.io.File;

// Connects using sensible SQLite defaults
Database db = Database.sqlite(new File(getDataFolder(), "data.db"));
```

### SQLite Connection with Custom Hikari Settings
```java
Database db = Database.sqlite(new File(getDataFolder(), "data.db"), config -> {
    config.setConnectionTimeout(10000); // 10 seconds
});
```

### MySQL Connection
For MySQL, OumLib automatically activates optimized configuration flags (e.g., preparation statement caching, server prep statement usage, and batch rewrite support) to ensure lowest latency.
```java
Database db = Database.mysql("127.0.0.1", 3306, "my_database", "username", "password");
```

### MySQL Connection with Custom Hikari Settings
You can customize Hikari parameters using the config customizer callback:
```java
Database db = Database.mysql("127.0.0.1", 3306, "my_database", "username", "password", config -> {
    config.setMaximumPoolSize(20);
    config.setMinimumIdle(5);
    config.setPoolName("MyPlugin-Pool");
});
```

---

## 2. Executing Updates (DDL/DML)

Use `.executeUpdate(String sql, Object... params)` to run `CREATE TABLE`, `INSERT`, `UPDATE`, or `DELETE` statements asynchronously on virtual threads:

```java
// Create a table if it does not exist
db.executeUpdate("CREATE TABLE IF NOT EXISTS players (uuid VARCHAR(36) PRIMARY KEY, coins INT)")
    .thenAcceptSync(result -> {
        getLogger().info("Database initialization check complete!");
    });

// Insert or update player coins
String playerUuid = player.getUniqueId().toString();
db.executeUpdate("INSERT INTO players (uuid, coins) VALUES (?, ?) ON DUPLICATE KEY UPDATE coins = ?", 
    playerUuid, 100, 100);
```

---

## 3. Querying Data

Use `.executeQuery(String sql, Object... params)` to retrieve data. Results are returned as a list of key-value maps representing rows and columns:

```java
db.executeQuery("SELECT coins FROM players WHERE uuid = ?", player.getUniqueId().toString())
    .thenAcceptSync(rows -> {
        if (rows.isEmpty()) {
            player.sendMessage("No profile found.");
            return;
        }
        
        // Retrieve values by column name
        int coins = (int) rows.getFirst().get("coins");
        player.sendMessage("You have " + coins + " coins!");
    });
```

---

## 4. Batch Operations

To execute multiple updates in bulk efficiently (e.g., during automatic profile saving), use `.executeBatch(String sql, List<Object[]> parameterBatch)`:

```java
List<Object[]> batchParams = new ArrayList<>();
for (Player player : Bukkit.getOnlinePlayers()) {
    batchParams.add(new Object[] { player.getUniqueId().toString(), 150, 150 });
}

db.executeBatch("INSERT INTO players (uuid, coins) VALUES (?, ?) ON DUPLICATE KEY UPDATE coins = ?", batchParams)
    .thenAcceptSync(rowsUpdated -> {
        getLogger().info("Successfully saved " + rowsUpdated.length + " player records in batch.");
    });
```

---

## 5. Transactions

Use `.transaction(TransactionCallback<R>)` to run multiple queries sequentially inside a transaction on a single connection. The wrapper automatically disables auto-commit, commits upon success, and rolls back if an exception occurs:

```java
db.transaction(conn -> {
    // Both statements execute sequentially on the same connection
    try (var stmt1 = conn.prepareStatement("UPDATE players SET coins = coins - 10 WHERE uuid = ?");
         var stmt2 = conn.prepareStatement("INSERT INTO transactions (uuid, amount) VALUES (?, -10)")) {
        
        stmt1.setString(1, playerUuid);
        stmt1.executeUpdate();
        
        stmt2.setString(1, playerUuid);
        stmt2.executeUpdate();
    }
    return null;
}).whenCompleteSync(
    success -> getLogger().info("Transaction committed successfully!"),
    error -> getLogger().severe("Transaction failed and rolled back: " + error.getMessage())
);
```

---

## 6. Schema Script Execution

Use `.executeScript(String sqlScript)` to execute a multi-statement SQL script (e.g. schema tables setup). It splits statements by semicolons and filters out line (`--`) and block (`/* */`) comments:

```java
String initScript = 
    "CREATE TABLE IF NOT EXISTS players (uuid VARCHAR(36) PRIMARY KEY, coins INT);\n" +
    "CREATE TABLE IF NOT EXISTS logs (id INTEGER PRIMARY KEY AUTOINCREMENT, msg TEXT);";

db.executeScript(initScript)
    .thenAcceptSync(v -> getLogger().info("Schema successfully created."));
```

---

## 7. Advanced Integration (Row Mapping & Resource Loading)

### Automatic Class & Record Mapping
Instead of mapping database fields manually, you can pass a Java `Record` or custom class directly. OumLib will automatically inspect constructor parameters or class fields and map the query result rows directly:

```java
public record PlayerCoins(String uuid, int coins) {}

// Auto-maps fields in the result set to the record constructor
db.executeQuery("SELECT uuid, coins FROM players", PlayerCoins.class)
    .thenAcceptSync(profiles -> {
        for (PlayerCoins profile : profiles) {
            getLogger().info(profile.uuid() + " has " + profile.coins() + " coins!");
        }
    });
```

### Manual Mapping with `RowMapper`
If you need custom mapping behavior (such as custom data type conversions or composite objects), implement a custom `RowMapper`:

```java
import dev.oum.oumlib.database.RowMapper;

public record PlayerCoins(String uuid, int coins) {}

// Executing and mapping the result set manually
db.executeQuery("SELECT uuid, coins FROM players", rs -> new PlayerCoins(
    rs.getString("uuid"),
    rs.getInt("coins")
)).thenAcceptSync(profiles -> {
    for (PlayerCoins profile : profiles) {
        getLogger().info(profile.uuid() + " has " + profile.coins() + " coins!");
    }
});
```

### Loading SQL Scripts from Plugin Resources
Instead of hardcoding script strings, you can execute SQL scripts directly from your JAR resource folder (e.g. `schema.sql`) using an `InputStream`:

```java
import java.io.InputStream;

InputStream schemaStream = getResource("schema.sql");
if (schemaStream != null) {
    db.executeScript(schemaStream)
        .thenAcceptSync(v -> getLogger().info("Database tables initialized from schema.sql!"))
        .whenCompleteSync(null, err -> getLogger().severe("Failed to initialize database: " + err.getMessage()));
}
```

---

## 8. Custom ORMs & Raw DataSource Access

If you are using external SQL libraries (like JOOQ, Requery, or MyBatis) or need raw access to the datasource, retrieve it directly:

```java
import com.zaxxer.hikari.HikariDataSource;

HikariDataSource ds = db.dataSource();
```

To close the connection pool and release resources on plugin disable:
```java
db.close();
```
