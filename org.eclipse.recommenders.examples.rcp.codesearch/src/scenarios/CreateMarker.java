package scenarios;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;

public class CreateMarker {
    public IMarker createMarker(final IFile file) throws CoreException {
        final IMarker m = null;
        m.setAttribute("attribute", "value");
        return null;
    }
}
