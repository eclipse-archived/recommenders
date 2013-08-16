/**
 * Copyright (c) 2010, 2013 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.sandbox.rcp;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.SystemUtils.LINE_SEPARATOR;
import static org.eclipse.recommenders.sandbox.rcp.CompletionEvent.ProposalKind.toKind;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.ui.text.java.AbstractJavaCompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.recommenders.completion.rcp.IRecommendersCompletionContext;
import org.eclipse.recommenders.completion.rcp.processable.IProcessableProposal;
import org.eclipse.recommenders.completion.rcp.processable.SessionProcessor;
import org.eclipse.recommenders.rcp.IRcpService;
import org.eclipse.recommenders.rcp.JavaElementResolver;
import org.eclipse.recommenders.sandbox.rcp.CompletionEvent.ProposalKind;
import org.eclipse.recommenders.utils.gson.FieldNameTypeAdapter;
import org.eclipse.recommenders.utils.gson.FileTypeAdapter;
import org.eclipse.recommenders.utils.gson.ISO8601DateParser;
import org.eclipse.recommenders.utils.gson.MethodNameTypeAdapter;
import org.eclipse.recommenders.utils.gson.MultimapTypeAdapter;
import org.eclipse.recommenders.utils.gson.TypeNameTypeAdapter;
import org.eclipse.recommenders.utils.gson.UuidTypeAdapter;
import org.eclipse.recommenders.utils.names.IFieldName;
import org.eclipse.recommenders.utils.names.IMethodName;
import org.eclipse.recommenders.utils.names.ITypeName;
import org.eclipse.recommenders.utils.names.VmFieldName;
import org.eclipse.recommenders.utils.names.VmMethodName;
import org.eclipse.recommenders.utils.names.VmTypeName;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Charsets;
import com.google.common.collect.Multimap;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class StatisticsSessionProcessor extends SessionProcessor implements IRcpService {

    public static File getCompletionLogLocation() {
        Bundle bundle = FrameworkUtil.getBundle(StatisticsSessionProcessor.class);
        IPath location = Platform.getStateLocation(bundle);
        return new File(location.toFile(), "completion-stats.txt");
    }

    public static Gson getCompletionLogSerializer() {
        final GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(VmMethodName.class, new MethodNameTypeAdapter());
        builder.registerTypeAdapter(IMethodName.class, new MethodNameTypeAdapter());
        builder.registerTypeAdapter(VmTypeName.class, new TypeNameTypeAdapter());
        builder.registerTypeAdapter(ITypeName.class, new TypeNameTypeAdapter());
        builder.registerTypeAdapter(VmFieldName.class, new FieldNameTypeAdapter());
        builder.registerTypeAdapter(IFieldName.class, new FieldNameTypeAdapter());
        builder.registerTypeAdapter(File.class, new FileTypeAdapter());
        builder.registerTypeAdapter(UUID.class, new UuidTypeAdapter());
        builder.registerTypeAdapter(Date.class, new ISO8601DateParser());
        builder.registerTypeAdapter(Multimap.class, new MultimapTypeAdapter());

        return builder.create();
    }

    private static final ASTNode NULL = new NullAstNode();

    private static class NullAstNode extends ASTNode {

        @Override
        public StringBuffer print(int indent, StringBuffer output) {
            return output;
        }
    }

    JavaElementResolver resolver;
    CompletionEvent event;
    File dest;

    @Inject
    public StatisticsSessionProcessor(JavaElementResolver resolver) {
        this.resolver = resolver;
        dest = initializeDestination();
    }

    @VisibleForTesting
    protected File initializeDestination() {
        return getCompletionLogLocation();
    }

    @Override
    public boolean startSession(IRecommendersCompletionContext ctx) {
        flushCurrentEvent();
        event = new CompletionEvent();
        event.sessionStarted = System.currentTimeMillis();
        event.completionKind = ctx.getCompletionNode().or(NULL).getClass().getSimpleName();
        event.completionParentKind = ctx.getCompletionNodeParent().or(NULL).getClass().getSimpleName();
        IType receiverType = ctx.getReceiverType().orNull();
        if (receiverType != null) {
            event.receiverType = resolver.toRecType(receiverType);
        }
        event.prefix = ctx.getPrefix();
        return true;
    }

    private void flushCurrentEvent() {
        if (event != null) {
            try {
                final Gson gson = getCompletionLogSerializer();
                Files.append(gson.toJson(event) + LINE_SEPARATOR, dest, Charsets.UTF_8);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                event = null;
            }
        }
    }

    @Override
    public void endSession(List<ICompletionProposal> proposals) {
        event.numberOfProposals = proposals.size();
    }

    @Override
    public void applied(ICompletionProposal proposal) {
        event.sessionEnded = System.currentTimeMillis();

        if (proposal instanceof IProcessableProposal) {
            IProcessableProposal p = (IProcessableProposal) proposal;
            CompletionProposal coreProposal = p.getCoreProposal().orNull();
            if (coreProposal != null) {
                event.prefix = p.getPrefix();
                event.applied = toKind(coreProposal.getKind());
                event.completion = new String(coreProposal.getCompletion());
                if (ProposalKind.UNKNOWN == event.applied && isEmpty(event.completion)) {
                    event.error = coreProposal.toString();
                }
            }
        } else if (proposal instanceof AbstractJavaCompletionProposal) {
            event.applied = ProposalKind.UNKNOWN;
        }
        flushCurrentEvent();
    }

    @Override
    public void aboutToClose() {
        // about to close may or may not be called before apply....
        if (event != null) {
            event.sessionEnded = System.currentTimeMillis();
        }
    }
}
