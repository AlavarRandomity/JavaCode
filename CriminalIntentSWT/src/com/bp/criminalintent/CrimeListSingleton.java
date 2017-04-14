package com.bp.criminalintent;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.UUID;

public class CrimeListSingleton {

	// Constants
	private static final int ONEHUNDRED = 3;
	
	// Fields/Member Variables
	private static CrimeListSingleton instance = null;
	private ArrayList<Crime> mCrimeList = new ArrayList<Crime>();
	
	// Methods/Member Functions
	private CrimeListSingleton() {}
	
	public static CrimeListSingleton getInstance() {
		if (instance == null)
			instance = new CrimeListSingleton();
		
		return instance;
	}
	
	public void CreateOneHundred() {
		for (int i = 1; i <= ONEHUNDRED; ++i)
		{
			Crime tmp = new Crime();
			tmp.setmId(UUID.randomUUID());
			tmp.setmTitle("Crime #" + i);
			
			Calendar cal = Calendar.getInstance();
			tmp.setmDate(cal);
			tmp.setmSolved(i % 2 == 1);
			mCrimeList.add(tmp);
		}
	}
	
	public Crime getCrime(int idx) {
		return mCrimeList.get(idx);
	}
	
	private void setCrime(int idx, Crime crime) {
		if (idx < mCrimeList.size() && 
				mCrimeList.get(idx).getmId() == crime.getmId()) {
			// found an existing crime
			mCrimeList.get(idx).setmTitle(crime.getmTitle());
			mCrimeList.get(idx).setmDate(crime.getmDate());
			mCrimeList.get(idx).setmSolved(crime.ismSolved());
			return;
		}

		Crime tmp = new Crime();
		tmp.setmTitle(crime.getmTitle());
		tmp.setmDate(crime.getmDate());
		tmp.setmSolved(crime.ismSolved());
		
		mCrimeList.add(tmp);
	}
	
	public void addCrime(Crime crime) {
		setCrime(mCrimeList.size(), crime);
	}
	
	public int getSize() {
		return mCrimeList.size();
	}
}
