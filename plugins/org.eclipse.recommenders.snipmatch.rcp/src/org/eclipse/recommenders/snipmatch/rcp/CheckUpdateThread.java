/**
 * Copyright (c) 2011 Doug Wightman, Zi Ye
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
*/

package org.eclipse.recommenders.snipmatch.rcp;

import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.eclipse.recommenders.snipmatch.search.SearchClient;
import org.eclipse.recommenders.snipmatch.web.PostThread;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.PlatformUI;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * Asynchronously check for a newer version of the plugin, and notify the user if a newer version exists.
 */
public class CheckUpdateThread extends PostThread {

	public CheckUpdateThread(SearchClient client) {
		super(client, SnipMatchPlugin.UPDATE_SITE_INFO_URL);
	}
	
	@Override
	public void run() {
	
		InputStream response = post();

		DocumentBuilderFactory dbf;
		DocumentBuilder db;
		Document siteXml;

		try {
			dbf = DocumentBuilderFactory.newInstance();
			db = dbf.newDocumentBuilder();
			siteXml = db.parse(response);
		}
		catch (Exception e) {
			e.printStackTrace();
			return;
		}
		
		Element featureNode =
			(Element)siteXml.getElementsByTagName("feature").item(0);
		String version = featureNode.getAttribute("version");
		
		int siteVersionInt = Integer.parseInt(version.replaceAll("\\.", ""));
		int clientVersionInt = Integer.parseInt(
				client.getVersion().replaceAll("\\.", ""));
		
		if (siteVersionInt > clientVersionInt) {
			
			PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
				
				@Override
				public void run() {
					
					MessageBox popup = new MessageBox(PlatformUI.getWorkbench()
							.getActiveWorkbenchWindow().getShell(),
							SWT.ICON_INFORMATION | SWT.OK | SWT.APPLICATION_MODAL);
					
					popup.setText("SnipMatch Update");
					popup.setMessage("A new version of SnipMatch is available! " +
							"Please go to Help > Check for Updates in the " +
							"Eclipse menu to obtain the update.");
					popup.open();
				}
			});
		}
	}
}
