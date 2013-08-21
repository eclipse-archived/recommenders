package org.eclipse.recommenders.examples.calls;

import static org.eclipse.recommenders.utils.Recommendations.top;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.eclipse.recommenders.calls.ICallModel;
import org.eclipse.recommenders.calls.ICallModel.DefinitionKind;
import org.eclipse.recommenders.calls.SingleZipCallModelProvider;
import org.eclipse.recommenders.models.UniqueTypeName;
import org.eclipse.recommenders.utils.Constants;
import org.eclipse.recommenders.utils.Recommendation;
import org.eclipse.recommenders.utils.names.IMethodName;
import org.eclipse.recommenders.utils.names.ITypeName;

import com.google.common.collect.Sets;

/**
 * Shows how to create a call recommender that uses a single standalone zipfile containing <b>all</b> models used to
 * create recommendations.
 * <p>
 * The actual recommender is IDE dependent and has to be implemented for every IDE. A call recommenders for code
 * completion, for instance, may perform a lightweight static analysis on the AST of the active editor and then query
 * one or more models for their recommendations.
 */
public class SingleZipCallRecommender {

    private final SingleZipCallModelProvider store;

    public SingleZipCallRecommender(final File models) throws IOException {
        store = new SingleZipCallModelProvider(models);
        store.open();
    }

    public List<Recommendation<IMethodName>> computeRecommendations(final ObjectUsage query) throws Exception {
        UniqueTypeName name = new UniqueTypeName(null, query.type);
        ICallModel net = store.acquireModel(name).orNull();
        try {
            //
            // set the inputs into the net
            net.setObservedOverrideContext(query.overrideFirst);
            net.setObservedDefinitionKind(query.kind);
            net.setObservedDefiningMethod(query.definition);
            // note setObservedCalls should *always* be called.
            net.setObservedCalls(query.calls);

            //
            // query the recommender (just a few examples)
            List<Recommendation<String>> patterns = top(net.recommendPatterns(), 10, 0.01);
            List<Recommendation<IMethodName>> definitions = top(net.recommendDefinitions(), 10);
            List<Recommendation<IMethodName>> calls = top(net.recommendCalls(), 5, 0.01d);
            return calls;

        } finally {
            store.releaseModel(net);
        }
    }

    /**
     * The {@link ObjectUsage} is simple data struct that may contain the results of the on-the-fly static analysis done
     * in the IDE at completion time. It's not part of the official API!
     */
    public static class ObjectUsage {

        public static ObjectUsage newObjectUsageWithDefaults() {
            final ObjectUsage res = new ObjectUsage();
            res.type = Constants.UNKNOWN_TYPE;
            res.overrideFirst = Constants.UNKNOWN_METHOD;
            res.overrideSuper = Constants.UNKNOWN_METHOD;
            res.definition = Constants.UNKNOWN_METHOD;
            res.kind = DefinitionKind.UNKNOWN;
            return res;
        }

        public ITypeName type;
        public IMethodName overrideSuper;
        public IMethodName overrideFirst;
        public Set<IMethodName> calls = Sets.newHashSet();
        public DefinitionKind kind;
        public IMethodName definition;
    }
}
