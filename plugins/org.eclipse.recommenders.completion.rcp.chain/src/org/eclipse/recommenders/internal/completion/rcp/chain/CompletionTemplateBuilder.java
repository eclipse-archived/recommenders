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

import static org.eclipse.jdt.ui.JavaElementLabels.M_PARAMETER_NAMES;
import static org.eclipse.jdt.ui.JavaElementLabels.M_PARAMETER_TYPES;
import static org.eclipse.jdt.ui.JavaElementLabels.getElementLabel;

import java.util.List;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
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
import org.eclipse.swt.graphics.Image;

// TODO: field access may need to be qualified using "this." This is completely ignored ATM
/**
 * Creates the templates for a give call chain.
 */
public class CompletionTemplateBuilder {

    private HashBag<String> varNames;
    private StringBuilder sb;

    public TemplateProposal create(final List<MemberEdge> chain, final JavaContentAssistInvocationContext context) {

        final String title = createCompletionTitle(chain);
        final String body = createCompletionBody(chain);

        final Template template = new Template(title, chain.size() + " elements", "java", body, false);
        return createTemplateProposal(template, context);
    }

    private static String createCompletionTitle(final List<MemberEdge> chain) {
        final StringBuilder sb = new StringBuilder();
        for (final MemberEdge edge : chain) {
            switch (edge.getEdgeType()) {
            case FIELD:
            case LOCAL_VARIABLE:
                final IJavaElement var = edge.getEdgeElement();
                sb.append(var.getElementName());
                break;
            case METHOD:
                final IMethod method = edge.getEdgeElement();
                final String label = getElementLabel(method, M_PARAMETER_NAMES | M_PARAMETER_TYPES);
                sb.append(label);
            default:
                break;
            }
            for (int i = edge.getDimension(); i-- > 0;) {
                sb.append("[]");
            }
            sb.append(".");
        }
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }

    private String createCompletionBody(final List<MemberEdge> chain) {
        varNames = HashBag.newHashBag();
        sb = new StringBuilder();
        for (final MemberEdge edge : chain) {
            switch (edge.getEdgeType()) {
            case FIELD:
            case LOCAL_VARIABLE:
                final IJavaElement var = edge.getEdgeElement();
                appendIdentifier(var);
                break;
            case METHOD:
                final IMethod method = edge.getEdgeElement();
                appendIdentifier(method);
                appendParameters(method);
            default:
                break;
            }
            appendArrayDimensions(edge.getDimension());
            sb.append(".");
        }
        deleteLastChar();
        return sb.toString();
    }

    private StringBuilder appendIdentifier(final IJavaElement var) {
        return sb.append(var.getElementName());
    }

    private void appendParameters(final IMethod method) {
        sb.append("(");
        try {
            final String[] parameterNames = method.getParameterNames();
            for (final String paramName : parameterNames) {
                appendTemplateVariable(paramName);
                sb.append(", ");
            }
            if (parameterNames.length > 0) {
                deleteLastChar();
                deleteLastChar();
            }
        } catch (final JavaModelException e) {
            // we can not handle that. log it.
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        sb.append(")");
    }

    private void appendTemplateVariable(final String varname) {
        varNames.add(varname);
        sb.append("${").append(varname);
        final int count = varNames.count(varname);
        if (count > 1) {
            sb.append(count);
        }
        sb.append("}");
    }

    private StringBuilder deleteLastChar() {
        return sb.deleteCharAt(sb.length() - 1);
    }

    private void appendArrayDimensions(final int dimension) {
        for (int i = dimension; i-- > 0;) {
            addArrayDimension();
        }
    }

    private void addArrayDimension() {
        sb.append("[");
        appendTemplateVariable("index");
        sb.append("]");
    }

    static TemplateProposal createTemplateProposal(final Template template,
            final JavaContentAssistInvocationContext contentAssistContext) {
        final DocumentTemplateContext javaTemplateContext = createJavaContext(contentAssistContext);
        final TemplateProposal proposal = new TemplateProposal(template, javaTemplateContext, new Region(
                javaTemplateContext.getCompletionOffset(), javaTemplateContext.getCompletionLength()),
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
