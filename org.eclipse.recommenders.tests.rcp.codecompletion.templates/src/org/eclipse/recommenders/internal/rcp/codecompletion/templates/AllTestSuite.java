package org.eclipse.recommenders.internal.rcp.codecompletion.templates;

import org.eclipse.recommenders.internal.rcp.codecompletion.templates.ui.UiTestSuite;
import org.eclipse.recommenders.internal.rcp.codecompletion.templates.unit.UnitTestSuite;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ UnitTestSuite.class, UiTestSuite.class })
public class AllTestSuite {

}
