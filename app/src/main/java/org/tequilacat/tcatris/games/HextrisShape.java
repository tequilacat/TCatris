package org.tequilacat.tcatris.games;

import android.graphics.Rect;

/**
 * Hexagonal shape
 */
public class HextrisShape extends FlatShape {
  public HextrisShape(int[] cellData) {
    super(cellData);
  }

  @Override
  public FlatShape createCopy() {
    HextrisShape newShape = new HextrisShape(null);
    newShape.copyFields(this);
    return newShape;
  }

  @Override
  public void getBounds(Rect bounds) {
    // TODO real computation of hex shape bounds
    bounds.set(-1, -1, 2, 2);
  }

  @Override
  protected void rotate(int rotationDir) {
    // i'th contains distance from center
    // i+1'th element contains rotation
    // i+2'th element contains color

    for (int i = 0, len = this.myData.length; i < len; i += 3) {
      if (myData[i] != 0) {
        int cellRotation = myData[i + 1] + rotationDir;

        if (cellRotation < 0) {
          cellRotation = 5;
        } else if (cellRotation >= 6) {
          cellRotation = 0;
        }
        myData[i + 1] = cellRotation;
      }
    }
  }

  @Override
  public int getX(int i, int centerX, int centerY) {
    return 0;
  }

  @Override
  public int getY(int i, int centerX, int centerY) {
    return 0;
  }
}
