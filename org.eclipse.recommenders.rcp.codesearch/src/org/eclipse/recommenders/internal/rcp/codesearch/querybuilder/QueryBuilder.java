package org.eclipse.recommenders.internal.rcp.codesearch.querybuilder;

import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.recommenders.commons.codesearch.SnippetSummary;

public interface QueryBuilder {

    SnippetSummary createQuery(MethodDeclaration declaration);

    SnippetSummary createQuery(TypeDeclaration declaration);
}
