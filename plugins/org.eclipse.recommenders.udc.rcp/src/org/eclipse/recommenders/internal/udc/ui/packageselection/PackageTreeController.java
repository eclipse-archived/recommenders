/**
 * Copyright (c) 2011 Andreas Frankenberger.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andreas Frankenberger - initial API and implementation.
 */
package org.eclipse.recommenders.internal.udc.ui.packageselection;

/**
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andreas Frankenberger - initial API and implementation.
 */
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Tree;

public class PackageTreeController {
    private final class PackageTreeCellLabelProvider extends StyledCellLabelProvider {

        // TODO readable code
        {
            setOwnerDrawEnabled(true);
        }
        final Color additionalTextColor = Display.getDefault().getSystemColor(SWT.COLOR_DARK_YELLOW);
        final Color packageNameColor = Display.getDefault().getSystemColor(SWT.COLOR_BLACK);

        @Override
        public void update(final ViewerCell cell) {
            final Package element = (Package) cell.getElement();
            String cellText = "";
            final String packageName = element.toString();
            final boolean isPackageIncluded = packageTester.matches(element.getPackageIdentifier());
            if (!isSubtreeMatching(element, isPackageIncluded)) {
                final String additionalText = " (subtree contains " + (isPackageIncluded ? "excluded" : "included")
                        + " packages)";
                final StyleRange[] ranges = calculateStyleRanges(packageName, additionalText);
                cell.setStyleRanges(ranges);
                cellText = packageName + " " + additionalText;
            } else {
                cellText = packageName;
            }
            cell.setText(cellText);
            cell.setImage(getImage(isPackageIncluded));
        }

        private StyleRange[] calculateStyleRanges(final String packageName, final String additionalText) {
            final StyleRange packageNameRange = new StyleRange(0, packageName.length(), packageNameColor, null);
            final StyleRange additionalTextRange = new StyleRange(packageName.length() + 1, additionalText.length(),
                    additionalTextColor, null);
            final StyleRange[] ranges = new StyleRange[] { packageNameRange, additionalTextRange };
            return ranges;
        }

        private Image getImage(final boolean isPackageIncluded) {
            if (isPackageIncluded) {
                return ImageProvider.getInstance().getPackageMatchesExpressionsImage();
            } else {
                return ImageProvider.getInstance().getPackageDoesNotMatchExpressionsImage();
            }
        }
    }

    TreeViewer viewer;
    PackageTester packageTester = new PackageTester();
    PackageTreeContentProvider contentProvider;

    public PackageTreeController(final Tree tree) {
        viewer = new TreeViewer(tree);
        viewer.setContentProvider(contentProvider = new PackageTreeContentProvider());
        viewer.setLabelProvider(createLabelProvider());
        viewer.setComparator(new ViewerComparator());
    }

    private IBaseLabelProvider createLabelProvider() {

        return new PackageTreeCellLabelProvider();
    }

    public void setProjects(final IProject[] projects) {
        viewer.setInput(projects);
    }

    public void updateIncludedPackages(final String[] expressions) {
        packageTester.setIncludes(expressions);
    }

    public void updateFilter() {
        viewer.refresh();
    }

    public void updateExcludesPackages(final String[] expressions) {
        packageTester.setExcludes(expressions);
    }

    public void updateCheckedState() {

    }

    private boolean isSubtreeMatching(final Package parent, final boolean matchExpected) {
        for (final Package child : contentProvider.getChildren(parent)) {
            final boolean matches = packageTester.matches(child.getPackageIdentifier());
            if (matches != matchExpected) {
                return false;
            }
            if (!isSubtreeMatching(child, matchExpected)) {
                return false;
            }
        }
        return true;
    }
}
