package org.eclipse.recommenders.tests.ui;

import static org.eclipse.recommenders.tests.ui.Eclipse.openPreferences;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(SWTBotJunit4ClassRunner.class)
public class ShowPreferencePagesTest {

    private static SWTWorkbenchBot bot;

    @BeforeClass
    public static void beforeClass() throws Exception {
        bot = new SWTWorkbenchBot();
    }

    @Test
    public void test() {
        SWTBotShell shell = openPreferences(bot);
        bot.tree().select("Code Recommenders").expandNode("Code Recommenders").select("Calls").select("Extdoc");
        shell.close();
    }
}
