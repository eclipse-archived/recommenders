package org.eclipse.recommenders.tests.completion.rcp.calls

import java.util.List
import org.eclipse.core.resources.ResourcesPlugin
import org.eclipse.jdt.core.dom.CompilationUnit
import org.eclipse.jdt.core.dom.MethodDeclaration
import org.eclipse.jdt.core.dom.NodeFinder
import org.eclipse.recommenders.commons.udc.ObjectUsage
import org.eclipse.recommenders.internal.completion.rcp.calls.engine.AstBasedObjectUsageResolver
import org.eclipse.recommenders.tests.jdt.JavaProjectFixture
import org.junit.Test

import static junit.framework.Assert.*
import org.eclipse.recommenders.internal.completion.rcp.calls.engine.CallsCompletionProposalComputer
import org.eclipse.recommenders.internal.completion.rcp.RecommendersCompletionContext
import org.eclipse.recommenders.tests.completion.rcp.RecommendersCompletionContextFactoryMock
import org.eclipse.recommenders.utils.rcp.JavaElementResolver
import org.eclipse.recommenders.tests.completion.rcp.JavaContentAssistContextMock
import org.eclipse.jdt.core.dom.AST
 
class CallCompletionProposalComputerSmokeTest { 
  
	@Test
	def void smokeTest(){
		val code =
'''
/**
 *$ Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This$ program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.$eclipse.org/legal/epl-v10.html
 *
 * Contributors$:
 *    Marcel Bruch $- initial API and implementation.
 */
package org.ecli$pse.recommenders.tests.comp$letion.rcp.calls$;$
$
im$port java.$util.*$;
$
/**
 * Some $class comments {@link$plain $}
 * 
 * @see $
 */
public class AllJavaFeatures<T extends Collection> {

    /**
     * $
     */
    static {
        S$et $s = new Has$hSet<St$ring>();
        s$.$add("$");
    }

    /**
     * $
     * 
     * @par$am $
     */
    pub$lic st$atic voi$d stat$ic1(fi$nal St$ring ar$g) {
        ch$ar$ c$ = a$rg.$charAt($);
        Str$ing $s $=$ "$"$;
    }
    
    
    
    priv$ate sta$tic cl$ass MyInne$rClass extend$s Obse$rvable{
        
        @Override
        pub$lic synchro$nized vo$id addObs$erver(Observ$er $o) {
        	o$
        	;
            // TO$DO A$uto-generated method stub
            sup$er.addOb$server($o);
            o.$
        }
    }
}
'''
		exercise(code)
	}
	
	
		@Test
		def void testFailCompletionOnTypeParameter(){
		val code =
			'''
			package completion.calls;
			import java.util.Collection;
			public class CompletionInClassWithGenerics {
			
				public void <T> test() {
					final T item;
					item.$
				}
			}'''
		exercise(code)
	}

	def private exercise (CharSequence code){
		val fixture = new JavaProjectFixture(ResourcesPlugin::getWorkspace(),"test")
		val struct = fixture.createFileAndParseWithMarkers(code.toString)
		val cu = struct.first;
		cu.becomeWorkingCopy(null)
		// just be sure that this file still compiles...
		val ast = cu.reconcile(AST::JLS4, true,true, null,null);
		assertNotNull(ast)
		val CallsCompletionProposalComputer sut = new CallsCompletionProposalComputer(new ModelStoreMock(), new JavaElementResolver(),
            new RecommendersCompletionContextFactoryMock())
		for(pos : struct.second){
			sut.computeCompletionProposals(new JavaContentAssistContextMock(cu, pos), null);
		}
	}
}