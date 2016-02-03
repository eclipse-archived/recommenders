/**
 * Copyright (c) 2015 Pawel Nowak.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.recommenders.internal.news.rcp;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.recommenders.news.rcp.IFeedMessage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.google.common.collect.Lists;

@RunWith(Parameterized.class)
public class MessageUtilsGetUnreadMessagesNumberTest {
    private static final int EXPECT_ZERO = 0;
    private static final int EXPECT_ONE = 1;
    private static final int EXPECT_TWO = 2;

    private final List<IFeedMessage> inputMessages;
    private final int expectedResult;

    public MessageUtilsGetUnreadMessagesNumberTest(List<IFeedMessage> inputMessages, int expectedResult) {
        this.inputMessages = inputMessages;
        this.expectedResult = expectedResult;
    }

    @Parameters
    public static Collection<Object[]> scenarios() {
        List<Object[]> scenarios = Lists.newArrayList();

        scenarios.add(new Object[] { null, EXPECT_ZERO });
        scenarios.add(new Object[] { Collections.emptyList(), EXPECT_ZERO });
        scenarios.add(new Object[] { TestUtils.mockMessagesAsList(true), EXPECT_ZERO });
        scenarios.add(new Object[] { TestUtils.mockMessagesAsList(false), EXPECT_ONE });
        scenarios.add(new Object[] { TestUtils.mockMessagesAsList(true, false, false), EXPECT_TWO });

        return scenarios;
    }

    @Test
    public void testGetUnreadMessages() {
        assertThat(MessageUtils.getUnreadMessagesNumber(inputMessages), is(expectedResult));
    }
}
