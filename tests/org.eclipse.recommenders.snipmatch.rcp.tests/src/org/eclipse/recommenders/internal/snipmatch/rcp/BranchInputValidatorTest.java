package org.eclipse.recommenders.internal.snipmatch.rcp;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class BranchInputValidatorTest {

    private final BranchInputValidator sut = new BranchInputValidator();

    @Test
    public void testNormalPrefix() {
        assertThat(sut.isValid("refs/heads"), is(nullValue()));
    }

    @Test
    public void testGerritPrefix() {
        assertThat(sut.isValid("refs/for"), is(nullValue()));
    }

    @Test
    public void testNumbers() {
        assertThat(sut.isValid("refs/for2"), is(nullValue()));
    }

    @Test
    public void testSpecialChar() {
        assertThat(sut.isValid("refs*for"), is(notNullValue()));
    }

    @Test
    public void testTrailingSlash() {
        assertThat(sut.isValid("refs/for/"), is(notNullValue()));
    }

    @Test
    public void testLeadingSlash() {
        assertThat(sut.isValid("/refs/for"), is(notNullValue()));
    }

    @Test
    public void testMultipleSlashes() {
        assertThat(sut.isValid("refs//for"), is(notNullValue()));
    }

    @Test
    public void testDash() {
        assertThat(sut.isValid("refs/for-something"), is(nullValue()));
    }

    @Test
    public void testTrailingDash() {
        assertThat(sut.isValid("refs/for-"), is(notNullValue()));
    }

    @Test
    public void testLeadingDash() {
        assertThat(sut.isValid("-refs/for"), is(notNullValue()));
    }

    @Test
    public void testSlashAndDash() {
        assertThat(sut.isValid("refs/-for"), is(notNullValue()));
    }
}
