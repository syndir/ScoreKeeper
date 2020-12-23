// TODO:
// √ number entry vs number picker for score update?
//     - Both. Choosable in preferences.
// how to delete team members individually?
// √ starting score value preference (NOT min score)
// √ set score number pickers to current value when shown
// score value histories

package com.dancingweasels.scorekeeper;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.Editable;
import android.text.TextWatcher;
//import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity
{
	RecyclerView 						recyclerView;
	ItemTouchHelper 					itemTouchHelperTeam;
	ItemTouchHelper						itemTouchHelperPlayer;
	public static GameTracker			gameTracker;

	/**
	 * test data
	 */
	public void addDummyData()
	{
		gameTracker.addPlayer(new PlayerInfo("a", 1));
		gameTracker.addPlayer(new PlayerInfo("b", 2));
		gameTracker.addPlayer(new PlayerInfo("c", 3));
		gameTracker.addPlayer(new PlayerInfo("d", 4));
	}

	/**
	 * test data
	 */
	public void addDummyTeamData()
	{
		List<TeamPlayerInfo> tpi = new ArrayList<>();
		tpi.add(new TeamPlayerInfo("a", 1, false));
		tpi.add(new TeamPlayerInfo("b", 2, false));
		tpi.add(new TeamPlayerInfo("c", 3, false));
		tpi.add(new TeamPlayerInfo("d", 4, false));
		gameTracker.addTeam(new TeamInfo("team 1", tpi, 1));

		tpi = new ArrayList<>();
		tpi.add(new TeamPlayerInfo("a", 1, false));
		tpi.add(new TeamPlayerInfo("b", 2, false));
		tpi.add(new TeamPlayerInfo("c", 3, false));
		tpi.add(new TeamPlayerInfo("d", 4, false));
		gameTracker.addTeam(new TeamInfo("team 2", tpi, 2));

		tpi = new ArrayList<>();
		tpi.add(new TeamPlayerInfo("a", 1, false));
		tpi.add(new TeamPlayerInfo("b", 2, false));
		tpi.add(new TeamPlayerInfo("c", 3, false));
		tpi.add(new TeamPlayerInfo("d", 4, false));
		gameTracker.addTeam(new TeamInfo("team 3", tpi, 3));

		tpi = new ArrayList<>();
		tpi.add(new TeamPlayerInfo("a", 1, false));
		tpi.add(new TeamPlayerInfo("b", 2, false));
		tpi.add(new TeamPlayerInfo("c", 3, false));
		tpi.add(new TeamPlayerInfo("d", 4, false));
		gameTracker.addTeam(new TeamInfo("team 4", tpi, 4));
	}

	/**
	 * Runs when the app first starts
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		ToastHelper toastHelper = new ToastHelper(this);
		gameTracker = new GameTracker();

		// init settings
		Settings settings = new Settings();
		settings.loadSettings();

		// set view to main activity
		setContentView(R.layout.activity_main);

		// Add the ActionBar at the top of the main activity
		Toolbar actionBar = findViewById(R.id.main_toolbar);
		setSupportActionBar(actionBar);

		// Set to ignore orientation rotation
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
	}

	/**
	 * Creates the actionbar menu items from the XML file
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.mainactionbar_menu, menu);

		return super.onCreateOptionsMenu(menu);
	}

	/**
	 * Handler for the actionbar menu
	 *
	 * return true if we handled the message
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			// start a new game to keep track of
			case R.id.action_bar_new_game:
				newSinglePlayerGame();
				break;

			// start a new team game to keep track of
			case R.id.action_bar_new_team_game:
				newTeamGame();
				break;

/*
			// save game information
			case R.id.action_bar_save_game:
				saveGameInformation();
				break;

			// view history of game results
			case R.id.action_viewhistory:
				viewHistory();
				break;
*/
			case R.id.action_settings:
				displaySettings();
				break;

			case R.id.action_contact_us:
				contactMenuClicked();
				break;

			case R.id.action_exit:
				this.finishAffinity();
				break;

			default:
				return super.onOptionsItemSelected(item);
		}

		return true;
	}

	// for the game name edit text
	class gameNameTextChangedListener implements TextWatcher
	{
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

		@Override
		public void onTextChanged(CharSequence charSequence, int start, int before, int count)
		{
			String s = charSequence.toString();
			gameTracker.setGameName(s);
		}

		@Override
		public void afterTextChanged(Editable s) { }
	}

	/* *********************************************************************************************
				SINGLE PLAYER GAME METHODS
	   ****************************************************************************************** */

	/**
	 * starts a new single player game to track
	 */
	public void newSinglePlayerGame()
	{
		// make sure our listeners are cleared
		resetListeners();

		// start a new GameTracker for non-team mode
		gameTracker = new GameTracker();

		// Hide the welcome messages
		TextView welcomeText = findViewById(R.id.main_info_welcome_textview);
		welcomeText.setVisibility(View.GONE);
		TextView welcomeTextHowTo = findViewById(R.id.main_info_howto_textview);
		welcomeTextHowTo.setVisibility(View.GONE);

		// unhide the game name EditText
		EditText gameNameEditText = findViewById(R.id.game_name_edittext);
		gameNameEditText.setVisibility(View.VISIBLE);
		gameNameEditText.setText(gameTracker.getGameName());

		// bind text watcher to game name EditText
		gameNameEditText.addTextChangedListener(new gameNameTextChangedListener());

		// set up & show the bottom single player toolbar
		Toolbar gameToolbar = findViewById(R.id.game_toolbar);
		gameToolbar.getMenu().clear();
		gameToolbar.inflateMenu(R.menu.player_toolbar_menu);
		gameToolbar.setOnMenuItemClickListener(new singlePlayerToolbarMenu());
		gameToolbar.setVisibility(View.VISIBLE);

		// set up the recycler view
		// get the view from XML
		recyclerView = findViewById(R.id.main_recycler_view);
		recyclerView.setHasFixedSize(true);
		recyclerView.setLayoutManager(new LinearLayoutManager(this));

		// create the adapter
		PlayerAdapter adapter = new PlayerAdapter(gameTracker.getPlayers());
		recyclerView.setAdapter(adapter);

		// set up the swipe to remove for the recyclerView
		initSwipeToRemoveSinglePlayer();

		recyclerView.getAdapter().notifyDataSetChanged();

		// try to clear the focus
		recyclerView.requestFocus();
	}

	/**
	 * Sets things up so we can swipe to the left to remove an entry
	 */
	public void initSwipeToRemoveSinglePlayer()
	{
		itemTouchHelperPlayer = new ItemTouchHelper(swipeItemTouchCallbackSinglePlayer);
		itemTouchHelperPlayer.attachToRecyclerView(recyclerView);
	}

	ItemTouchHelper.SimpleCallback swipeItemTouchCallbackSinglePlayer = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT)
	{
		@Override
		public int getSwipeDirs(RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder)
		{
			// This will enable/disable swiping depending on whether the current item is edit locked
			try
			{
				int position = viewHolder.getAdapterPosition();
				PlayerInfo playerInfo = gameTracker.getPlayers().get(position);
				if (playerInfo.getEditLock())
					return 0;
				else
					return super.getSwipeDirs(recyclerView, viewHolder);
			}
			catch (IndexOutOfBoundsException e)
			{
				return 0;
			}
		}

		@Override
		public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target)
		{
			return false;
		}

		@Override
		public void onSwiped(final RecyclerView.ViewHolder viewHolder, int direction)
		{
			AlertDialog.Builder builder = new AlertDialog.Builder(ToastHelper.context);

			builder.setTitle("Delete Entry");
			builder.setMessage("Are you sure you want to delete this entry?");

			builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener()
					{
						@Override
						public void onClick(DialogInterface dialog, int which)
						{
							//Remove swiped item from list and notify the RecyclerView
							try
							{
								int position = viewHolder.getAdapterPosition();
								gameTracker.getPlayers().remove(position);
								recyclerView.getAdapter().notifyDataSetChanged();
							}
							catch (Exception e)
							{
								//Log.d("Exception", "Out of bounds");
							}
							dialog.cancel();
						}
					});
			builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener()
					{
						@Override
						public void onClick(DialogInterface dialog, int which)
						{
							// don't do anything, user cancelled
							recyclerView.getAdapter().notifyDataSetChanged();
							dialog.cancel();
						}
					});

			builder.setOnCancelListener(new DialogInterface.OnCancelListener()
										{
											@Override
											public void onCancel(DialogInterface dialog)
											{
												// don't do anything, user cancelled
												recyclerView.getAdapter().notifyDataSetChanged();
												dialog.cancel();
											}
										});

			builder.setOnDismissListener(new DialogInterface.OnDismissListener()
										 {
											 @Override
											 public void onDismiss(DialogInterface dialog)
											 {
												 // don't do anything, user cancelled
												 recyclerView.getAdapter().notifyDataSetChanged();
												 dialog.cancel();
											 }
										 });

			AlertDialog dialog = builder.create();
			dialog.show();
		}
	};

	class singlePlayerToolbarMenu implements Toolbar.OnMenuItemClickListener
	{
		public boolean onMenuItemClick(MenuItem item)
		{
			switch(item.getItemId())
			{
				// add a player
				case R.id.player_toolbar_add_player:
					gameTracker.addPlayer(new PlayerInfo());
					recyclerView.getAdapter().notifyDataSetChanged();
					break;

				// trigger a manual sort of the players
				case R.id.player_toolbar_sort_players:
					Collections.sort(gameTracker.getPlayers());
					recyclerView.getAdapter().notifyDataSetChanged();
					//ToastHelper.Toast(getApplication(), "Sorted");
					break;

				default:
					return false;
			}
			return true;
		}

	}

	/* *********************************************************************************************
	 			TEAM GAME METHODS
	   ****************************************************************************************** */

	public void newTeamGame()
	{
		// make sure our listeners are cleared
		resetListeners();

		// start a new tracker and set it to team mode
		gameTracker = new GameTracker();
		gameTracker.setIsTeamGame(true);

		// Hide the welcome messages
		TextView welcomeText = findViewById(R.id.main_info_welcome_textview);
		welcomeText.setVisibility(View.GONE);
		TextView welcomeTextHowTo = findViewById(R.id.main_info_howto_textview);
		welcomeTextHowTo.setVisibility(View.GONE);

		// unhide the game name EditText
		EditText gameNameEditText = findViewById(R.id.game_name_edittext);
		gameNameEditText.setVisibility(View.VISIBLE);
		gameNameEditText.setText(gameTracker.getGameName());

		// bind text watcher to game name EditText
		gameNameEditText.addTextChangedListener(new gameNameTextChangedListener());

		// set up & show the bottom single player toolbar
		Toolbar gameToolbar = findViewById(R.id.game_toolbar);
		gameToolbar.getMenu().clear();
		gameToolbar.inflateMenu(R.menu.team_toolbar_menu);
		gameToolbar.setOnMenuItemClickListener(new teamToolbarMenu());
		gameToolbar.setVisibility(View.VISIBLE);

		// set up the recycler view
		// get the view from XML
		recyclerView = findViewById(R.id.main_recycler_view);
		recyclerView.setHasFixedSize(true);
		recyclerView.setLayoutManager(new LinearLayoutManager(this));

		// create the adapter
		TeamAdapter adapter = new TeamAdapter(gameTracker.getTeams());
		recyclerView.setAdapter(adapter);

		// set up the swipe to remove for the recyclerView
		initSwipeToRemoveTeam();

		// TODO: dummy data
		//addDummyTeamData();

		recyclerView.getAdapter().notifyDataSetChanged();

		// try to clear the focus
		recyclerView.requestFocus();
	}

	/**
	 * Sets things up so we can swipe to the left to remove an entry
	 */
	public void initSwipeToRemoveTeam()
	{
		itemTouchHelperTeam = new ItemTouchHelper(swipeItemTouchCallbackTeam);
		itemTouchHelperTeam.attachToRecyclerView(recyclerView);
	}

	ItemTouchHelper.SimpleCallback swipeItemTouchCallbackTeam = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT)
	{
		@Override
		public int getSwipeDirs(RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder)
		{
			// This will enable/disable swiping depending on whether the current item is edit locked
			int position = viewHolder.getAdapterPosition();
			TeamInfo teamInfo = gameTracker.getTeams().get(position);
			if(teamInfo.getEditLock() != 0)
				return 0;
			else
				return super.getSwipeDirs(recyclerView, viewHolder);
		}

		@Override
		public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target)
		{
			return false;
		}

		@Override
		public void onSwiped(final RecyclerView.ViewHolder viewHolder, int direction)
		{
			AlertDialog.Builder builder = new AlertDialog.Builder(ToastHelper.context);

			builder.setTitle("Delete Entry");
			builder.setMessage("Are you sure you want to delete this entry?");

			builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener()
			{
				@Override
				public void onClick(DialogInterface dialog, int which)
				{
					//Remove swiped item from list and notify the RecyclerView
					try
					{
						int position = viewHolder.getAdapterPosition();
						gameTracker.getTeams().remove(position);
						recyclerView.getAdapter().notifyDataSetChanged();
					}
					catch (Exception e)
					{
						//Log.d("Exception", "Out of bounds");
					}
					dialog.cancel();
				}
			});
			builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener()
			{
				@Override
				public void onClick(DialogInterface dialog, int which)
				{
					// don't do anything, user cancelled
					recyclerView.getAdapter().notifyDataSetChanged();
					dialog.cancel();
				}
			});

			builder.setOnCancelListener(new DialogInterface.OnCancelListener()
			{
				@Override
				public void onCancel(DialogInterface dialog)
				{
					// don't do anything, user cancelled
					recyclerView.getAdapter().notifyDataSetChanged();
					dialog.cancel();
				}
			});

			builder.setOnDismissListener(new DialogInterface.OnDismissListener()
			{
				@Override
				public void onDismiss(DialogInterface dialog)
				{
					// don't do anything, user cancelled
					recyclerView.getAdapter().notifyDataSetChanged();
					dialog.cancel();
				}
			});

			AlertDialog dialog = builder.create();
			dialog.show();
		}
	};

	class teamToolbarMenu implements Toolbar.OnMenuItemClickListener
	{
		public boolean onMenuItemClick(MenuItem item)
		{
			switch(item.getItemId())
			{
				// add a player
				case R.id.team_toolbar_add_team:
					gameTracker.addTeam(new TeamInfo());
					recyclerView.getAdapter().notifyDataSetChanged();
					break;

				// trigger a manual sort of the players
				case R.id.team_toolbar_sort_teams:
					Collections.sort(gameTracker.getTeams());
					recyclerView.getAdapter().notifyDataSetChanged();
					//ToastHelper.Toast(getApplication(), "Sorted");
					break;

				default:
					return false;
			}
			return true;
		}

	}

	/* *********************************************************************************************
	 			OTHER MENU OPTIONS
	   ****************************************************************************************** */

	/**
	 * Save the game information to history
	 */
	public void saveGameInformation()
	{
		// TODO: process the save call
	}

	/**
	 * Displays the history
	 */
	public void viewHistory()
	{
		// TODO: process viewHistory call
	}

	/**
	 * Displays the settings dialog
	 */
	public void displaySettings()
	{
		Intent i = new Intent(this, Settings.class);
		startActivity(i);
	}

	/**
	 * Send an email to dancing.weasels.software@gmail.com
	 */
	public void contactMenuClicked()
	{

		Intent i = new Intent(Intent.ACTION_SENDTO);
		i.setType("message/rfc822");
		i.putExtra(Intent.EXTRA_SUBJECT, "Score Keeper");
		i.setData(Uri.parse("mailto:dancing.weasels.software@gmail.com"));
		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(i);
	}

	/* *********************************************************************************************
				HELPER FUNCTIONS
	   ****************************************************************************************** */

	/**
	 * Resets any listeners for the recycler view
	 */
	public void resetListeners()
	{
		if(recyclerView != null && recyclerView.hasOnClickListeners())
		{
			recyclerView.setOnClickListener(null);
			recyclerView.setOnLongClickListener(null);
		}

		if(itemTouchHelperPlayer != null)
			itemTouchHelperPlayer.attachToRecyclerView(null);

		if(itemTouchHelperTeam != null)
			itemTouchHelperTeam.attachToRecyclerView(null);

	}
}
