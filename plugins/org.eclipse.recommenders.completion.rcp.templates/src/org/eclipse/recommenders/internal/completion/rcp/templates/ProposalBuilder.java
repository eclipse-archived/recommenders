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
package org.eclipse.recommenders.internal.completion.rcp.templates;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.corext.template.java.AbstractJavaContextType;
import org.eclipse.jdt.internal.corext.template.java.JavaContext;
import org.eclipse.jdt.internal.corext.template.java.JavaContextType;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.recommenders.completion.rcp.IRecommendersCompletionContext;
import org.eclipse.recommenders.utils.Names;
import org.eclipse.recommenders.utils.names.IMethodName;
import org.eclipse.recommenders.utils.names.ITypeName;
import org.eclipse.recommenders.utils.names.VmTypeName;
import org.eclipse.recommenders.utils.rcp.JavaElementResolver;
import org.eclipse.swt.graphics.Image;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

@SuppressWarnings("restriction")
public class ProposalBuilder {

    private final List<PatternRecommendation> patterns = Lists.newLinkedList();
    private final Image icon;
    private final IRecommendersCompletionContext rCtx;
    private JavaContext documentContext;
    private final JavaElementResolver resolver;
    private String variableName;

    public ProposalBuilder(final Image icon, final IRecommendersCompletionContext rCtx,
            final JavaElementResolver resolver, final String variableName) {
        this.icon = icon;
        this.rCtx = rCtx;
        this.resolver = resolver;
        this.variableName = variableName;
        createDocumentContext();
    }

    private void createDocumentContext() {
        final JavaPlugin plugin = JavaPlugin.getDefault();
        if (plugin != null) {
            final AbstractJavaContextType type = (AbstractJavaContextType) plugin.getTemplateContextRegistry()
                    .getContextType(JavaContextType.ID_ALL);

            final JavaContentAssistInvocationContext javaContext = rCtx.getJavaContext();
            final Region region = rCtx.getReplacementRange();
            final int offset = region.getOffset() - variableName.length();
            final int length = Math.max(0, region.getLength() - 1);
            documentContext = new JavaContext(type, javaContext.getDocument(), offset, length,
                    rCtx.getCompilationUnit());
            documentContext.setForceEvaluation(true);
        } else {
            throw new IllegalStateException("No default JavaPlugin found.");
        }
    }

    public void addPattern(final PatternRecommendation pattern) {
        patterns.add(pattern);
    }

    public List<JavaTemplateProposal> createProposals() {
        final List<JavaTemplateProposal> result = Lists.newLinkedList();
        for (final PatternRecommendation pattern : patterns) {
            try {
                result.add(new JavaTemplateProposal(createTemplate(pattern), documentContext, icon, pattern));
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    private Template createTemplate(final PatternRecommendation pattern) {
        final String code = createTemplateCode(pattern);
        return new Template(pattern.getName(), pattern.getType().getClassName(), "java", code, false);
    }

    private String createTemplateCode(final PatternRecommendation pattern) {
        final TemplateCodeBuilder builder = new TemplateCodeBuilder();
        for (final IMethodName method : pattern.getMethods()) {
            builder.addMethodCall(method);
        }
        return builder.build();
    }

    private class TemplateCodeBuilder {
        private final StringBuilder builder = new StringBuilder();
        private final String lineSeparator = System.getProperty("line.separator");
        private final Map<String, Integer> argumentCounter = Maps.newHashMap();

        public void addMethodCall(final IMethodName method) {
            appendVariableDeclaration(method);
            appendInvocationPrefix(method);
            appendMethodCall(method);
            builder.append(";");
            builder.append(lineSeparator);
        }

        private void appendVariableDeclaration(final IMethodName method) {
            if (method.isInit()) {
                builder.append(String.format("${constructedType:newType(%s)}",
                        getTypeIdentifier(method.getDeclaringType())));
                builder.append(" ");
                builder.append(getNewVariableNameFromMethod(method));
                builder.append(" = ");
            } else if (!method.isVoid()) {
                if (method.getReturnType().isPrimitiveType()) {
                    builder.append(getTypeIdentifier(method.getReturnType()));
                } else {
                    final String typeIdentifier = getTypeIdentifier(method.getReturnType());
                    final String returnTypeTemplateVariable = typeIdentifier.replaceAll("\\W", "_");
                    builder.append(String.format(
                            "${%s:newType(%s)}%s",
                            returnTypeTemplateVariable,
                            typeIdentifier,
                            method.getReturnType().isArrayType() ? StringUtils.repeat("[]", method.getReturnType()
                                    .getArrayDimensions()) : ""));
                }
                builder.append(" ");
                builder.append(getNewVariableNameFromMethod(method));
                builder.append(" = ");
            }
        }

        private void appendInvocationPrefix(final IMethodName method) {
            if (method.isInit()) {
                builder.append("new ");
            } else if (!isImplicitThis() && !variableName.isEmpty()) {
                builder.append(variableName);
                builder.append(".");
            }
        }

        private void appendMethodCall(final IMethodName method) {
            if (method.isInit()) {
                builder.append("${constructedType}");
            } else {
                builder.append(method.getName());
            }
            appendParameters(method);
        }

        private void appendParameters(final IMethodName method) {
            final Optional<IMethod> jdtMethod = resolver.toJdtMethod(method);
            if (!jdtMethod.isPresent()) {
                throw new IllegalStateException("Method could not be resolved: " + method);
            }
            try {
                builder.append("(");
                final String[] parameterNames = jdtMethod.get().getParameterNames();
                final ITypeName[] parameterTypes = method.getParameterTypes();
                for (int i = 0; i < parameterNames.length; ++i) {
                    appendParameter(parameterNames[i], parameterTypes[i]);
                    if (i + 1 < parameterNames.length) {
                        builder.append(", ");
                    }
                }
                builder.append(")");
            } catch (final JavaModelException e) {
                throw new IllegalStateException(e);
            }
        }

        private void appendParameter(final String parameterName, final ITypeName parameterType) {
            String appendix = "";
            // TODO: Appendix for more types, add array support.
            if (parameterType.isDeclaredType()) {
                if (!parameterType.getIdentifier().startsWith("Ljava")) {
                    final String typeName = Names.vm2srcTypeName(parameterType.getIdentifier());
                    appendix = String.format(":var(%s)", typeName);
                }
            } else if (parameterType == VmTypeName.BOOLEAN) {
                appendix = ":link(false, true)";
            } else if (parameterType == VmTypeName.INT || parameterType == VmTypeName.DOUBLE
                    || parameterType == VmTypeName.FLOAT || parameterType == VmTypeName.LONG
                    || parameterType == VmTypeName.SHORT) {
                appendix = ":link(0)";
            }
            builder.append(String.format("${%s%s}", getParameterName(parameterName), appendix));
        }

        private String getParameterName(final String parameterName) {
            final String name = parameterName.length() <= 5 && parameterName.startsWith("arg") ? "arg" : parameterName;
            if (argumentCounter.containsKey(name)) {
                final Integer counter = argumentCounter.get(name);
                argumentCounter.put(name, counter + 1);
                return String.format("%s%s", name, counter + 1);
            } else {
                argumentCounter.put(name, 1);
            }
            return name;
        }

        private String getNewVariableNameFromMethod(final IMethodName method) {
            if (method.isInit()) {
                variableName = "${unconstructed}";
                return String.format("${unconstructed:newName(%s)}", getTypeIdentifier(method.getDeclaringType()));
            } else if (!method.isVoid()) {
                if (method.getName().startsWith("get")) {
                    return StringUtils.uncapitalize(method.getName().substring(3));
                } else {
                    return StringUtils.uncapitalize(method.getReturnType().getClassName());
                }
            } else {
                throw new IllegalStateException();
            }
        }

        public String build() {
            return String.format("%s${cursor}", builder.toString());
        }

        private boolean isImplicitThis() {
            return (!rCtx.getReceiverType().isPresent()) && rCtx.getReceiverName().isEmpty();
        }

        private String getTypeIdentifier(final ITypeName typeName) {
            return typeName.isPrimitiveType() ? Names.vm2srcSimpleTypeName(typeName) : typeName.getIdentifier()
                    .replace('/', '.').substring(1);
        }
    }

}
