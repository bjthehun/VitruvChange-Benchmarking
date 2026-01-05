package tools.vitruv.methodologisttemplate.vsum.simulinkautosar.simulink2autosar;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import static tools.vitruv.methodologisttemplate.vsum.simulinkautosar.util.SimuLinkQueryUtil.*;
import org.eclipse.emf.ecore.util.EcoreUtil;
import edu.kit.ipd.sdq.metamodels.simulink.SubSystem;
import tools.vitruv.methodologisttemplate.vsum.observers.VitruvChangeTimingExtension;

public class SimuLinkToAutoSARBlockTest extends AbstractSimuLinkToAutoSARTest {
	private static final String DEFAULT_BLOCK_NAME = "Testblock";
	private static final String DEFAULT_SUBSYSTEM_NAME = "TestSubsystem";
	
	/*
	 * Tests for Blocks and Subsystems
	 */
	
	@RepeatedTest(VitruvChangeTimingExtension.MEASUREMENT_RUNS)
	void testCreateBlock() {
		createBlockInModel(DEFAULT_BLOCK_NAME);
		validation.assertBlockWithNameInRootModel(DEFAULT_BLOCK_NAME);
	}
	
	@RepeatedTest(VitruvChangeTimingExtension.MEASUREMENT_RUNS)
	void testDeleteBlock(){
		createBlockInModel(DEFAULT_BLOCK_NAME);
		viewFactory.changeSimuLinkView((view) -> {
			EcoreUtil.remove(claimSimuLinkBlock(view, DEFAULT_BLOCK_NAME));
		});
		validation.assertNoElementWithNameInRootModel(DEFAULT_BLOCK_NAME);
	}
	
	@RepeatedTest(VitruvChangeTimingExtension.MEASUREMENT_RUNS)
	void testCreateSubsystem() {
		createSubsystemInModel(DEFAULT_SUBSYSTEM_NAME);
		createBlockInModel(DEFAULT_BLOCK_NAME);
		viewFactory.changeSimuLinkView((view) -> {
			var block = claimSimuLinkBlock(view, DEFAULT_BLOCK_NAME);
			var subsystem = (SubSystem) claimSimuLinkBlock(view, DEFAULT_SUBSYSTEM_NAME);
			subsystem.getSubBlocks().add(block);
		});

		validation.assertSubSystemtWithNameInRootModel(DEFAULT_SUBSYSTEM_NAME);
	}
	
	@RepeatedTest(VitruvChangeTimingExtension.MEASUREMENT_RUNS)
	void testDeleteSubsystem(){
		createSubsystemInModel(DEFAULT_SUBSYSTEM_NAME);
		viewFactory.changeSimuLinkView((view) -> {
			EcoreUtil.remove((SubSystem) claimSimuLinkBlock(view, DEFAULT_SUBSYSTEM_NAME));
		});
		validation.assertNoElementWithNameInRootModel(DEFAULT_SUBSYSTEM_NAME);
	}
	
	
	@RepeatedTest(VitruvChangeTimingExtension.MEASUREMENT_RUNS)
	void testSubSystemAndCheckContainedBlocks() {
		createSubsystemInModel(DEFAULT_SUBSYSTEM_NAME);
		createBlockInModel(DEFAULT_BLOCK_NAME);

		viewFactory.changeSimuLinkView((view) -> {
			var block = claimSimuLinkBlock(view, DEFAULT_BLOCK_NAME);
			var subsystem = (SubSystem) claimSimuLinkBlock(view, DEFAULT_SUBSYSTEM_NAME);
			subsystem.getSubBlocks().add(block);
		});
		viewFactory.changeSimuLinkView((view) -> {
			EcoreUtil.remove((SubSystem) claimSimuLinkBlock(view, DEFAULT_SUBSYSTEM_NAME));
		});
		validation.assertNoElementWithNameInRootModel(DEFAULT_SUBSYSTEM_NAME);
		validation.assertNoElementWithNameInRootModel(DEFAULT_BLOCK_NAME);
	}
}