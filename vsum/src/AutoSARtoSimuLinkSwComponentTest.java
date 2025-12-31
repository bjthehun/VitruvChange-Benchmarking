package tools.vitruv.methodologisttemplate.vsum.simulinkautosar.autosar2simulink;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.eclipse.emf.ecore.util.EcoreUtil;

import static tools.vitruv.methodologisttemplate.vsum.simulinkautosar.util.AutoSARQueryUtil.*;
import edu.kit.ipd.sdq.metamodels.autosar.AtomicSwComponent;
import edu.kit.ipd.sdq.metamodels.autosar.CompositeSwComponent;
import tools.vitruv.methodologisttemplate.vsum.observers.VitruvChangeTimingExtension;

class AutoSARtoSimuLinkSwComponentTest extends AbstractAutoSARToSimuLinkTest {
	
	static final String DEFAULT_COMPONENT_NAME = "TestComponent";
	static final String DEFAULT_COMPOSITE_COMPONENT_NAME = "TestCompositeComponent";
	
	/*
	 * Tests for AtomicSwComponents
	 */

	@RepeatedTest(VitruvChangeTimingExtension.MEASUREMENT_RUNS)
	void testCreateComponent() {
		createAtomicSWComponentInModel(DEFAULT_COMPONENT_NAME);
		validation.assertSwComponentWithNameInRootModel(DEFAULT_COMPONENT_NAME);
	}

	@RepeatedTest(VitruvChangeTimingExtension.MEASUREMENT_RUNS)
	void testDeleteComponent(){
		createAtomicSWComponentInModel(DEFAULT_COMPONENT_NAME);
		viewFactory.changeAutoSARView(view ->
			EcoreUtil.delete(claimAutoSARElement(view, AtomicSwComponent.class, DEFAULT_COMPONENT_NAME))
		);
		validation.assertNoElementWithNameInRootModel(DEFAULT_COMPONENT_NAME);
	}

	/*
	 * Tests for CompositeSwComponents
	 */

	@RepeatedTest(VitruvChangeTimingExtension.MEASUREMENT_RUNS)
	void testCreateCompositeComponent() {
		createCompositeSWComponentInModel(DEFAULT_COMPOSITE_COMPONENT_NAME);
		createAtomicSWComponentInModel(DEFAULT_COMPONENT_NAME);
		viewFactory.changeAutoSARView(view -> {
			var atomicComponent = (AtomicSwComponent) claimAutoSARElement(view, AtomicSwComponent.class, DEFAULT_COMPONENT_NAME);
			claimAutoSARCompositeSwComponent(view, CompositeSwComponent.class, DEFAULT_COMPOSITE_COMPONENT_NAME)
				.getAtomicswcomponent()
				.add(atomicComponent);
		});
		
		validation.assertCompositeSwComponentWithNameInRootModel(DEFAULT_COMPOSITE_COMPONENT_NAME);
	}

	@RepeatedTest(VitruvChangeTimingExtension.MEASUREMENT_RUNS)
	void testDeleteCompositeComponent(){
		createCompositeSWComponentInModel(DEFAULT_COMPOSITE_COMPONENT_NAME);
		viewFactory.changeAutoSARView(view ->
			EcoreUtil.delete(claimAutoSARCompositeSwComponent(view, CompositeSwComponent.class, DEFAULT_COMPOSITE_COMPONENT_NAME))
		);
		validation.assertNoElementWithNameInRootModel(DEFAULT_COMPOSITE_COMPONENT_NAME);
	}

	@RepeatedTest(VitruvChangeTimingExtension.MEASUREMENT_RUNS)
	void testDeleteCompositeComponentAndCheckContainedAtomicComponent() {
		createCompositeSWComponentInModel(DEFAULT_COMPOSITE_COMPONENT_NAME);
		createAtomicSWComponentInModel(DEFAULT_COMPONENT_NAME);
		viewFactory.changeAutoSARView(view -> {
			var atomicComponent = (AtomicSwComponent) claimAutoSARElement(view, AtomicSwComponent.class, DEFAULT_COMPONENT_NAME);
			claimAutoSARCompositeSwComponent(view, CompositeSwComponent.class, DEFAULT_COMPOSITE_COMPONENT_NAME)
				.getAtomicswcomponent()
				.add(atomicComponent);
		});
		viewFactory.changeAutoSARView(view ->
			EcoreUtil.delete(claimAutoSARCompositeSwComponent(
				view,
				CompositeSwComponent.class,
				DEFAULT_COMPOSITE_COMPONENT_NAME))
		);
		validation.assertNoElementWithNameInRootModel(DEFAULT_COMPOSITE_COMPONENT_NAME);
		validation.assertNoElementWithNameInRootModel(DEFAULT_COMPONENT_NAME);
	}
}