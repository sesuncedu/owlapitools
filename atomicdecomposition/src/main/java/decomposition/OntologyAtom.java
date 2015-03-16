package decomposition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** An ontology atom including module and dependencies information.
 * 
 * @author ignazio */
public class OntologyAtom {
    static Comparator<OntologyAtom> comparator = new Comparator<OntologyAtom>() {
        @Override
        public int compare(OntologyAtom arg0, OntologyAtom arg1) {
            return arg0.getId() - arg1.getId();
        }
    };
    /** set of axioms in the atom */
    private List<AxiomWrapper> axioms = new ArrayList<AxiomWrapper>();
    /** set of axioms in the module (Atom's ideal) */
    private List<AxiomWrapper> module = new ArrayList<AxiomWrapper>();
    /** set of atoms current one depends on */
    private Set<OntologyAtom> dependencies = new HashSet<OntologyAtom>();
    /** set of all atoms current one depends on */
    private Set<OntologyAtom> allDependencies = new HashSet<OntologyAtom>();
    /** unique atom's identifier */
    private int id = 0;

    /** remove all atoms in AllDepAtoms from DepAtoms */
    private void filterDep() {
        for (OntologyAtom p : allDependencies) {
            dependencies.remove(p);
        }
    }

    /** build all dep atoms; filter them from DepAtoms
     * 
     * @param checked
     *            sets of atoms to check */
    private void buildAllDepAtoms(Set<OntologyAtom> checked) {
        // first gather all dep atoms from all known dep atoms
        for (OntologyAtom p : dependencies) {
            Set<OntologyAtom> Dep = p.getAllDepAtoms(checked);
            allDependencies.addAll(Dep);
        }
        // now filter them out from known dep atoms
        filterDep();
        // add direct deps to all deps
        allDependencies.addAll(dependencies);
        // now the atom is checked
        checked.add(this);
    }

    // fill in the sets
    /** set the module axioms
     * 
     * @param module
     *            the module axioms */
    public void setModule(Collection<AxiomWrapper> module) {
        this.module = new ArrayList<AxiomWrapper>(module);
    }

    /** @param ax
     *            axiom to add to the atom */
    public void addAxiom(AxiomWrapper ax) {
        axioms.add(ax);
        ax.setAtom(this);
    }

    /** @param axs
     *            axioms to add to the atom */
    public void addAxioms(Collection<AxiomWrapper> axs) {
        for (AxiomWrapper ax : axs) {
            addAxiom(ax);
        }
    }

    /** @param atom
     *            add atom to the dependency set */
    public void addDepAtom(OntologyAtom atom) {
        if (atom != null && atom != this) {
            dependencies.add(atom);
        }
    }

    /** get all the atoms the current one depends on; build this set if necessary
     * 
     * @param checked
     *            atoms to check
     * @return all dependencies */
    public Set<OntologyAtom> getAllDepAtoms(Set<OntologyAtom> checked) {
        if (checked.contains(this)) {
            buildAllDepAtoms(checked);
        }
        return allDependencies;
    }

    // access to axioms
    /** @return all the atom's axioms */
    public List<AxiomWrapper> getAtomAxioms() {
        return axioms;
    }

    /** @return all the module axioms */
    public List<AxiomWrapper> getModule() {
        return module;
    }

    /** @return atoms a given one depends on */
    public Set<OntologyAtom> getDependencies() {
        return dependencies;
    }

    /** @return the value of the id */
    public int getId() {
        return id;
    }

    /** @param id
     *            the id value */
    public void setId(int id) {
        this.id = id;
    }
}
