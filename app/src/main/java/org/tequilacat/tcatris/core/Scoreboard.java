package org.tequilacat.tcatris.core;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Stores scores for all games played in this installation.
 * Games are distinguished by string ID.
 * For each game, top MAX_SCORE_LENGTH are stored.
 * For each score entry, its timestamp and scores are stored.
 *
 * Created by user1 on 07.03.2016.
 */
public class Scoreboard implements Parcelable {

  public static final int MAX_SCORE_LENGTH = 5;
  private static Scoreboard _instance = new Scoreboard();

  private Map<String, GameScores> _gameScoresMap = new HashMap<>();

  private Scoreboard() { }

  protected Scoreboard(Parcel in) {
    // reads scores for all games into _gameScoresMap
    int entryCount = in.readInt();
    ClassLoader cl = GameScores.class.getClassLoader();

    for(int i = 0; i < entryCount; i++) {
      String gameId = in.readString();
      GameScores gameScores = in.readParcelable(cl);
      _gameScoresMap.put(gameId, gameScores);
    }
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeInt(_gameScoresMap.size());

    for (Map.Entry<String, GameScores> entry : _gameScoresMap.entrySet()) {
      dest.writeString(entry.getKey());
      dest.writeParcelable(entry.getValue(), flags);
    }
  }

  @Override
  public int describeContents() {
    return 0;
  }

  public static final Creator<Scoreboard> CREATOR = new Creator<Scoreboard>() {
    @Override
    public Scoreboard createFromParcel(Parcel in) {
      return new Scoreboard(in);
    }

    @Override
    public Scoreboard[] newArray(int size) {
      return new Scoreboard[size];
    }
  };

  public static void setState(Scoreboard board) {
    _instance = board == null ? new Scoreboard() : board;
  }

  public static Scoreboard instance() {
    return _instance;
  }

  public static class ScoreEntry {
    private int _score;
    private long _time;

    public ScoreEntry(int score, long timeStamp) {
      _score = score;
      _time = timeStamp;
    }

    public int getScore() {
      return _score;
    }

    public void setScore(int score) {
      _score = score;
    }

    public long getTime() {
      return _time;
    }

    public void setTime(long time) {
      _time = time;
    }
  }

  /**
   * finds game scores object for certain game, or creates new one
   *
   * @param gameId
   * @return
   */
  public GameScores getGameScores(String gameId) {
    final GameScores gameScores;

    if (_gameScoresMap.containsKey(gameId)) {
      gameScores = _gameScoresMap.get(gameId);
    } else {
      gameScores = new GameScores(null);
      _gameScoresMap.put(gameId, gameScores);
    }

    return gameScores;
  }


  public static class GameScores implements Parcelable {
    private List<ScoreEntry> _entries = new ArrayList<>();
    private ScoreEntry _currentEntry = null;

    protected GameScores(Parcel in) {
      if(in != null) {
        // read all data from parcel
        int count = in.readInt();

        for(int i = 0;i<count;i++) {
          int score = in.readInt();
          long timestamp = in.readLong();
          _entries.add(new ScoreEntry(score, timestamp));
        }
      }
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
      dest.writeInt(_entries.size());

      for (ScoreEntry entry : _entries) {
        dest.writeInt(entry.getScore());
        dest.writeLong(entry.getTime());
      }
    }

    @Override
    public int describeContents() {
      return 0;
    }

    public static final Creator<GameScores> CREATOR = new Creator<GameScores>() {
      @Override
      public GameScores createFromParcel(Parcel in) {
        return new GameScores(in);
      }

      @Override
      public GameScores[] newArray(int size) {
        return new GameScores[size];
      }
    };

    public int getMaxScore() {
      return _entries.isEmpty() ? 0 : _entries.get(0).getScore();
    }

    public boolean isCurrent(ScoreEntry entry) {
      return entry == _currentEntry;
    }

    public List<ScoreEntry> getEntries() {
      return _entries;
    }

    /**
     * creates new entry for current player (if score = 0)
     * or updates already existing current entry.
     * If score is too small it's not appended.
     * If score is 0 the game is considered just started
     *
     * @param score
     */
    public void setScore(int score) {
      if (score == 0 || _currentEntry == null) {
        // avoid entry recreation for the same score parameter
        if (_currentEntry == null || _currentEntry.getScore() != score) {
          _currentEntry = new ScoreEntry(score, 0);
          _currentEntry.setScore(score);
        }

      } else if (score > _currentEntry.getScore()) { //foolproof - usually must be greater than last invocation
        // current entry is non null, see if it fits scoreboard

        // if less than last, and not 0, we add it to last if we have slots
        int scorePos = _entries.size() - 1;
        // find scorePos with score >= added one

        while (scorePos >= 0) {
          if (score <= _entries.get(scorePos).getScore()) {
            break;
          }
          scorePos--;
        }
//        while (scorePos >= 0 && score > _entries.get(scorePos).getScore()) {
//          scorePos--;
//        }

        // now scorePos is next higher score.
        // add if new pos is within LE maxcount
        if (scorePos + 1 < MAX_SCORE_LENGTH) {
          _entries.remove(_currentEntry);
          _entries.add(scorePos + 1, _currentEntry);
        }

        while (_entries.size() > MAX_SCORE_LENGTH) {
          _entries.remove(_entries.size() - 1);
        }

        _currentEntry.setScore(score);
        _currentEntry.setTime(System.currentTimeMillis());
      }
    }
  }
}
