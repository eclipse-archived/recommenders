package org.eclipse.recommenders.server.extdoc.types;

import static org.eclipse.recommenders.commons.utils.Checks.ensureIsNotNull;

import org.eclipse.recommenders.commons.utils.names.ITypeName;

public class ClassOverridePatterns implements IServerType {

    public static ClassOverridePatterns create(final ITypeName type, final MethodPattern[] patterns) {
        final ClassOverridePatterns res = new ClassOverridePatterns();
        res.type = type;
        res.patterns = patterns;
        return res;
    }

    private String providerId = getClass().getSimpleName();

    private ITypeName type;
    private MethodPattern[] patterns;

    public MethodPattern[] getPatterns() {
        return patterns;
    }

    @Override
    public void validate() {
        ensureIsNotNull(type);
        ensureIsNotNull(patterns);
        providerId = getClass().getSimpleName();
    }
}
