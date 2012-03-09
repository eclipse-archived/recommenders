/**
 * Copyright (c) 2011 Sebastian Proksch.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Sebastian Proksch - initial API and implementation
 */
package org.eclipse.recommenders.commons.mining;

import static org.eclipse.recommenders.internal.analysis.codeelements.ObjectUsage.UNKNOWN_KIND;
import static org.eclipse.recommenders.internal.analysis.codeelements.ObjectUsage.UNKNOWN_METHOD;
import static org.eclipse.recommenders.internal.analysis.codeelements.ObjectUsage.UNKNOWN_TYPE;

import java.io.Serializable;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.recommenders.commons.mining.features.CallFeature;
import org.eclipse.recommenders.commons.mining.features.ContextFeature;
import org.eclipse.recommenders.commons.mining.features.DefinitionFeature;
import org.eclipse.recommenders.commons.mining.features.Feature;
import org.eclipse.recommenders.commons.mining.features.FeatureVisitor;
import org.eclipse.recommenders.commons.mining.features.KindFeature;
import org.eclipse.recommenders.commons.mining.features.TypeFeature;
import org.eclipse.recommenders.internal.analysis.codeelements.DefinitionSite.Kind;
import org.eclipse.recommenders.utils.names.IMethodName;
import org.eclipse.recommenders.utils.names.ITypeName;

public class Observation implements Cloneable, Serializable {

    private static final long serialVersionUID = 1L;

    private TypeFeature type;
    private ContextFeature context;
    private KindFeature kind;
    private DefinitionFeature definition;
    private Set<CallFeature> calls;

    public Observation() {
        setType(UNKNOWN_TYPE);
        setContext(UNKNOWN_METHOD);
        setKind(UNKNOWN_KIND);
        setDefinition(UNKNOWN_METHOD);
        calls = getNewCallsSet();
    }

    private Set<CallFeature> getNewCallsSet() {
        return new TreeSet<CallFeature>(new Comparator<CallFeature>() {
            @Override
            public int compare(CallFeature o1, CallFeature o2) {
                return o1.compareTo(o2);
            }
        });
    }

    public void setType(ITypeName typeName) {
        type = new TypeFeature(typeName);
    }

    public TypeFeature getType() {
        return type;
    }

    public void setContext(IMethodName contextName) {
        context = new ContextFeature(contextName);
    }

    public ContextFeature getContext() {
        return context;
    }

    public void setKind(Kind kindName) {
        kind = new KindFeature(kindName);
    }

    public KindFeature getKind() {
        return kind;
    }

    public void setDefinition(IMethodName definitionName) {
        definition = new DefinitionFeature(definitionName);
    }

    public DefinitionFeature getDefinition() {
        return definition;
    }

    public void addCall(IMethodName callName) {
        calls.add(new CallFeature(callName));
    }

    public void clearCalls() {
        calls.clear();
    }

    public Set<CallFeature> getCalls() {
        return calls;
    }

    public Set<Feature> getAllFeatures() {
        Set<Feature> all = new LinkedHashSet<Feature>();

        all.add(type);
        all.add(context);
        all.add(kind);
        all.add(definition);

        for (Feature call : calls) {
            all.add(call);
        }

        return all;
    }

    public boolean hasFeature(Feature feature) {
        final boolean[] hasFeature = new boolean[] { false };
        feature.accept(new FeatureVisitor() {
            @Override
            public void visit(CallFeature call) {
                hasFeature[0] = calls.contains(call);
            }

            @Override
            public void visit(ContextFeature f) {
                hasFeature[0] = context.equals(f);
            }

            @Override
            public void visit(DefinitionFeature f) {
                hasFeature[0] = definition.equals(f);
            }

            @Override
            public void visit(KindFeature f) {
                hasFeature[0] = kind.equals(f);
            }

            @Override
            public void visit(TypeFeature f) {
                hasFeature[0] = type.equals(f);
            }
        });
        return hasFeature[0];
    }

    public String getIdentifier() {
        return type.toString() + "_" + hashCode();
    }

    public boolean isQueryOf(Observation parent) {
        boolean isQuery = true;

        isQuery = type.equals(parent.type) ? isQuery : false;
        isQuery = context.equals(parent.context) ? isQuery : false;
        isQuery = kind.equals(parent.kind) ? isQuery : false;
        isQuery = definition.equals(parent.definition) ? isQuery : false;

        for (Feature call : calls) {
            isQuery = parent.calls.contains(call) ? isQuery : false;
        }

        return isQuery;
    }

    @Override
    public Observation clone() {
        Observation clone = new Observation();

        clone.type = type;
        clone.context = context;
        clone.kind = kind;
        clone.definition = definition;

        clone.calls = getNewCallsSet();
        for (CallFeature call : calls) {
            clone.calls.add(call);
        }

        return clone;
    }

    @Override
    public String toString() {
        String callNames = "";
        for (CallFeature call : calls) {
            callNames += call.shortName() + ", ";
        }
        return String.format("[%s, %s, %s, %s, %s]", type.shortName(), context.shortName(), kind,
                definition.shortName(), callNames);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Observation) {
            Observation other = (Observation) obj;
            boolean isEq = true;
            isEq = type.equals(other.type) ? isEq : false;
            isEq = context.equals(other.context) ? isEq : false;
            isEq = kind.equals(other.kind) ? isEq : false;
            isEq = definition.equals(other.definition) ? isEq : false;
            isEq = calls.equals(other.calls) ? isEq : false;
            return isEq;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash = hash * 37 + type.hashCode();
        hash = hash * 37 + context.hashCode();
        hash = hash * 37 + kind.hashCode();
        hash = hash * 37 + definition.hashCode();
        for (Feature call : calls) {
            hash = hash * 37 + call.hashCode();
        }
        return hash;
    }
}