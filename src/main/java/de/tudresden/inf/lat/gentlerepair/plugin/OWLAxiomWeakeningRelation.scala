package de.tudresden.inf.lat.gentlerepair.plugin

import java.util.Collections
import java.util.HashSet
import java.util.Set
import java.util.function.BiPredicate

import org.semanticweb.owlapi.model.AxiomType
import org.semanticweb.owlapi.model.OWLAxiom
import org.semanticweb.owlapi.model.OWLDataFactory
import org.semanticweb.owlapi.model.OWLOntology
import org.semanticweb.owlapi.model.OWLOntologyCreationException
import org.semanticweb.owlapi.model.OWLOntologyManager
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom
import org.semanticweb.owlapi.reasoner.OWLReasoner
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory

import com.google.common.collect.Sets

import conexp.fx.core.dl.ELConceptDescription

trait OWLAxiomWeakeningRelation {

  def getWeakenings(
    ontologyManager:     OWLOntologyManager,
    reasonerFactory:     OWLReasonerFactory,
    staticOntology:      OWLOntology,
    justification:       Set[OWLAxiom],
    axiom:               OWLAxiom,
    unwantedConsequence: OWLAxiom): Set[OWLAxiom]

}

object OWLAxiomWeakeningRelation {

  def classicalWeakeningRelation: OWLAxiomWeakeningRelation =
    (_1, _2, _3, _4, _5, _6) ⇒ Collections.emptySet()

  def semanticELConceptInclusionWeakeningRelation(dataFactory: OWLDataFactory): OWLAxiomWeakeningRelation =
    (ontologyManager, reasonerFactory, staticOntology, justification, axiom, unwantedConsequence) ⇒ {
      if (!axiom.getAxiomType().equals(AxiomType.SUBCLASS_OF))
        throw new IllegalArgumentException("Currently, only concept inclusions are supported.")
      val baseAxioms: Set[OWLAxiom] = Sets.newConcurrentHashSet()
      baseAxioms.addAll(staticOntology.getAxioms())
      baseAxioms.addAll(justification)
      baseAxioms.remove(axiom)
      val subClassOfAxiom: OWLSubClassOfAxiom = axiom.asInstanceOf[OWLSubClassOfAxiom]
      val conclusion: ELConceptDescription = ELConceptDescription.of(subClassOfAxiom.getSuperClass())
      val weakenings: Set[OWLAxiom] = Sets.newConcurrentHashSet()
      val nextCandidates: Set[ELConceptDescription] = Sets.newConcurrentHashSet(conclusion.upperNeighborsReduced())
      while (!nextCandidates.isEmpty()) {
        val processedCandidates: Set[ELConceptDescription] = new HashSet(nextCandidates)
        nextCandidates.parallelStream().forEach(candidate ⇒ {
          try {
            val weakenedAxiom: OWLSubClassOfAxiom =
              dataFactory.getOWLSubClassOfAxiom(subClassOfAxiom.getSubClass(), candidate.toOWLClassExpression())
            val weakenedOntology: OWLOntology = ontologyManager.createOntology()
            ontologyManager.addAxioms(weakenedOntology, baseAxioms)
            ontologyManager.addAxiom(weakenedOntology, weakenedAxiom)
            val reasoner: OWLReasoner = reasonerFactory.createReasoner(weakenedOntology)
            if (reasoner.isEntailed(unwantedConsequence))
              nextCandidates.addAll(candidate.upperNeighborsReduced())
            else
              weakenings.add(weakenedAxiom)
            reasoner.dispose()
            ontologyManager.removeOntology(weakenedOntology)
          } catch {
            case e: OWLOntologyCreationException ⇒ throw new RuntimeException(e)
          }
        })
        nextCandidates.removeAll(processedCandidates)
      }
      def isWeakerThan(a: OWLAxiom, b: OWLAxiom): Boolean = {
        try {
          val bOntology: OWLOntology = ontologyManager.createOntology(Collections.singleton(b))
          val bReasoner: OWLReasoner = reasonerFactory.createReasoner(bOntology)
          val result: Boolean = bReasoner.isEntailed(a)
          bReasoner.dispose()
          ontologyManager.removeOntology(bOntology)
          return result
        } catch {
          case e: OWLOntologyCreationException ⇒ throw new RuntimeException(e)
        }
      }
      def isStrictlyWeakerThan(a: OWLAxiom, b: OWLAxiom): Boolean = isWeakerThan(a, b) && !isWeakerThan(b, a)

      val nonMinimalWeakenings: Set[OWLAxiom] = Sets.newConcurrentHashSet()
      weakenings.parallelStream().forEach(weakening1 ⇒ {
        weakenings.parallelStream().forEach(weakening2 ⇒ {
          if (!weakening1.equals(weakening2))
            if (isStrictlyWeakerThan(weakening1, weakening2))
              nonMinimalWeakenings.add(weakening1)
        })
      })
      weakenings.removeAll(nonMinimalWeakenings)
      weakenings
    }

  def syntacticELConceptInclusionWeakeningRelation(dataFactory: OWLDataFactory): OWLAxiomWeakeningRelation =
    (ontologyManager, reasonerFactory, staticOntology, justification, axiom, unwantedConsequence) ⇒ {
      if (!axiom.getAxiomType().equals(AxiomType.SUBCLASS_OF))
        throw new IllegalArgumentException("Currently, only concept inclusions are supported.")
      val subClassOfAxiom: OWLSubClassOfAxiom = axiom.asInstanceOf[OWLSubClassOfAxiom]
      throw new RuntimeException("To be implemented.")
    }

}
