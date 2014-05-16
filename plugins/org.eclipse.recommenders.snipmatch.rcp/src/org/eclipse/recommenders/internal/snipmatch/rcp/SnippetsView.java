/**
 * Copyright (c) 2010, 2013 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.snipmatch.rcp;

import static org.eclipse.jface.databinding.swt.WidgetProperties.enabled;
import static org.eclipse.jface.databinding.viewers.ViewerProperties.singleSelection;
import static org.eclipse.recommenders.internal.snipmatch.rcp.SnippetProposal.createDisplayString;
import static org.eclipse.recommenders.utils.Checks.cast;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;

import javax.inject.Inject;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.conversion.IConverter;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.databinding.viewers.IViewerObservableValue;
import org.eclipse.jface.databinding.viewers.ViewersObservables;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.recommenders.internal.snipmatch.rcp.EclipseGitSnippetRepository.SnippetRepositoryClosedEvent;
import org.eclipse.recommenders.internal.snipmatch.rcp.EclipseGitSnippetRepository.SnippetRepositoryContentChangedEvent;
import org.eclipse.recommenders.internal.snipmatch.rcp.EclipseGitSnippetRepository.SnippetRepositoryOpenedEvent;
import org.eclipse.recommenders.internal.snipmatch.rcp.editors.SnippetEditorInput;
import org.eclipse.recommenders.rcp.IRcpService;
import org.eclipse.recommenders.rcp.utils.ObjectToBooleanConverter;
import org.eclipse.recommenders.snipmatch.ISnippet;
import org.eclipse.recommenders.snipmatch.ISnippetRepository;
import org.eclipse.recommenders.snipmatch.Snippet;
import org.eclipse.recommenders.utils.Recommendation;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import com.google.common.base.Throwables;
import com.google.common.collect.Sets;
import com.google.common.eventbus.Subscribe;

public class SnippetsView extends ViewPart implements IRcpService {

    private final Set<ISnippetRepository> repos;
    private Text txtSearch;
    private List list;
    private ListViewer viewer;
    private Button btnEdit;
    private Button btnRemove;
    private Button btnAdd;
    private Button btnReIndex;

    private DataBindingContext ctx;
    private IViewerObservableValue selection;

    @Inject
    public SnippetsView(Set<ISnippetRepository> repos) {
        this.repos = repos;
    }

    @Override
    public void createPartControl(Composite parent) {

        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(2, false));

        txtSearch = new Text(composite, SWT.BORDER | SWT.ICON_SEARCH | SWT.SEARCH | SWT.CANCEL);
        txtSearch.setMessage(Messages.SEARCH_PLACEHOLDER_FILTER_TEXT);
        txtSearch.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        txtSearch.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                refreshInput();
            }
        });

        txtSearch.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.keyCode == SWT.ARROW_DOWN && list.getItemCount() != 0) {
                    list.setFocus();
                    list.setSelection(0);
                }
            }
        });
        new Label(composite, SWT.NONE);

        viewer = new ListViewer(composite);
        list = viewer.getList();
        list.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 4));

        btnAdd = new Button(composite, SWT.NONE);
        btnAdd.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        btnAdd.setText(Messages.SNIPPETS_VIEW_BUTTON_ADD);
        btnAdd.setEnabled(isImportSupported());
        btnAdd.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                for (ISnippetRepository repo : repos) {
                    if (repo.isImportSupported()) {
                        // TODO Make the repo selectable
                        // don't just store in the first that can import
                        doAdd(repo);
                        break;
                    }
                }
            }
        });

        btnEdit = new Button(composite, SWT.NONE);
        btnEdit.setEnabled(false);
        btnEdit.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        btnEdit.setText(Messages.SNIPPETS_VIEW_BUTTON_EDIT);
        btnEdit.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                doOpen();
            }
        });

        btnRemove = new Button(composite, SWT.NONE);
        btnRemove.setEnabled(false);
        btnRemove.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false, 1, 1));
        btnRemove.setText(Messages.SNIPPETS_VIEW_BUTTON_REMOVE);
        btnRemove.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                Recommendation<ISnippet> recommendation = cast(selection.getValue());
                try {
                    for (ISnippetRepository repo : repos) {
                        repo.delete(recommendation.getProposal().getUuid());
                    }
                } catch (Exception e) {
                    Throwables.propagate(e);
                }
            }
        });

        btnReIndex = new Button(composite, SWT.NONE);
        btnReIndex.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false, 1, 1));
        btnReIndex.setText(Messages.SNIPPETS_VIEW_BUTTON_REFRESH);
        btnReIndex.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                refreshInput();
            }
        });

        viewer.setLabelProvider(new LabelProvider() {
            @Override
            public String getText(Object element) {
                Recommendation<ISnippet> recommendation = cast(element);
                ISnippet snippet = recommendation.getProposal();
                return createDisplayString(snippet);
            }
        });
        viewer.addOpenListener(new IOpenListener() {

            @Override
            public void open(OpenEvent event) {
                doOpen();
            }
        });
        viewer.setContentProvider(new ArrayContentProvider());
        viewer.setSorter(new ViewerSorter());
        refreshInput();
        selection = ViewersObservables.observeSingleSelection(viewer);
        initDataBindings();
    }

    @Subscribe
    public void onEvent(SnippetRepositoryOpenedEvent e) throws IOException {
        refreshInput();
        if (e.getRepository().isImportSupported()) {
            Display.getDefault().asyncExec(new Runnable() {
                @Override
                public void run() {
                    btnAdd.setEnabled(true);
                }
            });
        }
    }

    @Subscribe
    public void onEvent(SnippetRepositoryClosedEvent e) throws IOException {
        refreshInput();
        btnAdd.setEnabled(isImportSupported());
    }

    private boolean isImportSupported() {
        for (ISnippetRepository repo : repos) {
            if (repo.isImportSupported()) {
                return true;
            }
        }
        return false;
    }

    @Subscribe
    public void onEvent(SnippetRepositoryContentChangedEvent e) throws IOException {
        refreshInput();
    }

    private void refreshInput() {
        Job refreshJob = new Job(Messages.JOB_REFRESHING_SNIPPETS_VIEW) {

            @Override
            protected IStatus run(IProgressMonitor monitor) {

                Display.getDefault().asyncExec(new Runnable() {
                    @Override
                    public void run() {
                        if (!viewer.getControl().isDisposed()) {
                            final Set<Recommendation<ISnippet>> snippets = Sets.newHashSet();
                            for (ISnippetRepository repo : repos) {
                                snippets.addAll(repo.search(txtSearch.getText()));
                            }
                            viewer.setInput(snippets);
                        }
                    }
                });
                return Status.OK_STATUS;
            }
        };
        refreshJob.schedule();
    }

    private void doAdd(ISnippetRepository repo) {
        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();

        try {
            ISnippet snippet = new Snippet(UUID.randomUUID(), "", "", Collections.<String>emptyList(),
                    Collections.<String>emptyList(), "");

            final SnippetEditorInput input = new SnippetEditorInput(snippet, repo);
            page.openEditor(input, "org.eclipse.recommenders.snipmatch.rcp.editors.snippet"); //$NON-NLS-1$
        } catch (Exception e) {
            Throwables.propagate(e);
        }
    }

    private void doOpen() {
        Recommendation<ISnippet> recommendation = cast(selection.getValue());

        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        try {
            ISnippet snippet = recommendation.getProposal();
            ISnippetRepository repository = findRepoForOriginalSnippet(snippet);

            final SnippetEditorInput input = new SnippetEditorInput(snippet, repository);
            page.openEditor(input, "org.eclipse.recommenders.snipmatch.rcp.editors.snippet"); //$NON-NLS-1$
        } catch (Exception e) {
            Throwables.propagate(e);
        }
    }

    private ISnippetRepository findRepoForOriginalSnippet(ISnippet snippet) {
        for (ISnippetRepository repo : repos) {
            if (repo.hasSnippet(snippet.getUuid())) {
                return repo;
            }
        }
        return null;
    }

    @Override
    public void setFocus() {
        viewer.getControl().setFocus();
    }

    protected void initDataBindings() {
        ctx = new DataBindingContext();
        //
        UpdateValueStrategy simpleStrategy = new UpdateValueStrategy();
        simpleStrategy.setConverter(new ObjectToBooleanConverter());

        IObservableValue selectionValue = singleSelection().observe(viewer);
        IObservableValue enabledBtnEditValue = enabled().observe(btnEdit);
        ctx.bindValue(selectionValue, enabledBtnEditValue, simpleStrategy, null);

        UpdateValueStrategy deleteSupportedStrategy = new UpdateValueStrategy();

        deleteSupportedStrategy.setConverter(new IConverter() {

            @Override
            public Object getFromType() {
                return Recommendation.class;
            }

            @Override
            public Object getToType() {
                return Boolean.class;
            }

            @Override
            public Boolean convert(Object fromObject) {
                if (fromObject == null) {
                    return false;
                }
                Recommendation<ISnippet> selection = (Recommendation<ISnippet>) fromObject;
                ISnippet snippet = selection.getProposal();
                for (ISnippetRepository repo : repos) {
                    if (repo.isDeleteSupported()) {
                        if (repo.hasSnippet(snippet.getUuid())) {
                            return true;
                        }
                    }
                }
                return false;
            }

        });
        IObservableValue enabledBtnRemoveValue = enabled().observe(btnRemove);
        ctx.bindValue(selectionValue, enabledBtnRemoveValue, deleteSupportedStrategy, null);
    }
}
