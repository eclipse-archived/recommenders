/**
 * Copyright (c) 2011 Andreas Frankenberger.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andreas Frankenberger - initial API and implementation.
 */
package org.eclipse.recommenders.internal.udc.ui.depersonalisation;

/**
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andreas Frankenberger - initial API and implementation.
 */
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;

import org.eclipse.recommenders.internal.udc.depersonalizer.ICompilationUnitDepersonalizer;
import org.eclipse.recommenders.internal.udc.depersonalizer.LineNumberDepersonalizer;
import org.eclipse.recommenders.internal.udc.depersonalizer.NameDepersonalizer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class DepersonalisationController {
    public static String depersonalisationEnabledProperty = "depersonalisationEnabled";
    public static String errorMsgProperty = "errorMessage";
    private PreviewController previewController;

    private String errorMessage;

    private final PropertyChangeSupport support = new PropertyChangeSupport(this);
    private DepersonalisationComposite composite;

    public void addPropertyChangeListener(final PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    /**
     * @wbp.parser.entryPoint
     */
    public Control createControl(final Composite parent) {
        composite = new DepersonalisationComposite(parent, SWT.NONE);
        previewController = new PreviewController(composite.getCompareComposite());

        setDepersonalisationEnabled(false);
        addListeners();
        return composite;
    }

    private void addListeners() {
        composite.getDepersonalizeButton().addSelectionListener(createDepersonalisationSelectedListener());
    }

    private SelectionAdapter createDepersonalisationSelectedListener() {
        return new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                doDepersonalisationEnabled(composite.getDepersonalizeButton().getSelection());
            }
        };
    }

    protected void doDepersonalisationEnabled(final boolean enabled) {
        previewController.setDepersonalizers(getDepersonalizers());
    }

    public ICompilationUnitDepersonalizer[] getDepersonalizers() {
        final ArrayList<ICompilationUnitDepersonalizer> result = new ArrayList<ICompilationUnitDepersonalizer>();
        if (isDepersonalisationRequired()) {
            result.add(new NameDepersonalizer());
            result.add(new LineNumberDepersonalizer());
        }
        return result.toArray(new ICompilationUnitDepersonalizer[result.size()]);
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public boolean isDepersonalisationRequired() {
        return composite.getDepersonalizeButton().getSelection();
    }

    public void removePropertyChangeListener(final PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }

    public void setDepersonalisationEnabled(final boolean enabled) {
        composite.getDepersonalizeButton().setSelection(enabled);
        doDepersonalisationEnabled(enabled);
    }

}