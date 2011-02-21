/**
 * Copyright (c) 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.rcp.codesearch.views;

import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jdt.ui.text.IJavaColorConstants;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.ITokenScanner;
import org.eclipse.jface.text.rules.PatternRule;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.recommenders.commons.codesearch.Proposal;
import org.eclipse.recommenders.commons.utils.gson.GsonUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Display;

public class SimpleSummaryViewerConfiguration extends SourceViewerConfiguration {
    public static final TextAttribute ITALIC = new TextAttribute(null, null, SWT.ITALIC);
    public static final TextAttribute BOLD = new TextAttribute(null, null, SWT.BOLD);
    public static final TextAttribute BLUE = new TextAttribute(JavaUI.getColorManager().getColor(
            IJavaColorConstants.JAVA_KEYWORD), null, SWT.BOLD);
    private static Font font = getFont();
    public static final TextAttribute TITTLE_FONT = new TextAttribute(JavaUI.getColorManager().getColor(
            IJavaColorConstants.JAVADOC_LINK), null, SWT.BOLD, font);
    public static final TextAttribute TITTLE_FONT_KEYWORD = new TextAttribute(JavaUI.getColorManager().getColor(
            IJavaColorConstants.JAVA_KEYWORD), null, SWT.BOLD, font);
    public Proposal hit;
    public String tittle;

    @Override
    public String[] getConfiguredContentTypes(final ISourceViewer sourceViewer) {
        return SimpleSummaryPartitionScanner.CONTENT_TYPES;
    }

    private static Font getFont() {
        final Font initialFont = JFaceResources.getDefaultFont();
        final FontData[] fontData = initialFont.getFontData();
        for (int i = 0; i < fontData.length; i++) {
            fontData[i].setHeight(12);
        }
        final Font newFont = new Font(Display.getCurrent(), fontData);
        return newFont;
    }

    @Override
    public ITextHover getTextHover(final ISourceViewer sourceViewer, final String contentType) {
        return new ITextHover() {
            @Override
            public IRegion getHoverRegion(final ITextViewer textViewer, final int offset) {
                return new Region(offset, 0);
            }

            @Override
            public String getHoverInfo(final ITextViewer textViewer, final IRegion hoverRegion) {
                final String stats = GsonUtil.serialize(hit.featureScores);
                return stats;
            }
        };
    }

    @Override
    public IPresentationReconciler getPresentationReconciler(final ISourceViewer sourceViewer) {
        final PresentationReconciler reconciler = new PresentationReconciler();
        final DefaultDamagerRepairer dr = new DefaultDamagerRepairer(getRuleScanner());
        reconciler.setDamager(dr, SimpleSummaryPartitionScanner.TITTLE_TYPE);
        reconciler.setRepairer(dr, SimpleSummaryPartitionScanner.TITTLE_TYPE);
        reconciler.install(sourceViewer);
        dr.setDocument(sourceViewer.getDocument());
        //
        // DefaultDamagerRepairer dr = new
        // DefaultDamagerRepairer(getRuleScanner());
        // reconciler.setDamager(dr, SimpleSummaryPartitionScanner.CODE_CALLS);
        // reconciler.setRepairer(dr, SimpleSummaryPartitionScanner.CODE_CALLS);
        // reconciler.setDamager(dr,
        // SimpleSummaryPartitionScanner.CODE_LOCATION);
        // reconciler.setRepairer(dr,
        // SimpleSummaryPartitionScanner.CODE_LOCATION);
        // reconciler.setDamager(dr, SimpleSummaryPartitionScanner.CODE_USES);
        // reconciler.setRepairer(dr, SimpleSummaryPartitionScanner.CODE_USES);
        // reconciler.setDamager(dr, SimpleSummaryPartitionScanner.CODE_SCORE);
        // reconciler.setRepairer(dr, SimpleSummaryPartitionScanner.CODE_SCORE);
        return reconciler;
    }

    protected ITokenScanner getRuleScanner() {
        // if (scanner == null)
        // {
        final RuleBasedScanner scanner = new RuleBasedScanner();
        final IRule implementsRule = new PatternRule(" implements", " ", new Token(TITTLE_FONT_KEYWORD), '\\', true);
        final IRule extendsRule = new PatternRule(" extends", " ", new Token(TITTLE_FONT_KEYWORD), '\\', true);
        scanner.setRules(new IRule[] { implementsRule, extendsRule });
        // { createCallsRule(), createInClassRule(), createInMethodRule(),
        // createScoreRule(), createUsesRule(), });
        scanner.setDefaultReturnToken(new Token(TITTLE_FONT));
        return scanner;
    }
}
