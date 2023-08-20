package org.msgpack.template.builder.beans;

public interface PropertyEditor {
   void setAsText(String var1) throws IllegalArgumentException;

   String[] getTags();

   String getJavaInitializationString();

   String getAsText();

   void setValue(Object var1);

   Object getValue();

   void removePropertyChangeListener(PropertyChangeListener var1);

   void addPropertyChangeListener(PropertyChangeListener var1);

   boolean supportsCustomEditor();

   boolean isPaintable();
}
