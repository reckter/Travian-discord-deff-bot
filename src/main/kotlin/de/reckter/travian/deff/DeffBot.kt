package de.reckter.travian.deff

import com.gitlab.kordlib.common.entity.Snowflake
import com.gitlab.kordlib.core.Kord
import com.gitlab.kordlib.core.any
import com.gitlab.kordlib.core.entity.Message
import com.gitlab.kordlib.core.entity.ReactionEmoji
import com.gitlab.kordlib.core.event.message.MessageCreateEvent
import com.gitlab.kordlib.core.on
import de.reckter.travian.discord.DiscordConfiguration
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Service
import javax.annotation.PostConstruct

@Service
class DeffBot(
    private val repository: DeffCallRepository,
    private val kord: Kord,
    private val discordConfiguration: DiscordConfiguration
) {

    @PostConstruct
    fun onInit() {
        println("onInit")
        kord.on<MessageCreateEvent> {
            println("handling message ${message.author?.username}: ${message.content}")
            if (message.content.startsWith("!deff")) startDeffCall(message)
            if (message.content.startsWith("!return")) returnCall(message)

            if (message.content.startsWith("!delete")) deleteCall(message)
        }

        GlobalScope.async {
            kord.login()
        }

        runBlocking {

            val string = kord.getGuild(Snowflake(723811250521374731))
                ?.channels
                ?.toList()
                ?.joinToString("\n") { "${it.name}: ${it.id}" }
            println(string)
        }

    }


    fun deleteCall(message: Message) {
        runBlocking {
            message.delete()
            val roles  = message.author?.asMemberOrNull(message.getGuild().id)
                ?.roles?.toList() ?: error("lol")

            if (roles.toList().none { it.id == Snowflake(discordConfiguration.adminRole) }) {
                println("not deleting call")
                return@runBlocking
            }

            val name = message.content.removePrefix("!delete ")
            val call = repository.findOneByName(name)

            call?.let { repository.delete(it) }

            if(call?.discordMessageId != null)
                kord.rest.channel.deleteMessage(call.discordChannelId, call.discordMessageId)
        }
    }


    fun startDeffCall(message: Message) {
        println("start deff")
        val string = message.content.removePrefix("!deff ")
        val (name, content) = string.split(" ".toRegex(), 2)

        val existing = repository.findOneByName(name)
        if(existing != null) {
            val updated = repository.save(existing.copy(content = content))
            updateDeffPost(updated)
            runBlocking { message.delete() }
            return
        }

        val call = repository.save(DeffCall(
            name = name,
            discordChannelId = message.channelId.value,
            content = content,
            troupRules = emptyMap()
        ))

        println(call)
        updateDeffPost(call)

        runBlocking { message.delete() }
    }

    fun returnCall(message: Message) {
        val string = message.content.removePrefix("!return ")
        val splittedName =  string.split("\n".toRegex(), 2)

        val (name, player, content) = if(splittedName.size == 2) {
            val splitted = splittedName.first().split(" ".toRegex(), 2)

            if(splitted.size != 2) {
                runBlocking {
                    val ownMessage = message.channel.createMessage("Please provide a player name! <@${message.author?.id?.longValue}>")
                    message.delete()

                    delay(5000)
                    ownMessage.delete()
                }
                return
            }
            splitted + splittedName.last()
        } else {
            val splitted = splittedName.first().split(" ".toRegex(), 3)
            if(splitted.size != 3) {
                runBlocking {
                    val ownMessage = message.channel.createMessage("Please Follow the instructions above<@${message.author?.id?.longValue}>")
                    message.delete()

                    delay(5000)
                    ownMessage.delete()
                }
                return
            }

            splitted
        }


        val call = repository.findOneByName(name) ?: return

        val toSafe = call.copy(
            troupRules = call.troupRules + (player to content.trim())
        )

        val saved = repository.save(toSafe)

        updateDeffPost(saved)
        runBlocking { message.delete() }
    }


    fun updateDeffPost(call: DeffCall) {
        val text = """Deff Aktion ${call.name}
            |${call.content}
            | 
            |Truppen zurückschicken: 
            |${call.troupRules.entries.joinToString("\n\n") { (player, rule) -> "```$player``` $rule" }}
            |
            |um zu ändern: ```!return ${call.name} <spieler-name>
            |
            |<Regel>```
        """.trimMargin()


        if(call.discordMessageId != null) {
            runBlocking {
                kord.rest.channel.editMessage(
                    call.discordChannelId,
                    call.discordMessageId
                ) {
                    content = text
                }
            }
            return
        }

        runBlocking {
            println("making deff post")
            println(text)
            val created = kord.rest.channel.createMessage(call.discordChannelId) {
                content = text
            }

            repository.save(call.copy(discordMessageId = created.id))
        }
    }

}
