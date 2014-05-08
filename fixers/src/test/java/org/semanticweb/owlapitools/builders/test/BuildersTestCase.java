package org.semanticweb.owlapitools.builders.test;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;
import org.semanticweb.owlapi.model.EntityType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLFacetRestriction;
import org.semanticweb.owlapi.model.OWLImportsDeclaration;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.SWRLAtom;
import org.semanticweb.owlapi.model.SWRLDArgument;
import org.semanticweb.owlapi.model.SWRLIArgument;
import org.semanticweb.owlapi.vocab.OWLFacet;
import org.semanticweb.owlapitools.builders.BuilderAnnotation;
import org.semanticweb.owlapitools.builders.BuilderAnnotationAssertion;
import org.semanticweb.owlapitools.builders.BuilderAnnotationProperty;
import org.semanticweb.owlapitools.builders.BuilderAnnotationPropertyDomain;
import org.semanticweb.owlapitools.builders.BuilderAnnotationPropertyRange;
import org.semanticweb.owlapitools.builders.BuilderAnonymousIndividual;
import org.semanticweb.owlapitools.builders.BuilderAsymmetricObjectProperty;
import org.semanticweb.owlapitools.builders.BuilderClass;
import org.semanticweb.owlapitools.builders.BuilderClassAssertion;
import org.semanticweb.owlapitools.builders.BuilderComplementOf;
import org.semanticweb.owlapitools.builders.BuilderDataAllValuesFrom;
import org.semanticweb.owlapitools.builders.BuilderDataComplementOf;
import org.semanticweb.owlapitools.builders.BuilderDataExactCardinality;
import org.semanticweb.owlapitools.builders.BuilderDataHasValue;
import org.semanticweb.owlapitools.builders.BuilderDataIntersectionOf;
import org.semanticweb.owlapitools.builders.BuilderDataMaxCardinality;
import org.semanticweb.owlapitools.builders.BuilderDataMinCardinality;
import org.semanticweb.owlapitools.builders.BuilderDataOneOf;
import org.semanticweb.owlapitools.builders.BuilderDataProperty;
import org.semanticweb.owlapitools.builders.BuilderDataPropertyAssertion;
import org.semanticweb.owlapitools.builders.BuilderDataPropertyDomain;
import org.semanticweb.owlapitools.builders.BuilderDataPropertyRange;
import org.semanticweb.owlapitools.builders.BuilderDataSomeValuesFrom;
import org.semanticweb.owlapitools.builders.BuilderDataUnionOf;
import org.semanticweb.owlapitools.builders.BuilderDatatype;
import org.semanticweb.owlapitools.builders.BuilderDatatypeDefinition;
import org.semanticweb.owlapitools.builders.BuilderDatatypeRestriction;
import org.semanticweb.owlapitools.builders.BuilderDeclaration;
import org.semanticweb.owlapitools.builders.BuilderDifferentIndividuals;
import org.semanticweb.owlapitools.builders.BuilderDisjointClasses;
import org.semanticweb.owlapitools.builders.BuilderDisjointDataProperties;
import org.semanticweb.owlapitools.builders.BuilderDisjointObjectProperties;
import org.semanticweb.owlapitools.builders.BuilderDisjointUnion;
import org.semanticweb.owlapitools.builders.BuilderEntity;
import org.semanticweb.owlapitools.builders.BuilderEquivalentClasses;
import org.semanticweb.owlapitools.builders.BuilderEquivalentDataProperties;
import org.semanticweb.owlapitools.builders.BuilderEquivalentObjectProperties;
import org.semanticweb.owlapitools.builders.BuilderFacetRestriction;
import org.semanticweb.owlapitools.builders.BuilderFunctionalDataProperty;
import org.semanticweb.owlapitools.builders.BuilderFunctionalObjectProperty;
import org.semanticweb.owlapitools.builders.BuilderHasKey;
import org.semanticweb.owlapitools.builders.BuilderImportsDeclaration;
import org.semanticweb.owlapitools.builders.BuilderInverseFunctionalObjectProperty;
import org.semanticweb.owlapitools.builders.BuilderInverseObjectProperties;
import org.semanticweb.owlapitools.builders.BuilderIrreflexiveObjectProperty;
import org.semanticweb.owlapitools.builders.BuilderLiteral;
import org.semanticweb.owlapitools.builders.BuilderNamedIndividual;
import org.semanticweb.owlapitools.builders.BuilderNegativeDataPropertyAssertion;
import org.semanticweb.owlapitools.builders.BuilderNegativeObjectPropertyAssertion;
import org.semanticweb.owlapitools.builders.BuilderObjectAllValuesFrom;
import org.semanticweb.owlapitools.builders.BuilderObjectExactCardinality;
import org.semanticweb.owlapitools.builders.BuilderObjectHasSelf;
import org.semanticweb.owlapitools.builders.BuilderObjectHasValue;
import org.semanticweb.owlapitools.builders.BuilderObjectIntersectionOf;
import org.semanticweb.owlapitools.builders.BuilderObjectInverseOf;
import org.semanticweb.owlapitools.builders.BuilderObjectMaxCardinality;
import org.semanticweb.owlapitools.builders.BuilderObjectMinCardinality;
import org.semanticweb.owlapitools.builders.BuilderObjectProperty;
import org.semanticweb.owlapitools.builders.BuilderObjectPropertyAssertion;
import org.semanticweb.owlapitools.builders.BuilderObjectPropertyDomain;
import org.semanticweb.owlapitools.builders.BuilderObjectPropertyRange;
import org.semanticweb.owlapitools.builders.BuilderObjectSomeValuesFrom;
import org.semanticweb.owlapitools.builders.BuilderOneOf;
import org.semanticweb.owlapitools.builders.BuilderPropertyChain;
import org.semanticweb.owlapitools.builders.BuilderReflexiveObjectProperty;
import org.semanticweb.owlapitools.builders.BuilderSWRLBuiltInAtom;
import org.semanticweb.owlapitools.builders.BuilderSWRLClassAtom;
import org.semanticweb.owlapitools.builders.BuilderSWRLDataPropertyAtom;
import org.semanticweb.owlapitools.builders.BuilderSWRLDataRangeAtom;
import org.semanticweb.owlapitools.builders.BuilderSWRLDifferentIndividualsAtom;
import org.semanticweb.owlapitools.builders.BuilderSWRLIndividualArgument;
import org.semanticweb.owlapitools.builders.BuilderSWRLLiteralArgument;
import org.semanticweb.owlapitools.builders.BuilderSWRLObjectPropertyAtom;
import org.semanticweb.owlapitools.builders.BuilderSWRLRule;
import org.semanticweb.owlapitools.builders.BuilderSWRLSameIndividualAtom;
import org.semanticweb.owlapitools.builders.BuilderSWRLVariable;
import org.semanticweb.owlapitools.builders.BuilderSameIndividual;
import org.semanticweb.owlapitools.builders.BuilderSubAnnotationPropertyOf;
import org.semanticweb.owlapitools.builders.BuilderSubClass;
import org.semanticweb.owlapitools.builders.BuilderSubDataProperty;
import org.semanticweb.owlapitools.builders.BuilderSubObjectProperty;
import org.semanticweb.owlapitools.builders.BuilderSymmetricObjectProperty;
import org.semanticweb.owlapitools.builders.BuilderTransitiveObjectProperty;
import org.semanticweb.owlapitools.builders.BuilderUnionOf;

import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

@SuppressWarnings("javadoc")
public class BuildersTestCase {
    private OWLDataFactory df = new OWLDataFactoryImpl();
    private OWLAnnotationProperty ap = df.getOWLAnnotationProperty(IRI
            .create("urn:test#ann"));
    private OWLObjectProperty op = df.getOWLObjectProperty(IRI.create("urn:test#op"));
    private OWLDataProperty dp = df.getOWLDataProperty(IRI.create("urn:test#dp"));
    private OWLLiteral lit = df.getOWLLiteral(false);
    private IRI iri = IRI.create("urn:test#iri");
    private Set<OWLAnnotation> annotations = new HashSet<OWLAnnotation>(Arrays.asList(df
            .getOWLAnnotation(ap, df.getOWLLiteral("test"))));
    private OWLClass ce = df.getOWLClass(IRI.create("urn:test#c"));
    private OWLNamedIndividual i = df.getOWLNamedIndividual(IRI.create("urn:test#i"));
    private OWLDatatype d = df.getBooleanOWLDatatype();
    private Set<OWLDataProperty> dps = new HashSet<OWLDataProperty>(Arrays.asList(
            df.getOWLDataProperty(iri), dp));
    private Set<OWLObjectProperty> ops = new HashSet<OWLObjectProperty>(Arrays.asList(
            df.getOWLObjectProperty(iri), op));
    private Set<OWLClass> classes = new HashSet<OWLClass>(Arrays.asList(
            df.getOWLClass(iri), ce));
    private Set<OWLIndividual> inds = new HashSet<OWLIndividual>(Arrays.asList(i,
            df.getOWLNamedIndividual(iri)));
    private SWRLDArgument var1 = df.getSWRLVariable(IRI.create("var1"));
    private SWRLIArgument var2 = df.getSWRLVariable(IRI.create("var2"));
    private SWRLAtom v1 = df.getSWRLBuiltInAtom(
            IRI.create("v1"),
            Arrays.asList((SWRLDArgument) df.getSWRLVariable(IRI.create("var3")),
                    df.getSWRLVariable(IRI.create("var4"))));
    private SWRLAtom v2 = df.getSWRLBuiltInAtom(
            IRI.create("v2"),
            Arrays.asList((SWRLDArgument) df.getSWRLVariable(IRI.create("var5")),
                    df.getSWRLVariable(IRI.create("var6"))));
    private Set<SWRLAtom> body = new HashSet<SWRLAtom>(Arrays.asList(v1));
    private Set<SWRLAtom> head = new HashSet<SWRLAtom>(Arrays.asList(v2));

    @Test
    public void shouldBuildAnnotation() {
        // given
        BuilderAnnotation builder = new BuilderAnnotation().withProperty(ap).withValue(
                lit);
        OWLObject expected = df.getOWLAnnotation(ap, lit);
        // when
        OWLObject built = builder.buildObject();
        // then
        assertEquals(expected, built);
    }

    @Test
    public void shouldBuildAnnotationAssertion() {
        // given
        BuilderAnnotationAssertion builder = new BuilderAnnotationAssertion()
                .withAnnotations(annotations).withProperty(ap).withSubject(iri)
                .withValue(lit);
        OWLObject expected = df.getOWLAnnotationAssertionAxiom(ap, iri, lit, annotations);
        // when
        OWLObject built = builder.buildObject();
        // then
        assertEquals(expected, built);
    }

    @Test
    public void shouldBuildAnnotationProperty() {
        // given
        BuilderAnnotationProperty builder = new BuilderAnnotationProperty().withIRI(iri);
        OWLObject expected = df.getOWLAnnotationProperty(iri);
        // when
        OWLObject built = builder.buildObject();
        // then
        assertEquals(expected, built);
    }

    @Test
    public void shouldBuildAnnotationPropertyDomain() {
        // given
        BuilderAnnotationPropertyDomain builder = new BuilderAnnotationPropertyDomain()
                .withProperty(ap).withDomain(iri).withAnnotations(annotations);
        OWLObject expected = df.getOWLAnnotationPropertyDomainAxiom(ap, iri, annotations);
        // when
        OWLObject built = builder.buildObject();
        // then
        assertEquals(expected, built);
    }

    @Test
    public void shouldBuildAnnotationPropertyRange() {
        // given
        BuilderAnnotationPropertyRange builder = new BuilderAnnotationPropertyRange()
                .withProperty(ap).withRange(iri).withAnnotations(annotations);
        OWLObject expected = df.getOWLAnnotationPropertyRangeAxiom(ap, iri, annotations);
        // when
        OWLObject built = builder.buildObject();
        // then
        assertEquals(expected, built);
    }

    @Test
    public void shouldBuildAnonymousIndividual() {
        // given
        BuilderAnonymousIndividual builder = new BuilderAnonymousIndividual()
                .withId("id");
        OWLObject expected = df.getOWLAnonymousIndividual("id");
        // when
        OWLObject built = builder.buildObject();
        // then
        assertEquals(expected, built);
    }

    @Test
    public void shouldBuildAsymmetricObjectProperty() {
        // given
        BuilderAsymmetricObjectProperty builder = new BuilderAsymmetricObjectProperty()
                .withProperty(op).withAnnotations(annotations);
        OWLObject expected = df.getOWLAsymmetricObjectPropertyAxiom(op, annotations);
        // when
        OWLObject built = builder.buildObject();
        // then
        assertEquals(expected, built);
    }

    @Test
    public void shouldBuildClass() {
        // given
        BuilderClass builder = new BuilderClass().withIRI(iri);
        OWLObject expected = df.getOWLClass(iri);
        // when
        OWLObject built = builder.buildObject();
        // then
        assertEquals(expected, built);
    }

    @Test
    public void shouldBuildClassAssertion() {
        // given
        BuilderClassAssertion builder = new BuilderClassAssertion().withClass(ce)
                .withIndividual(i).withAnnotations(annotations);
        OWLObject expected = df.getOWLClassAssertionAxiom(ce, i, annotations);
        // when
        OWLObject built = builder.buildObject();
        // then
        assertEquals(expected, built);
    }

    @Test
    public void shouldBuildComplementOf() {
        // given
        BuilderComplementOf builder = new BuilderComplementOf().withClass(ce);
        OWLObject expected = df.getOWLObjectComplementOf(ce);
        // when
        OWLObject built = builder.buildObject();
        // then
        assertEquals(expected, built);
    }

    @Test
    public void shouldBuildDataAllValuesFrom() {
        // given
        BuilderDataAllValuesFrom builder = new BuilderDataAllValuesFrom()
                .withProperty(dp).withRange(d);
        OWLObject expected = df.getOWLDataAllValuesFrom(dp, d);
        // when
        OWLObject built = builder.buildObject();
        // then
        assertEquals(expected, built);
    }

    @Test
    public void shouldBuildDataComplementOf() {
        // given
        BuilderDataComplementOf builder = new BuilderDataComplementOf().withRange(d);
        OWLObject expected = df.getOWLDataComplementOf(d);
        // when
        OWLObject built = builder.buildObject();
        // then
        assertEquals(expected, built);
    }

    @Test
    public void shouldBuildDataExactCardinality() {
        // given
        BuilderDataExactCardinality builder = new BuilderDataExactCardinality()
                .withCardinality(1).withProperty(dp).withRange(d);
        OWLObject expected = df.getOWLDataExactCardinality(1, dp, d);
        // when
        OWLObject built = builder.buildObject();
        // then
        assertEquals(expected, built);
    }

    @Test
    public void shouldBuildDataHasValue() {
        // given
        BuilderDataHasValue builder = new BuilderDataHasValue().withProperty(dp)
                .withLiteral(lit);
        OWLObject expected = df.getOWLDataHasValue(dp, lit);
        // when
        OWLObject built = builder.buildObject();
        // then
        assertEquals(expected, built);
    }

    @Test
    public void shouldBuildDataIntersectionOf() {
        // given
        BuilderDataIntersectionOf builder = new BuilderDataIntersectionOf().withItem(d)
                .withItem(df.getFloatOWLDatatype());
        OWLObject expected = df.getOWLDataIntersectionOf(d, df.getFloatOWLDatatype());
        // when
        OWLObject built = builder.buildObject();
        // then
        assertEquals(expected, built);
    }

    @Test
    public void shouldBuildDataMaxCardinality() {
        // given
        BuilderDataMaxCardinality builder = new BuilderDataMaxCardinality()
                .withCardinality(1).withProperty(dp).withRange(d);
        OWLObject expected = df.getOWLDataMaxCardinality(1, dp, d);
        // when
        OWLObject built = builder.buildObject();
        // then
        assertEquals(expected, built);
    }

    @Test
    public void shouldBuildDataMinCardinality() {
        // given
        BuilderDataMinCardinality builder = new BuilderDataMinCardinality()
                .withCardinality(1).withProperty(dp).withRange(d);
        OWLObject expected = df.getOWLDataMinCardinality(1, dp, d);
        // when
        OWLObject built = builder.buildObject();
        // then
        assertEquals(expected, built);
    }

    @Test
    public void shouldBuildDataOneOf() {
        // given
        BuilderDataOneOf builder = new BuilderDataOneOf().withItem(lit);
        OWLObject expected = df.getOWLDataOneOf(lit);
        // when
        OWLObject built = builder.buildObject();
        // then
        assertEquals(expected, built);
    }

    @Test
    public void shouldBuildDataProperty() {
        // given
        BuilderDataProperty builder = new BuilderDataProperty().withIRI(iri);
        OWLObject expected = df.getOWLDataProperty(iri);
        // when
        OWLObject built = builder.buildObject();
        // then
        assertEquals(expected, built);
    }

    @Test
    public void shouldBuildDataPropertyAssertion() {
        // given
        BuilderDataPropertyAssertion builder = new BuilderDataPropertyAssertion()
                .withProperty(dp).withSubject(i).withValue(lit)
                .withAnnotations(annotations);
        OWLObject expected = df.getOWLDataPropertyAssertionAxiom(dp, i, lit, annotations);
        // when
        OWLObject built = builder.buildObject();
        // then
        assertEquals(expected, built);
    }

    @Test
    public void shouldBuildDataPropertyDomain() {
        // given
        BuilderDataPropertyDomain builder = new BuilderDataPropertyDomain()
                .withProperty(dp).withDomain(ce).withAnnotations(annotations);
        OWLObject expected = df.getOWLDataPropertyDomainAxiom(dp, ce, annotations);
        // when
        OWLObject built = builder.buildObject();
        // then
        assertEquals(expected, built);
    }

    @Test
    public void shouldBuildDataPropertyRange() {
        // given
        BuilderDataPropertyRange builder = new BuilderDataPropertyRange()
                .withProperty(dp).withRange(d).withAnnotations(annotations);
        OWLObject expected = df.getOWLDataPropertyRangeAxiom(dp, d, annotations);
        // when
        OWLObject built = builder.buildObject();
        // then
        assertEquals(expected, built);
    }

    @Test
    public void shouldBuildDataSomeValuesFrom() {
        // given
        BuilderDataSomeValuesFrom builder = new BuilderDataSomeValuesFrom().withProperty(
                dp).withRange(d);
        OWLObject expected = df.getOWLDataSomeValuesFrom(dp, d);
        // when
        OWLObject built = builder.buildObject();
        // then
        assertEquals(expected, built);
    }

    @Test
    public void shouldBuildDatatype() {
        // given
        BuilderDatatype builder = new BuilderDatatype().withIRI(iri).withAnnotations(
                annotations);
        OWLObject expected = df.getOWLDatatype(iri);
        // when
        OWLObject built = builder.buildObject();
        // then
        assertEquals(expected, built);
    }

    @Test
    public void shouldBuildDatatypeDefinition() {
        // given
        BuilderDatatypeDefinition builder = new BuilderDatatypeDefinition().with(d)
                .withType(df.getDoubleOWLDatatype()).withAnnotations(annotations);
        OWLObject expected = df.getOWLDatatypeDefinitionAxiom(d,
                df.getDoubleOWLDatatype(), annotations);
        // when
        OWLObject built = builder.buildObject();
        // then
        assertEquals(expected, built);
    }

    @Test
    public void shouldBuildDatatypeRestriction() {
        // given
        OWLFacetRestriction r = df.getOWLFacetRestriction(OWLFacet.MAX_LENGTH, lit);
        BuilderDatatypeRestriction builder = new BuilderDatatypeRestriction().withItem(r)
                .withDatatype(d);
        OWLObject expected = df.getOWLDatatypeRestriction(d, r);
        // when
        OWLObject built = builder.buildObject();
        // then
        assertEquals(expected, built);
    }

    @Test
    public void shouldBuildDataUnionOf() {
        // given
        BuilderDataUnionOf builder = new BuilderDataUnionOf().withItem(d).withItem(
                df.getDoubleOWLDatatype());
        OWLObject expected = df.getOWLDataUnionOf(d, df.getDoubleOWLDatatype());
        // when
        OWLObject built = builder.buildObject();
        // then
        assertEquals(expected, built);
    }

    @Test
    public void shouldBuildDeclaration() {
        // given
        BuilderDeclaration builder = new BuilderDeclaration().withEntity(ce)
                .withAnnotations(annotations);
        OWLObject expected = df.getOWLDeclarationAxiom(ce, annotations);
        // when
        OWLObject built = builder.buildObject();
        // then
        assertEquals(expected, built);
    }

    @Test
    public void shouldBuildDifferentIndividuals() {
        // given
        BuilderDifferentIndividuals builder = new BuilderDifferentIndividuals().withItem(
                i).withItem(df.getOWLNamedIndividual(iri));
        OWLObject expected = df.getOWLDifferentIndividualsAxiom(i,
                df.getOWLNamedIndividual(iri));
        // when
        OWLObject built = builder.buildObject();
        // then
        assertEquals(expected, built);
    }

    @Test
    public void shouldBuildDisjointClasses() {
        // given
        BuilderDisjointClasses builder = new BuilderDisjointClasses().withItem(ce)
                .withItem(df.getOWLClass(iri));
        OWLObject expected = df.getOWLDisjointClassesAxiom(ce, df.getOWLClass(iri));
        // when
        OWLObject built = builder.buildObject();
        // then
        assertEquals(expected, built);
    }

    @Test
    public void shouldBuildDisjointDataProperties() {
        // given
        BuilderDisjointDataProperties builder = new BuilderDisjointDataProperties()
                .withItems(dps).withAnnotations(annotations);
        OWLObject expected = df.getOWLDisjointDataPropertiesAxiom(dps, annotations);
        // when
        OWLObject built = builder.buildObject();
        // then
        assertEquals(expected, built);
    }

    @Test
    public void shouldBuildDisjointObjectProperties() {
        // given
        BuilderDisjointObjectProperties builder = new BuilderDisjointObjectProperties()
                .withItems(ops).withAnnotations(annotations);
        OWLObject expected = df.getOWLDisjointObjectPropertiesAxiom(ops, annotations);
        // when
        OWLObject built = builder.buildObject();
        // then
        assertEquals(expected, built);
    }

    @Test
    public void shouldBuildDisjointUnion() {
        // given
        BuilderDisjointUnion builder = new BuilderDisjointUnion().withClass(ce)
                .withItems(classes).withAnnotations(annotations);
        OWLObject expected = df.getOWLDisjointUnionAxiom(ce, classes, annotations);
        // when
        OWLObject built = builder.buildObject();
        // then
        assertEquals(expected, built);
    }

    @Test
    public void shouldBuildEntity() {
        // given
        BuilderEntity builder = new BuilderEntity().withIRI(iri).withType(
                EntityType.CLASS);
        OWLObject expected = df.getOWLClass(iri);
        // when
        OWLObject built = builder.buildObject();
        // then
        assertEquals(expected, built);
    }

    @Test
    public void shouldBuildEquivalentClasses() {
        // given
        BuilderEquivalentClasses builder = new BuilderEquivalentClasses().withItems(
                classes).withAnnotations(annotations);
        OWLObject expected = df.getOWLEquivalentClassesAxiom(classes, annotations);
        // when
        OWLObject built = builder.buildObject();
        // then
        assertEquals(expected, built);
    }

    @Test
    public void shouldBuildEquivalentDataProperties() {
        // given
        BuilderEquivalentDataProperties builder = new BuilderEquivalentDataProperties()
                .withItems(dps).withAnnotations(annotations);
        OWLObject expected = df.getOWLEquivalentDataPropertiesAxiom(dps, annotations);
        // when
        OWLObject built = builder.buildObject();
        // then
        assertEquals(expected, built);
    }

    @Test
    public void shouldBuildEquivalentObjectProperties() {
        // given
        BuilderEquivalentObjectProperties builder = new BuilderEquivalentObjectProperties()
                .withItems(ops).withAnnotations(annotations);
        OWLObject expected = df.getOWLEquivalentObjectPropertiesAxiom(ops, annotations);
        // when
        OWLObject built = builder.buildObject();
        // then
        assertEquals(expected, built);
    }

    @Test
    public void shouldBuildFacetRestriction() {
        // given
        BuilderFacetRestriction builder = new BuilderFacetRestriction().withLiteral(lit)
                .withFacet(OWLFacet.MAX_EXCLUSIVE);
        OWLObject expected = df.getOWLFacetRestriction(OWLFacet.MAX_EXCLUSIVE, lit);
        // when
        OWLObject built = builder.buildObject();
        // then
        assertEquals(expected, built);
    }

    @Test
    public void shouldBuildFunctionalDataProperty() {
        // given
        BuilderFunctionalDataProperty builder = new BuilderFunctionalDataProperty()
                .withProperty(dp).withAnnotations(annotations);
        OWLObject expected = df.getOWLFunctionalDataPropertyAxiom(dp, annotations);
        // when
        OWLObject built = builder.buildObject();
        // then
        assertEquals(expected, built);
    }

    @Test
    public void shouldBuildFunctionalObjectProperty() {
        // given
        BuilderFunctionalObjectProperty builder = new BuilderFunctionalObjectProperty()
                .withProperty(op).withAnnotations(annotations);
        OWLObject expected = df.getOWLFunctionalObjectPropertyAxiom(op, annotations);
        // when
        OWLObject built = builder.buildObject();
        // then
        assertEquals(expected, built);
    }

    @Test
    public void shouldBuildHasKey() {
        // given
        BuilderHasKey builder = new BuilderHasKey().withAnnotations(annotations)
                .withClass(ce).withItems(ops);
        OWLObject expected = df.getOWLHasKeyAxiom(ce, ops, annotations);
        // when
        OWLObject built = builder.buildObject();
        // then
        assertEquals(expected, built);
    }

    @Test
    public void shouldBuildImportsDeclarationProperty() {
        // given
        BuilderImportsDeclaration builder = new BuilderImportsDeclaration()
                .withImportedOntology(iri);
        OWLImportsDeclaration expected = df.getOWLImportsDeclaration(iri);
        // when
        OWLImportsDeclaration built = builder.buildObject();
        // then
        assertEquals(expected, built);
    }

    @Test
    public void shouldBuildInverseFunctionalObjectProperty() {
        // given
        BuilderInverseFunctionalObjectProperty builder = new BuilderInverseFunctionalObjectProperty()
                .withProperty(op).withAnnotations(annotations);
        OWLObject expected = df.getOWLInverseFunctionalObjectPropertyAxiom(op,
                annotations);
        // when
        OWLObject built = builder.buildObject();
        // then
        assertEquals(expected, built);
    }

    @Test
    public void shouldBuildInverseObjectProperties() {
        // given
        BuilderInverseObjectProperties builder = new BuilderInverseObjectProperties()
                .withProperty(op).withInverseProperty(op).withAnnotations(annotations);
        OWLObject expected = df.getOWLInverseObjectPropertiesAxiom(op, op, annotations);
        // when
        OWLObject built = builder.buildObject();
        // then
        assertEquals(expected, built);
    }

    @Test
    public void shouldBuildIrreflexiveObjectProperty() {
        // given
        BuilderIrreflexiveObjectProperty builder = new BuilderIrreflexiveObjectProperty()
                .withProperty(op).withAnnotations(annotations);
        OWLObject expected = df.getOWLIrreflexiveObjectPropertyAxiom(op, annotations);
        // when
        OWLObject built = builder.buildObject();
        // then
        assertEquals(expected, built);
    }

    @Test
    public void shouldBuildLiteral() {
        // given
        BuilderLiteral builder = new BuilderLiteral().withValue(true).withAnnotations(
                annotations);
        OWLObject expected = df.getOWLLiteral(true);
        // when
        OWLObject built = builder.buildObject();
        // then
        assertEquals(expected, built);
    }

    @Test
    public void shouldBuildNamedIndividual() {
        // given
        BuilderNamedIndividual builder = new BuilderNamedIndividual().withIRI(iri);
        OWLObject expected = df.getOWLNamedIndividual(iri);
        // when
        OWLObject built = builder.buildObject();
        // then
        assertEquals(expected, built);
    }

    @Test
    public void shouldBuildNegativeDataPropertyAssertion() {
        // given
        BuilderNegativeDataPropertyAssertion builder = new BuilderNegativeDataPropertyAssertion()
                .withAnnotations(annotations).withProperty(dp).withValue(lit)
                .withSubject(i);
        OWLObject expected = df.getOWLNegativeDataPropertyAssertionAxiom(dp, i, lit,
                annotations);
        // when
        OWLObject built = builder.buildObject();
        // then
        assertEquals(expected, built);
    }

    @Test
    public void shouldBuildNegativeObjectPropertyAssertion() {
        // given
        BuilderNegativeObjectPropertyAssertion builder = new BuilderNegativeObjectPropertyAssertion()
                .withAnnotations(annotations).withProperty(op).withValue(i)
                .withSubject(i);
        OWLObject expected = df.getOWLNegativeObjectPropertyAssertionAxiom(op, i, i,
                annotations);
        // when
        OWLObject built = builder.buildObject();
        // then
        assertEquals(expected, built);
    }

    @Test
    public void shouldBuildObjectAllValuesFrom() {
        // given
        BuilderObjectAllValuesFrom builder = new BuilderObjectAllValuesFrom()
                .withProperty(op).withRange(ce);
        OWLObject expected = df.getOWLObjectAllValuesFrom(op, ce);
        // when
        OWLObject built = builder.buildObject();
        // then
        assertEquals(expected, built);
    }

    @Test
    public void shouldBuildObjectExactCardinality() {
        // given
        BuilderObjectExactCardinality builder = new BuilderObjectExactCardinality()
                .withCardinality(1).withProperty(op).withRange(ce);
        OWLObject expected = df.getOWLObjectExactCardinality(1, op, ce);
        // when
        OWLObject built = builder.buildObject();
        // then
        assertEquals(expected, built);
    }

    @Test
    public void shouldBuildObjectHasSelf() {
        // given
        BuilderObjectHasSelf builder = new BuilderObjectHasSelf().withProperty(op);
        OWLObject expected = df.getOWLObjectHasSelf(op);
        // when
        OWLObject built = builder.buildObject();
        // then
        assertEquals(expected, built);
    }

    @Test
    public void shouldBuildObjectHasValue() {
        // given
        BuilderObjectHasValue builder = new BuilderObjectHasValue().withProperty(op)
                .withValue(i);
        OWLObject expected = df.getOWLObjectHasValue(op, i);
        // when
        OWLObject built = builder.buildObject();
        // then
        assertEquals(expected, built);
    }

    @Test
    public void shouldBuildObjectIntersectionOf() {
        // given
        BuilderObjectIntersectionOf builder = new BuilderObjectIntersectionOf()
                .withItems(classes);
        OWLObject expected = df.getOWLObjectIntersectionOf(classes);
        // when
        OWLObject built = builder.buildObject();
        // then
        assertEquals(expected, built);
    }

    @Test
    public void shouldBuildObjectInverseOf() {
        // given
        BuilderObjectInverseOf builder = new BuilderObjectInverseOf().withProperty(op);
        OWLObject expected = df.getOWLObjectInverseOf(op);
        // when
        OWLObject built = builder.buildObject();
        // then
        assertEquals(expected, built);
    }

    @Test
    public void shouldBuildObjectMaxCardinality() {
        // given
        BuilderObjectMaxCardinality builder = new BuilderObjectMaxCardinality()
                .withCardinality(1).withProperty(op).withRange(ce);
        OWLObject expected = df.getOWLObjectMaxCardinality(1, op, ce);
        // when
        OWLObject built = builder.buildObject();
        // then
        assertEquals(expected, built);
    }

    @Test
    public void shouldBuildObjectMinCardinality() {
        // given
        BuilderObjectMinCardinality builder = new BuilderObjectMinCardinality()
                .withCardinality(1).withProperty(op).withRange(ce);
        OWLObject expected = df.getOWLObjectMinCardinality(1, op, ce);
        // when
        OWLObject built = builder.buildObject();
        // then
        assertEquals(expected, built);
    }

    @Test
    public void shouldBuildObjectProperty() {
        // given
        BuilderObjectProperty builder = new BuilderObjectProperty().withIRI(iri);
        OWLObject expected = df.getOWLObjectProperty(iri);
        // when
        OWLObject built = builder.buildObject();
        // then
        assertEquals(expected, built);
    }

    @Test
    public void shouldBuildObjectPropertyAssertion() {
        // given
        BuilderObjectPropertyAssertion builder = new BuilderObjectPropertyAssertion()
                .withProperty(op).withSubject(i).withValue(i)
                .withAnnotations(annotations);
        OWLObject expected = df.getOWLObjectPropertyAssertionAxiom(op, i, i, annotations);
        // when
        OWLObject built = builder.buildObject();
        // then
        assertEquals(expected, built);
    }

    @Test
    public void shouldBuildObjectPropertyDomain() {
        // given
        BuilderObjectPropertyDomain builder = new BuilderObjectPropertyDomain()
                .withAnnotations(annotations);
        OWLObject expected = df.getOWLObjectPropertyDomainAxiom(op, ce, annotations);
        builder.withDomain(ce).withProperty(op).withAnnotations(annotations);
        // when
        OWLObject built = builder.buildObject();
        // then
        assertEquals(expected, built);
    }

    @Test
    public void shouldBuildObjectPropertyRange() {
        // given
        BuilderObjectPropertyRange builder = new BuilderObjectPropertyRange()
                .withProperty(op).withRange(ce).withAnnotations(annotations);
        OWLObject expected = df.getOWLObjectPropertyRangeAxiom(op, ce, annotations);
        // when
        OWLObject built = builder.buildObject();
        // then
        assertEquals(expected, built);
    }

    @Test
    public void shouldBuildObjectSomeValuesFrom() {
        // given
        BuilderObjectSomeValuesFrom builder = new BuilderObjectSomeValuesFrom()
                .withProperty(op).withRange(ce);
        OWLObject expected = df.getOWLObjectSomeValuesFrom(op, ce);
        // when
        OWLObject built = builder.buildObject();
        // then
        assertEquals(expected, built);
    }

    @Test
    public void shouldBuildOneOf() {
        // given
        BuilderOneOf builder = new BuilderOneOf().withItem(i);
        OWLObject expected = df.getOWLObjectOneOf(i);
        // when
        OWLObject built = builder.buildObject();
        // then
        assertEquals(expected, built);
    }

    @Test
    public void shouldBuildPropertyChain() {
        // given
        ArrayList<OWLObjectPropertyExpression> chain = new ArrayList<OWLObjectPropertyExpression>(
                ops);
        BuilderPropertyChain builder = new BuilderPropertyChain().withProperty(op)
                .withAnnotations(annotations);
        for (OWLObjectPropertyExpression p : chain) {
            builder.withPropertyInChain(p);
        }
        OWLObject expected = df.getOWLSubPropertyChainOfAxiom(chain, op, annotations);
        // when
        OWLObject built = builder.buildObject();
        // then
        assertEquals(expected, built);
    }

    @Test
    public void shouldBuildReflexiveObjectProperty() {
        // given
        BuilderReflexiveObjectProperty builder = new BuilderReflexiveObjectProperty()
                .withProperty(op).withAnnotations(annotations);
        OWLObject expected = df.getOWLReflexiveObjectPropertyAxiom(op, annotations);
        // when
        OWLObject built = builder.buildObject();
        // then
        assertEquals(expected, built);
    }

    @Test
    public void shouldBuildSameIndividual() {
        // given
        BuilderSameIndividual builder = new BuilderSameIndividual().withItems(inds)
                .withAnnotations(annotations);
        OWLObject expected = df.getOWLSameIndividualAxiom(inds, annotations);
        // when
        OWLObject built = builder.buildObject();
        // then
        assertEquals(expected, built);
    }

    @Test
    public void shouldBuildSubAnnotationPropertyOf() {
        // given
        BuilderSubAnnotationPropertyOf builder = new BuilderSubAnnotationPropertyOf()
                .withSub(ap).withSup(df.getRDFSLabel()).withAnnotations(annotations);
        OWLObject expected = df.getOWLSubAnnotationPropertyOfAxiom(ap, df.getRDFSLabel(),
                annotations);
        // when
        OWLObject built = builder.buildObject();
        // then
        assertEquals(expected, built);
    }

    @Test
    public void shouldBuildSubClass() {
        // given
        BuilderSubClass builder = new BuilderSubClass().withAnnotations(annotations)
                .withSub(ce).withSup(df.getOWLThing());
        OWLObject expected = df.getOWLSubClassOfAxiom(ce, df.getOWLThing(), annotations);
        // when
        OWLObject built = builder.buildObject();
        // then
        assertEquals(expected, built);
    }

    @Test
    public void shouldBuildSubDataProperty() {
        // given
        BuilderSubDataProperty builder = new BuilderSubDataProperty().withSub(dp)
                .withSup(df.getOWLTopDataProperty());
        OWLObject expected = df.getOWLSubDataPropertyOfAxiom(dp,
                df.getOWLTopDataProperty());
        // when
        OWLObject built = builder.buildObject();
        // then
        assertEquals(expected, built);
    }

    @Test
    public void shouldBuildSubObjectProperty() {
        // given
        BuilderSubObjectProperty builder = new BuilderSubObjectProperty().withSub(op)
                .withSup(df.getOWLTopObjectProperty()).withAnnotations(annotations);
        OWLObject expected = df.getOWLSubObjectPropertyOfAxiom(op,
                df.getOWLTopObjectProperty(), annotations);
        // when
        OWLObject built = builder.buildObject();
        // then
        assertEquals(expected, built);
    }

    @Test
    public void shouldBuildSWRLBuiltInAtom() {
        // given
        BuilderSWRLBuiltInAtom builder = new BuilderSWRLBuiltInAtom().with(iri)
                .with(var1);
        OWLObject expected = df.getSWRLBuiltInAtom(iri, Arrays.asList(var1));
        // when
        OWLObject built = builder.buildObject();
        // then
        assertEquals(expected, built);
    }

    @Test
    public void shouldBuildSWRLClassAtom() {
        // given
        BuilderSWRLClassAtom builder = new BuilderSWRLClassAtom().with(ce).with(var2);
        OWLObject expected = df.getSWRLClassAtom(ce, var2);
        // when
        OWLObject built = builder.buildObject();
        // then
        assertEquals(expected, built);
    }

    @Test
    public void shouldBuildSWRLDataPropertyAtom() {
        // given
        BuilderSWRLDataPropertyAtom builder = new BuilderSWRLDataPropertyAtom()
                .withProperty(dp).with(var2).with(var1);
        OWLObject expected = df.getSWRLDataPropertyAtom(dp, var2, var1);
        // when
        OWLObject built = builder.buildObject();
        // then
        assertEquals(expected, built);
    }

    @Test
    public void shouldBuildSWRLDataRangeAtom() {
        // given
        BuilderSWRLDataRangeAtom builder = new BuilderSWRLDataRangeAtom().with(d).with(
                var1);
        OWLObject expected = df.getSWRLDataRangeAtom(d, var1);
        // when
        OWLObject built = builder.buildObject();
        // then
        assertEquals(expected, built);
    }

    @Test
    public void shouldBuildSWRLDifferentIndividualsAtom() {
        // given
        BuilderSWRLDifferentIndividualsAtom builder = new BuilderSWRLDifferentIndividualsAtom()
                .withArg0(var2).withArg1(var2).withAnnotations(annotations);
        OWLObject expected = df.getSWRLDifferentIndividualsAtom(var2, var2);
        // when
        OWLObject built = builder.buildObject();
        // then
        assertEquals(expected, built);
    }

    @Test
    public void shouldBuildSWRLIndividualArgument() {
        // given
        BuilderSWRLIndividualArgument builder = new BuilderSWRLIndividualArgument()
                .with(i);
        OWLObject expected = df.getSWRLIndividualArgument(i);
        // when
        OWLObject built = builder.buildObject();
        // then
        assertEquals(expected, built);
    }

    @Test
    public void shouldBuildSWRLLiteralArgument() {
        // given
        BuilderSWRLLiteralArgument builder = new BuilderSWRLLiteralArgument().with(lit);
        OWLObject expected = df.getSWRLLiteralArgument(lit);
        // when
        OWLObject built = builder.buildObject();
        // then
        assertEquals(expected, built);
    }

    @Test
    public void shouldBuildSWRLObjectPropertyAtom() {
        // given
        BuilderSWRLObjectPropertyAtom builder = new BuilderSWRLObjectPropertyAtom()
                .withProperty(op).withArg0(var2).withArg1(var2);
        OWLObject expected = df.getSWRLObjectPropertyAtom(op, var2, var2);
        // when
        OWLObject built = builder.buildObject();
        // then
        assertEquals(expected, built);
    }

    @Test
    public void shouldBuildSWRLRule() {
        // given
        BuilderSWRLRule builder = new BuilderSWRLRule().withBody(v1).withHead(v2);
        OWLObject expected = df.getSWRLRule(body, head);
        // when
        OWLObject built = builder.buildObject();
        // then
        assertEquals(expected, built);
    }

    @Test
    public void shouldBuildSWRLSameIndividualAtom() {
        // given
        BuilderSWRLSameIndividualAtom builder = new BuilderSWRLSameIndividualAtom()
                .withArg0(df.getSWRLIndividualArgument(i)).withArg1(
                        df.getSWRLIndividualArgument(i));
        OWLObject expected = df.getSWRLSameIndividualAtom(
                df.getSWRLIndividualArgument(i), df.getSWRLIndividualArgument(i));
        // when
        OWLObject built = builder.buildObject();
        // then
        assertEquals(expected, built);
    }

    @Test
    public void shouldBuildSWRLVariable() {
        // given
        BuilderSWRLVariable builder = new BuilderSWRLVariable().with(iri);
        OWLObject expected = df.getSWRLVariable(iri);
        // when
        OWLObject built = builder.buildObject();
        // then
        assertEquals(expected, built);
    }

    @Test
    public void shouldBuildSymmetricObjectProperty() {
        // given
        BuilderSymmetricObjectProperty builder = new BuilderSymmetricObjectProperty()
                .withProperty(op).withAnnotations(annotations);
        OWLObject expected = df.getOWLSymmetricObjectPropertyAxiom(op, annotations);
        // when
        OWLObject built = builder.buildObject();
        // then
        assertEquals(expected, built);
    }

    @Test
    public void shouldBuildTransitiveObjectProperty() {
        // given
        BuilderTransitiveObjectProperty builder = new BuilderTransitiveObjectProperty()
                .withProperty(op).withAnnotations(annotations);
        OWLObject expected = df.getOWLTransitiveObjectPropertyAxiom(op, annotations);
        // when
        OWLObject built = builder.buildObject();
        // then
        assertEquals(expected, built);
    }

    @Test
    public void shouldBuildUnionOf() {
        // given
        BuilderUnionOf builder = new BuilderUnionOf().withItems(classes);
        OWLObject expected = df.getOWLObjectUnionOf(classes);
        // when
        OWLObject built = builder.buildObject();
        // then
        assertEquals(expected, built);
    }
}
