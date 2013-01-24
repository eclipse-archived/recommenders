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
package org.eclipse.recommenders.internal.extdoc.rcp.ui;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.Policy;
import org.eclipse.recommenders.extdoc.rcp.providers.ExtdocProvider;
import org.eclipse.recommenders.rcp.RecommendersPlugin;
import org.eclipse.swt.dnd.ByteArrayTransfer;
import org.eclipse.swt.dnd.TransferData;

public class ExtdocProviderTransfer extends ByteArrayTransfer {

    private static final String TYPE_NAME = "extdoc-provider-transfer-format"; //$NON-NLS-1$
    private static final int TYPEID = registerType(TYPE_NAME);
    private static final ExtdocProviderTransfer INSTANCE = new ExtdocProviderTransfer();
    private ExtdocProvider extDocProvider;

    private ExtdocProviderTransfer() {
    };

    public static ExtdocProviderTransfer getInstance() {
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

    public void javaToNative(final Object object, final TransferData transferData) {
        final byte[] check = TYPE_NAME.getBytes();
        super.javaToNative(check, transferData);
    }

    @Override
    public Object nativeToJava(final TransferData transferData) {
        final Object convert = super.nativeToJava(transferData);
        if (isInvalidNativeType(convert)) {
            RecommendersPlugin.log(new Status(IStatus.ERROR, Policy.JFACE, IStatus.ERROR, JFaceResources
                    .getString("ExtdocProviderTransfer.errorMessage"), null)); //$NON-NLS-1$
        }
        return getExtdocProvider();
    }

    private boolean isInvalidNativeType(final Object result) {
        return !(result instanceof byte[]) || !TYPE_NAME.equals(new String((byte[]) result));
    }

    public ExtdocProvider getExtdocProvider() {
        return extDocProvider;
    }

    public void setExtdocProvider(final ExtdocProvider extDocProvider) {
        this.extDocProvider = extDocProvider;
    }
}
