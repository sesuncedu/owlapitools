package uk.ac.manchester.cs.atomicdecomposition;

import decomposition.*;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.util.MultiMap;
import uk.ac.manchester.cs.owlapi.modularity.ModuleType;

import java.util.*;

/** atomc decomposition implementation */
public class AtomicDecomposerOWLAPITOOLS implements AtomicDecomposition {
    Set<OWLAxiom> globalAxioms;
    private final MultiMap<OWLEntity, Atom> termBasedIndex = new MultiMap<OWLEntity, Atom>() {
        private static final long serialVersionUID = 1L;

        @Override
        protected Collection<Atom> createCollection() {
            return Collections.newSetFromMap(new IdentityHashMap<Atom, Boolean>());
        }
    };
    private List<Atom> atoms;
    private Map<Atom, Integer> atomIndex = new HashMap<Atom, Integer>();
    private IdentityMultiMap<Atom, Atom> dependents = new IdentityMultiMap<Atom, Atom>();
    private IdentityMultiMap<Atom, Atom> dependencies = new IdentityMultiMap<Atom, Atom>();
    private Decomposer decomposer;
    private final ModuleType type;

    private Set<OWLAxiom> asSet(Collection<AxiomWrapper> c) {
        Set<OWLAxiom> toReturn = new HashSet<OWLAxiom>();
        for (AxiomWrapper p : c) {
            toReturn.add(p.getAxiom());
        }
        return toReturn;
    }

    /** @param o
     *            o */
    public AtomicDecomposerOWLAPITOOLS(OWLOntology o) {
        this(AxiomSelector.selectAxioms(o), ModuleType.BOT);
    }

    /** @param o
     *            o
     * @param type
     *            type */
    public AtomicDecomposerOWLAPITOOLS(OWLOntology o, ModuleType type) {
        this(AxiomSelector.selectAxioms(o), type);
    }

    /** @param axioms
     *            axioms
     * @param type
     *            type */
    private AtomicDecomposerOWLAPITOOLS(List<OWLAxiom> axioms, ModuleType type) {
        this.type = type;
        decomposer = new Decomposer(AxiomSelector.wrap(axioms),
                new SyntacticLocalityChecker());
        int size = decomposer.getAOS(this.type).size();
        atoms = new ArrayList<Atom>();
        for (int i = 0; i < size; i++) {
            final Atom atom = new Atom(asSet(decomposer.getAOS().get(i).getAtomAxioms()));
            atoms.add(atom);
            atomIndex.put(atom, i);
            for (OWLEntity e : atom.getSignature()) {
                termBasedIndex.put(e, atom);
            }
        }
        for (int i = 0; i < size; i++) {
            Set<OntologyAtom> dependentIndexes = decomposer.getAOS().get(i)
                    .getDependencies();
            for (OntologyAtom j : dependentIndexes) {
                dependencies.put(atoms.get(i), atoms.get(j.getId()));
                dependents.put(atoms.get(j.getId()), atoms.get(i));
            }
        }
    }

    @Override
    public Set<Atom> getAtoms() {
        return new HashSet<Atom>(atoms);
    }

    @Override
    public Set<Atom> getDependencies(Atom atom, boolean direct) {
        return explore(atom, direct, dependencies);
    }

    private Set<Atom> explore(Atom atom, boolean direct, IdentityMultiMap<Atom, Atom> multimap) {
        if (direct) {
            Set<Atom> hashSet = new HashSet<Atom>(multimap.get(atom));
            for (Atom a : multimap.get(atom)) {
                hashSet.removeAll(multimap.get(a));
            }
            return hashSet;
        }
        Map<Atom, Atom> toReturn = new HashMap<Atom, Atom>();
        toReturn.put(atom, atom);
        List<Atom> toDo = new ArrayList<Atom>();
        toDo.add(atom);
        for (int i = 0; i < toDo.size(); i++) {
            final Atom key = toDo.get(i);
            if (key != null) {
                Collection<Atom> c = multimap.get(key);
                for (Atom a : c) {
                    if (toReturn.put(a, a) == null) {
                        toDo.add(a);
                    }
                }
            }
        }
        return toReturn.keySet();
    }

    @Override
    public Set<Atom> getBottomAtoms() {
        Set<Atom> keys = getAtoms();
        keys.removeAll(dependents.getAllValues());
        return keys;
    }

    /** get a set of axioms that corresponds to the module of the atom with the
     * id INDEX
     * 
     * @param index
     *            index
     * @return module at index */
    Collection<AxiomWrapper> getAtomModule(int index) {
        return decomposer.getAOS().get(index).getModule();
    }

}
