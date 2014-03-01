package es.moodbox.txy.app.domain;

import java.util.Date;

/**
 * Defines a meetup between two users, that users cannot be null
 * the time starts to count when created
 * <p/>
 * Created by victor on 1/03/14.
 */
public class Meetup {

	private User userA;
	private User userB;
	private Date meetingStart;
	private Date meetingEnd;


	public Meetup(User userA, User userB) throws IllegalArgumentException {
		if (userA == null || userB == null) {
			throw new IllegalArgumentException();
		}

		this.userA = userA;
		this.userB = userB;

		meetingStart = new Date();
	}

	public User getUserA() {
		return userA;
	}

	public User getUserB() {
		return userB;
	}

	/**
	 * Return the time spent in milliseconds
	 *
	 * @return
	 */
	private long endMeeting() {

		meetingEnd = new Date();

		return meetingEnd.getTime() - meetingStart.getTime();
	}

	/**
	 * Returns the time spent since the meetup started, until now
	 *
	 * @return
	 */
	private long howLong() {
		return new Date().getTime() - meetingStart.getTime();
	}

	@Override
	public boolean equals(Object b) {
		if (b == null || !(b instanceof Meetup)) {
			return false;
		} else {
			return userB.getUserName().equals(((Meetup) b).getUserB().getUserName());
		}
	}

	@Override
	public int hashCode() {
		return userB.getUserName().hashCode();
	}

	@Override
	public String toString() {
		return userA.toString() + " -> " + userB.toString() + " for: " + howLong() / 1000 + " s";
	}
}
