package com.ubtechinc.service.protocols;

import org.msgpack.annotation.Index;
import org.msgpack.annotation.Message;

@Message
public class AllMotoAngle {
   @Index(0)
   int m_nAngle1;
   @Index(1)
   int m_nAngle2;
   @Index(2)
   int m_nAngle3;
   @Index(3)
   int m_nAngle4;
   @Index(4)
   int m_nAngle5;
   @Index(5)
   int m_nAngle6;
   @Index(6)
   int m_nAngle7;
   @Index(7)
   int m_nAngle8;
   @Index(8)
   int m_nAngle9;
   @Index(9)
   int m_nAngle10;
   @Index(10)
   int m_nAngle11;
   @Index(11)
   int m_nAngle12;
   @Index(12)
   int m_nAngle13;
   @Index(13)
   int m_nAngle14;
   @Index(14)
   int m_nAngle15;
   @Index(15)
   int m_nAngle16;
   @Index(16)
   int m_nAngle17;
   @Index(17)
   int m_nAngle18;
   @Index(18)
   int m_nAngle19;
   @Index(19)
   int m_nAngle20;
   @Index(20)
   int m_nTime;

   public AllMotoAngle() {
   }

   public int getM_nTime() {
      return this.m_nTime;
   }

   public void setM_nTime(int m_nTime) {
      this.m_nTime = m_nTime;
   }

   public int[] getAngle() {
      int[] angle = new int[]{this.m_nAngle1, this.m_nAngle2, this.m_nAngle3, this.m_nAngle4, this.m_nAngle5, this.m_nAngle6, this.m_nAngle7, this.m_nAngle8, this.m_nAngle9, this.m_nAngle10, this.m_nAngle11, this.m_nAngle12, this.m_nAngle13, this.m_nAngle14, this.m_nAngle15, this.m_nAngle16, this.m_nAngle17, this.m_nAngle18, this.m_nAngle19, this.m_nAngle20};
      return angle;
   }
}
