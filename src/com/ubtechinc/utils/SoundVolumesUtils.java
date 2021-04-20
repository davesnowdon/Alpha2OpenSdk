package com.ubtechinc.utils;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.Log;
import com.example.alpha2entritylib.R.raw;
import java.util.HashMap;

public class SoundVolumesUtils {
   private static SoundVolumesUtils _instance;
   private String TAG = "SoundVolumesUtils";
   private Context mContext;
   private AudioManager mAudioManager;
   private HashMap<Integer, Integer> mSoundPoolMap;
   private SoundPool mSoundPool;

   public static synchronized SoundVolumesUtils getInstance(Context mContext) {
      if (_instance == null) {
         _instance = new SoundVolumesUtils(mContext);
      }

      return _instance;
   }

   public SoundVolumesUtils(Context mContext) {
      this.mContext = mContext;
      this.mAudioManager = (AudioManager)mContext.getSystemService("audio");
      this.mSoundPool = new SoundPool(4, 3, 0);
      this.mSoundPoolMap = new HashMap();
      this.loadSounds();
   }

   public void loadSounds() {
      this.mSoundPoolMap.put(1, this.mSoundPool.load(this.mContext, raw.media_volume, 1));
      this.mSoundPoolMap.put(2, this.mSoundPool.load(this.mContext, raw.wavefail, 1));
      this.mSoundPoolMap.put(3, this.mSoundPool.load(this.mContext, raw.qrcode, 1));
   }

   public void addVolume(int value) {
      int currentVolume = this.mAudioManager.getStreamVolume(3);
      int maxVolume = this.mAudioManager.getStreamMaxVolume(3);
      currentVolume += value;
      currentVolume = currentVolume < maxVolume ? currentVolume : maxVolume;
      this.mAudioManager.setStreamVolume(3, currentVolume, 0);
      Log.i(this.TAG, "currentVolume" + currentVolume);
      this.playSound();
   }

   public void mulVolume(int value) {
      int currentVolume = this.mAudioManager.getStreamVolume(3);
      currentVolume -= value;
      currentVolume = 0 < currentVolume ? currentVolume : 0;
      this.mAudioManager.setStreamVolume(3, currentVolume, 0);
      Log.i(this.TAG, "currentVolume" + currentVolume);
      this.playSound();
   }

   public void playSound() {
      this.mSoundPool.play((Integer)this.mSoundPoolMap.get(1), 1.0F, 1.0F, 1, 0, 1.0F);
   }

   public void playSound(int i) {
      this.mSoundPool.play((Integer)this.mSoundPoolMap.get(i), 1.0F, 1.0F, 1, 0, 1.0F);
   }
}
