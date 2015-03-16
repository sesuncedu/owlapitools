package uk.ac.manchester.cs.atomicdecomposition;

import java.util.Set;

/** The atomic decomposition graph */
public interface AtomicDecomposition {

    /** @return all atoms */
    Set<Atom> getAtoms();

    /** @return the set of bottom atoms */
    Set<Atom> getBottomAtoms();

    /** @param atom
     *            atom
     * @param direct
     *            true if only direct dependencies should be returned
     * @return dependencies set for atom; it includes atom */
    Set<Atom> getDependencies(Atom atom, boolean direct);

}
