package org.tequilacat.tcatris.core;

/**
 * stores parameters of certain game type
 */
public class GameDescriptor {
  private final String _gameClassName;
  private final String _label;
  private final String _gameParameters;

  public GameDescriptor(String gameClassName, String label, String gameParams) {
    _gameClassName = gameClassName;
    _label = label;
    _gameParameters = gameParams;
  }

  public String getGameClassName() {
    return _gameClassName;
  }

  public String getLabel() {
    return _label;
  }

  public String getGameParameters() {
    return _gameParameters;
  }

  public String getId() { return getGameClassName()+"/"+getLabel(); }

  @Override
  public String toString() {
    return getLabel();
  }
}
