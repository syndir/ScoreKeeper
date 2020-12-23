package com.dancingweasels.scorekeeper;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class GameTracker
{
	private Calendar			dateCreated;
	private boolean 			inProgress; // is there a game in progress?
	private boolean				isTeamGame; // true if this is a team game
	private List<PlayerInfo> 	players;
	private List<TeamInfo>		teams;
	private String				gameName;

	GameTracker() { this(ToastHelper.context.getResources().getString(R.string.new_game), null, null, false, Calendar.getInstance()); }

	GameTracker(String gameName, List<PlayerInfo> players, List<TeamInfo> teams, boolean isTeamGame, Calendar dateCreated)
	{
		this.gameName = gameName;
		if(players == null)
			this.players = new ArrayList<>();
		else
			this.players = players;
		if(teams == null)
			this.teams = new ArrayList<>();
		else
			this.teams = teams;
		this.isTeamGame = isTeamGame;
		this.inProgress = true;
		this.dateCreated = dateCreated;
	}

	public void resetGameTracker()
	{
		players.clear();
		teams.clear();
		gameName = ToastHelper.context.getResources().getString(R.string.new_game);
	}
	public void clear() { resetGameTracker(); }

	public void setGameName(String gameName) { this.gameName = gameName; }
	public void setPlayers(List<PlayerInfo> players) { this.players = players; }
	public void setTeams(List<TeamInfo> teams) { this.teams = teams; }
	public void setIsTeamGame(boolean isTeamGame) { this.isTeamGame = isTeamGame; }
	public void setDateCreated(Calendar dateCreated) { this.dateCreated = dateCreated; }

	public String getGameName() { return this.gameName; }
	public List<PlayerInfo> getPlayers() { return this.players; }
	public List<TeamInfo> getTeams() { return this.teams; }
	public boolean getIsTeamGame() { return this.isTeamGame; }
	public boolean getInProgress() { return this.inProgress; }
	public Calendar getDateCreated() { return this.dateCreated;	}

	public void addPlayer(PlayerInfo playerToAdd) { players.add(playerToAdd); }
	public void removePlayer(PlayerInfo playerToRemove) { players.remove(playerToRemove); }

	public void addTeam(TeamInfo teamToAdd) { teams.add(teamToAdd); }
	public void removeTeam(TeamInfo teamToRemove) { teams.remove(teamToRemove); }
}
