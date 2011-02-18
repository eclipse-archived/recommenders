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
package org.eclipse.recommenders.tests.commons.ui.utils;

import org.eclipse.swt.SWT;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEclipseEditor;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTable;
import org.junit.Assert;

public class CompletionUtil {

    public static void triggerCodeCompletion(final SWTBotEclipseEditor editor, final SWTWorkbenchBot bot,
            final String proposalExpression) {
        final SWTBotShell completionShell = triggerCodeCompletion(editor, bot);
        selectProposal(completionShell, proposalExpression);
        closeCompletionShell(editor, completionShell);
    }

    public static SWTBotShell triggerCodeCompletion(final SWTBotEclipseEditor editor, final SWTWorkbenchBot bot) {
        final SWTBotShell[] shellsBeforeCompletion = bot.shells();
        editor.setFocus();
        editor.pressShortcut(SWT.CTRL, ' ');
        bot.sleep(500);
        final SWTBotShell[] shellsAfterCompletion = bot.shells();
        final SWTBotShell completionShell = findNewShell(shellsBeforeCompletion, shellsAfterCompletion);
        completionShell.activate();
        return completionShell;
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

    private static void selectProposal(final SWTBotShell completionShell, final String proposalExpression) {
        final StringBuffer availableProposals = new StringBuffer();
        final SWTBotTable table = completionShell.bot().table();
        int matches = 0;
        int matchingRow = -1;

        for (int row = 0; row < table.rowCount(); row++) {
            final String cell = table.cell(row, 0);
            availableProposals.append(cell + "\n");
            if (cell.matches(proposalExpression)) {
                matchingRow = row;
                matches++;
            }
        }

        if (matches == 0) {
            Assert.fail("Proposal not found: " + proposalExpression + "\nAvailable Proposals:\n" + availableProposals);
        } else if (matches > 1) {
            Assert.fail("Regular expression for proposal selection did match more than one proposal. Matches: "
                    + matches + "; Regex: " + proposalExpression + "\nAvailable Proposals:\n" + availableProposals);
        } else {
            table.setFocus();
            table.doubleClick(matchingRow, 0);
            table.pressShortcut(SWT.CR, SWT.LF);
        }
    }

    private static void closeCompletionShell(final SWTBotEclipseEditor editor, final SWTBotShell completionShell) {
        editor.setFocus();
    }
}
