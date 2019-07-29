package de.tudresden.inf.lat.gentlerepair.plugin;

import java.awt.BorderLayout;
import java.awt.Label;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;

import org.protege.editor.core.ui.list.MListButton;
import org.protege.editor.owl.model.event.EventType;
import org.protege.editor.owl.model.event.OWLModelManagerChangeEvent;
import org.protege.editor.owl.model.event.OWLModelManagerListener;
import org.protege.editor.owl.ui.editor.OWLGeneralAxiomEditor;
import org.protege.editor.owl.ui.frame.AxiomListFrame;
import org.protege.editor.owl.ui.frame.AxiomListFrameSectionRow;
import org.protege.editor.owl.ui.framelist.OWLFrameList;
import org.protege.editor.owl.ui.renderer.OWLOntologyCellRenderer;
import org.protege.editor.owl.ui.view.AbstractOWLViewComponent;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

public class GentleRepairViewComponent extends AbstractOWLViewComponent {

  private static final long       serialVersionUID   = -835105430620103724L;
//  private static final Logger     log              = LoggerFactory.getLogger(GentleRepairViewComponent.class);

  private OWLGeneralAxiomEditor   unwantedConsequenceAxiomEditor;
  private JComboBox<OWLOntology>  staticOntologyComboBox;
  private JComboBox<OWLOntology>  refutableOntologyComboBox;
  private JComboBox<RepairType>   repairMethodComboBox;
  private OWLModelManagerListener owlModelManagerListener;

  private JPanel                  uiPanel;
  private final JProgressBar      progressBar        = new JProgressBar(0, 100);

  private final Label             statusLabel        = new Label();

  private final Label             configurationLabel = new Label();
  private final JButton           repairButton       = new JButton("Repair");

  @SuppressWarnings("incomplete-switch")
  @Override
  protected void initialiseOWLView() throws Exception {
    setLayout(new BorderLayout());
    add(initializeConfigurationPanel(), BorderLayout.NORTH);
    add(initializeRepairPanel(), BorderLayout.CENTER);
//    log.info("Gentle Repair View Component initialized");
  }

  private JPanel initializeConfigurationPanel() {
    final Label staticOntologyLabel = new Label("Static Ontology: ");
    final Label refutableOntologyLabel = new Label("Refutable Ontology: ");
    final Label repairMethodLabel = new Label("Repair Method: ");
    final Label unwantedConsequenceLabel = new Label("Unwanted Consequence: ");

    staticOntologyComboBox = new JComboBox<>();
    staticOntologyComboBox.setRenderer(new OWLOntologyCellRenderer(getOWLEditorKit()));

    refutableOntologyComboBox = new JComboBox<>();
    refutableOntologyComboBox.setRenderer(new OWLOntologyCellRenderer(getOWLEditorKit()));

    repairMethodComboBox = new JComboBox<>();
    repairMethodComboBox.setModel(new DefaultComboBoxModel<RepairType>(RepairType.values()));

    unwantedConsequenceAxiomEditor = new OWLGeneralAxiomEditor(getOWLEditorKit());
    final JScrollPane unwantedConsequenceAxiomEditorScrollPane =
        new JScrollPane(unwantedConsequenceAxiomEditor.getEditorComponent());

    owlModelManagerListener = event -> {
      Vector<OWLOntology> staticOntologies;
      switch (event.getType()) {
      case ACTIVE_ONTOLOGY_CHANGED:
      case ONTOLOGY_CREATED:
      case ONTOLOGY_LOADED:
        staticOntologies = new Vector<>(getOWLModelManager().getOntologies());
        staticOntologies.add(0, Util.EMPTY_ONTOLOGY);
        staticOntologyComboBox.setModel(new DefaultComboBoxModel<OWLOntology>(staticOntologies));
        refutableOntologyComboBox
            .setModel(new DefaultComboBoxModel<OWLOntology>(new Vector<>(getOWLModelManager().getOntologies())));
      }
    };
    getOWLModelManager().addListener(owlModelManagerListener);
    owlModelManagerListener.handleChange(new OWLModelManagerChangeEvent(null, EventType.ONTOLOGY_LOADED));

    repairButton.setEnabled(false);
    repairButton.addActionListener(action -> startRepair());
    final JPanel statusPanel = new JPanel();
    statusPanel.setLayout(new BorderLayout());
    statusPanel.add(configurationLabel, BorderLayout.CENTER);
    statusPanel.add(repairButton, BorderLayout.EAST);
//    repairButton.setSize(64, 16);
    staticOntologyComboBox.addActionListener(action -> checkSelection());
    refutableOntologyComboBox.addActionListener(action -> checkSelection());
    unwantedConsequenceAxiomEditor.addStatusChangedListener(state -> checkSelection());

    final JPanel configurationPanel = new JPanel();
    final GroupLayout layout = new GroupLayout(configurationPanel);
    layout.setAutoCreateContainerGaps(true);
    layout.setAutoCreateGaps(true);
    configurationPanel.setLayout(layout);

    layout
        .setHorizontalGroup(
            layout
                .createSequentialGroup()
                .addGroup(
                    layout
                        .createParallelGroup(Alignment.LEADING)
                        .addComponent(staticOntologyLabel)
                        .addComponent(refutableOntologyLabel)
                        .addComponent(repairMethodLabel)
                        .addComponent(unwantedConsequenceLabel))
                .addGroup(
                    layout
                        .createParallelGroup(Alignment.LEADING, true)
                        .addComponent(
                            staticOntologyComboBox,
                            GroupLayout.DEFAULT_SIZE,
                            GroupLayout.DEFAULT_SIZE,
                            Short.MAX_VALUE)
                        .addComponent(
                            refutableOntologyComboBox,
                            GroupLayout.DEFAULT_SIZE,
                            GroupLayout.DEFAULT_SIZE,
                            Short.MAX_VALUE)
                        .addComponent(
                            repairMethodComboBox,
                            GroupLayout.DEFAULT_SIZE,
                            GroupLayout.DEFAULT_SIZE,
                            Short.MAX_VALUE)
                        .addComponent(
                            unwantedConsequenceAxiomEditorScrollPane,
                            GroupLayout.DEFAULT_SIZE,
                            GroupLayout.DEFAULT_SIZE,
                            Short.MAX_VALUE)
                        .addComponent(
                            statusPanel,
                            GroupLayout.DEFAULT_SIZE,
                            GroupLayout.DEFAULT_SIZE,
                            Short.MAX_VALUE)));
    layout
        .setVerticalGroup(
            layout
                .createSequentialGroup()
                .addGroup(
                    layout.createParallelGroup().addComponent(staticOntologyLabel).addComponent(staticOntologyComboBox))
                .addGroup(
                    layout
                        .createParallelGroup()
                        .addComponent(refutableOntologyLabel)
                        .addComponent(refutableOntologyComboBox))
                .addGroup(
                    layout.createParallelGroup().addComponent(repairMethodLabel).addComponent(repairMethodComboBox))
                .addGroup(
                    layout
                        .createParallelGroup(Alignment.LEADING, true)
                        .addComponent(
                            unwantedConsequenceLabel,
                            GroupLayout.DEFAULT_SIZE,
                            GroupLayout.DEFAULT_SIZE,
                            Short.MAX_VALUE)
                        .addComponent(
                            unwantedConsequenceAxiomEditorScrollPane,
                            GroupLayout.DEFAULT_SIZE,
                            GroupLayout.DEFAULT_SIZE,
                            Short.MAX_VALUE))
                .addComponent(statusPanel));

    layout
        .linkSize(
            SwingConstants.HORIZONTAL,
            staticOntologyLabel,
            refutableOntologyLabel,
            repairMethodLabel,
            unwantedConsequenceLabel);
    layout.linkSize(SwingConstants.VERTICAL, staticOntologyLabel, refutableOntologyLabel, repairMethodLabel);
    layout.linkSize(SwingConstants.VERTICAL, staticOntologyComboBox, refutableOntologyComboBox, repairMethodComboBox);

    return configurationPanel;
  }

  private void checkSelection() {
    try {
      final OWLOntologyManager ontologyManager = getOWLModelManager().getOWLOntologyManager();
      final OWLReasonerFactory reasonerFactory =
          getOWLModelManager().getOWLReasonerManager().getCurrentReasonerFactory().getReasonerFactory();
      final OWLOntology staticOntology =
          ((OWLOntology) staticOntologyComboBox.getSelectedItem()).equals(Util.EMPTY_ONTOLOGY)
              ? ontologyManager.createOntology() : (OWLOntology) staticOntologyComboBox.getSelectedItem();
      final OWLOntology refutableOntology = (OWLOntology) refutableOntologyComboBox.getSelectedItem();
      final OWLAxiom unwantedConsequence = unwantedConsequenceAxiomEditor.getEditedObject();
      if (unwantedConsequence == null) {
        configurationLabel.setText("Cannot parse an OWL axiom from the text field.");
        repairButton.setEnabled(false);
        return;
      }

      final OWLReasoner reasoner1 = reasonerFactory.createNonBufferingReasoner(staticOntology);
      if (reasoner1.isEntailed(unwantedConsequence)) {
        reasoner1.dispose();
        configurationLabel.setText("The axiom must not be entailed by the static ontology.");
        repairButton.setEnabled(false);
        return;
      }
      reasoner1.dispose();

      final OWLOntology unionOntology = ontologyManager.createOntology();
      ontologyManager.addAxioms(unionOntology, staticOntology.getAxioms());
      ontologyManager.addAxioms(unionOntology, refutableOntology.getAxioms());
      final OWLReasoner reasoner2 = reasonerFactory.createNonBufferingReasoner(unionOntology);
      if (!reasoner2.isEntailed(unwantedConsequence)) {
        reasoner2.dispose();
        configurationLabel.setText("The axiom must be entailed by the union of the static and the refutable ontology.");
        repairButton.setEnabled(false);
        return;
      }
      reasoner2.dispose();
      configurationLabel.setText("The input is well-formed.");
      repairButton.setEnabled(true);
    } catch (IllegalArgumentException | OWLException e) {
      configurationLabel.setText("An error occurred.");
      repairButton.setEnabled(false);
      throw new RuntimeException(e);
    }
  }

  private JPanel initializeRepairPanel() {
    final JPanel repairPanel = new JPanel();
    repairPanel.setLayout(new BorderLayout());
    final JPanel statusPanel = new JPanel();
    statusPanel.setLayout(new BorderLayout(10, 10));
    statusPanel.add(statusLabel, BorderLayout.CENTER);
    statusPanel.add(progressBar, BorderLayout.EAST);
    repairPanel.add(statusPanel, BorderLayout.SOUTH);
    uiPanel = new JPanel();
    repairPanel.add(uiPanel, BorderLayout.CENTER);
    return repairPanel;
  }

  @Override
  protected void disposeOWLView() {
    getOWLModelManager().removeListener(owlModelManagerListener);
//    log.info("Gentle Repair View Component disposed");
  }

  private void startRepair() {
    configurationLabel.setText("");
    repairButton.setEnabled(false);
//    progressBar.setValue(50);
//    progressBar.setIndeterminate(true);
    progressBar.setValue(0);
    final Consumer<Integer> progressConsumer = progress -> {
      Util.runOnProtegeThread(() -> {
        progressBar.setValue(progress);
      }, true);
    };
    statusLabel.setAlignment(Label.RIGHT);
    final Consumer<String> statusConsumer = status -> {
      Util.runOnProtegeThread(() -> {
        statusLabel.setText(status);
      }, true);
    };
    try {
      final OWLOntologyManager ontologyManager = getOWLModelManager().getOWLOntologyManager();
      final OWLReasonerFactory reasonerFactory =
          getOWLModelManager().getOWLReasonerManager().getCurrentReasonerFactory().getReasonerFactory();
      final OWLOntology staticOntology =
          ((OWLOntology) staticOntologyComboBox.getSelectedItem()).equals(Util.EMPTY_ONTOLOGY)
              ? ontologyManager.createOntology() : (OWLOntology) staticOntologyComboBox.getSelectedItem();
      final OWLOntology refutableOntology = (OWLOntology) refutableOntologyComboBox.getSelectedItem();
      final OWLAxiom unwantedConsequence = unwantedConsequenceAxiomEditor.getEditedObject();

      final AtomicReference<OWLAxiomWeakeningRelation> weakeningRelation = new AtomicReference<>();
      final AtomicReference<Function<Set<OWLAxiom>, Future<OWLAxiom>>> axiomFromJustificationSelector =
          new AtomicReference<>();
      final AtomicReference<Function<Set<OWLAxiom>, Future<OWLAxiom>>> axiomFromWeakeningsSelector =
          new AtomicReference<>();

      switch ((RepairType) repairMethodComboBox.getSelectedItem()) {
      case CLASSICAL_REPAIR_RANDOM:
        weakeningRelation.set(OWLAxiomWeakeningRelation.classicalWeakeningRelation);
        axiomFromJustificationSelector.set(Util.randomSelector());
        axiomFromWeakeningsSelector.set(Util.randomSelector());
        break;
      case CLASSICAL_REPAIR_USER:
        weakeningRelation.set(OWLAxiomWeakeningRelation.classicalWeakeningRelation);
        axiomFromJustificationSelector.set(axiomSelector("Selection an axiom from the below justification.", uiPanel));
        axiomFromWeakeningsSelector.set(Util.randomSelector());
        break;
      case MODIFIED_GENTLE_REPAIR_SEMANTIC_RANDOM:
        weakeningRelation
            .set(
                OWLAxiomWeakeningRelation
                    .semanticELConceptInclusionWeakeningRelationWithRandomChoices(getOWLDataFactory()));
        axiomFromJustificationSelector.set(Util.randomSelector());
        axiomFromWeakeningsSelector.set(Util.randomSelector());
        break;
      case MODIFIED_GENTLE_REPAIR_SEMANTIC_USER:
        weakeningRelation
            .set(
                OWLAxiomWeakeningRelation
                    .semanticELConceptInclusionWeakeningRelationWithRandomChoices(getOWLDataFactory()));
        axiomFromJustificationSelector.set(axiomSelector("Selection an axiom from the below justification.", uiPanel));
        axiomFromWeakeningsSelector.set(axiomSelector("Selection a weakened axiom from the below list.", uiPanel));
        break;
//      case MODIFIED_GENTLE_REPAIR_SYNTACTIC_RANDOM:
//      case MODIFIED_GENTLE_REPAIR_SYNTACTIC_USER:
//      case INTERACTIVE_GENTLE_REPAIR:
//        throw new RuntimeException("Not implemented");
      }
      new Thread(() -> {
        try {
          new ModifiedGentleOWLOntologyRepair(
              ontologyManager,
              reasonerFactory,
              staticOntology,
              refutableOntology,
              unwantedConsequence,
              weakeningRelation.get(),
              axiomFromJustificationSelector.get(),
              axiomFromWeakeningsSelector.get(),
              progressConsumer,
              statusConsumer).repair();
          progressBar.setIndeterminate(false);
          progressBar.setValue(100);
          checkSelection();
        } catch (IllegalArgumentException | OWLException e) {
          throw new RuntimeException(e);
        }
      }).start();
    } catch (IllegalArgumentException | OWLException e) {
      throw new RuntimeException(e);
    }
  }

  private Function<Set<OWLAxiom>, Future<OWLAxiom>> axiomSelector(final String question, final JPanel panel) {
    return set -> {
      final AtomicBoolean isDone = new AtomicBoolean(false);
      final AtomicReference<OWLAxiom> selection = new AtomicReference<>();
      Util.runOnProtegeThread(() -> {
        final AxiomListFrame axiomListFrame = new AxiomListFrame(getOWLEditorKit());
        final OWLFrameList<Set<OWLAxiom>> axiomList =
            new OWLFrameList<Set<OWLAxiom>>(getOWLEditorKit(), axiomListFrame) {

              private static final long serialVersionUID = -4628615359964268695L;

              protected List<MListButton> getButtons(Object value) {
                return Collections.emptyList();
              }
            };
        axiomList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        axiomList.setRootObject(set);
        final JScrollPane axiomListScrollPane = new JScrollPane(axiomList);
        final JButton submitButton = new JButton("Submit selection");
        panel.setLayout(new BorderLayout());
        panel.add(new JLabel(question), BorderLayout.NORTH);
        final JPanel axiomPanel = new JPanel();
        axiomPanel.setLayout(new BoxLayout(axiomPanel, BoxLayout.X_AXIS));
        axiomPanel.add(axiomListScrollPane);
        axiomPanel.add(Box.createHorizontalStrut(10));
        axiomPanel.add(submitButton);
        panel.add(axiomPanel, BorderLayout.CENTER);
        submitButton.addActionListener(action -> {
          if (!axiomList.isSelectionEmpty()) {
            selection.set(((AxiomListFrameSectionRow) axiomList.getSelectedValue()).getAxiom());
            panel.removeAll();
            axiomListFrame.dispose();
            axiomList.dispose();
            panel.repaint();
            panel.validate();
            isDone.set(true);
          }
        });
        panel.repaint();
        panel.validate();
      }, true);
      return new Future<OWLAxiom>() {

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
          throw new RuntimeException("Not supported");
        }

        @Override
        public boolean isCancelled() {
          return false;
        }

        @Override
        public boolean isDone() {
          return isDone.get();
        }

        @Override
        public OWLAxiom get() throws InterruptedException, ExecutionException {
          return selection.get();
        }

        @Override
        public OWLAxiom get(long timeout, TimeUnit unit)
            throws InterruptedException, ExecutionException, TimeoutException {
          return selection.get();
        }
      };
    };
  }

}
