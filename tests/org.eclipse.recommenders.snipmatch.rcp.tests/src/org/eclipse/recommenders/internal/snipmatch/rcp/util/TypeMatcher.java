package org.eclipse.recommenders.internal.snipmatch.rcp.util;

import org.eclipse.jdt.core.dom.ITypeBinding;
import org.mockito.ArgumentMatcher;

public class TypeMatcher extends ArgumentMatcher<ITypeBinding> {

    private final String name;

    public TypeMatcher(String name) {
        this.name = name;
    }

    @Override
    public boolean matches(Object argument) {
        if (argument instanceof ITypeBinding) {
            ITypeBinding type = (ITypeBinding) argument;
            return type.getName().equals(name);
        }
        return false;
    }
}
