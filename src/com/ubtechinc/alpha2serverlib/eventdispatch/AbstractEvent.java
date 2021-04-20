package com.ubtechinc.alpha2serverlib.eventdispatch;

public abstract class AbstractEvent {
   private int mPriority;
   private AbstractEvent.EventType mType;
   private AbstractEvent.EventState mState;
   private int mIndex;
   protected int mID;
   protected boolean mCanPause = true;
   protected boolean mNeedCheck;
   private IStateListener mStateListener;
   private Object mObj;

   public AbstractEvent(IStateListener listener) {
      this.mStateListener = listener;
   }

   public int getmPriority() {
      return this.mPriority;
   }

   public void setmPriority(int mPriority) {
      this.mPriority = mPriority;
   }

   public AbstractEvent.EventState getmState() {
      return this.mState;
   }

   public void setmState(AbstractEvent.EventState mState) {
      this.mState = mState;
   }

   public int getmID() {
      return this.mID;
   }

   public void setmID(int mID) {
      this.mID = mID;
   }

   public IStateListener getmStateListener() {
      return this.mStateListener;
   }

   public void setmStateListener(IStateListener mStateListener) {
      this.mStateListener = mStateListener;
   }

   public abstract void start();

   public abstract void pause();

   public abstract void resume();

   public abstract void stop();

   public int getmIndex() {
      return this.mIndex;
   }

   public void setmIndex(int mIndex) {
      this.mIndex = mIndex;
   }

   public boolean ismCanPause() {
      return this.mCanPause;
   }

   public void setmCanPause(boolean mCanPause) {
      this.mCanPause = mCanPause;
   }

   public Object getmObj() {
      return this.mObj;
   }

   public void setmObj(Object mObj) {
      this.mObj = mObj;
   }

   public boolean ismNeedCheck() {
      return this.mNeedCheck;
   }

   public void setmNeedCheck(boolean mNeedCheck) {
      this.mNeedCheck = mNeedCheck;
   }

   public AbstractEvent.EventType getmType() {
      return this.mType;
   }

   public void setmType(AbstractEvent.EventType mType) {
      this.mType = mType;
   }

   public static enum EventState {
      STATE_PREPARE,
      STATE_START,
      STATE_BLOCK,
      STATE_PAUSE,
      STATE_RESUME,
      STATE_STOP;

      private EventState() {
      }
   }

   public static enum EventType {
      EVENT_TYPE_TTS,
      EVENT_TYPE_ACTION,
      EVENT_TYPE_TTS_ACTION,
      EVENT_TYPE_CHEST_UPDATE,
      EVENT_TYPE_HEADER_UPDATE;

      private EventType() {
      }
   }

   public class EventPriority {
      public static final int PRIORITY_MAX = 5;
      public static final int PRIORITY_HIGH = 4;
      public static final int PRIORITY_NORMAL = 3;
      public static final int PRIORITY_LOW = 2;
      public static final int PRIORITY_MIN = 1;

      public EventPriority() {
      }
   }
}
