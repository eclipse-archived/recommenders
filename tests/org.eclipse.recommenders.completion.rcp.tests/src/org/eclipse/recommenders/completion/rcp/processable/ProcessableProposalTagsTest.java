package org.eclipse.recommenders.completion.rcp.processable;

import static org.apache.commons.lang3.ArrayUtils.toArray;
import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.LinkedList;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.mockito.Mockito;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

@RunWith(Parameterized.class)
public class ProcessableProposalTagsTest {

    private Class<IProcessableProposal> clazz;
    private IProcessableProposal sut;

    public ProcessableProposalTagsTest(Class<IProcessableProposal> clazz) {
        this.clazz = clazz;
    }

    @SuppressWarnings("unchecked")
    @Parameters(name = "{0}")
    public static Collection<Object[]> fieldDeclarations() {
        LinkedList<Object[]> classes = Lists.newLinkedList();
        classes.add(toArray(ProcessableAnonymousTypeCompletionProposal.class));
        classes.add(toArray(ProcessableFilledArgumentNamesMethodProposal.class));
        classes.add(toArray(ProcessableJavaCompletionProposal.class));
        classes.add(toArray(ProcessableGetterSetterCompletionProposal.class));
        classes.add(toArray(ProcessableJavaFieldWithCastedReceiverCompletionProposal.class));
        classes.add(toArray(ProcessableJavaMethodCompletionProposal.class));
        classes.add(toArray(ProcessableLazyGenericTypeProposal.class));
        classes.add(toArray(ProcessableLazyJavaTypeCompletionProposal.class));
        classes.add(toArray(ProcessableLazyPackageCompletionProposal.class));
        classes.add(toArray(ProcessableMethodDeclarationCompletionProposal.class));
        classes.add(toArray(ProcessableOverrideCompletionProposal.class));
        classes.add(toArray(ProcessableParameterGuessingProposal.class));
        return classes;
    }

    @Before
    public void before() throws Exception {
        sut = Mockito.mock(clazz);
        Field f = clazz.getDeclaredField("tags");
        f.setAccessible(true);
        f.set(sut, Maps.newHashMap());
        Mockito.doCallRealMethod().when(sut).setTag(Mockito.any(IProposalTag.class), Mockito.anyObject());
        Mockito.doCallRealMethod().when(sut).getTag(Mockito.any(IProposalTag.class), Mockito.anyObject());
        Mockito.doCallRealMethod().when(sut).getTag(Mockito.any(IProposalTag.class));
    }

    @Test
    public void testSetTagOptional() throws Exception {
        String expected = "value";
        sut.setTag(TestTag.KEY, expected);
        Object actual = sut.getTag(TestTag.KEY).get();
        assertSame(expected, actual);
    }

    @Test
    public void testRemoveTag() throws Exception {
        String expected = "value";
        sut.setTag(TestTag.REMOVE, expected);
        sut.setTag(TestTag.REMOVE, null);
        assertFalse(sut.getTag(TestTag.REMOVE).isPresent());
    }

    @Test
    public void testgetTagDefault01() throws Exception {
        String expected = "value";
        sut.setTag(TestTag.DEFAULT, expected);
        assertEquals(expected, sut.getTag(TestTag.DEFAULT, expected));
    }

    @Test
    public void testgetTagDefault02() throws Exception {
        assertEquals("default", sut.getTag(TestTag.DEFAULT_UNSET, "default"));
    }

    private static enum TestTag implements IProposalTag {
        KEY,
        REMOVE,
        DEFAULT,
        DEFAULT_UNSET
    }

}
