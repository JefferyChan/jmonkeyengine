/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jme3.checker;

import checkers.basetype.BaseTypeChecker;
import checkers.basetype.BaseTypeVisitor;

import checkers.quals.TypeQualifiers;
import checkers.types.AnnotatedTypeMirror;
import checkers.types.AnnotatedTypeMirror.AnnotatedDeclaredType;
import checkers.types.TypeHierarchy;
import checkers.util.AnnotationUtils;
import com.jme3.system.Annotations.Destructive;
import com.jme3.system.Annotations.ReadOnly;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;

/**
 * An annotation processor that checks a program's use of the Jme
 * type annotations ({@code @ReadOnly}, {@code @Destructive}).
 *
 * @checker.framework.manual #jme-checker Jme Checker
 */
@TypeQualifiers( { ReadOnly.class, Destructive.class })
public class JmeChecker extends BaseTypeChecker {

    protected AnnotationMirror READONLY, DESTRUCTIVE;

    /**
     * Initializes the checker: calls init method on super class,
     * creates a local AnnotationFactory based on the processing
     * environment, and uses it to create the protected
     * AnnotationMirrors used through this checker.
     * @param processingEnv the processing environment to use in the local AnnotationFactory
     */
    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        AnnotationUtils annoFactory = AnnotationUtils.getInstance(processingEnv);
        this.READONLY = annoFactory.fromClass(ReadOnly.class);
        this.DESTRUCTIVE = annoFactory.fromClass(Destructive.class);
        super.init(processingEnv);
    }

    /**
     * Implements the {@code @QReadOnly} behavior on generic types,
     * creating a new {@link TypeHierarchy} class that allows a
     * comparison of type arguments to succeed if the left hand side
     * is annotated with {@code @QReadOnly} or if the regular
     * comparison succeeds.
     */
    @Override
    protected TypeHierarchy createTypeHierarchy() {
        return new TypeHierarchy(getQualifierHierarchy()) {
            @Override
            protected boolean isSubtypeAsTypeArgument(AnnotatedTypeMirror rhs, AnnotatedTypeMirror lhs) {
                return lhs.hasAnnotation(READONLY) || super.isSubtypeAsTypeArgument(rhs, lhs);
            }
         };
    }

    /**
     * Checks if one the parameters is primitive, or if a type is
     * subtype of another. Primitive types always pass to avoid issues
     * with boxing.
     */
    @Override
    public boolean isSubtype(AnnotatedTypeMirror sub, AnnotatedTypeMirror sup) {
        return sub.getKind().isPrimitive() || sup.getKind().isPrimitive() || super.isSubtype(sub, sup);
    }


    /**
     * Always true; no type validity checking is made by the BaseTypeVisitor.
     *
     * @see BaseTypeVisitor
     */
    @Override
    public boolean isValidUse(AnnotatedDeclaredType elemType, AnnotatedDeclaredType useType) {
        if(elemType.hasAnnotation(READONLY) && useType.hasAnnotation(DESTRUCTIVE)){
            return false;
        }
        return true;
    }

}
