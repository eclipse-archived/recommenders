/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.extdoc.rcp.providers.hooks;

import static com.google.common.base.Optional.absent;
import static com.google.common.base.Optional.of;
import static org.eclipse.jdt.ui.JavaElementLabels.M_APP_RETURNTYPE;
import static org.eclipse.jdt.ui.JavaElementLabels.M_PARAMETER_TYPES;
import static org.eclipse.jdt.ui.JavaElementLabels.getElementLabel;
import static org.eclipse.recommenders.internal.extdoc.rcp.ui.ExtdocUtils.createComposite;
import static org.eclipse.recommenders.internal.extdoc.rcp.ui.ExtdocUtils.createLabel;
import static org.eclipse.recommenders.rcp.events.JavaSelectionEvent.JavaSelectionLocation.METHOD_DECLARATION;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.inject.Inject;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.corext.util.JdtFlags;
import org.eclipse.jdt.ui.JavaElementLabels;
import org.eclipse.recommenders.extdoc.rcp.providers.ExtdocProvider;
import org.eclipse.recommenders.extdoc.rcp.providers.JavaSelectionSubscriber;
import org.eclipse.recommenders.internal.extdoc.rcp.ui.ExtdocUtils;
import org.eclipse.recommenders.rcp.RecommendersPlugin;
import org.eclipse.recommenders.rcp.events.JavaSelectionEvent;
import org.eclipse.recommenders.utils.IOUtils;
import org.eclipse.recommenders.utils.rcp.JavaElementResolver;
import org.eclipse.recommenders.utils.rcp.JdtUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.collect.TreeMultimap;
import com.google.common.eventbus.EventBus;

public class StaticHooksFinder extends ExtdocProvider {

    private final class HooksRendererRunnable implements Runnable {
        private final TreeMultimap<IType, IMethod> index;
        private final Composite parent;
        private StyledText styledText;

        private HooksRendererRunnable(final TreeMultimap<IType, IMethod> index, final Composite parent) {
            this.index = index;
            this.parent = parent;
        }

        @Override
        public void run() {
            final Composite container = createComposite(parent, 1);
            if (index.isEmpty()) {
                createLabel(container, "No public static method found in selected package (-root)", true);
            }

            final List<StyleRange> typeRanges = Lists.newLinkedList();
            // final List<StyleRange> methodRanges = Lists.newLinkedList();
            final StringBuilder sb = new StringBuilder();
            for (final IType type : index.keySet()) {
                final String typeLabel = type.getFullyQualifiedName();
                final int typeLabelBegin = sb.length();
                sb.append(typeLabel);
                final int typeLabelEnd = sb.length();
                final StyleRange styleRange = new StyleRange();
                styleRange.rise = -12;
                styleRange.start = typeLabelBegin;
                styleRange.length = typeLabelEnd - typeLabelBegin;
                styleRange.fontStyle = SWT.BOLD;
                styleRange.data = type;
                typeRanges.add(styleRange);
                sb.append(IOUtils.LINE_SEPARATOR);
                for (final IMethod method : index.get(type)) {
                    sb.append("    ");
                    final int methodLabelBegin = sb.length();
                    final String methodLabel = getElementLabel(method, M_APP_RETURNTYPE | M_PARAMETER_TYPES);
                    sb.append(methodLabel);
                    final int methodLabelEnd = sb.length();
                    final StyleRange methodStyleRange = new StyleRange();
                    methodStyleRange.start = methodLabelBegin;
                    methodStyleRange.length = methodLabelEnd - methodLabelBegin;
                    methodStyleRange.data = method;
                    methodStyleRange.underline = true;
                    // methodStyleRange.fontStyle = SWT.BOLD;
                    methodStyleRange.foreground = Display.getDefault().getSystemColor(SWT.COLOR_BLUE);
                    typeRanges.add(methodStyleRange);
                    sb.append(IOUtils.LINE_SEPARATOR);
                }
            }

            styledText = new StyledText(container, SWT.NONE);
            styledText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
            ExtdocUtils.setInfoBackgroundColor(styledText);
            styledText.setEditable(false);
            styledText.setText(sb.toString());
            styledText.setStyleRanges(typeRanges.toArray(new StyleRange[0]));
            final Cursor c1 = Display.getDefault().getSystemCursor(SWT.CURSOR_ARROW);
            final Cursor c2 = Display.getDefault().getSystemCursor(SWT.CURSOR_HAND);
            styledText.addListener(SWT.MouseDown, new Listener() {

                @Override
                public void handleEvent(final Event event) {
                    // It is up to the application to determine when and how a link should be activated.
                    // In this snippet links are activated on mouse down when the control key is held down
                    // if ((event.stateMask) != 0) {
                    final Optional<IMethod> opt = getSelectedMethod(event.x, event.y);
                    if (opt.isPresent()) {
                        final JavaSelectionEvent sEvent = new JavaSelectionEvent(opt.get(), METHOD_DECLARATION);
                        workspaceBus.post(sEvent);
                    }
                    // }
                }
            });

            styledText.addMouseMoveListener(new MouseMoveListener() {

                @Override
                public void mouseMove(final MouseEvent e) {
                    final Optional<IMethod> opt = getSelectedMethod(e.x, e.y);
                    if (opt.isPresent()) {
                        styledText.setCursor(c2);
                    } else {
                        styledText.setCursor(c1);
                    }

                }
            });
            styledText.addMouseTrackListener(new MouseTrackListener() {

                @Override
                public void mouseHover(final MouseEvent e) {
                    //
                    // final Optional<IMethod> opt = getSelectedMethod(e.x, e.y);
                    // if (opt.isPresent()) {
                    // styledText.setCursor(new Cursor(Display.getDefault(), SWT.CURSOR_HAND));
                    // } else {
                    // styledText.setCursor(new Cursor(Display.getDefault(), SWT.CURSOR_ARROW));
                    // }

                }

                @Override
                public void mouseExit(final MouseEvent e) {
                    // TODO Auto-generated method stub
                    System.out.println(e);
                }

                @Override
                public void mouseEnter(final MouseEvent e) {
                    // TODO Auto-generated method stub
                    System.out.println(e);
                }
            });
        }

        private Optional<StyleRange> getSelectedStyleRange(final int x, final int y) {
            try {
                final int offset = styledText.getOffsetAtLocation(new Point(x, y));
                final StyleRange style = styledText.getStyleRangeAtOffset(offset);
                return Optional.fromNullable(style);
            } catch (final IllegalArgumentException e) {
                return absent();
            }
        }

        private Optional<IMethod> getSelectedMethod(final int x, final int y) {
            final Optional<StyleRange> range = getSelectedStyleRange(x, y);
            if (!range.isPresent()) {
                return absent();
            }

            final Object data = range.get().data;
            if (data instanceof IMethod) {
                return of((IMethod) data);
            } else {
                return absent();
            }
        }
    }

    // final GridDataFactory labelFactory = GridDataFactory.swtDefaults().indent(3, 5);
    // for (final IType type : index.keySet()) {
    // labelFactory.applyTo(createLabel(container, type.getFullyQualifiedName() + ":", true, false,
    // SWT.COLOR_BLACK, true));
    // final Table table = new Table(container, SWT.NONE | SWT.HIDE_SELECTION);
    // table.setBackground(ExtdocUtils.createColor(SWT.COLOR_INFO_BACKGROUND));
    // table.setLayoutData(GridDataFactory.fillDefaults().indent(10, 0).create());
    // final TableColumn column1 = new TableColumn(table, SWT.NONE);
    //
    // for (final IMethod method : index.get(type)) {
    // // final Link l = createMethodLink(container, method, workspaceBus);
    // // linkFactory.applyTo(l);
    //
    // final Link bar = createMethodLink(table, method, workspaceBus);
    // final TableItem item = new TableItem(table, SWT.NONE);
    // item.setText(new String[] { bar.getText() });
    // item.setFont(0, JFaceResources.getBannerFont());
    // final TableEditor editor = new TableEditor(table);
    // editor.grabHorizontal = editor.grabVertical = true;
    // editor.setEditor(bar, item, 0);
    //
    // }
    // column1.pack();
    //
    // }
    // }

    private final class MethodNameComparator implements Comparator<IMethod> {
        @Override
        public int compare(final IMethod o1, final IMethod o2) {
            final String s1 = JavaElementLabels.getElementLabel(o1, JavaElementLabels.ALL_FULLY_QUALIFIED);
            final String s2 = JavaElementLabels.getElementLabel(o2, JavaElementLabels.ALL_FULLY_QUALIFIED);
            return s1.compareTo(s2);
        }
    }

    private final class TypeNameComparator implements Comparator<IType> {
        @Override
        public int compare(final IType arg0, final IType arg1) {
            final String s0 = arg0.getFullyQualifiedName();
            final String s1 = arg1.getFullyQualifiedName();
            return s0.compareTo(s1);
        }
    }

    private final JavaElementResolver resolver;
    private final EventBus workspaceBus;

    @Inject
    public StaticHooksFinder(final JavaElementResolver resolver, final EventBus workspaceBus) {
        this.resolver = resolver;
        this.workspaceBus = workspaceBus;

    }

    @JavaSelectionSubscriber
    public Status onPackageRootSelection(final IPackageFragmentRoot root, final JavaSelectionEvent event,
            final Composite parent) throws ExecutionException {

        final TreeMultimap<IType, IMethod> index = TreeMultimap.create(new TypeNameComparator(),
                new MethodNameComparator());
        try {
            for (final IJavaElement e : root.getChildren()) {
                if (e.getElementType() == IJavaElement.PACKAGE_FRAGMENT) {
                    findStaticHooks((IPackageFragment) e, index);
                }
            }
        } catch (final Exception x) {
            RecommendersPlugin.logError(x, "Failed to determine static members for %s", root.getElementName());
        }
        runSyncInUiThread(new HooksRendererRunnable(index, parent));
        return Status.OK;
    }

    @JavaSelectionSubscriber
    public Status onPackageSelection(final IPackageFragment pkg, final JavaSelectionEvent event, final Composite parent)
            throws ExecutionException {

        final TreeMultimap<IType, IMethod> index = TreeMultimap.create(new TypeNameComparator(),
                new MethodNameComparator());
        try {
            findStaticHooks(pkg, index);
        } catch (final Exception e) {
            RecommendersPlugin.logError(e, "Failed to determine static members for package %s", pkg.getElementName());
        }

        runSyncInUiThread(new HooksRendererRunnable(index, parent));
        return Status.OK;
    }

    private void findStaticHooks(final IPackageFragment pkg, final TreeMultimap<IType, IMethod> index)
            throws JavaModelException {
        for (final ITypeRoot f : pkg.getClassFiles()) {
            findStaticHooks(index, f);
        }
        for (final ITypeRoot f : pkg.getCompilationUnits()) {
            findStaticHooks(index, f);
        }
    }

    private void findStaticHooks(final TreeMultimap<IType, IMethod> index, final ITypeRoot root)
            throws JavaModelException {
        final IType type = root.findPrimaryType();
        for (final IMethod m : type.getMethods()) {
            if (JdtFlags.isStatic(m) && JdtFlags.isPublic(m) && !JdtUtils.isInitializer(m)) {
                index.put(type, m);
            }
        }
    }
}
