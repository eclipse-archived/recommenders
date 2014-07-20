package org.eclipse.recommenders.internal.snipmatch.rcp.editors;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class SnippetSourceValidatorTest {

    @Test
    public void testEmptySnippetIsValid() {
        assertThat(SnippetSourceValidator.isSourceValid("").isEmpty(), is(true));
    }

    @Test
    public void testSimpleSnippet() {
        assertThat(SnippetSourceValidator.isSourceValid("Lorem ipsum dolor").isEmpty(), is(true));
    }

    @Test
    public void testSnippetWithId() {
        assertThat(SnippetSourceValidator.isSourceValid("${id}").isEmpty(), is(true));
    }

    @Test
    public void testSnippetWithVarReference() {
        assertThat(SnippetSourceValidator.isSourceValid("${id:var(java.lang.String)}").isEmpty(), is(true));
    }

    @Test
    public void testSnippetWithEscapedDollarSign() {
        assertThat(SnippetSourceValidator.isSourceValid("$$").isEmpty(), is(true));
    }

    @Test
    public void testSnippetWithSingleDollarSign() {
        assertThat(SnippetSourceValidator.isSourceValid("$").isEmpty(), is(false));
    }

    @Test
    public void testSnippetWithUnclosedVarReference() {
        assertThat(SnippetSourceValidator.isSourceValid("${id:var(java.lang.String)").isEmpty(), is(false));
    }
}
