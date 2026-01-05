package tools.vitruv.methodologisttemplate.vsum.simulinkautosar.autosar2simulink;

import edu.kit.ipd.sdq.metamodels.autosar.AutoSARFactory;
import edu.kit.ipd.sdq.metamodels.autosar.AutoSARModel;
import org.junit.jupiter.api.BeforeEach;
import tools.vitruv.methodologisttemplate.vsum.simulinkautosar.SimuLinkAutoSARTransformationTest;
import edu.kit.ipd.sdq.metamodels.autosar.SwComponent;

import java.util.function.Consumer;

abstract class AbstractAutoSARToSimuLinkTest extends SimuLinkAutoSARTransformationTest {

	@BeforeEach
	protected void setup() {
		createAutoSARModel((model) -> {
			model.setName(AUTOSAR_MODEL_NAME);
		});
	}

	protected void createAutoSARModel(Consumer<AutoSARModel> autoSARModelInitialization) {
		viewFactory.changeAutoSARView((view) -> {
			var autosarModel = AutoSARFactory.eINSTANCE.createAutoSARModel();
			createAndRegisterRoot(view, autosarModel, getUri(getProjectModelPath(AUTOSAR_MODEL_NAME)));
			autoSARModelInitialization.accept(autosarModel);
		});
	}

	protected void changeAutoSARModel(Consumer<AutoSARModel> modelModification) {
		viewFactory.changeAutoSARView((view) ->
			modelModification.accept(getDefaultAutoSARModel(view))
		);
	}
	
	private void addComponentToRootModel(SwComponent newComponent, String name) {
		changeAutoSARModel((autoSARModel) -> {
			newComponent.setName(name);
			autoSARModel.getSwcomponent().add(newComponent);
		});
	}
	
	
	protected void createAtomicSWComponentInModel(String componentName) {
		addComponentToRootModel(AutoSARFactory.eINSTANCE.createAtomicSwComponent(), componentName);
		validation.assertSwComponentWithNameInRootModel(componentName);
	}
	
	
	protected void createCompositeSWComponentInModel(String componentName){
		addComponentToRootModel(AutoSARFactory.eINSTANCE.createCompositeSwComponent(), componentName);
	}
}
