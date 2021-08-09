package ch.zxseitz.tbsg.server.repo.mongo

import com.fasterxml.jackson.databind.ObjectMapper
import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.MongoCredential
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.core.MongoTemplate

import java.nio.file.Paths

@Configuration
open class MongoConfig(@Value("\${app.db.mongo.config}") private val configPath: String) {
    private data class MongoProperties(
        val url: String,
        val name: String,
        val auth_scheme: String,
        val user: String,
        val password: String
    )

    //todo validation and error handling

    private val properties: MongoProperties

    init {
        val mapper = ObjectMapper()
        this.properties = mapper.readValue(Paths.get(configPath).toFile(), MongoProperties::class.java)
    }

    @Bean
    open fun mongoClient(): MongoClient {
        return MongoClients.create(
            MongoClientSettings.builder()
                .credential(
                    MongoCredential.createCredential(
                        properties.user,
                        properties.auth_scheme,
                        properties.password.toCharArray()
                    )
                )
                .applyConnectionString(ConnectionString (properties.url))
                .build()
        )
    }

    @Bean
    open fun mongoTemplate(): MongoTemplate {
        return MongoTemplate (mongoClient(), properties.name)
    }
}
