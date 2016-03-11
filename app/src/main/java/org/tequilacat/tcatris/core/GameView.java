package org.tequilacat.tcatris.core;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import org.tequilacat.tcatris.MainActivity;
import org.tequilacat.tcatris.R;

import java.util.ArrayList;
import java.util.List;

public final class GameView extends SurfaceView {

  private final Object _gameChangeLock = new Object();
  private Tetris _currentGame;
  private GameAction _gameThreadAction;
  private boolean _isPaused;
  private boolean _isRunning;
  private Thread _gameThread;
  private DragStates _dragStates;

  DynamicState _bgThreadDynamicState = new DynamicState(2);

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
        Debug.print("surfaceCreated");
        // start game
        gameStart();
      }

      @Override
      public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Debug.print("surfaceChanged");
        layoutGameScreen(width, height);
      }

      @Override
      public void surfaceDestroyed(SurfaceHolder holder) {
        Debug.print("surfaceDestroyed");
        gameStop();
      }
    });

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
    restartGame();
  }

  /**
   * Just reinit current game , no thread work here
   */
  public void restartGame() {
    _dragStates = new DragStates();// now no info on current drag state available to game thread

    getGame().initGame();
    // create new slot
    Scoreboard.instance().getGameScores(getGame().getDescriptor().getId()).setScore(0);
  }

  public Tetris getGame() {
    return _currentGame;
  }

  /** reset timer to wait whole cycle */
  final static long WAIT_CYCLE = -1;

  private void runGame() {
    // runs
    long INTERVAL = 500; // 300 millis per step

    // on 1st iteration just display screen
    _gameThreadAction = GameAction.UNPAUSE;
    _isRunning = true;
    _isPaused = false;
    long nextTickMoment = WAIT_CYCLE;

    synchronized (_gameChangeLock) {
      try {
        while (_isRunning) {
          GameAction curAction = _gameThreadAction;
          _gameThreadAction = null;

          if (_isPaused) {
            Debug.print("Paused: exit game thread");
            break;
          }

          if (getGame().getState() == Tetris.LOST) {
            Debug.print("Lost: show scores, exit game thread");
            final MainActivity mainActivity = (MainActivity) getContext();
            mainActivity.runOnUiThread(new Runnable() {
              @Override
              public void run() {
                mainActivity.showScores();
              }
            });
            break;
          }

          ScreenPaintType repaintType;

          if (curAction == null) {
            repaintType = ScreenPaintType.FULLSCREEN;
            nextTickMoment = WAIT_CYCLE;

          } else if (curAction == GameAction.UNPAUSE) {
            nextTickMoment = WAIT_CYCLE;
            repaintType = ScreenPaintType.FULLSCREEN;

          } else { // ACTIVE: run action, see consequences

//            Debug.print(String.format("Move: %s Rotate: %s",
//              (_dragStates.isActive(Button.ButtonType.HORIZONTAL) ?
//                ("" + _dragStates.getValue(Button.ButtonType.HORIZONTAL)) : "NONE"),
//              (_dragStates.isActive(Button.ButtonType.ROTATE) ?
//                ("" + _dragStates.getValue(Button.ButtonType.ROTATE)) : "NONE")
//            ));

            // run action
            switch (curAction) {
              case DRAG:
                repaintType = ScreenPaintType.FIELD_ONLY;
                break;

              case ADVANCE:
              case DROP:
                boolean gameStateChanged = getGame().nextState(curAction == GameAction.DROP);
                Scoreboard.instance().getGameScores(getGame().getDescriptor().getId()).setScore(getGame().getScore());
                repaintType = gameStateChanged ? ScreenPaintType.FULLSCREEN : ScreenPaintType.FIELD_ONLY;
                nextTickMoment = WAIT_CYCLE; // reset timer to wait next
                break;

              case LEFT:
                repaintType = getGame().doAction(GameImpulse.MOVE_LEFT) ? ScreenPaintType.FIELD_ONLY : null;
                break;
              case RIGHT:
                repaintType = getGame().doAction(GameImpulse.MOVE_RIGHT) ? ScreenPaintType.FIELD_ONLY : null;
                break;
              case ROTATE_CW:
                repaintType = getGame().doAction(GameImpulse.ROTATE_CW) ? ScreenPaintType.FIELD_ONLY : null;
                break;
              case ROTATE_CCW:
                repaintType = getGame().doAction(GameImpulse.ROTATE_CCW) ? ScreenPaintType.FIELD_ONLY : null;
                break;
              default:
                repaintType = null;
                break;
            }
          }

          long now = System.currentTimeMillis();
          paintScreen(repaintType);

          if(nextTickMoment == WAIT_CYCLE) {
            nextTickMoment = now + INTERVAL;// after debug use
          }
          if (nextTickMoment <= now) {
            nextTickMoment = now + 1;
          }

          //Debug.print("Sleep " + (nextTickMoment - now));

          _gameThreadAction = GameAction.ADVANCE;
          _gameChangeLock.wait(nextTickMoment - now);
        }
      } catch (InterruptedException e) {
        // TODO process exception
        Debug.print("Thread interrupted: " + e);
      }
    }
  }

  private void gameStart() {
    Debug.print("game start");
    _gameThread = new Thread() {
      @Override
      public void run() {
        runGame();
      }
    };
    _gameThread.start();
  }

  private void gameStop() {
    _isRunning = false;
    sendAction(null);
    boolean stopped = false;

    while(!stopped) {
      try {
        _gameThread.join();
        _gameThread = null;
        stopped = true;
      } catch (InterruptedException e) { }
    }
  }

  enum GameAction {
    LEFT, RIGHT, ROTATE_CW, ROTATE_CCW, DROP, UNPAUSE, DRAG, ADVANCE,
  }

  /**
   * sends action from GUI thread to the thread waking it from waiting
   * @param action
   */
  private void sendAction(GameAction action){

    synchronized (_gameChangeLock) {
      _gameThreadAction = action;
      // copy to drag control instance from existing drag types

      for (DragTrack dt : _tracksByType) {
        _dragStates.setState(dt.getDragType(), dt.isStarted(), dt.isStarted()? dt.getDragValue() : 0);
      }

      _gameChangeLock.notify();
    }
  }

  public static class DragStates {
    private final boolean[] _dragStatuses = new boolean[Button.ButtonType.values().length];
    private final double[] _dragDeltas = new double[Button.ButtonType.values().length];

    public void setState(final Button.ButtonType type, boolean newState, double value) {
      final int pos = type.ordinal();
      _dragDeltas[pos] = value;
      _dragStatuses[pos] = newState;
    }

    public boolean isActive(final Button.ButtonType type) {
      return _dragStatuses[type.ordinal()];
    }

    public double getValue(final Button.ButtonType type) {
      return _dragStatuses[type.ordinal()] ? _dragDeltas[type.ordinal()] : 0;
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

    private final Button.ButtonType _dragType;
    private final int _stepDistance;

    // pointer id for which this track currently registers offset
    public int pointerId;

    // last coordinates
    private int lastX, lastY;

    GameAction _lastAction;
    int _lastActionCount;

    private int _startX;
    private int _startY;
    private int _lastStoredX;
    private int _lastStoredY;

    public DragTrack(Button.ButtonType dragType, GameAction onPositive, GameAction onNegative, int distance) {
      this._dragType = dragType;
      _positiveOffsetAction = onPositive;
      _negativeOffsetAction = onNegative;
      _stepDistance = distance;
      pointerId = -1;
    }

    public void start(int x, int y, int pointerId) {
      this.pointerId = pointerId;
      _startX = x;
      _startY = y;
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
      _lastStoredX = newX;
      _lastStoredY = newY;

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

    public Button.ButtonType getDragType() {
      return _dragType;
    }

    public double getDragValue() {
      // consider
      final double value;

      if(!isStarted()){
        value=0;
      }else {
        // consider x only, may be reimplemented to be vertical otherwise
        value = (_lastStoredX - _startX) / (double) _stepDistance;
      }
      return value;
    }
  }

  private DragTrack[] _tracksByType;

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
      //DragType _dragType = buttonType==null ? null : (buttonType== Button.ButtonType.HORIZONTAL ? DragType.MOVE_LEFTRIGHT : )
      if(buttonType== Button.ButtonType.DROP) {
        dragAction = GameAction.DROP;

      }else if (buttonType != null) {

        // find drag type (button ID) in currently tracked list,
        // if does not exist we remember it, if the button is already dragged we ignore it
        // that's all for ACTION_DOWN
        DragTrack found = null;

        for (DragTrack track : _tracksByType) {
          if (track.getDragType() == buttonType && !track.isStarted()) {
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
          //Debug.print("   -- " + track._dragType);
          break;
        }
      }

    } else if (action == MotionEvent.ACTION_MOVE) {
      for(DragTrack track : _tracksByType) {
        // check if is dragging, get its pointer ID,
        if(track.isStarted()) {
          int pointerIndex = MotionEventCompat.findPointerIndex(event, track.pointerId);
          int x = (int) MotionEventCompat.getX(event, pointerIndex), y = (int) MotionEventCompat.getY(event, pointerIndex);

          //track.dragTo(x, y);
          dragAction = track.dragTo(x, y);

          if(COMPUTE_DRAGS) {
            dragAction = GameAction.DRAG;

          }else {
            // this code is for old style

            if (dragAction != null) {
              break;
            }
          }
        }
      }
    }

    return dragAction;
  }

  private static final boolean COMPUTE_DRAGS = false;

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

          if (getGame().getGameScreenLayout().getFieldRect().contains(eventX, eventY)) {
            // check field click - set pause
            // setPaused(true);
            ((MainActivity)getContext()).showScores();
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
  public void setPaused(boolean isPaused) {
    _isPaused = isPaused;
    sendAction(_isPaused ? null : GameAction.UNPAUSE);
  }

  /**
   * checks if current game is paused
   * @return
   */
  public boolean isPaused() {
    return _isPaused;
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

    // define drag factors (pixels to cell movements) as fraction of screen dimensions
    _tracksByType = new DragTrack[]{
      new DragTrack(Button.ButtonType.ROTATE, GameAction.ROTATE_CCW, GameAction.ROTATE_CW,
        (int)(w *0.4 / 10)), // 10 rotations per btn
      new DragTrack(Button.ButtonType.HORIZONTAL, GameAction.RIGHT, GameAction.LEFT,
        (int)(h * 0.4 / getGame().getWidth() / 2)), // [W]*2 movements per button
    };

    // compute proportional sizes of painted screen components
    _fontSize = getResources().getDimensionPixelSize(R.dimen.gameinfo_font_size);
    final int scoreHeight = getLineHeight();
    final int buttonHeight = h / 10;

    _scoreBarArea.set(0, 0, w, scoreHeight);
    int buttonAreaTop = h - buttonHeight - scoreHeight;

    int buttonH = h - buttonAreaTop, buttonW = w / 5, buttonX = 0, buttonY = buttonAreaTop;

    _buttons.clear();
    _buttons.add(new Button(Button.ButtonType.ROTATE, Ui.ButtonGlyph.RCCW, buttonX, buttonY, buttonW * 2, buttonH));
    _buttons.add(new Button(Button.ButtonType.DROP, Ui.ButtonGlyph.DROP, buttonX + buttonW * 2, buttonY, buttonW, buttonH));
    _buttons.add(new Button(Button.ButtonType.HORIZONTAL, Ui.ButtonGlyph.RIGHT, buttonX + buttonW * 3, buttonY, buttonW * 2, buttonH));

    LayoutParameters layoutParams = new LayoutParameters();
    layoutParams.MARGIN_BOTTOM = layoutParams.MARGIN_LEFT
      = layoutParams.MARGIN_RIGHT = layoutParams.MARGIN_TOP = layoutParams.SPACING_VERT
      = w / 30;
    layoutParams.GameArea = new Rect(0, _scoreBarArea.bottom, w, h - buttonHeight - scoreHeight);

    getGame().layout(layoutParams);
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
   * paints app screen according to expected info to be displayed
   * @param paintType type of info to be displayed
   */
  private void paintScreen(ScreenPaintType paintType) {
    if (paintType != null) {
      long start = System.currentTimeMillis();

      synchronized (getHolder()) {
        Canvas c = null;

        try {
          Rect updateRect;

          Rect fieldRect = getGame().getGameScreenLayout().getFieldRect();

          if (paintType != ScreenPaintType.FIELD_ONLY) {
            updateRect = null;
          } else {
            updateRect = _fieldUpdateRect;
            updateRect.set(fieldRect);
          }

          c = getHolder().lockCanvas(updateRect);
          if(c == null){
            return; // just if view is not displayed
          }

          if (updateRect != null && !updateRect.equals(fieldRect)) {
            updateRect = null;
          }

          if (paintType == ScreenPaintType.PAUSED) {
            // c.drawColor(ColorCodes.blue); // not shown anymore

//            Ui.drawGlyph(c, 10, 10, 100, 100, Ui.ButtonGlyph.LEFT);
//            Ui.drawGlyph(c, 300, 300, 100, 100, Ui.ButtonGlyph.RIGHT);

          } else if (paintType == ScreenPaintType.FAILED) {
            // c.drawColor(ColorCodes.red);

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

      long end = System.currentTimeMillis();
      //Debug.print("Repaint [" + paintType + "] took " + (end - start) + " ms");
    }
  }

  /**
   * paints field or whole game screen
   * @param c
   * @param repaintAll
   */
  private void paintGameStateScreen(Canvas c, boolean repaintAll) {
    //repaintAll = true; Debug.print("paint all (debug)");

    final GameScreenLayout layout = getGame().getGameScreenLayout();
    Rect fieldRect = layout.getFieldRect();

    // Debug.print("paint: " + (repaintAll ? "ALL" : "field only"));

    if(repaintAll) {
      c.drawColor(Ui.UI_COLOR_PANEL);

      Rect next = layout.getNextShapeRect();
      getGame().paintNext(c);

      // TODO paint scores as bar
      Ui.fillRect(c, _scoreBarArea, ColorCodes.black);
      Ui.drawText(c,
        String.format("%s %s: %d", getGame().getDescriptor().getLabel(), getContext().getString(R.string.msg_score), getGame().getScore()),
        _scoreBarArea.left, _scoreBarArea.top, _fontSize, ColorCodes.yellow);

      // paint buttons
      for(Button btn : _buttons) {
        Ui.draw3dRect(c, btn.rect);
        int side = Math.min(btn.rect.width(), btn.rect.height());
        Ui.drawGlyph(c, btn.rect.left + (btn.rect.width() - side) / 2,
                btn.rect.top + (btn.rect.height() - side) / 2,
                side, side, btn.glyph);
      }
    }

    // fill dyn state values
    // _bgThreadDynamicState.values = new double[2];
    // #0: horizontal
    // #1: rotate
    _bgThreadDynamicState.setState(0,
      _dragStates.isActive(Button.ButtonType.HORIZONTAL) ?
        DynamicState.ValueState.VALID : DynamicState.ValueState.NOT_TRACKED,
        _dragStates.getValue(Button.ButtonType.HORIZONTAL));

    _bgThreadDynamicState.setState(1,
      _dragStates.isActive(Button.ButtonType.ROTATE) ?
        DynamicState.ValueState.VALID : DynamicState.ValueState.NOT_TRACKED,
      _dragStates.getValue(Button.ButtonType.ROTATE));

    getGame().paintField(c, _bgThreadDynamicState);
  }


}
