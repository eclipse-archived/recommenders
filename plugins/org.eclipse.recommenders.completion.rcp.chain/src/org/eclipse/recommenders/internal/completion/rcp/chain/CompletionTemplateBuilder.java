/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.completion.rcp.chain;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.VariableBinding;
import org.eclipse.jdt.internal.corext.template.java.JavaContext;
import org.eclipse.jdt.internal.corext.template.java.JavaContextType;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.internal.ui.text.template.contentassist.TemplateProposal;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.templates.ContextTypeRegistry;
import org.eclipse.jface.text.templates.DocumentTemplateContext;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.recommenders.utils.HashBag;
import org.eclipse.recommenders.utils.rcp.internal.RecommendersUtilsPlugin;
import org.eclipse.swt.graphics.Image;

/**
 * Creates the templates for a given call chain.
 */
@SuppressWarnings("restriction")
public final class CompletionTemplateBuilder {

    private CompletionTemplateBuilder() {
    }

    public static TemplateProposal create(final List<ChainElement> chain, final int expectedDimension,
            final JavaContentAssistInvocationContext context) {
        final String title = createChainCode(chain, true, 0);
        final String body = createChainCode(chain, false, expectedDimension);

        final Template template = new Template(title, chain.size() + " elements", "java", body, false);
        return createTemplateProposal(template, context);
    }

    private static String createChainCode(final List<ChainElement> chain, final boolean createAsTitle,
            final int expectedDimension) {
        final HashBag<String> varNames = HashBag.newHashBag();
        final StringBuilder sb = new StringBuilder(64);
        for (final ChainElement edge : chain) {
            switch (edge.getElementType()) {
            case FIELD:
            case LOCAL_VARIABLE:
                appendVariableString(edge, sb);
                break;
            case METHOD:
                final MethodBinding method = edge.getElementBinding();
                if (createAsTitle) {
                    sb.append(method.readableName());
                } else {
                    sb.append(method.selector);
                    appendParameters(sb, method, varNames);
                }
                break;
            default:
                RecommendersUtilsPlugin.logWarning("Can't handle %s's element type.", edge);
            }
            final boolean appendVariables = !createAsTitle;
            appendArrayDimensions(sb, edge.getReturnTypeDimension(), expectedDimension, appendVariables, varNames);
            sb.append(".");
        }
        deleteLastChar(sb);
        return sb.toString();
    }

    private static void appendVariableString(final ChainElement edge, final StringBuilder sb) {
        if (edge.requiresThisForQualification() && sb.length() == 0) {
            sb.append("this.");
        }
        sb.append(((VariableBinding) edge.getElementBinding()).name);
    }

    private static void appendParameters(final StringBuilder sb, final MethodBinding method,
            final HashBag<String> varNames) {
        sb.append("(");
        for (final TypeBinding parameter : method.parameters) {
            String parameterName = StringUtils.uncapitalize(String.valueOf(parameter.shortReadableName()));
            parameterName = StringUtils.substringBefore(parameterName, "<");
            appendTemplateVariable(sb, parameterName, varNames);
            sb.append(", ");
        }
        if (method.parameters.length > 0) {
            deleteLastChar(sb);
            deleteLastChar(sb);
        }
        sb.append(")");
    }

    private static void appendTemplateVariable(final StringBuilder sb, final String varname,
            final HashBag<String> varNames) {
        varNames.add(varname);
        sb.append("${").append(varname);
        final int count = varNames.count(varname);
        if (count > 1) {
            sb.append(count);
        }
        sb.append("}");
    }

    private static void appendArrayDimensions(final StringBuilder sb, final int dimension, final int expectedDimension,
            final boolean appendVariables, final HashBag<String> varNames) {
        for (int i = dimension; i-- > expectedDimension;) {
            sb.append("[");
            if (appendVariables) {
                appendTemplateVariable(sb, "i", varNames);
            }
            sb.append("]");
        }
    }

    private static StringBuilder deleteLastChar(final StringBuilder sb) {
        return sb.deleteCharAt(sb.length() - 1);
    }

    static TemplateProposal createTemplateProposal(final Template template,
            final JavaContentAssistInvocationContext contentAssistContext) {
        final DocumentTemplateContext templateContext = createJavaContext(contentAssistContext);
        final Region region = new Region(templateContext.getCompletionOffset(), templateContext.getCompletionLength());
        final TemplateProposal proposal = new TemplateProposal(template, templateContext, region,
                getChainCompletionIcon());
        return proposal;
    }

    static JavaContext createJavaContext(final JavaContentAssistInvocationContext contentAssistContext) {
        final ContextTypeRegistry templateContextRegistry = JavaPlugin.getDefault().getTemplateContextRegistry();
        final TemplateContextType templateContextType = templateContextRegistry.getContextType(JavaContextType.ID_ALL);
        final JavaContext javaTemplateContext = new JavaContext(templateContextType,
                contentAssistContext.getDocument(), contentAssistContext.getInvocationOffset(), contentAssistContext
                        .getCoreContext().getToken().length, contentAssistContext.getCompilationUnit());
        javaTemplateContext.setForceEvaluation(true);
        return javaTemplateContext;
    }

    static Image getChainCompletionIcon() {
        return JavaPlugin.getImageDescriptorRegistry().get(JavaPluginImages.DESC_MISC_PUBLIC);
    }
}
