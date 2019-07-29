package de.tudresden.inf.lat.gentlerepair.plugin;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.function.Function;

import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

public class ModifiedGentleOWLOntologyRepair {

  private final OWLOntologyManager                        ontologyManager;
  private final OWLReasonerFactory                        reasonerFactory;
  private final OWLOntology                               staticOntology;
  private final OWLOntology                               refutableOntology;
  private final OWLAxiom                                  unwantedConsequence;
  private final OWLAxiomWeakeningRelation                 weakeningRelation;
  private final Function<Set<OWLAxiom>, Future<OWLAxiom>> axiomFromJustificationSelector;
  private final Function<Set<OWLAxiom>, Future<OWLAxiom>> axiomFromWeakeningsSelector;
  private final Consumer<Integer>                         progressConsumer;
  private final Consumer<String>                          statusConsumer;

  public ModifiedGentleOWLOntologyRepair(
      OWLOntologyManager ontologyManager,
      OWLReasonerFactory reasonerFactory,
      OWLOntology staticOntology,
      OWLOntology refutableOntology,
      OWLAxiom unwantedConsequence,
      OWLAxiomWeakeningRelation weakeningRelation,
      Function<Set<OWLAxiom>, Future<OWLAxiom>> axiomFromJustificationSelector,
      Function<Set<OWLAxiom>, Future<OWLAxiom>> axiomFromWeakeningsSelector,
      Consumer<Integer> progressConsumer,
      Consumer<String> statusConsumer) throws IllegalArgumentException {
    super();
    this.ontologyManager = ontologyManager;
    this.reasonerFactory = reasonerFactory;
    this.staticOntology = staticOntology;
    this.refutableOntology = refutableOntology;
    this.unwantedConsequence = unwantedConsequence;
    this.weakeningRelation = weakeningRelation;
    this.axiomFromJustificationSelector = axiomFromJustificationSelector;
    this.axiomFromWeakeningsSelector = axiomFromWeakeningsSelector;
    this.progressConsumer = progressConsumer;
    this.statusConsumer = statusConsumer;
    checkArguments();
  }

  private void checkArguments() throws IllegalArgumentException {
    statusConsumer.accept("Deciding if the unwanted axiom is entailed by the static ontology...");
    final OWLReasoner reasoner1 = reasonerFactory.createNonBufferingReasoner(staticOntology);
    if (reasoner1.isEntailed(unwantedConsequence)) {
      reasoner1.dispose();
      throw new IllegalArgumentException("The axiom must not be entailed by the static ontology.");
    }
    reasoner1.dispose();

    statusConsumer
        .accept("Deciding if the unwanted axiom is entailed by the union of the static and the refutable ontology...");
    try {
      final OWLOntology unionOntology = ontologyManager.createOntology();
      ontologyManager.addAxioms(unionOntology, staticOntology.getAxioms());
      ontologyManager.addAxioms(unionOntology, refutableOntology.getAxioms());
      final OWLReasoner reasoner2 = reasonerFactory.createNonBufferingReasoner(unionOntology);
      if (!reasoner2.isEntailed(unwantedConsequence)) {
        reasoner2.dispose();
        throw new IllegalArgumentException(
            "The axiom must be entailed by the union of the static and the refutable ontology.");
      }
      reasoner2.dispose();
    } catch (OWLOntologyCreationException e) {
      throw new RuntimeException(e);
    }
    statusConsumer.accept("The input is well-formed and the repairing can now be started.");
    progressConsumer.accept(10);
  }

  private Set<OWLAxiom> getOneMinimalJustification() throws OWLException {
    statusConsumer.accept("Computing one minimal justification...");
    final Set<OWLAxiom> justification = new HashSet<>(refutableOntology.getAxioms(AxiomType.SUBCLASS_OF));
    if (!isEntailed(justification, unwantedConsequence))
      return null;
    boolean isEntailed = true;
    while (isEntailed) {
      OWLAxiom superfluousAxiom = Util.getRandomElement(justification).get();
      if (superfluousAxiom == null)
        throw new RuntimeException();
      justification.remove(superfluousAxiom);
      if (isEntailed(justification, unwantedConsequence))
        isEntailed = true;
      else {
        isEntailed = false;
        justification.add(superfluousAxiom);
      }
    }
    return justification;
  }

  private boolean isEntailed(Set<OWLAxiom> refutableAxioms, OWLAxiom unwantedConsequence) throws OWLException {
    final OWLOntology unionOntology = ontologyManager.createOntology();
    ontologyManager.addAxioms(unionOntology, staticOntology.getAxioms());
    ontologyManager.addAxioms(unionOntology, refutableAxioms);
    final OWLReasoner reasoner = reasonerFactory.createNonBufferingReasoner(unionOntology);
    final boolean result = reasoner.isEntailed(unwantedConsequence);
    reasoner.dispose();
    return result;
  }

  public void repair() throws OWLException {
    int progress = 10;
    for (Set<OWLAxiom> justification = getOneMinimalJustification(); justification != null; justification =
        getOneMinimalJustification()) {
      try {
        statusConsumer.accept("Choosing an axiom from the justification...");
        final Future<OWLAxiom> axiomFuture = axiomFromJustificationSelector.apply(justification);
        while (!axiomFuture.isDone())
          Thread.sleep(100);
        final OWLAxiom axiom = axiomFuture.get();
        ontologyManager.removeAxiom(refutableOntology, axiom);
        statusConsumer.accept("Computing the maximally strong weakenings...");
        final Set<OWLAxiom> weakenings = weakeningRelation
            .getWeakenings(ontologyManager, reasonerFactory, staticOntology, justification, axiom, unwantedConsequence);
        if (!weakenings.isEmpty()) {
          statusConsumer.accept("Choosing a maximally strong weakening...");
          final Future<OWLAxiom> weakeningFuture = axiomFromWeakeningsSelector.apply(weakenings);
          while (!weakeningFuture.isDone())
            Thread.sleep(100);
          ontologyManager.addAxiom(refutableOntology, weakeningFuture.get());
        }
      } catch (InterruptedException | ExecutionException e) {
        throw new RuntimeException(e);
      }
      progress = Math.min(90, progress + 5);
      progressConsumer.accept(progress);
    }
    statusConsumer.accept("The ontology has been repaired.");
    progressConsumer.accept(100);
  }

}
