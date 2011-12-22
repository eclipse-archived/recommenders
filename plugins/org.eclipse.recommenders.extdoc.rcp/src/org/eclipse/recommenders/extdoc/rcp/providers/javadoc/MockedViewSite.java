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
package org.eclipse.recommenders.extdoc.rcp.providers.javadoc;

import java.util.Collection;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.NotEnabledException;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IKeyBindingService;
import org.eclipse.ui.ISourceProvider;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.IHandlerActivation;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.services.IServiceLocator;

@SuppressWarnings({ "deprecation", "rawtypes" })
public final class MockedViewSite implements IViewSite {

    private ISelectionProvider selectionProvider;
    private final IWorkbenchWindow workbenchWindow;

    public MockedViewSite(final IWorkbenchWindow workbenchWindow) {
        this.workbenchWindow = workbenchWindow;
    }

    @Override
    public String getId() {
        return null;
    }

    @Override
    public String getPluginId() {
        return null;
    }

    @Override
    public String getRegisteredName() {
        return null;
    }

    @Override
    public void registerContextMenu(final String menuId, final MenuManager menuManager,
            final ISelectionProvider selectionProvider) {
    }

    @Override
    public void registerContextMenu(final MenuManager menuManager, final ISelectionProvider selectionProvider) {
    }

    @Override
    public IKeyBindingService getKeyBindingService() {
        return null;
    }

    @Override
    public IWorkbenchPart getPart() {
        return null;
    }

    @Override
    public IWorkbenchPage getPage() {
        return null;
    }

    @Override
    public ISelectionProvider getSelectionProvider() {
        return selectionProvider;
    }

    @Override
    public Shell getShell() {
        return workbenchWindow.getShell();
    }

    @Override
    public IWorkbenchWindow getWorkbenchWindow() {
        return workbenchWindow;
    }

    @Override
    public void setSelectionProvider(final ISelectionProvider provider) {
        selectionProvider = provider;
    }

    @Override
    public Object getAdapter(final Class adapter) {
        return null;
    }

    @Override
    public Object getService(final Class api) {
        return new IHandlerService() {

            @Override
            public void addSourceProvider(final ISourceProvider provider) {
            }

            @Override
            public void removeSourceProvider(final ISourceProvider provider) {
            }

            @Override
            public void dispose() {
            }

            @Override
            public IHandlerActivation activateHandler(final IHandlerActivation activation) {
                return null;
            }

            @Override
            public IHandlerActivation activateHandler(final String commandId, final IHandler handler) {
                return null;
            }

            @Override
            public IHandlerActivation activateHandler(final String commandId, final IHandler handler,
                    final Expression expression) {
                return null;
            }

            @Override
            public IHandlerActivation activateHandler(final String commandId, final IHandler handler,
                    final Expression expression, final boolean global) {
                return null;
            }

            @Override
            public IHandlerActivation activateHandler(final String commandId, final IHandler handler,
                    final Expression expression, final int sourcePriorities) {
                return null;
            }

            @Override
            public ExecutionEvent createExecutionEvent(final Command command, final Event event) {
                return null;
            }

            @Override
            public ExecutionEvent createExecutionEvent(final ParameterizedCommand command, final Event event) {
                return null;
            }

            @Override
            public void deactivateHandler(final IHandlerActivation activation) {
            }

            @Override
            public void deactivateHandlers(final Collection activations) {
            }

            @Override
            public Object executeCommand(final String commandId, final Event event) throws ExecutionException,
                    NotDefinedException, NotEnabledException, NotHandledException {
                return null;
            }

            @Override
            public Object executeCommand(final ParameterizedCommand command, final Event event)
                    throws ExecutionException, NotDefinedException, NotEnabledException, NotHandledException {
                return null;
            }

            @Override
            public Object executeCommandInContext(final ParameterizedCommand command, final Event event,
                    final IEvaluationContext context) throws ExecutionException, NotDefinedException,
                    NotEnabledException, NotHandledException {
                return null;
            }

            @Override
            public IEvaluationContext createContextSnapshot(final boolean includeSelection) {
                return null;
            }

            @Override
            public IEvaluationContext getCurrentState() {
                return null;
            }

            @Override
            public void readRegistry() {
            }

            @Override
            public void setHelpContextId(final IHandler handler, final String helpContextId) {
            }
        };
    }

    @Override
    public boolean hasService(final Class api) {
        return false;
    }

    @Override
    public IActionBars getActionBars() {
        return new IActionBars() {

            @Override
            public void updateActionBars() {
            }

            @Override
            public void setGlobalActionHandler(final String actionId, final IAction handler) {
            }

            @Override
            public IToolBarManager getToolBarManager() {
                return new ToolBarManager();
            }

            @Override
            public IStatusLineManager getStatusLineManager() {
                return null;
            }

            @Override
            public IServiceLocator getServiceLocator() {
                return null;
            }

            @Override
            public IMenuManager getMenuManager() {
                return null;
            }

            @Override
            public IAction getGlobalActionHandler(final String actionId) {
                return null;
            }

            @Override
            public void clearGlobalActionHandlers() {
            }
        };
    }

    @Override
    public String getSecondaryId() {
        return null;
    }

}
