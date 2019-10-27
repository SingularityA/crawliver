package ru.spbu.crawliver.dbstats;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.url.WebURL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.spbu.crawliver.db.DatabaseService;

import java.util.regex.Pattern;

public class DatabaseStoringCrawler extends WebCrawler {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseStoringCrawler.class);

    private static final Pattern FILE_ENDING_EXCLUSION_PATTERN = Pattern.compile(".*(\\.(" +
            "css|js" +
            "|bmp|gif|jpe?g|JPE?G|png|tiff?|ico|nef|raw" +
            "|mid|mp2|mp3|mp4|wav|wma|flv|mpe?g" +
            "|avi|mov|mpeg|ram|m4v|wmv|rm|smil" +
            "|swf" +
            "|zip|rar|gz|bz2|7z|bin" +
            "|xml|txt|java|c|cpp|exe" +
            "))$");

    private final DatabaseService service;
    private final String domain;

    public DatabaseStoringCrawler(DatabaseService service, String domain) {
        this.service = service;
        this.domain = domain;
    }

    @Override
    public boolean shouldVisit(Page referringPage, WebURL url) {
        final String href = url.getURL().toLowerCase();
        return href.contains(domain) && !FILE_ENDING_EXCLUSION_PATTERN.matcher(href).matches();
    }

    @Override
    public void visit(Page page) {
        final String url = page.getWebURL().getURL();
        logger.info("URL: {}", url);
        logger.info("ContentType: {}", page.getContentType());

        try {
            service.store(page);
        } catch (RuntimeException e) {
            logger.error("Storing failed", e);
        }
    }

    @Override
    public void onBeforeExit() {
        if (service != null) {
            service.close();
        }
    }
}