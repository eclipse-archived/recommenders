package org.eclipse.recommenders.calls.rcp.it

import com.google.common.collect.Sets
import java.util.HashSet
import org.eclipse.core.resources.ResourcesPlugin
import org.eclipse.core.runtime.NullProgressMonitor
import org.eclipse.jdt.ui.SharedASTProvider
import org.eclipse.recommenders.calls.ICallModel
import org.eclipse.recommenders.calls.ICallModel.DefinitionKind
import org.eclipse.recommenders.completion.rcp.it.CompletionSmokeTest
import org.eclipse.recommenders.completion.rcp.it.MockedIntelligentCompletionProposalComputer
import org.eclipse.recommenders.internal.calls.rcp.CallCompletionSessionProcessor
import org.eclipse.recommenders.tests.CodeBuilder
import org.eclipse.recommenders.tests.jdt.JavaProjectFixture
import org.junit.Assert
import org.junit.Ignore
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
        verifyCalls(newHashSet())
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
        verifyCalls(newHashSet())
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
        verifyCalls(newHashSet())
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

        verifyDefinition(THIS);
        verifyCalls(newHashSet("equals", "hashCode", "wait"))
    }

    @Test
    def void testIfCondition() {
        code = CodeBuilder::method(
            '''
                Object o = null;
                if (o.hashCode() != 0) {
                }
                o.$
            ''')

        exercise()

        verifyDefinition(NULL_LITERAL)
        verifyCalls(newHashSet("hashCode"))
    }

    @Test
    @Ignore("Error Recovery does not work under Luna-M7. @see https://bugs.eclipse.org/bugs/show_bug.cgi?id=405778")
    def void testIfConditionThenCompletion() {
        code = CodeBuilder::method(
            '''
                Object o = new Object();
                if (o.hashCode() != 0) {
                o.$
                }
            ''')

        exercise()

        verifyDefinition(NEW)
        verifyCalls(newHashSet("hashCode"))
    }

    @Test
    def void testIfThen() {
        code = CodeBuilder::method(
            '''
                Object o = null;
                if (true) {
                    o.hashCode();
                }
                o.$
            ''')

        exercise()

        verifyDefinition(NULL_LITERAL)
        verifyCalls(newHashSet("hashCode"))
    }

    @Test
    def void testIfElse() {
        code = CodeBuilder::method(
            '''
                Object o = null;
                if (true) {
                } else {
                    o.hashCode();
                }
                o.$
            ''')

        exercise()

        verifyDefinition(NULL_LITERAL)
        verifyCalls(newHashSet("hashCode"))
    }

    @Test
    def void testIfThenElse() {
        code = CodeBuilder::method(
            '''
                Object o = null;
                if (true) {
                    o.equals()
                } else {
                    o.hashCode();
                }
                o.$
            ''')

        exercise()

        verifyDefinition(NULL_LITERAL)
        verifyCalls(newHashSet("equals", "hashCode"))
    }

    @Test
    @Ignore("Error Recovery does not work under Luna-M7. @see https://bugs.eclipse.org/bugs/show_bug.cgi?id=405778")
    def void testElse() {
        code = CodeBuilder::method(
            '''
                Object o = null;
                if (o.equals()) {
                    o.wait();
                } else {
                    o.hashCode();
                    o.$
                }
            ''')

        exercise()

        verifyDefinition(NULL_LITERAL)
        verifyCalls(newHashSet("equals", "wait", "hashCode"))
    }

    @Test
    def void testSwitch() {
        code = CodeBuilder::method(
            '''
                Object o = null;
                switch(o.hashCode()) {
                    case 0: o.equals();
                    default: o.wait();
                }
                o.$;
            ''')

        exercise()

        verifyDefinition(NULL_LITERAL)
        verifyCalls(newHashSet("hashCode", "equals", "wait"))
    }

    @Test
    def void testSwitchBreak() {
        code = CodeBuilder::method(
            '''
                Object o = null;
                switch(o.hashCode()) {
                    case 0: o.equals(); break;
                    default: o.wait(); break;
                }
                o.$;
            ''')

        exercise()

        verifyDefinition(NULL_LITERAL)
        verifyCalls(newHashSet("hashCode", "equals", "wait"))
    }

    @Test
    @Ignore("Error Recovery does not work under Luna-M7. @see https://bugs.eclipse.org/bugs/show_bug.cgi?id=405778")
    def void testWhileLoop() {
        code = CodeBuilder::method(
            '''
                Object o = null;
                while (o.equals()) {
                    o.hashCode();
                    o.$
                }
            ''')

        exercise()

        verifyDefinition(NULL_LITERAL)
        verifyCalls(newHashSet("equals", "hashCode"))
    }

    @Test
    def void testDoLoop() {
        code = CodeBuilder::method(
            '''
                Object o = null;
                do {
                    o.hashCode();
                } while (o.equals());
                o.$
            ''')

        exercise()

        verifyDefinition(NULL_LITERAL)
        verifyCalls(newHashSet("hashCode", "equals"))
    }

    @Test
    def void testForLoop() {
        code = CodeBuilder::method(
            '''
                Object o = null;
                for (int i=0; i<5; i++) {
                    o.hashCode();
                }
                o.$
            ''')

        exercise()

        verifyDefinition(NULL_LITERAL)
        verifyCalls(newHashSet("hashCode"))
    }

    @Test
    def void testForLoopCondition() {
        code = CodeBuilder::method(
            '''
                Object o = null;
                for (int i=0; i<o.hashCode(); i++) {
                    o.equals();
                }
                o.$
            ''')

        exercise()

        verifyDefinition(NULL_LITERAL)
        verifyCalls(newHashSet("hashCode", "equals"))
    }

    @Test
    def void testForEachLoop() {
        code = CodeBuilder::method(
            '''
                List l = null;
                for (Object o : l) {
                    o.hashCode();
                    l.size();
                }
                l.$
            ''')

        exercise()

        verifyDefinition(NULL_LITERAL)
        verifyCalls(newHashSet("size"))
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
        verifyCalls(newHashSet())
    }

    @Test
    def void testDefThis02() {
        code = CodeBuilder::method('''w$''')

        exercise()

        verifyDefinition(THIS)
        verifyCalls(newHashSet())
    }

    @Test
    def void testDefThis03() {
        code = CodeBuilder::method('''this.$''')
        exercise()

        verifyDefinition(THIS)
        verifyCalls(newHashSet())
    }

    @Test
    def void testDefThis03a() {
        code = CodeBuilder::method('''this.w$''')

        exercise()

        verifyDefinition(THIS)
        verifyCalls(newHashSet())
    }

    @Test
    def void testDefThis04() {
        code = CodeBuilder::method('''super.$''')

        exercise()

        verifyDefinition(THIS)
        verifyCalls(newHashSet())
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
        verifyCalls(newHashSet("equals"))
    }

    @Test
    def void testOnStringConstant_1() {
        code = CodeBuilder::method('''"".$''')

        exercise()

        verifyDefinition(STRING_LITERAL)
        verifyCalls(newHashSet())
    }

    @Test
    def void testOnStringConstant_2() {
        code = CodeBuilder::method('''"some".$''')

        exercise()

        verifyDefinition(STRING_LITERAL)
        verifyCalls(newHashSet())
    }

    @Test
    def void testArrayAccess_1() {
        code = CodeBuilder::method('''String[] args=null; args[0].$''')

        exercise()

        verifyDefinition(ARRAY_ACCESS)
        verifyCalls(newHashSet())
    }

    @Test
    def void testArrayAccess_2() {
        code = CodeBuilder::method('''String[] args=null; args[0].w$''')

        exercise()

        verifyDefinition(ARRAY_ACCESS)
        verifyCalls(newHashSet())
    }

    @Test
    def void testArrayAccess_3() {
        code = CodeBuilder::method('''String[] args=null; String arg = args[0]; arg.w$''')

        exercise()

        verifyDefinition(ARRAY_ACCESS)
        verifyCalls(newHashSet())
    }

    @Test
    def void testOther_3() {
        code = CodeBuilder::method('''String[] args=new String[0]; List l = null; l.$''')

        exercise()

        verifyDefinition(NULL_LITERAL)
        verifyCalls(newHashSet())
    }

    /**
     * documentation purpose: we simply match on variable names.
     * We do no control flow or variable scope analysis!
     */
    @Test
    def void testCallsOnReusedVar() {
        code = CodeBuilder::method(
            '''
                Object o = new Object();
                o.hashCode();
                o = new Object();
                o.equals(null);
                o.$
            ''')

        exercise()

        verifyDefinition(NEW)
        verifyCalls(newHashSet("hashCode", "equals"))
    }

    @Test
    def void testCallsOnThisAndSuper() {
        code = CodeBuilder::method(
            '''
                hashCode();
                super.wait();
                this.equals(null);
                $
            ''')

        exercise()

        verifyDefinition(THIS)
        verifyCalls(newHashSet("hashCode", "wait", "equals"))
    }

    @Test
    def void testCallsSuperConstructor() {
        val className = "TestCallsSuperConstructor"
        code = CodeBuilder::classbody(className,
            className + '''
                () {
                    super();
                    $
                }
            ''')

        exercise()

        verifyDefinition(THIS)
        verifyCalls(newHashSet("<init>"))
    }

    @Test
    def void testCallThisConstructor() {
        val className = "TestCallThisConstructor"
        code = CodeBuilder::classbody(className,
            className + '''
                () {
                }
            ''' + className + '''
                (String s) {
                    this();
                    $
                }
            ''')

        exercise()

        verifyDefinition(THIS)
        verifyCalls(newHashSet("<init>"))
    }

    @Test
    def void testDefConstructor() {
        code = CodeBuilder::method(
            '''
                Object o = new Object();
                o.$
            ''')

        exercise()

        verifyDefinition(NEW)
        verifyCalls(newHashSet())
    }

    @Test
    def void testDefSuperMethodReturn() {
        code = CodeBuilder::method(
            '''
                Integer hash = super.hashCode();
                hash.$
            ''')

        exercise()

        verifyDefinition(RETURN)
        verifyCalls(newHashSet())
    }

    @Test
    def void testDefOnCallChain() {
        code = CodeBuilder::method(
            '''
                Integer i = Executors.newCachedThreadPool().hashCode();
                i.$
            ''')

        exercise()

        verifyDefinition(RETURN)
        verifyCalls(newHashSet())
    }

    @Test
    def void testDefOnAlias() {
        code = CodeBuilder::method(
            '''
                Object a = new Object();
                Object b = a;
                b.$
            ''')

        exercise()

        // Field is really just an unknown definition
        verifyDefinition(FIELD)
        verifyCalls(newHashSet())
    }

    @Test
    def void testDefAssignment() {
        code = CodeBuilder::method(
            '''
                Object a = new Object();
                Object b = new Object();
                b = a;
                b.$
            ''')

        exercise()

        verifyDefinition(NEW)
        verifyCalls(newHashSet())
    }

    @Test
    def void testDefFor() {
        code = CodeBuilder.method(
            '''
                List<String> l;
                for(Iterator<String> it = l.iterator(); it.$) {
                    
                }
            ''')

        exercise()

        verifyDefinition(RETURN);
        verifyCalls(newHashSet())
    }

    def verifyDefinition(DefinitionKind expected) {
        val actual = model.observedDefinitionKind.orNull
        Assert.assertEquals(expected, actual)
    }

    def void exercise() {
        val struct = fixture.createFileAndParseWithMarkers(code)
        val cu = struct.first;
        cu.becomeWorkingCopy(null)

        val ast = SharedASTProvider.getAST(cu, SharedASTProvider.WAIT_YES, new NullProgressMonitor());
        Assert.assertNotNull(ast)

        computer = Stubs.newCallComputer
        processor = computer.getProcessor
        CompletionSmokeTest.complete(computer, cu, struct.second.head)
        model = processor.model
    }
}
