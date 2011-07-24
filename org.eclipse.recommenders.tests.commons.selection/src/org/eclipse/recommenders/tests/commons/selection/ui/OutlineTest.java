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

import org.eclipse.recommenders.commons.selection.IJavaElementSelection;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEclipseEditor;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(SWTBotJunit4ClassRunner.class)
public final class OutlineTest extends AbstractUiTest {

    private void testCommons(final SWTBotTreeItem item) {
        item.expand();
        item.select();
        final IJavaElementSelection context = getLastSelection();

        // Assert.assertEquals(-1, context.getInvocationOffset());

        // TODO ...
    }

    @Test
    public void testEditor() throws InterruptedException {
        for (final SWTBotTreeItem srcPackage : getSourceNode().getItems()) {
            for (final SWTBotTreeItem javaFile : srcPackage.getItems()) {
                javaFile.doubleClick();
                final SWTBotEclipseEditor editor = bot.editorByTitle(javaFile.getText()).toTextEditor();
                editor.selectCurrentLine();
                testJavaFile(bot.viewByTitle("Outline"));
            }
        }
    }

    private void testJavaFile(final SWTBotView outline) throws InterruptedException {
        for (final SWTBotTreeItem item : outline.bot().tree().getAllItems()) {
            testCommons(item);

            // TODO ...

            for (final SWTBotTreeItem classElement : item.getItems()) {
                testClassElement(classElement);
            }
        }
    }

    private void testClassElement(final SWTBotTreeItem javaClass) {
        testCommons(javaClass);
        // TODO ...
    }

}
