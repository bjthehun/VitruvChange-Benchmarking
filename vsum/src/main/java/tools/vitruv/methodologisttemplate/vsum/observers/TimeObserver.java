package tools.vitruv.methodologisttemplate.vsum.observers;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.LinkedList;
import java.util.List;

public abstract class TimeObserver<T> {
  private final String [] headers;
  private final String filePath;

  protected TimeObserver (String[] headers, String filePath) {
    this.headers = headers;
    this.filePath = filePath;
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

  public void acceptMeasurement() {
    acceptedTimes.addAll(timesPerChangeType);
    timesPerChangeType.clear();
  }

  public void rejectMeasurement() {
    timesPerChangeType.clear();
  }

  public void printResultsTo() throws IOException {
    try (var writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath)))) {
      writer.write(String.join(", ", headers));
      writer.newLine();
      for (var record: acceptedTimes) {
        writer.write(String.join(", ", getLineFor(record)));
        writer.newLine();
      }
      writer.flush();
      writer.close();
    }
  }
}
