package tools.vitruv.methodologisttemplate.vsum.simulinkautosar.util;

import edu.kit.ipd.sdq.metamodels.autosar.AutoSARModel;
import edu.kit.ipd.sdq.metamodels.simulink.SimulinkModel;
import lombok.AllArgsConstructor;
import java.util.List;
import java.util.function.Consumer;

import tools.vitruv.framework.views.CommittableView;
import tools.vitruv.framework.views.View;
import tools.vitruv.framework.views.ViewProvider;
import tools.vitruv.framework.testutils.integration.TestViewFactory;


public class SimuLinkAutoSARViewFactory extends TestViewFactory {
	public SimuLinkAutoSARViewFactory(ViewProvider viewProvider) {
		super(viewProvider);
	}

	private View createAutoSARView() {
		return createViewOfElements("AutoSAR", List.of(AutoSARModel.class));
	}

	private View createSimuLinkView() {
		return createViewOfElements("SimuLink",List.of(SimulinkModel.class));
	}

	private View createAutoSARAndSimuLinkModelView() {
		return createViewOfElements("AutoSAR and SimuLink classes", 
			List.of(AutoSARModel.class, SimulinkModel.class)
		);
	}


	/**
	 * Changes the AutoSAR view containing all AutoSAR models as root elements 
	 * according to the given modification function. 
	 * Records the performed changes, commits the recorded changes, and closes the view afterwards.
	 */
	public void changeAutoSARView(Consumer<View> modelModification) {
		try {
			changeViewRecordingChanges(createAutoSARView(), modelModification::accept);
		} catch (Exception e) {
			return;
		}
	}

	/**
	 * Changes the SimuLink view containing all SimuLink packages and classes as root elements 
	 * according to the given modification function. 
	 * Records the performed changes, commits the recorded changes, and closes the view afterwards.
	 */
	public void changeSimuLinkView(Consumer<View> modelModification) {
		try {
			changeViewRecordingChanges(createSimuLinkView(), modelModification::accept);
		} catch (Exception e) {
			return;
		}
	}

	/**
	 * Validates the AutoSAR view containing all AutoSAR models by applying the validation function
	 * and closes the view afterwards.
	 */
	public void validateAutoSARView(Consumer<View> viewValidation) {
		try {
			validateView(createAutoSARView(), viewValidation);
		} catch (Exception e) {
			return;
		}
	}

	/**
	 * Validates the SimuLink view containing all packages and classes by applying the validation function
	 * and closes the view afterwards.
	 */
	public void validateSimuLinkView(Consumer<View> viewValidation) {
		try {
			validateView(createSimuLinkView(), viewValidation);
		} catch (Exception e) {
			return;
		}
	}

	/**
	 * Validates the SimuLink and AutoSAR view containing all AutoSAR models and SimuLink models by applying the 
	 * validation function and closes the view afterwards.
	 */
	public void validateAutoSARAndSimuLinkClassesView(Consumer<View> viewValidation) {
		try {
			validateView(createAutoSARAndSimuLinkModelView(), viewValidation);
		} catch (Exception e) {
			return;
		}
	}

	
}
