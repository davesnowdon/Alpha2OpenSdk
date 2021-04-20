package org.codehaus.jackson.org.objectweb.asm;

public class MethodAdapter implements MethodVisitor {
   protected MethodVisitor mv;

   public MethodAdapter(MethodVisitor var1) {
      this.mv = var1;
   }

   public AnnotationVisitor visitAnnotationDefault() {
      return this.mv.visitAnnotationDefault();
   }

   public AnnotationVisitor visitAnnotation(String var1, boolean var2) {
      return this.mv.visitAnnotation(var1, var2);
   }

   public AnnotationVisitor visitParameterAnnotation(int var1, String var2, boolean var3) {
      return this.mv.visitParameterAnnotation(var1, var2, var3);
   }

   public void visitAttribute(Attribute var1) {
      this.mv.visitAttribute(var1);
   }

   public void visitCode() {
      this.mv.visitCode();
   }

   public void visitFrame(int var1, int var2, Object[] var3, int var4, Object[] var5) {
      this.mv.visitFrame(var1, var2, var3, var4, var5);
   }

   public void visitInsn(int var1) {
      this.mv.visitInsn(var1);
   }

   public void visitIntInsn(int var1, int var2) {
      this.mv.visitIntInsn(var1, var2);
   }

   public void visitVarInsn(int var1, int var2) {
      this.mv.visitVarInsn(var1, var2);
   }

   public void visitTypeInsn(int var1, String var2) {
      this.mv.visitTypeInsn(var1, var2);
   }

   public void visitFieldInsn(int var1, String var2, String var3, String var4) {
      this.mv.visitFieldInsn(var1, var2, var3, var4);
   }

   public void visitMethodInsn(int var1, String var2, String var3, String var4) {
      this.mv.visitMethodInsn(var1, var2, var3, var4);
   }

   public void visitJumpInsn(int var1, Label var2) {
      this.mv.visitJumpInsn(var1, var2);
   }

   public void visitLabel(Label var1) {
      this.mv.visitLabel(var1);
   }

   public void visitLdcInsn(Object var1) {
      this.mv.visitLdcInsn(var1);
   }

   public void visitIincInsn(int var1, int var2) {
      this.mv.visitIincInsn(var1, var2);
   }

   public void visitTableSwitchInsn(int var1, int var2, Label var3, Label[] var4) {
      this.mv.visitTableSwitchInsn(var1, var2, var3, var4);
   }

   public void visitLookupSwitchInsn(Label var1, int[] var2, Label[] var3) {
      this.mv.visitLookupSwitchInsn(var1, var2, var3);
   }

   public void visitMultiANewArrayInsn(String var1, int var2) {
      this.mv.visitMultiANewArrayInsn(var1, var2);
   }

   public void visitTryCatchBlock(Label var1, Label var2, Label var3, String var4) {
      this.mv.visitTryCatchBlock(var1, var2, var3, var4);
   }

   public void visitLocalVariable(String var1, String var2, String var3, Label var4, Label var5, int var6) {
      this.mv.visitLocalVariable(var1, var2, var3, var4, var5, var6);
   }

   public void visitLineNumber(int var1, Label var2) {
      this.mv.visitLineNumber(var1, var2);
   }

   public void visitMaxs(int var1, int var2) {
      this.mv.visitMaxs(var1, var2);
   }

   public void visitEnd() {
      this.mv.visitEnd();
   }
}
