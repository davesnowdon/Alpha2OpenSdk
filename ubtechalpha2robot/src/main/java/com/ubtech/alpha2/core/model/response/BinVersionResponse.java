package com.ubtech.alpha2.core.model.response;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import com.ubtech.alpha2.core.base.BaseModel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@XStreamAlias("binversion_xml")
public class BinVersionResponse extends BaseModel {
   private static final long serialVersionUID = -8167727836628076453L;
   @XStreamImplicit(
      itemFieldName = "item"
   )
   private List<BinVersionItem> item = new ArrayList();

   public BinVersionResponse() {
   }

   public List<BinVersionItem> getItem() {
      return this.item;
   }

   public void setItem(List<BinVersionItem> item) {
      this.item = item;
   }

   public BinVersionItem getItemByteTag(String tag) {
      Iterator var2 = this.item.iterator();

      BinVersionItem binVersionItem;
      do {
         if (!var2.hasNext()) {
            return null;
         }

         binVersionItem = (BinVersionItem)var2.next();
      } while(!binVersionItem.getTag().equals(tag));

      return binVersionItem;
   }
}
