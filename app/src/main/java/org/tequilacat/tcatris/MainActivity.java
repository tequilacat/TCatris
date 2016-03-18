package org.tequilacat.tcatris;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.tequilacat.tcatris.core.Debug;
import org.tequilacat.tcatris.core.GameDescriptor;
import org.tequilacat.tcatris.core.GameList;
import org.tequilacat.tcatris.core.GameView;
import org.tequilacat.tcatris.core.Scoreboard;
import org.tequilacat.tcatris.core.Tetris;
import org.tequilacat.tcatris.core.VisualResources;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

  private static final String SCOREBOARD_PARCEL_KEY = "scoreboard_parcel_key";
  private static final String SCORE_PREFBANK_NAME = "scores";

  private GameSelectorFragment _gameSelectorFragment;
  private GameViewFragment _gameSurfaceFragment;
  private ScoreFragment _scoreFragment;

  private List<MainActivityFragment> _fragments = new ArrayList<>();

  private Tetris _currentGame;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // set preferences from XML
    PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

    setCurrentGame(null);

    // the sound vol buttons will control sound of FX
    setVolumeControlStream(AudioManager.STREAM_MUSIC);

    VisualResources.Defaults = new VisualResources(getResources());

    setContentView(R.layout.activity_main);

    Toolbar myToolbar = (Toolbar) findViewById(R.id.gameselector_toolbar);
    setSupportActionBar(myToolbar);

    // fill game list
    GameList.init();
    // init scores from saved preferences
    SharedPreferences prefs = getSharedPreferences(SCORE_PREFBANK_NAME, MODE_PRIVATE);
    Scoreboard.setState(prefs.getString(SCOREBOARD_PARCEL_KEY, null));

    _gameSelectorFragment = new GameSelectorFragment();
    _gameSurfaceFragment = new GameViewFragment();
    _scoreFragment = new ScoreFragment();

    _fragments = Arrays.asList(_gameSelectorFragment,_gameSurfaceFragment,_scoreFragment);

    FragmentTransaction transaction = getFragmentManager().beginTransaction();

    for (Fragment fragment : _fragments) {
      transaction.add(R.id.main_layout, fragment);
      transaction.hide(fragment);
    }
    transaction.commit();

    showFragment(_gameSelectorFragment);
  }

  /**
   * shows specified fragment
   *
   * @param fragment
   */
  private void showFragment(MainActivityFragment fragment) {
    FragmentTransaction transaction = getFragmentManager().beginTransaction();

    for (Fragment aFragment : _fragments) {
      if(aFragment!=fragment) {
        transaction.hide(aFragment);
      }
    }

    if(fragment.isShowToolbar()) {
      getSupportActionBar().show();
    }else{
      getSupportActionBar().hide();
    }

    transaction.show(fragment);
    transaction.commit();
  }

  public Tetris getCurrentGame() {
    return _currentGame;
  }

  public void setCurrentGame(Tetris currentGame) {
    _currentGame = currentGame;
  }

  public abstract static class MainActivityFragment extends Fragment {
    private boolean _showToolbar;

    public MainActivityFragment(boolean showToolbar) {
      _showToolbar = showToolbar;
    }

    public boolean isShowToolbar() {
      return _showToolbar;
    }

    public void setShowToolbar(boolean showToolbar) {
      _showToolbar = showToolbar;
    }

    protected MainActivity getMainActivity() {
      return (MainActivity) getActivity();
    }
  }

  public static class GameSelectorFragment extends MainActivityFragment {
    public GameSelectorFragment() {
      super(true);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
      super.onCreateOptionsMenu(menu, inflater);
      inflater.inflate(R.menu.gameselector_menu, menu);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
      View view = inflater.inflate(R.layout.view_gameselector, container, false);

      final List<GameDescriptor> gameTypes = GameList.instance().getGameDescriptors();
      final ArrayAdapter<GameDescriptor> adapter = new ArrayAdapter<>(getMainActivity(),
          android.R.layout.simple_list_item_1, gameTypes);

      ListView gameListView = (ListView) view.findViewById(R.id.lvMainOptionList);
      gameListView.setAdapter(adapter);
      gameListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
          getMainActivity().runGame(gameTypes.get(position));
        }
      });
      return view;
    }
  }

  public static class GameViewFragment extends MainActivityFragment {

    private GameView _gameView;

    public GameViewFragment() {
      super(false);
    }

    // TODO remove gameView property of GameViewFragment
    public GameView getGameView() {
      return _gameView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
      View view = inflater.inflate(R.layout.view_gamesurface, container, false);
      _gameView = (GameView) view;
      return view;
    }
  }

  public static class ScoreFragment extends MainActivityFragment {

    private static java.text.DateFormat TIMESTAMP_FORMAT =
        DateFormat.getDateTimeInstance();

    public ScoreFragment() {
      super(true);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
      super.onCreateOptionsMenu(menu, inflater);
      inflater.inflate(R.menu.score_menu, menu);

      boolean isGameActive = getMainActivity().getCurrentGame().getState() != Tetris.LOST;
      menu.findItem(R.id.mnu_back_to_game).setVisible(isGameActive);
      menu.findItem(R.id.mnu_play_again).setVisible(!isGameActive);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
      return inflater.inflate(R.layout.view_scores, container, false);
    }

    public void loadScores() {
      Tetris game = getMainActivity().getCurrentGame();
      final Scoreboard.GameScores gs = Scoreboard.instance().getGameScores(game.getDescriptor().getId());

      ListView scoreListView = (ListView) getActivity().findViewById(R.id.lvScoreList);

      ArrayAdapter<Scoreboard.ScoreEntry> adapter = new ArrayAdapter<Scoreboard.ScoreEntry>(
          getActivity(), android.R.layout.simple_list_item_2,
          android.R.id.text1, gs.getEntries()) {
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
          View view = super.getView(position, convertView, parent);
          TextView text1 = (TextView) view.findViewById(android.R.id.text1);
          TextView text2 = (TextView) view.findViewById(android.R.id.text2);

          Scoreboard.ScoreEntry item = getItem(position);
          text1.setText(Integer.valueOf(item.getScore()).toString());
          text2.setText(gs.isCurrent(item) ? getContext().getString(R.string.current_score)
              : TIMESTAMP_FORMAT.format(new Date(item.getTime())));
          return view;
        }
      };
      scoreListView.setAdapter(adapter);

      // if current score did not make into roaster show message
      TextView scoreTooLowText = (TextView) getActivity().findViewById(R.id.scoreOutOfRoasterText);
      scoreTooLowText.setText(gs.containsCurrentScore() ? null :
          String.format(getString(R.string.current_score_too_low), game.getScore()));

      // create score view title
      TextView scoreViewTitle = (TextView) getView().findViewById(R.id.scoreViewTitle);
      scoreViewTitle.setText(String.format(getString(R.string.top_scores),
          game.getDescriptor().getLabel()));
    }
  }

  /**
   * stores game scores for all games
   */
  @Override
  protected void onPause() {
    super.onPause();

    SharedPreferences.Editor prefEditor = getSharedPreferences(SCORE_PREFBANK_NAME, MODE_PRIVATE).edit();
    prefEditor.putString(SCOREBOARD_PARCEL_KEY, Scoreboard.getState());
    prefEditor.apply();
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    boolean res = true;

    int itemId = item.getItemId();

    if (itemId == R.id.mnu_back_to_game || itemId == R.id.mnu_play_again) {
      backFromScores();

    }else if (itemId == R.id.mnu_selectgame) {
      showFragment(_gameSelectorFragment);

    }else if (itemId == R.id.mnu_settings) {
      showSettings();

    } else if (itemId == R.id.mnu_about) {
      Toast toast = Toast.makeText(this,
          String.format(getText(R.string.txt_about).toString(), BuildConfig.VERSION_NAME),
          Toast.LENGTH_LONG);

      toast.show();

    } else {
      res = super.onOptionsItemSelected(item);
    }

    return res;
  }

  private void runGame(GameDescriptor gameDescriptor) {
    setCurrentGame(GameList.instance().createGame(gameDescriptor));
    restartGame();
    _gameSurfaceFragment.getGameView().setGame(getCurrentGame());
    showFragment(_gameSurfaceFragment);
  }

  /**
   * on back button if current is gamelist we finish it by calling base,
   * if current is game we set paused mode
   */
  @Override
  public void onBackPressed() {
    Debug.print("Back is pressed");

    if (_gameSurfaceFragment.isVisible()) {
      showScores();

    } else if (_gameSelectorFragment.isVisible()) {
      super.onBackPressed();

    } else if (_scoreFragment.isVisible()) {
      backFromScores();
    }
  }

  /**
   * Just reinit current game , no thread work here
   */
  private void restartGame() {
    getCurrentGame().initGame();
    // create new slot
    Scoreboard.instance().getGameScores(getCurrentGame().getDescriptor().getId()).setScore(0);
  }

  /**
   * Called on pause, or when game is lost.
   * Shows scores screen.
   */
  public void showScores() {
    // fill scores with current score
    _scoreFragment.loadScores();
    showFragment(_scoreFragment);
  }

  private void backFromScores() {
    Debug.print("restart/continue after scores");

    GameView gameView = (GameView) _gameSurfaceFragment.getGameView();
        //findViewById(R.id.gameView);

    if (gameView.getGame().getState() == Tetris.LOST) {
      restartGame();
    }
    showFragment(_gameSurfaceFragment);
  }

  private void showSettings() {
    Intent intent = new Intent();
    intent.setClass(MainActivity.this, SettingsActivity.class);
    startActivityForResult(intent, 0);
  }
}
