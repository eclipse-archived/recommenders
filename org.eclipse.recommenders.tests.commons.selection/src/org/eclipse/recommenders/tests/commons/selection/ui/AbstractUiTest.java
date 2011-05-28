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
package org.eclipse.recommenders.tests.commons.selection.ui;

import java.io.IOException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.recommenders.commons.internal.selection.SelectionPlugin;
import org.eclipse.recommenders.tests.commons.ui.utils.DefaultUiTest;
import org.eclipse.recommenders.tests.commons.ui.utils.FixtureUtil;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;

abstract class AbstractUiTest extends DefaultUiTest {

    private static final String FIXTUREPROJECT = "org.eclipse.recommenders.tests.fixtures.commons.selection";

    private static SWTBotTreeItem projectNode;
    private static SWTBotTreeItem srcNode;
    private static SelectionObserver observer;

    static {
        try {
            FixtureUtil.copyProjectToWorkspace(FIXTUREPROJECT);
        } catch (final IOException e) {
            e.printStackTrace();
        } catch (final CoreException e) {
            e.printStackTrace();
        }

        final SWTBotView packageExplorer = bot.viewByTitle("Package Explorer");

        projectNode = packageExplorer.bot().tree().getTreeItem(getProjectName());
        projectNode.expand();
        srcNode = projectNode.getNode("src");
        srcNode.expand();
    }

    static final String getProjectName() {
        return FIXTUREPROJECT;
    }

    static final SWTBotTreeItem getProjectNode() {
        return projectNode;
    }

    static final SWTBotTreeItem getSourceNode() {
        return srcNode;
    }

    static final SelectionObserver getObserver() {
        if (observer == null) {
            observer = new SelectionObserver();
            SelectionPlugin.addListener(observer);
        }
        return observer;
    }

}
