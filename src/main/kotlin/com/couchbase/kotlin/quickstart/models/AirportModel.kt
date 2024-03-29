package com.couchbase.kotlin.quickstart.models

import com.couchbase.kotlin.quickstart.repositories.AirportRepository
import com.couchbase.kotlin.quickstart.services.AirportService
import com.fasterxml.jackson.annotation.JsonIgnoreProperties

import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import java.lang.IllegalArgumentException

// This class is used to represent
// client requests to create a new
// Airport record
@JsonIgnoreProperties(ignoreUnknown = true)
open class AirportModel(
    var airportname: String? = null,
    var city: String? = null,
    var country: String? = null,
    var faa: String? = null,
    var geo: Geo? = null,
    var icao: String? = null,
    var tz: String? = null
) {
    fun validate() {
        if (airportname.isNullOrBlank() || city.isNullOrBlank() || country.isNullOrBlank() || faa.isNullOrBlank()) {
            throw IllegalArgumentException()
        }
    }
}

open class Geo (
    var alt: Double = 0.0,
    var lat: Double = 0.0,
    var lon: Double = 0.0
)

// This class is used to represent
// Airport records
@JsonIgnoreProperties(ignoreUnknown = true)
class Airport : AirportModel()

val airportModule = module {
    singleOf(::AirportRepository)
    singleOf(::AirportService)
}