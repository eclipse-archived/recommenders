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

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IType;
import org.eclipse.recommenders.commons.selection.IJavaElementSelection;
import org.eclipse.recommenders.commons.selection.JavaElementLocation;
import org.eclipse.recommenders.commons.utils.Names;
import org.eclipse.recommenders.commons.utils.names.IMethodName;
import org.eclipse.recommenders.internal.rcp.extdoc.providers.swt.TextAndFeaturesLine;
import org.eclipse.recommenders.rcp.extdoc.AbstractLocationSensitiveProviderComposite;
import org.eclipse.recommenders.rcp.extdoc.SwtFactory;
import org.eclipse.recommenders.server.extdoc.SubclassingServer;
import org.eclipse.recommenders.server.extdoc.types.ClassOverridePatterns;
import org.eclipse.recommenders.server.extdoc.types.MethodPattern;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.progress.UIJob;

import com.google.common.collect.Lists;
import com.google.inject.Inject;

public final class SubclassingTemplatesProvider extends AbstractLocationSensitiveProviderComposite {

    private final SubclassingServer server;

    private Composite composite;

    @Inject
    public SubclassingTemplatesProvider(final SubclassingServer server) {
        this.server = server;
    }

    @Override
    protected Control createContentControl(final Composite parent) {
        composite = SwtFactory.createGridComposite(parent, 1, 0, 11, 0, 0);
        return composite;
    }

    @Override
    public boolean isAvailableForLocation(final JavaElementLocation location) {
        return location != JavaElementLocation.PACKAGE_DECLARATION;
    }

    @Override
    protected boolean updateExtendsDeclarationSelection(final IJavaElementSelection selection, final IType type) {
        return printProposals(type);
    }

    private boolean printProposals(final IType type) {
        final ClassOverridePatterns directive = server.getClassOverridePatterns(type);
        if (directive == null) {
            return false;
        }
        final MethodPattern[] patterns = getPatternsSortedByFrequency(directive);
        final int numberOfSubclasses = computeTotalNumberOfSubclasses(patterns);

        final String text = String
                .format("By analysing %d subclasses subclasses that override at least one method, the following subclassing patterns have been identified.",
                        numberOfSubclasses);
        final SubclassingTemplatesProvider provider = this;

        new UIJob("Updating Subclassing Templates Provider") {
            @Override
            public IStatus runInUIThread(final IProgressMonitor monitor) {
                if (!composite.isDisposed()) {
                    disposeChildren(composite);
                    SwtFactory.createStyledText(composite, text);

                    final Composite templates = SwtFactory.createGridComposite(composite, 1, 0, 12, 0, 0);

                    for (int i = 0; i < Math.min(patterns.length, 3); ++i) {
                        final MethodPattern pattern = patterns[i];
                        final double patternProbability = pattern.getNumberOfObservations()
                                / (double) numberOfSubclasses;
                        String text2 = String
                                .format("Pattern #%d - covers approximately %3.0f%% of the examined subclasses (%d subclasses).",
                                        i + 1, 100 * patternProbability, pattern.getNumberOfObservations());
                        final TextAndFeaturesLine line = new TextAndFeaturesLine(templates, text2, type, provider,
                                server);
                        // line.createStyleRange(0, 16, SWT.BOLD, false, false);
                        // line.createStyleRange(40, 3, SWT.NORMAL, true,
                        // false);

                        final Composite template = SwtFactory.createGridComposite(templates, 4, 12, 2, 12, 0);
                        final List<Entry<IMethodName, Double>> entries = getRecommendedMethodOverridesSortedByLikelihood(pattern);
                        for (final Entry<IMethodName, Double> entry : entries) {
                            SwtFactory.createSquare(template);
                            final IMethodName method = entry.getKey();
                            text2 = "override " + method.getDeclaringType().getClassName() + "."
                                    + Names.vm2srcSimpleMethod(method);
                            SwtFactory.createLabel(template, text2, false, true, SWT.COLOR_BLACK);
                            SwtFactory.createLabel(template, "-");
                            SwtFactory.createLabel(template, String.format("~ %3.0f%%", entry.getValue() * 100), false,
                                    false, SWT.COLOR_BLUE);
                        }
                    }
                    composite.layout(true);
                }
                return Status.OK_STATUS;
            }
        }.schedule();

        return true;
    }

    private List<Entry<IMethodName, Double>> getRecommendedMethodOverridesSortedByLikelihood(final MethodPattern pattern) {
        final List<Entry<IMethodName, Double>> entries = Lists.newArrayList(pattern.getMethods().entrySet());
        Collections.sort(entries, new Comparator<Entry<IMethodName, Double>>() {
            @Override
            public int compare(final Entry<IMethodName, Double> o1, final Entry<IMethodName, Double> o2) {
                return Double.compare(o2.getValue(), o1.getValue());
            }
        });
        return entries;
    }

    private MethodPattern[] getPatternsSortedByFrequency(final ClassOverridePatterns directive) {
        final MethodPattern[] patterns = directive.getPatterns();
        Arrays.sort(patterns, new Comparator<MethodPattern>() {
            @Override
            public int compare(final MethodPattern pattern1, final MethodPattern pattern2) {
                return pattern2.getNumberOfObservations() - pattern1.getNumberOfObservations();
            }
        });
        return patterns;
    }

    private int computeTotalNumberOfSubclasses(final MethodPattern[] patterns) {
        int numberOfSubclasses = 0;
        for (final MethodPattern p : patterns) {
            numberOfSubclasses += p.getNumberOfObservations();
        }
        return numberOfSubclasses;
    }

}
