package net.darmo_creations.jenealogio2.model.datetime.calendar;

import ca.rmen.lfrc.FrenchRevolutionaryCalendarDate;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

/**
 * The French republican/revolutionary calendar system with decimal time.
 * <p>
 * There may be some small discrepancies when converting to and from decimal time.
 *
 * @see ca.rmen.lfrc.FrenchRevolutionaryCalendar
 */
public final class FrenchRepublicanDecimalCalendar implements Calendar<FrenchRepublicanDecimalDateTime> {
  public static final String NAME = "french_republican_decimal";

  @Override
  public FrenchRepublicanDecimalDateTime getDate(int year, int month, int day, Integer hour, Integer minute) {
    var date = new FrenchRevolutionaryCalendarDate(
        Locale.getDefault(), year, month, day, hour != null ? hour : 0, minute != null ? minute : 0, 0);
    return new FrenchRepublicanDecimalDateTime(date, hour != null && minute != null);
  }

  @Override
  public FrenchRepublicanDecimalDateTime convertDate(@NotNull LocalDateTime date, boolean isTimeSet) {
    //noinspection MagicConstant
    var cal = new GregorianCalendar(date.getYear(), date.getMonthValue() - 1, date.getDayOfMonth(),
        date.getHour(), date.getMinute());
    cal.setGregorianChange(new Date(Long.MIN_VALUE));
    return new FrenchRepublicanDecimalDateTime(FrenchRepublicanCalendar.CAL.getDate(cal), isTimeSet);
  }

  @Override
  public String name() {
    return NAME;
  }

  @Override
  public int lengthOfYearInMonths() {
    return 13;
  }

  FrenchRepublicanDecimalCalendar() {
  }
}
