package tools.vitruv.methodologisttemplate.vsum;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.RepetitionInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import mir.reactions.model2Model2.Model2Model2ChangePropagationSpecification;
import tools.vitruv.change.atomic.command.internal.ApplyEChangeSwitch;
import tools.vitruv.change.propagation.ChangePropagationMode;
import tools.vitruv.change.testutils.TestUserInteraction;
import tools.vitruv.framework.views.CommittableView;
import tools.vitruv.framework.views.View;
import tools.vitruv.framework.views.ViewTypeFactory;
import tools.vitruv.framework.vsum.VirtualModel;
import tools.vitruv.framework.vsum.VirtualModelBuilder;
import tools.vitruv.framework.vsum.internal.InternalVirtualModel;
import tools.vitruv.framework.vsum.internal.VirtualModelImpl;
import tools.vitruv.methodologisttemplate.model.model.ModelFactory;
import tools.vitruv.methodologisttemplate.model.model.System;
import tools.vitruv.methodologisttemplate.model.model2.Root;

/**
 * This class provides an example how to define and use a VSUM.
 */
public class VSUMExampleTest {
  private static final int WARM_UP_RUNS = 15;
  private static final int MEASUREMENT_RUNS = 15 + WARM_UP_RUNS;

  private static final AtomicEChangeTimeObserver eChangeObserver = new AtomicEChangeTimeObserver();
  private static final VitruvChangeTimeObserver vitruvChangeObserver = new VitruvChangeTimeObserver(eChangeObserver);
  private static final ConsistencyPreservationRuleTimeObserver cprObserver = new ConsistencyPreservationRuleTimeObserver(eChangeObserver);

  @BeforeAll
  static void setup() {
    Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("*", new XMIResourceFactoryImpl());
    ApplyEChangeSwitch.registerObserver(eChangeObserver);
  }

  @BeforeEach
  void decideAboutAcceptingMeasurement(RepetitionInfo repetitionInfo) {
    var runsForTest = repetitionInfo.getCurrentRepetition();
    if (runsForTest <= WARM_UP_RUNS) {
      eChangeObserver.rejectMeasurement();
      vitruvChangeObserver.rejectMeasurement();
      cprObserver.rejectMeasurement();
    }
    else {
      eChangeObserver.acceptMeasurement();
      vitruvChangeObserver.acceptMeasurement();
      cprObserver.acceptMeasurement();
    }
  }

  @AfterAll
  static void tearDown() throws IOException {
    ApplyEChangeSwitch.deregisterObserver(eChangeObserver);
    eChangeObserver.printResultsTo("results_echange.csv");
    cprObserver.printResultsTo("results_cprs.csv");
    vitruvChangeObserver.printResultsTo("results_vitruviuschange.csv");
  }

  @RepeatedTest(MEASUREMENT_RUNS)
  void reloadFilledVirtualModel(@TempDir Path tempDir) {
    InternalVirtualModel vsum = createDefaultVirtualModel(tempDir);
    addSystem(vsum, tempDir);
    vsum.dispose();
    vsum = createDefaultVirtualModel(tempDir);
    // Assert that the reloaded virtual model contains the changes we made before disposing it
    Assertions.assertEquals(1, getDefaultView(vsum, List.of(System.class)).getRootObjects().size());
    Assertions.assertEquals(1, getDefaultView(vsum, List.of(Root.class)).getRootObjects().size());
  }

  @RepeatedTest(MEASUREMENT_RUNS)
  void systemInsertionAndPropagationTest(@TempDir Path tempDir) {
    VirtualModel vsum = createDefaultVirtualModel(tempDir);
    addSystem(vsum, tempDir);
    // assert that the directly added System is present
    Assertions.assertEquals(1, getDefaultView(vsum, List.of(System.class)).getRootObjects().size());
    // as well as the Root that should be created by the Reactions, see
    // templateReactions.reactions#14
    Assertions.assertEquals(1, getDefaultView(vsum, List.of(Root.class)).getRootObjects().size());
  }

  @RepeatedTest(MEASUREMENT_RUNS)
  void insertComponent(@TempDir Path tempDir) {
    InternalVirtualModel vsum = createDefaultVirtualModel(tempDir);
    addSystem(vsum, tempDir);
    addComponent(vsum);
    Assertions.assertTrue(assertView(getDefaultView(vsum, List.of(System.class, Root.class)), (View v) -> {
      // assert that a component has been inserted, a entity has been created and that
      // both have the same name
      // Note: to make the test result easier to understand, these different effects
      // should be tested one by one
      return v.getRootObjects(System.class).iterator().next()
          .getComponents().get(0).getName()
          .equals(v.getRootObjects(Root.class).iterator().next()
              .getEntities().get(0).getName());
    }));
  }

  @RepeatedTest(MEASUREMENT_RUNS)
  void insertRouter(@TempDir Path tempDir) {
    InternalVirtualModel vsum = createDefaultVirtualModel(tempDir);
    addSystem(vsum, tempDir);
    addRouter(vsum);
    modifyView(getDefaultView(vsum, List.of(System.class)).withChangeDerivingTrait(), (CommittableView v) -> {
      // add a router to the system
      v.getRootObjects(System.class).iterator().next().getComponents().add(ModelFactory.eINSTANCE.createRouter());
    });
    Assertions.assertTrue(assertView(getDefaultView(vsum, List.of(System.class, Root.class)), (View v) -> {
      // assert that the router has been added and that the corresponding entity has
      // been created
      return v.getRootObjects(System.class).iterator().next()
          .getComponents().get(0).getName()
          .equals(v.getRootObjects(Root.class).iterator().next()
              .getEntities().get(0).getName());
    }));
  }

  @RepeatedTest(MEASUREMENT_RUNS)
  void renameComponent(@TempDir Path tempDir) {
    final String newName = "newName";
    VirtualModel vsum = createDefaultVirtualModel(tempDir);
    addSystem(vsum, tempDir);
    addComponent(vsum);
    modifyView(getDefaultView(vsum, List.of(System.class)).withChangeDerivingTrait(), (CommittableView v) -> {
      // change the name of the component
      v.getRootObjects(System.class).iterator().next().getComponents().get(0).setName(newName);
    });
    Assertions.assertTrue(assertView(getDefaultView(vsum, List.of(System.class, Root.class)), (View v) -> {
      // assert that the renaming worked on the component as well as the corresponding
      // entity
      return v.getRootObjects(System.class).iterator().next()
          .getComponents().get(0).getName().equals(newName)
          && v.getRootObjects(Root.class).iterator().next()
              .getEntities().get(0).getName().equals(newName);
    }));
  }

  @RepeatedTest(MEASUREMENT_RUNS)
  void deleteComponent(@TempDir Path tempDir) {
    VirtualModel vsum = createDefaultVirtualModel(tempDir);
    addSystem(vsum, tempDir);
    addComponent(vsum);
    modifyView(getDefaultView(vsum, List.of(System.class)).withChangeDerivingTrait(), (CommittableView v) -> {
      v.getRootObjects(System.class).iterator().next().getComponents().remove(0);
    });
    Assertions.assertTrue(assertView(getDefaultView(vsum, List.of(System.class, Root.class)), (View v) -> {
      // assert that the deletion of the component worked and that the corresponding
      // entity also got deleted
      return v.getRootObjects(System.class).iterator().next().getComponents().isEmpty()
          && v.getRootObjects(Root.class).iterator().next().getEntities().isEmpty();
    }));
  }

  @RepeatedTest(MEASUREMENT_RUNS)
  void testLink(@TempDir Path tempDir) {
    VirtualModel vsum = createDefaultVirtualModel(tempDir);
    addSystem(vsum, tempDir);

    // add two components, protocol and add a link between them
    modifyView(getDefaultView(vsum, List.of(System.class)).withChangeDerivingTrait(), (CommittableView v) -> {

      var system = v.getRootObjects(System.class).iterator().next();

      var component1 = ModelFactory.eINSTANCE.createComponent();
      component1.setName("component1");
      var component2 = ModelFactory.eINSTANCE.createComponent();
      component2.setName("component2");
      system.getComponents().addAll(List.of(component1, component2));

      var protocol = ModelFactory.eINSTANCE.createProtocol();
      protocol.setName("exampleProtocol");
      system.getProtocols().add(protocol);

      var link = ModelFactory.eINSTANCE.createLink();
      system.getLinks().add(link);
      link.setProtocol(protocol);
      link.getComponents().addAll(List.of(component1, component2));
    });

    // assert that the link has been created and that it is connected to the two
    // components
    Assertions.assertTrue(assertView(getDefaultView(vsum, List.of(Root.class)), (View v) -> {
      var root = v.getRootObjects(Root.class).iterator().next();
      return root.getLinks().size() == 1
          && root.getLinks().get(0).getEntities().size() == 2
          && root.getLinks().get(0).getEntities().stream()
              .allMatch(c -> c.getName().startsWith("component"));
    }));
  }

  private void addSystem(VirtualModel vsum, Path projectPath) {
    CommittableView view = getDefaultView(vsum, List.of(System.class)).withChangeDerivingTrait();
    modifyView(view, (CommittableView v) -> {
      v.registerRoot(
          ModelFactory.eINSTANCE.createSystem(),
          URI.createFileURI(projectPath.toString() + "/example.model"));
    });

  }

  private void addComponent(VirtualModel vsum) {
    CommittableView view = getDefaultView(vsum, List.of(System.class)).withChangeDerivingTrait();
    modifyView(view, (CommittableView v) -> {
      var component = ModelFactory.eINSTANCE.createComponent();
      component.setName("specialname");
      v.getRootObjects(System.class).iterator().next().getComponents().add(component);
    });
  }

  private void addRouter(VirtualModel vsum) {
    CommittableView view = getDefaultView(vsum, List.of(System.class)).withChangeDerivingTrait();
    modifyView(view, (CommittableView v) -> {
      var component = ModelFactory.eINSTANCE.createRouter();
      component.setName("specialRouterName");
      v.getRootObjects(System.class).iterator().next().getComponents().add(component);
    });
  }

  private InternalVirtualModel createDefaultVirtualModel(Path projectPath) {
    VirtualModelImpl model = (VirtualModelImpl) new VirtualModelBuilder()
        .withStorageFolder(projectPath)
        .withUserInteractorForResultProvider(new TestUserInteraction.ResultProvider(new TestUserInteraction()))
        .withChangePropagationSpecifications(new Model2Model2ChangePropagationSpecification())
        .buildAndInitialize();
    model.setChangePropagationMode(ChangePropagationMode.TRANSITIVE_CYCLIC);
    model.registerObserver(cprObserver);
    model.addChangePropagationListener(vitruvChangeObserver);
    return model;
  }

  // See https://github.com/vitruv-tools/Vitruv/issues/717 for more information
  // about the rootTypes
  private View getDefaultView(VirtualModel vsum, Collection<Class<?>> rootTypes) {
    var selector = vsum.createSelector(ViewTypeFactory.createIdentityMappingViewType("default"));
    selector.getSelectableElements().stream()
        .filter(element -> rootTypes.stream().anyMatch(it -> it.isInstance(element)))
        .forEach(it -> selector.setSelected(it, true));
    return selector.createView();
  }

  // These functions are only for convience, as they make the code a bit better
  // readable
  private void modifyView(CommittableView view, Consumer<CommittableView> modificationFunction) {
    modificationFunction.accept(view);
    view.commitChanges();
  }

  private boolean assertView(View view, Function<View, Boolean> viewAssertionFunction) {
    return viewAssertionFunction.apply(view);
  }

}
