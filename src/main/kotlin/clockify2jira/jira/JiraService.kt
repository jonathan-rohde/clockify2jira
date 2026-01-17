package clockify2jira.jira

import clockify2jira.jira.api.IssueWorklogsApi
import clockify2jira.jira.model.Worklog
import mu.KLogging
import org.springframework.stereotype.Service
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import kotlin.time.Duration

interface JiraService {
    fun addWorklog(key: String, start: OffsetDateTime, worklog: Duration)
    fun hasWorklogs(key: String, start: OffsetDateTime, duration: Duration): Boolean
}

@Service
class JiraServiceImpl(
    private val issueWorklogsApi: IssueWorklogsApi,
    private val jiraConfig: JiraConfigProperties
) : JiraService {
    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ")

    override fun addWorklog(key: String, start: OffsetDateTime, worklog: Duration) {
        issueWorklogsApi.addWorklog(key, worklog.toWorklog(key, start))
    }

    override fun hasWorklogs(key: String, start: OffsetDateTime, duration: Duration): Boolean {
        val worklogs = issueWorklogsApi.getIssueWorklog(
            issueIdOrKey = key,
            startedAfter = (start.toEpochSecond() * 1000) - 1,
            startedBefore = (start.toEpochSecond() * 1000) + 1
        ).worklogs ?: emptyList()

        return worklogs.filter { it.author?.accountId == jiraConfig.accountId }
            .filter { it.timeSpentSeconds == duration.inWholeSeconds }
            .any { it.started == formatter.format(start) }
    }

    private fun Duration.toWorklog(key: String, start: OffsetDateTime): Worklog {
        return Worklog(
            issueId = key,
            started = formatter.format(start),
            timeSpentSeconds = inWholeSeconds
        )
    }

    companion object : KLogging()
}
