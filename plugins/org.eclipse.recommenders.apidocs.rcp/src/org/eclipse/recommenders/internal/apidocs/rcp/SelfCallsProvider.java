/**
 * Copyright (c) 2011 Stefan Henss.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stefan Henss - initial API and implementation.
 *    Olav Lenz - externalize Strings.
 */
package org.eclipse.recommenders.internal.apidocs.rcp;

import org.eclipse.recommenders.apidocs.rcp.ApidocProvider;

public final class SelfCallsProvider extends ApidocProvider {

    // private final JavaElementResolver resolver;
    // private final EventBus workspaceBus;
    // private final ClassSelfcallsModelStore cStore;
    // private final MethodSelfcallsModelStore mStore;

    // @Inject
    // public SelfCallsProvider(ClassSelfcallsModelStore cStore, MethodSelfcallsModelStore mStore,
    // final JavaElementResolver resolver, final EventBus workspaceBus) {
    // this.cStore = cStore;
    // this.mStore = mStore;
    // this.resolver = resolver;
    // this.workspaceBus = workspaceBus;
    //
    // }

    // @JavaSelectionSubscriber
    // public void onTypeRootSelection(final ITypeRoot root, final JavaSelectionEvent event, final Composite parent)
    // throws ExecutionException {
    // final IType type = root.findPrimaryType();
    // if (type != null) {
    // onTypeSelection(type, event, parent);
    // }
    // }
    //
    // @JavaSelectionSubscriber
    // public void onTypeSelection(final IType type, final JavaSelectionEvent event, final Composite parent)
    // throws ExecutionException {
    // Optional<ClassSelfcallDirectives> model = cStore.aquireModel(type);
    // if (model.isPresent()) {
    // runSyncInUiThread(new TypeSelfcallDirectivesRenderer(type, model.get(), parent));
    // }
    // }
    //
    // @JavaSelectionSubscriber
    // public void onMethodSelection(final IMethod method, final JavaSelectionEvent event, final Composite parent) {
    //
    // for (IMethod current = method; current != null; current = JdtUtils.findOverriddenMethod(current).orNull()) {
    // final Optional<MethodSelfcallDirectives> selfcalls = mStore.aquireModel(current);
    // if (selfcalls.isPresent()) {
    // runSyncInUiThread(new MethodSelfcallDirectivesRenderer(method, selfcalls.get(), parent));
    // }
    // }
    // }
    //
    // private class TypeSelfcallDirectivesRenderer implements Runnable {
    //
    // private final IType type;
    // private final ClassSelfcallDirectives directive;
    // private final Composite parent;
    // private Composite container;
    //
    // public TypeSelfcallDirectivesRenderer(final IType type, final ClassSelfcallDirectives selfcalls,
    // final Composite parent) {
    // this.type = type;
    // this.directive = selfcalls;
    // this.parent = parent;
    // }
    //
    // @Override
    // public void run() {
    // createContainer();
    // addHeader();
    // addDirectives();
    // }
    //
    // private void createContainer() {
    // container = new Composite(parent, SWT.NONE);
    // setInfoBackgroundColor(container);
    // container.setLayout(new GridLayout());
    // }
    //
    // private void addHeader() {
    // final String message = format(Messages.EXTDOC_SELFCALLS_INTRO_SUBCLASSES,
    // directive.getNumberOfSubclasses(), type.getElementName());
    // createLabel(container, message, true);
    // }
    //
    // private void addDirectives() {
    // final int numberOfSubclasses = directive.getNumberOfSubclasses();
    // final TreeBag<IMethodName> b = newTreeBag(directive.getCalls());
    // renderMethodDirectivesBlock(container, b, numberOfSubclasses, workspaceBus, resolver,
    // Messages.EXTDOC_SELFCALLS_CALLS);
    // }
    // }
    //
    // private class MethodSelfcallDirectivesRenderer implements Runnable {
    //
    // private final IMethod method;
    // private final MethodSelfcallDirectives directive;
    // private final Composite parent;
    //
    // private Composite container;
    //
    // public MethodSelfcallDirectivesRenderer(final IMethod method, final MethodSelfcallDirectives selfcalls,
    // final Composite parent) {
    // this.method = method;
    // this.directive = selfcalls;
    // this.parent = parent;
    // }
    //
    // @Override
    // public void run() {
    // createContainer();
    // addHeader();
    // addDirectives();
    // }
    //
    // private void createContainer() {
    // container = new Composite(parent, SWT.NONE);
    // setInfoBackgroundColor(container);
    // container.setLayout(new GridLayout());
    // }
    //
    // private void addHeader() {
    // final String message = format(
    // Messages.EXTDOC_SELFCALLS_INTRO_IMPLEMENTORS,
    // directive.getNumberOfDefinitions(), method.getElementName());
    // createLabel(container, message, true);
    // }
    //
    // private void addDirectives() {
    // final int numberOfSubclasses = directive.getNumberOfDefinitions();
    // final TreeBag<IMethodName> b = newTreeBag(directive.getCalls());
    // renderMethodDirectivesBlock(container, b, numberOfSubclasses, workspaceBus, resolver,
    // Messages.EXTDOC_SELFCALLS_CALLS);
    // }
    // }
}
