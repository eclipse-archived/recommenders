/**
 * Copyright (c) 2012 Cheng Chen
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *    Cheng Chen - initial API and implementation and/or initial documentation
*/

package org.eclipse.recommenders.snipmatch.preferences;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.recommenders.snipmatch.rcp.SnipMatchPlugin;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * This class represents a preference page that is used to set up whether to use
 * local search engine or remote search engine .
 * <p>
 * This page is used to modify preferences only. They are stored in the
 * preference store that belongs to the main plug-in class. That way,
 * preferences can be accessed directly via the preference store.
 */

public class SnipMatchPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public SnipMatchPreferencePage() {
		super(GRID);
		setPreferenceStore(SnipMatchPlugin.getDefault().getPreferenceStore());
		setDescription("Setting pages for Recommenders SnipMatch");
	}

	/**
	 * Creates the local search engine & remote search engine selection
	 * checkbox.
	 */
	public void createFieldEditors() {

	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {

	}

}
