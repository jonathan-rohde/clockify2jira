package clockify2jira.clockify

import clockify2jira.GroupBy
import java.time.*

private val groupKey = mapOf(
    GroupBy.ENTRY to { it: OffsetDateTime -> it.toLocalDateTime() },
    GroupBy.DAY to { it: OffsetDateTime -> it.toLocalDateTime().withFixedTime() },
    GroupBy.WEEK to { it: OffsetDateTime -> it.getFirstDayOfWeek() },
    GroupBy.MONTH to { it: OffsetDateTime -> it.withDayOfMonth(1).toLocalDateTime().withFixedTime() }
)

fun Map.Entry<String, List<ClockifyEntry>>.groupBy(groupBy: GroupBy): Map<LocalDateTime, Long> {
    return value.groupBy { groupKey[groupBy]!!(OffsetDateTime.parse(it.start)) }.mapValues { (_, entries) -> entries.sumMinutes() }
}

private fun List<ClockifyEntry>.sumMinutes(): Long {
    return fold(0L) { acc, entry -> acc + (entry.duration?.inWholeMinutes ?: 0L) }
}

val weekCache = mutableMapOf<Triple<Int, Int, Int>, LocalDateTime>()
private fun OffsetDateTime.getFirstDayOfWeek(): LocalDateTime {
    return weekCache.getOrPut(Triple(year, monthValue, dayOfMonth)) {
        var start = this
        while (start.dayOfWeek != DayOfWeek.MONDAY) {
            start = start.minusDays(1)
        }
        start.toLocalDateTime().withFixedTime()
    }
}

private fun LocalDateTime.withFixedTime(): LocalDateTime {
    return withHour(8).withMinute(0).withSecond(0).withNano(0)
}