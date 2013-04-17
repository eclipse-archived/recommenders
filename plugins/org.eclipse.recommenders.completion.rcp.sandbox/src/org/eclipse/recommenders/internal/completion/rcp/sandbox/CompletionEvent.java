package org.eclipse.recommenders.internal.completion.rcp.sandbox;

import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.recommenders.utils.names.ITypeName;

public class CompletionEvent {
    public static enum ProposalKind {

        UNKNOWN, ANNOTATION_ATTRIBUTE_REF, ANONYMOUS_CLASS_CONSTRUCTOR_INVOCATION, ANONYMOUS_CLASS_DECLARATION, CONSTRUCTOR_INVOCATION, FIELD_IMPORT, FIELD_REF, FIELD_REF_WITH_CASTED_RECEIVER, JAVADOC_BLOCK_TAG, JAVADOC_FIELD_REF, JAVADOC_INLINE_TAG, JAVADOC_METHOD_REF, JAVADOC_PARAM_REF, JAVADOC_TYPE_REF, JAVADOC_VALUE_REF, KEYWORD, LABEL_REF, LOCAL_VARIABLE_REF, METHOD_DECLARATION, METHOD_IMPORT, METHOD_NAME_REFERENCE, METHOD_REF, METHOD_REF_WITH_CASTED_RECEIVER, PACKAGE_REF, POTENTIAL_METHOD_DECLARATION, TYPE_IMPORT, TYPE_REF, VARIABLE_DECLARATION;

        public static ProposalKind toKind(int proposalKind) {
            switch (proposalKind) {
            case CompletionProposal.ANNOTATION_ATTRIBUTE_REF:
                return ANNOTATION_ATTRIBUTE_REF;
            case CompletionProposal.ANONYMOUS_CLASS_CONSTRUCTOR_INVOCATION:
                return ANONYMOUS_CLASS_CONSTRUCTOR_INVOCATION;
            case CompletionProposal.ANONYMOUS_CLASS_DECLARATION:
                return ANONYMOUS_CLASS_DECLARATION;
            case CompletionProposal.CONSTRUCTOR_INVOCATION:
                return CONSTRUCTOR_INVOCATION;
            case CompletionProposal.FIELD_IMPORT:
                return FIELD_IMPORT;
            case CompletionProposal.FIELD_REF:
                return FIELD_REF;
            case CompletionProposal.FIELD_REF_WITH_CASTED_RECEIVER:
                return FIELD_REF_WITH_CASTED_RECEIVER;
            case CompletionProposal.JAVADOC_BLOCK_TAG:
                return JAVADOC_BLOCK_TAG;
            case CompletionProposal.JAVADOC_FIELD_REF:
                return JAVADOC_FIELD_REF;
            case CompletionProposal.JAVADOC_INLINE_TAG:
                return JAVADOC_INLINE_TAG;
            case CompletionProposal.JAVADOC_METHOD_REF:
                return JAVADOC_METHOD_REF;
            case CompletionProposal.JAVADOC_PARAM_REF:
                return JAVADOC_PARAM_REF;
            case CompletionProposal.JAVADOC_TYPE_REF:
                return JAVADOC_TYPE_REF;
            case CompletionProposal.JAVADOC_VALUE_REF:
                return JAVADOC_VALUE_REF;
            case CompletionProposal.KEYWORD:
                return KEYWORD;
            case CompletionProposal.LABEL_REF:
                return LABEL_REF;
            case CompletionProposal.LOCAL_VARIABLE_REF:
                return LOCAL_VARIABLE_REF;
            case CompletionProposal.METHOD_DECLARATION:
                return METHOD_DECLARATION;
            case CompletionProposal.METHOD_IMPORT:
                return METHOD_IMPORT;
            case CompletionProposal.METHOD_NAME_REFERENCE:
                return METHOD_NAME_REFERENCE;
            case CompletionProposal.METHOD_REF:
                return METHOD_REF;
            case CompletionProposal.METHOD_REF_WITH_CASTED_RECEIVER:
                return METHOD_REF_WITH_CASTED_RECEIVER;
            case CompletionProposal.PACKAGE_REF:
                return PACKAGE_REF;
            case CompletionProposal.POTENTIAL_METHOD_DECLARATION:
                return POTENTIAL_METHOD_DECLARATION;
            case CompletionProposal.TYPE_IMPORT:
                return TYPE_IMPORT;
            case CompletionProposal.TYPE_REF:
                return TYPE_REF;
            case CompletionProposal.VARIABLE_DECLARATION:
                return VARIABLE_DECLARATION;
            default:
                return UNKNOWN;
            }
        }
    }

    public long sessionStarted;
    public long sessionEnded;
    public int numberOfProposals;
    public String completionKind;
    public String completionParentKind;
    public String prefix;
    public String completion;
    public ITypeName receiverType;
    public ProposalKind applied;
    public String error;
}
