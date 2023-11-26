# Alpha2OpenSdk

## Decompile official SDK

Decompile with Fernflower: <https://github.com/JetBrains/intellij-community/blob/0e2aa4030ee763c9b0c828f0b5119f4cdcc66f35/plugins/java-decompiler/engine/README.md> which ships with JetBrains IDE.

```bash
java -cp java-decompiler.jar org.jetbrains.java.decompiler.main.decompiler.ConsoleDecompiler -hdc=0 -dgs=1 -rsy=1 -lit=1 ubtechalpha2robot.jar source/
```

## Included Open Source Code

The official SDK appears to include code for CodeHaus Jackson 1.8.3 and msgpack 0.6.11. It's not clear why these were embedded in the jar and  weren't referenced as dependencies. For now I've replaced the decompiled binaries for these libraries with the original source and made a few hacks so it builds. The decompiled source would not build "as is" either so I have my doubts whether this code was build for android or whether it is actually required by the Alpha 2 SDK. For now the aim to to get the SDK to a usable state and once this is done I will look to remove the other code embedded in it.