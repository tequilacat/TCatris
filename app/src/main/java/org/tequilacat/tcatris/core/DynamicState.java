package org.tequilacat.tcatris.core;

/**
 * Stores state changing during same game state,
 * e.g. rotation stages of a current shapes
 */
public class DynamicState {

  public enum ValueState {
    NOT_TRACKED, VALID, INVALID
  }

  public final float[] values;
  public final ValueState[] valueStates;

  public DynamicState(int count) {
    values = new float[count];
    valueStates = new ValueState[count];
  }

  public void setState(int position, ValueState valueState, double value) {
    if (position >= 0 && position < values.length) {
      valueStates[position] = valueState;
      values[position] = (float) value;
    }
  }
}
