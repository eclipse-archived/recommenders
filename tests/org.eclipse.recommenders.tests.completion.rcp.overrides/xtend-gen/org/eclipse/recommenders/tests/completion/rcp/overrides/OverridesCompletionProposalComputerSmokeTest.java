package org.eclipse.recommenders.tests.completion.rcp.overrides;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.recommenders.internal.completion.rcp.overrides.InstantOverridesRecommender;
import org.eclipse.recommenders.internal.completion.rcp.overrides.OverridesCompletionProposalComputer;
import org.eclipse.recommenders.tests.completion.rcp.JavaContentAssistContextMock;
import org.eclipse.recommenders.tests.completion.rcp.overrides.MockRecommender;
import org.eclipse.recommenders.tests.completion.rcp.overrides.RecommendersCompletionContextFactoryMock;
import org.eclipse.recommenders.tests.jdt.JavaProjectFixture;
import org.eclipse.recommenders.utils.Tuple;
import org.eclipse.recommenders.utils.rcp.JavaElementResolver;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.junit.Test;

@SuppressWarnings("all")
public class OverridesCompletionProposalComputerSmokeTest {
  @Test
  public void test01() {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("public class MyClass extends Object{");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("$");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("@Override$");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("pu$blic$ bool$ean eq$uals(O$bject $o$){");
      _builder.newLine();
      _builder.append("\t\t");
      _builder.append("int $i = $o$.$ha$shCode$();");
      _builder.newLine();
      _builder.append("\t\t");
      _builder.append("retur$n$ $f$alse;");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("$}$");
      _builder.newLine();
      _builder.append("$}$");
      _builder.newLine();
      final CharSequence code = _builder;
      this.exercise(code);
  }
  
  @Test
  public void test02() {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("publ$ic cl$ass MyCla$ss{");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("$");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("@Override$");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("pu$blic$ bool$ean eq$uals(O$bject $o$){");
      _builder.newLine();
      _builder.append("\t\t");
      _builder.append("int $i = $o$.$ha$shCode$();");
      _builder.newLine();
      _builder.append("\t\t");
      _builder.append("retur$n$ $f$alse;");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("$}$");
      _builder.newLine();
      _builder.append("$}$");
      _builder.newLine();
      final CharSequence code = _builder;
      this.exercise(code);
  }
  
  public void exercise(final CharSequence code) {
    try {
      {
        IWorkspace _workspace = ResourcesPlugin.getWorkspace();
        JavaProjectFixture _javaProjectFixture = new JavaProjectFixture(_workspace, "test");
        final JavaProjectFixture fixture = _javaProjectFixture;
        String _string = code.toString();
        Tuple<ICompilationUnit,Set<Integer>> _createFileAndParseWithMarkers = fixture.createFileAndParseWithMarkers(_string);
        final Tuple<ICompilationUnit,Set<Integer>> struct = _createFileAndParseWithMarkers;
        ICompilationUnit _first = struct.getFirst();
        final ICompilationUnit cu = _first;
        Set<Integer> _second = struct.getSecond();
        for (final Integer completionIndex : _second) {
          {
            JavaContentAssistContextMock _javaContentAssistContextMock = new JavaContentAssistContextMock(cu, (completionIndex).intValue());
            final JavaContentAssistContextMock ctx = _javaContentAssistContextMock;
            JavaElementResolver _javaElementResolver = new JavaElementResolver();
            final JavaElementResolver resolver = _javaElementResolver;
            InstantOverridesRecommender _get = MockRecommender.get();
            final InstantOverridesRecommender recommender = _get;
            RecommendersCompletionContextFactoryMock _recommendersCompletionContextFactoryMock = new RecommendersCompletionContextFactoryMock();
            OverridesCompletionProposalComputer _overridesCompletionProposalComputer = new OverridesCompletionProposalComputer(recommender, _recommendersCompletionContextFactoryMock, resolver);
            final OverridesCompletionProposalComputer sut = _overridesCompletionProposalComputer;
            sut.sessionStarted();
            sut.computeCompletionProposals(ctx, null);
          }
        }
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
