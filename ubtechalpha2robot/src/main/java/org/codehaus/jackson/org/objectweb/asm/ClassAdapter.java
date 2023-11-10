package org.codehaus.jackson.org.objectweb.asm;

public class ClassAdapter implements ClassVisitor {
   protected ClassVisitor cv;

   public ClassAdapter(ClassVisitor var1) {
      this.cv = var1;
   }

   public void visit(int var1, int var2, String var3, String var4, String var5, String[] var6) {
      this.cv.visit(var1, var2, var3, var4, var5, var6);
   }

   public void visitSource(String var1, String var2) {
      this.cv.visitSource(var1, var2);
   }

   public void visitOuterClass(String var1, String var2, String var3) {
      this.cv.visitOuterClass(var1, var2, var3);
   }

   public AnnotationVisitor visitAnnotation(String var1, boolean var2) {
      return this.cv.visitAnnotation(var1, var2);
   }

   public void visitAttribute(Attribute var1) {
      this.cv.visitAttribute(var1);
   }

   public void visitInnerClass(String var1, String var2, String var3, int var4) {
      this.cv.visitInnerClass(var1, var2, var3, var4);
   }

   public FieldVisitor visitField(int var1, String var2, String var3, String var4, Object var5) {
      return this.cv.visitField(var1, var2, var3, var4, var5);
   }

   public MethodVisitor visitMethod(int var1, String var2, String var3, String var4, String[] var5) {
      return this.cv.visitMethod(var1, var2, var3, var4, var5);
   }

   public void visitEnd() {
      this.cv.visitEnd();
   }
}
