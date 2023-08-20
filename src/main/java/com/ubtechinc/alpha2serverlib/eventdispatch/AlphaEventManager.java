package com.ubtechinc.alpha2serverlib.eventdispatch;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Queue;

public class AlphaEventManager implements IStateListener {
   private static final int STATE_PREPARE = 0;
   private static final int STATE_START = 1;
   private static final int STATE_PAUSE = 2;
   private static final int STATE_RESUME = 3;
   private static final int STATE_STOP = 4;
   public static final String HARDWARE_APP_EVENT = "com.ubtechinc.hardware.eventmsg";
   public static final String BTACTION = "com.alpha.ubtech.btevent";
   private static final String TAG = "AlphaEventManager";
   private Context mContext;
   private Handler mEventHandler;
   private Queue<AlphaEvent> mEventQueue;
   private static AlphaEventManager sAlphaEventManager;
   private AlphaEvent mWorkingEvent;
   private Comparator<AlphaEvent> OrderIsdn = new Comparator<AlphaEvent>() {
      public int compare(AlphaEvent a1, AlphaEvent a2) {
         int priorityA = a1.getmPriority();
         int priorityB = a2.getmPriority();
         if (priorityB > priorityA) {
            return 1;
         } else {
            return priorityB < priorityA ? -1 : 0;
         }
      }
   };

   private AlphaEventManager(Context context, Handler handler) {
      this.mContext = context;
      this.mEventQueue = new PriorityQueue(11, this.OrderIsdn);
      this.mWorkingEvent = null;
      this.mEventHandler = handler;
   }

   public static AlphaEventManager getAlphaEventManagerInstance(Context context, Handler handler) {
      if (sAlphaEventManager == null) {
         sAlphaEventManager = new AlphaEventManager(context, handler);
      }

      return sAlphaEventManager;
   }

   public void parseIntent(Context context, Intent intent) {
      String action = intent.getAction();
      AlphaEvent event = new AlphaEvent(this);
      event.setmState(AbstractEvent.EventState.STATE_PREPARE);
      if (action.equals("android.net.wifi.STATE_CHANGE")) {
         event.setmIndex(0);
         event.setmPriority(3);
         event.setmNeedCheck(true);
      } else if (action.equals("com.ubtechinc.services.speechcmd")) {
         event.setmIndex(1);
         event.setmPriority(3);
         event.setmNeedCheck(true);
      } else if (action.equals("com.ubtechinc.services.chest")) {
         event.setmIndex(2);
         event.setmPriority(3);
         event.setmNeedCheck(false);
      } else if (action.equals("com.ubtechinc.services.header")) {
         event.setmIndex(3);
         event.setmPriority(3);
         event.setmNeedCheck(false);
      } else if (action.equals("com.ubtechinc.services.baseaction")) {
         event.setmIndex(4);
         event.setmPriority(3);
         event.setmNeedCheck(false);
      } else if ("com.ubtechinc.services.phone".equals(action)) {
         event.setmIndex(5);
         event.setmPriority(3);
         event.setmNeedCheck(false);
      } else if (action.equals("com.ubtechinc.update.chess")) {
         event.setmIndex(6);
         event.setmPriority(3);
         event.setmNeedCheck(false);
      } else if (action.equals("com.ubtechinc.udate.header")) {
         event.setmIndex(7);
         event.setmPriority(3);
         event.setmNeedCheck(false);
      } else if ("com.ubtechinc.services.bluetooth".equals(action)) {
         event.setmIndex(8);
         event.setmPriority(3);
         event.setmNeedCheck(false);
      } else if ("com.ubt.alpha2.bt.alarm".equals(action)) {
         event.setmIndex(9);
         event.setmPriority(3);
         event.setmNeedCheck(false);
      } else if ("com.ubtechinc.services.thirdparty.playaction".equals(action)) {
         event.setmIndex(10);
         event.setmPriority(3);
         event.setmNeedCheck(true);
      } else if ("com.ubt.alpha2.app.manager".equals(action)) {
         event.setmIndex(11);
         event.setmPriority(3);
         event.setmNeedCheck(false);
      } else if ("com.ubt.alpha2.digital.decode".equals(action)) {
         event.setmIndex(12);
         event.setmPriority(3);
         event.setmNeedCheck(false);
      } else if ("com.ubtechinc.services.speech.thirdparty.tts".equals(action)) {
         event.setmIndex(13);
         event.setmPriority(3);
         event.setmNeedCheck(true);
      } else if ("com.ubtechinc.services.verifycationcode".equals(action)) {
         event.setmIndex(14);
         event.setmPriority(3);
         event.setmNeedCheck(true);
      } else if ("com.ubtechinc.services.deskclock.wakeup".equals(action)) {
         event.setmIndex(15);
         event.setmPriority(3);
         event.setmNeedCheck(true);
      } else if ("com.ubtechinc.services.action.play".equals(action)) {
         event.setmIndex(16);
         event.setmPriority(3);
         event.setmNeedCheck(true);
      } else if ("AlphaCooeeServiceBussiness".equals(action)) {
         event.setmIndex(17);
         event.setmPriority(3);
         event.setmNeedCheck(false);
      } else if ("com.ubt.alpha2.cooee.decode".equals(action)) {
         event.setmIndex(18);
         event.setmPriority(3);
         event.setmNeedCheck(false);
      } else if ("com.ubt.alpha2.wifiresult".equals(action)) {
         event.setmIndex(19);
         event.setmPriority(3);
         event.setmNeedCheck(true);
      } else if ("com.alpha2.hardware.test".equals(action)) {
         event.setmIndex(20);
         event.setmPriority(3);
         event.setmNeedCheck(false);
      } else if ("com.ubtechinc.hardware.eventmsg".equals(action)) {
         event.setmIndex(21);
         event.setmPriority(3);
         event.setmNeedCheck(false);
      } else if ("com.ubtechinc.robot.tts_hint_wakeup".equals(action)) {
         Log.d("AlphaEventManager", "ALPHA_TTS_HINT " + intent.getExtras().getString("hint_event"));
         event.setmIndex(22);
         event.setmPriority(3);
         event.setmNeedCheck(true);
      } else if ("com.ubtechinc.services.bluetooth.open".equals(action)) {
         event.setmIndex(23);
         event.setmPriority(3);
         event.setmNeedCheck(false);
      } else if ("com.alpha.ubtech.btevent".equals(action)) {
         event.setmIndex(24);
         event.setmPriority(3);
         event.setmNeedCheck(false);
      } else if ("com.ubtechinc.services.LED_ACTION".equals(action)) {
         event.setmIndex(25);
         event.setmPriority(3);
         event.setmNeedCheck(false);
      } else if ("com.ubtechinc.services.SET_RTC_TIME".equals(action)) {
         event.setmIndex(26);
         event.setmPriority(3);
         event.setmNeedCheck(false);
      } else if ("com.ubtechinc.services.POWER_SAVE".equals(action)) {
         event.setmIndex(28);
         event.setmPriority(3);
         event.setmNeedCheck(false);
      } else if ("com.ubtechinc.services.SET_CHARGE_PLAY".equals(action)) {
         event.setmIndex(29);
         event.setmPriority(3);
         event.setmNeedCheck(false);
      } else if ("com.ubtechinc.services.NUANCE_OFFLINE_CMD".equals(action)) {
         event.setmIndex(30);
         event.setmPriority(3);
         event.setmNeedCheck(false);
      }

      event.setmObj(intent);
      if (event.ismNeedCheck()) {
         Log.d("AlphaEventManager", "STATE_PREPARE event.index=" + event.getmIndex());
         this.mEventQueue.add(event);
         this.process((AbstractEvent)null, 0);
      } else {
         this.sendMessage(event);
      }

   }

   public void processEvent(AlphaEvent event) {
      if (event.ismNeedCheck()) {
         Log.d("AlphaEventManager", "STATE_PREPARE event.index=" + event.getmIndex());
         this.mEventQueue.add(event);
         this.process((AbstractEvent)null, 0);
      } else {
         this.sendMessage(event);
      }

   }

   private void sendMessage(AlphaEvent event) {
      Message msg = this.mEventHandler.obtainMessage();
      msg.what = event.getmIndex();
      msg.obj = event;
      this.mEventHandler.sendMessage(msg);
   }

   private void process(AbstractEvent event, int state) {
      Log.d("AlphaEventManager", " mEventQueue.length=" + this.mEventQueue.size() + " State = " + state);
      AlphaEvent nextEvent;
      switch(state) {
      case 0:
         nextEvent = (AlphaEvent)this.mEventQueue.peek();
         if (nextEvent != null) {
            if (this.mWorkingEvent == null) {
               nextEvent = (AlphaEvent)this.mEventQueue.poll();
               this.mWorkingEvent = nextEvent;
               this.sendMessage(this.mWorkingEvent);
            } else {
               Log.d("AlphaEventManager", " mWorkingEvent.index=" + this.mWorkingEvent.getmIndex());
               if (nextEvent.getmPriority() >= this.mWorkingEvent.getmPriority()) {
                  if (this.mWorkingEvent.ismCanPause()) {
                     this.mWorkingEvent.pause();
                  } else {
                     this.mWorkingEvent.stop();
                  }
               }
            }
         }
      case 1:
      case 3:
      default:
         break;
      case 2:
         if (this.mEventQueue.size() > 0) {
            if (event == null || event.getmID() == this.mWorkingEvent.getmID()) {
               nextEvent = (AlphaEvent)this.mEventQueue.peek();
               if (nextEvent != null) {
                  nextEvent = (AlphaEvent)this.mEventQueue.poll();
                  this.mWorkingEvent = nextEvent;
                  this.sendMessage(this.mWorkingEvent);
               }
            }
         } else {
            this.mWorkingEvent = null;
         }
         break;
      case 4:
         if (this.mEventQueue.size() > 0) {
            if (event == null || event.getmID() == this.mWorkingEvent.getmID()) {
               nextEvent = (AlphaEvent)this.mEventQueue.peek();
               if (nextEvent != null) {
                  nextEvent = (AlphaEvent)this.mEventQueue.poll();
                  this.mWorkingEvent = nextEvent;
                  this.sendMessage(this.mWorkingEvent);
               } else {
                  this.mWorkingEvent = null;
               }
            }
         } else {
            this.mWorkingEvent = null;
         }
      }

   }

   public void onEventStart(AbstractEvent event) {
   }

   public void onEventPause(AbstractEvent event) {
      this.process(event, 2);
   }

   public void onEventResume(AbstractEvent event) {
   }

   public void onEventStop(AbstractEvent event) {
      Log.d("AlphaEventManager", " onEventStop event.index=" + event.getmIndex());
      this.process(event, 4);
   }
}
