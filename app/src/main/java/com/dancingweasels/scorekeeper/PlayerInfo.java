package com.dancingweasels.scorekeeper;

import android.support.annotation.NonNull;

public class PlayerInfo implements Comparable<PlayerInfo>
{
	private String 	playerName;
	private	int		playerScore;
	private boolean	editLock;

	PlayerInfo() { this("", Settings.getStartingValue()); } // default score goes to starting value set by preferences
	PlayerInfo(String playerName, int playerScore)
	{
		this.playerName = playerName;
		this.playerScore = playerScore;
		this.editLock = false;
	}

	public void setPlayerName(String playerName) { this.playerName = playerName; }
	public void setPlayerScore(int playerScore) { this.playerScore = playerScore; }
	public void setEditLock(boolean editLock) { this.editLock = editLock; }

	public String getPlayerName() { return this.playerName; }
	public int getPlayerScore() { return this.playerScore; }
	public boolean getEditLock() { return this.editLock; }

	@Override
	public int compareTo(@NonNull PlayerInfo playerInfo)
	{
		switch(Settings.getFFASortingMethod())
		{
			case Settings.SortType.SCORE_HI_LO:
				if (this.getPlayerScore() > playerInfo.getPlayerScore())
					return -1;
				else if (this.getPlayerScore() == playerInfo.getPlayerScore())
					return 0;
				else
					return 1;

			case Settings.SortType.SCORE_LO_HI:
				if (this.getPlayerScore() > playerInfo.getPlayerScore())
					return 1;
				else if (this.getPlayerScore() == playerInfo.getPlayerScore())
					return 0;
				else
					return -1;

			case Settings.SortType.NAME_A_Z:
				if (this.getPlayerName().compareToIgnoreCase(playerInfo.getPlayerName()) > 0)
					return 1;
				else if (this.getPlayerName().compareToIgnoreCase(playerInfo.getPlayerName()) == 0)
					return 0;
				else
					return -1;

			case Settings.SortType.NAME_Z_A:
				if (this.getPlayerName().compareToIgnoreCase(playerInfo.getPlayerName()) > 0)
					return -1;
				else if (this.getPlayerName().compareToIgnoreCase(playerInfo.getPlayerName()) == 0)
					return 0;
				else
					return 1;
		}

		return 0; // by default, return 0 (no-sort)
	}
}
