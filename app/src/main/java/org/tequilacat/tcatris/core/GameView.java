package org.tequilacat.tcatris.core;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import org.tequilacat.tcatris.R;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class GameView extends SurfaceView {

  public static final int MARGIN_LEFT = 5;
  public static final int MARGIN_RIGHT = 5;
  public static final int MARGIN_TOP = 5;
  public static final int MARGIN_BOTTOM = 5;
  public static final int SPACING_VERT = 5;

  private Tetris _currentGame;

  //boolean myDisplayIconsVertically;

  private final Object _gameChangeLock = new Object();
  //private int _screenWidth;
  //private int _screenHeight;
  //private SurfaceHolder _holder;
  //private int _scorebarHeight;
  private GameAction _gameThreadAction;
  private boolean _isPaused;
  private boolean _isRunning;
  private Thread _gameThread;

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
    SurfaceHolder _holder = getHolder();
    _holder.addCallback(new SurfaceHolder.Callback() {
      @Override
      public void surfaceCreated(SurfaceHolder holder) {
        // start game
        gameStart();
      }

      @Override
      public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) { }

      @Override
      public void surfaceDestroyed(SurfaceHolder holder) {
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
    layoutGameScreen(w, h);
  }

  public void setGame(Tetris game) {
    _currentGame = game;
    //GameList.instance().getMaxScore(_currentGame.getGameLabel())
  }

  private Tetris getGame() {
    return _currentGame;
  }

  private void runGame() {
    // runs
    long INTERVAL = 500; // 300 millis per step
    long towait = INTERVAL;
    _gameThreadAction = null;
    _isRunning = true;
    _isPaused = false;
    getGame().initGame();

    synchronized (_gameChangeLock) {
      try {
        while(_isRunning) {

          //Debug.print("Sleep " + towait);
          long time0 = System.currentTimeMillis();
          _gameChangeLock.wait(towait);
          long sleptTime = System.currentTimeMillis() - time0;

          if (_isRunning) {
            // otherwise just exit from while
            GameAction curAction = _gameThreadAction;
            _gameThreadAction = null;

            if (_isPaused) {
              // show paused screen and wait for next kick
              Debug.print("PAUSE: show paused screen and wait for next kick");
              towait = 0;
              paintScreen(ScreenPaintType.PAUSED);

            } else if (getGame().getState() == Tetris.LOST) {
              // show scores
              Debug.print("lost, wait for next kick to restart");
              towait = 0;
              paintScreen(ScreenPaintType.FAILED);

            } else if (curAction == GameAction.UNPAUSE) {
              towait = INTERVAL;
              paintScreen(ScreenPaintType.FULLSCREEN);

            } else { // ACTIVE: run action, see consequences
              // normal timing operation, check action and depending on it repaint screen
              //Debug.print("   woke up [slept=" + sleptTime + "], action = " + _gameThreadAction);
              ScreenPaintType repaintType = null;

              if (curAction == null) {
                //repaintType = ScreenPaintType.FULLSCREEN;
                repaintType = getGame().nextState(false) ? ScreenPaintType.FULLSCREEN : ScreenPaintType.FIELD_ONLY;
                towait = INTERVAL; // getGame().getLevelDelayMS();

              } else {
                // run action
                final boolean doRepaint;

                switch (curAction) {
                  case DROP:
                    doRepaint = true;
                    repaintType = getGame().nextState(true) ? ScreenPaintType.FULLSCREEN : ScreenPaintType.FIELD_ONLY;
                    sleptTime = 0;
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

                if (doRepaint && repaintType == null) {
                  repaintType = ScreenPaintType.FIELD_ONLY;
                }

                towait = INTERVAL - sleptTime;
                if (towait <= 0) {
                  // slept or worked too long, next cycle very soon
                  towait = 1;
                }
                //Debug.print("... User action, sleep remaining ");
              }

              paintScreen(repaintType);
            }
          }
        }
      } catch (InterruptedException e) {
        // TODO process exception
        Debug.print("Thread interrupted: "+e);
      }
    }
  }

  private void gameStart() {
    Debug.print("game start");
    initTracks();
    _gameThread.start();
  }

  private void gameStop() {
    try {
      _isRunning = false;
      sendAction(null);
      _gameThread.join();
    } catch (InterruptedException e) {
      // TODO catch Interruption
    }
  }
  /*
 private void showPauseDialog() {
    AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(getContext());
   dlgAlert.setMessage(R.string.msg_app_paused);
   dlgAlert.setTitle(R.string.app_name);
   dlgAlert.setCancelable(true);
   dlgAlert.setPositiveButton(R.string.btn_ok,
           new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int which) {
               //dismiss the dialog
               Debug.print("unpause");
               _isPaused = false;
               sendAction(null); // just kick the waiter
             }
           });

   _isPaused = true;
   sendAction(null);
   dlgAlert.create().show();

   _isPaused = true;
   sendAction(null);
 }
 */
  enum GameAction {
    LEFT, RIGHT, ROTATE_CW, ROTATE_CCW, DROP, UNPAUSE,
  }

  /**
   * sends action from GUI thread to the thread waking it from waiting
   * @param action
   */
  private void sendAction(GameAction action){
    synchronized (_gameChangeLock) {
      _gameThreadAction = action;
      _gameChangeLock.notify();
    }
  }

  private static Ui.ButtonGlyph[] BTN_GLYPHS = new Ui.ButtonGlyph[] {
    Ui.ButtonGlyph.LEFT, Ui.ButtonGlyph.RCCW, Ui.ButtonGlyph.DROP, Ui.ButtonGlyph.RCW, Ui.ButtonGlyph.RIGHT
  };

  private static final GameAction[] BUTTON_ACTIONS = new GameAction[]{
    GameAction.LEFT, GameAction.ROTATE_CCW, GameAction.DROP, GameAction.ROTATE_CW, GameAction.RIGHT
  };

  /**
   * stores info on dragged direction button
   */
  static class DragTrack {
    private final GameAction _positiveOffsetAction;
    private final GameAction _negativeOffsetAction;
    public final Button.ButtonType dragType;
    private final int _stepDistance;

    // pointer id for which this track currently registers offset
    public int pointerId;

    // start coordinates
    //private int x0, y0;
    // last coordinates
    private int lastX, lastY;

    GameAction _lastAction;
    int _lastActionCount;

    public DragTrack(Button.ButtonType dragType, GameAction onPositive, GameAction onNegative, int distance) {
      this.dragType = dragType;
      _positiveOffsetAction = onPositive;
      _negativeOffsetAction = onNegative;
      _stepDistance = distance;
      pointerId = -1;
    }

    public void start(int x, int y, int pointerId) {
      this.pointerId = pointerId;
      lastX = x;
      lastY = y;
    }

    public boolean isStarted() {
      return pointerId >= 0;
    }

    public void stop() {
      pointerId = -1;
    }

    public GameAction dragTo(int newX, int newY) {
      // only X is currently processed
      _lastAction = null;
      _lastActionCount = 0;
      int moveCount = (newX - lastX) / _stepDistance;

      if(moveCount != 0) {
        _lastAction = moveCount > 0 ? _positiveOffsetAction : _negativeOffsetAction;
        // update lastX, set action to
        //dragAction = LE
        _lastActionCount = Math.abs(moveCount);
        // fix lastX so it's round , so next movement will add its delta to remaining diff
        lastX += moveCount * _stepDistance;
      }

      return _lastAction;
    }

    public int getLastActionCount() {
      return _lastActionCount;
    }

    public GameAction getLastAction() {
      return _lastAction;
    }
  }

  private DragTrack[] _tracksByType;

  private void initTracks() {
    // dragging half screen (approx button width) should drag shape across whole field width twice
    _tracksByType = new DragTrack[]{

            new DragTrack(Button.ButtonType.ROTATE, GameAction.ROTATE_CW, GameAction.ROTATE_CCW,
                    (int)(getWidth() *0.4 / 10)), // 10 rotations per btn

            new DragTrack(Button.ButtonType.HORIZONTAL, GameAction.RIGHT, GameAction.LEFT,
                    (int)(getWidth() * 0.4 / getGame().getWidth() / 2)), // [W]*2 movements per button
    };
  }

  private Button.ButtonType getButtonTypeAt( int x, int y){
    Button.ButtonType type = null;

    for(Button btn  : _buttons) {
      if (btn.rect.contains(x, y)) {
        type = btn.type;
        break;
      }
    }

    return type;
  }

  /**
   * for drag event returns the action for this drag gesture,
   * e.g. dragging specific button left or right will return LEFT or RIGHT actions
   * after dragging for certain distance.
   *
   * If drag resulted in action, returns this action and _lastDragActionCount is set to number of actions to take
   * @param event
   * @return
   */
  private GameAction trackDrag(MotionEvent event) {
    GameAction dragAction = null;
    int action = MotionEventCompat.getActionMasked(event);

    if(action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_DOWN){
      int index = MotionEventCompat.getActionIndex(event);
      int pointerId = MotionEventCompat.getPointerId(event, index);
//      Debug.print(">>> Event AD/APD #" + action + " (" + event.getAction() + "): index = " + index + " of " + MotionEventCompat.getPointerCount(event));
      int x = (int) MotionEventCompat.getX(event, index), y = (int) MotionEventCompat.getY(event, index);

      Button.ButtonType buttonType = getButtonTypeAt(x, y);
      //DragType dragType = buttonType==null ? null : (buttonType== Button.ButtonType.HORIZONTAL ? DragType.MOVE_LEFTRIGHT : )
      if(buttonType== Button.ButtonType.DROP) {
        dragAction = GameAction.DROP;

      }else if (buttonType != null) {

        // find drag type (button ID) in currently tracked list,
        // if does not exist we remember it, if the button is already dragged we ignore it
        // that's all for ACTION_DOWN
        DragTrack found = null;

        for (DragTrack track : _tracksByType) {
          if (track.dragType == buttonType && !track.isStarted()) {
            found = track;
            break;
          }
        }

        if (found != null) {
          Debug.print("  ++ " + buttonType + ", id = " + pointerId + " [index = " + index + "]");
          found.start(x, y, pointerId);
        }
      }

    } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_POINTER_UP || action == MotionEvent.ACTION_CANCEL) {
      //Debug.print(">>> Event AU/APU/AC #" + action + " (" + event.getAction() + ")");//: index = " + index + " of " + MotionEventCompat.getPointerCount(event));
      int pointerId = MotionEventCompat.getPointerId(event, MotionEventCompat.getActionIndex(event));

      // find drag for this index,
      for (DragTrack track : _tracksByType) {
        if(track.pointerId == pointerId){
          track.stop();
          //Debug.print("   -- " + track.dragType);
          break;
        }
      }

    } else if (action == MotionEvent.ACTION_MOVE) {
      for(DragTrack track : _tracksByType) {
        // check if is dragging, get its pointer ID,
        if(track.isStarted()) {
          int pointerIndex = MotionEventCompat.findPointerIndex(event, track.pointerId);
          int x = (int) MotionEventCompat.getX(event, pointerIndex), y = (int) MotionEventCompat.getY(event, pointerIndex);
          dragAction = track.dragTo(x, y);

          if (dragAction != null) {
//            _lastDragActionCount = track.getLastActionCount();
            //Debug.print("Dragging: " + dragAction + "[" + _lastDragActionCount + "]");
            break;
          }
        }
      }
    }

    return dragAction;
  }

  /**
   * detects game action from touch event and sends it to game thread
   * @param event
   * @return
   */
  @Override
  public boolean onTouchEvent(MotionEvent event) {

    GameAction gameAction = trackDrag(event);

    if(gameAction != null) {
      sendAction(gameAction);

    }else {
      int actionId = MotionEventCompat.getActionMasked(event);

      if (actionId == MotionEvent.ACTION_DOWN || actionId == MotionEvent.ACTION_POINTER_DOWN) {
        if (_isPaused) {
          setPaused(false);

        } else {
          int index = MotionEventCompat.getActionIndex(event);
          int eventX = (int) MotionEventCompat.getX(event, index), eventY = (int) MotionEventCompat.getY(event, index);

          if (getGame().getGameScreenLayout().getFieldRect().contains(
                  eventX - _gameArea.left, eventY - _gameArea.top)) {
            // check field click - set pause
            setPaused(true);
          }
        }
      }
    }

    return true;
  }

  /**
   * sets paused mode and updates screen
   * @param isPaused
   */
  private  void  setPaused(boolean isPaused) {
    _isPaused = isPaused;
    sendAction(_isPaused ? null : GameAction.UNPAUSE);
  }

  /**************************************************
   **************************************************/
  public void stopGame() {
    //myTickerThread = null;
    //myStop = true;
    if (_currentGame != null) {
      Debug.print("Stop game");
      //_currentGame.insertTopScore(_currentGame.getScore());
    }
  }


  static class Button {
    enum ButtonType {
      HORIZONTAL, ROTATE, DROP
    }

    public final Ui.ButtonGlyph glyph;
    public final Rect rect;
    public final ButtonType type;

    public Button(ButtonType type, Ui.ButtonGlyph glyph, int x, int y, int w, int h) {
      this.type = type;
      this.glyph = glyph;
      rect = new Rect(x, y, x + w, y + h);
    }
  }

  private final List<Button> _buttons = new ArrayList<>();

  private Rect _gameArea = new Rect();
  //private Rect _buttonArea = new Rect();
  private Rect _scoreBarArea = new Rect();

  private int _fontSize;

  private int getLineHeight() {
    return _fontSize;
  }

  /**
   * computes all areas to be displayed on screen
   * @param w
   * @param h
   */
  private void layoutGameScreen(int w, int h) {
    _fontSize = getResources().getDimensionPixelSize(R.dimen.gamescreen_font_size);
    //setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimensionPixelSize(R.dimen.typo14));

    final int scoreHeight = getLineHeight();
    final int buttonHeight = h / 10;

    _scoreBarArea.set(0, 0, w, scoreHeight);
    _gameArea.set(0, _scoreBarArea.bottom, w, h - buttonHeight - scoreHeight);
    //_buttonArea.set(0, _gameArea.bottom, w, h);

    int buttonH = h - _gameArea.bottom, buttonW = w / 5, buttonX = 0, buttonY = _gameArea.bottom;
    _buttons.clear();
    _buttons.add(new Button(Button.ButtonType.ROTATE, Ui.ButtonGlyph.RCCW, buttonX, buttonY, buttonW * 2, buttonH));
    _buttons.add(new Button(Button.ButtonType.DROP, Ui.ButtonGlyph.DROP, buttonX + buttonW * 2, buttonY, buttonW, buttonH));
    _buttons.add(new Button(Button.ButtonType.HORIZONTAL, Ui.ButtonGlyph.RIGHT, buttonX + buttonW * 3, buttonY, buttonW * 2, buttonH));

    getGame().layout(_gameArea.width(), _gameArea.height());
    Rect fieldRect = getGame().getGameScreenLayout().getFieldRect();
    _fieldRect.set(_gameArea.left + fieldRect.left, _gameArea.top + fieldRect.top,
      _gameArea.left + fieldRect.right, _gameArea.top + fieldRect.bottom);
  }

  /**
   * type of repaint
   */
  private enum ScreenPaintType {
    PAUSED, FAILED, FULLSCREEN, FIELD_ONLY,
  }

  /**
   * field used in determining whether to repaint everything
   */
  private Rect _fieldUpdateRect = new Rect();

  /**
   * game field in absolute screen coordinates
   * */
  private Rect _fieldRect = new Rect();

  /**
   * paints app screen according to expected info to be displayed
   * @param paintType type of info to be displayed
   */
  private void paintScreen(ScreenPaintType paintType) {
    if (paintType != null) {
      synchronized (getHolder()) {
        Canvas c = null;

        try {
          Rect updateRect;

          if (paintType != ScreenPaintType.FIELD_ONLY) {
            updateRect = null;
          } else {
            updateRect = _fieldUpdateRect;
            updateRect.set(_fieldRect);
          }

          c = getHolder().lockCanvas(updateRect);

          if (updateRect != null && !updateRect.equals(_fieldRect)) {
            updateRect = null;
          }

          if (paintType == ScreenPaintType.PAUSED) {
            c.drawColor(ColorCodes.blue);
//            Ui.drawGlyph(c, 10, 10, 100, 100, Ui.ButtonGlyph.LEFT);
//            Ui.drawGlyph(c, 300, 300, 100, 100, Ui.ButtonGlyph.RIGHT);

          } else if (paintType == ScreenPaintType.FAILED) {
            c.drawColor(ColorCodes.red);

          } else if (paintType == ScreenPaintType.FULLSCREEN || paintType == ScreenPaintType.FIELD_ONLY) {
            //Debug.print("PGS: " + paintType);
            paintGameStateScreen(c, updateRect == null);
          }
        } finally {
          if (c != null) {
            getHolder().unlockCanvasAndPost(c);
          }
        }
      }
    }
  }

  /**
   * paints field or whole game screen
   * @param c
   * @param repaintAll
   */
  private void paintGameStateScreen(Canvas c, boolean repaintAll) {
    //repaintAll = true;

    final GameScreenLayout layout = getGame().getGameScreenLayout();
    Rect fieldRect = layout.getFieldRect();

    // Debug.print("paint: " + (repaintAll ? "ALL" : "field only"));

    if(repaintAll) {
      c.drawColor(Ui.UI_COLOR_PANEL);

      Rect next = layout.getNextShapeRect();
      getGame().paintNext(c, _gameArea.left + next.left, _gameArea.top + next.top, next.width(), next.height());

      // TODO paint scores as bar
      Ui.fillRect(c, _scoreBarArea, ColorCodes.black);
      Ui.drawText(c,
        String.format("%s %s: %d", getGame().getGameLabel(), getContext().getString(R.string.msg_score), getGame().getScore()),
        _scoreBarArea.left, _scoreBarArea.top, _fontSize, ColorCodes.yellow);

      // paint buttons
      for(Button btn : _buttons) {
        Ui.draw3dRect(c, btn.rect);
        int side = Math.min(btn.rect.width(), btn.rect.height());
        Ui.drawGlyph(c, btn.rect.left + (btn.rect.width() - side) / 2,
                btn.rect.top + (btn.rect.height() - side) / 2,
                side, side, btn.glyph);
      }

      /*int bX = _buttonArea.left, bW = _buttonArea.width() / BUTTON_ACTIONS.length,
        bY = _buttonArea.top, bH = _buttonArea.height();

      for (int i = 0; i < BUTTON_ACTIONS.length; i++) {
      //for(Ui.ButtonGlyph glyph : Ui.ButtonGlyph.values()) {
        Ui.draw3dRect(c, bX, bY, bW, bH);
        Ui.drawGlyph(c, bX, bY, bW, bH, BTN_GLYPHS[i]);

        bX += bW;
      } */
    }

    int dx = fieldRect.left + _gameArea.left, dy = fieldRect.top + _gameArea.top;
    c.translate(dx, dy);
    getGame().paintField(c, fieldRect.height());
    c.translate(-dx, -dy);
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

        g.setColor(ColorCodes.white);
        g.fillRect(x, y, width, height);

        g.setColor(ColorCodes.black);
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

  private void showInGameScores(Canvas c) {
    GameScreenLayout layout = getGame().getGameScreenLayout();
    int curScore = getGame().getScore();
    float fontHeight = getLineHeight();

    int[] hiScores = getGame().getHiScores();
    // along right side, draw

    // DEBUG
//                    hiScores[0] = 0;
//                    curScore = 15;

    int scoreWidth = _scoreBarArea.width();
    int scoreHeight = _scoreBarArea.height();

    int scoreX = MARGIN_LEFT + layout.getFieldRect().width() + SPACING_VERT;
    scoreWidth = scoreWidth - scoreX - MARGIN_RIGHT;
    int scoreY = layout.getNextShapeRect().top + layout.getNextShapeRect().height() + MARGIN_LEFT;
    //int scoreHeight = layout.getFieldRect().top + layout.getFieldRect().height() - scoreY;
    //layout.getGlassClipY() + layout.getGlassClipHeight() - scoreY;

//        g.setColor(ColorCodes.green);
//        g.fillRect(scoreX, scoreY, scoreWidth, scoreHeight);
//        Ui.draw3dRect(g, scoreX, scoreY, scoreWidth, scoreHeight);

    //Debug.print("display score : is icon Vert? "+myDisplayIconsVertically);

    /// SCORES AND LEVEL
    //c.setColor(ColorCodes.black);
    //int fontX = scoreX, fontY = scoreY;
    float dY;
    int playerIconHeight = PlayerIcon == null ? 50 : PlayerIcon.getHeight();
    boolean myDisplayIconsVertically = true;// historical, to be removed completely
    if (myDisplayIconsVertically) {
      scoreX += (scoreWidth - MARGIN_RIGHT - scoreX) / 2;
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
      int barColor = (curScore >= hiScores[0]) ? ColorCodes.green : ColorCodes.yellow;
      int sbX, sbY, sbWidth, sbHeight;


      // assume horizontal score bar
      sbHeight = scoreHeight - 2;
      sbWidth = scoreWidth - MARGIN_LEFT - MARGIN_RIGHT;
      sbX = MARGIN_LEFT;
      sbY = _scoreBarArea.top;

      Ui.drawRect(c, sbX, sbY, sbWidth, sbHeight, ColorCodes.black);
      Ui.drawRect(c, sbX, sbY, sbWidth, sbHeight, ColorCodes.black);

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

  private void showScoreTable(Canvas c) {
    int scrWidth = getWidth(), scrHeight = getHeight();
    Paint p = new Paint();
    float fontHeight = getLineHeight();

    int curScore = getGame().getScore();

    c.drawColor(ColorCodes.white);

    p.setColor(ColorCodes.black);
    // display game name
    c.drawText(_currentGame.getGameLabel(), 0, 0, p);
    p.setTextAlign(Paint.Align.RIGHT);
    c.drawText(getTimeStr(0), scrWidth, 0, p);
    p.setTextAlign(Paint.Align.LEFT);
    c.drawText("press any key", 0, scrHeight - 1 - fontHeight, p);

    c.drawRect(0, fontHeight, scrWidth, scrHeight - fontHeight * 2, p);

    int nScores = _currentGame.getScoreTableSize();

    if (nScores > 0) {
      int pos = 0, curScorePosition = _currentGame.findScorePosition(curScore);
      float yPos = fontHeight;
      float entryHeight = (scrHeight - fontHeight * 2) / nScores;

      if (entryHeight > fontHeight * 2) {
        entryHeight = fontHeight * 2;
      }

//            String label;
      //Font curFont = g.getFont();
      //Font boldFont = Font.getFont(curFont.getFace(), curFont.getStyle() | Font.STYLE_BOLD, curFont.getSize());
      long recordDate;
      while ((recordDate = _currentGame.getScoreTableEntry(pos)) >= 0) {

        if (pos == curScorePosition) {
          p.setColor(ColorCodes.yellow);
        } else {
          p.setColor(ColorCodes.white);
        }

        //g.drawRect(1, yPos+1, scrWidth-3, entryHeight - 2);

        String posAndScore = "" + (pos + 1) + ". " + _currentGame.getHiScores()[pos];

        //g.setFont(boldFont);
        c.drawText(posAndScore, 3, yPos + 2 + (entryHeight - fontHeight) / 2, p);
        //g.setFont(curFont);
        p.setTextAlign(Paint.Align.RIGHT);
        c.drawText(getTimeStr(recordDate),
          scrWidth - 3, yPos + 2 + (entryHeight - fontHeight) / 2 - fontHeight, p);
        p.setTextAlign(Paint.Align.LEFT);

        yPos += entryHeight;
        pos++;
      }

      p.setColor(ColorCodes.black);

    }
  }
*/

}
