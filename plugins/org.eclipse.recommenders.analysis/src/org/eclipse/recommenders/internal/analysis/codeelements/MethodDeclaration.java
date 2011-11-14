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
package org.eclipse.recommenders.internal.analysis.codeelements;

import java.util.Collection;
import java.util.Set;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.eclipse.recommenders.utils.names.IMethodName;
import org.eclipse.recommenders.utils.names.IName;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

public class MethodDeclaration implements INamedCodeElement {

    public static MethodDeclaration create() {
        final MethodDeclaration res = new MethodDeclaration();
        return res;
    }

    public static MethodDeclaration create(final IMethodName name) {
        final MethodDeclaration res = create();
        res.name = name;
        return res;
    }

    @Override
    public IName getName() {
        return name;
    }

    public IMethodName name;

    public IMethodName superDeclaration;

    public IMethodName firstDeclaration;

    public int line;

    public Set<TypeDeclaration> nestedTypes = Sets.newHashSet();

    public int modifiers;

    public Set<ObjectInstanceKey> objects = Sets.newHashSet();

    /**
     * A variable is more or less a pointer in source code.
     */
    private transient Set<Variable> variables;

    private MethodDeclaration() {
        // use create methods instead
    }

    private transient Multimap<String, ObjectInstanceKey> localNames;

    public Multimap<String, ObjectInstanceKey> getLocalNamesTable() {
        if (localNames == null) {
            computeLocalNamesTable();
        }
        return localNames;
    }

    private synchronized void computeLocalNamesTable() {
        final Multimap<String, ObjectInstanceKey> localNames = HashMultimap.create();
        for (final ObjectInstanceKey obj : objects) {
            for (final String localName : obj.names) {
                localNames.put(localName, obj);
            }
        }
        this.localNames = localNames;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

    private synchronized void initVariables() {
        final Multimap<String, ObjectInstanceKey> names2instances = HashMultimap.create();
        for (final ObjectInstanceKey object : objects) {
            for (final String name : object.names) {
                names2instances.put(name, object);
            }
        }
        final Set<Variable> variables = Sets.newHashSet();
        for (final String localName : names2instances.keySet()) {
            final Collection<ObjectInstanceKey> objects = names2instances.get(localName);
            final Variable var = Variable.create(localName, Iterables.getFirst(objects, null).type, this.name);
            var.pointsTo = Sets.newHashSet(names2instances.get(localName));
            variables.add(var);
        }
        this.variables = variables;
    }

    public void clearEmptySets() {
        if (nestedTypes.isEmpty())
            nestedTypes = null;
        if (objects.isEmpty())
            objects = null;
    }

    public synchronized Set<Variable> getVariables() {
        if (variables == null) {
            initVariables();
        }
        return variables;
    }

    public Variable findVariable(final String name) {
        for (final Variable variable : getVariables()) {
            if (name.equals(variable.getNameLiteral())) {
                return variable;
            }
        }
        // sorry, we couldn't find any matching variable.
        return null;
    }

    @Override
    public void accept(final CompilationUnitVisitor v) {
        if (v.visit(this)) {
            if (nestedTypes != null)
                for (final TypeDeclaration nestedType : nestedTypes) {
                    nestedType.accept(v);
                }
            if (objects != null)
                for (final ObjectInstanceKey obj : objects) {
                    obj.accept(v);
                }
            if (variables != null)
                for (final Variable var : getVariables()) {
                    var.accept(v);
                }
        }

    }
}
