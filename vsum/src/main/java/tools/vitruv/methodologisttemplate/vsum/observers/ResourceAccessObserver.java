package tools.vitruv.methodologisttemplate.vsum.observers;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import edu.kit.ipd.sdq.commons.util.java.Quintuple;
import tools.vitruv.change.utils.ResourcePersistenceObserver;

public class ResourceAccessObserver 
    extends TimeObserver<Quintuple<Long, Long, String, Long, String>>
    implements ResourcePersistenceObserver {

    private long counter = 0;
    private long modelElementCount;
    private ResourceAccessType accessType;
    private URI resourceURI;

    private enum ResourceAccessType {
        Load,
        Save,
        Delete
    }

    private long countEObjectsWithContainment(Resource resource) {
        var iterator = resource.getAllContents();
        var count = 0;
        for (; iterator.hasNext(); count++) {
            iterator.next();
        }
        return count;
    }

    @Override
    public void startLoadingResource(URI uri) {
        accessType = ResourceAccessType.Load;
        resourceURI = uri;
        startTiming();
    }

    @Override
    public void finishLoadingResource(Resource resource) {
        var time = stopTiming();
        assertEquals(resource.getURI(), resourceURI, "Tried to load a different resource!");
        modelElementCount = countEObjectsWithContainment(resource);
        timesPerChangeType.add(
            new Quintuple<Long,Long,String,Long,String>(
                counter, time, resourceURI.toString(), modelElementCount, accessType.toString()));
        counter++;
    }

    @Override
    public void startSavingResource(Resource resource) {
        accessType = ResourceAccessType.Save;
        modelElementCount = countEObjectsWithContainment(resource);
        resourceURI = resource.getURI();
        startTiming();
    }

    @Override
    public void finishSavingResource(Resource resource) {
        var time = stopTiming();
        assertEquals(resource.getURI(), resourceURI, "Tried to save a different resource!");
        timesPerChangeType.add(
            new Quintuple<Long,Long,String,Long,String>(
                counter, time, resourceURI.toString(), modelElementCount, accessType.toString()));
        counter++;
    }

    @Override
    public void startDeletingResource(Resource resource) {
        accessType = ResourceAccessType.Delete;
        modelElementCount = countEObjectsWithContainment(resource);
        resourceURI = resource.getURI();
        startTiming();
    }


    public ResourceAccessObserver(String filePath) {
        super(new String[]{ "Number", "Time", "Resource", "NoOfElements", "Type" }, filePath);
    }

    @Override
    public void finishDeletingResource(Resource resource) {
        var time = stopTiming();
        assertEquals(resource.getURI(), resourceURI, "Tried to delete a different resource!");
        timesPerChangeType.add(
            new Quintuple<Long,Long,String,Long,String>(
                counter, time, resourceURI.toString(), modelElementCount, accessType.toString()));
        counter++;
    }

    @Override
    protected String[] getLineFor(Quintuple<Long, Long, String, Long, String> record) {
        return new String[] {
            record.get0().toString(), record.get1().toString(), record.get2(), record.get3().toString(), record.get4()
        };
    }
}