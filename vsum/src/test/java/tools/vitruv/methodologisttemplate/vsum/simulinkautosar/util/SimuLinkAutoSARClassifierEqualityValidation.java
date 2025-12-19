package tools.vitruv.methodologisttemplate.vsum.simulinkautosar.util;

import edu.kit.ipd.sdq.metamodels.autosar.AtomicSwComponent;
import edu.kit.ipd.sdq.metamodels.autosar.AutoSARElement;
import edu.kit.ipd.sdq.metamodels.autosar.ProvidedPort;
import edu.kit.ipd.sdq.metamodels.autosar.RequiredPort;
import edu.kit.ipd.sdq.metamodels.autosar.SwComponent;
import org.eclipse.xtend.lib.annotations.FinalFieldsConstructor;
import edu.kit.ipd.sdq.metamodels.simulink.Block;
import edu.kit.ipd.sdq.metamodels.simulink.InPort;
import edu.kit.ipd.sdq.metamodels.simulink.OutPort;
import edu.kit.ipd.sdq.metamodels.simulink.SimulinkElement;
import tools.vitruv.framework.views.View;

import static org.junit.jupiter.api.Assertions.*;
import static tools.vitruv.methodologisttemplate.vsum.simulinkautosar.util.AutoSARQueryUtil.*;
import static tools.vitruv.methodologisttemplate.vsum.simulinkautosar.util.SimuLinkQueryUtil.*;

import java.util.Collections;
import java.util.Set;
import java.util.function.Consumer;

import edu.kit.ipd.sdq.metamodels.autosar.CompositeSwComponent;
import edu.kit.ipd.sdq.metamodels.simulink.SubSystem;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import edu.kit.ipd.sdq.metamodels.simulink.SingleConnection;
import edu.kit.ipd.sdq.metamodels.autosar.DelegationSwConnector;
import edu.kit.ipd.sdq.metamodels.autosar.AssemblySwConnector;

/**
 * This class provides validations for the equal existence of classifiers of different types to exist
 * in both AutoSAR and SimuLink models.
 */
public class SimuLinkAutoSARClassifierEqualityValidation {
	private final String AutoSARModelName;
	private final Consumer<Consumer<View>> viewExecutor;

	public SimuLinkAutoSARClassifierEqualityValidation(String AutoSARModelName, Consumer<Consumer<View>> viewExecutor) {
		this.AutoSARModelName = AutoSARModelName;
		this.viewExecutor = viewExecutor;
	}
	
	public void assertBlockWithNameInRootModel(String blockName) {
		assertElementWithName(Block.class, SwComponent.class, blockName);
	}
	
	public void assertSubSystemtWithNameInRootModel(String compositeComponentName){
		assertElementWithName(SubSystem.class, CompositeSwComponent.class, compositeComponentName);
	}
	
	public void assertPortsInBlockOrComponent(String blockName){
		assertElementWithName(Block.class, SwComponent.class, blockName);
	}
	
	public void assertNoPortWithNameInComponent(String blockName, String portName){
		assertNoPortWithNameInComponentOrBlock(blockName, portName);
	}
	
	public void assertSwComponentWithNameInRootModel(String swComponentName){
		assertElementWithName(Block.class, SwComponent.class, swComponentName);
	}
	
	public void assertCompositeSwComponentWithNameInRootModel(String compositeComponentName){
		assertElementWithName(SubSystem.class, CompositeSwComponent.class, compositeComponentName);
	}

	/*
	 * Generic method to assert that no SimuLinkElement or AutoSAR Element exists
	 * with elementName.
	 */
	public void assertNoElementWithNameInRootModel(String elememtName){
viewExecutor.accept((view) -> {
			assertThat("no Element in AutoSAR model with name " + elememtName + " is expected to exist",
				AutoSARQueryUtil.claimAutoSARModel(view, AutoSARModelName)
                        .getSwcomponent()
						.stream().filter(component -> component.getName().equals(elememtName))
						.toList(),
				is(Collections.EMPTY_LIST)
			);
			assertThat("no Element in SimuLink model with name " + elememtName + " is expected to exist",
					SimuLinkQueryUtil.claimSimuLinkModel(view, AutoSARModelName)
							.getContains()
							.stream().filter(block -> block.getName().equals(elememtName))
							.toList(),
					is(Collections.EMPTY_LIST)
			);
		});

	}

	private  void  assertNoPortWithNameInComponentOrBlock(String blockName, String portName) {
		viewExecutor.accept((view) -> {
			assertThat("no Port in AutoSAR Component with name " + portName + " is expected to exist,",
				AutoSARQueryUtil.claimAutoSARModel(view, AutoSARModelName)
					.getSwcomponent()
					.stream().filter(swComponent -> swComponent.getName().equals(blockName))
					.flatMap(swComponent -> swComponent.getPort().stream())
					.filter(port -> port.getName().equals(portName))
					.toList(),
				is(Collections.emptyList())
			);
			assertThat("no SimuLink Element with name " + portName + " is expected to exist",
				SimuLinkQueryUtil.claimSimuLinkModel(view, AutoSARModelName)
					.getContains()
					.stream().filter(block -> block.getName().equals(blockName))
					.flatMap(block -> block.getPorts().stream())
					.filter(port -> port.getName().equals(portName))
					.toList(),
				is(Collections.emptyList()));
		});
	}

	
	
	/*
	 * Generic method to assert that a SimuLinkElement and an AutoSARElement are equal.
	 */
	private void assertElementWithName(
		Class <? extends SimulinkElement> simulinkElement,
		Class <? extends AutoSARElement> autoSARElement,
		String name) {
		viewExecutor.accept((view) -> {
			var SimulinkElement = claimSimuLinkElement(view, simulinkElement, name);
			var AutoSARElement = claimAutoSARElement(view, autoSARElement, name);
			assertElementEquals(SimulinkElement, AutoSARElement);
		});
	}
	
	
	public static  void assertElementEquals(Block block, SwComponent component){
		if (block instanceof SubSystem subSystem) {
			assertSimuLinkSubsystemEqualsAutoSARCompositeComponent(subSystem, (CompositeSwComponent) component);
		}
		else {
			assertSimuLinkBlockEqualsAutoSARSwComponent(block, (AtomicSwComponent) component);
		}

	}

	private static void assertSimuLinkBlockEqualsAutoSARSwComponent(Block simulinkElement, SwComponent autoSARElement){
		assertEquals(simulinkElement.getName(), autoSARElement.getName());
		assertSamePortsInElement(simulinkElement, autoSARElement);
	}	
		
	private static void assertSimuLinkSubsystemEqualsAutoSARCompositeComponent(SubSystem subsystem, CompositeSwComponent compositeComponent){
		assertEquals(subsystem.getName(), compositeComponent.getName());
		assertSameSubBlocksOrComponents(subsystem,compositeComponent);
	}
	
	private static void assertSameSubBlocksOrComponents(SubSystem subsystem, CompositeSwComponent compositeComponent){
		var simuLinkBlocks = subsystem.getSubBlocks();
		var autoSARComponents = compositeComponent.getAtomicswcomponent();
		assertEquals(autoSARComponents.size(), simuLinkBlocks.size());
		
		for (var simuLinkBlock : simuLinkBlocks){
			var autoSARComponent = claimAutoSARBlockinComposite(compositeComponent, simuLinkBlock.getName());
			assertSimuLinkBlockEqualsAutoSARSwComponent(simuLinkBlock, autoSARComponent);
		}
	}

	private static void assertSamePortsInElement(Block simulinkElement, SwComponent autoSARElement){
		var simulinkPorts = simulinkElement.getPorts();
		var autosarPorts = autoSARElement.getPort();
		
		assertEquals(simulinkPorts.size(), autosarPorts.size());
		
		for (var simulinkPort : simulinkPorts){
			var autosarPort = claimAutoSARPort(autoSARElement, simulinkPort.getName());
			if (simulinkPort instanceof InPort inPort){
                assertInstanceOf(RequiredPort.class, autosarPort);
				assertSimuLinkPortEqualsAutoSARSwPort(inPort, (RequiredPort) autosarPort);
			}
			else if (simulinkPort instanceof OutPort){
                assertInstanceOf(ProvidedPort.class, autosarPort);
			}
		}
	}
	
	private static void assertSimuLinkPortEqualsAutoSARSwPort(InPort inPort, RequiredPort requiredPort){
		assertEquals(inPort.getName(), requiredPort.getName());
	}
		
	public void assertDelegationSwConnectorEqualsSingleConnection(String ConnectionName, 
		String compositeComponentName, String addedOutPortBlockName, String atomicComponentName
	){
		viewExecutor.accept((view) -> {
			var compositeComponent = (CompositeSwComponent)
				claimAutoSARElement(view, CompositeSwComponent.class, compositeComponentName);
			
			var delegationSwConnector = (DelegationSwConnector) (compositeComponent
				.getSwconnector()
				.stream()
				.filter(connector -> connector.getName().equals(ConnectionName))
				.findFirst()
				.get());

			var singleConnection = (SingleConnection) claimSimuLinkConnection(view, ConnectionName);
			var subsystem = (SubSystem) claimSimuLinkElement(view, SubSystem.class, compositeComponentName);
			var addedOutPortBlock = claimSimuLinkBlockOfSubsystem(subsystem, addedOutPortBlockName);
			
			
			assertEquals(singleConnection.getName(), delegationSwConnector.getName());
			assertEquals(singleConnection.getOutport().getName(), delegationSwConnector.getInnerPort().getName());
			assertEquals(singleConnection.getInport().getName(),
				claimSimuLinkPort(addedOutPortBlock,singleConnection.getInport().getName()).getName());
			
		});
	}

	public void assertAssemblySwConnectorEqualsSingleConnection(String connectionName, String compositeConnectionName) {
		viewExecutor.accept((view -> {
			var compositeComponent = (CompositeSwComponent)
				claimAutoSARElement(view, CompositeSwComponent.class, compositeConnectionName);
			var assemblySwConnector = compositeComponent.getSwconnector()
				.stream().filter(swConnector -> swConnector.getName().equals(connectionName))
				.map(AssemblySwConnector.class::cast)
				.findFirst()
				.get();
			var singleConnection = (SingleConnection) claimSimuLinkConnection(view, connectionName);

			assertEquals(singleConnection.getName(), assemblySwConnector.getName());

			assertEquals(
				singleConnection.getOutport().getName(),
				assemblySwConnector.getProvidedport().get(0).getName()
			);
			assertEquals(
				singleConnection.getInport().getName(),
				assemblySwConnector.getRequiredport().get(0).getName()
			);
		}));

	}
}
