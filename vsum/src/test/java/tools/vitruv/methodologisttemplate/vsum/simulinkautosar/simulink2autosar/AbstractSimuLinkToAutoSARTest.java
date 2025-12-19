package tools.vitruv.methodologisttemplate.vsum.simulinkautosar.simulink2autosar;

import java.util.function.Consumer;

import org.junit.jupiter.api.BeforeEach;
import edu.kit.ipd.sdq.metamodels.simulink.Block;
import edu.kit.ipd.sdq.metamodels.simulink.Connection;
import edu.kit.ipd.sdq.metamodels.simulink.SimuLinkFactory;
import edu.kit.ipd.sdq.metamodels.simulink.SimulinkModel;
import tools.vitruv.methodologisttemplate.vsum.simulinkautosar.SimuLinkAutoSARTransformationTest;

abstract class AbstractSimuLinkToAutoSARTest extends SimuLinkAutoSARTransformationTest {

	@BeforeEach
	protected void setup() {
		createSimuLinkModel((model) -> {
			model.setName(SIMULINK_MODEL_NAME);
		});
	}

	protected void createSimuLinkModel(Consumer<SimulinkModel> simuLinkModelInitialization) {
		viewFactory.changeSimuLinkView((view) -> {
			var simuLinkModel = SimuLinkFactory.eINSTANCE.createSimulinkModel();
			simuLinkModelInitialization.accept(simuLinkModel);
			createAndRegisterRoot(view, simuLinkModel,
				getUri(getProjectModelPath(SIMULINK_MODEL_NAME)));
		});
	}

	protected void changeSimuLinkModel(Consumer<SimulinkModel> modelModification) {
		viewFactory.changeSimuLinkView((view) ->
			modelModification.accept(getDefaultSimuLinkModel(view))
		);
	}
	
	private void addBlockToRootModel(Block newBlock, String name) {
		changeSimuLinkModel((model) -> {
			newBlock.setName(name);
			model.getContains().add(newBlock);
		});
	}

	protected void createBlockInModel(String blockName) {
		addBlockToRootModel(SimuLinkFactory.eINSTANCE.createBlock(), blockName);
		validation.assertBlockWithNameInRootModel(blockName);
	}
	
	protected void createOutPortBlockinModel(String blockName){
		addBlockToRootModel(SimuLinkFactory.eINSTANCE.createOutPortBlock(), blockName);
		validation.assertBlockWithNameInRootModel(blockName);
	}
	
	protected void createSubsystemInModel(String subSystemName) {
		addBlockToRootModel(SimuLinkFactory.eINSTANCE.createSubSystem(), subSystemName);
		validation.assertBlockWithNameInRootModel(subSystemName);
	}
	
	protected void createSingleConnectionInModel(String name){
		changeSimuLinkModel((model) -> {
			Connection singleConnection = SimuLinkFactory.eINSTANCE.createSingleConnection();
			singleConnection.setName(name);
			model.getConnection().add(singleConnection);
		});
	}
}