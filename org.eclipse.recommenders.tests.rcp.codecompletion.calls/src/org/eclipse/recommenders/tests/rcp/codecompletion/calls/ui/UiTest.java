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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.recommenders.tests.commons.ui.utils.DefaultUiTest;
import org.eclipse.recommenders.tests.commons.ui.utils.FixtureUtil;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEclipseEditor;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(SWTBotJunit4ClassRunner.class)
public class UiTest extends DefaultUiTest {

    private static final String fixtureProjectName = "org.eclipse.recommenders.tests.fixtures.rcp.codecompletion.calls";

    @Test
    public void testClassesInFixtureProject() throws Exception {
        FixtureUtil.copyProjectToWorkspace(fixtureProjectName);
        final SWTBotTreeItem srcNode = findSourceNode();
        searchAndTestClasses(srcNode.getItems());
    }

    private SWTBotTreeItem findSourceNode() {
        final SWTBotView projectExplorerView = bot.viewByTitle("Package Explorer");
        final SWTBotTreeItem projectTreeItem = projectExplorerView.bot().tree().expandNode(fixtureProjectName);
        final SWTBotTreeItem srcNode = projectTreeItem.getNode("src");
        srcNode.expand();
        return srcNode;
    }

    private void searchAndTestClasses(final SWTBotTreeItem[] items) {
        for (int i = 0; i < items.length; i++) {
            if (isJavaFile(items[i])) {
                openAndTestFile(items[i]);
            } else {
                items[i].expand();
                searchAndTestClasses(items[i].getItems());
            }
        }
    }

    private boolean isJavaFile(final SWTBotTreeItem item) {
        return item.getText().endsWith(".java");
    }

    private void openAndTestFile(final SWTBotTreeItem item) {
        final SWTBotEclipseEditor editor = openClassFileEditor(item);

        try {
            testClassFile(editor);
        } catch (final Exception e) {
            throw new RuntimeException("Test failed for file: " + item.getText(), e);
        } catch (final AssertionError e) {
            throw new RuntimeException("Test failed for file: " + item.getText(), e);
        }
    }

    private SWTBotEclipseEditor openClassFileEditor(final SWTBotTreeItem item) {
        item.setFocus();
        item.select();
        item.doubleClick();
        return bot.editorByTitle(item.getText()).toTextEditor();
    }

    private void testClassFile(final SWTBotEclipseEditor editor) {
        bot.sleep(1000);
        searchMarkersAndInvokeCompletion(editor);
        new CompletionVerifier(editor.getLines()).verify();
        editor.saveAndClose();
    }

    private void searchMarkersAndInvokeCompletion(final SWTBotEclipseEditor editor) {
        int lineNumber = 0;
        for (final String line : editor.getLines()) {
            final int column = line.indexOf("<^Space");
            if (column >= 0) {
                editor.navigateTo(lineNumber, column);
                final String proposalExpression = findProposalExpression(line);
                CompletionUtil.triggerCodeCompletion(editor, bot, proposalExpression);
            }

            lineNumber++;
        }
    }

    private String findProposalExpression(final String line) {
        final Pattern pattern = Pattern.compile("\\<\\^Space\\|(.+?)\\>");
        final Matcher matcher = pattern.matcher(line);
        matcher.find();
        return matcher.group(1);
    }

}
