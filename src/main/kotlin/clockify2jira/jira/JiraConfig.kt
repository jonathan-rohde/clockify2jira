package clockify2jira.jira

import com.atlassian.jira.rest.client.api.JiraRestClient
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.net.URI

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

    @Bean
    fun jiraClient(config: JiraApiConfigProperties): JiraRestClient {
        return AsynchronousJiraRestClientFactory().createWithBasicHttpAuthentication(
            URI.create(config.baseUrl),
            config.email,
            config.token
        )
    }
}
