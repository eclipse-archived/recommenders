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
package org.eclipse.recommenders.internal.rcp.extdoc.swt;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.recommenders.rcp.extdoc.ExtDocPlugin;
import org.eclipse.swt.graphics.Image;

abstract class AbstractAction extends Action {

    AbstractAction(final String text, final String icon, final int style) {
        this(text, ExtDocPlugin.getIcon(icon), style);
    }

    public AbstractAction(final String text, final Image icon, final int style) {
        super(text, style);
        setImageDescriptor(ImageDescriptor.createFromImage(icon));
        setToolTipText(text);
    }

}
