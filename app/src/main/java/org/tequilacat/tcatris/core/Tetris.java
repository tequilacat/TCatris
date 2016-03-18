package org.tequilacat.tcatris.core;

import android.content.SharedPreferences;
import android.graphics.Canvas;

import java.util.EnumSet;

/**
 * implements game logic
 */
public abstract class Tetris {

  private boolean _prefEnableSound;
  private boolean _prefShowDropTarget;

  public enum CellState {
    FALLING, SQUEEZED, SETTLED
  }

  //private boolean myShowNext;
  private int myState = NOTINIT;
  public static final int NOTINIT = 0;
  public static final int ACTIVE = 1;
  public static final int LOST = 3;
  long seed = System.currentTimeMillis();
  private boolean myCanSqueeze = false;

  protected int myLastScored;

  private int myFieldWidth;
  private int myFieldHeight;

  private int _level;
  private int _score;

  private GameScreenLayout _gameScreenLayout;
  private GameDescriptor _descriptor;

  /**************************************************
   **************************************************/
  public Tetris(GameDescriptor descriptor) {
    //String gameLabel, String gameDescriptor
    // byte[] gameData, String gameLabel, int fWidth, int fHeight, int nextWidth, int nextHeight){
    // example:
    // 5:13:
    // 5:13:specificparams
    _descriptor = descriptor;
    String gameParams = descriptor.getGameParameters();

    int lastSep = gameParams.lastIndexOf(':');
    int whSep = gameParams.indexOf(':');

    myFieldWidth = Integer.parseInt(gameParams.substring(0, whSep));
    myFieldHeight = Integer.parseInt(gameParams.substring(whSep+1, lastSep));

    configure(gameParams.substring(lastSep + 1));
  }

  public GameDescriptor getDescriptor() {
    return _descriptor;
  }

  public void initGame() {
    myState = 0;
    myCanSqueeze = false;
    myLastScored = 0;
    _allowedImpulses.clear();

    init();
    internalThrowInNewShape();
  }


  public int getLevel() {
    return _level;
  }

  protected void setLevel(int level) {
    _level = level;
  }

  public int getScore() {
    return _score;
  }

  protected void setScore(int score) {
    _score = score;
  }

  /**
   * @return width of game field in cells
   */
  public final int getWidth() {
    return myFieldWidth;
  }

  /**
   * @return height of game field in cells
   */
  public final int getHeight() {
    return myFieldHeight;
  }

  private synchronized int nextRandom(int i) {
    seed = seed * 0x5deece66dL + 11L & 0xffffffffffffL;
    return (int) (seed >>> 48 - i);
  }

  public int getRandomInt(int i) {
//        if(i <= 0)
//            throw new IllegalArgumentException("n must be positive");
    if ((i & -i) == i)
      return (int) ((long) i * (long) nextRandom(31) >> 31);
    int j;
    int k;
    do {
      j = nextRandom(31);
      k = j % i;
    } while ((j - k) + (i - 1) < 0);
    return k;
  }

  /**
   * Calls implementation of acquireFallenShape() and refreshes available impulses
   * @return result of called acquireFallenShape()
   */
  private boolean internalAcquireFallenShape() {
    boolean canContinue = acquireFallenShape();

    if(canContinue) {
      checkEffectiveImpulses();
    }

    return canContinue;
  }

  /**
   * calls implementation of throwInNewShape,
   * updates game state and available impulses
   */
  private void internalThrowInNewShape() {
    myState = throwInNewShape() ? ACTIVE : LOST;

    if(myState == ACTIVE) {
      checkEffectiveImpulses();
    }
  }

  /**************************************************
   **************************************************/
  public int getLastScored() {
    return myLastScored;
  }

  public final int getState() {
    return myState;
  }

  /**
   * remaining specs of the gamedef line.
   * default implementation does nothing.
   * @param specSettings
   */
  protected void configure(String specSettings) {}

  public abstract void layout(LayoutParameters layoutParams);

  /**
   * returns game impulse for specified axis and direction
   * @param axis
   * @param positiveDirection used as sign along the axis (>=0 or <0)
   * @return impulse for the game
   */
  public abstract GameImpulse getAxisImpulse(DragAxis axis, boolean positiveDirection);

  /**
   * defines game-specific sensitivity along specified axis
   * @param axis
   * @return
   */
  public DragSensitivity getAxisSensitivity(DragAxis axis) {
    DragSensitivity sensitivity;

    switch (axis){
      case ROTATE: sensitivity = DragSensitivity.ROTATE; break;
      case HORIZONTAL: sensitivity = DragSensitivity.MOVE; break;
      default: sensitivity = null;break;
    }

    return sensitivity;
  }

  /**
   * Adjusts game settings from preferences instance.
   * Can be called anytime during lifecycle of a game, at least once before main game cycle.
   * @param preferences
   */
  public void initSettings(SharedPreferences preferences) {
    _prefEnableSound = preferences.getBoolean("pref_sound_enable", false);
    _prefShowDropTarget = preferences.getBoolean("pref_show_droptarget", false);
  }

  public abstract void paintNext(Canvas g);

  public abstract void init();

  protected abstract boolean computeCanSqueeze();

  protected abstract boolean dropCurrent(boolean tillBottom);

  /**
   * copies fallen shape into field
   * @return if all of shape cells are within field (and game can continue)
   */
  protected abstract boolean acquireFallenShape();

  /**
   *
   * @return whether this squeeze leads to next possible squeezes
   */
  protected abstract boolean squeeze();

  protected abstract boolean throwInNewShape();

  /**
   * paints game field
   * @param g canvas to draw to
   * @param dynamicState props of current move state
   */
  public abstract void paintField(Canvas g, DynamicState dynamicState);

  public boolean canSqueeze() {
    return myCanSqueeze;
  }

  /**
   * add flash or vibration here when some cells are collapsed
   * @return if squeeze succeeded
   */
  private boolean internalSqueeze() {
    //#ifdef Platform_MIDP20
    //TetrisCanvas.display.vibrate(100);
    //#endif

    return squeeze();
  }

  private EnumSet<GameImpulse> _allowedImpulses = EnumSet.noneOf(GameImpulse.class);

  /**
   *
   * @return whether given impulse is understood by the game type
   * and whether it's allowed by field configuration in current state
   * @param impulse action affecting game state
   */
  public boolean isEffective(GameImpulse impulse) {
    return _allowedImpulses.contains(impulse);
  }

  /**
   *
   * @return impulses supported by this kind of game
   */
  public abstract EnumSet<GameImpulse> getSupportedImpulses();

  /**
   * Called after modification of field.
   * Queries implementation for allowed impulses in new configuration
   */
  protected void checkEffectiveImpulses() {
    _allowedImpulses.clear();
    addEffectiveImpulses(_allowedImpulses);
    //Debug.print(">>> checkEffectiveImpulses: " + _allowedImpulses);
  }

  private boolean internalDropCurrent(boolean toBottom) {
    boolean moved = dropCurrent(toBottom);

    if(moved) {
      checkEffectiveImpulses();
    }

    return moved;
  }

  /**
   * Implementors must return all impulses
   * @return list of
   */
  public abstract void addEffectiveImpulses(EnumSet<GameImpulse> actionSet);

  /**
   * tries to change game state by issuing impulse affecting it.
   * @param impulse
   * @return whether the state has changed
   */
  public abstract boolean doAction(GameImpulse impulse);

  /**
   *
   * @param doDrop if falling shape must be dropped to bottom
   * @return whether state changes globally and whole screen to be repainted
   */
  public final boolean nextState(boolean doDrop) {
    boolean repaintAll;

    if (myCanSqueeze) {
      myCanSqueeze = internalSqueeze() && computeCanSqueeze();

      if (!myCanSqueeze) {
        internalThrowInNewShape();
      }

      repaintAll = true;
      //return true;

    }else if (internalDropCurrent(doDrop)) {
      repaintAll = false;
      //return false;

    }else if (!internalAcquireFallenShape()) {
      myState = LOST;
      repaintAll = true;

    }else {
      myCanSqueeze = computeCanSqueeze();

      if (myCanSqueeze) {
        repaintAll = false;
      } else {
        internalThrowInNewShape();
        repaintAll = true;
      }
    }

    return repaintAll;
  }

  public int getLevelDelayMS() {
    int i = (10 - getLevel()) * 90;
    if (i < 0) {
      i = 0;
    }
    return 100 + i;
  }

  public GameScreenLayout getGameScreenLayout() {
    return _gameScreenLayout;
  }

  public void setGameScreenLayout(GameScreenLayout _gameScreenLayout) {
    this._gameScreenLayout = _gameScreenLayout;
  }
}
