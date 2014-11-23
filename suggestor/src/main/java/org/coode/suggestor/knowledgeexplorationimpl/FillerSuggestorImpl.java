/**
 * Date: Dec 17, 2007
 *
 * code made available under Mozilla Public License (http://www.mozilla.org/MPL/MPL-1.1.html)
 *
 * copyright 2007, The University of Manchester
 *
 * @author Nick Drummond, The University Of Manchester, Bio Health Informatics Group
 */
package org.coode.suggestor.knowledgeexplorationimpl;

import static org.semanticweb.owlapi.util.OWLAPIStreamUtils.*;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import org.coode.suggestor.api.FillerSanctionRule;
import org.coode.suggestor.api.FillerSuggestor;
import org.coode.suggestor.util.ReasonerHelper;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLDataRange;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLPropertyExpression;
import org.semanticweb.owlapi.model.OWLPropertyRange;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.impl.OWLClassNode;
import org.semanticweb.owlapi.reasoner.impl.OWLClassNodeSet;
import org.semanticweb.owlapi.reasoner.impl.OWLDatatypeNode;
import org.semanticweb.owlapi.reasoner.impl.OWLDatatypeNodeSet;
import org.semanticweb.owlapi.reasoner.knowledgeexploration.OWLKnowledgeExplorerReasoner;
import org.semanticweb.owlapi.reasoner.knowledgeexploration.OWLKnowledgeExplorerReasoner.RootNode;

/** Default implementation of the FillerSuggestor. */
class FillerSuggestorImpl implements FillerSuggestor {

    @Nonnull
    protected final OWLKnowledgeExplorerReasoner r;
    protected final OWLDataFactory df;
    protected final ReasonerHelper helper;
    private final Set<FillerSanctionRule> sanctioningRules = new HashSet<>();
    private final AbstractOPMatcher currentOPMatcher = new AbstractOPMatcher() {

        @Override
        public boolean isMatch(OWLClassExpression c,
                OWLObjectPropertyExpression p, OWLClassExpression f) {
            return helper
                    .isDescendantOf(c, df.getOWLObjectSomeValuesFrom(p, f));
        }
    };
    private final AbstractDPMatcher currentDPMatcher = new AbstractDPMatcher() {

        @Override
        public boolean isMatch(OWLClassExpression c,
                OWLDataPropertyExpression p, OWLDataRange f) {
            return helper.isDescendantOf(c, df.getOWLDataSomeValuesFrom(p, f));
        }
    };
    private final AbstractOPMatcher possibleOPMatcher = new AbstractOPMatcher() {

        @Override
        public boolean isMatch(OWLClassExpression c,
                OWLObjectPropertyExpression p, OWLClassExpression f) {
            return !r.isSatisfiable(df.getOWLObjectIntersectionOf(
                    c,
                    df.getOWLObjectAllValuesFrom(p,
                            df.getOWLObjectComplementOf(f))));
        }
    };
    private final AbstractDPMatcher possibleDPMatcher = new AbstractDPMatcher() {

        @Override
        public boolean isMatch(OWLClassExpression c,
                OWLDataPropertyExpression p, OWLDataRange f) {
            return !r
                    .isSatisfiable(df.getOWLObjectIntersectionOf(
                            c,
                            df.getOWLDataAllValuesFrom(p,
                                    df.getOWLDataComplementOf(f))));
        }
    };

    public FillerSuggestorImpl(@Nonnull OWLKnowledgeExplorerReasoner r) {
        this.r = r;
        df = r.getRootOntology().getOWLOntologyManager().getOWLDataFactory();
        helper = new ReasonerHelper(r);
    }

    @Override
    public void addSanctionRule(FillerSanctionRule rule) {
        sanctioningRules.add(rule);
        rule.setSuggestor(this);
    }

    @Override
    public void removeSanctionRule(FillerSanctionRule rule) {
        sanctioningRules.remove(rule);
        rule.setSuggestor(null);
    }

    @Override
    public OWLKnowledgeExplorerReasoner getReasoner() {
        return r;
    }

    // BOOLEAN TESTS
    @Override
    public boolean isCurrent(OWLClassExpression c,
            OWLObjectPropertyExpression p, OWLClassExpression f) {
        return currentOPMatcher.isMatch(c, p, f);
    }

    @Override
    public boolean
            isCurrent(OWLClassExpression c, OWLObjectPropertyExpression p,
                    OWLClassExpression f, boolean direct) {
        return currentOPMatcher.isMatch(c, p, f, direct);
    }

    @Override
    public boolean isCurrent(OWLClassExpression c, OWLDataProperty p,
            OWLDataRange f) {
        return currentDPMatcher.isMatch(c, p, f);
    }

    @Override
    public boolean isCurrent(OWLClassExpression c, OWLDataProperty p,
            OWLDataRange f, boolean direct) {
        return currentDPMatcher.isMatch(c, p, f, direct);
    }

    @Override
    public boolean isPossible(OWLClassExpression c,
            OWLObjectPropertyExpression p, OWLClassExpression f) {
        return possibleOPMatcher.isMatch(c, p, f);
    }

    @Override
    public boolean isPossible(OWLClassExpression c, OWLDataProperty p,
            OWLDataRange f) {
        return possibleDPMatcher.isMatch(c, p, f);
    }

    @Override
    public boolean isSanctioned(OWLClassExpression c,
            OWLObjectPropertyExpression p, OWLClassExpression f) {
        return isPossible(c, p, f) && meetsSanctions(c, p, f);
    }

    @Override
    public boolean isSanctioned(OWLClassExpression c, OWLDataProperty p,
            OWLDataRange f) {
        return isPossible(c, p, f) && meetsSanctions(c, p, f);
    }

    @Override
    public boolean isRedundant(OWLClassExpression c,
            OWLObjectPropertyExpression p, OWLClassExpression f) {
        if (isCurrent(c, p, f)) {
            return true;
        }
        for (Node<OWLClass> node : r.getSubClasses(f, true)) {
            // check the direct subclasses
            final OWLClass sub = node.getRepresentativeElement();
            if (isCurrent(c, p, sub)
                    || helper.isDescendantOf(c,
                            df.getOWLObjectAllValuesFrom(p, sub))) {
                return true;
            }
        }
        return false;
    }

    // GETTERS
    @Override
    public NodeSet<OWLClass> getCurrentNamedFillers(OWLClassExpression c,
            OWLObjectPropertyExpression p, boolean direct) {
        return currentOPMatcher.getLeaves(c, p,
                helper.getGlobalAssertedRange(p), direct);
    }

    @Override
    public NodeSet<OWLClass> getPossibleNamedFillers(OWLClassExpression c,
            @Nonnull OWLObjectPropertyExpression p, OWLClassExpression root,
            boolean direct) {
        return possibleOPMatcher.getRoots(c, p,
                root == null ? helper.getGlobalAssertedRange(p) : root, direct);
    }

    @Override
    public Set<OWLClass> getSanctionedFillers(OWLClassExpression c,
            OWLObjectPropertyExpression p, OWLClassExpression root,
            boolean direct) {
        return asSet(getPossibleNamedFillers(c, p, root, direct).entities()
                .filter(f -> meetsSanctions(c, p, f)));
    }

    // INTERNALS
    private boolean meetsSanctions(OWLClassExpression c,
            OWLObjectPropertyExpression p, OWLClassExpression f) {
        for (FillerSanctionRule rule : sanctioningRules) {
            if (rule.meetsSanction(c, p, f)) {
                return true;
            }
        }
        return false;
    }

    private boolean meetsSanctions(OWLClassExpression c, OWLDataProperty p,
            OWLDataRange f) {
        for (FillerSanctionRule rule : sanctioningRules) {
            if (rule.meetsSanction(c, p, f)) {
                return true;
            }
        }
        return false;
    }

    // DELEGATES
    // F is an OWLEntity that extends R and will be the type returned by
    // getMatches().
    // eg for R = OWLClassExpression, F = OWLClass, P =
    // OWLObjectPropertyExpression
    // It would be nice if we could enforce this with multiple generics, but R &
    // OWLEntity is disallowed currently
    private interface Matcher<R extends OWLPropertyRange, F extends R, P extends OWLPropertyExpression> {

        boolean isMatch(@Nonnull OWLClassExpression c, @Nonnull P p,
                @Nonnull R f);

        boolean isMatch(@Nonnull OWLClassExpression c, @Nonnull P p,
                @Nonnull R f, boolean direct);

        /**
         * Perform a recursive search, adding nodes that match. If direct is
         * true only add nodes if they have no subs that match
         * 
         * @param c
         *        class
         * @param p
         *        property
         * @param start
         *        start
         * @param direct
         *        direct
         * @return set of matching nodes
         */
        @Nonnull
        NodeSet<F> getLeaves(@Nonnull OWLClassExpression c, @Nonnull P p,
                @Nonnull R start, boolean direct);

        /*
         * Perform a search on the direct subs of start, adding nodes that
         * match. If direct is false then recurse into descendants of start
         */@Nonnull
        NodeSet<F> getRoots(@Nonnull OWLClassExpression c, @Nonnull P p,
                @Nonnull R start, boolean direct);
    }

    private abstract class AbstractMatcher<R extends OWLPropertyRange, F extends R, P extends OWLPropertyExpression>
            implements Matcher<R, F, P> {

        public AbstractMatcher() {}

        @Override
        public final boolean isMatch(@Nonnull OWLClassExpression c,
                @Nonnull P p, @Nonnull R f, boolean direct) {
            if (!direct) {
                return isMatch(c, p, f);
            }
            if (!isMatch(c, p, f)) {
                return false;
            }
            NodeSet<F> directSubs = getDirectSubs(f);
            for (Node<F> node : directSubs) {
                F representativeElement = node.getRepresentativeElement();
                if (isMatch(c, p, representativeElement)) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public final NodeSet<F> getRoots(OWLClassExpression c, P p, R start,
                boolean direct) {
            Set<Node<F>> nodes = new HashSet<>();
            for (Node<F> sub : getDirectSubs(start)) {
                if (isMatch(c, p, sub.getRepresentativeElement())) {
                    nodes.add(sub);
                    if (!direct) {
                        add(nodes,
                                getRoots(c, p, sub.getRepresentativeElement(),
                                        direct).nodes());
                    }
                }
            }
            return createNodeSet(nodes);
        }

        @Nonnull
        protected abstract NodeSet<F> getDirectSubs(@Nonnull R f);

        @Nonnull
        protected abstract Node<F> getEquivalents(@Nonnull R f);

        @Nonnull
        protected abstract NodeSet<F>
                createNodeSet(@Nonnull Set<Node<F>> nodes);
    }

    private abstract class AbstractOPMatcher
            extends
            AbstractMatcher<OWLClassExpression, OWLClass, OWLObjectPropertyExpression> {

        public AbstractOPMatcher() {}

        @Override
        protected final NodeSet<OWLClass> getDirectSubs(
                @Nonnull OWLClassExpression c) {
            return r.getSubClasses(c, true);
        }

        @Override
        protected Node<OWLClass> getEquivalents(@Nonnull OWLClassExpression f) {
            return r.getEquivalentClasses(f);
        }

        @Override
        protected final NodeSet<OWLClass> createNodeSet(
                @Nonnull Set<Node<OWLClass>> nodes) {
            return new OWLClassNodeSet(nodes);
        }

        @Override
        public final NodeSet<OWLClass> getLeaves(OWLClassExpression c,
                OWLObjectPropertyExpression p, OWLClassExpression start,
                boolean direct) {
            RootNode root = r.getRoot(c);
            if (root.getNode() == null) {
                System.out
                        .println("FillerSuggestorImpl.AbstractMatcher.getLeaves() null root? "
                                + c + "\t" + p + "\t" + start + "\t" + direct);
            }
            Set<Node<OWLClass>> toReturn = new HashSet<>();
            r.getObjectNeighbours(root, true)
                    .entities()
                    .flatMap(
                            p1 -> r.getObjectNeighbours(root,
                                    p1.asOWLObjectProperty()).stream())
                    .forEach(pointer -> {
                        // TODO anonymous expressions
                            Stream<OWLClass> node = r
                                    .getObjectLabel(pointer, direct).entities()
                                    .filter(c1 -> c1 != null)
                                    .map(c1 -> c1.asOWLClass());
                            toReturn.add(new OWLClassNode(node));
                        });
            return createNodeSet(toReturn);
        }
    }

    private abstract class AbstractDPMatcher
            extends
            AbstractMatcher<OWLDataRange, OWLDatatype, OWLDataPropertyExpression> {

        public AbstractDPMatcher() {}

        @Override
        protected final NodeSet<OWLDatatype> getDirectSubs(OWLDataRange range) {
            return helper.getSubtypes(range);
        }

        @Override
        protected Node<OWLDatatype> getEquivalents(OWLDataRange range) {
            return helper.getEquivalentTypes(range);
        }

        @Override
        protected NodeSet<OWLDatatype> createNodeSet(
                Set<Node<OWLDatatype>> nodes) {
            return new OWLDatatypeNodeSet(nodes);
        }

        @Override
        public final NodeSet<OWLDatatype>
                getLeaves(OWLClassExpression c, OWLDataPropertyExpression p,
                        OWLDataRange start, boolean direct) {
            RootNode root = r.getRoot(c);
            if (root.getNode() == null) {
                System.out
                        .println("FillerSuggestorImpl.AbstractMatcher.getLeaves() null root? "
                                + c + "\t" + p + "\t" + start + "\t" + direct);
                return createNodeSet(Collections.emptySet());
            }
            Set<Node<OWLDatatype>> toReturn = asSet(r
                    .getDataNeighbours(root, true)
                    .entities()
                    .map(p1 -> p1.asOWLDataProperty())
                    .flatMap(p1 -> r.getDataNeighbours(root, p1).stream())
                    .map(pointer -> new OWLDatatypeNode(buildNode(direct,
                            pointer))));
            return createNodeSet(toReturn);
        }

        protected List<OWLDatatype> buildNode(boolean direct, RootNode pointer) {
            // TODO anonymous expressions are nulls in the
            // entities
            List<OWLDatatype> node = asList(r.getDataLabel(pointer, direct)
                    .entities().filter(c1 -> c1 != null)
                    .map(c1 -> c1.asOWLDatatype()));
            return node;
        }
    }
}
