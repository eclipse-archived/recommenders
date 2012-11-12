package org.eclipse.recommenders.tests.completion.rcp.chain;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import junit.framework.Assert;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.recommenders.internal.completion.rcp.chain.ChainCompletionProposal;
import org.eclipse.recommenders.tests.CodeBuilder;
import org.eclipse.recommenders.tests.SmokeTestScenarios;
import org.eclipse.recommenders.tests.completion.rcp.JavaContentAssistContextMock;
import org.eclipse.recommenders.tests.completion.rcp.RecommendersCompletionContextFactoryMock;
import org.eclipse.recommenders.tests.completion.rcp.chain.ChainPreferenceStoreMock;
import org.eclipse.recommenders.tests.completion.rcp.chain.TestingChainCompletionProposalComputer;
import org.eclipse.recommenders.tests.jdt.JavaProjectFixture;
import org.eclipse.recommenders.utils.Tuple;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Conversions;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.Functions.Function0;
import org.eclipse.xtext.xbase.lib.IntegerRange;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

@SuppressWarnings("all")
public class ChainCompletionScenariosTest {
  private static JavaProjectFixture fixture = new Function0<JavaProjectFixture>() {
    public JavaProjectFixture apply() {
      IWorkspace _workspace = ResourcesPlugin.getWorkspace();
      JavaProjectFixture _javaProjectFixture = new JavaProjectFixture(_workspace, "test");
      return _javaProjectFixture;
    }
  }.apply();
  
  @Before
  public void before() {
    try {
      ChainCompletionScenariosTest.fixture.clear();
    } catch (Exception _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  @Test
  public void smokeTestScenarios() {
    try {
      List<CharSequence> _scenarios = SmokeTestScenarios.scenarios();
      for (final CharSequence scenario : _scenarios) {
        {
          final Tuple<ICompilationUnit,Set<Integer>> struct = ChainCompletionScenariosTest.fixture.createFileAndParseWithMarkers(scenario);
          final ICompilationUnit cu = struct.getFirst();
          Set<Integer> _second = struct.getSecond();
          for (final Integer completionIndex : _second) {
            {
              JavaContentAssistContextMock _javaContentAssistContextMock = new JavaContentAssistContextMock(cu, (completionIndex).intValue());
              final JavaContentAssistContextMock ctx = _javaContentAssistContextMock;
              RecommendersCompletionContextFactoryMock _recommendersCompletionContextFactoryMock = new RecommendersCompletionContextFactoryMock();
              IPreferenceStore _create = ChainPreferenceStoreMock.create();
              TestingChainCompletionProposalComputer _testingChainCompletionProposalComputer = new TestingChainCompletionProposalComputer(_recommendersCompletionContextFactoryMock, _create);
              final TestingChainCompletionProposalComputer sut = _testingChainCompletionProposalComputer;
              sut.sessionStarted();
              sut.computeCompletionProposals(ctx, null);
            }
          }
        }
      }
    } catch (Exception _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  @Test
  public void testAccessMethodParameter() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("public void method(final List list){");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("Iterator it = $");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    final CharSequence code = CodeBuilder.classbody(_builder);
    ArrayList<String> _newArrayList = CollectionLiterals.<String>newArrayList(
      "list iterator", 
      "list listIterator", 
      "list listIterator", 
      "list subList iterator", 
      "list subList listIterator", 
      "list subList listIterator");
    List<List<String>> expected = this.w(((String[])Conversions.unwrapArray(_newArrayList, String.class)));
    this.exercise(code, expected);
  }
  
  @Test
  public void testAvoidRecursiveCallsToMember() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("AvoidRecursiveCallsToMember");
    StringConcatenation _builder_1 = new StringConcatenation();
    _builder_1.append("public AtomicBoolean findMe = new AtomicBoolean();");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("public AvoidRecursiveCallsToMember getSubElement() {");
    _builder_1.newLine();
    _builder_1.append("\t");
    _builder_1.append("return new AvoidRecursiveCallsToMember();");
    _builder_1.newLine();
    _builder_1.append("}");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("public static void method2() {");
    _builder_1.newLine();
    _builder_1.append("\t");
    _builder_1.append("final AvoidRecursiveCallsToMember useMe = new AvoidRecursiveCallsToMember();");
    _builder_1.newLine();
    _builder_1.append("\t");
    _builder_1.append("final AtomicBoolean c = useMe.get$");
    _builder_1.newLine();
    _builder_1.append("}");
    _builder_1.newLine();
    final CharSequence code = CodeBuilder.classbody(_builder, _builder_1);
    ArrayList<String> _newArrayList = CollectionLiterals.<String>newArrayList("getSubElement findMe");
    List<List<String>> _w = this.w(((String[])Conversions.unwrapArray(_newArrayList, String.class)));
    this.exercise(code, _w);
  }
  
  @Test
  public void testCompletionOnArrayMemberAccessInMethod1() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("public AtomicInteger findUs[] = { new AtomicInteger(1), new AtomicInteger(2) };");
    _builder.newLine();
    _builder.append("public AtomicBoolean findUs1[][][] = new AtomicBoolean[1][1][1];");
    _builder.newLine();
    _builder.newLine();
    _builder.append("public static void method1() {");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("final CompletionOnArrayMemberAccessInMethod obj = new CompletionOnArrayMemberAccessInMethod();");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("final AtomicInteger c = $");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    final CharSequence code = CodeBuilder.classbody("CompletionOnArrayMemberAccessInMethod", _builder);
    ArrayList<String> _newArrayList = CollectionLiterals.<String>newArrayList("obj findUs");
    List<List<String>> _w = this.w(((String[])Conversions.unwrapArray(_newArrayList, String.class)));
    this.exercise(code, _w);
  }
  
  @Test
  public void testCompletionOnArrayMemberAccessInMethod2() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("public AtomicInteger findUs[] = { new AtomicInteger(1), new AtomicInteger(2) };");
    _builder.newLine();
    _builder.append("public AtomicBoolean findUs1[][][] = new AtomicBoolean[1][1][1];");
    _builder.newLine();
    _builder.newLine();
    _builder.append("public static void method2() {");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("final CompletionOnArrayMemberAccessInMethod obj = new CompletionOnArrayMemberAccessInMethod();");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("final AtomicInteger[] c = $");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    final CharSequence code = CodeBuilder.classbody("CompletionOnArrayMemberAccessInMethod", _builder);
    ArrayList<String> _newArrayList = CollectionLiterals.<String>newArrayList("obj findUs");
    List<List<String>> _w = this.w(((String[])Conversions.unwrapArray(_newArrayList, String.class)));
    this.exercise(code, _w);
  }
  
  @Test
  public void testCompletionOnArrayMemberAccessInMethod3() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("public AtomicInteger findUs[] = { new AtomicInteger(1), new AtomicInteger(2) };");
    _builder.newLine();
    _builder.append("public AtomicBoolean findUs1[][][] = new AtomicBoolean[1][1][1];");
    _builder.newLine();
    _builder.newLine();
    _builder.append("public static void method3() {");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("final CompletionOnArrayMemberAccessInMethod obj = new CompletionOnArrayMemberAccessInMethod();");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("final AtomicBoolean[][][] c = $");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    final CharSequence code = CodeBuilder.classbody("CompletionOnArrayMemberAccessInMethod", _builder);
    ArrayList<String> _newArrayList = CollectionLiterals.<String>newArrayList("obj findUs1");
    List<List<String>> _w = this.w(((String[])Conversions.unwrapArray(_newArrayList, String.class)));
    this.exercise(code, _w);
  }
  
  @Test
  public void testCompletionOnArrayMemberAccessInMethod4() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("public AtomicInteger findUs[] = { new AtomicInteger(1), new AtomicInteger(2) };");
    _builder.newLine();
    _builder.append("public AtomicBoolean findUs1[][][] = new AtomicBoolean[1][1][1];");
    _builder.newLine();
    _builder.newLine();
    _builder.append("public static void method4() {");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("final CompletionOnArrayMemberAccessInMethod obj = new CompletionOnArrayMemberAccessInMethod();");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("final AtomicBoolean[][] c = $");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    final CharSequence code = CodeBuilder.classbody("CompletionOnArrayMemberAccessInMethod", _builder);
    ArrayList<String> _newArrayList = CollectionLiterals.<String>newArrayList("obj findUs1");
    List<List<String>> _w = this.w(((String[])Conversions.unwrapArray(_newArrayList, String.class)));
    this.exercise(code, _w);
  }
  
  @Test
  public void testCompletionOnArrayMemberAccessInMethod5() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("public AtomicInteger findUs[] = { new AtomicInteger(1), new AtomicInteger(2) };");
    _builder.newLine();
    _builder.append("public AtomicBoolean findUs1[][][] = new AtomicBoolean[1][1][1];");
    _builder.newLine();
    _builder.newLine();
    _builder.append("public static void method5() {");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("final CompletionOnArrayMemberAccessInMethod obj = new CompletionOnArrayMemberAccessInMethod();");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("final AtomicBoolean c[] = $");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    final CharSequence code = CodeBuilder.classbody("CompletionOnArrayMemberAccessInMethod", _builder);
    ArrayList<String> _newArrayList = CollectionLiterals.<String>newArrayList("obj findUs1");
    List<List<String>> _w = this.w(((String[])Conversions.unwrapArray(_newArrayList, String.class)));
    this.exercise(code, _w);
  }
  
  @Test
  public void testCompletionOnArrayMemberAccessInMethod6() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("public AtomicInteger findUs[] = { new AtomicInteger(1), new AtomicInteger(2) };");
    _builder.newLine();
    _builder.append("public AtomicBoolean findUs1[][][] = new AtomicBoolean[1][1][1];");
    _builder.newLine();
    _builder.newLine();
    _builder.append("public static void method6() {");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("final CompletionOnArrayMemberAccessInMethod obj = new CompletionOnArrayMemberAccessInMethod();");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("final AtomicBoolean c = $");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    final CharSequence code = CodeBuilder.classbody("CompletionOnArrayMemberAccessInMethod", _builder);
    ArrayList<String> _newArrayList = CollectionLiterals.<String>newArrayList("obj findUs1");
    List<List<String>> _w = this.w(((String[])Conversions.unwrapArray(_newArrayList, String.class)));
    this.exercise(code, _w);
  }
  
  @Test
  public void testCompletionOnArrayWithCastsSupertype1() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("CompletionOnArrayWithCastsSupertype");
    StringConcatenation _builder_1 = new StringConcatenation();
    _builder_1.append("public Integer[][][] findme;");
    _builder_1.newLine();
    _builder_1.append("public int i;");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("public static void method1() {");
    _builder_1.newLine();
    _builder_1.append("\t");
    _builder_1.append("final CompletionOnArrayWithCastsSupertype obj = new CompletionOnArrayWithCastsSupertype();");
    _builder_1.newLine();
    _builder_1.append("\t");
    _builder_1.append("final Number c = $");
    _builder_1.newLine();
    _builder_1.append("}");
    _builder_1.newLine();
    final CharSequence code = CodeBuilder.classbody(_builder, _builder_1);
    ArrayList<String> _newArrayList = CollectionLiterals.<String>newArrayList("obj findme");
    List<List<String>> _w = this.w(((String[])Conversions.unwrapArray(_newArrayList, String.class)));
    this.exercise(code, _w);
  }
  
  @Test
  public void testCompletionOnArrayWithCastsSupertype2() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("CompletionOnArrayWithCastsSupertype");
    StringConcatenation _builder_1 = new StringConcatenation();
    _builder_1.append("public Integer[][][] findme;");
    _builder_1.newLine();
    _builder_1.append("public int i;");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("public static void method2() {");
    _builder_1.newLine();
    _builder_1.append("\t");
    _builder_1.append("final CompletionOnArrayWithCastsSupertype obj = new CompletionOnArrayWithCastsSupertype();");
    _builder_1.newLine();
    _builder_1.append("\t");
    _builder_1.append("final Number[] c = $");
    _builder_1.newLine();
    _builder_1.append("}");
    _builder_1.newLine();
    final CharSequence code = CodeBuilder.classbody(_builder, _builder_1);
    ArrayList<String> _newArrayList = CollectionLiterals.<String>newArrayList("obj findme");
    List<List<String>> _w = this.w(((String[])Conversions.unwrapArray(_newArrayList, String.class)));
    this.exercise(code, _w);
  }
  
  @Test
  public void testCompletionOnArrayWithCastsSupertype3() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("CompletionOnArrayWithCastsSupertype");
    StringConcatenation _builder_1 = new StringConcatenation();
    _builder_1.append("public Integer[][][] findme;");
    _builder_1.newLine();
    _builder_1.append("public int i;");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("public static void method3() {");
    _builder_1.newLine();
    _builder_1.append("\t");
    _builder_1.append("final CompletionOnArrayWithCastsSupertype obj = new CompletionOnArrayWithCastsSupertype();");
    _builder_1.newLine();
    _builder_1.append("\t");
    _builder_1.append("final Number[][] c = $");
    _builder_1.newLine();
    _builder_1.append("}");
    _builder_1.newLine();
    final CharSequence code = CodeBuilder.classbody(_builder, _builder_1);
    ArrayList<String> _newArrayList = CollectionLiterals.<String>newArrayList("obj findme");
    List<List<String>> _w = this.w(((String[])Conversions.unwrapArray(_newArrayList, String.class)));
    this.exercise(code, _w);
  }
  
  @Test
  public void testCompletionOnArrayWithCastsSupertype4() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("CompletionOnArrayWithCastsSupertype");
    StringConcatenation _builder_1 = new StringConcatenation();
    _builder_1.append("public Integer[][][] findme;");
    _builder_1.newLine();
    _builder_1.append("public int i;");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("public static void method4() {");
    _builder_1.newLine();
    _builder_1.append("\t");
    _builder_1.append("final CompletionOnArrayWithCastsSupertype obj = new CompletionOnArrayWithCastsSupertype();");
    _builder_1.newLine();
    _builder_1.append("\t");
    _builder_1.append("final Number[][][] c = $");
    _builder_1.newLine();
    _builder_1.append("}");
    _builder_1.newLine();
    final CharSequence code = CodeBuilder.classbody(_builder, _builder_1);
    ArrayList<String> _newArrayList = CollectionLiterals.<String>newArrayList("obj findme");
    List<List<String>> _w = this.w(((String[])Conversions.unwrapArray(_newArrayList, String.class)));
    this.exercise(code, _w);
  }
  
  @Test
  public void testCompletionOnGenericTypeInMethod1() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("public List<String> findMe = new ArrayList<String>();");
    _builder.newLine();
    _builder.newLine();
    _builder.append("public static void test_exactGenericType() {");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("final CompletionOnGenericTypeInMethod variable = new CompletionOnGenericTypeInMethod();");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("final List<String> c = $");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    final CharSequence code = CodeBuilder.classbody("CompletionOnGenericTypeInMethod", _builder);
    ArrayList<String> _newArrayList = CollectionLiterals.<String>newArrayList("variable findMe");
    List<List<String>> _w = this.w(((String[])Conversions.unwrapArray(_newArrayList, String.class)));
    this.exercise(code, _w);
  }
  
  @Test
  @Ignore(value = "Bug! However, it already existed in old version.")
  public void testCompletionOnGenericTypeInMethod2() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("public List<String> findMe = new ArrayList<String>();");
    _builder.newLine();
    _builder.newLine();
    _builder.append("public static void test_exactButWrongGenericType() {");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("final CompletionOnGenericTypeInMethod variable = new CompletionOnGenericTypeInMethod();");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("final List<Integer> c = $");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    final CharSequence code = CodeBuilder.classbody("CompletionOnGenericTypeInMethod", _builder);
    ArrayList<String> _newArrayList = CollectionLiterals.<String>newArrayList();
    List<List<String>> _w = this.w(((String[])Conversions.unwrapArray(_newArrayList, String.class)));
    this.exercise(code, _w);
  }
  
  @Test
  public void testCompletionOnGenericTypeInMethod3() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("public List<String> findMe = new ArrayList<String>();");
    _builder.newLine();
    _builder.newLine();
    _builder.append("public static void test_anonymousGenericType() {");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("final CompletionOnGenericTypeInMethod variable = new CompletionOnGenericTypeInMethod();");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("final List<?> c = $");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    final CharSequence code = CodeBuilder.classbody("CompletionOnGenericTypeInMethod", _builder);
    ArrayList<String> _newArrayList = CollectionLiterals.<String>newArrayList("variable findMe");
    List<List<String>> _w = this.w(((String[])Conversions.unwrapArray(_newArrayList, String.class)));
    this.exercise(code, _w);
  }
  
  @Test
  public void testCompletionOnGenericTypeInMethod4() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("public List<String> findMe = new ArrayList<String>();");
    _builder.newLine();
    _builder.newLine();
    _builder.append("public static void test_genericSuperType() {");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("final CompletionOnGenericTypeInMethod variable = new CompletionOnGenericTypeInMethod();");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("final List<? extends Serializable> c = $");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    final CharSequence code = CodeBuilder.classbody("CompletionOnGenericTypeInMethod", _builder);
    ArrayList<String> _newArrayList = CollectionLiterals.<String>newArrayList("variable findMe");
    List<List<String>> _w = this.w(((String[])Conversions.unwrapArray(_newArrayList, String.class)));
    this.exercise(code, _w);
  }
  
  @Test
  @Ignore(value = "Bug! However, it already existed in old version.")
  public void testCompletionOnGenericTypeInMethod5() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("public List<String> findMe = new ArrayList<String>();");
    _builder.newLine();
    _builder.newLine();
    _builder.append("public static void test_wrongGenericSuperType() {");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("final CompletionOnGenericTypeInMethod variable = new CompletionOnGenericTypeInMethod();");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("final List<? extends File> c = $");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    final CharSequence code = CodeBuilder.classbody("CompletionOnGenericTypeInMethod", _builder);
    ArrayList<String> _newArrayList = CollectionLiterals.<String>newArrayList();
    List<List<String>> _w = this.w(((String[])Conversions.unwrapArray(_newArrayList, String.class)));
    this.exercise(code, _w);
  }
  
  @Test
  public void testCompletionOnGenericTypeInMethod6() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("public List<String> findMe = new ArrayList<String>();");
    _builder.newLine();
    _builder.newLine();
    _builder.append("public static void test_genericSubType() {");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("final CompletionOnGenericTypeInMethod variable = new CompletionOnGenericTypeInMethod();");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("final List<? super String> c = $");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    final CharSequence code = CodeBuilder.classbody("CompletionOnGenericTypeInMethod", _builder);
    ArrayList<String> _newArrayList = CollectionLiterals.<String>newArrayList("variable findMe");
    List<List<String>> _w = this.w(((String[])Conversions.unwrapArray(_newArrayList, String.class)));
    this.exercise(code, _w);
  }
  
  @Test
  @Ignore(value = "Bug! However, it already existed in old version.")
  public void testCompletionOnGenericTypeInMethod7() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("public List<String> findMe = new ArrayList<String>();");
    _builder.newLine();
    _builder.newLine();
    _builder.append("public static void test_wrongGenericSubType() {");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("final CompletionOnGenericTypeInMethod variable = new CompletionOnGenericTypeInMethod();");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("final List<? super Serializable> c = $");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    final CharSequence code = CodeBuilder.classbody("CompletionOnGenericTypeInMethod", _builder);
    ArrayList<String> _newArrayList = CollectionLiterals.<String>newArrayList();
    List<List<String>> _w = this.w(((String[])Conversions.unwrapArray(_newArrayList, String.class)));
    this.exercise(code, _w);
  }
  
  @Test
  public void testCompletionOnMemberCallChainDepth1() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("public class A {");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("public B b = new B();");
    _builder.newLine();
    _builder.newLine();
    _builder.append("\t");
    _builder.append("public class B {");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("public File findMember = new File(\"\");");
    _builder.newLine();
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("public File findMethod() {");
    _builder.newLine();
    _builder.append("\t\t\t");
    _builder.append("return null;");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("}");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("}");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    _builder.newLine();
    _builder.append("A a = new A();");
    _builder.newLine();
    _builder.append("File c = $");
    _builder.newLine();
    final CharSequence code = CodeBuilder.classbody(_builder);
    ArrayList<String> _newArrayList = CollectionLiterals.<String>newArrayList("a b findMethod", "a b findMember");
    List<List<String>> _w = this.w(((String[])Conversions.unwrapArray(_newArrayList, String.class)));
    this.exercise(code, _w);
  }
  
  @Test
  @Ignore(value = "Bug! Chains using this.a are expected as well.")
  public void testCompletionOnMemberCallChainDepth2() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("public class A {");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("public B b = new B();");
    _builder.newLine();
    _builder.newLine();
    _builder.append("\t");
    _builder.append("public class B {");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("public File findMember = new File(\"\");");
    _builder.newLine();
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("public File findMethod() {");
    _builder.newLine();
    _builder.append("\t\t\t");
    _builder.append("return null;");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("}");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("}");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    _builder.newLine();
    _builder.append("A a = new A();");
    _builder.newLine();
    _builder.newLine();
    _builder.append("public CompletionOnMemberCallChainDepth2(){");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("final A a = new A();");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("final File c = $");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    final CharSequence code = CodeBuilder.classbody("CompletionOnMemberCallChainDepth2", _builder);
    ArrayList<String> _newArrayList = CollectionLiterals.<String>newArrayList("a b findMethod", "a b findMember", "a b findMethod", "a b findMember");
    List<List<String>> _w = this.w(((String[])Conversions.unwrapArray(_newArrayList, String.class)));
    this.exercise(code, _w);
  }
  
  @Test
  @Ignore(value = "Bug! Chains using this.a are expected as well.")
  public void testCompletionOnMemberCallChainDepth3() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("public class A {");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("public B b = new B();");
    _builder.newLine();
    _builder.newLine();
    _builder.append("\t");
    _builder.append("public class B {");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("public File findMember = new File(\"\");");
    _builder.newLine();
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("public File findMethod() {");
    _builder.newLine();
    _builder.append("\t\t\t");
    _builder.append("return null;");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("}");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("}");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    _builder.newLine();
    _builder.append("A a = new A();");
    _builder.newLine();
    _builder.newLine();
    _builder.append("public void method(){");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("final A a = new A();");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("final File c = $");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    final CharSequence code = CodeBuilder.classbody(_builder);
    ArrayList<String> _newArrayList = CollectionLiterals.<String>newArrayList("a b findMethod", "a b findMember", "this a b findMethod", "this a b findMember");
    List<List<String>> _w = this.w(((String[])Conversions.unwrapArray(_newArrayList, String.class)));
    this.exercise(code, _w);
  }
  
  @Test
  public void testCompletionOnMemberInMethodWithPrefix() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("public AtomicBoolean findMe = new AtomicBoolean();");
    _builder.newLine();
    _builder.newLine();
    _builder.append("public CompletionOnMemberInMethodWithPrefix getSubElement() {");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("return new CompletionOnMemberInMethodWithPrefix();");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    _builder.newLine();
    _builder.append("public static void method2() {");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("final CompletionOnMemberInMethodWithPrefix useMe = new CompletionOnMemberInMethodWithPrefix();");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("final AtomicBoolean c = useMe.get$");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    final CharSequence code = CodeBuilder.classbody("CompletionOnMemberInMethodWithPrefix", _builder);
    ArrayList<String> _newArrayList = CollectionLiterals.<String>newArrayList("getSubElement findMe");
    List<List<String>> _w = this.w(((String[])Conversions.unwrapArray(_newArrayList, String.class)));
    this.exercise(code, _w);
  }
  
  @Test
  public void testCompletionOnMethodReturn() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("public void method() {");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("final Iterator<AtomicLong> c = $");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    _builder.newLine();
    _builder.append("private List<AtomicLong> getList() {");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("return new LinkedList<AtomicLong>();");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    final CharSequence code = CodeBuilder.classbody(_builder);
    ArrayList<String> _newArrayList = CollectionLiterals.<String>newArrayList(
      "getList iterator", 
      "getList listIterator", 
      "getList listIterator", 
      "getList subList iterator", 
      "getList subList listIterator", 
      "getList subList listIterator");
    List<List<String>> expected = this.w(((String[])Conversions.unwrapArray(_newArrayList, String.class)));
    this.exercise(code, expected);
  }
  
  @Test
  public void testCompletionOnNonPublicMemberInMethod1() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("protected AtomicBoolean findMe1 = new AtomicBoolean();");
    _builder.newLine();
    _builder.append("AtomicInteger findMe2 = new AtomicInteger();");
    _builder.newLine();
    _builder.append("private final AtomicLong findMe3 = new AtomicLong();");
    _builder.newLine();
    _builder.newLine();
    _builder.append("public static void test_protected() {");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("final CompletionOnNonPublicMemberInMethod useMe = new CompletionOnNonPublicMemberInMethod();");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("final AtomicBoolean c = $");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    final CharSequence code = CodeBuilder.classbody("CompletionOnNonPublicMemberInMethod", _builder);
    ArrayList<String> _newArrayList = CollectionLiterals.<String>newArrayList("useMe findMe1");
    List<List<String>> _w = this.w(((String[])Conversions.unwrapArray(_newArrayList, String.class)));
    this.exercise(code, _w);
  }
  
  @Test
  @Ignore(value = "Context does not seem to resolve primitives as target types")
  public void testCompletionOnPrimitiveTypeInMethod1() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("private class A {");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("public int findMe = 5;");
    _builder.newLine();
    _builder.newLine();
    _builder.append("\t");
    _builder.append("public int[] findMe2() {");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("return new int[1];");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("}");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    _builder.newLine();
    _builder.append("final A useMe = new A();");
    _builder.newLine();
    _builder.newLine();
    _builder.append("public void method() {");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("final A useMe = new A();");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("final int c = $");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    final CharSequence code = CodeBuilder.classbody(_builder);
    ArrayList<String> _newArrayList = CollectionLiterals.<String>newArrayList("useMe findMe", "useMe findMe2");
    List<List<String>> _w = this.w(((String[])Conversions.unwrapArray(_newArrayList, String.class)));
    this.exercise(code, _w);
  }
  
  @Test
  public void testCompletionOnSupertypeInMethod() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("public ByteArrayInputStream findMe = new ByteArrayInputStream(new byte[] { 0, 1, 2, 3 });");
    _builder.newLine();
    _builder.newLine();
    _builder.append("public static void method() {");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("final CompletionOnSupertypeInMethod useMe = new CompletionOnSupertypeInMethod();");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("final InputStream c = $");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    final CharSequence code = CodeBuilder.classbody("CompletionOnSupertypeInMethod", _builder);
    ArrayList<String> _newArrayList = CollectionLiterals.<String>newArrayList("useMe findMe");
    List<List<String>> _w = this.w(((String[])Conversions.unwrapArray(_newArrayList, String.class)));
    this.exercise(code, _w);
  }
  
  @Test
  public void testCompletionOnSupertypeMemberInMethod1() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("public static class Subtype extends CompletionOnSupertypeMemberInMethod {");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    _builder.newLine();
    _builder.append("public AtomicBoolean findMe = new AtomicBoolean();");
    _builder.newLine();
    _builder.newLine();
    _builder.append("public static void test_onAttribute() {");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("final Subtype useMe = new Subtype();");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("final AtomicBoolean c = $");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    final CharSequence code = CodeBuilder.classbody("CompletionOnSupertypeMemberInMethod", _builder);
    ArrayList<String> _newArrayList = CollectionLiterals.<String>newArrayList("useMe findMe");
    List<List<String>> _w = this.w(((String[])Conversions.unwrapArray(_newArrayList, String.class)));
    this.exercise(code, _w);
  }
  
  @Test
  public void testCompletionOnSupertypeMemberInMethod2() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("public static class Subtype extends CompletionOnSupertypeMemberInMethod {");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    _builder.newLine();
    _builder.append("public AtomicInteger findMe() {");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("return new AtomicInteger();");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    _builder.newLine();
    _builder.append("public static void test_onMethod() {");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("final Subtype useMe = new Subtype();");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("final AtomicInteger c = $");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    final CharSequence code = CodeBuilder.classbody("CompletionOnSupertypeMemberInMethod", _builder);
    ArrayList<String> _newArrayList = CollectionLiterals.<String>newArrayList("useMe findMe");
    List<List<String>> _w = this.w(((String[])Conversions.unwrapArray(_newArrayList, String.class)));
    this.exercise(code, _w);
  }
  
  @Test
  public void testCompletionOnThisAndLocal() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("final Map map = new HashMap();");
    _builder.newLine();
    _builder.append("final Collection c = $");
    _builder.newLine();
    final CharSequence code = CodeBuilder.method(_builder);
    ArrayList<String> _newArrayList = CollectionLiterals.<String>newArrayList("map entrySet", "map keySet", "map values");
    List<List<String>> _w = this.w(((String[])Conversions.unwrapArray(_newArrayList, String.class)));
    this.exercise(code, _w);
  }
  
  @Test
  public void testCompletionOnType() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("public static class S {");
    _builder.newLine();
    _builder.newLine();
    _builder.append("\t");
    _builder.append("private static S INSTANCE = new S();");
    _builder.newLine();
    _builder.newLine();
    _builder.append("\t");
    _builder.append("public Integer findMe() {");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("return 0;");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("}");
    _builder.newLine();
    _builder.newLine();
    _builder.append("\t");
    _builder.append("public static S getInstance() {");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("return INSTANCE;");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("}");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    _builder.newLine();
    _builder.append("public void __test() {");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("Integer i = S.$");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    final CharSequence code = CodeBuilder.classbody(_builder);
    ArrayList<String> _newArrayList = CollectionLiterals.<String>newArrayList("getInstance findMe");
    List<List<String>> _w = this.w(((String[])Conversions.unwrapArray(_newArrayList, String.class)));
    this.exercise(code, _w);
  }
  
  @Test
  public void testCompletionViaGenericTypeInMethod() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("final Iterator<CompletionViaGenericTypeInMethod> useMe = Arrays.asList(");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("new CompletionViaGenericTypeInMethod()).iterator();");
    _builder.newLine();
    _builder.append("final CompletionViaGenericTypeInMethod c = $");
    _builder.newLine();
    final CharSequence code = CodeBuilder.method("CompletionViaGenericTypeInMethod", _builder);
    ArrayList<String> _newArrayList = CollectionLiterals.<String>newArrayList("useMe next");
    List<List<String>> _w = this.w(((String[])Conversions.unwrapArray(_newArrayList, String.class)));
    this.exercise(code, _w);
  }
  
  @Test
  public void testCompletionViaLocalVariableInMethod() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("public AtomicBoolean findMe = new AtomicBoolean();");
    _builder.newLine();
    _builder.newLine();
    _builder.append("public static void method() {");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("final CompletionViaLocalVariableInMethod variable = new CompletionViaLocalVariableInMethod();");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("final AtomicBoolean c = $");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    final CharSequence code = CodeBuilder.classbody("CompletionViaLocalVariableInMethod", _builder);
    ArrayList<String> _newArrayList = CollectionLiterals.<String>newArrayList("variable findMe");
    List<List<String>> _w = this.w(((String[])Conversions.unwrapArray(_newArrayList, String.class)));
    this.exercise(code, _w);
  }
  
  @Test
  public void testCompletionViaStaticArrayInMethod() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("public AtomicBoolean findMe = new AtomicBoolean();");
    _builder.newLine();
    _builder.newLine();
    _builder.append("public static CompletionViaStaticArrayInMethod useUs[] = { new CompletionViaStaticArrayInMethod(),");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("new CompletionViaStaticArrayInMethod() };");
    _builder.newLine();
    _builder.newLine();
    _builder.append("public static void method1() {");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("final AtomicBoolean c = $");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    final CharSequence code = CodeBuilder.classbody("CompletionViaStaticArrayInMethod", _builder);
    ArrayList<String> _newArrayList = CollectionLiterals.<String>newArrayList("useUs findMe");
    List<List<String>> _w = this.w(((String[])Conversions.unwrapArray(_newArrayList, String.class)));
    this.exercise(code, _w);
  }
  
  @Test
  public void testExpectArray() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("BigInteger bigInt = null;");
    _builder.newLine();
    _builder.append("final BigInteger[] array = $");
    _builder.newLine();
    final CharSequence code = CodeBuilder.method(_builder);
    ArrayList<String> _newArrayList = CollectionLiterals.<String>newArrayList(
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
      "bigInt pow divideAndRemainder");
    List<List<String>> expected = this.w(((String[])Conversions.unwrapArray(_newArrayList, String.class)));
    this.exercise(code, expected);
  }
  
  @Test
  public void testLocalWithPrefix() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("public AtomicBoolean findMe = new AtomicBoolean();");
    _builder.newLine();
    _builder.newLine();
    _builder.append("public static void method1() {");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("final LocalWithPrefix useMe = new LocalWithPrefix();");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("final AtomicBoolean c = use$");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    final CharSequence code = CodeBuilder.classbody("LocalWithPrefix", _builder);
    ArrayList<String> _newArrayList = CollectionLiterals.<String>newArrayList("useMe findMe");
    List<List<String>> _w = this.w(((String[])Conversions.unwrapArray(_newArrayList, String.class)));
    this.exercise(code, _w);
  }
  
  @Test
  public void testFindLocalAnchor() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("ExecutorService pool = Executors.newCachedThreadPool();");
    _builder.newLine();
    _builder.append("Future future = $");
    _builder.newLine();
    final CharSequence code = CodeBuilder.method(_builder);
    ArrayList<String> _newArrayList = CollectionLiterals.<String>newArrayList(
      "pool submit", 
      "pool submit", 
      "pool submit", 
      "pool invokeAll get", 
      "pool invokeAll remove", 
      "pool invokeAll set", 
      "pool invokeAll get", 
      "pool invokeAll remove", 
      "pool invokeAll set", 
      "pool invokeAll iterator next", 
      "pool invokeAll listIterator next", 
      "pool invokeAll listIterator previous", 
      "pool invokeAll listIterator next", 
      "pool invokeAll listIterator previous", 
      "pool invokeAll subList get", 
      "pool invokeAll subList remove", 
      "pool invokeAll subList set", 
      "pool invokeAll iterator next", 
      "pool invokeAll listIterator next", 
      "pool invokeAll listIterator previous");
    List<List<String>> expected = this.w(((String[])Conversions.unwrapArray(_newArrayList, String.class)));
    this.exercise(code, expected);
  }
  
  @Test
  public void testFindLocalAnchorWithIsExactMatch() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("import java.util.*;");
    _builder.newLine();
    _builder.append("class MyClass {");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("void m(){");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("List<Object> findMe;");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("List<String> l = $");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("}");
    _builder.newLine();
    _builder.append("}");
    final CharSequence code = _builder;
    ArrayList<String> _newArrayList = CollectionLiterals.<String>newArrayList("findMe");
    List<List<String>> _w = this.w(((String[])Conversions.unwrapArray(_newArrayList, String.class)));
    this.exercise(code, _w);
  }
  
  @Test
  public void testFindFieldAnchor() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("import java.util.concurrent.*;");
    _builder.newLine();
    _builder.append("public class MyClass {");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("ExecutorService pool;");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("void test() {");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("Future future = $");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("}");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    final CharSequence code = _builder;
    ArrayList<String> _newArrayList = CollectionLiterals.<String>newArrayList(
      "pool submit", 
      "pool submit", 
      "pool submit", 
      "pool invokeAll get", 
      "pool invokeAll remove", 
      "pool invokeAll set", 
      "pool invokeAll get", 
      "pool invokeAll remove", 
      "pool invokeAll set", 
      "pool invokeAll iterator next", 
      "pool invokeAll listIterator next", 
      "pool invokeAll listIterator previous", 
      "pool invokeAll listIterator next", 
      "pool invokeAll listIterator previous", 
      "pool invokeAll subList get", 
      "pool invokeAll subList remove", 
      "pool invokeAll subList set", 
      "pool invokeAll iterator next", 
      "pool invokeAll listIterator next", 
      "pool invokeAll listIterator previous");
    List<List<String>> expected = this.w(((String[])Conversions.unwrapArray(_newArrayList, String.class)));
    this.exercise(code, expected);
  }
  
  @Test
  public void testFindArrayFieldAnchor() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("import java.util.concurrent.*;");
    _builder.newLine();
    _builder.append("public class MyClass {");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("ExecutorService pool[];");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("void test() {");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("Future future = $");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("}");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    final CharSequence code = _builder;
    ArrayList<String> _newArrayList = CollectionLiterals.<String>newArrayList(
      "pool submit", 
      "pool submit", 
      "pool submit", 
      "pool invokeAll get", 
      "pool invokeAll remove", 
      "pool invokeAll set", 
      "pool invokeAll get", 
      "pool invokeAll remove", 
      "pool invokeAll set", 
      "pool invokeAll iterator next", 
      "pool invokeAll listIterator next", 
      "pool invokeAll listIterator previous", 
      "pool invokeAll listIterator next", 
      "pool invokeAll listIterator previous", 
      "pool invokeAll subList get", 
      "pool invokeAll subList remove", 
      "pool invokeAll subList set", 
      "pool invokeAll iterator next", 
      "pool invokeAll listIterator next", 
      "pool invokeAll listIterator previous");
    List<List<String>> expected = this.w(((String[])Conversions.unwrapArray(_newArrayList, String.class)));
    this.exercise(code, expected);
  }
  
  @Test
  public void testFindMultiDimArrayField() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("import java.util.concurrent.*;");
    _builder.newLine();
    _builder.append("public class MyClass {");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("ExecutorService pool[][][];");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("void test() {");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("Future future = $");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("} ");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    final CharSequence code = _builder;
    ArrayList<String> _newArrayList = CollectionLiterals.<String>newArrayList(
      "pool submit", 
      "pool submit", 
      "pool submit", 
      "pool invokeAll get", 
      "pool invokeAll remove", 
      "pool invokeAll set", 
      "pool invokeAll get", 
      "pool invokeAll remove", 
      "pool invokeAll set", 
      "pool invokeAll iterator next", 
      "pool invokeAll listIterator next", 
      "pool invokeAll listIterator previous", 
      "pool invokeAll listIterator next", 
      "pool invokeAll listIterator previous", 
      "pool invokeAll subList get", 
      "pool invokeAll subList remove", 
      "pool invokeAll subList set", 
      "pool invokeAll iterator next", 
      "pool invokeAll listIterator next", 
      "pool invokeAll listIterator previous");
    List<List<String>> expected = this.w(((String[])Conversions.unwrapArray(_newArrayList, String.class)));
    this.exercise(code, expected);
  }
  
  @Test
  public void testFindFieldInSuperType() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("import java.util.*;");
    _builder.newLine();
    _builder.append("import java.awt.*;");
    _builder.newLine();
    _builder.append("class MyClass extends Event{");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("void m(){");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("Event e = $");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("}");
    _builder.newLine();
    _builder.append("}");
    final CharSequence code = _builder;
    ArrayList<String> _newArrayList = CollectionLiterals.<String>newArrayList("evt");
    List<List<String>> _w = this.w(((String[])Conversions.unwrapArray(_newArrayList, String.class)));
    this.exercise(code, _w);
  }
  
  @Test
  public void testCompletionOnRuntime() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("import java.io.*;");
    _builder.newLine();
    _builder.append("class MyClass {");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("void m(){");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("InputStream in = Runtime.$");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("}");
    _builder.newLine();
    _builder.append("}");
    final CharSequence code = _builder;
    ArrayList<String> _newArrayList = CollectionLiterals.<String>newArrayList(
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
      "getRuntime exec getErrorStream");
    List<List<String>> expected = this.w(((String[])Conversions.unwrapArray(_newArrayList, String.class)));
    this.exercise(code, expected);
  }
  
  @Test
  public void testCompletionOnLocaVariable() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("import java.util.*;");
    _builder.newLine();
    _builder.append("class MyClass {");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("void m(){");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("List<Object> findMe;");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("List<String> l = findMe.$");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("}");
    _builder.newLine();
    _builder.append("}");
    final CharSequence code = _builder;
    ArrayList<String> _newArrayList = CollectionLiterals.<String>newArrayList("subList");
    List<List<String>> _w = this.w(((String[])Conversions.unwrapArray(_newArrayList, String.class)));
    this.exercise(code, _w);
  }
  
  @Test
  public void testCompletionOnStaticType() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("Iterator<String> l = Collections.$");
    final CharSequence code = CodeBuilder.method(_builder);
    final ArrayList<String> wanted = CollectionLiterals.<String>newArrayList(
      "asLifoQueue iterator", 
      "checkedCollection iterator", 
      "checkedList iterator", 
      "checkedList listIterator", 
      "checkedList listIterator", 
      "checkedSet iterator", 
      "checkedSortedSet iterator", 
      "emptyList iterator", 
      "emptyList listIterator", 
      "emptyList listIterator", 
      "emptySet iterator", 
      "list iterator", 
      "list listIterator", 
      "list listIterator", 
      "singleton iterator", 
      "singletonList iterator", 
      "nCopies iterator", 
      "nCopies listIterator", 
      "nCopies listIterator", 
      "newSetFromMap iterator");
    if (SystemUtils.IS_JAVA_1_7) {
      wanted.add("emptyIterator");
      wanted.add("emptyListIterator");
      wanted.remove("singleton iterator");
      wanted.remove("singletonList iterator");
    }
    List<List<String>> expected = this.w(((String[])Conversions.unwrapArray(wanted, String.class)));
    this.exercise(code, expected);
  }
  
  @Test
  public void testCompletionOnReturnStatement() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("import java.util.*;");
    _builder.newLine();
    _builder.append("class MyClass {");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("Iterator<String> m(){");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("List<String> l;");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("return $");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("}");
    _builder.newLine();
    _builder.append("}");
    final CharSequence code = _builder;
    ArrayList<String> _newArrayList = CollectionLiterals.<String>newArrayList(
      "m", 
      "l iterator", 
      "l listIterator", 
      "l listIterator", 
      "l subList iterator", 
      "l subList listIterator", 
      "l subList listIterator");
    List<List<String>> expected = this.w(((String[])Conversions.unwrapArray(_newArrayList, String.class)));
    this.exercise(code, expected);
  }
  
  @Test
  public void testCompletionOnEnumDoesNotThrowNPE() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("import java.lang.annotation.*;");
    _builder.newLine();
    _builder.append("import java.util.*;");
    _builder.newLine();
    _builder.append("class MyClass {");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("void m(){");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("String s = Annotation.$");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("}");
    _builder.newLine();
    _builder.append("}");
    final CharSequence code = _builder;
    ArrayList<String> _newArrayList = CollectionLiterals.<String>newArrayList();
    List<List<String>> expected = this.w(((String[])Conversions.unwrapArray(_newArrayList, String.class)));
    this.exercise(code, expected);
  }
  
  /**
   * we had some trouble with supertype hierarchy. This test that we do not generate
   * any chains that return a supertype of the requested type (ExecutorService in this case)
   */
  @Test
  public void testFindSelfAssignment() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("import java.util.concurrent.*;");
    _builder.newLine();
    _builder.append("public class MyClass {");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("ThreadPoolExecutor pool;");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("void test() {");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("MyClass clazz = new MyClass();");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("pool = $");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("}");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    final CharSequence code = _builder;
    ArrayList<String> _newArrayList = CollectionLiterals.<String>newArrayList("pool", "clazz pool");
    List<List<String>> _w = this.w(((String[])Conversions.unwrapArray(_newArrayList, String.class)));
    this.exercise(code, _w);
  }
  
  @Test
  public void testFindMatchingSubtypeForAssignment() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("import java.util.concurrent.*;");
    _builder.newLine();
    _builder.append("public class MyClass {");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("ThreadPoolExecutor pool;");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("void test() {");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("ExecutorService pool = $");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("}");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    final CharSequence code = _builder;
    ArrayList<String> _newArrayList = CollectionLiterals.<String>newArrayList("pool");
    List<List<String>> _w = this.w(((String[])Conversions.unwrapArray(_newArrayList, String.class)));
    this.exercise(code, _w);
  }
  
  @Test
  public void testCompletionOnFieldField() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("import java.awt.*;");
    _builder.newLine();
    _builder.append("public class MyClass {");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("Event e;");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("void test() {");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("Event evt = e.evt.$");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("}");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    final CharSequence code = _builder;
    ArrayList<String> _newArrayList = CollectionLiterals.<String>newArrayList("evt");
    List<List<String>> _w = this.w(((String[])Conversions.unwrapArray(_newArrayList, String.class)));
    this.exercise(code, _w);
  }
  
  @Test
  public void testPrefixFilter() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("import java.awt.*;");
    _builder.newLine();
    _builder.append("public class MyClass {");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("Event evt;");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("Event aevt;");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("void test() {");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("Event evt = a$");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("}");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    final CharSequence code = _builder;
    ArrayList<String> _newArrayList = CollectionLiterals.<String>newArrayList("aevt");
    List<List<String>> _w = this.w(((String[])Conversions.unwrapArray(_newArrayList, String.class)));
    this.exercise(code, _w);
  }
  
  @Test
  public void test012() {
    this.compile(this.FIELDS);
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("public static Fields f = new Fields();");
    _builder.newLine();
    _builder.append("public static void test_protected() {");
    _builder.newLine();
    _builder.append("final Boolean c = $");
    _builder.newLine();
    final CharSequence code = CodeBuilder.classbody(_builder);
    ArrayList<String> _newArrayList = CollectionLiterals.<String>newArrayList("f _public", "f _protected");
    List<List<String>> _w = this.w(((String[])Conversions.unwrapArray(_newArrayList, String.class)));
    this.exercise(code, _w);
  }
  
  @Test
  public void testParameters1() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("ThreadPoolExecutor pool;");
    _builder.newLine();
    _builder.append("public void test() {");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("pool.getKeepAliveTime($);");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    _builder.append("private TimeUnit getTimeUnit() {");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("return TimeUnit.MICROSECONDS;");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    final CharSequence code = CodeBuilder.classbody(_builder);
    ArrayList<String> _newArrayList = CollectionLiterals.<String>newArrayList("getTimeUnit");
    List<List<String>> _w = this.w(((String[])Conversions.unwrapArray(_newArrayList, String.class)));
    this.exercise(code, _w);
  }
  
  @Test
  public void testParameters2() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("ThreadPoolExecutor pool;");
    _builder.newLine();
    _builder.append("public void test() {");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("pool.getKeepAliveTime(g$);");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    _builder.append("private TimeUnit getTimeUnit() {");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("return TimeUnit.MICROSECONDS;");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    final CharSequence code = CodeBuilder.classbody(_builder);
    ArrayList<String> _newArrayList = CollectionLiterals.<String>newArrayList("getTimeUnit");
    List<List<String>> _w = this.w(((String[])Conversions.unwrapArray(_newArrayList, String.class)));
    this.exercise(code, _w);
  }
  
  @Test
  public void testParameters3() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("public void test() {");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("File otherFile = null;");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("URI uri = null;");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("File file = new File($);");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    final CharSequence code = CodeBuilder.classbody(_builder);
    ArrayList<String> _newArrayList = CollectionLiterals.<String>newArrayList("otherFile", "uri");
    List<List<String>> _w = this.w(((String[])Conversions.unwrapArray(_newArrayList, String.class)));
    this.exercise(code, _w);
  }
  
  @Test
  @Ignore(value = "This should only propose otherFile, since with the 2nd String param, only a File is a valid 1st param.")
  public void testParameters4() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("public void test() {");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("File otherFile = null;");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("URI uri = null;");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("File file = new File($,\"\");");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    final CharSequence code = CodeBuilder.classbody(_builder);
    ArrayList<String> _newArrayList = CollectionLiterals.<String>newArrayList("otherFile");
    List<List<String>> _w = this.w(((String[])Conversions.unwrapArray(_newArrayList, String.class)));
    this.exercise(code, _w);
  }
  
  @Test
  public void testParameters5() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("public void test() {");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("Charset cs = null;");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("CharsetDecoder dec = null;");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("InputStream in = null;");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("InputStreamReader reader = new InputStreamReader($,dec);");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    final CharSequence code = CodeBuilder.classbody(_builder);
    ArrayList<String> _newArrayList = CollectionLiterals.<String>newArrayList("in");
    List<List<String>> _w = this.w(((String[])Conversions.unwrapArray(_newArrayList, String.class)));
    this.exercise(code, _w);
  }
  
  @Test
  public void testParameters6() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("public void test() {");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("Charset cs = null;");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("CharsetDecoder dec = null;");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("InputStream in = null;");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("InputStreamReader reader = new InputStreamReader(null,$);");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    final CharSequence code = CodeBuilder.classbody(_builder);
    ArrayList<String> _newArrayList = CollectionLiterals.<String>newArrayList("dec", "cs");
    List<List<String>> _w = this.w(((String[])Conversions.unwrapArray(_newArrayList, String.class)));
    this.exercise(code, _w);
  }
  
  public void exercise(final CharSequence code, final List<? extends List<String>> expected) {
    try {
      String _string = code.toString();
      final Tuple<ICompilationUnit,Set<Integer>> struct = ChainCompletionScenariosTest.fixture.createFileAndParseWithMarkers(_string);
      final ICompilationUnit cu = struct.getFirst();
      Set<Integer> _second = struct.getSecond();
      final Integer completionIndex = IterableExtensions.<Integer>head(_second);
      JavaContentAssistContextMock _javaContentAssistContextMock = new JavaContentAssistContextMock(cu, (completionIndex).intValue());
      final JavaContentAssistContextMock ctx = _javaContentAssistContextMock;
      RecommendersCompletionContextFactoryMock _recommendersCompletionContextFactoryMock = new RecommendersCompletionContextFactoryMock();
      IPreferenceStore _create = ChainPreferenceStoreMock.create();
      TestingChainCompletionProposalComputer _testingChainCompletionProposalComputer = new TestingChainCompletionProposalComputer(_recommendersCompletionContextFactoryMock, _create);
      final TestingChainCompletionProposalComputer sut = _testingChainCompletionProposalComputer;
      sut.sessionStarted();
      IntegerRange _upTo = new IntegerRange(0, 0);
      for (final Integer i : _upTo) {
        sut.computeCompletionProposals(ctx, null);
      }
      final List<ICompletionProposal> proposals = sut.computeCompletionProposals(ctx, null);
      for (final ICompletionProposal proposal : proposals) {
        {
          final List<String> names = ((ChainCompletionProposal) proposal).getChainElementNames();
          StringConcatenation _builder = new StringConcatenation();
          _builder.append("couldn\'t find ");
          _builder.append(names, "");
          _builder.append(" in expected.");
          String _string_1 = _builder.toString();
          boolean _remove = expected.remove(names);
          Assert.assertTrue(_string_1, _remove);
        }
      }
      StringConcatenation _builder = new StringConcatenation();
      _builder.append(" ");
      _builder.append("some expected values were not found ");
      _builder.append(expected, " ");
      _builder.append(" in ");
      _builder.append(proposals, " ");
      _builder.append(" ");
      String _string_1 = _builder.toString();
      boolean _isEmpty = expected.isEmpty();
      Assert.assertTrue(_string_1, _isEmpty);
    } catch (Exception _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  public Tuple<ICompilationUnit,Set<Integer>> compile(final CharSequence code) {
    try {
      Tuple<ICompilationUnit,Set<Integer>> _createFileAndParseWithMarkers = ChainCompletionScenariosTest.fixture.createFileAndParseWithMarkers(code);
      return _createFileAndParseWithMarkers;
    } catch (Exception _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  public List<String> l(final String spaceSeparatedElementNames) {
    final String[] elementNames = StringUtils.split(spaceSeparatedElementNames);
    ArrayList<String> _newArrayList = CollectionLiterals.<String>newArrayList(elementNames);
    return ((List<String>) _newArrayList);
  }
  
  public List<List<String>> w(final String[] chains) {
    final List<List<String>> res = CollectionLiterals.<List<String>>newArrayList();
    for (final String chain : chains) {
      List<String> _l = this.l(chain);
      res.add(_l);
    }
    return ((List<List<String>>) res);
  }
  
  private CharSequence FIELDS = new Function0<CharSequence>() {
    public CharSequence apply() {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("public class Fields {");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("public static \t\t\tBoolean _spublic;");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("protected static \t\tBoolean _sprotected;");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("/* default */ static \tBoolean _sdefault;");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("private static \t\t\tBoolean _sprivate;");
      _builder.newLine();
      _builder.newLine();
      _builder.append("\t");
      _builder.append("public \t\t\tBoolean _public;");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("protected \t\tBoolean _protected;");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("/* default */\tBoolean _default;");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("private \t\tBoolean _private;");
      _builder.newLine();
      _builder.append("}");
      _builder.newLine();
      return _builder;
    }
  }.apply();
}
