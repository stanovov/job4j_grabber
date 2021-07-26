package ru.job4j.html;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.job4j.grabber.Parse;
import ru.job4j.grabber.Post;
import ru.job4j.grabber.utils.DateTimeParser;
import ru.job4j.grabber.utils.SqlRuDateTimeParser;

import java.io.IOException;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SqlRuParse implements Parse {

    private final DateTimeParser dateTimeParser;

    public SqlRuParse(DateTimeParser dateTimeParser) {
        this.dateTimeParser = dateTimeParser;
    }

    @Override
    public List<Post> list(String link) {
        List<Post> result = new ArrayList<>();
        int limit = 1;
        for (int page = 1; page <= limit; page++) {
            String url = String.format("%s/%d", link, page);
            Document doc;
            try {
                doc = Jsoup.connect(url).get();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            Elements elements = doc.select(".postslisttopic");
            List<Element> elemPosts = elements.stream()
                    .filter(element -> !element.text().startsWith("Важно: ") && !element.text().contains("[закрыт]"))
                    .collect(Collectors.toList());
            for (Element elemPost : elemPosts) {
                String postLink = elemPost.child(0).attr("href");
                Post post = detail(postLink);
                result.add(post);
            }
        }
        return result;
    }

    @Override
    public Post detail(String link) {
        Document doc;
        try {
            doc = Jsoup.connect(link).get();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Element msgTable = doc.select(".msgTable").first().child(0);
        String title = msgTable.child(0).child(0).text();
        String description = msgTable.child(1).child(1).text();
        String dataTime = msgTable.child(2).child(0).text();
        dataTime = dataTime.substring(0, dataTime.indexOf("[") - 1);
        LocalDateTime created;
        try {
            created = dateTimeParser.parse(dataTime);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        return new Post(title, link, description, created);
    }

    public static void main(String[] args) {
        String url = "https://www.sql.ru/forum/job-offers";
        SqlRuParse sqlRuParse = new SqlRuParse(new SqlRuDateTimeParser());
        for (Post post : sqlRuParse.list(url)) {
            System.out.println(post);
        }
    }
}
