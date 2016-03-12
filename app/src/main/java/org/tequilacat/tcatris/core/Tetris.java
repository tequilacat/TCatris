package org.tequilacat.tcatris.core;

import android.graphics.Canvas;

import java.util.EnumSet;

/**
 * implements game logic
 */
public abstract class Tetris {

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
  private int myNextWidth;
  private int myNextHeight;

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
    // 5:13:1:3
    _descriptor = descriptor;
    String gameParams = descriptor.getGameParameters();
    int sep1 = gameParams.indexOf(':'),
        sep2 = gameParams.indexOf(':', sep1 + 1),
        sep3 = gameParams.indexOf(':', sep2 + 1),
        sep4 = gameParams.indexOf(':', sep3 + 1);

    myFieldWidth = Integer.parseInt(gameParams.substring(0, sep1));
    myFieldHeight = Integer.parseInt(gameParams.substring(sep1 + 1, sep2));
    myNextWidth = Integer.parseInt(gameParams.substring(sep2 + 1, sep3));
    myNextHeight = Integer.parseInt(gameParams.substring(sep3 + 1, (sep4 > 0) ? sep4 : gameParams.length()));

    configure((sep4 < 0) ? null : gameParams.substring(sep4 + 1));
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

  public final int getMaxShapeWidth() {
    return myNextWidth;
  }

  public final int getMaxShapeHeight() {
    return myNextHeight;
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
  public abstract GameImpulse getAxisImpulse(GameRunner.DragType axis, boolean positiveDirection);

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
