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

import java.io.IOException;
import java.util.List;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEditor;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;

@RunWith(SWTBotJunit4ClassRunner.class)
public abstract class DefaultUiTest {

    protected static SWTWorkbenchBot bot;

    @BeforeClass
    public static void beforeClass() throws IOException {
        initializeBot();
        tryCloseWelcomeScreen();
        openJavaPerspective();
    }

    private static void initializeBot() {
        SWTBotPreferences.KEYBOARD_LAYOUT = "EN_US";
        bot = new SWTWorkbenchBot();
    }

    private static void tryCloseWelcomeScreen() {
        try {
            bot.viewByTitle("Welcome").close();
        } catch (final Exception e) {
        }
    }

    private static void openJavaPerspective() {
        bot.menu("Window").menu("Open Perspective").menu("Other...").click();
        bot.shell("Open Perspective").activate();
        if (bot.table().containsItem("Java")) {
            bot.table().select("Java");
        } else {
            bot.table().select("Java (default)");
        }
        bot.button("OK").click();
    }

    @AfterClass
    public static void afterClass() {
        bot.sleep(1000);
        saveAndCloseAllEditors();
    }

    private static void saveAndCloseAllEditors() {
        final List<? extends SWTBotEditor> editors = bot.editors();
        for (final SWTBotEditor editor : editors) {
            editor.saveAndClose();
        }
    }
}
