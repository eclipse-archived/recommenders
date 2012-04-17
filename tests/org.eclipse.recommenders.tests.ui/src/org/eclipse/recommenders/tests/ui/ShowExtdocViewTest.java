package org.eclipse.recommenders.tests.ui;

import static org.junit.Assert.assertTrue;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(SWTBotJunit4ClassRunner.class)
public class ShowExtdocViewTest {

    private static SWTWorkbenchBot bot;

    @BeforeClass
    public static void beforeClass() throws Exception {
        bot = new SWTWorkbenchBot();
        bot.perspectiveById("org.eclipse.jdt.ui.JavaPerspective").activate();
        bot.viewByTitle("Package Explorer").show();
        // bot.viewByTitle("Welcome").close();
        // Eclipse.switchToJavaPerspective(bot);
        Eclipse.createProject("test", bot);
    }

    @Test
    public void test() {
        bot.sleep(1000);
        bot.menu("Window").menu("Show View").menu("Other...").click();
        bot.shell("Show View").activate();
        bot.tree().expandNode("Recommenders").select("Extdoc View");
        bot.button("OK").click();
        assertTrue(bot.viewByTitle("Extdoc View").isActive());
    }

    @Test
    public void testClick() {
        bot.viewByTitle("Package Explorer").show();
        bot.sleep(1000);
        bot.tree().expandNode("test").getNode(1).expand().getNode(1).select();
        bot.sleep(2000);
    }
}
