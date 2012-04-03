package org.eclipse.recommenders.tests.ui;

import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;

public class Eclipse {

    public static SWTBotShell openPreferences(SWTBot bot) {
        PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
            @Override
            public void run() {
                IWorkbenchWindow workbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
                ActionFactory.PREFERENCES.create(workbenchWindow).run();
            }
        });
        return bot.shell("Preferences").activate();
    }

    public static void createProject(String name, SWTBot bot) {
        bot.menu("File").menu("New").menu("Java Project").click();
        SWTBotShell s = bot.shell("New Java Project");
        s.activate();
        bot.textWithLabel("Project name:").setText(name);
        bot.button("Finish").click();
        s.close();
    }

    public static void switchToJavaPerspective(SWTBot bot) {
        // Change the perspective via the Open Perspective dialog
        bot.menu("Window").menu("Open Perspective").menu("Other...").click();
        SWTBotShell openPerspectiveShell = bot.shell("Open Perspective");
        openPerspectiveShell.activate();

        // select the dialog
        bot.table().select("Java");
        bot.button("OK").click();
    }

}
