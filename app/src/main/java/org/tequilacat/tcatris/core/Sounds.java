package org.tequilacat.tcatris.core;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;

import org.tequilacat.tcatris.R;

/**
 * Manages sounds available to games
 */
public class Sounds {
  public enum Id {
    MOVEMENT, SQUEEZE;

    private int _soundId;

    public int getSoundId() {
      return _soundId;
    }

    public void setSoundId(int soundId) {
      _soundId = soundId;
    }
  }

  private SoundPool _soundPool;

  public Sounds(Context context) {
    _soundPool =  new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
    Id.MOVEMENT.setSoundId(_soundPool.load(context, R.raw.click_sound, 1));
    Id.SQUEEZE.setSoundId(_soundPool.load(context, R.raw.single_pop, 1));
  }

  public void play(Id soundId) {
    _soundPool.play(soundId.getSoundId(), 1f, 1f, 0, 0, 1f);
  }
}
