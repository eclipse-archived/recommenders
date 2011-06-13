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

import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.recommenders.commons.selection.IJavaElementSelection;
import org.eclipse.recommenders.commons.utils.Names;
import org.eclipse.recommenders.commons.utils.names.IMethodName;
import org.eclipse.recommenders.internal.rcp.extdoc.providers.swt.TemplateEditDialog;
import org.eclipse.recommenders.rcp.extdoc.AbstractProviderComposite;
import org.eclipse.recommenders.rcp.extdoc.SwtFactory;
import org.eclipse.recommenders.rcp.extdoc.features.FeaturesComposite;
import org.eclipse.recommenders.server.extdoc.SubclassingServer;
import org.eclipse.recommenders.server.extdoc.types.ClassOverrideDirectives;
import org.eclipse.recommenders.server.extdoc.types.ClassSelfcallDirectives;
import org.eclipse.recommenders.server.extdoc.types.MethodSelfcallDirectives;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public final class SubclassingProvider extends AbstractProviderComposite {

    private final SubclassingServer server = new SubclassingServer();

    private Composite parentComposite;
    private Composite composite;

    @Override
    protected Control createContentControl(final Composite parent) {
        parentComposite = SwtFactory.createGridComposite(parent, 1, 0, 8, 0, 0);
        return parentComposite;
    }

    @Override
    protected void updateContent(final IJavaElementSelection selection) {
        final IJavaElement element = selection.getJavaElement();
        if (element instanceof IType) {
            displayContentForType((IType) element);
        } else if (element instanceof IMethod) {
            displayContentForMethod((IMethod) element);
        } else {
            printUnavailable();
        }
        parentComposite.layout(true);
    }

    private void displayContentForType(final IType type) {
        final ClassOverrideDirectives overrides = server.getClassOverrideDirective(type);
        initComposite();
        if (overrides == null) {
            displayNoneAvailable(type.getElementName());
        } else {
            final String elementName = type.getElementName();
            final int subclasses = overrides.getNumberOfSubclasses();
            Composite line = SwtFactory.createGridComposite(composite, 2, 10, 0, 0, 0);
            String lineText = "Based on "
                    + subclasses
                    + " direct subclasses of "
                    + type.getElementName()
                    + " we created the following statistics. Subclassers may consider to override the following methods.";
            final StyledText styledText = SwtFactory.createStyledText(line, lineText);
            SwtFactory.createStyleRange(styledText, 31 + getLength(subclasses), elementName.length(), SWT.NORMAL,
                    false, true);
            FeaturesComposite.create(line, type, elementName, this, server, new TemplateEditDialog(getShell()));

            displayDirectives(overrides.getOverrides(), "override", subclasses);

            final ClassSelfcallDirectives calls = server.getClassSelfcallDirective(type);
            if (calls != null) {
                line = SwtFactory.createGridComposite(composite, 2, 10, 0, 0, 0);
                lineText = "Subclassers may consider to call the following methods to configure instances of this class via self calls.";
                SwtFactory.createLabel(line, lineText, false, false, false);
                FeaturesComposite.create(line, type, elementName, this, server, new TemplateEditDialog(getShell()));
                displayDirectives(calls.getCalls(), "call", calls.getNumberOfSubclasse());
            }
        }
    }

    private void displayContentForMethod(final IMethod method) {
        final MethodSelfcallDirectives selfcalls = server.getMethodSelfcallDirective(method);
        initComposite();
        if (selfcalls == null) {
            displayNoneAvailable(method.getElementName());
        } else {
            String text = "Subclasses of "
                    + method.getParent().getElementName()
                    + " typically should overrride this method (92%). When overriding subclasses may call the super implementation (25%).";
            StyledText styledText = SwtFactory.createStyledText(composite, text);
            final int length = method.getParent().getElementName().length();
            SwtFactory.createStyleRange(styledText, 14, length, SWT.NORMAL, false, true);
            SwtFactory.createStyleRange(styledText, length + 25, 6, SWT.BOLD, false, false);
            SwtFactory.createStyleRange(styledText, length + 55, 3, SWT.NORMAL, true, false);
            SwtFactory.createStyleRange(styledText, length + 88, 3, SWT.BOLD, false, false);
            SwtFactory.createStyleRange(styledText, length + 101, 5, SWT.NORMAL, false, true);
            SwtFactory.createStyleRange(styledText, length + 123, 3, SWT.NORMAL, true, false);

            final int definitions = selfcalls.getNumberOfDefinitions();
            text = "Based on " + definitions + " implementations of " + method.getElementName()
                    + " we created the following statistics. Implementors may consider to call the following methods.";
            styledText = SwtFactory.createStyledText(composite, text);
            SwtFactory.createStyleRange(styledText, 29 + getLength(definitions), method.getElementName().length(),
                    SWT.NORMAL, false, true);

            displayDirectives(selfcalls.getCalls(), "call", definitions);
            FeaturesComposite.create(composite, method, method.getElementName(), this, server, new TemplateEditDialog(
                    getShell()));
        }
    }

    private void displayDirectives(final Map<IMethodName, Integer> directives, final String actionKeyword,
            final int definitions) {
        final Composite directiveComposite = SwtFactory.createGridComposite(composite, 4, 12, 3, 15, 0);
        for (final Entry<IMethodName, Integer> directive : directives.entrySet()) {
            final int percent = (int) Math.round(directive.getValue() * 100.0 / definitions);
            final String label;
            if (percent >= 95) {
                label = "must";
            } else if (percent >= 65) {
                label = "should";
            } else if (percent >= 25) {
                label = "may";
            } else if (percent >= 10) {
                label = "rarely";
            } else {
                label = "should not";
            }

            SwtFactory.createSquare(directiveComposite);
            SwtFactory.createLabel(directiveComposite, label, true, false, false);
            SwtFactory.createLabel(directiveComposite,
                    actionKeyword + " " + Names.vm2srcSimpleMethod(directive.getKey()), false, false, true);
            final StyledText txt = SwtFactory.createStyledText(directiveComposite, "(" + directive.getValue()
                    + " times - " + percent + "%)");
            SwtFactory.createStyleRange(txt, 10 + getLength(directive.getValue()), getLength(percent) + 1, SWT.NORMAL,
                    true, false);
        }
    }

    private void displayNoneAvailable(final String elementName) {
        final StyledText styledText = SwtFactory.createStyledText(composite, "There are no directives available for "
                + elementName + ".");
        SwtFactory.createStyleRange(styledText, 38, elementName.length(), SWT.NORMAL, false, true);
    }

    private void printUnavailable() {
        initComposite();
        SwtFactory.createStyledText(composite, "Subclassing directives are only available for Java types and methods.");
    }

    private int getLength(final int number) {
        return String.valueOf(number).length();
    }

    private void initComposite() {
        if (composite != null) {
            composite.dispose();
        }
        composite = SwtFactory.createGridComposite(parentComposite, 1, 0, 12, 0, 0);
    }

}
