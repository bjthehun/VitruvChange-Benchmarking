package tools.vitruv.methodologisttemplate.vsum;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;

import edu.kit.ipd.sdq.commons.util.java.Quadruple;
import edu.kit.ipd.sdq.commons.util.java.Quintuple;
import edu.kit.ipd.sdq.commons.util.java.Triple;
import tools.vitruv.change.atomic.EChange;
import tools.vitruv.change.atomic.uuid.Uuid;
import tools.vitruv.change.composite.description.PropagatedChange;
import tools.vitruv.change.composite.description.VitruviusChange;
import tools.vitruv.change.composite.propagation.ChangePropagationListener;

public class VitruvChangeTimeObserver extends TimeObserver<Quintuple<Long, Long, Long, Integer, Integer>> implements ChangePropagationListener {
  private long vitruviusChangeCounter = 0;
  private long eChangeCounter = 0;

  public VitruvChangeTimeObserver() {
    super(new String[]{"VitruviusChangeCounter", "NoOfEChanges", "Time", "NoOfAffectedModels", "NoOfAffectedElements"});
  }

  @Override
  public void finishedChangePropagation(Iterable<PropagatedChange> propagatedChanges) {
    var time = stopTiming();
    Set<EChange<?>> allEChanges = new HashSet<>();
    Set<EObject> affectedModelElements = new HashSet<>();
    Set<URI> affectedModelURIS = new HashSet<>();
    long eChangesRequired = eChangeCounter;

    for (var change : propagatedChanges) {
      Stream.concat(
        change.getOriginalChange().getAffectedEObjects().stream(),
        change.getConsequentialChanges().getAffectedEObjects().stream())
        .forEach(eObject -> affectedModelElements.add(eObject));
      
      Stream.concat(
        change.getOriginalChange().getChangedURIs().stream(),
        change.getConsequentialChanges().getChangedURIs().stream())
        .forEach(uri -> affectedModelURIS.add(uri));

      allEChanges.addAll(change.getOriginalChange().getEChanges());
      allEChanges.addAll(change.getConsequentialChanges().getEChanges());
    };
    eChangesRequired += allEChanges.size();

    timesPerChangeType.add(new Quintuple<Long, Long, Long, Integer, Integer>(
      vitruviusChangeCounter, eChangesRequired, time,
      affectedModelURIS.size(),
      affectedModelElements.size()));
    vitruviusChangeCounter += 1;
  }

  @Override
  public void startedChangePropagation(VitruviusChange<Uuid> originalChange) {
    eChangeCounter = originalChange.getEChanges().size();
    startTiming();
  }

  @Override
  protected String[] getLineFor(Quintuple<Long, Long, Long, Integer, Integer> record) {
    return new String[] {
      record.get0().toString(), 
      record.get1().toString(),
      record.get2().toString(),
      record.get3().toString(), 
      record.get4().toString()};
  }
}
