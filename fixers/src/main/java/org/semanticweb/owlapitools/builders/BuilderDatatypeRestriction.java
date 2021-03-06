package org.semanticweb.owlapitools.builders;

import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLDatatypeRestriction;
import org.semanticweb.owlapi.model.OWLFacetRestriction;

/** Builder class for OWLDatatypeRestriction */
public class BuilderDatatypeRestriction
        extends
        BaseSetBuilder<OWLDatatypeRestriction, BuilderDatatypeRestriction, OWLFacetRestriction> {
    private OWLDatatype type = null;

    /** builder initialized from an existing object
     * 
     * @param expected
     *            the existing object */
    public BuilderDatatypeRestriction(OWLDatatypeRestriction expected) {
        withDatatype(expected.getDatatype()).withItems(expected.getFacetRestrictions());
    }

    /** uninitialized builder */
    public BuilderDatatypeRestriction() {}

    /** @param arg
     *            range
     * @return builder */
    public BuilderDatatypeRestriction withDatatype(OWLDatatype arg) {
        type = arg;
        return this;
    }

    @Override
    public OWLDatatypeRestriction buildObject() {
        return df.getOWLDatatypeRestriction(type, items);
    }
}
