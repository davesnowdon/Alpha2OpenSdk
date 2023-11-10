package org.codehaus.jackson.org.objectweb.asm.signature;

public interface SignatureVisitor {
   char EXTENDS = '+';
   char SUPER = '-';
   char INSTANCEOF = '=';

   void visitFormalTypeParameter(String var1);

   SignatureVisitor visitClassBound();

   SignatureVisitor visitInterfaceBound();

   SignatureVisitor visitSuperclass();

   SignatureVisitor visitInterface();

   SignatureVisitor visitParameterType();

   SignatureVisitor visitReturnType();

   SignatureVisitor visitExceptionType();

   void visitBaseType(char var1);

   void visitTypeVariable(String var1);

   SignatureVisitor visitArrayType();

   void visitClassType(String var1);

   void visitInnerClassType(String var1);

   void visitTypeArgument();

   SignatureVisitor visitTypeArgument(char var1);

   void visitEnd();
}
