/**
 * Date: Dec 17, 2007
 *
 * code made available under Mozilla Public License (http://www.mozilla.org/MPL/MPL-1.1.html)
 *
 * copyright 2007, The University of Manchester
 *
 * @author Nick Drummond, The University Of Manchester, Bio Health Informatics Group
 */
package org.coode.suggestor.util;

import static org.semanticweb.owlapi.util.OWLAPIStreamUtils.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;

import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLDataRange;
import org.semanticweb.owlapi.model.OWLDataSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.impl.OWLDatatypeNode;
import org.semanticweb.owlapi.reasoner.impl.OWLDatatypeNodeSet;
import org.semanticweb.owlapi.search.Searcher;

/** Utility methods for common reasoner tasks. */
public class ReasonerHelper {

    private final OWLReasoner r;
    private final OWLDataFactory df;

    /**
     * @param r
     *        reasoner to use
     */
    public ReasonerHelper(OWLReasoner r) {
        if (r == null) {
            throw new IllegalArgumentException("Reasoner cannot be null");
        }
        this.r = r;
        df = r.getRootOntology().getOWLOntologyManager().getOWLDataFactory();
    }

    /** @return referenced properties */
    public Set<OWLObjectProperty> getReferencedObjectProperties() {
        final OWLOntology root = r.getRootOntology();
        Set<OWLObjectProperty> p = asSet(root
                .objectPropertiesInSignature(Imports.INCLUDED));
        p.add(df.getOWLTopObjectProperty());
        p.add(df.getOWLBottomObjectProperty());
        return p;
    }

    /** @return referenced properties */
    public Set<OWLDataProperty> getReferencedDataProperties() {
        final OWLOntology root = r.getRootOntology();
        Set<OWLDataProperty> p = asSet(root
                .dataPropertiesInSignature(Imports.INCLUDED));
        p.add(df.getOWLTopDataProperty());
        p.add(df.getOWLBottomDataProperty());
        return p;
    }

    /**
     * @param cls1
     *        class 1
     * @param cls2
     *        class 2
     * @return true if cls1 is a subclass of cls2
     */
    public boolean isDescendantOf(@Nonnull OWLClassExpression cls1,
            @Nonnull OWLClassExpression cls2) {
        // return isAncestorOf(cls2, cls1);
        if (!cls1.isAnonymous()) {
            return r.getSubClasses(cls2, false).containsEntity(
                    cls1.asOWLClass());
        }
        return r.isEntailed(df.getOWLSubClassOfAxiom(cls1, cls2));
    }

    /**
     * @param cls1
     *        class 1
     * @param cls2
     *        class 2
     * @return true if cls2 is a subclass of cls1
     */
    public boolean isAncestorOf(@Nonnull OWLClassExpression cls1,
            @Nonnull OWLClassExpression cls2) {
        if (!cls1.isAnonymous()) {
            return r.getSuperClasses(cls2, false).containsEntity(
                    cls1.asOWLClass());
        }
        return r.isEntailed(df.getOWLSubClassOfAxiom(cls2, cls1));
    }

    /**
     * @param clses
     *        classes
     * @return clses without classes with superclasses in the set
     */
    public Set<OWLClassExpression> filterClassExpressions(
            Set<OWLClassExpression> clses) {
        Set<OWLClassExpression> nonRedundantSet = new HashSet<>();
        List<OWLClassExpression> clsList = new ArrayList<>(clses);
        for (int i = 0; i < clsList.size(); i++) {
            final OWLClassExpression head = clsList.get(i);
            assert head != null;
            if (!containsSubclass(clsList.subList(i + 1, clsList.size()), head)
                    && !containsSubclass(nonRedundantSet, head)) {
                nonRedundantSet.add(head);
            }
        }
        return nonRedundantSet;
    }

    /**
     * @param potentialSubs
     *        potential subs
     * @param cls
     *        class
     * @return true if one of potentialSubs is a subclass of cls
     */
    public boolean containsSubclass(
            Collection<OWLClassExpression> potentialSubs,
            @Nonnull OWLClassExpression cls) {
        for (OWLClassExpression potentialSub : potentialSubs) {
            assert potentialSub != null;
            if (isDescendantOf(potentialSub, cls)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param c
     *        class
     * @param p
     *        property
     * @return true if c is a subclass of max 1 p
     */
    public boolean isLocallyFunctional(OWLClassExpression c,
            OWLObjectPropertyExpression p) {
        return isDescendantOf(c, df.getOWLObjectMaxCardinality(1, p));
    }

    /**
     * @param c
     *        class
     * @param p
     *        property
     * @return true if c is a subclass of max 1 p
     */
    public boolean isLocallyFunctional(OWLClassExpression c,
            @Nonnull OWLDataProperty p) {
        return isDescendantOf(c, df.getOWLDataMaxCardinality(1, p));
    }

    /**
     * <p>
     * Check the ontologies for range assertions on p and all ancestors of p.
     * </p>
     * 
     * @param p
     *        the object property for which a range is wanted
     * @return an intersection of the non-redundant ranges or Thing if no range
     *         assertions have been made
     */
    @Nonnull
    public OWLClassExpression getGlobalAssertedRange(
            @Nonnull OWLObjectPropertyExpression p) {
        Set<OWLClassExpression> assertedRanges = new HashSet<>();
        Set<OWLObjectPropertyExpression> ancestors = asSet(r
                .getSuperObjectProperties(p, false).entities());
        ancestors.add(p);
        r.getRootOntology()
                .importsClosure()
                .forEach(
                        o -> ancestors.forEach(a -> add(assertedRanges,
                                Searcher.range(o.objectPropertyRangeAxioms(a)))));
        if (assertedRanges.isEmpty()) {
            return df.getOWLThing();
        }
        // filter to remove redundant ranges (supers of others) as
        // getRanges() just returns all of them
        Set<OWLClassExpression> cleanedRanges = filterClassExpressions(assertedRanges);
        if (cleanedRanges.size() == 1) {
            return cleanedRanges.iterator().next();
        }
        return df.getOWLObjectIntersectionOf(cleanedRanges);
    }

    /**
     * <p>
     * Find the asserted ranges on this property and all of its ancestors.
     * </p>
     * <p>
     * If multiple ranges are found, they are pulled together into an
     * intersection.
     * </p>
     * 
     * @param p
     *        the property we are querying
     * @return the range of this property or Top if none is found
     */
    public OWLDataRange getGlobalAssertedRange(OWLDataProperty p) {
        Set<OWLDataRange> assertedRanges = new HashSet<>();
        Set<OWLDataProperty> ancestors = asSet(r.getSuperDataProperties(p,
                false).entities());
        ancestors.add(p);
        r.getRootOntology()
                .importsClosure()
                .forEach(
                        o -> ancestors.forEach(a -> add(assertedRanges,
                                Searcher.range(o.dataPropertyRangeAxioms(a)))));
        if (assertedRanges.isEmpty()) {
            return df.getTopDatatype();
        }
        if (assertedRanges.size() == 1) {
            return assertedRanges.iterator().next();
        }
        return df.getOWLDataIntersectionOf(assertedRanges);
    }

    /**
     * Subsumption checking between dataranges/types. This will only work if
     * there is a suitable data property in the ontology. This must satisfy the
     * criteria in
     * {@link #getCandidatePropForRangeSubsumptionCheck(OWLDataRange)}.
     * 
     * @param subRange
     *        subRange
     * @param superRange
     *        superRange
     * @return true if subRange is subsumed by superRange
     * @throws RuntimeException
     *         if no suitable property can be found
     */
    public boolean isSubtype(OWLDataRange subRange, OWLDataRange superRange) {
        OWLDataPropertyExpression p = getCandidatePropForRangeSubsumptionCheck(superRange);
        if (p == null) {
            throw new RuntimeException(
                    "Cannot find a candidate property for datatype subsumption checking");
        }
        return r.isEntailed(df.getOWLSubClassOfAxiom(
                df.getOWLDataSomeValuesFrom(p, subRange),
                df.getOWLDataSomeValuesFrom(p, superRange)));
    }

    /**
     * Subsumption between dataranges/types. This will only work if there is a
     * suitable data property in the ontology. This must satisfy the criteria in
     * {@link #getCandidatePropForRangeSubsumptionCheck(OWLDataRange)}.
     * 
     * @param range
     *        The data range for which we will retrieve subtypes
     * @return a NodeSet containing named datatypes that are known to be
     *         subtypes of range
     * @throws RuntimeException
     *         if no suitable property can be found
     */
    @Nonnull
    public NodeSet<OWLDatatype> getSubtypes(OWLDataRange range) {
        OWLDataPropertyExpression p = getCandidatePropForRangeSubsumptionCheck(range);
        if (p == null) {
            throw new RuntimeException(
                    "Cannot find a candidate property for datatype subsumption checking");
        }
        Set<Node<OWLDatatype>> subs = new HashSet<>();
        OWLDataSomeValuesFrom pSomeRange = df
                .getOWLDataSomeValuesFrom(p, range);
        for (OWLDatatype dt : getDatatypesInSignature()) {
            if (!dt.equals(range)) {
                final OWLDataSomeValuesFrom pSomeDatatype = df
                        .getOWLDataSomeValuesFrom(p, dt);
                if (!r.isSatisfiable(pSomeDatatype)) {
                    // TODO can we protect against this?
                    System.err
                            .println("Warning: unsatisfiable concept in subtype checking: "
                                    + pSomeDatatype);
                } else if (r.isEntailed(df.getOWLSubClassOfAxiom(pSomeDatatype,
                        pSomeRange))) {
                    subs.add(new OWLDatatypeNode(dt));
                }
            }
        }
        return new OWLDatatypeNodeSet(subs);
    }

    /**
     * Equivalence between dataranges/types. This will only work if there is a
     * suitable data property in the ontology. This must satisfy the criteria in
     * {@link #getCandidatePropForRangeSubsumptionCheck(OWLDataRange)}.
     * 
     * @param range
     *        The data range for which we will retrieve equivalents
     * @return a NodeSet containing named datatypes that are known to be
     *         equivalent to range
     * @throws RuntimeException
     *         if no suitable property can be found
     */
    @Nonnull
    public Node<OWLDatatype> getEquivalentTypes(OWLDataRange range) {
        OWLDataPropertyExpression p = getCandidatePropForRangeSubsumptionCheck(range);
        if (p == null) {
            throw new RuntimeException(
                    "Cannot find a candidate property for datatype subsumption checking");
        }
        Set<OWLDatatype> subs = new HashSet<>();
        if (range.isOWLDatatype()) {
            subs.add(range.asOWLDatatype());
        }
        OWLDataSomeValuesFrom pSomeRange = df
                .getOWLDataSomeValuesFrom(p, range);
        for (OWLDatatype dt : getDatatypesInSignature()) {
            if (!dt.equals(range)) {
                final OWLDataSomeValuesFrom pSomeDatatype = df
                        .getOWLDataSomeValuesFrom(p, dt);
                if (!r.isSatisfiable(pSomeDatatype)) {
                    // TODO can we protect against this?
                    System.err
                            .println("Warning: unsatisfiable concept in equiv type checking: "
                                    + pSomeDatatype);
                } else if (r.isEntailed(df.getOWLEquivalentClassesAxiom(
                        pSomeDatatype, pSomeRange))) {
                    subs.add(dt);
                }
            }
        }
        return new OWLDatatypeNode(subs);
    }

    /**
     * <p>
     * Find a candidate property for datatype subsumption checking.
     * </p>
     * <p>
     * If {O} is the set of ontologies loaded into the reasoner, a candidate is
     * ANY data property (APART FROM Top) in the signature of {O} that satifies
     * the criteria isSatisfiable(SomeValuesFrom(p, range).
     * </p>
     * 
     * @param range
     *        the data range from which will be used in the above test
     * @return a candidate property that fulfils the above criteria or null if
     *         none can be found
     */
    public OWLDataPropertyExpression getCandidatePropForRangeSubsumptionCheck(
            OWLDataRange range) {
        return r.getRootOntology()
                .importsClosure()
                .flatMap(o -> o.dataPropertiesInSignature())
                .filter(p -> !p.isTopEntity()
                        && r.isSatisfiable(df
                                .getOWLDataSomeValuesFrom(p, range)))
                .findFirst().orElse(null);
    }

    /** @return all datatypes in the ontology import closure */
    public Set<OWLDatatype> getDatatypesInSignature() {
        return asSet(r.getRootOntology().importsClosure()
                .flatMap(o -> o.datatypesInSignature()));
    }

    /**
     * @param p
     *        property
     * @param f
     *        range
     * @return true if range is asserted
     */
    public boolean isInAssertedRange(OWLObjectPropertyExpression p,
            OWLClassExpression f) {
        return isDescendantOf(f, getGlobalAssertedRange(p));
    }

    /**
     * @param p
     *        property
     * @param f
     *        range
     * @return true if range is asserted
     */
    public boolean isInAssertedRange(OWLDataProperty p, OWLDataRange f) {
        return isSubtype(f, getGlobalAssertedRange(p));
    }
}
