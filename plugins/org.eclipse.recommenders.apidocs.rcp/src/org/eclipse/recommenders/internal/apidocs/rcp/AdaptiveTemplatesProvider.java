/**
 * Copyright (c) 2010, 2013 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Olav Lenz - initial API and implementation.
 */
package org.eclipse.recommenders.internal.apidocs.rcp;

import static com.google.common.base.Optional.*;
import static com.google.common.collect.Collections2.filter;
import static org.eclipse.jdt.core.IJavaElement.PACKAGE_FRAGMENT_ROOT;
import static org.eclipse.recommenders.utils.Checks.cast;
import static org.eclipse.recommenders.utils.Constants.CLASS_CALL_MODELS;
import static org.eclipse.recommenders.utils.IOUtils.LINE_SEPARATOR;
import static org.eclipse.recommenders.utils.Recommendations.top;
import static org.eclipse.recommenders.utils.names.Names.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Set;

import javax.inject.Inject;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.recommenders.apidocs.rcp.ApidocProvider;
import org.eclipse.recommenders.apidocs.rcp.JavaSelectionSubscriber;
import org.eclipse.recommenders.calls.ICallModel;
import org.eclipse.recommenders.calls.SingleZipCallModelProvider;
import org.eclipse.recommenders.models.IModelIndex;
import org.eclipse.recommenders.models.IModelRepository;
import org.eclipse.recommenders.models.ModelCoordinate;
import org.eclipse.recommenders.models.ProjectCoordinate;
import org.eclipse.recommenders.models.UniqueTypeName;
import org.eclipse.recommenders.models.rcp.IProjectCoordinateProvider;
import org.eclipse.recommenders.rcp.JavaElementSelectionEvent;
import org.eclipse.recommenders.utils.Recommendation;
import org.eclipse.recommenders.utils.names.IMethodName;
import org.eclipse.recommenders.utils.names.IPackageName;
import org.eclipse.recommenders.utils.names.ITypeName;
import org.eclipse.recommenders.utils.names.VmPackageName;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.List;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Lists;

public class AdaptiveTemplatesProvider extends ApidocProvider {

    @Inject
    IProjectCoordinateProvider pcProvider;

    @Inject
    IModelRepository modelRepo;

    @Inject
    IModelIndex modelIndex;

    @JavaSelectionSubscriber
    public void onPackageSelection(final IPackageFragment selected, JavaElementSelectionEvent event, Composite parent) {
        IPackageFragmentRoot pfr = cast(selected.getAncestor(PACKAGE_FRAGMENT_ROOT));
        Optional<Set<ITypeName>> data = fetchData(pfr);

        if (data.isPresent()) {
            Collection<ITypeName> typesInPackage = filter(data.get(), new Predicate<ITypeName>() {
                IPackageName pkg = VmPackageName.get(selected.getElementName().replace(".", "/"));

                @Override
                public boolean apply(ITypeName t) {
                    return pkg.equals(t.getPackage());
                }
            });
            runSyncInUiThread(new AcquireableTypesRenderer(parent, typesInPackage, false));
        }
    }

    @JavaSelectionSubscriber
    public void onPackageRootSelection(IPackageFragmentRoot selected, JavaElementSelectionEvent event, Composite parent) {
        Optional<Set<ITypeName>> data = fetchData(selected);
        if (data.isPresent()) {
            runSyncInUiThread(new AcquireableTypesRenderer(parent, data.get(), true));
        }
    }

    @JavaSelectionSubscriber
    public void onProjectSelection(IJavaProject selected, JavaElementSelectionEvent event, Composite parent) {
        Optional<Set<ITypeName>> data = fetchData(selected);
        if (data.isPresent()) {
            runSyncInUiThread(new AcquireableTypesRenderer(parent, data.get(), true));
        }
    }

    @JavaSelectionSubscriber
    public void onTypeSelection(IType selected, JavaElementSelectionEvent event, Composite parent) throws IOException {
        UniqueTypeName name = pcProvider.toUniqueName(selected).orNull();
        if (name == null) {
            return;
        }
        ModelCoordinate mc = modelIndex.suggest(name.getProjectCoordinate(), CLASS_CALL_MODELS).orNull();
        if (mc == null) {
            return;
        }
        File zip = modelRepo.getLocation(mc).orNull();
        if (zip == null) {
            return;
        }
        SingleZipCallModelProvider p = new SingleZipCallModelProvider(zip);
        p.open();
        ICallModel model = p.acquireModel(name).orNull();
        p.close();
        if (model == null) {
            return;
        }
        ArrayList<String> patterns = Lists.newArrayList();
        model.setObservedCalls(Collections.<IMethodName>emptySet());
        for (Recommendation<IMethodName> def : top(model.recommendDefinitions(), 5, 0.01d)) {
            model.reset();
            model.setObservedCalls(Collections.<IMethodName>emptySet());
            model.setObservedDefiningMethod(def.getProposal());
            for (Recommendation<String> pattern : top(model.recommendPatterns(), 3, 0.01d)) {
                model.setObservedPattern(pattern.getProposal());
                java.util.List<Recommendation<IMethodName>> calls = top(model.recommendCalls(), 8, 0.1d);
                if (calls.size() < 2) {
                    continue;
                }
                StringBuilder sb = new StringBuilder();
                sb.append(model.getReceiverType().getClassName()).append(" var = ")
                        .append(vm2srcQualifiedMethod(def.getProposal())).append(LINE_SEPARATOR);
                for (Recommendation<IMethodName> call : calls) {
                    sb.append("var.").append(vm2srcSimpleMethod(call.getProposal())).append(LINE_SEPARATOR);
                }
                patterns.add(sb.toString());
            }
        }
        runSyncInUiThread(new CodeRenderer(parent, patterns));
    }

    private Optional<Set<ITypeName>> fetchData(IPackageFragmentRoot selectedPackageFragmentRoot) {
        Optional<ProjectCoordinate> opc = pcProvider.resolve(selectedPackageFragmentRoot);
        if (opc.isPresent()) {
            return fetchData(opc.get());
        }
        return absent();
    }

    private Optional<Set<ITypeName>> fetchData(IJavaProject javaProject) {
        Optional<ProjectCoordinate> opc = pcProvider.resolve(javaProject);
        if (opc.isPresent()) {
            return fetchData(opc.get());
        }
        return absent();
    }

    private Optional<Set<ITypeName>> fetchData(ProjectCoordinate pc) {
        Optional<ModelCoordinate> omc = modelIndex.suggest(pc, CLASS_CALL_MODELS);
        if (omc.isPresent()) {
            return fetchData(omc.get());
        }
        return absent();
    }

    private Optional<Set<ITypeName>> fetchData(ModelCoordinate mc) {
        Optional<File> file = modelRepo.getLocation(mc);
        if (!file.isPresent()) {
            return absent();
        }
        SingleZipCallModelProvider modelProvider = new SingleZipCallModelProvider(file.get());
        Set<ITypeName> acquireableTypes;
        try {
            modelProvider.open();
            acquireableTypes = modelProvider.acquireableTypes();
            modelProvider.close();
            return of(acquireableTypes);
        } catch (IOException e) {
            return absent();
        }
    }

    class AcquireableTypesRenderer implements Runnable {

        private Composite parent;
        private java.util.List<ITypeName> types;
        private boolean showPackages;

        public AcquireableTypesRenderer(Composite parent, Collection<ITypeName> acquireableTypes, boolean showPackages) {
            this.parent = parent;
            types = sort(acquireableTypes);
            this.showPackages = showPackages;
        }

        private java.util.List<ITypeName> sort(Collection<ITypeName> acquireableTypes) {
            ArrayList<ITypeName> types = Lists.newArrayList(acquireableTypes);

            Collections.sort(types, new Comparator<ITypeName>() {

                @Override
                public int compare(ITypeName o1, ITypeName o2) {
                    return ComparisonChain.start().compare(o1.getPackage(), o2.getPackage())
                            .compare(o1.getClassName(), o2.getClassName()).result();
                }
            });
            return types;
        }

        @Override
        public void run() {
            ApidocsViewUtils.createLabel(parent,
                    "Recommendations are available for the following Types: (" + types.size() + ")", true);

            List typeList = new List(parent, SWT.NONE);
            typeList.setBackground(parent.getBackground());
            GridData data = new GridData(SWT.LEFT, SWT.BEGINNING, false, true);
            data.horizontalIndent = 10;
            typeList.setLayoutData(data);

            for (ITypeName typeName : types) {
                if (showPackages) {
                    typeList.add(vm2srcQualifiedType(typeName));
                } else {
                    typeList.add(typeName.getClassName());
                }
            }
        }
    }

    class CodeRenderer implements Runnable {

        private Composite parent;
        private Collection<String> snippets;

        public CodeRenderer(Composite parent, Collection<String> snippets) {
            this.parent = parent;
            this.snippets = snippets;
        }

        @Override
        public void run() {
            ApidocsViewUtils.createLabel(parent,
                    "This provider is highly experimental. Select a type name (like \"String\") in your editor \n"
                            + "and it will show a few likely *example* code snippets (" + snippets.size()
                            + ") it has found but not all it \nknowns. This provider is for demo purpose only.", true);
            StyledText text = ApidocsViewUtils.createStyledText(parent, Joiner.on("\n").join(snippets),
                    SWT.COLOR_BLACK, false);
            GridData data = (GridData) text.getLayoutData();
            data.horizontalIndent = 20;
            ApidocsViewUtils.setInfoBackgroundColor(text);
        }
    }
}
