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

import org.eclipse.jdt.core.IType;
import org.eclipse.recommenders.commons.selection.IJavaElementSelection;
import org.eclipse.recommenders.commons.utils.Names;
import org.eclipse.recommenders.commons.utils.names.IMethodName;
import org.eclipse.recommenders.commons.utils.names.ITypeName;
import org.eclipse.recommenders.internal.rcp.extdoc.providers.swt.TableListing;
import org.eclipse.recommenders.internal.rcp.extdoc.providers.swt.TextAndFeaturesLine;
import org.eclipse.recommenders.internal.rcp.extdoc.providers.utils.ElementResolver;
import org.eclipse.recommenders.rcp.extdoc.AbstractLocationSensitiveTitledProvider;
import org.eclipse.recommenders.rcp.extdoc.ProviderUiJob;
import org.eclipse.recommenders.rcp.extdoc.SwtFactory;
import org.eclipse.recommenders.rcp.extdoc.features.CommunityFeatures;
import org.eclipse.recommenders.server.extdoc.SubclassingServer;
import org.eclipse.recommenders.server.extdoc.types.ClassOverridePatterns;
import org.eclipse.recommenders.server.extdoc.types.MethodPattern;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import com.google.common.collect.Lists;
import com.google.inject.Inject;

public final class SubclassingTemplatesProvider extends AbstractLocationSensitiveTitledProvider {

    private final SubclassingServer server;

    @Inject
    SubclassingTemplatesProvider(final SubclassingServer server) {
        this.server = server;
    }

    @Override
    protected Composite createContentComposite(final Composite parent) {
        return SwtFactory.createGridComposite(parent, 1, 0, 11, 0, 0);
    }

    @Override
    protected ProviderUiJob updateExtendsDeclarationSelection(final IJavaElementSelection selection, final IType type) {
        return printProposals(ElementResolver.toRecType(type));
    }

    private ProviderUiJob printProposals(final ITypeName type) {
        final ClassOverridePatterns directive = server.getClassOverridePatterns(type);
        if (directive == null) {
            return null;
        }
        final MethodPattern[] patterns = getPatternsSortedByFrequency(directive);
        final Integer numberOfSubclasses = Integer.valueOf(computeTotalNumberOfSubclasses(patterns));

        final String text = String
                .format("By analysing %d subclasses subclasses that override at least one method, the following subclassing patterns have been identified.",
                        numberOfSubclasses);
        final CommunityFeatures ratings = CommunityFeatures.create(type, null, this, server);

        return new ProviderUiJob() {
            @Override
            public void run(final Composite composite) {
                disposeChildren(composite);
                SwtFactory.createStyledText(composite, text, SWT.COLOR_BLACK, true);

                final Composite templates = SwtFactory.createGridComposite(composite, 1, 0, 12, 0, 0);
                for (int i = 0; i < Math.min(patterns.length, 3); ++i) {
                    final MethodPattern pattern = patterns[i];
                    final int patternProbability = (int) (pattern.getNumberOfObservations()
                            / numberOfSubclasses.doubleValue() * 100);
                    String text2 = String.format(
                            "Pattern #%d - covers approximately %d%% of the examined subclasses (%d subclasses).",
                            i + 1, patternProbability, pattern.getNumberOfObservations());
                    new TextAndFeaturesLine(templates, text2, ratings);

                    final TableListing table = new TableListing(templates, 4);
                    final List<Entry<IMethodName, Double>> entries = getRecommendedMethodOverridesSortedByLikelihood(pattern);
                    for (final Entry<IMethodName, Double> entry : entries) {
                        table.startNewRow();
                        final IMethodName method = entry.getKey();
                        text2 = "override " + method.getDeclaringType().getClassName() + "."
                                + Names.vm2srcSimpleMethod(method);
                        table.addCell(text2, false, true, SWT.COLOR_BLACK);
                        table.addCell("-", false, false, SWT.COLOR_BLACK);
                        table.addCell(String.format("%3.0f%%", entry.getValue() * 100), false, false, SWT.COLOR_BLUE);
                    }
                }

                ratings.loadCommentsComposite(composite);
            }
        };
    }

    static List<Entry<IMethodName, Double>> getRecommendedMethodOverridesSortedByLikelihood(final MethodPattern pattern) {
        final List<Entry<IMethodName, Double>> entries = Lists.newArrayList(pattern.getMethods().entrySet());
        Collections.sort(entries, new Comparator<Entry<IMethodName, Double>>() {
            @Override
            public int compare(final Entry<IMethodName, Double> o1, final Entry<IMethodName, Double> o2) {
                return o2.getValue().compareTo(o1.getValue());
            }
        });
        return entries;
    }

    private static MethodPattern[] getPatternsSortedByFrequency(final ClassOverridePatterns directive) {
        final MethodPattern[] patterns = directive.getPatterns();
        Arrays.sort(patterns, new Comparator<MethodPattern>() {
            @Override
            public int compare(final MethodPattern pattern1, final MethodPattern pattern2) {
                return pattern2.getNumberOfObservations() - pattern1.getNumberOfObservations();
            }
        });
        return patterns;
    }

    private static int computeTotalNumberOfSubclasses(final MethodPattern[] patterns) {
        int numberOfSubclasses = 0;
        for (final MethodPattern p : patterns) {
            numberOfSubclasses += p.getNumberOfObservations();
        }
        return numberOfSubclasses;
    }

}
