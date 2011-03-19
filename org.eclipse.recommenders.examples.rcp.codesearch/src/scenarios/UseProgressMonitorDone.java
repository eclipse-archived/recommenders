package scenarios;

import org.eclipse.core.runtime.IProgressMonitor;

public class UseProgressMonitorDone {

    public void useProgressMonitor(final IProgressMonitor monitor) {
        monitor.beginTask("Performing task: ", 10);
        monitor.subTask("step");
        monitor.worked(1);
    }
}
