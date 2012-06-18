/**
 * Copyright (c) 2011 Doug Wightman, Zi Ye, Cheng Chen
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.recommenders.snipmatch.rcp;

import java.io.IOException;
import java.net.URL;
import java.util.Random;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.recommenders.snipmatch.core.Effect;
import org.eclipse.recommenders.snipmatch.local.LocalMatchClient;
import org.eclipse.recommenders.snipmatch.local.StringCompareSearchEngine;
import org.eclipse.recommenders.snipmatch.preferences.PreferenceConstants;
import org.eclipse.recommenders.snipmatch.search.SearchClient;
import org.eclipse.recommenders.snipmatch.search.SnipMatchSearchEngine;
import org.eclipse.recommenders.snipmatch.web.ILoginListener;
import org.eclipse.recommenders.snipmatch.web.ISendFeedbackListener;
import org.eclipse.recommenders.snipmatch.web.RemoteMatchClient;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

/**
 * Main plugin class. Methods are mostly for setting things into motion, and are fairly self-explanatory.
 */
public class SnipMatchPlugin extends AbstractUIPlugin implements IStartup {

    public static final String PLUGIN_ID = "org.eclipse.recommenders.snipmatch.rcp";
    public static final String UPDATE_SITE_INFO_URL = "http://snipmatch.com/eclipsePlugin/site.xml";
    public static final String SUBMIT_HELP_URL = "http://snipmatch.com/help";
    public static final String REGISTER_URL = "http://snipmatch.com/register";

    // We can switch different searchEngine here, default just use a simple string compare search engine
    private final SnipMatchSearchEngine searchEngine = StringCompareSearchEngine.getInstance();

    private SearchClient workClient = null;

    private SearchClient localClient = null;
    private SearchClient remoteClient = null;

    private LoginBox loginBox;
    private SearchBox searchBox;
    private SubmitBox submitBox;
    private ProfileBox profileBox;

    private static SnipMatchPlugin plugin;

    @Override
    public void earlyStartup() {

        reportIn();
    }

    /**
     * Let's the server know when the user starts Eclipse.
     */
    private void reportIn() {

        ISendFeedbackListener listener = new ISendFeedbackListener() {
            @Override
            public void sendFeedbackSucceeded() {
            }

            @Override
            public void sendFeedbackFailed(final String error) {
            }
        };

        if (!workClient.isWorking())
            workClient.startSendFeedback("", null, null, 1, false, false, true, SnipMatchPlugin.getClientId(), false,
                    listener);
    }

    public void initSearchEngine(String snippetsDir, String indexDir) {
        if (!searchEngine.isInitialized(snippetsDir, indexDir)) {
            try {
                searchEngine.initialize(snippetsDir, indexDir);
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }
    }

    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;

        // We just use local search client for demo, we will switch between
        // local and remote in the future
        localClient = LocalMatchClient.getInstance();
        remoteClient = RemoteMatchClient.getInstance();

        loginBox = new LoginBox();
        searchBox = new SearchBox();
        submitBox = new SubmitBox();
        profileBox = new ProfileBox();
        initSearchClient();

        String snippetsDir = SnipMatchPlugin.getDefault().getPreferenceStore()
                .getString(PreferenceConstants.SNIPPETS_STORE_DIR);
        String indexDir = SnipMatchPlugin.getDefault().getPreferenceStore()
                .getString(PreferenceConstants.SNIPPETS_INDEX_FILE);
        initSearchEngine(snippetsDir, indexDir);
    }

    public void initSearchClient() {
        String searchClient = SnipMatchPlugin.getDefault().getPreferenceStore()
                .getString(PreferenceConstants.SEARCH_MODEL);

        if (searchClient.equals(PreferenceConstants.SEARCH_MODEL_LOCAL)) {
            if (workClient instanceof LocalMatchClient)
                return;
            if (workClient instanceof RemoteMatchClient)
                workClient.cancelWork();

            workClient = localClient;
            loginBox.setSearchClient(localClient);
            searchBox.setSearchClient(localClient);
            submitBox.setSearchClient(localClient);
            profileBox.setSearchClient(localClient);
        } else {
            if (workClient instanceof RemoteMatchClient)
                return;

            workClient = remoteClient;
            loginBox.setSearchClient(remoteClient);
            searchBox.setSearchClient(remoteClient);
            submitBox.setSearchClient(remoteClient);
            profileBox.setSearchClient(remoteClient);
        }

    }

    public static boolean isLoggedIn() {

        if (plugin == null || plugin.workClient == null)
            return false;
        return plugin.workClient.isLoggedIn();
    }

    /**
     * Gets the client's instance ID. Randomly generated the first time, then saved in preferences.
     * 
     * @return
     */
    public static long getClientId() {

        IEclipsePreferences prefs = new InstanceScope().getNode(PLUGIN_ID);
        long id = prefs.getLong("client.id", generateId());
        prefs.putLong("client.id", id);
        return id;
    }

    /**
     * Randomly generates an ID for the client.
     * 
     * @return
     */
    private static long generateId() {

        Random rand = new Random();
        long id = rand.nextLong();
        if (id < 0)
            id *= -1;
        return id;
    }

    public void stop(BundleContext context) throws Exception {

        plugin = null;
        super.stop(context);
    }

    public void showLoginBox(Runnable onSuccess, Runnable onFail) {

        IEclipsePreferences prefs = new InstanceScope().getNode(PLUGIN_ID);
        String username = prefs.get("login.username", "guest");
        String password = prefs.get("login.password", "guest");

        loginBox.show(username, password, onSuccess, onFail);
    }

    public void logout() {

        workClient.logout();

        PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {

            @Override
            public void run() {

                MessageBox popup = new MessageBox(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
                        SWT.ICON_INFORMATION | SWT.OK | SWT.APPLICATION_MODAL);

                popup.setText("SnipMatch");
                popup.setMessage("Signed out.");

                popup.open();
            }
        });
    }

    public void showSearchBox(final String envName) {
        searchBox.show(envName);
    }

    public void showSubmitBox() {

        showSubmitBox(null);
    }

    private void showRegisterDialog() {

        MessageBox popup = new MessageBox(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
                SWT.ICON_INFORMATION | SWT.YES | SWT.NO | SWT.APPLICATION_MODAL);

        popup.setText("SnipMatch");
        popup.setMessage("");

        if (popup.open() == SWT.YES) {

            try {
                PlatformUI.getWorkbench().getBrowserSupport().getExternalBrowser()
                        .openURL(new URL(SnipMatchPlugin.REGISTER_URL));
            } catch (Exception e) {
            }
        }
    }

    public void showSubmitBox(final Effect effect) {
    }

    public void showProfileBox() {

        if (workClient.isLoggedIn()) {

            if (workClient.getUsername().equals("guest")) {

                showRegisterDialog();
                return;
            }

            profileBox.show();
            profileBox.populate();
        } else {

            IEclipsePreferences prefs = new InstanceScope().getNode(PLUGIN_ID);
            String username = prefs.get("login.username", "guest");
            String password = prefs.get("login.password", "guest");

            profileBox.show();

            workClient.startLogin(username, password, new ILoginListener() {

                @Override
                public void loginSucceeded() {

                    PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {

                        @Override
                        public void run() {

                            if (workClient.getUsername().equals("guest")) {

                                showRegisterDialog();
                                profileBox.hide();
                            } else {
                                profileBox.populate();
                                checkUpdates();
                            }
                        }
                    });
                }

                @Override
                public void loginFailed(String error) {

                    PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {

                        @Override
                        public void run() {

                            loginBox.show("guest", "guest", new Runnable() {

                                @Override
                                public void run() {

                                    profileBox.populate();
                                }
                            }, new Runnable() {

                                @Override
                                public void run() {

                                    profileBox.hide();
                                }
                            });
                        }
                    });
                }
            });
        }
    }

    public void checkUpdates() {

        new CheckUpdateThread(workClient).start();
    }

    protected void initializeImageRegistry(ImageRegistry imageRegistry) {

        super.initializeImageRegistry(imageRegistry);
        Bundle bundle = Platform.getBundle(PLUGIN_ID);

        ImageDescriptor imgDesc = ImageDescriptor.createFromURL(FileLocator.find(bundle,
                new Path("images/warning.gif"), null));
        imageRegistry.put("warning", imgDesc);

        imgDesc = ImageDescriptor.createFromURL(FileLocator.find(bundle, new Path("images/thumbs_up.gif"), null));
        imageRegistry.put("thumbs_up", imgDesc);

        imgDesc = ImageDescriptor.createFromURL(FileLocator.find(bundle, new Path("images/thumbs_down.gif"), null));
        imageRegistry.put("thumbs_down", imgDesc);

        imgDesc = ImageDescriptor.createFromURL(FileLocator.find(bundle, new Path("images/flag.gif"), null));
        imageRegistry.put("flag", imgDesc);

        imgDesc = ImageDescriptor.createFromURL(FileLocator.find(bundle, new Path("images/comment.gif"), null));
        imageRegistry.put("comment", imgDesc);
    }

    public static SnipMatchPlugin getDefault() {

        return plugin;
    }

    public SnipMatchSearchEngine getSearchEngine() {
        return searchEngine;
    }
}