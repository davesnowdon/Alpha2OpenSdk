package com.ubtech.alpha2.core.model.response;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.ubtech.alpha2.core.base.BaseModel;

@XStreamAlias("item")
public class BinVersionItem extends BaseModel {
   private static final long serialVersionUID = 6823528118579547018L;
   @XStreamAsAttribute
   private String tag;
   @XStreamAsAttribute
   private String version;
   @XStreamAsAttribute
   private String url;
   @XStreamAsAttribute
   private String info;

   public BinVersionItem() {
   }

   public String getTag() {
      return this.tag;
   }

   public void setTag(String tag) {
      this.tag = tag;
   }

   public String getVersion() {
      return this.version;
   }

   public void setVersion(String version) {
      this.version = version;
   }

   public String getUrl() {
      return this.url;
   }

   public void setUrl(String url) {
      this.url = url;
   }

   public String getInfo() {
      return this.info;
   }

   public void setInfo(String info) {
      this.info = info;
   }
}
