package ch.zxseitz.tbsg.repo.mongo;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.io.IOException;
import java.util.Properties;

@Configuration
public class MongoConfig {
    //todo validation and error handling

    private final Properties config;

    public MongoConfig() throws IOException {
        config = new Properties();
        config.load(MongoConfig.class.getClassLoader().getResourceAsStream("mongo.properties"));
    }

    @Bean
    public MongoClient mongoClient() {
        return MongoClients.create(MongoClientSettings.builder()
                .credential(MongoCredential.createCredential(config.getProperty("user"), config.getProperty("auth_scheme"), config.getProperty("pwd").toCharArray()))
                .applyConnectionString(new ConnectionString(config.getProperty("url")))
                .build());
    }

    @Bean()
    public MongoTemplate mongoTemplate() {
        return new MongoTemplate(mongoClient(), config.getProperty("name"));
    }
}
