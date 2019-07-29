package de.tudresden.inf.lat.gentlerepair.plugin;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiPredicate;

import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

import com.google.common.collect.Sets;

import conexp.fx.core.dl.ELConceptDescription;

@FunctionalInterface
public interface OWLAxiomWeakeningRelation {

  public Set<OWLAxiom> getWeakenings(
      OWLOntologyManager ontologyManager,
      OWLReasonerFactory reasonerFactory,
      OWLOntology staticOntology,
      Set<OWLAxiom> justification,
      OWLAxiom axiom,
      OWLAxiom unwantedConsequence);

  public static OWLAxiomWeakeningRelation classicalWeakeningRelation =
      (_1, _2, _3, _4, _5, _6) -> Collections.emptySet();

  public static OWLAxiomWeakeningRelation
      semanticELConceptInclusionWeakeningRelationWithRandomChoices(final OWLDataFactory dataFactory) {
    return (ontologyManager, reasonerFactory, staticOntology, justification, axiom, unwantedConsequence) -> {
      if (!axiom.getAxiomType().equals(AxiomType.SUBCLASS_OF))
        throw new IllegalArgumentException("Currently, only concept inclusions are supported.");
      final Set<OWLAxiom> baseAxioms = Sets.newConcurrentHashSet();
      baseAxioms.addAll(staticOntology.getAxioms());
      baseAxioms.addAll(justification);
      baseAxioms.remove(axiom);
      final OWLSubClassOfAxiom subClassOfAxiom = (OWLSubClassOfAxiom) axiom;
      final ELConceptDescription conclusion = ELConceptDescription.of(subClassOfAxiom.getSuperClass());
      final Set<OWLAxiom> weakenings = Sets.newConcurrentHashSet();
      final Set<ELConceptDescription> nextCandidates = Sets.newConcurrentHashSet(conclusion.upperNeighborsReduced());
      while (!nextCandidates.isEmpty()) {
        final Set<ELConceptDescription> processedCandidates = new HashSet<>(nextCandidates);
        nextCandidates.parallelStream().forEach(candidate -> {
          try {
            final OWLSubClassOfAxiom weakenedAxiom =
                dataFactory.getOWLSubClassOfAxiom(subClassOfAxiom.getSubClass(), candidate.toOWLClassExpression());
            final OWLOntology weakenedOntology = ontologyManager.createOntology();
//            weakenedOntology.getAxioms().addAll(baseAxioms);
//            weakenedOntology.getAxioms().add(weakenedAxiom);
            ontologyManager.addAxioms(weakenedOntology, baseAxioms);
            ontologyManager.addAxiom(weakenedOntology, weakenedAxiom);
            final OWLReasoner reasoner = reasonerFactory.createNonBufferingReasoner(weakenedOntology);
            if (reasoner.isEntailed(unwantedConsequence))
              nextCandidates.addAll(candidate.upperNeighborsReduced());
            else
              weakenings.add(weakenedAxiom);
            reasoner.dispose();
          } catch (OWLOntologyCreationException e) {
            throw new RuntimeException(e);
          }
        });
        nextCandidates.removeAll(processedCandidates);
      }
      final BiPredicate<OWLAxiom, OWLAxiom> isWeakerThan = (a, b) -> {
        try {
          final OWLOntology bOntology = ontologyManager.createOntology(Collections.singleton(b));
          final OWLReasoner bReasoner = reasonerFactory.createNonBufferingReasoner(bOntology);
          final boolean result = bReasoner.isEntailed(a);
          bReasoner.dispose();
          return result;
        } catch (OWLOntologyCreationException e) {
          throw new RuntimeException(e);
        }
      };
      final BiPredicate<OWLAxiom, OWLAxiom> isStrictlyWeakerThan =
          (a, b) -> isWeakerThan.test(a, b) && !isWeakerThan.test(b, a);

      final Set<OWLAxiom> nonMinimalWeakenings = Sets.newConcurrentHashSet();
      weakenings.parallelStream().forEach(weakening1 -> {
        weakenings.parallelStream().forEach(weakening2 -> {
          if (!weakening1.equals(weakening2))
            if (isStrictlyWeakerThan.test(weakening1, weakening2))
              nonMinimalWeakenings.add(weakening1);
        });
      });
      weakenings.removeAll(nonMinimalWeakenings);
      return weakenings;
    };
  }

  public static OWLAxiomWeakeningRelation
      syntacticELConceptInclusionWeakeningRelationWithRandomChoices(final OWLDataFactory dataFactory) {
    return (ontologyManager, reasonerFactory, staticOntology, justification, axiom, unwantedConsequence) -> {
      if (!axiom.getAxiomType().equals(AxiomType.SUBCLASS_OF))
        throw new IllegalArgumentException("Currently, only concept inclusions are supported.");
      final OWLSubClassOfAxiom subClassOfAxiom = (OWLSubClassOfAxiom) axiom;
      throw new RuntimeException("To be implemented.");
    };
  }

}
