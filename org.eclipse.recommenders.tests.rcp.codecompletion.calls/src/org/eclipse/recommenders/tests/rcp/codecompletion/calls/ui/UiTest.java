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

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEclipseEditor;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEditor;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(SWTBotJunit4ClassRunner.class)
public class UiTest {

    private static SWTWorkbenchBot bot;

    @BeforeClass
    public static void beforeClass() {
        SWTBotPreferences.KEYBOARD_LAYOUT = "EN_US";
        bot = new SWTWorkbenchBot();
        // bot.viewByTitle("Welcome").close();
    }

    @AfterClass
    public static void sleep() {
        bot.sleep(1000);
        final List<? extends SWTBotEditor> editors = bot.editors();
        for (final SWTBotEditor editor : editors) {
            editor.saveAndClose();
        }
    }

    @Test
    public void testCalls() throws Exception {
        final String projectName = "org.eclipse.recommenders.tests.fixtures.rcp.codecompletion.calls";
        UiTestHelper.copyProjectToWorkspace(projectName);

        final SWTBotView projectExplorerView = bot.viewByTitle("Project Explorer");
        final SWTBotTreeItem srcNode = projectExplorerView.bot().tree().expandNode(projectName).getNode("src");
        srcNode.expand();
        openAndTestFiles(srcNode.getItems());
    }

    private void testFile(final String file) {
        final SWTBotEclipseEditor editor = bot.editorByTitle(file).toTextEditor();
        bot.sleep(1000);
        int lineNumber = 0;
        for (final String line : editor.getLines()) {
            final int column = line.indexOf("<^Space");
            if (column >= 0) {
                System.out.println(lineNumber + ":" + column + "-> " + line);
                editor.navigateTo(lineNumber, column);

                final Pattern pattern = Pattern.compile("\\<\\^Space\\|(.+?)\\>");
                final Matcher matcher = pattern.matcher(line);
                matcher.find();
                final String method = matcher.group(1);

                UiTestHelper.triggerCodeCompletion(editor, bot, method);

                editor.setFocus();
            }

            lineNumber++;
        }

        final CompletionVerifier verifier = new CompletionVerifier(editor.getLines());
        verifier.verify();
        editor.saveAndClose();
    }

    private void openAndTestFiles(final SWTBotTreeItem[] items) {
        for (int i = 0; i < items.length; i++) {
            if (isJavaFile(items[i])) {
                openAndTestFile(items[i]);
            } else {
                items[i].expand();
                openAndTestFiles(items[i].getItems());
            }
        }
    }

    private boolean isJavaFile(final SWTBotTreeItem item) {
        return item.getText().endsWith(".java");
    }

    private void openAndTestFile(final SWTBotTreeItem item) {
        System.out.println(item.getText());
        item.setFocus();
        item.select();
        item.doubleClick();
        try {
            testFile(item.getText());
        } catch (final Exception e) {
            throw new RuntimeException("Test failed for file: " + item.getText(), e);
        } catch (final AssertionError e) {
            throw new RuntimeException("Test failed for file: " + item.getText(), e);
        }
    }
}
