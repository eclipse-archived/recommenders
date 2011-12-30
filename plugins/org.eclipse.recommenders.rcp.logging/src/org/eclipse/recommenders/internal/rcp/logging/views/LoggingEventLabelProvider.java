/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 */
package org.eclipse.recommenders.internal.rcp.logging.views;

import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.recommenders.internal.rcp.logging.model.LoggingEventManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IMemento;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;

class LoggingEventLabelProvider extends LabelProvider implements ITableLabelProvider, ITableColorProvider {

    private static final String TAG_LABEL = "labelProvider"; //$NON-NLS-1$
    private static final String TAG_PATTERN = "pattern"; //$NON-NLS-1$

    private static String DEFAULT_PATTERN = "%relative %level [%thread] %logger{25} %message %nopex"; //$NON-NLS-1$
    private final LoggerContext context;
    private PatternLayout patternLayout;

    private final Color cachedRed;
    private final Color cachedOrange;
    private final Color cachedBlack;
    private final Color cachedWhite;
    private final Color cachedGray;

    public LoggingEventLabelProvider() {
        context = new LoggerContext();
        context.setName("logging.ctx");
        createDefaultPatternLayout();
        final Display display = Display.getCurrent();
        cachedRed = Display.getCurrent().getSystemColor(SWT.COLOR_RED);
        cachedOrange = new Color(display, 255, 140, 0);
        cachedBlack = Display.getCurrent().getSystemColor(SWT.COLOR_BLACK);
        cachedWhite = Display.getCurrent().getSystemColor(SWT.COLOR_WHITE);
        cachedGray = new Color(display, 240, 240, 240);
    }

    public String getPattern() {
        return patternLayout.getPattern();
    }

    public void updatePattern(final String pattern) {
        patternLayout.stop();
        patternLayout.setPattern(pattern);
        patternLayout.start();
    }

    public void saveState(final IMemento memento) {
        final IMemento mem = memento.createChild(TAG_LABEL);
        mem.putString(TAG_PATTERN, patternLayout.getPattern());
    }

    public void init(final IMemento memento) {
        final IMemento mem = memento.getChild(TAG_LABEL);
        if (mem == null) {
            createDefaultPatternLayout();
            return;
        }

        final String pattern = mem.getString(TAG_PATTERN);
        if (pattern == null || pattern.length() == 0) {
            createDefaultPatternLayout();
            return;
        }

        patternLayout = new PatternLayout();
        patternLayout.setContext(context);
        patternLayout.setPattern(pattern);
        patternLayout.start();
    }

    private void createDefaultPatternLayout() {
        patternLayout = new PatternLayout();
        patternLayout.setContext(context);
        patternLayout.setPattern(DEFAULT_PATTERN);
        patternLayout.start();
    }

    @Override
    public String getColumnText(final Object element, final int columnIndex) {
        if (!(element instanceof ILoggingEvent)) {
            return ""; //$NON-NLS-1$
        }

        final ILoggingEvent event = (ILoggingEvent) element;

        return patternLayout.doLayout(event);
    }

    @Override
    public Color getBackground(final Object element, final int columnIndex) {
        final ILoggingEvent event = (ILoggingEvent) element;
        final int index = LoggingEventManager.getManager().getIndex(event);
        if (index % 2 == 0) {
            return cachedGray;
        } else {
            return cachedWhite;
        }
    }

    @Override
    public Color getForeground(final Object element, final int columnIndex) {
        final ILoggingEvent event = (ILoggingEvent) element;
        if (event.getLevel().equals(Level.ERROR)) {
            return cachedRed;
        } else if (event.getLevel().equals(Level.WARN)) {
            return cachedOrange;
        } else {
            return cachedBlack;
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        cachedGray.dispose();
        cachedOrange.dispose();
    }

    @Override
    public Image getColumnImage(final Object element, final int columnIndex) {
        // TODO Auto-generated method stub
        return null;
    }
}