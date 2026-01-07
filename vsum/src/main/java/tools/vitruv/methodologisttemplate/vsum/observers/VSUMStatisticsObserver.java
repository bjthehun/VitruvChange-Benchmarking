package tools.vitruv.methodologisttemplate.vsum.observers;

import org.eclipse.emf.ecore.resource.Resource;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import tools.vitruv.change.propagation.ChangePropagationSpecification;
import tools.vitruv.change.propagation.impl.CompositeChangePropagationSpecification;
import tools.vitruv.dsls.reactions.runtime.reactions.AbstractReactionsChangePropagationSpecification;
import tools.vitruv.framework.vsum.internal.VirtualModelImpl;

public class VSUMStatisticsObserver {
    private final VirtualModelImpl vsum;

    public VSUMStatisticsObserver(VirtualModelImpl vsum) {
        this.vsum = vsum;
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
        long noOfCPRs = getNumberOfCPRs();
    
        // Write to File
        try (var writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path)))) {
            writer.write("NoOfModels, NoOfResources, NoOfCPRs");
            writer.newLine();
            writer.write(noOfModels + ", " + noOfResources + ", " + noOfCPRs);
            writer.newLine();
            writer.flush();
            writer.close();
        }
    }

    private long getNumberOfCPRs() {
        long noOfCPRs = 0;
        for (var specification : vsum.getChangePropagationSpecifications()) {
            noOfCPRs += getNumberOfCPRsFor(specification);
        }
        return noOfCPRs;
    }

    private long getNumberOfCPRsFor(ChangePropagationSpecification specification) {
        if (specification instanceof CompositeChangePropagationSpecification composite) {
            long noOfCPRs = 0;
            for (var childSpecification : composite.getAllProcessors()) {
                noOfCPRs += getNumberOfCPRsFor(childSpecification);
            }
            return noOfCPRs;
        }
        else {
            AbstractReactionsChangePropagationSpecification reactionSpec 
                = (AbstractReactionsChangePropagationSpecification) specification;
            return reactionSpec.getNumberOfReactions();
        }
    }
}
