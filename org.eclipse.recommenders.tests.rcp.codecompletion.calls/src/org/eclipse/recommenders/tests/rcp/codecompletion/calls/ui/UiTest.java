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

import org.eclipse.recommenders.tests.commons.ui.utils.DefaultUiTest;
import org.eclipse.recommenders.tests.commons.ui.utils.FixtureUtil;
import org.eclipse.recommenders.tests.commons.ui.utils.TestProjectClassesHelper;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(SWTBotJunit4ClassRunner.class)
public class UiTest extends DefaultUiTest {

    private static final String fixtureProjectName = "org.eclipse.recommenders.tests.fixtures.rcp.codecompletion.calls";

    @Test
    public void testClassesInFixtureProject() throws Exception {
        FixtureUtil.copyProjectToWorkspace(fixtureProjectName);
        final TestProjectClassesHelper helper = new TestProjectClassesHelper(bot);
        helper.searchAndTestClasses(fixtureProjectName);
    }

}
