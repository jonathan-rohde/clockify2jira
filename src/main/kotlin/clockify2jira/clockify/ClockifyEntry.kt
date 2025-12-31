package clockify2jira.clockify

import clockify2jira.clockify.model.TimeEntry
import kotlin.time.Duration

data class ClockifyEntry(
    val id: String,
    val description: String,
    val jiraKey: String? = null,
    val start: String,
    val end: String,
    val duration: Duration?
) {
    // Additional methods or properties can be added here if needed
}

fun TimeEntry.toClockifyEntry(): ClockifyEntry {
    return ClockifyEntry(
        id = this.id,
        description = this.description,
        jiraKey = this.description.toJiraKey(),
        start = this.timeInterval.start?.toString() ?: "",
        end = this.timeInterval.end?.toString() ?: "",
        duration = this.timeInterval.duration?.toDuration()
    )
}

private fun String.toDuration(): Duration = Duration.parse(this)
private fun String.toJiraKey(): String? {
    val regex = Regex("^([A-Z0-9]+-[0-9]+)[^A-Z0-9].*$")
    val matcher = regex.find(this)
    return if (matcher != null) {
        matcher.groupValues[1]
    } else {
        null
    }
}
