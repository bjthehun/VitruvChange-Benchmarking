package tools.vitruv.methodologisttemplate.vsum;

import edu.kit.ipd.sdq.commons.util.java.Triple;

import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.ecore.EObject;

import com.google.common.collect.Iterables;

import tools.vitruv.change.atomic.EChange;
import tools.vitruv.change.atomic.command.ApplyEChangeObserver;

public class AtomicEChangeTimeObserver extends TimeObserver<Triple<String, Long, Integer>> implements ApplyEChangeObserver {
  protected long eChangeCounter = 0;
  private EChange<EObject> lastChange;

  public AtomicEChangeTimeObserver() {
    super(new String[]{"EChangeType", "Time", "Commands"});
  }

  @Override
  public void endApplyEChange(Iterable<Command> commands) {
    var time = stopTiming();
    var changeClassName = lastChange.eClass().getName();
    timesPerChangeType.add(new Triple<String, Long, Integer>
        (changeClassName, time, Iterables.size(commands)));
    eChangeCounter += 1;
  }

  @Override
  public void startToApplyEChange(EChange<EObject> change, boolean forward) {
    lastChange = change;
    startTiming();
  }

  @Override
  protected String[] getLineFor(Triple<String, Long, Integer> record) {
    return new String[] {record.getFirst(), record.getSecond().toString(), record.getThird().toString()};
  }
}
