/**
 * Copyright (c) 2010, 2012 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Patrick Gottschaemmer, Olav Lenz - add Drag'n'Drop support
 */
package org.eclipse.recommenders.internal.apidocs.rcp;

import org.eclipse.recommenders.apidocs.rcp.ApidocProvider;
import org.eclipse.recommenders.internal.apidocs.rcp.l10n.LogMessages;
import org.eclipse.recommenders.utils.Logs;
import org.eclipse.swt.dnd.ByteArrayTransfer;
import org.eclipse.swt.dnd.TransferData;

public final class DnDProviderTransfer extends ByteArrayTransfer {

    private static final String TYPE_NAME = "extdoc-provider-transfer-format"; //$NON-NLS-1$
    private static final int TYPEID = registerType(TYPE_NAME);
    private static final DnDProviderTransfer INSTANCE = new DnDProviderTransfer();

    private ApidocProvider extDocProvider;

    private DnDProviderTransfer() {
    };

    public static DnDProviderTransfer getInstance() {
        return INSTANCE;
    }

    @Override
    protected int[] getTypeIds() {
        return new int[] { TYPEID };
    }

    @Override
    protected String[] getTypeNames() {
        return new String[] { TYPE_NAME };
    }

    @Override
    public void javaToNative(final Object object, final TransferData transferData) {
        final byte[] check = TYPE_NAME.getBytes();
        super.javaToNative(check, transferData);
    }

    @Override
    public Object nativeToJava(final TransferData transferData) {
        final Object convert = super.nativeToJava(transferData);
        if (isInvalidNativeType(convert)) {
            Logs.log(LogMessages.ERROR_FAILED_TO_PERFORM_DRAG_AND_DROP);
        }
        return getExtdocProvider();
    }

    private boolean isInvalidNativeType(final Object result) {
        return !(result instanceof byte[]) || !TYPE_NAME.equals(new String((byte[]) result));
    }

    public ApidocProvider getExtdocProvider() {
        return extDocProvider;
    }

    public void setExtdocProvider(final ApidocProvider extDocProvider) {
        this.extDocProvider = extDocProvider;
    }
}
