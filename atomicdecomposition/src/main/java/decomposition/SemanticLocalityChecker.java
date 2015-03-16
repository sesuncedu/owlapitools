package decomposition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLAsymmetricObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLAxiomVisitor;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLDataPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLDatatypeDefinitionAxiom;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLDifferentIndividualsAxiom;
import org.semanticweb.owlapi.model.OWLDisjointClassesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointDataPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointUnionAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentDataPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLFunctionalDataPropertyAxiom;
import org.semanticweb.owlapi.model.OWLFunctionalObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLHasKeyAxiom;
import org.semanticweb.owlapi.model.OWLInverseFunctionalObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLInverseObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLIrreflexiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLNegativeDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLNegativeObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLReflexiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLRuntimeException;
import org.semanticweb.owlapi.model.OWLSameIndividualAxiom;
import org.semanticweb.owlapi.model.OWLSubAnnotationPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.OWLSubDataPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubObjectPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubPropertyChainOfAxiom;
import org.semanticweb.owlapi.model.OWLSymmetricObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLTransitiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.SWRLRule;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.util.MultiMap;

/** semantic locality checker for DL axioms */
public class SemanticLocalityChecker implements OWLAxiomVisitor,
        LocalityChecker {

    /** Reasoner to detect the tautology */
    private OWLReasoner Kernel;
    private OWLDataFactory df;
    private OWLReasonerFactory factory;
    /** map between axioms and concept expressions */
    private MultiMap<OWLAxiom, OWLClassExpression> ExprMap = new MultiMap<OWLAxiom, OWLClassExpression>();

    /**
     * @param axiom
     *        axiom
     * @return expression necessary to build query for a given type of an axiom
     */
    private Collection<OWLClassExpression> getExpr(OWLAxiom axiom) {
        return axiom.getNestedClassExpressions();
    }

    /** signature to keep */
    private Signature sig = new Signature();

    @Override
    public Signature getSignature() {
        return sig;
    }

    /** set a new value of a signature (without changing a locality parameters) */
    @Override
    public void setSignatureValue(Signature Sig) {
        sig = Sig;
    }

    /** remember the axiom locality value here */
    private boolean isLocal;
    private OWLOntologyManager manager;

    /**
     * init c'tor
     * 
     * @param f
     *        reasoner factory
     * @param m
     *        manager
     */
    public SemanticLocalityChecker(OWLReasonerFactory f, OWLOntologyManager m) {
        factory = f;
        manager = m;
        df = manager.getOWLDataFactory();
        isLocal = true;
    }

    @Override
    public boolean local(OWLAxiom axiom) {
        axiom.accept(this);
        return isLocal;
    }

    /* init kernel with the ontology signature */
    @Override
    public void preprocessOntology(Collection<AxiomWrapper> axioms) {
        ExprMap.clear();
        Signature s = new Signature();
        for (AxiomWrapper q : axioms) {
            if (q.isUsed()) {
                ExprMap.putAll(q.getAxiom(), getExpr(q.getAxiom()));
                s.addAll(q.getAxiom().getSignature());
            }
        }
        // register all the objects in the ontology signature
        Set<OWLAxiom> declarationAxioms = new HashSet<OWLAxiom>();
        for (OWLEntity p : s.getSignature()) {
            declarationAxioms.add(df.getOWLDeclarationAxiom(p));
        }
        try {
            Kernel = factory.createReasoner(manager
                    .createOntology(declarationAxioms));
        } catch (OWLOntologyCreationException e) {
            throw new OWLRuntimeException(e);
        }
        Kernel.precomputeInferences(InferenceType.CLASS_HIERARCHY);
    }

    @Override
    public void visit(OWLDeclarationAxiom axiom) {
        isLocal = true;
    }

    @Override
    public void visit(OWLEquivalentClassesAxiom axiom) {
        isLocal = false;
        List<OWLClassExpression> arguments = new ArrayList<OWLClassExpression>(
                axiom.getClassExpressions());
        int size = arguments.size();
        OWLClassExpression C = arguments.get(0);
        for (int i = 1; i < size; i++) {
            OWLClassExpression p = arguments.get(i);
            if (!Kernel.isEntailed(df.getOWLEquivalentClassesAxiom(C, p))) {
                return;
            }
        }
        isLocal = true;
    }

    @Override
    public void visit(OWLDisjointClassesAxiom axiom) {
        isLocal = false;
        List<OWLClassExpression> arguments = new ArrayList<OWLClassExpression>(
                axiom.getClassExpressions());
        int size = arguments.size();
        for (int i = 0; i < size; i++) {
            OWLClassExpression p = arguments.get(i);
            for (int j = i + 1; j < size; j++) {
                OWLClassExpression q = arguments.get(j);
                if (!Kernel.isEntailed(df.getOWLDisjointClassesAxiom(p, q))) {
                    return;
                }
            }
        }
        isLocal = true;
    }

    @Override
    public void visit(OWLDisjointUnionAxiom axiom) {
        isLocal = false;
        // check A = (or C1... Cn)
        if (!Kernel.isEntailed(df.getOWLEquivalentClassesAxiom(
                axiom.getOWLClass(),
                df.getOWLObjectIntersectionOf(axiom.getClassExpressions())))) {
            return;
        }
        // check disjoint(C1...Cn)
        List<OWLClassExpression> arguments = new ArrayList<OWLClassExpression>(
                axiom.getClassExpressions());
        int size = arguments.size();
        for (int i = 0; i < size; i++) {
            for (int j = i + 1; j < size; j++) {
                if (!Kernel.isEntailed(df.getOWLDisjointClassesAxiom(
                        arguments.get(i), arguments.get(j)))) {
                    return;
                }
            }
        }
        isLocal = true;
    }

    @Override
    public void visit(OWLEquivalentObjectPropertiesAxiom axiom) {
        isLocal = false;
        List<OWLObjectPropertyExpression> arguments = new ArrayList<OWLObjectPropertyExpression>(
                axiom.getProperties());
        int size = arguments.size();
        OWLObjectPropertyExpression R = arguments.get(0);
        for (int i = 1; i < size; i++) {
            if (!(Kernel.isEntailed(df.getOWLSubObjectPropertyOfAxiom(R,
                    arguments.get(i))) && Kernel.isEntailed(df
                    .getOWLSubObjectPropertyOfAxiom(arguments.get(i), R)))) {
                return;
            }
        }
        isLocal = true;
    }

    // tautology if all the subsumptions Ri [= Rj holds
    @Override
    public void visit(OWLEquivalentDataPropertiesAxiom axiom) {
        isLocal = false;
        List<OWLDataPropertyExpression> arguments = new ArrayList<OWLDataPropertyExpression>(
                axiom.getProperties());
        OWLDataPropertyExpression R = arguments.get(0);
        for (int i = 1; i < arguments.size(); i++) {
            if (!(Kernel.isEntailed(df.getOWLSubDataPropertyOfAxiom(R,
                    arguments.get(i))) && Kernel.isEntailed(df
                    .getOWLSubDataPropertyOfAxiom(arguments.get(i), R)))) {
                return;
            }
        }
        isLocal = true;
    }

    @Override
    public void visit(OWLDisjointObjectPropertiesAxiom axiom) {
        isLocal = Kernel.isEntailed(axiom);
    }

    @Override
    public void visit(OWLDisjointDataPropertiesAxiom axiom) {
        isLocal = Kernel.isEntailed(axiom);
    }

    // never local
    @Override
    public void visit(OWLSameIndividualAxiom axiom) {
        isLocal = false;
    }

    // never local
    @Override
    public void visit(OWLDifferentIndividualsAxiom axiom) {
        isLocal = false;
    }

    // R = inverse(S) is tautology iff R [= S- and S [= R-
    @Override
    public void visit(OWLInverseObjectPropertiesAxiom axiom) {
        isLocal = Kernel.isEntailed(df.getOWLSubObjectPropertyOfAxiom(axiom
                .getFirstProperty(), axiom.getSecondProperty()
                .getInverseProperty()))
                && Kernel.isEntailed(df.getOWLSubObjectPropertyOfAxiom(axiom
                        .getFirstProperty().getInverseProperty(), axiom
                        .getSecondProperty()));
    }

    @Override
    public void visit(OWLSubPropertyChainOfAxiom axiom) {
        isLocal = Kernel.isEntailed(axiom);
    }

    @Override
    public void visit(OWLSubDataPropertyOfAxiom axiom) {
        isLocal = Kernel.isEntailed(axiom);
    }

    @Override
    public void visit(OWLSubObjectPropertyOfAxiom axiom) {
        isLocal = Kernel.isEntailed(axiom);
    }

    // Domain(R) = C is tautology iff ER.Top [= C
    @Override
    public void visit(OWLObjectPropertyDomainAxiom axiom) {
        isLocal = true;
        for (OWLClassExpression e : ExprMap.get(axiom)) {
            isLocal &= Kernel.isEntailed(df.getOWLSubClassOfAxiom(e,
                    axiom.getDomain()));
        }
    }

    @Override
    public void visit(OWLDataPropertyDomainAxiom axiom) {
        isLocal = true;
        for (OWLClassExpression e : ExprMap.get(axiom)) {
            isLocal &= Kernel.isEntailed(df.getOWLSubClassOfAxiom(e,
                    axiom.getDomain()));
        }
    }

    // Range(R) = C is tautology iff ER.~C is unsatisfiable
    @Override
    public void visit(OWLObjectPropertyRangeAxiom axiom) {
        isLocal = true;
        for (OWLClassExpression e : ExprMap.get(axiom)) {
            isLocal &= !Kernel.isSatisfiable(e);
        }
    }

    @Override
    public void visit(OWLDataPropertyRangeAxiom axiom) {
        isLocal = true;
        for (OWLClassExpression e : ExprMap.get(axiom)) {
            isLocal &= !Kernel.isSatisfiable(e);
        }
    }

    @Override
    public void visit(OWLTransitiveObjectPropertyAxiom axiom) {
        isLocal = Kernel.isEntailed(axiom);
    }

    @Override
    public void visit(OWLReflexiveObjectPropertyAxiom axiom) {
        isLocal = Kernel.isEntailed(axiom);
    }

    @Override
    public void visit(OWLIrreflexiveObjectPropertyAxiom axiom) {
        isLocal = Kernel.isEntailed(axiom);
    }

    @Override
    public void visit(OWLSymmetricObjectPropertyAxiom axiom) {
        isLocal = Kernel.isEntailed(axiom);
    }

    @Override
    public void visit(OWLAsymmetricObjectPropertyAxiom axiom) {
        isLocal = Kernel.isEntailed(axiom);
    }

    @Override
    public void visit(OWLFunctionalObjectPropertyAxiom axiom) {
        isLocal = Kernel.isEntailed(axiom);
    }

    @Override
    public void visit(OWLFunctionalDataPropertyAxiom axiom) {
        isLocal = Kernel.isEntailed(axiom);
    }

    @Override
    public void visit(OWLInverseFunctionalObjectPropertyAxiom axiom) {
        isLocal = Kernel.isEntailed(axiom);
    }

    @Override
    public void visit(OWLSubClassOfAxiom axiom) {
        isLocal = Kernel.isEntailed(axiom);
    }

    // for top locality, this might be local
    @Override
    public void visit(OWLClassAssertionAxiom axiom) {
        isLocal = Kernel.isEntailed(axiom);
    }

    @Override
    public void visit(OWLObjectPropertyAssertionAxiom axiom) {
        isLocal = Kernel.isEntailed(axiom);
    }

    @Override
    public void visit(OWLNegativeObjectPropertyAssertionAxiom axiom) {
        isLocal = Kernel.isEntailed(axiom);
    }

    @Override
    public void visit(OWLDataPropertyAssertionAxiom axiom) {
        isLocal = Kernel.isEntailed(axiom);
    }

    @Override
    public void visit(OWLNegativeDataPropertyAssertionAxiom axiom) {
        isLocal = Kernel.isEntailed(axiom);
    }

    @Override
    public void visit(OWLAnnotationAssertionAxiom axiom) {
        // TODO Auto-generated method stub
    }

    @Override
    public void visit(OWLSubAnnotationPropertyOfAxiom axiom) {
        // TODO Auto-generated method stub
    }

    @Override
    public void visit(OWLAnnotationPropertyDomainAxiom axiom) {
        // TODO Auto-generated method stub
    }

    @Override
    public void visit(OWLAnnotationPropertyRangeAxiom axiom) {
        // TODO Auto-generated method stub
    }

    @Override
    public void visit(OWLHasKeyAxiom axiom) {
        // TODO Auto-generated method stub
    }

    @Override
    public void visit(OWLDatatypeDefinitionAxiom axiom) {
        // TODO Auto-generated method stub
    }

    @Override
    public void visit(SWRLRule rule) {
        // TODO Auto-generated method stub
    }

    @Override
    public boolean isTopEquivalent(OWLObject expr) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isBotEquivalent(OWLObject expr) {
        // TODO Auto-generated method stub
        return false;
    }
}
