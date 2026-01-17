package clockify2jira.jira

import clockify2jira.jira.api.IssueWorklogsApi
import mu.KLogging
import okhttp3.Credentials
import okhttp3.OkHttpClient
import org.apache.http.HttpHeaders
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@ConfigurationProperties(prefix = "jira.api")
data class JiraApiConfigProperties(
    val email: String,
    val token: String,
    val baseUrl: String
) {
    init {
        require(email.isNotBlank()) { "Jira email must not be blank" }
        require(token.isNotBlank()) { "Jira token must not be blank" }
        require(baseUrl.isNotBlank()) { "Jira url must not be blank" }
    }
}

@ConfigurationProperties(prefix = "jira")
data class JiraConfigProperties(
    val accountId: String
)

@Configuration
class JiraConfig {

    companion object : KLogging()

    @Bean
    fun jiraApi(
        config: JiraApiConfigProperties,
        @Qualifier("jiraHttpClient") httpClient: OkHttpClient
    ): IssueWorklogsApi {
        return IssueWorklogsApi(basePath = config.baseUrl, client = httpClient)
    }

    @Bean("jiraHttpClient")
    fun jiraHttpClient(config: JiraApiConfigProperties): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor { chain ->
                val credentials = Credentials.basic(config.email, config.token)
                val request = chain.request().newBuilder()
                    .header(HttpHeaders.AUTHORIZATION, credentials)
                    .build()
                chain.proceed(request)
            }
            .build()
    }
}
