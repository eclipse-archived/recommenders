package org.eclipse.recommenders.codegen

import java.util.List
import org.eclipse.xtend.lib.macro.Active
import org.eclipse.xtend.lib.macro.TransformationContext
import org.eclipse.xtend.lib.macro.TransformationParticipant
import org.eclipse.xtend.lib.macro.declaration.MutableFieldDeclaration
import org.slf4j.LoggerFactory

@Active(typeof(LoggerCompilationParticipant))
annotation Slf4jLogger {
}

class LoggerCompilationParticipant implements TransformationParticipant<MutableFieldDeclaration> {

    override doTransform(List<? extends MutableFieldDeclaration> annotatedTargetElements,
        extension TransformationContext context) {
        for (field : annotatedTargetElements) {
            val fieldName = field.declaringType
            val factory = typeof(LoggerFactory).newTypeReference
            field.initializer = [
                '''«toJavaCode(factory)».getLogger(«fieldName.simpleName».class)'''
            ]
        }

    }

}
