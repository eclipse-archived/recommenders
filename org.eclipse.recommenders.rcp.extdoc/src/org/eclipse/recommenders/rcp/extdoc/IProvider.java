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
package org.eclipse.recommenders.rcp.extdoc;

import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.recommenders.commons.selection.IJavaElementSelection;
import org.eclipse.recommenders.commons.selection.JavaElementLocation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchWindow;

public interface IProvider extends IExecutableExtension {

    String getProviderName();

    String getProviderFullName();

    Image getIcon();

    boolean isAvailableForLocation(JavaElementLocation location);

    Composite createComposite(Composite parent, IWorkbenchWindow workbenchWindow);

    boolean selectionChanged(IJavaElementSelection context, Composite composite);

}
