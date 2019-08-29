package de.tudresden.inf.lat.gentlerepair.plugin

import java.util.concurrent.atomic.AtomicInteger

import scala.collection.JavaConverters._

import org.semanticweb.owlapi.model.AxiomType
import org.semanticweb.owlapi.model.IRI
import org.semanticweb.owlapi.model.OWLAxiom
import org.semanticweb.owlapi.model.OWLDataFactory
import org.semanticweb.owlapi.model.OWLOntology
import org.semanticweb.owlapi.model.OWLOntologyCreationException
import org.semanticweb.owlapi.model.OWLOntologyManager
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom
import org.semanticweb.owlapi.reasoner.OWLReasoner
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory

import com.google.common.collect.Sets

import conexp.fx.core.collections.relation.MatrixRelation
import conexp.fx.core.dl.ELConceptDescription
import conexp.fx.core.dl.ELConceptInclusion

trait OWLAxiomWeakeningRelation {

  def getWeakenings(
    ontologyManager:     OWLOntologyManager,
    reasonerFactory:     OWLReasonerFactory,
    staticOntology:      OWLOntology,
    justification:       java.util.Set[OWLAxiom],
    axiom:               OWLAxiom,
    unwantedConsequence: OWLAxiom): java.util.Set[OWLAxiom]

}

object OWLAxiomWeakeningRelation {

  def classicalWeakeningRelation: OWLAxiomWeakeningRelation =
    (_1, _2, _3, _4, _5, _6) ⇒ java.util.Collections.emptySet()

  def semanticELConceptInclusionWeakeningRelation(dataFactory: OWLDataFactory): OWLAxiomWeakeningRelation =
    (ontologyManager, reasonerFactory, staticOntology, justification, axiom, unwantedConsequence) ⇒ {
      if (!axiom.getAxiomType().equals(AxiomType.SUBCLASS_OF))
        throw new IllegalArgumentException("Currently, only concept inclusions are supported.")
      val baseAxioms: java.util.Set[OWLAxiom] = Sets.newConcurrentHashSet()
      baseAxioms.addAll(staticOntology.getAxioms())
      baseAxioms.addAll(justification)
      baseAxioms.remove(axiom)
      val subClassOfAxiom: OWLSubClassOfAxiom = axiom.asInstanceOf[OWLSubClassOfAxiom]
      val conclusion: ELConceptDescription = ELConceptDescription.of(subClassOfAxiom.getSuperClass())
      val weakenings: java.util.Set[OWLAxiom] = Sets.newConcurrentHashSet()
      val nextCandidates: java.util.Set[ELConceptDescription] = Sets.newConcurrentHashSet(conclusion.upperNeighborsReduced())
      while (!nextCandidates.isEmpty()) {
        val processedCandidates: java.util.Set[ELConceptDescription] = new java.util.HashSet(nextCandidates)
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
          val bOntology: OWLOntology = ontologyManager.createOntology(java.util.Collections.singleton(b))
          val bReasoner: OWLReasoner = reasonerFactory.createReasoner(bOntology)
          val result: Boolean = bReasoner.isEntailed(a)
          bReasoner.dispose()
          ontologyManager.removeOntology(bOntology)
          result
        } catch {
          case e: OWLOntologyCreationException ⇒ throw new RuntimeException(e)
        }
      }
      def isStrictlyWeakerThan(a: OWLAxiom, b: OWLAxiom): Boolean = isWeakerThan(a, b) && !isWeakerThan(b, a)

      val nonMinimalWeakenings: java.util.Set[OWLAxiom] = Sets.newConcurrentHashSet()
      weakenings.parallelStream().sequential().forEach(weakening1 ⇒ {
        weakenings.parallelStream().sequential().forEach(weakening2 ⇒ {
          if (!weakening1.equals(weakening2))
            if (isStrictlyWeakerThan(weakening1, weakening2))
              nonMinimalWeakenings.add(weakening1)
        })
      })
      weakenings.removeAll(nonMinimalWeakenings)
      weakenings
    }

  def semanticELConceptInclusionWeakeningRelation2(dataFactory: OWLDataFactory): OWLAxiomWeakeningRelation =
    (ontologyManager, reasonerFactory, staticOntology, justification, axiom, unwantedConsequence) ⇒ {
      if (!axiom.getAxiomType().equals(AxiomType.SUBCLASS_OF))
        throw new IllegalArgumentException("Currently, only concept inclusions are supported.")
      val baseAxioms: java.util.Set[OWLAxiom] = Sets.newConcurrentHashSet()
      baseAxioms.addAll(staticOntology.getAxioms())
      baseAxioms.addAll(justification)
      baseAxioms.remove(axiom)
      val subClassOfAxiom: OWLSubClassOfAxiom = axiom.asInstanceOf[OWLSubClassOfAxiom]
      val conclusion: ELConceptDescription = ELConceptDescription.of(subClassOfAxiom.getSuperClass())
      val weakenings: java.util.Set[OWLAxiom] = Sets.newConcurrentHashSet()
      val nextCandidates: java.util.Set[ELConceptDescription] = Sets.newConcurrentHashSet(conclusion.upperNeighborsReduced())
      while (!nextCandidates.isEmpty()) {
        val processedCandidates: java.util.Set[ELConceptDescription] = new java.util.HashSet(nextCandidates)
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

      val nonMinimalWeakenings: java.util.Set[OWLAxiom] = Sets.newConcurrentHashSet()
      val order: MatrixRelation[OWLAxiom, OWLAxiom] = new MatrixRelation(true)
      order.rowHeads().addAll(weakenings)
      weakenings.stream().parallel().forEach(weakening1 ⇒ {
        val ontology: OWLOntology = ontologyManager.createOntology(java.util.Collections.singleton(weakening1))
        val reasoner: OWLReasoner = reasonerFactory.createReasoner(ontology)
        weakenings.stream().sequential().forEach(weakening2 ⇒ {
          if (!(weakening1 equals weakening2))
            if (reasoner isEntailed weakening2)
              order.add(weakening1, weakening2)
        })
        reasoner.dispose()
        ontologyManager.removeOntology(ontology)
        System.gc()
      })
      weakenings.stream().parallel().forEach(weakening2 ⇒ {
        val ontology: OWLOntology = ontologyManager.createOntology(java.util.Collections.singleton(weakening2))
        val reasoner: OWLReasoner = reasonerFactory.createReasoner(ontology)
        order.col(weakening2).stream().sequential().forEach(weakening1 ⇒ {
          if (!(weakening1 equals weakening2))
            if (!(reasoner isEntailed weakening1))
              nonMinimalWeakenings add weakening2
        })
        reasoner.dispose()
        ontologyManager.removeOntology(ontology)
        System.gc()
      })
      weakenings.removeAll(nonMinimalWeakenings)
      weakenings
    }

  def semanticELConceptInclusionWeakeningRelation3(dataFactory: OWLDataFactory): OWLAxiomWeakeningRelation =
    (ontologyManager, reasonerFactory, staticOntology, justification, axiom, unwantedConsequence) ⇒ {
      if (!axiom.getAxiomType().equals(AxiomType.SUBCLASS_OF))
        throw new IllegalArgumentException("Currently, only concept inclusions are supported.")
      val baseAxioms: java.util.Set[OWLAxiom] = Sets.newConcurrentHashSet()
      baseAxioms.addAll(staticOntology.getAxioms())
      baseAxioms.addAll(justification)
      baseAxioms.remove(axiom)
      val subClassOfAxiom: OWLSubClassOfAxiom = axiom.asInstanceOf[OWLSubClassOfAxiom]
      val conclusion: ELConceptDescription = ELConceptDescription.of(subClassOfAxiom.getSuperClass())
      val weakenedRHS: java.util.Set[ELConceptDescription] = Sets.newConcurrentHashSet()
      val nextCandidates: java.util.Set[ELConceptDescription] = Sets.newConcurrentHashSet(conclusion.upperNeighborsReduced())
      while (!nextCandidates.isEmpty()) {
        val processedCandidates: java.util.Set[ELConceptDescription] = new java.util.HashSet(nextCandidates)
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
              weakenedRHS add candidate
            reasoner.dispose()
            ontologyManager.removeOntology(weakenedOntology)
          } catch {
            case e: OWLOntologyCreationException ⇒ throw new RuntimeException(e)
          }
        })
        nextCandidates.removeAll(processedCandidates)
      }

      def isStrictlyMoreSpecific(c: ELConceptDescription, d: ELConceptDescription): Boolean = (c compareTo d) == -1
      val nonMinimalRHS: java.util.Set[ELConceptDescription] = Sets.newConcurrentHashSet()
      weakenedRHS.parallelStream().forEach(c ⇒ {
        weakenedRHS.parallelStream().forEach(d ⇒ {
          if (isStrictlyMoreSpecific(c, d))
            nonMinimalRHS add d
        })
      })
      weakenedRHS removeAll nonMinimalRHS

      val weakenings: java.util.Set[OWLAxiom] = Sets.newConcurrentHashSet()
      weakenedRHS.parallelStream().forEach(c ⇒ {
        weakenings add dataFactory.getOWLSubClassOfAxiom(subClassOfAxiom.getSubClass(), c.toOWLClassExpression())
      })

      val nonMinimalWeakenings: java.util.Set[OWLAxiom] = Sets.newConcurrentHashSet()
      val order: MatrixRelation[OWLAxiom, OWLAxiom] = new MatrixRelation(true)
      order.rowHeads().addAll(weakenings)
      weakenings.stream().parallel().forEach(weakening1 ⇒ {
        val ontology: OWLOntology = ontologyManager.createOntology(java.util.Collections.singleton(weakening1))
        val reasoner: OWLReasoner = reasonerFactory.createReasoner(ontology)
        weakenings.stream().sequential().forEach(weakening2 ⇒ {
          if (!(weakening1 equals weakening2))
            if (reasoner isEntailed weakening2)
              order.add(weakening1, weakening2)
        })
        reasoner.dispose()
        ontologyManager.removeOntology(ontology)
        System.gc()
      })
      weakenings.stream().parallel().forEach(weakening2 ⇒ {
        val ontology: OWLOntology = ontologyManager.createOntology(java.util.Collections.singleton(weakening2))
        val reasoner: OWLReasoner = reasonerFactory.createReasoner(ontology)
        order.col(weakening2).stream().sequential().forEach(weakening1 ⇒ {
          if (!(weakening1 equals weakening2))
            if (!(reasoner isEntailed weakening1))
              nonMinimalWeakenings add weakening2
        })
        reasoner.dispose()
        ontologyManager.removeOntology(ontology)
        System.gc()
      })
      weakenings.removeAll(nonMinimalWeakenings)

      val weakenings4: java.util.Set[OWLAxiom] = semanticELConceptInclusionWeakeningRelation4(dataFactory).getWeakenings(ontologyManager, reasonerFactory, staticOntology, justification, axiom, unwantedConsequence)
      println()
      println("same weakenings found: " + ((weakenings containsAll weakenings4) && (weakenings4 containsAll weakenings)))
      println("\r\nweakenings only found by method number 3:")
      Sets.difference(weakenings, weakenings4).forEach(ax ⇒ println("\r\n" + ax))
      println("\r\nweakenings only found by method number 4:")
      Sets.difference(weakenings4, weakenings).forEach(ax ⇒ println("\r\n" + ax))

      weakenings
    }

  def semanticELConceptInclusionWeakeningRelation4(dataFactory: OWLDataFactory): OWLAxiomWeakeningRelation =
    (ontologyManager, reasonerFactory, staticOntology, justification, axiom, unwantedConsequence) ⇒ {
      if (!axiom.getAxiomType().equals(AxiomType.SUBCLASS_OF))
        throw new IllegalArgumentException("Currently, only concept inclusions are supported.")

      val subClassOfAxiom: OWLSubClassOfAxiom = axiom.asInstanceOf[OWLSubClassOfAxiom]
      val premise: ELConceptDescription = ELConceptDescription.of(subClassOfAxiom.getSubClass)
      val conclusion: ELConceptDescription = ELConceptDescription.of(subClassOfAxiom.getSuperClass)

      val deconstructedAxiom: java.util.Set[OWLSubClassOfAxiom] = new java.util.HashSet
      val n: AtomicInteger = new AtomicInteger(0)
      val x0: ELConceptDescription = ELConceptDescription.conceptName(IRI.create("X(" + conclusion.toString + "," + n.getAndIncrement + ")"))
      val baseAxiom: OWLSubClassOfAxiom = new ELConceptInclusion(premise, x0).toOWLSubClassOfAxiom
      deconstructedAxiom add baseAxiom
      def deconstruct(xm: ELConceptDescription, sub: ELConceptDescription) {
        sub.getConceptNames.forEach(a ⇒ deconstructedAxiom add new ELConceptInclusion(xm, ELConceptDescription.conceptName(a)).toOWLSubClassOfAxiom)
        sub.getExistentialRestrictions.entries.forEach(er ⇒ {
          val role: IRI = er.getKey
          val filler: ELConceptDescription = er.getValue
          val xn: ELConceptDescription = ELConceptDescription.conceptName(IRI.create("X(" + conclusion.toString() + "," + n.getAndIncrement + ")"))
          deconstructedAxiom add new ELConceptInclusion(xm, ELConceptDescription.existentialRestriction(role, xn)).toOWLSubClassOfAxiom
          deconstruct(xn, filler)
        })
      }
      deconstruct(x0, conclusion)
      //      System.out.println("Deconstructed axiom:")
      //      deconstructedAxiom.forEach(System.out println _)

      val axiomJustifications: java.util.Set[java.util.Set[OWLSubClassOfAxiom]] = Sets.newConcurrentHashSet()
      val nextCandidates: java.util.Set[java.util.Set[OWLSubClassOfAxiom]] = Sets.newConcurrentHashSet()
      nextCandidates add java.util.Collections.singleton(baseAxiom)
      while (!nextCandidates.isEmpty()) {
        val currentCandidates: java.util.Set[java.util.Set[OWLSubClassOfAxiom]] = new java.util.HashSet(nextCandidates)
        nextCandidates.clear
        currentCandidates.stream().parallel().forEach(candidate ⇒ {
          val ontology: OWLOntology = ontologyManager.createOntology()
          ontologyManager.addAxioms(ontology, staticOntology.getAxioms)
          ontologyManager.addAxioms(ontology, justification)
          ontologyManager.removeAxiom(ontology, axiom)
          ontologyManager.addAxioms(ontology, candidate)
          val reasoner: OWLReasoner = reasonerFactory.createReasoner(ontology)
          if (reasoner.isEntailed(unwantedConsequence))
            axiomJustifications add candidate // we need to minimize here
          else {
            deconstructedAxiom.stream().filter(!candidate.contains(_)).forEach(ax ⇒ {
              val newCandidate: java.util.Set[OWLSubClassOfAxiom] = new java.util.HashSet
              newCandidate addAll candidate
              newCandidate add ax
              nextCandidates add newCandidate
            })
          }
          reasoner.dispose
          ontologyManager.removeOntology(ontology)
        })
      }
      val nonMinimalAxiomJustifications: java.util.Set[java.util.Set[OWLSubClassOfAxiom]] = Sets.newConcurrentHashSet()
      axiomJustifications.stream().parallel().forEach(j1 ⇒ {
        axiomJustifications.stream().parallel().forEach(j2 ⇒ {
          if (j1.containsAll(j2) && !j2.containsAll(j1))
            nonMinimalAxiomJustifications add j1
        })
      })
      axiomJustifications removeAll nonMinimalAxiomJustifications

      //      axiomJustifications.forEach(just ⇒ {
      //        System.out.println()
      //        System.out.println("Justification:")
      //        just.forEach(System.out println _)
      //      })

      val policy: java.util.Set[ELConceptDescription] = Sets.newConcurrentHashSet()
      def construct(c: ELConceptDescription, xm: ELConceptDescription, j: java.util.Set[OWLSubClassOfAxiom]) {
        j.stream().filter(_.getSubClass() equals xm.toOWLClassExpression()).forEach(ax ⇒ {
          val atom: ELConceptDescription = ELConceptDescription.of(ax.getSuperClass())
          if (atom.getConceptNames.size + atom.getExistentialRestrictions.size != 1)
            throw new RuntimeException("Unexpected value.")
          if (!atom.getConceptNames.isEmpty)
            c.getConceptNames.addAll(atom.getConceptNames())
          else {
            val er: java.util.Map.Entry[IRI, ELConceptDescription] = atom.getExistentialRestrictions.entries().iterator().next()
            val r: IRI = er.getKey
            val xn: ELConceptDescription = er.getValue
            val d: ELConceptDescription = new ELConceptDescription
            c.getExistentialRestrictions.put(r, d)
            construct(d, xn, j)
          }
        })
      }
      axiomJustifications.stream().parallel().forEach(j ⇒ {
        val p: ELConceptDescription = new ELConceptDescription
        construct(p, x0, j)
        policy add p
      })

      //      System.out.println()
      //      System.out.println("Policy:")
      //      policy.forEach(System.out println _.toShortString())

      val scgs: java.util.Set[ELConceptDescription] = specificCompliantGeneralizations(conclusion, policy)
      val weakenings: java.util.Set[OWLAxiom] = Sets.newConcurrentHashSet()
      scgs.forEach(scg ⇒ {
        val weakening: OWLAxiom = new ELConceptInclusion(premise, scg).toOWLSubClassOfAxiom()
        val ontology: OWLOntology = ontologyManager.createOntology
        ontologyManager.addAxioms(ontology, staticOntology.getAxioms)
        ontologyManager.addAxioms(ontology, justification)
        ontologyManager.removeAxiom(ontology, axiom)
        ontologyManager.addAxiom(ontology, weakening)
        val reasoner: OWLReasoner = reasonerFactory createReasoner ontology
        if (!(reasoner isEntailed unwantedConsequence))
          weakenings add weakening
        reasoner dispose ()
        ontologyManager removeOntology ontology
      })
      System.out.println()
      System.out.println("Weakened Axioms:")
      weakenings.forEach(ax ⇒ println("\r\n" + ax))
      println("\r\ndone.")

      weakenings
    }

  //  def subsetOfByPredicate[T](predicate: (T, T) ⇒ Boolean, set1: Set[T], set2: Set[T]): Boolean = {
  //    set1.forall(x ⇒ set2.exists(y ⇒ predicate(x, y)))
  //  }

  def specificCompliantGeneralizations(concept: ELConceptDescription, policy: java.util.Set[ELConceptDescription]): java.util.Set[ELConceptDescription] = {
    val _concept: ELConceptDescription = concept.clone.reduce
    val _policy: java.util.Set[ELConceptDescription] = new java.util.HashSet(policy.asScala.map(_.clone.reduce).asJava)
    val redundantPolicyConcepts: java.util.Set[ELConceptDescription] = Sets.newConcurrentHashSet()
    _policy.stream().parallel().forEach(p ⇒ {
      _policy.stream().parallel().forEach(q ⇒ {
        if (!(p equals q) && (p compareTo q) == 1)
          redundantPolicyConcepts add q
      })
    })
    _policy removeAll redundantPolicyConcepts
    if (_policy.stream.parallel.anyMatch(_ isTop))
      java.util.Collections.emptySet()
    if (_policy.stream.parallel.noneMatch(_ subsumes _concept))
      java.util.Collections.singleton(_concept)
    val scgs: java.util.Set[ELConceptDescription] = Sets.newConcurrentHashSet()
    val hypergraph: Set[Set[ELConceptDescription]] = _policy.asScala.map(_.topLevelConjuncts.asScala.toSet).to[scala.collection.immutable.Set]
    val hittingSets: Set[Set[ELConceptDescription]] = cartesianProduct(hypergraph)
    var nonMinimalHittingSets: scala.collection.mutable.Set[Set[ELConceptDescription]] = Sets.newConcurrentHashSet().asScala
    hittingSets.par.foreach(h1 ⇒
      hittingSets.par.foreach(h2 ⇒
        if (!(h1 equals h2)
          && (h1 subsetOf h2) // && subsetOfByPredicate[ELConceptDescription](_ equals _, h1, h2)
          && !(h2 subsetOf h1)) // && !subsetOfByPredicate[ELConceptDescription](_ equals _, h2, h1))
          nonMinimalHittingSets add h2))
    val minimalHittingSets: Set[Set[ELConceptDescription]] = hittingSets -- nonMinimalHittingSets
    minimalHittingSets.par.foreach(h ⇒ {
      val scg: ELConceptDescription = _concept.clone
      h.map(_ getConceptNames).foreach(as ⇒ scg.getConceptNames.removeAll(as))
      _concept.getExistentialRestrictions.entries().forEach(er ⇒ {
        val role: IRI = er.getKey
        val filler: ELConceptDescription = er.getValue
        val qolicy: java.util.Set[ELConceptDescription] = h.filter(_.getConceptNames.isEmpty)
          .filter(_.getExistentialRestrictions containsKey role)
          .map(_.getExistentialRestrictions.get(role).iterator().next())
          .filter(_ subsumes filler)
          .asJava
        if (!qolicy.isEmpty()) {
          scg.getExistentialRestrictions.remove(role, filler)
          //          if (!qolicy.asScala.exists(_ isTop))
          specificCompliantGeneralizations(filler, qolicy)
            .forEach(scg.getExistentialRestrictions.put(role, _))
        }
      })
      scgs add scg
    })
    //    var str: String = "concept: " + concept.toShortString() + "\r\n"
    //    str += "policy:\r\n"
    //    policy.forEach(str += "- " + _.toShortString() + "\r\n")
    //    var m = 0
    //    str += "minimal hitting sets:\r\n"
    //    minimalHittingSets.foreach(hs ⇒ {
    //      m += 1
    //      str += m + ": "
    //      hs.foreach(str += _.toShortString() + ", ")
    //      str += "\r\n"
    //    })
    //    var n = 0
    //    str += "specific compliant generalizations:\r\n"
    //    scgs.forEach(scg ⇒ {
    //      n += 1
    //      str += n + ": " + scg.toShortString() + "\r\n"
    //    })
    //    println(str)
    scgs
  }

  /**
   * returns the set of those sets which contain exactly one element from each set in the input set.
   */
  def cartesianProduct[T](sets: Set[Set[T]]): Set[Set[T]] = {
    if (sets.isEmpty)
      Set.empty[Set[T]]
    else {
      val seed: Set[Set[T]] = Set(Set())
      val foldFunction: ((Set[Set[T]], Set[T]) ⇒ Set[Set[T]]) = {
        (xs: Set[Set[T]], ys: Set[T]) ⇒ xs.flatMap((x: Set[T]) ⇒ ys.map((y: T) ⇒ x + y))
      }
      sets.foldLeft(seed)(foldFunction)
    }
  }

  def syntacticELConceptInclusionWeakeningRelation(dataFactory: OWLDataFactory): OWLAxiomWeakeningRelation =
    (ontologyManager, reasonerFactory, staticOntology, justification, axiom, unwantedConsequence) ⇒ {
      if (!axiom.getAxiomType().equals(AxiomType.SUBCLASS_OF))
        throw new IllegalArgumentException("Currently, only concept inclusions are supported.")
      val subClassOfAxiom: OWLSubClassOfAxiom = axiom.asInstanceOf[OWLSubClassOfAxiom]
      throw new RuntimeException("To be implemented.")
    }

}
