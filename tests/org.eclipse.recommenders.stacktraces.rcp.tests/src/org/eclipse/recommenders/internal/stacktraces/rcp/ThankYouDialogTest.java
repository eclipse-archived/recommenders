/**
 * Copyright (c) 2014 Codetrails GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial implementation
 */
package org.eclipse.recommenders.internal.stacktraces.rcp;

import java.text.MessageFormat;
import java.util.Arrays;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.mockito.Mockito;

import com.google.common.base.Optional;

@RunWith(Parameterized.class)
public class ThankYouDialogTest {

    private static final String BUG_URL = "http://bug/bug42";
    private static final String BUG_INFORMATION = "Bug information";

    private static String MESSAGE_INFO = Messages.THANKYOUDIALOG_ADDITIONAL_INFORMATIONS + BUG_INFORMATION;

    private static String MESSAGE_END = Messages.THANKYOUDIALOG_PLEASE_NOTE_ADDITIONAL_PERMISSIONS
            + Messages.THANKYOUDIALOG_THANK_YOU_FOR_HELP;

    private static ReportState S_NEW_UNKNOWN_CREATED = newReportState(BUG_URL, BUG_INFORMATION, null,
            ReportState.UNKNOWN, ReportState.NEW, true);
    private static String M_TRACKED = MessageFormat.format(Messages.THANKYOUDIALOG_TRACKED_PLEASE_ADD_TO_CC, BUG_URL)
            + MESSAGE_INFO + MESSAGE_END;

    private static ReportState S_UNCONFIRMED_UNKNOWN = newReportState(BUG_URL, BUG_INFORMATION, null,
            ReportState.UNKNOWN, ReportState.UNCONFIRMED, false);
    private static ReportState S_NEW_UNKNOWN = newReportState(BUG_URL, BUG_INFORMATION, null, ReportState.UNKNOWN,
            ReportState.NEW, false);
    private static ReportState S_ASSIGNED_UNKNOWN = newReportState(BUG_URL, BUG_INFORMATION, null, ReportState.UNKNOWN,
            ReportState.ASSIGNED, false);
    private static String M_MATCHED = MessageFormat.format(Messages.THANKYOUDIALOG_MATCHED_PLEASE_ADD_TO_CC, BUG_URL)
            + MESSAGE_INFO + MESSAGE_END;

    private static ReportState S_RESOLVED_FIXED = newReportState(BUG_URL, BUG_INFORMATION, null, ReportState.FIXED,
            ReportState.RESOLVED, false);
    private static ReportState S_CLOSED_FIXED = newReportState(BUG_URL, BUG_INFORMATION, null, ReportState.FIXED,
            ReportState.CLOSED, false);
    private static String M_FIXED = MessageFormat.format(Messages.THANKYOUDIALOG_MARKED_FIXED, BUG_URL) + MESSAGE_INFO
            + MESSAGE_END;

    private static ReportState S_RESOLVED_DUPLICATE = newReportState(BUG_URL, BUG_INFORMATION, null,
            ReportState.DUPLICATE, ReportState.RESOLVED, false);
    private static ReportState S_CLOSED_DUPLICATE = newReportState(BUG_URL, BUG_INFORMATION, null,
            ReportState.DUPLICATE, ReportState.CLOSED, false);
    private static String M_DUPLICATE = MessageFormat.format(Messages.THANKYOUDIALOG_MARKED_DUPLICATE, BUG_URL)
            + MESSAGE_INFO + MESSAGE_END;

    private static ReportState S_RESOLVED_MOVED = newReportState(BUG_URL, BUG_INFORMATION, null, ReportState.MOVED,
            ReportState.RESOLVED, false);
    private static ReportState S_CLOSED_MOVED = newReportState(BUG_URL, BUG_INFORMATION, null, ReportState.MOVED,
            ReportState.CLOSED, false);
    private static String M_MOVED = MessageFormat.format(Messages.THANKYOUDIALOG_MARKED_MOVED, BUG_URL) + MESSAGE_INFO
            + MESSAGE_END;

    private static ReportState S_RESOLVED_WORKSFORME = newReportState(BUG_URL, BUG_INFORMATION, null,
            ReportState.WORKSFORME, ReportState.RESOLVED, false);
    private static ReportState S_CLOSED_WORKSFORME = newReportState(BUG_URL, BUG_INFORMATION, null,
            ReportState.WORKSFORME, ReportState.CLOSED, false);
    private static String M_VISIT = MessageFormat.format(Messages.THANKYOUDIALOG_NOT_ABLE_TO_REPRODUCE_PLEASE_VISIT,
            BUG_URL) + MESSAGE_INFO + MESSAGE_END;

    private static ReportState S_RESOLVED_WONTFIX = newReportState(BUG_URL, BUG_INFORMATION, null, ReportState.WONTFIX,
            ReportState.RESOLVED, false);
    private static ReportState S_CLOSED_WONTFIX = newReportState(BUG_URL, BUG_INFORMATION, null, ReportState.WONTFIX,
            ReportState.CLOSED, false);
    private static ReportState S_RESOLVED_INVALID = newReportState(BUG_URL, BUG_INFORMATION, null, ReportState.INVALID,
            ReportState.RESOLVED, false);
    private static ReportState S_CLOSED_INVALID = newReportState(BUG_URL, BUG_INFORMATION, null, ReportState.INVALID,
            ReportState.CLOSED, false);
    private static ReportState S_RESOLVED_NOT_ECLIPSE = newReportState(BUG_URL, BUG_INFORMATION, null,
            ReportState.NOT_ECLIPSE, ReportState.RESOLVED, false);
    private static ReportState S_CLOSED_NOT_ECLIPSE = newReportState(BUG_URL, BUG_INFORMATION, null,
            ReportState.NOT_ECLIPSE, ReportState.CLOSED, false);
    private static String M_MARKED_NORMAL = MessageFormat.format(Messages.THANKYOUDIALOG_MARKED_NORMAL, BUG_URL)
            + MESSAGE_INFO + MESSAGE_END;

    private static ReportState S_RESOLVED_UNKNOWN = newReportState(BUG_URL, BUG_INFORMATION, null, ReportState.UNKNOWN,
            ReportState.RESOLVED, false);
    private static ReportState S_CLOSED_UNKNOWN = newReportState(BUG_URL, BUG_INFORMATION, null, ReportState.UNKNOWN,
            ReportState.CLOSED, false);
    private static String M_UNKNWON = MessageFormat.format(Messages.THANKYOUDIALOG_MARKED_UNKNOWN, ReportState.UNKNOWN,
            BUG_URL) + MESSAGE_INFO + MESSAGE_END;

    private static ReportState S_UNKNOWN_STATUS = newReportState(BUG_URL, BUG_INFORMATION, null, ReportState.UNKNOWN,
            "any unknown server status", false);
    private static String M_UNKNOWN_RESPONSE = MessageFormat.format(
            Messages.THANKYOUDIALOG_RECEIVED_UNKNOWN_SERVER_RESPONSE, ReportState.UNKNOWN, BUG_URL)
            + MESSAGE_INFO
            + MESSAGE_END;

    private static ReportState S_NEED_INFO = newReportState(BUG_URL, BUG_INFORMATION,
            new String[] { ReportState.KEYWORD_NEEDINFO }, ReportState.UNKNOWN, ReportState.ASSIGNED, true);
    private static String M_NEED_INFO = MessageFormat.format(Messages.THANKYOUDIALOG_TRACKED_PLEASE_ADD_TO_CC, BUG_URL)
            + MESSAGE_INFO + MessageFormat.format(Messages.THANKYOUDIALOG_MATCHED_NEED_FURTHER_INFORMATION, BUG_URL)
            + MessageFormat.format(Messages.THANKYOUDIALOG_FURTHER_INFORMATION, BUG_INFORMATION) + MESSAGE_END;

    private static ReportState S_NEED_INFO_NULL = newReportState(BUG_URL, null,
            new String[] { ReportState.KEYWORD_NEEDINFO }, ReportState.UNKNOWN, ReportState.ASSIGNED, true);
    private static String M_NEED_INFO_NULL = MessageFormat.format(Messages.THANKYOUDIALOG_TRACKED_PLEASE_ADD_TO_CC,
            BUG_URL)
            + Messages.THANKYOUDIALOG_MATCHED_NEED_FURTHER_INFORMATION
            + MessageFormat.format(Messages.THANKYOUDIALOG_FURTHER_INFORMATION,
                    Messages.THANKYOUDIALOG_NO_FURTHER_INFORMATIONS) + MESSAGE_END;

    private ReportState state;
    private String expectedMessage;

    public ThankYouDialogTest(ReportState state, String expectedMessage) {
        this.state = state;
        this.expectedMessage = expectedMessage;
    }

    @Parameters
    public static Iterable<Object[]> parameters() {
        return Arrays.asList(new Object[][] { { S_NEW_UNKNOWN_CREATED, M_TRACKED },
                { S_UNCONFIRMED_UNKNOWN, M_MATCHED }, { S_NEW_UNKNOWN, M_MATCHED }, { S_ASSIGNED_UNKNOWN, M_MATCHED },
                { S_RESOLVED_FIXED, M_FIXED }, { S_CLOSED_FIXED, M_FIXED }, { S_RESOLVED_DUPLICATE, M_DUPLICATE },
                { S_CLOSED_DUPLICATE, M_DUPLICATE }, { S_RESOLVED_MOVED, M_MOVED }, { S_CLOSED_MOVED, M_MOVED },
                { S_RESOLVED_WORKSFORME, M_VISIT }, { S_CLOSED_WORKSFORME, M_VISIT },
                { S_RESOLVED_WONTFIX, M_MARKED_NORMAL }, { S_CLOSED_WONTFIX, M_MARKED_NORMAL },
                { S_RESOLVED_INVALID, M_MARKED_NORMAL }, { S_CLOSED_INVALID, M_MARKED_NORMAL },
                { S_RESOLVED_NOT_ECLIPSE, M_MARKED_NORMAL }, { S_CLOSED_NOT_ECLIPSE, M_MARKED_NORMAL },
                { S_RESOLVED_UNKNOWN, M_UNKNWON }, { S_CLOSED_UNKNOWN, M_UNKNWON },
                { S_UNKNOWN_STATUS, M_UNKNOWN_RESPONSE }, { S_NEED_INFO, M_NEED_INFO },
                { S_NEED_INFO_NULL, M_NEED_INFO_NULL } });
    }

    private static ReportState newReportState(String bugUrl, String information, String[] keywords, String resolved,
            String status, Boolean created) {
        ReportState mock = Mockito.mock(ReportState.class);
        Mockito.when(mock.getBugUrl()).thenReturn(Optional.fromNullable(bugUrl));
        Mockito.when(mock.getInformation()).thenReturn(Optional.fromNullable(information));
        Mockito.when(mock.getKeywords()).thenReturn(Optional.fromNullable(keywords));
        Mockito.when(mock.getResolved()).thenReturn(Optional.fromNullable(resolved));
        Mockito.when(mock.getStatus()).thenReturn(Optional.fromNullable(status));
        Mockito.when(mock.isCreated()).thenReturn(created);
        return mock;
    }

    @Test
    public void testMessageForState() {
        ThankYouDialog sut = new ThankYouDialog(null, state);

        String buildText = sut.buildText();
        Assert.assertThat(buildText, Matchers.is(expectedMessage));
    }
}
