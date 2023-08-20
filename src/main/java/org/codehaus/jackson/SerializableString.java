package org.codehaus.jackson;

public interface SerializableString {
   String getValue();

   int charLength();

   char[] asQuotedChars();

   byte[] asUnquotedUTF8();

   byte[] asQuotedUTF8();
}
