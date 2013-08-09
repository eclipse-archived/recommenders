package org.eclipse.recommenders.calls.rcp;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.recommenders.internal.calls.rcp.ProposalMatcher;
import org.eclipse.recommenders.utils.names.VmMethodName;
import org.junit.Test;

public class ProposalMatcherTest {

    private ProposalMatcher sut;

    @Test
    public void test() {
        // just to be sure we also fail sometimes ;)
        assertFalse(check("m(I)V", "LC.m()V"));

        //
        assertTrue(check("m()V", "LC.m()V"));
        assertTrue(check("m(Ljava.util.Collection<*>;)V", "LC.m(Ljava/util/Collection;)V"));
        assertTrue(check("m(Ljava.util.Collection<+Ljava.lang.Object;>;)V", "LC.m(Ljava/util/Collection;)V"));
        assertTrue(check("m(Ljava.lang.Object;)V", "LC.m(Ljava/lang/Object;)V"));
        assertTrue(check("m([TT;)V", "LC.m([Ljava/lang/Object;)V"));
        assertTrue(check("m([I)V", "LC.m([I)V"));
        assertTrue(check("m([I)V", "LC.m([I)V"));
        assertTrue(check("m<T:Ljava.lang.Object;>([TT;)[TT;", "LC.m([Ljava/lang/Object;)V"));
    }

    private boolean check(String jSignature, String rSignature) {
        int sep = StringUtils.indexOfAny(jSignature, "<", "(");
        String name = StringUtils.substring(jSignature, 0, sep);
        String sig = StringUtils.substring(jSignature, sep);
        CompletionProposal p = mock(CompletionProposal.class);
        when(p.getName()).thenReturn(name.toCharArray());
        when(p.getSignature()).thenReturn(sig.toCharArray());
        sut = new ProposalMatcher(p);
        return sut.match(VmMethodName.get(rSignature));
    }
}
