package clockify2jira.clockify

import mu.KLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import clockify2jira.clockify.api.ClockifyApi
import clockify2jira.clockify.model.TimeEntry
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneId


interface ClockifyService {

    fun getLastEntries(start: LocalDate, end: LocalDate): List<ClockifyEntry>

}

@Service
class ClockifyServiceImpl : ClockifyService {

    @Autowired
    private lateinit var clockifyApi: ClockifyApi

    @Autowired
    private lateinit var config: ClockifyConfigProperties


    override fun getLastEntries(start: LocalDate, end: LocalDate): List<ClockifyEntry> {
        require(config.pageSize in 1..5000) { "Number of entries must be between 1 and 5000" }

        var pageNumber = 1
        return generateSequence { fetchPage(start, end, pageNumber++) }
            .flatMap {
                it.map { entry -> entry.toClockifyEntry() }
            }.toList()
    }

    private fun fetchPage(start: LocalDate, end: LocalDate, pageNumber: Int): List<TimeEntry>? {
        return clockifyApi.getTimeEntries(
            workspaceId = config.workspaceId,
            userId = config.userId,
            start = start.atSyncStart(),
            end = end.atSyncEnd(),
            pageSize = config.pageSize,
            page = pageNumber,
            project = config.projectId,
        ).ifEmpty { null }
    }

    companion object : KLogging()

    private fun LocalDate.atSyncStart(): OffsetDateTime =
        atStartOfDay().atZone(ZoneId.of("Europe/Berlin")).toOffsetDateTime()

    private fun LocalDate.atSyncEnd(): OffsetDateTime =
        this.plusDays(1).atStartOfDay().atZone(ZoneId.of("Europe/Berlin")).toOffsetDateTime().minusSeconds(1)
}
