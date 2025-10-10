package tools.vitruv.methodologisttemplate.vsum;

import org.eclipse.emf.ecore.EObject;

import edu.kit.ipd.sdq.commons.util.java.Triple;
import tools.vitruv.change.atomic.EChange;
import tools.vitruv.change.propagation.ChangePropagationObserver;
import tools.vitruv.change.propagation.ChangePropagationSpecification;

public class ConsistencyPreservationRuleTimeObserver extends TimeObserver<Triple<Long, Long, Long>> implements ChangePropagationObserver {
  private long cpsApplicationCounter = 0;

  private AtomicEChangeTimeObserver delegateEChangeObserver;
  private long eChangeApplicationCounter = 0;

  public ConsistencyPreservationRuleTimeObserver(AtomicEChangeTimeObserver delegateEChangeTimeObserver) {
    super(new String[]{"RuleCounter", "NoOfChanges", "Time"});
    this.delegateEChangeObserver = delegateEChangeTimeObserver;
  }

  @Override
  protected String[] getLineFor(Triple<Long, Long, Long> record) {
    return new String[]{record.getFirst().toString(), record.getSecond().toString(), record.getThird().toString()};
  }

  @Override
  public void changePropagationStarted(ChangePropagationSpecification cps, EChange<EObject> triggeringEChange) {
    eChangeApplicationCounter = delegateEChangeObserver.eChangeCounter;
    startTiming();
  }

  @Override
  public void changePropagationStopped(ChangePropagationSpecification cps, EChange<EObject> triggeringChange) {
    var timeForCPS = stopTiming();
    var eChangesRequired = delegateEChangeObserver.eChangeCounter - eChangeApplicationCounter;
    timesPerChangeType.add(new Triple<Long,Long,Long>(cpsApplicationCounter, eChangesRequired, timeForCPS));
    cpsApplicationCounter++;
  }

  @Override
  public void objectCreated(EObject object) {
    return;
  }
}
