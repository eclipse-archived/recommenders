package org.eclipse.recommenders.tests.completion.rcp.chain

import java.util.List
import org.apache.commons.lang3.StringUtils
import org.eclipse.core.resources.ResourcesPlugin
import org.eclipse.recommenders.internal.completion.rcp.chain.ChainCompletionProposal
import org.eclipse.recommenders.internal.completion.rcp.chain.ChainCompletionProposalComputer
import org.eclipse.recommenders.tests.CodeBuilder
import org.eclipse.recommenders.tests.completion.rcp.JavaContentAssistContextMock
import org.eclipse.recommenders.tests.completion.rcp.RecommendersCompletionContextFactoryMock
import org.eclipse.recommenders.tests.jdt.JavaProjectFixture
import org.junit.Ignore
import org.junit.Test

import static junit.framework.Assert.*
import static org.eclipse.recommenders.tests.SmokeTestScenarios.*
import static org.eclipse.recommenders.tests.completion.rcp.chain.ChainCompletionScenariosTest.*
import org.junit.Before
 
class ChainCompletionScenariosTest { 
  
	static JavaProjectFixture fixture = new JavaProjectFixture(ResourcesPlugin::getWorkspace(),"test")
	
	@Before
	def void before(){
		fixture.clear
	}
	
 	@Test
	def void smokeTestScenarios(){
		for(scenario : scenarios){
			val struct = fixture.createFileAndParseWithMarkers(scenario)
			val cu = struct.first;
	
			for(completionIndex : struct.second){
				val ctx = new org.eclipse.recommenders.tests.completion.rcp.JavaContentAssistContextMock(cu, completionIndex)
				val sut = new org.eclipse.recommenders.internal.completion.rcp.chain.ChainCompletionProposalComputer(new RecommendersCompletionContextFactoryMock())
				sut.sessionStarted
				sut.computeCompletionProposals(ctx, null)
			}
		}
	}
	
	@Test
	@Ignore("TODO: Doesn't seem to work for some target platforms")
	def void testAccessMethodParameter(){
		val code = CodeBuilder::classbody('''
			public void method(final List list){
				Iterator it = $
			}
		''')
		exercise(code, w(newArrayList("list iterator")));
	}
	
	@Test
	def void testAvoidRecursiveCallsToMember(){
		val code = CodeBuilder::classbody('''AvoidRecursiveCallsToMember''', '''
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
	def void testCompletionOnArrayMemberAccessInMethod1(){
		val code = CodeBuilder::classbody("CompletionOnArrayMemberAccessInMethod", '''
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
	def void testCompletionOnArrayMemberAccessInMethod2(){
		val code = CodeBuilder::classbody("CompletionOnArrayMemberAccessInMethod", '''
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
	def void testCompletionOnArrayMemberAccessInMethod3(){
		val code = CodeBuilder::classbody("CompletionOnArrayMemberAccessInMethod", '''
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
	def void testCompletionOnArrayMemberAccessInMethod4(){
		val code = CodeBuilder::classbody("CompletionOnArrayMemberAccessInMethod", '''
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
	def void testCompletionOnArrayMemberAccessInMethod5(){
		val code = CodeBuilder::classbody("CompletionOnArrayMemberAccessInMethod", '''
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
	def void testCompletionOnArrayMemberAccessInMethod6(){
		val code = CodeBuilder::classbody("CompletionOnArrayMemberAccessInMethod", '''
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
	def void testCompletionOnArrayWithCastsSupertype1(){
		val code = CodeBuilder::classbody('''CompletionOnArrayWithCastsSupertype''', '''
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
	def void testCompletionOnArrayWithCastsSupertype2(){
		val code = CodeBuilder::classbody('''CompletionOnArrayWithCastsSupertype''', '''
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
	def void testCompletionOnArrayWithCastsSupertype3(){
		val code = CodeBuilder::classbody('''CompletionOnArrayWithCastsSupertype''', '''
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
	def void testCompletionOnArrayWithCastsSupertype4(){
		val code = CodeBuilder::classbody('''CompletionOnArrayWithCastsSupertype''', '''
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
	def void testCompletionOnGenericTypeInMethod1(){
		val code = CodeBuilder::classbody("CompletionOnGenericTypeInMethod", '''
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
	def void testCompletionOnGenericTypeInMethod2(){
		val code = CodeBuilder::classbody("CompletionOnGenericTypeInMethod", '''
			public List<String> findMe = new ArrayList<String>();

			public static void test_exactButWrongGenericType() {
				final CompletionOnGenericTypeInMethod variable = new CompletionOnGenericTypeInMethod();
				final List<Integer> c = $
			}
		''')
		exercise(code, w(newArrayList()));
	}
	
	@Test 
	def void testCompletionOnGenericTypeInMethod3(){
		val code = CodeBuilder::classbody("CompletionOnGenericTypeInMethod", '''
			public List<String> findMe = new ArrayList<String>();

			public static void test_anonymousGenericType() {
				final CompletionOnGenericTypeInMethod variable = new CompletionOnGenericTypeInMethod();
				final List<?> c = $
			}
		''')
		exercise(code, w(newArrayList("variable findMe")));
	}
	
	@Test 
	def void testCompletionOnGenericTypeInMethod4(){
		val code = CodeBuilder::classbody("CompletionOnGenericTypeInMethod", '''
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
	def void testCompletionOnGenericTypeInMethod5(){
		val code = CodeBuilder::classbody("CompletionOnGenericTypeInMethod", '''
			public List<String> findMe = new ArrayList<String>();

			public static void test_wrongGenericSuperType() {
				final CompletionOnGenericTypeInMethod variable = new CompletionOnGenericTypeInMethod();
				final List<? extends File> c = $
			}
		''')
		exercise(code, w(newArrayList()));
	}
	
	@Test 
	def void testCompletionOnGenericTypeInMethod6(){
		val code = CodeBuilder::classbody("CompletionOnGenericTypeInMethod", '''
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
	def void testCompletionOnGenericTypeInMethod7(){
		val code = CodeBuilder::classbody("CompletionOnGenericTypeInMethod", '''
			public List<String> findMe = new ArrayList<String>();

			public static void test_wrongGenericSubType() {
				final CompletionOnGenericTypeInMethod variable = new CompletionOnGenericTypeInMethod();
				final List<? super Serializable> c = $
			}
		''')
		exercise(code, w(newArrayList()));
	}

	@Test
	def void testCompletionOnMemberCallChainDepth1(){
		val code = CodeBuilder::classbody('''
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
	def void testCompletionOnMemberCallChainDepth2(){
		val code = CodeBuilder::classbody("CompletionOnMemberCallChainDepth2", '''
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
	def void testCompletionOnMemberCallChainDepth3(){
		val code = CodeBuilder::classbody('''
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
		exercise(code, w(newArrayList("a b findMethod", "a b findMember", "a b findMethod", "a b findMember")));
	}
	
	@Test
	def void testCompletionOnMemberInMethodWithPrefix(){
		val code = CodeBuilder::classbody("CompletionOnMemberInMethodWithPrefix", '''
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
	def void testCompletionOnMethodReturn(){
		val code = CodeBuilder::classbody('''
			public void method() {
				final Iterator<AtomicLong> c = $
			}

			private List<AtomicLong> getList() {
				return new LinkedList<AtomicLong>();
			}
		''')
		var expected = w(newArrayList(
			"getList iterator",
			"getList listIterator",
			"getList listIterator",
			"getList subList iterator",
			"getList subList listIterator",
			"getList subList listIterator"
			))
		exercise(code, expected);
	}
	
	@Test
	// TODO: More methods from this fixture
	def void testCompletionOnNonPublicMemberInMethod1(){
		val code = CodeBuilder::classbody("CompletionOnNonPublicMemberInMethod", '''
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
	// TODO: More methods from this fixture
	@Ignore("Context does not seem to resolve primitives as target types")
	def void testCompletionOnPrimitiveTypeInMethod1(){
		val code = CodeBuilder::classbody('''
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
	def void testCompletionOnSupertypeInMethod(){
		val code = CodeBuilder::classbody("CompletionOnSupertypeInMethod", '''
			public ByteArrayInputStream findMe = new ByteArrayInputStream(new byte[] { 0, 1, 2, 3 });

			public static void method() {
				final CompletionOnSupertypeInMethod useMe = new CompletionOnSupertypeInMethod();
				final InputStream c = $
			}
		''')
		var expected = w(newArrayList(
			"useMe findMe",
			"useMe getClass getResourceAsStream",
			"useMe getClass getClassLoader getResourceAsStream",
			"useMe getClass getSuperclass getResourceAsStream",
			"useMe getClass getInterfaces getResourceAsStream",
			"useMe getClass getComponentType getResourceAsStream",
			"useMe getClass getDeclaringClass getResourceAsStream",
			"useMe getClass getEnclosingClass getResourceAsStream",
			"useMe getClass getClasses getResourceAsStream",
			"useMe getClass getDeclaredClasses getResourceAsStream",
			"useMe getClass getResource openStream",
			"useMe getClass asSubclass getResourceAsStream",
			"useMe clone getClass getResourceAsStream",
			"useMe toString getClass getResourceAsStream"
			))
		exercise(code, expected);
	}
	
	@Test
	def void testCompletionOnSupertypeMemberInMethod1(){
		val code = CodeBuilder::classbody("CompletionOnSupertypeMemberInMethod", '''
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
	def void testCompletionOnSupertypeMemberInMethod2(){
		val code = CodeBuilder::classbody("CompletionOnSupertypeMemberInMethod", '''
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
	def void testCompletionOnThisAndLocal(){
		val code = CodeBuilder::method('''
			final Map map = new HashMap();
			final Collection c = $
		''')
		exercise(code, w(newArrayList("map entrySet", "map keySet", "map values")));
	}
	
	@Test 
	def void testCompletionOnType(){
		val code = CodeBuilder::classbody('''
			static class S {

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
	@Ignore("This doesn't seem to have worked before as well")
	def void testCompletionViaGenericTypeInMethod(){
		val code = CodeBuilder::method("CompletionViaGenericTypeInMethod", '''
			final Iterator<CompletionViaGenericTypeInMethod> useMe = Arrays.asList(
				new CompletionViaGenericTypeInMethod()).iterator();
			final CompletionViaGenericTypeInMethod c = $
		''')
		exercise(code, w(newArrayList("useMe next")));
	}
	
	@Test
	def void testCompletionViaLocalVariableInMethod(){
		val code = CodeBuilder::classbody("CompletionViaLocalVariableInMethod", '''
			public AtomicBoolean findMe = new AtomicBoolean();

			public static void method() {
				final CompletionViaLocalVariableInMethod variable = new CompletionViaLocalVariableInMethod();
				final AtomicBoolean c = $
			}
		''')
		exercise(code, w(newArrayList("variable findMe")));
	}
	
	@Test
	def void testCompletionViaStaticArrayInMethod(){
		val code = CodeBuilder::classbody("CompletionViaStaticArrayInMethod", '''
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
	def void testExpectArray(){
		val code = CodeBuilder::method('''
			BigInteger bigInt = null;
			final BigInteger[] array = $
		''')
		var expected = w(newArrayList(
			"bigInt divideAndRemainder",
			"bigInt nextProbablePrime divideAndRemainder",
			"bigInt add divideAndRemainder",
			"bigInt subtract divideAndRemainder",
			"bigInt multiply divideAndRemainder",
			"bigInt divide divideAndRemainder",
			"bigInt remainder divideAndRemainder",
			"bigInt pow divideAndRemainder",
			"bigInt gcd divideAndRemainder",
			"bigInt abs divideAndRemainder",
			"bigInt negate divideAndRemainder",
			"bigInt mod divideAndRemainder",
			"bigInt modPow divideAndRemainder",
			"bigInt modInverse divideAndRemainder",
			"bigInt shiftLeft divideAndRemainder",
			"bigInt shiftRight divideAndRemainder",
			"bigInt and divideAndRemainder",
			"bigInt or divideAndRemainder",
			"bigInt xor divideAndRemainder",
			"bigInt not divideAndRemainder"
			))
		exercise(code, expected);
	}
	
	@Test
	def void testLocalWithPrefix(){
		val code = CodeBuilder::classbody("LocalWithPrefix", '''
			public AtomicBoolean findMe = new AtomicBoolean();

			public static void method1() {
				final LocalWithPrefix useMe = new LocalWithPrefix();
				final AtomicBoolean c = use$
			}
		''')
		exercise(code, w(newArrayList("useMe findMe")));
	}
 
	@Test
	def void testFindLocalAnchor(){
		val code = CodeBuilder::method('''
			ExecutorService pool = Executors.newCachedThreadPool();
			Future future = $
		''')
		var expected = w(newArrayList(
			"pool submit", // different args
			"pool submit", // different args
			"pool submit"  // different args
			))
		exercise(code, expected);
	}
	 
	@Test
	@Ignore("Rework so it returns chains of more than 1 element")
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
		var expected = w(newArrayList(
			"findMe",
			"findMe subList"
			))
		exercise(code, expected);
	}
	
	
	@Test
	def void testFindFieldAnchor(){
		val code = '''
		import java.util.concurrent.*;
		public class MyClass {
			ExecutorService pool;
			void test() {
				Future future = $
			}
		}
		'''
		var expected = w(newArrayList(
			"pool submit", // different args
			"pool submit", // different args
			"pool submit"  // different args
			))
			
		exercise(code, expected);
	}

	@Test
	def void testFindArrayFieldAnchor(){
		val code = '''
		import java.util.concurrent.*;
		public class MyClass {
			ExecutorService pool[];
			void test() {
				Future future = $
			}
		}
		'''
		var expected = w(newArrayList(
			"pool submit", // different args
			"pool submit", // different args
			"pool submit"  // different args
			))
			
		exercise(code, expected);
	}
	@Test
	def void testFindMultiDimArrayField(){
		val code = '''
		import java.util.concurrent.*;
		public class MyClass {
			ExecutorService pool[][][];
			void test() {
				Future future = $
			} 
		}
		'''
		var expected = w(newArrayList(
			"pool submit", // different args
			"pool submit", // different args
			"pool submit"  // different args
			))
		exercise(code, expected);
	}

	@Test
	@Ignore("Rework so it returns chains of more than 1 element")
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
		var expected = w(newArrayList(
			"evt"
			))
		exercise(code, expected);
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
		var expected = w(newArrayList(
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
			"getRuntime exec getErrorStream",
			"getRuntime getClass getResourceAsStream",
			"getRuntime exec getClass getResourceAsStream",
			"getRuntime exec getClass getResourceAsStream",
			"getRuntime exec getClass getResourceAsStream",
			"getRuntime exec getClass getResourceAsStream",
			"getRuntime exec getClass getResourceAsStream",
			"getRuntime exec getClass getResourceAsStream"
			))
		exercise(code, expected);
	}
	
	@Test
	@Ignore("Rework so it returns chains of more than 1 element")
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
		var expected = w(newArrayList(
			"subList"
			))
		exercise(code, expected);
	}
	@Test 
	def void testCompletionOnStaticType() {
		val code =  CodeBuilder::method('''Iterator<String> l = Collections.$''')
		
		// need to def expectations
		var expected = w(newArrayList(
			"unmodifiableCollection iterator",
			"unmodifiableSet iterator",
			"unmodifiableSortedSet iterator",
			"unmodifiableList iterator",
			"unmodifiableList listIterator",
			"unmodifiableList listIterator",
			"synchronizedCollection iterator",
			"synchronizedSet iterator",
			"synchronizedSortedSet iterator",
			"synchronizedList iterator",
			"synchronizedList listIterator",
			"synchronizedList listIterator",
			"checkedCollection iterator",
			"checkedSet iterator",
			"checkedSortedSet iterator",
			"checkedList iterator",
			"checkedList listIterator",
			"checkedList listIterator",
			"emptySet iterator",
			"emptyList iterator"
			))
		exercise(code, expected);
	}
	
	@Test
	def void testCompletionOnReturnStatement() {
		val code = '''
		import java.util.*;
		class MyClass {
			Iterator<String> m(){
				List<Object> l;
				return $
			}
		}''' 
		
		// need to def expectations
		var expected = w(newArrayList(
			"l iterator",
			"l listIterator",
			"l listIterator",
			"l subList iterator",
			"l subList listIterator",
			"l subList listIterator"
			))
		exercise(code, expected);
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
	def void testFindSelfAssignment(){
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
		exercise(code, w(newArrayList("clazz pool")));
	}
	
	// TODO we should qualify the proposed field with "this." 
	@Test
	@Ignore("Rework so it returns chains of more than 1 element")
	def void testFindMatchingSubtypeForAssignment(){
		val code = '''
		import java.util.concurrent.*;
		public class MyClass {
			ThreadPoolExecutor pool;
			void test() {
				ExecutorService pool = $
			}
		}
		'''
		var expected = w(newArrayList(
			"pool"
			))
			
		exercise(code, expected);
	}

	
	@Test
	@Ignore("Rework so it returns chains of more than 1 element")
	def void testCompletionOnFieldField(){
		val code = '''
		import java.awt.*;
		public class MyClass {
			Event e;
			void test() {
				Event evt = e.evt.$
			}
		}
		'''
		var expected = w(newArrayList(
			"evt"
			))
			
		exercise(code, expected);
	}
	
	@Test
	@Ignore("Rework so it returns chains of more than 1 element")
	def void testPrefixFilter(){
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
		var expected = w(newArrayList(
			"aevt","aevt evt"
			))
			
		exercise(code, expected);
	}
	
	
	@Test
	def void test012(){
		compile(FIELDS)
		val code = CodeBuilder::classbody(
			'''
				public static Fields f = new Fields();
				public static void test_protected() {
				final Boolean c = $
			''') 
		exercise(code, w(newArrayList("f _public")));
	}
	
	def exercise(CharSequence code, List<? extends List<String>> expected){
		val struct = fixture.createFileAndParseWithMarkers(code.toString)
		val cu = struct.first;
		val completionIndex = struct.second.head
		val ctx = new org.eclipse.recommenders.tests.completion.rcp.JavaContentAssistContextMock(cu, completionIndex)
		
		val sut = new org.eclipse.recommenders.internal.completion.rcp.chain.ChainCompletionProposalComputer(new RecommendersCompletionContextFactoryMock())
		sut.sessionStarted
		val proposals = sut.computeCompletionProposals(ctx, null)
		println(code.toString)
		println(proposals)
		for(proposal : proposals){
			val names = (proposal as org.eclipse.recommenders.internal.completion.rcp.chain.ChainCompletionProposal).getChainElementNames
			assertTrue('''couldn't find «names» in expected.'''.toString, expected.remove(names))
		} 
		assertTrue(''' some expected values were not found «expected» '''.toString, expected.empty)
	}
	
	
	def compile(CharSequence code){
		fixture.createFileAndParseWithMarkers(code)
	}


	def l(String spaceSeparatedElementNames){ 
		val elementNames = StringUtils::split(spaceSeparatedElementNames);
		return newArrayList(elementNames) as List<String>
	}
	def  w (String [] chains){
		val List<List<String>> res = newArrayList();
		for(chain :chains){
			res.add(l(chain))
		}
		return res as List<List<String>>
	}
	
	CharSequence FIELDS = 
		'''
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