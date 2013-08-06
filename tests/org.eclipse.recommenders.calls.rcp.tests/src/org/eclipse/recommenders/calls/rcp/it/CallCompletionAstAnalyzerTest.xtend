package org.eclipse.recommenders.calls.rcp.it

import com.google.common.collect.Sets
import java.util.HashSet
import org.eclipse.core.resources.ResourcesPlugin
import org.eclipse.jdt.core.dom.AST
import org.eclipse.recommenders.calls.ICallModel
import org.eclipse.recommenders.calls.ICallModel.DefinitionKind
import org.eclipse.recommenders.completion.rcp.it.CompletionSmokeTest
import org.eclipse.recommenders.completion.rcp.it.MockedIntelligentCompletionProposalComputer
import org.eclipse.recommenders.internal.calls.rcp.CallCompletionSessionProcessor
import org.eclipse.recommenders.tests.CodeBuilder
import org.eclipse.recommenders.tests.jdt.JavaProjectFixture
import org.junit.Assert
import org.junit.Test

import static org.eclipse.recommenders.calls.ICallModel.DefinitionKind.*

class CallCompletionAstAnalyzerTest {

    static val fixture = new JavaProjectFixture(ResourcesPlugin.getWorkspace(), "test")
    CharSequence code

    MockedIntelligentCompletionProposalComputer<CallCompletionSessionProcessor> computer

    CallCompletionSessionProcessor processor

    ICallModel model

    @Test
    def void testDefMethodReturn01() {
        code = CodeBuilder::method(
            '''
            List l = Collections.emptyList();
            l.get(0).$''')
        exercise()
        verifyDefinition(RETURN)
    }

    @Test
    def void testDefMethodReturn012() {
        code = CodeBuilder::method(
            '''
            List l;
            Object o = l.get(0);
            o.$''')
        exercise()

        verifyDefinition(RETURN)
    }

    @Test
    def void testDefField() {
        code = CodeBuilder::classbody(
            '''
            List l;
            void __test(){
            	l.$;
            }''')
        exercise()

        verifyDefinition(FIELD)
    }

    @Test
    def void testFindCalls_01() {
        code = CodeBuilder::method(
            '''
                Object o = null;
                o.equals(new Object() {
                    public boolean equals(Object obj) {
                        o.hashCode();
                        return false;
                    }
                });
                o.$
                }
            ''')
        exercise()

        verifyDefinition(NULL_LITERAL)
        verifyCalls(newHashSet("equals"))
    }

    @Test
    def void testFindCalls_02() {
        code = CodeBuilder::method(
            '''
                Object o = null;
                o.equals();
                Object o2 = null;
                o2.hashCode();
                o.$
                }
            ''')
        exercise()
        verifyDefinition(NULL_LITERAL)
        verifyCalls(newHashSet("equals"))
    }

    @Test
    def void testFindCalls_03() {
        code = CodeBuilder::method(
            '''
                hashCode();
                this.wait();
                super.equals(this);
                w$
                }
            ''')
        exercise()
        verifyCalls(newHashSet("equals", "hashCode", "wait"))
    }


    def verifyCalls(HashSet<String> strings) {
        val actual = model.observedCalls.map[name].toSet
        val diff = Sets.difference(strings, actual)
        Assert.assertTrue(diff.toString(), diff.empty)
    }

    @Test
    def void testDefThis01() {
        code = CodeBuilder::method('''$''')
        exercise()

        verifyDefinition(THIS)
    }

    @Test
    def void testDefThis02() {
        code = CodeBuilder::method('''w$''')
        exercise()
        verifyDefinition(THIS)
    }

    @Test
    def void testDefThis03() {
        code = CodeBuilder::method('''this.$''')
        exercise()

        verifyDefinition(THIS)
    }

    @Test
    def void testDefThis03a() {
        code = CodeBuilder::method('''this.w$''')
        exercise()

        verifyDefinition(THIS)
    }

    @Test
    def void testDefThis04() {
        code = CodeBuilder::method('''super.$''')
        exercise()

        verifyDefinition(THIS)
    }

    @Test
    def void testDefThis05() {
        code = CodeBuilder::classbody(
            '''
            public boolean equals(Object o){
            	boolean res = super.equals(o);
            	this.hash$
            }''')
        exercise()

        verifyDefinition(THIS)
    }

    @Test
    def void testOnStringConstant_1() {
        code = CodeBuilder::method('''"".$''')
        exercise()
        verifyDefinition(STRING_LITERAL)
    }

    @Test
    def void testOnStringConstant_2() {
        code = CodeBuilder::method('''"some".$''')
        exercise()
        verifyDefinition(STRING_LITERAL)
    }

    @Test
    def void testArrayAccess_1() {
        code = CodeBuilder::method('''String[] args=null; args[0].$''')
        exercise()
        verifyDefinition(ARRAY_ACCESS)
    }

    @Test
    def void testArrayAccess_2() {
        code = CodeBuilder::method('''String[] args=null; args[0].w$''')
        exercise()
        verifyDefinition(ARRAY_ACCESS)
    }

    @Test
    def void testArrayAccess_3() {
        code = CodeBuilder::method('''String[] args=null; String arg = args[0]; arg.w$''')
        exercise()
        verifyDefinition(ARRAY_ACCESS)
    }

    @Test
    def void testOther_3() {
        code = CodeBuilder::method('''String[] args=new String[0]; args.; List l=null; l.$''')
        exercise()
        verifyDefinition(NULL_LITERAL)
    }

    def verifyDefinition(DefinitionKind expected) {
        val actual = model.observedDefinitionKind.orNull
        Assert.assertEquals(expected, actual)
    }

    def void exercise() {
        val struct = fixture.createFileAndParseWithMarkers(code)
        val cu = struct.first;
        cu.becomeWorkingCopy(null)

        // just be sure that this file still compiles...
        val ast = cu.reconcile(AST::JLS4, true, true, null, null);
        Assert.assertNotNull(ast)

        computer = Stubs.newCallComputer
        processor = computer.getProcessor
        CompletionSmokeTest.complete(computer, cu, struct.second.head)
        model = processor.model
    }

}
