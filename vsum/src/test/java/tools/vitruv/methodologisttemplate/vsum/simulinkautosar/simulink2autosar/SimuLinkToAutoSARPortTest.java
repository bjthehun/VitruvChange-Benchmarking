package tools.vitruv.methodologisttemplate.vsum.simulinkautosar.simulink2autosar;

import org.junit.jupiter.api.RepeatedTest;
import static tools.vitruv.methodologisttemplate.vsum.simulinkautosar.util.SimuLinkQueryUtil.*;
import org.eclipse.emf.ecore.util.EcoreUtil;
import edu.kit.ipd.sdq.metamodels.simulink.SubSystem;
import edu.kit.ipd.sdq.metamodels.simulink.SimuLinkFactory;
import edu.kit.ipd.sdq.metamodels.simulink.Block;
import edu.kit.ipd.sdq.metamodels.simulink.InPort;
import edu.kit.ipd.sdq.metamodels.simulink.OutPort;
import edu.kit.ipd.sdq.metamodels.simulink.OutPortBlock;
import edu.kit.ipd.sdq.metamodels.simulink.SingleConnection;
import tools.vitruv.methodologisttemplate.vsum.observers.VitruvChangeTimingExtension;

class SimuLinkToAutoSARPortTest extends AbstractSimuLinkToAutoSARTest {
	
	public static final String DEFAULT_BLOCK_NAME = "Testblock";
	public static final String DEFAULT_BLOCK_NAME_TWO = "Testblock2";
	public static final String DEFAULT_SUBSYSTEM_NAME = "TestSubsystem";
	public static final String DEFAULT_INPORT_NAME = "TestPort";
	public static final String DEFAULT_OUTPORT_NAME = "TestOutPort";
	public static final String DEFAULT_SUBSYSTEM_OUTPORT_NAME = "SubSystemOutport";
	public static final String DEFAULT_SUBSYSTEM_INPORT_NAME = "SubSystemInport";
	public static final String DEFAULT_OUTPORT_BLOCK_NAME = "OutPortBlock";
	public static final String DEFAULT_SINGLECONNECTION_NAME = "SingleConnection";

	/*
	 * Tests for Ports
	 */
	@RepeatedTest(VitruvChangeTimingExtension.MEASUREMENT_RUNS)
	void testCreateInPortinBlock() {
		createBlockInModel(DEFAULT_BLOCK_NAME);
		viewFactory.changeSimuLinkView((view) -> {
			Block block = claimSimuLinkBlock(view, DEFAULT_BLOCK_NAME);
			InPort inport = SimuLinkFactory.eINSTANCE.createInPort();
			inport.setName(DEFAULT_INPORT_NAME);
			block.getPorts().add(inport);
		});
		validation.assertPortsInBlockOrComponent(DEFAULT_BLOCK_NAME);
	}

	@RepeatedTest(VitruvChangeTimingExtension.MEASUREMENT_RUNS)
	void testCreateOutPortinBlock() {
		createBlockInModel(DEFAULT_BLOCK_NAME);
		viewFactory.changeSimuLinkView((view) -> {
			Block block = claimSimuLinkBlock(view, DEFAULT_BLOCK_NAME);
			OutPort outport = SimuLinkFactory.eINSTANCE.createOutPort();
			outport.setName(DEFAULT_OUTPORT_NAME);
			block.getPorts().add(outport);
		});
		validation.assertPortsInBlockOrComponent(DEFAULT_BLOCK_NAME);
	}
	
	
	@RepeatedTest(VitruvChangeTimingExtension.MEASUREMENT_RUNS)
	void testCreateInPortinSubSystem() {
		createSubsystemInModel(DEFAULT_SUBSYSTEM_NAME);
		viewFactory.changeSimuLinkView((view) -> {
			Block subsystem = claimSimuLinkBlock(view, DEFAULT_SUBSYSTEM_NAME);
			InPort inport = SimuLinkFactory.eINSTANCE.createInPort();
			inport.setName(DEFAULT_INPORT_NAME);
			subsystem.getPorts().add(inport);
		});
		validation.assertPortsInBlockOrComponent(DEFAULT_SUBSYSTEM_NAME);
	}
	
	@RepeatedTest(VitruvChangeTimingExtension.MEASUREMENT_RUNS)
	void testCreateOutPortinSubSystem() {
		createSubsystemInModel(DEFAULT_SUBSYSTEM_NAME);
		viewFactory.changeSimuLinkView((view) -> {
			Block subsystem = claimSimuLinkBlock(view, DEFAULT_SUBSYSTEM_NAME);
			OutPort outport = SimuLinkFactory.eINSTANCE.createOutPort();
			outport.setName(DEFAULT_OUTPORT_NAME);
			subsystem.getPorts().add(outport);
		});
		validation.assertPortsInBlockOrComponent(DEFAULT_SUBSYSTEM_NAME);
	}
	
	@RepeatedTest(VitruvChangeTimingExtension.MEASUREMENT_RUNS)
	void testDeleteInPortinBlock() {
		testCreateInPortinBlock();
		viewFactory.changeSimuLinkView((view) -> {
			Block block = claimSimuLinkBlock(view, DEFAULT_BLOCK_NAME);
			EcoreUtil.remove(claimSimuLinkPort(block, DEFAULT_INPORT_NAME));
		});
		validation.assertNoPortWithNameInComponent(DEFAULT_BLOCK_NAME, DEFAULT_INPORT_NAME);
	}
	
	@RepeatedTest(VitruvChangeTimingExtension.MEASUREMENT_RUNS)
	void testDeleteOutPortinSwComponent() {
		testCreateOutPortinBlock();
		viewFactory.changeSimuLinkView((view) -> {
			Block block = claimSimuLinkBlock(view, DEFAULT_BLOCK_NAME);
			EcoreUtil.remove(claimSimuLinkPort(block, DEFAULT_OUTPORT_NAME));
		});
		validation.assertNoPortWithNameInComponent(DEFAULT_BLOCK_NAME, DEFAULT_OUTPORT_NAME);
	}
	
	/**
	 * Test of Connections
	 */
	@RepeatedTest(VitruvChangeTimingExtension.MEASUREMENT_RUNS)
	void testCreateSingleConnectionFromBlockToOutPortBlock(){
		createSubsystemInModel(DEFAULT_SUBSYSTEM_NAME);
	 	createBlockInModel(DEFAULT_BLOCK_NAME);
	 	createOutPortBlockinModel(DEFAULT_OUTPORT_BLOCK_NAME);
	 	createSingleConnectionInModel(DEFAULT_SINGLECONNECTION_NAME);
		viewFactory.changeSimuLinkView((view) -> {
	 		Block block = claimSimuLinkBlock(view, DEFAULT_BLOCK_NAME);
	 		OutPort from = SimuLinkFactory.eINSTANCE.createOutPort();
	 		from.setName(DEFAULT_OUTPORT_NAME);
	 		block.getPorts().add(from);

	 		SubSystem subSystem = (SubSystem) claimSimuLinkBlock(view, DEFAULT_SUBSYSTEM_NAME);
			subSystem.getSubBlocks().add(block);

			OutPortBlock outPortBlock = (OutPortBlock) claimSimuLinkBlock(view, DEFAULT_OUTPORT_BLOCK_NAME);
			OutPort outPort = SimuLinkFactory.eINSTANCE.createOutPort();
			outPort.setName(DEFAULT_SUBSYSTEM_OUTPORT_NAME);
			outPortBlock.getPorts().add(outPort);

			InPort to = SimuLinkFactory.eINSTANCE.createInPort();
			to.setName(DEFAULT_SUBSYSTEM_INPORT_NAME);
			outPortBlock.getPorts().add(to);

			subSystem.getSubBlocks().add(outPortBlock);
			// inport needs to be added first because the reaction
			// needs this port to know where the corresponding AutoSAR Typ needs to be contained
			SingleConnection singleConnection = (SingleConnection) claimSimuLinkConnection(view, DEFAULT_SINGLECONNECTION_NAME);
			singleConnection.setInport(to);
			singleConnection.setOutport(from);
	 	});

		validation.assertDelegationSwConnectorEqualsSingleConnection(
			DEFAULT_SINGLECONNECTION_NAME,
			DEFAULT_SUBSYSTEM_NAME,
			DEFAULT_OUTPORT_BLOCK_NAME,
			DEFAULT_BLOCK_NAME);
	}
	
	@RepeatedTest(VitruvChangeTimingExtension.MEASUREMENT_RUNS)
	void testCreateSingleConnectionBetweenTwoBlocks(){
		createSubsystemInModel(DEFAULT_SUBSYSTEM_NAME);
		createBlockInModel(DEFAULT_BLOCK_NAME);
	 	createBlockInModel(DEFAULT_BLOCK_NAME_TWO);
	 	createSingleConnectionInModel(DEFAULT_SINGLECONNECTION_NAME);
	 	viewFactory.changeSimuLinkView((view) -> {
	 		var block = claimSimuLinkBlock(view, DEFAULT_BLOCK_NAME);
	 		var from = SimuLinkFactory.eINSTANCE.createOutPort();
	 		block.getPorts().add(from);
			var subsystem = (SubSystem) claimSimuLinkBlock(view, DEFAULT_SUBSYSTEM_NAME);
			subsystem.getSubBlocks().add(block);

	 		var block2 = claimSimuLinkBlock(view, DEFAULT_BLOCK_NAME_TWO);
	 		var to = SimuLinkFactory.eINSTANCE.createInPort();
			to.setName(DEFAULT_INPORT_NAME);
			block2.getPorts().add(to);
			subsystem.getSubBlocks().add(block2);

			// inport needs to be added first because the reaction
	 		// needs this port to know where the corresponding AutoSAR Typ needs to be contained
			SingleConnection connect = (SingleConnection) claimSimuLinkConnection(view, DEFAULT_SINGLECONNECTION_NAME);
			connect.setInport(to);
			connect.setOutport(from);
		});
		validation.
			assertAssemblySwConnectorEqualsSingleConnection(DEFAULT_SINGLECONNECTION_NAME, DEFAULT_SUBSYSTEM_NAME);
	}
}