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
package org.eclipse.recommenders.rcp.codecompletion.subwords;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ NGramUtilsTest.class, RegexMatcherTest.class, SubwordsCompletionRequestorTest.class,
        SubwordsCompletionProposalComputerTest.class, SubwordsRelevanceCalculatorTest.class, SubwordsUtilsTest.class,
        SubwordsCompletionContextTest.class })
public class UnitTestSuite {

}
