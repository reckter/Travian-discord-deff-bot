package de.reckter.travian.deff

import org.springframework.data.mongodb.repository.MongoRepository

interface DeffCallRepository:  MongoRepository<DeffCall, String> {

    fun findOneByName(name: String): DeffCall?
}
