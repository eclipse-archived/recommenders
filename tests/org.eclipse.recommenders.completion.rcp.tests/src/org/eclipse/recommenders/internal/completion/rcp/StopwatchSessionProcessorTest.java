package org.eclipse.recommenders.internal.completion.rcp;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;

import org.eclipse.recommenders.completion.rcp.processable.IProcessableProposal;
import org.eclipse.recommenders.completion.rcp.processable.SessionProcessor;
import org.eclipse.recommenders.completion.rcp.processable.StopwatchSessionProcessor;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class StopwatchSessionProcessorTest {

    @Test
    public void test() throws Exception {
        IProcessableProposal proposal = Mockito.mock(IProcessableProposal.class);
        SessionProcessor delegate = Mockito.mock(SessionProcessor.class);
        doAnswer(new DelayedAnswer()).when(delegate).process(any(IProcessableProposal.class));

        StopwatchSessionProcessor sut = new StopwatchSessionProcessor(delegate);
        assertThat(sut.elapsed(), is(0L));
        sut.startSession(null);

        sut.process(proposal);
        assertThat(sut.elapsed(), greaterThan(4L));
        sut.process(proposal);
        assertThat(sut.elapsed(), greaterThan(8L));
        sut.process(proposal);
        assertThat(sut.elapsed(), greaterThan(12L));
    }

    private static final class DelayedAnswer implements Answer<Void> {

        @Override
        public Void answer(InvocationOnMock invocation) throws Throwable {
            Thread.sleep(5);
            return null;
        }
    }
}
