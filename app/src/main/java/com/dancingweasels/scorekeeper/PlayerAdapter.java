package com.dancingweasels.scorekeeper;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class PlayerAdapter extends RecyclerView.Adapter<PlayerAdapter.PlayerViewHolder>
{
	private List<PlayerInfo> playerList;		// All our players

	PlayerAdapter(List<PlayerInfo> playerList)
	{
		this.playerList = playerList;
	}

	@NonNull
	@Override
	public PlayerAdapter.PlayerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
	{
		// inflate the view
		LayoutInflater inflater = LayoutInflater.from(parent.getContext());
		View view = inflater.inflate(R.layout.player_list_layout, parent, false);

		// return the view holder for this view
		return new PlayerViewHolder(view);
	}

	@Override
	public void onBindViewHolder(@NonNull PlayerViewHolder viewHolder, int position)
	{
		// update the watchers' & listeners' positions every time we bind
		viewHolder.playerNameTextWatcher.setPosition(position);
		viewHolder.playerScoreOnClickListener.setPosition(position);
		viewHolder.playerEditLockOnClickListener.setPosition(position);

		// get the player at the specified position
		PlayerInfo player = playerList.get(position);

		// bind the data with the view holder views
		viewHolder.textPlayerName.setText(player.getPlayerName());
		viewHolder.buttonPlayerScore.setText(String.format(Locale.getDefault(), "%d", player.getPlayerScore()));

		// edit lock is enabled
		if(player.getEditLock())
		{
			viewHolder.imageButtonPlayerLock.setImageResource(R.drawable.ic_lock_black_24dp);
			setEditable(viewHolder, false);
		}
		// edit lock is disabled
		else
		{
			viewHolder.imageButtonPlayerLock.setImageResource(R.drawable.ic_lock_open_black_24dp);
			setEditable(viewHolder, true);
		}
	}

	/**
	 * Changes the enabled states of the items in the viewholder
	 */
	private void setEditable(@NonNull PlayerViewHolder viewHolder, boolean editable)
	{
		viewHolder.textPlayerName.setEnabled(editable);
		viewHolder.buttonPlayerScore.setEnabled(editable);
	}

	@Override
	public int getItemCount()
	{
		return playerList.size();
	}


	class PlayerViewHolder extends RecyclerView.ViewHolder
	{
		LinearLayout playerListLinearLayout;
		CardView	playerListCardView;
		EditText 	textPlayerName;
		Button		buttonPlayerScore;
		ImageButton imageButtonPlayerLock;

		PlayerNameTextWatcher playerNameTextWatcher;
		PlayerScoreOnClickListener playerScoreOnClickListener;
		PlayerEditLockOnClickListener playerEditLockOnClickListener;

		PlayerViewHolder(View view)
		{
			super(view);

			playerListLinearLayout = view.findViewById(R.id.player_list_linearlayout);
			playerListCardView = view.findViewById(R.id.player_list_cardview);
			textPlayerName = view.findViewById(R.id.player_name);
			buttonPlayerScore = view.findViewById(R.id.team_member_score_button);
			imageButtonPlayerLock = view.findViewById(R.id.player_lock);

			// create the PlayerName text listener and attach
			playerNameTextWatcher = new PlayerNameTextWatcher();
			textPlayerName.addTextChangedListener(playerNameTextWatcher);

			// create the PlayerScore OnClickListener and attach
			playerScoreOnClickListener = new PlayerScoreOnClickListener();
			buttonPlayerScore.setOnClickListener(playerScoreOnClickListener);

			// create the lock button OnClickListener and attach
			playerEditLockOnClickListener = new PlayerEditLockOnClickListener();
			imageButtonPlayerLock.setOnClickListener(playerEditLockOnClickListener);
		}
	}

	class PlayerNameTextWatcher implements TextWatcher
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
			PlayerInfo playerInfo = playerList.get(position);
			playerInfo.setPlayerName(s);
			playerList.set(position, playerInfo);
		}

		@Override
		public void afterTextChanged(Editable editable) { }
	}

	class PlayerScoreOnClickListener implements Button.OnClickListener, NumberPicker.OnValueChangeListener, TextWatcher
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
			PlayerInfo pi = playerList.get(position);

			final AppCompatDialog dialog = new AppCompatDialog(view.getContext(), R.style.Theme_App_AppCompatDialogStyle);
			dialog.setTitle("Set Score");
			dialog.setContentView(R.layout.picker_dialog);
			Button buttonSet = dialog.findViewById(R.id.button_set);
			Button buttonCancel = dialog.findViewById(R.id.button_cancel);
			final NumberPicker numberPicker = dialog.findViewById(R.id.number_picker);
			final EditText scoreEditText = dialog.findViewById(R.id.score_edittext);

			// update the score changed by textview
			scoreChangedByTextView = dialog.findViewById(R.id.score_change_textview);

			// update the textview to display the current score
			TextView currentScoreTextView = dialog.findViewById(R.id.current_score_textview);
			String s = String.format(Locale.getDefault(), "Current Score: %d", pi.getPlayerScore());
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

					if(numberPicker != null)
					{
						numberPicker.setMinValue(0);
						numberPicker.setMaxValue(arr.length - 1);
						numberPicker.setDisplayedValues(arr);
						numberPicker.setWrapSelectorWheel(false);
						numberPicker.setOnClickListener(this);
						numberPicker.setOnValueChangedListener(this);

						// set the displayed/selected value to the stored value
						int score = playerList.get(position).getPlayerScore();
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
								PlayerInfo playerInfo = playerList.get(position);
								if(numberPicker != null)
									playerInfo.setPlayerScore(Integer.parseInt(arr[numberPicker.getValue()]));
								playerList.set(position, playerInfo);
								dialog.dismiss();

								// should we sort?
								if (Settings.isAutoSortEnabled())
									Collections.sort(playerList);

								notifyDataSetChanged();
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
						scoreEditText.setText(String.valueOf(playerList.get(position).getPlayerScore()));
						scoreEditText.addTextChangedListener(this);
						scoreChangedByTextView.setText(String.format(Locale.getDefault(), "%s: %+d",
								ToastHelper.context.getResources().getString(R.string.score_adjusted_by), 0));
						scoreEditText.setHint(R.string.score_edittext_absolute);
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
								PlayerInfo playerInfo = playerList.get(position);

								int val = 0;
								// save #
								try
								{
									if(scoreEditText != null)
										val = Integer.parseInt(scoreEditText.getText().toString());
								} catch (Exception e)
								{
									val = playerInfo.getPlayerScore();
								}
								if (scoreEditText != null)
									playerInfo.setPlayerScore(val);
								else
									playerInfo.setPlayerScore(playerInfo.getPlayerScore());

								playerList.set(position, playerInfo);
								dialog.dismiss();

								// should we sort?
								if (Settings.isAutoSortEnabled())
									Collections.sort(playerList);

								notifyDataSetChanged();
							}
						});
					}
					break;

				// Offset mode
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
							ToastHelper.context.getString(R.string.new_score), playerList.get(position).getPlayerScore()));
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
								PlayerInfo playerInfo = playerList.get(position);

								// offset in case we are doing offset mode (oldscore +/- entered value)
								int offsetBase = playerList.get(position).getPlayerScore();
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
									playerInfo.setPlayerScore(offsetBase + val);
								else
									playerInfo.setPlayerScore(offsetBase);
								playerList.set(position, playerInfo);
								dialog.dismiss();

								// should we sort?
								if (Settings.isAutoSortEnabled())
									Collections.sort(playerList);

								notifyDataSetChanged();
							}
						});
					}
					break;
			}
			dialog.show();
		}

		/**
		 * when the value changes, update the adjustment text
		 */
		@Override
		public void onValueChange(NumberPicker picker, int oldVal, int newVal)
		{
			int pos = picker.getValue();
			int val = (pos * Settings.getStepValue()) + Settings.getMinValue();
			int delta = val - playerList.get(position).getPlayerScore();

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
					val = playerList.get(position).getPlayerScore();
				}

				int delta = val - playerList.get(position).getPlayerScore();
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

				int delta = val + playerList.get(position).getPlayerScore();
				scoreChangedByTextView.setText(String.format(Locale.getDefault(), "%s: %d",
						ToastHelper.context.getString(R.string.new_score), delta));
			}
		}

		@Override
		public void afterTextChanged(Editable s) {}
	}

	class PlayerEditLockOnClickListener implements ImageButton.OnClickListener
	{
		// this is used to know which list # we are so we can update the correct record
		private int position;
		public void setPosition(int position) { this.position = position; }

		@Override
		public void onClick(View view)
		{
			PlayerInfo playerInfo = playerList.get(position);

			if(playerInfo.getEditLock())
				playerInfo.setEditLock(false);
			else
				playerInfo.setEditLock(true);

			playerList.set(position, playerInfo);
			notifyDataSetChanged();
		}
	}
}
