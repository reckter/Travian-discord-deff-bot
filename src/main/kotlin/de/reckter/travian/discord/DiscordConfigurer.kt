package de.reckter.travian.discord

import com.gitlab.kordlib.core.Kord
import kotlinx.coroutines.runBlocking
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties("discord")
data class DiscordConfiguration(
    var key: String = "",
    var deffChannel: String = "",
    var adminRole: Long = -1
)


@Configuration
class DiscordConfigurer () {

    @Bean
    fun discordClient(config: DiscordConfiguration): Kord {
        return runBlocking { Kord(config.key) }
    }
}
