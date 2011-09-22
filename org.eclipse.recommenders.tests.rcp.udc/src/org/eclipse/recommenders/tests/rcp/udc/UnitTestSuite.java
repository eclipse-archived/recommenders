/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marcel Bruch - Initial API and implementation
 */
package org.eclipse.recommenders.tests.rcp.udc;

import org.eclipse.recommenders.internal.depersonalizer.LineNumberDepersonalizerTest;
import org.eclipse.recommenders.internal.depersonalizer.NameDepersonalizerTest;
import org.eclipse.recommenders.internal.depersonalizer.ObjectUsageFilterTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ LineNumberDepersonalizerTest.class, NameDepersonalizerTest.class, ObjectUsageFilterTest.class })
public class UnitTestSuite {

}
