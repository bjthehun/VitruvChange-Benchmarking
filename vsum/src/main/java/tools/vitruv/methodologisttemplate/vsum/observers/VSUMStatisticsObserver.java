package tools.vitruv.methodologisttemplate.vsum.observers;

import org.eclipse.emf.compare.internal.conflict.AttributeChangeConflictSearch.Change;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;

import com.google.common.collect.Sets;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import tools.vitruv.change.propagation.ChangePropagationSpecification;
import tools.vitruv.change.propagation.impl.CompositeChangePropagationSpecification;
import tools.vitruv.dsls.reactions.runtime.reactions.AbstractReactionsChangePropagationSpecification;
import tools.vitruv.dsls.reactions.runtime.reactions.Reaction;
import tools.vitruv.framework.vsum.internal.VirtualModelImpl;

public class VSUMStatisticsObserver {
    private final VirtualModelImpl vsum;

    public VSUMStatisticsObserver(VirtualModelImpl vsum) {
        this.vsum = vsum;
    }

    public void createStatisticsFile(String path) {

    }

    public void writeVSUMStatistics(String path) throws IOException {
        var viewSourceModels = vsum.getViewSourceModels();
        // Number of Models
        long noOfModels = viewSourceModels.size();

        // Number of Resources
        long noOfResources = 0;
        for (var model: viewSourceModels) {
            var iterator = model.getAllContents();
            while (iterator.hasNext()) {
                iterator.next();
                noOfResources++;
            }
        }

        // Number of CPRs
        long noOfCPRs = getReactionsOf(vsum.getChangePropagationSpecifications()).size();
        // Number of affected metamodel elements
        long noOfAffectedMetamodelElements =
            getReactionsOf(vsum.getChangePropagationSpecifications())
            .stream()
            .map(reaction -> {
                System.out.println(reaction.getMatchingMetamodelElement());
                return reaction.getMatchingMetamodelElement();
            })
            .distinct()
            .count();
    
        // Number of metamodel elements
        // Assumption is for each relevant metamodel, a model exists
        var ePackagesOfModels = viewSourceModels
            .stream()
            .flatMap(model -> model.getContents().stream())
            .map(eObject -> eObject.eClass().getEPackage())
            .distinct()
            .toList();
        var metamodelElementCount = ePackagesOfModels
            .stream()
            .map(ePackage -> countMetamodelElements(ePackage))
            .reduce(0L, (a, b) -> a + b);

        // Write to File
        try (var writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path)))) {
            writer.write("NoOfModels, NoOfResources, NoOfCPRs, NoOfAffectedMetamodelElements, NoOfAllMetamodelElements");
            writer.newLine();
            writer.write(noOfModels + ", " + noOfResources + ", " + noOfCPRs + ", " + noOfAffectedMetamodelElements + ", " + metamodelElementCount);
            writer.newLine();
            writer.flush();
        }
    }

    private Set<Reaction> getReactionsOf(Collection<ChangePropagationSpecification> allCPS) {
        return allCPS
            .stream()
            .map(cps -> getReactionsOf(cps))
            .reduce(Set.<Reaction>of(), (set1, set2) -> Sets.union(set1, set2));
    }

    private Set<Reaction> getReactionsOf(ChangePropagationSpecification specification) {
        if (specification instanceof CompositeChangePropagationSpecification composite) {
            return composite.getAllProcessors()
                .stream()
                .map(specification2 -> getReactionsOf(specification2))
                .reduce(Set.<Reaction>of(),
                     (set1, set2) -> Sets.union(set1, set2));
        }
        else {
            AbstractReactionsChangePropagationSpecification reactionSpec 
                = (AbstractReactionsChangePropagationSpecification) specification;
            return new HashSet<>(reactionSpec.getReactions());
        }
    }

    /**
     * Counts the number of metamodel elements ({@link EClass}es, {@link EAttribute}s, and {@link EReference}s)
     * that are contained in the metaclasses of <code>package</code>, and its subpackages.
     * Inherited EAttributes and EReferences are counted as well.
     * 
     * @param package - {@link EPackage}
     * @return long
     */
    private long countMetamodelElements(EPackage ePackage) {
        // Count in subpackages
        long subpackageCount = ePackage.getESubpackages()
            .stream()
            .map(subPackage -> countMetamodelElements(subPackage))
            .reduce(0L, (a, b) -> a + b);
    
        // Count locally
        // Each EClass counts as model element; as does each of its EAttributes and EReferences
        long localElementCount = ePackage.getEClassifiers()
            .stream()
            .filter(eClassifier -> eClassifier instanceof EClass)
            .map(eClassifier -> (EClass) eClassifier)
            .map(eClass -> 1L + eClass.getFeatureCount())
            .reduce(0L, (a, b) -> a + b);
        return subpackageCount + localElementCount;
    }
}
