package clockify2jira.jira

import com.atlassian.jira.rest.client.api.JiraRestClient
import com.atlassian.jira.rest.client.api.domain.input.WorklogInput
import mu.KLogging
import org.joda.time.DateTime
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.net.URI
import java.time.OffsetDateTime
import kotlin.time.Duration

interface JiraService {

    fun getWorklogUriForIssueKey(issueKey: String): URI
    fun addWorklog(uri: URI, start: OffsetDateTime, worklog: Duration)
}

@Service
class JiraServiceImpl : JiraService {

    @Autowired
    private lateinit var jiraRestClient: JiraRestClient

    override fun getWorklogUriForIssueKey(issueKey: String): URI {
        return jiraRestClient.issueClient.getIssue(issueKey).get().worklogUri
    }

    override fun addWorklog(uri: URI, start: OffsetDateTime, worklog: Duration) {
        jiraRestClient.issueClient.addWorklog(uri, worklog.toWorklogEntry(uri, start)).get()
    }

    private fun Duration.toWorklogEntry(uri: URI, start: OffsetDateTime): WorklogInput {
        return WorklogInput.create(
            URI("$uri/worklog"),
            "",
            DateTime.parse(start.toString()),
            this.inWholeMinutes.toInt(),
        )
    }

    companion object : KLogging()
}
