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

public final class OverridesProvider extends ApidocProvider {

    // private final JavaElementResolver resolver;
    // private final EventBus workspaceBus;
    // private final ClassOverridesModelStore oStore;
    // private final ClassOverridesPatternsModelStore pStore;
    //
    // @Inject
    // public OverridesProvider(ClassOverridesModelStore oStore, ClassOverridesPatternsModelStore pStore,
    // final JavaElementResolver resolver, final EventBus workspaceBus) {
    // this.oStore = oStore;
    // this.pStore = pStore;
    // this.resolver = resolver;
    // this.workspaceBus = workspaceBus;
    // }
    //
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
    // public void onTypeSelection(final IType type, final JavaSelectionEvent event, final Composite parent) throws
    // ExecutionException {
    // renderClassOverrideDirectives(type, parent);
    // renderClassOverridesPatterns(type, parent);
    // }
    //
    // private boolean renderClassOverrideDirectives(final IType type, final Composite parent) throws ExecutionException
    // {
    // Optional<ClassOverrideDirectives> model = oStore.aquireModel(type);
    // if (!model.isPresent() || model.get().getOverrides() == null) {
    // return false;
    // }
    // runSyncInUiThread(new TypeOverrideDirectivesRenderer(type, model.get(), parent));
    // return true;
    // }
    //
    // private boolean renderClassOverridesPatterns(final IType type, final Composite parent) throws ExecutionException
    // {
    // Optional<ClassOverridePatterns> opt = pStore.aquireModel(type);
    // if (!opt.isPresent()) {
    // return false;
    // }
    // runSyncInUiThread(new OverridePatternsRenderer(type, opt.get(), parent));
    // return true;
    // }
    //
    // // ========================================================================
    // // TODO: Review the renderer code is redundant and needs refactoring after
    // // all providers have been written to
    // // identify more common parts.
    //
    // private class TypeOverrideDirectivesRenderer implements Runnable {
    //
    // private final IType type;
    // private final ClassOverrideDirectives directive;
    // private final Composite parent;
    // private Composite container;
    //
    // public TypeOverrideDirectivesRenderer(final IType type, final ClassOverrideDirectives directive,
    // final Composite parent) {
    // this.type = type;
    // this.directive = directive;
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
    // final String message =
    // format(Messages.EXTDOC_OVERRIDES_INTRO,
    // directive.getNumberOfSubclasses(),
    // type.getElementName());
    // Label label = new Label(container, SWT.NONE);
    // label.setText(message);
    // setInfoForegroundColor(label);
    // setInfoBackgroundColor(label);
    // }
    //
    // private void addDirectives() {
    // final int numberOfSubclasses = directive.getNumberOfSubclasses();
    // final TreeBag<IMethodName> b = newTreeBag(directive.getOverrides());
    // ExtdocUtils.renderMethodDirectivesBlock(container,
    // b,
    // numberOfSubclasses,
    // workspaceBus,
    // resolver,
    // Messages.EXTDOC_OVERRIDES_OVERRIDES);
    // }
    //
    // }
    //
    // private class OverridePatternsRenderer implements Runnable {
    //
    // private final Composite parent;
    // private Composite container;
    //
    // double totalNumberOfExamples;
    // private List<MethodPattern> patterns;
    //
    // public OverridePatternsRenderer(final IType type, final ClassOverridePatterns directive, final Composite parent)
    // {
    // this.parent = parent;
    // setPatterns(directive);
    // computeTotalNumberOfExamples();
    // filterInfrequentPatterns();
    // sortPatterns();
    // }
    //
    // private void setPatterns(final ClassOverridePatterns patterns) {
    // this.patterns = asList(patterns.getPatterns());
    // }
    //
    // private void filterInfrequentPatterns() {
    // patterns = newLinkedList(filter(patterns, new Predicate<MethodPattern>() {
    // @Override
    // public boolean apply(final MethodPattern input) {
    // final int numberOfObservations = input.getNumberOfObservations();
    // return (numberOfObservations / totalNumberOfExamples) > 0.1;
    // }
    // }));
    // }
    //
    // private void sortPatterns() {
    // Collections.sort(patterns, new Comparator<MethodPattern>() {
    //
    // @Override
    // public int compare(final MethodPattern o1, final MethodPattern o2) {
    // return o2.getNumberOfObservations() - o1.getNumberOfObservations();
    // }
    // });
    // }
    //
    // private void computeTotalNumberOfExamples() {
    // for (final MethodPattern pattern : patterns) {
    // totalNumberOfExamples += pattern.getNumberOfObservations();
    // }
    // }
    //
    // @Override
    // public void run() {
    // createContainer();
    // addHeader();
    //
    // int i = 1;
    // for (final MethodPattern pattern : patterns) {
    // addDirectives(pattern, i++);
    // }
    // }
    //
    // private void createContainer() {
    // container = new Composite(parent, SWT.NONE);
    // setInfoBackgroundColor(container);
    // container.setLayout(new GridLayout());
    // container.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
    // }
    //
    // private void addHeader() {
    // new Label(container, SWT.None);
    // final String message =
    // format(Messages.EXTDOC_OVERRIDES_INTRO_PATTERN);
    // createLabel(container, message, true);
    // }
    //
    // private void addDirectives(final MethodPattern pattern, final int index) {
    //
    // final int patternPercentage =
    // (int) Math.rint(100 * pattern.getNumberOfObservations() / totalNumberOfExamples);
    // final String text =
    // format(Messages.EXTDOC_OVERRIDES_PERCENTAGE_PATTERN,
    // index,
    // patternPercentage,
    // pattern.getNumberOfObservations());
    // createLabel(container, text, true, false, SWT.COLOR_DARK_GRAY, true);
    // final Composite group = createGridComposite(container, 1, 0, 0, 0, 0);
    // final List<Entry<IMethodName, Double>> s = Lists.newLinkedList(pattern.getMethods().entrySet());
    // Collections.sort(s, new Comparator<Entry<IMethodName, Double>>() {
    //
    // @Override
    // public int compare(final Entry<IMethodName, Double> o1, final Entry<IMethodName, Double> o2) {
    // // return o2.getValue().compareTo(o1.getValue());
    // return o1.getKey().getName().compareTo(o2.getKey().getName());
    // }
    // });
    //
    // final Table table = new Table(group, SWT.NONE | SWT.HIDE_SELECTION);
    // table.setBackground(ExtdocUtils.createColor(SWT.COLOR_INFO_BACKGROUND));
    // table.setLayoutData(GridDataFactory.fillDefaults().indent(10, 0).create());
    // final TableColumn column1 = new TableColumn(table, SWT.NONE);
    // final TableColumn column2 = new TableColumn(table, SWT.NONE);
    // final TableColumn column3 = new TableColumn(table, SWT.NONE);
    // final TableColumn column4 = new TableColumn(table, SWT.NONE);
    //
    // for (final Entry<IMethodName, Double> entry : s) {
    // final int percentage = (int) Math.rint(entry.getValue() * 100);
    // final String phraseText = percentageToRecommendationPhrase(percentage);
    // final String stats = format(Messages.EXTDOC_OVERRIDES_PERCENTAGE, percentage);
    //
    // final Link bar = createMethodLink(table, entry.getKey(), resolver, workspaceBus);
    // final TableItem item = new TableItem(table, SWT.NONE);
    // item.setText(new String[] { phraseText, Messages.EXTDOC_OVERRIDES_OVERRIDE, bar.getText(), stats });
    // item.setFont(0, JFaceResources.getBannerFont());
    // item.setForeground(createColor(COLOR_INFO_FOREGROUND));
    // final TableEditor editor = new TableEditor(table);
    // editor.grabHorizontal = editor.grabVertical = true;
    // editor.setEditor(bar, item, 2);
    //
    // }
    // column1.pack();
    // column2.pack();
    // column3.pack();
    // column4.pack();
    //
    // new Label(container, SWT.SEPARATOR | SWT.HORIZONTAL);
    //
    // }
    // }
}
