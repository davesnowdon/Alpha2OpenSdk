package org.codehaus.jackson.map.ser;

public abstract class FilterProvider {
   public FilterProvider() {
   }

   public abstract BeanPropertyFilter findFilter(Object var1);
}
