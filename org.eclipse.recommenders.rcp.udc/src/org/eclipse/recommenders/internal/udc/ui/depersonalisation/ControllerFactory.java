/**
 * Copyright (c) 2011 Andreas Frankenberger.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andreas Frankenberger - initial API and implementation.
 */
package org.eclipse.recommenders.internal.udc.ui.depersonalisation;

import org.eclipse.recommenders.commons.injection.InjectionService;

public class ControllerFactory {

    public DependencySelectionController createDependencySelectionController() {
        final DependencySelectionController controller = new DependencySelectionController();
        InjectionService.getInstance().injectMembers(controller);
        return controller;
    }

    public static ControllerFactory instance() {
        return new ControllerFactory();
    }

}
