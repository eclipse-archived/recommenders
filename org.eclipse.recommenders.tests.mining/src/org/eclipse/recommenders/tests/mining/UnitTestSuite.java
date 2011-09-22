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
package org.eclipse.recommenders.tests.mining;

import org.eclipse.recommenders.mining.calls.NetworkUtilsTest;
import org.eclipse.recommenders.mining.calls.NewObjectUsagesAvailablePredicateTest;
import org.eclipse.recommenders.mining.calls.TypeModelsBuilderTest;
import org.eclipse.recommenders.mining.calls.ZipModelSpecificationProviderTest;
import org.eclipse.recommenders.mining.extdoc.ClassOverridePatternsGeneratorTest;
import org.eclipse.recommenders.mining.extdoc.ClassOverridesDirectivesGeneratorTest;
import org.eclipse.recommenders.mining.extdoc.OverridesClustererTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ NetworkUtilsTest.class, NewObjectUsagesAvailablePredicateTest.class, TypeModelsBuilderTest.class,
        ZipModelSpecificationProviderTest.class, ClassOverridePatternsGeneratorTest.class,
        ClassOverridesDirectivesGeneratorTest.class, OverridesClustererTest.class })
public class UnitTestSuite {

}
