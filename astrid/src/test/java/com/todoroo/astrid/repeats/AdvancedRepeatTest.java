/**
 * Copyright (c) 2012 Todoroo Inc
 *
 * See the file "LICENSE" for the full license governing this code.
 */
package com.todoroo.astrid.repeats;

import com.google.ical.values.Frequency;
import com.google.ical.values.RRule;
import com.google.ical.values.Weekday;
import com.google.ical.values.WeekdayNum;
import com.todoroo.andlib.test.TodorooRobolectricTestCase;
import com.todoroo.andlib.utility.DateUtilities;
import com.todoroo.astrid.data.Task;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.tasks.date.DateTimeUtils.newDate;

@RunWith(RobolectricTestRunner.class)
public class AdvancedRepeatTest extends TodorooRobolectricTestCase {

    private static final int PREV_PREV = -2;
    private static final int PREV = -1;
    private static final int THIS = 1;
    private static final int NEXT = 2;
    private static final int NEXT_NEXT = 3;

    private Task task;
    private long nextDueDate;
    private RRule rrule;

    @Before
    public void before() {
        super.before();
        task = new Task();
        task.setCompletionDate(DateUtilities.now());
        rrule = new RRule();
    }

    // --- date with time tests

    @Test
    public void testDueDateSpecificTime() throws ParseException {
        buildRRule(1, Frequency.DAILY);

        // test specific day & time
        long dayWithTime = Task.createDueDate(Task.URGENCY_SPECIFIC_DAY_TIME, newDate(2010, 8, 1, 10, 4, 0).getTime());
        task.setDueDate(dayWithTime);

        long nextDayWithTime = dayWithTime + DateUtilities.ONE_DAY;
        nextDueDate = RepeatTaskCompleteListener.computeNextDueDate(task, rrule.toIcal(), false);
        assertDateTimeEquals(nextDayWithTime, nextDueDate);
    }

    @Test
    public void testCompletionDateSpecificTime() throws ParseException {
        buildRRule(1, Frequency.DAILY);

        // test specific day & time
        long dayWithTime = Task.createDueDate(Task.URGENCY_SPECIFIC_DAY_TIME, newDate(2010, 8, 1, 10, 4, 0).getTime());
        task.setDueDate(dayWithTime);

        Date todayWithTime = newDate();
        todayWithTime.setHours(10);
        todayWithTime.setMinutes(4);
        todayWithTime.setSeconds(1);
        long nextDayWithTimeLong = todayWithTime.getTime();
        nextDayWithTimeLong += DateUtilities.ONE_DAY;
        nextDayWithTimeLong = nextDayWithTimeLong / 1000L * 1000;

        nextDueDate = RepeatTaskCompleteListener.computeNextDueDate(task, rrule.toIcal(), true);
        assertDateTimeEquals(nextDayWithTimeLong, nextDueDate);
    }

    // --- due date tests

    /** test multiple days per week - DUE DATE */
    @Test
    public void testDueDateInPastSingleWeekMultiDay() throws Exception {
        buildRRule(1, Frequency.WEEKLY, Weekday.MO, Weekday.WE, Weekday.FR);

        setTaskDueDate(THIS, Calendar.SUNDAY);
        computeNextDueDate(false);
        assertDueDate(nextDueDate, THIS, Calendar.MONDAY);

        setTaskDueDate(THIS, Calendar.MONDAY);
        computeNextDueDate(false);
        assertDueDate(nextDueDate, THIS, Calendar.WEDNESDAY);

        setTaskDueDate(THIS, Calendar.FRIDAY);
        computeNextDueDate(false);
        assertDueDate(nextDueDate, THIS, Calendar.MONDAY);
    }

    /** test single day repeats - DUE DATE */
    @Test
    public void testDueDateSingleDay() throws Exception {
        buildRRule(1, Frequency.WEEKLY, Weekday.MO);

        setTaskDueDate(PREV_PREV, Calendar.MONDAY);
        computeNextDueDate(false);
        assertDueDate(nextDueDate, NEXT, Calendar.MONDAY);

        setTaskDueDate(PREV_PREV, Calendar.FRIDAY);
        computeNextDueDate(false);
        assertDueDate(nextDueDate, THIS, Calendar.MONDAY);

        setTaskDueDate(PREV, Calendar.MONDAY);
        computeNextDueDate(false);
        assertDueDate(nextDueDate, NEXT, Calendar.MONDAY);

        setTaskDueDate(PREV, Calendar.FRIDAY);
        computeNextDueDate(false);
        assertDueDate(nextDueDate, THIS, Calendar.MONDAY);

        setTaskDueDate(THIS, Calendar.SUNDAY);
        computeNextDueDate(false);
        assertDueDate(nextDueDate, THIS, Calendar.MONDAY);

        setTaskDueDate(THIS, Calendar.MONDAY);
        computeNextDueDate(false);
        assertDueDate(nextDueDate, NEXT, Calendar.MONDAY);
    }

    /** test multiple days per week - DUE DATE */
    @Test
    public void testDueDateSingleWeekMultiDay() throws Exception {

        buildRRule(1, Frequency.WEEKLY, Weekday.MO, Weekday.WE, Weekday.FR);

        setTaskDueDate(THIS, Calendar.SUNDAY);
        computeNextDueDate(false);
        assertDueDate(nextDueDate, THIS, Calendar.MONDAY);

        setTaskDueDate(THIS, Calendar.MONDAY);
        computeNextDueDate(false);
        assertDueDate(nextDueDate, THIS, Calendar.WEDNESDAY);

        setTaskDueDate(THIS, Calendar.FRIDAY);
        computeNextDueDate(false);
        assertDueDate(nextDueDate, THIS, Calendar.MONDAY);
    }

    /** test multiple days per week, multiple intervals - DUE DATE */
    @Test
    public void testDueDateMultiWeekMultiDay() throws Exception {
        buildRRule(2, Frequency.WEEKLY, Weekday.MO, Weekday.WE, Weekday.FR);

        setTaskDueDate(THIS, Calendar.SUNDAY);
        computeNextDueDate(false);
        assertDueDate(nextDueDate, NEXT, Calendar.MONDAY);

        setTaskDueDate(THIS, Calendar.MONDAY);
        computeNextDueDate(false);
        assertDueDate(nextDueDate, THIS, Calendar.WEDNESDAY);

        setTaskDueDate(THIS, Calendar.FRIDAY);
        computeNextDueDate(false);
        assertDueDate(nextDueDate, NEXT, Calendar.MONDAY);
    }

    // --- completion tests

    /** test multiple days per week - COMPLETE DATE */
    @Test
    public void testCompleteDateSingleWeek() throws Exception {
        for(Weekday wday : Weekday.values()) {
            buildRRule(1, Frequency.WEEKLY, wday);
            computeNextDueDate(true);
            long expected = getDate(DateUtilities.now() + DateUtilities.ONE_DAY, THIS, wday.javaDayNum);
            nextDueDate = Task.createDueDate(Task.URGENCY_SPECIFIC_DAY, nextDueDate);
            assertEquals(expected, nextDueDate);
        }

        for(Weekday wday1 : Weekday.values()) {
            for(Weekday wday2 : Weekday.values()) {
                if(wday1 == wday2)
                    continue;

                buildRRule(1, Frequency.WEEKLY, wday1, wday2);
                long nextOne = getDate(DateUtilities.now() + DateUtilities.ONE_DAY, THIS, wday1.javaDayNum);
                long nextTwo = getDate(DateUtilities.now() + DateUtilities.ONE_DAY, THIS, wday2.javaDayNum);
                computeNextDueDate(true);
                nextDueDate = Task.createDueDate(Task.URGENCY_SPECIFIC_DAY, nextDueDate);
                assertEquals(Math.min(nextOne, nextTwo), nextDueDate);
            }
        }
    }

    /** test multiple days per week, multiple intervals - COMPLETE DATE */
    @Test
    public void testCompleteDateMultiWeek() throws Exception {
        for(Weekday wday : Weekday.values()) {
            buildRRule(2, Frequency.WEEKLY, wday);
            computeNextDueDate(true);
            long expected = getDate(DateUtilities.now() + DateUtilities.ONE_DAY, NEXT, wday.javaDayNum);
            nextDueDate = Task.createDueDate(Task.URGENCY_SPECIFIC_DAY, nextDueDate);
            assertEquals(expected, nextDueDate);
        }

        for(Weekday wday1 : Weekday.values()) {
            for(Weekday wday2 : Weekday.values()) {
                if(wday1 == wday2)
                    continue;

                buildRRule(2, Frequency.WEEKLY, wday1, wday2);
                long nextOne = getDate(DateUtilities.now() + DateUtilities.ONE_DAY, NEXT, wday1.javaDayNum);
                long nextTwo = getDate(DateUtilities.now() + DateUtilities.ONE_DAY, NEXT, wday2.javaDayNum);
                computeNextDueDate(true);
                nextDueDate = Task.createDueDate(Task.URGENCY_SPECIFIC_DAY, nextDueDate);
                assertEquals(Math.min(nextOne, nextTwo), nextDueDate);
            }
        }
    }

    // --- helpers

    private void computeNextDueDate(boolean fromComplete) throws ParseException{
        nextDueDate = RepeatTaskCompleteListener.computeNextDueDate(task, rrule.toIcal(), fromComplete);
    }

    private void buildRRule(int interval, Frequency freq, Weekday... weekdays) {
        rrule.setInterval(interval);
        rrule.setFreq(freq);
        setRRuleDays(rrule, weekdays);
    }

    private void assertDueDate(long actual, int expectedWhich, int expectedDayOfWeek) {
        long expected = getDate(task.getDueDate(), expectedWhich, expectedDayOfWeek);
        assertEquals(expected, actual);
    }

    public static void assertDateTimeEquals(long date, long other) {
        assertEquals("Expected: " + newDate(date) + ", Actual: " + newDate(other),
                date, other);
    }

    private void setRRuleDays(RRule rrule, Weekday... weekdays) {
        ArrayList<WeekdayNum> days = new ArrayList<WeekdayNum>();
        for(Weekday wd : weekdays)
            days.add(new WeekdayNum(0, wd));
        rrule.setByDay(days);
    }

    private void setTaskDueDate(int which, int day) {
        long time = getDate(DateUtilities.now(), which, day);

        task.setDueDate(time);
    }

    private long getDate(long start, int which, int dayOfWeek) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(start);
        int direction = which > 0 ? 1 : -1;

        while(c.get(Calendar.DAY_OF_WEEK) != dayOfWeek) {
            c.add(Calendar.DAY_OF_MONTH, direction);
        }
        c.add(Calendar.DAY_OF_MONTH, (Math.abs(which) - 1) * direction * 7);
        return Task.createDueDate(Task.URGENCY_SPECIFIC_DAY, c.getTimeInMillis());
    }
}
