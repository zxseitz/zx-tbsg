package ch.zxseitz.tbsg.repo.mongo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.io.IOException;
import java.nio.file.Paths;

@Configuration
public class MongoConfig {
    private static class MongoProperties {
        public String url;
        public String name;
        public String auth_scheme;
        public String user;
        public String password;
    }

    //todo validation and error handling

    private final MongoProperties properties;

    public MongoConfig(@Value("${app.db.mongo.config}") String configPath) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        this.properties = mapper.readValue(Paths.get(configPath).toFile(), MongoProperties.class);
    }

    @Bean
    public MongoClient mongoClient() {
        return MongoClients.create(MongoClientSettings.builder()
                .credential(MongoCredential.createCredential(properties.user, properties.auth_scheme, properties.password.toCharArray()))
                .applyConnectionString(new ConnectionString(properties.url))
                .build());
    }

    @Bean()
    public MongoTemplate mongoTemplate() {
        return new MongoTemplate(mongoClient(), properties.name);
    }
}
