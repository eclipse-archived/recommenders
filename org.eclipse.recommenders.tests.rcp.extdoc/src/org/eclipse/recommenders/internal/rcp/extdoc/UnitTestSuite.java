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
package org.eclipse.recommenders.internal.rcp.extdoc;

import org.eclipse.recommenders.internal.rcp.extdoc.swt.ExtDocViewTest;
import org.eclipse.recommenders.internal.rcp.extdoc.swt.ProvidersCompositeTest;
import org.eclipse.recommenders.internal.rcp.extdoc.swt.ProvidersTableDropAdapterTest;
import org.eclipse.recommenders.internal.rcp.extdoc.swt.ProvidersTableSelectionListenerTest;
import org.eclipse.recommenders.internal.rcp.extdoc.swt.ProvidersTableTest;
import org.eclipse.recommenders.rcp.extdoc.AbstractLocationSensitiveProviderCompositeTest;
import org.eclipse.recommenders.rcp.extdoc.AbstractProviderCompositeTest;
import org.eclipse.recommenders.rcp.extdoc.SourceCodeAreaTest;
import org.eclipse.recommenders.rcp.extdoc.features.CommentsCompositeTest;
import org.eclipse.recommenders.rcp.extdoc.features.StarsRatingCompositeTest;
import org.eclipse.recommenders.rcp.extdoc.preferences.ExtDocPreferencePageTest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ ProviderStoreTest.class, UiManagerTest.class, ExtDocViewTest.class, ProvidersCompositeTest.class,
        ProvidersTableDropAdapterTest.class, ProvidersTableSelectionListenerTest.class, ProvidersTableTest.class,
        AbstractLocationSensitiveProviderCompositeTest.class, AbstractProviderCompositeTest.class,
        SourceCodeAreaTest.class, CommentsCompositeTest.class, StarsRatingCompositeTest.class,
        ExtDocPreferencePageTest.class })
public final class UnitTestSuite {
}
