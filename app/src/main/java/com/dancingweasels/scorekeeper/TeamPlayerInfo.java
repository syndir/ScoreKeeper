package com.dancingweasels.scorekeeper;

import android.support.annotation.NonNull;

public class TeamPlayerInfo implements Comparable<TeamPlayerInfo>
{
	private String teamPlayerName;
	private int teamPlayerScore;
	private boolean teamPlayerEditLock;

	TeamPlayerInfo() { this("", Settings.getStartingValue(), false); }
	TeamPlayerInfo(String teamPlayerName, int teamPlayerScore, boolean teamPlayerLock)
	{
		this.teamPlayerName = teamPlayerName;
		this.teamPlayerScore = teamPlayerScore;
		this.teamPlayerEditLock = teamPlayerLock;
	}

	public void setTeamPlayerName(String teamPlayerName) { this.teamPlayerName = teamPlayerName; }
	public void setTeamPlayerScore(int teamPlayerScore) { this.teamPlayerScore = teamPlayerScore; }
	public void setTeamPlayerEditLock(boolean teamPlayerEditLock) { this.teamPlayerEditLock = teamPlayerEditLock; }

	public String getTeamPlayerName() { return this.teamPlayerName; }
	public int getTeamPlayerScore() { return this.teamPlayerScore; }
	public boolean getTeamPlayerEditLock() { return this.teamPlayerEditLock; }


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
	public int compareTo(@NonNull TeamPlayerInfo o)
	{
		switch(Settings.getTeamMemberSortMethod())
		{
			case Settings.SortType.SCORE_HI_LO:
				if (this.getTeamPlayerScore() > o.getTeamPlayerScore())
					return -1;
				else if (this.getTeamPlayerScore() == o.getTeamPlayerScore())
					return 0;
				else
					return 1;

			case Settings.SortType.SCORE_LO_HI:
				if (this.getTeamPlayerScore() > o.getTeamPlayerScore())
					return 1;
				else if (this.getTeamPlayerScore() == o.getTeamPlayerScore())
					return 0;
				else
					return -1;

			case Settings.SortType.NAME_A_Z:
				if (this.getTeamPlayerName().compareToIgnoreCase(o.getTeamPlayerName()) > 0)
					return 1;
				else if (this.getTeamPlayerName().compareToIgnoreCase(o.getTeamPlayerName()) == 0)
					return 0;
				else
					return -1;

			case Settings.SortType.NAME_Z_A:
				if (this.getTeamPlayerName().compareToIgnoreCase(o.getTeamPlayerName()) > 0)
					return -1;
				else if (this.getTeamPlayerName().compareToIgnoreCase(o.getTeamPlayerName()) == 0)
					return 0;
				else
					return 1;
		}

		return 0; // by default, return 0 (no-sort)
	}
}
