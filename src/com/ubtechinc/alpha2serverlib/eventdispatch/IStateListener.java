package com.ubtechinc.alpha2serverlib.eventdispatch;

public interface IStateListener {
   void onEventStart(AbstractEvent var1);

   void onEventPause(AbstractEvent var1);

   void onEventResume(AbstractEvent var1);

   void onEventStop(AbstractEvent var1);
}
