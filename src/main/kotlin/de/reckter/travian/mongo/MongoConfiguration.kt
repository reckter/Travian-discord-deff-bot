package de.reckter.travian.mongo

import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties("mongo")
data class MongoConfig(
    var url: String = ""
)

@Configuration
class MongoConfiguration {

    @Bean
    open fun mongoClient(configuration: MongoConfig): MongoClient {
        return MongoClients.create(configuration.url)
    }

    @Bean
    fun mongoTemplate(mongoClient: MongoClient, configuration: MongoConfig): MongoTemplate {
        println("mongo template")
        return MongoTemplate(mongoClient, configuration.url.substringAfterLast("/") ?: "travian")
    }
}
