/**
 * Copyright (c) 2014 Codetrails GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 *
 *  Based on https://github.com/awltech/eclipse-mylyn-notifications
 */
package org.eclipse.recommenders.internal.stacktraces.rcp.fadedialog;

import java.lang.reflect.Field;

import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Display;

/**
 * @author Mik Kersten
 * @since 3.0
 */
class CommonFonts {

    public static Font BOLD;

    public static Font ITALIC;

    public static Font BOLD_ITALIC;

    public static Font STRIKETHROUGH = null;

    public static boolean HAS_STRIKETHROUGH;

    static {
        if (Display.getCurrent() != null) {
            init();
        } else {
            Display.getDefault().asyncExec(new Runnable() {
                @Override
                public void run() {
                    init();
                }
            });
        }
    }

    private static void init() {
        BOLD = JFaceResources.getFontRegistry().getBold(JFaceResources.DEFAULT_FONT);
        ITALIC = JFaceResources.getFontRegistry().getItalic(JFaceResources.DEFAULT_FONT);
        BOLD_ITALIC = new Font(Display.getCurrent(), getModifiedFontData(ITALIC.getFontData(), SWT.BOLD | SWT.ITALIC));

        Font defaultFont = JFaceResources.getFontRegistry().get(JFaceResources.DEFAULT_FONT);
        FontData[] defaultData = defaultFont.getFontData();
        if (defaultData != null && defaultData.length == 1) {
            FontData data = new FontData(defaultData[0].getName(), defaultData[0].getHeight(),
                    defaultData[0].getStyle());

            if ("win32".equals(SWT.getPlatform())) { //$NON-NLS-1$
                // NOTE: Windows only, for: data.data.lfStrikeOut = 1;
                try {
                    Field dataField = data.getClass().getDeclaredField("data"); //$NON-NLS-1$
                    Object dataObject = dataField.get(data);
                    Class<?> clazz = dataObject.getClass().getSuperclass();
                    Field strikeOutFiled = clazz.getDeclaredField("lfStrikeOut"); //$NON-NLS-1$
                    strikeOutFiled.set(dataObject, (byte) 1);
                    CommonFonts.STRIKETHROUGH = new Font(Display.getCurrent(), data);
                } catch (Throwable t) {
                    // ignore
                }
            }
        }
        if (CommonFonts.STRIKETHROUGH == null) {
            CommonFonts.HAS_STRIKETHROUGH = false;
            CommonFonts.STRIKETHROUGH = defaultFont;
        } else {
            CommonFonts.HAS_STRIKETHROUGH = true;
        }
    }

    /**
     * NOTE: disposal of JFaceResources fonts handled by registry.
     */
    public static void dispose() {
        if (CommonFonts.STRIKETHROUGH != null && !CommonFonts.STRIKETHROUGH.isDisposed()) {
            CommonFonts.STRIKETHROUGH.dispose();
            CommonFonts.BOLD_ITALIC.dispose();
        }
    }

    /**
     * Copied from {@link FontRegistry}
     */
    private static FontData[] getModifiedFontData(FontData[] baseData, int style) {
        FontData[] styleData = new FontData[baseData.length];
        for (int i = 0; i < styleData.length; i++) {
            FontData base = baseData[i];
            styleData[i] = new FontData(base.getName(), base.getHeight(), base.getStyle() | style);
        }

        return styleData;
    }
}
