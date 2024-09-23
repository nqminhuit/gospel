package org.nqm;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import org.apache.commons.text.WordUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

public class Gospel {

  private static final String CONTENT_FILE = "content";
  private static final HttpClient http = HttpClient.newBuilder().build();
  private static final String BASE_URL = "https://www.vaticannews.va/vi/loi-chua-hang-ngay";
  private static final Path LOCAL_PATH = Path.of("/", "tmp", "vi", "loi-chua-hang-ngay");
  private static final String DATE_FORMAT = "yyyy/MM/dd";

  private static String print(String s) {
    var wrapped = WordUtils.wrap(s, 80, null, true);
    System.out.println("\u001B[33m%s\u001B[0m".formatted(wrapped));
    return wrapped;
  }

  public static void main(String[] args) throws IOException {
    var sdt = new SimpleDateFormat(DATE_FORMAT);
    var date = new Date();
    if (args.length > 0) {
      try {
        date = sdt.parse(args[0]);
      }
      catch (ParseException e) {
        throw new GospelException(
            "Invalid date '%s'! Should be of format: '%s', fallback to current date."
                .formatted(args[0], DATE_FORMAT));
      }
    }
    var sDate = sdt.format(date);
    print(sDate);

    var path = LOCAL_PATH.resolve(sDate);
    var tmpFile = path.resolve(CONTENT_FILE);
    if (Files.exists(tmpFile)) {
      Files.lines(tmpFile).forEach(Gospel::print);
      return;
    }

    var req = HttpRequest.newBuilder(URI.create("%s/%s.html".formatted(BASE_URL, sDate))).GET().build();
    var f = http.sendAsync(req, HttpResponse.BodyHandlers.ofInputStream())
        .thenApply(resp -> {
          var code = resp.statusCode();
          if (code == 404) {
            throw new GospelException("Gospel not found!");
          }
          return resp.body();
        })
        .thenAccept(body -> parse(body, path))
        .handle((result, e) -> {
          if (e != null) {
            System.err.println(e.getMessage());
          }
          return result;
        });
    f.join();
  }

  private static void writeToFile(Path path, List<String> lines) throws IOException {
    if (!Files.exists(path)) {
      Files.createDirectories(path);
    }
    Files.write(path.resolve(CONTENT_FILE), lines, StandardOpenOption.CREATE);
  }

  private static void parse(InputStream in, Path path) {
    try {
      var lines = Jsoup.parse(in, "UTF-8", "")
          .getElementsByClass("section__content")
          .getLast()
          .stream()
          .filter(el -> "p".equals(el.tagName()))
          .map(el -> {
            el.getElementsByTag("sup").forEach(Element::remove);
            return el.text();
          })
          .map(Gospel::print)
          .toList();
      writeToFile(path, lines);
    }
    catch (IOException e) {
      throw new GospelException(e.getMessage());
    }
  }
}
