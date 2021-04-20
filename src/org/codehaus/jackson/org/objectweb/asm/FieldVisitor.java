package org.codehaus.jackson.org.objectweb.asm;

public interface FieldVisitor {
   AnnotationVisitor visitAnnotation(String var1, boolean var2);

   void visitAttribute(Attribute var1);

   void visitEnd();
}
