package clockify2jira.clockify

import clockify2jira.GroupBy
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.OffsetDateTime

private val groupKey = mapOf(
    GroupBy.ENTRY to { dateTime: OffsetDateTime -> dateTime.toLocalDateTime() },
    GroupBy.DAY to { dateTime: OffsetDateTime -> dateTime.toLocalDateTime().withFixedTime() },
    GroupBy.WEEK to { dateTime: OffsetDateTime -> dateTime.getFirstDayOfWeek() },
    GroupBy.MONTH to { dateTime: OffsetDateTime -> dateTime.withDayOfMonth(1).toLocalDateTime().withFixedTime() }
)
private const val FIXED_HOUR = 8
private const val FIXED_MINUTES = 0
private const val FIXED_SECOND = 0
private const val FIXED_NANO = 0

fun Map.Entry<String, List<ClockifyEntry>>.groupBy(groupBy: GroupBy): Map<LocalDateTime, Long> {
    return value.groupBy { groupKey[groupBy]!!(OffsetDateTime.parse(it.start)) }
        .mapValues { (_, entries) -> entries.sumMinutes() }
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
    return withHour(FIXED_HOUR).withMinute(FIXED_MINUTES).withSecond(FIXED_SECOND).withNano(FIXED_NANO)
}
