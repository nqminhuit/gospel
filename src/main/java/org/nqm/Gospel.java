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
import java.util.Optional;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

public class Gospel {

  private static final String CONTENT_FILE = "content";
  private static final HttpClient http = HttpClient.newBuilder().build();
  private static final String BASE_URL = "https://www.vaticannews.va/vi/loi-chua-hang-ngay";
  private static final Path LOCAL_PATH = Path.of("/", "tmp", "vi", "loi-chua-hang-ngay");
  private static final String DATE_FORMAT = "yyyy/MM/dd";

  private static String print(String s, boolean isCenter, int textWidth, boolean noColored) {
    final int width = textWidth < 60 ? 60 : textWidth;
    var wrapped = WordUtils.wrap(s, width, null, true);
    if (!noColored) {
      System.out.print("\u001B[33m"); // yellow
    }
    if (isCenter) {
      Stream.of(wrapped.split("%n".formatted()))
          .map(line -> StringUtils.center(line, width + 2))
          .forEach(line -> System.out.println(line));
    }
    else {
      System.out.println(wrapped);
    }

    // return to default color no matter what
    System.out.print("\u001B[0m");
    return s;
  }

  private static String print(String s) {
    return print(s, false, 0, false);
  }

  private static Optional<String> extractArgValue(String key, String[] args) {
    var tmp = key + "=";
    return Stream.of(args).filter(a -> a.startsWith(tmp))
        .findFirst()
        .map(a -> a.substring(tmp.length(), a.length()));
  }

  public static void main(String[] args) throws IOException {
    var sdt = new SimpleDateFormat(DATE_FORMAT);
    var date = extractArgValue("--date", args)
        .map(d -> {
          try {
            return sdt.parse(d);
          }
          catch (ParseException e) {
            throw new GospelException(
                "Invalid date '%s'! Should be of format: '%s', fallback to current date."
                    .formatted(d, DATE_FORMAT));
          }
        })
        .orElse(new Date());

    final var isCenter = Stream.of(args).anyMatch("--center"::equals);
    final var noColored = Stream.of(args).anyMatch("--no-colored"::equals);
    final var textWidth = extractArgValue("--width", args).map(Integer::valueOf).orElse(0);
    final var sDate = sdt.format(date);

    var path = LOCAL_PATH.resolve(sDate);
    var tmpFile = path.resolve(CONTENT_FILE);
    if (Files.exists(tmpFile)) {
      Files.lines(tmpFile).forEach(line -> print(line, isCenter, textWidth, noColored));
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
        .thenAccept(body -> parse(body, path, isCenter, textWidth, noColored))
        .handle((result, e) -> {
          if (e != null) {
            System.err.println(e.getMessage());
          }
          return result;
        });
    f.join();
    print(sDate);
  }

  private static void writeToFile(Path path, List<String> lines) throws IOException {
    if (!Files.exists(path)) {
      Files.createDirectories(path);
    }
    Files.write(path.resolve(CONTENT_FILE), lines, StandardOpenOption.CREATE);
  }

  private static void parse(InputStream in, Path path, boolean isCenter, int textWidth, boolean noColored) {
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
          .map(text -> print(text, isCenter, textWidth, noColored))
          .toList();
      writeToFile(path, lines);
    }
    catch (IOException e) {
      throw new GospelException(e.getMessage());
    }
  }
}
