package tools.vitruv.methodologisttemplate.vsum;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.LinkedList;
import java.util.List;

import edu.kit.ipd.sdq.commons.util.java.Pair;
import tools.vitruv.change.atomic.uuid.Uuid;
import tools.vitruv.change.composite.description.PropagatedChange;
import tools.vitruv.change.composite.description.VitruviusChange;
import tools.vitruv.change.composite.propagation.ChangePropagationListener;

public class VitruvChangeTimeObserver implements ChangePropagationListener {
    private List<Pair<Long, Long>> timesPerVitruviusChange = new LinkedList<>();
    private long vitruviusChangeCounter = 0;
    private long timestamp;

    @Override
    public void finishedChangePropagation(Iterable<PropagatedChange> propagatedChanges) {
        var time = System.nanoTime() - timestamp;
        timesPerVitruviusChange.add(new Pair<Long,Long>(vitruviusChangeCounter, time));
        vitruviusChangeCounter += 1;
    }

    @Override
    public void startedChangePropagation(VitruviusChange<Uuid> originalChange) {
        timestamp = System.nanoTime();
    }

    public void printResultsTo(String path) throws IOException {
    try (var writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path)))) {
      writer.write("VitruviusChangeCounter,Time");
      writer.newLine();
      for (var record: timesPerVitruviusChange) {
        writer.write(record.getFirst() + "," + record.getSecond());
        writer.newLine();
      }
      writer.flush();
      writer.close();
    }
  }
}
