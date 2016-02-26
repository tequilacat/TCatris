package org.tequilacat.tcatris.core;

import android.graphics.Canvas;

import java.io.*;

/**
 * implements game logic
 */
public abstract class Tetris {
  public String GameName;

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

  public boolean ConfRepaintsBackground = false;

  //public boolean IsLayedOut = false;

  private int myFieldWidth;
  private int myFieldHeight;
  private int myNextWidth;
  private int myNextHeight;

  private GameScreenLayout _gameScreenLayout;


  /**************************************************
   **************************************************/
  public Tetris(byte[] gameData, int gameIndex) {
    parseHiScores(gameData);
  }

  /**************************************************
   **************************************************/
  public Tetris() {
  }

  /**************************************************
   **************************************************/
  public void init(String gameLabel, String gameDescriptor, byte[] gameData) {
    // byte[] gameData, String gameLabel, int fWidth, int fHeight, int nextWidth, int nextHeight){
    // example:
    // 5:13:1:3

    GameName = gameLabel;
//        Debug.print("Game data: '"+ gameDescriptor +"'");
    int sep1 = gameDescriptor.indexOf(':'),
      sep2 = gameDescriptor.indexOf(':', sep1 + 1),
      sep3 = gameDescriptor.indexOf(':', sep2 + 1),
      sep4 = gameDescriptor.indexOf(':', sep3 + 1);

    myFieldWidth = Integer.parseInt(gameDescriptor.substring(0, sep1));
    myFieldHeight = Integer.parseInt(gameDescriptor.substring(sep1 + 1, sep2));
    myNextWidth = Integer.parseInt(gameDescriptor.substring(sep2 + 1, sep3));
    myNextHeight = Integer.parseInt(gameDescriptor.substring(sep3 + 1, (sep4 > 0) ? sep4 : gameDescriptor.length()));

    parseHiScores(gameData);

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
    Color.lightGray,
    Color.red, Color.blue, Color.purple, Color.orange, Color.green,
    Color.darkRed, Color.darkGreen, Color.blue, Color.cyan, Color.magenta, Color.orange, Color.lightBrown
  };

  /**************************************************
   **************************************************/
  public static int getTypeColor(int cellType) {
    // return (cellType == 0) ? getFieldBackground() :  sCellColors[cellType - 1];
    return sCellColors[cellType];
  }

  public int getFieldBackground() {
    //return myFieldBackground;
    return getTypeColor(0);
  }

  /**************************************************
   * save format:
   * <p/>
   * nTopScores(int),
   * topscore (*nTopScores):  int scores, long timeMillis, UTF label
   * <p/>
   * converted : int[] scores, long[] millis, String[] chars;
   **************************************************/
  private void parseHiScores(byte[] data) {
    try {
      myScores = new int[5];
      myScoreDates = new long[myScores.length];

      if (data != null && data.length > 0) {
        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(data));
        int scoreTableSize = dis.readInt();

//                Debug.print(">>> Read score table ["+scoreTableSize+"]");

        for (int i = 0; i < scoreTableSize; i++) {
          int score = dis.readInt();
          long date = dis.readLong();
          String title = dis.readUTF();
          if (i < myScores.length) {
            myScores[i] = score;
            myScoreDates[i] = date;
          }
        }
      }
    } catch (IOException ioe) { // it cant happen, lets pretend
    }
  }

  /**************************************************
   **************************************************/
//    private void debugDumpScores(){
//        int i = 0;
//        String line;
//        while( (line = getScoreTableEntry(i)) != null){
//            Debug.print(line);
//            i++;
//        }
//    }

  /**
   * @return entry in score table, or null if out of bounds or null
   */
  protected long getScoreTableEntry(int index) {
    return (index >= myScores.length || myScores[index] == 0) ?
      -1 : myScoreDates[index];
    //(""+(index+1)+": "+myScores[index]+" ( "+getTimeStr(myScoreDates[index])+" )");
  }


  /**
   */
  protected int getScoreTableSize() {
    int nScores = 0;
    while (nScores < myScores.length && myScores[nScores] > 0) {
      nScores++;
    }
    return nScores;
  }

  /**
   */
  protected int findScorePosition(int score) {
    if (score > 0) {
      for (int i = 0; i < myScores.length; i++) {
        if (myScores[i] == score) {
          return i;
        }
      }
    }
    return -1;
  }

  /**
   */
  protected boolean insertTopScore(int score) {
    boolean scoreInserted = false;
    if (score > 0) {
      int i = myScores.length - 1;
      while (i >= -1) {
        if (i == -1 || score < myScores[i]) { // add under it
          i++;
          if (i < myScores.length) {
            myScores[i] = score;
            myScoreDates[i] = System.currentTimeMillis();
//                        Debug.print("!!!  Added @ pos "+ (i+1));
            scoreInserted = true;
          }
          break;
        } else { // i >= 0, score >= myScores[i]
          if (i + 1 < myScores.length) {
            myScores[i + 1] = myScores[i];
            myScoreDates[i + 1] = myScoreDates[i];
          }
        }
        i--;
      }

//            Debug.print("AFTER : ");
//            debugDumpScores();

      if (scoreInserted) {
        //Debug.print("Score inserted, dump scores to the app props");
//                debugDumpScores();
        // TODO record top score in master game code
        // GameList.storeGameData(encodeTopScores(), GameName);
      }
    }
    return scoreInserted;
  }

  /**
   * @return byte array containing earned scores in this game
   */
  protected byte[] encodeTopScores() {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try {

      int nScores = getScoreTableSize();

      DataOutputStream dos = new DataOutputStream(baos);
      dos.writeInt(nScores);
      for (int i = 0; i < nScores; i++) {
        dos.writeInt(myScores[i]);
        dos.writeLong(myScoreDates[i]);
        dos.writeUTF("");
      }

    } catch (IOException ioe) { // it cant happen, lets pretend
    }

    return baos.toByteArray();
  }

  /**
   */
  public int[] getHiScores() {
    return myScores;
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
