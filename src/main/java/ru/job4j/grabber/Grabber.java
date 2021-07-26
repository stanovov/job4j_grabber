package ru.job4j.grabber;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import ru.job4j.grabber.utils.SqlRuDateTimeParser;
import ru.job4j.html.SqlRuParse;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

public class Grabber implements Grab {
    private final Properties cfg = new Properties();

    private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    public Store store() {
        return new PsqlStore(cfg);
    }

    public Scheduler scheduler() throws SchedulerException {
        Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
        scheduler.start();
        return scheduler;
    }

    public void cfg() throws IOException {
        try (InputStream in = PsqlStore.class.getClassLoader().getResourceAsStream("grabber.properties")) {
            cfg.load(in);
        }
    }

    public void web(Store store) {
        final String htmlDoc = "<html>"
                       + "<head>"
                            + "<meta charset=\"UTF-8\">"
                            + "<title>Java Вакансии</title>"
                       + "</head>"
                       + "<body>"
                            + "<h1>Java вакансии на %s</h1>"
                            + "<hr align=\"center\" size=\"2\" color=\"#000000\" />"
                            + "<br/>"
                            + "%s"
                       + "</body>"
                       + "</html>";
        final String htmlPost = "<a href=\"%s\"><h3>№%d %s</h3></a>"
                        + "<p>ID: %d, Дата публикации: %s</p>"
                        + "<p>%s</p>"
                        + "<hr align=\"center\" size=\"1\" color=\"#000000\" />";
        new Thread(() -> {
            try (ServerSocket server = new ServerSocket(Integer.parseInt(cfg.getProperty("port")))) {
                while (!server.isClosed()) {
                    Socket socket = server.accept();
                    try (OutputStream out = socket.getOutputStream()) {
                        out.write("HTTP/1.1 200 OK\r\n\r\n".getBytes());
                        StringBuilder sb = new StringBuilder();
                        int count = 1;
                        List<Post> posts = store.getAll().stream()
                                .sorted(Comparator.comparing(Post::getCreated).reversed())
                                .collect(Collectors.toList());
                        for (Post post : posts) {
                            sb.append(
                                    String.format(htmlPost,
                                            post.getLink(),
                                            count++,
                                            post.getTitle(),
                                            post.getId(),
                                            post.getCreated().format(formatter),
                                            post.getDescription()
                                    )
                            );
                        }
                        out.write(
                                String.format(htmlDoc,
                                        LocalDateTime.now().format(formatter),
                                        sb.toString()
                                ).getBytes()
                        );
                        out.write(System.lineSeparator().getBytes());

                    } catch (IOException io) {
                        io.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    @Override
    public void init(Parse parse, Store store, Scheduler scheduler) throws SchedulerException {
        JobDataMap data = new JobDataMap();
        data.put("store", store);
        data.put("parse", parse);
        data.put("url", cfg.getProperty("url"));
        data.put("limit", Integer.parseInt(cfg.getProperty("limit")));
        JobDetail job = newJob(GrabJob.class)
                .usingJobData(data)
                .build();
        SimpleScheduleBuilder times = simpleSchedule()
                .withIntervalInSeconds(Integer.parseInt(cfg.getProperty("time")))
                .repeatForever();
        Trigger trigger = newTrigger()
                .startNow()
                .withSchedule(times)
                .build();
        scheduler.scheduleJob(job, trigger);
    }

    public static class GrabJob implements Job {

        @Override
        public void execute(JobExecutionContext context) throws JobExecutionException {
            JobDataMap map = context.getJobDetail().getJobDataMap();
            Store store = (Store) map.get("store");
            Parse parse = (Parse) map.get("parse");
            String url = (String) map.get("url");
            int limit = (Integer) map.get("limit");
            for (int page = 1; page < limit; page++) {
                List<Post> posts = parse.list(String.format("%s/%d", url, page));
                for (Post post : posts) {
                    store.save(post);
                }
            }
        }
    }

    public static void main(String[] args) throws Exception {
        Grabber grab = new Grabber();
        grab.cfg();
        Scheduler scheduler = grab.scheduler();
        Store store = grab.store();
        grab.init(new SqlRuParse(new SqlRuDateTimeParser()), store, scheduler);
        grab.web(store);
    }
}
