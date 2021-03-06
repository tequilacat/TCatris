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
import android.view.ViewConfiguration;

import org.tequilacat.tcatris.MainActivity;

import java.util.ArrayList;
import java.util.List;

import static org.tequilacat.tcatris.core.GameRunner.GameAction;

public final class GameView extends SurfaceView {
  private ABrickGame _currentGame;
  private Thread _gameThread;
  private boolean _gameStarted;
  private Sounds _sounds;
  private boolean _prefEnableSound;

  private final Object _layoutLock = new Object();

  // layout objects

  private final List<ClickableZone> _clickableZones = new ArrayList<>();
  private final List<Button> _buttons = new ArrayList<>();

  /**
   * game statictics area: scores and level
   */
  private final Rect _gameStatisticsArea = new Rect();

  /**
   * black area displaying
   */
  private final Rect _scoreArea = new Rect();

  /**
   * shows number of current level
   */
  private final Rect _levelArea = new Rect();

  private final Rect _buttonArea = new Rect();

  /**
   * separates color bars in score area
   */
  private float _scoreMargin;

  private final Rect _scoresClickableArea = new Rect();


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
    DragTrack.MinDragDistance = ViewConfiguration.get(context).getScaledTouchSlop();
    DragTrack.MaxTapInterval = ViewConfiguration.get(context).getJumpTapTimeout();

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

  public void setGame(ABrickGame game) {
    _currentGame = game;
  }

  public ABrickGame getGame() {
    return _currentGame;
  }

  private void gameStart(final SurfaceHolder surfaceHolder) {
    final ABrickGame game = getGame();
    _dragStates = new GameRunner.DragStates();
    Debug.print("game start");

    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
    _prefEnableSound = preferences.getBoolean(GameConstants.PREF_SOUND_ENABLE, false);
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

  /**
   * stores info on dragged direction button
   */
  static class DragTrack {
    /** min dist in pixels required to start drag */
    public static int MinDragDistance;
    private static int MaxTapInterval;

    private final DragAxis _dragType;
    private final int _stepDistance;

    /**
     * pointer id for which this track currently registers offset
     */
    public int pointerId;
    private int _startX;
    private int _startY;
    private int _lastStoredX;
    private int _lastStoredY;
    private long _startDragTime;
    private boolean _considerSingleTap;
    private boolean _draggedMinDistance;

    public DragTrack(DragAxis dragType, int distance) {
      this._dragType = dragType;
      _stepDistance = distance;
      pointerId = -1;
    }

    public void start(int x, int y, int pointerId) {
      this.pointerId = pointerId;
      _draggedMinDistance = false;
      _considerSingleTap = true;
      _startX = x;
      _startY = y;
      _startDragTime = System.currentTimeMillis();
    }

    public boolean isStarted() {
      return pointerId >= 0;
    }

    public void stop() {
      pointerId = -1;
      _considerSingleTap &= (System.currentTimeMillis() - _startDragTime) < MaxTapInterval;
//      if(_considerSingleTap) {
//        Debug.print("Singletap: "+Math.abs(_lastStoredX - _startX) +" < " +  MinDragDistance);
//      }
    }

    public DragProgress dragTo(int newX, int newY) {
      // if drag ever exceeds minDragDistance we return inProgress
      _lastStoredX = newX;
      _lastStoredY = newY;

      if(!_draggedMinDistance && Math.abs(_lastStoredX - _startX) > MinDragDistance) {
        // reset dx, dy, allow dragging
        _draggedMinDistance = true;
        _considerSingleTap = false;
        _startX = newX;
        _startY = newY;
      }

      return _draggedMinDistance ? DragProgress.InProgress : DragProgress.None;
    }

    public boolean isSingleTap() {
      return _considerSingleTap;
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

  enum DragProgress {
    None, InProgress, SingleTap
  }

  /**
   * for drag event returns the action for this drag gesture,
   * e.g. dragging specific button left or right will return LEFT or RIGHT actions
   * after dragging for certain distance.
   *
   * If drag resulted in action, returns this action and _lastDragActionCount is set to number of actions to take
   * @param event touch event to process
   * @return whether user has dragged a draggable zone
   */
  private DragProgress trackDrag(MotionEvent event) {
    DragProgress dragResult = DragProgress.None;
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
          if(track.isSingleTap()) {
            // Debug.print("   -- " + track._dragType + " is single "+track.);
            dragResult = DragProgress.SingleTap;
          } else {
            dragResult = DragProgress.InProgress;
          }

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
          if(track.dragTo(x, y)==DragProgress.InProgress) {
            dragResult = DragProgress.InProgress;
          }
        }
      }
    }

    return dragResult;
  }

  /**
   * detects game action from touch event and sends it to game thread
   * @param event touch event to process
   * @return true
   */
  @Override
  public boolean onTouchEvent(MotionEvent event) {

    DragProgress dragResult = trackDrag(event);

    int actionId = MotionEventCompat.getActionMasked(event);

    if (dragResult == DragProgress.InProgress) {
      sendAction(GameAction.DRAG, null);

    } else if (dragResult == DragProgress.SingleTap ||
      (actionId == MotionEvent.ACTION_UP || actionId == MotionEvent.ACTION_POINTER_UP)) {

      int index = MotionEventCompat.getActionIndex(event);
      int eventX = (int) MotionEventCompat.getX(event, index), eventY = (int) MotionEventCompat.getY(event, index);
      ClickableZone zone = getZoneAt(eventX, eventY);

      if (zone != null) {
        if (zone.type == ClickableZoneType.DROP_BUTTON) {
          sendAction(GameAction.DROP, null);

        } else if (zone.type == ClickableZoneType.PAUSE_BUTTON) {
          ((MainActivity) getContext()).showScores();

        } else if (zone.type == ClickableZoneType.AXIS_BUTTON) {
//           get the direction and send action with specified impulse
          GameImpulse impulse = getGame().getAxisImpulse(zone.axis, zone.isPositiveDirection);
          if (impulse != null) {
            sendAction(GameAction.IMPULSE, impulse);
          }

        }
      }
    }

    return true;
  }

  private GameRunner.DragStates _dragStates;

  /**
   * udpates drag states and sends it and action to game thread
   * @param action action to pass to game thread
   * @param impulse sent non-null only when action is GameAction.IMPULSE
   */
  private void sendAction(GameAction action, GameImpulse impulse) {
    for (DragTrack dt : _tracksByType) {
      _dragStates.setState(dt.getDragType(), dt.isStarted(), dt.isStarted()? dt.getDragValue() : 0);
    }

    _gameRunner.sendAction(action, impulse, _dragStates);
  }

  enum ClickableZoneType {
    DROP_BUTTON, PAUSE_BUTTON, AXIS_BUTTON,
  }

  static class ClickableZone {
    private DragAxis axis = null;
    private boolean isPositiveDirection = false;
    public Rect rect;
    public ClickableZoneType type;

    public ClickableZone(ClickableZoneType type, Rect rect) {
      this.type = type;
      this.rect = new Rect(rect);
    }

    public ClickableZone(Rect rect, DragAxis axis, boolean isPositiveDirection) {
      this(ClickableZoneType.AXIS_BUTTON, rect);
      this.axis = axis;
      this.isPositiveDirection = isPositiveDirection;
    }
  }

  static class Button {
    //public final Ui.ButtonGlyph[] glyphs;
    public final ABrickGame.ImpulseSemantics[] arrowSemantics;
    public final Rect rect;
    public final DragAxis dragType;

    public Button(DragAxis dragType, ABrickGame.ImpulseSemantics[] semantics, int x, int y, int w, int h) {
      this.dragType = dragType;
      this.arrowSemantics = semantics;
      rect = new Rect(x, y, x + w, y + h);
    }
  }

  /**
   * computes all areas to be displayed on screen
   * @param w width of game screen
   * @param h height of game screen
   */
  private void layoutGameScreen(int w, int h) {
    // define drag factors (pixels to cell movements) as fraction of screen dimensions
    ABrickGame game = getGame();

    if (game != null) {
      synchronized (_layoutLock) {
        _tracksByType = new DragTrack[]{
            new DragTrack(DragAxis.ROTATE, (int) (w * 0.4 / 12)), // rotations per btn
            new DragTrack(DragAxis.HORIZONTAL, (int) (w * 0.4 / game.getMoveDimension() / 2)), // [W]*2 movements per button
        };

        _scoreMargin = VisualResources.Defaults.HEADER_FONT_SIZE / 3;
        int scoreMargin = (int) _scoreMargin;
        int scoreAreaH = scoreMargin * 7;
        int levelX = scoreMargin, statTop = scoreMargin;

        // find width
        _levelArea.set(levelX, statTop,
            levelX + (int) Ui.getTextWidth(VisualResources.Defaults.HEADER_FONT_SIZE, "00") + scoreMargin * 2,
            statTop + scoreAreaH);

        _scoreArea.set(_levelArea.right + scoreMargin, statTop, w - scoreMargin, statTop + scoreAreaH);

        // compute proportional sizes of painted screen components
        _gameStatisticsArea.set(0, 0, w, _scoreArea.bottom);

        _buttonArea.set(0, h - VisualResources.Defaults.BUTTONAREA_HEIGHT, w, h);
        LayoutParameters layoutParams = new LayoutParameters();
        layoutParams.GameArea = new Rect(0, _gameStatisticsArea.bottom, w, _buttonArea.top);
        game.layout(layoutParams);

        int buttonWidth;
        final boolean LOWER_DROP_BTN = false;
        Button dropButton;

        if (LOWER_DROP_BTN) {
          buttonWidth = w / 5;
          dropButton = new Button(null, new ABrickGame.ImpulseSemantics[]{
              ABrickGame.ImpulseSemantics.MOVE_DOWN},
              _buttonArea.left + buttonWidth * 2, _buttonArea.top, buttonWidth, _buttonArea.height());
        } else {
          buttonWidth = w / 4;
          dropButton = null;
        }

        Button dragRotateButton = new Button(DragAxis.ROTATE,
            new ABrickGame.ImpulseSemantics[]{
                game.getImpulseSemantics(game.getAxisImpulse(DragAxis.ROTATE, false)),
                game.getImpulseSemantics(game.getAxisImpulse(DragAxis.ROTATE, true)),
            }, _buttonArea.left, _buttonArea.top, buttonWidth * 2, _buttonArea.height());

        Button dragMoveButton = new Button(DragAxis.HORIZONTAL,
            new ABrickGame.ImpulseSemantics[]{
                game.getImpulseSemantics(game.getAxisImpulse(DragAxis.HORIZONTAL, false)),
                game.getImpulseSemantics(game.getAxisImpulse(DragAxis.HORIZONTAL, true)),
            }, _buttonArea.right - buttonWidth * 2, _buttonArea.top, buttonWidth * 2, _buttonArea.height());

        GameScreenLayout gsl = game.getGameScreenLayout();
        // to display drop inside Next frame,
        // diff between minScoreBottom and buttons top must be at least buttonsHeight + nextWidth
        _scoresClickableArea.set(gsl.getNextShapeRect());
        int paddingMargin = (int) (+VisualResources.Defaults.ROUNDED_FRAME_MARGIN
                        + VisualResources.Defaults.ROUNDED_FRAME_PADDING);
        // expand by margin so clickable area covers margins around next field

        Ui.expand(_scoresClickableArea, paddingMargin);
        // resize nextfig bottom to button area to have half button height in between
        // but not lower than main field (otherwise looks ugly)
        _scoresClickableArea.bottom = Math.min(gsl.getFieldRect().bottom + paddingMargin,
            _buttonArea.top - _buttonArea.height() / 2);


        _buttons.clear();
        _buttons.add(dragRotateButton);
        _buttons.add(dragMoveButton);

        _clickableZones.clear();
        _clickableZones.add(new ClickableZone(ClickableZoneType.DROP_BUTTON, _scoresClickableArea));
        Rect clickableFieldArea = new Rect(gsl.getFieldRect());
        Ui.expand(clickableFieldArea, paddingMargin);
        _clickableZones.add(new ClickableZone(ClickableZoneType.PAUSE_BUTTON, clickableFieldArea));

        if (dropButton != null) {
          _buttons.add(dropButton);
          _clickableZones.add(new ClickableZone(ClickableZoneType.DROP_BUTTON, dropButton.rect));
        }

        // add zones for halves of axis buttons
        // Rotate button
        Rect btnRect = dragRotateButton.rect;
        _clickableZones.add(new ClickableZone(new Rect(btnRect.left, btnRect.top,
            btnRect.left + btnRect.width() / 2, btnRect.bottom), DragAxis.ROTATE, false));
        _clickableZones.add(new ClickableZone(new Rect(btnRect.left + btnRect.width() / 2, btnRect.top,
            btnRect.right, btnRect.bottom), DragAxis.ROTATE, true));

        // Move button
        btnRect = dragMoveButton.rect;
        _clickableZones.add(new ClickableZone(new Rect(btnRect.left, btnRect.top,
            btnRect.left + btnRect.width() / 2, btnRect.bottom), DragAxis.HORIZONTAL, false));
        _clickableZones.add(new ClickableZone(new Rect(btnRect.left + btnRect.width() / 2, btnRect.top,
            btnRect.right, btnRect.bottom), DragAxis.HORIZONTAL, true));

        //Debug.print("do game view_scores [" + getGame().getDescriptor().getId() + "]");
      }
    }
  }

  /**
   * paints field or whole game screen
   * @param c canvas to draw on
   * @param repaintAll whether to redraw all game screen, not only field
   */
  private void paintGameScreen(Canvas c, boolean repaintAll, DynamicState dynamicState) {
    //repaintAll = true; Debug.print("paint all (debug)");
    // synch to avoid painting laid out components and layout itself
    synchronized (_layoutLock) {
      //final GameScreenLayout layout = getGame().getGameScreenLayout();
      //Rect fieldRect = view_scores.getFieldRect();

      // Debug.print("paint: " + (repaintAll ? "ALL" : "field only"));
      final ABrickGame game = getGame();

//      int frameMargin = (int) VisualResources.Defaults.ROUNDED_FRAME_MARGIN,
//          framePadding = (int) VisualResources.Defaults.ROUNDED_FRAME_PADDING;

      if (repaintAll) {
        c.drawColor(VisualResources.Defaults.SCREEN_BG_COLOR);

        Ui.drawRoundedArea(c, game.getGameScreenLayout().getFieldRect(), Ui.FramePosition.AROUND);
        Ui.drawRoundedArea(c, _scoresClickableArea, Ui.FramePosition.INSIDE);
        game.paintNext(c);

        // drop arrow below next fig
        Rect nextRect = game.getGameScreenLayout().getNextShapeRect();
        int dropX = nextRect.left, dropWidth = nextRect.width();
        int dropBottom = (int) (_scoresClickableArea.bottom
            - VisualResources.Defaults.ROUNDED_FRAME_MARGIN
            - VisualResources.Defaults.ROUNDED_FRAME_PADDING);
        int dropY = Math.max(nextRect.bottom, dropBottom - dropWidth);
        Ui.drawGlyph(c, dropX, dropY, dropWidth, dropBottom - dropY, ABrickGame.ImpulseSemantics.MOVE_DOWN);

        Scoreboard.GameScores gs = Scoreboard.instance().getGameScores(game.getDescriptor().getId());
        Ui.paintScores(c, game.getScore(), gs.getMaxScore(), _scoreArea.left, _scoreArea.top,
            _scoreArea.width(), _scoreArea.height(), _scoreMargin);

        Ui.fillRect(c, _levelArea, VisualResources.Defaults.SCORE_BG_COLOR);
        Ui.drawText(c, Integer.toString(game.getLevel()),
            _levelArea.left + (int) _scoreMargin,
            (_levelArea.top + _levelArea.bottom)/2,
            //_levelArea.top + (int) _scoreMargin,
            VisualResources.Defaults.HEADER_FONT_SIZE, VisualResources.Defaults.SCORE_CUR_TEXTCOLOR_ALONE);

        // paint buttons

        int buttonMargin = 4;

        for (Button btn : _buttons) {
          int btnHeight = btn.rect.height();
          int glyphAreaWidth = btn.rect.width() / btn.arrowSemantics.length, gX = btn.rect.left + glyphAreaWidth / 2;
          float btnRadius = btnHeight * 0.45f;
          int gliphSide = (int) (btnRadius * 1.5f);

          int roundedX = btn.rect.left, roundedY = btn.rect.top,
              roundedWidth = glyphAreaWidth, roundedHeight = btn.rect.height();

          for (ABrickGame.ImpulseSemantics buttonGlyph : btn.arrowSemantics) {
            Ui.drawRoundedButton(c, roundedX + buttonMargin, roundedY + buttonMargin,
                roundedWidth - buttonMargin - buttonMargin,
                roundedHeight - buttonMargin - buttonMargin, ColorCodes.red, 0xffffbaba);

            //Ui.drawGlyph(c, gX - gliphSide / 2, btn.rect.top + btnHeight / 2 - gliphSide / 2,
            Ui.drawGlyph(c, gX - gliphSide / 2, btn.rect.top + btnHeight / 2 - gliphSide / 2,
                gliphSide, gliphSide, buttonGlyph);
            gX += glyphAreaWidth;
            roundedX += glyphAreaWidth;
          }
        }
      }

      getGame().paintField(c, dynamicState);

    /*
    Rect field = layout.getFieldRect();
    int scoreX = field.left + 20, scoreY = field.top + 20, scoreW = field.width() - scoreX * 2;
    int scoreH = _scoreArea.height();
    int dy = scoreH + 20;

    // no scores
    Ui.paintScores(c, 0, 0, scoreX, scoreY, scoreW, scoreH, _scoreMargin); scoreY+= dy;
    // no scores and have top scores
    Ui.paintScores(c, 0, 1000, scoreX, scoreY, scoreW, scoreH, _scoreMargin); scoreY+= dy;
    // 10, 0 - no top scores
    Ui.paintScores(c, 10, 0, scoreX, scoreY, scoreW, scoreH, _scoreMargin); scoreY+= dy;
    // 10, 1000 - less than top, width too small to display over bar
    Ui.paintScores(c, 10, 1000, scoreX, scoreY, scoreW, scoreH, _scoreMargin); scoreY+= dy;
    // 800,1000 - enough space to display over bar
    Ui.paintScores(c, 200, 1000, scoreX, scoreY, scoreW, scoreH, _scoreMargin); scoreY+= dy;
    // 1000, 1000 - equals
    Ui.paintScores(c, 1000, 1000, scoreX, scoreY, scoreW, scoreH, _scoreMargin); scoreY+= dy;
    // 1000, 800 - more than top
    Ui.paintScores(c, 1000, 800, scoreX, scoreY, scoreW, scoreH, _scoreMargin); scoreY+= dy;
    */
    }
  }

}
