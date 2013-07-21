package org.eclipse.recommenders.codegen

import java.beans.PropertyChangeListener
import java.beans.PropertyChangeSupport
import java.util.List
import org.eclipse.xtend.lib.macro.Active
import org.eclipse.xtend.lib.macro.TransformationContext
import org.eclipse.xtend.lib.macro.TransformationParticipant
import org.eclipse.xtend.lib.macro.declaration.MutableClassDeclaration
import org.eclipse.xtend.lib.macro.declaration.Visibility

@Active(typeof(ObservableCompilationParticipant))
annotation Observable {
}

class ObservableCompilationParticipant implements TransformationParticipant<MutableClassDeclaration> {

    override doTransform(List<? extends MutableClassDeclaration> classes, extension TransformationContext context) {
        for (clazz : classes) {

            for (field : clazz.declaredFields) {
                val fieldName = field.simpleName
                val fieldType = field.type

                // generate field name constant for reference in code
                clazz.addField(fieldName.toUpperCase) [
                    visibility = Visibility.PUBLIC
                    static = true
                    final = true
                    type = typeof(String).newTypeReference
                    initializer = ['"fieldName"']
                ]

                // generate getters:
                clazz.addMethod('get' + fieldName.toFirstUpper) [
                    returnType = fieldType
                    body = ['''return this.«fieldName»;''']
                ]

                // generate setters:
                if (!field.final) {
                    clazz.addMethod('set' + fieldName.toFirstUpper) [
                        addParameter(fieldName, fieldType)
                        body = [
                            '''
                                «fieldType» _oldValue = this.«fieldName»;
                                this.«fieldName» = «fieldName»;
                                _propertyChangeSupport.firePropertyChange("«fieldName»", _oldValue, 
                                  «fieldName»);
                            ''']
                    ]
                }
            }

            // add propertyChangeSupport
            val changeSupportType = typeof(PropertyChangeSupport).newTypeReference
            clazz.addField("_propertyChangeSupport") [
                type = changeSupportType
                initializer = ['''new «toJavaCode(changeSupportType)»(this)''']
            ]

            // add method 'addPropertyChangeListener'
            clazz.addMethod("addPropertyChangeListener") [
                addParameter("l", typeof(PropertyChangeListener).newTypeReference)
                body = [
                    '''
                        _propertyChangeSupport.addPropertyChangeListener(l);
                    ''']
            ]
            clazz.addMethod("addPropertyChangeListener") [
                addParameter("p", typeof(String).newTypeReference)
                addParameter("l", typeof(PropertyChangeListener).newTypeReference)
                body = [
                    '''
                        _propertyChangeSupport.addPropertyChangeListener(p, l);
                    ''']
            ]

            // add method 'removePropertyChangeListener'
            clazz.addMethod("removePropertyChangeListener") [
                addParameter("l", typeof(PropertyChangeListener).newTypeReference)
                body = [
                    '''
                        _propertyChangeSupport.removePropertyChangeListener(l);
                    ''']
            ]
            clazz.addMethod("removePropertyChangeListener") [
                addParameter("p", typeof(String).newTypeReference)
                addParameter("l", typeof(PropertyChangeListener).newTypeReference)
                body = [
                    '''
                        _propertyChangeSupport.removePropertyChangeListener(p, l);
                    ''']
            ]
        }
    }
}
