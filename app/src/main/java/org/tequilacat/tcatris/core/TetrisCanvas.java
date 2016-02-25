package org.tequilacat.tcatris.core;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.view.SurfaceView;

import java.util.Calendar;
import java.util.TimeZone;
import java.util.Date;

// Referenced classes of package tetris:
//            ScoreBoard, Color, Tetris

public final class TetrisCanvas extends SurfaceView implements Runnable {

  //public static Display display;
  public static Bitmap PlayerIcon, WinnerIcon, LevelIcon;

  public static final int DM_GAME = 0;
  public static final int DM_MENU = 1;
  public static final int DM_HISCORES = 2;
  int myDisplayMode = DM_GAME;

  public static final int MARGIN_LEFT = 5;
  public static final int MARGIN_RIGHT = 5;
  public static final int MARGIN_TOP = 5;
  public static final int MARGIN_BOTTOM = 5;
  public static final int SPACING_VERT = 5;

  private Tetris myGame;

  private Thread myTickerThread;

  boolean myDisplayIconsVertically;
  private static int SCOREBAR_WIDTH = 7;

  private Object myGameChangeLock = new Object();
  private int _screenWidth;
  private int _screenHeight;

  /**************************************************
   **************************************************/
  public TetrisCanvas(Context context) {
    super(context);
    showNewGameMenu();
  }

  @Override
  protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    super.onSizeChanged(w, h, oldw, oldh);

    _screenWidth = w;
    _screenHeight = h;

    if (getGame() != null) {
      getGame().layout(_screenWidth, _screenHeight);
    }
  }

  /**************************************************
   **************************************************/
  public Tetris getGame() {
    return myGame;
  }


  /**************************************************
   **************************************************/
  private static void initGameGraphics() {
    if (PlayerIcon == null) {
      SCOREBAR_WIDTH = _screenWidth / 20;

      String graphicsType = (ScreenWidth < 176) ? "/small" : "/big";
      try {
        PlayerIcon = Image.createImage(graphicsType + "/icon-player.png");
        WinnerIcon = Image.createImage(graphicsType + "/icon-winner.png");
        LevelIcon = Image.createImage(graphicsType + "/icon-level.png");
      } catch (Exception e) {
      }
    }
  }


  /**************************************************
   **************************************************/
  private void showMainMenu() {
    Ui.initMenu(Ui.MENU_INGAME);
    Ui.addItem(Ui.ITEM_BACK);
    Ui.addItem(Ui.ITEM_NEWGAME);
    Ui.addItem(Ui.ITEM_SHOWSCORES);
    Ui.addItem(Ui.ITEM_OPTIONS);
    Ui.addItem(Ui.ITEM_EXIT);
    myDisplayMode = DM_MENU;
  }

  /**************************************************
   **************************************************/
  private void showNewGameMenu() {
    Ui.initMenu(Ui.MENU_SELECT_GAME);
    TetrisMidlet.instance.populateStartGameMenu();

    Ui.addItem((getGame() != null) ? Ui.ITEM_BACK : Ui.ITEM_EXIT);
    myDisplayMode = DM_MENU;
  }

  /**************************************************
   **************************************************/
  private void startGame() {
    myDisplayMode = DM_GAME;

    getGame().initGame();
    //myIsPaused = false;
//        myStop = false;
//        myFigureDropSteps = 0;

    myTickerThread = new Thread(this);

    myTickerThread.start();
  }

  /**************************************************
   * runnable
   **************************************************/
  public void run() {
    Tetris curGame = getGame();

    // while(!myStop && curGame == getGame()) {
    while (myTickerThread != null && curGame == getGame() && curGame.getState() == Tetris.ACTIVE) {
      try {
        if (myDisplayMode == DM_GAME) {
          tick();
        }
        int sleepTime = getGame().getLevelDelayMS();
        Thread.sleep(sleepTime);
      } catch (InterruptedException interruptedexception) {
      }
    }

    if (curGame != getGame()) {
      Debug.print("Game " + curGame + " finished, switch to " + getGame());
    }
  }

  /**************************************************
   **************************************************/
  public void stopGame() {
    myTickerThread = null;
    //myStop = true;
    if (myGame != null) {
      Debug.print("Stop game");
      myGame.insertTopScore(myGame.getScore());
    }
  }

  /**************************************************
   **************************************************/
  public void tick() {
    synchronized (myGameChangeLock) {
      if (isShown()) {
        nextGameCycle(false);
      }
    }
  }


  /**************************************************
   **************************************************/
  private void nextGameCycle(boolean drop) {
    if (myGame.getState() == Tetris.ACTIVE) {

      if (myGame.nextState(drop)) {
//                myFigureDropSteps = 0;
        repaint();
      } else {
//                myFigureDropSteps++;
        repaintField();
      }

      if (myGame.getState() == Tetris.LOST) {
        stopGame();
      }
    } else {
      Debug.print("ERROR : FAILED GAME LIVES ON");
    }
  }

  /**************************************************
   **************************************************/
  private void processMenuItem(String item) {
    if (item != null) { // notify tetris midlet of selection
      //Debug.print("Item '"+ item +"'");

      if (Ui.getMenuId() == Ui.MENU_SELECT_GAME) {
        if (item != Ui.ITEM_BACK) {
          stopGame();

          myGame = GameList.createGame(Ui.getCurrentItemIndex());
          myGame.layout(_screenWidth, _screenHeight - SCOREBAR_WIDTH);

          startGame();
          repaint();
          return;
        }
      }

      //TetrisMidlet.instance.menuItemSelected(item, Ui.getCurrentItemIndex());
      if (item == Ui.ITEM_SHOWSCORES) {
        myDisplayMode = DM_HISCORES;
      } else if (item == Ui.ITEM_NEWGAME) {
        showNewGameMenu();
      } else if (item == Ui.ITEM_BACK) {
        myDisplayMode = DM_GAME;
      }

    }
    repaint();
  }

  private static final int TETRIS_NOP = 0;
  private static final int TETRIS_DROP = 1;
  private static final int TETRIS_ROTATE_CW = 2;
  private static final int TETRIS_ROTATE_CCW = 3;
  private static final int TETRIS_LEFT = 4;
  private static final int TETRIS_RIGHT = 5;
  private static final int TETRIS_MENU = 6;

  /**************************************************
   **************************************************/
  public void pointerPressed(int x, int y) {
    if (myDisplayMode == DM_MENU) {
      processMenuItem(Ui.getItemAtPoint(x, y));
    } else if (myDisplayMode == DM_GAME) {
      //x = x / 3;
      int clickedArea = x * 3 / ScreenWidth
        + 3 * (y * 3 / ScreenHeight);
      if (clickedArea == 6) {
        gameAction(TETRIS_LEFT);
      } else if (clickedArea == 7) {
        gameAction(TETRIS_DROP);
      } else if (clickedArea == 8) {
        gameAction(TETRIS_RIGHT);
      } else if (clickedArea == 3) {
        gameAction(TETRIS_ROTATE_CCW);
      } else if (clickedArea == 4) {
        gameAction(TETRIS_MENU);
      } else if (clickedArea == 5) {
        gameAction(TETRIS_ROTATE_CW);
      }
    }
  }

  /**************************************************
   **************************************************/
  private void gameAction(int tetrisAction) {

    // now process actions in DM_GAME mode
    if (tetrisAction == TETRIS_MENU) {
      if (myGame.getState() == Tetris.LOST) { // check if we display hiscores or not
        if (myGame.findScorePosition(myGame.getScore()) >= 0) {
          myDisplayMode = DM_HISCORES;
        } else {
          startGame();
        }
      } else { // show menu
        showMainMenu();
      }
      repaint();
      return;
    }


    if (myGame.getState() != Tetris.ACTIVE) {
      return;
    }

    if (myGame.canSqueeze()) {
      tick();
      return;
    }

    boolean updatedField = false, updatedScreen = false;

    synchronized (myGameChangeLock) {
      if (tetrisAction == TETRIS_DROP) {
        //            System.out.println("drop");
        nextGameCycle(true);
      } else if (tetrisAction == TETRIS_LEFT) {
        updatedField = myGame.moveLeft();
      } else if (tetrisAction == TETRIS_RIGHT) {
        updatedField = myGame.moveRight();
      } else if (tetrisAction == TETRIS_ROTATE_CCW) {
        updatedField = myGame.rotateAntiClockwise();
      } else if (tetrisAction == TETRIS_ROTATE_CW) {
        updatedField = myGame.rotateClockwise();
      }
    }

    if (updatedField) {
      repaintField();
    } else if (updatedScreen) {
      repaint();
    }
  }


  /**************************************************
   **************************************************/
  public void paint(Canvas c) {

    synchronized (myGameChangeLock) {

      if (!myIsLayedOut) {
        initGameGraphics();

        if (myGame != null && !myGame.IsLayedOut) {
          myGame.layout(this, ScreenWidth, ScreenHeight - SCOREBAR_WIDTH);
          myGame.IsLayedOut = true;
        }
      }

      if (myDisplayMode == DM_MENU) {
        Ui.displayMenu(g, ScreenWidth, ScreenHeight, (getGame() == null) ? null : getGame().GameName);
        return;
      }

      if (myDisplayMode == DM_HISCORES) {
        showScoreTable(g);
        return;
      }

      paintScreen(g, !getClipGlassOnly(g));

      if (myGame.getState() == myGame.LOST) {
        message(g, Ui.MSG_GAMEOVER);
      }

    }
  }

  /**************************************************
   **************************************************/
  private void paintScreen(Graphics g, boolean repaintAll) {
    boolean repaintScores = true; // debug!
    boolean repaintNext = true; // debug!

    final int COLOR_FIELD_BG = getGame().getFieldBackground();

    if (repaintAll) {
      repaintScores = true;
      repaintNext = true;
      g.setColor(Ui.UI_COLOR_PANEL);
      g.fillRect(0, 0, ScreenWidth, ScreenHeight);

    }

    //Debug.print("Paint screen: "+repaintAll+", "+repaintScores+", "+repaintNext);

    //if(!myPaintsBackground){
    if (!getGame().ConfRepaintsBackground) {
      g.setColor(COLOR_FIELD_BG);
      g.fillRect(getGlassClipX(), getGlassClipY(), getGlassClipWidth(), getGlassClipHeight());
    }
    Ui.draw3dRect(g, getGlassClipX(), getGlassClipY(), getGlassClipWidth(), getGlassClipHeight());

    // paint field
    g.translate(getGlassClipX(), getGlassClipY());
    getGame().paintField(g, getGlassClipHeight());
    g.translate(-g.getTranslateX(), -g.getTranslateY());
    g.setClip(0, 0, ScreenWidth, ScreenHeight);


    // debug
//        g.setColor(Color.black);
//        g.drawString("Drops: "+myFigureDropSteps, 0, 0, Ui.G_LEFT_TOP);

    if (getGame().getLastScored() > 0) {
      Ui.drawShadowText(
        g, "+" + getGame().getLastScored(), getGlassClipX(), getGlassClipY(),
        Ui.G_LEFT_TOP,
        Ui.UI_COLOR_SELITEM_TEXT, Ui.UI_COLOR_DARKSHADOW);
    }


    if (repaintNext) {
      g.setColor(COLOR_FIELD_BG);
      g.fillRect(getNextFigX(), getNextFigY(), getNextFigWidth(), getNextFigHeight());

      Ui.draw3dRect(g, getNextFigX(), getNextFigY(), getNextFigWidth(), getNextFigHeight());

      getGame().paintNext(g, getNextFigX(), getNextFigY(), getNextFigWidth(), getNextFigHeight());
      g.setClip(0, 0, ScreenWidth, ScreenHeight);
    }

    if (repaintScores) {
      showInGameScores(g);
    }
  }

  private boolean getClipGlassOnly(Graphics g) {
    int i = g.getClipX();
    int j = g.getClipY();
    int k = g.getClipWidth();
    int l = g.getClipHeight();
    return l == getGlassClipHeight() && k == getGlassClipWidth() && i == getGlassClipX() && j == getGlassClipY();
  }

  public void repaintField() {
    repaint(getGlassClipX(), getGlassClipY(), getGlassClipWidth(), getGlassClipHeight());
  }

  protected void message(Graphics g, String msg) {
    int nStrings = 0;
    Font f = g.getFont();
    int width = 0, fromPos = 0, fHeight = f.getHeight();
    for (int i = 0; i <= msg.length(); i++) {
      if (i == msg.length() || msg.charAt(i) == '\n') {
        nStrings++;
        int w = f.substringWidth(msg, fromPos, i - fromPos);
        if (width < w) width = w;
        fromPos = i + 1;
      }
    }
    int height = nStrings * fHeight;

    width += 4;
    height += 2;
    //int scrWidth = getWidth(), scrHeight = getHeight();
    int x = (getWidth() - width) / 2, y = (getHeight() - height) / 2;

    g.setColor(Color.white);
    g.fillRect(x, y, width, height);

    g.setColor(Color.black);
    g.drawRect(x, y, width, height);

    fromPos = 0;
    int centerX = getWidth() / 2;

    for (int i = 0; i <= msg.length(); i++) {
      if (i == msg.length() || msg.charAt(i) == '\n') {
        g.drawSubstring(msg, fromPos, i - fromPos, centerX, y + 1, g.HCENTER | g.TOP);
        y += fHeight;
        fromPos = i + 1;
      }
    }
  }

  /**************************************************
   **************************************************/
  private void displayEntry(Graphics g, Image img, int score, int xPos, int yPos) {
    if (img != null) {
      g.drawImage(img, xPos, yPos, myDisplayIconsVertically ? Ui.G_CENTER_TOP : Ui.G_LEFT_TOP);
      if (myDisplayIconsVertically) {
        yPos += img.getHeight();
      } else {
        xPos += img.getWidth();
        int fontHeight = g.getFont().getHeight(),
          lineHeight = (fontHeight > img.getHeight()) ? fontHeight : img.getHeight();
        yPos += (lineHeight - fontHeight) / 2;
      }
    }
    // g.drawString(""+score, xPos + 2, yPos, myDisplayIconsVertically ? Ui.G_CENTER_TOP : Ui.G_LEFT_TOP);
        
        /* g.setColor(Ui.UI_COLOR_DARKSHADOW);
        g.drawString(""+score, xPos + 1, yPos - 1, myDisplayIconsVertically ? Ui.G_CENTER_TOP : Ui.G_LEFT_TOP);
        g.setColor(Ui.UI_COLOR_SELITEM_TEXT);
        g.drawString(""+score, xPos + 2, yPos, myDisplayIconsVertically ? Ui.G_CENTER_TOP : Ui.G_LEFT_TOP); */
    Ui.drawShadowText(
      g, "" + score, xPos, yPos,
      myDisplayIconsVertically ? Ui.G_CENTER_TOP : Ui.G_LEFT_TOP,
      Ui.UI_COLOR_SELITEM_TEXT, Ui.UI_COLOR_DARKSHADOW);
  }

  /**************************************************
   **************************************************/
  private void showInGameScores(Canvas c) {
    int curScore = getGame().getScore();
    int fontHeight = g.getFont().getHeight();

    int[] hiScores = getGame().getHiScores();
    // along right side, draw

    // DEBUG
//                    hiScores[0] = 0;
//                    curScore = 15;


    int scoreX = MARGIN_LEFT + getGlassClipWidth() + SPACING_VERT;
    int scoreWidth = ScreenWidth - scoreX - MARGIN_RIGHT;
    int scoreY = getNextFigY() + getNextFigHeight() + MARGIN_LEFT;
    int scoreHeight = getGlassClipY() + getGlassClipHeight() - scoreY;

//        g.setColor(Color.green);
//        g.fillRect(scoreX, scoreY, scoreWidth, scoreHeight);
//        Ui.draw3dRect(g, scoreX, scoreY, scoreWidth, scoreHeight);

    //Debug.print("display score : is icon Vert? "+myDisplayIconsVertically);

    /// SCORES AND LEVEL
    g.setColor(Color.black);
    //int fontX = scoreX, fontY = scoreY;
    int dY;
    if (myDisplayIconsVertically) {
      scoreX += (ScreenWidth - MARGIN_RIGHT - scoreX) / 2;
      dY = fontHeight + PlayerIcon.getHeight();
    } else {
      dY = (fontHeight > PlayerIcon.getHeight()) ? fontHeight : PlayerIcon.getHeight();
    }

    displayEntry(g, LevelIcon, getGame().getLevel(), scoreX, scoreY);
    scoreY += dY;

    //fontY = scoreY + 3*fontHeight;


    if (hiScores[0] == 0) { // only my scores
      displayEntry(g, PlayerIcon, curScore, scoreX, scoreY);
    } else if (hiScores[0] > curScore) { // player competes, below
      displayEntry(g, WinnerIcon, hiScores[0], scoreX, scoreY);
      displayEntry(g, PlayerIcon, curScore, scoreX, scoreY + dY);
    } else { // player wins
      displayEntry(g, PlayerIcon, curScore, scoreX, scoreY);
      displayEntry(g, WinnerIcon, hiScores[0], scoreX, scoreY + dY);
    }

    /////////// SCORE BAR (AS PLAYER COMPETES HISCORE)
    if (hiScores[0] > 0) {
      int barColor = (curScore >= hiScores[0]) ? Color.green : Color.yellow;
      int sbX, sbY, sbWidth, sbHeight;


      // assume horizontal score bar

      sbHeight = SCOREBAR_WIDTH - 2;
      sbWidth = ScreenWidth - MARGIN_LEFT - MARGIN_RIGHT;
      sbX = MARGIN_LEFT;
      sbY = ScreenHeight - SCOREBAR_WIDTH;
      g.setColor(Color.black);
      g.fillRect(sbX, sbY, sbWidth, sbHeight);

      g.setColor(barColor);

      if (curScore >= hiScores[0]) { // win
        int newPos = sbWidth * hiScores[0] / curScore;
        g.fillRect(sbX + newPos, sbY, sbWidth - newPos, sbHeight);
      } else { // compete
        g.fillRect(sbX, sbY, sbWidth * curScore / hiScores[0], sbHeight);
      }

      // 3d bevel around score bar
      Ui.draw3dRect(g, sbX, sbY, sbWidth, sbHeight);
    }


  }

  /**************************************************
   **************************************************/
  private void showScoreTable(Graphics g) {
    int fontHeight = g.getFont().getHeight();

    int curScore = getGame().getScore();

    // display all scores
    g.setColor(getGame().getFieldBackground());
    g.fillRect(0, 0, ScreenWidth, ScreenHeight);

    g.setColor(Color.black);
    // display game name
    g.drawString(myGame.GameName, 0, 0, Ui.G_LEFT_TOP);
    g.drawString(getTimeStr(0), ScreenWidth, 0, Ui.G_RIGHT_TOP);
    g.drawString(Ui.MSG_PRESS_ANYKEY, 0, ScreenHeight - 1, Graphics.LEFT | Graphics.BOTTOM);

    g.fillRect(0, fontHeight, ScreenWidth, ScreenHeight - fontHeight * 2);

    int nScores = myGame.getScoreTableSize();

    if (nScores > 0) {


      int yPos = fontHeight;

      int pos = 0, curScorePosition = myGame.findScorePosition(curScore),
        entryHeight = (ScreenHeight - fontHeight * 2) / nScores;
      if (entryHeight > fontHeight * 2) {
        entryHeight = fontHeight * 2;
      }

//            String label;
      Font curFont = g.getFont();
      Font boldFont = Font.getFont(curFont.getFace(), curFont.getStyle() | Font.STYLE_BOLD, curFont.getSize());
      long recordDate;
      while ((recordDate = myGame.getScoreTableEntry(pos)) >= 0) {

        if (pos == curScorePosition) {
          g.setColor(Color.yellow);
        } else {
          g.setColor(Color.white);
        }

        //g.drawRect(1, yPos+1, scrWidth-3, entryHeight - 2);

        String posAndScore = "" + (pos + 1) + ". " + myGame.getHiScores()[pos];

        g.setFont(boldFont);
        g.drawString(posAndScore, 3, yPos + 2 + (entryHeight - fontHeight) / 2, Ui.G_LEFT_TOP);
        g.setFont(curFont);
        g.drawString(getTimeStr(recordDate),
          ScreenWidth - 3, yPos + 2 + (entryHeight - fontHeight) / 2, Graphics.RIGHT | Graphics.TOP);


        yPos += entryHeight;
        pos++;
      }

      g.setColor(Color.black);

    }
  }


  /**************************************************
   **************************************************/
  public static String getTimeStr(long millis) {
    StringBuffer stb = new StringBuffer();

//        TimeZone.getDefault()
    Calendar curTime = Calendar.getInstance();
    Calendar scoreTime = curTime;
    if (millis != 0) {
      scoreTime = Calendar.getInstance();
      scoreTime.setTime(new Date(millis));
    }
        
        /* 
        Debug.print("Cur time: "+curTime.get(Calendar.YEAR)+"."
            +curTime.get(Calendar.MONTH)+"."+curTime.get(Calendar.DAY_OF_MONTH));
            
        Debug.print("Score time ["+ millis +"]: "+scoreTime.get(Calendar.YEAR)+"."
            +scoreTime.get(Calendar.MONTH)+"."+scoreTime.get(Calendar.DAY_OF_MONTH));
         */
//         Debug.print("Current TZ: "+curTime.getTimeZone().getID());

    // today, or if request current time:
    if (scoreTime == curTime ||
      (curTime.get(Calendar.YEAR) == scoreTime.get(Calendar.YEAR)
        && curTime.get(Calendar.MONTH) == scoreTime.get(Calendar.MONTH)
        && curTime.get(Calendar.DAY_OF_MONTH) == scoreTime.get(Calendar.DAY_OF_MONTH))) {

      // same day, display HH:MM
      int hh = scoreTime.get(Calendar.HOUR_OF_DAY), mm = scoreTime.get(Calendar.MINUTE);

      stb.append(hh).append(':');
      if (mm < 10) stb.append('0');
      stb.append(mm);
    } else {
      stb.append(scoreTime.get(Calendar.DAY_OF_MONTH)).append('.')
        .append(scoreTime.get(Calendar.MONTH)).append('.')
        .append(scoreTime.get(Calendar.YEAR));
    }
    return stb.toString();
  }
}
