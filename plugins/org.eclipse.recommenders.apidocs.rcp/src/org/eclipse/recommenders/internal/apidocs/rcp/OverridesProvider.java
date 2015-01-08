/**
 * Copyright (c) 2011 Stefan Henss.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stefan Henss - initial API and implementation.
 *    Olav Lenz - externalize Strings.
 */
package org.eclipse.recommenders.internal.apidocs.rcp;

import static com.google.common.collect.Collections2.filter;
import static com.google.common.collect.Lists.newLinkedList;
import static java.text.MessageFormat.format;
import static java.util.Arrays.asList;
import static org.eclipse.recommenders.internal.apidocs.rcp.ApidocsViewUtils.*;
import static org.eclipse.recommenders.utils.Bags.newHashMultiset;
import static org.eclipse.swt.SWT.COLOR_INFO_FOREGROUND;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;

import javax.inject.Inject;

import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.recommenders.apidocs.ClassOverrideDirectives;
import org.eclipse.recommenders.apidocs.ClassOverridePatterns;
import org.eclipse.recommenders.apidocs.MethodPattern;
import org.eclipse.recommenders.apidocs.OverrideDirectivesModelProvider;
import org.eclipse.recommenders.apidocs.OverridePatternsModelProvider;
import org.eclipse.recommenders.apidocs.rcp.ApidocProvider;
import org.eclipse.recommenders.apidocs.rcp.JavaSelectionSubscriber;
import org.eclipse.recommenders.models.IInputStreamTransformer;
import org.eclipse.recommenders.models.IModelIndex;
import org.eclipse.recommenders.models.IModelRepository;
import org.eclipse.recommenders.models.rcp.IProjectCoordinateProvider;
import org.eclipse.recommenders.models.rcp.ModelEvents.ModelIndexOpenedEvent;
import org.eclipse.recommenders.rcp.JavaElementResolver;
import org.eclipse.recommenders.rcp.JavaElementSelectionEvent;
import org.eclipse.recommenders.utils.names.IMethodName;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.collect.Multiset;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

public final class OverridesProvider extends ApidocProvider {

    private final JavaElementResolver resolver;
    private final EventBus workspaceBus;
    private final IProjectCoordinateProvider pcProvider;
    private final OverrideDirectivesModelProvider dStore;
    private final OverridePatternsModelProvider pStore;

    @Inject
    public OverridesProvider(IProjectCoordinateProvider pcProvider, JavaElementResolver resolver,
            EventBus workspaceBus, IModelRepository repository, IModelIndex index,
            Map<String, IInputStreamTransformer> transformers) {
        this.pcProvider = pcProvider;
        this.resolver = resolver;
        this.workspaceBus = workspaceBus;
        this.pStore = new OverridePatternsModelProvider(repository, index, transformers);
        this.dStore = new OverrideDirectivesModelProvider(repository, index, transformers);
    }

    @Subscribe
    public void onEvent(ModelIndexOpenedEvent e) throws IOException {
        pStore.close();
        pStore.open();

        dStore.close();
        dStore.open();
    }

    @JavaSelectionSubscriber
    public void onTypeRootSelection(final ITypeRoot root, final JavaElementSelectionEvent event, final Composite parent)
            throws ExecutionException {
        final IType type = root.findPrimaryType();
        if (type != null) {
            onTypeSelection(type, event, parent);
        }
    }

    @JavaSelectionSubscriber
    public void onMethodSelection(final IMethod method, final JavaElementSelectionEvent event, final Composite parent)
            throws ExecutionException {
        onTypeSelection(method.getDeclaringType(), event, parent);
    }

    @JavaSelectionSubscriber
    public void onVariableSelection(ILocalVariable var, JavaElementSelectionEvent event, Composite parent)
            throws ExecutionException {
        IType type = ApidocsViewUtils.findType(var).orNull();
        if (type != null) {
            onTypeSelection(type, event, parent);
        }
    }

    @JavaSelectionSubscriber
    public void onVariableSelection(IField var, JavaElementSelectionEvent event, Composite parent)
            throws ExecutionException, JavaModelException {
        IType type = ApidocsViewUtils.findType(var).orNull();
        if (type != null) {
            onTypeSelection(type, event, parent);
        }
    }

    @JavaSelectionSubscriber
    public void onTypeSelection(final IType type, final JavaElementSelectionEvent event, final Composite parent)
            throws ExecutionException {
        renderClassOverrideDirectives(type, parent);
        renderClassOverridesPatterns(type, parent);
    }

    private boolean renderClassOverrideDirectives(final IType type, final Composite parent) throws ExecutionException {
        ClassOverrideDirectives model = dStore.acquireModel(pcProvider.toUniqueName(type).orNull()).orNull();
        try {
            if (model == null || model.getOverrides() == null) {
                return false;
            }
            runSyncInUiThread(new TypeOverrideDirectivesRenderer(type, model, parent));
        } finally {
            dStore.releaseModel(model);
        }
        return true;
    }

    private boolean renderClassOverridesPatterns(final IType type, final Composite parent) throws ExecutionException {
        ClassOverridePatterns opt = pStore.acquireModel(pcProvider.toUniqueName(type).orNull()).orNull();
        try {
            if (opt != null) {
                runSyncInUiThread(new OverridePatternsRenderer(type, opt, parent));
            }
        } finally {
            pStore.releaseModel(opt);
        }
        return true;
    }

    // ========================================================================
    // TODO: Review the renderer code is redundant and needs refactoring after
    // all providers have been written to
    // identify more common parts.

    private class TypeOverrideDirectivesRenderer implements Runnable {

        private final IType type;
        private final ClassOverrideDirectives directive;
        private final Composite parent;
        private Composite container;

        public TypeOverrideDirectivesRenderer(final IType type, final ClassOverrideDirectives directive,
                final Composite parent) {
            this.type = type;
            this.directive = directive;
            this.parent = parent;
        }

        @Override
        public void run() {
            createContainer();
            addHeader();
            addDirectives();
        }

        private void createContainer() {
            container = new Composite(parent, SWT.NONE);
            setInfoBackgroundColor(container);
            container.setLayout(new GridLayout());
        }

        private void addHeader() {
            final String message = format(Messages.PROVIDER_INTRO_OVERRIDE_STATISTICS,
                    directive.getNumberOfSubclasses(), type.getElementName());
            Label label = new Label(container, SWT.NONE);
            label.setText(message);
            setInfoForegroundColor(label);
            setInfoBackgroundColor(label);
        }

        private void addDirectives() {
            final int numberOfSubclasses = directive.getNumberOfSubclasses();
            final Multiset<IMethodName> b = newHashMultiset(directive.getOverrides());
            renderMethodDirectivesBlock(container, b, numberOfSubclasses, workspaceBus, resolver,
                    Messages.TABLE_CELL_RELATION_OVERRIDE);
        }
    }

    private class OverridePatternsRenderer implements Runnable {

        private final Composite parent;
        private Composite container;

        double totalNumberOfExamples;
        private List<MethodPattern> patterns;

        public OverridePatternsRenderer(final IType type, final ClassOverridePatterns directive, final Composite parent) {
            this.parent = parent;
            setPatterns(directive);
            computeTotalNumberOfExamples();
            filterInfrequentPatterns();
            sortPatterns();
        }

        private void setPatterns(final ClassOverridePatterns patterns) {
            this.patterns = asList(patterns.getPatterns());
        }

        private void filterInfrequentPatterns() {
            patterns = newLinkedList(filter(patterns, new Predicate<MethodPattern>() {
                @Override
                public boolean apply(final MethodPattern input) {
                    final int numberOfObservations = input.getNumberOfObservations();
                    return numberOfObservations / totalNumberOfExamples > 0.1;
                }
            }));
        }

        private void sortPatterns() {
            Collections.sort(patterns, new Comparator<MethodPattern>() {

                @Override
                public int compare(final MethodPattern o1, final MethodPattern o2) {
                    return o2.getNumberOfObservations() - o1.getNumberOfObservations();
                }
            });
        }

        private void computeTotalNumberOfExamples() {
            for (final MethodPattern pattern : patterns) {
                totalNumberOfExamples += pattern.getNumberOfObservations();
            }
        }

        @Override
        public void run() {
            createContainer();
            addHeader();

            int i = 1;
            for (final MethodPattern pattern : patterns) {
                addDirectives(pattern, i++);
            }
        }

        private void createContainer() {
            container = new Composite(parent, SWT.NONE);
            setInfoBackgroundColor(container);
            container.setLayout(new GridLayout());
            container.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
        }

        private void addHeader() {
            new Label(container, SWT.None);
            final String message = format(Messages.PROVIDER_INTRO_OVERRIDE_PATTERNS);
            createLabel(container, message, true);
        }

        private void addDirectives(final org.eclipse.recommenders.apidocs.MethodPattern pattern, final int index) {

            final double patternPercentage = pattern.getNumberOfObservations() / totalNumberOfExamples;
            final String text = format(Messages.TABLE_HEADER_OVERRIDE_PATTERN, index, patternPercentage,
                    pattern.getNumberOfObservations());
            createLabel(container, text, true, false, SWT.COLOR_DARK_GRAY, true);
            final Composite group = createGridComposite(container, 1, 0, 0, 0, 0);
            final List<Entry<IMethodName, Double>> s = Lists.newLinkedList(pattern.getMethods().entrySet());
            Collections.sort(s, new Comparator<Entry<IMethodName, Double>>() {

                @Override
                public int compare(final Entry<IMethodName, Double> o1, final Entry<IMethodName, Double> o2) {
                    // return o2.getValue().compareTo(o1.getValue());
                    return o1.getKey().getName().compareTo(o2.getKey().getName());
                }
            });

            final Table table = new Table(group, SWT.NONE | SWT.HIDE_SELECTION);
            table.setBackground(createColor(SWT.COLOR_INFO_BACKGROUND));
            table.setLayoutData(GridDataFactory.fillDefaults().indent(10, 0).create());
            final TableColumn column1 = new TableColumn(table, SWT.NONE);
            final TableColumn column2 = new TableColumn(table, SWT.NONE);
            final TableColumn column3 = new TableColumn(table, SWT.NONE);
            final TableColumn column4 = new TableColumn(table, SWT.NONE);

            for (final Entry<IMethodName, Double> entry : s) {
                final double percentage = entry.getValue();
                final String phraseText = percentageToRecommendationPhrase((int) Math.rint(percentage * 100));
                final String stats = format(Messages.TABLE_CELL_SUFFIX_PERCENTAGE, percentage);

                final Link bar = createMethodLink(table, entry.getKey(), resolver, workspaceBus);
                final TableItem item = new TableItem(table, SWT.NONE);
                item.setText(new String[] { phraseText, Messages.TABLE_CELL_RELATION_OVERRIDE, bar.getText(), stats });
                item.setFont(0, JFaceResources.getBannerFont());
                item.setForeground(createColor(COLOR_INFO_FOREGROUND));
                final TableEditor editor = new TableEditor(table);
                editor.grabHorizontal = editor.grabVertical = true;
                editor.setEditor(bar, item, 2);

            }
            column1.pack();
            column2.pack();
            column3.pack();
            column4.pack();

            new Label(container, SWT.SEPARATOR | SWT.HORIZONTAL);
        }
    }
}
