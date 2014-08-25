package org.eclipse.recommenders.completion.rcp.it

import org.eclipse.core.resources.ResourcesPlugin
import org.eclipse.jdt.ui.SharedASTProvider
import org.eclipse.recommenders.completion.rcp.CompletionContextFunctions.ImportedPackagesFunction
import org.eclipse.recommenders.completion.rcp.CompletionContextKey
import org.eclipse.recommenders.completion.rcp.IRecommendersCompletionContext
import org.eclipse.recommenders.testing.jdt.JavaProjectFixture
import org.eclipse.recommenders.utils.names.VmPackageName
import org.junit.Test

import static org.eclipse.recommenders.testing.CodeBuilder.*
import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*
import static org.mockito.Mockito.*

class ImportedPackagesContextFunctionTest {

    @Test
    def void IMPORT_01() {
        '''
            import java.util.List;
            import static java.text.MessageFormat.format;
            import static javax.print.ServiceUIFactory.*;
            import javax.xml.*;
            public class «classname» {}
        '''.exercise
    }

    def void exercise(CharSequence code) {
        val fixture = new JavaProjectFixture(ResourcesPlugin.getWorkspace(), "test")
        val cu = fixture.createFile(code.toString, true)
        val ast = SharedASTProvider.getAST(cu, SharedASTProvider.WAIT_YES, null)
        
        val ctx = mock(IRecommendersCompletionContext)
        when(ctx.AST).thenReturn(ast)
        
        val actual = new ImportedPackagesFunction().compute(ctx, CompletionContextKey.IMPORTED_PACKAGES)
        assertThat(actual, hasItem(VmPackageName.get("java/util")))
        assertThat(actual, hasItem(VmPackageName.get("java/text")))
        assertThat(actual, hasItem(VmPackageName.get("javax/print")))
        assertThat(actual, hasItem(VmPackageName.get("javax/xml")))
    }
}
