package com.example.processor.config

import io.netty.channel.ChannelOption
import io.netty.handler.timeout.ReadTimeoutHandler
import io.netty.handler.timeout.WriteTimeoutHandler
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient
import reactor.netty.resources.ConnectionProvider
import java.time.Duration
import java.util.concurrent.TimeUnit

@Configuration
@EnableConfigurationProperties(HttpClientProperties::class)
class WebClientConfig {

    @Bean
    fun webClient(httpClientProperties: HttpClientProperties): WebClient {
        val connectionProvider = ConnectionProvider.builder("pooled-http-client")
            .maxConnections(httpClientProperties.maxConnections)
            .pendingAcquireMaxCount(httpClientProperties.pendingAcquireMaxCount)
            .pendingAcquireTimeout(httpClientProperties.pendingAcquireTimeout)
            .maxIdleTime(httpClientProperties.maxIdleTime)
            .maxLifeTime(httpClientProperties.maxLifeTime)
            .lifo()
            .build()

        val httpClient = HttpClient.create(connectionProvider)
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, httpClientProperties.connectionTimeout.toMillis().toInt())
            .option(ChannelOption.TCP_NODELAY, httpClientProperties.tcpNoDelay)
            .option(ChannelOption.SO_KEEPALIVE, httpClientProperties.soKeepAlive)
            .responseTimeout(httpClientProperties.responseTimeout)
            .doOnConnected { connection ->
                connection.addHandlerLast(
                    ReadTimeoutHandler(httpClientProperties.readTimeout.toMillis(), TimeUnit.MILLISECONDS)
                )
                connection.addHandlerLast(
                    WriteTimeoutHandler(httpClientProperties.writeTimeout.toMillis(), TimeUnit.MILLISECONDS)
                )
            }

        return WebClient.builder()
            .clientConnector(ReactorClientHttpConnector(httpClient))
            .build()
    }
}

@ConfigurationProperties(prefix = "http.client")
data class HttpClientProperties(
    val maxConnections: Int = 500,
    val pendingAcquireMaxCount: Int = 1000,
    val pendingAcquireTimeout: Duration = Duration.ofSeconds(5),
    val maxIdleTime: Duration = Duration.ofSeconds(30),
    val maxLifeTime: Duration = Duration.ofMinutes(2),
    val connectionTimeout: Duration = Duration.ofSeconds(2),
    val responseTimeout: Duration = Duration.ofSeconds(5),
    val readTimeout: Duration = Duration.ofSeconds(5),
    val writeTimeout: Duration = Duration.ofSeconds(5),
    val tcpNoDelay: Boolean = true,
    val soKeepAlive: Boolean = true
)
