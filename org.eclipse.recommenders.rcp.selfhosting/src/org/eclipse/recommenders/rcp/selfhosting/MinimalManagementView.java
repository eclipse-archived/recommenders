/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marcel Bruch - Initial API and implementation
 */
package org.eclipse.recommenders.rcp.selfhosting;

import static org.apache.commons.io.FileUtils.iterateFiles;
import static org.apache.commons.lang3.StringUtils.removeStart;
import static org.eclipse.recommenders.commons.utils.Checks.ensureIsDirectory;
import static org.eclipse.recommenders.commons.utils.Checks.ensureIsNotNull;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import javax.ws.rs.core.MediaType;

import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.recommenders.commons.client.ClientConfiguration;
import org.eclipse.recommenders.commons.client.NotFoundException;
import org.eclipse.recommenders.commons.client.TransactionResult;
import org.eclipse.recommenders.commons.client.WebServiceClient;
import org.eclipse.recommenders.mining.calls.Algorithm;
import org.eclipse.recommenders.mining.calls.AlgorithmParameters;
import org.eclipse.recommenders.mining.calls.data.couch.CouchGuiceModule;
import org.eclipse.recommenders.mining.calls.data.couch.ModelSpecsGenerator;
import org.eclipse.recommenders.server.commons.ServerConfiguration;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
import org.osgi.framework.Bundle;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.inject.Guice;
import com.google.inject.Injector;

public class MinimalManagementView extends ViewPart {

    private static final String COUCH_BASEURL = "http://localhost:5984";

    @Override
    public void createPartControl(final Composite parent) {
        final Composite container = new Composite(parent, SWT.NONE);
        container.setLayout(new FillLayout(SWT.VERTICAL));

        createButton("Initialize CouchDB", container, new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                try {
                    final WebServiceClient c = new WebServiceClient(ClientConfiguration.create(COUCH_BASEURL));
                    final Bundle setupBundle = ensureIsNotNull(Platform
                            .getBundle("org.eclipse.recommenders.server.setup"));
                    final File setupBasedir = FileLocator.getBundleFile(setupBundle);
                    ensureIsDirectory(setupBasedir);

                    final File couchdbBasedir = new File(setupBasedir.getAbsoluteFile(), "couchdb");
                    ensureIsDirectory(couchdbBasedir);
                    for (final File db : couchdbBasedir.listFiles()) {
                        findOrCreateDatabase(db.getName(), c);
                        final Iterator<File> it = iterateFiles(db, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
                        while (it.hasNext()) {
                            putDocument(c, couchdbBasedir, it.next());
                        }
                    }

                } catch (final Exception e1) {
                    e1.printStackTrace();
                }
            }

            private void putDocument(final WebServiceClient c, final File couchdbBasedir, final File contentFile)
                    throws IOException {

                String path = removeStart(contentFile.getAbsolutePath(), couchdbBasedir.getAbsolutePath());
                path = path.replace("\\", "/");
                path = StringUtils.removeStart(path, "/");
                path = StringUtils.removeEnd(path, ".json");
                try {
                    final String content = Files.toString(contentFile, Charsets.UTF_8);
                    c.createRequestBuilder(path).type(MediaType.TEXT_PLAIN_TYPE).put(content);
                } catch (final Exception e) {
                    System.out.printf("Didn't put contents of %s. May already exist?: %s\n", contentFile,
                            e.getMessage());
                }
            }

            private void findOrCreateDatabase(final String databaseName, final WebServiceClient c) {
                try {
                    final TransactionResult doGetRequest = c.doGetRequest(databaseName, TransactionResult.class);
                    System.out.println(doGetRequest);
                } catch (final NotFoundException nfe) {
                    c.doPutRequest(databaseName, "", TransactionResult.class);
                }
            }
        });
        createButton("Generate Recommender Models", container, new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                new WorkspaceJob("Code Recommendes Model Generation") {

                    @Override
                    public IStatus runInWorkspace(final IProgressMonitor monitor) throws CoreException {
                        monitor.beginTask("Generating...", 5);
                        try {
                            {
                                final File out = new File(ServerConfiguration.getDataBasedir(), "models/calls/");
                                out.mkdirs();
                                final AlgorithmParameters arguments = new AlgorithmParameters();
                                arguments.setOut(out);
                                arguments.setForce(true);
                                monitor.subTask("Updating model specifications for all known libraries...");
                                final Injector injector = Guice.createInjector(new CouchGuiceModule(arguments));
                                injector.getInstance(ModelSpecsGenerator.class).execute();
                                monitor.worked(1);
                                monitor.subTask("Creating call models for available data...");
                                injector.getInstance(Algorithm.class).run();
                                monitor.worked(2);
                            }
                            {
                                monitor.subTask("Creating extdoc models from available data...");
                                final org.eclipse.recommenders.mining.extdocs.AlgorithmParameters arguments = new org.eclipse.recommenders.mining.extdocs.AlgorithmParameters();
                                final Injector injector = Guice
                                        .createInjector(new org.eclipse.recommenders.mining.extdocs.couch.CouchGuiceModule(
                                                arguments));
                                injector.getInstance(org.eclipse.recommenders.mining.extdocs.Algorithm.class).run();
                                monitor.worked(2);
                            }

                        } catch (final Exception x) {
                            x.printStackTrace();
                            return new Status(IStatus.ERROR, "org.eclipse.recommenders.rcp",
                                    "Error during mode generation.", x);
                        } finally {
                            monitor.done();
                        }
                        return Status.OK_STATUS;
                    }
                }.schedule();

            }
        });

        // createButton("Generate Extdocs Models", container, new
        // SelectionAdapter() {
        // @Override
        // public void widgetSelected(final SelectionEvent e) {
        // System.out.println("do it");
        // }
        // });
    }

    private void createButton(final String label, final Composite container, final SelectionListener listener) {
        final Button btn = new Button(container, SWT.PUSH);
        btn.setText(label);
        btn.addSelectionListener(listener);
    }

    @Override
    public void setFocus() {

    }

}