package clockify2jira

import clockify2jira.clockify.ClockifyService
import clockify2jira.clockify.groupBy
import clockify2jira.jira.JiraService
import mu.NamedKLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.system.exitProcess
import kotlin.time.DurationUnit
import kotlin.time.toDuration

@SpringBootApplication
@ConfigurationPropertiesScan
class MainApplication : ApplicationRunner {

    @Autowired
    lateinit var clockifyService: ClockifyService

    @Autowired
    lateinit var jiraService: JiraService

    companion object : NamedKLogging("Clockify2Jira")

    override fun run(args: ApplicationArguments) {
        val arguments = args.parse()
        logger.info { "Starting migration from ${arguments.start} to ${arguments.end}, dryRun: ${arguments.dryRun}" }
        val lastEntries = clockifyService.getLastEntries(
            arguments.start,
            arguments.end
        )
        val dryRun = arguments.dryRun
        logger.info("Fetched ${lastEntries.size} entries from Clockify")

        if (lastEntries.any { it.jiraKey == null }) {
            val missingKeys = lastEntries.filter { it.jiraKey == null }.joinToString("\n") { it.toString() }
            logger.error { "Some entries do not have a Jira key: \n$missingKeys" }
            exitProcess(1)
        }

        val data = lastEntries.groupBy { it.jiraKey!! }
            .mapValues { entry -> entry.groupBy(arguments.groupBy) }

        data.toSortedMap(compareBy { it })
            .forEach { (key, days) ->
                days.toSortedMap(compareBy { it }).forEach { (date, minutes) ->
                    logger.info { "Adding worklog to $key for $date: $minutes minutes".withDryRun(dryRun) }
                    if (!dryRun) {
                        jiraService.addWorklog(key, date.toOffsetDateTime(), minutes.toDuration(DurationUnit.MINUTES))
                    }
                }
            }
    }

    private fun String.withDryRun(dryRun: Boolean) =
        if (dryRun) {
            "Dry run: $this"
        } else {
            this
        }

    private fun LocalDateTime.toOffsetDateTime(): OffsetDateTime =
        ZonedDateTime.of(this, ZoneId.of("Europe/Berlin")).toOffsetDateTime()

    private fun ApplicationArguments.parse(): Arguments {
        val startDate = getOptionValues("start")?.firstOrNull()
        val endDate = getOptionValues("end")?.firstOrNull()
        val dryRun = containsOption("dry-run")
        val groupBy = getOptionValues("groupBy")?.firstOrNull()

        return Arguments(
            start = startDate?.let { LocalDate.parse(it) } ?: LocalDate.now().minusDays(7),
            end = endDate?.let { LocalDate.parse(it) } ?: LocalDate.now(),
            dryRun = dryRun,
            groupBy = groupBy.parseGroupBy()
        )
    }
}

fun main(args: Array<String>) {
    runApplication<MainApplication>(*args)
}

enum class GroupBy {
    ENTRY,
    DAY,
    WEEK,
    MONTH
}

private fun String?.parseGroupBy(): GroupBy {
    if (this == null) return GroupBy.DAY

    return GroupBy.entries.firstOrNull { it.name.equals(this, ignoreCase = true) }
        ?: GroupBy.DAY
}

data class Arguments(
    val start: LocalDate,
    val end: LocalDate,
    val dryRun: Boolean,
    val groupBy: GroupBy
)
