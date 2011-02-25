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

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.Assert;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEclipseEditor;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;

import com.google.common.collect.Lists;

public class TestProjectClassesHelper {

    private final SWTWorkbenchBot bot;
    private String projectName;
    private final boolean stopOnError;
    private final List<Throwable> errors = Lists.newLinkedList();

    public TestProjectClassesHelper(final SWTWorkbenchBot bot) {
        this(bot, true);
    }

    public TestProjectClassesHelper(final SWTWorkbenchBot bot, final boolean stopOnError) {
        this.bot = bot;
        this.stopOnError = stopOnError;
    }

    public void searchAndTestClasses(final String projectName) {
        this.projectName = projectName;
        final SWTBotTreeItem srcNode = findSourceNode();
        searchAndTestClasses(srcNode.getItems());
        if (errors.size() > 0) {
            printErrors();
        }
    }

    private SWTBotTreeItem findSourceNode() {
        final SWTBotView projectExplorerView = bot.viewByTitle("Package Explorer");
        final SWTBotTreeItem projectTreeItem = projectExplorerView.bot().tree().expandNode(projectName);
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
            handleError(e, item.getText());
        } catch (final AssertionError e) {
            handleError(e, item.getText());
        }
    }

    private void handleError(final Throwable t, final String filename) {
        final RuntimeException exception = new RuntimeException("Test failed for file: " + filename, t);
        if (stopOnError) {
            throw exception;
        } else {
            errors.add(exception);
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
            final int column = searchMarker(line);
            if (column >= 0) {
                editor.navigateTo(lineNumber, column);
                final String proposalExpression = findProposalExpression(line);
                CompletionUtil.triggerCodeCompletion(editor, bot, proposalExpression);
            }

            lineNumber++;
        }
    }

    private int searchMarker(final String line) {
        final int markerPosition = line.indexOf("<^Space");
        if (markerPosition >= 0) {
            final int commentPosition = line.indexOf("//");
            if (commentPosition >= 0 && commentPosition < markerPosition) {
                return commentPosition;
            } else {
                return markerPosition;
            }
        }

        return -1;
    }

    private String findProposalExpression(final String line) {
        final Pattern pattern = Pattern.compile("\\<\\^Space\\|(.+?)\\>");
        final Matcher matcher = pattern.matcher(line);
        matcher.find();
        return matcher.group(1);
    }

    private void printErrors() {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final PrintStream printStream = new PrintStream(baos);
        for (final Throwable error : errors) {
            error.printStackTrace(printStream);
        }

        Assert.fail("Multiple errors occured: \n" + baos.toString());
    }
}
