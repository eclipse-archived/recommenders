package org.eclipse.recommenders.tests.completion.rcp.overrides;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import junit.framework.Assert;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.recommenders.completion.rcp.IntelligentCompletionContextResolver;
import org.eclipse.recommenders.internal.completion.rcp.overrides.InstantOverridesRecommender;
import org.eclipse.recommenders.internal.completion.rcp.overrides.OverridesCompletionProposalComputer;
import org.eclipse.recommenders.tests.completion.rcp.overrides.MockRecommender;
import org.eclipse.recommenders.tests.jdt.JavaProjectFixture;
import org.eclipse.recommenders.tests.jdt.TestJavaContentAssistContext;
import org.eclipse.recommenders.utils.Tuple;
import org.eclipse.recommenders.utils.rcp.JavaElementResolver;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Conversions;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.junit.Test;

@SuppressWarnings("all")
public class CompletionScenarios {
  @Test
  public void testFindLocalAnchor() {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("import java.util.concurrent.*;");
      _builder.newLine();
      _builder.append("public class MyClass extends Object{");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("public int hashCode(){return 0;}");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("$");
      _builder.newLine();
      _builder.append("}");
      _builder.newLine();
      final CharSequence code = _builder;
      ArrayList<String> _newArrayList = CollectionLiterals.<String>newArrayList("equals");
      List<List<String>> _w = this.w(((String[])Conversions.unwrapArray(_newArrayList, String.class)));
      List<List<String>> expected = _w;
      this.exercise(code, expected);
  }
  
  public void exercise(final CharSequence code, final List<? extends List<String>> expected) {
    try {
      {
        IWorkspace _workspace = ResourcesPlugin.getWorkspace();
        JavaProjectFixture _javaProjectFixture = new JavaProjectFixture(_workspace, "test");
        final JavaProjectFixture fixture = _javaProjectFixture;
        String _string = code.toString();
        Tuple<ICompilationUnit,Set<Integer>> _createFileAndParseWithMarkers = fixture.createFileAndParseWithMarkers(_string, "MyClass.java");
        final Tuple<ICompilationUnit,Set<Integer>> struct = _createFileAndParseWithMarkers;
        ICompilationUnit _first = struct.getFirst();
        final ICompilationUnit cu = _first;
        Set<Integer> _second = struct.getSecond();
        Integer _head = IterableExtensions.<Integer>head(_second);
        final Integer completionIndex = _head;
        TestJavaContentAssistContext _testJavaContentAssistContext = new TestJavaContentAssistContext(cu, (completionIndex).intValue());
        final TestJavaContentAssistContext ctx = _testJavaContentAssistContext;
        JavaElementResolver _javaElementResolver = new JavaElementResolver();
        final JavaElementResolver resolver = _javaElementResolver;
        InstantOverridesRecommender _get = MockRecommender.get();
        final InstantOverridesRecommender recommender = _get;
        IntelligentCompletionContextResolver _intelligentCompletionContextResolver = new IntelligentCompletionContextResolver(resolver);
        OverridesCompletionProposalComputer _overridesCompletionProposalComputer = new OverridesCompletionProposalComputer(recommender, _intelligentCompletionContextResolver, resolver);
        final OverridesCompletionProposalComputer sut = _overridesCompletionProposalComputer;
        sut.sessionStarted();
        List _computeCompletionProposals = sut.computeCompletionProposals(ctx, null);
        final List proposals = _computeCompletionProposals;
        for (final Object proposal : proposals) {
        }
        StringConcatenation _builder = new StringConcatenation();
        _builder.append(" ");
        _builder.append("some expected values were not found ");
        _builder.append(expected, " ");
        _builder.append(" ");
        String _string_1 = _builder.toString();
        boolean _isEmpty = expected.isEmpty();
        Assert.assertTrue(_string_1, _isEmpty);
      }
    } catch (Exception _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  public List<String> l(final String spaceSeparatedElementNames) {
      String[] _split = StringUtils.split(spaceSeparatedElementNames);
      final String[] elementNames = _split;
      ArrayList<String> _newArrayList = CollectionLiterals.<String>newArrayList(elementNames);
      return ((List<String>) _newArrayList);
  }
  
  public List<List<String>> w(final String[] chains) {
      ArrayList<List<String>> _newArrayList = CollectionLiterals.<List<String>>newArrayList();
      final List<List<String>> res = _newArrayList;
      for (final String chain : chains) {
        List<String> _l = this.l(chain);
        res.add(_l);
      }
      return ((List<List<String>>) res);
  }
}
