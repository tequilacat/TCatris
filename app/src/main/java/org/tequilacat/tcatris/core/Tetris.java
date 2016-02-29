package org.tequilacat.tcatris.core;

import android.graphics.Canvas;

import java.io.*;

/**
 * implements game logic
 */
public abstract class Tetris {

  public enum CellState {
    FALLING, SQUEEZED, SETTLED
  }

  private String _gameLabel;

  //private boolean myShowNext;
  private int myState = NOTINIT;
  public static final int NOTINIT = 0;
  public static final int ACTIVE = 1;
  public static final int LOST = 3;
  long seed = System.currentTimeMillis();
  private boolean myCanSqueeze = false;

  protected int myLastScored;

  private int[] myScores;
  private long[] myScoreDates;

  private int myFieldWidth;
  private int myFieldHeight;
  private int myNextWidth;
  private int myNextHeight;

  /**
   * uniquely identifies game type
   */
  private String _id;

  private GameScreenLayout _gameScreenLayout;

  public Tetris() {
  }

  public String getGameLabel() {
    return _gameLabel;
  }

  public String getId() {
    return _id;
  }

  public void setId(String id) {
    _id = id;
  }

  /**************************************************
   **************************************************/
  public void init(String gameLabel, String gameDescriptor) {
    // byte[] gameData, String gameLabel, int fWidth, int fHeight, int nextWidth, int nextHeight){
    // example:
    // 5:13:1:3

    _gameLabel = gameLabel;
//        Debug.print("Game data: '"+ gameDescriptor +"'");
    int sep1 = gameDescriptor.indexOf(':'),
      sep2 = gameDescriptor.indexOf(':', sep1 + 1),
      sep3 = gameDescriptor.indexOf(':', sep2 + 1),
      sep4 = gameDescriptor.indexOf(':', sep3 + 1);

    myFieldWidth = Integer.parseInt(gameDescriptor.substring(0, sep1));
    myFieldHeight = Integer.parseInt(gameDescriptor.substring(sep1 + 1, sep2));
    myNextWidth = Integer.parseInt(gameDescriptor.substring(sep2 + 1, sep3));
    myNextHeight = Integer.parseInt(gameDescriptor.substring(sep3 + 1, (sep4 > 0) ? sep4 : gameDescriptor.length()));

    configure((sep4 < 0) ? null : gameDescriptor.substring(sep4 + 1));
  }

  /**
   * remaining specs of the gamedef line.
   * default implementation does nothing.
   * @param specSettings
   */
  protected void configure(String specSettings) {}

  /**************************************************
   **************************************************/
  public final int getWidth() {
    return myFieldWidth;
  }

  public final int getHeight() {
    return myFieldHeight;
  }

  public final int getMaxShapeWidth() {
    return myNextWidth;
  }

  public final int getMaxShapeHeight() {
    return myNextHeight;
  }

  /**************************************************
   **************************************************/
  //protected abstract void initGameGraphics(int fieldPixWidth, int fieldPixHeight);
  public abstract void layout(int screenWidth, int screenHeight);

  public abstract void paintNext(Canvas g, int nextFigX, int nextFigY, int nextFigWidth, int nextFigHeight);

  public abstract void paintField(Canvas g, int fieldPixHeight);

  private static final int[] sCellColors = new int[]{
    ColorCodes.lightGray,
    ColorCodes.red, ColorCodes.blue, ColorCodes.purple, ColorCodes.orange, ColorCodes.green,
    ColorCodes.darkRed, ColorCodes.darkGreen, ColorCodes.blue, ColorCodes.cyan, ColorCodes.magenta, ColorCodes.orange, ColorCodes.lightBrown
  };

  public static int getTypeColor(int cellType) {
    return sCellColors[cellType];
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

  public abstract void init();

  void initGame() {
    myState = 0;
    myCanSqueeze = false;
    myLastScored = 0;

    init();
    myState = throwInNewShape() ? ACTIVE : LOST;
  }

  /**************************************************
   **************************************************/
  public int getLastScored() {
    return myLastScored;
  }

  public abstract boolean moveLeft();

  public abstract boolean moveRight();

  public abstract boolean rotateClockwise();

  public abstract boolean rotateAntiClockwise();

  public abstract int getLevel();

  public abstract int getScore();

  public final int getState() {
    return myState;
  }

  protected abstract boolean computeCanSqueeze();

  protected abstract boolean dropCurrent(boolean tillBottom);

  protected abstract boolean acquireFallenShape();

  protected abstract boolean squeeze();

  protected abstract boolean throwInNewShape();

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

  /**
   *
   * @param doDrop if falling shape must be dropped to bottom
   * @return whether state changes globally (true) and whole screen to be repainted
   */
  public final boolean nextState(boolean doDrop) {
    boolean repaintAll;

    if (myCanSqueeze) {
      myCanSqueeze = internalSqueeze() && computeCanSqueeze();

      if (!myCanSqueeze) {
        myState = throwInNewShape() ? ACTIVE : LOST;
      }

      repaintAll = true;
      //return true;

    }else if (dropCurrent(doDrop)) {
      repaintAll = false;
      //return false;

    }else if (!acquireFallenShape()) {
      myState = LOST;
      repaintAll = true;

    }else {
      myCanSqueeze = computeCanSqueeze();

      if (myCanSqueeze) {
        repaintAll = false;
      } else {
        myState = throwInNewShape() ? ACTIVE : LOST;
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
