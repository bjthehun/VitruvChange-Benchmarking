package tools.vitruv.methodologisttemplate.vsum.simulinkautosar.util;

import edu.kit.ipd.sdq.activextendannotations.Utility;
import edu.kit.ipd.sdq.metamodels.simulink.SimulinkModel;
import edu.kit.ipd.sdq.metamodels.simulink.SimulinkElement;
import edu.kit.ipd.sdq.metamodels.simulink.SimuLinkPackage;
import tools.vitruv.framework.views.View;
import java.util.List;
import org.eclipse.emf.ecore.EClassifier;
import static edu.kit.ipd.sdq.commons.util.java.lang.IterableUtil.claimOne;

import edu.kit.ipd.sdq.metamodels.simulink.Block;
import edu.kit.ipd.sdq.metamodels.simulink.Connection;
import edu.kit.ipd.sdq.metamodels.simulink.Port;
import edu.kit.ipd.sdq.metamodels.simulink.SubSystem;

/*
 * Utility Class for Simulink.
 * Contains Methods to claim and find SimuLinkElements of different types.
 */
@Utility
public class SimuLinkQueryUtil {
	
	/*
	 * Returns a SimuLinkModel filtered by name in the RootObjects of the view.
	 * If no model with the name is found an error is thrown.
	 */
	public static SimulinkModel claimSimuLinkModel(View view, String modelname) {
		return view.getRootObjects(SimulinkModel.class)
			.stream()
			.filter(it -> it.getName().equals(modelname))
			.findFirst()
			.get();
	}
	
	public static List<EClassifier> getSimuLinkClassifiers(View view) {
		return view.getRootObjects(SimuLinkPackage.class)
			.stream()
			.flatMap(it -> it.getEClassifiers().stream())
			.toList();
	}
	
	
	public static <T extends SimulinkElement> List<T> getSimuLinkElementsOfType(View view, Class<T> type) {
		return getSimuLinkClassifiers(view)
			.stream()
			.filter(classifier -> type.isInstance(classifier))
			.map(element -> type.cast(element))
			.toList();
	}
	
	public static List<Block> getBlocksOfModel(View view, String elementName){
		return view.getRootObjects(SimulinkModel.class).stream()
			.flatMap(model -> model.getContains().stream())
			.toList();
	}
	
	public static Block claimSimuLinkBlock(View view, String blockName){
		return claimSimuLinkElement(view, Block.class, blockName);
	}
	
	
	public static <T extends SimulinkElement> Block claimSimuLinkElement(View view, Class<T> simuLinkType,
		String elementName) {
		return getBlocksOfModel(view,elementName)
			.stream()
			.filter(it -> it.getName().equals(elementName))
			.findFirst()
			.get();
	}
	
	
	public static Block claimSimuLinkBlockOfSubsystem(SubSystem subsystem, String blockname){
		return subsystem.getSubBlocks()
			.stream()
			.filter(it -> it.getName().equals(blockname))
			.findFirst()
			.get();
	}
	
	public static Port claimSimuLinkPort(Block block, String portName){
		return block.getPorts()
			.stream()
			.filter(it -> it.getName().equals(portName))
			.findFirst()
			.get();
	}
	
	public static Connection claimSimuLinkConnection(View view, String connectionName){
		return view.getRootObjects(SimulinkModel.class)
			.stream()
			.flatMap(root -> root.getConnection().stream())
			.filter(conn -> conn.getName().equals(connectionName))
			.findFirst()
			.get();
	}
	
}
