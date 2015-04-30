/**
Copyright (c) 2014 Olav Lenz.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Eclipse Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *      Olav Lenz - initial API and implementation
 */
package org.eclipse.recommenders.internal.models.rcp;

import static org.eclipse.recommenders.rcp.SharedImages.Images.OBJ_JAR;

import java.util.Comparator;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.recommenders.coordinates.DependencyInfo;
import org.eclipse.recommenders.coordinates.ProjectCoordinate;
import org.eclipse.recommenders.injection.InjectionService;
import org.eclipse.recommenders.models.IDependencyListener;
import org.eclipse.recommenders.models.rcp.IProjectCoordinateProvider;
import org.eclipse.recommenders.rcp.SharedImages;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.FilteredItemsSelectionDialog;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;

@SuppressWarnings("restriction")
public class ProjectCoordinateSelectionDialog extends FilteredItemsSelectionDialog {

    private static final String DIALOG_SETTINGS = "ProjectCoordinateSelectionDialog"; //$NON-NLS-1$

    private LabelProvider labelProvider;

    private final SharedImages images;
    private final IDependencyListener dependencyListener;
    private final IProjectCoordinateProvider pcAdvisor;

    public ProjectCoordinateSelectionDialog(Shell shell) {
        super(shell, true);
        setTitle(Messages.DIALOG_TITLE_SELECT_PROJECT_COORDINATE);

        this.images = InjectionService.getInstance().requestInstance(SharedImages.class);
        this.dependencyListener = InjectionService.getInstance().requestInstance(IDependencyListener.class);
        this.pcAdvisor = InjectionService.getInstance().requestInstance(IProjectCoordinateProvider.class);

        this.labelProvider = new LabelProvider() {
            @Override
            public String getText(Object element) {
                if (element == null) {
                    return ""; //$NON-NLS-1$
                }
                if (element instanceof ProjectCoordinate) {
                    return createLabelForProjectCoordinate((ProjectCoordinate) element);
                }
                return element.toString();
            }

            @Override
            public Image getImage(Object element) {
                if (element instanceof ProjectCoordinate) {
                    return createImageForProjectCoordinate((ProjectCoordinate) element);
                }
                return super.getImage(element);
            }
        };

        setListLabelProvider(labelProvider);
        setDetailsLabelProvider(labelProvider);
    }

    public Image createImageForProjectCoordinate(ProjectCoordinate element) {
        return images.getImage(OBJ_JAR);
    }

    public String createLabelForProjectCoordinate(ProjectCoordinate element) {
        return element.toString();
    }

    @Override
    protected Control createExtendedContentArea(Composite parent) {
        return null;
    }

    @Override
    protected IDialogSettings getDialogSettings() {
        IDialogSettings settings = IDEWorkbenchPlugin.getDefault().getDialogSettings().getSection(DIALOG_SETTINGS);

        if (settings == null) {
            settings = IDEWorkbenchPlugin.getDefault().getDialogSettings().addNewSection(DIALOG_SETTINGS);
        }

        return settings;
    }

    @Override
    protected IStatus validateItem(Object item) {
        return Status.OK_STATUS;
    }

    @Override
    protected ItemsFilter createFilter() {
        return new ItemsFilter() {
            @Override
            public boolean matchItem(Object item) {
                if (item instanceof ProjectCoordinate) {
                    if (filter((ProjectCoordinate) item)) {
                        return false;
                    }
                }
                return matches(item.toString());
            }

            @Override
            public String getPattern() {
                String pattern = super.getPattern();
                if (pattern.equals("")) { //$NON-NLS-1$
                    return "?"; //$NON-NLS-1$
                }
                return pattern;
            }

            @Override
            public boolean isConsistentItem(Object item) {
                return true;
            }
        };
    }

    /**
     * Subclasses can override to filter out unwanted project coordinates. I.e. coordinates that have already been
     * selected.
     *
     * @return <code>false</code> if ProjectCoordinate should be displayed, <code>false</code> otherwise.
     */
    public boolean filter(ProjectCoordinate pc) {
        return false;
    }

    @Override
    protected Comparator<?> getItemsComparator() {
        return Ordering.usingToString();
    }

    @Override
    protected void fillContentProvider(AbstractContentProvider contentProvider, ItemsFilter itemsFilter,
            IProgressMonitor monitor) throws CoreException {
        ImmutableSet<DependencyInfo> dependencies = dependencyListener.getDependencies();
        monitor.beginTask(Messages.DIALOG_RESOLVING_DEPENDENCIES, dependencies.size());
        try {
            for (DependencyInfo dependencyInfo : dependencies) {
                ProjectCoordinate pc = pcAdvisor.resolve(dependencyInfo).orNull();
                if (pc != null) {
                    contentProvider.add(pc, itemsFilter);
                }
                monitor.worked(1);
            }
        } finally {
            monitor.done();
        }
    }

    @Override
    public String getElementName(Object item) {
        return labelProvider.getText(item);
    }

    public Set<ProjectCoordinate> getSelectedElements() {
        Set<ProjectCoordinate> selectedElements = Sets.newHashSet();
        Object[] result = getResult();
        if (result != null) {
            for (Object object : result) {
                if (object instanceof ProjectCoordinate) {
                    ProjectCoordinate pc = (ProjectCoordinate) object;
                    selectedElements.add(pc);
                }
            }
        }
        return selectedElements;
    }
}
