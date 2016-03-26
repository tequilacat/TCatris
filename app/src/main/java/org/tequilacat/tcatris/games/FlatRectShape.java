package org.tequilacat.tcatris.games;

import android.graphics.Rect;

/**
 * Implementation of FlatShape represented in rectangular coordinate system
 */
public class FlatRectShape extends FlatShape {

  public FlatRectShape(int[] data) {
    super(data);
  }

  @Override
  public void getBounds(Rect bounds) {
    int minCol = 0, maxCol = 0, minRow = 0, maxRow = 0;

    for (int i = 0, len = this.size(); i < len; i++) {
      int col = this.getX(i), row = this.getY(i);

      if (i == 0) {
        minCol = maxCol = col;
        minRow = maxRow = row;
      }else {
        if (minCol > col) {
          minCol = col;
        }else if (maxCol < col) {
          maxCol = col;
        }

        if (minRow > row) {
          minRow = row;
        }else if(maxRow < row) {
          maxRow = row;
        }
      }
    }
    bounds.set(minCol, minRow, maxCol + 1, maxRow + 1);
  }

  @Override
  public FlatShape createCopy() {
    FlatRectShape copy = new FlatRectShape(null);
    copy.copyFields(this);
    return copy;
  }

  @Override
  public int getX(int i, int centerX, int centerY) {
    return myData[(i << 1) + i] + centerX;
  }

  @Override
  protected void rotate(int rotationDir) {
    if (rotationDir != 0) {

      for (int i = 0; i < myData.length; i += 3) {
        int x = myData[i], y = myData[i + 1];
        if (x != 0 || y != 0) {
          if (rotationDir > 0) {
            // clockwise
            myData[i] = -y;
            myData[i + 1] = x;
          } else {
            myData[i] = y;
            myData[i + 1] = -x;
          }
        }
      }
    }
  }

  /**
   * gets Y coord of i-th cell assuming shape is centered at centerX, centerY
   * @param i
   * @param centerX
   * @param centerY
   * @return
   */
  @Override
  public int getY(int i, int centerX, int centerY) {
    // quick *3
    return myData[(i << 1) + i + 1] + centerY;
  }

}
