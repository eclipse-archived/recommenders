/**
 * Copyright (c) 2011 Doug Wightman, Zi Ye
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
*/

package org.eclipse.recommenders.snipmatch.rcp;

import java.util.ArrayList;

import org.eclipse.recommenders.snipmatch.core.Effect;
import org.eclipse.recommenders.snipmatch.web.IDeleteEffectListener;
import org.eclipse.recommenders.snipmatch.web.ILoadProfileListener;
import org.eclipse.recommenders.snipmatch.web.MatchClient;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;



/**
 * This is the interface that shows the user's own created snippets, and offers controls
 * to delete or edit them, or to create a new one.
 */
public class ProfileBox {
	
	private MatchClient client;
	private Shell shell;
	private List effectList;
	private Button newButton;
	private Button editButton;
	private Button deleteButton;
	private Button okButton;
	private ArrayList<Effect> effects;
	
	public ProfileBox(MatchClient client) {
		
		this.client = client;
		this.effects = new ArrayList<Effect>();
	}

	public void show() {
		
		if (shell != null && !shell.isDisposed()) shell.dispose();

		shell = new Shell(PlatformUI.getWorkbench().
				getActiveWorkbenchWindow().getShell(), SWT.CLOSE);

		shell.setText("My Snippets");
		shell.setSize(500, 400);
		
		shell.addDisposeListener(new DisposeListener() {
			
			@Override
			public void widgetDisposed(DisposeEvent e) {
				
				client.cancelWork();
			}
		});
		
		FormLayout fl = new FormLayout();
		fl.marginWidth = 5;
		fl.marginHeight = 5;
		fl.spacing = 5;
		shell.setLayout(fl);
		
		FormData fd = new FormData();
		fd.left = new FormAttachment(0);
		fd.top = new FormAttachment(0);
		fd.right = new FormAttachment(100, -100);
		fd.bottom = new FormAttachment(100);
		
		effectList = new List(shell, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL);
		effectList.setLayoutData(fd);
		effectList.setEnabled(false);
		effectList.add("Loading...");
		
		effectList.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				selectEffect();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				selectEffect();
			}
			
			private void selectEffect() {

				int[] selection = effectList.getSelectionIndices();
				editButton.setEnabled(selection.length == 1);
				deleteButton.setEnabled(selection.length >= 1);
			}
		});
		
		fd = new FormData();
		fd.left = new FormAttachment(effectList);
		fd.top = new FormAttachment(0);
		fd.right = new FormAttachment(100);
		
		newButton = new Button(shell, SWT.PUSH);
		newButton.setText("New");
		newButton.setToolTipText("Submit new snippet to server.");
		newButton.setLayoutData(fd);
		
		newButton.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {

				shell.close();
				SnipMatchPlugin.getDefault().showSubmitBox();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {

				shell.close();
				SnipMatchPlugin.getDefault().showSubmitBox();
			}
		});
		
		fd = new FormData();
		fd.left = new FormAttachment(effectList);
		fd.top = new FormAttachment(newButton);
		fd.right = new FormAttachment(100);
		
		editButton = new Button(shell, SWT.PUSH);
		editButton.setText("Edit");
		editButton.setToolTipText("Edit snippet.");
		editButton.setLayoutData(fd);
		editButton.setEnabled(false);
		
		editButton.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				editSelected();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				editSelected();
			}
			
			private void editSelected() {

				Effect effect = effects.get(effectList.getSelectionIndex());
				shell.close();
				SnipMatchPlugin.getDefault().showSubmitBox(effect);
			}
		});
		
		fd = new FormData();
		fd.left = new FormAttachment(effectList);
		fd.top = new FormAttachment(editButton);
		fd.right = new FormAttachment(100);
		
		deleteButton = new Button(shell, SWT.PUSH);
		deleteButton.setText("Delete");
		deleteButton.setToolTipText("Delete snippet(s) from server.");
		deleteButton.setLayoutData(fd);
		deleteButton.setEnabled(false);
		
		deleteButton.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				deleteSelected();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				deleteSelected();
			}
			
			private void deleteSelected() {
				
				PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
					
					@Override
					public void run() {

						MessageBox popup = new MessageBox(PlatformUI.getWorkbench()
								.getActiveWorkbenchWindow().getShell(),
								SWT.ICON_QUESTION | SWT.YES | SWT.NO | SWT.APPLICATION_MODAL);
						
						popup.setText("SnipMatch");
						popup.setMessage("Permanently delete snippet(s) from server?");
						
						if (popup.open() != SWT.YES) return;
						
						int[] selection = effectList.getSelectionIndices();

						for (int i : selection) {
							
							Effect effect = effects.get(i);
							client.startDeleteEffect(effect, new IDeleteEffectListener() {
								
								@Override
								public void deleteEffectSucceeded() {
									
									PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
										
										@Override
										public void run() {

											MessageBox popup = new MessageBox(PlatformUI.getWorkbench()
													.getActiveWorkbenchWindow().getShell(),
													SWT.ICON_INFORMATION | SWT.OK | SWT.APPLICATION_MODAL);
											
											popup.setText("SnipMatch");
											popup.setMessage("Snippet successfully deleted.");
											popup.open();
											
											populate();
										}
									});
								}
								
								@Override
								public void deleteEffectFailed(final String error) {
									
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
								}
							});
						}
					}
				});
			}
		});

		fd = new FormData();
		fd.left = new FormAttachment(effectList);
		fd.bottom = new FormAttachment(100);
		fd.right = new FormAttachment(100);
		
		okButton = new Button(shell, SWT.PUSH);
		okButton.setText("OK");
		okButton.setLayoutData(fd);
		
		okButton.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				shell.close();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				shell.close();
			}
		});
		
		shell.open();
	}
	
	/**
	 * This actually populates the list of snippets. Only called if the user is logged in as a non-guest.
	 */
	public void populate() {
		
		effectList.removeAll();
		effects.clear();
		editButton.setEnabled(false);
		deleteButton.setEnabled(false);

		client.startLoadProfile(new ILoadProfileListener() {
			
			@Override
			public void loadProfileSucceeded() {
				
				PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
					
					@Override
					public void run() {

						if (!effectList.isEnabled()) {
							effectList.setEnabled(true);
							effectList.removeAll();
						}
					}
				});
			}
			
			@Override
			public void loadProfileFailed(final String error) {
				
				PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
					
					@Override
					public void run() {

						MessageBox popup = new MessageBox(PlatformUI.getWorkbench()
								.getActiveWorkbenchWindow().getShell(),
								SWT.ICON_ERROR | SWT.OK | SWT.APPLICATION_MODAL);
						
						popup.setText("SnipMatch");
						popup.setMessage(error);
						popup.open();
						
						shell.close();
					}
				});
			}
	
			@Override
			public void effectLoaded(final Effect effect) {
				
				PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
					
					@Override
					public void run() {

						if (!effectList.isEnabled()) {
							effectList.setEnabled(true);
							effectList.removeAll();
						}
						
						// Only display the snippets for the Java Snippet match environment.
						if (effect.getEnvironmentName().equals(
								new JavaSnippetMatchEnvironment().getName())) {

							effects.add(effect);
							effectList.add(effect.getPattern(0));
						}
					}
				});
			}
		});
	}
	
	public void hide() {
		
		shell.close();
	}
}