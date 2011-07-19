/**
 * Copyright (c) 2010 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.commons.analysis.codeelements;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.eclipse.recommenders.commons.utils.names.IMethodName;
import org.eclipse.recommenders.commons.utils.names.IName;
import org.eclipse.recommenders.commons.utils.names.VmTypeName;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.annotations.SerializedName;

/**
 * {@link CompilationUnit}s represent .class or .java files. All units MUST have
 * a primary type, i.e., a instance of {@link TypeDeclaration}. In addition,
 * primary types of source types may also have one or many nested types.
 * <p>
 * {@link CompilationUnit#imports} is a pointer to the {@link TypeReference}s
 * used within this {@link CompilationUnit} as detected by the analyzer used
 * during analysis.
 */
public class CompilationUnit implements ICodeElement {

    public enum Kind {
        SNAPSHOT, RELEASE
    }

    public static CompilationUnit create(final Set<TypeReference> usedTypes) {
        final CompilationUnit res = create();
        res.imports.addAll(usedTypes);
        return res;
    }

    public static CompilationUnit create() {
        final CompilationUnit res = new CompilationUnit();
        return res;
    }

    public static CompilationUnit createRelease(final Set<TypeReference> usedTypes) {
        final CompilationUnit res = createRelease();
        res.imports.addAll(usedTypes);
        return res;
    }

    public static CompilationUnit createRelease() {
        final CompilationUnit res = create();
        res.kind = Kind.RELEASE;
        return res;
    }

    @SerializedName("_id")
    public String id;
    @SerializedName("_rev")
    public String rev;

    public Date creationTimestamp = new Date();

    public Set<TypeReference> imports = Sets.newTreeSet();

    public Kind kind = Kind.SNAPSHOT;

    public Date analysedOn = new Date();

    public String name;

    public IName getName() {
        return VmTypeName.get(name);
    }

    public TypeDeclaration primaryType;

    /**
     * @see #create(Set)
     */
    protected CompilationUnit() {
        // use static create methods to obtain an instance of this class
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

    public MethodDeclaration findMethod(final IMethodName methodName) {
        return primaryType.findMethod(methodName);
    }

    public List<TypeDeclaration> allTypes() {
        final List<TypeDeclaration> types = Lists.newLinkedList();
        accept(new CompilationUnitVisitor() {
            @Override
            public boolean visit(final TypeDeclaration type) {
                types.add(type);
                return true;
            }
        });

        return types;
    }

    @Override
    public void accept(final CompilationUnitVisitor v) {
        if (v.visit(this)) {
            if (primaryType != null) {
                primaryType.accept(v);
            }
        }
    }
}
