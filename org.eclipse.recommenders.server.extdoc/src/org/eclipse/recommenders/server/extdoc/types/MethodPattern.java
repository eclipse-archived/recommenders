package org.eclipse.recommenders.server.extdoc.types;

import java.util.Map;

import org.eclipse.recommenders.commons.utils.names.IMethodName;

public class MethodPattern {

    public static MethodPattern create(final int numberOfObservations, final Map<IMethodName, Double> methodGroup) {
        final MethodPattern res = new MethodPattern();
        res.numberOfObservations = numberOfObservations;
        res.methods = methodGroup;
        return res;
    }

    private int numberOfObservations;
    private Map<IMethodName, Double> methods;

    public int getNumberOfObservations() {
        return numberOfObservations;
    }

    public Map<IMethodName, Double> getMethods() {
        return methods;
    }
}
