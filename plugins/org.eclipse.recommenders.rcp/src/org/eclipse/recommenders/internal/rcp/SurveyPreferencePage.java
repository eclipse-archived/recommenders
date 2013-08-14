package org.eclipse.recommenders.internal.rcp;

import java.net.URL;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.recommenders.utils.Urls;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Link;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;

public class SurveyPreferencePage extends org.eclipse.jface.preference.PreferencePage implements
        IWorkbenchPreferencePage {

    private static final String PAGE_DESCRIPTION = "To help us develop Code Recommenders to your needs, we ask you for a few moments of your time to fill out our user survey.";
    private static final URL SURVEY_URL = Urls
            .toUrl("https://docs.google.com/a/codetrails.com/forms/d/1SqzZh1trpzS6UNEMjVWCvQTzGTBvjBFV-ZdwPuAwm5o/viewform");
    private static final String SURVEY_LINK_TEXT = "<a>Take the survey</a> (will open in a browser window).";

    @Override
    public void init(IWorkbench workbench) {
        setDescription(PAGE_DESCRIPTION);
    }

    @Override
    protected Control createContents(final Composite parent) {
        Composite container = new Composite(parent, SWT.NONE);

        container.setLayout(GridLayoutFactory.swtDefaults().margins(0, 0).create());
        Link surveyLink = new Link(container, SWT.NONE | SWT.WRAP);
        surveyLink
                .setLayoutData(GridDataFactory.swtDefaults().align(SWT.FILL, SWT.BEGINNING).grab(true, false)
                        .hint(convertHorizontalDLUsToPixels(IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH), SWT.DEFAULT)
                        .create());
        surveyLink.setText(SURVEY_LINK_TEXT);
        surveyLink.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent event) {
                try {
                    IWebBrowser browser = PlatformUI.getWorkbench().getBrowserSupport().getExternalBrowser();
                    browser.openURL(SURVEY_URL);
                } catch (PartInitException e) {
                    e.printStackTrace();
                }
            }
        });

        return container;
    }

}
