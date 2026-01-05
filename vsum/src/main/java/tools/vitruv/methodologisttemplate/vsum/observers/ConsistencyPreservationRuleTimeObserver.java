package tools.vitruv.methodologisttemplate.vsum.observers;

import org.eclipse.emf.ecore.EObject;

import edu.kit.ipd.sdq.commons.util.java.Pair;
import tools.vitruv.change.atomic.EChange;
import tools.vitruv.change.propagation.ChangePropagationObserver;
import tools.vitruv.change.propagation.ChangePropagationSpecification;

public class ConsistencyPreservationRuleTimeObserver extends TimeObserver<Pair<Long, Long>> implements ChangePropagationObserver {
  private long cpsApplicationCounter = 0;

  public ConsistencyPreservationRuleTimeObserver() {
    super(new String[]{"RuleCounter", "Time"});

  }

  @Override
  protected String[] getLineFor(Pair<Long, Long> record) {
    return new String[]{record.getFirst().toString(), record.getSecond().toString()};
  }

  @Override
  public void changePropagationStarted(ChangePropagationSpecification cps, EChange<EObject> triggeringEChange) {
    startTiming();
  }

  @Override
  public void changePropagationStopped(ChangePropagationSpecification cps, EChange<EObject> triggeringChange) {
    var timeForCPS = stopTiming();
    cpsApplicationCounter++;
    timesPerChangeType.add(new Pair<Long,Long>(cpsApplicationCounter, timeForCPS));
  }

  @Override
  public void objectCreated(EObject object) {
    return;
  }
}
