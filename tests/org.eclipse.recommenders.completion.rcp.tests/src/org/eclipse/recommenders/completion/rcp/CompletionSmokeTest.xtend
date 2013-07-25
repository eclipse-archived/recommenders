package org.eclipse.recommenders.completion.rcp

import com.google.common.base.Optional
import org.eclipse.core.resources.ResourcesPlugin
import org.eclipse.core.runtime.NullProgressMonitor
import org.eclipse.jdt.core.ICompilationUnit
import org.eclipse.jdt.core.IType
import org.eclipse.jdt.core.dom.AST
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposalComputer
import org.eclipse.recommenders.calls.ICallModel
import org.eclipse.recommenders.calls.ICallModelProvider
import org.eclipse.recommenders.calls.NullCallModel
import org.eclipse.recommenders.completion.rcp.processable.IntelligentCompletionProposalComputer
import org.eclipse.recommenders.completion.rcp.processable.ProcessableProposalFactory
import org.eclipse.recommenders.completion.rcp.processable.SessionProcessor
import org.eclipse.recommenders.completion.rcp.processable.SessionProcessorDescriptor
import org.eclipse.recommenders.internal.calls.rcp.CallCompletionSessionProcessor
import org.eclipse.recommenders.internal.overrides.rcp.OverrideCompletionSessionProcessor
import org.eclipse.recommenders.internal.rcp.CachingAstProvider
import org.eclipse.recommenders.internal.subwords.rcp.SubwordsSessionProcessor
import org.eclipse.recommenders.models.ProjectCoordinate
import org.eclipse.recommenders.models.rcp.IProjectCoordinateProvider
import org.eclipse.recommenders.overrides.IOverrideModel
import org.eclipse.recommenders.overrides.IOverrideModelProvider
import org.eclipse.recommenders.overrides.NullOverrideModel
import org.eclipse.recommenders.rcp.IAstProvider
import org.eclipse.recommenders.tests.jdt.JavaProjectFixture
import org.eclipse.recommenders.rcp.JavaElementResolver
import org.eclipse.ui.IEditorInput
import org.eclipse.ui.IEditorPart
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameters

import static org.eclipse.recommenders.tests.CodeBuilder.*
import static org.mockito.Matchers.*
import static org.mockito.Mockito.*

import static extension com.google.common.collect.Iterables.*
import org.eclipse.jdt.core.IMethod
import org.eclipse.recommenders.models.BasedTypeName
import org.eclipse.recommenders.utils.names.VmTypeName

@RunWith(Parameterized)
class CompletionSmokeTest {

    @Parameters(name="{index}")
    def static Iterable<Object[]> data() {
        return #{#{"calls"}.toArray, #{"overrides"}.toArray, #{"subwords"}.toArray}
    }

    String processor;

    new(String processor) {
        this.processor = processor;
    }

    @Test
    def void IMPORT_01() {
        '''
            $i$mport$ $java$.$uti$l.$
            public class «classname» {}
        '''.exercise
    }

    @Test
    def void IMPORT_02() {
        '''
            import $stat$ic$ $java$.$uti$l.Collection.$
            public class «classname» {}
        '''.exercise
    }

    @Test
    def void IMPORT_03() {
        '''
            $
            public class «classname» {}
        '''.exercise
    }

    @Test
    def void PACKAGE_01() {
        '''
            $
            public class «classname» {}
        '''.exercise
    }

    @Test
    def void PACKAGE_02() {
        '''
            pack$age $
            public class «classname» {}
        '''.exercise
    }

    @Test
    def void PACKAGE_03() {
        '''
            package org.$
            public class «classname» {}
        '''.exercise
    }

    @Test
    def void EXTENDS_01() {
        '''interface «classname» extends $Clo$sable {}'''.exercise
    }

    @Test
    def void EXTENDS_02() {
        '''class «classname» extends $Your$Class {}'''.exercise
    }

    @Test
    def void IMPLEMENTS_01() {
        '''class «classname» implements $Closab$le {}'''.exercise
    }

    @Test
    def void CLASSBODY_01() {
        classDeclaration(
            '''class «classname» extends UnknownType''',
            '''$'''
        ).exercise
    }

    @Test
    def void CLASSBODY_02() {
        classDeclaration(
            '''class «classname» extends UnknownType''',
            '''siz$'''
        ).exercise
    }

    @Test
    def void CLASSBODY_03() {
        classbody('''private UnknownType field = $''').exercise
    }

    @Test
    def void CLASSBODY_04() {
        classbody('''modifier Object o = $''').exercise
    }

    @Test
    def void CLASSBODY_05() {
        classbody('''public List = $''').exercise
    }

    @Test
    def void METHOD_STMT_01() {
        method('''Ob$;''').exercise
    }

    @Test
    def void METHOD_STMT_02() {
        method('''Object $''').exercise
    }

    @Test
    def void METHOD_STMT_03() {
        method('''Object $o$ = $''').exercise
    }

    @Test
    def void METHOD_STMT_04() {
        method('''Object o = new $''').exercise
    }

    @Test
    def void METHOD_STMT_05() {
        method(
            '''
                Object o = "";
                o.$
            ''').exercise
    }

    @Test
    def void METHOD_STMT_06() {
        classbody(
            '''void <T> m(T t){
		t.$
		}''').exercise
    }

    @Test
    def void METHOD_STMT_07() {
        method('''UnknownType.$exit$($)''').exercise
    }

    @Test
    def void METHOD_STMT_08() {
        method('''UnknownType o = $new $File($to$String())''').exercise
    }

    @Test
    def void METHOD_STMT_09() {
        method(
            '''
            UnknownType o = "";
            o.$''').exercise
    }

    @Test
    def void METHOD_STMT_10() {
        method('''undef$inedMethod($).$call($)''').exercise
    }

    @Test
    def void METHOD_STMT_11() {
        method('''java.util.Arrays.asList(get$)''').exercise
    }

    @Test
    def void METHOD_STMT_12() {
        method(
            '''List<?> l = new java.util.ArrayList();
		l.$subList(0, 1).$''').exercise
    }

    @Test
    def void COMMENTS_01() {
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
            public class Comments01 {
            	
            }
        '''.exercise
    }

    @Test
    def void COMMENTS_02() {
        classbody(
            '''
            /**
            * $
            */
            static {
            }''').exercise
    }

    @Test
    def void OLD_TEST_CLASS() {
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
            im$port $stati$c$ java.util.Collections.$;
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
                
                public void <T$> mT$ypeParameter(T$ s$) {
                    s.$;
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
        '''.exercise
    }

    @Test
    def void testStdCompletion() {
        methodWithLocals('''o.$''').exercise
    }

    @Test
    def void testOnConstructor() {
        methodWithLocals('''new Object().$''').exercise
    }

    @Test
    def void testOnReturn() {
        methodWithLocals('''l.get(0).$''').exercise
    }

    @Test
    def void testInIf() {
        methodWithLocals('''if(o.$)''').exercise
    }

    @Test
    def void testExpectsPrimitive() {
        methodWithLocals('''int i = o.$''').exercise
    }

    @Test
    def void testExpectsNonPrimitive() {
        methodWithLocals('''Object o1 = o.$''').exercise
    }

    @Test
    def void testInMessageSend() {
        methodWithLocals('''l.add(o.$)''').exercise
    }

    @Test
    def void testPrefix01() {
        methodWithLocals('''o.has$''').exercise
    }

    @Test
    def void testPrefix02() {
        methodWithLocals('''o.hashc$''').exercise
    }

    @Test
    def void testPrefix03() {
        methodWithLocals('''o.hashC$''').exercise
    }

    @Test
    def void testPrefix04() {
        methodWithLocals('''o.x$''').exercise
    }

    def methodWithLocals(CharSequence code) {
        classbody(
            '''
                public void __test(Object o, List l) {
                    «code»
                }
            ''')
    }

    static val fixture = new JavaProjectFixture(ResourcesPlugin.getWorkspace(), "test")

    def void exercise(CharSequence scenario) {
        val jer = new JavaElementResolver()
        val pcp = mock(IProjectCoordinateProvider)
        when(pcp.resolve(anyObject() as IType)).thenReturn(Optional.of(ProjectCoordinate.UNKNOWN))
        when(pcp.toName(anyObject() as IMethod)).thenReturn(Optional.absent())
        when(pcp.toBasedName(anyObject() as IType)).thenReturn(
            Optional.of(new BasedTypeName(ProjectCoordinate.UNKNOWN, VmTypeName.NULL)))

        val sut = createSut(pcp, jer)

        val struct = fixture.createFileAndParseWithMarkers(scenario)
        val cu = struct.first;
        cu.becomeWorkingCopy(null)

        // just be sure that this file still compiles...
        val ast = cu.reconcile(AST::JLS4, true, true, null, null);
        Assert.assertNotNull(ast)
        val computer = new MockedIntelligentCompletionProposalComputer(sut)

        for (completionIndex : struct.second) {
            val ctx = new JavaContentAssistContextMock(cu, completionIndex)
            val monitor = new NullProgressMonitor
            computer.sessionStarted
            computer.computeCompletionProposals(ctx, monitor)
            computer.sessionEnded
        }
    }

    static def complete(IJavaCompletionProposalComputer computer, ICompilationUnit cu, int completionIndex) {
        val ctx = new JavaContentAssistContextMock(cu, completionIndex)
        val monitor = new NullProgressMonitor
        computer.sessionStarted
        val res = computer.computeCompletionProposals(ctx, monitor)
        computer.sessionEnded
        return res;
    }

    def createSut(IProjectCoordinateProvider pcp, JavaElementResolver jer) {
        switch processor {
            case "calls": {
                val mp = mock(ICallModelProvider)
                when(mp.acquireModel(anyObject())).thenReturn(Optional.<ICallModel>of(NullCallModel.NULL_MODEL))
                return new CallCompletionSessionProcessor(pcp, mp)
            }
            case "overrides": {
                val mp = mock(IOverrideModelProvider)
                when(mp.acquireModel(anyObject())).thenReturn(
                    Optional.<IOverrideModel>of(NullOverrideModel.INSTANCE))
                return new OverrideCompletionSessionProcessor(pcp, mp, jer)
            }
            case "subwords": {
                return new MockSubwordsSessionProcessor(new CachingAstProvider)
            }
        }
    }

    static def newCallComputer() {
        val pcp = mock(IProjectCoordinateProvider)
        when(pcp.resolve(anyObject() as IType)).thenReturn(Optional.of(ProjectCoordinate.UNKNOWN))
        val mp = mock(ICallModelProvider)
        when(mp.acquireModel(anyObject())).thenReturn(Optional.<ICallModel>of(NullCallModel.NULL_MODEL))
        val sut = new CallCompletionSessionProcessor(pcp, mp)
        new MockedIntelligentCompletionProposalComputer(sut)

    }
}

class MockedIntelligentCompletionProposalComputer<T extends SessionProcessor> extends IntelligentCompletionProposalComputer {

    T processor;

    new(T processor) {
        super(
            #{new SessionProcessorDescriptor("", "", null, 0, true, processor)}.toArray(SessionProcessorDescriptor),
            new ProcessableProposalFactory(), new CachingAstProvider());
        this.processor = processor
    }

    def getProcessor() {
        return processor
    }

    override shouldReturnResults() {
        true;
    }
}

class MockSubwordsSessionProcessor extends SubwordsSessionProcessor {

    new(IAstProvider astProvider) {
        super(astProvider)
    }

    override IEditorPart lookupEditor(ICompilationUnit cu) {
        val ed = mock(IEditorPart)
        val in = mock(IEditorInput)
        when(ed.editorInput).thenReturn(in)
        when(in.getAdapter(any())).thenReturn(cu)
        ed
    }
}
