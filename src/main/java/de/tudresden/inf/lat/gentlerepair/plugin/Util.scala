package de.tudresden.inf.lat.gentlerepair.plugin

import java.io.OutputStream
import java.lang.reflect.InvocationTargetException
import java.util.Collection
import java.util.Collections
import java.util.Optional
import java.util.Random
import java.util.Set
import java.util.concurrent.ExecutionException
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import java.util.function.Function

import javax.swing.SwingUtilities

import org.semanticweb.owlapi.io.OWLOntologyDocumentTarget
import org.semanticweb.owlapi.model.AxiomType
import org.semanticweb.owlapi.model.IRI
import org.semanticweb.owlapi.model.OWLAnnotation
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom
import org.semanticweb.owlapi.model.OWLAnnotationAxiom
import org.semanticweb.owlapi.model.OWLAnnotationProperty
import org.semanticweb.owlapi.model.OWLAnnotationPropertyDomainAxiom
import org.semanticweb.owlapi.model.OWLAnnotationPropertyRangeAxiom
import org.semanticweb.owlapi.model.OWLAnnotationSubject
import org.semanticweb.owlapi.model.OWLAnonymousIndividual
import org.semanticweb.owlapi.model.OWLAsymmetricObjectPropertyAxiom
import org.semanticweb.owlapi.model.OWLAxiom
import org.semanticweb.owlapi.model.OWLClass
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom
import org.semanticweb.owlapi.model.OWLClassAxiom
import org.semanticweb.owlapi.model.OWLClassExpression
import org.semanticweb.owlapi.model.OWLDataProperty
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom
import org.semanticweb.owlapi.model.OWLDataPropertyAxiom
import org.semanticweb.owlapi.model.OWLDataPropertyDomainAxiom
import org.semanticweb.owlapi.model.OWLDataPropertyExpression
import org.semanticweb.owlapi.model.OWLDataPropertyRangeAxiom
import org.semanticweb.owlapi.model.OWLDatatype
import org.semanticweb.owlapi.model.OWLDatatypeDefinitionAxiom
import org.semanticweb.owlapi.model.OWLDeclarationAxiom
import org.semanticweb.owlapi.model.OWLDifferentIndividualsAxiom
import org.semanticweb.owlapi.model.OWLDisjointClassesAxiom
import org.semanticweb.owlapi.model.OWLDisjointDataPropertiesAxiom
import org.semanticweb.owlapi.model.OWLDisjointObjectPropertiesAxiom
import org.semanticweb.owlapi.model.OWLDisjointUnionAxiom
import org.semanticweb.owlapi.model.OWLDocumentFormat
import org.semanticweb.owlapi.model.OWLEntity
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom
import org.semanticweb.owlapi.model.OWLEquivalentDataPropertiesAxiom
import org.semanticweb.owlapi.model.OWLEquivalentObjectPropertiesAxiom
import org.semanticweb.owlapi.model.OWLFunctionalDataPropertyAxiom
import org.semanticweb.owlapi.model.OWLFunctionalObjectPropertyAxiom
import org.semanticweb.owlapi.model.OWLHasKeyAxiom
import org.semanticweb.owlapi.model.OWLImportsDeclaration
import org.semanticweb.owlapi.model.OWLIndividual
import org.semanticweb.owlapi.model.OWLIndividualAxiom
import org.semanticweb.owlapi.model.OWLInverseFunctionalObjectPropertyAxiom
import org.semanticweb.owlapi.model.OWLInverseObjectPropertiesAxiom
import org.semanticweb.owlapi.model.OWLIrreflexiveObjectPropertyAxiom
import org.semanticweb.owlapi.model.OWLLogicalAxiom
import org.semanticweb.owlapi.model.OWLNamedIndividual
import org.semanticweb.owlapi.model.OWLNamedObjectVisitor
import org.semanticweb.owlapi.model.OWLNamedObjectVisitorEx
import org.semanticweb.owlapi.model.OWLNegativeDataPropertyAssertionAxiom
import org.semanticweb.owlapi.model.OWLNegativeObjectPropertyAssertionAxiom
import org.semanticweb.owlapi.model.OWLObject
import org.semanticweb.owlapi.model.OWLObjectProperty
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom
import org.semanticweb.owlapi.model.OWLObjectPropertyAxiom
import org.semanticweb.owlapi.model.OWLObjectPropertyDomainAxiom
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression
import org.semanticweb.owlapi.model.OWLObjectPropertyRangeAxiom
import org.semanticweb.owlapi.model.OWLObjectVisitor
import org.semanticweb.owlapi.model.OWLObjectVisitorEx
import org.semanticweb.owlapi.model.OWLOntology
import org.semanticweb.owlapi.model.OWLOntologyID
import org.semanticweb.owlapi.model.OWLOntologyManager
import org.semanticweb.owlapi.model.OWLOntologyStorageException
import org.semanticweb.owlapi.model.OWLPrimitive
import org.semanticweb.owlapi.model.OWLReflexiveObjectPropertyAxiom
import org.semanticweb.owlapi.model.OWLSameIndividualAxiom
import org.semanticweb.owlapi.model.OWLSubAnnotationPropertyOfAxiom
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom
import org.semanticweb.owlapi.model.OWLSubDataPropertyOfAxiom
import org.semanticweb.owlapi.model.OWLSubObjectPropertyOfAxiom
import org.semanticweb.owlapi.model.OWLSymmetricObjectPropertyAxiom
import org.semanticweb.owlapi.model.OWLTransitiveObjectPropertyAxiom
import org.semanticweb.owlapi.model.parameters.AxiomAnnotations
import org.semanticweb.owlapi.model.parameters.Imports
import org.semanticweb.owlapi.model.parameters.Navigation
import org.semanticweb.owlapi.util.OWLAxiomSearchFilter

object Util {

  private val random: Random = new Random()

  def getRandomElement[E](c: Collection[E]): Optional[E] = {
    if (c.isEmpty())
      return Optional.empty()
    return c.stream().skip(random.nextInt(c.size())).findFirst()
  }

  def randomSelector[E >: Null <: AnyRef](set: Set[E]): Future[E] =
    new Future[E]() {

      private val result: E = getRandomElement(set).orElse(null)

      override def cancel(mayInterruptIfRunning: Boolean): Boolean = false
      override def isCancelled(): Boolean = false
      override def isDone(): Boolean = true

      @throws(classOf[InterruptedException])
      @throws(classOf[ExecutionException])
      override def get(): E = result

      @throws(classOf[InterruptedException])
      @throws(classOf[ExecutionException])
      @throws(classOf[TimeoutException])
      override def get(timeout: Long, unit: TimeUnit): E = result
    }

  def runOnProtegeThread(runnable: Runnable, sync: Boolean) {
    try {
      if (sync)
        SwingUtilities.invokeAndWait(runnable)
      else
        SwingUtilities.invokeLater(runnable)
      // System.out.println("Started on GUI thread.")
    } catch {
      case e: Error ⇒
        if (e.getMessage().equals("Cannot call invokeAndWait from the event dispatcher thread")) {
          // System.out.println("Already in GUI thread, running here.")
          runnable.run()
        } else throw new RuntimeException(e)
      case e @ (_: InvocationTargetException | _: InterruptedException) ⇒ throw new RuntimeException(e)
    }
  }

  val EMPTY_ONTOLOGY: OWLOntology = new OWLOntology() {
    private val serialVersionUID: Long = -3491651253007787369L

    override def getTransitiveObjectPropertyAxioms(
      property: OWLObjectPropertyExpression): Set[OWLTransitiveObjectPropertyAxiom] =
      null

    override def getSymmetricObjectPropertyAxioms(
      property: OWLObjectPropertyExpression): Set[OWLSymmetricObjectPropertyAxiom] =
      null

    override def getSubClassAxiomsForSuperClass(cls: OWLClass): Set[OWLSubClassOfAxiom] =
      null

    override def getSubClassAxiomsForSubClass(cls: OWLClass): Set[OWLSubClassOfAxiom] =
      null

    override def getSubAnnotationPropertyOfAxioms(
      subProperty: OWLAnnotationProperty): Set[OWLSubAnnotationPropertyOfAxiom] =
      null

    override def getSameIndividualAxioms(individual: OWLIndividual): Set[OWLSameIndividualAxiom] =
      null

    override def getReflexiveObjectPropertyAxioms(
      property: OWLObjectPropertyExpression): Set[OWLReflexiveObjectPropertyAxiom] =
      null

    override def getObjectSubPropertyAxiomsForSuperProperty(
      superProperty: OWLObjectPropertyExpression): Set[OWLSubObjectPropertyOfAxiom] =
      null

    override def getObjectSubPropertyAxiomsForSubProperty(
      subProperty: OWLObjectPropertyExpression): Set[OWLSubObjectPropertyOfAxiom] =
      null

    override def getObjectPropertyRangeAxioms(
      property: OWLObjectPropertyExpression): Set[OWLObjectPropertyRangeAxiom] =
      null

    override def getObjectPropertyDomainAxioms(
      property: OWLObjectPropertyExpression): Set[OWLObjectPropertyDomainAxiom] =
      null

    override def getObjectPropertyAssertionAxioms(individual: OWLIndividual): Set[OWLObjectPropertyAssertionAxiom] =
      null

    override def getNegativeObjectPropertyAssertionAxioms(
      individual: OWLIndividual): Set[OWLNegativeObjectPropertyAssertionAxiom] =
      null

    override def getNegativeDataPropertyAssertionAxioms(
      individual: OWLIndividual): Set[OWLNegativeDataPropertyAssertionAxiom] =
      null

    override def getIrreflexiveObjectPropertyAxioms(
      property: OWLObjectPropertyExpression): Set[OWLIrreflexiveObjectPropertyAxiom] =
      null

    override def getInverseObjectPropertyAxioms(
      property: OWLObjectPropertyExpression): Set[OWLInverseObjectPropertiesAxiom] =
      null

    override def getInverseFunctionalObjectPropertyAxioms(
      property: OWLObjectPropertyExpression): Set[OWLInverseFunctionalObjectPropertyAxiom] =
      null

    override def getHasKeyAxioms(cls: OWLClass): Set[OWLHasKeyAxiom] =
      null

    override def getFunctionalObjectPropertyAxioms(
      property: OWLObjectPropertyExpression): Set[OWLFunctionalObjectPropertyAxiom] =
      null

    override def getFunctionalDataPropertyAxioms(
      property: OWLDataPropertyExpression): Set[OWLFunctionalDataPropertyAxiom] =
      null

    override def getEquivalentObjectPropertiesAxioms(
      property: OWLObjectPropertyExpression): Set[OWLEquivalentObjectPropertiesAxiom] =
      null

    override def getEquivalentDataPropertiesAxioms(property: OWLDataProperty): Set[OWLEquivalentDataPropertiesAxiom] =
      null

    override def getEquivalentClassesAxioms(cls: OWLClass): Set[OWLEquivalentClassesAxiom] =
      null

    override def getDisjointUnionAxioms(owlClass: OWLClass): Set[OWLDisjointUnionAxiom] =
      null

    override def getDisjointObjectPropertiesAxioms(
      property: OWLObjectPropertyExpression): Set[OWLDisjointObjectPropertiesAxiom] =
      null

    override def getDisjointDataPropertiesAxioms(property: OWLDataProperty): Set[OWLDisjointDataPropertiesAxiom] =
      null

    override def getDisjointClassesAxioms(cls: OWLClass): Set[OWLDisjointClassesAxiom] =
      null

    override def getDifferentIndividualAxioms(individual: OWLIndividual): Set[OWLDifferentIndividualsAxiom] =
      null

    override def getDeclarationAxioms(subject: OWLEntity): Set[OWLDeclarationAxiom] =
      null

    override def getDatatypeDefinitions(datatype: OWLDatatype): Set[OWLDatatypeDefinitionAxiom] =
      null

    override def getDataSubPropertyAxiomsForSuperProperty(
      superProperty: OWLDataPropertyExpression): Set[OWLSubDataPropertyOfAxiom] =
      null

    override def getDataSubPropertyAxiomsForSubProperty(
      subProperty: OWLDataProperty): Set[OWLSubDataPropertyOfAxiom] =
      null

    override def getDataPropertyRangeAxioms(property: OWLDataProperty): Set[OWLDataPropertyRangeAxiom] =
      null

    override def getDataPropertyDomainAxioms(property: OWLDataProperty): Set[OWLDataPropertyDomainAxiom] =
      null

    override def getDataPropertyAssertionAxioms(individual: OWLIndividual): Set[OWLDataPropertyAssertionAxiom] =
      null

    override def getClassAssertionAxioms(ce: OWLClassExpression): Set[OWLClassAssertionAxiom] =
      null

    override def getClassAssertionAxioms(individual: OWLIndividual): Set[OWLClassAssertionAxiom] =
      null

    override def getAxioms[T <: OWLAxiom](
      `type`:         Class[T],
      explicitClass:  Class[_ <: OWLObject],
      entity:         OWLObject,
      includeImports: Imports,
      forSubPosition: Navigation): Set[T] =
      null

    override def getAxioms[T <: OWLAxiom](
      `type`:         Class[T],
      entity:         OWLObject,
      includeImports: Imports,
      forSubPosition: Navigation): Set[T] =
      null

    override def getAsymmetricObjectPropertyAxioms(
      property: OWLObjectPropertyExpression): Set[OWLAsymmetricObjectPropertyAxiom] =
      null

    override def getAnnotationPropertyRangeAxioms(
      property: OWLAnnotationProperty): Set[OWLAnnotationPropertyRangeAxiom] =
      null

    override def getAnnotationPropertyDomainAxioms(
      property: OWLAnnotationProperty): Set[OWLAnnotationPropertyDomainAxiom] =
      null

    override def getAnnotationAssertionAxioms(entity: OWLAnnotationSubject): Set[OWLAnnotationAssertionAxiom] =
      null

    override def filterAxioms[T <: OWLAxiom](
      filter:                OWLAxiomSearchFilter,
      key:                   AnyRef,
      includeImportsClosure: Imports): Collection[T] =
      null

    override def contains(
      filter:                OWLAxiomSearchFilter,
      key:                   AnyRef,
      includeImportsClosure: Imports): Boolean =
      false

    override def getReferencedAnonymousIndividuals(
      includeImportsClosure: Boolean): Set[OWLAnonymousIndividual] =
      null

    override def getObjectPropertiesInSignature(includeImportsClosure: Boolean): Set[OWLObjectProperty] =
      null

    override def getIndividualsInSignature(includeImportsClosure: Boolean): Set[OWLNamedIndividual] =
      null

    override def getEntitiesInSignature(
      iri:                   IRI,
      includeImportsClosure: Boolean): Set[OWLEntity] =
      null

    override def getDatatypesInSignature(includeImportsClosure: Boolean): Set[OWLDatatype] =
      null

    override def getDataPropertiesInSignature(includeImportsClosure: Boolean): Set[OWLDataProperty] =
      null

    override def getClassesInSignature(includeImportsClosure: Boolean): Set[OWLClass] =
      null

    override def getAnnotationPropertiesInSignature(
      includeImportsClosure: Boolean): Set[OWLAnnotationProperty] =
      null

    override def containsReference(
      entity:                OWLEntity,
      includeImportsClosure: Boolean): Boolean =
      false

    override def containsObjectPropertyInSignature(
      owlObjectPropertyIRI:  IRI,
      includeImportsClosure: Boolean): Boolean =
      false

    override def containsIndividualInSignature(
      owlIndividualIRI:      IRI,
      includeImportsClosure: Boolean): Boolean =
      false

    override def containsEntityInSignature(
      entityIRI:             IRI,
      includeImportsClosure: Boolean): Boolean =
      false

    override def containsEntityInSignature(
      owlEntity:             OWLEntity,
      includeImportsClosure: Boolean): Boolean =
      false

    override def containsDatatypeInSignature(
      owlDatatypeIRI:        IRI,
      includeImportsClosure: Boolean): Boolean =
      false

    override def containsDataPropertyInSignature(
      owlDataPropertyIRI:    IRI,
      includeImportsClosure: Boolean): Boolean =
      false

    override def containsClassInSignature(
      owlClassIRI:           IRI,
      includeImportsClosure: Boolean): Boolean =
      false

    override def containsAnnotationPropertyInSignature(
      owlAnnotationPropertyIRI: IRI,
      includeImportsClosure:    Boolean): Boolean =
      false

    override def getEntitiesInSignature(entityIRI: IRI): Set[OWLEntity] =
      null

    override def getReferencedAnonymousIndividuals(
      includeImportsClosure: Imports): Set[OWLAnonymousIndividual] =
      null

    override def getPunnedIRIs(includeImportsClosure: Imports): Set[IRI] =
      null

    override def getObjectPropertiesInSignature(includeImportsClosure: Imports): Set[OWLObjectProperty] =
      null

    override def getIndividualsInSignature(includeImportsClosure: Imports): Set[OWLNamedIndividual] =
      null

    override def getEntitiesInSignature(
      iri:                   IRI,
      includeImportsClosure: Imports): Set[OWLEntity] =
      null

    override def getDatatypesInSignature(includeImportsClosure: Imports): Set[OWLDatatype] =
      null

    override def getDataPropertiesInSignature(includeImportsClosure: Imports): Set[OWLDataProperty] =
      null

    override def getClassesInSignature(includeImportsClosure: Imports): Set[OWLClass] =
      null

    override def getAnnotationPropertiesInSignature(
      includeImportsClosure: Imports): Set[OWLAnnotationProperty] =
      null

    override def containsReference(
      entity:                OWLEntity,
      includeImportsClosure: Imports): Boolean =
      false

    override def containsReference(entity: OWLEntity): Boolean =
      false

    override def containsObjectPropertyInSignature(
      owlObjectPropertyIRI:  IRI,
      includeImportsClosure: Imports): Boolean =
      false

    override def containsObjectPropertyInSignature(owlObjectPropertyIRI: IRI): Boolean =
      false

    override def containsIndividualInSignature(
      owlIndividualIRI:      IRI,
      includeImportsClosure: Imports): Boolean =
      false

    override def containsIndividualInSignature(
      owlIndividualIRI: IRI): Boolean =
      false

    override def containsEntityInSignature(
      entityIRI:             IRI,
      includeImportsClosure: Imports): Boolean =
      false

    override def containsEntityInSignature(
      owlEntity:             OWLEntity,
      includeImportsClosure: Imports): Boolean =
      false

    override def containsEntityInSignature(entityIRI: IRI): Boolean =
      false

    override def containsDatatypeInSignature(
      owlDatatypeIRI:        IRI,
      includeImportsClosure: Imports): Boolean =
      false

    override def containsDatatypeInSignature(owlDatatypeIRI: IRI): Boolean =
      false

    override def containsDataPropertyInSignature(
      owlDataPropertyIRI:    IRI,
      includeImportsClosure: Imports): Boolean =
      false

    override def containsDataPropertyInSignature(
      owlDataPropertyIRI: IRI): Boolean =
      false

    override def containsClassInSignature(
      owlClassIRI:           IRI,
      includeImportsClosure: Imports): Boolean =
      false

    override def containsClassInSignature(owlClassIRI: IRI): Boolean =
      false

    override def containsAnnotationPropertyInSignature(
      owlAnnotationPropertyIRI: IRI,
      includeImportsClosure:    Imports): Boolean =
      false

    override def containsAnnotationPropertyInSignature(
      owlAnnotationPropertyIRI: IRI): Boolean =
      false

    override def getReferencingAxioms(owlEntity: OWLPrimitive): Set[OWLAxiom] =
      null

    override def getLogicalAxiomCount(): Int =
      0

    override def getAxiomsIgnoreAnnotations(axiom: OWLAxiom): Set[OWLAxiom] =
      null

    override def getAxioms(datatype: OWLDatatype): Set[OWLDatatypeDefinitionAxiom] =
      null

    override def getAxioms(property: OWLAnnotationProperty): Set[OWLAnnotationAxiom] =
      null

    override def getAxioms(individual: OWLIndividual): Set[OWLIndividualAxiom] =
      null

    override def getAxioms(property: OWLDataProperty): Set[OWLDataPropertyAxiom] =
      null

    override def getAxioms(property: OWLObjectPropertyExpression): Set[OWLObjectPropertyAxiom] =
      null

    override def getAxioms(cls: OWLClass): Set[OWLClassAxiom] =
      null

    override def getAxiomCount[T <: OWLAxiom](axiomType: AxiomType[T]): Int =
      0

    override def getAxiomCount(): Int =
      0

    override def containsAxiomIgnoreAnnotations(axiom: OWLAxiom): Boolean =
      false

    override def getReferencingAxioms(
      owlEntity:             OWLPrimitive,
      includeImportsClosure: Boolean): Set[OWLAxiom] =
      null

    override def getLogicalAxioms(includeImportsClosure: Boolean): Set[OWLLogicalAxiom] =
      null

    override def getLogicalAxiomCount(includeImportsClosure: Boolean): Int =
      0

    override def getAxiomsIgnoreAnnotations(
      axiom:                 OWLAxiom,
      includeImportsClosure: Boolean): Set[OWLAxiom] =
      null

    override def getAxioms(
      datatype:              OWLDatatype,
      includeImportsClosure: Boolean): Set[OWLDatatypeDefinitionAxiom] =
      null

    override def getAxioms(
      property:              OWLAnnotationProperty,
      includeImportsClosure: Boolean): Set[OWLAnnotationAxiom] =
      null

    override def getAxioms(
      individual:            OWLIndividual,
      includeImportsClosure: Boolean): Set[OWLIndividualAxiom] =
      null

    override def getAxioms(
      property:              OWLDataProperty,
      includeImportsClosure: Boolean): Set[OWLDataPropertyAxiom] =
      null

    override def getAxioms(
      property:              OWLObjectPropertyExpression,
      includeImportsClosure: Boolean): Set[OWLObjectPropertyAxiom] =
      null

    override def getAxioms(cls: OWLClass, includeImportsClosure: Boolean): Set[OWLClassAxiom] =
      null

    override def getAxioms[T <: OWLAxiom](
      axiomType:             AxiomType[T],
      includeImportsClosure: Boolean): Set[T] =
      null

    override def getAxioms(b: Boolean): Set[OWLAxiom] =
      null

    override def getAxiomCount[T <: OWLAxiom](
      axiomType:             AxiomType[T],
      includeImportsClosure: Boolean): Int =
      0

    override def getAxiomCount(includeImportsClosure: Boolean): Int =
      0

    override def containsAxiomIgnoreAnnotations(
      axiom:                 OWLAxiom,
      includeImportsClosure: Boolean): Boolean =
      false

    override def containsAxiom(axiom: OWLAxiom, includeImportsClosure: Boolean): Boolean =
      false

    override def containsAxiom(axiom: OWLAxiom): Boolean =
      false

    override def getAxioms[T <: OWLAxiom](axiomType: AxiomType[T]): Set[T] =
      null

    override def getLogicalAxioms(): Set[OWLLogicalAxiom] =
      null

    override def getAxioms(): Set[OWLAxiom] = Collections.emptySet()

    override def getReferencingAxioms(
      owlEntity:             OWLPrimitive,
      includeImportsClosure: Imports): Set[OWLAxiom] =
      null

    override def getLogicalAxioms(includeImportsClosure: Imports): Set[OWLLogicalAxiom] =
      null

    override def getLogicalAxiomCount(includeImportsClosure: Imports): Int =
      0

    override def getAxiomsIgnoreAnnotations(
      axiom:                 OWLAxiom,
      includeImportsClosure: Imports): Set[OWLAxiom] =
      null

    override def getAxioms(
      datatype:              OWLDatatype,
      includeImportsClosure: Imports): Set[OWLDatatypeDefinitionAxiom] =
      null

    override def getAxioms(
      property:              OWLAnnotationProperty,
      includeImportsClosure: Imports): Set[OWLAnnotationAxiom] =
      null

    override def getAxioms(
      individual:            OWLIndividual,
      includeImportsClosure: Imports): Set[OWLIndividualAxiom] =
      null

    override def getAxioms(
      property:              OWLDataProperty,
      includeImportsClosure: Imports): Set[OWLDataPropertyAxiom] =
      null

    override def getAxioms(
      property:              OWLObjectPropertyExpression,
      includeImportsClosure: Imports): Set[OWLObjectPropertyAxiom] =
      null

    override def getAxioms(cls: OWLClass, includeImportsClosure: Imports): Set[OWLClassAxiom] =
      null

    override def getAxioms[T <: OWLAxiom](
      axiomType:             AxiomType[T],
      includeImportsClosure: Imports): Set[T] =
      null

    override def getAxioms(includeImportsClosure: Imports): Set[OWLAxiom] =
      null

    override def getAxiomCount[T <: OWLAxiom](
      axiomType:             AxiomType[T],
      includeImportsClosure: Imports): Int =
      0

    override def getAxiomCount(includeImportsClosure: Imports): Int =
      0

    override def containsAxiom(
      axiom:                 OWLAxiom,
      includeImportsClosure: Imports,
      ignoreAnnotations:     AxiomAnnotations): Boolean =
      false

    override def getAnnotationPropertiesInSignature(): Set[OWLAnnotationProperty] =
      null

    override def getDatatypesInSignature(): Set[OWLDatatype] =
      null

    override def getIndividualsInSignature(): Set[OWLNamedIndividual] =
      null

    override def getDataPropertiesInSignature(): Set[OWLDataProperty] =
      null

    override def getObjectPropertiesInSignature(): Set[OWLObjectProperty] =
      null

    override def getClassesInSignature(): Set[OWLClass] =
      null

    override def getAnonymousIndividuals(): Set[OWLAnonymousIndividual] =
      null

    override def containsEntityInSignature(owlEntity: OWLEntity): Boolean =
      false

    override def compareTo(o: OWLObject): Int =
      0

    override def isTopEntity(): Boolean =
      false

    override def isBottomEntity(): Boolean =
      false

    override def getNestedClassExpressions(): Set[OWLClassExpression] =
      null

    override def accept[O](visitor: OWLObjectVisitorEx[O]): O =
      ???

    override def accept(visitor: OWLObjectVisitor): Unit = {}

    override def setOWLOntologyManager(manager: OWLOntologyManager): Unit = {}

    override def saveOntology(
      ontologyFormat: OWLDocumentFormat,
      documentTarget: OWLOntologyDocumentTarget): Unit = {}

    override def saveOntology(
      ontologyFormat: OWLDocumentFormat,
      outputStream:   OutputStream): Unit = {}

    override def saveOntology(
      ontologyFormat: OWLDocumentFormat,
      documentIRI:    IRI): Unit = {}

    override def saveOntology(
      documentTarget: OWLOntologyDocumentTarget): Unit = {}

    override def saveOntology(ontologyFormat: OWLDocumentFormat): Unit = {}

    override def saveOntology(outputStream: OutputStream): Unit = {}

    override def saveOntology(documentIRI: IRI): Unit = {}

    override def saveOntology(): Unit = {}

    override def isEmpty(): Boolean =
      false

    override def isDeclared(
      owlEntity:             OWLEntity,
      includeImportsClosure: Imports): Boolean =
      false

    override def isDeclared(owlEntity: OWLEntity): Boolean =
      false

    override def isAnonymous(): Boolean =
      false

    override def getTBoxAxioms(includeImportsClosure: Imports): Set[OWLAxiom] =
      null

    override def getSignature(includeImportsClosure: Imports): Set[OWLEntity] =
      null

    override def getSignature(): Set[OWLEntity] =
      null

    override def getRBoxAxioms(includeImportsClosure: Imports): Set[OWLAxiom] =
      null

    override def getOntologyID(): OWLOntologyID =
      new OWLOntologyID(IRI.create("urn:the-empty-ontology"))

    override def getOWLOntologyManager(): OWLOntologyManager =
      null

    override def getImportsDeclarations(): Set[OWLImportsDeclaration] =
      null

    override def getImportsClosure(): Set[OWLOntology] =
      null

    override def getImports(): Set[OWLOntology] =
      null

    override def getGeneralClassAxioms(): Set[OWLClassAxiom] =
      null

    override def getDirectImportsDocuments(): Set[IRI] =
      null

    override def getDirectImports(): Set[OWLOntology] =
      null

    override def getAnnotations(): Set[OWLAnnotation] =
      null

    override def getABoxAxioms(includeImportsClosure: Imports): Set[OWLAxiom] =
      null

    override def accept[O](visitor: OWLNamedObjectVisitorEx[O]): O =
      ???

    override def accept(visitor: OWLNamedObjectVisitor): Unit = {}

  }

}
