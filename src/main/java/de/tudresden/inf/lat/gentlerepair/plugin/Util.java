package de.tudresden.inf.lat.gentlerepair.plugin;

import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

import javax.swing.SwingUtilities;

import org.semanticweb.owlapi.io.OWLOntologyDocumentTarget;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAnnotationPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationSubject;
import org.semanticweb.owlapi.model.OWLAnonymousIndividual;
import org.semanticweb.owlapi.model.OWLAsymmetricObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClassAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLDataPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLDatatypeDefinitionAxiom;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLDifferentIndividualsAxiom;
import org.semanticweb.owlapi.model.OWLDisjointClassesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointDataPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointUnionAxiom;
import org.semanticweb.owlapi.model.OWLDocumentFormat;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentDataPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLFunctionalDataPropertyAxiom;
import org.semanticweb.owlapi.model.OWLFunctionalObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLHasKeyAxiom;
import org.semanticweb.owlapi.model.OWLImportsDeclaration;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLIndividualAxiom;
import org.semanticweb.owlapi.model.OWLInverseFunctionalObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLInverseObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLIrreflexiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLNamedObjectVisitor;
import org.semanticweb.owlapi.model.OWLNamedObjectVisitorEx;
import org.semanticweb.owlapi.model.OWLNegativeDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLNegativeObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLObjectVisitor;
import org.semanticweb.owlapi.model.OWLObjectVisitorEx;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.OWLPrimitive;
import org.semanticweb.owlapi.model.OWLReflexiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLSameIndividualAxiom;
import org.semanticweb.owlapi.model.OWLSubAnnotationPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.OWLSubDataPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubObjectPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSymmetricObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLTransitiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.parameters.AxiomAnnotations;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.model.parameters.Navigation;
import org.semanticweb.owlapi.util.OWLAxiomSearchFilter;

public final class Util {

  static final OWLOntology EMPTY_ONTOLOGY = new OWLOntology() {

    private static final long serialVersionUID = -3491651253007787369L;

    @Override
    public Set<OWLTransitiveObjectPropertyAxiom>
        getTransitiveObjectPropertyAxioms(OWLObjectPropertyExpression property) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public Set<OWLSymmetricObjectPropertyAxiom> getSymmetricObjectPropertyAxioms(OWLObjectPropertyExpression property) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public Set<OWLSubClassOfAxiom> getSubClassAxiomsForSuperClass(OWLClass cls) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public Set<OWLSubClassOfAxiom> getSubClassAxiomsForSubClass(OWLClass cls) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public Set<OWLSubAnnotationPropertyOfAxiom> getSubAnnotationPropertyOfAxioms(OWLAnnotationProperty subProperty) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public Set<OWLSameIndividualAxiom> getSameIndividualAxioms(OWLIndividual individual) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public Set<OWLReflexiveObjectPropertyAxiom> getReflexiveObjectPropertyAxioms(OWLObjectPropertyExpression property) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public Set<OWLSubObjectPropertyOfAxiom>
        getObjectSubPropertyAxiomsForSuperProperty(OWLObjectPropertyExpression superProperty) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public Set<OWLSubObjectPropertyOfAxiom>
        getObjectSubPropertyAxiomsForSubProperty(OWLObjectPropertyExpression subProperty) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public Set<OWLObjectPropertyRangeAxiom> getObjectPropertyRangeAxioms(OWLObjectPropertyExpression property) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public Set<OWLObjectPropertyDomainAxiom> getObjectPropertyDomainAxioms(OWLObjectPropertyExpression property) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public Set<OWLObjectPropertyAssertionAxiom> getObjectPropertyAssertionAxioms(OWLIndividual individual) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public Set<OWLNegativeObjectPropertyAssertionAxiom>
        getNegativeObjectPropertyAssertionAxioms(OWLIndividual individual) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public Set<OWLNegativeDataPropertyAssertionAxiom> getNegativeDataPropertyAssertionAxioms(OWLIndividual individual) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public Set<OWLIrreflexiveObjectPropertyAxiom>
        getIrreflexiveObjectPropertyAxioms(OWLObjectPropertyExpression property) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public Set<OWLInverseObjectPropertiesAxiom> getInverseObjectPropertyAxioms(OWLObjectPropertyExpression property) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public Set<OWLInverseFunctionalObjectPropertyAxiom>
        getInverseFunctionalObjectPropertyAxioms(OWLObjectPropertyExpression property) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public Set<OWLHasKeyAxiom> getHasKeyAxioms(OWLClass cls) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public Set<OWLFunctionalObjectPropertyAxiom>
        getFunctionalObjectPropertyAxioms(OWLObjectPropertyExpression property) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public Set<OWLFunctionalDataPropertyAxiom> getFunctionalDataPropertyAxioms(OWLDataPropertyExpression property) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public Set<OWLEquivalentObjectPropertiesAxiom>
        getEquivalentObjectPropertiesAxioms(OWLObjectPropertyExpression property) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public Set<OWLEquivalentDataPropertiesAxiom> getEquivalentDataPropertiesAxioms(OWLDataProperty property) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public Set<OWLEquivalentClassesAxiom> getEquivalentClassesAxioms(OWLClass cls) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public Set<OWLDisjointUnionAxiom> getDisjointUnionAxioms(OWLClass owlClass) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public Set<OWLDisjointObjectPropertiesAxiom>
        getDisjointObjectPropertiesAxioms(OWLObjectPropertyExpression property) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public Set<OWLDisjointDataPropertiesAxiom> getDisjointDataPropertiesAxioms(OWLDataProperty property) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public Set<OWLDisjointClassesAxiom> getDisjointClassesAxioms(OWLClass cls) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public Set<OWLDifferentIndividualsAxiom> getDifferentIndividualAxioms(OWLIndividual individual) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public Set<OWLDeclarationAxiom> getDeclarationAxioms(OWLEntity subject) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public Set<OWLDatatypeDefinitionAxiom> getDatatypeDefinitions(OWLDatatype datatype) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public Set<OWLSubDataPropertyOfAxiom>
        getDataSubPropertyAxiomsForSuperProperty(OWLDataPropertyExpression superProperty) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public Set<OWLSubDataPropertyOfAxiom> getDataSubPropertyAxiomsForSubProperty(OWLDataProperty subProperty) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public Set<OWLDataPropertyRangeAxiom> getDataPropertyRangeAxioms(OWLDataProperty property) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public Set<OWLDataPropertyDomainAxiom> getDataPropertyDomainAxioms(OWLDataProperty property) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public Set<OWLDataPropertyAssertionAxiom> getDataPropertyAssertionAxioms(OWLIndividual individual) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public Set<OWLClassAssertionAxiom> getClassAssertionAxioms(OWLClassExpression ce) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public Set<OWLClassAssertionAxiom> getClassAssertionAxioms(OWLIndividual individual) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public <T extends OWLAxiom> Set<T> getAxioms(
        Class<T> type,
        Class<? extends OWLObject> explicitClass,
        OWLObject entity,
        Imports includeImports,
        Navigation forSubPosition) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public <T extends OWLAxiom> Set<T>
        getAxioms(Class<T> type, OWLObject entity, Imports includeImports, Navigation forSubPosition) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public Set<OWLAsymmetricObjectPropertyAxiom>
        getAsymmetricObjectPropertyAxioms(OWLObjectPropertyExpression property) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public Set<OWLAnnotationPropertyRangeAxiom> getAnnotationPropertyRangeAxioms(OWLAnnotationProperty property) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public Set<OWLAnnotationPropertyDomainAxiom> getAnnotationPropertyDomainAxioms(OWLAnnotationProperty property) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public Set<OWLAnnotationAssertionAxiom> getAnnotationAssertionAxioms(OWLAnnotationSubject entity) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public <T extends OWLAxiom> Collection<T>
        filterAxioms(OWLAxiomSearchFilter filter, Object key, Imports includeImportsClosure) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public boolean contains(OWLAxiomSearchFilter filter, Object key, Imports includeImportsClosure) {
      // TODO Auto-generated method stub
      return false;
    }

    @Override
    public Set<OWLAnonymousIndividual> getReferencedAnonymousIndividuals(boolean includeImportsClosure) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public Set<OWLObjectProperty> getObjectPropertiesInSignature(boolean includeImportsClosure) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public Set<OWLNamedIndividual> getIndividualsInSignature(boolean includeImportsClosure) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public Set<OWLEntity> getEntitiesInSignature(IRI iri, boolean includeImportsClosure) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public Set<OWLDatatype> getDatatypesInSignature(boolean includeImportsClosure) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public Set<OWLDataProperty> getDataPropertiesInSignature(boolean includeImportsClosure) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public Set<OWLClass> getClassesInSignature(boolean includeImportsClosure) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public Set<OWLAnnotationProperty> getAnnotationPropertiesInSignature(boolean includeImportsClosure) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public boolean containsReference(OWLEntity entity, boolean includeImportsClosure) {
      // TODO Auto-generated method stub
      return false;
    }

    @Override
    public boolean containsObjectPropertyInSignature(IRI owlObjectPropertyIRI, boolean includeImportsClosure) {
      // TODO Auto-generated method stub
      return false;
    }

    @Override
    public boolean containsIndividualInSignature(IRI owlIndividualIRI, boolean includeImportsClosure) {
      // TODO Auto-generated method stub
      return false;
    }

    @Override
    public boolean containsEntityInSignature(IRI entityIRI, boolean includeImportsClosure) {
      // TODO Auto-generated method stub
      return false;
    }

    @Override
    public boolean containsEntityInSignature(OWLEntity owlEntity, boolean includeImportsClosure) {
      // TODO Auto-generated method stub
      return false;
    }

    @Override
    public boolean containsDatatypeInSignature(IRI owlDatatypeIRI, boolean includeImportsClosure) {
      // TODO Auto-generated method stub
      return false;
    }

    @Override
    public boolean containsDataPropertyInSignature(IRI owlDataPropertyIRI, boolean includeImportsClosure) {
      // TODO Auto-generated method stub
      return false;
    }

    @Override
    public boolean containsClassInSignature(IRI owlClassIRI, boolean includeImportsClosure) {
      // TODO Auto-generated method stub
      return false;
    }

    @Override
    public boolean containsAnnotationPropertyInSignature(IRI owlAnnotationPropertyIRI, boolean includeImportsClosure) {
      // TODO Auto-generated method stub
      return false;
    }

    @Override
    public Set<OWLEntity> getEntitiesInSignature(IRI entityIRI) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public Set<OWLAnonymousIndividual> getReferencedAnonymousIndividuals(Imports includeImportsClosure) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public Set<IRI> getPunnedIRIs(Imports includeImportsClosure) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public Set<OWLObjectProperty> getObjectPropertiesInSignature(Imports includeImportsClosure) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public Set<OWLNamedIndividual> getIndividualsInSignature(Imports includeImportsClosure) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public Set<OWLEntity> getEntitiesInSignature(IRI iri, Imports includeImportsClosure) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public Set<OWLDatatype> getDatatypesInSignature(Imports includeImportsClosure) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public Set<OWLDataProperty> getDataPropertiesInSignature(Imports includeImportsClosure) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public Set<OWLClass> getClassesInSignature(Imports includeImportsClosure) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public Set<OWLAnnotationProperty> getAnnotationPropertiesInSignature(Imports includeImportsClosure) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public boolean containsReference(OWLEntity entity, Imports includeImportsClosure) {
      // TODO Auto-generated method stub
      return false;
    }

    @Override
    public boolean containsReference(OWLEntity entity) {
      // TODO Auto-generated method stub
      return false;
    }

    @Override
    public boolean containsObjectPropertyInSignature(IRI owlObjectPropertyIRI, Imports includeImportsClosure) {
      // TODO Auto-generated method stub
      return false;
    }

    @Override
    public boolean containsObjectPropertyInSignature(IRI owlObjectPropertyIRI) {
      // TODO Auto-generated method stub
      return false;
    }

    @Override
    public boolean containsIndividualInSignature(IRI owlIndividualIRI, Imports includeImportsClosure) {
      // TODO Auto-generated method stub
      return false;
    }

    @Override
    public boolean containsIndividualInSignature(IRI owlIndividualIRI) {
      // TODO Auto-generated method stub
      return false;
    }

    @Override
    public boolean containsEntityInSignature(IRI entityIRI, Imports includeImportsClosure) {
      // TODO Auto-generated method stub
      return false;
    }

    @Override
    public boolean containsEntityInSignature(OWLEntity owlEntity, Imports includeImportsClosure) {
      // TODO Auto-generated method stub
      return false;
    }

    @Override
    public boolean containsEntityInSignature(IRI entityIRI) {
      // TODO Auto-generated method stub
      return false;
    }

    @Override
    public boolean containsDatatypeInSignature(IRI owlDatatypeIRI, Imports includeImportsClosure) {
      // TODO Auto-generated method stub
      return false;
    }

    @Override
    public boolean containsDatatypeInSignature(IRI owlDatatypeIRI) {
      // TODO Auto-generated method stub
      return false;
    }

    @Override
    public boolean containsDataPropertyInSignature(IRI owlDataPropertyIRI, Imports includeImportsClosure) {
      // TODO Auto-generated method stub
      return false;
    }

    @Override
    public boolean containsDataPropertyInSignature(IRI owlDataPropertyIRI) {
      // TODO Auto-generated method stub
      return false;
    }

    @Override
    public boolean containsClassInSignature(IRI owlClassIRI, Imports includeImportsClosure) {
      // TODO Auto-generated method stub
      return false;
    }

    @Override
    public boolean containsClassInSignature(IRI owlClassIRI) {
      // TODO Auto-generated method stub
      return false;
    }

    @Override
    public boolean containsAnnotationPropertyInSignature(IRI owlAnnotationPropertyIRI, Imports includeImportsClosure) {
      // TODO Auto-generated method stub
      return false;
    }

    @Override
    public boolean containsAnnotationPropertyInSignature(IRI owlAnnotationPropertyIRI) {
      // TODO Auto-generated method stub
      return false;
    }

    @Override
    public Set<OWLAxiom> getReferencingAxioms(OWLPrimitive owlEntity) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public int getLogicalAxiomCount() {
      // TODO Auto-generated method stub
      return 0;
    }

    @Override
    public Set<OWLAxiom> getAxiomsIgnoreAnnotations(OWLAxiom axiom) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public Set<OWLDatatypeDefinitionAxiom> getAxioms(OWLDatatype datatype) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public Set<OWLAnnotationAxiom> getAxioms(OWLAnnotationProperty property) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public Set<OWLIndividualAxiom> getAxioms(OWLIndividual individual) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public Set<OWLDataPropertyAxiom> getAxioms(OWLDataProperty property) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public Set<OWLObjectPropertyAxiom> getAxioms(OWLObjectPropertyExpression property) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public Set<OWLClassAxiom> getAxioms(OWLClass cls) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public <T extends OWLAxiom> int getAxiomCount(AxiomType<T> axiomType) {
      // TODO Auto-generated method stub
      return 0;
    }

    @Override
    public int getAxiomCount() {
      // TODO Auto-generated method stub
      return 0;
    }

    @Override
    public boolean containsAxiomIgnoreAnnotations(OWLAxiom axiom) {
      // TODO Auto-generated method stub
      return false;
    }

    @Override
    public Set<OWLAxiom> getReferencingAxioms(OWLPrimitive owlEntity, boolean includeImportsClosure) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public Set<OWLLogicalAxiom> getLogicalAxioms(boolean includeImportsClosure) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public int getLogicalAxiomCount(boolean includeImportsClosure) {
      // TODO Auto-generated method stub
      return 0;
    }

    @Override
    public Set<OWLAxiom> getAxiomsIgnoreAnnotations(OWLAxiom axiom, boolean includeImportsClosure) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public Set<OWLDatatypeDefinitionAxiom> getAxioms(OWLDatatype datatype, boolean includeImportsClosure) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public Set<OWLAnnotationAxiom> getAxioms(OWLAnnotationProperty property, boolean includeImportsClosure) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public Set<OWLIndividualAxiom> getAxioms(OWLIndividual individual, boolean includeImportsClosure) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public Set<OWLDataPropertyAxiom> getAxioms(OWLDataProperty property, boolean includeImportsClosure) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public Set<OWLObjectPropertyAxiom> getAxioms(OWLObjectPropertyExpression property, boolean includeImportsClosure) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public Set<OWLClassAxiom> getAxioms(OWLClass cls, boolean includeImportsClosure) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public <T extends OWLAxiom> Set<T> getAxioms(AxiomType<T> axiomType, boolean includeImportsClosure) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public Set<OWLAxiom> getAxioms(boolean b) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public <T extends OWLAxiom> int getAxiomCount(AxiomType<T> axiomType, boolean includeImportsClosure) {
      // TODO Auto-generated method stub
      return 0;
    }

    @Override
    public int getAxiomCount(boolean includeImportsClosure) {
      // TODO Auto-generated method stub
      return 0;
    }

    @Override
    public boolean containsAxiomIgnoreAnnotations(OWLAxiom axiom, boolean includeImportsClosure) {
      // TODO Auto-generated method stub
      return false;
    }

    @Override
    public boolean containsAxiom(OWLAxiom axiom, boolean includeImportsClosure) {
      // TODO Auto-generated method stub
      return false;
    }

    @Override
    public boolean containsAxiom(OWLAxiom axiom) {
      // TODO Auto-generated method stub
      return false;
    }

    @Override
    public <T extends OWLAxiom> Set<T> getAxioms(AxiomType<T> axiomType) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public Set<OWLLogicalAxiom> getLogicalAxioms() {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public Set<OWLAxiom> getAxioms() {
      return Collections.emptySet();
    }

    @Override
    public Set<OWLAxiom> getReferencingAxioms(OWLPrimitive owlEntity, Imports includeImportsClosure) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public Set<OWLLogicalAxiom> getLogicalAxioms(Imports includeImportsClosure) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public int getLogicalAxiomCount(Imports includeImportsClosure) {
      // TODO Auto-generated method stub
      return 0;
    }

    @Override
    public Set<OWLAxiom> getAxiomsIgnoreAnnotations(OWLAxiom axiom, Imports includeImportsClosure) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public Set<OWLDatatypeDefinitionAxiom> getAxioms(OWLDatatype datatype, Imports includeImportsClosure) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public Set<OWLAnnotationAxiom> getAxioms(OWLAnnotationProperty property, Imports includeImportsClosure) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public Set<OWLIndividualAxiom> getAxioms(OWLIndividual individual, Imports includeImportsClosure) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public Set<OWLDataPropertyAxiom> getAxioms(OWLDataProperty property, Imports includeImportsClosure) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public Set<OWLObjectPropertyAxiom> getAxioms(OWLObjectPropertyExpression property, Imports includeImportsClosure) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public Set<OWLClassAxiom> getAxioms(OWLClass cls, Imports includeImportsClosure) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public <T extends OWLAxiom> Set<T> getAxioms(AxiomType<T> axiomType, Imports includeImportsClosure) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public Set<OWLAxiom> getAxioms(Imports includeImportsClosure) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public <T extends OWLAxiom> int getAxiomCount(AxiomType<T> axiomType, Imports includeImportsClosure) {
      // TODO Auto-generated method stub
      return 0;
    }

    @Override
    public int getAxiomCount(Imports includeImportsClosure) {
      // TODO Auto-generated method stub
      return 0;
    }

    @Override
    public boolean containsAxiom(OWLAxiom axiom, Imports includeImportsClosure, AxiomAnnotations ignoreAnnotations) {
      // TODO Auto-generated method stub
      return false;
    }

    @Override
    public Set<OWLAnnotationProperty> getAnnotationPropertiesInSignature() {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public Set<OWLDatatype> getDatatypesInSignature() {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public Set<OWLNamedIndividual> getIndividualsInSignature() {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public Set<OWLDataProperty> getDataPropertiesInSignature() {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public Set<OWLObjectProperty> getObjectPropertiesInSignature() {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public Set<OWLClass> getClassesInSignature() {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public Set<OWLAnonymousIndividual> getAnonymousIndividuals() {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public boolean containsEntityInSignature(OWLEntity owlEntity) {
      // TODO Auto-generated method stub
      return false;
    }

    @Override
    public int compareTo(OWLObject o) {
      // TODO Auto-generated method stub
      return 0;
    }

    @Override
    public boolean isTopEntity() {
      // TODO Auto-generated method stub
      return false;
    }

    @Override
    public boolean isBottomEntity() {
      // TODO Auto-generated method stub
      return false;
    }

    @Override
    public Set<OWLClassExpression> getNestedClassExpressions() {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public <O> O accept(OWLObjectVisitorEx<O> visitor) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public void accept(OWLObjectVisitor visitor) {
      // TODO Auto-generated method stub

    }

    @Override
    public void setOWLOntologyManager(OWLOntologyManager manager) {
      // TODO Auto-generated method stub

    }

    @Override
    public void saveOntology(OWLDocumentFormat ontologyFormat, OWLOntologyDocumentTarget documentTarget)
        throws OWLOntologyStorageException {
      // TODO Auto-generated method stub

    }

    @Override
    public void saveOntology(OWLDocumentFormat ontologyFormat, OutputStream outputStream)
        throws OWLOntologyStorageException {
      // TODO Auto-generated method stub

    }

    @Override
    public void saveOntology(OWLDocumentFormat ontologyFormat, IRI documentIRI) throws OWLOntologyStorageException {
      // TODO Auto-generated method stub

    }

    @Override
    public void saveOntology(OWLOntologyDocumentTarget documentTarget) throws OWLOntologyStorageException {
      // TODO Auto-generated method stub

    }

    @Override
    public void saveOntology(OWLDocumentFormat ontologyFormat) throws OWLOntologyStorageException {
      // TODO Auto-generated method stub

    }

    @Override
    public void saveOntology(OutputStream outputStream) throws OWLOntologyStorageException {
      // TODO Auto-generated method stub

    }

    @Override
    public void saveOntology(IRI documentIRI) throws OWLOntologyStorageException {
      // TODO Auto-generated method stub

    }

    @Override
    public void saveOntology() throws OWLOntologyStorageException {
      // TODO Auto-generated method stub

    }

    @Override
    public boolean isEmpty() {
      // TODO Auto-generated method stub
      return false;
    }

    @Override
    public boolean isDeclared(OWLEntity owlEntity, Imports includeImportsClosure) {
      // TODO Auto-generated method stub
      return false;
    }

    @Override
    public boolean isDeclared(OWLEntity owlEntity) {
      // TODO Auto-generated method stub
      return false;
    }

    @Override
    public boolean isAnonymous() {
      // TODO Auto-generated method stub
      return false;
    }

    @Override
    public Set<OWLAxiom> getTBoxAxioms(Imports includeImportsClosure) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public Set<OWLEntity> getSignature(Imports includeImportsClosure) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public Set<OWLEntity> getSignature() {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public Set<OWLAxiom> getRBoxAxioms(Imports includeImportsClosure) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public OWLOntologyID getOntologyID() {
      return new OWLOntologyID(IRI.create("urn:the-empty-ontology"));
    }

    @Override
    public OWLOntologyManager getOWLOntologyManager() {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public Set<OWLImportsDeclaration> getImportsDeclarations() {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public Set<OWLOntology> getImportsClosure() {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public Set<OWLOntology> getImports() {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public Set<OWLClassAxiom> getGeneralClassAxioms() {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public Set<IRI> getDirectImportsDocuments() {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public Set<OWLOntology> getDirectImports() {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public Set<OWLAnnotation> getAnnotations() {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public Set<OWLAxiom> getABoxAxioms(Imports includeImportsClosure) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public <O> O accept(OWLNamedObjectVisitorEx<O> visitor) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public void accept(OWLNamedObjectVisitor visitor) {
      // TODO Auto-generated method stub

    }
  };

  private Util() {}

  private static final Random random = new Random();

  public static final <E> Optional<E> getRandomElement(final Collection<E> c) {
    if (c.isEmpty())
      return Optional.empty();
    return c.stream().skip(random.nextInt(c.size())).findFirst();
  }

  public static final <E> Function<Set<E>, Future<E>> randomSelector() {
    return set -> new Future<E>() {

      private final E result = getRandomElement(set).orElse(null);

      @Override
      public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
      }

      @Override
      public boolean isCancelled() {
        return false;
      }

      @Override
      public boolean isDone() {
        return true;
      }

      @Override
      public E get() throws InterruptedException, ExecutionException {
        return result;
      }

      @Override
      public E get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return result;
      }
    };
  }

  public static final void runOnProtegeThread(final Runnable runnable, final boolean sync) {
    try {
      if (sync)
        SwingUtilities.invokeAndWait(runnable);
      else
        SwingUtilities.invokeLater(runnable);
//      System.out.println("Started on GUI thread.");
    } catch (Error e) {
      if (e.getMessage().equals("Cannot call invokeAndWait from the event dispatcher thread")) {
//        System.out.println("Already in GUI thread, running here.");
        runnable.run();
      } else
        throw new RuntimeException(e);
    } catch (InvocationTargetException | InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

}
