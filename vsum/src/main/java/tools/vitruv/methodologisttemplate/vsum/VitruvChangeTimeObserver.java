package tools.vitruv.methodologisttemplate.vsum;

import java.util.HashSet;
import java.util.Set;

import edu.kit.ipd.sdq.commons.util.java.Triple;
import tools.vitruv.change.atomic.EChange;
import tools.vitruv.change.atomic.uuid.Uuid;
import tools.vitruv.change.composite.description.PropagatedChange;
import tools.vitruv.change.composite.description.VitruviusChange;
import tools.vitruv.change.composite.propagation.ChangePropagationListener;

public class VitruvChangeTimeObserver extends TimeObserver<Triple<Long, Long, Long>> implements ChangePropagationListener {
  private long vitruviusChangeCounter = 0;
  private long eChangeCounter = 0;

  public VitruvChangeTimeObserver() {
    super(new String[]{"VitruviusChangeCounter", "NoOfEChanges", "Time"});
  }

  @Override
  public void finishedChangePropagation(Iterable<PropagatedChange> propagatedChanges) {
    var time = stopTiming();
    Set<EChange<?>> allEChanges = new HashSet<>();
    long eChangesRequired = eChangeCounter;

    for (var change : propagatedChanges) {
      allEChanges.addAll(change.getOriginalChange().getEChanges());
      allEChanges.addAll(change.getConsequentialChanges().getEChanges());
    };
    eChangesRequired += allEChanges.size();

    timesPerChangeType.add(new Triple<Long,Long,Long>(vitruviusChangeCounter, eChangesRequired, time));
    vitruviusChangeCounter += 1;
  }

  @Override
  public void startedChangePropagation(VitruviusChange<Uuid> originalChange) {
    eChangeCounter = originalChange.getEChanges().size();
    startTiming();
  }

  @Override
  protected String[] getLineFor(Triple<Long, Long, Long> record) {
    return new String[] {record.getFirst().toString(), record.getSecond().toString(), record.getThird().toString()};
  }
}
