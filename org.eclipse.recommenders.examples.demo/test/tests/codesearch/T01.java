/**
 * Copyright (c) 2010 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */package tests.codesearch;

import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

public class T01 extends ViewPart {
	public void setMessage(final String newMessage) {
		//
		// == How do I get an instance of IStatusLineManager? ==
		//
		final IStatusLineManager manager = null;
		manager.setMessage(newMessage);
	}

	@Override
	public void createPartControl(final Composite arg0) {
	}

	@Override
	public void setFocus() {

	}
}
