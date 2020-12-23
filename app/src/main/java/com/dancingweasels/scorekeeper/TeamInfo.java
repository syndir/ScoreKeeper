package com.dancingweasels.scorekeeper;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class TeamInfo implements Comparable<TeamInfo>
{
	private String teamName;
	private List<TeamPlayerInfo> teamPlayerInfoList;
	private int teamScore;
	private int editLock; // 0 -> no lock, 1 -> team member lock, 2 -> full lock

	TeamInfo() { this("", null, Settings.getStartingValue()); }
	TeamInfo(String teamName, List<TeamPlayerInfo> teamPlayerInfoList, int teamScore)
	{
		this.teamName = teamName;
		this.teamScore = teamScore;
		if (teamPlayerInfoList == null)
			this.teamPlayerInfoList = new ArrayList<>();
		else
			this.teamPlayerInfoList = teamPlayerInfoList;
		this.editLock = 0;
	}

	public void setTeamName(String teamName) { this.teamName = teamName; }
	public void setTeamPlayerInfoList(List<TeamPlayerInfo> teamPlayerInfoList) { this.teamPlayerInfoList = teamPlayerInfoList; }
	public void setTeamScore(int teamScore) { this.teamScore = teamScore; }
	public void setEditLock(int editLock) { this.editLock = editLock; }

	public String getTeamName() { return this.teamName; }
	public List<TeamPlayerInfo> getTeamPlayerInfoList() { return this.teamPlayerInfoList; }
	public int getTeamScore() { return this.teamScore; }
	public int getEditLock() { return this.editLock; }

	/**
	 * Compares this object with the specified object for order.  Returns a
	 * negative integer, zero, or a positive integer as this object is less
	 * than, equal to, or greater than the specified object.
	 * <p>
	 * <p>The implementor must ensure <tt>sgn(x.compareTo(y)) ==
	 * -sgn(y.compareTo(x))</tt> for all <tt>x</tt> and <tt>y</tt>.  (This
	 * implies that <tt>x.compareTo(y)</tt> must throw an exception iff
	 * <tt>y.compareTo(x)</tt> throws an exception.)
	 * <p>
	 * <p>The implementor must also ensure that the relation is transitive:
	 * <tt>(x.compareTo(y)&gt;0 &amp;&amp; y.compareTo(z)&gt;0)</tt> implies
	 * <tt>x.compareTo(z)&gt;0</tt>.
	 * <p>
	 * <p>Finally, the implementor must ensure that <tt>x.compareTo(y)==0</tt>
	 * implies that <tt>sgn(x.compareTo(z)) == sgn(y.compareTo(z))</tt>, for
	 * all <tt>z</tt>.
	 * <p>
	 * <p>It is strongly recommended, but <i>not</i> strictly required that
	 * <tt>(x.compareTo(y)==0) == (x.equals(y))</tt>.  Generally speaking, any
	 * class that implements the <tt>Comparable</tt> interface and violates
	 * this condition should clearly indicate this fact.  The recommended
	 * language is "Note: this class has a natural ordering that is
	 * inconsistent with equals."
	 * <p>
	 * <p>In the foregoing description, the notation
	 * <tt>sgn(</tt><i>expression</i><tt>)</tt> designates the mathematical
	 * <i>signum</i> function, which is defined to return one of <tt>-1</tt>,
	 * <tt>0</tt>, or <tt>1</tt> according to whether the value of
	 * <i>expression</i> is negative, zero or positive.
	 *
	 * @param o the object to be compared.
	 * @return a negative integer, zero, or a positive integer as this object
	 * is less than, equal to, or greater than the specified object.
	 * @throws NullPointerException if the specified object is null
	 * @throws ClassCastException   if the specified object's type prevents it
	 *                              from being compared to this object.
	 */
	@Override
	public int compareTo(@NonNull TeamInfo o)
	{
		switch(Settings.getTeamSortMethod())
		{
			case Settings.SortType.SCORE_HI_LO:
				if (this.getTeamScore() > o.getTeamScore())
					return -1;
				else if (this.getTeamScore() == o.getTeamScore())
					return 0;
				else
					return 1;

			case Settings.SortType.SCORE_LO_HI:
				if (this.getTeamScore() > o.getTeamScore())
					return 1;
				else if (this.getTeamScore() == o.getTeamScore())
					return 0;
				else
					return -1;

			case Settings.SortType.NAME_A_Z:
				if (this.getTeamName().compareToIgnoreCase(o.getTeamName()) > 0)
					return 1;
				else if (this.getTeamName().compareToIgnoreCase(o.getTeamName()) == 0)
					return 0;
				else
					return -1;

			case Settings.SortType.NAME_Z_A:
				if (this.getTeamName().compareToIgnoreCase(o.getTeamName()) > 0)
					return -1;
				else if (this.getTeamName().compareToIgnoreCase(o.getTeamName()) == 0)
					return 0;
				else
					return 1;
		}

		return 0; // by default, return 0 (no-sort)
	}
}
