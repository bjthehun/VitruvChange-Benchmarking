package tools.vitruv.methodologisttemplate.vsum.observers;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.extension.*;


/**
 * A JUnit extension that can be added to test cases that work with a V-SUM.
 */
public class VitruvChangeTimingExtension implements 
    BeforeAllCallback, AfterTestExecutionCallback, AfterAllCallback {
  /**
   * Observer for applying entire VitruvChanges (transactions).
   */
  private final VitruvChangeTimeObserver vitruvChangeObserver = new VitruvChangeTimeObserver();
  /**
   * Observer for applying consistency preservation rule changes.
   */
  private final ConsistencyPreservationRuleTimeObserver cprObserver = new ConsistencyPreservationRuleTimeObserver();
  /**
   * Observers for I/O operations (loading, saving, deleting resources).
   */
  private final ResourceAccessObserver accessObserver = new ResourceAccessObserver();
  /**
   * Name of the class extended with this extension.
   */
  private String extendedClassName;
  
  public static final int WARM_UP_RUNS = 15;
  public static final int MEASUREMENT_RUNS = 15 + WARM_UP_RUNS;

  private final Map<Method, Long> observedExecutions = new HashMap<>();

  /**
   * Configure observers.
   */
  @Override
  public void beforeAll(ExtensionContext context) throws Exception {
    extendedClassName = context.getDisplayName().toLowerCase();
  }

  /**
   * De-configure observers; also print out results.
   */
  @Override
  public void afterAll(ExtensionContext context) throws Exception {
    cprObserver.printResultsTo("results_" + extendedClassName + " _cprs.csv");
    vitruvChangeObserver.printResultsTo("results_" + extendedClassName + "_vitruviuschange.csv");
    accessObserver.printResultsTo("results_" + extendedClassName + "_resourceaccess.csv");
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
      cprObserver.rejectMeasurement();
      vitruvChangeObserver.rejectMeasurement();
      accessObserver.rejectMeasurement();
    }
    else {
      cprObserver.acceptMeasurement();
      vitruvChangeObserver.acceptMeasurement();
      accessObserver.acceptMeasurement();
    }
  }
}
