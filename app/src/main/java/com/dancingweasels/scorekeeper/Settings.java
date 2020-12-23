package com.dancingweasels.scorekeeper;

import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceGroup;
import android.preference.PreferenceManager;

public class Settings extends PreferenceActivity
{
	final static String		SETTINGS_AUTOSORT_ENABLED = "com.dancingweasels.SETTINGS_AUTOSORT_ENABLED";
	final static String		SETTINGS_FFA_SORT_METHOD = "com.dancingweasels.SETTINGS_FFA_SORT_METHOD";
	final static String		SETTINGS_STARTING_VALUE = "com.dancingweasels.SETTINGS_STARTING_VALUE";
	final static String		SETTINGS_MIN_VALUE = "com.dancingweasels.SETTINGS_MIN_VALUE";
	final static String		SETTINGS_MAX_VALUE = "com.dancingweasels.SETTINGS_MAX_VALUE";
	final static String		SETTINGS_STEP_SIZE = "com.dancingweasels.SETTINGS_STEP_SIZE";
	final static String		SETTINGS_TEAM_SCORE_TRACK_METHOD = "com.dancingweasels.SETTINGS_TEAM_SCORE_TRACK_METHOD";
	final static String		SETTINGS_TEAM_SORT_METHOD = "com.dancingweasels.SETTINGS_TEAM_SORT_METHOD";
	final static String		SETTINGS_TEAM_MEMBER_SORT_METHOD = "com.dancingweasels.SETTINGS_TEAM_MEMBER_SORT_METHOD";
	final static String		SETTINGS_SCORE_EDIT_MODE = "com.dancingweasels.SETTINGS_SCORE_EDIT_MODE";

	public final class SortType
	{
		public final static String SCORE_HI_LO = "Score (High to low)";
		public final static String SCORE_LO_HI = "Score (Low to high)";
		public final static String NAME_A_Z = "Name (A-Z)";
		public final static String NAME_Z_A = "Name (Z-A)";
	}

	public final class ScoreEditMode
	{
		public final static String ABSOLUTE = "Absolute";
		public final static String OFFSET = "Offset";
		public final static String PICKER = "Picker";
	}

	private static String	ffaSortingMethod;
	private static boolean 	autoSortEnabled;
	private static String	scoreEditMode;
	private static int		startingValue;
	private static int		minValue;
	private static int		maxValue;
	private static int 		stepValue;
	private static String	teamTrackMethod;
	private static String	teamSortMethod;
	private static String	teamMemberSortMethod;

	public static String getFFASortingMethod() { return ffaSortingMethod; }
	public static boolean isAutoSortEnabled() { return autoSortEnabled; }
	public static String getScoreEditMode() { return scoreEditMode; }
	public static int getStartingValue() { return startingValue; }
	public static int getMinValue() { return minValue; }
	public static int getMaxValue() { return maxValue; }
	public static int getStepValue() { return stepValue; }
	public static String getTeamTrackMethod() { return teamTrackMethod; }
	public static String getTeamSortMethod() { return teamSortMethod; }
	public static String getTeamMemberSortMethod() { return teamMemberSortMethod; }
	public static boolean isTeamTrackIndividualScoresEnabled() { return teamTrackMethod.equals(ToastHelper.context.getString(R.string.settings_team_score_track_players)); }

	// Loads the shared preferences
	public void loadSettings()
	{
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ToastHelper.context);
		SharedPreferences.Editor editor = sp.edit();

		autoSortEnabled = sp.getBoolean(SETTINGS_AUTOSORT_ENABLED, false);
		ffaSortingMethod = sp.getString(SETTINGS_FFA_SORT_METHOD, SortType.SCORE_HI_LO);
		scoreEditMode = sp.getString(SETTINGS_SCORE_EDIT_MODE, ToastHelper.context.getString(R.string.settings_score_edit_mode_absolute));
		try
		{
			startingValue = Integer.parseInt(sp.getString(SETTINGS_STARTING_VALUE, "0"));
		} catch(NumberFormatException e) {
			startingValue = 0;
			editor.putString(SETTINGS_STARTING_VALUE, Integer.toString(startingValue));
			editor.apply();
		}
		try
		{
			minValue = Integer.parseInt(sp.getString(SETTINGS_MIN_VALUE, "-1000"));
		} catch (NumberFormatException e) {
			minValue = -1000;
			editor.putString(SETTINGS_MIN_VALUE, Integer.toString(minValue));
			editor.apply();
		}
		try
		{
			maxValue = Integer.parseInt(sp.getString(SETTINGS_MAX_VALUE, "1000"));
		} catch (NumberFormatException e) {
			maxValue = 1000;
			editor.putString(SETTINGS_MAX_VALUE, Integer.toString(maxValue));
			editor.apply();
		}
		try
		{
			stepValue = Integer.parseInt(sp.getString(SETTINGS_STEP_SIZE, "1"));
		} catch (NumberFormatException e) {
			stepValue = 1;
			editor.putString(SETTINGS_STEP_SIZE, Integer.toString(stepValue));
			editor.apply();
		}
		teamTrackMethod = sp.getString(SETTINGS_TEAM_SCORE_TRACK_METHOD, ToastHelper.context.getString(R.string.settings_team_score_track_team));
		teamSortMethod = sp.getString(SETTINGS_TEAM_SORT_METHOD, SortType.SCORE_HI_LO);
		teamMemberSortMethod = sp.getString(SETTINGS_TEAM_MEMBER_SORT_METHOD, SortType.NAME_A_Z);
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.preferences);
		PreferenceManager.setDefaultValues(Settings.this, R.xml.preferences, false);

		// this sets the summary data for our items
		initPreferences(getPreferenceScreen());

		// this will ignore screen rotation
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);

		// set up our listener
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ToastHelper.context);
		sp.registerOnSharedPreferenceChangeListener(listener);

		showPreferenceFields();
	}

	/**
	 * Enable or disable the appropriate preference fields
	 */
	public void showPreferenceFields()
	{
		Preference p;

		// depending on our score edit mode, show/hide appropriate fields
		switch(Settings.scoreEditMode)
		{
			case ScoreEditMode.ABSOLUTE:
			case ScoreEditMode.OFFSET:
				p = findPreference(SETTINGS_MIN_VALUE);
				p.setEnabled(false);
				p = findPreference(SETTINGS_MAX_VALUE);
				p.setEnabled(false);
				p = findPreference(SETTINGS_STEP_SIZE);
				p.setEnabled(false);
				break;

			case ScoreEditMode.PICKER:
				p = findPreference(SETTINGS_MIN_VALUE);
				p.setEnabled(true);
				p = findPreference(SETTINGS_MAX_VALUE);
				p.setEnabled(true);
				p = findPreference(SETTINGS_STEP_SIZE);
				p.setEnabled(true);
				break;
		}
	}

	// our listener for changes in preferences
	SharedPreferences.OnSharedPreferenceChangeListener listener = new SharedPreferences.OnSharedPreferenceChangeListener()
	{
		@Override
		public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
		{
			Preference p = findPreference(key);

			switch(key)
			{
				case SETTINGS_AUTOSORT_ENABLED:
					updatePrefSummary(p);
					autoSortEnabled = sharedPreferences.getBoolean(key, false);
					break;

				case SETTINGS_FFA_SORT_METHOD:
					updatePrefSummary(p);
					ffaSortingMethod =  sharedPreferences.getString(key, getString(R.string.settings_sort_method_score_hi_lo));
					break;

				case SETTINGS_SCORE_EDIT_MODE:
					updatePrefSummary(p);
					scoreEditMode = sharedPreferences.getString(key, getString(R.string.settings_score_edit_mode_absolute));
					showPreferenceFields();
					break;

				case SETTINGS_STARTING_VALUE:
					if (p instanceof  EditTextPreference)
					{
						EditTextPreference editTextPreference = (EditTextPreference) p;
						int oldVal = startingValue;
						try
						{
							startingValue = Integer.parseInt(editTextPreference.getText());
							p.setSummary(editTextPreference.getText());
						} catch (NumberFormatException e) // if we couldn't convert to an integer (ie: characters or blank), revert to old value
						{
							startingValue = oldVal;
							SharedPreferences.Editor editor = sharedPreferences.edit();
							editor.putString(SETTINGS_STARTING_VALUE, Integer.toString(startingValue));
							editor.apply();
						}
					}
					break;

				case SETTINGS_MIN_VALUE:
					if (p instanceof EditTextPreference)
					{
						EditTextPreference editTextPref = (EditTextPreference) p;
						int oldVal = minValue;
						try
						{
							minValue = Integer.parseInt(editTextPref.getText());
							p.setSummary(editTextPref.getText());
						} catch (NumberFormatException e) // if we couldn't convert to an integer (ie: characters or blank), revert to old value
						{
							minValue = oldVal;
							SharedPreferences.Editor editor = sharedPreferences.edit();
							editor.putString(SETTINGS_MIN_VALUE, Integer.toString(minValue));
							editor.apply();
						}
					}
					break;

				case SETTINGS_MAX_VALUE:
					if (p instanceof EditTextPreference)
					{
						EditTextPreference editTextPref = (EditTextPreference) p;
						int oldVal = maxValue;
						try
						{
							maxValue = Integer.parseInt(editTextPref.getText());
							p.setSummary(editTextPref.getText());
						} catch (NumberFormatException e) // if we couldn't convert to an integer (ie: characters or blank), revert to old value
						{
							maxValue = oldVal;
							SharedPreferences.Editor editor = sharedPreferences.edit();
							editor.putString(SETTINGS_MAX_VALUE, Integer.toString(maxValue));
							editor.apply();
						}
					}
					break;

				case SETTINGS_STEP_SIZE:
					if (p instanceof EditTextPreference)
					{
						EditTextPreference editTextPref = (EditTextPreference) p;
						int oldVal = stepValue;
						try
						{
							stepValue = Integer.parseInt(editTextPref.getText());
							p.setSummary(editTextPref.getText());
						} catch (NumberFormatException e) // if we couldn't convert to an integer (ie: characters or blank), revert to old value
						{
							stepValue = oldVal;
							SharedPreferences.Editor editor = sharedPreferences.edit();
							editor.putString(SETTINGS_STEP_SIZE, Integer.toString(stepValue));
							editor.apply();
						}
					}
					break;

				case SETTINGS_TEAM_SCORE_TRACK_METHOD:
					updatePrefSummary(p);
					teamTrackMethod = sharedPreferences.getString(key, getString(R.string.settings_team_score_track_team));
					break;

				case SETTINGS_TEAM_SORT_METHOD:
					updatePrefSummary(p);
					teamSortMethod = sharedPreferences.getString(key, getString(R.string.settings_sort_method_score_hi_lo));
					break;

				case SETTINGS_TEAM_MEMBER_SORT_METHOD:
					updatePrefSummary(p);
					teamMemberSortMethod = sharedPreferences.getString(key, getString(R.string.settings_sort_method_name_a_z));
					break;
			}
		}

	};

	@Override
	public void onResume()
	{
		super.onResume();
		PreferenceManager.getDefaultSharedPreferences(ToastHelper.context).registerOnSharedPreferenceChangeListener(listener);
	}

	@Override
	public void onPause()
	{
		super.onPause();
		PreferenceManager.getDefaultSharedPreferences(ToastHelper.context).unregisterOnSharedPreferenceChangeListener(listener);
	}

	/**
	 * Update the summary lines of all preferences
	 */
	private void initPreferences(Preference p)
	{
		if (p instanceof PreferenceGroup)
		{
			PreferenceGroup pGrp = (PreferenceGroup) p;
			for (int i = 0; i < pGrp.getPreferenceCount(); i++)
			{
				initPreferences(pGrp.getPreference(i));
			}
		}
		else
		{
			updatePrefSummary(p);
		}
	}

	/**
	 * Updates the summary lines of the preference p
	 */
	private void updatePrefSummary(Preference p)
	{
		if (p instanceof ListPreference)
		{
			ListPreference listPref = (ListPreference) p;
			p.setSummary(listPref.getEntry());
		}
		else if (p instanceof EditTextPreference)
		{
			EditTextPreference editTextPref = (EditTextPreference) p;
			p.setSummary(editTextPref.getText());
		}
	}
}
