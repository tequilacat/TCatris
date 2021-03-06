package org.tequilacat.tcatris.core;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.view.SurfaceHolder;

/**
 * Worker class processing game cycles according to
 */
public abstract class GameRunner {

  enum GameAction {
    DROP, UNPAUSE, DRAG, ADVANCE, IMPULSE,
  }

  private GameAction _gameThreadAction;
  private GameImpulse _currentImpulse;
  private final Object _gameChangeLock = new Object();
  private DynamicState _bgThreadDynamicState = new DynamicState(2);

  private boolean _isRunning;

  /** reset timer to wait whole cycle */
  final static long WAIT_CYCLE = -1;

  double[] _dtPositions = new double[DragAxis.values().length];

  public static class DragStates {
    private final boolean[] _dragStatuses = new boolean[DragAxis.values().length];
    private final double[] _dragDeltas = new double[DragAxis.values().length];

    public void setState(final DragAxis type, boolean newState, double value) {
      final int pos = type.ordinal();
      _dragDeltas[pos] = value;
      _dragStatuses[pos] = newState;
    }

    public boolean isActive(final DragAxis type) {
      return _dragStatuses[type.ordinal()];
    }

    public double getValue(final DragAxis type) {
      return _dragStatuses[type.ordinal()] ? _dragDeltas[type.ordinal()] : 0;
    }

    public void setFrom(DragStates uiDragStates) {
      System.arraycopy(uiDragStates._dragDeltas, 0, _dragDeltas, 0, _dragDeltas.length);
      System.arraycopy(uiDragStates._dragStatuses, 0, _dragStatuses, 0, _dragStatuses.length);
    }
  }

  private DragStates _dragStates;
  private SurfaceHolder _surfaceHolder;
  private ABrickGame _currentGame;

  /**
   * Runs in thread, waits according to game timer or till ui input,
   * analyses _gameThreadAction value passed from UI thread to change game state.
   */
  public void runGame(SurfaceHolder surfaceHolder, ABrickGame game) {
    _surfaceHolder = surfaceHolder;
    _currentGame = game;

    _dragStates = new DragStates();// now no info on current drag state available to game thread
    // runs
    //long INTERVAL = 500; // millis per step

    // on 1st iteration just display screen
    _gameThreadAction = GameAction.UNPAUSE;
    _isRunning = true;
    long nextTickMoment = WAIT_CYCLE;

    synchronized (_gameChangeLock) {
      try {
        while (_isRunning) {
          GameAction curAction = _gameThreadAction;
          _gameThreadAction = null;

          GameImpulse currentImpulse = curAction == GameAction.IMPULSE ? _currentImpulse : null;
          _currentImpulse = null;

          if (getGame().getState() == ABrickGame.LOST) {
            Debug.print("Lost: show scores, exit game thread");
            onGameLost();
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
            //GameImpulse currentImpulse = null;

            GameImpulse dragImpulse = processDrag(curAction);

            if(curAction == GameAction.DRAG && dragImpulse != null) {
              curAction = GameAction.IMPULSE;
              currentImpulse = dragImpulse;
            }

            switch (curAction) {
              case DRAG:
                repaintType = ScreenPaintType.FIELD_ONLY;
                break;

              case ADVANCE:
              case DROP:
                boolean gameStateChanged = getGame().nextState(curAction == GameAction.DROP);
                if(getGame().canSqueeze()) {
                  playViewSoundEffect(Sounds.Id.SQUEEZE);

//                  for (int squeezeStep = getGame().getSqueezeSteps(); squeezeStep > 0; squeezeStep--) {
//
//                  }
                }

                if(gameStateChanged) {
                  for (DragAxis dt : DragAxis.values()) {
                    int pos = dt.ordinal();
                    _dtPositions[pos] = _dragStates.getValue(dt);
                    _bgThreadDynamicState.setState(pos, _bgThreadDynamicState.valueStates[pos], 0);
                  }
                }

                // Scoreboard.instance().getGameScores(getGame().getDescriptor().getId()).setScore(getGame().getScore());
                repaintType = gameStateChanged ? ScreenPaintType.FULLSCREEN : ScreenPaintType.FIELD_ONLY;
                nextTickMoment = WAIT_CYCLE; // reset timer to wait next
                break;

              case IMPULSE:
                if (getGame().doAction(currentImpulse)) {
                  repaintType = ScreenPaintType.FIELD_ONLY;
                  playViewSoundEffect(Sounds.Id.MOVEMENT);
                } else {
                  repaintType = null;
                }

                break;
              default:
                repaintType = null;
                break;
            }
          }

          long now = System.currentTimeMillis();
          paintScreen(repaintType);

          if(nextTickMoment == WAIT_CYCLE) {
            nextTickMoment = now + game.getLevelDelayMillis();// after debug use
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

  /**
   * Processes drag offsets and finds if they result in another impulse.
   * @param curAction action for which this drag state is actual
   * @return impulse if drag exceeds certain threshold or null
   */
  private GameImpulse processDrag(GameAction curAction) {
    GameImpulse currentImpulse = null;

    for (DragAxis dt : DragAxis.values()) {
      int pos = dt.ordinal();

      if (!_dragStates.isActive(dt)) {
        _dtPositions[pos] = 0;
        _bgThreadDynamicState.setState(pos, DynamicState.ValueState.NOT_TRACKED, 0);

      } else if (curAction == GameAction.DRAG) {
        DynamicState.ValueState newState;
        double newValue;
        double curValue = _dragStates.getValue(dt);
        newValue = curValue - _dtPositions[pos];
        double absDistance = Math.abs(newValue);
        DragSensitivity sensitivity = getGame().getAxisSensitivity(dt);

        if (absDistance < sensitivity.MIN) {
          // don't display if too small
          newState = DynamicState.ValueState.NOT_TRACKED;
          newValue = 0;

        } else if (absDistance >= sensitivity.MAX) {
          // replace DRAG with one of impulses, reset init pos to current
          currentImpulse = getGame().getAxisImpulse(dt, newValue >= 0);
          //curAction = GameAction.IMPULSE;
          _dtPositions[pos] = curValue;
          newState = DynamicState.ValueState.NOT_TRACKED;
          newValue = 0;
          //Debug.print("Drag results in game action: " + curAction);

        } else {
          // in between - see if allowed, set valid/invalid
          GameImpulse impulse = getGame().getAxisImpulse(dt, newValue >= 0);
          newState = impulse == null ? DynamicState.ValueState.NOT_TRACKED :
            (getGame().isEffective(impulse) ? DynamicState.ValueState.VALID : DynamicState.ValueState.INVALID);
        }

        _bgThreadDynamicState.setState(pos, newState, newValue);
      }
    }

    return currentImpulse;
  }
  /**
   * Called from UI thread.
   * Sends action from GUI thread to the thread waking it from waiting
   * @param action
   */
  public void sendAction(GameAction action, GameImpulse impulse, DragStates uiDragStates){

    synchronized (_gameChangeLock) {
      _gameThreadAction = action;
      _currentImpulse = impulse;

      // copy to thread drag control instance from ui-provided instance
      if(uiDragStates != null) {
        _dragStates.setFrom(uiDragStates);
      }
      _gameChangeLock.notify();
    }
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

      synchronized (_surfaceHolder) {
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

          c = _surfaceHolder.lockCanvas(updateRect);
          if(c == null){
            return; // just if view is not displayed
          }

          if (updateRect != null && !updateRect.equals(fieldRect)) {
            updateRect = null;
          }

          if (paintType == ScreenPaintType.FULLSCREEN || paintType == ScreenPaintType.FIELD_ONLY) {
            //Debug.print("PGS: " + paintType);
            onPaintGameScreen(c, updateRect == null, _bgThreadDynamicState);
          }
        } finally {
          if (c != null) {
            _surfaceHolder.unlockCanvasAndPost(c);
          }
        }
      }

      long end = System.currentTimeMillis();
      Debug.print("Repaint [" + paintType + "] took " + (end - start) + " ms");
    }
  }

  /**
   * Called from background thread witih canvas locked on surface holder.
   * Override to paint new game state.
   * @param c
   * @param repaintAll
   * @param dynamicState
   */
  public abstract void onPaintGameScreen(Canvas c, boolean repaintAll, DynamicState dynamicState);

  /**
   * Called from background thread when a sound must be played.
   * @param soundId
   */
  protected abstract void playViewSoundEffect(final Sounds.Id soundId);

  /**
   * called from bg thread when the game is lost
   */
  public void onGameLost() {}

  private ABrickGame getGame() {
    return _currentGame;
  }

  public void stop() {
    _isRunning = false;
    sendAction(null, null, null);
  }
}
