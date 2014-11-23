/**
 * Date: Dec 17, 2007
 *
 * code made available under Mozilla Public License (http://www.mozilla.org/MPL/MPL-1.1.html)
 *
 * copyright 2007, The University of Manchester
 *
 * @author Nick Drummond, The University Of Manchester, Bio Health Informatics Group
 */
package org.coode.suggestor.test;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import org.coode.suggestor.api.PropertySuggestor;
import org.coode.suggestor.impl.SimpleAnnotationPropertySanctionRule;
import org.coode.suggestor.impl.SuggestorFactory;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

@SuppressWarnings("javadoc")
public class PropertySuggestorTests extends AbstractSuggestorTest {

    @Nonnull
    OWLClass ca = createClass("a");
    @Nonnull
    OWLClass cb = createClass("b");
    @Nonnull
    OWLClass cc = createClass("c");
    @Nonnull
    OWLClass cd = createClass("d");
    @Nonnull
    OWLClass ce = createClass("e");
    @Nonnull
    OWLClass cf = createClass("f");
    @Nonnull
    OWLObjectProperty op = createObjectProperty("p");
    @Nonnull
    OWLObjectProperty oq = createObjectProperty("q");
    @Nonnull
    OWLObjectProperty or = createObjectProperty("r");
    @Nonnull
    OWLObjectProperty os = createObjectProperty("s");
    @Nonnull
    OWLObjectProperty ot = createObjectProperty("t");
    @Nonnull
    OWLDataProperty dx = createDataProperty("x");
    @Nonnull
    OWLDataProperty dy = createDataProperty("y");
    @Nonnull
    OWLDataProperty z = createDataProperty("z");

    /*
     * a -> p some b a -> q some b a -> not(s some Thing) a -> x some integer c
     * -> a c -> t some b q -> r
     */@Nonnull
    private OWLOntology createModelA() throws Exception {
        OWLOntology ont = createOntology();
        List<OWLOntologyChange> changes = new ArrayList<>();
        changes.add(new AddAxiom(ont, df.getOWLSubClassOfAxiom(ca,
                df.getOWLObjectSomeValuesFrom(op, cb))));
        changes.add(new AddAxiom(ont, df.getOWLSubClassOfAxiom(ca,
                df.getOWLObjectSomeValuesFrom(oq, cb))));
        changes.add(new AddAxiom(ont, df.getOWLSubClassOfAxiom(ca,
                df.getOWLDataSomeValuesFrom(dx, df.getIntegerOWLDatatype()))));
        changes.add(new AddAxiom(ont, df.getOWLSubClassOfAxiom(cc, ca)));
        changes.add(new AddAxiom(ont, df.getOWLSubClassOfAxiom(cc,
                df.getOWLObjectSomeValuesFrom(ot, cb))));
        changes.add(new AddAxiom(ont, df.getOWLSubObjectPropertyOfAxiom(oq, or)));
        changes.add(new AddAxiom(ont, df.getOWLSubClassOfAxiom(ca, df
                .getOWLObjectComplementOf(df.getOWLObjectSomeValuesFrom(os,
                        df.getOWLThing())))));
        mngr.applyChanges(changes);
        return ont;
    }

    /*
     * a -> p some b a -> q some b a -> not(s some Thing) r -> s
     */@Nonnull
    private OWLOntology createModelB() throws Exception {
        OWLOntology ont = createOntology();
        List<OWLOntologyChange> changes = new ArrayList<>();
        changes.add(new AddAxiom(ont, df.getOWLSubClassOfAxiom(ca,
                df.getOWLObjectSomeValuesFrom(op, cb))));
        changes.add(new AddAxiom(ont, df.getOWLSubClassOfAxiom(ca,
                df.getOWLObjectSomeValuesFrom(oq, cb))));
        changes.add(new AddAxiom(ont, df.getOWLSubClassOfAxiom(ca, df
                .getOWLObjectComplementOf(df.getOWLObjectSomeValuesFrom(os,
                        df.getOWLThing())))));
        changes.add(new AddAxiom(ont, df.getOWLSubObjectPropertyOfAxiom(or, os)));
        mngr.applyChanges(changes);
        return ont;
    }

    public void testIsCurrentObjectProperties() throws Exception {
        OWLOntology ont = createModelA();
        OWLReasoner r = ((OWLReasonerFactory) Class.forName(
                DEFAULT_REASONER_FACTORY).newInstance())
                .createNonBufferingReasoner(ont);
        SuggestorFactory fac = new SuggestorFactory(r);
        PropertySuggestor ps = fac.getPropertySuggestor();
        // FillerSuggestor fs = fac.getFillerSuggestor();
        final OWLObjectProperty topOP = df.getOWLTopObjectProperty();
        final OWLDataProperty topDP = df.getOWLTopDataProperty();
        assertTrue(ps.isCurrent(ca, op));
        assertTrue(ps.isCurrent(ca, oq));
        assertTrue(ps.isCurrent(ca, or));
        assertFalse(ps.isCurrent(ca, os));
        assertFalse(ps.isCurrent(ca, ot));
        assertTrue(ps.isCurrent(ca, topOP));
        assertTrue(ps.isCurrent(ca, dx));
        assertTrue(ps.isCurrent(ca, topDP));
        assertTrue(ps.isCurrent(ca, op, true));
        assertTrue(ps.isCurrent(ca, oq, true));
        assertFalse(ps.isCurrent(ca, or, true));
        assertFalse(ps.isCurrent(ca, os, true));
        assertFalse(ps.isCurrent(ca, ot, true));
        assertFalse(ps.isCurrent(ca, topOP, true));
        assertTrue(ps.isCurrent(ca, dx, true));
        assertFalse(ps.isCurrent(ca, topDP, true));
        // same should hold for c, but with additional t property
        assertTrue(ps.isCurrent(cc, op));
        assertTrue(ps.isCurrent(cc, oq));
        assertTrue(ps.isCurrent(cc, or));
        assertFalse(ps.isCurrent(cc, os));
        assertTrue(ps.isCurrent(cc, ot));
        assertTrue(ps.isCurrent(cc, topOP));
        assertTrue(ps.isCurrent(cc, dx));
        assertTrue(ps.isCurrent(cc, topDP));
        assertTrue(ps.isCurrent(cc, op, true));
        assertTrue(ps.isCurrent(cc, oq, true));
        assertFalse(ps.isCurrent(cc, or, true));
        assertFalse(ps.isCurrent(cc, os, true));
        assertTrue(ps.isCurrent(cc, ot, true));
        assertFalse(ps.isCurrent(cc, topOP, true));
        assertTrue(ps.isCurrent(cc, dx, true));
        assertFalse(ps.isCurrent(cc, topDP, true));
    }

    public void testIsPossibleObjectProperties() throws Exception {
        OWLOntology ont = createModelA();
        OWLReasoner r = ((OWLReasonerFactory) Class.forName(
                DEFAULT_REASONER_FACTORY).newInstance())
                .createNonBufferingReasoner(ont);
        SuggestorFactory fac = new SuggestorFactory(r);
        PropertySuggestor ps = fac.getPropertySuggestor();
        // FillerSuggestor fs = fac.getFillerSuggestor();
        long start = System.currentTimeMillis();
        for (int i = 0; i < 100; i++) {
            assertTrue(ps.isPossible(ca, op));
            assertTrue(ps.isPossible(ca, oq));
            assertTrue(ps.isPossible(ca, or));
            assertFalse(ps.isPossible(ca, os));
            assertTrue(ps.isPossible(ca, ot));
            assertTrue(ps.isPossible(ca, df.getOWLTopObjectProperty()));
            assertTrue(ps.isPossible(cc, op));
            assertTrue(ps.isPossible(cc, oq));
            assertTrue(ps.isPossible(cc, or));
            assertFalse(ps.isPossible(cc, os));
            assertTrue(ps.isPossible(cc, ot));
            assertTrue(ps.isPossible(cc, df.getOWLTopObjectProperty()));
        }
        long end = System.currentTimeMillis();
        System.out.println(end - start + "ms");
    }

    public void testIsImpossibleObjectProperties() throws Exception {
        OWLOntology ont = createModelB();
        OWLReasoner r = ((OWLReasonerFactory) Class.forName(
                DEFAULT_REASONER_FACTORY).newInstance())
                .createNonBufferingReasoner(ont);
        SuggestorFactory fac = new SuggestorFactory(r);
        PropertySuggestor ps = fac.getPropertySuggestor();
        // FillerSuggestor fs = fac.getFillerSuggestor();
        assertTrue(ps.isPossible(ca, op));
        assertTrue(ps.isPossible(ca, oq));
        assertFalse(ps.isPossible(ca, or));
        assertFalse(ps.isPossible(ca, os));
        assertTrue(ps.isPossible(ca, df.getOWLTopObjectProperty()));
    }

    public void testGetCurrentObjectProperties() throws Exception {
        OWLOntology ont = createModelA();
        OWLReasoner r = ((OWLReasonerFactory) Class.forName(
                DEFAULT_REASONER_FACTORY).newInstance())
                .createNonBufferingReasoner(ont);
        SuggestorFactory fac = new SuggestorFactory(r);
        PropertySuggestor ps = fac.getPropertySuggestor();
        // FillerSuggestor fs = fac.getFillerSuggestor();
        NodeSet<OWLObjectPropertyExpression> direct = ps
                .getCurrentObjectProperties(ca, true);
        assertTrue(direct.containsEntity(op));
        assertTrue(direct.containsEntity(oq));
        assertEquals(2, direct.nodes().count());
        NodeSet<OWLObjectPropertyExpression> all = ps
                .getCurrentObjectProperties(ca, false);
        assertTrue(all.containsEntity(op));
        assertTrue(all.containsEntity(oq));
        assertTrue(all.containsEntity(or));
        assertTrue(all.containsEntity(df.getOWLTopObjectProperty()));
        assertEquals(4, all.nodes().count());
    }

    public void testGetCurrentDataProperties() throws Exception {
        OWLOntology ont = createModelA();
        OWLReasoner r = ((OWLReasonerFactory) Class.forName(
                DEFAULT_REASONER_FACTORY).newInstance())
                .createNonBufferingReasoner(ont);
        SuggestorFactory fac = new SuggestorFactory(r);
        PropertySuggestor ps = fac.getPropertySuggestor();
        // FillerSuggestor fs = fac.getFillerSuggestor();
        NodeSet<OWLDataProperty> direct = ps.getCurrentDataProperties(ca, true);
        assertTrue(direct.containsEntity(dx));
        assertEquals(1, direct.nodes().count());
        NodeSet<OWLDataProperty> all = ps.getCurrentDataProperties(ca, false);
        assertTrue(all.containsEntity(dx));
        assertTrue(all.containsEntity(df.getOWLTopDataProperty()));
        assertEquals(2, all.nodes().count());
    }

    /*
     * a -> p some b p == q
     */
    public void testCurrentObjectPropertiesSynonyms() throws Exception {
        OWLOntology ont = createOntology();
        OWLReasoner r = ((OWLReasonerFactory) Class.forName(
                DEFAULT_REASONER_FACTORY).newInstance())
                .createNonBufferingReasoner(ont);
        SuggestorFactory fac = new SuggestorFactory(r);
        PropertySuggestor ps = fac.getPropertySuggestor();
        // FillerSuggestor fs = fac.getFillerSuggestor();
        List<OWLOntologyChange> changes = new ArrayList<>();
        changes.add(new AddAxiom(ont, df.getOWLSubClassOfAxiom(ca,
                df.getOWLObjectSomeValuesFrom(op, cb))));
        changes.add(new AddAxiom(ont, df.getOWLEquivalentObjectPropertiesAxiom(
                op, oq)));
        mngr.applyChanges(changes);
        assertTrue(ps.isCurrent(ca, op, true));
        assertTrue(ps.isCurrent(ca, oq, true));
        NodeSet<OWLObjectPropertyExpression> direct = ps
                .getCurrentObjectProperties(ca, true);
        assertTrue(direct.containsEntity(op));
        assertTrue(direct.containsEntity(oq));
        assertEquals(1, direct.nodes().count());
        NodeSet<OWLObjectPropertyExpression> all = ps
                .getCurrentObjectProperties(ca, false);
        assertTrue(all.containsEntity(op));
        assertTrue(all.containsEntity(oq));
        assertTrue(all.containsEntity(df.getOWLTopObjectProperty()));
        assertEquals(2, all.nodes().count());
    }

    public void testGetPossibleObjectProperties() throws Exception {
        OWLOntology ont = createModelA();
        OWLReasoner r = ((OWLReasonerFactory) Class.forName(
                DEFAULT_REASONER_FACTORY).newInstance())
                .createNonBufferingReasoner(ont);
        SuggestorFactory fac = new SuggestorFactory(r);
        PropertySuggestor ps = fac.getPropertySuggestor();
        // FillerSuggestor fs = fac.getFillerSuggestor();
        NodeSet<OWLObjectPropertyExpression> specific = ps
                .getPossibleObjectProperties(ca, null, true);
        assertEquals(7, specific.nodes().count());
        assertTrue(specific.containsEntity(op));
        assertTrue(specific.containsEntity(df.getOWLObjectInverseOf(op)));
        assertTrue(specific.containsEntity(or));
        assertTrue(specific.containsEntity(df.getOWLObjectInverseOf(or)));
        assertTrue(specific.containsEntity(ot));
        assertTrue(specific.containsEntity(df.getOWLObjectInverseOf(ot)));
        assertTrue(specific.containsEntity(df.getOWLObjectInverseOf(os)));
        NodeSet<OWLObjectPropertyExpression> all = ps
                .getPossibleObjectProperties(ca, null, false);
        assertEquals(9, all.nodes().count());
        assertTrue(all.containsEntity(op));
        assertTrue(all.containsEntity(df.getOWLObjectInverseOf(op)));
        assertTrue(all.containsEntity(oq));
        assertTrue(all.containsEntity(df.getOWLObjectInverseOf(oq)));
        assertTrue(all.containsEntity(or));
        assertTrue(all.containsEntity(df.getOWLObjectInverseOf(or)));
        assertTrue(all.containsEntity(df.getOWLObjectInverseOf(os)));
        assertTrue(all.containsEntity(ot));
        assertTrue(all.containsEntity(df.getOWLObjectInverseOf(ot)));
    }

    public void testGetImpossibleObjectProperties() throws Exception {
        OWLOntology ont = createModelB();
        OWLReasoner r = ((OWLReasonerFactory) Class.forName(
                DEFAULT_REASONER_FACTORY).newInstance())
                .createNonBufferingReasoner(ont);
        SuggestorFactory fac = new SuggestorFactory(r);
        PropertySuggestor ps = fac.getPropertySuggestor();
        // FillerSuggestor fs = fac.getFillerSuggestor();
        NodeSet<OWLObjectPropertyExpression> properties = ps
                .getPossibleObjectProperties(ca, null, false);
        assertFalse(properties.containsEntity(os));
        assertFalse(properties.containsEntity(or)); // any ancestor of s will
                                                    // also be impossible
    }

    /**
     * Check that the getters return the same results as running the is..
     * methods over the signature
     */
    public void testGettersAgainstBooleanMethods() throws Exception {
        OWLOntology ont = createModelA();
        OWLReasoner r = ((OWLReasonerFactory) Class.forName(
                DEFAULT_REASONER_FACTORY).newInstance())
                .createNonBufferingReasoner(ont);
        SuggestorFactory fac = new SuggestorFactory(r);
        PropertySuggestor ps = fac.getPropertySuggestor();
        // FillerSuggestor fs = fac.getFillerSuggestor();
        // current direct
        NodeSet<OWLObjectPropertyExpression> direct = ps
                .getCurrentObjectProperties(ca, true);
        ont.objectPropertiesInSignature().forEach(
                p -> assertEquals(ps.isCurrent(ca, p, true),
                        direct.containsEntity(p)));
        // current indirect
        NodeSet<OWLObjectPropertyExpression> indirect = ps
                .getCurrentObjectProperties(ca, false);
        ont.objectPropertiesInSignature().forEach(
                p -> assertEquals(ps.isCurrent(ca, p, false),
                        indirect.containsEntity(p)));
        // possible direct
        // check for all properties in the ontology
        ont.objectPropertiesInSignature()
                .forEach(
                        p -> {
                            NodeSet<OWLObjectPropertyExpression> posdirect = ps
                                    .getPossibleObjectProperties(ca, p, true);
                            for (Node<OWLObjectPropertyExpression> q : r
                                    .getSubObjectProperties(p, true)) {
                                assertEquals("unexpected " + q
                                        + " in direct possible properties of "
                                        + p, ps.isPossible(ca,
                                        q.getRepresentativeElement()),
                                        posdirect.containsEntity(q
                                                .getRepresentativeElement()));
                            }
                        });
        // possible indirect
        NodeSet<OWLObjectPropertyExpression> posindirect = ps
                .getPossibleObjectProperties(ca, null, false);
        ont.objectPropertiesInSignature().forEach(
                p -> assertEquals(ps.isPossible(ca, p),
                        posindirect.containsEntity(p)));
    }

    public void testAnnotationPropertySanctioning() throws Exception {
        OWLOntology ont = createOntology();
        OWLReasoner r = ((OWLReasonerFactory) Class.forName(
                DEFAULT_REASONER_FACTORY).newInstance())
                .createNonBufferingReasoner(ont);
        SuggestorFactory fac = new SuggestorFactory(r);
        PropertySuggestor ps = fac.getPropertySuggestor();
        // FillerSuggestor fs = fac.getFillerSuggestor();
        OWLAnnotationProperty annot = createAnnotationProperty("ap");
        OWLClass a = createClass("a");
        OWLObjectProperty p = createObjectProperty("p");
        OWLObjectProperty q = createObjectProperty("q");
        OWLObjectProperty rr = createObjectProperty("r");
        OWLDataProperty j = createDataProperty("j");
        OWLDataProperty k = createDataProperty("k");
        List<OWLOntologyChange> changes = new ArrayList<>();
        // force a to be in the signature
        changes.add(new AddAxiom(ont, df.getOWLSubClassOfAxiom(a,
                df.getOWLThing())));
        // add an annotation on a that sanctions p
        changes.add(new AddAxiom(ont, df.getOWLAnnotationAssertionAxiom(
                a.getIRI(), df.getOWLAnnotation(annot, p.getIRI()))));
        // and one that uses a plain literal to say q is sanctioned
        changes.add(new AddAxiom(ont, df.getOWLAnnotationAssertionAxiom(
                a.getIRI(),
                df.getOWLAnnotation(annot,
                        df.getOWLLiteral(q.getIRI().toString())))));
        // add an annotation on a that sanctions j
        changes.add(new AddAxiom(ont, df.getOWLAnnotationAssertionAxiom(
                a.getIRI(), df.getOWLAnnotation(annot, j.getIRI()))));
        // force properties to be in the signature by stating something about
        // them
        changes.add(new AddAxiom(ont, df.getOWLFunctionalObjectPropertyAxiom(p)));
        changes.add(new AddAxiom(ont, df.getOWLFunctionalObjectPropertyAxiom(q)));
        changes.add(new AddAxiom(ont, df
                .getOWLFunctionalObjectPropertyAxiom(rr)));
        changes.add(new AddAxiom(ont, df.getOWLFunctionalDataPropertyAxiom(j)));
        changes.add(new AddAxiom(ont, df.getOWLFunctionalDataPropertyAxiom(k)));
        mngr.applyChanges(changes);
        ps.addSanctionRule(new SimpleAnnotationPropertySanctionRule(annot, true));
        //
        // Set<OWLPropertyExpression> sanctionedProperties =
        // ps.getSanctionedProperties(a, false);
        //
        // assertEquals(3, sanctionedProperties.size());
        // assertTrue(sanctionedProperties.contains(p));
        // assertTrue(sanctionedProperties.contains(q));
        // assertTrue(sanctionedProperties.contains(j));
    }
}
