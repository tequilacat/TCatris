package org.tequilacat.tcatris.core;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.preference.PreferenceManager;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import org.tequilacat.tcatris.MainActivity;
import org.tequilacat.tcatris.R;

import java.util.ArrayList;
import java.util.List;

import static org.tequilacat.tcatris.core.GameRunner.GameAction;

public final class GameView extends SurfaceView {

  private Tetris _currentGame;
  private Thread _gameThread;

  private boolean _prefEnableSound;
  private boolean _prefShowDropTarget;

  private boolean _gameStarted;
  Sounds _sounds;

  private GameRunner _gameRunner = new GameRunner() {
    @Override
    public void onPaintGameScreen(Canvas c, boolean repaintAll, DynamicState dynamicState) {
      paintGameScreen(c, repaintAll, dynamicState);
    }

    @Override
    protected void playViewSoundEffect(final Sounds.Id soundId) {
      if(_prefEnableSound) {
        _sounds.play(soundId);
      }
    }

    @Override
    public void onGameLost() {
      // called from background thread
      final MainActivity mainActivity = (MainActivity) getContext();
      mainActivity.runOnUiThread(new Runnable() {
        @Override
        public void run() {
          mainActivity.showScores();
        }
      });
    }
  };

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
    _sounds = new Sounds(context);

    SurfaceHolder _holder = getHolder();
    _holder.addCallback(new SurfaceHolder.Callback() {
      @Override
      public void surfaceCreated(SurfaceHolder holder) {
        Debug.print("surfaceCreated");
        // start game
        _gameStarted = false;
        //gameStart(holder);
      }

      @Override
      public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Debug.print("surfaceChanged");
        layoutGameScreen(width, height);

        if (!_gameStarted) {
          _gameStarted = true;
          gameStart(holder);
        }
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
  }

  public Tetris getGame() {
    return _currentGame;
  }

  private void gameStart(final SurfaceHolder surfaceHolder) {
    final Tetris game = getGame();
    _dragStates = new GameRunner.DragStates();
    Debug.print("game start");

    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
    _prefEnableSound = preferences.getBoolean("pref_sound_enable", false);
    _prefShowDropTarget = preferences.getBoolean("pref_show_droptarget", false);
    game.initSettings(preferences);

    _gameThread = new Thread() {
      @Override
      public void run() {
        _gameRunner.runGame(surfaceHolder, game);
      }
    };

    _gameThread.start();
  }

  private void gameStop() {
    _gameRunner.stop();
    boolean stopped = false;

    while(!stopped) {
      try {
        _gameThread.join();
        _gameThread = null;
        stopped = true;
      } catch (InterruptedException e) { }
    }
  }

/*
  private static Ui.ButtonGlyph[] BTN_GLYPHS = new Ui.ButtonGlyph[] {
    Ui.ButtonGlyph.LEFT, Ui.ButtonGlyph.RCCW, Ui.ButtonGlyph.DROP, Ui.ButtonGlyph.RCW, Ui.ButtonGlyph.RIGHT
  };

  private static final GameAction[] BUTTON_ACTIONS = new GameAction[]{
    GameAction.LEFT, GameAction.ROTATE_CCW, GameAction.DROP, GameAction.ROTATE_CW, GameAction.RIGHT
  };
*/

  /**
   * stores info on dragged direction button
   */
  static class DragTrack {

    private final DragAxis _dragType;
    private final int _stepDistance;

    // pointer id for which this track currently registers offset
    public int pointerId;

    // last coordinates
    //private int lastX, lastY;

    //GameAction _lastAction;
    //int _lastActionCount;

    private int _startX;
    private int _startY;
    private int _lastStoredX;
    private int _lastStoredY;

    public DragTrack(DragAxis dragType, int distance) {
      this._dragType = dragType;
      _stepDistance = distance;
      pointerId = -1;
    }

    public void start(int x, int y, int pointerId) {
      this.pointerId = pointerId;
      _startX = x;
      _startY = y;
//      lastX = x;
//      lastY = y;
    }

    public boolean isStarted() {
      return pointerId >= 0;
    }

    public void stop() {
      pointerId = -1;
    }

    public void dragTo(int newX, int newY) {
      _lastStoredX = newX;
      _lastStoredY = newY;

      // only X is currently processed
      /*
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
      */
    }

    public DragAxis getDragType() {
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

  private ClickableZone getZoneAt(int x, int y) {
    ClickableZone foundZone = null;

    for (ClickableZone zone : _clickableZones) {
      if (zone.rect.contains(x, y)) {
        foundZone = zone;
        break;
      }
    }

    return foundZone;
  }

  private Button getButtonAt( int x, int y){
    Button foundButton = null;

    for(Button btn  : _buttons) {
      if (btn.rect.contains(x, y)) {
        foundButton = btn;
        break;
      }
    }

    return foundButton;
  }

  private DragTrack[] _tracksByType;

  /**
   * for drag event returns the action for this drag gesture,
   * e.g. dragging specific button left or right will return LEFT or RIGHT actions
   * after dragging for certain distance.
   *
   * If drag resulted in action, returns this action and _lastDragActionCount is set to number of actions to take
   * @param event
   * @return whether user has dragged a draggable zone
   */
  private boolean trackDrag(MotionEvent event) {
    boolean dragHappened = false;
    //GameAction dragAction = null;
    int action = MotionEventCompat.getActionMasked(event);

    if(action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_DOWN){
      int index = MotionEventCompat.getActionIndex(event);
      int pointerId = MotionEventCompat.getPointerId(event, index);
//      Debug.print(">>> Event AD/APD #" + action + " (" + event.getAction() + "): index = " + index + " of " + MotionEventCompat.getPointerCount(event));
      int x = (int) MotionEventCompat.getX(event, index), y = (int) MotionEventCompat.getY(event, index);

      Button btn = getButtonAt(x, y);

      if (btn != null && btn.dragType != null) {
        // find drag dragType (button ID) in currently tracked list,
        // if does not exist we remember it, if the button is already dragged we ignore it
        // that's all for ACTION_DOWN
        DragTrack found = null;

        for (DragTrack track : _tracksByType) {
          if (track.getDragType() == btn.dragType && !track.isStarted()) {
            found = track;
            break;
          }
        }

        if (found != null) {
          // Debug.print("  ++ " + btn.dragType + ", id = " + pointerId + " [index = " + index + "]");
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
          dragHappened = true;
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
          track.dragTo(x, y);
          dragHappened = true;
        }
      }
    }

    return dragHappened;
  }

  /**
   * detects game action from touch event and sends it to game thread
   * @param event
   * @return true
   */
  @Override
  public boolean onTouchEvent(MotionEvent event) {

    boolean hasDragged = trackDrag(event);

    if(hasDragged) {
      sendAction(GameAction.DRAG);

    }else {
      int actionId = MotionEventCompat.getActionMasked(event);

      if (actionId == MotionEvent.ACTION_DOWN || actionId == MotionEvent.ACTION_POINTER_DOWN) {

        int index = MotionEventCompat.getActionIndex(event);
        int eventX = (int) MotionEventCompat.getX(event, index), eventY = (int) MotionEventCompat.getY(event, index);
        ClickableZone zone = getZoneAt(eventX, eventY);

        if (zone != null) {
          if (zone.type == ClickableZoneType.DROP_BUTTON) {
            sendAction(GameAction.DROP);
          } else if (zone.type == ClickableZoneType.PAUSE_BUTTON) {
            ((MainActivity) getContext()).showScores();
          }
        }
      }
    }

    return true;
  }

  private GameRunner.DragStates _dragStates;

  /**
   * udpates drag states and sends it and action to game thread
   * @param action
   */
  private void sendAction(GameAction action) {
    for (DragTrack dt : _tracksByType) {
      _dragStates.setState(dt.getDragType(), dt.isStarted(), dt.isStarted()? dt.getDragValue() : 0);
    }

    _gameRunner.sendAction(action, _dragStates);
  }

  enum ClickableZoneType {
    DROP_BUTTON, PAUSE_BUTTON,
  }

  static class ClickableZone {
    public Rect rect;
    public ClickableZoneType type;

    public ClickableZone(ClickableZoneType type, Rect rect) {
      this.type = type;
      this.rect = new Rect(rect);
    }
  }

  static class Button {
    public final Ui.ButtonGlyph glyph;
    public final Rect rect;
    public final DragAxis dragType;

    public Button(DragAxis dragType, Ui.ButtonGlyph glyph, int x, int y, int w, int h) {
      this.dragType = dragType;
      this.glyph = glyph;
      rect = new Rect(x, y, x + w, y + h);
    }
  }

  private final List<ClickableZone> _clickableZones = new ArrayList<>();
  private final List<Button> _buttons = new ArrayList<>();

  private Rect _scoreBarArea = new Rect();

  /**
   * computes all areas to be displayed on screen
   * @param w
   * @param h
   */
  private void layoutGameScreen(int w, int h) {
    // define drag factors (pixels to cell movements) as fraction of screen dimensions

    _tracksByType = new DragTrack[]{
      new DragTrack(DragAxis.ROTATE, (int) (w *0.4 / 12)), // rotations per btn
      new DragTrack(DragAxis.HORIZONTAL, (int) (w * 0.4 / getGame().getWidth() / 2)), // [W]*2 movements per button
    };

    // compute proportional sizes of painted screen components
    _scoreBarArea.set(0, 0, w, VisualResources.Defaults.HEADER_FONT_SIZE);

    int buttonHeight = h / 10, buttonY = h - buttonHeight, buttonWidth = w / 5, buttonX = 0;

    _buttons.clear();
    _buttons.add(new Button(DragAxis.ROTATE, Ui.ButtonGlyph.RCCW, buttonX, buttonY, buttonWidth * 2, buttonHeight));
    _buttons.add(new Button(null, Ui.ButtonGlyph.DROP, buttonX + buttonWidth * 2, buttonY, buttonWidth, buttonHeight));
    _buttons.add(new Button(DragAxis.HORIZONTAL, Ui.ButtonGlyph.RIGHT, buttonX + buttonWidth * 3, buttonY, buttonWidth * 2, buttonHeight));

    LayoutParameters layoutParams = new LayoutParameters();
    layoutParams.GameArea = new Rect(0, _scoreBarArea.bottom, w, h - buttonHeight - _scoreBarArea.height());

    _clickableZones.clear();
    _clickableZones.add(new ClickableZone(ClickableZoneType.DROP_BUTTON, _buttons.get(1).rect));
    _clickableZones.add(new ClickableZone(ClickableZoneType.PAUSE_BUTTON, layoutParams.GameArea));

    Debug.print("do game view_scores [" + getGame().getDescriptor().getId() + "]");
    getGame().layout(layoutParams);
  }


  /**
   * paints field or whole game screen
   * @param c
   * @param repaintAll
   */
  private void paintGameScreen(Canvas c, boolean repaintAll, DynamicState dynamicState) {
    //repaintAll = true; Debug.print("paint all (debug)");

    final GameScreenLayout layout = getGame().getGameScreenLayout();
    //Rect fieldRect = view_scores.getFieldRect();

    // Debug.print("paint: " + (repaintAll ? "ALL" : "field only"));

    if(repaintAll) {
      c.drawColor(VisualResources.Defaults.SCREEN_BG_COLOR);

      //Rect next = view_scores.getNextShapeRect();
      getGame().paintNext(c);

      // TODO paint scores as bar
      Ui.fillRect(c, _scoreBarArea, VisualResources.Defaults.SCORE_BG_COLOR);
      Ui.drawText(c, String.format("%s %s: %d", getGame().getDescriptor().getLabel(), getContext().getString(R.string.msg_score),
              getGame().getScore()), _scoreBarArea.left, _scoreBarArea.top,
          VisualResources.Defaults.HEADER_FONT_SIZE, VisualResources.Defaults.SCORE_TEXT_COLOR);

      // paint buttons
      for(Button btn : _buttons) {
        Ui.draw3dRect(c, btn.rect);
        int side = Math.min(btn.rect.width(), btn.rect.height());
        Ui.drawGlyph(c, btn.rect.left + (btn.rect.width() - side) / 2,
          btn.rect.top + (btn.rect.height() - side) / 2,
          side, side, btn.glyph);
      }
    }

    getGame().paintField(c, dynamicState);
  }

}
