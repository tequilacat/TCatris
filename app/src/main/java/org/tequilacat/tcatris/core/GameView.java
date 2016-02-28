package org.tequilacat.tcatris.core;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Region;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import org.tequilacat.tcatris.R;

import java.util.Calendar;
import java.util.Date;

// Referenced classes of package tetris:
//            ScoreBoard, Color, Tetris

public final class GameView extends SurfaceView {

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

  private Object myGameChangeLock = new Object();
  private int _screenWidth;
  private int _screenHeight;
  private SurfaceHolder _holder;
  private int _scorebarHeight;
  private GameAction _gameThreadAction;
  private boolean _isPaused;
  private boolean _isRunning;

  /**
   * override for visual constructor
   * @param context
   * @param attrs
   */
  public GameView(Context context, AttributeSet attrs) {
    super(context, attrs);
    initView(context);
  }

  /**
   * override for visual constructor
   * @param context
   * @param attrs
   * @param defStyle
   */
  public GameView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    initView(context);
  }

  /**
   * override for visual constructor
   * @param context
   */
  public GameView(Context context) {
    super(context);
    initView(context);
  }

  /**
   * Real constructor code
   * @param context
   */
  private void initView(Context context) {
    _holder = getHolder();
    _holder.addCallback(new SurfaceHolder.Callback() {
      @Override
      public void surfaceCreated(SurfaceHolder holder) {
        // start game
        Debug.print("surface created");
        gameStart();
      }

      @Override
      public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) { }

      @Override
      public void surfaceDestroyed(SurfaceHolder holder) {
        Debug.print("destroyed");
        gameStop();
      }
    });

    _gameThread = new Thread() {
      @Override
      public void run() {
        runGame();
      }
    };

    setFocusable(true);
    setFocusableInTouchMode(true);
  }

  @Override
  protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    super.onSizeChanged(w, h, oldw, oldh);
    _scorebarHeight = (int) (Ui.getLineHeight() * 1.2);

    _screenWidth = w;
    _screenHeight = h - _scorebarHeight;
    Debug.print("onSizeChanged " + w + " x " + h);

    if (getGame() != null) {
      getGame().layout(_screenWidth, _screenHeight);
    }
  }


  public void setGame(Tetris game) {
    myGame = game;
  }

  private Tetris getGame() {
    return myGame;
  }


  private void runGame() {
    // runs
    long INTERVAL = 500; // 300 millis per step
    long towait = INTERVAL;
    _gameThreadAction = null;
    _isRunning = true;
    _isPaused = false;
    getGame().initGame();

    synchronized (myGameChangeLock) {
      try {
        while(_isRunning) {
          Debug.print("Sleep " + towait);
          long time0 = System.currentTimeMillis();
          myGameChangeLock.wait(towait);
          long sleptTime = System.currentTimeMillis() - time0;

          if (_isRunning) {
            // otherwise just exit from while

            Canvas c = _holder.lockCanvas();

            synchronized (_holder) {
              if (_isPaused) {
                // show paused screen and wait for next kick
                Debug.print("PAUSE: show paused screen and wait for next kick");
                towait = 0;
                c.drawColor(Color.blue);

              } else if (getGame().getState() == Tetris.LOST) {
                // show scores
                Debug.print("lost, wait for next kick to restart");
                towait = 0;
                c.drawColor(Color.red);

              } else { // ACTIVE: run action, see consequences
                // normal timing operation, check action and depending on it repaint screen
                Debug.print("   woke up [slept=" + sleptTime + "], action = " + _gameThreadAction);
                GameAction curAction = _gameThreadAction;
                _gameThreadAction = null;
                // whether all screen data changed or only field with falling shape
                boolean repaintAll = false;
                boolean doRepaint = true;

                if (curAction == null) {
                  repaintAll = getGame().nextState(false);
                  towait = INTERVAL; // getGame().getLevelDelayMS();

                } else {
                  // run action
                  switch (curAction) {
                    case DROP:
                      repaintAll = getGame().nextState(true);
                      break;
                    case LEFT:
                      doRepaint = getGame().moveLeft();
                      break;
                    case RIGHT:
                      doRepaint = getGame().moveRight();
                      break;
                    case ROTATE_CW:
                      doRepaint = getGame().rotateClockwise();
                      break;
                    case ROTATE_CCW:
                      doRepaint = getGame().rotateAntiClockwise();
                      break;
                    default:
                      doRepaint = false;
                      break;
                  }

                  towait = INTERVAL - sleptTime;
                  if (towait <= 0) {
                    // slept or worked too long, next cycle very soon
                    towait = 1;
                  }
                  //Debug.print("... User action, sleep remaining ");
                }
                if (doRepaint) {
                  //c.drawColor(Color.cyan);
                  Paint p = new Paint();
                  p.setStyle(Paint.Style.FILL);
                  p.setColor(Color.black);
                  p.setAlpha(125); c.drawRect(0, 0, _screenWidth,_screenHeight, p);
                  //paintScreen(c, repaintAll);
                }

              }
            }
            _holder.unlockCanvasAndPost(c);
          }
        }
      } catch (InterruptedException e) {
        // TODO process exception
        Debug.print("Thread interrupted: "+e);
      }

      Debug.print("Exit game thread");
    }
  }

  private Thread _gameThread;

  private void gameStart() {
    Debug.print("game start");
    _gameThread.start();
  }

  private void gameStop() {
    try {
      Debug.print("game stop ...");
      _isRunning = false;
      sendAction(null);
      _gameThread.join();
      Debug.print("game stop done.");
    } catch (InterruptedException e) {
      // TODO catch Interruption
    }
  }

  private void showPauseDialog() {
    AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(getContext());
    dlgAlert.setMessage("Game is paused");
    dlgAlert.setTitle(R.string.app_name);
    dlgAlert.setCancelable(true);
    dlgAlert.setPositiveButton("Ok",
            new DialogInterface.OnClickListener() {
              public void onClick(DialogInterface dialog, int which) {
                //dismiss the dialog
                Debug.print("unpause");
                _isPaused = false;
                sendAction(null); // just kick the waiter
              }
            });

    _isPaused = true;
    dlgAlert.create().show();
  }

  enum GameAction {
    LEFT, RIGHT, ROTATE_CW, ROTATE_CCW, DROP,
  }

  /**
   * sends action from GUI thread to the thread waking it from waiting
   * @param action
   */
  private void sendAction(GameAction action){
    synchronized (myGameChangeLock) {
      _gameThreadAction = action;
      Debug.print("UI: notify of " + action);
      myGameChangeLock.notify();
    }
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {

    if (event.getAction() == MotionEvent.ACTION_DOWN) {
      // notify with some param
      float x = event.getX(), y = event.getY();
      int clickedArea = (int)x * 3 / _screenWidth + 3 * ((int)y * 3 / _screenHeight);

      if (clickedArea == 6) {
        sendAction(GameAction.LEFT);
      } else if (clickedArea == 7) {
        sendAction(GameAction.DROP);
      } else if (clickedArea == 8) {
        sendAction(GameAction.RIGHT);
      } else if (clickedArea == 3) {
        sendAction(GameAction.ROTATE_CCW);
      } else if (clickedArea == 4) {
        showPauseDialog();
      } else if (clickedArea == 5) {
        sendAction(GameAction.ROTATE_CW);
      }
    }
    return super.onTouchEvent(event);
  }

  /**
   * old game cycle
   */


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

  /**
   * called from thread, or from pointer events
   * @param drop
   */
  private void nextGameCycle(boolean drop) {
    if (myGame.getState() == Tetris.ACTIVE) {

      if (myGame.nextState(drop)) {
        //repaintAll();
      } else {
        //repaintField();
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
    /*
    TODO totally remove item processing
    if (item != null) { // notify tetris midlet of selection
      //Debug.print("Item '"+ item +"'");

      if (Ui.getMenuId() == Ui.MENU_SELECT_GAME) {
        if (item != Ui.ITEM_BACK) {
          stopGame();

          // TODO create game myGame = _gameList.createGame(Ui.getCurrentItemIndex());
          myGame.layout(_screenWidth, _screenHeight - _scorebarHeight);

          startGame();
          repaintAll();
          return;
        }
      }

      //TetrisMidlet.instance.menuItemSelected(item, Ui.getCurrentItemIndex());
      if (item == Ui.ITEM_SHOWSCORES) {
        myDisplayMode = DM_HISCORES;
      } else if (item == Ui.ITEM_NEWGAME) {
        //showNewGameMenu();
      } else if (item == Ui.ITEM_BACK) {
        myDisplayMode = DM_GAME;
      }

    }
    repaintAll();
    */
  }
/*
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

private void startGameOld() {
    myDisplayMode = DM_GAME;

    getGame().initGame();
    //myIsPaused = false;
//        myStop = false;
//        myFigureDropSteps = 0;

    myTickerThread = new Thread(this);

    myTickerThread.start();
  }

  private static final int TETRIS_NOP = 0;
  private static final int TETRIS_DROP = 1;
  private static final int TETRIS_ROTATE_CW = 2;
  private static final int TETRIS_ROTATE_CCW = 3;
  private static final int TETRIS_LEFT = 4;
  private static final int TETRIS_RIGHT = 5;
  private static final int TETRIS_MENU = 6;

  public void pointerPressed(int x, int y) {
    if (myDisplayMode == DM_MENU) {
      processMenuItem(Ui.getItemAtPoint(x, y));
    } else if (myDisplayMode == DM_GAME) {
      //x = x / 3;
      int clickedArea = x * 3 / _screenWidth + 3 * (y * 3 / _screenHeight);

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

  private void gameAction(int tetrisAction) {

    if (myGame.getState() == Tetris.ACTIVE) {

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
    }

  }
  */
  /**************************************************
   **************************************************/
  private void paintScreen(Canvas c, boolean repaintAll) {
    boolean repaintScores = true; // debug!
    boolean repaintNext = true; // debug!
    final GameScreenLayout layout = getGame().getGameScreenLayout();
    final int COLOR_FIELD_BG = getGame().getFieldBackground();

    if (repaintAll) {
      repaintScores = true;
      repaintNext = true;
      c.drawColor(Ui.UI_COLOR_PANEL);
    }

    //Debug.print("Paint screen: "+repaintAll+", "+repaintScores+", "+repaintNext);

    //if(!myPaintsBackground){
    if (!getGame().ConfRepaintsBackground) {
      Ui.fillRect(c, layout.getFieldRect(), COLOR_FIELD_BG);
    }
    Ui.draw3dRect(c, layout.getFieldRect());

    // paint field
    c.translate(layout.getFieldRect().left, layout.getFieldRect().top);
    getGame().paintField(c, layout.getFieldRect().height());
    c.translate(-layout.getFieldRect().left, -layout.getFieldRect().top);
    c.clipRect(0, 0, _screenWidth, _screenHeight, Region.Op.REPLACE);


    // debug
//        g.setColor(Color.black);
//        g.drawString("Drops: "+myFigureDropSteps, 0, 0, Ui.G_LEFT_TOP);

    if (getGame().getLastScored() > 0) {
      Ui.drawShadowText(
        c, "+" + getGame().getLastScored(), layout.getFieldRect().left, layout.getFieldRect().top,
        Ui.UI_COLOR_SELITEM_TEXT, Ui.UI_COLOR_DARKSHADOW);
    }


    if (repaintNext) {
      Rect next = layout.getNextShapeRect();
      Ui.fillRect(c, next, COLOR_FIELD_BG);

      Ui.draw3dRect(c, layout.getNextShapeRect());

      getGame().paintNext(c, next.left, next.top, next.width(), next.height());
      //c.setClip(0, 0, _screenWidth, _screenHeight);
    }

    if (repaintScores) {
      showInGameScores(c);
    }
  }

/*

// TODO implement message() or use combobox
    protected void message(Graphics g, String msg) {
        int nStrings = 0;
        int width = 0, fromPos = 0;
        float fHeight = Ui.getLineHeight();

        for (int i = 0; i <= msg.length(); i++) {
            if (i == msg.length() || msg.charAt(i) == '\n') {
                nStrings++;
                int w = f.substringWidth(msg, fromPos, i - fromPos);
                if (width < w) width = w;
                fromPos = i + 1;
            }
        }

        float height = nStrings * fHeight;

        width += 4;
        height += 2;
        //int scrWidth = getWidth(), scrHeight = getHeight();
        float x = (getWidth() - width) / 2, y = (getHeight() - height) / 2;

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
*/

  private void displayEntry(Canvas c, Bitmap img, int score, int xPos, int yPos) {
    // TODO implement display entry
//
//        if (img != null) {
//            //g.drawImage(img, xPos, yPos, myDisplayIconsVertically ? Ui.G_CENTER_TOP : Ui.G_LEFT_TOP);
//
//            if (myDisplayIconsVertically) {
//                yPos += img.getHeight();
//            } else {
//                xPos += img.getWidth();
//                int fontHeight = g.getFont().getHeight(),
//                        lineHeight = (fontHeight > img.getHeight()) ? fontHeight : img.getHeight();
//                yPos += (lineHeight - fontHeight) / 2;
//            }
//        }
//        // g.drawString(""+score, xPos + 2, yPos, myDisplayIconsVertically ? Ui.G_CENTER_TOP : Ui.G_LEFT_TOP);
//
//        /* g.setColor(Ui.UI_COLOR_DARKSHADOW);
//        g.drawString(""+score, xPos + 1, yPos - 1, myDisplayIconsVertically ? Ui.G_CENTER_TOP : Ui.G_LEFT_TOP);
//        g.setColor(Ui.UI_COLOR_SELITEM_TEXT);
//        g.drawString(""+score, xPos + 2, yPos, myDisplayIconsVertically ? Ui.G_CENTER_TOP : Ui.G_LEFT_TOP); */
//        Ui.drawShadowText(
//                c, "" + score, xPos, yPos,
//                myDisplayIconsVertically ? Ui.G_CENTER_TOP : Ui.G_LEFT_TOP,
//                Ui.UI_COLOR_SELITEM_TEXT, Ui.UI_COLOR_DARKSHADOW);
  }

  /**************************************************
   **************************************************/
  private void showInGameScores(Canvas c) {
    GameScreenLayout layout = getGame().getGameScreenLayout();
    int curScore = getGame().getScore();
    float fontHeight = Ui.getLineHeight();

    int[] hiScores = getGame().getHiScores();
    // along right side, draw

    // DEBUG
//                    hiScores[0] = 0;
//                    curScore = 15;


    int scoreX = MARGIN_LEFT + layout.getFieldRect().width() + SPACING_VERT;
    int scoreWidth = _screenWidth - scoreX - MARGIN_RIGHT;
    int scoreY = layout.getNextShapeRect().top + layout.getNextShapeRect().height() + MARGIN_LEFT;
    int scoreHeight = layout.getFieldRect().top + layout.getFieldRect().height() - scoreY;
    //layout.getGlassClipY() + layout.getGlassClipHeight() - scoreY;

//        g.setColor(Color.green);
//        g.fillRect(scoreX, scoreY, scoreWidth, scoreHeight);
//        Ui.draw3dRect(g, scoreX, scoreY, scoreWidth, scoreHeight);

    //Debug.print("display score : is icon Vert? "+myDisplayIconsVertically);

    /// SCORES AND LEVEL
    //c.setColor(Color.black);
    //int fontX = scoreX, fontY = scoreY;
    float dY;
    int playerIconHeight = PlayerIcon == null ? 50 : PlayerIcon.getHeight();

    if (myDisplayIconsVertically) {
      scoreX += (_screenWidth - MARGIN_RIGHT - scoreX) / 2;
      dY = fontHeight + playerIconHeight;
    } else {
      dY = (fontHeight > playerIconHeight) ? fontHeight : playerIconHeight;
    }

    displayEntry(c, LevelIcon, getGame().getLevel(), scoreX, scoreY);
    scoreY += dY;

    //fontY = scoreY + 3*fontHeight;


    if (hiScores[0] == 0) { // only my scores
      displayEntry(c, PlayerIcon, curScore, scoreX, scoreY);
    } else if (hiScores[0] > curScore) { // player competes, below
      displayEntry(c, WinnerIcon, hiScores[0], scoreX, scoreY);
      displayEntry(c, PlayerIcon, curScore, scoreX, scoreY + (int) dY);
    } else { // player wins
      displayEntry(c, PlayerIcon, curScore, scoreX, scoreY);
      displayEntry(c, WinnerIcon, hiScores[0], scoreX, scoreY + (int) dY);
    }

    /////////// SCORE BAR (AS PLAYER COMPETES HISCORE)
    if (hiScores[0] > 0) {
      int barColor = (curScore >= hiScores[0]) ? Color.green : Color.yellow;
      int sbX, sbY, sbWidth, sbHeight;


      // assume horizontal score bar
      sbHeight = _scorebarHeight - 2;
      sbWidth = _screenWidth - MARGIN_LEFT - MARGIN_RIGHT;
      sbX = MARGIN_LEFT;
      sbY = _screenHeight - _scorebarHeight;

      Ui.drawRect(c, sbX, sbY, sbWidth, sbHeight, Color.black);
      Ui.drawRect(c, sbX, sbY, sbWidth, sbHeight, Color.black);

      if (curScore >= hiScores[0]) { // win
        int newPos = sbWidth * hiScores[0] / curScore;
        Ui.drawRect(c, sbX + newPos, sbY, sbWidth - newPos, sbHeight, barColor);
      } else { // compete
        Ui.drawRect(c, sbX, sbY, sbWidth * curScore / hiScores[0], sbHeight, barColor);
      }

      // 3d bevel around score bar
      Ui.draw3dRect(c, sbX, sbY, sbWidth, sbHeight);
    }
  }

  /**************************************************
   **************************************************/
  private void showScoreTable(Canvas c) {
    Paint p = new Paint();
    float fontHeight = Ui.getLineHeight();

    int curScore = getGame().getScore();

    c.drawColor(getGame().getFieldBackground());

    p.setColor(Color.black);
    // display game name
    c.drawText(myGame.GameName, 0, 0, p);
    p.setTextAlign(Paint.Align.RIGHT);
    c.drawText(getTimeStr(0), _screenWidth, 0, p);
    p.setTextAlign(Paint.Align.LEFT);
    c.drawText(Ui.MSG_PRESS_ANYKEY, 0, _screenHeight - 1 - fontHeight, p);

    c.drawRect(0, fontHeight, _screenWidth, _screenHeight - fontHeight * 2, p);

    int nScores = myGame.getScoreTableSize();

    if (nScores > 0) {
      int pos = 0, curScorePosition = myGame.findScorePosition(curScore);
      float yPos = fontHeight;
      float entryHeight = (_screenHeight - fontHeight * 2) / nScores;

      if (entryHeight > fontHeight * 2) {
        entryHeight = fontHeight * 2;
      }

//            String label;
      //Font curFont = g.getFont();
      //Font boldFont = Font.getFont(curFont.getFace(), curFont.getStyle() | Font.STYLE_BOLD, curFont.getSize());
      long recordDate;
      while ((recordDate = myGame.getScoreTableEntry(pos)) >= 0) {

        if (pos == curScorePosition) {
          p.setColor(Color.yellow);
        } else {
          p.setColor(Color.white);
        }

        //g.drawRect(1, yPos+1, scrWidth-3, entryHeight - 2);

        String posAndScore = "" + (pos + 1) + ". " + myGame.getHiScores()[pos];

        //g.setFont(boldFont);
        c.drawText(posAndScore, 3, yPos + 2 + (entryHeight - fontHeight) / 2, p);
        //g.setFont(curFont);
        p.setTextAlign(Paint.Align.RIGHT);
        c.drawText(getTimeStr(recordDate),
          _screenWidth - 3, yPos + 2 + (entryHeight - fontHeight) / 2 - fontHeight, p);
        p.setTextAlign(Paint.Align.LEFT);

        yPos += entryHeight;
        pos++;
      }

      p.setColor(Color.black);

    }
  }


  /**************************************************
   **************************************************/
  public static String getTimeStr(long millis) {
    StringBuilder stb = new StringBuilder();

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
