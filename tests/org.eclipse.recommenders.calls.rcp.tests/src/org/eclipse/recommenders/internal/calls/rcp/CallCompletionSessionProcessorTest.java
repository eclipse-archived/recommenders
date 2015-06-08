package org.eclipse.recommenders.internal.calls.rcp;

import static com.google.common.base.Optional.fromNullable;
import static org.eclipse.recommenders.internal.calls.rcp.CallCompletionContextFunctions.*;
import static org.eclipse.recommenders.utils.Recommendation.newRecommendation;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.internal.codeassist.complete.CompletionOnJavadocMessageSend;
import org.eclipse.jdt.internal.codeassist.complete.CompletionOnMessageSend;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment;
import org.eclipse.recommenders.calls.ICallModel;
import org.eclipse.recommenders.calls.ICallModelProvider;
import org.eclipse.recommenders.completion.rcp.CompletionContextKey;
import org.eclipse.recommenders.completion.rcp.IProposalNameProvider;
import org.eclipse.recommenders.completion.rcp.IRecommendersCompletionContext;
import org.eclipse.recommenders.coordinates.ProjectCoordinate;
import org.eclipse.recommenders.internal.completion.rcp.ProposalNameProvider;
import org.eclipse.recommenders.models.UniqueTypeName;
import org.eclipse.recommenders.models.rcp.IProjectCoordinateProvider;
import org.eclipse.recommenders.rcp.SharedImages;
import org.eclipse.recommenders.utils.Recommendation;
import org.eclipse.recommenders.utils.Result;
import org.eclipse.recommenders.utils.names.IMethodName;
import org.eclipse.recommenders.utils.names.ITypeName;
import org.eclipse.recommenders.utils.names.VmMethodName;
import org.eclipse.recommenders.utils.names.VmTypeName;
import org.junit.Test;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

@SuppressWarnings("restriction")
public class CallCompletionSessionProcessorTest {

    private static final ITypeName OBJECT = VmTypeName.get("Ljava/lang/Object");
    private static final IMethodName OBJECT_HASH_CODE = VmMethodName.get("Ljava/lang/Object.hashCode()I");
    private static final IMethodName OBJECT_EQUALS = VmMethodName.get("Ljava/lang/Object.equals(Ljava/lang/Object;)Z");
    private static final IMethodName OBJECT_VOID = VmMethodName.get("Ljava/lang/Object.wait()V");
    private static final ProjectCoordinate JRE_1_6_0 = new ProjectCoordinate("jre", "jre", "1.6.0");

    private static final String ANY_TYPE_SIGNATURE = null;
    private static final List<IMethodName> NO_OBSERVATIONS = Collections.emptyList();
    private static final List<Recommendation<IMethodName>> NO_RECOMMENDATIONS = Collections.emptyList();

    private static final IType OBJECT_TYPE = mock(IType.class);

    private IProjectCoordinateProvider pcProvider;
    private ICallModelProvider modelProvider;
    private IProposalNameProvider proposalNameProvider = new ProposalNameProvider();
    private IRecommendersCompletionContext context;

    @Test
    public void testCompletionOnObjectWithoutRecommendations() {
        setUp(CompletionOnMessageSend.class, new UniqueTypeName(JRE_1_6_0, OBJECT), OBJECT_TYPE, NO_OBSERVATIONS, "",
                NO_RECOMMENDATIONS);

        CallCompletionSessionProcessor sut = new CallCompletionSessionProcessor(pcProvider, modelProvider,
                proposalNameProvider, createDefaultPreferences(), new SharedImages());

        boolean shouldProcess = sut.startSession(context);

        assertThat(shouldProcess, is(equalTo(false)));
    }

    @Test
    public void testCompletionOnObjectWithRecommendations() {
        setUp(CompletionOnMessageSend.class, new UniqueTypeName(JRE_1_6_0, OBJECT), OBJECT_TYPE, NO_OBSERVATIONS, "",
                ImmutableList.of(newRecommendation(OBJECT_EQUALS, 0.5)));

        CallsRcpPreferences pref = createDefaultPreferences();
        CallCompletionSessionProcessor sut = new CallCompletionSessionProcessor(pcProvider, modelProvider,
                proposalNameProvider, pref, new SharedImages());

        boolean shouldProcess = sut.startSession(context);

        assertThat(shouldProcess, is(equalTo(true)));
    }

    @Test
    public void testCompletionOnObjectWith100PercentMinProposalProbability() {
        setUp(CompletionOnMessageSend.class, new UniqueTypeName(JRE_1_6_0, OBJECT), OBJECT_TYPE, NO_OBSERVATIONS, "",
                ImmutableList.of(newRecommendation(OBJECT_HASH_CODE, 0.5)));

        CallsRcpPreferences pref = createPreferencesWithMinimalProposalProbability(100);
        CallCompletionSessionProcessor sut = new CallCompletionSessionProcessor(pcProvider, modelProvider,
                proposalNameProvider, pref, new SharedImages());

        boolean shouldProcess = sut.startSession(context);

        assertThat(shouldProcess, is(equalTo(false)));
    }

    @Test
    public void testCompletionOnObjectWithObservedMethods() {
        setUp(CompletionOnMessageSend.class, new UniqueTypeName(JRE_1_6_0, OBJECT), OBJECT_TYPE,
                ImmutableList.of(OBJECT_HASH_CODE), "", NO_RECOMMENDATIONS);

        CallCompletionSessionProcessor sut = new CallCompletionSessionProcessor(pcProvider, modelProvider,
                proposalNameProvider, createDefaultPreferences(), new SharedImages());

        boolean shouldProcess = sut.startSession(context);

        assertThat(shouldProcess, is(equalTo(true)));
    }

    @Test
    public void testCannotGetCompletionNode() {
        setUp(null, new UniqueTypeName(JRE_1_6_0, OBJECT), OBJECT_TYPE, NO_OBSERVATIONS, ANY_TYPE_SIGNATURE,
                ImmutableList.of(newRecommendation(OBJECT_HASH_CODE, 0.5)));

        CallCompletionSessionProcessor sut = new CallCompletionSessionProcessor(pcProvider, modelProvider,
                proposalNameProvider, createDefaultPreferences(), new SharedImages());

        boolean shouldProcess = sut.startSession(context);

        assertThat(shouldProcess, is(equalTo(false)));
    }

    @Test
    public void testUnsupportedCompletionRequest() {
        setUp(CompletionOnJavadocMessageSend.class, new UniqueTypeName(JRE_1_6_0, OBJECT), OBJECT_TYPE, NO_OBSERVATIONS,
                ANY_TYPE_SIGNATURE, ImmutableList.of(newRecommendation(OBJECT_HASH_CODE, 0.5)));

        CallCompletionSessionProcessor sut = new CallCompletionSessionProcessor(pcProvider, modelProvider,
                proposalNameProvider, createDefaultPreferences(), new SharedImages());

        boolean shouldProcess = sut.startSession(context);

        assertThat(shouldProcess, is(equalTo(false)));
    }

    @Test
    public void testCannotDetermineReceiverType() {
        setUp(CompletionOnMessageSend.class, new UniqueTypeName(JRE_1_6_0, OBJECT), null,
                ImmutableList.of(OBJECT_HASH_CODE), "", ImmutableList.of(newRecommendation(OBJECT_EQUALS, 0.5)));

        CallCompletionSessionProcessor sut = new CallCompletionSessionProcessor(pcProvider, modelProvider,
                proposalNameProvider, createDefaultPreferences(), new SharedImages());

        boolean shouldProcess = sut.startSession(context);

        assertThat(shouldProcess, is(equalTo(false)));
    }

    @Test
    public void testCannotObtainUniqueNameForReceiverType() {
        setUp(CompletionOnMessageSend.class, null, OBJECT_TYPE, ImmutableList.of(OBJECT_HASH_CODE), "",
                ImmutableList.of(newRecommendation(OBJECT_EQUALS, 0.5)));

        CallCompletionSessionProcessor sut = new CallCompletionSessionProcessor(pcProvider, modelProvider,
                proposalNameProvider, createDefaultPreferences(), new SharedImages());

        boolean shouldProcess = sut.startSession(context);

        assertThat(shouldProcess, is(equalTo(false)));
    }

    @Test
    public void testCannotAcquireModel() {
        setUp(CompletionOnMessageSend.class, new UniqueTypeName(JRE_1_6_0, OBJECT), OBJECT_TYPE,
                ImmutableList.of(OBJECT_HASH_CODE), "", null);

        CallCompletionSessionProcessor sut = new CallCompletionSessionProcessor(pcProvider, modelProvider,
                proposalNameProvider, createDefaultPreferences(), new SharedImages());

        boolean shouldProcess = sut.startSession(context);

        assertThat(shouldProcess, is(equalTo(false)));
    }

    @Test
    public void testCompletionOnObjectWithNullExpectedTypeSignature() {
        setUp(CompletionOnMessageSend.class, new UniqueTypeName(JRE_1_6_0, OBJECT), OBJECT_TYPE, NO_OBSERVATIONS,
                ANY_TYPE_SIGNATURE, ImmutableList.of(newRecommendation(OBJECT_HASH_CODE, 0.5)));

        CallCompletionSessionProcessor sut = new CallCompletionSessionProcessor(pcProvider, modelProvider,
                proposalNameProvider, createDefaultPreferences(), new SharedImages());

        boolean shouldProcess = sut.startSession(context);

        assertThat(shouldProcess, is(equalTo(true)));
    }

    @Test
    public void testExpectedTypeSignatureWithVoidReturnRecommendation() {
        setUp(CompletionOnMessageSend.class, new UniqueTypeName(JRE_1_6_0, OBJECT), OBJECT_TYPE, NO_OBSERVATIONS, "",
                ImmutableList.of(newRecommendation(OBJECT_VOID, 0.5)));

        CallCompletionSessionProcessor sut = new CallCompletionSessionProcessor(pcProvider, modelProvider,
                proposalNameProvider, createDefaultPreferences(), new SharedImages());

        boolean shouldProcess = sut.startSession(context);

        assertThat(shouldProcess, is(equalTo(false)));
    }

    private CallsRcpPreferences createDefaultPreferences() {
        return createPreferences(0, 7);
    }

    private CallsRcpPreferences createPreferencesWithMinimalProposalProbability(int minProposalProbability) {
        return createPreferences(minProposalProbability, 7);
    }

    private CallsRcpPreferences createPreferences(int minProposalProbability, int maxNumberOfProposals) {
        CallsRcpPreferences pref = new CallsRcpPreferences();
        pref.maxNumberOfProposals = maxNumberOfProposals;
        pref.minProposalPercentage = minProposalProbability;
        return pref;
    }

    private void setUp(Class<? extends ASTNode> completionType, UniqueTypeName uniqueTypeName, IType receiverType,
            List<IMethodName> observedCalls, String expectedTypeSignature,
            List<Recommendation<IMethodName>> recommendations) {
        LookupEnvironment lookupEnvironment = mock(LookupEnvironment.class);
        context = mock(IRecommendersCompletionContext.class);
        when(context.get(CompletionContextKey.LOOKUP_ENVIRONMENT)).thenReturn(Optional.of(lookupEnvironment));
        Optional<ASTNode> completionNode = completionType == null ? Optional.<ASTNode>absent()
                : Optional.<ASTNode>of(mock(completionType));
        when(context.getCompletionNode()).thenReturn(completionNode);
        when(context.getExpectedTypeSignature()).thenReturn(
                expectedTypeSignature == null ? Optional.<String>absent() : Optional.<String>of(expectedTypeSignature));
        when(context.get(RECEIVER_TYPE2, null)).thenReturn(receiverType);
        when(context.get(eq(RECEIVER_CALLS), anyListOf(IMethodName.class))).thenReturn(observedCalls);

        pcProvider = mock(IProjectCoordinateProvider.class);
        when(pcProvider.tryToUniqueName(receiverType)).thenReturn(Result.fromNullable(uniqueTypeName));

        modelProvider = mock(ICallModelProvider.class);
        ICallModel model;
        if (recommendations == null) {
            model = null;
        } else {
            model = mock(ICallModel.class);
            when(model.recommendCalls()).thenReturn(recommendations);
        }

        if (uniqueTypeName != null) {
            when(modelProvider.acquireModel(uniqueTypeName)).thenReturn(fromNullable(model));
        }
    }
}
