package com.ubtechinc.alpha2serverlib.eventdispatch;

public class AlphaEvent extends AbstractEvent {
   public AlphaEvent(IStateListener listener) {
      super(listener);
      this.mID = (int)Math.random() * 1000000;
   }

   public void start() {
      this.getmStateListener().onEventStart(this);
   }

   public void pause() {
      this.getmStateListener().onEventPause(this);
   }

   public void resume() {
      this.getmStateListener().onEventResume(this);
   }

   public void stop() {
      this.getmStateListener().onEventStop(this);
   }
}
