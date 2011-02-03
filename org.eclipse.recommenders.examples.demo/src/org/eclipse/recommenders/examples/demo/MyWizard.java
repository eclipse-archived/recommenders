/**
 * Copyright (c) 2010 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.examples.demo;

import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Button;

/**
 * Tools to show:
 * <ul>
 * <li>Call & Overrides Recommender
 * <li>Dynamic Code Templates Recommender
 * <li>Extended Documentation
 * </ul>
 */
public class MyWizard extends Wizard {

    Button check;

    // trigger code completion here to see overrides recommender offering you to
    // override the methods #addPages and others:
    @Override
    public void addPages() {
    };

    @Override
    public boolean canFinish() {
        // TODO Auto-generated method stub
        return super.canFinish();
    }

    @Override
    public boolean performFinish() {
        return false;
    }
}
