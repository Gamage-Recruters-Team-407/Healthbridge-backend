package lk.gamage.backend.healthbridgebackend.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.MongoConverter;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;

@Configuration
public class MongoConfig extends AbstractMongoClientConfiguration {

    @Value("${spring.data.mongodb.database:health-bridge-dev}")
    private String databaseName;

    @Value("${spring.data.mongodb.uri:mongodb://localhost:27017/health-bridge-dev}")
    private String connectionUri;

    @Override
    protected String getDatabaseName() {
        return databaseName;
    }

    @Override
    @Bean
    public MongoClient mongoClient() {
        MongoClient client = MongoClients.create(MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(connectionUri))
                .applyToClusterSettings(builder -> builder.serverSelectionTimeout(8, TimeUnit.SECONDS))
                .applyToSocketSettings(builder -> builder.connectTimeout(5, TimeUnit.SECONDS)
                        .readTimeout(10, TimeUnit.SECONDS))
                .applyToConnectionPoolSettings(builder -> builder.maxWaitTime(8, TimeUnit.SECONDS))
                .build());
        try {
            warmUp(() -> client.getDatabase(databaseName).runCommand(new org.bson.Document("ping", 1)));
            return client;
        } catch (RuntimeException error) {
            client.close();
            throw error;
        }
    }

    static void warmUp(Runnable ping) {
        for (int attempt = 1; attempt <= 4; attempt++) {
            try {
                ping.run();
                return;
            } catch (com.mongodb.MongoTimeoutException | com.mongodb.MongoSocketException error) {
                if (attempt == 4) throw error;
                org.slf4j.LoggerFactory.getLogger(MongoConfig.class)
                        .warn("MongoDB is not reachable yet; retrying startup connection ({}/4).", attempt + 1);
            }
        }
    }

    @Bean
    public MongoTemplate mongoTemplate() {
        return new MongoTemplate(mongoClient(), getDatabaseName());
    }

    @Bean
    public GridFsTemplate gridFsTemplate(MongoDatabaseFactory dbFactory, MongoConverter converter) {
        return new GridFsTemplate(dbFactory, converter);
    }
}
