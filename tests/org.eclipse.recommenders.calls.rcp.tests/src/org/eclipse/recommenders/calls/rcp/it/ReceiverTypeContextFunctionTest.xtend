package org.eclipse.recommenders.calls.rcp.it

import org.eclipse.core.resources.ResourcesPlugin
import org.eclipse.jdt.core.IType
import org.eclipse.jdt.core.dom.AST
import org.eclipse.recommenders.completion.rcp.RecommendersCompletionContext
import org.eclipse.recommenders.completion.rcp.it.JavaContentAssistContextMock
import org.eclipse.recommenders.internal.calls.rcp.CallCompletionContextFunctions.ReceiverTypeContextFunction
import org.eclipse.recommenders.internal.rcp.CachingAstProvider
import org.eclipse.recommenders.tests.CodeBuilder
import org.eclipse.recommenders.tests.jdt.JavaProjectFixture
import org.junit.Assert
import org.junit.Test

class ReceiverTypeContextFunctionTest {

	static val fixture = new JavaProjectFixture(ResourcesPlugin.getWorkspace(), "test")
	CharSequence code

	Object res

	@Test
	def void testExplicitThis() {
		code = CodeBuilder::method('''this.$''')
		exercise()
		verifyType("Object")
	}

	@Test
	def void testImplicitThis01() {
		code = CodeBuilder::method('''$''')
		exercise()
		verifyType("Object")
	}

	@Test
	def void testImplicitThis02() {
		code = CodeBuilder::method('''h$''')
		exercise()
		verifyType("Object")
	}

	@Test
	def void testExplicitSuper() {
		code = CodeBuilder::method('''super.$''')
		exercise()
		verifyType("Object")
	}

	def void exercise() {
		val struct = fixture.createFileAndParseWithMarkers(code)
		val cu = struct.first;
		cu.becomeWorkingCopy(null)

		// just be sure that this file still compiles...
		val ast = cu.reconcile(AST::JLS4, true, true, null, null);
		Assert.assertNotNull(ast)
		val ctx = new JavaContentAssistContextMock(cu, struct.second.head)
		val sut = new ReceiverTypeContextFunction;
		val rctx = new RecommendersCompletionContext(ctx, new CachingAstProvider)
		res = sut.compute(rctx, null);
	}

	def verifyType(String simpleClassname) {
		val receiver = res as IType;
		Assert.assertEquals(simpleClassname, receiver.elementName)
	}
}
