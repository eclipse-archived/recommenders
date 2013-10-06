package org.eclipse.recommenders.completion.rcp.processable;

import static org.apache.commons.lang3.ArrayUtils.toArray;
import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.LinkedList;

import org.eclipse.recommenders.completion.rcp.processable.IProcessableProposal;
import org.eclipse.recommenders.completion.rcp.processable.ProcessableAnonymousTypeCompletionProposal;
import org.eclipse.recommenders.completion.rcp.processable.ProcessableFilledArgumentNamesMethodProposal;
import org.eclipse.recommenders.completion.rcp.processable.ProcessableGetterSetterCompletionProposal;
import org.eclipse.recommenders.completion.rcp.processable.ProcessableJavaCompletionProposal;
import org.eclipse.recommenders.completion.rcp.processable.ProcessableJavaFieldWithCastedReceiverCompletionProposal;
import org.eclipse.recommenders.completion.rcp.processable.ProcessableJavaMethodCompletionProposal;
import org.eclipse.recommenders.completion.rcp.processable.ProcessableLazyGenericTypeProposal;
import org.eclipse.recommenders.completion.rcp.processable.ProcessableLazyJavaTypeCompletionProposal;
import org.eclipse.recommenders.completion.rcp.processable.ProcessableLazyPackageCompletionProposal;
import org.eclipse.recommenders.completion.rcp.processable.ProcessableMethodDeclarationCompletionProposal;
import org.eclipse.recommenders.completion.rcp.processable.ProcessableOverrideCompletionProposal;
import org.eclipse.recommenders.completion.rcp.processable.ProcessableParameterGuessingProposal;
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
        Mockito.doCallRealMethod().when(sut).setTag(Mockito.anyString(), Mockito.anyObject());
        Mockito.doCallRealMethod().when(sut).getTag(Mockito.anyString(), Mockito.anyObject());
        Mockito.doCallRealMethod().when(sut).getTag(Mockito.anyString());
    }

    @Test
    public void testSetTagOptional() throws Exception {
        String expected = "value";
        sut.setTag("key", expected);
        Object actual = sut.getTag("key").get();
        assertSame(expected, actual);
    }

    @Test
    public void testRemoveTag() throws Exception {
        String expected = "value";
        sut.setTag("remove", expected);
        sut.setTag("remove", null);
        assertFalse(sut.getTag("remove").isPresent());
    }

    @Test
    public void testgetTagDefault01() throws Exception {
        String expected = "value";
        sut.setTag("default", expected);
        assertEquals(expected, sut.getTag("default", expected));
    }

    @Test
    public void testgetTagDefault02() throws Exception {
        assertEquals("default", sut.getTag("default-unset", "default"));
    }

}
