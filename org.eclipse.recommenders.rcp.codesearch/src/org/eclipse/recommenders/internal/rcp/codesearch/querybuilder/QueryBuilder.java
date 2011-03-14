package org.eclipse.recommenders.internal.rcp.codesearch.querybuilder;

import org.eclipse.recommenders.commons.codesearch.SnippetSummary;

public interface QueryBuilder {

    SnippetSummary createQuery();
}
