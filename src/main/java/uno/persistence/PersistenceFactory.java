package uno.persistence;

import javax.sql.DataSource;

import org.apache.ibatis.datasource.pooled.PooledDataSource;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;

public class PersistenceFactory {

    public static final String DEFAULT_DB_URL = "jdbc:h2:file:./data/uno;AUTO_SERVER=TRUE";

    private PersistenceFactory() {}

    public static GameHistoryRepository createDefault() {
        return create(databaseUrl());
    }

    public static GameHistoryRepository create(String jdbcUrl) {
        PooledDataSource dataSource = new PooledDataSource();
        dataSource.setDriver("org.h2.Driver");
        dataSource.setUrl(jdbcUrl);
        String user = databaseUser();
        if (!user.isBlank()) {
            dataSource.setUsername(user);
            dataSource.setPassword(databasePassword());
        }
        SchemaInitializer.initialize(dataSource);
        return new GameHistoryRepository(sqlSessionFactory(dataSource));
    }

    private static String databaseUrl() {
        String fromProperty = System.getProperty("uno.db.url");
        if (fromProperty != null && !fromProperty.isBlank()) return fromProperty;
        String fromEnv = System.getenv("UNO_DB_URL");
        if (fromEnv != null && !fromEnv.isBlank()) return fromEnv;
        return DEFAULT_DB_URL;
    }

    private static String databaseUser() {
        String fromProperty = System.getProperty("uno.db.user");
        if (fromProperty != null) return fromProperty;
        String fromEnv = System.getenv("UNO_DB_USER");
        return fromEnv == null ? "" : fromEnv;
    }

    private static String databasePassword() {
        String fromProperty = System.getProperty("uno.db.password");
        if (fromProperty != null) return fromProperty;
        String fromEnv = System.getenv("UNO_DB_PASSWORD");
        return fromEnv == null ? "" : fromEnv;
    }

    private static SqlSessionFactory sqlSessionFactory(DataSource dataSource) {
        Environment environment = new Environment(
                "uno",
                new JdbcTransactionFactory(),
                dataSource
        );
        Configuration configuration = new Configuration(environment);
        configuration.setMapUnderscoreToCamelCase(true);
        configuration.addMapper(PlayerMapper.class);
        configuration.addMapper(GameMapper.class);
        configuration.addMapper(RoundMapper.class);
        configuration.addMapper(ScoreMapper.class);
        return new SqlSessionFactoryBuilder().build(configuration);
    }
}
