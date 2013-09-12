/**
 * Copyright (c) 2010, 2013 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.completion.rcp.processable;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.DecorationOverlayIcon;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.swt.graphics.Image;

public class Proposals {

    public static void overlay(IProcessableProposal proposal, ImageDescriptor icon) {
        Image originalImage = proposal.getImage();
        DecorationOverlayIcon decorator = new DecorationOverlayIcon(originalImage, icon, IDecoration.TOP_LEFT);
        proposal.setImage(decorator.createImage());
    }
}
