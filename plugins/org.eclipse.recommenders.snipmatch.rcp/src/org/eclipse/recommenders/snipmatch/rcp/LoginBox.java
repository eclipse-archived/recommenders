/**
 * Copyright (c) 2011 Doug Wightman, Zi Ye
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
*/

package org.eclipse.recommenders.snipmatch.rcp;

import java.net.URL;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.recommenders.snipmatch.search.ClientSwitcher;
import org.eclipse.recommenders.snipmatch.web.ILoginListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;


/**
 * The login interface.
 */
public class LoginBox extends ClientSwitcher{
	
	private Shell shell;
	private Text usernameText;
	private Text passwordText;
	private Button loginButton;
	private Button cancelButton;
	private Runnable onSuccess;
	private Runnable onFail;
	
	public LoginBox() {
	}

	public void show(final String username, final String password,
			Runnable onSuccess, final Runnable onFail) {
		
		this.onSuccess = onSuccess;
		this.onFail = onFail;
	
		if (shell == null || shell.isDisposed()) {

			shell = new Shell(PlatformUI.getWorkbench().
					getActiveWorkbenchWindow().getShell(), SWT.CLOSE);
			
			shell.setText("SnipMatch");
			shell.setSize(280, 150);
			
			FormLayout fl = new FormLayout();
			fl.spacing = 5;
			fl.marginWidth = 5;
			fl.marginHeight = 5;
			
			shell.setLayout(fl);
			
			{
				Label usernameLabel = new Label(shell, SWT.NONE);
				usernameLabel.setText("Username: ");
				
				FormData fd = new FormData();
				fd.left = new FormAttachment(0, 70);
				fd.right = new FormAttachment(100);
				
				usernameText = new Text(shell, SWT.BORDER);
				usernameText.setLayoutData(fd);
				if (username != null) usernameText.setText(username);
			}
			
			{
				FormData fd = new FormData();
				fd.top = new FormAttachment(usernameText);
		
				Label passwordLabel = new Label(shell, SWT.NONE);
				passwordLabel.setText("Password: ");
				passwordLabel.setLayoutData(fd);
				
				fd = new FormData();
				fd.left = new FormAttachment(usernameText, 0, SWT.LEFT);
				fd.right = new FormAttachment(100);
				fd.top = new FormAttachment(usernameText);
				
				passwordText = new Text(shell, SWT.BORDER | SWT.PASSWORD);
				passwordText.setLayoutData(fd);
				if (password != null) passwordText.setText(password);
			}
			
			{
				FormData fd = new FormData();
				fd.top = new FormAttachment(passwordText);

				Link link = new Link(shell, SWT.NONE);
				link.setText("<a href=\"" + SnipMatchPlugin.REGISTER_URL +
						"\">Register for a free account</a>");
				link.setLayoutData(fd);
				
				link.addSelectionListener(new SelectionAdapter(){
					
					@Override
					public void widgetSelected(SelectionEvent e) {

						try {
						    PlatformUI.getWorkbench().getBrowserSupport()
						    .getExternalBrowser().openURL(new URL(e.text));
						} 
						catch (Exception ex) {}
					}
			    });
			}
			
			{
				FormData fd = new FormData();
				fd.left = new FormAttachment(50);
				fd.right = new FormAttachment(100);
				fd.bottom = new FormAttachment(100);

				cancelButton = new Button(shell, SWT.NONE);
				cancelButton.setLayoutData(fd);
				cancelButton.setText("Cancel");
				
				cancelButton.addSelectionListener(new SelectionListener() {
					
					@Override
					public void widgetSelected(SelectionEvent e) {
						shell.close();
					}
					
					@Override
					public void widgetDefaultSelected(SelectionEvent e) {
						shell.close();
					}
				});
			}
			
			{
				FormData fd = new FormData();
				fd.left = new FormAttachment(0);
				fd.right = new FormAttachment(50);
				fd.bottom = new FormAttachment(100);

				loginButton = new Button(shell, SWT.NONE);
				loginButton.setLayoutData(fd);
				loginButton.setText("Sign In");
				
				loginButton.addSelectionListener(new SelectionListener() {
					
					@Override
					public void widgetSelected(SelectionEvent e) {
						startLogin();
					}
					
					@Override
					public void widgetDefaultSelected(SelectionEvent e) {
						startLogin();
					}
				});
			}

			shell.setDefaultButton(loginButton);
			
			shell.addDisposeListener(new DisposeListener() {
				
				@Override
				public void widgetDisposed(DisposeEvent e) {

					if (onFail != null)
						PlatformUI.getWorkbench().getDisplay().asyncExec(onFail);
				}
			});
		}
		
		shell.open();
	}
	
	public void hide() {
		
		shell.dispose();
	}

	private void startLogin() {

		client.startLogin(usernameText.getText(), passwordText.getText(),
		new ILoginListener() {
			
			@Override
			public void loginSucceeded() {
				
				PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
					
					@Override
					public void run() {
						
						IEclipsePreferences prefs = new InstanceScope()
						.getNode(SnipMatchPlugin.PLUGIN_ID);
						
						prefs.put("login.username", usernameText.getText());
						prefs.put("login.password", passwordText.getText());
						
						try { prefs.flush(); }
						catch (Exception e) {}
						
						shell.close();
					}
				});
				
				if (onSuccess != null)
					PlatformUI.getWorkbench().getDisplay().asyncExec(onSuccess);
				
				SnipMatchPlugin.getDefault().checkUpdates();
			}
			
			@Override
			public void loginFailed(final String error) {
				
				PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
					
					@Override
					public void run() {
						
						MessageBox popup = new MessageBox(PlatformUI.getWorkbench()
								.getActiveWorkbenchWindow().getShell(),
								SWT.ICON_ERROR | SWT.OK | SWT.APPLICATION_MODAL);
						
						popup.setText("SnipMatch");
						popup.setMessage(error);
						popup.open();
					}
				});
				
				if (onFail != null)
					PlatformUI.getWorkbench().getDisplay().asyncExec(onFail);
			}
		});
	}
}
