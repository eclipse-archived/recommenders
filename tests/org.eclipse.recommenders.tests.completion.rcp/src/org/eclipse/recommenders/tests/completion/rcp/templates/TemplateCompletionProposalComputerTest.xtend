package org.eclipse.recommenders.tests.completion.rcp.templates

import java.util.concurrent.atomic.AtomicInteger
import org.eclipse.core.resources.ResourcesPlugin
import org.eclipse.jdt.core.dom.AST
import org.eclipse.recommenders.tests.jdt.JavaProjectFixture
import org.junit.Before
import org.junit.Ignore
import org.junit.Test

import static junit.framework.Assert.*

@Ignore 
class TemplateCompletionProposalComputerTest { 
  
  static AtomicInteger classId = new AtomicInteger()
	static JavaProjectFixture fixture = new JavaProjectFixture(ResourcesPlugin::getWorkspace(),"test")
	@Before
	def void before(){
		fixture.clear
	}

	def method(CharSequence code){
		'''
		import javax.swing.*;
		public class Template«classId.incrementAndGet()» {
			void test (){
				«code»
			}
		}
		'''
	}
	
	@Test
	def void testNotImportedTypeNameCompletion(){
		val code = '''
			// import java.awt.Button;
			public class Template«classId.incrementAndGet()» {
				void test() {
					Button$
				}
			}'''
		test(code)
	}
	
	@Test
	def void testOnQulifiedTypeName(){
		val code = '''
			// import java.awt.Button;
			public class Template«classId.incrementAndGet()» {
				void test() {
					java.awt.Button$
				}
			}'''
		test(code)
	}
	
	@Test
	def void testImportedTypeNameCompletion(){
		val code = '''
			import java.awt.Button;
			public class Template«classId.incrementAndGet()» {
				void test() {
					Button$
				}
			}'''
		test(code)
	}
	
	@Test
	def void testInMessageSend(){
		val code = method('''
			List l;
			l.add(l$);
			''')
		test(code)
	}

	@Test
	def void testInCompletionOnQualifiedNameRef(){
		val code = method('''
			List l;
			l.$
			''')
		test(code)
	}


	@Test
	def void testInMessageSend2(){
		val code = method('''
			List l;
			l.add(l.$);
			''')
		test(code)
	}

	@Test
	def void testLocalWithTypeName(){
		val code = '''
			import java.awt.Button;
			public class Template«classId.incrementAndGet()» {
				void test() {
					Integer i= null;
					i$
				}
			}'''
		test(code)
	}

	def private test(CharSequence code){
		test(code,1)
	}

	def private test(CharSequence code, int numberOfExpectedProposals){
		val fixture = new JavaProjectFixture(ResourcesPlugin::getWorkspace(),"test")
		val struct = fixture.createFileAndParseWithMarkers(code.toString)
		val cu = struct.first;
		cu.becomeWorkingCopy(null)
		// just be sure that this file still compiles...
		val ast = cu.reconcile(AST::JLS4, true,true, null,null);
		assertNotNull(ast)
//		val sut = new TemplatesCompletionProposalComputer(new RecommendersCompletionContextFactoryMock())
//		val pos = struct.second.head;
//		val proposals = sut.computeCompletionProposals(new JavaContentAssistContextMock(cu, pos), null);
//		assertEquals("wrong number of proposals", numberOfExpectedProposals, proposals.size())
	}
}