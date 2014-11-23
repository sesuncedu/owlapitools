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

import org.coode.suggestor.api.FillerSuggestor;
import org.coode.suggestor.impl.SuggestorFactory;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.vocab.OWLFacet;

@SuppressWarnings("javadoc")
public class FillerSuggestorTests extends AbstractSuggestorTest {

    @Nonnull
    private OWLClass ca = createClass("ca");
    @Nonnull
    private OWLClass ca1 = createClass("ca1");
    @Nonnull
    private OWLClass cb = createClass("cb");
    @Nonnull
    private OWLClass cb1 = createClass("cb1");
    @Nonnull
    private OWLClass cc = createClass("cc");
    @Nonnull
    private OWLClass cc1 = createClass("cc1");
    @Nonnull
    private OWLClass cd = createClass("cd");
    @Nonnull
    private OWLClass ce = createClass("ce");
    @Nonnull
    private OWLObjectProperty oa = createObjectProperty("oa");
    @Nonnull
    private OWLObjectProperty ob = createObjectProperty("ob");
    @Nonnull
    private OWLObjectProperty ob1 = createObjectProperty("ob1");
    @Nonnull
    private OWLDataProperty da = createDataProperty("da");

    /*
     * ca -> oa some cb // "redundant" ca -> oa some cb1 ca -> ob1 some cb ca ->
     * da some integer cd == oa some cb cd -> oa some ce cb1 -> cb cc1 -> cc ca1
     * -> ca ob1 -> ob
     */
    @Nonnull
    private OWLOntology createModelA() throws Exception {
        OWLOntology ont = createOntology();
        List<OWLOntologyChange> changes = new ArrayList<>();
        changes.add(new AddAxiom(ont, df.getOWLSubClassOfAxiom(cb1, cb)));
        changes.add(new AddAxiom(ont, df.getOWLSubClassOfAxiom(ca1, ca)));
        changes.add(new AddAxiom(ont, df
                .getOWLSubObjectPropertyOfAxiom(ob1, ob)));
        changes.add(new AddAxiom(ont, df.getOWLSubClassOfAxiom(ca,
                df.getOWLObjectSomeValuesFrom(oa, cb))));
        changes.add(new AddAxiom(ont, df.getOWLSubClassOfAxiom(ca,
                df.getOWLDataSomeValuesFrom(da, df.getIntegerOWLDatatype()))));
        changes.add(new AddAxiom(ont, df.getOWLEquivalentClassesAxiom(cd,
                df.getOWLObjectSomeValuesFrom(oa, cb))));
        changes.add(new AddAxiom(ont, df.getOWLSubClassOfAxiom(cd,
                df.getOWLObjectSomeValuesFrom(oa, ce))));
        changes.add(new AddAxiom(ont, df.getOWLSubClassOfAxiom(ca,
                df.getOWLObjectSomeValuesFrom(oa, cb1))));
        changes.add(new AddAxiom(ont, df.getOWLSubClassOfAxiom(ca,
                df.getOWLObjectSomeValuesFrom(ob1, cb))));
        changes.add(new AddAxiom(ont, df.getOWLSubClassOfAxiom(cc1, cc)));
        mngr.applyChanges(changes);
        return ont;
    }

    public void testIsCurrentFiller() throws Exception {
        OWLOntology ont = createModelA();
        OWLReasoner r = ((OWLReasonerFactory) Class.forName(
                DEFAULT_REASONER_FACTORY).newInstance())
                .createNonBufferingReasoner(ont);
        SuggestorFactory fac = new SuggestorFactory(r);
        // PropertySuggestor ps = fac.getPropertySuggestor();
        FillerSuggestor fs = fac.getFillerSuggestor();
        OWLDataFactory f = fs.getReasoner().getRootOntology()
                .getOWLOntologyManager().getOWLDataFactory();
        // System.out.println("FillerSuggestorTests.testIsCurrentFiller()\n"
        // + fs.getReasoner().getRootOntology().getAxioms().toString()
        // .replace(",", ",\n\t"));
        // System.out
        // .println("FillerSuggestorTests.testIsCurrentFiller() subclasses of thing \n"
        // + fs.getReasoner().getSubClasses(f.getOWLThing(), true));
        // System.out
        // .println("FillerSuggestorTests.testIsCurrentFiller() subclasses of thing \n"
        // + fs.getReasoner()
        // .getSubClasses(f.getOWLThing(), false));
        // TODO add a generic reasoner to implement these tests
        // OWLReasoner test=new
        // JFactFactory().createReasoner(fs.getReasoner().getRootOntology());
        // System.out.println("FillerSuggestorTests.testIsCurrentFiller() subclasses of thing test \n"+test.getSubClasses(f.getOWLThing(),
        // true));
        // System.out.println("FillerSuggestorTests.testIsCurrentFiller() subclasses of thing test\n"+test.getSubClasses(f.getOWLThing(),
        // false));
        assertTrue(fs.isCurrent(ca, oa, cb1, true));
        assertFalse(fs.isCurrent(ca, oa, cb, true));
        assertFalse(fs.isCurrent(
                ca,
                oa,
                df.getOWLObjectIntersectionOf(cb,
                        df.getOWLObjectSomeValuesFrom(ob, cc)), true));
        assertFalse(fs.isCurrent(ca, oa, cc, true));
        assertTrue(fs.isCurrent(ca, oa, ce, true)); // from interaction with d
        assertTrue(fs.isCurrent(ca, da, df.getIntegerOWLDatatype(), true));
        assertFalse(fs.isCurrent(ca, da, df.getOWLDatatypeRestriction(
                df.getIntegerOWLDatatype(), OWLFacet.MIN_INCLUSIVE,
                df.getOWLLiteral(2)), true));
        assertFalse(fs.isCurrent(ca, oa, df.getOWLThing(), true));
        assertTrue(fs.isCurrent(ca, oa, cb1));
        assertTrue(fs.isCurrent(ca, oa, cb));
        assertFalse(fs.isCurrent(
                ca,
                oa,
                df.getOWLObjectIntersectionOf(cb,
                        df.getOWLObjectSomeValuesFrom(ob, cc))));
        assertFalse(fs.isCurrent(ca, oa, cc));
        assertTrue(fs.isCurrent(ca, oa, df.getOWLThing()));
        assertTrue(fs.isCurrent(ca, da, df.getIntegerOWLDatatype()));
        assertTrue(fs.isCurrent(ca, da, df.getTopDatatype()));
        // inherited
        assertTrue(fs.isCurrent(ca1, oa, cb1, true));
    }

    /*
     * ca -> not(oa some cb) cb1 -> cb cc1 -> cc
     */
    private OWLOntology createModelB() throws Exception {
        OWLOntology ont = createOntology();
        List<OWLOntologyChange> changes = new ArrayList<>();
        changes.add(new AddAxiom(ont, df.getOWLSubClassOfAxiom(ca,
                df.getOWLObjectComplementOf(df.getOWLObjectSomeValuesFrom(oa,
                        cb)))));
        changes.add(new AddAxiom(ont, df.getOWLSubClassOfAxiom(cb1, cb)));
        changes.add(new AddAxiom(ont, df.getOWLSubClassOfAxiom(cc1, cc)));
        mngr.applyChanges(changes);
        return ont;
    }

    public void testIsPossibleFiller() throws Exception {
        OWLOntology ont = createModelB();
        OWLReasoner r = ((OWLReasonerFactory) Class.forName(
                DEFAULT_REASONER_FACTORY).newInstance())
                .createNonBufferingReasoner(ont);
        SuggestorFactory fac = new SuggestorFactory(r);
        // PropertySuggestor ps = fac.getPropertySuggestor();
        FillerSuggestor fs = fac.getFillerSuggestor();
        assertTrue(fs.isPossible(ca, oa, ca));
        assertFalse(fs.isPossible(ca, oa, cb));
        assertTrue(fs.isPossible(ca, oa, cc));
        assertTrue(fs.isPossible(ca, oa, cc1));
        assertTrue(fs.isPossible(ca, oa, df.getOWLThing()));
    }

    /*
     * ca -> oa some cb ca -> ob some cc ca -> ob some cd ob -> oa cd -> cc
     */
    public void testGetCurrentFillers() throws Exception {
        OWLOntology ont = createOntology();
        OWLReasoner r = ((OWLReasonerFactory) Class.forName(
                DEFAULT_REASONER_FACTORY).newInstance())
                .createNonBufferingReasoner(ont);
        SuggestorFactory fac = new SuggestorFactory(r);
        // PropertySuggestor ps = fac.getPropertySuggestor();
        FillerSuggestor fs = fac.getFillerSuggestor();
        List<OWLOntologyChange> changes = new ArrayList<>();
        changes.add(new AddAxiom(ont, df.getOWLSubClassOfAxiom(ca,
                df.getOWLObjectSomeValuesFrom(oa, cb))));
        changes.add(new AddAxiom(ont, df.getOWLSubClassOfAxiom(ca,
                df.getOWLObjectSomeValuesFrom(ob, cc))));
        changes.add(new AddAxiom(ont, df.getOWLSubClassOfAxiom(ca,
                df.getOWLObjectSomeValuesFrom(ob, cd))));
        changes.add(new AddAxiom(ont, df.getOWLSubObjectPropertyOfAxiom(ob, oa)));
        changes.add(new AddAxiom(ont, df.getOWLSubClassOfAxiom(cd, cc)));
        mngr.applyChanges(changes);
        NodeSet<OWLClass> all = fs.getCurrentNamedFillers(ca, oa, false);
        assertTrue(all.containsEntity(cb));
        assertTrue(all.containsEntity(cc));
        assertTrue(all.containsEntity(cd));
        assertTrue(all.containsEntity(df.getOWLThing()));
        assertEquals(4, all.nodes().count());
        NodeSet<OWLClass> direct = fs.getCurrentNamedFillers(ca, oa, true);
        assertTrue(direct.containsEntity(cb));
        // as cd is more specific
        assertFalse(direct.containsEntity(cc));
        assertTrue(direct.containsEntity(cd));
        // as more specific properties have been found
        assertFalse(direct.containsEntity(df.getOWLThing()));
        assertEquals(2, direct.nodes().count());
    }

    public void testGetPossibleFillers() throws Exception {
        OWLOntology ont = createModelA();
        OWLReasoner r = ((OWLReasonerFactory) Class.forName(
                DEFAULT_REASONER_FACTORY).newInstance())
                .createNonBufferingReasoner(ont);
        SuggestorFactory fac = new SuggestorFactory(r);
        // PropertySuggestor ps = fac.getPropertySuggestor();
        FillerSuggestor fs = fac.getFillerSuggestor();
        NodeSet<OWLClass> pSuccessorsA = fs.getPossibleNamedFillers(ca, oa,
                null, false);
        assertEquals(8, pSuccessorsA.nodes().count());
        assertTrue(pSuccessorsA.containsEntity(ca));
        assertTrue(pSuccessorsA.containsEntity(ca1));
        assertTrue(pSuccessorsA.containsEntity(cb));
        assertTrue(pSuccessorsA.containsEntity(cb1));
        assertTrue(pSuccessorsA.containsEntity(cc));
        assertTrue(pSuccessorsA.containsEntity(cc1));
        assertTrue(pSuccessorsA.containsEntity(cd));
        assertTrue(pSuccessorsA.containsEntity(ce));
        NodeSet<OWLClass> pSuccessorsADirect = fs.getPossibleNamedFillers(ca,
                oa, null, true);
        assertEquals(4, pSuccessorsADirect.nodes().count());
        // not ca as it is a sub of cd
        assertTrue(pSuccessorsADirect.containsEntity(cb));
        assertTrue(pSuccessorsADirect.containsEntity(cc));
        assertTrue(pSuccessorsADirect.containsEntity(cd));
        assertTrue(pSuccessorsADirect.containsEntity(ce));
    }
}
