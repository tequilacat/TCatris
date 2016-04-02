package org.tequilacat.tcatris.core;

import android.graphics.Rect;

/**
 * stores dimensions of game field elements computed for current game and screen size
 */
public class GameScreenLayout {
  private Rect _fieldRect;
  private Rect _nextShapeRect;

  /****************
   */
  public GameScreenLayout(int fX, int fY, int fWidth, int fHeight,
                          int nextX, int nextY, int nextW, int nextH) {

    _fieldRect = new Rect(fX, fY, fX + fWidth, fY + fHeight);
    _nextShapeRect = new Rect(nextX, nextY, nextX + nextW, nextY + nextH);
  }

  /**
   * force field rect to have exact steps by X and Y axis
   * @param xStep
   * @param yStep
   */
  public void roundFieldRect(int xStep, int yStep) {
    _fieldRect.right = _fieldRect.left + (_fieldRect.width() / xStep) * xStep;
    _fieldRect.bottom = _fieldRect.top + (_fieldRect.height() / yStep) * yStep;
  }

  public Rect getNextShapeRect() {
    return _nextShapeRect;
  }

  public Rect getFieldRect() {
    return _fieldRect;
  }
}
