package com.ubtechinc.service.protocols;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import java.io.Serializable;
import org.msgpack.annotation.Index;
import org.msgpack.annotation.Message;

@Message
@XStreamAlias("actionprofile_xml")
public class ActionProfileXMLResponse implements Serializable {
   private static final long serialVersionUID = -8167727836628076453L;
   @Index(0)
   @XStreamAsAttribute
   private String directionA;
   @Index(1)
   @XStreamAsAttribute
   private String directionB;
   @Index(2)
   @XStreamAsAttribute
   private String directionC;
   @Index(3)
   @XStreamAsAttribute
   private String directionD;
   @Index(4)
   @XStreamAsAttribute
   private String directionDown;
   @Index(5)
   @XStreamAsAttribute
   private String directionLeft;
   @Index(6)
   @XStreamAsAttribute
   private String directionRight;
   @Index(7)
   @XStreamAsAttribute
   private String directionUp;

   public ActionProfileXMLResponse() {
   }

   public String toString() {
      return "direction_left" + this.directionLeft + " direction_up" + this.directionUp + " direction_right" + this.directionRight + " direction_down" + this.directionDown + " direction_a" + this.directionD + " direction_b" + this.directionB + " direction_c" + this.directionC + " direction_d" + this.directionD + "";
   }

   public String getDirectionA() {
      return this.directionA;
   }

   public void setDirectionA(String directionA) {
      this.directionA = directionA;
   }

   public String getDirectionB() {
      return this.directionB;
   }

   public void setDirectionB(String directionB) {
      this.directionB = directionB;
   }

   public String getDirectionC() {
      return this.directionC;
   }

   public void setDirectionC(String directionC) {
      this.directionC = directionC;
   }

   public String getDirectionD() {
      return this.directionD;
   }

   public void setDirectionD(String directionD) {
      this.directionD = directionD;
   }

   public String getDirectionDown() {
      return this.directionDown;
   }

   public void setDirectionDown(String directionDown) {
      this.directionDown = directionDown;
   }

   public String getDirectionLeft() {
      return this.directionLeft;
   }

   public void setDirectionLeft(String directionLeft) {
      this.directionLeft = directionLeft;
   }

   public String getDirectionRight() {
      return this.directionRight;
   }

   public void setDirectionRight(String directionRight) {
      this.directionRight = directionRight;
   }

   public String getDirectionUp() {
      return this.directionUp;
   }

   public void setDirectionUp(String directionUp) {
      this.directionUp = directionUp;
   }
}
