package com.ubtechinc.alpha2ctrlapp.network.model.request;

class DownloadLogger extends CommonRequest {
   String robotSeq;
   private int tpye;

   DownloadLogger() {
   }

   public String getRobotSeq() {
      return this.robotSeq;
   }

   public void setRobotSeq(String robotSeq) {
      this.robotSeq = robotSeq;
   }

   public int getTpye() {
      return this.tpye;
   }

   public void setTpye(int tpye) {
      this.tpye = tpye;
   }
}
