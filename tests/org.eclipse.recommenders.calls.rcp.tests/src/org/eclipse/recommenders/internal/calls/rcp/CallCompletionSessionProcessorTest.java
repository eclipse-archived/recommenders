package org.eclipse.recommenders.internal.calls.rcp;

import static java.util.Arrays.asList;
import static org.eclipse.jdt.core.CompletionProposal.METHOD_REF;
import static org.eclipse.recommenders.internal.calls.rcp.CallCompletionContextFunctions.RECEIVER_CALLS;
import static org.eclipse.recommenders.internal.calls.rcp.CallCompletionContextFunctions.RECEIVER_TYPE2;
import static org.eclipse.recommenders.utils.Recommendation.newRecommendation;
import static org.eclipse.recommenders.utils.names.VmTypeName.OBJECT;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.internal.codeassist.complete.CompletionOnMemberAccess;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.recommenders.calls.ICallModel;
import org.eclipse.recommenders.calls.ICallModelProvider;
import org.eclipse.recommenders.completion.rcp.IRecommendersCompletionContext;
import org.eclipse.recommenders.completion.rcp.processable.IProcessableProposal;
import org.eclipse.recommenders.completion.rcp.processable.ProposalProcessorManager;
import org.eclipse.recommenders.completion.rcp.processable.SimpleProposalProcessor;
import org.eclipse.recommenders.models.ProjectCoordinate;
import org.eclipse.recommenders.models.UniqueTypeName;
import org.eclipse.recommenders.models.rcp.IProjectCoordinateProvider;
import org.eclipse.recommenders.rcp.SharedImages;
import org.eclipse.recommenders.utils.Recommendation;
import org.eclipse.recommenders.utils.names.IMethodName;
import org.eclipse.recommenders.utils.names.ITypeName;
import org.eclipse.recommenders.utils.names.VmMethodName;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.base.Optional;

@RunWith(MockitoJUnitRunner.class)
public class CallCompletionSessionProcessorTest {

    private static final ProjectCoordinate JRE = ProjectCoordinate.valueOf("jre:jre:1.7.0");

    private static final IMethodName OBJECT_HASHCODE = VmMethodName.get("Ljava/lang/Object.hashCode()I");
    private static final IMethodName OBJECT_WAIT = VmMethodName.get("Ljava/lang/Object.wait()V");

    private final SharedImages images = new SharedImages();

    @Mock
    private IProjectCoordinateProvider pcProvider;

    @Mock
    private ICallModelProvider modelProvider;

    @Mock
    private IType receiverType;

    private CallsRcpPreferences preferences;

    @Before
    public void setUp() {
        preferences = new CallsRcpPreferences();
        preferences.minProposalProbability = 0;
        preferences.maxNumberOfProposals = Integer.MAX_VALUE;
        preferences.changeProposalRelevance = true;
        preferences.decorateProposalText = true;
        preferences.highlightUsedProposals = true;
    }

    @Test
    public void testObservedMethodsAreDecorated() {
        final CallCompletionSessionProcessor sut = new CallCompletionSessionProcessor(pcProvider, modelProvider,
                preferences, images);

        registerCallModel(JRE, OBJECT, newRecommendation(OBJECT_HASHCODE, 1.0));

        IRecommendersCompletionContext context = mockCompletionContext(receiverType, OBJECT_HASHCODE);

        sut.startSession(context);

        IProcessableProposal proposal = mockMethodRefProposal(OBJECT_HASHCODE);

        ProposalProcessorManager manager = mock(ProposalProcessorManager.class);
        when(proposal.getProposalProcessorManager()).thenReturn(manager);

        sut.process(proposal);

        verify(manager).addProcessor(new SimpleProposalProcessor(1, "used"));
    }

    @Test
    public void testObservedUnrecommendedMethodsAreDecorated() {
        final CallCompletionSessionProcessor sut = new CallCompletionSessionProcessor(pcProvider, modelProvider,
                preferences, images);

        registerCallModel(JRE, OBJECT, newRecommendation(OBJECT_HASHCODE, 1.0));

        IRecommendersCompletionContext context = mockCompletionContext(receiverType, OBJECT_WAIT);

        sut.startSession(context);

        IProcessableProposal proposal = mockMethodRefProposal(OBJECT_WAIT);

        ProposalProcessorManager manager = mock(ProposalProcessorManager.class);
        when(proposal.getProposalProcessorManager()).thenReturn(manager);

        sut.process(proposal);

        verify(manager).addProcessor(new SimpleProposalProcessor(1, "used"));
    }

    private void registerCallModel(ProjectCoordinate pc, ITypeName receiverTypeName,
            Recommendation<IMethodName>... recommendations) {
        final UniqueTypeName name = new UniqueTypeName(pc, receiverTypeName);
        final ICallModel model = mock(ICallModel.class);

        when(pcProvider.toUniqueName(eq(receiverType))).thenReturn(Optional.of(name));
        when(modelProvider.acquireModel(eq(name))).thenReturn(Optional.of(model));
        when(model.recommendCalls()).thenReturn(asList(recommendations));
    }

    private static IRecommendersCompletionContext mockCompletionContext(IType receiverType,
            IMethodName... observedMethods) {
        final IRecommendersCompletionContext context = mock(IRecommendersCompletionContext.class);
        final CompletionOnMemberAccess completionNode = mock(CompletionOnMemberAccess.class);

        when(context.getCompletionNode()).thenReturn(Optional.<ASTNode>of(completionNode));
        when(context.getExpectedTypeSignature()).thenReturn(Optional.<String>absent());
        when(context.get(eq(RECEIVER_TYPE2), any(IType.class))).thenReturn(receiverType);
        when(context.get(eq(RECEIVER_CALLS), anyListOf(IMethodName.class))).thenReturn(asList(observedMethods));

        return context;
    }

    private static IProcessableProposal mockMethodRefProposal(IMethodName method) {
        IProcessableProposal proposal = mock(IProcessableProposal.class);
        CompletionProposal coreProposal = mock(CompletionProposal.class);

        when(proposal.getCoreProposal()).thenReturn(Optional.of(coreProposal));

        when(coreProposal.getKind()).thenReturn(METHOD_REF);
        when(coreProposal.getSignature()).thenReturn(method.getIdentifier().toCharArray());
        when(coreProposal.getName()).thenReturn(method.getName().toCharArray());

        return proposal;
    }
}
