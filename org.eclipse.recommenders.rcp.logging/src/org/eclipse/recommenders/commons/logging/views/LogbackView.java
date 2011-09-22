/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 */
package org.eclipse.recommenders.commons.logging.views;

import java.net.Socket;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.recommenders.commons.logging.SimpleSocketServer;
import org.eclipse.recommenders.commons.logging.model.EventFilter;
import org.eclipse.recommenders.commons.logging.model.LoggingEventManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;

import ch.qos.logback.classic.boolex.JaninoEventEvaluator;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.EvaluatorFilter;
import ch.qos.logback.core.spi.FilterReply;

public class LogbackView extends ViewPart {

    private static final String TAG_DISPLAY = "display"; //$NON-NLS-1$
    private static final String TAG_FONTNAME = "fontName"; //$NON-NLS-1$
    private static final String TAG_FONTSIZE = "fontSize"; //$NON-NLS-1$

    public static final String ID = "ch.qos.logback.eclipse.views.LogbackView"; //$NON-NLS-1$

    private TableViewer viewer;
    private TableColumn textColumn;
    private final Integer tmpFontSize = null;
    private final String tmpFontName = null;

    private LoggingEventContentProvider provider;
    private LoggingEventLabelProvider labelProvider;

    private Action clearConsoleAction;
    private Action autoScrollAction;

    private SimpleSocketServer server;
    private Thread serverThread;

    private IMemento memento;

    public LogbackView() {
    }

    @Override
    public void createPartControl(final Composite parent) {
        viewer = new TableViewer(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.SINGLE | SWT.FULL_SELECTION | SWT.VIRTUAL);

        final Table table = viewer.getTable();

        textColumn = new TableColumn(table, SWT.LEFT);
        textColumn.setText("Event");
        textColumn.setWidth(2000);

        viewer.setItemCount(0);

        provider = new LoggingEventContentProvider(viewer);
        LoggingEventManager.getManager().addLoggingEventManagerListener(provider);
        viewer.setContentProvider(provider);

        labelProvider = new LoggingEventLabelProvider();
        if (memento != null) {
            labelProvider.init(memento);
        }
        viewer.setLabelProvider(labelProvider);

        updateFont(tmpFontName, tmpFontSize);
    }

    @Override
    public void init(final IViewSite site) throws PartInitException {

        super.init(site);
        initServer();
    }

    @Override
    public void saveState(final IMemento memento) {
        server.saveState(memento);
        server.stop();
    }

    public void clearAll() {
        // public only for testing purpose
        LoggingEventManager.getManager().clearEventList();
        viewer.getTable().setItemCount(0);
        viewer.refresh();
    }

    @Override
    public void setFocus() {
        viewer.getControl().setFocus();
    }

    @Override
    public void dispose() {
        super.dispose();
        provider.dispose();
        labelProvider.dispose();
        server.stop();
    }

    public int getServerPort() {
        return server.getPort();
    }

    public String getPattern() {
        return labelProvider.getPattern();
    }

    public void updatePattern(final String pattern) {
        labelProvider.updatePattern(pattern);
        viewer.refresh();
    }

    public void updateServerPort(final Integer port) {
        if (port != null) {
            if (SimpleSocketServer.isPortFree(port)) {
                server.stop();
                openAndCloseClientToServer();
                if (serverThread != null) {
                    serverThread.interrupt();
                    serverThread = null;
                }
                server.setPort(port);
                startServerThread(server);
            }
        }
    }

    private void openAndCloseClientToServer() {
        try {
            new Socket("localhost", server.getPort()); //$NON-NLS-1$
        } catch (final Exception e) {
            // do nothing
        }
    }

    private void initServer() {
        server = new SimpleSocketServer();
        if (memento != null) {
            server.init(memento);
        }
        startServerThread(server);
    }

    private void startServerThread(final SimpleSocketServer server) {
        if (SimpleSocketServer.isPortFree(server.getPort())) {
            serverThread = new Thread(server);
            serverThread.start();
        } else {
        }
    }

    public Viewer getViewer() {
        // used in tests
        return viewer;
    }

    public ILoggingEvent getSelectedEvent() {
        final IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
        if (selection.size() > 0) {
            return (ILoggingEvent) selection.iterator().next();
        }
        return null;
    }

    public Font getFont() {
        return viewer.getTable().getFont();
    }

    public void updateFont(final String fontName, final Integer fontSize) {
        if (fontName == null || fontSize == null) {
            return;
        }

        Font font = null;
        try {
            font = new Font(Display.getDefault(), fontName, fontSize, SWT.NORMAL);
        } catch (final SWTError error) {
        }
        if (font != null) {
            viewer.getTable().setFont(font);
            viewer.refresh();
        }
    }

    public String getLastLine() {
        // used for testing only
        if (viewer.getTable().getItemCount() == 0) {
            return null;
        }
        final TableItem item = viewer.getTable().getItem(viewer.getTable().getItemCount() - 1);
        return item.getText();
    }

    public int getNumberOfLines() {
        // used for testing only
        return viewer.getTable().getItemCount();
    }

    public void addNoDebugFilter() {
        // used for testing only
        final EvaluatorFilter filter = FilterContentProvider.getProvider().createNewFilter();
        final JaninoEventEvaluator eval = (JaninoEventEvaluator) filter.getEvaluator();
        eval.setExpression("");
        eval.start();
        filter.setName("");
        filter.setOnMatch(FilterReply.NEUTRAL);
        filter.setOnMismatch(FilterReply.DENY);
        filter.start();
        EventFilter.add(filter);
    }

}