package uno.persistence;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

public class SchemaInitializer {

    private SchemaInitializer() {}

    public static void initialize(DataSource dataSource) {
        try (InputStream input = SchemaInitializer.class.getResourceAsStream("/db/schema.sql")) {
            if (input == null) {
                throw new IllegalStateException("Missing db/schema.sql");
            }
            String schema = new String(input.readAllBytes(), StandardCharsets.UTF_8);
            executeStatements(dataSource, schema);
        } catch (IOException e) {
            throw new IllegalStateException("Could not read schema.sql", e);
        }
    }

    private static void executeStatements(DataSource dataSource, String schema) {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            for (String sql : schema.split(";")) {
                String trimmed = sql.trim();
                if (!trimmed.isEmpty()) {
                    statement.execute(trimmed);
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Could not initialize database schema", e);
        }
    }
}
