package com.hehe.habit_tracker.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/** Unit test thuần cho map frequency -> RRULE. */
class RRuleBuilderTest {

    private final RRuleBuilder builder = new RRuleBuilder();

    @Test
    void daily_toDailyRule() {
        assertEquals("RRULE:FREQ=DAILY", builder.toRRule("\"daily\""));
    }

    @Test
    void weeklyN_flexible_toDailyRule() {
        assertEquals("RRULE:FREQ=DAILY", builder.toRRule("\"weekly_3\""));
        assertEquals("RRULE:FREQ=DAILY", builder.toRRule("\"weekly_5\""));
    }

    @Test
    void specificDays_toWeeklyByDay_sortedFromSunday() {
        // days JS: 1=T2,3=T4,5=T6 -> MO,WE,FR
        assertEquals("RRULE:FREQ=WEEKLY;BYDAY=MO,WE,FR",
                builder.toRRule("{\"type\":\"days\",\"days\":[5,1,3]}"));
    }

    @Test
    void specificDays_weekendIncludesSunday() {
        // 0=CN, 6=T7 -> SU,SA (sắp theo index 0..6)
        assertEquals("RRULE:FREQ=WEEKLY;BYDAY=SU,SA",
                builder.toRRule("{\"type\":\"days\",\"days\":[0,6]}"));
    }

    @Test
    void nullOrBlankOrGarbage_fallbackDaily() {
        assertEquals("RRULE:FREQ=DAILY", builder.toRRule(null));
        assertEquals("RRULE:FREQ=DAILY", builder.toRRule(""));
        assertEquals("RRULE:FREQ=DAILY", builder.toRRule("something-weird"));
    }

    @Test
    void emptyDaysArray_fallbackDaily() {
        assertEquals("RRULE:FREQ=DAILY", builder.toRRule("{\"type\":\"days\",\"days\":[]}"));
    }
}
