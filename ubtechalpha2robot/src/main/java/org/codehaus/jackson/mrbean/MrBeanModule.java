package org.codehaus.jackson.mrbean;

import org.codehaus.jackson.Version;
import org.codehaus.jackson.map.Module;

public class MrBeanModule extends Module {
   private final String NAME;
   private static final Version VERSION = new Version(1, 8, 0, (String)null);
   protected AbstractTypeMaterializer _materializer;

   public MrBeanModule() {
      this(new AbstractTypeMaterializer());
   }

   public MrBeanModule(AbstractTypeMaterializer materializer) {
      this.NAME = "MrBeanModule";
      this._materializer = materializer;
   }

   public String getModuleName() {
      return "MrBeanModule";
   }

   public Version version() {
      return VERSION;
   }

   public void setupModule(Module.SetupContext context) {
      context.addAbstractTypeResolver(this._materializer);
   }
}
