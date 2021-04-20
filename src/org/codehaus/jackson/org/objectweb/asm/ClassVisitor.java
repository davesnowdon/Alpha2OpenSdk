package org.codehaus.jackson.org.objectweb.asm;

public interface ClassVisitor {
   void visit(int var1, int var2, String var3, String var4, String var5, String[] var6);

   void visitSource(String var1, String var2);

   void visitOuterClass(String var1, String var2, String var3);

   AnnotationVisitor visitAnnotation(String var1, boolean var2);

   void visitAttribute(Attribute var1);

   void visitInnerClass(String var1, String var2, String var3, int var4);

   FieldVisitor visitField(int var1, String var2, String var3, String var4, Object var5);

   MethodVisitor visitMethod(int var1, String var2, String var3, String var4, String[] var5);

   void visitEnd();
}
