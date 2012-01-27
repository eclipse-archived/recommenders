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
package org.eclipse.recommenders.internal.extdoc.rcp.providers.subclassing;

import static com.google.common.base.Optional.fromNullable;
import static com.google.common.collect.Collections2.filter;
import static com.google.common.collect.Lists.newLinkedList;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.eclipse.recommenders.internal.extdoc.rcp.ui.ExtdocUtils.createGridComposite;
import static org.eclipse.recommenders.internal.extdoc.rcp.ui.ExtdocUtils.createLabel;
import static org.eclipse.recommenders.internal.extdoc.rcp.ui.ExtdocUtils.percentageToRecommendationPhrase;
import static org.eclipse.recommenders.rcp.events.JavaSelectionEvent.JavaSelectionLocation.METHOD_DECLARATION;
import static org.eclipse.recommenders.utils.TreeBag.newTreeBag;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;

import javax.inject.Inject;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.recommenders.extdoc.ClassOverrideDirectives;
import org.eclipse.recommenders.extdoc.ClassOverridePatterns;
import org.eclipse.recommenders.extdoc.MethodPattern;
import org.eclipse.recommenders.extdoc.rcp.providers.ExtdocProvider;
import org.eclipse.recommenders.extdoc.rcp.providers.JavaSelectionSubscriber;
import org.eclipse.recommenders.internal.extdoc.rcp.providers.ExtdocResourceProxy;
import org.eclipse.recommenders.internal.extdoc.rcp.ui.ExtdocUtils;
import org.eclipse.recommenders.rcp.events.JavaSelectionEvent;
import org.eclipse.recommenders.utils.Names;
import org.eclipse.recommenders.utils.TreeBag;
import org.eclipse.recommenders.utils.names.IMethodName;
import org.eclipse.recommenders.utils.names.ITypeName;
import org.eclipse.recommenders.utils.rcp.JavaElementResolver;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.collect.Lists;
import com.google.common.eventbus.EventBus;

public final class OverridesProvider extends ExtdocProvider {

    private final ExtdocResourceProxy proxy;
    private final JavaElementResolver resolver;
    private final EventBus workspaceBus;
    private final Cache<ITypeName, Optional<ClassOverrideDirectives>> cache1 = CacheBuilder.newBuilder()
            .maximumSize(20).concurrencyLevel(1).build(new CacheLoader<ITypeName, Optional<ClassOverrideDirectives>>() {

                @Override
                public Optional<ClassOverrideDirectives> load(final ITypeName typeName) throws Exception {
                    return fromNullable(proxy.findClassOverrideDirectives(typeName));
                }
            });
    private final Cache<ITypeName, Optional<ClassOverridePatterns>> cache2 = CacheBuilder.newBuilder().maximumSize(20)
            .concurrencyLevel(1).build(new CacheLoader<ITypeName, Optional<ClassOverridePatterns>>() {

                @Override
                public Optional<ClassOverridePatterns> load(final ITypeName typeName) throws Exception {
                    return fromNullable(proxy.findClassOverridePatterns(typeName));
                }
            });

    @Inject
    public OverridesProvider(final ExtdocResourceProxy proxy, final JavaElementResolver resolver,
            final EventBus workspaceBus) {
        this.proxy = proxy;
        this.resolver = resolver;
        this.workspaceBus = workspaceBus;

    }

    @JavaSelectionSubscriber
    public Status onTypeRootSelection(final ITypeRoot root, final JavaSelectionEvent event, final Composite parent)
            throws ExecutionException {
        final IType type = root.findPrimaryType();
        if (type != null) {
            return onTypeSelection(type, event, parent);
        }
        return Status.NOT_AVAILABLE;
    }

    @JavaSelectionSubscriber
    public Status onTypeSelection(final IType type, final JavaSelectionEvent event, final Composite parent)
            throws ExecutionException {
        boolean hasData = false;
        hasData |= renderClassOverrideDirectives(type, parent);
        hasData |= renderClassOverridesPatterns(type, parent);
        return hasData ? Status.OK : Status.NOT_AVAILABLE;
    }

    private boolean renderClassOverrideDirectives(final IType type, final Composite parent) throws ExecutionException {
        final ITypeName typeName = resolver.toRecType(type);
        final Optional<ClassOverrideDirectives> opt = cache1.get(typeName);
        if (opt.isPresent()) {
            runSyncInUiThread(new TypeOverrideDirectivesRenderer(type, opt.get(), parent));
            return true;
        }
        return false;
    }

    private boolean renderClassOverridesPatterns(final IType type, final Composite parent) throws ExecutionException {
        final ITypeName typeName = resolver.toRecType(type);
        final Optional<ClassOverridePatterns> opt = cache2.get(typeName);
        if (opt.isPresent()) {
            runSyncInUiThread(new OverridePatternsRenderer(type, opt.get(), parent));
            return true;
        }
        return false;
    }

    Link createMethodLink(final Composite parent, final IMethodName method) {
        final String text = "<a>" + Names.vm2srcSimpleMethod(method) + "</a>";
        final String tooltip = Names.vm2srcQualifiedMethod(method);

        final Link link = new Link(parent, SWT.NONE);
        link.setText(text);
        link.setBackground(ExtdocUtils.createColor(SWT.COLOR_INFO_BACKGROUND));
        link.setToolTipText(tooltip);
        link.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                final IMethod jdtMethod = resolver.toJdtMethod(method);
                if (jdtMethod != null) {
                    final JavaSelectionEvent event = new JavaSelectionEvent(jdtMethod, METHOD_DECLARATION);
                    workspaceBus.post(event);
                } else {
                    link.setEnabled(false);
                }
            }
        });
        return link;
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
            container = new Composite(parent, SWT.NO_BACKGROUND);
            container.setLayout(new GridLayout());
        }

        private void addHeader() {
            final String message = format("Based on %d direct subclasses of %s, we created the following statistics:",
                    directive.getNumberOfSubclasses(), type.getElementName());
            new Label(container, SWT.NONE).setText(message);
        }

        private void addDirectives() {
            final int numberOfSubclasses = directive.getNumberOfSubclasses();
            final TreeBag<IMethodName> b = newTreeBag(directive.getOverrides());

            final Composite group = createGridComposite(container, 4, 0, 0, 0, 0);
            for (final IMethodName method : b.elementsOrderedByFrequency()) {

                final int frequency = b.count(method);
                final int percentage = (int) Math.round(frequency * 100.0d / numberOfSubclasses);

                createLabel(group, "   " + percentageToRecommendationPhrase(percentage), true, false, SWT.COLOR_BLACK,
                        true);
                createLabel(group, "override", false);
                createMethodLink(group, method);
                createLabel(group, format(" -   (%d %% - %d times)", percentage, frequency), true);
            }
        }

    }

    private class OverridePatternsRenderer implements Runnable {

        private final IType type;
        private final ClassOverridePatterns directive;
        private final Composite parent;
        private Composite container;

        double totalNumberOfExamples;
        private List<MethodPattern> patterns;

        public OverridePatternsRenderer(final IType type, final ClassOverridePatterns directive, final Composite parent) {
            this.type = type;
            this.directive = directive;
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
                    return (numberOfObservations / totalNumberOfExamples) > 0.1;
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
            container = new Composite(parent, SWT.NO_BACKGROUND);
            container.setLayout(new GridLayout());
        }

        private void addHeader() {
            new Label(container, SWT.None);
            final String message = format(
                    "Based on the above examples, we identified the following patterns\nhow this class is typically extended:",
                    directive.getPatterns().length, type.getElementName());
            createLabel(container, message, true);
        }

        private void addDirectives(final MethodPattern pattern, final int index) {

            final int patternPercentage = (int) Math.rint(100 * pattern.getNumberOfObservations()
                    / totalNumberOfExamples);
            final String text = format("Pattern #%d (%d%% - %d examples):", index, patternPercentage,
                    pattern.getNumberOfObservations());
            createLabel(container, text, true, false, SWT.COLOR_DARK_GRAY, true);
            final Composite group = createGridComposite(container, 4, 0, 0, 0, 0);
            final List<Entry<IMethodName, Double>> s = Lists.newLinkedList(pattern.getMethods().entrySet());
            Collections.sort(s, new Comparator<Entry<IMethodName, Double>>() {

                @Override
                public int compare(final Entry<IMethodName, Double> o1, final Entry<IMethodName, Double> o2) {
                    // return o2.getValue().compareTo(o1.getValue());
                    return o1.getKey().getName().compareTo(o2.getKey().getName());
                }
            });

            for (final Entry<IMethodName, Double> entry : s) {
                final int percentage = (int) Math.rint(entry.getValue() * 100);

                createLabel(group, "   " + percentageToRecommendationPhrase(percentage), true, false, SWT.COLOR_BLACK,
                        true);
                createLabel(group, "override", false);
                createMethodLink(group, entry.getKey());
                createLabel(group, format(" -   (%d %%)", percentage), true);
            }
            new Label(container, SWT.SEPARATOR | SWT.HORIZONTAL);

        }
    }
}
