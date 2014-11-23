/**
 * Date: Dec 17, 2007
 *
 * code made available under Mozilla Public License (http://www.mozilla.org/MPL/MPL-1.1.html)
 *
 * copyright 2007, The University of Manchester
 *
 * @author Nick Drummond, The University Of Manchester, Bio Health Informatics Group
 */
package org.coode.suggestor.impl;

import org.coode.suggestor.api.PropertySanctionRule;
import org.coode.suggestor.api.PropertySuggestor;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLProperty;
import org.semanticweb.owlapi.model.OWLPropertyExpression;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.search.Searcher;

/**
 * Checks the class to see if it has an annotation (specified by the
 * constructor) matching the URI of the property. If recursive is true, then all
 * ancestors of the class are also checked.
 */
public class SimpleAnnotationPropertySanctionRule implements
        PropertySanctionRule {

    private OWLReasoner r;
    private final OWLAnnotationProperty annotationProperty;
    private final boolean recursive;

    /**
     * @param annotationProperty
     *        property to use
     * @param recursive
     *        true if recursive
     */
    public SimpleAnnotationPropertySanctionRule(
            OWLAnnotationProperty annotationProperty, boolean recursive) {
        this.annotationProperty = annotationProperty;
        this.recursive = recursive;
    }

    @Override
    public void setSuggestor(PropertySuggestor ps) {
        r = ps.getReasoner();
    }

    @Override
    public boolean meetsSanction(OWLClassExpression c,
            OWLObjectPropertyExpression p) {
        return hasAnnotation(c, p);
    }

    @Override
    public boolean meetsSanction(OWLClassExpression c, OWLDataProperty p) {
        return hasAnnotation(c, p);
    }

    private boolean
            hasAnnotation(OWLClassExpression c, OWLPropertyExpression p) {
        if (p.isAnonymous()) {
            return false;
        }
        if (!c.isAnonymous()
                && hasSanctionAnnotation(c.asOWLClass(), (OWLProperty) p)) {
            return true;
        }
        if (recursive) {
            // check the ancestors
            return r.getSuperClasses(c, true).entities()
                    .anyMatch(cls -> hasAnnotation(cls, p));
        }
        return false;
    }

    private boolean hasSanctionAnnotation(OWLClass c, OWLProperty p) {
        IRIMatcher iriMatcher = new IRIMatcher(p.getIRI());
        return r.getRootOntology()
                .importsClosure()
                .flatMap(
                        o -> Searcher.annotations(o.annotationAssertionAxioms(c
                                .getIRI())))
                .anyMatch(
                        a -> a.getProperty().equals(annotationProperty)
                                && a.getValue().accept(iriMatcher)
                                        .booleanValue());
    }
}
