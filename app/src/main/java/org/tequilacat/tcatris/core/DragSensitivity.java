package org.tequilacat.tcatris.core;


/**
 * simple class defining sensitivity along axis by each type of game.
 * eg sensitivity for rotate, move and color shift may differ
 */

public enum DragSensitivity {
  MOVE(0.1f, 1f),
  ROTATE(0.1f, 0.8f),
  COLORSHIFT (0.2f, 0.8f);

  public final float MIN, MAX;

  DragSensitivity(float min, float max) {
    MIN = min;
    MAX = max;
  }
}
