/**
 * Copyright (c) 2014 Codetrails GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.completion.rcp;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.internal.codeassist.InternalCompletionProposal;

import com.google.common.annotations.Beta;
import com.google.common.base.Objects;
import com.google.common.base.Optional;

/**
 * EXPERIMENTAL. Not recommended for public API use.
 */
@SuppressWarnings("restriction")
@Beta
public class AccessibleCompletionProposal extends InternalCompletionProposal {

    private Map<String, Object> data = new HashMap<>();

    public AccessibleCompletionProposal(int kind, int completionOffset) {
        super(kind, completionOffset);
    }

    @Override
    public void setPackageName(char[] packageName) {
        super.setPackageName(packageName);
    }

    @Override
    public void setTypeName(char[] typeName) {
        super.setTypeName(typeName);
    }

    @Override
    public char[] getTypeName() {
        return super.getTypeName();
    }

    public void setData(String key, Object value) {
        if (value != null) {
            data.put(key, value);
        } else {
            data.remove(key);
        }
    }

    public void setData(Class<?> key, Object value) {
        setData(key.getName(), value);
    }

    public <T> T getData(String key, T defaultValue) {
        @SuppressWarnings("unchecked")
        T res = (T) data.get(key);
        return Objects.firstNonNull(res, defaultValue);
    }

    public <T> T getData(Class<?> key, T defaultValue) {
        return getData(key.getName(), defaultValue);
    }

    public <T> Optional<T> getData(String key) {
        @SuppressWarnings("unchecked")
        T res = (T) data.get(key);
        return Optional.fromNullable(res);
    }

    public <T> Optional<T> getData(Class<T> key) {
        return getData(key.getName());
    }
}
