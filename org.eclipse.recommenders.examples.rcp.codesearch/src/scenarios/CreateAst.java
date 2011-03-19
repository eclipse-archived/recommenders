package scenarios;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

public class CreateAst {
	private void createASTFromSource(final String source) {
		final ASTParser parser = null;
		parser.setSource(source.toCharArray());
		final CompilationUnit cu;
	}
}
