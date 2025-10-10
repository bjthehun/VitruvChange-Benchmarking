package tools.vitruv.methodologisttemplate.vsum;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import edu.kit.ipd.sdq.commons.util.java.Pair;
import tools.vitruv.change.atomic.uuid.Uuid;
import tools.vitruv.change.composite.description.PropagatedChange;
import tools.vitruv.change.composite.description.VitruviusChange;
import tools.vitruv.change.composite.propagation.ChangePropagationListener;

public class VitruvChangeTimeObserver extends TimeObserver<Pair<Long, Long>> implements ChangePropagationListener {
  private long vitruviusChangeCounter = 0;

  public VitruvChangeTimeObserver() {
    super(new String[]{"VitruviusChangeCounter","Time"});
  }

  @Override
  public void finishedChangePropagation(Iterable<PropagatedChange> propagatedChanges) {
    var time = stopTiming();
    timesPerChangeType.add(new Pair<Long,Long>(vitruviusChangeCounter, time));
    vitruviusChangeCounter += 1;
  }

  @Override
  public void startedChangePropagation(VitruviusChange<Uuid> originalChange) {
    startTiming();
  }

  @Override
  protected String[] getLineFor(Pair<Long, Long> record) {
    return new String[] {record.getFirst().toString(), record.getSecond().toString()};
  }
}
