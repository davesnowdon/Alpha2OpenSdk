package com.ubtech.alpha2.core.model.response;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.ubtech.alpha2.core.base.BaseModel;

@XStreamAlias("item")
public class OrderItem extends BaseModel {
   private static final long serialVersionUID = 1L;
   private Long id;
   @XStreamAsAttribute
   private String name;
   @XStreamAsAttribute
   private String messagetype;
   @XStreamAsAttribute
   private String voicecode;
   @XStreamAsAttribute
   private String focus;

   public String getFocus() {
      return this.focus;
   }

   public void setFocus(String focus) {
      this.focus = focus;
   }

   public OrderItem() {
   }

   public OrderItem(Long id) {
      this.id = id;
   }

   public OrderItem(Long id, String name, String messagetype, String voicecode, String focus) {
      this.id = id;
      this.name = name;
      this.messagetype = messagetype;
      this.voicecode = voicecode;
      this.focus = focus;
   }

   public Long getId() {
      return this.id;
   }

   public void setId(Long id) {
      this.id = id;
   }

   public String getName() {
      return this.name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public String getMessagetype() {
      return this.messagetype;
   }

   public void setMessagetype(String messagetype) {
      this.messagetype = messagetype;
   }

   public String getVoicecode() {
      return this.voicecode;
   }

   public void setVoicecode(String voicecode) {
      this.voicecode = voicecode;
   }

   public String toString() {
      return "<item id =" + this.id + " name=" + this.name + " voicecode=" + this.voicecode + " messagetype=" + this.messagetype + " focus=" + this.focus + " />";
   }
}
