package decomposition;

import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import uk.ac.manchester.cs.atomicdecomposition.Atom;
import uk.ac.manchester.cs.atomicdecomposition.AtomicDecomposerOWLAPITOOLS;
import uk.ac.manchester.cs.atomicdecomposition.AtomicDecomposition;

import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@SuppressWarnings("javadoc")
public class AtomicDecomposerDepedenciesTest {
    @Test
    public void atomicDecomposerDepedenciesTest() throws OWLOntologyCreationException {
        // given
        OWLOntology o = getOntology();
        assertEquals(3, o.getAxiomCount());
        AtomicDecomposition ad = new AtomicDecomposerOWLAPITOOLS(o);
        assertEquals(3, ad.getAtoms().size());
        Atom atom = ad.getBottomAtoms().iterator().next();
        assertNotNull(atom);
        // when
        Set<Atom> dependencies = ad.getDependencies(atom, true);
        Set<Atom> dependencies2 = ad.getDependencies(atom, false);
        dependencies2.remove(atom);
        // then
        assertEquals(0, dependencies2.size());
        assertEquals(0, dependencies.size());
    }

    private OWLOntology getOntology() throws OWLOntologyCreationException {
        OWLOntologyManager m = OWLManager.createOWLOntologyManager();
        OWLOntology o = m.createOntology();
        OWLDataFactory f = m.getOWLDataFactory();
        OWLClass powerYoga = f.getOWLClass(IRI.create("urn:test#PowerYoga"));
        OWLClass yoga = f.getOWLClass(IRI.create("urn:test#Yoga"));
        OWLClass relaxation = f.getOWLClass(IRI.create("urn:test#Relaxation"));
        OWLClass activity = f.getOWLClass(IRI.create("urn:test#Activity"));
        m.addAxiom(o, f.getOWLSubClassOfAxiom(powerYoga, yoga));
        m.addAxiom(o, f.getOWLSubClassOfAxiom(yoga, relaxation));
        m.addAxiom(o, f.getOWLSubClassOfAxiom(relaxation, activity));
        return o;
    }
}
