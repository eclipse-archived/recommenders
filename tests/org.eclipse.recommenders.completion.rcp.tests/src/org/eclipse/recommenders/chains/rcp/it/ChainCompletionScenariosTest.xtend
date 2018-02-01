package org.eclipse.recommenders.chains.rcp.it

import java.util.List
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.SystemUtils
import org.eclipse.core.resources.ResourcesPlugin
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions
import org.eclipse.jface.preference.IPreferenceStore
import org.eclipse.recommenders.internal.chain.rcp.ChainCompletionProposal
import org.eclipse.recommenders.internal.chain.rcp.ChainCompletionProposalComputer
import org.eclipse.recommenders.internal.chain.rcp.ChainsPreferencePage
import org.eclipse.recommenders.internal.rcp.CachingAstProvider
import org.eclipse.recommenders.rcp.IAstProvider
import org.eclipse.recommenders.testing.CodeBuilder
import org.eclipse.recommenders.testing.jdt.JavaProjectFixture
import org.eclipse.recommenders.testing.rcp.jdt.JavaContentAssistContextMock
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.mockito.Mockito

import static org.eclipse.recommenders.testing.SmokeTestScenarios.*
import static org.junit.Assert.*

@SuppressWarnings("unchecked")
class ChainCompletionScenariosTest {

    static JavaProjectFixture fixture = new JavaProjectFixture(ResourcesPlugin::getWorkspace(), "test")

    @Before
    def void before() {
        fixture.clear
    }

    static def javaVersionAtLeast(String minimumVersion) {
        val runtimeJdk = CompilerOptions.versionToJdkLevel(SystemUtils::JAVA_SPECIFICATION_VERSION)
        if (runtimeJdk == 0) {
            return true // Runtime JDK not known to JDT; must be newer
        }
        val expectedJdk = CompilerOptions.versionToJdkLevel(minimumVersion)
        if (expectedJdk == 0) {
            return false // Expected JDK not known to JDT but runtime is; hence, expected must be newer than runtime
        }
        return runtimeJdk >= expectedJdk;
    }

    @Test
    def void smokeTestScenarios() {
        for (scenario : scenarios) {
            val struct = fixture.createFileAndParseWithMarkers(scenario)
            val cu = struct.first;

            for (completionIndex : struct.second) {
                val ctx = new JavaContentAssistContextMock(cu, completionIndex)
                val sut = new TestingChainCompletionProposalComputer(new CachingAstProvider,
                    ChainPreferenceStoreMock::create())
                sut.sessionStarted
                sut.computeCompletionProposals(ctx, null)
            }
        }
    }

    @Test
    def void testAccessMethodParameter() {
        val code = CodeBuilder::classbody(
            '''
                public void method(final List list){
                	Iterator it = $
                }
            ''')
        var wanted =
            newArrayList(
                "list iterator",
                "list listIterator",
                "list listIterator",
                "list subList iterator",
                "list subList listIterator",
                "list subList listIterator"
            )
        if (javaVersionAtLeast("1.8")) {
            wanted.add("list parallelStream iterator")
            wanted.add("list stream iterator")
        }
        exercise(code, w(wanted));
    }

    @Test
    def void testAvoidRecursiveCallsToMember() {
        val code = CodeBuilder::classbody('''AvoidRecursiveCallsToMember''',
            '''
                public AtomicBoolean findMe = new AtomicBoolean();
                
                public AvoidRecursiveCallsToMember getSubElement() {
                	return new AvoidRecursiveCallsToMember();
                }
                
                public static void method2() {
                	final AvoidRecursiveCallsToMember useMe = new AvoidRecursiveCallsToMember();
                	final AtomicBoolean c = useMe.get$
                }
            ''')
        exercise(code, w(newArrayList("getSubElement findMe")));
    }

    @Test
    def void testCompletionOnArrayMemberAccessInMethod1() {
        val code = CodeBuilder::classbody("CompletionOnArrayMemberAccessInMethod",
            '''
                public AtomicInteger findUs[] = { new AtomicInteger(1), new AtomicInteger(2) };
                public AtomicBoolean findUs1[][][] = new AtomicBoolean[1][1][1];
                
                public static void method1() {
                	final CompletionOnArrayMemberAccessInMethod obj = new CompletionOnArrayMemberAccessInMethod();
                	final AtomicInteger c = $
                }
            ''')
        exercise(code, w(newArrayList("obj findUs")));
    }

    @Test
    def void testCompletionOnArrayMemberAccessInMethod2() {
        val code = CodeBuilder::classbody("CompletionOnArrayMemberAccessInMethod",
            '''
                public AtomicInteger findUs[] = { new AtomicInteger(1), new AtomicInteger(2) };
                public AtomicBoolean findUs1[][][] = new AtomicBoolean[1][1][1];
                
                public static void method2() {
                	final CompletionOnArrayMemberAccessInMethod obj = new CompletionOnArrayMemberAccessInMethod();
                	final AtomicInteger[] c = $
                }
            ''')
        exercise(code, w(newArrayList("obj findUs")));
    }

    @Test
    def void testCompletionOnArrayMemberAccessInMethod3() {
        val code = CodeBuilder::classbody("CompletionOnArrayMemberAccessInMethod",
            '''
                public AtomicInteger findUs[] = { new AtomicInteger(1), new AtomicInteger(2) };
                public AtomicBoolean findUs1[][][] = new AtomicBoolean[1][1][1];
                
                public static void method3() {
                	final CompletionOnArrayMemberAccessInMethod obj = new CompletionOnArrayMemberAccessInMethod();
                	final AtomicBoolean[][][] c = $
                }
            ''')
        exercise(code, w(newArrayList("obj findUs1")));
    }

    @Test
    def void testCompletionOnArrayMemberAccessInMethod4() {
        val code = CodeBuilder::classbody("CompletionOnArrayMemberAccessInMethod",
            '''
                public AtomicInteger findUs[] = { new AtomicInteger(1), new AtomicInteger(2) };
                public AtomicBoolean findUs1[][][] = new AtomicBoolean[1][1][1];
                
                public static void method4() {
                	final CompletionOnArrayMemberAccessInMethod obj = new CompletionOnArrayMemberAccessInMethod();
                	final AtomicBoolean[][] c = $
                }
            ''')
        exercise(code, w(newArrayList("obj findUs1")));
    }

    @Test
    def void testCompletionOnArrayMemberAccessInMethod5() {
        val code = CodeBuilder::classbody("CompletionOnArrayMemberAccessInMethod",
            '''
                public AtomicInteger findUs[] = { new AtomicInteger(1), new AtomicInteger(2) };
                public AtomicBoolean findUs1[][][] = new AtomicBoolean[1][1][1];
                
                public static void method5() {
                	final CompletionOnArrayMemberAccessInMethod obj = new CompletionOnArrayMemberAccessInMethod();
                	final AtomicBoolean c[] = $
                }
            ''')
        exercise(code, w(newArrayList("obj findUs1")));
    }

    @Test
    def void testCompletionOnArrayMemberAccessInMethod6() {
        val code = CodeBuilder::classbody("CompletionOnArrayMemberAccessInMethod",
            '''
                public AtomicInteger findUs[] = { new AtomicInteger(1), new AtomicInteger(2) };
                public AtomicBoolean findUs1[][][] = new AtomicBoolean[1][1][1];
                
                public static void method6() {
                	final CompletionOnArrayMemberAccessInMethod obj = new CompletionOnArrayMemberAccessInMethod();
                	final AtomicBoolean c = $
                }
            ''')
        exercise(code, w(newArrayList("obj findUs1")));
    }

    @Test
    def void testCompletionOnArrayWithCastsSupertype1() {
        val code = CodeBuilder::classbody('''CompletionOnArrayWithCastsSupertype''',
            '''
                public Integer[][][] findme;
                public int i;
                
                public static void method1() {
                	final CompletionOnArrayWithCastsSupertype obj = new CompletionOnArrayWithCastsSupertype();
                	final Number c = $
                }
            ''')
        exercise(code, w(newArrayList("obj findme")));
    }

    @Test
    def void testCompletionOnArrayWithCastsSupertype2() {
        val code = CodeBuilder::classbody('''CompletionOnArrayWithCastsSupertype''',
            '''
                public Integer[][][] findme;
                public int i;
                
                public static void method2() {
                	final CompletionOnArrayWithCastsSupertype obj = new CompletionOnArrayWithCastsSupertype();
                	final Number[] c = $
                }
            ''')
        exercise(code, w(newArrayList("obj findme")));
    }

    @Test
    def void testCompletionOnArrayWithCastsSupertype3() {
        val code = CodeBuilder::classbody('''CompletionOnArrayWithCastsSupertype''',
            '''
                public Integer[][][] findme;
                public int i;
                
                public static void method3() {
                	final CompletionOnArrayWithCastsSupertype obj = new CompletionOnArrayWithCastsSupertype();
                	final Number[][] c = $
                }
            ''')
        exercise(code, w(newArrayList("obj findme")));
    }

    @Test
    def void testCompletionOnArrayWithCastsSupertype4() {
        val code = CodeBuilder::classbody('''CompletionOnArrayWithCastsSupertype''',
            '''
                public Integer[][][] findme;
                public int i;
                
                public static void method4() {
                	final CompletionOnArrayWithCastsSupertype obj = new CompletionOnArrayWithCastsSupertype();
                	final Number[][][] c = $
                }
            ''')
        exercise(code, w(newArrayList("obj findme")));
    }

    @Test
    def void testCompletionOnGenericTypeInMethod1() {
        val code = CodeBuilder::classbody("CompletionOnGenericTypeInMethod",
            '''
                public List<String> findMe = new ArrayList<String>();
                
                public static void test_exactGenericType() {
                	final CompletionOnGenericTypeInMethod variable = new CompletionOnGenericTypeInMethod();
                	final List<String> c = $
                }
            ''')
        exercise(code, w(newArrayList("variable findMe")));
    }

    @Test
    @Ignore("Bug! However, it already existed in old version.")
    def void testCompletionOnGenericTypeInMethod2() {
        val code = CodeBuilder::classbody("CompletionOnGenericTypeInMethod",
            '''
                public List<String> findMe = new ArrayList<String>();
                
                public static void test_exactButWrongGenericType() {
                	final CompletionOnGenericTypeInMethod variable = new CompletionOnGenericTypeInMethod();
                	final List<Integer> c = $
                }
            ''')
        exercise(code, w(newArrayList()));
    }

    @Test
    def void testCompletionOnGenericTypeInMethod3() {
        val code = CodeBuilder::classbody("CompletionOnGenericTypeInMethod",
            '''
                public List<String> findMe = new ArrayList<String>();
                
                public static void test_anonymousGenericType() {
                	final CompletionOnGenericTypeInMethod variable = new CompletionOnGenericTypeInMethod();
                	final List<?> c = $
                }
            ''')
        exercise(code, w(newArrayList("variable findMe")));
    }

    @Test
    def void testCompletionOnGenericTypeInMethod4() {
        val code = CodeBuilder::classbody("CompletionOnGenericTypeInMethod",
            '''
                public List<String> findMe = new ArrayList<String>();
                
                public static void test_genericSuperType() {
                	final CompletionOnGenericTypeInMethod variable = new CompletionOnGenericTypeInMethod();
                	final List<? extends Serializable> c = $
                }
            ''')
        exercise(code, w(newArrayList("variable findMe")));
    }

    @Test
    @Ignore("Bug! However, it already existed in old version.")
    def void testCompletionOnGenericTypeInMethod5() {
        val code = CodeBuilder::classbody("CompletionOnGenericTypeInMethod",
            '''
                public List<String> findMe = new ArrayList<String>();
                
                public static void test_wrongGenericSuperType() {
                	final CompletionOnGenericTypeInMethod variable = new CompletionOnGenericTypeInMethod();
                	final List<? extends File> c = $
                }
            ''')
        exercise(code, w(newArrayList()));
    }

    @Test
    def void testCompletionOnGenericTypeInMethod6() {
        val code = CodeBuilder::classbody("CompletionOnGenericTypeInMethod",
            '''
                public List<String> findMe = new ArrayList<String>();
                
                public static void test_genericSubType() {
                	final CompletionOnGenericTypeInMethod variable = new CompletionOnGenericTypeInMethod();
                	final List<? super String> c = $
                }
            ''')
        exercise(code, w(newArrayList("variable findMe")));
    }

    @Test
    @Ignore("Bug! However, it already existed in old version.")
    def void testCompletionOnGenericTypeInMethod7() {
        val code = CodeBuilder::classbody("CompletionOnGenericTypeInMethod",
            '''
                public List<String> findMe = new ArrayList<String>();
                
                public static void test_wrongGenericSubType() {
                	final CompletionOnGenericTypeInMethod variable = new CompletionOnGenericTypeInMethod();
                	final List<? super Serializable> c = $
                }
            ''')
        exercise(code, w(newArrayList()));
    }

    @Test
    def void testCompletionOnMemberCallChainDepth1() {
        val code = CodeBuilder::classbody(
            '''
                public class A {
                	public B b = new B();
                
                	public class B {
                	public File findMember = new File("");
                
                		public File findMethod() {
                	return null;
                	}
                	}
                }
                
                A a = new A();
                File c = $
            ''')
        exercise(code, w(newArrayList("a b findMethod", "a b findMember")));
    }

    @Test
    @Ignore("Bug! Chains using this.a are expected as well.")
    def void testCompletionOnMemberCallChainDepth2() {
        val code = CodeBuilder::classbody("CompletionOnMemberCallChainDepth2",
            '''
                public class A {
                	public B b = new B();
                
                	public class B {
                	public File findMember = new File("");
                
                		public File findMethod() {
                	return null;
                	}
                	}
                }
                
                A a = new A();
                
                public CompletionOnMemberCallChainDepth2(){
                	final A a = new A();
                	final File c = $
                }
            ''')
        exercise(code, w(newArrayList("a b findMethod", "a b findMember", "a b findMethod", "a b findMember")));
    }

    @Test
    @Ignore("Bug! Chains using this.a are expected as well.")
    def void testCompletionOnMemberCallChainDepth3() {
        val code = CodeBuilder::classbody(
            '''
                public class A {
                	public B b = new B();
                
                	public class B {
                	public File findMember = new File("");
                
                		public File findMethod() {
                	return null;
                	}
                	}
                }
                
                A a = new A();
                
                public void method(){
                	final A a = new A();
                	final File c = $
                }
            ''')
        exercise(code, w(newArrayList("a b findMethod", "a b findMember", "this a b findMethod", "this a b findMember")));
    }

    @Test
    def void testCompletionOnMemberInMethodWithPrefix() {
        val code = CodeBuilder::classbody("CompletionOnMemberInMethodWithPrefix",
            '''
                public AtomicBoolean findMe = new AtomicBoolean();
                
                public CompletionOnMemberInMethodWithPrefix getSubElement() {
                	return new CompletionOnMemberInMethodWithPrefix();
                }
                
                public static void method2() {
                	final CompletionOnMemberInMethodWithPrefix useMe = new CompletionOnMemberInMethodWithPrefix();
                	final AtomicBoolean c = useMe.get$
                }
            ''')
        exercise(code, w(newArrayList("getSubElement findMe")));
    }

    @Test
    def void testCompletionOnMethodReturn() {
        val code = CodeBuilder::classbody(
            '''
                public void method() {
                	final Iterator<AtomicLong> c = $
                }
                
                private List<AtomicLong> getList() {
                	return new LinkedList<AtomicLong>();
                }
            ''')
        var wanted =
            newArrayList(
                "getList iterator",
                "getList listIterator",
                "getList listIterator",
                "getList subList iterator",
                "getList subList listIterator",
                "getList subList listIterator"
            )
        if (javaVersionAtLeast("1.8")) {
            wanted.add("getList parallelStream iterator")
            wanted.add("getList stream iterator")
        }
        exercise(code, w(wanted));
    }

    @Test
    def void testCompletionOnNonPublicMemberInMethod1() {
        val code = CodeBuilder::classbody("CompletionOnNonPublicMemberInMethod",
            '''
                protected AtomicBoolean findMe1 = new AtomicBoolean();
                AtomicInteger findMe2 = new AtomicInteger();
                private final AtomicLong findMe3 = new AtomicLong();
                
                public static void test_protected() {
                	final CompletionOnNonPublicMemberInMethod useMe = new CompletionOnNonPublicMemberInMethod();
                	final AtomicBoolean c = $
                }
            ''')
        exercise(code, w(newArrayList("useMe findMe1")));
    }

    @Test
    @Ignore("Context does not seem to resolve primitives as target types")
    def void testCompletionOnPrimitiveTypeInMethod1() {
        val code = CodeBuilder::classbody(
            '''
                private class A {
                	public int findMe = 5;
                
                	public int[] findMe2() {
                	return new int[1];
                	}
                }
                
                final A useMe = new A();
                
                public void method() {
                	final A useMe = new A();
                	final int c = $
                }
            ''')
        exercise(code, w(newArrayList("useMe findMe", "useMe findMe2")));
    }

    @Test
    def void testCompletionOnSupertypeInMethod() {
        val code = CodeBuilder::classbody("CompletionOnSupertypeInMethod",
            '''
                public ByteArrayInputStream findMe = new ByteArrayInputStream(new byte[] { 0, 1, 2, 3 });
                
                public static void method() {
                	final CompletionOnSupertypeInMethod useMe = new CompletionOnSupertypeInMethod();
                	final InputStream c = $
                }
            ''')
        exercise(code, w(newArrayList("useMe findMe")));
    }

    @Test
    def void testCompletionOnSupertypeMemberInMethod1() {
        val code = CodeBuilder::classbody("CompletionOnSupertypeMemberInMethod",
            '''
                public static class Subtype extends CompletionOnSupertypeMemberInMethod {
                }
                
                public AtomicBoolean findMe = new AtomicBoolean();
                
                public static void test_onAttribute() {
                	final Subtype useMe = new Subtype();
                	final AtomicBoolean c = $
                }
            ''')
        exercise(code, w(newArrayList("useMe findMe")));
    }

    @Test
    def void testCompletionOnSupertypeMemberInMethod2() {
        val code = CodeBuilder::classbody("CompletionOnSupertypeMemberInMethod",
            '''
                public static class Subtype extends CompletionOnSupertypeMemberInMethod {
                }
                
                public AtomicInteger findMe() {
                	return new AtomicInteger();
                }
                
                public static void test_onMethod() {
                	final Subtype useMe = new Subtype();
                	final AtomicInteger c = $
                }
            ''')
        exercise(code, w(newArrayList("useMe findMe")));
    }

    @Test
    def void testCompletionOnThisAndLocal() {
        val code = CodeBuilder::method(
            '''
                final Map map = new HashMap();
                final Collection c = $
            ''')
        exercise(code, w(newArrayList("map entrySet", "map keySet", "map values")));
    }

    @Test
    def void testCompletionOnType() {
        val code = CodeBuilder::classbody(
            '''
                public static class S {
                
                	private static S INSTANCE = new S();
                
                	public Integer findMe() {
                	return 0;
                	}
                
                	public static S getInstance() {
                	return INSTANCE;
                	}
                }
                
                public void __test() {
                	Integer i = S.$
                }
            ''')
        exercise(code, w(newArrayList("getInstance findMe")));
    }

    @Test
    def void testCompletionViaGenericTypeInMethod() {
        val code = CodeBuilder::method("CompletionViaGenericTypeInMethod",
            '''
                final Iterator<CompletionViaGenericTypeInMethod> useMe = Arrays.asList(
                	new CompletionViaGenericTypeInMethod()).iterator();
                final CompletionViaGenericTypeInMethod c = $
            ''')
        exercise(code, w(newArrayList("useMe next")));
    }

    @Test
    def void testCompletionViaLocalVariableInMethod() {
        val code = CodeBuilder::classbody("CompletionViaLocalVariableInMethod",
            '''
                public AtomicBoolean findMe = new AtomicBoolean();
                
                public static void method() {
                	final CompletionViaLocalVariableInMethod variable = new CompletionViaLocalVariableInMethod();
                	final AtomicBoolean c = $
                }
            ''')
        exercise(code, w(newArrayList("variable findMe")));
    }

    @Test
    def void testCompletionViaStaticArrayInMethod() {
        val code = CodeBuilder::classbody("CompletionViaStaticArrayInMethod",
            '''
                public AtomicBoolean findMe = new AtomicBoolean();
                
                public static CompletionViaStaticArrayInMethod useUs[] = { new CompletionViaStaticArrayInMethod(),
                	new CompletionViaStaticArrayInMethod() };
                
                public static void method1() {
                	final AtomicBoolean c = $
                }
            ''')
        exercise(code, w(newArrayList("useUs findMe")));
    }

    @Test
    def void testExpectArray() {
        val code = CodeBuilder::method(
            '''
                BigInteger bigInt = null;
                final BigInteger[] array = $
            ''')
        var wanted = newArrayList(
                "bigInt divideAndRemainder",
                "bigInt abs divideAndRemainder",
                "bigInt add divideAndRemainder",
                "bigInt and divideAndRemainder",
                "bigInt andNot divideAndRemainder",
                "bigInt clearBit divideAndRemainder",
                "bigInt divide divideAndRemainder",
                "bigInt flipBit divideAndRemainder",
                "bigInt gcd divideAndRemainder",
                "bigInt max divideAndRemainder",
                "bigInt min divideAndRemainder",
                "bigInt mod divideAndRemainder",
                "bigInt modInverse divideAndRemainder",
                "bigInt modPow divideAndRemainder",
                "bigInt multiply divideAndRemainder",
                "bigInt negate divideAndRemainder",
                "bigInt nextProbablePrime divideAndRemainder",
                "bigInt not divideAndRemainder",
                "bigInt or divideAndRemainder",
                "bigInt pow divideAndRemainder",
                "bigInt remainder divideAndRemainder",
                "bigInt setBit divideAndRemainder",
                "bigInt shiftLeft divideAndRemainder",
                "bigInt shiftRight divideAndRemainder",
                "bigInt subtract divideAndRemainder",
                "bigInt xor divideAndRemainder"
            )
        if (javaVersionAtLeast("9")) {
            wanted.add("bigInt sqrt divideAndRemainder")

            wanted.add("bigInt sqrtAndRemainder")
            wanted.add("bigInt abs sqrtAndRemainder")
            wanted.add("bigInt add sqrtAndRemainder")
            wanted.add("bigInt and sqrtAndRemainder")
            wanted.add("bigInt andNot sqrtAndRemainder")
            wanted.add("bigInt clearBit sqrtAndRemainder")
            wanted.add("bigInt divide sqrtAndRemainder")
            wanted.add("bigInt flipBit sqrtAndRemainder")
            wanted.add("bigInt gcd sqrtAndRemainder")
            wanted.add("bigInt max sqrtAndRemainder")
            wanted.add("bigInt min sqrtAndRemainder")
            wanted.add("bigInt mod sqrtAndRemainder")
            wanted.add("bigInt modInverse sqrtAndRemainder")
            wanted.add("bigInt modPow sqrtAndRemainder")
            wanted.add("bigInt multiply sqrtAndRemainder")
            wanted.add("bigInt negate sqrtAndRemainder")
            wanted.add("bigInt nextProbablePrime sqrtAndRemainder")
            wanted.add("bigInt not sqrtAndRemainder")
            wanted.add("bigInt or sqrtAndRemainder")
            wanted.add("bigInt pow sqrtAndRemainder")
            wanted.add("bigInt remainder sqrtAndRemainder")
            wanted.add("bigInt setBit sqrtAndRemainder")
            wanted.add("bigInt shiftLeft sqrtAndRemainder")
            wanted.add("bigInt shiftRight sqrtAndRemainder")
            wanted.add("bigInt sqrt sqrtAndRemainder")
            wanted.add("bigInt subtract sqrtAndRemainder")
            wanted.add("bigInt xor sqrtAndRemainder")
        }
        var expected = w(wanted)
        exercise(code, expected);
    }

    @Test
    def void testLocalWithPrefix() {
        val code = CodeBuilder::classbody("LocalWithPrefix",
            '''
                public AtomicBoolean findMe = new AtomicBoolean();
                
                public static void method1() {
                	final LocalWithPrefix useMe = new LocalWithPrefix();
                	final AtomicBoolean c = use$
                }
            ''')
        exercise(code, w(newArrayList("useMe findMe")));
    }

    @Test
    def void testFindLocalAnchor() {
        val code = CodeBuilder::method(
            '''
                ExecutorService pool = Executors.newCachedThreadPool();
                Future future = $
            ''')
        var expected = w(
            newArrayList(
                "pool submit", // different args
                "pool submit", // different args
                "pool submit", // different args
                "pool invokeAll get",
                "pool invokeAll remove",
                "pool invokeAll set",
                "pool invokeAll get",
                "pool invokeAll remove",
                "pool invokeAll set"
            ))
        exercise(code, expected);
    }

    @Test
    def void testFindLocalAnchorWithIsExactMatch() {

        // well, not really an exact match...!
        val code = '''
        import java.util.*;
        class MyClass {
        	void m(){
        		List<Object> findMe;
        		List<String> l = $
        	}
        }'''

        // need to def expectations
        exercise(code, w(newArrayList("findMe")));
    }

    @Test
    def void testFindFieldAnchor() {
        val code = '''
            import java.util.concurrent.*;
            public class MyClass {
            	ExecutorService pool;
            	void test() {
            		Future future = $
            	}
            }
        '''
        var expected = w(
            newArrayList(
                "pool submit", // different args
                "pool submit", // different args
                "pool submit", // different args
                "pool invokeAll get",
                "pool invokeAll remove",
                "pool invokeAll set",
                "pool invokeAll get",
                "pool invokeAll remove",
                "pool invokeAll set"
            ))
        exercise(code, expected);
    }

    @Test
    def void testFindArrayFieldAnchor() {
        val code = '''
            import java.util.concurrent.*;
            public class MyClass {
            	ExecutorService pool[];
            	void test() {
            		Future future = $
            	}
            }
        '''
        var expected = w(
            newArrayList(
                "pool submit", // different args
                "pool submit", // different args
                "pool submit", // different args
                "pool invokeAll get",
                "pool invokeAll remove",
                "pool invokeAll set",
                "pool invokeAll get",
                "pool invokeAll remove",
                "pool invokeAll set"
            ))
        exercise(code, expected);
    }

    @Test
    def void testFindMultiDimArrayField() {
        val code = '''
            import java.util.concurrent.*;
            public class MyClass {
            	ExecutorService pool[][][];
            	void test() {
            		Future future = $
            	} 
            }
        '''
        var expected = w(
            newArrayList(
                "pool submit", // different args
                "pool submit", // different args
                "pool submit", // different args
                "pool invokeAll get",
                "pool invokeAll remove",
                "pool invokeAll set",
                "pool invokeAll get",
                "pool invokeAll remove",
                "pool invokeAll set"
            ))
        exercise(code, expected);
    }

    @Test
    def void testFindFieldInSuperType() {
        val code = '''
        import java.util.*;
        import java.awt.*;
        class MyClass extends Event{
        	void m(){
        		Event e = $
        	}
        }'''

        // need to def expectations
        exercise(code, w(newArrayList("evt")));
    }

    @Test
    def void testCompletionOnRuntime() {
        val code = '''
        import java.io.*;
        class MyClass {
        	void m(){
        		InputStream in = Runtime.$
        	}
        }'''

        // need to def expectations
        var expected = w(
            newArrayList(
                "getRuntime getLocalizedInputStream",
                "getRuntime exec getInputStream",
                "getRuntime exec getErrorStream",
                "getRuntime exec getInputStream",
                "getRuntime exec getErrorStream",
                "getRuntime exec getInputStream",
                "getRuntime exec getErrorStream",
                "getRuntime exec getInputStream",
                "getRuntime exec getErrorStream",
                "getRuntime exec getInputStream",
                "getRuntime exec getErrorStream",
                "getRuntime exec getInputStream",
                "getRuntime exec getErrorStream"
            ))
        exercise(code, expected);
    }

    @Test
    def void testCompletionOnLocaVariable() {
        val code = '''
        import java.util.*;
        class MyClass {
        	void m(){
        		List<Object> findMe;
        		List<String> l = findMe.$
        	}
        }'''

        // need to def expectations
        exercise(code, w(newArrayList("subList")));
    }

    @Test
    def void testCompletionOnStaticType() {
        val code = CodeBuilder::method('''Iterator<String> l = Collections.$''')

        val wanted = newArrayList(
            "EMPTY_LIST iterator",
            "EMPTY_LIST listIterator",
            "EMPTY_LIST listIterator",
            "EMPTY_LIST subList iterator",
            "EMPTY_LIST subList listIterator",
            "EMPTY_LIST subList listIterator",
            "EMPTY_MAP entrySet iterator",
            "EMPTY_MAP keySet iterator",
            "EMPTY_MAP values iterator",
            "EMPTY_SET iterator",
            "asLifoQueue iterator",
            "checkedCollection iterator",
            "checkedList iterator",
            "checkedList listIterator",
            "checkedList listIterator",
            "checkedList subList iterator",
            "checkedList subList listIterator",
            "checkedList subList listIterator",
            "checkedMap entrySet iterator",
            "checkedMap keySet iterator",
            "checkedMap values iterator",
            "checkedSet iterator",
            "checkedSortedMap entrySet iterator",
            "checkedSortedMap keySet iterator",
            "checkedSortedMap values iterator",
            "checkedSortedSet headSet iterator",
            "checkedSortedSet iterator",
            "checkedSortedSet subSet iterator",
            "checkedSortedSet tailSet iterator",
            "emptyIterator",
            "emptyList iterator",
            "emptyList listIterator",
            "emptyList listIterator",
            "emptyList subList iterator",
            "emptyList subList listIterator",
            "emptyList subList listIterator",
            "emptyListIterator",
            "emptyMap entrySet iterator",
            "emptyMap keySet iterator",
            "emptyMap values iterator",
            "emptySet iterator",
            "list iterator",
            "list listIterator",
            "list listIterator",
            "list subList iterator",
            "list subList listIterator",
            "list subList listIterator",
            "nCopies iterator",
            "nCopies listIterator",
            "nCopies listIterator",
            "nCopies subList iterator",
            "nCopies subList listIterator",
            "nCopies subList listIterator",
            "newSetFromMap iterator",
            "singleton iterator",
            "singletonList iterator",
            "singletonList listIterator",
            "singletonList listIterator",
            "singletonList subList iterator",
            "singletonList subList listIterator",
            "singletonList subList listIterator",
            "singletonMap entrySet iterator",
            "singletonMap keySet iterator",
            "singletonMap values iterator",
            "synchronizedCollection iterator",
            "synchronizedList iterator",
            "synchronizedList listIterator",
            "synchronizedList listIterator",
            "synchronizedList subList iterator",
            "synchronizedList subList listIterator",
            "synchronizedList subList listIterator",
            "synchronizedMap entrySet iterator",
            "synchronizedMap keySet iterator",
            "synchronizedMap values iterator",
            "synchronizedSet iterator",
            "synchronizedSortedMap entrySet iterator",
            "synchronizedSortedMap keySet iterator",
            "synchronizedSortedMap values iterator",
            "synchronizedSortedSet headSet iterator",
            "synchronizedSortedSet iterator",
            "synchronizedSortedSet subSet iterator",
            "synchronizedSortedSet tailSet iterator",
            "unmodifiableCollection iterator",
            "unmodifiableList iterator",
            "unmodifiableList listIterator",
            "unmodifiableList listIterator",
            "unmodifiableList subList iterator",
            "unmodifiableList subList listIterator",
            "unmodifiableList subList listIterator",
            "unmodifiableMap entrySet iterator",
            "unmodifiableMap keySet iterator",
            "unmodifiableMap values iterator",
            "unmodifiableSet iterator",
            "unmodifiableSortedMap entrySet iterator",
            "unmodifiableSortedMap keySet iterator",
            "unmodifiableSortedMap values iterator",
            "unmodifiableSortedSet headSet iterator",
            "unmodifiableSortedSet iterator",
            "unmodifiableSortedSet subSet iterator",
            "unmodifiableSortedSet tailSet iterator"
        )
        if (javaVersionAtLeast("1.8")) {
            wanted.add("EMPTY_LIST parallelStream iterator")
            wanted.add("EMPTY_LIST stream iterator")
            wanted.add("EMPTY_SET parallelStream iterator")
            wanted.add("EMPTY_SET stream iterator")
            wanted.add("asLifoQueue parallelStream iterator")
            wanted.add("asLifoQueue stream iterator")
            wanted.add("checkedCollection parallelStream iterator")
            wanted.add("checkedCollection stream iterator")
            wanted.add("checkedList parallelStream iterator")
            wanted.add("checkedList stream iterator")
            wanted.add("checkedNavigableMap descendingKeySet descendingIterator")
            wanted.add("checkedNavigableMap descendingKeySet iterator")
            wanted.add("checkedNavigableMap entrySet iterator")
            wanted.add("checkedNavigableMap keySet iterator")
            wanted.add("checkedNavigableMap navigableKeySet descendingIterator")
            wanted.add("checkedNavigableMap navigableKeySet iterator")
            wanted.add("checkedNavigableMap values iterator")
            wanted.add("checkedNavigableSet descendingIterator")
            wanted.add("checkedNavigableSet descendingSet descendingIterator")
            wanted.add("checkedNavigableSet descendingSet iterator")
            wanted.add("checkedNavigableSet headSet descendingIterator")
            wanted.add("checkedNavigableSet headSet iterator")
            wanted.add("checkedNavigableSet headSet iterator")
            wanted.add("checkedNavigableSet iterator")
            wanted.add("checkedNavigableSet parallelStream iterator")
            wanted.add("checkedNavigableSet stream iterator")
            wanted.add("checkedNavigableSet subSet descendingIterator")
            wanted.add("checkedNavigableSet subSet iterator")
            wanted.add("checkedNavigableSet subSet iterator")
            wanted.add("checkedNavigableSet tailSet descendingIterator")
            wanted.add("checkedNavigableSet tailSet iterator")
            wanted.add("checkedNavigableSet tailSet iterator")
            wanted.add("checkedQueue iterator")
            wanted.add("checkedQueue parallelStream iterator")
            wanted.add("checkedQueue stream iterator")
            wanted.add("checkedSet parallelStream iterator")
            wanted.add("checkedSet stream iterator")
            wanted.add("checkedSortedSet parallelStream iterator")
            wanted.add("checkedSortedSet stream iterator")
            wanted.add("emptyList parallelStream iterator")
            wanted.add("emptyList stream iterator")
            wanted.add("emptyNavigableMap descendingKeySet descendingIterator")
            wanted.add("emptyNavigableMap descendingKeySet iterator")
            wanted.add("emptyNavigableMap entrySet iterator")
            wanted.add("emptyNavigableMap keySet iterator")
            wanted.add("emptyNavigableMap navigableKeySet descendingIterator")
            wanted.add("emptyNavigableMap navigableKeySet iterator")
            wanted.add("emptyNavigableMap values iterator")
            wanted.add("emptyNavigableSet descendingIterator")
            wanted.add("emptyNavigableSet descendingSet descendingIterator")
            wanted.add("emptyNavigableSet descendingSet iterator")
            wanted.add("emptyNavigableSet headSet descendingIterator")
            wanted.add("emptyNavigableSet headSet iterator")
            wanted.add("emptyNavigableSet headSet iterator")
            wanted.add("emptyNavigableSet iterator")
            wanted.add("emptyNavigableSet parallelStream iterator")
            wanted.add("emptyNavigableSet stream iterator")
            wanted.add("emptyNavigableSet subSet descendingIterator")
            wanted.add("emptyNavigableSet subSet iterator")
            wanted.add("emptyNavigableSet subSet iterator")
            wanted.add("emptyNavigableSet tailSet descendingIterator")
            wanted.add("emptyNavigableSet tailSet iterator")
            wanted.add("emptyNavigableSet tailSet iterator")
            wanted.add("emptySet parallelStream iterator")
            wanted.add("emptySet stream iterator")
            wanted.add("emptySortedMap entrySet iterator")
            wanted.add("emptySortedMap keySet iterator")
            wanted.add("emptySortedMap values iterator")
            wanted.add("emptySortedSet headSet iterator")
            wanted.add("emptySortedSet iterator")
            wanted.add("emptySortedSet parallelStream iterator")
            wanted.add("emptySortedSet stream iterator")
            wanted.add("emptySortedSet subSet iterator")
            wanted.add("emptySortedSet tailSet iterator")
            wanted.add("list parallelStream iterator")
            wanted.add("list stream iterator")
            wanted.add("nCopies parallelStream iterator")
            wanted.add("nCopies stream iterator")
            wanted.add("newSetFromMap parallelStream iterator")
            wanted.add("newSetFromMap stream iterator")
            wanted.add("singleton parallelStream iterator")
            wanted.add("singleton stream iterator")
            wanted.add("singletonList parallelStream iterator")
            wanted.add("singletonList stream iterator")
            wanted.add("synchronizedCollection parallelStream iterator")
            wanted.add("synchronizedCollection stream iterator")
            wanted.add("synchronizedList parallelStream iterator")
            wanted.add("synchronizedList stream iterator")
            wanted.add("synchronizedNavigableMap descendingKeySet descendingIterator")
            wanted.add("synchronizedNavigableMap descendingKeySet iterator")
            wanted.add("synchronizedNavigableMap entrySet iterator")
            wanted.add("synchronizedNavigableMap keySet iterator")
            wanted.add("synchronizedNavigableMap navigableKeySet descendingIterator")
            wanted.add("synchronizedNavigableMap navigableKeySet iterator")
            wanted.add("synchronizedNavigableMap values iterator")
            wanted.add("synchronizedNavigableSet descendingIterator")
            wanted.add("synchronizedNavigableSet descendingSet descendingIterator")
            wanted.add("synchronizedNavigableSet descendingSet iterator")
            wanted.add("synchronizedNavigableSet headSet descendingIterator")
            wanted.add("synchronizedNavigableSet headSet iterator")
            wanted.add("synchronizedNavigableSet headSet iterator")
            wanted.add("synchronizedNavigableSet iterator")
            wanted.add("synchronizedNavigableSet parallelStream iterator")
            wanted.add("synchronizedNavigableSet stream iterator")
            wanted.add("synchronizedNavigableSet subSet descendingIterator")
            wanted.add("synchronizedNavigableSet subSet iterator")
            wanted.add("synchronizedNavigableSet subSet iterator")
            wanted.add("synchronizedNavigableSet tailSet descendingIterator")
            wanted.add("synchronizedNavigableSet tailSet iterator")
            wanted.add("synchronizedNavigableSet tailSet iterator")
            wanted.add("synchronizedSet parallelStream iterator")
            wanted.add("synchronizedSet stream iterator")
            wanted.add("synchronizedSortedSet parallelStream iterator")
            wanted.add("synchronizedSortedSet stream iterator")
            wanted.add("unmodifiableCollection parallelStream iterator")
            wanted.add("unmodifiableCollection stream iterator")
            wanted.add("unmodifiableList parallelStream iterator")
            wanted.add("unmodifiableList stream iterator")
            wanted.add("unmodifiableNavigableMap descendingKeySet descendingIterator")
            wanted.add("unmodifiableNavigableMap descendingKeySet iterator")
            wanted.add("unmodifiableNavigableMap entrySet iterator")
            wanted.add("unmodifiableNavigableMap keySet iterator")
            wanted.add("unmodifiableNavigableMap navigableKeySet descendingIterator")
            wanted.add("unmodifiableNavigableMap navigableKeySet iterator")
            wanted.add("unmodifiableNavigableMap values iterator")
            wanted.add("unmodifiableNavigableSet descendingIterator")
            wanted.add("unmodifiableNavigableSet descendingSet descendingIterator")
            wanted.add("unmodifiableNavigableSet descendingSet iterator")
            wanted.add("unmodifiableNavigableSet headSet descendingIterator")
            wanted.add("unmodifiableNavigableSet headSet iterator")
            wanted.add("unmodifiableNavigableSet headSet iterator")
            wanted.add("unmodifiableNavigableSet iterator")
            wanted.add("unmodifiableNavigableSet parallelStream iterator")
            wanted.add("unmodifiableNavigableSet stream iterator")
            wanted.add("unmodifiableNavigableSet subSet descendingIterator")
            wanted.add("unmodifiableNavigableSet subSet iterator")
            wanted.add("unmodifiableNavigableSet subSet iterator")
            wanted.add("unmodifiableNavigableSet tailSet descendingIterator")
            wanted.add("unmodifiableNavigableSet tailSet iterator")
            wanted.add("unmodifiableNavigableSet tailSet iterator")
            wanted.add("unmodifiableSet parallelStream iterator")
            wanted.add("unmodifiableSet stream iterator")
            wanted.add("unmodifiableSortedSet parallelStream iterator")
            wanted.add("unmodifiableSortedSet stream iterator")
        }
        if (javaVersionAtLeast("9")) {
            wanted.add("emptyEnumeration asIterator")
            wanted.add("enumeration asIterator")
        }

        // need to def expectations
        var expected = w(wanted)
        exercise(code, expected);
    }

    @Test
    def void testCompletionOnReturnStatement() {
        val code = '''
        import java.util.*;
        class MyClass {
        	Iterator<String> m(){
        		List<String> l;
        		return $
        	}
        }'''

        // need to def expectations
        var wanted =
            newArrayList(
                "m",
                "l iterator",
                "l listIterator",
                "l listIterator",
                "l subList iterator",
                "l subList listIterator",
                "l subList listIterator"
            )
        if (javaVersionAtLeast("1.8")) {
            wanted.add("l stream iterator")
            wanted.add("l parallelStream iterator")
        }
        exercise(code, w(wanted));
    }

    @Test
    def void testCompletionOnEnumDoesNotThrowNPE() {
        val code = '''
        import java.lang.annotation.*;
        import java.util.*;
        class MyClass {
        	void m(){
        		String s = Annotation.$
        	}
        }'''

        // don't expect anything.
        var expected = w(newArrayList())
        exercise(code, expected);
    }

    /**
	 * we had some trouble with supertype hierarchy. This test that we do not generate 
	 * any chains that return a supertype of the requested type (ExecutorService in this case)
	 */
    @Test
    def void testFindSelfAssignment() {
        val code = '''
            import java.util.concurrent.*;
            public class MyClass {
            	ThreadPoolExecutor pool;
            	void test() {
            		MyClass clazz = new MyClass();
            		pool = $
            	}
            }
        '''
        exercise(code, w(newArrayList("pool", "clazz pool")));
    }

    // TODO we should qualify the proposed field with "this." 
    @Test
    def void testFindMatchingSubtypeForAssignment() {
        val code = '''
            import java.util.concurrent.*;
            public class MyClass {
            	ThreadPoolExecutor pool;
            	void test() {
            		ExecutorService pool = $
            	}
            }
        '''
        exercise(code, w(newArrayList("pool")));
    }

    @Test
    def void testCompletionOnFieldField() {
        val code = '''
            import java.awt.*;
            public class MyClass {
            	Event e;
            	void test() {
            		Event evt = e.evt.$
            	}
            }
        '''
        exercise(code, w(newArrayList("evt")));
    }

    @Test
    def void testPrefixFilter() {
        val code = '''
            import java.awt.*;
            public class MyClass {
            	Event evt;
            	Event aevt;
            	void test() {
            		Event evt = a$
            	}
            }
        '''
        exercise(code, w(newArrayList("aevt")));
    }

    @Test
    def void test012() {
        compile(FIELDS)
        val code = CodeBuilder::classbody(
            '''
                public static Fields f = new Fields();
                public static void test_protected() {
                final Boolean c = $
            ''')
        exercise(code, w(newArrayList("f _public", "f _protected")));
    }

    @Test
    def void testParameters1() {
        val code = CodeBuilder::classbody(
            '''
                ThreadPoolExecutor pool;
                public void test() {
                	pool.getKeepAliveTime($);
                }
                private TimeUnit getTimeUnit() {
                	return TimeUnit.MICROSECONDS;
                }
            ''')
        exercise(code, w(newArrayList("getTimeUnit")));
    }

    @Test
    def void testParameters2() {
        val code = CodeBuilder::classbody(
            '''
                ThreadPoolExecutor pool;
                public void test() {
                	pool.getKeepAliveTime(g$);
                }
                private TimeUnit getTimeUnit() {
                	return TimeUnit.MICROSECONDS;
                }
            ''')
        exercise(code, w(newArrayList("getTimeUnit")));
    }

    @Test
    def void testParameters3() {
        val code = CodeBuilder::classbody(
            '''
                public void test() {
                	File otherFile = null;
                	URI uri = null;
                	File file = new File($);
                }
            ''')
        exercise(code, w(newArrayList("otherFile", "uri")));
    }

    @Test
    @Ignore("This should only propose otherFile, since with the 2nd String param, only a File is a valid 1st param.")
    def void testParameters4() {
        val code = CodeBuilder::classbody(
            '''
                public void test() {
                	File otherFile = null;
                	URI uri = null;
                	File file = new File($,"");
                }
            ''')
        exercise(code, w(newArrayList("otherFile")));
    }

    @Test
    def void testParameters5() {
        val code = CodeBuilder::classbody(
            '''
                public void test() {
                	Charset cs = null;
                	CharsetDecoder dec = null;
                	InputStream in = null;
                	InputStreamReader reader = new InputStreamReader($,dec);
                }
            ''')
        exercise(code, w(newArrayList("in")));
    }

    @Test
    def void testParameters6() {
        val code = CodeBuilder::classbody(
            '''
                public void test() {
                	Charset cs = null;
                	CharsetDecoder dec = null;
                	InputStream in = null;
                	InputStreamReader reader = new InputStreamReader(null,$);
                }
            ''')
        exercise(code, w(newArrayList("dec", "cs")));
    }

    def exercise(CharSequence code, List<? extends List<String>> expected) {
        val struct = fixture.createFileAndParseWithMarkers(code.toString)
        val cu = struct.first;
        val completionIndex = struct.second.head
        val ctx = new JavaContentAssistContextMock(cu, completionIndex)

        val sut = new TestingChainCompletionProposalComputer(new CachingAstProvider, ChainPreferenceStoreMock::create())
        sut.sessionStarted

        for (i : 0 .. 0) {
            sut.computeCompletionProposals(ctx, null)
        }

        val proposals = sut.computeCompletionProposals(ctx, null)
        for (proposal : proposals) {
            val names = (proposal as ChainCompletionProposal).getChainElementNames
            assertTrue('''couldn't find «names» in expected.'''.toString, expected.remove(names))
        }
        assertTrue(''' some expected values were not found «expected» in «proposals» '''.toString, expected.empty)
    }

    def compile(CharSequence code) {
        fixture.createFileAndParseWithMarkers(code)
    }

    def l(String spaceSeparatedElementNames) {
        val elementNames = StringUtils::split(spaceSeparatedElementNames);
        return newArrayList(elementNames) as List<String>
    }

    def w(String[] chains) {
        val List<List<String>> res = newArrayList();
        for (chain : chains) {
            res.add(l(chain))
        }
        return res
    }

    CharSequence FIELDS = '''
        public class Fields {
        	public static 			Boolean _spublic;
        	protected static 		Boolean _sprotected;
        	/* default */ static 	Boolean _sdefault;
        	private static 			Boolean _sprivate;
        
        	public 			Boolean _public;
        	protected 		Boolean _protected;
        	/* default */	Boolean _default;
        	private 		Boolean _private;
        }
    '''
}

class TestingChainCompletionProposalComputer extends ChainCompletionProposalComputer {

    new(IAstProvider astProvider, IPreferenceStore preferenceStore) {
        super(astProvider, preferenceStore);
    }

    override shouldMakeProposals() {
        return true;
    }
}

public class ChainPreferenceStoreMock {

    static def IPreferenceStore create() {
        val store = Mockito.mock(IPreferenceStore);
        Mockito.when(store.getInt(ChainsPreferencePage.PREF_MAX_CHAINS)).thenReturn(Integer::MAX_VALUE);
        Mockito.when(store.getInt(ChainsPreferencePage.PREF_MAX_CHAIN_LENGTH)).thenReturn(3);
        Mockito.when(store.getInt(ChainsPreferencePage.PREF_TIMEOUT)).thenReturn(3);
        Mockito.when(store.getInt(ChainsPreferencePage.PREF_MIN_CHAIN_LENGTH)).thenReturn(1);
        Mockito.when(store.getString(ChainsPreferencePage.PREF_IGNORED_TYPES)).thenReturn(
            "java.lang.Object" + ChainsPreferencePage.IGNORE_TYPES_SEPARATOR + "java.lang.Class" +
                ChainsPreferencePage.IGNORE_TYPES_SEPARATOR + "java.lang.String");
        return store;
    }

}
