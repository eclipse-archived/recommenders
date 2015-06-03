/**
 * Copyright (c) 2014 Codetrails GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Johannes Dorn - initial API and implementation.
 */
package org.eclipse.recommenders.internal.snipmatch.rcp;

import static org.eclipse.jdt.ui.text.IJavaPartitions.*;

import java.util.Set;

import org.eclipse.jdt.core.CompletionContext;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.recommenders.coordinates.ProjectCoordinate;
import org.eclipse.recommenders.internal.snipmatch.rcp.l10n.LogMessages;
import org.eclipse.recommenders.snipmatch.Location;
import org.eclipse.recommenders.snipmatch.SearchContext;
import org.eclipse.recommenders.utils.Logs;

import com.google.common.annotations.VisibleForTesting;

public class JavaEditorSearchContext extends SearchContext {

    private final JavaContentAssistInvocationContext invocationContext;

    public JavaEditorSearchContext(String searchText, JavaContentAssistInvocationContext invocationContext,
            Set<ProjectCoordinate> projectCoordinates) {
        super(searchText, getLocation(invocationContext), projectCoordinates);
        this.invocationContext = invocationContext;
    }

    private static Location getLocation(JavaContentAssistInvocationContext context) {
        try {
            String partition = TextUtilities.getContentType(context.getDocument(), JAVA_PARTITIONING,
                    context.getInvocationOffset(), true);
            return getLocation(context, partition);
        } catch (BadLocationException e) {
            Logs.log(LogMessages.ERROR_CANNOT_COMPUTE_LOCATION, e);
        }
        return Location.FILE;
    }

    @VisibleForTesting
    static Location getLocation(JavaContentAssistInvocationContext context, String partition) {
        if (partition.equals(JAVA_DOC)) {
            return Location.JAVADOC;
        }
        if (partition.equals(JAVA_SINGLE_LINE_COMMENT) || partition.equals(JAVA_MULTI_LINE_COMMENT)) {
            return Location.FILE;
        }
        CompletionContext coreContext = context.getCoreContext();
        if (coreContext != null) {
            int tokenLocation = coreContext.getTokenLocation();
            if ((tokenLocation & CompletionContext.TL_MEMBER_START) != 0) {
                return Location.JAVA_TYPE_MEMBERS;
            } else if ((tokenLocation & CompletionContext.TL_STATEMENT_START) != 0) {
                return Location.JAVA_STATEMENTS;
            }
            return Location.UNKNOWN;
        }
        return Location.FILE;
    }

    public JavaContentAssistInvocationContext getInvocationContext() {
        return invocationContext;
    }
}
