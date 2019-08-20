package de.tudresden.inf.lat.gentlerepair.plugin

import java.awt.BorderLayout
import java.awt.Label
import java.util.Collections
import java.util.List
import java.util.Set
import java.util.Vector
import java.util.concurrent.ExecutionException
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import java.util.function.Consumer
import java.util.function.Function

import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.DefaultComboBoxModel
import javax.swing.GroupLayout
import javax.swing.GroupLayout.Alignment
import javax.swing.JButton
import javax.swing.JComboBox
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JProgressBar
import javax.swing.JScrollPane
import javax.swing.ListSelectionModel
import javax.swing.SwingConstants

import org.protege.editor.core.ui.list.MListButton
import org.protege.editor.owl.model.event.EventType
import org.protege.editor.owl.model.event.OWLModelManagerChangeEvent
import org.protege.editor.owl.model.event.OWLModelManagerListener
import org.protege.editor.owl.ui.editor.OWLGeneralAxiomEditor
import org.protege.editor.owl.ui.frame.AxiomListFrame
import org.protege.editor.owl.ui.frame.AxiomListFrameSectionRow
import org.protege.editor.owl.ui.framelist.OWLFrameList
import org.protege.editor.owl.ui.renderer.OWLOntologyCellRenderer
import org.protege.editor.owl.ui.view.AbstractOWLViewComponent
import org.semanticweb.owlapi.model.OWLAxiom
import org.semanticweb.owlapi.model.OWLException
import org.semanticweb.owlapi.model.OWLOntology
import org.semanticweb.owlapi.model.OWLOntologyManager
import org.semanticweb.owlapi.reasoner.OWLReasoner
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory

import collection.JavaConverters._

@SerialVersionUID(-835105430620103724L)
class GentleRepairViewComponent extends AbstractOWLViewComponent {

  // val log: Logger              = LoggerFactory.getLogger(GentleRepairViewComponent.class)

  private var unwantedConsequenceAxiomEditor: OWLGeneralAxiomEditor = _
  private var staticOntologyComboBox: JComboBox[OWLOntology] = _
  private var refutableOntologyComboBox: JComboBox[OWLOntology] = _
  private var repairMethodComboBox: JComboBox[RepairType] = _
  private var owlModelManagerListener: OWLModelManagerListener = _
  private var uiPanel: JPanel = _
  private val progressBar: JProgressBar = new JProgressBar(0, 100)
  private val statusLabel: Label = new Label()
  private val configurationLabel: Label = new Label()
  private val repairButton: JButton = new JButton("Repair")

  private var isRepairing: Boolean = false

  @throws(classOf[Exception])
  override protected def initialiseOWLView() {
    setLayout(new BorderLayout())
    add(initializeConfigurationPanel(), BorderLayout.NORTH)
    add(initializeRepairPanel(), BorderLayout.CENTER)
    //    log.info("Gentle Repair View Component initialized")
  }

  private def initializeConfigurationPanel(): JPanel = {
    val staticOntologyLabel: Label = new Label("Static Ontology: ")
    val refutableOntologyLabel: Label = new Label("Refutable Ontology: ")
    val repairMethodLabel: Label = new Label("Repair Method: ")
    val unwantedConsequenceLabel: Label = new Label("Unwanted Consequence: ")

    staticOntologyComboBox = new JComboBox()
    staticOntologyComboBox.setRenderer(new OWLOntologyCellRenderer(getOWLEditorKit()))

    refutableOntologyComboBox = new JComboBox()
    refutableOntologyComboBox.setRenderer(new OWLOntologyCellRenderer(getOWLEditorKit()))

    repairMethodComboBox = new JComboBox()
    repairMethodComboBox.setModel(new DefaultComboBoxModel[RepairType](RepairType.values()))

    unwantedConsequenceAxiomEditor = new OWLGeneralAxiomEditor(getOWLEditorKit())
    val unwantedConsequenceAxiomEditorScrollPane: JScrollPane =
      new JScrollPane(unwantedConsequenceAxiomEditor.getEditorComponent())

    owlModelManagerListener = event ⇒ {
      if (!isRepairing)
        event.getType() match {
          case EventType.ACTIVE_ONTOLOGY_CHANGED | EventType.ONTOLOGY_CREATED | EventType.ONTOLOGY_LOADED ⇒
            val staticOntologies: Vector[OWLOntology] = new Vector(getOWLModelManager().getOntologies())
            staticOntologies.add(0, Util.EMPTY_ONTOLOGY)
            staticOntologyComboBox.setModel(new DefaultComboBoxModel[OWLOntology](staticOntologies))
            refutableOntologyComboBox
              .setModel(new DefaultComboBoxModel[OWLOntology](new Vector(getOWLModelManager().getOntologies())))
        }
    }
    getOWLModelManager().addListener(owlModelManagerListener)
    owlModelManagerListener.handleChange(new OWLModelManagerChangeEvent(null, EventType.ONTOLOGY_LOADED))

    repairButton.setEnabled(false)
    repairButton.addActionListener(action ⇒ startRepair())
    val statusPanel: JPanel = new JPanel()
    statusPanel.setLayout(new BorderLayout())
    statusPanel.add(configurationLabel, BorderLayout.CENTER)
    statusPanel.add(repairButton, BorderLayout.EAST)
    //    repairButton.setSize(64, 16)
    staticOntologyComboBox.addActionListener(action ⇒ checkSelection())
    refutableOntologyComboBox.addActionListener(action ⇒ checkSelection())
    unwantedConsequenceAxiomEditor.addStatusChangedListener(state ⇒ checkSelection())

    val configurationPanel: JPanel = new JPanel()
    val layout: GroupLayout = new GroupLayout(configurationPanel)
    layout.setAutoCreateContainerGaps(true)
    layout.setAutoCreateGaps(true)
    configurationPanel.setLayout(layout)

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
                java.lang.Short.MAX_VALUE)
              .addComponent(
                refutableOntologyComboBox,
                GroupLayout.DEFAULT_SIZE,
                GroupLayout.DEFAULT_SIZE,
                java.lang.Short.MAX_VALUE)
              .addComponent(
                repairMethodComboBox,
                GroupLayout.DEFAULT_SIZE,
                GroupLayout.DEFAULT_SIZE,
                java.lang.Short.MAX_VALUE)
              .addComponent(
                unwantedConsequenceAxiomEditorScrollPane,
                GroupLayout.DEFAULT_SIZE,
                GroupLayout.DEFAULT_SIZE,
                java.lang.Short.MAX_VALUE)
              .addComponent(
                statusPanel,
                GroupLayout.DEFAULT_SIZE,
                GroupLayout.DEFAULT_SIZE,
                java.lang.Short.MAX_VALUE)))
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
                java.lang.Short.MAX_VALUE)
              .addComponent(
                unwantedConsequenceAxiomEditorScrollPane,
                GroupLayout.DEFAULT_SIZE,
                GroupLayout.DEFAULT_SIZE,
                java.lang.Short.MAX_VALUE))
          .addComponent(statusPanel))

    layout
      .linkSize(
        SwingConstants.HORIZONTAL,
        staticOntologyLabel,
        refutableOntologyLabel,
        repairMethodLabel,
        unwantedConsequenceLabel)
    layout.linkSize(SwingConstants.VERTICAL, staticOntologyLabel, refutableOntologyLabel, repairMethodLabel)
    layout.linkSize(SwingConstants.VERTICAL, staticOntologyComboBox, refutableOntologyComboBox, repairMethodComboBox)

    return configurationPanel
  }

  private def checkSelection() {
    try {
      val ontologyManager: OWLOntologyManager = getOWLModelManager().getOWLOntologyManager()
      val reasonerFactory: OWLReasonerFactory =
        getOWLModelManager().getOWLReasonerManager().getCurrentReasonerFactory().getReasonerFactory()
      val staticOntology: OWLOntology =
        if (staticOntologyComboBox.getSelectedItem().asInstanceOf[OWLOntology].equals(Util.EMPTY_ONTOLOGY))
          ontologyManager.createOntology() else staticOntologyComboBox.getSelectedItem().asInstanceOf[OWLOntology]
      val refutableOntology: OWLOntology = refutableOntologyComboBox.getSelectedItem().asInstanceOf[OWLOntology]
      val unwantedConsequence: OWLAxiom = unwantedConsequenceAxiomEditor.getEditedObject()
      if (unwantedConsequence == null) {
        configurationLabel.setText("Cannot parse an OWL axiom from the text field.")
        repairButton.setEnabled(false)
        return
      }

      val reasoner1: OWLReasoner = reasonerFactory.createNonBufferingReasoner(staticOntology)
      if (reasoner1.isEntailed(unwantedConsequence)) {
        reasoner1.dispose()
        configurationLabel.setText("The axiom must not be entailed by the static ontology.")
        repairButton.setEnabled(false)
        return
      }
      reasoner1.dispose()

      val unionOntology: OWLOntology = ontologyManager.createOntology()
      ontologyManager.addAxioms(unionOntology, staticOntology.getAxioms())
      ontologyManager.addAxioms(unionOntology, refutableOntology.getAxioms())
      val reasoner2: OWLReasoner = reasonerFactory.createNonBufferingReasoner(unionOntology)
      if (!reasoner2.isEntailed(unwantedConsequence)) {
        reasoner2.dispose()
        configurationLabel.setText("The axiom must be entailed by the union of the static and the refutable ontology.")
        repairButton.setEnabled(false)
        return
      }
      reasoner2.dispose()
      configurationLabel.setText("The input is well-formed.")
      repairButton.setEnabled(true)
    } catch {
      case e @ (_: IllegalArgumentException | _: OWLException) ⇒
        configurationLabel.setText("An error occurred.")
        repairButton.setEnabled(false)
        throw new RuntimeException(e)
    }
  }

  private def initializeRepairPanel(): JPanel = {
    val repairPanel: JPanel = new JPanel()
    repairPanel.setLayout(new BorderLayout())
    val statusPanel: JPanel = new JPanel()
    statusPanel.setLayout(new BorderLayout(10, 10))
    statusPanel.add(statusLabel, BorderLayout.CENTER)
    statusPanel.add(progressBar, BorderLayout.EAST)
    repairPanel.add(statusPanel, BorderLayout.SOUTH)
    uiPanel = new JPanel()
    repairPanel.add(uiPanel, BorderLayout.CENTER)
    return repairPanel
  }

  @Override
  protected def disposeOWLView() {
    getOWLModelManager().removeListener(owlModelManagerListener)
    //    log.info("Gentle Repair View Component disposed")
  }

  private def startRepair() {
    isRepairing = true
    configurationLabel.setText("")
    repairButton.setEnabled(false)
    staticOntologyComboBox.setEnabled(false)
    refutableOntologyComboBox.setEnabled(false)
    repairMethodComboBox.setEnabled(false)
    unwantedConsequenceAxiomEditor.getEditorComponent().asInstanceOf[JPanel].getComponents().foreach(component ⇒ component.setEnabled(false))
    //    progressBar.setValue(50)
    //    progressBar.setIndeterminate(true)
    progressBar.setValue(0)
    def progressConsumer(progress: Integer) {
      Util.runOnProtegeThread(() ⇒ {
        progressBar.setValue(progress)
      }, true)
    }
    statusLabel.setAlignment(Label.RIGHT)
    def statusConsumer(status: String) {
      Util.runOnProtegeThread(() ⇒ {
        statusLabel.setText(status)
      }, true)
    }
    try {
      val ontologyManager: OWLOntologyManager = getOWLModelManager().getOWLOntologyManager()
      val reasonerFactory: OWLReasonerFactory =
        getOWLModelManager().getOWLReasonerManager().getCurrentReasonerFactory().getReasonerFactory()
      val staticOntology: OWLOntology =
        if (staticOntologyComboBox.getSelectedItem().asInstanceOf[OWLOntology].equals(Util.EMPTY_ONTOLOGY))
          ontologyManager.createOntology() else staticOntologyComboBox.getSelectedItem().asInstanceOf[OWLOntology]
      val refutableOntology: OWLOntology = refutableOntologyComboBox.getSelectedItem().asInstanceOf[OWLOntology]
      val unwantedConsequence: OWLAxiom = unwantedConsequenceAxiomEditor.getEditedObject()

      val weakeningRelation: AtomicReference[OWLAxiomWeakeningRelation] = new AtomicReference()
      val axiomFromJustificationSelector: AtomicReference[(Set[OWLAxiom]) ⇒ Future[OWLAxiom]] =
        new AtomicReference()
      val axiomFromWeakeningsSelector: AtomicReference[(Set[OWLAxiom]) ⇒ Future[OWLAxiom]] =
        new AtomicReference()

      repairMethodComboBox.getSelectedItem().asInstanceOf[RepairType] match {
        case RepairType.CLASSICAL_REPAIR_RANDOM ⇒
          weakeningRelation.set(OWLAxiomWeakeningRelation.classicalWeakeningRelation)
          axiomFromJustificationSelector.set(Util.randomSelector)
          axiomFromWeakeningsSelector.set(Util.randomSelector)
        // break
        case RepairType.CLASSICAL_REPAIR_USER ⇒
          weakeningRelation.set(OWLAxiomWeakeningRelation.classicalWeakeningRelation)
          axiomFromJustificationSelector.set(axiomSelector("Selection an axiom from the below justification.", uiPanel))
          axiomFromWeakeningsSelector.set(Util.randomSelector)
        // break
        case RepairType.MODIFIED_GENTLE_REPAIR_SEMANTIC_RANDOM ⇒
          weakeningRelation
            .set(
              OWLAxiomWeakeningRelation
                .semanticELConceptInclusionWeakeningRelation(getOWLDataFactory()))
          axiomFromJustificationSelector.set(Util.randomSelector)
          axiomFromWeakeningsSelector.set(Util.randomSelector)
        // break
        case RepairType.MODIFIED_GENTLE_REPAIR_SEMANTIC_USER ⇒
          weakeningRelation
            .set(
              OWLAxiomWeakeningRelation
                .semanticELConceptInclusionWeakeningRelation(getOWLDataFactory()))
          axiomFromJustificationSelector.set(axiomSelector("Selection an axiom from the below justification.", uiPanel))
          axiomFromWeakeningsSelector.set(axiomSelector("Selection a weakened axiom from the below list.", uiPanel))
        // break
        // case RepairType.MODIFIED_GENTLE_REPAIR_SYNTACTIC_RANDOM | RepairType.MODIFIED_GENTLE_REPAIR_SYNTACTIC_USER | RepairType.INTERACTIVE_GENTLE_REPAIR =>
        // throw new RuntimeException("Not implemented")
      }
      new Thread(() ⇒ {
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
            statusConsumer).repair()
          progressBar.setIndeterminate(false)
          progressBar.setValue(100)
          staticOntologyComboBox.setEnabled(true)
          refutableOntologyComboBox.setEnabled(true)
          repairMethodComboBox.setEnabled(true)
          unwantedConsequenceAxiomEditor.getEditorComponent().asInstanceOf[JPanel].getComponents().foreach(component ⇒ component.setEnabled(true))
          isRepairing = false
          checkSelection()
        } catch {
          case e @ (_: IllegalArgumentException | _: OWLException) ⇒ throw new RuntimeException(e)
        }
      }).start()
    } catch {
      case e @ (_: IllegalArgumentException | _: OWLException) ⇒ throw new RuntimeException(e)
    }
  }

  private def axiomSelector(question: String, panel: JPanel)(set: Set[OWLAxiom]): Future[OWLAxiom] = {
    val _isDone: AtomicBoolean = new AtomicBoolean(false)
    val selection: AtomicReference[OWLAxiom] = new AtomicReference()
    Util.runOnProtegeThread(() ⇒ {
      val axiomListFrame: AxiomListFrame = new AxiomListFrame(getOWLEditorKit())
      val axiomList: OWLFrameList[Set[OWLAxiom]] =
        new OWLFrameList[Set[OWLAxiom]](getOWLEditorKit(), axiomListFrame) {
          // private static final long serialVersionUID = -4628615359964268695L
          override protected def getButtons(value: Object): List[MListButton] = Collections.emptyList()
        }
      axiomList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
      axiomList.setRootObject(set)
      val axiomListScrollPane: JScrollPane = new JScrollPane(axiomList)
      val submitButton: JButton = new JButton("Submit selection")
      panel.setLayout(new BorderLayout())
      panel.add(new JLabel(question), BorderLayout.NORTH)
      val axiomPanel: JPanel = new JPanel()
      axiomPanel.setLayout(new BoxLayout(axiomPanel, BoxLayout.X_AXIS))
      axiomPanel.add(axiomListScrollPane)
      axiomPanel.add(Box.createHorizontalStrut(10))
      axiomPanel.add(submitButton)
      panel.add(axiomPanel, BorderLayout.CENTER)
      submitButton.addActionListener(action ⇒ {
        if (!axiomList.isSelectionEmpty()) {
          selection.set(axiomList.getSelectedValue().asInstanceOf[AxiomListFrameSectionRow].getAxiom())
          panel.removeAll()
          axiomList.dispose()
          axiomListFrame.dispose()
          panel.repaint()
          panel.validate()
          _isDone.set(true)
        }
      })
      panel.repaint()
      panel.validate()
    }, true)
    new Future[OWLAxiom]() {

      override def cancel(mayInterruptIfRunning: Boolean): Boolean = throw new RuntimeException("Not supported")
      override def isCancelled(): Boolean = false
      override def isDone(): Boolean = _isDone.get

      @throws(classOf[InterruptedException])
      @throws(classOf[ExecutionException])
      override def get(): OWLAxiom = selection.get()

      @throws(classOf[InterruptedException])
      @throws(classOf[ExecutionException])
      @throws(classOf[TimeoutException])
      override def get(timeout: Long, unit: TimeUnit): OWLAxiom = selection.get()
    }
  }

}
