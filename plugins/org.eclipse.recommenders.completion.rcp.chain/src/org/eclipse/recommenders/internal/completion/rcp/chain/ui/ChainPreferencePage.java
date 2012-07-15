/**
 * Copyright (c) 2011 Stefan Henss.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stefan Henß - initial API and implementation.
 */
package org.eclipse.recommenders.internal.completion.rcp.chain.ui;

import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.ui.IJavaElementSearchConstants;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.ListEditor;
import org.eclipse.recommenders.internal.completion.rcp.chain.ChainCompletionPlugin;
import org.eclipse.recommenders.utils.Throws;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.dialogs.SelectionDialog;

import com.google.common.base.Joiner;

public class ChainPreferencePage extends org.eclipse.jface.preference.FieldEditorPreferencePage implements
        IWorkbenchPreferencePage {

    public static final String ID_MAX_CHAINS = "recommenders.chain.max_chains";
    public static final String ID_MAX_DEPTH = "recommenders.chain.max_chain_length";
    public static final String ID_TIMEOUT = "recommenders.chain.timeout";
    public static final String ID_IGNORE_TYPES = "recommenders.chain.ignore_types";
    public static final char IGNORE_TYPES_SEPARATOR = '|';

    public ChainPreferencePage() {
        super(GRID);
        setPreferenceStore(ChainCompletionPlugin.getDefault().getPreferenceStore());
        setDescription("Call chains offer ways to obtain objects of the requested type by calling multiple methods in a row. "
                + "Since those chains can become long and time-consuming to search, the following options allow to limit the proposals.");
    }

    @Override
    protected void createFieldEditors() {
        addField(ID_MAX_CHAINS, "Maximum number of chains:", 1, 99);
        addField(ID_MAX_DEPTH, "Maximum chain depth:", 2, 99);
        addField(ID_TIMEOUT, "Chain search timeout (sec):", 1, 99);

        addField(new IgnoredTypesEditor("Return types to ignore:", getFieldEditorParent()));
    }

    private void addField(final String name, final String labeltext, final int min, final int max) {
        final IntegerFieldEditor field = new IntegerFieldEditor(name, labeltext, getFieldEditorParent());
        field.setValidRange(min, max);
        addField(field);
    }

    @Override
    public void init(final IWorkbench workbench) {
    }

    private static final class IgnoredTypesEditor extends ListEditor {

        IgnoredTypesEditor(final String label, final Composite parent) {
            super(ID_IGNORE_TYPES, label, parent);
        }

        @Override
        protected void doFillIntoGrid(final Composite parent, final int numColumns) {
            super.doFillIntoGrid(parent, numColumns);
        }

        @Override
        protected String[] parseString(final String stringList) {
            getUpButton().setVisible(false);
            getDownButton().setVisible(false);
            return stringList.split("\\" + IGNORE_TYPES_SEPARATOR);
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