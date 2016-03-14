// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

package org.tequilacat.tcatris.games;

// Referenced classes of package flat:
//            FlatGame, Shape

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;

import org.tequilacat.tcatris.core.ColorCodes;
import org.tequilacat.tcatris.core.DragAxis;
import org.tequilacat.tcatris.core.DynamicState;
import org.tequilacat.tcatris.core.GameDescriptor;
import org.tequilacat.tcatris.core.GameImpulse;

import java.util.EnumSet;

public class Columns extends FlatGame {

  int myLastScores;
  private static final int N_FIGURES = 5;
  boolean cellsToSqueeze[][];
  boolean squeezable;

  // default is xixit
  // "columns" same but shapes are vertical
  // "trix" same but shapes are rotated

  private int myGameType;
  private static final int FIGTYPE_VERT = 0;
  private static final int FIGTYPE_HORZ = 1;
  private static final int FIGTYPE_ROTATE = 2;

  private static Path _shiftedCellPath = new Path();
  private static Paint _shiftedCellFill = new Paint();
  private static Paint _shiftedCellStroke = new Paint();

  public Columns(GameDescriptor descriptor) {
    super(descriptor, new FlatRectGamePainter());

    _shiftedCellFill.setStyle(Paint.Style.STROKE);
    _shiftedCellFill.setColor(ColorCodes.black);

    _shiftedCellFill.setStyle(Paint.Style.FILL);
    _shiftedCellFill.setColor(ColorCodes.darkYellow);
  }

  /**
   *
   * @return whether this game shifts cells instead of rotating
   */
  private boolean isColorShifting() {
    return myGameType != FIGTYPE_ROTATE;
  }

  /**************************************************
   **************************************************/
  @Override
  protected void configure(String specSettings) {
    if ("columns".equals(specSettings)) {// was horz
      myGameType = FIGTYPE_HORZ;
    } else if ("xixit".equals(specSettings)) { // was vert
      myGameType = FIGTYPE_VERT;
    } else {
      myGameType = FIGTYPE_ROTATE; // was trix
    }
  }

  /**************************************
   * creates new (mutable) shape
   **************************************/
  @Override
  public FlatShape createNext() {
    int c1 = 1 + getRandomInt(N_FIGURES);
    int c2 = 1 + getRandomInt(N_FIGURES);
    int c3 = 1 + getRandomInt(N_FIGURES);

    FlatShape fs = new FlatShape(new int[]{0, -1, c1, 0, 0, c2, 0, 1, c3});
    if (myGameType == FIGTYPE_HORZ) {
      fs.transform(GameImpulse.ROTATE_CW);
    }
    return fs;
  }

  /**
   * For colorshifting games returns null maning game does not support rotation
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
  public GameImpulse getAxisImpulse(DragAxis axis, boolean positiveDirection) {
    // for color shift return shift impulses instead of rotating
    GameImpulse impulse;

    if(isColorShifting() && axis == DragAxis.ROTATE) {
      impulse = positiveDirection ? GameImpulse.SHIFT_FORWARD : GameImpulse.SHIFT_BACKWARD;
    }else {
      impulse = super.getAxisImpulse(axis, positiveDirection);
    }

    return impulse;
  }

  // 10 points for contour
  //private float[] _shiftedCellContour = new float[20];

  private void addContourPoint(int pos, float x, float y, int rotate) {
    // convert acc to rotation
    if(_shiftedCellPath.isEmpty()) {
      _shiftedCellPath.moveTo(x, y);
    }else{
      _shiftedCellPath.lineTo(x, y);
    }
  }

  @Override
  protected void paintFallingShape(Canvas c, DynamicState dynamicState) {
    super.paintFallingShape(c, dynamicState);

    if(isColorShifting()) {
      float val = dynamicState.getValue(getRotationAxis().ordinal());

      if(val >= DynamicState.MIN_DRAG) {
        _shiftedCellPath.rewind();

        // create
        int pos = 0;
        int cellSize = getGameScreenLayout().getCellSize();
        float x0 = -cellSize / 2, y0 = -cellSize / 2;
        addContourPoint(pos++, x0, y0, 0);
        addContourPoint(pos++, x0 + cellSize * val, y0, 0);
        addContourPoint(pos++, x0 + cellSize * val * 1.2f, 0, 0);
        addContourPoint(pos++, x0 + cellSize * val, y0 + cellSize, 0);
        addContourPoint(pos++, x0, y0 + cellSize, 0);

        _shiftedCellPath.close();

        // TODO move init cell fills to constructor
        _shiftedCellFill.setStrokeWidth(cellSize / 20f);
        // foreach cell translate and draw/fill path
        FlatShape fallingShape = getCurrentShape();

        for(int i = 0, len = fallingShape.size(); i < len;i++){
          int col = fallingShape.getX(i), row = fallingShape.getY(i);
          // compute x, y
        }
      }
      //
    }
  }

  @Override
  public boolean doAction(GameImpulse impulse) {
    boolean modified;

    // for color shifting process only
    if (impulse == GameImpulse.SHIFT_BACKWARD || impulse == GameImpulse.SHIFT_FORWARD) {
      shift(getCurrentShape(), impulse);
      modified = true;
    } else {
      modified = super.doAction(impulse);
    }

    return modified;
  }

  private void shift(FlatShape shape, GameImpulse impulse) {
    int dir = (impulse == GameImpulse.ROTATE_CCW) ? -1 : 1;
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
    return (cellsToSqueeze != null) && canSqueeze() && cellsToSqueeze[y][x];
  }

  @Override
  public boolean computeCanSqueeze() {
    squeezable = false;
    myLastScores = 0;

    if (cellsToSqueeze == null)
      cellsToSqueeze = new boolean[getHeight()][getWidth()];

    for (int y = 0; y < getHeight(); y++) { // scan rows
      for (int x = 0; x < getWidth(); x++) {// scan cells in rows
        cellsToSqueeze[y][x] = false;
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
    return squeezable;
  }

  // runs along direction, marks all 3&more cells as toRemove.

  /************************************************
   ************************************************/
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
          squeezable = true;
          scores += rlen;
          int xx = x - dx, yy = y - dy;
          while (rlen-- > 0) {
//							diag = "field["+ y +"]["+ x +"], ";
//							diag += "cellsToSqueeze["+ yy +"]["+ xx +"]";

            cellsToSqueeze[yy][xx] = true;
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
      squeezable = true;
      scores += rlen;
      int xx = x - dx, yy = y - dy;
      while (rlen-- > 0) {
//							diag = "field["+ y +"]["+ x +"], ";
//							diag += "cellsToSqueeze["+ yy +"]["+ xx +"]";

        cellsToSqueeze[yy][xx] = true;
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
      // foreach col, remove
      int curY = 0, fromY = 0;

      while (curY < getHeight()) {
        if (fromY >= getHeight()) { // copy to cell from outside
          field[curY][x] = EMPTY;
          cellsToSqueeze[curY][x] = false;
          curY++;
        } else
          //  fromY is readable, test
          if (cellsToSqueeze[fromY][x]) { // cur cell is squeezable
//					Debug.print("  row "+ x +", SQUEEZE: "+ curY +" / "+ fromY +" : "+dbgGetCol(x));
            fromY++;
          } else {
            if (curY != fromY) {
              String s = "  " + field[fromY][x] + "[ @" + fromY + "]  -> " + field[curY][x] + "[ @" + curY + "]";
              field[curY][x] = field[fromY][x];
              cellsToSqueeze[curY][x] = false;
//						Debug.print(s+"  : "+dbgGetCol(x));
            }
            curY++;
            fromY++;
          }
      }
    }

    return true;
  }
}
