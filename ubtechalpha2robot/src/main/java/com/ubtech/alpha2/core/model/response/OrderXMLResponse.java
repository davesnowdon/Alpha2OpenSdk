package com.ubtech.alpha2.core.model.response;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import com.ubtech.alpha2.core.base.BaseModel;
import java.util.ArrayList;
import java.util.List;

@XStreamAlias("order_xml")
public class OrderXMLResponse extends BaseModel {
   private static final long serialVersionUID = -8167727836628076453L;
   @XStreamImplicit(
      itemFieldName = "item"
   )
   private List<OrderItem> item = new ArrayList();

   public OrderXMLResponse() {
   }

   public List<OrderItem> getItem() {
      return this.item;
   }

   public void setItem(List<OrderItem> item) {
      this.item = item;
   }
}
