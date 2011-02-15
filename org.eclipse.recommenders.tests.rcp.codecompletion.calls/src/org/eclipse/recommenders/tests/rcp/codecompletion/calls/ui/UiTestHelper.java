/**
 * Copyright (c) 2010 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Johannes Lerch - initial API and implementation.
 */
package org.eclipse.recommenders.tests.rcp.codecompletion.calls.ui;

import static org.junit.Assert.assertFalse;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.SWT;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEclipseEditor;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTable;
import org.junit.Assert;

public class UiTestHelper {

    private static File fixturesBasedir = new File("../org.eclipse.recommenders.tests.fixtures/projects/");

    public static IProject copyProjectToWorkspace(final String projectName) throws IOException, CoreException {
        final IWorkspaceRoot root = getWorkspaceRoot();
        final File wsDir = root.getLocation().toFile();
        final File destDir = new File(wsDir, projectName);
        assertFalse(destDir.exists());
        final File srcDir = new File(fixturesBasedir, projectName);
        FileUtils.copyDirectory(srcDir, destDir);
        final IProject project = root.getProject(projectName);
        project.create(null);
        project.open(null);
        project.refreshLocal(IProject.DEPTH_INFINITE, null);
        project.build(IncrementalProjectBuilder.FULL_BUILD, null);
        return project;
    }

    public static IWorkspaceRoot getWorkspaceRoot() {
        final IWorkspace workspace = ResourcesPlugin.getWorkspace();
        final IWorkspaceRoot root = workspace.getRoot();
        return root;
    }

    private static SWTBotShell findNewShell(final SWTBotShell[] oldShells, final SWTBotShell[] newShells) {
        for (int i = 0; i < newShells.length; i++) {
            if (!contains(oldShells, newShells[i])) {
                return newShells[i];
            }
        }

        throw new IllegalArgumentException("No new shells in array.");
    }

    private static boolean contains(final SWTBotShell[] array, final SWTBotShell element) {
        for (int i = 0; i < array.length; i++) {
            if (array[i].widget == element.widget) {
                return true;
            }
        }
        return false;
    }

    public static void triggerCodeCompletion(final SWTBotEclipseEditor editor, final SWTWorkbenchBot bot,
            final String method) {
        final SWTBotShell completionShell = triggerCodeCompletion(editor, bot);
        final SWTBotTable table = completionShell.bot().table();
        int matches = 0;
        int matchingRow = -1;

        for (int row = 0; row < table.rowCount(); row++) {
            final String cell = table.cell(row, 0);
            if (cell.matches(method)) {
                matchingRow = row;
                matches++;
            }
        }

        if (matches == 0) {
            Assert.fail("Proposal not found: " + method);
        } else if (matches > 1) {
            Assert.fail("Regular expression for proposal selection did match more than one proposal. Matches: "
                    + matches + "; Regex: " + method);
        } else {
            table.setFocus();
            table.doubleClick(matchingRow, 0);
            table.pressShortcut(SWT.CR, SWT.LF);
        }
    }

    public static SWTBotShell triggerCodeCompletion(final SWTBotEclipseEditor editor, final SWTWorkbenchBot bot) {
        final SWTBotShell[] previousShells = bot.shells();

        editor.setFocus();
        editor.pressShortcut(SWT.CTRL, ' ');
        bot.sleep(500);
        final SWTBotShell completionShell = findNewShell(previousShells, bot.shells());
        completionShell.activate();
        return completionShell;
    }
}
