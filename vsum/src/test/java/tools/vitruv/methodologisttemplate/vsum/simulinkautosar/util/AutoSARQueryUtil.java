package tools.vitruv.methodologisttemplate.vsum.simulinkautosar.util;

import edu.kit.ipd.sdq.metamodels.autosar.AutoSARElement;
import edu.kit.ipd.sdq.metamodels.autosar.AutoSARModel;
import edu.kit.ipd.sdq.metamodels.autosar.Port;
import edu.kit.ipd.sdq.metamodels.autosar.SwComponent;
import edu.kit.ipd.sdq.activextendannotations.Utility;
import tools.vitruv.framework.views.View;

import edu.kit.ipd.sdq.metamodels.autosar.CompositeSwComponent;
import edu.kit.ipd.sdq.metamodels.autosar.AtomicSwComponent;


/*
 * 
 * Utility Class for AutoSAR 
 * Contains Methods to claim and find AutoSARElements of different types
 * The AutoSAR Element was added to the meta model for easier handling and derivation of the name attribute to all AutoSARElements 
 * 
 */


@Utility
public final class AutoSARQueryUtil {
	private AutoSARQueryUtil() {}
	
	public static AutoSARModel claimAutoSARModel(View view, String name) {
		return view.getRootObjects(AutoSARModel.class)
			.stream()
			.filter(model -> model.getName().equals(name))
			.findFirst()
			.get();
	}
	
	public static <S extends AutoSARElement> SwComponent claimAutoSARElement(View view, Class<S> element,
		String elementName) {
		return view.getRootObjects(AutoSARModel.class)
			.stream()
			.flatMap(model -> model.getSwcomponent().stream())
			.filter(component -> component.getName().equals(elementName))
			.findFirst()
			.get();
	}
	
	public static  <T extends AutoSARElement> CompositeSwComponent claimAutoSARCompositeSwComponent(View view, Class<T> element,
		String elementName) {
		return (CompositeSwComponent) claimAutoSARElement(view, element, elementName);		
	}
	
	public static AtomicSwComponent claimAutoSARBlockinComposite(CompositeSwComponent compositeComponent, String atomicComponentName){
		return compositeComponent.getAtomicswcomponent()
			.stream()
			.filter(component -> component.getName().equals(atomicComponentName))
			.findFirst()
			.get();
	}
	
	public static Port claimAutoSARPort(SwComponent swComponent, String portname){
		return swComponent.getPort()
			.stream()
			.filter(port -> port.getName().equals(portname))
			.findFirst()
			.get();
	}
}
