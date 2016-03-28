package org.tequilacat.tcatris.games;

import com.google.gson.Gson;

import org.tequilacat.tcatris.core.DragAxis;
import org.tequilacat.tcatris.core.DragSensitivity;
import org.tequilacat.tcatris.core.GameConstants;
import org.tequilacat.tcatris.core.GameDescriptor;
import org.tequilacat.tcatris.core.GameImpulse;

import java.util.EnumSet;

public class ColorShiftGame extends FlatGame {

  int myLastScores;
  private static final int N_FIGURES = 5;
  private boolean _cellsToSqueeze[][];
  private boolean _isSqueezable;

  enum ColorGameType {
    SHIFT_HORIZONTALLY, SHIFT_VERTICALLY, ROTATE
  }

  private ColorGameType _gameType;

  // private static final Path _shiftedCellPath = new Path();

  public ColorShiftGame(GameDescriptor descriptor) {
    super(descriptor, new ColorShiftGamePainter());
    _gameType = new Gson().fromJson(descriptor.getGameParameters().get(GameConstants.JSON_GAMETYPE),
        ColorGameType.class);
    _cellsToSqueeze = new boolean[getHeight()][getWidth()];
  }

  public ColorGameType getGameType() {
    return _gameType;
  }

  /**
   * @return whether this game shifts cells instead of rotating
   */
  public boolean isColorShifting() {
    return _gameType != ColorGameType.ROTATE;
  }

//  @Override
//  protected int getMaxNextWidth() {
//    return _gameType == ColorGameType.SHIFT_HORIZONTALLY ? 3 : 1;
//  }
//
//  @Override
//  protected int getMaxNextHeight() {
//    return _gameType == ColorGameType.SHIFT_HORIZONTALLY ? 1 : 3;
//  }

  /**
   * consider
   * @param threeSides array of at least 3 cells
   */
  protected void estimateSides(float[] threeSides) {
    // field height normalized to width
    threeSides[0] = getHeight() / (float) getWidth();
    // next field width
    threeSides[1] = 1f / getWidth();// since it's 1 cell
    threeSides[2] = threeSides[1] * 3;
  }

  /**
   * Creates new shape
   * @return a shape
   */
  @Override
  public FlatShape createNext() {
    int c1 = 1 + getRandomInt(N_FIGURES);
    int c2 = 1 + getRandomInt(N_FIGURES);
    int c3 = 1 + getRandomInt(N_FIGURES);

    FlatShape fs = new FlatRectShape(new int[]{0, -1, c1, 0, 0, c2, 0, 1, c3});

    if (_gameType == ColorGameType.SHIFT_HORIZONTALLY) {
      fs.transform(GameImpulse.ROTATE_CW);
    }
    return fs;
  }

  /**
   * For colorshifting games returns null maning game does not support rotation
   *
   * @return rotation axis
   */
  public DragAxis getRotationAxis() {
    return isColorShifting() ? null : DragAxis.ROTATE;
  }

  private static EnumSet<GameImpulse> _colorShiftImpulses = EnumSet.of(GameImpulse.MOVE_LEFT, GameImpulse.MOVE_RIGHT,
      GameImpulse.SHIFT_FORWARD, GameImpulse.SHIFT_BACKWARD);

  @Override
  public EnumSet<GameImpulse> getSupportedImpulses() {
    return isColorShifting() ? _colorShiftImpulses : super.getSupportedImpulses();
  }

  @Override
  public void addEffectiveImpulses(EnumSet<GameImpulse> actionSet) {
    // for color shifting allow move and shift, never allow rotate
    if (isColorShifting()) {
      // always supported, won't be checked in base class
      // - and flatshape does not support them anyway
      actionSet.add(GameImpulse.SHIFT_BACKWARD);
      actionSet.add(GameImpulse.SHIFT_FORWARD);
    }
    // now test in base class which handles move and rotate
    super.addEffectiveImpulses(actionSet);
  }

  @Override
  public DragSensitivity getAxisSensitivity(DragAxis axis) {
    // for rotate axis return colorshift sensitivity
    return (axis == DragAxis.ROTATE) ? DragSensitivity.COLORSHIFT : super.getAxisSensitivity(axis);
  }

  private static GameImpulse POSITIVE_SHIFT = GameImpulse.SHIFT_FORWARD;

  @Override
  public GameImpulse getAxisImpulse(DragAxis axis, boolean positiveDirection) {
    // for color shift return shift impulses instead of rotating
    GameImpulse impulse;

    if (isColorShifting() && axis == DragAxis.ROTATE) {
      //    impulse = positiveDirection ? GameImpulse.SHIFT_FORWARD : GameImpulse.SHIFT_BACKWARD;

      if (positiveDirection) {
        impulse = POSITIVE_SHIFT;
      } else if (POSITIVE_SHIFT == GameImpulse.SHIFT_FORWARD) {
        impulse = GameImpulse.SHIFT_BACKWARD;
      } else {
        impulse = GameImpulse.SHIFT_BACKWARD;
      }

    } else {
      impulse = super.getAxisImpulse(axis, positiveDirection);
    }

    return impulse;
  }

  @Override
  public ImpulseSemantics getImpulseSemantics(GameImpulse impulse) {
    ImpulseSemantics semantics;

    if (impulse == GameImpulse.MOVE_LEFT) {
      semantics = ImpulseSemantics.MOVE_LEFT;

    } else if (impulse == GameImpulse.MOVE_RIGHT) {
      semantics = ImpulseSemantics.MOVE_RIGHT;

    } else if (_gameType == ColorGameType.ROTATE) {
      if (impulse == GameImpulse.ROTATE_CW) {
        semantics = ImpulseSemantics.ROTATE_CW;
      } else if (impulse == GameImpulse.ROTATE_CCW) {
        semantics = ImpulseSemantics.ROTATE_CCW;
      } else {
        semantics = null;
      }

    } else {
      if (impulse == GameImpulse.SHIFT_FORWARD) {
        semantics = _gameType == ColorGameType.SHIFT_VERTICALLY ? ImpulseSemantics.SHIFT_DOWN : ImpulseSemantics.SHIFT_RIGHT;

      } else if (impulse == GameImpulse.SHIFT_BACKWARD) {
        semantics = _gameType == ColorGameType.SHIFT_VERTICALLY ? ImpulseSemantics.SHIFT_UP : ImpulseSemantics.SHIFT_LEFT;

      } else {
        semantics = null;
      }
    }

    return semantics;
  }

  @Override
  public boolean doAction(GameImpulse impulse) {
    boolean modified;

    // for color shifting process only
    if (impulse == GameImpulse.SHIFT_BACKWARD || impulse == GameImpulse.SHIFT_FORWARD) {
      shift((FlatRectShape)getCurrentShape(), impulse);
      modified = true;
    } else {
      modified = super.doAction(impulse);
    }

    return modified;
  }

  private void shift(FlatRectShape shape, GameImpulse impulse) {
    boolean isShapeCoDirected = shape.getX(0) < shape.getX(shape.size() - 1)
        || shape.getY(0) < shape.getY(shape.size() - 1);

    int dir = (isShapeCoDirected != (impulse != POSITIVE_SHIFT)) ? -1 : 1;
    //int dir = (isShapeCoDirected != (impulse == GameImpulse.SHIFT_BACKWARD)) ? -1 : 1;

    int i = (dir > 0) ? 0 : shape.size() - 1, count = shape.size(),
        newPos = i + dir, firstValue = shape.getCellType(i);

    while (--count > 0) {
      shape.setCellType(i, shape.getCellType(newPos));

      newPos += dir;
      i += dir;
    }
    shape.setCellType(i, firstValue);
  }


  @Override
  protected boolean isSqueezable(int x, int y) {
    return canSqueeze() && _cellsToSqueeze[y][x];
  }

  @Override
  public boolean computeCanSqueeze() {
    _isSqueezable = false;
    myLastScores = 0;

    for (int y = 0; y < getHeight(); y++) { // scan rows
      for (int x = 0; x < getWidth(); x++) {// scan cells in rows
        _cellsToSqueeze[y][x] = false;
      }
    }

    // left to right
    for (int y = 0; y < getHeight(); y++) {
      myLastScores += runLength(0, y, 1, 0);
    }

    // bottom to top
    for (int x = 0; x < getWidth(); x++) {
      myLastScores += runLength(x, 0, 0, 1);
    }

    // diagonals
    int x = 2, yPos = 0;
    do {
      int xPos = (x < getWidth()) ? x : (getWidth() - 1);
      myLastScores += runLength(xPos, yPos, -1, 1);
      myLastScores += runLength(xPos, getHeight() - 1 - yPos, -1, -1);

      x++;
      yPos = (x < getWidth()) ? 0 : (x - getWidth() + 1);
    } while (yPos + 2 < getHeight());

    myLastScored = myLastScores;
    return _isSqueezable;
  }

  /**
   * Runs along direction, marks all 3 and more cells as toRemove.
   *
   * @param x0 from
   * @param y0 from
   * @param dx direction
   * @param dy direction
   * @return scores picked during this run
   */
  private int runLength(int x0, int y0, int dx, int dy) {
    int scores = 0;

    int x = x0, y = y0;
    int prevCol = -1;
    int width = getWidth(), height = getHeight();
    int rlen = 0;

    for (; x > -1 && y > -1 && x < width && y < height; x += dx, y += dy) {
//				diag = "field["+ y +"]["+ x +"]";

      int curCol = field[y][x];
      if (curCol != prevCol) {
        if (rlen >= 3) {
          _isSqueezable = true;
          scores += rlen;
          int xx = x - dx, yy = y - dy;
          while (rlen-- > 0) {
//							diag = "field["+ y +"]["+ x +"], ";
//							diag += "_cellsToSqueeze["+ yy +"]["+ xx +"]";

            _cellsToSqueeze[yy][xx] = true;
            xx -= dx;
            yy -= dy;
          }
        }
        rlen = 1;
        prevCol = curCol;
      } else {
        if (curCol != EMPTY) {
          rlen++;
        }
      }
    }

    if (rlen >= 3) {
      _isSqueezable = true;
      scores += rlen;
      int xx = x - dx, yy = y - dy;
      while (rlen-- > 0) {
//							diag = "field["+ y +"]["+ x +"], ";
//							diag += "_cellsToSqueeze["+ yy +"]["+ xx +"]";

        _cellsToSqueeze[yy][xx] = true;
        xx -= dx;
        yy -= dy;
      }
    }
    return scores;
  }

  @Override
  public boolean squeeze() {
    setScore(getScore() + myLastScores);
    myLastScores = 0;

    for (int x = 0; x < getWidth(); x++) {// scan cells in rows
      int dst = getHeight() - 1, src = dst;
      while (src >= 0) {
        if (_cellsToSqueeze[src][x]) {
          src--;
        } else {
          if (src < dst) {
            field[dst][x] = field[src][x];
          }
          src--;
          dst--;
        }
      }
      while (dst >= 0) {
        field[dst][x] = EMPTY;
        dst--;
      }
    }

    return true;
  }
}
