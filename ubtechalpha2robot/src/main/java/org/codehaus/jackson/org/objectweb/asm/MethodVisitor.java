package org.codehaus.jackson.org.objectweb.asm;

public interface MethodVisitor {
   AnnotationVisitor visitAnnotationDefault();

   AnnotationVisitor visitAnnotation(String var1, boolean var2);

   AnnotationVisitor visitParameterAnnotation(int var1, String var2, boolean var3);

   void visitAttribute(Attribute var1);

   void visitCode();

   void visitFrame(int var1, int var2, Object[] var3, int var4, Object[] var5);

   void visitInsn(int var1);

   void visitIntInsn(int var1, int var2);

   void visitVarInsn(int var1, int var2);

   void visitTypeInsn(int var1, String var2);

   void visitFieldInsn(int var1, String var2, String var3, String var4);

   void visitMethodInsn(int var1, String var2, String var3, String var4);

   void visitJumpInsn(int var1, Label var2);

   void visitLabel(Label var1);

   void visitLdcInsn(Object var1);

   void visitIincInsn(int var1, int var2);

   void visitTableSwitchInsn(int var1, int var2, Label var3, Label[] var4);

   void visitLookupSwitchInsn(Label var1, int[] var2, Label[] var3);

   void visitMultiANewArrayInsn(String var1, int var2);

   void visitTryCatchBlock(Label var1, Label var2, Label var3, String var4);

   void visitLocalVariable(String var1, String var2, String var3, Label var4, Label var5, int var6);

   void visitLineNumber(int var1, Label var2);

   void visitMaxs(int var1, int var2);

   void visitEnd();
}
