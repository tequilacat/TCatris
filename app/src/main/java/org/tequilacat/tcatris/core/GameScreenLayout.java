package org.tequilacat.tcatris.core;

import android.graphics.Rect;

/**
 * Created by avo on 24.02.2016.
 */
public class GameScreenLayout {
  private final boolean myDisplayIconsVertically;

  private Rect _fieldRect;
  private Rect _nextShapeRect;

  /****************
   */
  public GameScreenLayout(int fX, int fY, int fWidth, int fHeight,
                          int nextX, int nextY, int nextW, int nextH, boolean verticalIconLayout) {

    myDisplayIconsVertically = verticalIconLayout;

    _fieldRect = new Rect(fX, fY, fX + fWidth, fY + fHeight);
    _nextShapeRect = new Rect(nextX, nextY, nextX + nextW, nextY + nextH);
  }

  public Rect getNextShapeRect() {
    return _nextShapeRect;
  }

  public Rect getFieldRect() {
    return _fieldRect;
  }
}
