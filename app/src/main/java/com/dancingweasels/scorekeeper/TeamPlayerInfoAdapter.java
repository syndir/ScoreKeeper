package com.dancingweasels.scorekeeper;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatDialog;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class TeamPlayerInfoAdapter extends RecyclerView.Adapter<TeamPlayerInfoAdapter.TeamPlayerInfoViewHolder>
{
	private boolean editLock;
	private List<TeamPlayerInfo> teamPlayerInfoList;
	TeamPlayerInfoAdapter(List<TeamPlayerInfo> teamPlayerInfoList) { this.teamPlayerInfoList = teamPlayerInfoList; editLock = false; }

	/**
	 * Called when RecyclerView needs a new {@link TeamPlayerInfoViewHolder} of the given type to represent
	 * an item.
	 * <p>
	 * This new ViewHolder should be constructed with a new View that can represent the items
	 * of the given type. You can either create a new View manually or inflate it from an XML
	 * layout file.
	 * <p>
	 * The new ViewHolder will be used to display items of the adapter using
	 * {@link #onBindViewHolder(TeamPlayerInfoViewHolder, int, List)}. Since it will be re-used to display
	 * different items in the data set, it is a good idea to cache references to sub views of
	 * the View to avoid unnecessary {@link View#findViewById(int)} calls.
	 *
	 * @param parent   The ViewGroup into which the new View will be added after it is bound to
	 *                 an adapter position.
	 * @param viewType The view type of the new View.
	 * @return A new ViewHolder that holds a View of the given view type.
	 * @see #getItemViewType(int)
	 * @see #onBindViewHolder(TeamPlayerInfoViewHolder, int)
	 */
	@NonNull
	@Override
	public TeamPlayerInfoAdapter.TeamPlayerInfoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
	{
		// inflate the view
		LayoutInflater inflater = LayoutInflater.from(parent.getContext());
		View view = inflater.inflate(R.layout.team_member_layout, parent, false);

		// return the view holder for this view
		return new TeamPlayerInfoViewHolder(view);
	}

	/**
	 * Called by RecyclerView to display the data at the specified position. This method should
	 * update the contents of the {@link TeamPlayerInfoViewHolder#itemView} to reflect the item at the given
	 * position.
	 * <p>
	 * Note that unlike {@link ListView}, RecyclerView will not call this method
	 * again if the position of the item changes in the data set unless the item itself is
	 * invalidated or the new position cannot be determined. For this reason, you should only
	 * use the <code>position</code> parameter while acquiring the related data item inside
	 * this method and should not keep a copy of it. If you need the position of an item later
	 * on (e.g. in a click listener), use {@link TeamPlayerInfoViewHolder#getAdapterPosition()} which will
	 * have the updated adapter position.
	 * <p>
	 * Override {@link #onBindViewHolder(TeamPlayerInfoViewHolder, int, List)} instead if Adapter can
	 * handle efficient partial bind.
	 *
	 * @param holder   The ViewHolder which should be updated to represent the contents of the
	 *                 item at the given position in the data set.
	 * @param position The position of the item within the adapter's data set.
	 */
	@Override
	public void onBindViewHolder(@NonNull TeamPlayerInfoAdapter.TeamPlayerInfoViewHolder holder, int position)
	{
		holder.teamMemberNameTextWatcher.setPosition(position);
		holder.teamMemberScoreOnClickListener.setPosition(position);

		// get the data & bind to the views
		TeamPlayerInfo teamPlayerInfo = teamPlayerInfoList.get(position);
		holder.teamMemberNameEditText.setText(teamPlayerInfo.getTeamPlayerName());
		holder.teamMemberScoreButton.setText(String.format(Locale.getDefault(), "%d", teamPlayerInfo.getTeamPlayerScore()));

		if(Settings.isTeamTrackIndividualScoresEnabled())
			holder.teamMemberScoreButton.setVisibility(View.VISIBLE);
		else
			holder.teamMemberScoreButton.setVisibility(View.GONE);

		holder.teamMemberNameEditText.setEnabled(!editLock);
		holder.teamMemberScoreButton.setEnabled(!editLock);
	}


	/**
	 * Returns the total number of items in the data set held by the adapter.
	 *
	 * @return The total number of items in this adapter.
	 */
	@Override
	public int getItemCount()
	{
		return teamPlayerInfoList.size();
	}

	/**
	 * Set our edit lock style
	 */
	public void setEditLock(boolean editLock) { this.editLock = editLock; }

	class TeamPlayerInfoViewHolder extends RecyclerView.ViewHolder implements SharedPreferences.OnSharedPreferenceChangeListener
	{
		SharedPreferences sharedPreferences;
		Button teamMemberScoreButton;
		EditText teamMemberNameEditText;

		TeamMemberNameTextWatcher teamMemberNameTextWatcher;
		TeamMemberScoreOnClickListener teamMemberScoreOnClickListener;

		TeamPlayerInfoViewHolder(View itemView)
		{
			super(itemView);

			sharedPreferences = PreferenceManager.getDefaultSharedPreferences(ToastHelper.context);
			sharedPreferences.registerOnSharedPreferenceChangeListener(this);

			teamMemberScoreButton = itemView.findViewById(R.id.team_member_score_button);
			teamMemberNameEditText = itemView.findViewById(R.id.team_member_name_edittext);

			// our watchers/listeners
			teamMemberNameTextWatcher = new TeamMemberNameTextWatcher();
			teamMemberNameEditText.addTextChangedListener(teamMemberNameTextWatcher);
			teamMemberScoreOnClickListener = new TeamMemberScoreOnClickListener();
			teamMemberScoreButton.setOnClickListener(teamMemberScoreOnClickListener);
		}

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
			switch(key)
			{
				case Settings.SETTINGS_TEAM_SORT_METHOD:
					if(Settings.isAutoSortEnabled())
					{
						Collections.sort(teamPlayerInfoList);
						notifyDataSetChanged();
					}
					break;

				// this is so that when a user changes the "team score tracking method" preference,
				// it's updated immediately
				case Settings.SETTINGS_TEAM_SCORE_TRACK_METHOD:
					notifyDataSetChanged();
					break;
			}
		}
	}

	class TeamMemberNameTextWatcher implements TextWatcher
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
			TeamPlayerInfo teamPlayerInfo = teamPlayerInfoList.get(position);
			teamPlayerInfo.setTeamPlayerName(s);
			teamPlayerInfoList.set(position, teamPlayerInfo);
		}

		@Override
		public void afterTextChanged(Editable editable) { }
	}

	class TeamMemberScoreOnClickListener implements Button.OnClickListener, NumberPicker.OnValueChangeListener, TextWatcher
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
			TeamPlayerInfo tpi = teamPlayerInfoList.get(position);
			String s = String.format(Locale.getDefault(), "Current Score: %d", tpi.getTeamPlayerScore());
			if(currentScoreTextView != null)
				currentScoreTextView.setText(s);

			switch (Settings.getScoreEditMode())
			{
				// picker mode
				case Settings.ScoreEditMode.PICKER:
					if(scoreEditText != null)
						scoreEditText.setVisibility(View.GONE);
					if(numberPicker != null)
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
						int score = teamPlayerInfoList.get(position).getTeamPlayerScore();
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
								TeamPlayerInfo teamPlayerInfo = teamPlayerInfoList.get(position);
								if (numberPicker != null)
									teamPlayerInfo.setTeamPlayerScore(Integer.parseInt(arr[numberPicker.getValue()]));
								else
									teamPlayerInfo.setTeamPlayerScore(0);
								teamPlayerInfoList.set(position, teamPlayerInfo);
								dialog.dismiss();

								// should we sort?
								if (Settings.isAutoSortEnabled())
									Collections.sort(teamPlayerInfoList);

								notifyDataSetChanged();

								// TODO: redraw parent
								// we do this to force the team score to update
								// there *has* to be a better way, as this redraws all the team views
								if (Settings.isTeamTrackIndividualScoresEnabled())
								{
									SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ToastHelper.context);
									SharedPreferences.Editor editor = preferences.edit();
									editor.remove("com.dancingweasels.UPDATE_TEAMSCORE");
									editor.commit();
									editor.putInt("com.dancingweasels.UPDATE_TEAMSCORE", 1);
									editor.commit();
								}
							}
						});
					}
					break;

				// EditText mode - absolute
				case Settings.ScoreEditMode.ABSOLUTE:
					if(numberPicker != null)
						numberPicker.setVisibility(View.GONE);
					if(scoreEditText != null)
					{
						scoreEditText.setVisibility(View.VISIBLE);
						scoreEditText.setText(String.valueOf(teamPlayerInfoList.get(position).getTeamPlayerScore()));
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
								TeamPlayerInfo teamPlayerInfo = teamPlayerInfoList.get(position);

								int val = 0;
								// save #
								try
								{
									if(scoreEditText != null)
										val = Integer.parseInt(scoreEditText.getText().toString());
								} catch (Exception e)
								{
									val = teamPlayerInfo.getTeamPlayerScore();
								}
								if (scoreEditText != null)
									teamPlayerInfo.setTeamPlayerScore(val);
								else
									teamPlayerInfo.setTeamPlayerScore(teamPlayerInfo.getTeamPlayerScore());

								teamPlayerInfoList.set(position, teamPlayerInfo);
								dialog.dismiss();

								// should we sort?
								if (Settings.isAutoSortEnabled())
									Collections.sort(teamPlayerInfoList);

								notifyDataSetChanged();
							}
						});
					}
					break;
				case Settings.ScoreEditMode.OFFSET:
					if(numberPicker != null)
						numberPicker.setVisibility(View.GONE);
					if(scoreEditText != null)
					{
						scoreEditText.setVisibility(View.VISIBLE);
						scoreEditText.setText("");
						scoreEditText.addTextChangedListener(this);
						scoreEditText.setHint(R.string.score_edittext_offset);
					}

					scoreChangedByTextView.setText(String.format(Locale.getDefault(), "%s: %d",
							ToastHelper.context.getString(R.string.new_score), teamPlayerInfoList.get(position).getTeamPlayerScore()));
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
								TeamPlayerInfo teamPlayerInfo = teamPlayerInfoList.get(position);

								// offset in case we are doing offset mode (oldscore +/- entered value)
								int offsetBase = teamPlayerInfoList.get(position).getTeamPlayerScore();
								int val = 0;

								// save #
								try
								{
									if(scoreEditText != null)
										val = Integer.parseInt(scoreEditText.getText().toString());
								} catch (Exception e)
								{
									val = 0;
								}
								if (scoreEditText != null)
									teamPlayerInfo.setTeamPlayerScore(offsetBase + val);
								else
									teamPlayerInfo.setTeamPlayerScore(offsetBase);
								teamPlayerInfoList.set(position, teamPlayerInfo);
								dialog.dismiss();

								// should we sort?
								if (Settings.isAutoSortEnabled())
									Collections.sort(teamPlayerInfoList);

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
			int delta = val - teamPlayerInfoList.get(position).getTeamPlayerScore();

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
					val = teamPlayerInfoList.get(position).getTeamPlayerScore();
				}

				int delta = val - teamPlayerInfoList.get(position).getTeamPlayerScore();
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

				int delta = val + teamPlayerInfoList.get(position).getTeamPlayerScore();
				scoreChangedByTextView.setText(String.format(Locale.getDefault(), "%s: %d",
						ToastHelper.context.getString(R.string.new_score), delta));
			}
		}

		@Override
		public void afterTextChanged(Editable s) {}
	}


}
