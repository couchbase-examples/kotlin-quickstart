package com.couchbase.kotlin.quickstart

import com.couchbase.client.kotlin.Bucket
import com.couchbase.client.kotlin.Cluster
import com.couchbase.client.kotlin.Scope
import io.ktor.server.config.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime

// Couchbase beans
@ExperimentalTime
val couchbaseModule = module {
  singleOf(::CouchbaseConfiguration)
  singleOf(::createCluster)
  singleOf(::createBucket)
  singleOf(::createScope)
}

class CouchbaseConfiguration(cfg: ApplicationConfig) {
  val connectionString: String = System.getenv("DB_CONN_STR") ?: cfg.propertyOrNull("couchbase.connectionString")?.getString() ?: "localhost"
  val username: String = System.getenv("DB_USERNAME") ?: cfg.propertyOrNull("couchbase.username")?.getString() ?: "Administrator"
  val password: String = System.getenv("DB_PASSWORD") ?: cfg.propertyOrNull("couchbase.password")?.getString() ?: "password"
  val bucket: String = cfg.propertyOrNull("couchbase.bucket")?.getString() ?: "travel-sample"
  val scope: String = cfg.propertyOrNull("couchbase.scope")?.getString() ?: "inventory"
}

// Creates a cluster bean
fun createCluster(configuration: CouchbaseConfiguration): Cluster {
  if (configuration.connectionString.isEmpty() || configuration.username.isEmpty() || configuration.password.isEmpty()) {
    throw IllegalArgumentException("Connection string, username, or password is not provided in the configuration.")
  }
  return Cluster.connect(
    connectionString = configuration.connectionString,
    username = configuration.username,
    password = configuration.password,
  )
}

// Creates a bucket bean
@ExperimentalTime
fun createBucket(cluster: Cluster, configuration: CouchbaseConfiguration): Bucket {
  if (configuration.bucket.isEmpty()) {
    throw IllegalArgumentException("Bucket name is not provided in the configuration.")
  }
  val result : Bucket?
  runBlocking {
    result = cluster.bucket(configuration.bucket).waitUntilReady(10.seconds)
  }
  return result ?: throw IllegalStateException("Ensure that you have the travel-sample bucket loaded in the cluster.")
}

// Creates a bucket scope bean
fun createScope(bucket: Bucket, configuration: CouchbaseConfiguration): Scope {
  if (configuration.scope.isEmpty()) {
    throw IllegalArgumentException("Scope name is not provided in the configuration.")
  }

  val scope = bucket.scope(configuration.scope)

  try {
    scope.collection(configuration.scope)
  } catch (e: Exception) {
    throw IllegalStateException("Inventory scope does not exist in the bucket. Ensure that you have the inventory scope in your travel-sample bucket.")
  }

  return scope
}



