package tools.vitruv.methodologisttemplate.vsum;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.extension.*;

import tools.vitruv.change.atomic.command.internal.ApplyEChangeSwitch;

/**
 * A JUnit extension that can be added to test cases that work with a V-SUM.
 */
public class VitruvChangeTimingExtension implements 
    BeforeAllCallback, AfterTestExecutionCallback, AfterAllCallback {
  private final AtomicEChangeTimeObserver eChangeObserver = new AtomicEChangeTimeObserver();
  private final VitruvChangeTimeObserver vitruvChangeObserver = new VitruvChangeTimeObserver(eChangeObserver);
  private final ConsistencyPreservationRuleTimeObserver cprObserver = new ConsistencyPreservationRuleTimeObserver(eChangeObserver);
  
  public static final int WARM_UP_RUNS = 15;
  public static final int MEASUREMENT_RUNS = 15 + WARM_UP_RUNS;

  private final Map<Method, Long> observedExecutions = new HashMap<>();

  /**
   * De-configure observers; also print out results.
   */
  @Override
  public void afterAll(ExtensionContext context) throws Exception {
    ApplyEChangeSwitch.deregisterObserver(eChangeObserver);
    eChangeObserver.printResultsTo("results_echange.csv");
    cprObserver.printResultsTo("results_cprs.csv");
    vitruvChangeObserver.printResultsTo("results_vitruviuschange.csv");
  }

  /**
   * Configure observers.
   */
  @Override
  public void beforeAll(ExtensionContext context) throws Exception {
    ApplyEChangeSwitch.registerObserver(eChangeObserver);
  }

  /**
   * Record how often a test has been executed.
   * If the execution threshold is met, record the execution times,
   * otherwise, ignore them.
   */
  @Override
  public void afterTestExecution(ExtensionContext context) throws Exception {
    var testMethod = context.getRequiredTestMethod();
    var callsToTestMethod = observedExecutions.compute(testMethod, (method, noOfCalls) -> {
      if (noOfCalls == null) {
        return 1L;
      }
      return noOfCalls + 1;
    });
    observedExecutions.put(testMethod, callsToTestMethod);
    if (callsToTestMethod <= WARM_UP_RUNS) {
      eChangeObserver.rejectMeasurement();
      cprObserver.rejectMeasurement();
      vitruvChangeObserver.rejectMeasurement();
    }
    else {
      eChangeObserver.acceptMeasurement();
      cprObserver.acceptMeasurement();
      vitruvChangeObserver.acceptMeasurement();
    }
  }
}
