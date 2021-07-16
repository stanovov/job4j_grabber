package ru.job4j.html;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.job4j.grabber.Post;
import ru.job4j.grabber.utils.DateTimeParser;
import ru.job4j.grabber.utils.SqlRuDateTimeParser;

import java.io.IOException;
import java.text.ParseException;
import java.time.LocalDateTime;

public class SqlRuParse {

    private static DateTimeParser timeParser = new SqlRuDateTimeParser();

    public static void main(String[] args) throws Exception {
        String url = "https://www.sql.ru/forum/job-offers";
        for (int page = 1; page <= 5; page++) {
            Document doc = Jsoup.connect(String.format("%s/%d", url, page)).get();
            Elements row = doc.select(".postslisttopic");
            for (Element td : row) {
                System.out.println(parseVacancy(td));
            }
        }
    }

    public static Post parseVacancy(Element td) {
        Element elemVacancy = td.child(0);
        String link = elemVacancy.attr("href");
        String title = elemVacancy.text();
        try {
            Document docPost = Jsoup.connect(link).get();
            Element msgTable = docPost.select(".msgTable").first();
            String description = msgTable.child(0).child(1).child(1).text();
            String dataTime = msgTable.child(0).child(2).child(0).text();
            dataTime = dataTime.substring(0, dataTime.indexOf("[") - 1);
            LocalDateTime created;
            try {
                created = timeParser.parse(dataTime);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
            return new Post(title, link, description, created);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
