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

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.Nonnull;

import org.coode.suggestor.api.FillerSuggestor;
import org.coode.suggestor.api.PropertySuggestor;
import org.coode.suggestor.impl.SuggestorFactory;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

@SuppressWarnings("javadoc")
public class CreateExistentialTree extends AbstractSuggestorTest {

    private final Set<Node<OWLClass>> visited = new HashSet<>();

    @Override
    protected OWLOntology createOntology() throws OWLOntologyCreationException {
        return mngr.loadOntologyFromOntologyDocument(IRI
                .create("http://www.co-ode.org/ontologies/pizza/pizza.owl"));
    }

    public void testCreateTree() throws Exception {
        OWLOntology ont = createOntology();
        OWLReasoner r = ((OWLReasonerFactory) Class.forName(
                DEFAULT_REASONER_FACTORY).newInstance())
                .createNonBufferingReasoner(ont);
        SuggestorFactory fac = new SuggestorFactory(r);
        PropertySuggestor ps = fac.getPropertySuggestor();
        FillerSuggestor fs = fac.getFillerSuggestor();
        long start = System.currentTimeMillis();
        for (int i = 0; i < 20; i++) {
            visited.clear();
            printClass(r.getTopClassNode(), 0, ps, r, fs);
        }
        long end = System.currentTimeMillis();
        System.out.println("Complete in " + (end - start) + "ms");
    }

    private void printClass(Node<OWLClass> cNode, int indent,
            PropertySuggestor ps, OWLReasoner r, FillerSuggestor fs) {
        print(cNode, indent);
        if (visited.add(cNode)) {
            final OWLClassExpression c = cNode.getRepresentativeElement();
            for (Node<OWLObjectPropertyExpression> p : ps
                    .getCurrentObjectProperties(c, true)) {
                printProperty(c, p, indent + 3, fs);
            }
            for (Node<OWLClass> sub : r.getSubClasses(c, true)) {
                if (!sub.isBottomNode()) {
                    printClass(sub, indent + 1, ps, r, fs);
                }
            }
        }
    }

    private static void
            printProperty(@Nonnull OWLClassExpression c,
                    Node<OWLObjectPropertyExpression> p, int indent,
                    FillerSuggestor fs) {
        print(p, indent);
        for (Node<OWLClass> f : fs.getCurrentNamedFillers(c,
                p.getRepresentativeElement(), true)) {
            print(f, indent + 1);
        }
    }

    private static void print(Node<? extends OWLObject> node, int indent) {
        System.out.println();
        for (int i = 0; i < indent; i++) {
            System.out.print("    ");
        }
        AtomicBoolean started = new AtomicBoolean(false);
        node.entities().forEach(o -> {
            if (started.get()) {
                System.out.print(" == ");
            } else {
                started.set(true);
            }
            if (o instanceof OWLEntity) {
                System.out.print(((OWLEntity) o).getIRI().getShortForm());
            } else {
                System.out.print(o);
            }
        });
    }
}
