package org.semanticweb.owlapitools.profiles.test;

import static org.junit.Assert.assertEquals;
import static org.semanticweb.owlapi.apibinding.OWLFunctionalSyntaxFactory.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Ignore;
import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.SWRLAtom;
import org.semanticweb.owlapi.vocab.OWL2Datatype;
import org.semanticweb.owlapi.vocab.OWLFacet;
import org.semanticweb.owlapitools.profiles.OWL2DLProfile;
import org.semanticweb.owlapitools.profiles.OWL2ELProfile;
import org.semanticweb.owlapitools.profiles.OWL2Profile;
import org.semanticweb.owlapitools.profiles.OWL2QLProfile;
import org.semanticweb.owlapitools.profiles.OWL2RLProfile;
import org.semanticweb.owlapitools.profiles.OWLProfile;
import org.semanticweb.owlapitools.profiles.OWLProfileViolation;
import org.semanticweb.owlapitools.profiles.OWLProfileViolationVisitorAdapter;
import org.semanticweb.owlapitools.profiles.OWLProfileViolationVisitorExAdapter;
import org.semanticweb.owlapitools.profiles.violations.CycleInDatatypeDefinition;
import org.semanticweb.owlapitools.profiles.violations.DatatypeIRIAlsoUsedAsClassIRI;
import org.semanticweb.owlapitools.profiles.violations.EmptyOneOfAxiom;
import org.semanticweb.owlapitools.profiles.violations.IllegalPunning;
import org.semanticweb.owlapitools.profiles.violations.InsufficientIndividuals;
import org.semanticweb.owlapitools.profiles.violations.InsufficientOperands;
import org.semanticweb.owlapitools.profiles.violations.InsufficientPropertyExpressions;
import org.semanticweb.owlapitools.profiles.violations.LastPropertyInChainNotInImposedRange;
import org.semanticweb.owlapitools.profiles.violations.LexicalNotInLexicalSpace;
import org.semanticweb.owlapitools.profiles.violations.OntologyIRINotAbsolute;
import org.semanticweb.owlapitools.profiles.violations.OntologyVersionIRINotAbsolute;
import org.semanticweb.owlapitools.profiles.violations.UseOfAnonymousIndividual;
import org.semanticweb.owlapitools.profiles.violations.UseOfBuiltInDatatypeInDatatypeDefinition;
import org.semanticweb.owlapitools.profiles.violations.UseOfDataOneOfWithMultipleLiterals;
import org.semanticweb.owlapitools.profiles.violations.UseOfDefinedDatatypeInDatatypeRestriction;
import org.semanticweb.owlapitools.profiles.violations.UseOfIllegalAxiom;
import org.semanticweb.owlapitools.profiles.violations.UseOfIllegalClassExpression;
import org.semanticweb.owlapitools.profiles.violations.UseOfIllegalDataRange;
import org.semanticweb.owlapitools.profiles.violations.UseOfIllegalFacetRestriction;
import org.semanticweb.owlapitools.profiles.violations.UseOfNonAbsoluteIRI;
import org.semanticweb.owlapitools.profiles.violations.UseOfNonAtomicClassExpression;
import org.semanticweb.owlapitools.profiles.violations.UseOfNonEquivalentClassExpression;
import org.semanticweb.owlapitools.profiles.violations.UseOfNonSimplePropertyInAsymmetricObjectPropertyAxiom;
import org.semanticweb.owlapitools.profiles.violations.UseOfNonSimplePropertyInCardinalityRestriction;
import org.semanticweb.owlapitools.profiles.violations.UseOfNonSimplePropertyInDisjointPropertiesAxiom;
import org.semanticweb.owlapitools.profiles.violations.UseOfNonSimplePropertyInFunctionalPropertyAxiom;
import org.semanticweb.owlapitools.profiles.violations.UseOfNonSimplePropertyInInverseFunctionalObjectPropertyAxiom;
import org.semanticweb.owlapitools.profiles.violations.UseOfNonSimplePropertyInIrreflexivePropertyAxiom;
import org.semanticweb.owlapitools.profiles.violations.UseOfNonSimplePropertyInObjectHasSelf;
import org.semanticweb.owlapitools.profiles.violations.UseOfNonSubClassExpression;
import org.semanticweb.owlapitools.profiles.violations.UseOfNonSuperClassExpression;
import org.semanticweb.owlapitools.profiles.violations.UseOfObjectOneOfWithMultipleIndividuals;
import org.semanticweb.owlapitools.profiles.violations.UseOfObjectPropertyInverse;
import org.semanticweb.owlapitools.profiles.violations.UseOfPropertyInChainCausesCycle;
import org.semanticweb.owlapitools.profiles.violations.UseOfReservedVocabularyForAnnotationPropertyIRI;
import org.semanticweb.owlapitools.profiles.violations.UseOfReservedVocabularyForClassIRI;
import org.semanticweb.owlapitools.profiles.violations.UseOfReservedVocabularyForDataPropertyIRI;
import org.semanticweb.owlapitools.profiles.violations.UseOfReservedVocabularyForIndividualIRI;
import org.semanticweb.owlapitools.profiles.violations.UseOfReservedVocabularyForObjectPropertyIRI;
import org.semanticweb.owlapitools.profiles.violations.UseOfReservedVocabularyForOntologyIRI;
import org.semanticweb.owlapitools.profiles.violations.UseOfReservedVocabularyForVersionIRI;
import org.semanticweb.owlapitools.profiles.violations.UseOfTopDataPropertyAsSubPropertyInSubPropertyAxiom;
import org.semanticweb.owlapitools.profiles.violations.UseOfUndeclaredAnnotationProperty;
import org.semanticweb.owlapitools.profiles.violations.UseOfUndeclaredClass;
import org.semanticweb.owlapitools.profiles.violations.UseOfUndeclaredDataProperty;
import org.semanticweb.owlapitools.profiles.violations.UseOfUndeclaredDatatype;
import org.semanticweb.owlapitools.profiles.violations.UseOfUndeclaredObjectProperty;
import org.semanticweb.owlapitools.profiles.violations.UseOfUnknownDatatype;

@SuppressWarnings({ "javadoc", "rawtypes" })
public class OWLProfileJUnitTest {
    private static final String START = OWLThing().getIRI().getNamespace();
    private static final OWLClass cl = Class(IRI("urn:test#fakeclass"));
    private static final OWLDataProperty datap = DataProperty(IRI("urn:datatype#fakedatatypeproperty"));
    private static final OWLDataPropertyRangeAxiom DATA_PROPERTY_RANGE2 = DataPropertyRange(
            datap,
            DatatypeRestriction(Integer(),
                    FacetRestriction(OWLFacet.LANG_RANGE, Literal(1))));
    private static final OWLDataPropertyRangeAxiom DATA_PROPERTY_RANGE = DataPropertyRange(
            datap,
            DatatypeRestriction(Integer(),
                    FacetRestriction(OWLFacet.MAX_EXCLUSIVE, Literal(1))));
    private static final OWLObjectProperty op = ObjectProperty(IRI("urn:datatype#fakeobjectproperty"));
    private static final OWLDatatype unknownfakedatatype = Datatype(IRI(START
            + "unknownfakedatatype"));
    private static final OWLDatatype fakeundeclareddatatype = Datatype(IRI("urn:datatype#fakeundeclareddatatype"));
    private static final OWLDatatype fakedatatype = Datatype(IRI("urn:datatype#fakedatatype"));
    private static final IRI onto = IRI.create("urn:test#ontology");
    private static final OWLDataFactory df = OWLManager.getOWLDataFactory();
    private static final OWLObjectProperty p = ObjectProperty(IRI("urn:test#objectproperty"));

    public void declare(OWLOntology o, OWLEntity... entities) {
        OWLOntologyManager m = o.getOWLOntologyManager();
        for (OWLEntity e : entities) {
            m.addAxiom(o, Declaration(e));
        }
    }

    Comparator<Class> comp = new Comparator<Class>() {
        @Override
        public int compare(Class o1, Class o2) {
            return o1.getSimpleName().compareTo(o2.getSimpleName());
        }
    };

    public void checkInCollection(List<OWLProfileViolation<?>> violations, Class[] _list) {
        List<Class> list = new ArrayList<Class>(Arrays.asList(_list));
        List<Class> list1 = new ArrayList<Class>();
        for (OWLProfileViolation v : violations) {
            list1.add(v.getClass());
        }
        Collections.sort(list, comp);
        Collections.sort(list1, comp);
        assertEquals(list1.toString(), list, list1);
    }

    public void runAssert(OWLOntology o, OWLProfile profile, int expected,
            Class[] expectedViolations) {
        List<OWLProfileViolation<?>> violations = profile.checkOntology(o)
                .getViolations();
        assertEquals(expected, violations.size());
        checkInCollection(violations, expectedViolations);
        for (OWLProfileViolation<?> violation : violations) {
            o.getOWLOntologyManager().applyChanges(violation.repair());
            violation.accept(new OWLProfileViolationVisitorAdapter());
            violation.accept(new OWLProfileViolationVisitorExAdapter<String>() {
                @Override
                protected String doDefault(OWLProfileViolation<?> v) {
                    return v.toString();
                }
            });
        }
        violations = profile.checkOntology(o).getViolations();
        assertEquals(0, violations.size());
    }

    @Test
    @Tests(method = "public Object visit(OWLDatatype datatype)")
    public void shouldCreateViolationForOWLDatatypeInOWL2DLProfile() throws Exception {
        OWLOntology o = createOnto();
        OWLOntologyManager m = o.getOWLOntologyManager();
        declare(o, unknownfakedatatype, fakedatatype, Class(fakedatatype.getIRI()), datap);
        m.addAxiom(o, DataPropertyRange(datap, fakeundeclareddatatype));
        OWL2DLProfile profile = new OWL2DLProfile();
        int expected = 4;
        Class[] expectedViolations = new Class[] { UseOfUnknownDatatype.class,
                UseOfUndeclaredDatatype.class, DatatypeIRIAlsoUsedAsClassIRI.class,
                DatatypeIRIAlsoUsedAsClassIRI.class };
        runAssert(o, profile, expected, expectedViolations);
    }

    @Test
    @Tests(method = "public Object visit(OWLDatatypeDefinitionAxiom axiom)")
    public void shouldCreateViolationForOWLDatatypeDefinitionAxiomInOWL2DLProfile()
            throws Exception {
        OWLOntology o = createOnto();
        OWLOntologyManager m = o.getOWLOntologyManager();
        declare(o, Integer(), Boolean(), fakedatatype);
        m.addAxiom(o, DatatypeDefinition(Boolean(), Integer()));
        m.addAxiom(o, DatatypeDefinition(fakedatatype, Integer()));
        m.addAxiom(o, DatatypeDefinition(Integer(), fakedatatype));
        OWL2DLProfile profile = new OWL2DLProfile();
        int expected = 4;
        Class[] expectedViolations = new Class[] { CycleInDatatypeDefinition.class,
                CycleInDatatypeDefinition.class,
                UseOfBuiltInDatatypeInDatatypeDefinition.class,
                UseOfBuiltInDatatypeInDatatypeDefinition.class };
        runAssert(o, profile, expected, expectedViolations);
    }

    @Test
    @Tests(method = "public Object visit(OWLDatatypeDefinitionAxiom axiom)")
    public void
            shouldCreateViolationForOWLDatatypeDefinitionAxiomInOWL2DLProfile_cycles()
                    throws Exception {
        OWLOntology o = createOnto();
        OWLOntologyManager m = o.getOWLOntologyManager();
        OWL2DLProfile profile = new OWL2DLProfile();
        OWLDatatype d = Datatype(IRI(START + "test"));
        declare(o, d, Integer(), Boolean(), fakedatatype);
        m.addAxiom(o, DatatypeDefinition(d, Boolean()));
        m.addAxiom(o, DatatypeDefinition(Boolean(), d));
        m.addAxiom(o, DatatypeDefinition(fakedatatype, Integer()));
        m.addAxiom(o, DatatypeDefinition(Integer(), fakedatatype));
        int expected = 10;
        Class[] expectedViolations = new Class[] { CycleInDatatypeDefinition.class,
                CycleInDatatypeDefinition.class, CycleInDatatypeDefinition.class,
                CycleInDatatypeDefinition.class,
                UseOfBuiltInDatatypeInDatatypeDefinition.class,
                UseOfBuiltInDatatypeInDatatypeDefinition.class,
                UseOfBuiltInDatatypeInDatatypeDefinition.class,
                UseOfUnknownDatatype.class, UseOfUnknownDatatype.class,
                UseOfUnknownDatatype.class };
        runAssert(o, profile, expected, expectedViolations);
    }

    @Test
    @Tests(method = "public Object visit(OWLObjectProperty property)")
    public void shouldCreateViolationForOWLObjectPropertyInOWL2DLProfile()
            throws Exception {
        OWLOntology o = createOnto();
        OWLOntologyManager m = o.getOWLOntologyManager();
        IRI iri = IRI(START + "test");
        declare(o, ObjectProperty(iri), DataProperty(iri), AnnotationProperty(iri));
        m.addAxiom(o, SubObjectPropertyOf(op, ObjectProperty(iri)));
        OWL2DLProfile profile = new OWL2DLProfile();
        int expected = 13;
        Class[] expectedViolations = new Class[] {
                UseOfReservedVocabularyForObjectPropertyIRI.class,
                UseOfReservedVocabularyForAnnotationPropertyIRI.class,
                UseOfReservedVocabularyForDataPropertyIRI.class,
                UseOfReservedVocabularyForObjectPropertyIRI.class,
                UseOfUndeclaredObjectProperty.class, IllegalPunning.class,
                IllegalPunning.class, IllegalPunning.class, IllegalPunning.class,
                IllegalPunning.class, IllegalPunning.class, IllegalPunning.class,
                IllegalPunning.class };
        runAssert(o, profile, expected, expectedViolations);
    }

    @Test
    @Tests(method = "public Object visit(OWLDataProperty property)")
    public void shouldCreateViolationForOWLDataPropertyInOWL2DLProfile1()
            throws Exception {
        OWLOntology o = createOnto();
        declare(o, DataProperty(IRI(START + "fail")));
        OWL2DLProfile profile = new OWL2DLProfile();
        int expected = 1;
        Class[] expectedViolations = new Class[] { UseOfReservedVocabularyForDataPropertyIRI.class };
        runAssert(o, profile, expected, expectedViolations);
    }

    @Test
    @Tests(method = "public Object visit(OWLDataProperty property)")
    public void shouldCreateViolationForOWLDataPropertyInOWL2DLProfile2()
            throws Exception {
        OWLOntology o = createOnto();
        OWLOntologyManager m = o.getOWLOntologyManager();
        m.addAxiom(o, FunctionalDataProperty(datap));
        OWL2DLProfile profile = new OWL2DLProfile();
        int expected = 1;
        Class[] expectedViolations = new Class[] { UseOfUndeclaredDataProperty.class };
        runAssert(o, profile, expected, expectedViolations);
    }

    @Test
    @Tests(method = "public Object visit(OWLDataProperty property)")
    public void shouldCreateViolationForOWLDataPropertyInOWL2DLProfile3()
            throws Exception {
        OWLOntology o = createOnto();
        declare(o, datap, AnnotationProperty(datap.getIRI()));
        OWL2DLProfile profile = new OWL2DLProfile();
        int expected = 2;
        Class[] expectedViolations = new Class[] { IllegalPunning.class,
                IllegalPunning.class };
        runAssert(o, profile, expected, expectedViolations);
    }

    @Test
    @Tests(method = "public Object visit(OWLDataProperty property)")
    public void shouldCreateViolationForOWLDataPropertyInOWL2DLProfile4()
            throws Exception {
        OWLOntology o = createOnto();
        declare(o, datap, ObjectProperty(datap.getIRI()));
        OWL2DLProfile profile = new OWL2DLProfile();
        int expected = 2;
        Class[] expectedViolations = new Class[] { IllegalPunning.class,
                IllegalPunning.class };
        runAssert(o, profile, expected, expectedViolations);
    }

    @Test
    @Tests(method = "public Object visit(OWLAnnotationProperty property)")
    public void shouldCreateViolationForOWLAnnotationPropertyInOWL2DLProfile()
            throws Exception {
        OWLOntology o = createOnto();
        OWLOntologyManager m = o.getOWLOntologyManager();
        IRI iri = IRI(START + "test");
        declare(o, ObjectProperty(iri), DataProperty(iri), AnnotationProperty(iri));
        m.addAxiom(
                o,
                SubAnnotationPropertyOf(AnnotationProperty(IRI("urn:test#t")),
                        AnnotationProperty(iri)));
        OWL2DLProfile profile = new OWL2DLProfile();
        int expected = 13;
        Class[] expectedViolations = new Class[] {
                UseOfReservedVocabularyForAnnotationPropertyIRI.class,
                UseOfReservedVocabularyForAnnotationPropertyIRI.class,
                UseOfReservedVocabularyForObjectPropertyIRI.class,
                UseOfReservedVocabularyForDataPropertyIRI.class,
                UseOfUndeclaredAnnotationProperty.class, IllegalPunning.class,
                IllegalPunning.class, IllegalPunning.class, IllegalPunning.class,
                IllegalPunning.class, IllegalPunning.class, IllegalPunning.class,
                IllegalPunning.class };
        runAssert(o, profile, expected, expectedViolations);
    }

    @Test
    @Tests(method = "public Object visit(OWLOntology ontology)")
    public void shouldCreateViolationForOWLOntologyInOWL2DLProfile() throws Exception {
        OWLOntology o = OWLManager.createOWLOntologyManager().createOntology(
                new OWLOntologyID(IRI(START + "test"), IRI(START + "test1")));
        OWL2DLProfile profile = new OWL2DLProfile();
        int expected = 2;
        Class[] expectedViolations = new Class[] {
                UseOfReservedVocabularyForOntologyIRI.class,
                UseOfReservedVocabularyForVersionIRI.class };
        runAssert(o, profile, expected, expectedViolations);
    }

    @Test
    @Tests(method = "public Object visit(OWLClass desc)")
    public void shouldCreateViolationForOWLClassInOWL2DLProfile() throws Exception {
        OWLOntology o = createOnto();
        OWLOntologyManager m = o.getOWLOntologyManager();
        declare(o, Class(IRI(START + "test")), fakedatatype);
        m.addAxiom(o, ClassAssertion(Class(fakedatatype.getIRI()), AnonymousIndividual()));
        OWL2DLProfile profile = new OWL2DLProfile();
        int expected = 4;
        Class[] expectedViolations = new Class[] {
                UseOfReservedVocabularyForClassIRI.class, UseOfUndeclaredClass.class,
                DatatypeIRIAlsoUsedAsClassIRI.class, DatatypeIRIAlsoUsedAsClassIRI.class };
        runAssert(o, profile, expected, expectedViolations);
    }

    @Test
    @Tests(method = "public Object visit(OWLDataOneOf node)")
    public void shouldCreateViolationForOWLDataOneOfInOWL2DLProfile() throws Exception {
        OWLOntology o = createOnto();
        OWLOntologyManager m = o.getOWLOntologyManager();
        declare(o, datap);
        m.addAxiom(o, DataPropertyRange(datap, DataOneOf()));
        OWL2DLProfile profile = new OWL2DLProfile();
        int expected = 1;
        Class[] expectedViolations = new Class[] { EmptyOneOfAxiom.class };
        runAssert(o, profile, expected, expectedViolations);
    }

    @Test
    @Tests(method = "public Object visit(OWLDataUnionOf node)")
    public void shouldCreateViolationForOWLDataUnionOfInOWL2DLProfile() throws Exception {
        OWLOntology o = createOnto();
        OWLOntologyManager m = o.getOWLOntologyManager();
        declare(o, datap);
        m.addAxiom(o, DataPropertyRange(datap, DataUnionOf()));
        OWL2DLProfile profile = new OWL2DLProfile();
        int expected = 1;
        Class[] expectedViolations = new Class[] { InsufficientOperands.class };
        runAssert(o, profile, expected, expectedViolations);
    }

    @Test
    @Tests(method = "public Object visit(OWLDataIntersectionOf node)")
    public void shouldCreateViolationForOWLDataIntersectionOfInOWL2DLProfile()
            throws Exception {
        OWLOntology o = createOnto();
        OWLOntologyManager m = o.getOWLOntologyManager();
        declare(o, datap);
        m.addAxiom(o, DataPropertyRange(datap, DataIntersectionOf()));
        OWL2DLProfile profile = new OWL2DLProfile();
        int expected = 1;
        Class[] expectedViolations = new Class[] { InsufficientOperands.class };
        runAssert(o, profile, expected, expectedViolations);
    }

    @Test
    @Tests(method = "public Object visit(OWLObjectIntersectionOf node)")
    public void shouldCreateViolationForOWLObjectIntersectionOfInOWL2DLProfile()
            throws Exception {
        OWLOntology o = createOnto();
        OWLOntologyManager m = o.getOWLOntologyManager();
        declare(o, op);
        m.addAxiom(o, ObjectPropertyRange(op, ObjectIntersectionOf()));
        OWL2DLProfile profile = new OWL2DLProfile();
        int expected = 1;
        Class[] expectedViolations = new Class[] { InsufficientOperands.class };
        runAssert(o, profile, expected, expectedViolations);
    }

    @Test
    @Tests(method = "public Object visit(OWLObjectOneOf node)")
    public void shouldCreateViolationForOWLObjectOneOfInOWL2DLProfile() throws Exception {
        OWLOntology o = createOnto();
        OWLOntologyManager m = o.getOWLOntologyManager();
        declare(o, op);
        m.addAxiom(o, ObjectPropertyRange(op, ObjectOneOf()));
        OWL2DLProfile profile = new OWL2DLProfile();
        int expected = 1;
        Class[] expectedViolations = new Class[] { EmptyOneOfAxiom.class };
        runAssert(o, profile, expected, expectedViolations);
    }

    @Test
    @Tests(method = "public Object visit(OWLObjectUnionOf node)")
    public void shouldCreateViolationForOWLObjectUnionOfInOWL2DLProfile()
            throws Exception {
        OWLOntology o = createOnto();
        OWLOntologyManager m = o.getOWLOntologyManager();
        declare(o, op);
        m.addAxiom(o, ObjectPropertyRange(op, ObjectUnionOf()));
        OWL2DLProfile profile = new OWL2DLProfile();
        int expected = 1;
        Class[] expectedViolations = new Class[] { InsufficientOperands.class };
        runAssert(o, profile, expected, expectedViolations);
    }

    @Test
    @Tests(method = "public Object visit(OWLEquivalentClassesAxiom node)")
    public void shouldCreateViolationForOWLEquivalentClassesAxiomInOWL2DLProfile()
            throws Exception {
        OWLOntology o = createOnto();
        OWLOntologyManager m = o.getOWLOntologyManager();
        declare(o, op);
        m.addAxiom(o, EquivalentClasses());
        OWL2DLProfile profile = new OWL2DLProfile();
        int expected = 1;
        Class[] expectedViolations = new Class[] { InsufficientOperands.class };
        runAssert(o, profile, expected, expectedViolations);
    }

    @Test
    @Tests(method = "public Object visit(OWLDisjointClassesAxiom node)")
    public void shouldCreateViolationForOWLDisjointClassesAxiomInOWL2DLProfile()
            throws Exception {
        OWLOntology o = createOnto();
        OWLOntologyManager m = o.getOWLOntologyManager();
        declare(o, op);
        m.addAxiom(o, DisjointClasses());
        OWL2DLProfile profile = new OWL2DLProfile();
        int expected = 1;
        Class[] expectedViolations = new Class[] { InsufficientOperands.class };
        runAssert(o, profile, expected, expectedViolations);
    }

    @Test
    @Tests(method = "public Object visit(OWLDisjointUnionAxiom node)")
    public void shouldCreateViolationForOWLDisjointUnionAxiomInOWL2DLProfile()
            throws Exception {
        OWLOntology o = createOnto();
        OWLOntologyManager m = o.getOWLOntologyManager();
        declare(o, op);
        OWLClass otherfakeclass = Class(IRI("urn:test#otherfakeclass"));
        Set<OWLClassExpression> set = new HashSet<OWLClassExpression>();
        set.add(otherfakeclass);
        declare(o, cl);
        declare(o, otherfakeclass);
        m.addAxiom(o, DisjointUnion(cl, otherfakeclass));
        OWL2DLProfile profile = new OWL2DLProfile();
        int expected = 1;
        Class[] expectedViolations = new Class[] { InsufficientOperands.class };
        runAssert(o, profile, expected, expectedViolations);
    }

    @Test
    @Tests(method = "public Object visit(OWLEquivalentObjectPropertiesAxiom node)")
    public void
            shouldCreateViolationForOWLEquivalentObjectPropertiesAxiomInOWL2DLProfile()
                    throws Exception {
        OWLOntology o = createOnto();
        OWLOntologyManager m = o.getOWLOntologyManager();
        m.addAxiom(o, EquivalentObjectProperties());
        OWL2DLProfile profile = new OWL2DLProfile();
        int expected = 1;
        Class[] expectedViolations = new Class[] { InsufficientPropertyExpressions.class };
        runAssert(o, profile, expected, expectedViolations);
    }

    @Test
    @Tests(method = "public Object visit(OWLDisjointDataPropertiesAxiom node)")
    public void shouldCreateViolationForOWLDisjointDataPropertiesAxiomInOWL2DLProfile()
            throws Exception {
        OWLOntology o = createOnto();
        OWLOntologyManager m = o.getOWLOntologyManager();
        m.addAxiom(o, DisjointDataProperties());
        OWL2DLProfile profile = new OWL2DLProfile();
        int expected = 1;
        Class[] expectedViolations = new Class[] { InsufficientPropertyExpressions.class };
        runAssert(o, profile, expected, expectedViolations);
    }

    @Test
    @Tests(method = "public Object visit(OWLEquivalentDataPropertiesAxiom node)")
    public void shouldCreateViolationForOWLEquivalentDataPropertiesAxiomInOWL2DLProfile()
            throws Exception {
        OWLOntology o = createOnto();
        OWLOntologyManager m = o.getOWLOntologyManager();
        m.addAxiom(o, EquivalentDataProperties());
        OWL2DLProfile profile = new OWL2DLProfile();
        int expected = 1;
        Class[] expectedViolations = new Class[] { InsufficientPropertyExpressions.class };
        runAssert(o, profile, expected, expectedViolations);
    }

    @Test
    @Tests(method = "public Object visit(OWLHasKeyAxiom node)")
    public void shouldCreateViolationForOWLHasKeyAxiomInOWL2DLProfile() throws Exception {
        OWLOntology o = createOnto();
        OWLOntologyManager m = o.getOWLOntologyManager();
        declare(o, cl);
        m.addAxiom(o, HasKey(cl));
        OWL2DLProfile profile = new OWL2DLProfile();
        int expected = 1;
        Class[] expectedViolations = new Class[] { InsufficientPropertyExpressions.class };
        runAssert(o, profile, expected, expectedViolations);
    }

    @Test
    @Tests(method = "public Object visit(OWLSameIndividualAxiom node)")
    public void shouldCreateViolationForOWLSameIndividualAxiomInOWL2DLProfile()
            throws Exception {
        OWLOntology o = createOnto();
        OWLOntologyManager m = o.getOWLOntologyManager();
        m.addAxiom(o, SameIndividual());
        OWL2DLProfile profile = new OWL2DLProfile();
        int expected = 1;
        Class[] expectedViolations = new Class[] { InsufficientIndividuals.class };
        runAssert(o, profile, expected, expectedViolations);
    }

    @Test
    @Tests(method = "public Object visit(OWLDifferentIndividualsAxiom node)")
    public void shouldCreateViolationForOWLDifferentIndividualsAxiomInOWL2DLProfile()
            throws Exception {
        OWLOntology o = createOnto();
        OWLOntologyManager m = o.getOWLOntologyManager();
        m.addAxiom(o, DifferentIndividuals());
        OWL2DLProfile profile = new OWL2DLProfile();
        int expected = 1;
        Class[] expectedViolations = new Class[] { InsufficientIndividuals.class };
        runAssert(o, profile, expected, expectedViolations);
    }

    @Test
    @Tests(method = "public Object visit(OWLNamedIndividual individual)")
    public void shouldCreateViolationForOWLNamedIndividualInOWL2DLProfile()
            throws Exception {
        OWLOntology o = createOnto();
        OWLOntologyManager m = o.getOWLOntologyManager();
        m.addAxiom(o, ClassAssertion(OWLThing(), NamedIndividual(IRI(START + "i"))));
        OWL2DLProfile profile = new OWL2DLProfile();
        int expected = 1;
        Class[] expectedViolations = new Class[] { UseOfReservedVocabularyForIndividualIRI.class };
        runAssert(o, profile, expected, expectedViolations);
    }

    @Test
    @Tests(method = "public Object visit(OWLSubDataPropertyOfAxiom axiom)")
    public void shouldCreateViolationForOWLSubDataPropertyOfAxiomInOWL2DLProfile()
            throws Exception {
        OWLOntology o = createOnto();
        OWLOntologyManager m = o.getOWLOntologyManager();
        m.addAxiom(o,
                SubDataPropertyOf(df.getOWLTopDataProperty(), df.getOWLTopDataProperty()));
        OWL2DLProfile profile = new OWL2DLProfile();
        int expected = 1;
        Class[] expectedViolations = new Class[] { UseOfTopDataPropertyAsSubPropertyInSubPropertyAxiom.class };
        runAssert(o, profile, expected, expectedViolations);
    }

    @Test
    @Tests(method = "public Object visit(OWLObjectMinCardinality desc)")
    public void shouldCreateViolationForOWLObjectMinCardinalityInOWL2DLProfile()
            throws Exception {
        OWLOntology o = createOnto();
        OWLOntologyManager m = o.getOWLOntologyManager();
        declare(o, op, cl);
        m.addAxiom(o, TransitiveObjectProperty(op));
        m.addAxiom(o, SubClassOf(cl, ObjectMinCardinality(1, op, OWLThing())));
        OWL2DLProfile profile = new OWL2DLProfile();
        int expected = 1;
        Class[] expectedViolations = new Class[] { UseOfNonSimplePropertyInCardinalityRestriction.class };
        runAssert(o, profile, expected, expectedViolations);
    }

    @Test
    @Tests(method = "public Object visit(OWLObjectMaxCardinality desc)")
    public void shouldCreateViolationForOWLObjectMaxCardinalityInOWL2DLProfile()
            throws Exception {
        OWLOntology o = createOnto();
        OWLOntologyManager m = o.getOWLOntologyManager();
        declare(o, op, cl);
        m.addAxiom(o, TransitiveObjectProperty(op));
        m.addAxiom(o, SubClassOf(cl, ObjectMaxCardinality(1, op, OWLThing())));
        OWL2DLProfile profile = new OWL2DLProfile();
        int expected = 1;
        Class[] expectedViolations = new Class[] { UseOfNonSimplePropertyInCardinalityRestriction.class };
        runAssert(o, profile, expected, expectedViolations);
    }

    @Test
    @Tests(method = "public Object visit(OWLObjectExactCardinality desc)")
    public void shouldCreateViolationForOWLObjectExactCardinalityInOWL2DLProfile()
            throws Exception {
        OWLOntology o = createOnto();
        OWLOntologyManager m = o.getOWLOntologyManager();
        declare(o, op, cl);
        m.addAxiom(o, TransitiveObjectProperty(op));
        m.addAxiom(o, SubClassOf(cl, ObjectExactCardinality(1, op, OWLThing())));
        OWL2DLProfile profile = new OWL2DLProfile();
        int expected = 1;
        Class[] expectedViolations = new Class[] { UseOfNonSimplePropertyInCardinalityRestriction.class };
        runAssert(o, profile, expected, expectedViolations);
    }

    @Test
    @Tests(method = "public Object visit(OWLObjectHasSelf desc)")
    public void shouldCreateViolationForOWLObjectHasSelfInOWL2DLProfile()
            throws Exception {
        OWLOntology o = createOnto();
        OWLOntologyManager m = o.getOWLOntologyManager();
        declare(o, op);
        m.addAxiom(o, TransitiveObjectProperty(op));
        m.addAxiom(o, ObjectPropertyRange(op, ObjectHasSelf(op)));
        OWL2DLProfile profile = new OWL2DLProfile();
        int expected = 1;
        Class[] expectedViolations = new Class[] { UseOfNonSimplePropertyInObjectHasSelf.class };
        runAssert(o, profile, expected, expectedViolations);
    }

    @Test
    @Tests(method = "public Object visit(OWLFunctionalObjectPropertyAxiom axiom)")
    public void shouldCreateViolationForOWLFunctionalObjectPropertyAxiomInOWL2DLProfile()
            throws Exception {
        OWLOntology o = createOnto();
        OWLOntologyManager m = o.getOWLOntologyManager();
        declare(o, op);
        m.addAxiom(o, TransitiveObjectProperty(op));
        m.addAxiom(o, FunctionalObjectProperty(op));
        OWL2DLProfile profile = new OWL2DLProfile();
        int expected = 1;
        Class[] expectedViolations = new Class[] { UseOfNonSimplePropertyInFunctionalPropertyAxiom.class };
        runAssert(o, profile, expected, expectedViolations);
    }

    @Test
    @Tests(method = "public Object visit(OWLInverseFunctionalObjectPropertyAxiom axiom)")
    public
            void
            shouldCreateViolationForOWLInverseFunctionalObjectPropertyAxiomInOWL2DLProfile()
                    throws Exception {
        OWLOntology o = createOnto();
        OWLOntologyManager m = o.getOWLOntologyManager();
        declare(o, op);
        m.addAxiom(o, TransitiveObjectProperty(op));
        m.addAxiom(o, InverseFunctionalObjectProperty(op));
        OWL2DLProfile profile = new OWL2DLProfile();
        int expected = 1;
        Class[] expectedViolations = new Class[] { UseOfNonSimplePropertyInInverseFunctionalObjectPropertyAxiom.class };
        runAssert(o, profile, expected, expectedViolations);
    }

    @Test
    @Tests(method = "public Object visit(OWLIrreflexiveObjectPropertyAxiom axiom)")
    public void
            shouldCreateViolationForOWLIrreflexiveObjectPropertyAxiomInOWL2DLProfile()
                    throws Exception {
        OWLOntology o = createOnto();
        OWLOntologyManager m = o.getOWLOntologyManager();
        declare(o, op);
        m.addAxiom(o, TransitiveObjectProperty(op));
        m.addAxiom(o, IrreflexiveObjectProperty(op));
        OWL2DLProfile profile = new OWL2DLProfile();
        int expected = 1;
        Class[] expectedViolations = new Class[] { UseOfNonSimplePropertyInIrreflexivePropertyAxiom.class };
        runAssert(o, profile, expected, expectedViolations);
    }

    @Test
    @Tests(method = "public Object visit(OWLAsymmetricObjectPropertyAxiom axiom)")
    public void shouldCreateViolationForOWLAsymmetricObjectPropertyAxiomInOWL2DLProfile()
            throws Exception {
        OWLOntology o = createOnto();
        OWLOntologyManager m = o.getOWLOntologyManager();
        declare(o, op);
        m.addAxiom(o, TransitiveObjectProperty(op));
        m.addAxiom(o, AsymmetricObjectProperty(op));
        OWL2DLProfile profile = new OWL2DLProfile();
        int expected = 1;
        Class[] expectedViolations = new Class[] { UseOfNonSimplePropertyInAsymmetricObjectPropertyAxiom.class };
        runAssert(o, profile, expected, expectedViolations);
    }

    @Test
    @Tests(method = "public Object visit(OWLDisjointObjectPropertiesAxiom axiom)")
    public void shouldCreateViolationForOWLDisjointObjectPropertiesAxiomInOWL2DLProfile()
            throws Exception {
        OWLOntology o = createOnto();
        OWLOntologyManager m = o.getOWLOntologyManager();
        declare(o, op);
        m.addAxiom(o, TransitiveObjectProperty(op));
        m.addAxiom(o, DisjointObjectProperties(op));
        OWL2DLProfile profile = new OWL2DLProfile();
        int expected = 2;
        Class[] expectedViolations = new Class[] { InsufficientPropertyExpressions.class,
                UseOfNonSimplePropertyInDisjointPropertiesAxiom.class };
        runAssert(o, profile, expected, expectedViolations);
    }

    @Test
    @Tests(method = "public Object visit(OWLSubPropertyChainOfAxiom axiom)")
    public void shouldCreateViolationForOWLSubPropertyChainOfAxiomInOWL2DLProfile()
            throws Exception {
        OWLOntology o = createOnto();
        OWLOntologyManager m = o.getOWLOntologyManager();
        OWLObjectProperty op1 = ObjectProperty(IRI("urn:test#op"));
        declare(o, op, op1);
        m.addAxiom(o, SubPropertyChainOf(Arrays.asList(op1), op));
        m.addAxiom(o, SubPropertyChainOf(Arrays.asList(op, op1, op), op));
        m.addAxiom(o, SubPropertyChainOf(Arrays.asList(op, op1), op));
        m.addAxiom(o, SubPropertyChainOf(Arrays.asList(op1, op, op1, op), op));
        OWL2DLProfile profile = new OWL2DLProfile();
        int expected = 4;
        Class[] expectedViolations = new Class[] { InsufficientPropertyExpressions.class,
                UseOfPropertyInChainCausesCycle.class,
                UseOfPropertyInChainCausesCycle.class,
                UseOfPropertyInChainCausesCycle.class };
        runAssert(o, profile, expected, expectedViolations);
    }

    @Test
    @Tests(method = "public Object visit(OWLOntology ont)")
    public void shouldCreateViolationForOWLOntologyInOWL2Profile() throws Exception {
        OWLOntology o = OWLManager.createOWLOntologyManager().createOntology(
                new OWLOntologyID(IRI("test"), IRI("test1")));
        OWL2Profile profile = new OWL2Profile();
        int expected = 2;
        Class[] expectedViolations = new Class[] { OntologyIRINotAbsolute.class,
                OntologyVersionIRINotAbsolute.class };
        runAssert(o, profile, expected, expectedViolations);
    }

    @Test
    @Tests(method = "public Object visit(IRI iri)")
    public void shouldCreateViolationForIRIInOWL2Profile() throws Exception {
        OWLOntology o = createOnto();
        declare(o, Class(IRI("test")));
        OWL2Profile profile = new OWL2Profile();
        int expected = 1;
        Class[] expectedViolations = new Class[] { UseOfNonAbsoluteIRI.class };
        runAssert(o, profile, expected, expectedViolations);
    }

    @Test
    @Tests(method = "public Object visit(OWLLiteral node)")
    public void shouldCreateViolationForOWLLiteralInOWL2Profile() throws Exception {
        OWLOntology o = createOnto();
        OWLOntologyManager m = o.getOWLOntologyManager();
        declare(o, datap);
        m.addAxiom(
                o,
                DataPropertyAssertion(datap, AnonymousIndividual(),
                        Literal("wrong", OWL2Datatype.XSD_INTEGER)));
        OWL2Profile profile = new OWL2Profile();
        int expected = 1;
        Class[] expectedViolations = new Class[] { LexicalNotInLexicalSpace.class };
        runAssert(o, profile, expected, expectedViolations);
    }

    @Test
    @Tests(method = "public Object visit(OWLDatatypeRestriction node)")
    public void shouldCreateViolationForOWLDatatypeRestrictionInOWL2Profile()
            throws Exception {
        OWLOntology o = createOnto();
        OWLOntologyManager m = o.getOWLOntologyManager();
        declare(o, datap);
        m.addAxiom(o, DatatypeDefinition(Integer(), Boolean()));
        m.addAxiom(o, DATA_PROPERTY_RANGE2);
        OWL2Profile profile = new OWL2Profile();
        int expected = 3;
        Class[] expectedViolations = new Class[] {
                UseOfDefinedDatatypeInDatatypeRestriction.class,
                UseOfIllegalFacetRestriction.class, UseOfUndeclaredDatatype.class };
        runAssert(o, profile, expected, expectedViolations);
    }

    @Test
    @Tests(method = "public Object visit(OWLDatatypeDefinitionAxiom axiom)")
    public void shouldCreateViolationForOWLDatatypeDefinitionAxiomInOWL2Profile()
            throws Exception {
        OWLOntology o = createOnto();
        OWLOntologyManager m = o.getOWLOntologyManager();
        m.addAxiom(o, DatatypeDefinition(fakedatatype, Boolean()));
        OWL2Profile profile = new OWL2Profile();
        int expected = 1;
        Class[] expectedViolations = new Class[] { UseOfUndeclaredDatatype.class };
        runAssert(o, profile, expected, expectedViolations);
    }

    @Test
    @Tests(method = "public Object visit(OWLDatatype node)")
    public void shouldCreateViolationForOWLDatatypeInOWL2ELProfile() throws Exception {
        OWLOntology o = createOnto();
        declare(o, Boolean());
        OWL2ELProfile profile = new OWL2ELProfile();
        int expected = 1;
        Class[] expectedViolations = new Class[] { UseOfIllegalDataRange.class };
        runAssert(o, profile, expected, expectedViolations);
    }

    @Test
    @Tests(method = "public Object visit(OWLAnonymousIndividual individual)")
    public void shouldCreateViolationForOWLAnonymousIndividualInOWL2ELProfile()
            throws Exception {
        OWLOntology o = createOnto();
        OWLOntologyManager m = o.getOWLOntologyManager();
        m.addAxiom(o, ClassAssertion(OWLThing(), df.getOWLAnonymousIndividual()));
        OWL2ELProfile profile = new OWL2ELProfile();
        int expected = 1;
        Class[] expectedViolations = new Class[] { UseOfAnonymousIndividual.class };
        runAssert(o, profile, expected, expectedViolations);
    }

    @Test
    @Tests(method = "public Object visit(OWLObjectInverseOf property)")
    public void shouldCreateViolationForOWLObjectInverseOfInOWL2ELProfile()
            throws Exception {
        OWLOntology o = createOnto();
        OWLOntologyManager m = o.getOWLOntologyManager();
        declare(o, op);
        m.addAxiom(o, SubObjectPropertyOf(op, ObjectInverseOf(op)));
        OWL2ELProfile profile = new OWL2ELProfile();
        int expected = 1;
        Class[] expectedViolations = new Class[] { UseOfObjectPropertyInverse.class };
        runAssert(o, profile, expected, expectedViolations);
    }

    @Test
    @Tests(method = "public Object visit(OWLDataAllValuesFrom desc)")
    public void shouldCreateViolationForOWLDataAllValuesFromInOWL2ELProfile()
            throws Exception {
        OWLOntology o = createOnto();
        OWLOntologyManager m = o.getOWLOntologyManager();
        declare(o, datap, cl);
        m.addAxiom(o, SubClassOf(cl, DataAllValuesFrom(datap, Integer())));
        OWL2ELProfile profile = new OWL2ELProfile();
        int expected = 1;
        Class[] expectedViolations = new Class[] { UseOfIllegalClassExpression.class };
        runAssert(o, profile, expected, expectedViolations);
    }

    @Test
    @Tests(method = "public Object visit(OWLDataExactCardinality desc)")
    public void shouldCreateViolationForOWLDataExactCardinalityInOWL2ELProfile()
            throws Exception {
        OWLOntology o = createOnto();
        OWLOntologyManager m = o.getOWLOntologyManager();
        declare(o, datap, cl, Integer());
        m.addAxiom(o, SubClassOf(cl, DataExactCardinality(1, datap, Integer())));
        OWL2ELProfile profile = new OWL2ELProfile();
        int expected = 1;
        Class[] expectedViolations = new Class[] { UseOfIllegalClassExpression.class };
        runAssert(o, profile, expected, expectedViolations);
    }

    @Test
    @Tests(method = "public Object visit(OWLDataMaxCardinality desc)")
    public void shouldCreateViolationForOWLDataMaxCardinalityInOWL2ELProfile()
            throws Exception {
        OWLOntology o = createOnto();
        OWLOntologyManager m = o.getOWLOntologyManager();
        declare(o, datap, cl, Integer());
        m.addAxiom(o, SubClassOf(cl, DataMaxCardinality(1, datap, Integer())));
        OWL2ELProfile profile = new OWL2ELProfile();
        int expected = 1;
        Class[] expectedViolations = new Class[] { UseOfIllegalClassExpression.class };
        runAssert(o, profile, expected, expectedViolations);
    }

    @Test
    @Tests(method = "public Object visit(OWLDataMinCardinality desc)")
    public void shouldCreateViolationForOWLDataMinCardinalityInOWL2ELProfile()
            throws Exception {
        OWLOntology o = createOnto();
        OWLOntologyManager m = o.getOWLOntologyManager();
        declare(o, datap, cl, Integer());
        m.addAxiom(o, SubClassOf(cl, DataMinCardinality(1, datap, Integer())));
        OWL2ELProfile profile = new OWL2ELProfile();
        int expected = 1;
        Class[] expectedViolations = new Class[] { UseOfIllegalClassExpression.class };
        runAssert(o, profile, expected, expectedViolations);
    }

    @Test
    @Tests(method = "public Object visit(OWLObjectAllValuesFrom desc)")
    public void shouldCreateViolationForOWLObjectAllValuesFromInOWL2ELProfile()
            throws Exception {
        OWLOntology o = createOnto();
        OWLOntologyManager m = o.getOWLOntologyManager();
        declare(o, op, cl);
        m.addAxiom(o, SubClassOf(cl, ObjectAllValuesFrom(op, OWLThing())));
        OWL2ELProfile profile = new OWL2ELProfile();
        int expected = 1;
        Class[] expectedViolations = new Class[] { UseOfIllegalClassExpression.class };
        runAssert(o, profile, expected, expectedViolations);
    }

    @Test
    @Tests(method = "public Object visit(OWLObjectComplementOf desc)")
    public void shouldCreateViolationForOWLObjectComplementOfInOWL2ELProfile()
            throws Exception {
        OWLOntology o = createOnto();
        OWLOntologyManager m = o.getOWLOntologyManager();
        declare(o, op);
        m.addAxiom(o, ObjectPropertyRange(op, ObjectComplementOf(OWLNothing())));
        OWL2ELProfile profile = new OWL2ELProfile();
        int expected = 1;
        Class[] expectedViolations = new Class[] { UseOfIllegalClassExpression.class };
        runAssert(o, profile, expected, expectedViolations);
    }

    @Test
    @Tests(method = "public Object visit(OWLObjectExactCardinality desc)")
    public void shouldCreateViolationForOWLObjectExactCardinalityInOWL2ELProfile()
            throws Exception {
        OWLOntology o = createOnto();
        OWLOntologyManager m = o.getOWLOntologyManager();
        declare(o, op, cl);
        m.addAxiom(o, SubClassOf(cl, ObjectExactCardinality(1, op, OWLThing())));
        OWL2ELProfile profile = new OWL2ELProfile();
        int expected = 1;
        Class[] expectedViolations = new Class[] { UseOfIllegalClassExpression.class };
        runAssert(o, profile, expected, expectedViolations);
    }

    @Test
    @Tests(method = "public Object visit(OWLObjectMaxCardinality desc)")
    public void shouldCreateViolationForOWLObjectMaxCardinalityInOWL2ELProfile()
            throws Exception {
        OWLOntology o = createOnto();
        OWLOntologyManager m = o.getOWLOntologyManager();
        declare(o, op, cl);
        m.addAxiom(o, SubClassOf(cl, ObjectMaxCardinality(1, op, OWLThing())));
        OWL2ELProfile profile = new OWL2ELProfile();
        int expected = 1;
        Class[] expectedViolations = new Class[] { UseOfIllegalClassExpression.class };
        runAssert(o, profile, expected, expectedViolations);
    }

    @Test
    @Tests(method = "public Object visit(OWLObjectMinCardinality desc)")
    public void shouldCreateViolationForOWLObjectMinCardinalityInOWL2ELProfile()
            throws Exception {
        OWLOntology o = createOnto();
        OWLOntologyManager m = o.getOWLOntologyManager();
        declare(o, op, cl);
        m.addAxiom(o, SubClassOf(cl, ObjectMinCardinality(1, op, OWLThing())));
        OWL2ELProfile profile = new OWL2ELProfile();
        int expected = 1;
        Class[] expectedViolations = new Class[] { UseOfIllegalClassExpression.class };
        runAssert(o, profile, expected, expectedViolations);
    }

    @Test
    @Tests(method = "public Object visit(OWLObjectOneOf desc)")
    public void shouldCreateViolationForOWLObjectOneOfInOWL2ELProfile() throws Exception {
        OWLOntology o = createOnto();
        OWLOntologyManager m = o.getOWLOntologyManager();
        declare(o, op);
        m.addAxiom(
                o,
                ObjectPropertyRange(
                        op,
                        ObjectOneOf(NamedIndividual(IRI("urn:test#i1")),
                                NamedIndividual(IRI("urn:test#i2")))));
        OWL2ELProfile profile = new OWL2ELProfile();
        int expected = 1;
        Class[] expectedViolations = new Class[] { UseOfObjectOneOfWithMultipleIndividuals.class };
        runAssert(o, profile, expected, expectedViolations);
    }

    @Test
    @Tests(method = "public Object visit(OWLObjectUnionOf desc)")
    public void shouldCreateViolationForOWLObjectUnionOfInOWL2ELProfile()
            throws Exception {
        OWLOntology o = createOnto();
        OWLOntologyManager m = o.getOWLOntologyManager();
        declare(o, op);
        m.addAxiom(o, ObjectPropertyRange(op, ObjectUnionOf(OWLThing(), OWLNothing())));
        OWL2ELProfile profile = new OWL2ELProfile();
        int expected = 1;
        Class[] expectedViolations = new Class[] { UseOfIllegalClassExpression.class };
        runAssert(o, profile, expected, expectedViolations);
    }

    @Test
    @Tests(method = "public Object visit(OWLDataComplementOf node)")
    public void shouldCreateViolationForOWLDataComplementOfInOWL2ELProfile()
            throws Exception {
        OWLOntology o = createOnto();
        OWLOntologyManager m = o.getOWLOntologyManager();
        declare(o, datap);
        m.addAxiom(o, DataPropertyRange(datap, DataComplementOf(Double())));
        OWL2ELProfile profile = new OWL2ELProfile();
        int expected = 2;
        Class[] expectedViolations = new Class[] { UseOfIllegalDataRange.class,
                UseOfIllegalDataRange.class };
        runAssert(o, profile, expected, expectedViolations);
    }

    @Test
    @Tests(method = "public Object visit(OWLDataOneOf node)")
    public void shouldCreateViolationForOWLDataOneOfInOWL2ELProfile() throws Exception {
        OWLOntology o = createOnto();
        OWLOntologyManager m = o.getOWLOntologyManager();
        declare(o, datap);
        m.addAxiom(o, DataPropertyRange(datap, DataOneOf(Literal(1), Literal(2))));
        OWL2ELProfile profile = new OWL2ELProfile();
        int expected = 1;
        Class[] expectedViolations = new Class[] { UseOfDataOneOfWithMultipleLiterals.class };
        runAssert(o, profile, expected, expectedViolations);
    }

    @Test
    @Tests(method = "public Object visit(OWLDatatypeRestriction node)")
    public void shouldCreateViolationForOWLDatatypeRestrictionInOWL2ELProfile()
            throws Exception {
        OWLOntology o = createOnto();
        OWLOntologyManager m = o.getOWLOntologyManager();
        declare(o, datap);
        m.addAxiom(o, DATA_PROPERTY_RANGE);
        OWL2ELProfile profile = new OWL2ELProfile();
        int expected = 1;
        Class[] expectedViolations = new Class[] { UseOfIllegalDataRange.class };
        runAssert(o, profile, expected, expectedViolations);
    }

    @Test
    @Tests(method = "public Object visit(OWLDataUnionOf node)")
    public void shouldCreateViolationForOWLDataUnionOfInOWL2ELProfile() throws Exception {
        OWLOntology o = createOnto();
        OWLOntologyManager m = o.getOWLOntologyManager();
        declare(o, datap);
        m.addAxiom(o, DataPropertyRange(datap, DataUnionOf(Double(), Integer())));
        OWL2ELProfile profile = new OWL2ELProfile();
        int expected = 2;
        Class[] expectedViolations = new Class[] { UseOfIllegalDataRange.class,
                UseOfIllegalDataRange.class };
        runAssert(o, profile, expected, expectedViolations);
    }

    @Test
    @Tests(method = "public Object visit(OWLAsymmetricObjectPropertyAxiom axiom)")
    public void shouldCreateViolationForOWLAsymmetricObjectPropertyAxiomInOWL2ELProfile()
            throws Exception {
        OWLOntology o = createOnto();
        OWLOntologyManager m = o.getOWLOntologyManager();
        declare(o, op);
        m.addAxiom(o, AsymmetricObjectProperty(op));
        OWL2ELProfile profile = new OWL2ELProfile();
        int expected = 1;
        Class[] expectedViolations = new Class[] { UseOfIllegalAxiom.class };
        runAssert(o, profile, expected, expectedViolations);
    }

    @Test
    @Tests(method = "public Object visit(OWLDisjointDataPropertiesAxiom axiom)")
    public void shouldCreateViolationForOWLDisjointDataPropertiesAxiomInOWL2ELProfile()
            throws Exception {
        OWLOntology o = createOnto();
        OWLOntologyManager m = o.getOWLOntologyManager();
        OWLDataProperty dp = DataProperty(IRI("urn:test#other"));
        declare(o, datap, dp);
        m.addAxiom(o, DisjointDataProperties(datap, dp));
        OWL2ELProfile profile = new OWL2ELProfile();
        int expected = 1;
        Class[] expectedViolations = new Class[] { UseOfIllegalAxiom.class };
        runAssert(o, profile, expected, expectedViolations);
    }

    @Test
    @Tests(method = "public Object visit(OWLDisjointObjectPropertiesAxiom axiom)")
    public void shouldCreateViolationForOWLDisjointObjectPropertiesAxiomInOWL2ELProfile()
            throws Exception {
        OWLOntology o = createOnto();
        OWLOntologyManager m = o.getOWLOntologyManager();
        OWLObjectProperty op1 = ObjectProperty(IRI("urn:test#test"));
        declare(o, op, op1);
        m.addAxiom(o, DisjointObjectProperties(op1, op));
        OWL2ELProfile profile = new OWL2ELProfile();
        int expected = 1;
        Class[] expectedViolations = new Class[] { UseOfIllegalAxiom.class };
        runAssert(o, profile, expected, expectedViolations);
    }

    @Test
    @Tests(method = "public Object visit(OWLDisjointUnionAxiom axiom)")
    public void shouldCreateViolationForOWLDisjointUnionAxiomInOWL2ELProfile()
            throws Exception {
        OWLOntology o = createOnto();
        OWLOntologyManager m = o.getOWLOntologyManager();
        declare(o, cl);
        m.addAxiom(o, DisjointUnion(cl, OWLThing(), OWLNothing()));
        OWL2ELProfile profile = new OWL2ELProfile();
        int expected = 1;
        Class[] expectedViolations = new Class[] { UseOfIllegalAxiom.class };
        runAssert(o, profile, expected, expectedViolations);
    }

    @Test
    @Tests(method = "public Object visit(OWLFunctionalObjectPropertyAxiom axiom)")
    public void shouldCreateViolationForOWLFunctionalObjectPropertyAxiomInOWL2ELProfile()
            throws Exception {
        OWLOntology o = createOnto();
        OWLOntologyManager m = o.getOWLOntologyManager();
        declare(o, op);
        m.addAxiom(o, FunctionalObjectProperty(op));
        OWL2ELProfile profile = new OWL2ELProfile();
        int expected = 1;
        Class[] expectedViolations = new Class[] { UseOfIllegalAxiom.class };
        runAssert(o, profile, expected, expectedViolations);
    }

    @Test
    @Tests(method = "public Object visit(OWLHasKeyAxiom axiom)")
    public void shouldCreateViolationForOWLHasKeyAxiomInOWL2ELProfile() throws Exception {
        OWLOntology o = createOnto();
        OWLOntologyManager m = o.getOWLOntologyManager();
        declare(o, cl, op);
        m.addAxiom(o, HasKey(cl, op));
        OWL2ELProfile profile = new OWL2ELProfile();
        int expected = 1;
        Class[] expectedViolations = new Class[] { UseOfIllegalAxiom.class };
        runAssert(o, profile, expected, expectedViolations);
    }

    @Test
    @Tests(method = "public Object visit(OWLInverseFunctionalObjectPropertyAxiom axiom)")
    public
            void
            shouldCreateViolationForOWLInverseFunctionalObjectPropertyAxiomInOWL2ELProfile()
                    throws Exception {
        OWLOntology o = createOnto();
        OWLOntologyManager m = o.getOWLOntologyManager();
        declare(o, p);
        m.addAxiom(o, InverseFunctionalObjectProperty(p));
        OWL2ELProfile profile = new OWL2ELProfile();
        int expected = 1;
        Class[] expectedViolations = new Class[] { UseOfIllegalAxiom.class };
        runAssert(o, profile, expected, expectedViolations);
    }

    @Test
    @Tests(method = "public Object visit(OWLInverseObjectPropertiesAxiom axiom)")
    public void shouldCreateViolationForOWLInverseObjectPropertiesAxiomInOWL2ELProfile()
            throws Exception {
        OWLOntology o = createOnto();
        OWLOntologyManager m = o.getOWLOntologyManager();
        declare(o, p);
        OWLObjectProperty p1 = ObjectProperty(IRI("urn:test#objectproperty"));
        declare(o, p1);
        m.addAxiom(o, InverseObjectProperties(p, p1));
        OWL2ELProfile profile = new OWL2ELProfile();
        int expected = 1;
        Class[] expectedViolations = new Class[] { UseOfIllegalAxiom.class };
        runAssert(o, profile, expected, expectedViolations);
    }

    @Test
    @Tests(method = "public Object visit(OWLIrreflexiveObjectPropertyAxiom axiom)")
    public void
            shouldCreateViolationForOWLIrreflexiveObjectPropertyAxiomInOWL2ELProfile()
                    throws Exception {
        OWLOntology o = createOnto();
        OWLOntologyManager m = o.getOWLOntologyManager();
        declare(o, p);
        m.addAxiom(o, IrreflexiveObjectProperty(p));
        OWL2ELProfile profile = new OWL2ELProfile();
        int expected = 1;
        Class[] expectedViolations = new Class[] { UseOfIllegalAxiom.class };
        runAssert(o, profile, expected, expectedViolations);
    }

    @Test
    @Tests(method = "public Object visit(OWLSymmetricObjectPropertyAxiom axiom)")
    public void shouldCreateViolationForOWLSymmetricObjectPropertyAxiomInOWL2ELProfile()
            throws Exception {
        OWLOntology o = createOnto();
        OWLOntologyManager m = o.getOWLOntologyManager();
        declare(o, p);
        m.addAxiom(o, SymmetricObjectProperty(p));
        OWL2ELProfile profile = new OWL2ELProfile();
        int expected = 1;
        Class[] expectedViolations = new Class[] { UseOfIllegalAxiom.class };
        runAssert(o, profile, expected, expectedViolations);
    }

    @Test
    @Tests(method = "public Object visit(SWRLRule rule)")
    public void shouldCreateViolationForSWRLRuleInOWL2ELProfile() throws Exception {
        OWLOntology o = createOnto();
        OWLOntologyManager m = o.getOWLOntologyManager();
        m.addAxiom(o, df.getSWRLRule(new HashSet<SWRLAtom>(), new HashSet<SWRLAtom>()));
        OWL2ELProfile profile = new OWL2ELProfile();
        int expected = 1;
        Class[] expectedViolations = new Class[] { UseOfIllegalAxiom.class };
        runAssert(o, profile, expected, expectedViolations);
    }

    @Test
    @Tests(method = "public Object visit(OWLSubPropertyChainOfAxiom axiom)")
    public void shouldCreateViolationForOWLSubPropertyChainOfAxiomInOWL2ELProfile()
            throws Exception {
        OWLOntology o = createOnto();
        OWLOntologyManager m = o.getOWLOntologyManager();
        OWL2ELProfile profile = new OWL2ELProfile();
        OWLObjectProperty op1 = ObjectProperty(IRI("urn:test#op1"));
        OWLObjectProperty op2 = ObjectProperty(IRI("urn:test#op"));
        declare(o, op1, op, op2, cl);
        m.addAxiom(o, ObjectPropertyRange(op, cl));
        m.addAxiom(o, SubPropertyChainOf(Arrays.asList(op2, op1), op));
        int expected = 1;
        Class[] expectedViolations = new Class[] { LastPropertyInChainNotInImposedRange.class };
        runAssert(o, profile, expected, expectedViolations);
    }

    @Test
    @Tests(method = "public Object visit(OWLDatatype node)")
    public void shouldCreateViolationForOWLDatatypeInOWL2QLProfile() throws Exception {
        OWLOntology o = createOnto();
        declare(o, fakedatatype);
        OWL2QLProfile profile = new OWL2QLProfile();
        int expected = 1;
        Class[] expectedViolations = new Class[] { UseOfIllegalDataRange.class };
        runAssert(o, profile, expected, expectedViolations);
    }

    @Test
    @Tests(method = "public Object visit(OWLAnonymousIndividual individual)")
    public void shouldCreateViolationForOWLAnonymousIndividualInOWL2QLProfile()
            throws Exception {
        OWLOntology o = createOnto();
        OWLOntologyManager m = o.getOWLOntologyManager();
        OWL2QLProfile profile = new OWL2QLProfile();
        m.addAxiom(o, ClassAssertion(OWLThing(), df.getOWLAnonymousIndividual()));
        int expected = 1;
        Class[] expectedViolations = new Class[] { UseOfAnonymousIndividual.class };
        runAssert(o, profile, expected, expectedViolations);
    }

    @Test
    @Tests(method = "public Object visit(OWLHasKeyAxiom axiom)")
    public void shouldCreateViolationForOWLHasKeyAxiomInOWL2QLProfile() throws Exception {
        OWLOntology o = createOnto();
        OWLOntologyManager m = o.getOWLOntologyManager();
        declare(o, cl, op);
        m.addAxiom(o, HasKey(cl, op));
        OWL2QLProfile profile = new OWL2QLProfile();
        int expected = 1;
        Class[] expectedViolations = new Class[] { UseOfIllegalAxiom.class };
        runAssert(o, profile, expected, expectedViolations);
    }

    @Test
    @Tests(method = "public Object visit(OWLSubClassOfAxiom axiom)")
    public void shouldCreateViolationForOWLSubClassOfAxiomInOWL2QLProfile()
            throws Exception {
        OWLOntology o = createOnto();
        OWLOntologyManager m = o.getOWLOntologyManager();
        declare(o, op);
        m.addAxiom(
                o,
                SubClassOf(ObjectComplementOf(OWLNothing()),
                        ObjectUnionOf(OWLThing(), OWLNothing())));
        OWL2QLProfile profile = new OWL2QLProfile();
        int expected = 2;
        Class[] expectedViolations = new Class[] { UseOfNonSubClassExpression.class,
                UseOfNonSuperClassExpression.class };
        runAssert(o, profile, expected, expectedViolations);
    }

    @Test
    @Tests(method = "public Object visit(OWLEquivalentClassesAxiom axiom)")
    public void shouldCreateViolationForOWLEquivalentClassesAxiomInOWL2QLProfile()
            throws Exception {
        OWLOntology o = createOnto();
        OWLOntologyManager m = o.getOWLOntologyManager();
        m.addAxiom(o,
                EquivalentClasses(ObjectUnionOf(OWLNothing(), OWLThing()), OWLNothing()));
        OWL2QLProfile profile = new OWL2QLProfile();
        int expected = 1;
        Class[] expectedViolations = new Class[] { UseOfNonSubClassExpression.class };
        runAssert(o, profile, expected, expectedViolations);
    }

    @Test
    @Tests(method = "public Object visit(OWLDisjointClassesAxiom axiom)")
    public void shouldCreateViolationForOWLDisjointClassesAxiomInOWL2QLProfile()
            throws Exception {
        OWLOntology o = createOnto();
        OWLOntologyManager m = o.getOWLOntologyManager();
        OWL2QLProfile profile = new OWL2QLProfile();
        m.addAxiom(o, DisjointClasses(ObjectComplementOf(OWLThing()), OWLThing()));
        int expected = 1;
        Class[] expectedViolations = new Class[] { UseOfNonSubClassExpression.class };
        runAssert(o, profile, expected, expectedViolations);
    }

    @Test
    @Tests(method = "public Object visit(OWLObjectPropertyDomainAxiom axiom)")
    public void shouldCreateViolationForOWLObjectPropertyDomainAxiomInOWL2QLProfile()
            throws Exception {
        OWLOntology o = createOnto();
        OWLOntologyManager m = o.getOWLOntologyManager();
        declare(o, op);
        m.addAxiom(o, ObjectPropertyDomain(op, ObjectUnionOf(OWLNothing(), OWLThing())));
        OWL2QLProfile profile = new OWL2QLProfile();
        int expected = 1;
        Class[] expectedViolations = new Class[] { UseOfNonSuperClassExpression.class };
        runAssert(o, profile, expected, expectedViolations);
    }

    @Test
    @Tests(method = "public Object visit(OWLObjectPropertyRangeAxiom axiom)")
    public void shouldCreateViolationForOWLObjectPropertyRangeAxiomInOWL2QLProfile()
            throws Exception {
        OWLOntology o = createOnto();
        OWLOntologyManager m = o.getOWLOntologyManager();
        declare(o, op);
        m.addAxiom(o, ObjectPropertyRange(op, ObjectUnionOf(OWLNothing(), OWLThing())));
        OWL2QLProfile profile = new OWL2QLProfile();
        int expected = 1;
        Class[] expectedViolations = new Class[] { UseOfNonSuperClassExpression.class };
        runAssert(o, profile, expected, expectedViolations);
    }

    @Test
    @Tests(method = "public Object visit(OWLSubPropertyChainOfAxiom axiom)")
    public void shouldCreateViolationForOWLSubPropertyChainOfAxiomInOWL2QLProfile()
            throws Exception {
        OWLOntology o = createOnto();
        OWLOntologyManager m = o.getOWLOntologyManager();
        OWLObjectProperty op1 = ObjectProperty(IRI("urn:test#op"));
        declare(o, op, op1);
        m.addAxiom(o, SubPropertyChainOf(Arrays.asList(op, op1), op));
        OWL2QLProfile profile = new OWL2QLProfile();
        int expected = 1;
        Class[] expectedViolations = new Class[] { UseOfIllegalAxiom.class };
        runAssert(o, profile, expected, expectedViolations);
    }

    @Test
    @Tests(method = "public Object visit(OWLFunctionalObjectPropertyAxiom axiom)")
    public void shouldCreateViolationForOWLFunctionalObjectPropertyAxiomInOWL2QLProfile()
            throws Exception {
        OWLOntology o = createOnto();
        OWLOntologyManager m = o.getOWLOntologyManager();
        declare(o, op);
        m.addAxiom(o, FunctionalObjectProperty(op));
        OWL2QLProfile profile = new OWL2QLProfile();
        int expected = 1;
        Class[] expectedViolations = new Class[] { UseOfIllegalAxiom.class };
        runAssert(o, profile, expected, expectedViolations);
    }

    @Test
    @Tests(method = "public Object visit(OWLInverseFunctionalObjectPropertyAxiom axiom)")
    public
            void
            shouldCreateViolationForOWLInverseFunctionalObjectPropertyAxiomInOWL2QLProfile()
                    throws Exception {
        OWLOntology o = createOnto();
        OWLOntologyManager m = o.getOWLOntologyManager();
        declare(o, op);
        m.addAxiom(o, InverseFunctionalObjectProperty(op));
        OWL2QLProfile profile = new OWL2QLProfile();
        int expected = 1;
        Class[] expectedViolations = new Class[] { UseOfIllegalAxiom.class };
        runAssert(o, profile, expected, expectedViolations);
    }

    @Test
    @Tests(method = "public Object visit(OWLTransitiveObjectPropertyAxiom axiom)")
    public void shouldCreateViolationForOWLTransitiveObjectPropertyAxiomInOWL2QLProfile()
            throws Exception {
        OWLOntology o = createOnto();
        OWLOntologyManager m = o.getOWLOntologyManager();
        declare(o, op);
        m.addAxiom(o, TransitiveObjectProperty(op));
        OWL2QLProfile profile = new OWL2QLProfile();
        int expected = 1;
        Class[] expectedViolations = new Class[] { UseOfIllegalAxiom.class };
        runAssert(o, profile, expected, expectedViolations);
    }

    @Test
    @Tests(method = "public Object visit(OWLFunctionalDataPropertyAxiom axiom)")
    public void shouldCreateViolationForOWLFunctionalDataPropertyAxiomInOWL2QLProfile()
            throws Exception {
        OWLOntology o = createOnto();
        OWLOntologyManager m = o.getOWLOntologyManager();
        declare(o, datap);
        m.addAxiom(o, FunctionalDataProperty(datap));
        OWL2QLProfile profile = new OWL2QLProfile();
        int expected = 1;
        Class[] expectedViolations = new Class[] { UseOfIllegalAxiom.class };
        runAssert(o, profile, expected, expectedViolations);
    }

    @Test
    @Tests(method = "public Object visit(OWLDataPropertyDomainAxiom axiom)")
    public void shouldCreateViolationForOWLDataPropertyDomainAxiomInOWL2QLProfile()
            throws Exception {
        OWLOntology o = createOnto();
        OWLOntologyManager m = o.getOWLOntologyManager();
        declare(o, datap, op);
        m.addAxiom(o,
                DataPropertyDomain(datap, ObjectMaxCardinality(1, op, OWLNothing())));
        OWL2QLProfile profile = new OWL2QLProfile();
        int expected = 1;
        Class[] expectedViolations = new Class[] { UseOfNonSuperClassExpression.class };
        runAssert(o, profile, expected, expectedViolations);
    }

    @Test
    @Tests(method = "public Object visit(OWLClassAssertionAxiom axiom)")
    public void shouldCreateViolationForOWLClassAssertionAxiomInOWL2QLProfile()
            throws Exception {
        OWLOntology o = createOnto();
        OWLOntologyManager m = o.getOWLOntologyManager();
        OWLNamedIndividual i = NamedIndividual(IRI("urn:test#i"));
        declare(o, op, i);
        m.addAxiom(o, ClassAssertion(ObjectSomeValuesFrom(op, OWLThing()), i));
        OWL2QLProfile profile = new OWL2QLProfile();
        int expected = 1;
        Class[] expectedViolations = new Class[] { UseOfNonAtomicClassExpression.class };
        runAssert(o, profile, expected, expectedViolations);
    }

    @Test
    @Tests(method = "public Object visit(OWLSameIndividualAxiom axiom)")
    public void shouldCreateViolationForOWLSameIndividualAxiomInOWL2QLProfile()
            throws Exception {
        OWLOntology o = createOnto();
        OWLOntologyManager m = o.getOWLOntologyManager();
        m.addAxiom(
                o,
                SameIndividual(NamedIndividual(IRI("urn:test#individual1")),
                        NamedIndividual(IRI("urn:test#individual2"))));
        OWL2QLProfile profile = new OWL2QLProfile();
        int expected = 1;
        Class[] expectedViolations = new Class[] { UseOfIllegalAxiom.class };
        runAssert(o, profile, expected, expectedViolations);
    }

    @Test
    @Tests(method = "public Object visit(OWLNegativeObjectPropertyAssertionAxiom axiom)")
    public
            void
            shouldCreateViolationForOWLNegativeObjectPropertyAssertionAxiomInOWL2QLProfile()
                    throws Exception {
        OWLOntology o = createOnto();
        OWLOntologyManager m = o.getOWLOntologyManager();
        declare(o, op);
        OWLNamedIndividual i = NamedIndividual(IRI("urn:test#i"));
        OWLNamedIndividual i1 = NamedIndividual(IRI("urn:test#i"));
        declare(o, i, i1);
        m.addAxiom(o, NegativeObjectPropertyAssertion(op, i, i1));
        OWL2QLProfile profile = new OWL2QLProfile();
        int expected = 1;
        Class[] expectedViolations = new Class[] { UseOfIllegalAxiom.class };
        runAssert(o, profile, expected, expectedViolations);
    }

    @Test
    @Tests(method = "public Object visit(OWLNegativeDataPropertyAssertionAxiom axiom)")
    public
            void
            shouldCreateViolationForOWLNegativeDataPropertyAssertionAxiomInOWL2QLProfile()
                    throws Exception {
        OWLOntology o = createOnto();
        OWLOntologyManager m = o.getOWLOntologyManager();
        declare(o, datap);
        OWLNamedIndividual i = NamedIndividual(IRI("urn:test#i"));
        declare(o, i);
        m.addAxiom(o, NegativeDataPropertyAssertion(datap, i, Literal(1)));
        OWL2QLProfile profile = new OWL2QLProfile();
        int expected = 1;
        Class[] expectedViolations = new Class[] { UseOfIllegalAxiom.class };
        runAssert(o, profile, expected, expectedViolations);
    }

    @Test
    @Tests(method = "public Object visit(OWLDisjointUnionAxiom axiom)")
    public void shouldCreateViolationForOWLDisjointUnionAxiomInOWL2QLProfile()
            throws Exception {
        OWLOntology o = createOnto();
        OWLOntologyManager m = o.getOWLOntologyManager();
        declare(o, cl);
        m.addAxiom(o, DisjointUnion(cl, OWLThing(), OWLNothing()));
        OWL2QLProfile profile = new OWL2QLProfile();
        int expected = 1;
        Class[] expectedViolations = new Class[] { UseOfIllegalAxiom.class };
        runAssert(o, profile, expected, expectedViolations);
    }

    @Test
    @Tests(method = "public Object visit(OWLIrreflexiveObjectPropertyAxiom axiom)")
    public void
            shouldCreateViolationForOWLIrreflexiveObjectPropertyAxiomInOWL2QLProfile()
                    throws Exception {
        OWLOntology o = createOnto();
        OWLOntologyManager m = o.getOWLOntologyManager();
        declare(o, op);
        m.addAxiom(o, IrreflexiveObjectProperty(op));
        OWL2QLProfile profile = new OWL2QLProfile();
        int expected = 1;
        Class[] expectedViolations = new Class[] { UseOfIllegalAxiom.class };
        runAssert(o, profile, expected, expectedViolations);
    }

    @Test
    @Tests(method = "public Object visit(SWRLRule rule)")
    public void shouldCreateViolationForSWRLRuleInOWL2QLProfile() throws Exception {
        OWLOntology o = createOnto();
        OWLOntologyManager m = o.getOWLOntologyManager();
        m.addAxiom(o, df.getSWRLRule(new HashSet<SWRLAtom>(), new HashSet<SWRLAtom>()));
        OWL2QLProfile profile = new OWL2QLProfile();
        int expected = 1;
        Class[] expectedViolations = new Class[] { UseOfIllegalAxiom.class };
        runAssert(o, profile, expected, expectedViolations);
    }

    @Test
    @Tests(method = "public Object visit(OWLDataComplementOf node)")
    public void shouldCreateViolationForOWLDataComplementOfInOWL2QLProfile()
            throws Exception {
        OWLOntology o = createOnto();
        OWLOntologyManager m = o.getOWLOntologyManager();
        declare(o, datap);
        m.addAxiom(o, DataPropertyRange(datap, DataComplementOf(Integer())));
        OWL2QLProfile profile = new OWL2QLProfile();
        int expected = 1;
        Class[] expectedViolations = new Class[] { UseOfIllegalDataRange.class };
        runAssert(o, profile, expected, expectedViolations);
    }

    @Test
    @Tests(method = "public Object visit(OWLDataOneOf node)")
    public void shouldCreateViolationForOWLDataOneOfInOWL2QLProfile() throws Exception {
        OWLOntology o = createOnto();
        OWLOntologyManager m = o.getOWLOntologyManager();
        declare(o, datap);
        m.addAxiom(o, DataPropertyRange(datap, DataOneOf(Literal(1), Literal(2))));
        OWL2QLProfile profile = new OWL2QLProfile();
        int expected = 1;
        Class[] expectedViolations = new Class[] { UseOfIllegalDataRange.class };
        runAssert(o, profile, expected, expectedViolations);
    }

    @Test
    @Tests(method = "public Object visit(OWLDatatypeRestriction node)")
    public void shouldCreateViolationForOWLDatatypeRestrictionInOWL2QLProfile()
            throws Exception {
        OWLOntology o = createOnto();
        OWLOntologyManager m = o.getOWLOntologyManager();
        declare(o, datap);
        m.addAxiom(o, DATA_PROPERTY_RANGE);
        OWL2QLProfile profile = new OWL2QLProfile();
        int expected = 1;
        Class[] expectedViolations = new Class[] { UseOfIllegalDataRange.class };
        runAssert(o, profile, expected, expectedViolations);
    }

    @Test
    @Tests(method = "public Object visit(OWLDataUnionOf node)")
    public void shouldCreateViolationForOWLDataUnionOfInOWL2QLProfile() throws Exception {
        OWLOntology o = createOnto();
        OWLOntologyManager m = o.getOWLOntologyManager();
        declare(o, datap);
        m.addAxiom(o, DataPropertyRange(datap, DataUnionOf(Integer(), Boolean())));
        OWL2QLProfile profile = new OWL2QLProfile();
        int expected = 2;
        Class[] expectedViolations = new Class[] { UseOfIllegalDataRange.class,
                UseOfIllegalDataRange.class };
        runAssert(o, profile, expected, expectedViolations);
    }

    @Test
    @Tests(method = "public Object visit(OWLClassAssertionAxiom axiom)")
    public void shouldCreateViolationForOWLClassAssertionAxiomInOWL2RLProfile()
            throws Exception {
        OWLOntology o = createOnto();
        OWLOntologyManager m = o.getOWLOntologyManager();
        declare(o, op);
        m.addAxiom(
                o,
                ClassAssertion(ObjectMinCardinality(1, op, OWLThing()),
                        NamedIndividual(IRI("urn:test#i"))));
        OWL2RLProfile profile = new OWL2RLProfile();
        int expected = 1;
        Class[] expectedViolations = new Class[] { UseOfNonSuperClassExpression.class };
        runAssert(o, profile, expected, expectedViolations);
    }

    @Test
    @Tests(method = "public Object visit(OWLDataPropertyDomainAxiom axiom)")
    public void shouldCreateViolationForOWLDataPropertyDomainAxiomInOWL2RLProfile()
            throws Exception {
        OWLOntology o = createOnto();
        OWLOntologyManager m = o.getOWLOntologyManager();
        declare(o, datap, op);
        m.addAxiom(o, DataPropertyDomain(datap, ObjectMinCardinality(1, op, OWLThing())));
        OWL2RLProfile profile = new OWL2RLProfile();
        int expected = 1;
        Class[] expectedViolations = new Class[] { UseOfNonSuperClassExpression.class };
        runAssert(o, profile, expected, expectedViolations);
    }

    @Test
    @Tests(method = "public Object visit(OWLDisjointClassesAxiom axiom)")
    public void shouldCreateViolationForOWLDisjointClassesAxiomInOWL2RLProfile()
            throws Exception {
        OWLOntology o = createOnto();
        OWLOntologyManager m = o.getOWLOntologyManager();
        m.addAxiom(o, DisjointClasses(ObjectComplementOf(OWLThing()), OWLThing()));
        OWL2RLProfile profile = new OWL2RLProfile();
        int expected = 2;
        Class[] expectedViolations = new Class[] { UseOfNonSubClassExpression.class,
                UseOfNonSubClassExpression.class };
        runAssert(o, profile, expected, expectedViolations);
    }

    @Test
    @Tests(method = "public Object visit(OWLDisjointDataPropertiesAxiom axiom)")
    public void shouldCreateViolationForOWLDisjointDataPropertiesAxiomInOWL2RLProfile()
            throws Exception {
        OWLOntology o = createOnto();
        OWLOntologyManager m = o.getOWLOntologyManager();
        OWLDataProperty dp = DataProperty(IRI("urn:test#dproperty"));
        declare(o, datap, dp);
        m.addAxiom(o, DisjointDataProperties(datap, dp));
        OWL2RLProfile profile = new OWL2RLProfile();
        int expected = 1;
        Class[] expectedViolations = new Class[] { UseOfIllegalAxiom.class };
        runAssert(o, profile, expected, expectedViolations);
    }

    @Test
    @Tests(method = "public Object visit(OWLDisjointUnionAxiom axiom)")
    public void shouldCreateViolationForOWLDisjointUnionAxiomInOWL2RLProfile()
            throws Exception {
        OWLOntology o = createOnto();
        OWLOntologyManager m = o.getOWLOntologyManager();
        declare(o, cl);
        m.addAxiom(o, DisjointUnion(cl, OWLThing(), OWLNothing()));
        OWL2RLProfile profile = new OWL2RLProfile();
        int expected = 1;
        Class[] expectedViolations = new Class[] { UseOfIllegalAxiom.class };
        runAssert(o, profile, expected, expectedViolations);
    }

    @Test
    @Tests(method = "public Object visit(OWLEquivalentClassesAxiom axiom)")
    public void shouldCreateViolationForOWLEquivalentClassesAxiomInOWL2RLProfile()
            throws Exception {
        OWLOntology o = createOnto();
        OWLOntologyManager m = o.getOWLOntologyManager();
        m.addAxiom(o, EquivalentClasses(ObjectComplementOf(OWLThing()), OWLNothing()));
        OWL2RLProfile profile = new OWL2RLProfile();
        int expected = 1;
        Class[] expectedViolations = new Class[] { UseOfNonEquivalentClassExpression.class };
        runAssert(o, profile, expected, expectedViolations);
    }

    @Test
    @Tests(method = "public Object visit(OWLEquivalentDataPropertiesAxiom axiom)")
    public void shouldCreateViolationForOWLEquivalentDataPropertiesAxiomInOWL2RLProfile()
            throws Exception {
        OWLOntology o = createOnto();
        OWLOntologyManager m = o.getOWLOntologyManager();
        OWLDataProperty dp = DataProperty(IRI("urn:test#test"));
        declare(o, datap, dp);
        m.addAxiom(o, EquivalentDataProperties(datap, dp));
        OWL2RLProfile profile = new OWL2RLProfile();
        int expected = 1;
        Class[] expectedViolations = new Class[] { UseOfIllegalAxiom.class };
        runAssert(o, profile, expected, expectedViolations);
    }

    @Ignore
    @Test
    @Tests(method = "public Object visit(OWLFunctionalDataPropertyAxiom axiom)")
    public void shouldCreateViolationForOWLFunctionalDataPropertyAxiomInOWL2RLProfile()
            throws Exception {
        OWLOntology o = createOnto();
        OWLOntologyManager m = o.getOWLOntologyManager();
        declare(o, datap);
        m.addAxiom(o, FunctionalDataProperty(datap));
        OWL2RLProfile profile = new OWL2RLProfile();
        int expected = 1;
        Class[] expectedViolations = new Class[] { Object.class };
        runAssert(o, profile, expected, expectedViolations);
    }

    @Test
    @Tests(method = "public Object visit(OWLHasKeyAxiom axiom)")
    public void shouldCreateViolationForOWLHasKeyAxiomInOWL2RLProfile() throws Exception {
        OWLOntology o = createOnto();
        OWLOntologyManager m = o.getOWLOntologyManager();
        declare(o, cl, op);
        m.addAxiom(o, HasKey(ObjectComplementOf(cl), op));
        OWL2RLProfile profile = new OWL2RLProfile();
        int expected = 1;
        Class[] expectedViolations = new Class[] { UseOfNonSubClassExpression.class };
        runAssert(o, profile, expected, expectedViolations);
    }

    @Test
    @Tests(method = "public Object visit(OWLObjectPropertyDomainAxiom axiom)")
    public void shouldCreateViolationForOWLObjectPropertyDomainAxiomInOWL2RLProfile()
            throws Exception {
        OWLOntology o = createOnto();
        OWLOntologyManager m = o.getOWLOntologyManager();
        declare(o, op, op);
        m.addAxiom(o, ObjectPropertyDomain(op, ObjectMinCardinality(1, op, OWLThing())));
        OWL2RLProfile profile = new OWL2RLProfile();
        int expected = 1;
        Class[] expectedViolations = new Class[] { UseOfNonSuperClassExpression.class };
        runAssert(o, profile, expected, expectedViolations);
    }

    @Test
    @Tests(method = "public Object visit(OWLObjectPropertyRangeAxiom axiom)")
    public void shouldCreateViolationForOWLObjectPropertyRangeAxiomInOWL2RLProfile()
            throws Exception {
        OWLOntology o = createOnto();
        OWLOntologyManager m = o.getOWLOntologyManager();
        declare(o, op);
        m.addAxiom(o, ObjectPropertyRange(op, ObjectMinCardinality(1, op, OWLThing())));
        OWL2RLProfile profile = new OWL2RLProfile();
        int expected = 1;
        Class[] expectedViolations = new Class[] { UseOfNonSuperClassExpression.class };
        runAssert(o, profile, expected, expectedViolations);
    }

    @Test
    @Tests(method = "public Object visit(OWLSubClassOfAxiom axiom)")
    public void shouldCreateViolationForOWLSubClassOfAxiomInOWL2RLProfile()
            throws Exception {
        OWLOntology o = createOnto();
        OWLOntologyManager m = o.getOWLOntologyManager();
        m.addAxiom(
                o,
                SubClassOf(ObjectComplementOf(OWLThing()),
                        ObjectOneOf(NamedIndividual(IRI("urn:test#test")))));
        OWL2RLProfile profile = new OWL2RLProfile();
        int expected = 2;
        Class[] expectedViolations = new Class[] { UseOfNonSubClassExpression.class,
                UseOfNonSuperClassExpression.class };
        runAssert(o, profile, expected, expectedViolations);
    }

    @Test
    @Tests(method = "public Object visit(SWRLRule rule)")
    public void shouldCreateViolationForSWRLRuleInOWL2RLProfile() throws Exception {
        OWLOntology o = createOnto();
        OWLOntologyManager m = o.getOWLOntologyManager();
        m.addAxiom(o, df.getSWRLRule(new HashSet<SWRLAtom>(), new HashSet<SWRLAtom>()));
        OWL2RLProfile profile = new OWL2RLProfile();
        int expected = 1;
        Class[] expectedViolations = new Class[] { UseOfIllegalAxiom.class };
        runAssert(o, profile, expected, expectedViolations);
    }

    @Ignore
    @Test
    @Tests(method = "public Object visit(OWLDataComplementOf node)")
    public void shouldCreateViolationForOWLDataComplementOfInOWL2RLProfile()
            throws Exception {
        OWLOntology o = createOnto();
        OWL2RLProfile profile = new OWL2RLProfile();
        int expected = 2;
        Class[] expectedViolations = new Class[] { Object.class };
        runAssert(o, profile, expected, expectedViolations);
    }

    @Test
    @Tests(method = "public Object visit(OWLDataIntersectionOf node)")
    public void shouldCreateViolationForOWLDataIntersectionOfInOWL2RLProfile()
            throws Exception {
        OWLOntology o = createOnto();
        OWLOntologyManager m = o.getOWLOntologyManager();
        declare(o, datap);
        m.addAxiom(o, DataPropertyRange(datap, DataIntersectionOf(Integer(), Boolean())));
        OWL2RLProfile profile = new OWL2RLProfile();
        int expected = 1;
        Class[] expectedViolations = new Class[] { UseOfIllegalDataRange.class };
        runAssert(o, profile, expected, expectedViolations);
    }

    @Test
    @Tests(method = "public Object visit(OWLDataOneOf node)")
    public void shouldCreateViolationForOWLDataOneOfInOWL2RLProfile() throws Exception {
        OWLOntology o = createOnto();
        OWLOntologyManager m = o.getOWLOntologyManager();
        declare(o, datap);
        m.addAxiom(o, DataPropertyRange(datap, DataOneOf(Literal(1), Literal(2))));
        OWL2RLProfile profile = new OWL2RLProfile();
        int expected = 1;
        Class[] expectedViolations = new Class[] { UseOfIllegalDataRange.class };
        runAssert(o, profile, expected, expectedViolations);
    }

    @Test
    @Tests(method = "public Object visit(OWLDatatype node)")
    public void shouldCreateViolationForOWLDatatypeInOWL2RLProfile() throws Exception {
        OWLOntology o = createOnto();
        declare(o, Datatype(IRI("urn:test#test")));
        OWL2RLProfile profile = new OWL2RLProfile();
        int expected = 1;
        Class[] expectedViolations = new Class[] { UseOfIllegalDataRange.class };
        runAssert(o, profile, expected, expectedViolations);
    }

    @Test
    @Tests(method = "public Object visit(OWLDatatypeRestriction node)")
    public void shouldCreateViolationForOWLDatatypeRestrictionInOWL2RLProfile()
            throws Exception {
        OWLOntology o = createOnto();
        OWLOntologyManager m = o.getOWLOntologyManager();
        declare(o, datap);
        m.addAxiom(o, DATA_PROPERTY_RANGE);
        OWL2RLProfile profile = new OWL2RLProfile();
        int expected = 1;
        Class[] expectedViolations = new Class[] { UseOfIllegalDataRange.class };
        runAssert(o, profile, expected, expectedViolations);
    }

    @Test
    @Tests(method = "public Object visit(OWLDataUnionOf node)")
    public void shouldCreateViolationForOWLDataUnionOfInOWL2RLProfile() throws Exception {
        OWLOntology o = createOnto();
        OWLOntologyManager m = o.getOWLOntologyManager();
        declare(o, datap);
        m.addAxiom(o, DataPropertyRange(datap, DataUnionOf(Double(), Integer())));
        OWL2RLProfile profile = new OWL2RLProfile();
        int expected = 1;
        Class[] expectedViolations = new Class[] { UseOfIllegalDataRange.class };
        runAssert(o, profile, expected, expectedViolations);
    }

    @Test
    @Tests(method = "public Object visit(OWLDatatypeDefinitionAxiom axiom)")
    public void shouldCreateViolationForOWLDatatypeDefinitionAxiomInOWL2RLProfile()
            throws Exception {
        OWLOntology o = createOnto();
        OWLOntologyManager m = o.getOWLOntologyManager();
        OWLDatatype datatype = Datatype(IRI("urn:test#datatype"));
        declare(o, datatype);
        m.addAxiom(o, DatatypeDefinition(datatype, Boolean()));
        OWL2RLProfile profile = new OWL2RLProfile();
        int expected = 3;
        Class[] expectedViolations = new Class[] { UseOfIllegalAxiom.class,
                UseOfIllegalDataRange.class, UseOfIllegalDataRange.class };
        runAssert(o, profile, expected, expectedViolations);
    }

    private OWLOntology createOnto() throws OWLOntologyCreationException {
        return OWLManager.createOWLOntologyManager().createOntology(onto);
    }
}
