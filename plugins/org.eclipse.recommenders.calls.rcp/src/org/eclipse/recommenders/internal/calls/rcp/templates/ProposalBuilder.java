/**
 * Copyright (c) 2010 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Johannes Lerch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.calls.rcp.templates;

import java.util.HashSet;
import java.util.List;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.codeassist.complete.CompletionOnSingleNameReference;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.corext.template.java.AbstractJavaContextType;
import org.eclipse.jdt.internal.corext.template.java.JavaContext;
import org.eclipse.jdt.internal.corext.template.java.JavaContextType;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.recommenders.completion.rcp.IRecommendersCompletionContext;
import org.eclipse.recommenders.rcp.JavaElementResolver;
import org.eclipse.recommenders.utils.Recommendations;
import org.eclipse.recommenders.utils.names.IMethodName;
import org.eclipse.recommenders.utils.names.ITypeName;
import org.eclipse.swt.graphics.Image;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

@SuppressWarnings("restriction")
public class ProposalBuilder {

    private Logger log = LoggerFactory.getLogger(getClass());
    private List<PatternRecommendation> patterns = Lists.newLinkedList();
    private Image icon;
    private IRecommendersCompletionContext rCtx;
    private JavaContext documentContext;
    private JavaElementResolver resolver;
    private String variableName;

    public ProposalBuilder(Image icon, IRecommendersCompletionContext rCtx, JavaElementResolver resolver,
            String variableName) {
        this.icon = icon;
        this.rCtx = rCtx;
        this.resolver = resolver;
        this.variableName = variableName;
        createDocumentContext();
    }

    private void createDocumentContext() {
        JavaPlugin plugin = JavaPlugin.getDefault();
        if (plugin != null) {
            AbstractJavaContextType type = (AbstractJavaContextType) plugin.getTemplateContextRegistry()
                    .getContextType(JavaContextType.ID_ALL);

            JavaContentAssistInvocationContext javaContext = rCtx.getJavaContext();
            Region region = rCtx.getReplacementRange();

            int offset = 0;
            ASTNode node = rCtx.getCompletionNode().orNull();
            if (node instanceof CompletionOnSingleNameReference) {
                offset = region.getOffset() - rCtx.getPrefix().length();
            } else {
                offset = region.getOffset() - variableName.length();
            }

            int length = Math.max(0, region.getLength() - 1);
            documentContext = new JavaContext(type, javaContext.getDocument(), offset, length,
                    rCtx.getCompilationUnit());
            documentContext.setForceEvaluation(true);
        } else {
            throw new IllegalStateException("No default JavaPlugin found."); //$NON-NLS-1$
        }
    }

    public void addPattern(PatternRecommendation pattern) {
        patterns.add(pattern);
    }

    public List<JavaTemplateProposal> createProposals() {
        // 1. sort the most likely patterns on top:
        patterns = Recommendations.sortByRelevance(patterns);
        // 2. get rid of duplicates: yes, this happens!
        HashSet<PatternRecommendation> noDuplicates = Sets.newHashSet(patterns);

        List<JavaTemplateProposal> result = Lists.newLinkedList();
        for (PatternRecommendation pattern : noDuplicates) {
            try {
                result.add(new JavaTemplateProposal(createTemplate(pattern), documentContext, icon, pattern));
            } catch (Exception e) {
                log.warn("Failed to create proposals", e); //$NON-NLS-1$
            }
        }
        return result;
    }

    private Template createTemplate(PatternRecommendation pattern) {
        String code = createTemplateCode(pattern);
        return new Template(pattern.getName(), pattern.getType().getClassName(), "java", code, false); //$NON-NLS-1$
    }

    private String createTemplateCode(PatternRecommendation pattern) {
        TemplateBuilder tb = new TemplateBuilder();

        String receiverName = variableName;
        for (IMethodName method : pattern.getProposal()) {
            if (method.isInit()) {
                receiverName = tb.appendCtor(method, lookupArgumentNames(method));
                tb.nl();
            } else {
                tb.appendCall(method, receiverName, lookupArgumentNames(method));
                tb.nl();
            }
        }
        tb.cursor();
        return tb.toString();
    }

    private String[] lookupArgumentNames(IMethodName method) {
        IMethod jdtMethod = resolver.toJdtMethod(method).orNull();
        try {
            if (jdtMethod != null) {
                return jdtMethod.getParameterNames();
            }
        } catch (JavaModelException e) {
            log.warn("Failed to lookup method arguments names for {}", jdtMethod, e); //$NON-NLS-1$
        }

        ITypeName[] parameterTypes = method.getParameterTypes();
        String[] parameterNames = new String[parameterTypes.length];
        for (int i = 0; i < parameterNames.length; i++) {
            parameterNames[i] = "arg" + i; //$NON-NLS-1$
        }
        return parameterNames;
    }
}
