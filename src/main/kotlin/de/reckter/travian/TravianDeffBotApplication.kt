package de.reckter.travian

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories

@SpringBootApplication
@EnableMongoRepositories(basePackages = [ "de.reckter"])
class TravianDeffBotApplication

fun main(args: Array<String>) {
	runApplication<TravianDeffBotApplication>(*args)
	while(true) {}
}
