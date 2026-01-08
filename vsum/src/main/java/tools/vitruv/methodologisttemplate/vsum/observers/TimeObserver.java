package tools.vitruv.methodologisttemplate.vsum.observers;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.LinkedList;
import java.util.List;

public abstract class TimeObserver<T> {
  private final String [] headers;
  private Path path;

  protected TimeObserver (String[] headers) {
    this.headers = headers;
  }

  protected List<T> timesPerChangeType = new LinkedList<>();
  protected List<T> acceptedTimes = new LinkedList<>();
  protected long timestamp;

  protected abstract String[] getLineFor(T record);

  protected void startTiming() {
    timestamp = System.nanoTime();
  }

  protected long stopTiming() {
    return System.nanoTime() - timestamp;
  }

  public void setup(String filePath) throws IOException {
    path = Path.of(filePath);
    if (Files.exists(path)) {
      return;
    }
    Files.createFile(path);
    String header = String.join(", ", headers);
    try {
      Files.write(path, List.of(header));
    } catch (IOException e) {
      System.out.println(e);
      throw e;
    }
  }

  public void acceptMeasurement() throws IOException {
    acceptedTimes.addAll(timesPerChangeType);
    var linesToPrint = timesPerChangeType
        .stream()
        .map(record -> String.join(",", getLineFor(record)))
        .toList();
    Files.write(path, linesToPrint, StandardOpenOption.APPEND);
    timesPerChangeType.clear();
  }

  public void rejectMeasurement() {
    timesPerChangeType.clear();
  }

  public void printResultsTo() throws IOException {
//    try (var writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path.toString())))) {
//      for (var record: acceptedTimes) {
//        writer.write(String.join(", ", getLineFor(record)));
//        writer.newLine();
//      }
//      writer.flush();
//    }
  }
}
