package com.dancingweasels.scorekeeper;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.Editable;
import android.text.TextWatcher;
//import android.util.Log;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class TeamAdapter extends RecyclerView.Adapter<TeamAdapter.TeamViewHolder>
{
	private List<TeamInfo> teamInfoList;

	TeamAdapter(List<TeamInfo> teamInfoList) { this.teamInfoList = teamInfoList; }


	/**
	 * Called when RecyclerView needs a new {@link TeamViewHolder} of the given type to represent
	 * an item.
	 * <p>
	 * This new ViewHolder should be constructed with a new View that can represent the items
	 * of the given type. You can either create a new View manually or inflate it from an XML
	 * layout file.
	 * <p>
	 * The new ViewHolder will be used to display items of the adapter using
	 * {@link #onBindViewHolder(TeamViewHolder, int, List)}. Since it will be re-used to display
	 * different items in the data set, it is a good idea to cache references to sub views of
	 * the View to avoid unnecessary {@link View#findViewById(int)} calls.
	 *
	 * @param parent   The ViewGroup into which the new View will be added after it is bound to
	 *                 an adapter position.
	 * @param viewType The view type of the new View.
	 * @return A new ViewHolder that holds a View of the given view type.
	 * @see #getItemViewType(int)
	 * @see #onBindViewHolder(TeamViewHolder, int)
	 */
	@NonNull
	@Override
	public TeamAdapter.TeamViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
	{
		// inflate the view
		LayoutInflater inflater = LayoutInflater.from(parent.getContext());
		View view = inflater.inflate(R.layout.team_list_layout, parent, false);

		// return the view holder for this view
		return new TeamViewHolder(view);
	}

	/**
	 * Called by RecyclerView to display the data at the specified position. This method should
	 * update the contents of the {@link TeamViewHolder#itemView} to reflect the item at the given
	 * position.
	 * <p>
	 * Note that unlike {@link ListView}, RecyclerView will not call this method
	 * again if the position of the item changes in the data set unless the item itself is
	 * invalidated or the new position cannot be determined. For this reason, you should only
	 * use the <code>position</code> parameter while acquiring the related data item inside
	 * this method and should not keep a copy of it. If you need the position of an item later
	 * on (e.g. in a click listener), use {@link TeamViewHolder#getAdapterPosition()} which will
	 * have the updated adapter position.
	 * <p>
	 * Override {@link #onBindViewHolder(TeamViewHolder, int, List)} instead if Adapter can
	 * handle efficient partial bind.
	 *
	 * @param holder   The ViewHolder which should be updated to represent the contents of the
	 *                 item at the given position in the data set.
	 * @param position The position of the item within the adapter's data set.
	 */
	@Override
	public void onBindViewHolder(@NonNull TeamViewHolder holder, int position)
	{
		// set the position in the view holder
		holder.teamNameTextWatcher.setPosition(position);
		holder.teamScoreOnClickListener.setPosition(position);
		holder.teamEditLockOnClickListener.setPosition(position);
		holder.teamAddMemberOnClickListener.setPosition(position);
		holder.swipeToRemoveTeamMember.setListPosition(position);

		// set up the recycler view for our member list
		holder.teamPlayerListRecyclerView.setHasFixedSize(true);
		holder.teamPlayerListRecyclerView.setLayoutManager(new GridLayoutManager(ToastHelper.context, 2)); // grid layout, 2 columns wide

		// set up swipe right to remove team member
		//initSwipeToRemoveTeamMember(holder.teamPlayerListRecyclerView, position);

		// create the adapter
		TeamPlayerInfoAdapter adapter = new TeamPlayerInfoAdapter(teamInfoList.get(position).getTeamPlayerInfoList());
		holder.teamPlayerListRecyclerView.setAdapter(adapter);

		// get the data from the list & bind to the views
		TeamInfo teamInfo = teamInfoList.get(position);
		holder.teamNameEditText.setText(teamInfo.getTeamName());

		// update the image for the expand icon
		if(holder.teamMemberListExpanded)
			holder.teamMembersExpandImageButton.setImageResource(R.drawable.ic_keyboard_arrow_up_black_24dp);
		else
			holder.teamMembersExpandImageButton.setImageResource(R.drawable.ic_keyboard_arrow_down_black_24dp);

		// if we are tracking by player scores, disable the team score (player scores will be shown by the TeamPlayerInfoAdapter)
		if(Settings.isTeamTrackIndividualScoresEnabled())
		{
			holder.teamScoreButton.setEnabled(false);
		}
		else // we are tracking by team score, enable team score (player scores will be hidden by the TeamPlayerInfoAdapter)
		{
			holder.teamScoreButton.setEnabled(true);
		}

		// set up the edit lock
		int lockLevel = teamInfo.getEditLock();
		if(lockLevel == 0)
			holder.teamEditLockImageButton.setImageResource(R.drawable.ic_lock_open_black_24dp);
		else if(lockLevel == 1)
			holder.teamEditLockImageButton.setImageResource(R.drawable.ic_lock_outline_black_24dp);
		else
			holder.teamEditLockImageButton.setImageResource(R.drawable.ic_lock_black_24dp);
		setEditable(holder, lockLevel);

		// calculate the score if we're basing it off member scores
		if(Settings.isTeamTrackIndividualScoresEnabled())
		{
			int score = 0;
			List<TeamPlayerInfo> tpiList = teamInfo.getTeamPlayerInfoList();
			for(TeamPlayerInfo tpi : tpiList)
				score += tpi.getTeamPlayerScore();

			holder.teamScoreButton.setText(String.format(Locale.getDefault(), "%d", score));
		}
		else
		{
			holder.teamScoreButton.setText(String.format(Locale.getDefault(), "%d", teamInfo.getTeamScore()));
		}

		holder.teamPlayerListRecyclerView.getAdapter().notifyDataSetChanged();
	}


	private void setEditable(@NonNull TeamViewHolder holder, int lockLevel)
	{
		if(lockLevel == 0) // enable member stuff
		{
			TeamPlayerInfoAdapter adapter = (TeamPlayerInfoAdapter)holder.teamPlayerListRecyclerView.getAdapter();
			adapter.setEditLock(false);
			holder.teamPlayerListRecyclerView.setAdapter(adapter);
		}
		if(lockLevel == 0 || lockLevel == 1) // enable team stuff
		{
			holder.teamScoreButton.setEnabled(true);
			holder.addTeamMemberImageButton.setEnabled(true);
			holder.teamNameEditText.setEnabled(true);
		}

		if(lockLevel == 1 || lockLevel == 2) // disable member stuff
		{
			TeamPlayerInfoAdapter adapter = (TeamPlayerInfoAdapter)holder.teamPlayerListRecyclerView.getAdapter();
			adapter.setEditLock(true);
			holder.teamPlayerListRecyclerView.setAdapter(adapter);
		}

		if(lockLevel == 2) // disable team stuff
		{
			holder.teamScoreButton.setEnabled(false);
			holder.addTeamMemberImageButton.setEnabled(false);
			holder.teamNameEditText.setEnabled(false);
		}
	}


	/**
	 * Returns the total number of items in the data set held by the adapter.
	 *
	 * @return The total number of items in this adapter.
	 */
	@Override
	public int getItemCount()
	{
		return this.teamInfoList.size();
	}

	class TeamViewHolder extends RecyclerView.ViewHolder implements SharedPreferences.OnSharedPreferenceChangeListener
	{
		SharedPreferences sharedPreferences;
		boolean teamMemberListExpanded;

		EditText teamNameEditText;
		Button teamScoreButton;
		ImageButton addTeamMemberImageButton;
		ImageButton teamEditLockImageButton;
		ImageButton teamMembersExpandImageButton;
		ImageButton teamMembersSortImageButton;
		RecyclerView teamPlayerListRecyclerView;

		TeamNameTextWatcher teamNameTextWatcher;
		TeamScoreOnClickListener teamScoreOnClickListener;
		TeamEditLockOnClickListener teamEditLockOnClickListener;
		TeamAddMemberOnClickListener teamAddMemberOnClickListener;
		TeamSortOnClickListener teamSortOnClickListener;
		swipeRemoveTeamMember swipeToRemoveTeamMember;

		TeamViewHolder(View itemView)
		{
			super(itemView);


			sharedPreferences = PreferenceManager.getDefaultSharedPreferences(ToastHelper.context);
			sharedPreferences.registerOnSharedPreferenceChangeListener(this);

			teamPlayerListRecyclerView = itemView.findViewById(R.id.team_playerlist_recyclerview);
			teamNameEditText = itemView.findViewById(R.id.team_name_edittext);
			teamScoreButton = itemView.findViewById(R.id.team_score_button);
			addTeamMemberImageButton = itemView.findViewById(R.id.add_team_member_imagebutton);
			teamEditLockImageButton = itemView.findViewById(R.id.team_edit_lock_imagebutton);
			teamMembersExpandImageButton = itemView.findViewById(R.id.team_members_expand_imagebutton);
			teamMembersSortImageButton = itemView.findViewById(R.id.team_members_sort_imagebutton);

			// our watchers/listeners
			teamNameTextWatcher = new TeamNameTextWatcher();
			teamNameEditText.addTextChangedListener(teamNameTextWatcher);
			teamScoreOnClickListener = new TeamScoreOnClickListener();
			teamScoreButton.setOnClickListener(teamScoreOnClickListener);
			teamEditLockOnClickListener = new TeamEditLockOnClickListener();
			teamEditLockImageButton.setOnClickListener(teamEditLockOnClickListener);
			teamAddMemberOnClickListener = new TeamAddMemberOnClickListener();
			addTeamMemberImageButton.setOnClickListener(teamAddMemberOnClickListener);
			teamSortOnClickListener = new TeamSortOnClickListener();
			teamMembersSortImageButton.setOnClickListener(teamSortOnClickListener);
			teamMembersExpandImageButton.setOnClickListener(teamMembersExpandOnClickListener);

			// initialize swipe to remove team members
			swipeToRemoveTeamMember = new swipeRemoveTeamMember();
			ItemTouchHelper itemTouchHelper = new ItemTouchHelper(swipeToRemoveTeamMember.swipeItemTouchCallbackTeam);
			itemTouchHelper.attachToRecyclerView(teamPlayerListRecyclerView);

			// is it expanded or collapsed by default ?
			teamMemberListExpanded = true;
		}

		/**
		 * Expand/collapse the team member list
		 */
		ImageButton.OnClickListener teamMembersExpandOnClickListener = new ImageButton.OnClickListener()
		{
			/**
			 * Called when a view has been clicked.
			 *
			 * @param v The view that was clicked.
			 */
			@Override
			public void onClick(View v)
			{
				int vis = teamPlayerListRecyclerView.getVisibility();
				if(vis == View.VISIBLE)
				{
					teamPlayerListRecyclerView.setVisibility(View.GONE);
					teamMemberListExpanded = false;
					teamMembersExpandImageButton.setImageResource(R.drawable.ic_keyboard_arrow_down_black_24dp);
				}
				else
				{
					teamPlayerListRecyclerView.setVisibility(View.VISIBLE);
					teamMemberListExpanded = true;
					teamMembersExpandImageButton.setImageResource(R.drawable.ic_keyboard_arrow_up_black_24dp);
				}
			}
		};

		/**
		 * Called when a shared preference is changed, added, or removed. This
		 * may be called even if a preference is set to its existing value.
		 * <p>
		 * <p>This callback will be run on your main thread.
		 *
		 * @param sharedPreferences The {@link SharedPreferences} that received
		 *                          the change.
		 * @param key               The key of the preference that was changed, added, or
		 */
		@Override
		public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
		{
			switch (key)
			{
				case Settings.SETTINGS_TEAM_SCORE_TRACK_METHOD:
					notifyDataSetChanged();
					break;

				case "com.dancingweasels.UPDATE_TEAMSCORE":
					SharedPreferences.Editor editor = sharedPreferences.edit();
					editor.remove("com.dancingweasels.UPDATE_TEAMSCORE");
					editor.commit();
					notifyDataSetChanged();
					break;
			}
		}
	}

	class TeamNameTextWatcher implements TextWatcher
	{
		// this is used to know which list # we are so we can update the correct record
		private int position;
		public void setPosition(int position) { this.position = position; }

		@Override
		public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

		@Override
		public void onTextChanged(CharSequence charSequence, int i, int i1, int i2)
		{
			String s = charSequence.toString();
			TeamInfo teamInfo = teamInfoList.get(position);
			teamInfo.setTeamName(s);
			teamInfoList.set(position, teamInfo);
		}

		@Override
		public void afterTextChanged(Editable editable) { }
	}

	class TeamScoreOnClickListener implements Button.OnClickListener, NumberPicker.OnValueChangeListener, TextWatcher
	{
		// this is used to know which list # we are so we can update the correct record
		private int position;
		public void setPosition(int position) { this.position = position; }

		TextView scoreChangedByTextView;

		@Override
		public void onClick(View view)
		{
			createPickerDialog(view);
		}

		private void createPickerDialog(View view)
		{
			// if we're tracking by player scores, do nothing
			if(Settings.isTeamTrackIndividualScoresEnabled())
				return;

			final AppCompatDialog dialog = new AppCompatDialog(view.getContext(), R.style.Theme_App_AppCompatDialogStyle);
			dialog.setTitle("Set Score");
			dialog.setContentView(R.layout.picker_dialog);
			Button buttonSet = dialog.findViewById(R.id.button_set);
			Button buttonCancel = dialog.findViewById(R.id.button_cancel);
			final NumberPicker numberPicker = dialog.findViewById(R.id.number_picker);
			final EditText scoreEditText = dialog.findViewById(R.id.score_edittext);

			// update the score changed by textview
			scoreChangedByTextView = dialog.findViewById(R.id.score_change_textview);
			scoreChangedByTextView.setText(String.format(Locale.getDefault(), "%s: %+d", ToastHelper.context.getResources().getString(R.string.score_adjusted_by), 0));

			// update the textview to display the current score
			TextView currentScoreTextView = dialog.findViewById(R.id.current_score_textview);
			String s = String.format(Locale.getDefault(), "Current Score: %d", teamInfoList.get(position).getTeamScore());
			if(currentScoreTextView != null)
				currentScoreTextView.setText(s);

			switch (Settings.getScoreEditMode())
			{
				// picker mode
				case Settings.ScoreEditMode.PICKER:
					if (scoreEditText != null)
						scoreEditText.setVisibility(View.GONE);
					if (numberPicker != null)
						numberPicker.setVisibility(View.VISIBLE);

					scoreChangedByTextView.setText(String.format(Locale.getDefault(), "%s: %+d", ToastHelper.context.getResources().getString(R.string.score_adjusted_by), 0));

					// we need to figure out how big to make the array
					// ie: maxval 100 and minval 0 in steps of 5 is only 20 entries..
					int numberOfValues = ((Settings.getMaxValue() - Settings.getMinValue()) / Settings.getStepValue()) + 1;
					if ((Settings.getMaxValue() - Settings.getMinValue()) % Settings.getStepValue() != 0)
					{
						ToastHelper.Toast(String.format(Locale.getDefault(), "Range of values is not evenly divisible into steps of %d", Settings.getStepValue()));
					}

					// set the array
					final String[] arr = new String[numberOfValues];
					for (int i = 0; i < numberOfValues; i++)
						arr[i] = String.valueOf((i * Settings.getStepValue()) + Settings.getMinValue());
					if (numberPicker != null)
					{
						numberPicker.setMinValue(0);
						numberPicker.setMaxValue(arr.length - 1);
						numberPicker.setDisplayedValues(arr);
						numberPicker.setWrapSelectorWheel(false);
						numberPicker.setOnClickListener(this);
						numberPicker.setOnValueChangedListener(this);

						// set the displayed/selected value to the stored value
						int score = teamInfoList.get(position).getTeamScore();
						int pos = (score - Settings.getMinValue()) / Settings.getStepValue();
						numberPicker.setValue(pos);
					}

					if (buttonCancel != null)
					{
						buttonCancel.setOnClickListener(new Button.OnClickListener()
						{
							@Override
							public void onClick(View view)
							{
								dialog.dismiss();
							}
						});
					}

					if (buttonSet != null)
					{
						buttonSet.setOnClickListener(new Button.OnClickListener()
						{
							@Override
							public void onClick(View view)
							{
								// save #
								TeamInfo teamInfo = teamInfoList.get(position);
								if (numberPicker != null)
									teamInfo.setTeamScore(Integer.parseInt(arr[numberPicker.getValue()]));
								teamInfoList.set(position, teamInfo);
								dialog.dismiss();

								// should we sort?
								if (Settings.isAutoSortEnabled())
									Collections.sort(teamInfoList);

								notifyDataSetChanged();
							}
						});
					}
					break;

				// EditText mode - absolute
				case Settings.ScoreEditMode.ABSOLUTE:
					if (numberPicker != null)
						numberPicker.setVisibility(View.GONE);

					if (scoreEditText != null)
					{
						scoreEditText.setVisibility(View.VISIBLE);
						scoreEditText.setText(String.valueOf(teamInfoList.get(position).getTeamScore()));
						scoreEditText.addTextChangedListener(this);
						scoreEditText.setHint(R.string.score_edittext_absolute);
					}

					scoreChangedByTextView.setText(String.format(Locale.getDefault(), "%s: %+d",
							ToastHelper.context.getResources().getString(R.string.score_adjusted_by), 0));

					if (buttonCancel != null)
					{
						buttonCancel.setOnClickListener(new Button.OnClickListener()
						{
							@Override
							public void onClick(View view)
							{
								dialog.dismiss();
							}
						});
					}

					if (buttonSet != null)
					{
						buttonSet.setOnClickListener(new Button.OnClickListener()
						{
							@Override
							public void onClick(View view)
							{
								TeamInfo teamInfo = teamInfoList.get(position);

								int val = 0;
								// save #
								try
								{
									if (scoreEditText != null)
										val = Integer.parseInt(scoreEditText.getText().toString());
								} catch (Exception e)
								{
									val = teamInfo.getTeamScore();
								}
								teamInfo.setTeamScore(val);

								teamInfoList.set(position, teamInfo);
								dialog.dismiss();

								// should we sort?
								if (Settings.isAutoSortEnabled())
									Collections.sort(teamInfoList);

								notifyDataSetChanged();
							}
						});
					}
					break;

				// EditText mode - Offset
				case Settings.ScoreEditMode.OFFSET:
					if (numberPicker != null)
						numberPicker.setVisibility(View.GONE);
					if (scoreEditText != null)
					{
						scoreEditText.setVisibility(View.VISIBLE);
						scoreEditText.setText("");
						scoreEditText.addTextChangedListener(this);
						scoreEditText.setHint(R.string.score_edittext_offset);
					}

					scoreChangedByTextView.setText(String.format(Locale.getDefault(), "%s: %d",
							ToastHelper.context.getString(R.string.new_score), teamInfoList.get(position).getTeamScore()));

					if (buttonCancel != null)
					{
						buttonCancel.setOnClickListener(new Button.OnClickListener()
						{
							@Override
							public void onClick(View view)
							{
								dialog.dismiss();
							}
						});
					}

					if (buttonSet != null)
					{
						buttonSet.setOnClickListener(new Button.OnClickListener()
						{
							@Override
							public void onClick(View view)
							{
								TeamInfo teamInfo = teamInfoList.get(position);

								// offset in case we are doing offset mode (oldscore +/- entered value)
								int offsetBase = teamInfoList.get(position).getTeamScore();
								int val = 0;

								// save #
								try
								{
									if (scoreEditText != null)
										val = Integer.parseInt(scoreEditText.getText().toString());
								} catch (Exception e)
								{
									val = 0;
								}
								teamInfo.setTeamScore(offsetBase + val);
								teamInfoList.set(position, teamInfo);
								dialog.dismiss();

								// should we sort?
								if (Settings.isAutoSortEnabled())
									Collections.sort(teamInfoList);

								notifyDataSetChanged();
							}
						});
					}
					break;
			}
			dialog.show();
		}

		@Override
		public void onValueChange(NumberPicker picker, int oldVal, int newVal)
		{
			int pos = picker.getValue();
			int val = (pos * Settings.getStepValue()) + Settings.getMinValue();
			int delta = val - teamInfoList.get(position).getTeamScore();

			scoreChangedByTextView.setText(String.format(Locale.getDefault(), "%s: %+d",
					ToastHelper.context.getResources().getString(R.string.score_adjusted_by), delta));
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count)
		{
			int val;
			if(Settings.getScoreEditMode().equals(Settings.ScoreEditMode.ABSOLUTE))
			{
				try {
					val = Integer.parseInt(s.toString());
				} catch (Exception e) {
					val = teamInfoList.get(position).getTeamScore();
				}

				int delta = val - teamInfoList.get(position).getTeamScore();
				scoreChangedByTextView.setText(String.format(Locale.getDefault(), "%s: %+d",
						ToastHelper.context.getString(R.string.score_adjusted_by), delta));
			}
			else if(Settings.getScoreEditMode().equals(Settings.ScoreEditMode.OFFSET))
			{
				try {
					val = Integer.parseInt(s.toString());
				} catch (Exception e) {
					val = 0;
				}

				int delta = val + teamInfoList.get(position).getTeamScore();
				scoreChangedByTextView.setText(String.format(Locale.getDefault(), "%s: %d",
						ToastHelper.context.getString(R.string.new_score), delta));
			}
		}

		@Override
		public void afterTextChanged(Editable s) {}
	}

	class TeamEditLockOnClickListener implements ImageButton.OnClickListener
	{
		// this is used to know which list # we are so we can update the correct record
		private int position;
		public void setPosition(int position) { this.position = position; }

		@Override
		public void onClick(View view)
		{
			TeamInfo teamInfo = teamInfoList.get(position);

			int lockLevel = teamInfo.getEditLock();

			teamInfo.setEditLock((lockLevel+1) % 3);

			teamInfoList.set(position, teamInfo);
			notifyDataSetChanged();
		}
	}

	class TeamAddMemberOnClickListener implements ImageButton.OnClickListener
	{
		// this is used to know which list # we are so we can update the correct record
		private int position;
		public void setPosition(int position) { this.position = position; }

		@Override
		public void onClick(View view)
		{
			teamInfoList.get(position).getTeamPlayerInfoList().add(new TeamPlayerInfo("", Settings.getMinValue(), false));
			notifyDataSetChanged();
		}
	}

	class TeamSortOnClickListener implements ImageButton.OnClickListener
	{
		// this is used to know which list # we are so we can update the correct record
		private int position;
		public void setPosition(int position) { this.position = position; }

		@Override
		public void onClick(View view)
		{
			Collections.sort(teamInfoList.get(position).getTeamPlayerInfoList());
			notifyDataSetChanged();
		}
	}

	class swipeRemoveTeamMember
	{
		int listPosition;
		void setListPosition(int listPosition) { this.listPosition = listPosition; }

		ItemTouchHelper.SimpleCallback swipeItemTouchCallbackTeam = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT)
		{
			@Override
			public int getSwipeDirs(RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder)
			{
				// This will enable/disable swiping depending on whether the current item is edit locked
				//int position = viewHolder.getAdapterPosition();
				TeamInfo teamInfo = teamInfoList.get(listPosition);
				if (teamInfo.getEditLock() != 0)
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
							//teamInfoList.remove(position);
							teamInfoList.get(listPosition).getTeamPlayerInfoList().remove(position);
							notifyDataSetChanged();
						}
						catch (Exception e)
						{
							//Log.d("Exception", "ArrayIndexOutOfBounds");
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
						notifyDataSetChanged();
						dialog.cancel();
					}
				});

				builder.setOnCancelListener(new DialogInterface.OnCancelListener()
				{
					@Override
					public void onCancel(DialogInterface dialog)
					{
						// don't do anything, user cancelled
						notifyDataSetChanged();
						dialog.cancel();
					}
				});

				builder.setOnDismissListener(new DialogInterface.OnDismissListener()
				{
					@Override
					public void onDismiss(DialogInterface dialog)
					{
						// don't do anything, user cancelled
						notifyDataSetChanged();
						dialog.cancel();
					}
				});

				AlertDialog dialog = builder.create();
				dialog.show();
			}
		};
	}
}
