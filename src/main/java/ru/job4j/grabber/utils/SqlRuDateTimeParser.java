package ru.job4j.grabber.utils;

import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.sql.Timestamp;

public class SqlRuDateTimeParser implements DateTimeParser {

    private static final DateFormatSymbols FORMAT_SYMBOLS = new DateFormatSymbols() {
        @Override
        public String[] getShortMonths() {
            return new String[]{"янв", "фев", "мар",
                                "апр", "май", "июн",
                                "июл", "авг", "сен",
                                "окт", "ноя", "дек"};
        }
    };

    @Override
    public LocalDateTime parse(String parse) throws ParseException {
        SimpleDateFormat dateFormat;
        boolean today = parse.contains("сегодня"), yesterday = parse.contains("вчера");
        boolean alternative = today || yesterday;
        if (alternative) {
            int crop = (today ? 7 : 5) + 2;
            parse = parse.substring(crop);
            dateFormat = new SimpleDateFormat("HH:mm");
        } else {
            dateFormat = new SimpleDateFormat("d MMM yy, HH:mm", FORMAT_SYMBOLS);
        }
        Date date = dateFormat.parse(parse);
        LocalDateTime result = new Timestamp(date.getTime()).toLocalDateTime();
        if (alternative) {
            result = LocalDateTime.of(LocalDate.now(), result.toLocalTime());
            if (yesterday) {
                result = result.minusDays(1);
            }
        }
        return result;
    }
}
