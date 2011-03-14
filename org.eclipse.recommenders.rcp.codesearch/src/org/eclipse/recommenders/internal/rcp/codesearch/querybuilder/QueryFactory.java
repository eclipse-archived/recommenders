package org.eclipse.recommenders.internal.rcp.codesearch.querybuilder;

import org.eclipse.recommenders.rcp.codecompletion.IIntelligentCompletionContext;

public interface QueryFactory {

    QueryBuilder createDefinitionQuery(IIntelligentCompletionContext ctx);

    QueryBuilder createObjectUsageQuery(IIntelligentCompletionContext ctx);

    QueryBuilder createSimilarMethodsQuery(IIntelligentCompletionContext ctx);

    QueryBuilder createSimilarClassesQuery(IIntelligentCompletionContext ctx);
}
