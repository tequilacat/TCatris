package org.tequilacat.tcatris.core;

/**
 * Stores state changing during same game state,
 * e.g. rotation stages of a current shapes
 */
public class DynamicState {

  public final static double MIN_DRAG = 0.1;
  public final static double MAX_DRAG = 1;//0.8;

  public enum ValueState {
    NOT_TRACKED, VALID, INVALID
  }

  public final float[] values;
  public final ValueState[] valueStates;

  public DynamicState(int count) {
    values = new float[count];
    valueStates = new ValueState[count];
  }

  public float getValue(int pos) {
    return values[pos];
  }

  public boolean isTracking(int position) {
    ValueState state = valueStates[position];
    return state == ValueState.VALID || state == ValueState.INVALID;
  }

  public boolean isValid(int position) {
    return valueStates[position] == ValueState.VALID;
  }

  public void setState(int position, ValueState valueState, double value) {
    if (position >= 0 && position < values.length) {
      valueStates[position] = valueState;
      values[position] = (float) value;
    }
  }
}
