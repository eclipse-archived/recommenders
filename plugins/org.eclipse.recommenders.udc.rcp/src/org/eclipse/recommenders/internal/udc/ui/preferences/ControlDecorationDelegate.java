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
package org.eclipse.recommenders.internal.udc.ui.preferences;

/**
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andreas Frankenberger - initial API and implementation.
 */
import java.util.Map;

import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;

import com.google.common.collect.Maps;

public class ControlDecorationDelegate {
    private final Map<Control, ControlDecoration> decorators = Maps.newHashMap();

    public void setDecoratorText(final Control control, final String text) {
        ControlDecoration decoration = decorators.get(control);
        if (decoration == null) {
            decoration = createDecorator(control);
            decorators.put(control, decoration);
        }
        decoration.show();
        decoration.setDescriptionText(text);
    }

    public void clearDecorations() {
        for (final Control control : decorators.keySet()) {
            decorators.get(control).hide();
        }
    }

    ControlDecoration createDecorator(final Control control) {
        final ControlDecoration controlDecoration = new ControlDecoration(control, SWT.LEFT | SWT.TOP);
        final FieldDecoration fieldDecoration = FieldDecorationRegistry.getDefault().getFieldDecoration(
                FieldDecorationRegistry.DEC_ERROR);
        controlDecoration.setImage(fieldDecoration.getImage());
        return controlDecoration;
    }
}