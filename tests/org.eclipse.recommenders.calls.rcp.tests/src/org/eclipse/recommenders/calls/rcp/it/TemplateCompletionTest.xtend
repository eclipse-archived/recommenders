package org.eclipse.recommenders.calls.rcp.it

class TemplateCompletionTest { 
  
//  TemplatesCompletionProposalComputer sut 
//  List<IJavaCompletionProposal> proposals
//  CharSequence code
//  
//  	@Test
//  	def testThis(){
//  		code = method('''
//  			$
//  		''')
//  		exercise()
//  		assertEquals(THIS, sut.getCompletionMode())
//  		assertEquals("", sut.getMethodPrefix())
//  		assertEquals("", sut.getVariableName())
//  	}
//  
//  	@Test
//  	def testThisWithThisPrefix(){
//  		code = method('''
//  			this.$
//  		''')
//  		exercise()
//  		assertEquals(THIS, sut.getCompletionMode())
//  		assertEquals("", sut.getMethodPrefix())
//  		assertEquals("", sut.getVariableName())
//  	}
//
//	@Test
//  	def testThisWithSuperPrefix(){
//  		code = method('''
//  			super.$
//  		''')
//  		exercise()
//  		assertEquals(THIS, sut.getCompletionMode())
//  		assertEquals("", sut.getMethodPrefix())
//  		assertEquals("", sut.getVariableName())
//  	}
//    
//	@Test
//  	def testThisWithMethodPrefix(){
//  		code = method('''
//  			eq$
//  		''')
//  		exercise()
//  		assertEquals(THIS, sut.getCompletionMode())
//  		assertEquals("eq", sut.getMethodPrefix())
//  		assertEquals("", sut.getVariableName())
//  	}
//  
//
//	@Test
//  	def testType(){
//  		code = method('''
//  			List$
//  		''')
//  		exercise()
//  		assertEquals(TYPE_NAME, sut.getCompletionMode())
//  		assertEquals("", sut.getMethodPrefix())
//  		assertEquals("", sut.getVariableName())
//  	}
//  
//  	@Test
//  	def testQualifiedType(){
//  		code = method('''
//  			java.util.List$
//  		''')
//  		exercise()
//  		assertEquals(TYPE_NAME, sut.getCompletionMode())
//  		assertEquals("", sut.getMethodPrefix())
//  		assertEquals("", sut.getVariableName())
//  	}
//  	
//	@Test
//  	def testThisOnVariableName(){
//  		code = method('''
//  			Event evt;
//  			evt$
//  		''')
//  		exercise()
//  		assertEquals(THIS, sut.getCompletionMode())
//  		assertEquals("evt", sut.getMethodPrefix())
//  		assertEquals("", sut.getVariableName())
//  	}
//  	
//  	@Test
//  	def testBehindQualifiedType(){
//  		code = method('''
//  			List $
//  		''')
//  		exercise()
//  		assertNull(sut.getCompletionMode())
//  	}
//
//	@Test
//  	def testMemberAccess(){
//  		code = method('''
//  			Event evt;
//  			evt.$
//  		''')
//  		exercise()
//  		assertEquals(MEMBER_ACCESS, sut.getCompletionMode())
//  		assertEquals("", sut.getMethodPrefix())
//  		assertEquals("evt", sut.getVariableName())
//  	}
//
//	@Test
//  	def testQualifiedMemberAccess(){
//  		code = method('''
//  			Event evt;
//  			evt.evt.$
//  		''')
//  		exercise()
//  		assertEquals(MEMBER_ACCESS, sut.getCompletionMode())
//  		assertEquals("", sut.getMethodPrefix())
//  		assertEquals("evt.evt", sut.getVariableName())
//  	}
//  	
//  	@Test
//  	def testQualifiedMemberAccessWithMethodPrefix(){
//  		code = method('''
//  			Event evt;
//  			evt.evt.eq$
//  		''')
//  		exercise()
//  		assertEquals(MEMBER_ACCESS, sut.getCompletionMode())
//  		assertEquals("eq", sut.getMethodPrefix())
//  		assertEquals("evt.evt", sut.getVariableName())
//  	}
//  	
//  	@Test
//  	@Ignore("Not possible to distinguish this case and testThisOnVariableName")
//  	def testNoTemplates(){
//  		code = method('''
//  			Event evt = $
//  		''')
//  		exercise()
//  		assertEquals(null, sut.getCompletionMode())
//  	}
//
//	def private exercise(){
//		val fixture = new JavaProjectFixture(ResourcesPlugin::getWorkspace(),"test")
//		val struct = fixture.createFileAndParseWithMarkers(code.toString)
//		val cu = struct.first;
//		cu.becomeWorkingCopy(null)
//		// just be sure that this file still compiles...
//		val ast = cu.reconcile(AST::JLS4, true,true, null,null);
//		assertNotNull(ast)
//		sut = new TestingTemplateCompletionProposalComputer(new RecommendersCompletionContextFactoryMock(),
//			new ModelStoreMock(), new JavaElementResolver())
//		val pos = struct.second.head;
//		proposals = sut.computeCompletionProposals(new JavaContentAssistContextMock(cu, pos), null);
//	}
}

//public class TestingTemplateCompletionProposalComputer extends TemplatesCompletionProposalComputer {
//
// public TestingTemplateCompletionProposalComputer(final IRecommendersCompletionContextFactory ctxFactory,
// final IModelArchiveStore<IType, IObjectMethodCallsNet> store, final JavaElementResolver elementResolver) {
// super(ctxFactory, store, elementResolver);
// }
//
// @Override
// protected boolean shouldMakeProposals() {
// return true;
// }

// }