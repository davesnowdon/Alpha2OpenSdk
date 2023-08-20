# Alpha2Sdk

# Decompile official SDK

Decompile with Fernflower: https://github.com/JetBrains/intellij-community/blob/0e2aa4030ee763c9b0c828f0b5119f4cdcc66f35/plugins/java-decompiler/engine/README.md which ships with JetBrains IDE.

```
java -cp java-decompiler.jar org.jetbrains.java.decompiler.main.decompiler.ConsoleDecompiler -hdc=0 -dgs=1 -rsy=1 -lit=1 ubtechalpha2robot.jar source/
```