package org.eclipse.recommenders.codegen

import java.util.List
import org.eclipse.xtend.lib.macro.Active
import org.eclipse.xtend.lib.macro.TransformationContext
import org.eclipse.xtend.lib.macro.TransformationParticipant
import org.eclipse.xtend.lib.macro.declaration.MutableClassDeclaration
import org.eclipse.xtend.lib.macro.declaration.Visibility
import org.eclipse.xtend.lib.macro.declaration.MutableFieldDeclaration

@Active(typeof(SystemPropertiesCompilationParticipant))
annotation SystemProperties {
}

class SystemPropertiesCompilationParticipant implements TransformationParticipant<MutableClassDeclaration> {
    override doTransform(List<? extends MutableClassDeclaration> classes, extension TransformationContext context) {
        for (clazz : classes) {
            for (field : clazz.declaredFields) {

                // make sure it is static:
                field.static = true
                field.final = true
                val fieldName = field.simpleName
                val fieldType = field.type

                // generate field name constant for reference in code
                val propertyName = clazz.addField("P_" + fieldName.toUpperCase) [
                    visibility = Visibility.PUBLIC
                    static = true
                    final = true
                    type = typeof(String).newTypeReference
                    initializer = ['''"«fieldName.replace('_', '.').toLowerCase»"''']
                ]
                generateStaticGetterAndSetter(clazz, field, propertyName)
            }
        }
    }

    def generateStaticGetterAndSetter(MutableClassDeclaration clazz, MutableFieldDeclaration field,
        MutableFieldDeclaration propertyField) {

        val fieldName = field.simpleName
        val fieldType = field.type

        // generate getters:
        clazz.addMethod('' + fieldName.toFirstUpper) [
            returnType = fieldType
            static = true
            body = [
                switch fieldType.simpleName {
                    case "int": {
                        '''return Integer.getInteger(«propertyField.simpleName», «field.simpleName»);'''
                    }
                    case "boolean": {
                        '''
                        String value = System.getProperty(«propertyField.simpleName», Boolean.toString(«field.
                            simpleName»));
                        return Boolean.valueOf(value);'''
                    }
                    default: {
                        '''return System.getProperty(«propertyField.simpleName», «field.simpleName»);'''
                    }
                }
            ]
        ]

        //        // generate setters:
        //        if (!field.final) {
        //            clazz.addMethod('set' + fieldName.toFirstUpper) [
        //                addParameter(fieldName, fieldType)
        //                body = [
        //                    switch fieldType {
        //                        case int: {
        //                        }
        //                    }
        //                    '''
        //                        «fieldType» _oldValue = this.«fieldName»;
        //                        this.«fieldName» = «fieldName»;
        //                        _propertyChangeSupport.firePropertyChange("«fieldName»", _oldValue, 
        //                          «fieldName»);
        //                    ''']
        //            ]
        //        }
        }

    }
    