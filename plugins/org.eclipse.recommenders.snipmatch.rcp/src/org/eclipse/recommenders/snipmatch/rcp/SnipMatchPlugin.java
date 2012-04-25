/**
 * Copyright (c) 2011 Doug Wightman, Zi Ye
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
*/

package org.eclipse.recommenders.snipmatch.rcp;

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
import org.eclipse.recommenders.snipmatch.web.ILoginListener;
import org.eclipse.recommenders.snipmatch.web.ISendFeedbackListener;
import org.eclipse.recommenders.snipmatch.web.MatchClient;
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

	public static final String PLUGIN_ID = "org.eclipse.recommenders.snipmatch";
	public static final String UPDATE_SITE_INFO_URL = "http://snipmatch.com/eclipsePlugin/site.xml";
	public static final String SUBMIT_HELP_URL = "http://snipmatch.com/help";
	public static final String REGISTER_URL = "http://snipmatch.com/register";
	
	private MatchClient client;
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
			public void sendFeedbackSucceeded() {}
			@Override
			public void sendFeedbackFailed(final String error) {}
		};

		if (!client.isWorking())
			client.startSendFeedback("", null, null, 1, false, false, true,
					SnipMatchPlugin.getClientId(), false, listener);
	}

	public void start(BundleContext context) throws Exception {
		
		super.start(context);
		plugin = this;
		client = new MatchClient("eclipse", getBundle().getVersion().toString());
		loginBox = new LoginBox(client);
		searchBox = new SearchBox(client);
		submitBox = new SubmitBox(client);
		profileBox = new ProfileBox(client);
	}
	
	public static boolean isLoggedIn() {
		
		if (plugin == null || plugin.client == null) return false;
		return plugin.client.isLoggedIn();
	}
	
	/**
	 * Gets the client's instance ID. Randomly generated the first time, then saved in preferences.
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
	 * @return
	 */
	private static long generateId() {
		
		Random rand = new Random();
		long id = rand.nextLong();
		if (id < 0) id *= -1;
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
		
		client.logout();

		PlatformUI.getWorkbench().getDisplay()
		.asyncExec(new Runnable() {
			
			@Override
			public void run() {

				MessageBox popup = new MessageBox(PlatformUI.getWorkbench()
						.getActiveWorkbenchWindow().getShell(),
						SWT.ICON_INFORMATION | SWT.OK | SWT.APPLICATION_MODAL);
				
				popup.setText("SnipMatch");
				popup.setMessage("Signed out.");
				
				popup.open();
			}
		});
	}
	
	public void showSearchBox(final String envName) {

		if (client.isLoggedIn()) {
	
			searchBox.show(envName);
		}
		else {
			
			IEclipsePreferences prefs = new InstanceScope().getNode(PLUGIN_ID);
			String username = prefs.get("login.username", "guest");
			String password = prefs.get("login.password", "guest");
			
			client.startLogin(username, password, new ILoginListener() {
				
				@Override
				public void loginSucceeded() {
					
					PlatformUI.getWorkbench().getDisplay()
					.asyncExec(new Runnable() {
						
						@Override
						public void run() {

							searchBox.release();
						}
					});
				}
				
				@Override
				public void loginFailed(String error) {

					PlatformUI.getWorkbench().getDisplay()
					.asyncExec(new Runnable() {
						
						@Override
						public void run() {

							searchBox.hide();

							loginBox.show(null, null, new Runnable() {
								
								@Override
								public void run() {
									
									searchBox.show(envName);
								}
							},
							null);
						}
					});
				}
			});
			
			searchBox.show(envName);
			searchBox.lock();
		}
	}
	
	public void showSubmitBox() {
		
		showSubmitBox(null);
	}
	
	private void showRegisterDialog() {

		MessageBox popup = new MessageBox(PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getShell(),
				SWT.ICON_INFORMATION | SWT.YES | SWT.NO | SWT.APPLICATION_MODAL);
		
		popup.setText("SnipMatch");
		popup.setMessage("");

		if (popup.open() == SWT.YES) {

			try {
			    PlatformUI.getWorkbench().getBrowserSupport()
			    .getExternalBrowser().openURL(new URL(SnipMatchPlugin.REGISTER_URL));
			}
			catch(Exception e) {}
		}
	}
	
	public void showSubmitBox(final Effect effect) {
		
		if (client.isLoggedIn()) {

			if (client.getUsername().equals("guest")) {
				
				showRegisterDialog();
				return;
			}
			
			submitBox.show(effect);
		}
		else {
			
			IEclipsePreferences prefs = new InstanceScope().getNode(PLUGIN_ID);
			String username = prefs.get("login.username", "guest");
			String password = prefs.get("login.password", "guest");

			client.startLogin(username, password, new ILoginListener() {
				
				@Override
				public void loginSucceeded() {
					
					PlatformUI.getWorkbench().getDisplay()
					.asyncExec(new Runnable() {
						
						@Override
						public void run() {

							if (client.getUsername().equals("guest")) {
								showRegisterDialog();
							}
							else {
								submitBox.show(effect);
								checkUpdates();
							}
						}
					});
				}
				
				@Override
				public void loginFailed(String error) {
					
					PlatformUI.getWorkbench().getDisplay()
					.asyncExec(new Runnable() {
						
						@Override
						public void run() {

							loginBox.show("guest", "guest", null, null);
						}
					});
				}
			});
		}
	}
	
	public void showProfileBox() {
		
		if (client.isLoggedIn()) {

			if (client.getUsername().equals("guest")) {

				showRegisterDialog();
				return;
			}
			
			profileBox.show();
			profileBox.populate();
		}
		else {
			
			IEclipsePreferences prefs = new InstanceScope().getNode(PLUGIN_ID);
			String username = prefs.get("login.username", "guest");
			String password = prefs.get("login.password", "guest");
			
			profileBox.show();

			client.startLogin(username, password, new ILoginListener() {
				
				@Override
				public void loginSucceeded() {
					
					PlatformUI.getWorkbench().getDisplay()
					.asyncExec(new Runnable() {
						
						@Override
						public void run() {

							if (client.getUsername().equals("guest")) {

								showRegisterDialog();
								profileBox.hide();
							}
							else {
								profileBox.populate();
								checkUpdates();
							}
						}
					});
				}
				
				@Override
				public void loginFailed(String error) {
					
					PlatformUI.getWorkbench().getDisplay()
					.asyncExec(new Runnable() {
						
						@Override
						public void run() {

							loginBox.show("guest", "guest", new Runnable() {
								
								@Override
								public void run() {

									profileBox.populate();
								}
							},
							new Runnable() {
								
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
		
		new CheckUpdateThread(client).start();
	}
	
	protected void initializeImageRegistry(ImageRegistry imageRegistry) {
		
		super.initializeImageRegistry(imageRegistry);
		Bundle bundle = Platform.getBundle(PLUGIN_ID);
		
		ImageDescriptor imgDesc = ImageDescriptor.createFromURL(
				FileLocator.find(bundle, new Path("images/warning.gif"), null));
		imageRegistry.put("warning", imgDesc);
		
		imgDesc = ImageDescriptor.createFromURL(
				FileLocator.find(bundle, new Path("images/thumbs_up.gif"), null));
		imageRegistry.put("thumbs_up", imgDesc);
		
		imgDesc = ImageDescriptor.createFromURL(
				FileLocator.find(bundle, new Path("images/thumbs_down.gif"), null));
		imageRegistry.put("thumbs_down", imgDesc);
		
		imgDesc = ImageDescriptor.createFromURL(
				FileLocator.find(bundle, new Path("images/flag.gif"), null));
		imageRegistry.put("flag", imgDesc);
		
		imgDesc = ImageDescriptor.createFromURL(
				FileLocator.find(bundle, new Path("images/comment.gif"), null));
		imageRegistry.put("comment", imgDesc);
	}

	public static SnipMatchPlugin getDefault() {

		return plugin;
	}
}