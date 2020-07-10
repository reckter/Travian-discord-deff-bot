package de.reckter.travian.deff

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document

@Document
data class DeffCall(
    @Id
    val id: String? = null,

    @Indexed
    val name: String,

    val discordMessageId: String? = null,

    val discordChannelId: String,

    val content: String,

    val troupRules: Map<String, String>
)
