package com.bp.criminalintent;
import java.util.Calendar;
import java.util.UUID;

public class Crime {
	// --- Fields/Member Variables
	private UUID mId;
	private String mTitle;
	private Calendar mDate;
	private boolean mSolved;
	
	// --- Methods/Member Functions
	public Crime() {
		mId = UUID.randomUUID();
		mTitle = new String();
		mDate = Calendar.getInstance();
		mSolved = false;
	}
	
	public UUID getmId() {
		return mId;
	}
	public void setmId(UUID mId) {
		this.mId = mId;
	}
	public String getmTitle() {
		return mTitle;
	}
	public void setmTitle(String mTitle) {
		this.mTitle = mTitle;
	}
	public Calendar getmDate() {
		return mDate;
	}
	public void setmDate(Calendar mDate) {
		this.mDate = mDate;
	}
	public boolean ismSolved() {
		return mSolved;
	}
	public void setmSolved(boolean mSolved) {
		this.mSolved = mSolved;
	}	
}
