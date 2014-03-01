package es.moodbox.txy.app.domain;

/**
 * Defines a user
 * <p/>
 * Created by victor on 1/03/14.
 */
public class User {

	private String userName;
	private String userMacAddress;

	public User(String userName, String userMacAddress) {
		this.userName = userName;
		this.userMacAddress = userMacAddress;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getUserMacAddress() {
		return userMacAddress;
	}

	public void setUserMacAddress(String userMacAddress) {
		this.userMacAddress = userMacAddress;
	}

	@Override
	public boolean equals(Object b) {
		if (b == null || !(b instanceof User)) {
			return false;
		} else {
			return userName.equals(((User) b).getUserName());
		}
	}

	@Override
	public String toString(){
		return userName == null ? "@nobody" : userName;
	}
}
