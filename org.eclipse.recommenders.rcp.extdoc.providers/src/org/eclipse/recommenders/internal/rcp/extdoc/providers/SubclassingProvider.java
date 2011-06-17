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
import org.eclipse.recommenders.commons.selection.JavaElementLocation;
import org.eclipse.recommenders.commons.utils.Names;
import org.eclipse.recommenders.commons.utils.names.IMethodName;
import org.eclipse.recommenders.internal.rcp.extdoc.providers.swt.TemplateEditDialog;
import org.eclipse.recommenders.internal.rcp.extdoc.providers.swt.TextAndFeaturesLine;
import org.eclipse.recommenders.rcp.extdoc.AbstractProviderComposite;
import org.eclipse.recommenders.rcp.extdoc.SwtFactory;
import org.eclipse.recommenders.server.extdoc.SubclassingServer;
import org.eclipse.recommenders.server.extdoc.types.ClassOverrideDirectives;
import org.eclipse.recommenders.server.extdoc.types.ClassSelfcallDirectives;
import org.eclipse.recommenders.server.extdoc.types.MethodSelfcallDirectives;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import com.google.inject.Inject;

public final class SubclassingProvider extends AbstractProviderComposite {

    private final SubclassingServer server;

    private Composite parentComposite;
    private Composite composite;

    @Inject
    public SubclassingProvider(final SubclassingServer server) {
        this.server = server;
    }

    @Override
    protected Control createContentControl(final Composite parent) {
        parentComposite = SwtFactory.createGridComposite(parent, 1, 0, 8, 0, 0);
        return parentComposite;
    }

    @Override
    public boolean isAvailableForLocation(final JavaElementLocation location) {
        return location == JavaElementLocation.METHOD_BODY || location == JavaElementLocation.METHOD_DECLARATION
                || JavaElementLocation.isInTypeDeclaration(location);
    }

    @Override
    public boolean selectionChanged(final IJavaElementSelection selection) {
        final IJavaElement element = selection.getJavaElement();
        if (element instanceof IType) {
            return displayContentForType((IType) element);
        } else if (element instanceof IMethod) {
            return displayContentForMethod((IMethod) element);
        }
        return false;
    }

    private boolean displayContentForType(final IType type) {
        final ClassOverrideDirectives overrides = server.getClassOverrideDirective(type);
        initComposite();
        if (overrides == null) {
            return false;
        }
        final String elementName = type.getElementName();
        final int subclasses = overrides.getNumberOfSubclasses();

        String text = "Based on " + subclasses + " direct subclasses of " + elementName
                + " we created the following statistics. Subclassers may consider to override the following methods.";
        final TextAndFeaturesLine line = new TextAndFeaturesLine(composite, text, type, elementName, this, server,
                new TemplateEditDialog(getShell()));
        line.createStyleRange(31 + getLength(subclasses), elementName.length(), SWT.NORMAL, false, true);

        displayDirectives(overrides.getOverrides(), "override", subclasses);

        final ClassSelfcallDirectives calls = server.getClassSelfcallDirective(type);
        if (calls != null) {
            text = "Subclassers may consider to call the following methods to configure instances of this class via self calls.";
            new TextAndFeaturesLine(composite, text, type, elementName, this, server,
                    new TemplateEditDialog(getShell()));
            displayDirectives(calls.getCalls(), "call", calls.getNumberOfSubclasse());
        }
        parentComposite.layout(true);
        return true;
    }

    private boolean displayContentForMethod(final IMethod method) {
        final MethodSelfcallDirectives selfcalls = server.getMethodSelfcallDirective(method);
        initComposite();
        if (selfcalls == null) {
            return false;
        }
        String text = "Subclasses of "
                + method.getParent().getElementName()
                + " typically should overrride this method (92%). When overriding subclasses may call the super implementation (25%).";
        final StyledText styledText = SwtFactory.createStyledText(composite, text);
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
        final TextAndFeaturesLine line = new TextAndFeaturesLine(composite, text, method, method.getElementName(),
                this, server, new TemplateEditDialog(getShell()));
        line.createStyleRange(29 + getLength(definitions), method.getElementName().length(), SWT.NORMAL, false, true);

        displayDirectives(selfcalls.getCalls(), "call", definitions);

        parentComposite.layout(true);
        return true;
    }

    private void displayDirectives(final Map<IMethodName, Integer> directives, final String actionKeyword,
            final int definitions) {
        final Composite directiveComposite = SwtFactory.createGridComposite(composite, 4, 12, 2, 15, 0);
        final Map<IMethodName, Integer> orderedMap = new TreeMap<IMethodName, Integer>(new Comparator<IMethodName>() {
            @Override
            public int compare(final IMethodName o1, final IMethodName o2) {
                return directives.get(o2).compareTo(directives.get(o1));
            }
        });
        orderedMap.putAll(directives);
        for (final Entry<IMethodName, Integer> directive : orderedMap.entrySet()) {
            final int percent = (int) Math.round(directive.getValue() * 100.0 / definitions);

            SwtFactory.createSquare(directiveComposite);
            SwtFactory.createLabel(directiveComposite, getLabel(percent), true, false, SWT.COLOR_BLACK);
            SwtFactory.createLabel(directiveComposite,
                    actionKeyword + " " + Names.vm2srcSimpleMethod(directive.getKey()), false, true, SWT.COLOR_BLACK);
            final StyledText txt = SwtFactory.createStyledText(directiveComposite, "(" + directive.getValue()
                    + " times - " + percent + "%)");
            SwtFactory.createStyleRange(txt, 10 + getLength(directive.getValue()), getLength(percent) + 1, SWT.NORMAL,
                    true, false);
        }
    }

    private String getLabel(final int percent) {
        if (percent >= 95) {
            return "must";
        } else if (percent >= 65) {
            return "should";
        } else if (percent >= 25) {
            return "may";
        } else if (percent >= 10) {
            return "rarely";
        }
        return "should not";
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
