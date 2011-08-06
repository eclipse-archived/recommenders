/**
 * Copyright (c) 2011 Stefan Henss.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stefan Henss - initial API and implementation.
 */
package org.eclipse.recommenders.internal.rcp.extdoc.providers;

import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.recommenders.commons.selection.IJavaElementSelection;
import org.eclipse.recommenders.commons.utils.Names;
import org.eclipse.recommenders.commons.utils.names.IMethodName;
import org.eclipse.recommenders.commons.utils.names.ITypeName;
import org.eclipse.recommenders.internal.rcp.extdoc.providers.swt.TextAndFeaturesLine;
import org.eclipse.recommenders.internal.rcp.extdoc.providers.utils.ElementResolver;
import org.eclipse.recommenders.rcp.extdoc.AbstractTitledProvider;
import org.eclipse.recommenders.rcp.extdoc.ProviderUiJob;
import org.eclipse.recommenders.rcp.extdoc.SwtFactory;
import org.eclipse.recommenders.rcp.extdoc.features.CommunityFeatures;
import org.eclipse.recommenders.rcp.utils.JdtUtils;
import org.eclipse.recommenders.server.extdoc.SubclassingServer;
import org.eclipse.recommenders.server.extdoc.types.ClassOverrideDirectives;
import org.eclipse.recommenders.server.extdoc.types.ClassSelfcallDirectives;
import org.eclipse.recommenders.server.extdoc.types.MethodSelfcallDirectives;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Composite;

import com.google.inject.Inject;

public final class SubclassingProvider extends AbstractTitledProvider {

    private final SubclassingServer server;

    @Inject
    SubclassingProvider(final SubclassingServer server) {
        this.server = server;
    }

    @Override
    protected Composite createContentComposite(final Composite parent) {
        return SwtFactory.createGridComposite(parent, 1, 0, 12, 0, 0);
    }

    @Override
    public boolean updateSelection(final IJavaElementSelection selection, final Composite composite) {
        final IJavaElement element = selection.getJavaElement();
        if (element instanceof IType) {
            return displayContentForType(ElementResolver.toRecType((IType) element), composite);
        } else if (element instanceof IMethod) {
            final IMethod firstDeclaration = JdtUtils.findFirstDeclaration((IMethod) element);
            return displayContentForMethod(ElementResolver.toRecMethod((IMethod) element),
                    ElementResolver.toRecMethod(firstDeclaration), composite);
        }
        return false;
    }

    private boolean displayContentForType(final ITypeName type, final Composite composite) {
        final ClassOverrideDirectives overrides = server.getClassOverrideDirectives(type);
        if (overrides == null) {
            return false;
        }
        final String elementName = type.getClassName();
        final int subclasses = overrides.getNumberOfSubclasses();

        final String text = "Based on " + subclasses + " direct subclasses of " + elementName
                + " we created the following statistics. Subclassers may consider to override the following methods.";
        final String text2 = "Subclassers may consider to call the following methods to configure instances of this class via self calls.";
        final ClassSelfcallDirectives calls = server.getClassSelfcallDirectives(type);
        final CommunityFeatures features = CommunityFeatures.create(type, null, this, server);

        new ProviderUiJob() {
            @Override
            public Composite run() {
                if (!composite.isDisposed()) {
                    disposeChildren(composite);
                    final TextAndFeaturesLine line = new TextAndFeaturesLine(composite, text, features);
                    line.createStyleRange(31 + getLength(subclasses), elementName.length(), SWT.NORMAL, false, true);
                    displayDirectives(overrides.getOverrides(), "override", subclasses, composite);
                    if (calls != null) {
                        new TextAndFeaturesLine(composite, text2, features);
                        displayDirectives(calls.getCalls(), "call", calls.getNumberOfSubclasses(), composite);
                    }
                    features.loadCommentsComposite(composite);
                }
                return composite;
            }
        }.schedule();

        return true;
    }

    private boolean displayContentForMethod(final IMethodName method, final IMethodName firstDeclaration,
            final Composite composite) {
        // TODO first is not correct in all cases. this needs to be fixed soon
        // after the demo
        final MethodSelfcallDirectives selfcalls = server.getMethodSelfcallDirectives(firstDeclaration);
        if (selfcalls == null || method == null) {
            return false;
        }
        final int definitions = selfcalls.getNumberOfDefinitions();
        final CommunityFeatures features = CommunityFeatures.create(method, null, this, server);

        new ProviderUiJob() {
            @Override
            public Composite run() {
                if (!composite.isDisposed()) {
                    disposeChildren(composite);
                    // displayMethodOverrideInformation(firstDeclaration.getDeclaringType().getClassName(),
                    // definitions, 25);
                    final String text = String
                            .format("Based on %d implementations of %s we created the following statistics. Implementors may consider to call the following methods.",
                                    definitions, method.getName());
                    final TextAndFeaturesLine line = new TextAndFeaturesLine(composite, text, features);
                    line.createStyleRange(29 + getLength(definitions), method.getName().length(), SWT.NORMAL, false,
                            true);
                    displayDirectives(selfcalls.getCalls(), "call", definitions, composite);
                    features.loadCommentsComposite(composite);
                }
                return composite;
            }
        }.schedule();

        return true;
    }

    private static void displayMethodOverrideInformation(final String subclassedTypeName, final int methodOverrides,
            final int superCalls, final Composite composite) {
        final String text = "Subclasses of " + subclassedTypeName + " typically should override this method ("
                + methodOverrides + " times). When overriding subclasses may call the super implementation ("
                + superCalls + " times).";
        final StyledText styledText = SwtFactory.createStyledText(composite, text);
        final int length = subclassedTypeName.length();
        final int length2 = getLength(methodOverrides);
        SwtFactory.createStyleRange(styledText, 14, length, SWT.NORMAL, false, true);
        SwtFactory.createStyleRange(styledText, length + 25, 6, SWT.BOLD, false, false);
        SwtFactory.createStyleRange(styledText, length + 90 + length2, 3, SWT.BOLD, false, false);
        SwtFactory.createStyleRange(styledText, length + 103 + length2, 5, SWT.NORMAL, false, true);
    }

    private static void displayDirectives(final Map<IMethodName, Integer> directives, final String actionKeyword,
            final int definitions, final Composite composite) {
        final Composite directiveComposite = SwtFactory.createGridComposite(composite, 4, 12, 2, 15, 0);
        for (final Entry<IMethodName, Integer> directive : orderDirectives(directives).entrySet()) {
            final int percent = (int) Math.round(directive.getValue().doubleValue() * 100.0 / definitions);

            SwtFactory.createSquare(directiveComposite);
            SwtFactory.createLabel(directiveComposite, getLabel(percent), true, false, SWT.COLOR_BLACK);
            SwtFactory.createLabel(directiveComposite,
                    actionKeyword + " " + Names.vm2srcSimpleMethod(directive.getKey()), false, true, SWT.COLOR_BLACK);
            final StyledText text = SwtFactory.createStyledText(directiveComposite, "(" + directive.getValue()
                    + " times - " + percent + "%)");
            SwtFactory.createStyleRange(text, 10 + getLength(directive.getValue()), getLength(percent) + 1, SWT.NORMAL,
                    true, false);
        }
    }

    private static Map<IMethodName, Integer> orderDirectives(final Map<IMethodName, Integer> directives) {
        final Map<IMethodName, Integer> orderedMap = new TreeMap<IMethodName, Integer>(new Comparator<IMethodName>() {
            @Override
            public int compare(final IMethodName directive1, final IMethodName directive2) {
                return directives.get(directive2).compareTo(directives.get(directive1));
            }
        });
        orderedMap.putAll(directives);
        return orderedMap;
    }

    private static String getLabel(final int percent) {
        if (percent >= 95) {
            return "must";
        } else if (percent >= 65) {
            return "should";
        } else if (percent >= 25) {
            return "may";
        } else if (percent >= 10) {
            return "rarely";
        }
        // TODO: Some other label for probability < 10%?
        return "rarely";
    }

    private static int getLength(final int number) {
        return String.valueOf(number).length();
    }

}
