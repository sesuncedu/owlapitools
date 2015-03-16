package decomposition;

import org.semanticweb.owlapi.model.OWLDataRange;
import org.semanticweb.owlapi.util.OWLObjectVisitorAdapter;

/** helper class to set signature and locality class */
class SigAccessor extends OWLObjectVisitorAdapter {
    LocalityChecker localityChecker;

    /** @param c
     *            locality checker */
    SigAccessor(LocalityChecker c) {
        localityChecker = c;
    }

    /** @param expr
     *            data range to check
     * @return true iff EXPR is a top datatype or a built-in datatype; */
    public boolean isTopOrBuiltInDataType(OWLDataRange expr) {
        return expr.isTopDatatype() || expr.isDatatype()
                && expr.asOWLDatatype().isBuiltIn();
    }

    /** @return true iff roles are treated as TOPs */
    public boolean topRLocal() {
        return localityChecker.getSignature().topRLocal();
    }

    Signature getSignature() {
        return localityChecker.getSignature();
    }
}
