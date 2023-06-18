package net.darmo_creations.jenealogio2.model.datetime.calendar;

import org.jetbrains.annotations.NotNull;
import org.threeten.extra.chrono.CopticDate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoField;

/**
 * This class represents a date-time in the coptic calendar system.
 *
 * @see CopticCalendar
 */
public final class CopticDateTime extends CalendarSpecificDateTime {
  public static final int HOURS_IN_DAY = 24;
  public static final int MINUTES_IN_HOUR = 60;

  CopticDateTime(@NotNull CopticDate date, Integer hours, Integer minutes) {
    super(
        date.get(ChronoField.YEAR),
        date.get(ChronoField.MONTH_OF_YEAR),
        date.get(ChronoField.DAY_OF_MONTH),
        hours,
        minutes
    );
  }

  @Override
  public LocalDateTime toISO8601Date() {
    return LocalDate.ofEpochDay(CopticDate.of(this.year(), this.month(), this.dayOfMonth()).toEpochDay())
        .atTime(LocalTime.of(this.hour().orElse(0), this.minute().orElse(0)));
  }

  @Override
  protected int hoursInDay() {
    return HOURS_IN_DAY;
  }

  @Override
  protected int minutesInHour() {
    return MINUTES_IN_HOUR;
  }
}
