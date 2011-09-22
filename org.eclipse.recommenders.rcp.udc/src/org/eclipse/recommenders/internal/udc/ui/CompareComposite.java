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
package org.eclipse.recommenders.internal.udc.ui;

/**
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andreas Frankenberger - initial API and implementation.
 */
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.IEncodedStreamContentAccessor;
import org.eclipse.compare.ITypedElement;
import org.eclipse.compare.contentmergeviewer.TextMergeViewer;
import org.eclipse.compare.structuremergeviewer.DiffNode;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.recommenders.commons.utils.Checks;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

public class CompareComposite extends Composite {
    String leftText;
    String rightText;
    TextMergeViewer viewer;

    static class CompareElement implements ITypedElement, IEncodedStreamContentAccessor {
        private final String content;

        public CompareElement(final String content) {
            this.content = content;
        }

        @Override
        public Image getImage() {
            return null;
        }

        @Override
        public String getName() {
            return "no name";
        }

        @Override
        public String getType() {
            return "txt";
        }

        @Override
        public String getCharset() throws CoreException {
            return "UTF-8";
        }

        @Override
        public InputStream getContents() throws CoreException {
            try {
                return new ByteArrayInputStream(content.getBytes("UTF-8")); //$NON-NLS-1$
            } catch (final UnsupportedEncodingException e) {
                return new ByteArrayInputStream(content.getBytes());
            }
        }

    }

    public CompareComposite(final Composite parent, final int style) {
        super(parent, style);
        this.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        this.setLayout(new FillLayout());
        viewer = new TextMergeViewer(this, createConfiguration());
    }

    private CompareConfiguration createConfiguration() {
        final CompareConfiguration configuration = new CompareConfiguration();
        configuration.setLeftEditable(false);
        configuration.setRightEditable(false);
        configuration.setRightLabel("Uploaded CompilationUnit");
        configuration.setLeftLabel("Local CompilationUnit");
        return configuration;
    }

    public void setLeftText(final String leftText) {
        Checks.ensureIsNotNull(leftText);
        this.leftText = leftText;
    }

    public void setRightText(final String rightText) {
        Checks.ensureIsNotNull(rightText);
        this.rightText = rightText;
        viewer.setInput(new DiffNode(new CompareElement(leftText), new CompareElement(rightText)));
    }
}
