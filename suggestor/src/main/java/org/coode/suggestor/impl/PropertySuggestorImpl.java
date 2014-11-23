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

import static org.semanticweb.owlapi.util.OWLAPIStreamUtils.*;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nonnull;

import org.coode.suggestor.api.PropertySanctionRule;
import org.coode.suggestor.api.PropertySuggestor;
import org.coode.suggestor.util.ReasonerHelper;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLPropertyExpression;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.impl.OWLDataPropertyNodeSet;
import org.semanticweb.owlapi.reasoner.impl.OWLObjectPropertyNodeSet;

/** Default implementation of the PropertySuggestor. */
class PropertySuggestorImpl implements PropertySuggestor {

    protected final OWLReasoner r;
    protected final OWLDataFactory df;
    protected final ReasonerHelper helper;
    private final Set<PropertySanctionRule> sanctionRules = new HashSet<>();
    private final Matcher<OWLObjectPropertyExpression> currentOPMatcher = new AbstractOPMatcher() {

        @Override
        public boolean isMatch(OWLClassExpression c,
                OWLObjectPropertyExpression p) {
            if (p.isTopEntity()) {
                return true;
            }
            return helper.isDescendantOf(c,
                    df.getOWLObjectSomeValuesFrom(p, df.getOWLThing()));
        }
    };
    private final Matcher<OWLDataProperty> currentDPMatcher = new AbstractDPMatcher() {

        @Override
        public boolean isMatch(OWLClassExpression c, OWLDataProperty p) {
            if (p.isTopEntity()) {
                return true;
            }
            return helper.isDescendantOf(c,
                    df.getOWLDataSomeValuesFrom(p, df.getTopDatatype()));
        }
    };
    private Matcher<OWLObjectPropertyExpression> possibleOPMatcher = new AbstractOPMatcher() {

        @Override
        public boolean isMatch(OWLClassExpression c,
                OWLObjectPropertyExpression p) {
            // Hermit and JFact are very slightly faster using the entailment
            // check
            return !r.isEntailed(df.getOWLSubClassOfAxiom(c,
                    df.getOWLObjectAllValuesFrom(p, df.getOWLNothing())));
        }
    };
    private Matcher<OWLDataProperty> possibleDPMatcher = new AbstractDPMatcher() {

        @Override
        public boolean isMatch(OWLClassExpression c, OWLDataProperty p) {
            return r.isSatisfiable(df.getOWLObjectIntersectionOf(c,
                    df.getOWLDataSomeValuesFrom(p, df.getTopDatatype())));
        }
    };

    public PropertySuggestorImpl(OWLReasoner r) {
        this.r = r;
        df = r.getRootOntology().getOWLOntologyManager().getOWLDataFactory();
        helper = new ReasonerHelper(r);
    }

    @Override
    public void addSanctionRule(PropertySanctionRule rule) {
        sanctionRules.add(rule);
        rule.setSuggestor(this);
    }

    @Override
    public void removeSanctionRule(PropertySanctionRule rule) {
        sanctionRules.remove(rule);
        rule.setSuggestor(null);
    }

    @Override
    public OWLReasoner getReasoner() {
        return r;
    }

    // BOOLEAN TESTS
    @Override
    public boolean
            isCurrent(OWLClassExpression c, OWLObjectPropertyExpression p) {
        return currentOPMatcher.isMatch(c, p);
    }

    @Override
    public boolean isCurrent(OWLClassExpression c,
            OWLObjectPropertyExpression p, boolean direct) {
        return currentOPMatcher.isMatch(c, p, direct);
    }

    @Override
    public boolean isCurrent(OWLClassExpression c, OWLDataProperty p) {
        return currentDPMatcher.isMatch(c, p);
    }

    @Override
    public boolean isCurrent(OWLClassExpression c, OWLDataProperty p,
            boolean direct) {
        return currentDPMatcher.isMatch(c, p, direct);
    }

    @Override
    public boolean isPossible(OWLClassExpression c,
            OWLObjectPropertyExpression p) {
        return possibleOPMatcher.isMatch(c, p);
    }

    @Override
    public boolean isPossible(OWLClassExpression c, OWLDataProperty p) {
        return possibleDPMatcher.isMatch(c, p);
    }

    @Override
    public boolean isSanctioned(OWLClassExpression c,
            OWLObjectPropertyExpression p) {
        return isPossible(c, p) && meetsOPSanctions(c, p);
    }

    @Override
    public boolean isSanctioned(OWLClassExpression c, OWLDataProperty p) {
        return isPossible(c, p) && meetsDPSanctions(c, p);
    }

    // GETTERS
    @Override
    public NodeSet<OWLObjectPropertyExpression> getCurrentObjectProperties(
            OWLClassExpression c, boolean direct) {
        return currentOPMatcher.getLeaves(c, r.getTopObjectPropertyNode(),
                direct);
    }

    @Override
    public NodeSet<OWLDataProperty> getCurrentDataProperties(
            OWLClassExpression c, boolean direct) {
        return currentDPMatcher
                .getLeaves(c, r.getTopDataPropertyNode(), direct);
    }

    @Override
    public NodeSet<OWLObjectPropertyExpression> getPossibleObjectProperties(
            OWLClassExpression c, OWLObjectPropertyExpression root,
            boolean direct) {
        Node<OWLObjectPropertyExpression> rootNode = root == null ? r
                .getTopObjectPropertyNode() : r
                .getEquivalentObjectProperties(root);
        return possibleOPMatcher.getRoots(c, rootNode, direct);
    }

    @Override
    public NodeSet<OWLDataProperty> getPossibleDataProperties(
            OWLClassExpression c, OWLDataProperty root, boolean direct) {
        Node<OWLDataProperty> rootNode = root == null ? r
                .getTopDataPropertyNode() : r.getEquivalentDataProperties(root);
        return possibleDPMatcher.getRoots(c, rootNode, direct);
    }

    @Override
    public Set<OWLObjectPropertyExpression> getSanctionedObjectProperties(
            OWLClassExpression c, OWLObjectPropertyExpression root,
            boolean direct) {
        return asSet(getPossibleObjectProperties(c, root, direct).entities()
                .filter(p -> meetsOPSanctions(c, p)));
    }

    @Override
    public Set<OWLDataProperty> getSanctionedDataProperties(
            OWLClassExpression c, OWLDataProperty root, boolean direct) {
        return asSet(getPossibleDataProperties(c, root, direct).entities()
                .filter(p -> meetsDPSanctions(c, p)));
    }

    // INTERNALS
    private boolean meetsOPSanctions(OWLClassExpression c,
            OWLObjectPropertyExpression p) {
        for (PropertySanctionRule rule : sanctionRules) {
            if (rule.meetsSanction(c, p)) {
                return true;
            }
        }
        return false;
    }

    private boolean meetsDPSanctions(OWLClassExpression c, OWLDataProperty p) {
        for (PropertySanctionRule rule : sanctionRules) {
            if (rule.meetsSanction(c, p)) {
                return true;
            }
        }
        return false;
    }

    // DELEGATES
    private interface Matcher<P extends OWLPropertyExpression> {

        boolean isMatch(OWLClassExpression c, P p);

        boolean isMatch(@Nonnull OWLClassExpression c, @Nonnull P p,
                boolean direct);

        /*
         * Perform a recursive search, adding nodes that match and if direct is
         * true only if they have no subs that match
         */@Nonnull
        NodeSet<P> getLeaves(@Nonnull OWLClassExpression c,
                @Nonnull Node<P> root, boolean direct);

        /*
         * Perform a search on the direct subs of start, adding nodes that
         * match. If not direct then recurse
         */@Nonnull
        NodeSet<P> getRoots(@Nonnull OWLClassExpression c,
                @Nonnull Node<P> start, boolean direct);
    }

    private abstract class AbstractMatcher<P extends OWLPropertyExpression>
            implements Matcher<P> {

        public AbstractMatcher() {}

        @Override
        public final boolean isMatch(OWLClassExpression c, P p, boolean direct) {
            if (!direct) {
                return isMatch(c, p);
            }
            if (!isMatch(c, p)) {
                return false;
            }
            for (Node<P> node : getDirectSubs(p)) {
                // check the direct subproperties
                if (isMatch(c, node.getRepresentativeElement())) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public final NodeSet<P> getLeaves(OWLClassExpression c, Node<P> root,
                boolean direct) {
            Set<Node<P>> nodes = new HashSet<>();
            final P p = root.getRepresentativeElement();
            if (isMatch(c, p)) {
                for (Node<P> sub : getDirectSubs(p)) {
                    add(nodes, getLeaves(c, sub, direct).nodes());
                }
                if (!direct || nodes.isEmpty() && !root.isTopNode()) {
                    nodes.add(root);
                }
            }
            return createNodeSet(nodes);
        }

        @Override
        public final NodeSet<P> getRoots(OWLClassExpression c, Node<P> root,
                boolean direct) {
            Set<Node<P>> nodes = new HashSet<>();
            for (Node<P> sub : getDirectSubs(root.getRepresentativeElement())) {
                if (isMatch(c, sub.getRepresentativeElement())) {
                    nodes.add(sub);
                    if (!direct) {
                        add(nodes, getRoots(c, sub, direct).nodes());
                    }
                }
            }
            return createNodeSet(nodes);
        }

        @Nonnull
        protected abstract NodeSet<P> getDirectSubs(@Nonnull P p);

        @Nonnull
        protected abstract NodeSet<P>
                createNodeSet(@Nonnull Set<Node<P>> nodes);
    }

    private abstract class AbstractOPMatcher extends
            AbstractMatcher<OWLObjectPropertyExpression> {

        public AbstractOPMatcher() {}

        @Override
        protected final NodeSet<OWLObjectPropertyExpression> getDirectSubs(
                OWLObjectPropertyExpression p) {
            return r.getSubObjectProperties(p, true);
        }

        @Override
        protected final NodeSet<OWLObjectPropertyExpression> createNodeSet(
                @Nonnull Set<Node<OWLObjectPropertyExpression>> nodes) {
            return new OWLObjectPropertyNodeSet(nodes);
        }
    }

    private abstract class AbstractDPMatcher extends
            AbstractMatcher<OWLDataProperty> {

        public AbstractDPMatcher() {}

        @Override
        protected final NodeSet<OWLDataProperty>
                getDirectSubs(OWLDataProperty p) {
            return r.getSubDataProperties(p, true);
        }

        @Override
        protected NodeSet<OWLDataProperty> createNodeSet(
                Set<Node<OWLDataProperty>> nodes) {
            return new OWLDataPropertyNodeSet(nodes);
        }
    }
}
