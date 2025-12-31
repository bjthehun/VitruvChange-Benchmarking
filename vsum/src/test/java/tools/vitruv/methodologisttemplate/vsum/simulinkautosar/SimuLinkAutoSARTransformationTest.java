package tools.vitruv.methodologisttemplate.vsum.simulinkautosar;

import java.nio.file.Path;
import java.util.function.Consumer;
import java.util.List;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import lombok.Getter;
import lombok.AccessLevel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach; 
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
import tools.vitruv.methodologisttemplate.vsum.observers.VitruvChangeTimeObserver;
import tools.vitruv.methodologisttemplate.vsum.simulinkautosar.util.SimuLinkAutoSARViewFactory;
import tools.vitruv.methodologisttemplate.vsum.simulinkautosar.util.SimuLinkAutoSARClassifierEqualityValidation;

@ExtendWith(RegisterMetamodelsInStandalone.class)
public abstract class SimuLinkAutoSARTransformationTest extends ViewBasedVitruvApplicationTest {
	protected SimuLinkAutoSARViewFactory viewFactory;
	protected VitruvChangeTimeObserver observer = new VitruvChangeTimeObserver();
	
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
	final void setupViewFactory() {
		var vsum = getVirtualModel();
		vsum.addChangePropagationListener(observer);
		viewFactory = new SimuLinkAutoSARViewFactory(vsum);
	}

	@AfterEach
	final void deregisterObservers() {
		getVirtualModel().removeChangePropagationListener(observer);
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
		return List.of(new AutoSARToSimulinkChangePropagationSpecification(), new SimuLinkTOAutoSARChangePropagationSpecification());
	}

	protected void createAndRegisterRoot(View view, EObject rootObject, URI persistenceUri) {
		view.registerRoot(rootObject, persistenceUri);
	}
}
