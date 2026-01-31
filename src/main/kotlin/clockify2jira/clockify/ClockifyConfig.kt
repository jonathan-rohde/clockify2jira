package clockify2jira.clockify

import clockify2jira.clockify.api.ClockifyApi
import okhttp3.OkHttpClient
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@ConfigurationProperties(prefix = "clockify.api")
data class ClockifyApiConfigProperties(
    val token: String,
    val baseUrl: String
) {
    init {
        require(token.isNotBlank()) { "Jira token must not be blank" }
        require(baseUrl.isNotBlank()) { "Jira url must not be blank" }
    }
}

@ConfigurationProperties(prefix = "clockify")
data class ClockifyConfigProperties(
    val workspaceId: String,
    val userId: String,
    val pageSize: Int = 50,
    val projectId: String
) {
    init {
        require(userId.isNotBlank()) { "User id must not be blank" }
        require(projectId.isNotBlank()) { "ProjectId must not be blank" }
    }
}

@Configuration
class ClockifyConfig {

    @Bean
    fun clockifyApi(
        config: ClockifyApiConfigProperties,
        @Qualifier("clockifyHttpClient") httpClient: OkHttpClient
    ): ClockifyApi {
        return ClockifyApi(basePath = config.baseUrl, client = httpClient)
    }

    @Bean
    @Qualifier("clockifyHttpClient")
    fun clockifyHttpClient(config: ClockifyApiConfigProperties): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .header("X-Api-Key", config.token)
                    .build()
                chain.proceed(request)
            }
            .build()
    }
}
