package com.couchbase.kotlin.quickstart.repositories

import com.couchbase.client.kotlin.Scope
import com.couchbase.client.kotlin.query.QueryParameters
import com.couchbase.client.kotlin.query.execute
import com.couchbase.kotlin.quickstart.models.*
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.runBlocking

class AirlineRepository(scope: Scope) {

    private val collection = scope.collection("airline")
    @OptIn(DelicateCoroutinesApi::class)
    private val databaseContext = newSingleThreadContext("CouchbaseThread")

    fun update(airline: Airline, id: String): Airline {
        runBlocking(databaseContext) {
            collection.replace(id, airline)
        }
        return airline
    }

    fun delete(id: String) {
        runBlocking(databaseContext) {
            collection.remove(id)
        }
    }

    fun create(data: AirlineModel, id: String): Airline {
        val airline = Airline().apply {
            callsign = data.callsign
            country = data.country
            iata = data.iata
            icao = data.icao
            name = data.name
        }

        runBlocking(databaseContext) {
            collection.insert(id, airline)
        }
        return airline
    }

    fun getById(id: String): Airline {
        var result: Airline
        runBlocking(databaseContext) {
            result = collection.get(id).contentAs()
        }
        return result
    }

    fun list(country: String? = null, limit: Int = 10, offset: Int = 0): List<Airline> {
        val filterByCountry = !country.isNullOrEmpty()
        val query = """
            SELECT airline.callsign,
                   airline.country,
                   airline.iata,
                   airline.icao,
                   airline.name
            FROM airline AS airline
            ${if (filterByCountry) "WHERE lower(airline.country) = lower(\$country)" else ""}
            ORDER BY airline.name
            LIMIT ${'$'}limit
            OFFSET ${'$'}offset
        """
        val parameters = QueryParameters.named {
            if (filterByCountry) param("country", country)
            param("limit", limit)
            param("offset", offset)
        }

        return runBlocking(databaseContext) {
            val result = collection.scope.query(query, parameters = parameters, readonly = true).execute()
            result.rows.map {
                it.contentAs<Airline>()
            }.toList()
        }
    }

    fun toAirport(airport: String, limit: Int = 10, offset: Int = 0): List<Airline> {
        val query =
            """
                SELECT air.callsign,
                       air.country,
                       air.iata,
                       air.icao,
                       air.name
                       FROM (
                        SELECT DISTINCT META(airline).id AS airlineId
                        FROM route AS route
                        JOIN airline AS airline
                        ON route.airlineid = META(airline).id
                        WHERE lower(route.destinationairport) = lower(${'$'}airport)
                        ) AS SUBQUERY
                        JOIN airline AS air
                        ON META(air).id = SUBQUERY.airlineId
                        LIMIT ${'$'}limit
                        OFFSET ${'$'}offset
            """
        val parameters = QueryParameters.named("airport" to airport, "limit" to limit, "offset" to offset)

        return runBlocking(databaseContext) {
            val result = collection.scope.query(query, parameters = parameters, readonly = true).execute()
            result.rows.map {
                it.contentAs<Airline>()
            }.toList()
        }
    }

}