/**
 * Copyright (c) 2011 Stefan Henss.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stefan Henß - initial API and implementation.
 *    Olav Lenz - externalize Strings.
 */
package org.eclipse.recommenders.internal.chain.rcp;

import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.ui.IJavaElementSearchConstants;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.ListEditor;
import org.eclipse.recommenders.utils.Throws;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.dialogs.SelectionDialog;

import com.google.common.base.Joiner;

public class ChainsPreferencePage extends org.eclipse.jface.preference.FieldEditorPreferencePage implements
        IWorkbenchPreferencePage {

    public static final String PREF_MAX_CHAINS = "recommenders.chain.max_chains"; //$NON-NLS-1$
    public static final String PREF_MIN_CHAIN_LENGTH = "recommenders.chain.min_chain_length"; //$NON-NLS-1$
    public static final String PREF_MAX_CHAIN_LENGTH = "recommenders.chain.max_chain_length"; //$NON-NLS-1$
    public static final String PREF_TIMEOUT = "recommenders.chain.timeout"; //$NON-NLS-1$
    public static final String PREF_IGNORED_TYPES = "recommenders.chain.ignore_types"; //$NON-NLS-1$
    public static final String PREF_ENABLE_QUICK_ASSIST_CHAINS = "recommenders.chain.enable_quick_assist_chains"; //$NON-NLS-1

    public static final char IGNORE_TYPES_SEPARATOR = '|';

    public ChainsPreferencePage() {
        super(GRID);
    }

    @Override
    public void init(final IWorkbench workbench) {
        setDescription(Messages.PREFPAGE_DESCRIPTION_CHAINS);
        setPreferenceStore(ChainRcpPlugin.getDefault().getPreferenceStore());
    }

    @Override
    protected void createFieldEditors() {
        addField(PREF_MAX_CHAINS, Messages.FIELD_LABEL_MAX_CHAINS, 1, 99);
        addField(PREF_MIN_CHAIN_LENGTH, Messages.FIELD_LABEL_MIN_CHAIN_LENGTH, 1, 10);
        addField(PREF_MAX_CHAIN_LENGTH, Messages.FIELD_LABEL_MAX_CHAIN_LENGTH, 1, 10);
        addField(PREF_TIMEOUT, Messages.FIELD_LABEL_TIMEOUT, 1, 99);

        addField(new IgnoredTypesEditor(Messages.FIELD_LABEL_IGNORED_TYPES, getFieldEditorParent()));

        addText(Messages.PREFPAGE_FOOTER_IGNORED_TYPES_WARNING);
        addField(new BooleanFieldEditor(PREF_ENABLE_QUICK_ASSIST_CHAINS, Messages.FIELD_ENABLE_QUICK_ASSIST_CHAINS,
                getFieldEditorParent()));
    }

    private void addField(final String name, final String labeltext, final int min, final int max) {
        final IntegerFieldEditor field = new IntegerFieldEditor(name, labeltext, getFieldEditorParent());
        field.setValidRange(min, max);
        addField(field);
    }

    private void addText(final String text) {
        final Label label = new Label(getFieldEditorParent(), SWT.WRAP);
        label.setText(text);
        label.setLayoutData(GridDataFactory.fillDefaults().span(2, 1).create());
    }

    private static final class IgnoredTypesEditor extends ListEditor {

        IgnoredTypesEditor(final String label, final Composite parent) {
            super(PREF_IGNORED_TYPES, label, parent);
        }

        @Override
        protected void doFillIntoGrid(final Composite parent, final int numColumns) {
            super.doFillIntoGrid(parent, numColumns);
        }

        @Override
        protected String[] parseString(final String stringList) {
            getUpButton().setVisible(false);
            getDownButton().setVisible(false);
            return stringList.split("\\" + IGNORE_TYPES_SEPARATOR); //$NON-NLS-1$
        }

        @Override
        protected String getNewInputObject() {
            try {
                final SelectionDialog dialog = JavaUI.createTypeDialog(getShell(),
                        new ProgressMonitorDialog(getShell()), SearchEngine.createWorkspaceScope(),
                        IJavaElementSearchConstants.CONSIDER_ALL_TYPES, false);
                if (dialog.open() == IDialogConstants.CANCEL_ID) {
                    return null;
                }
                return ((IType) dialog.getResult()[0]).getFullyQualifiedName();
            } catch (final JavaModelException e) {
                Throws.throwIllegalArgumentException(e.getMessage());
                return null;
            }
        }

        @Override
        protected String createList(final String[] items) {
            return Joiner.on(IGNORE_TYPES_SEPARATOR).join(items);
        }
    }
}
