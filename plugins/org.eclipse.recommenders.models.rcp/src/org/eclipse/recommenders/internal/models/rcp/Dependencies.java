package org.eclipse.recommenders.internal.models.rcp;

import static com.google.common.base.Optional.*;

import java.io.File;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.recommenders.models.DependencyInfo;
import org.eclipse.recommenders.models.DependencyType;

import com.google.common.base.Optional;
import com.google.common.collect.Maps;

public final class Dependencies {

    public static Optional<DependencyInfo> createJREDependencyInfo(final IJavaProject javaProject) {
        String executionEnvironmentId = getExecutionEnvironmentId(javaProject);

        try {
            IVMInstall vmInstall = JavaRuntime.getVMInstall(javaProject);
            File javaHome = vmInstall.getInstallLocation();

            Map<String, String> attributes = Maps.newHashMap();
            attributes.put(DependencyInfo.EXECUTION_ENVIRONMENT, executionEnvironmentId);
            return fromNullable(new DependencyInfo(javaHome, DependencyType.JRE, attributes));
        } catch (CoreException e) {
            return absent();
        }
    }

    private static String getExecutionEnvironmentId(final IJavaProject javaProject) {
        try {
            for (IClasspathEntry entry : javaProject.getRawClasspath()) {
                if (entry.getEntryKind() == IClasspathEntry.CPE_CONTAINER) {
                    return JavaRuntime.getExecutionEnvironmentId(entry.getPath());
                }
            }
        } catch (JavaModelException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static DependencyInfo createDependencyInfoForProject(final IJavaProject project) {
        File file = project.getPath().toFile();
        DependencyInfo dependencyInfo = new DependencyInfo(file, DependencyType.PROJECT);
        return dependencyInfo;
    }
}
