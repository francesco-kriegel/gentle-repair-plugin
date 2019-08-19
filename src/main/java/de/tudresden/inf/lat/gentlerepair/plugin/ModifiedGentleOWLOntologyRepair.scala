package de.tudresden.inf.lat.gentlerepair.plugin

import java.util.HashSet
import java.util.Set
import java.util.concurrent.ExecutionException
import java.util.concurrent.Future
import java.util.function.Consumer
import java.util.function.Function

import org.semanticweb.owlapi.model.AxiomType
import org.semanticweb.owlapi.model.OWLAxiom
import org.semanticweb.owlapi.model.OWLException
import org.semanticweb.owlapi.model.OWLOntology
import org.semanticweb.owlapi.model.OWLOntologyCreationException
import org.semanticweb.owlapi.model.OWLOntologyManager
import org.semanticweb.owlapi.reasoner.OWLReasoner
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory

@throws(classOf[IllegalArgumentException])
class ModifiedGentleOWLOntologyRepair(ontologyManager: OWLOntologyManager, reasonerFactory: OWLReasonerFactory, staticOntology: OWLOntology, refutableOntology: OWLOntology, unwantedConsequence: OWLAxiom, weakeningRelation: OWLAxiomWeakeningRelation, axiomFromJustificationSelector: Function[Set[OWLAxiom], Future[OWLAxiom]], axiomFromWeakeningsSelector: Function[Set[OWLAxiom], Future[OWLAxiom]], progressConsumer: Consumer[Integer], statusConsumer: Consumer[String]) {

  checkArguments()

  @throws(classOf[IllegalArgumentException])
  private def checkArguments(): Unit = {
    statusConsumer.accept("Deciding if the unwanted axiom is entailed by the static ontology...")
    val reasoner1: OWLReasoner = reasonerFactory.createNonBufferingReasoner(staticOntology)
    if (reasoner1.isEntailed(unwantedConsequence)) {
      reasoner1.dispose()
      throw new IllegalArgumentException("The axiom must not be entailed by the static ontology.")
    }
    reasoner1.dispose()

    statusConsumer
      .accept("Deciding if the unwanted axiom is entailed by the union of the static and the refutable ontology...")
    try {
      val unionOntology: OWLOntology = ontologyManager.createOntology()
      ontologyManager.addAxioms(unionOntology, staticOntology.getAxioms())
      ontologyManager.addAxioms(unionOntology, refutableOntology.getAxioms())
      val reasoner2: OWLReasoner = reasonerFactory.createNonBufferingReasoner(unionOntology)
      if (!reasoner2.isEntailed(unwantedConsequence)) {
        reasoner2.dispose()
        throw new IllegalArgumentException(
          "The axiom must be entailed by the union of the static and the refutable ontology.")
      }
      reasoner2.dispose()
    } catch {
      case e: OWLOntologyCreationException ⇒ throw new RuntimeException(e)
    }
    statusConsumer.accept("The input is well-formed and the repairing can now be started.")
    progressConsumer.accept(10)
  }

  @throws(classOf[OWLException])
  private def getOneMinimalJustification(): Set[OWLAxiom] = {
    statusConsumer.accept("Computing one minimal justification...")
    val justification: Set[OWLAxiom] = new HashSet(refutableOntology.getAxioms(AxiomType.SUBCLASS_OF))
    if (!isEntailed(justification, unwantedConsequence))
      return null
    var _isEntailed: Boolean = true
    while (_isEntailed) {
      var superfluousAxiom: OWLAxiom = Util.getRandomElement(justification).get()
      if (superfluousAxiom == null)
        throw new RuntimeException()
      justification.remove(superfluousAxiom)
      if (isEntailed(justification, unwantedConsequence))
        _isEntailed = true
      else {
        _isEntailed = false
        justification.add(superfluousAxiom)
      }
    }
    return justification
  }
  @throws(classOf[OWLException])
  private def isEntailed(refutableAxioms: Set[OWLAxiom], unwantedConsequence: OWLAxiom): Boolean = {
    val unionOntology: OWLOntology = ontologyManager.createOntology()
    ontologyManager.addAxioms(unionOntology, staticOntology.getAxioms())
    ontologyManager.addAxioms(unionOntology, refutableAxioms)
    val reasoner: OWLReasoner = reasonerFactory.createNonBufferingReasoner(unionOntology)
    val result: Boolean = reasoner.isEntailed(unwantedConsequence)
    reasoner.dispose()
    return result
  }

  @throws(classOf[OWLException])
  def repair(): Unit = {
    var progress: Int = 10
    var justification: Set[OWLAxiom] = getOneMinimalJustification()
    while (justification != null) {
      try {
        statusConsumer.accept("Choosing an axiom from the justification...")
        val axiomFuture: Future[OWLAxiom] = axiomFromJustificationSelector.apply(justification)
        while (!axiomFuture.isDone())
          Thread.sleep(100)
        val axiom: OWLAxiom = axiomFuture.get()
        ontologyManager.removeAxiom(refutableOntology, axiom)
        statusConsumer.accept("Computing the maximally strong weakenings...")
        val weakenings: Set[OWLAxiom] = weakeningRelation
          .getWeakenings(ontologyManager, reasonerFactory, staticOntology, justification, axiom, unwantedConsequence)
        if (!weakenings.isEmpty()) {
          statusConsumer.accept("Choosing a maximally strong weakening...")
          val weakeningFuture: Future[OWLAxiom] = axiomFromWeakeningsSelector.apply(weakenings)
          while (!weakeningFuture.isDone())
            Thread.sleep(100)
          ontologyManager.addAxiom(refutableOntology, weakeningFuture.get())
        }
      } catch {
        case e @ (_: InterruptedException | _: ExecutionException) ⇒ throw new RuntimeException(e)
      }
      progress = Math.min(90, progress + 5)
      progressConsumer.accept(progress)
      justification = getOneMinimalJustification()
    }
    statusConsumer.accept("The ontology has been repaired.")
    progressConsumer.accept(100)
  }

}
