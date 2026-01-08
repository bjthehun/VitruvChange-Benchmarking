package tools.vitruv.methodologisttemplate.vsum.simulinkautosar.autosar2simulink;

import org.junit.jupiter.api.RepeatedTest;
import org.eclipse.emf.ecore.util.EcoreUtil;

import static tools.vitruv.methodologisttemplate.vsum.simulinkautosar.util.AutoSARQueryUtil.*;
import edu.kit.ipd.sdq.metamodels.autosar.AtomicSwComponent;
import edu.kit.ipd.sdq.metamodels.autosar.CompositeSwComponent;
import edu.kit.ipd.sdq.metamodels.autosar.AutoSARFactory;
import tools.vitruv.methodologisttemplate.vsum.observers.VitruvChangeTimingExtension;

class AutoSARtoSimuLinkPortTest extends AbstractAutoSARToSimuLinkTest {
	
	static final String DEFAULT_COMPONENT_NAME = "TestComponent";
	static final String DEFAULT_COMPOSITE_COMPONENT_NAME = "TestCompositeComponent";
	static final String DEFAULT_REQUIRED_PORT_NAME = "TestRequiredPort";
	static final String DEFAULT_PROVIDED_PORT_NAME = "TestProvidedPort";
	static final String DEFAULT_DELAGATIONSWCONNECTOR_NAME = "DelegationConnection";
	static final String DEFAULT_REQUIREDPORT_COMPOSITE_NAME = "TestName";
	static final String DEFAULT_OUTPORTBLOCK_NAME = "OutPortBlock";
	
	/*
	 * Tests for Ports
	 */
	 
	@RepeatedTest(VitruvChangeTimingExtension.MEASUREMENT_RUNS)
	void testCreateRequiredPortinSwComponent() {
		createAtomicSWComponentInModel(DEFAULT_COMPONENT_NAME);
		viewFactory.changeAutoSARView((view) -> {
			var atomicComponent = (AtomicSwComponent) claimAutoSARElement(view, AtomicSwComponent.class, DEFAULT_COMPONENT_NAME);
			var port = AutoSARFactory.eINSTANCE.createRequiredPort();
			port.setName(DEFAULT_REQUIRED_PORT_NAME);
			atomicComponent.getPort().add(port);
		});
		validation.assertPortsInBlockOrComponent(DEFAULT_COMPONENT_NAME);
	}



	@RepeatedTest(VitruvChangeTimingExtension.MEASUREMENT_RUNS)
	void testCreateProvidedPortinSwComponent() {
		createAtomicSWComponentInModel(DEFAULT_COMPONENT_NAME);
		viewFactory.changeAutoSARView((view) ->{
			var atomicComponent = (AtomicSwComponent) claimAutoSARElement(view, AtomicSwComponent.class, DEFAULT_COMPONENT_NAME);
			var port = AutoSARFactory.eINSTANCE.createProvidedPort();
			port.setName(DEFAULT_PROVIDED_PORT_NAME);
			atomicComponent.getPort().add(port);
		});
		validation.assertPortsInBlockOrComponent(DEFAULT_COMPONENT_NAME);
	}

	@RepeatedTest(VitruvChangeTimingExtension.MEASUREMENT_RUNS)
	void testCreateRequiredPortinCompositeSwComponent() {
		createCompositeSWComponentInModel(DEFAULT_COMPOSITE_COMPONENT_NAME);
		viewFactory.changeAutoSARView((view) ->{
			var atomicComponent = (CompositeSwComponent) claimAutoSARElement(view, CompositeSwComponent.class, DEFAULT_COMPONENT_NAME);
			var port = AutoSARFactory.eINSTANCE.createRequiredPort();
			port.setName(DEFAULT_REQUIRED_PORT_NAME);
			atomicComponent.getPort().add(port);
		});
		validation.assertPortsInBlockOrComponent(DEFAULT_COMPOSITE_COMPONENT_NAME);
	}

	@RepeatedTest(VitruvChangeTimingExtension.MEASUREMENT_RUNS)
	void testCreateProvidedPortinCompositeSwComponent() {
		createCompositeSWComponentInModel(DEFAULT_COMPOSITE_COMPONENT_NAME);
		viewFactory.changeAutoSARView((view) ->{
			var atomicComponent = (CompositeSwComponent) claimAutoSARElement(view, CompositeSwComponent.class, DEFAULT_COMPONENT_NAME);
			var port = AutoSARFactory.eINSTANCE.createProvidedPort();
			port.setName(DEFAULT_PROVIDED_PORT_NAME);
			atomicComponent.getPort().add(port);
		});
		validation.assertPortsInBlockOrComponent(DEFAULT_COMPOSITE_COMPONENT_NAME);
	}

	@RepeatedTest(VitruvChangeTimingExtension.MEASUREMENT_RUNS)
	void testDeleteRequiredPortinSwComponent() {
		testCreateRequiredPortinSwComponent();
				
		viewFactory.changeAutoSARView((view) -> {
			var atomicComponent = claimAutoSARElement(view, AtomicSwComponent.class, DEFAULT_COMPONENT_NAME);
			EcoreUtil.delete(claimAutoSARPort(atomicComponent, DEFAULT_REQUIRED_PORT_NAME));
		});
		validation.assertNoPortWithNameInComponent(DEFAULT_COMPONENT_NAME, DEFAULT_REQUIRED_PORT_NAME);
	}

	@RepeatedTest(VitruvChangeTimingExtension.MEASUREMENT_RUNS)
	void testDeleteProvidedPortinSwComponent() {
		testCreateProvidedPortinSwComponent();

		viewFactory.changeAutoSARView((view) -> {
			var atomicComponent = claimAutoSARElement(view, AtomicSwComponent.class, DEFAULT_COMPONENT_NAME);
			EcoreUtil.delete(claimAutoSARPort(atomicComponent, DEFAULT_PROVIDED_PORT_NAME));
		});
		validation.assertNoPortWithNameInComponent(DEFAULT_COMPONENT_NAME, DEFAULT_PROVIDED_PORT_NAME);
	}
	
	/*
	 * Test of SwConnectors
	 */
	@RepeatedTest(VitruvChangeTimingExtension.MEASUREMENT_RUNS)
	void testCreateDelegationSwConnector(){
		createCompositeSWComponentInModel(DEFAULT_COMPOSITE_COMPONENT_NAME);
		createAtomicSWComponentInModel(DEFAULT_COMPONENT_NAME);
		
		viewFactory.changeAutoSARView((view) -> {
			var atomicComponent = (AtomicSwComponent) claimAutoSARElement(view, AtomicSwComponent.class, DEFAULT_COMPONENT_NAME);

			var atomicOutPort = AutoSARFactory.eINSTANCE.createProvidedPort();
			atomicOutPort.setName(DEFAULT_PROVIDED_PORT_NAME);
			atomicComponent.getPort().add(atomicOutPort);

			var compositeComponent = claimAutoSARCompositeSwComponent(view, CompositeSwComponent.class, DEFAULT_COMPOSITE_COMPONENT_NAME);
			var compositeOutPort = AutoSARFactory.eINSTANCE.createProvidedPort();
			compositeOutPort.setName(DEFAULT_REQUIREDPORT_COMPOSITE_NAME);
			compositeComponent.getPort().add(compositeOutPort);
			compositeComponent.getAtomicswcomponent().add(atomicComponent);

			var swconnector = AutoSARFactory.eINSTANCE.createDelegationSwConnector();
			swconnector.setName(DEFAULT_DELAGATIONSWCONNECTOR_NAME);
			swconnector.setInnerPort(atomicOutPort);
			swconnector.setOuterPort(compositeOutPort);
			compositeComponent.getSwconnector().add(swconnector);
		});
		validation.assertDelegationSwConnectorEqualsSingleConnection(
			DEFAULT_DELAGATIONSWCONNECTOR_NAME,
			DEFAULT_COMPOSITE_COMPONENT_NAME,
			DEFAULT_OUTPORTBLOCK_NAME,
			DEFAULT_COMPONENT_NAME);
	}
}