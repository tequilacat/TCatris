package org.tequilacat.tcatris.core;

/**
 * Constants used across the codebase
 */
public class GameConstants {
  public static final String PREF_SOUND_ENABLE = "pref_sound_enable";
  public static final String PREF_SHOW_DROPTARGET = "pref_show_droptarget";
  public static final String PREF_LEVEL_ADVANCE = "pref_level_advance";

  public static final int MAX_LEVEL = 10;
  public static final int MAX_DROP_DELAY = 500;
  public static final int MIN_DROP_DELAY = 100;

  public static final String GAME_IMPL_PACKAGE = "org.tequilacat.tcatris.games";

  public static final String JSON_GAMECLASS = "class";
  public static final String JSON_GAMELABEL = "label";
  public static final String JSON_DIMENSIONS = "dim";
  public static final String JSON_GAMETYPE = "gameType";
}
