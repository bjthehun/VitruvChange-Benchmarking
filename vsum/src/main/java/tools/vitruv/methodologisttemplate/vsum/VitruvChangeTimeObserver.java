package tools.vitruv.methodologisttemplate.vsum;

import edu.kit.ipd.sdq.commons.util.java.Pair;
import edu.kit.ipd.sdq.commons.util.java.Triple;
import tools.vitruv.change.atomic.uuid.Uuid;
import tools.vitruv.change.composite.description.PropagatedChange;
import tools.vitruv.change.composite.description.VitruviusChange;
import tools.vitruv.change.composite.propagation.ChangePropagationListener;

public class VitruvChangeTimeObserver extends TimeObserver<Triple<Long, Long, Long>> implements ChangePropagationListener {
  private AtomicEChangeTimeObserver delegateEChangeObserver;
  private long vitruviusChangeCounter = 0;
  private long eChangeCounter = 0;

  public VitruvChangeTimeObserver(AtomicEChangeTimeObserver delegateEChangeTimeObserver) {
    super(new String[]{"VitruviusChangeCounter","NoOfEChanges", "Time"});
    this.delegateEChangeObserver = delegateEChangeTimeObserver;
  }

  @Override
  public void finishedChangePropagation(Iterable<PropagatedChange> propagatedChanges) {
    var time = stopTiming();
    long eChangesRequired = delegateEChangeObserver.eChangeCounter;
    for (var change : propagatedChanges) {
      eChangesRequired += change.getConsequentialChanges().getEChanges().size();
      eChangesRequired += change.getOriginalChange().getEChanges().size();
    };
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
