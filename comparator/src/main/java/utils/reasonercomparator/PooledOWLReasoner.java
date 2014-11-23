/*
 * Date: Jan 13, 2012
 *
 * code made available under Mozilla Public License (http://www.mozilla.org/MPL/MPL-1.1.html)
 *
 * copyright 2012, Ignazio Palmisano, University of Manchester
 *
 */
package utils.reasonercomparator;

import static org.semanticweb.owlapi.util.OWLAPIStreamUtils.asList;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.semanticweb.owlapi.apibinding.configurables.Computable;
import org.semanticweb.owlapi.apibinding.configurables.ComputableAllThrowables;
import org.semanticweb.owlapi.apibinding.configurables.MemoizingCache;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyChangeListener;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.AxiomNotInProfileException;
import org.semanticweb.owlapi.reasoner.BufferingMode;
import org.semanticweb.owlapi.reasoner.ClassExpressionNotInProfileException;
import org.semanticweb.owlapi.reasoner.FreshEntitiesException;
import org.semanticweb.owlapi.reasoner.FreshEntityPolicy;
import org.semanticweb.owlapi.reasoner.InconsistentOntologyException;
import org.semanticweb.owlapi.reasoner.IndividualNodeSetPolicy;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.ReasonerInterruptedException;
import org.semanticweb.owlapi.reasoner.TimeOutException;
import org.semanticweb.owlapi.reasoner.UnsupportedEntailmentTypeException;
import org.semanticweb.owlapi.util.Version;

/**
 * multiple reasoners pooled together to handle queries on separate threads
 * 
 * @author ignazio
 */
@SuppressWarnings("unchecked")
public final class PooledOWLReasoner implements OWLReasoner,
        OWLOntologyChangeListener {

    private static final class BoolKey {

        Object o;
        boolean b;

        public BoolKey() {}

        @Override
        public int hashCode() {
            return o.hashCode() * (b ? 1 : -1);
        }

        @Override
        public boolean equals(Object obj) {
            BoolKey bk = (BoolKey) obj;
            return b == bk.b && o.equals(bk.o);
        }
    }

    private static final class RegKey {

        Object o1;
        Object o2;

        public RegKey() {}

        @Override
        public int hashCode() {
            return o1.hashCode() * o2.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            RegKey bk = (RegKey) obj;
            return o1.equals(bk.o1) && o2.equals(bk.o2);
        }
    }

    private final static class CachedReasoner {

        public static Object makeKey(Object o1, Object o2) {
            RegKey r = new RegKey();
            r.o1 = o1;
            r.o2 = o2;
            return r;
        }

        public static Object makeKey(Object o1, boolean o2) {
            BoolKey r = new BoolKey();
            r.o = o1;
            r.b = o2;
            return r;
        }

        public CachedReasoner() {}

        MemoizingCache<CacheKeys, MemoizingCache<Object, Object>> mainCache = new MemoizingCache<>();

        public void clear() {
            mainCache.clear();
        }

        public <T> T get(CacheKeys cachekey, Object key, Computable<Object> c) {
            Computable<MemoizingCache<Object, Object>> cacheinit = new ComputableAllThrowables<MemoizingCache<Object, Object>>() {

                @Override
                public MemoizingCache<Object, Object> compute() {
                    try {
                        return new MemoizingCache<>();
                    } catch (Throwable e) {
                        exception = e;
                        return null;
                    }
                }
            };
            T t = (T) mainCache.get(cacheinit, cachekey).get(c, key);
            if (cacheinit.hasThrownException()) {
                if (cacheinit.thrownException() instanceof Error) {
                    throw (Error) cacheinit.thrownException();
                }
                throw (RuntimeException) cacheinit.thrownException();
            }
            return t;
        }
    }

    protected final CachedReasoner cache = new CachedReasoner();
    protected final OWLOntology rootOntology;
    protected int concurrentNumber = 4;
    protected final OWLReasoner[] concurrentDelegates = new OWLReasoner[concurrentNumber];
    protected int index = 0;
    private ExecutorService exec = Executors.newCachedThreadPool();

    /**
     * @param factory
     *        reasoner factory
     * @param ontology
     *        ontology
     * @param manager
     *        manager
     */
    public PooledOWLReasoner(OWLReasonerFactory factory, OWLOntology ontology,
            OWLOntologyManager manager) {
        for (int i = 0; i < concurrentNumber; i++) {
            concurrentDelegates[i] = factory.createReasoner(ontology);
        }
        manager.addOntologyChangeListener(this);
        rootOntology = ontology;
    }

    private void prefetch() {
        List<OWLClass> classes = asList(rootOntology.classesInSignature());
        int chunk = classes.size() / concurrentNumber;
        for (int i = 0; i < concurrentNumber; i++) {
            int position = i * chunk;
            exec.execute(() -> {
                for (int j = position; j < chunk + position; j++) {
                    /*
                     * preload all named classes; it is safe to start
                     * asynchronously since all actual reasoners are
                     * synchronized, so they are either precomputing
                     * inferences or will do so to answer the query
                     */
                    getEquivalentClasses(classes.get(j));
                    rootOntology.subClassAxiomsForSubClass(classes.get(j))
                            .forEach(
                                    ax -> getEquivalentClasses(ax
                                            .getSuperClass()));
                    rootOntology.equivalentClassesAxioms(classes.get(j))
                            .flatMap(ax -> ax.classExpressions())
                            .forEach(e -> getEquivalentClasses(e));
                }
            });
        }
    }

    protected synchronized final OWLReasoner next() {
        // System.out.println("PooledOWLReasoner.next() spinning " + index);
        OWLReasoner toReturn = concurrentDelegates[index++];
        index %= concurrentNumber;
        return toReturn;
    }

    private enum CacheKeys {
        isEntailed,
        subclasses,
        subclassesDirect,
        superclassesDirect,
        superclasses,
        equivclasses,
        disjointclasses,
        subobjectpropertiesDirect,
        subobjectproperties,
        superobjectpropertiesDirect,
        superobjectproperties,
        equivobjectproperties,
        disjointobjectproperties,
        inverseobjectproperties,
        objectpropertiesdomainsDirect,
        objectpropertiesdomains,
        objectpropertiesranges,
        objectpropertiesrangesDirect,
        diffindividual,
        datapropertiesdomains,
        datapropertiesdomainsDirect,
        datapropertiesvalues,
        disjointdataproperties,
        equivdataproperties,
        instances,
        instancesDirect,
        objectpropertiesvalues,
        sameindividual,
        subdataproperties,
        subdatapropertiesDirect,
        superdataproperties,
        superdatapropertiesDirect,
        types,
        typesDirect,
        issatisfiable
    }

    @Override
    public String getReasonerName() {
        return next().getReasonerName();
    }

    @Override
    public Version getReasonerVersion() {
        return next().getReasonerVersion();
    }

    @Override
    public BufferingMode getBufferingMode() {
        return next().getBufferingMode();
    }

    @Override
    public void flush() {
        cache.clear();
        for (int i = 0; i < concurrentNumber; i++) {
            if (concurrentDelegates[i] != null) {
                concurrentDelegates[i].flush();
            }
        }
    }

    @Override
    public List<OWLOntologyChange> getPendingChanges() {
        return next().getPendingChanges();
    }

    @Override
    public Set<OWLAxiom> getPendingAxiomAdditions() {
        return next().getPendingAxiomAdditions();
    }

    @Override
    public Set<OWLAxiom> getPendingAxiomRemovals() {
        return next().getPendingAxiomRemovals();
    }

    @Override
    public OWLOntology getRootOntology() {
        return rootOntology;
    }

    @Override
    public void interrupt() {
        cache.clear();
        for (int i = 0; i < concurrentNumber; i++) {
            if (concurrentDelegates[i] != null) {
                concurrentDelegates[i].interrupt();
            }
        }
    }

    @Override
    public void precomputeInferences(final InferenceType... inferenceTypes)
            throws ReasonerInterruptedException, TimeOutException,
            InconsistentOntologyException {
        cache.clear();
        for (int i = 0; i < concurrentNumber; i++) {
            final OWLReasoner r = next();
            Runnable starter = new Runnable() {

                @Override
                public void run() {
                    r.precomputeInferences(inferenceTypes);
                }
            };
            exec.execute(starter);
        }
        prefetch();
    }

    @Override
    public boolean isPrecomputed(InferenceType inferenceType) {
        return next().isPrecomputed(inferenceType);
    }

    @Override
    public Set<InferenceType> getPrecomputableInferenceTypes() {
        return next().getPrecomputableInferenceTypes();
    }

    @Override
    public boolean isConsistent() throws ReasonerInterruptedException,
            TimeOutException {
        return next().isConsistent();
    }

    @Override
    public boolean isSatisfiable(final OWLClassExpression classExpression)
            throws ReasonerInterruptedException, TimeOutException,
            ClassExpressionNotInProfileException, FreshEntitiesException,
            InconsistentOntologyException {
        ComputableAllThrowables<Object> entailer = new ComputableAllThrowables<Object>() {

            @Override
            @SuppressWarnings("boxing")
            public Object compute() {
                try {
                    return next().isSatisfiable(classExpression);
                } catch (Throwable e) {
                    exception = e;
                }
                return null;
            }
        };
        Boolean b = cache.get(CacheKeys.issatisfiable, classExpression,
                entailer);
        if (entailer.hasThrownException()) {
            Throwable e = entailer.thrownException();
            if (e instanceof ReasonerInterruptedException) {
                throw (ReasonerInterruptedException) e;
            }
            if (e instanceof ClassExpressionNotInProfileException) {
                throw (ClassExpressionNotInProfileException) e;
            }
            if (e instanceof TimeOutException) {
                throw (TimeOutException) e;
            }
            if (e instanceof AxiomNotInProfileException) {
                throw (AxiomNotInProfileException) e;
            }
            if (e instanceof FreshEntitiesException) {
                throw (FreshEntitiesException) e;
            }
            if (e instanceof InconsistentOntologyException) {
                throw (InconsistentOntologyException) e;
            }
            if (e instanceof Error) {
                throw (Error) e;
            }
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new RuntimeException(e);
        }
        return b.booleanValue();
        // return this.next().isSatisfiable(classExpression);
    }

    @Override
    public Node<OWLClass> getUnsatisfiableClasses()
            throws ReasonerInterruptedException, TimeOutException,
            InconsistentOntologyException {
        return next().getUnsatisfiableClasses();
    }

    @Override
    public boolean isEntailed(final OWLAxiom axiom)
            throws ReasonerInterruptedException,
            UnsupportedEntailmentTypeException, TimeOutException,
            AxiomNotInProfileException, FreshEntitiesException,
            InconsistentOntologyException {
        ComputableAllThrowables<Object> entailer = new ComputableAllThrowables<Object>() {

            @Override
            @SuppressWarnings("boxing")
            public Object compute() {
                try {
                    return next().isEntailed(axiom);
                } catch (Throwable e) {
                    exception = e;
                }
                return null;
            }
        };
        Boolean b = cache.get(CacheKeys.isEntailed, axiom, entailer);
        if (entailer.hasThrownException()) {
            Throwable e = entailer.thrownException();
            if (e instanceof ReasonerInterruptedException) {
                throw (ReasonerInterruptedException) e;
            }
            if (e instanceof UnsupportedEntailmentTypeException) {
                throw (UnsupportedEntailmentTypeException) e;
            }
            if (e instanceof TimeOutException) {
                throw (TimeOutException) e;
            }
            if (e instanceof AxiomNotInProfileException) {
                throw (AxiomNotInProfileException) e;
            }
            if (e instanceof FreshEntitiesException) {
                throw (FreshEntitiesException) e;
            }
            if (e instanceof InconsistentOntologyException) {
                throw (InconsistentOntologyException) e;
            }
            if (e instanceof Error) {
                throw (Error) e;
            }
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new RuntimeException(e);
        }
        return b.booleanValue();
        // return this.next().isEntailed(axiom);
    }

    @Override
    public boolean isEntailed(Set<? extends OWLAxiom> axioms)
            throws ReasonerInterruptedException,
            UnsupportedEntailmentTypeException, TimeOutException,
            AxiomNotInProfileException, FreshEntitiesException,
            InconsistentOntologyException {
        for (OWLAxiom ax : axioms) {
            if (!isEntailed(ax)) {
                return false;
            }
        }
        return true;
        // return this.next().isEntailed(axioms);
    }

    @Override
    public boolean isEntailmentCheckingSupported(AxiomType<?> axiomType) {
        return next().isEntailmentCheckingSupported(axiomType);
    }

    @Override
    public Node<OWLClass> getTopClassNode() {
        return next().getTopClassNode();
    }

    @Override
    public Node<OWLClass> getBottomClassNode() {
        return next().getBottomClassNode();
    }

    @Override
    public NodeSet<OWLClass> getSubClasses(final OWLClassExpression ce,
            final boolean direct) throws ReasonerInterruptedException,
            TimeOutException, FreshEntitiesException,
            InconsistentOntologyException, ClassExpressionNotInProfileException {
        final CacheKeys key = direct ? CacheKeys.subclassesDirect
                : CacheKeys.subclasses;
        Computable<Object> checker = new ComputableAllThrowables<Object>() {

            @Override
            public Object compute() {
                try {
                    return next().getSubClasses(ce, direct);
                } catch (Throwable e) {
                    exception = e;
                }
                return null;
            }
        };
        NodeSet<OWLClass> toReturn = (NodeSet<OWLClass>) cache.get(key,
                CachedReasoner.makeKey(ce, direct), checker);
        if (checker.hasThrownException()) {
            Throwable e = checker.thrownException();
            if (e instanceof ReasonerInterruptedException) {
                throw (ReasonerInterruptedException) e;
            }
            if (e instanceof UnsupportedEntailmentTypeException) {
                throw (UnsupportedEntailmentTypeException) e;
            }
            if (e instanceof TimeOutException) {
                throw (TimeOutException) e;
            }
            if (e instanceof AxiomNotInProfileException) {
                throw (AxiomNotInProfileException) e;
            }
            if (e instanceof FreshEntitiesException) {
                throw (FreshEntitiesException) e;
            }
            if (e instanceof InconsistentOntologyException) {
                throw (InconsistentOntologyException) e;
            }
            if (e instanceof ClassExpressionNotInProfileException) {
                throw (ClassExpressionNotInProfileException) e;
            }
            if (e instanceof Error) {
                throw (Error) e;
            }
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new RuntimeException(e);
        }
        return toReturn;
        // return this.next().getSubClasses(ce, direct);
    }

    @Override
    public NodeSet<OWLClass> getSuperClasses(final OWLClassExpression ce,
            final boolean direct) throws InconsistentOntologyException,
            ClassExpressionNotInProfileException, FreshEntitiesException,
            ReasonerInterruptedException, TimeOutException {
        final CacheKeys key = direct ? CacheKeys.superclassesDirect
                : CacheKeys.superclasses;
        Computable<Object> checker = new ComputableAllThrowables<Object>() {

            @Override
            public Object compute() {
                try {
                    return next().getSuperClasses(ce, direct);
                } catch (Throwable e) {
                    exception = e;
                }
                return null;
            }
        };
        NodeSet<OWLClass> toReturn = (NodeSet<OWLClass>) cache.get(key,
                CachedReasoner.makeKey(ce, direct), checker);
        if (checker.hasThrownException()) {
            Throwable e = checker.thrownException();
            if (e instanceof ReasonerInterruptedException) {
                throw (ReasonerInterruptedException) e;
            }
            if (e instanceof ClassExpressionNotInProfileException) {
                throw (ClassExpressionNotInProfileException) e;
            }
            if (e instanceof TimeOutException) {
                throw (TimeOutException) e;
            }
            if (e instanceof AxiomNotInProfileException) {
                throw (AxiomNotInProfileException) e;
            }
            if (e instanceof FreshEntitiesException) {
                throw (FreshEntitiesException) e;
            }
            if (e instanceof InconsistentOntologyException) {
                throw (InconsistentOntologyException) e;
            }
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new RuntimeException("Failure asking for superclasses of "
                    + ce + " " + direct, e);
        }
        return toReturn;
        // return this.next().getSuperClasses(ce, direct);
    }

    @Override
    public Node<OWLClass> getEquivalentClasses(final OWLClassExpression ce)
            throws InconsistentOntologyException,
            ClassExpressionNotInProfileException, FreshEntitiesException,
            ReasonerInterruptedException, TimeOutException {
        final CacheKeys key = CacheKeys.equivclasses;
        Computable<Object> checker = new ComputableAllThrowables<Object>() {

            @Override
            public Object compute() {
                try {
                    return next().getEquivalentClasses(ce);
                } catch (Throwable e) {
                    exception = e;
                }
                return null;
            }
        };
        Node<OWLClass> toReturn = (Node<OWLClass>) cache.get(key, ce, checker);
        if (checker.hasThrownException()) {
            Throwable e = checker.thrownException();
            if (e instanceof ReasonerInterruptedException) {
                throw (ReasonerInterruptedException) e;
            }
            if (e instanceof InconsistentOntologyException) {
                throw (InconsistentOntologyException) e;
            }
            if (e instanceof TimeOutException) {
                throw (TimeOutException) e;
            }
            if (e instanceof FreshEntitiesException) {
                throw (FreshEntitiesException) e;
            }
            if (e instanceof InconsistentOntologyException) {
                throw (InconsistentOntologyException) e;
            }
            if (e instanceof ClassExpressionNotInProfileException) {
                throw (ClassExpressionNotInProfileException) e;
            }
            if (e instanceof Error) {
                throw (Error) e;
            }
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new RuntimeException(e);
        }
        return toReturn;
        // return this.next().getEquivalentClasses(ce);
    }

    @Override
    public NodeSet<OWLClass> getDisjointClasses(final OWLClassExpression ce)
            throws ReasonerInterruptedException, TimeOutException,
            FreshEntitiesException, InconsistentOntologyException {
        final CacheKeys key = CacheKeys.disjointclasses;
        Computable<Object> checker = new ComputableAllThrowables<Object>() {

            @Override
            public Object compute() {
                try {
                    return next().getDisjointClasses(ce);
                } catch (Throwable e) {
                    exception = e;
                }
                return null;
            }
        };
        NodeSet<OWLClass> toReturn = (NodeSet<OWLClass>) cache.get(key, ce,
                checker);
        if (checker.hasThrownException()) {
            Throwable e = checker.thrownException();
            if (e instanceof ReasonerInterruptedException) {
                throw (ReasonerInterruptedException) e;
            }
            if (e instanceof InconsistentOntologyException) {
                throw (InconsistentOntologyException) e;
            }
            if (e instanceof TimeOutException) {
                throw (TimeOutException) e;
            }
            if (e instanceof FreshEntitiesException) {
                throw (FreshEntitiesException) e;
            }
            if (e instanceof InconsistentOntologyException) {
                throw (InconsistentOntologyException) e;
            }
            if (e instanceof ClassExpressionNotInProfileException) {
                throw (ClassExpressionNotInProfileException) e;
            }
            if (e instanceof Error) {
                throw (Error) e;
            }
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new RuntimeException(e);
        }
        return toReturn;
        // return this.next().getDisjointClasses(ce);
    }

    @Override
    public Node<OWLObjectPropertyExpression> getTopObjectPropertyNode() {
        return next().getTopObjectPropertyNode();
    }

    @Override
    public Node<OWLObjectPropertyExpression> getBottomObjectPropertyNode() {
        return next().getBottomObjectPropertyNode();
    }

    @Override
    public NodeSet<OWLObjectPropertyExpression> getSubObjectProperties(
            final OWLObjectPropertyExpression pe, final boolean direct)
            throws InconsistentOntologyException, FreshEntitiesException,
            ReasonerInterruptedException, TimeOutException {
        final CacheKeys key = direct ? CacheKeys.subobjectpropertiesDirect
                : CacheKeys.subobjectproperties;
        Computable<Object> checker = new ComputableAllThrowables<Object>() {

            @Override
            public Object compute() {
                try {
                    return next().getSubObjectProperties(pe, direct);
                } catch (Throwable e) {
                    exception = e;
                }
                return null;
            }
        };
        NodeSet<OWLObjectPropertyExpression> toReturn = (NodeSet<OWLObjectPropertyExpression>) cache
                .get(key, CachedReasoner.makeKey(pe, direct), checker);
        if (checker.hasThrownException()) {
            Throwable e = checker.thrownException();
            if (e instanceof ReasonerInterruptedException) {
                throw (ReasonerInterruptedException) e;
            }
            if (e instanceof InconsistentOntologyException) {
                throw (InconsistentOntologyException) e;
            }
            if (e instanceof TimeOutException) {
                throw (TimeOutException) e;
            }
            if (e instanceof FreshEntitiesException) {
                throw (FreshEntitiesException) e;
            }
            if (e instanceof InconsistentOntologyException) {
                throw (InconsistentOntologyException) e;
            }
            if (e instanceof ClassExpressionNotInProfileException) {
                throw (ClassExpressionNotInProfileException) e;
            }
            if (e instanceof Error) {
                throw (Error) e;
            }
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new RuntimeException(e);
        }
        return toReturn;
        // return this.next().getSubObjectProperties(pe, direct);
    }

    @Override
    public NodeSet<OWLObjectPropertyExpression> getSuperObjectProperties(
            final OWLObjectPropertyExpression pe, final boolean direct)
            throws InconsistentOntologyException, FreshEntitiesException,
            ReasonerInterruptedException, TimeOutException {
        final CacheKeys key = direct ? CacheKeys.superobjectpropertiesDirect
                : CacheKeys.superobjectproperties;
        Computable<Object> checker = new ComputableAllThrowables<Object>() {

            @Override
            public Object compute() {
                try {
                    return next().getSuperObjectProperties(pe, direct);
                } catch (Throwable e) {
                    exception = e;
                }
                return null;
            }
        };
        NodeSet<OWLObjectPropertyExpression> toReturn = (NodeSet<OWLObjectPropertyExpression>) cache
                .get(key, CachedReasoner.makeKey(pe, direct), checker);
        if (checker.hasThrownException()) {
            Throwable e = checker.thrownException();
            if (e instanceof ReasonerInterruptedException) {
                throw (ReasonerInterruptedException) e;
            }
            if (e instanceof InconsistentOntologyException) {
                throw (InconsistentOntologyException) e;
            }
            if (e instanceof TimeOutException) {
                throw (TimeOutException) e;
            }
            if (e instanceof FreshEntitiesException) {
                throw (FreshEntitiesException) e;
            }
            if (e instanceof InconsistentOntologyException) {
                throw (InconsistentOntologyException) e;
            }
            if (e instanceof ClassExpressionNotInProfileException) {
                throw (ClassExpressionNotInProfileException) e;
            }
            if (e instanceof Error) {
                throw (Error) e;
            }
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new RuntimeException(e);
        }
        return toReturn;
        // return this.next().getSuperObjectProperties(pe, direct);
    }

    @Override
    public Node<OWLObjectPropertyExpression> getEquivalentObjectProperties(
            final OWLObjectPropertyExpression pe)
            throws InconsistentOntologyException, FreshEntitiesException,
            ReasonerInterruptedException, TimeOutException {
        final CacheKeys key = CacheKeys.equivobjectproperties;
        Computable<Object> checker = new ComputableAllThrowables<Object>() {

            @Override
            public Object compute() {
                try {
                    return next().getEquivalentObjectProperties(pe);
                } catch (Throwable e) {
                    exception = e;
                }
                return null;
            }
        };
        Node<OWLObjectPropertyExpression> toReturn = (Node<OWLObjectPropertyExpression>) cache
                .get(key, pe, checker);
        if (checker.hasThrownException()) {
            Throwable e = checker.thrownException();
            if (e instanceof ReasonerInterruptedException) {
                throw (ReasonerInterruptedException) e;
            }
            if (e instanceof InconsistentOntologyException) {
                throw (InconsistentOntologyException) e;
            }
            if (e instanceof TimeOutException) {
                throw (TimeOutException) e;
            }
            if (e instanceof FreshEntitiesException) {
                throw (FreshEntitiesException) e;
            }
            if (e instanceof InconsistentOntologyException) {
                throw (InconsistentOntologyException) e;
            }
            if (e instanceof ClassExpressionNotInProfileException) {
                throw (ClassExpressionNotInProfileException) e;
            }
            if (e instanceof Error) {
                throw (Error) e;
            }
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new RuntimeException(e);
        }
        return toReturn;
        // return this.next().getEquivalentObjectProperties(pe);
    }

    @Override
    public NodeSet<OWLObjectPropertyExpression> getDisjointObjectProperties(
            final OWLObjectPropertyExpression pe)
            throws InconsistentOntologyException, FreshEntitiesException,
            ReasonerInterruptedException, TimeOutException {
        final CacheKeys key = CacheKeys.disjointobjectproperties;
        Computable<Object> checker = new ComputableAllThrowables<Object>() {

            @Override
            public Object compute() {
                try {
                    return next().getDisjointObjectProperties(pe);
                } catch (Throwable e) {
                    exception = e;
                }
                return null;
            }
        };
        NodeSet<OWLObjectPropertyExpression> toReturn = (NodeSet<OWLObjectPropertyExpression>) cache
                .get(key, pe, checker);
        if (checker.hasThrownException()) {
            Throwable e = checker.thrownException();
            if (e instanceof ReasonerInterruptedException) {
                throw (ReasonerInterruptedException) e;
            }
            if (e instanceof InconsistentOntologyException) {
                throw (InconsistentOntologyException) e;
            }
            if (e instanceof TimeOutException) {
                throw (TimeOutException) e;
            }
            if (e instanceof FreshEntitiesException) {
                throw (FreshEntitiesException) e;
            }
            if (e instanceof InconsistentOntologyException) {
                throw (InconsistentOntologyException) e;
            }
            if (e instanceof ClassExpressionNotInProfileException) {
                throw (ClassExpressionNotInProfileException) e;
            }
            if (e instanceof Error) {
                throw (Error) e;
            }
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new RuntimeException(e);
        }
        return toReturn;
        // return this.next().getDisjointObjectProperties(pe);
    }

    @Override
    public Node<OWLObjectPropertyExpression> getInverseObjectProperties(
            final OWLObjectPropertyExpression pe)
            throws InconsistentOntologyException, FreshEntitiesException,
            ReasonerInterruptedException, TimeOutException {
        final CacheKeys key = CacheKeys.inverseobjectproperties;
        Computable<Object> checker = new ComputableAllThrowables<Object>() {

            @Override
            public Object compute() {
                try {
                    return next().getInverseObjectProperties(pe);
                } catch (Throwable e) {
                    exception = e;
                }
                return null;
            }
        };
        Node<OWLObjectPropertyExpression> toReturn = (Node<OWLObjectPropertyExpression>) cache
                .get(key, pe, checker);
        if (checker.hasThrownException()) {
            Throwable e = checker.thrownException();
            if (e instanceof ReasonerInterruptedException) {
                throw (ReasonerInterruptedException) e;
            }
            if (e instanceof InconsistentOntologyException) {
                throw (InconsistentOntologyException) e;
            }
            if (e instanceof TimeOutException) {
                throw (TimeOutException) e;
            }
            if (e instanceof FreshEntitiesException) {
                throw (FreshEntitiesException) e;
            }
            if (e instanceof InconsistentOntologyException) {
                throw (InconsistentOntologyException) e;
            }
            if (e instanceof ClassExpressionNotInProfileException) {
                throw (ClassExpressionNotInProfileException) e;
            }
            if (e instanceof Error) {
                throw (Error) e;
            }
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new RuntimeException(e);
        }
        return toReturn;
        // return this.next().getInverseObjectProperties(pe);
    }

    @Override
    public NodeSet<OWLClass> getObjectPropertyDomains(
            final OWLObjectPropertyExpression pe, final boolean direct)
            throws InconsistentOntologyException, FreshEntitiesException,
            ReasonerInterruptedException, TimeOutException {
        final CacheKeys key = direct ? CacheKeys.objectpropertiesdomainsDirect
                : CacheKeys.objectpropertiesdomains;
        Computable<Object> checker = new ComputableAllThrowables<Object>() {

            @Override
            public Object compute() {
                try {
                    return next().getObjectPropertyDomains(pe, direct);
                } catch (Throwable e) {
                    exception = e;
                }
                return null;
            }
        };
        NodeSet<OWLClass> toReturn = (NodeSet<OWLClass>) cache.get(key,
                CachedReasoner.makeKey(pe, direct), checker);
        if (checker.hasThrownException()) {
            Throwable e = checker.thrownException();
            if (e instanceof ReasonerInterruptedException) {
                throw (ReasonerInterruptedException) e;
            }
            if (e instanceof InconsistentOntologyException) {
                throw (InconsistentOntologyException) e;
            }
            if (e instanceof TimeOutException) {
                throw (TimeOutException) e;
            }
            if (e instanceof FreshEntitiesException) {
                throw (FreshEntitiesException) e;
            }
            if (e instanceof InconsistentOntologyException) {
                throw (InconsistentOntologyException) e;
            }
            if (e instanceof ClassExpressionNotInProfileException) {
                throw (ClassExpressionNotInProfileException) e;
            }
            if (e instanceof Error) {
                throw (Error) e;
            }
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new RuntimeException(e);
        }
        return toReturn;
        // return this.next().getObjectPropertyDomains(pe, direct);
    }

    @Override
    public NodeSet<OWLClass> getObjectPropertyRanges(
            final OWLObjectPropertyExpression pe, final boolean direct)
            throws InconsistentOntologyException, FreshEntitiesException,
            ReasonerInterruptedException, TimeOutException {
        final CacheKeys key = direct ? CacheKeys.objectpropertiesrangesDirect
                : CacheKeys.objectpropertiesranges;
        Computable<Object> checker = new ComputableAllThrowables<Object>() {

            @Override
            public Object compute() {
                try {
                    return next().getObjectPropertyRanges(pe, direct);
                } catch (Throwable e) {
                    exception = e;
                }
                return null;
            }
        };
        NodeSet<OWLClass> toReturn = (NodeSet<OWLClass>) cache.get(key,
                CachedReasoner.makeKey(pe, direct), checker);
        if (checker.hasThrownException()) {
            Throwable e = checker.thrownException();
            if (e instanceof ReasonerInterruptedException) {
                throw (ReasonerInterruptedException) e;
            }
            if (e instanceof InconsistentOntologyException) {
                throw (InconsistentOntologyException) e;
            }
            if (e instanceof TimeOutException) {
                throw (TimeOutException) e;
            }
            if (e instanceof FreshEntitiesException) {
                throw (FreshEntitiesException) e;
            }
            if (e instanceof InconsistentOntologyException) {
                throw (InconsistentOntologyException) e;
            }
            if (e instanceof ClassExpressionNotInProfileException) {
                throw (ClassExpressionNotInProfileException) e;
            }
            if (e instanceof Error) {
                throw (Error) e;
            }
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new RuntimeException(e);
        }
        return toReturn;
        // return this.next().getObjectPropertyRanges(pe, direct);
    }

    @Override
    public Node<OWLDataProperty> getTopDataPropertyNode() {
        return next().getTopDataPropertyNode();
    }

    @Override
    public Node<OWLDataProperty> getBottomDataPropertyNode() {
        return next().getBottomDataPropertyNode();
    }

    @Override
    public NodeSet<OWLDataProperty> getSubDataProperties(
            final OWLDataProperty pe, final boolean direct)
            throws InconsistentOntologyException, FreshEntitiesException,
            ReasonerInterruptedException, TimeOutException {
        final CacheKeys key = direct ? CacheKeys.subdatapropertiesDirect
                : CacheKeys.subdataproperties;
        Computable<Object> checker = new ComputableAllThrowables<Object>() {

            @Override
            public Object compute() {
                try {
                    return next().getSubDataProperties(pe, direct);
                } catch (Throwable e) {
                    exception = e;
                }
                return null;
            }
        };
        NodeSet<OWLDataProperty> toReturn = (NodeSet<OWLDataProperty>) cache
                .get(key, CachedReasoner.makeKey(pe, direct), checker);
        if (checker.hasThrownException()) {
            Throwable e = checker.thrownException();
            if (e instanceof ReasonerInterruptedException) {
                throw (ReasonerInterruptedException) e;
            }
            if (e instanceof InconsistentOntologyException) {
                throw (InconsistentOntologyException) e;
            }
            if (e instanceof TimeOutException) {
                throw (TimeOutException) e;
            }
            if (e instanceof FreshEntitiesException) {
                throw (FreshEntitiesException) e;
            }
            if (e instanceof InconsistentOntologyException) {
                throw (InconsistentOntologyException) e;
            }
            if (e instanceof ClassExpressionNotInProfileException) {
                throw (ClassExpressionNotInProfileException) e;
            }
            if (e instanceof Error) {
                throw (Error) e;
            }
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new RuntimeException(e);
        }
        return toReturn;
        // return this.next().getSubDataProperties(pe, direct);
    }

    @Override
    public NodeSet<OWLDataProperty> getSuperDataProperties(
            final OWLDataProperty pe, final boolean direct)
            throws InconsistentOntologyException, FreshEntitiesException,
            ReasonerInterruptedException, TimeOutException {
        final CacheKeys key = direct ? CacheKeys.superdatapropertiesDirect
                : CacheKeys.superdataproperties;
        Computable<Object> checker = new ComputableAllThrowables<Object>() {

            @Override
            public Object compute() {
                try {
                    return next().getSuperDataProperties(pe, direct);
                } catch (Throwable e) {
                    exception = e;
                }
                return null;
            }
        };
        NodeSet<OWLDataProperty> toReturn = (NodeSet<OWLDataProperty>) cache
                .get(key, CachedReasoner.makeKey(pe, direct), checker);
        if (checker.hasThrownException()) {
            Throwable e = checker.thrownException();
            if (e instanceof ReasonerInterruptedException) {
                throw (ReasonerInterruptedException) e;
            }
            if (e instanceof InconsistentOntologyException) {
                throw (InconsistentOntologyException) e;
            }
            if (e instanceof TimeOutException) {
                throw (TimeOutException) e;
            }
            if (e instanceof FreshEntitiesException) {
                throw (FreshEntitiesException) e;
            }
            if (e instanceof InconsistentOntologyException) {
                throw (InconsistentOntologyException) e;
            }
            if (e instanceof ClassExpressionNotInProfileException) {
                throw (ClassExpressionNotInProfileException) e;
            }
            if (e instanceof Error) {
                throw (Error) e;
            }
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new RuntimeException(e);
        }
        return toReturn;
        // return this.next().getSuperDataProperties(pe, direct);
    }

    @Override
    public Node<OWLDataProperty> getEquivalentDataProperties(
            final OWLDataProperty pe) throws InconsistentOntologyException,
            FreshEntitiesException, ReasonerInterruptedException,
            TimeOutException {
        final CacheKeys key = CacheKeys.equivdataproperties;
        Computable<Object> checker = new ComputableAllThrowables<Object>() {

            @Override
            public Object compute() {
                try {
                    return next().getEquivalentDataProperties(pe);
                } catch (Throwable e) {
                    exception = e;
                }
                return null;
            }
        };
        Node<OWLDataProperty> toReturn = (Node<OWLDataProperty>) cache.get(key,
                pe, checker);
        if (checker.hasThrownException()) {
            Throwable e = checker.thrownException();
            if (e instanceof ReasonerInterruptedException) {
                throw (ReasonerInterruptedException) e;
            }
            if (e instanceof InconsistentOntologyException) {
                throw (InconsistentOntologyException) e;
            }
            if (e instanceof TimeOutException) {
                throw (TimeOutException) e;
            }
            if (e instanceof FreshEntitiesException) {
                throw (FreshEntitiesException) e;
            }
            if (e instanceof InconsistentOntologyException) {
                throw (InconsistentOntologyException) e;
            }
            if (e instanceof ClassExpressionNotInProfileException) {
                throw (ClassExpressionNotInProfileException) e;
            }
            if (e instanceof Error) {
                throw (Error) e;
            }
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new RuntimeException(e);
        }
        return toReturn;
        // return this.next().getEquivalentDataProperties(pe);
    }

    @Override
    public NodeSet<OWLDataProperty> getDisjointDataProperties(
            final OWLDataPropertyExpression pe)
            throws InconsistentOntologyException, FreshEntitiesException,
            ReasonerInterruptedException, TimeOutException {
        final CacheKeys key = CacheKeys.disjointdataproperties;
        Computable<Object> checker = new ComputableAllThrowables<Object>() {

            @Override
            public Object compute() {
                try {
                    return next().getDisjointDataProperties(pe);
                } catch (Throwable e) {
                    exception = e;
                }
                return null;
            }
        };
        NodeSet<OWLDataProperty> toReturn = (NodeSet<OWLDataProperty>) cache
                .get(key, pe, checker);
        if (checker.hasThrownException()) {
            Throwable e = checker.thrownException();
            if (e instanceof ReasonerInterruptedException) {
                throw (ReasonerInterruptedException) e;
            }
            if (e instanceof InconsistentOntologyException) {
                throw (InconsistentOntologyException) e;
            }
            if (e instanceof TimeOutException) {
                throw (TimeOutException) e;
            }
            if (e instanceof FreshEntitiesException) {
                throw (FreshEntitiesException) e;
            }
            if (e instanceof InconsistentOntologyException) {
                throw (InconsistentOntologyException) e;
            }
            if (e instanceof ClassExpressionNotInProfileException) {
                throw (ClassExpressionNotInProfileException) e;
            }
            if (e instanceof Error) {
                throw (Error) e;
            }
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new RuntimeException(e);
        }
        return toReturn;
        // return this.next().getDisjointDataProperties(pe);
    }

    @Override
    public NodeSet<OWLClass> getDataPropertyDomains(final OWLDataProperty pe,
            final boolean direct) throws InconsistentOntologyException,
            FreshEntitiesException, ReasonerInterruptedException,
            TimeOutException {
        final CacheKeys key = direct ? CacheKeys.datapropertiesdomainsDirect
                : CacheKeys.datapropertiesdomains;
        Computable<Object> checker = new ComputableAllThrowables<Object>() {

            @Override
            public Object compute() {
                try {
                    return next().getDataPropertyDomains(pe, direct);
                } catch (Throwable e) {
                    exception = e;
                }
                return null;
            }
        };
        NodeSet<OWLClass> toReturn = (NodeSet<OWLClass>) cache.get(key,
                CachedReasoner.makeKey(pe, direct), checker);
        if (checker.hasThrownException()) {
            Throwable e = checker.thrownException();
            if (e instanceof ReasonerInterruptedException) {
                throw (ReasonerInterruptedException) e;
            }
            if (e instanceof InconsistentOntologyException) {
                throw (InconsistentOntologyException) e;
            }
            if (e instanceof TimeOutException) {
                throw (TimeOutException) e;
            }
            if (e instanceof FreshEntitiesException) {
                throw (FreshEntitiesException) e;
            }
            if (e instanceof InconsistentOntologyException) {
                throw (InconsistentOntologyException) e;
            }
            if (e instanceof ClassExpressionNotInProfileException) {
                throw (ClassExpressionNotInProfileException) e;
            }
            if (e instanceof Error) {
                throw (Error) e;
            }
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new RuntimeException(e);
        }
        return toReturn;
        // return this.next().getDataPropertyDomains(pe, direct);
    }

    @Override
    public NodeSet<OWLClass> getTypes(final OWLNamedIndividual ind,
            final boolean direct) throws InconsistentOntologyException,
            FreshEntitiesException, ReasonerInterruptedException,
            TimeOutException {
        final CacheKeys key = direct ? CacheKeys.typesDirect : CacheKeys.types;
        Computable<Object> checker = new ComputableAllThrowables<Object>() {

            @Override
            public Object compute() {
                try {
                    return next().getTypes(ind, direct);
                } catch (Throwable e) {
                    exception = e;
                }
                return null;
            }
        };
        NodeSet<OWLClass> toReturn = (NodeSet<OWLClass>) cache.get(key,
                CachedReasoner.makeKey(ind, direct), checker);
        if (checker.hasThrownException()) {
            Throwable e = checker.thrownException();
            if (e instanceof ReasonerInterruptedException) {
                throw (ReasonerInterruptedException) e;
            }
            if (e instanceof InconsistentOntologyException) {
                throw (InconsistentOntologyException) e;
            }
            if (e instanceof TimeOutException) {
                throw (TimeOutException) e;
            }
            if (e instanceof FreshEntitiesException) {
                throw (FreshEntitiesException) e;
            }
            if (e instanceof InconsistentOntologyException) {
                throw (InconsistentOntologyException) e;
            }
            if (e instanceof ClassExpressionNotInProfileException) {
                throw (ClassExpressionNotInProfileException) e;
            }
            if (e instanceof Error) {
                throw (Error) e;
            }
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new RuntimeException(e);
        }
        return toReturn;
        // return this.next().getTypes(ind, direct);
    }

    @Override
    public NodeSet<OWLNamedIndividual> getInstances(
            final OWLClassExpression ce, final boolean direct)
            throws InconsistentOntologyException,
            ClassExpressionNotInProfileException, FreshEntitiesException,
            ReasonerInterruptedException, TimeOutException {
        final CacheKeys key = direct ? CacheKeys.instancesDirect
                : CacheKeys.instances;
        Computable<Object> checker = new ComputableAllThrowables<Object>() {

            @Override
            public Object compute() {
                try {
                    return next().getInstances(ce, direct);
                } catch (Throwable e) {
                    exception = e;
                }
                return null;
            }
        };
        NodeSet<OWLNamedIndividual> toReturn = (NodeSet<OWLNamedIndividual>) cache
                .get(key, CachedReasoner.makeKey(ce, direct), checker);
        if (checker.hasThrownException()) {
            Throwable e = checker.thrownException();
            if (e instanceof ReasonerInterruptedException) {
                throw (ReasonerInterruptedException) e;
            }
            if (e instanceof InconsistentOntologyException) {
                throw (InconsistentOntologyException) e;
            }
            if (e instanceof TimeOutException) {
                throw (TimeOutException) e;
            }
            if (e instanceof FreshEntitiesException) {
                throw (FreshEntitiesException) e;
            }
            if (e instanceof InconsistentOntologyException) {
                throw (InconsistentOntologyException) e;
            }
            if (e instanceof ClassExpressionNotInProfileException) {
                throw (ClassExpressionNotInProfileException) e;
            }
            if (e instanceof Error) {
                throw (Error) e;
            }
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new RuntimeException(e);
        }
        return toReturn;
        // return this.next().getInstances(ce, direct);
    }

    @Override
    public NodeSet<OWLNamedIndividual> getObjectPropertyValues(
            final OWLNamedIndividual ind, final OWLObjectPropertyExpression pe)
            throws InconsistentOntologyException, FreshEntitiesException,
            ReasonerInterruptedException, TimeOutException {
        final CacheKeys key = CacheKeys.objectpropertiesvalues;
        Computable<Object> checker = new ComputableAllThrowables<Object>() {

            @Override
            public Object compute() {
                try {
                    return next().getObjectPropertyValues(ind, pe);
                } catch (Throwable e) {
                    exception = e;
                }
                return null;
            }
        };
        NodeSet<OWLNamedIndividual> toReturn = (NodeSet<OWLNamedIndividual>) cache
                .get(key, CachedReasoner.makeKey(ind, pe), checker);
        if (checker.hasThrownException()) {
            Throwable e = checker.thrownException();
            if (e instanceof ReasonerInterruptedException) {
                throw (ReasonerInterruptedException) e;
            }
            if (e instanceof InconsistentOntologyException) {
                throw (InconsistentOntologyException) e;
            }
            if (e instanceof TimeOutException) {
                throw (TimeOutException) e;
            }
            if (e instanceof FreshEntitiesException) {
                throw (FreshEntitiesException) e;
            }
            if (e instanceof InconsistentOntologyException) {
                throw (InconsistentOntologyException) e;
            }
            if (e instanceof ClassExpressionNotInProfileException) {
                throw (ClassExpressionNotInProfileException) e;
            }
            if (e instanceof Error) {
                throw (Error) e;
            }
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new RuntimeException(e);
        }
        return toReturn;
        // return this.next().getObjectPropertyValues(ind, pe);
    }

    @Override
    public Set<OWLLiteral> getDataPropertyValues(final OWLNamedIndividual ind,
            final OWLDataProperty pe) throws InconsistentOntologyException,
            FreshEntitiesException, ReasonerInterruptedException,
            TimeOutException {
        final CacheKeys key = CacheKeys.datapropertiesvalues;
        Computable<Object> checker = new ComputableAllThrowables<Object>() {

            @Override
            public Object compute() {
                try {
                    return next().getDataPropertyValues(ind, pe);
                } catch (Throwable e) {
                    exception = e;
                }
                return null;
            }
        };
        Set<OWLLiteral> toReturn = (Set<OWLLiteral>) cache.get(key,
                CachedReasoner.makeKey(ind, pe), checker);
        if (checker.hasThrownException()) {
            Throwable e = checker.thrownException();
            if (e instanceof ReasonerInterruptedException) {
                throw (ReasonerInterruptedException) e;
            }
            if (e instanceof InconsistentOntologyException) {
                throw (InconsistentOntologyException) e;
            }
            if (e instanceof TimeOutException) {
                throw (TimeOutException) e;
            }
            if (e instanceof FreshEntitiesException) {
                throw (FreshEntitiesException) e;
            }
            if (e instanceof InconsistentOntologyException) {
                throw (InconsistentOntologyException) e;
            }
            if (e instanceof ClassExpressionNotInProfileException) {
                throw (ClassExpressionNotInProfileException) e;
            }
            if (e instanceof Error) {
                throw (Error) e;
            }
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new RuntimeException(e);
        }
        return toReturn;
        // return this.next().getDataPropertyValues(ind, pe);
    }

    @Override
    public Node<OWLNamedIndividual> getSameIndividuals(
            final OWLNamedIndividual ind) throws InconsistentOntologyException,
            FreshEntitiesException, ReasonerInterruptedException,
            TimeOutException {
        final CacheKeys key = CacheKeys.sameindividual;
        Computable<Object> checker = new ComputableAllThrowables<Object>() {

            @Override
            public Object compute() {
                try {
                    return next().getSameIndividuals(ind);
                } catch (Throwable e) {
                    exception = e;
                }
                return null;
            }
        };
        Node<OWLNamedIndividual> toReturn = (Node<OWLNamedIndividual>) cache
                .get(key, ind, checker);
        if (checker.hasThrownException()) {
            Throwable e = checker.thrownException();
            if (e instanceof ReasonerInterruptedException) {
                throw (ReasonerInterruptedException) e;
            }
            if (e instanceof InconsistentOntologyException) {
                throw (InconsistentOntologyException) e;
            }
            if (e instanceof TimeOutException) {
                throw (TimeOutException) e;
            }
            if (e instanceof FreshEntitiesException) {
                throw (FreshEntitiesException) e;
            }
            if (e instanceof InconsistentOntologyException) {
                throw (InconsistentOntologyException) e;
            }
            if (e instanceof ClassExpressionNotInProfileException) {
                throw (ClassExpressionNotInProfileException) e;
            }
            if (e instanceof Error) {
                throw (Error) e;
            }
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new RuntimeException(e);
        }
        return toReturn;
        // return this.next().getSameIndividuals(ind);
    }

    @Override
    public NodeSet<OWLNamedIndividual> getDifferentIndividuals(
            final OWLNamedIndividual ind) throws InconsistentOntologyException,
            FreshEntitiesException, ReasonerInterruptedException,
            TimeOutException {
        final CacheKeys key = CacheKeys.diffindividual;
        Computable<Object> checker = new ComputableAllThrowables<Object>() {

            @Override
            public Object compute() {
                try {
                    return next().getDifferentIndividuals(ind);
                } catch (Throwable e) {
                    exception = e;
                }
                return null;
            }
        };
        NodeSet<OWLNamedIndividual> toReturn = (NodeSet<OWLNamedIndividual>) cache
                .get(key, ind, checker);
        if (checker.hasThrownException()) {
            Throwable e = checker.thrownException();
            if (e instanceof ReasonerInterruptedException) {
                throw (ReasonerInterruptedException) e;
            }
            if (e instanceof InconsistentOntologyException) {
                throw (InconsistentOntologyException) e;
            }
            if (e instanceof TimeOutException) {
                throw (TimeOutException) e;
            }
            if (e instanceof FreshEntitiesException) {
                throw (FreshEntitiesException) e;
            }
            if (e instanceof InconsistentOntologyException) {
                throw (InconsistentOntologyException) e;
            }
            if (e instanceof ClassExpressionNotInProfileException) {
                throw (ClassExpressionNotInProfileException) e;
            }
            if (e instanceof Error) {
                throw (Error) e;
            }
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new RuntimeException(e);
        }
        return toReturn;
        // return this.next().getDifferentIndividuals(ind);
    }

    @Override
    public long getTimeOut() {
        return next().getTimeOut();
    }

    @Override
    public FreshEntityPolicy getFreshEntityPolicy() {
        return next().getFreshEntityPolicy();
    }

    @Override
    public IndividualNodeSetPolicy getIndividualNodeSetPolicy() {
        return next().getIndividualNodeSetPolicy();
    }

    @Override
    public void dispose() {
        cache.clear();
        for (int i = 0; i < concurrentNumber; i++) {
            if (concurrentDelegates[i] != null) {
                concurrentDelegates[i].dispose();
            }
        }
    }

    @Override
    public void ontologiesChanged(List<? extends OWLOntologyChange> changes) {
        // only invalidate the caches, the changes are supposed to go to the
        // reasoner via its own listener
        cache.clear();
    }
}
