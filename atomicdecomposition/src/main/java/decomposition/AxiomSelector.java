package decomposition;

import static org.semanticweb.owlapi.util.OWLAPIStreamUtils.asList;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.parameters.Imports;

/**
 * A filter for axioms
 * 
 * @author ignazio
 */
public class AxiomSelector {

    /**
     * @param o
     *        the ontology to filter
     * @return list of declarations and logical axioms
     */
    public static List<OWLAxiom> selectAxioms(OWLOntology o) {
        return asList(AxiomType.LOGICAL_AXIOMS_AND_DECLARATIONS_TYPES.stream()
                .flatMap(axioms(o)), OWLAxiom.class);
    }

    @SuppressWarnings("unchecked")
    protected static Function<? super AxiomType<?>, Stream<OWLAxiom>> axioms(
            OWLOntology o) {
        return type -> (Stream<OWLAxiom>) o.axioms(type, Imports.INCLUDED);
    }

    /**
     * @param o
     *        axioms to wrap
     * @return axioms wrapped as AxiomWrapper
     */
    public static List<AxiomWrapper> wrap(List<OWLAxiom> o) {
        return asList(o.stream().map(ax -> new AxiomWrapper(ax)));
    }
}
