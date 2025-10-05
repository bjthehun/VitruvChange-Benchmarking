package tools.vitruv.methodologisttemplate.vsum;

import edu.kit.ipd.sdq.commons.util.java.Pair;
import edu.kit.ipd.sdq.commons.util.java.Triple;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.ecore.EObject;

import com.google.common.collect.Iterables;

import tools.vitruv.change.atomic.EChange;
import tools.vitruv.change.atomic.command.ApplyEChangeObserver;

public class AtomicEChangeTimeObserver implements ApplyEChangeObserver {
    private final List<Triple<String, Long, Integer>> timesPerEChange = new LinkedList<>();
    private long timestamp;
    private EChange<EObject> lastChange;

    @Override
    public void endApplyEChange(Iterable<Command> commands) {
        var time = System.nanoTime() - timestamp;
        var changeClassName = lastChange.eClass().getName();
        timesPerEChange.add(new Triple<String, Long, Integer>
            (changeClassName, time, Iterables.size(commands)));
    }

    @Override
    public void startToApplyEChange(EChange<EObject> change, boolean forward) {
        timestamp = System.nanoTime();
        lastChange = change;
    }

    public void printResultsTo(String path) throws IOException {
        try (var writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path)))) {
            writer.write("EChangeType,Time,Commands");
            writer.newLine();
            for (var record: timesPerEChange) {
                writer.write(record.getFirst() + "," + record.getSecond() + "," + record.getThird());
                writer.newLine();
            }
            writer.flush();
            writer.close();
        }
    }
}
