package tools.vitruv.methodologisttemplate.vsum.simulinkautosar;

import java.io.IOException;
import java.nio.file.Path;
import java.util.function.Consumer;
import java.util.Collections;
import java.util.List;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import lombok.Getter;
import lombok.AccessLevel;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import edu.kit.ipd.sdq.metamodels.autosar.AutoSARModel;
import edu.kit.ipd.sdq.metamodels.simulink.SimulinkModel;
import mir.reactions.autoSARToSimulink.AutoSARToSimulinkChangePropagationSpecification;
import mir.reactions.simuLinkTOAutoSAR.SimuLinkTOAutoSARChangePropagationSpecification;
import tools.vitruv.framework.views.View;
import tools.vitruv.change.propagation.ChangePropagationSpecification;
import tools.vitruv.change.testutils.RegisterMetamodelsInStandalone;
import tools.vitruv.framework.testutils.integration.ViewBasedVitruvApplicationTest;

import static tools.vitruv.methodologisttemplate.vsum.simulinkautosar.util.SimuLinkQueryUtil.*;
import static tools.vitruv.methodologisttemplate.vsum.simulinkautosar.util.AutoSARQueryUtil.*;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import tools.vitruv.framework.vsum.internal.VirtualModelImpl;
import tools.vitruv.methodologisttemplate.vsum.observers.ConsistencyPreservationRuleTimeObserver;
import tools.vitruv.methodologisttemplate.vsum.observers.ResourceAccessObserver;
import tools.vitruv.methodologisttemplate.vsum.observers.VitruvChangeTimeObserver;
import tools.vitruv.methodologisttemplate.vsum.observers.VitruvChangeTimingExtension;
import tools.vitruv.methodologisttemplate.vsum.simulinkautosar.util.SimuLinkAutoSARViewFactory;
import tools.vitruv.methodologisttemplate.vsum.simulinkautosar.util.SimuLinkAutoSARClassifierEqualityValidation;

@ExtendWith(RegisterMetamodelsInStandalone.class)
public abstract class SimuLinkAutoSARTransformationTest extends ViewBasedVitruvApplicationTest {
	protected SimuLinkAutoSARViewFactory viewFactory;
	protected final SimuLinkAutoSARClassifierEqualityValidation validation = new SimuLinkAutoSARClassifierEqualityValidation(
		SIMULINK_MODEL_NAME,
			((Consumer<Consumer<View>>) (Consumer<View> viewApplication) -> {
				Consumer<View> applied = (View view) -> viewApplication.accept(view);
				this.viewFactory.validateAutoSARAndSimuLinkClassesView(applied);
		})
	);
	
	@Getter(AccessLevel.PROTECTED)
	protected static final String MODEL_FILE_EXTENSION = "arxml";
	@Getter(AccessLevel.PROTECTED)
	protected static final String SIMULINK_MODEL_NAME = "Model";
	@Getter(AccessLevel.PROTECTED)
	protected static final String AUTOSAR_MODEL_NAME = "Model";
	@Getter(AccessLevel.PROTECTED)
	protected static final String MODEL_FOLDER_NAME = "model";
	
	
	@BeforeAll
	static void setupSimuLinkFactories() {
		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("*", new XMIResourceFactoryImpl());
	}

	@BeforeEach
	void setupObservers(TestInfo testInfo) throws IOException {
		var testName = testInfo.getTestClass().get().getSimpleName();
		if (!propagateChanges) {
			testName += "_no_cprs";
		}
		vitruvChangeObserver.setup("results/vitruviuschange_"+testName+".csv");
		cprObserver.setup("results/cprs_"+testName+".csv");
		accessObserver.setup("results/accessoperations_"+testName+".csv");
	}

	protected VitruvChangeTimeObserver vitruvChangeObserver = new VitruvChangeTimeObserver();
	protected ResourceAccessObserver accessObserver = new ResourceAccessObserver();
	protected ConsistencyPreservationRuleTimeObserver cprObserver = new ConsistencyPreservationRuleTimeObserver();
	protected static boolean propagateChanges = false;

	@AfterEach
	final void deregisterObservers() {
		var vsum = (VirtualModelImpl) getVirtualModel();
		vsum.removeChangePropagationListener(vitruvChangeObserver);
		vsum.deregisterObserver(cprObserver);
		vsum.deregisterModelPersistanceObserver(accessObserver);
	}

	@AfterEach
	void writeResultsToFile(TestInfo testInfo) throws IOException {
		vitruvChangeObserver.printResultsTo();
		cprObserver.printResultsTo();
		accessObserver.printResultsTo();
	}

	@AfterEach
  	void decideAboutAcceptingMeasurement(RepetitionInfo repetitionInfo) throws IOException {
		var runsForTest = repetitionInfo.getCurrentRepetition();
		if (runsForTest <= VitruvChangeTimingExtension.WARM_UP_RUNS) {
			vitruvChangeObserver.rejectMeasurement();
			cprObserver.rejectMeasurement();
			accessObserver.rejectMeasurement();
		}
		else {
			System.out.println("Accepting Measurement");
			vitruvChangeObserver.acceptMeasurement();
			cprObserver.acceptMeasurement();
			accessObserver.acceptMeasurement();
		}
 	}

	@BeforeEach
	final void setupViewFactory() {
		var vsum = (VirtualModelImpl) getVirtualModel();
		vsum.addChangePropagationListener(vitruvChangeObserver);
		vsum.registerModelPersistanceObserver(accessObserver);
		vsum.registerObserver(cprObserver);
		viewFactory = new SimuLinkAutoSARViewFactory(vsum);
	}
	

	@Override
	protected boolean enableTransitiveCyclicChangePropagation() {
		return false;
	}
	
	protected SimulinkModel getDefaultSimuLinkModel(View view) {
		return claimSimuLinkModel(view, SIMULINK_MODEL_NAME);
	}
	
	protected AutoSARModel getDefaultAutoSARModel(View view) {
		return claimAutoSARModel(view, AUTOSAR_MODEL_NAME);
	}

	protected Path getProjectModelPath(String modelName) {
		return Path.of(MODEL_FOLDER_NAME).resolve(modelName + "." + MODEL_FILE_EXTENSION);
	}

	@Override
	protected Iterable<ChangePropagationSpecification> getChangePropagationSpecifications() {
		if (propagateChanges) {
			return List.of(
				new AutoSARToSimulinkChangePropagationSpecification(),
				new SimuLinkTOAutoSARChangePropagationSpecification());
		}
		return Collections.emptyList();
	}

	protected void createAndRegisterRoot(View view, EObject rootObject, URI persistenceUri) {
		view.registerRoot(rootObject, persistenceUri);
	}
}
