package com.iterators.temperaturedetector;
import android.content.*;
import android.database.*;
import android.database.sqlite.*;

public class ReadingsDatabase
{
	private static final String SERIAL_NUMBER="SNo";
	private static final String DATE="Date";
	private static final String TIME="Time";
	private static final String TEMPERATURE_READINGS="Temperature";
	
	private static final String DATABASE_NAME="TemperatureDetector";
	private static final String TABLE_NAME="TemperatureReadings";
	private static final int DATABASE_VERSION=1;
	
	private DbHelper ourHelper;
	private SQLiteDatabase ourDatabase;
	private Context ourContext;
	
	private class DbHelper extends SQLiteOpenHelper
	{
		public DbHelper(Context c){
			super(c,DATABASE_NAME,null,DATABASE_VERSION);
		}
		@Override
		public void onCreate(SQLiteDatabase p1){
			p1.execSQL("CREATE TABLE "+TABLE_NAME+" ( "+SERIAL_NUMBER+" INTEGER PRIMARY KEY AUTOINCREMENT, "+DATE+" text not null, "+TIME+" text not null, "+TEMPERATURE_READINGS+" text not null);");
		}

		@Override
		public void onUpgrade(SQLiteDatabase p1, int p2, int p3){
			p1.execSQL("DROP TABLE IF EXIST "+TABLE_NAME+";");
			onCreate(p1);
		}
	}
	
	public ReadingsDatabase(Context context){
		ourContext=context;
	}
	
	public ReadingsDatabase write() throws SQLiteException{
		ourHelper=new DbHelper(ourContext);
		ourDatabase=ourHelper.getWritableDatabase();
		return this;
	}
	
	public void close(){
		ourDatabase.close();
	}
	
	public void insertIntoTable(String date,String time,String temp){
		ContentValues cv=new ContentValues();
		cv.put(DATE,date);
		cv.put(TIME,time);
		cv.put(TEMPERATURE_READINGS,temp);
		ourDatabase.insert(TABLE_NAME,null,cv);
	}
	
	public String getData(){
		String send="";
		String[] columns={DATE,TIME,TEMPERATURE_READINGS};
		
		int sno=1;

		Cursor c=ourDatabase.query(TABLE_NAME,columns,null,null,null,null,null);
		
		int idDate=c.getColumnIndex(DATE);
		int idTime=c.getColumnIndex(TIME);
		int idTemp=c.getColumnIndex(TEMPERATURE_READINGS);
		
		for(c.moveToFirst();!c.isAfterLast();c.moveToNext()){
			send=send+Integer.toString(sno)+"  "+c.getString(idDate)+"  "+c.getString(idTime)+"  "+c.getString(idTemp)+"\n";
			sno++;
		}
		return send;
	}
	
	public String last100(){
		Cursor c=ourDatabase.rawQuery("Select * from "+TABLE_NAME,null);
		int rows=c.getCount();
		if(rows>=100){
			rows-=100;
		}
		else
			rows=0;
		
		String[] columns={DATE,TIME,TEMPERATURE_READINGS};
		
		c=ourDatabase.query(TABLE_NAME,columns,SERIAL_NUMBER+" >= "+rows,null,null,null,null);
		
		String send="";
		int sno=1;
		
		int idDate=c.getColumnIndex(DATE);
		int idTime=c.getColumnIndex(TIME);
		int idTemp=c.getColumnIndex(TEMPERATURE_READINGS);

		for(c.moveToFirst();!c.isAfterLast();c.moveToNext()){
			send=send+Integer.toString(sno)+"  "+c.getString(idDate)+"  "+c.getString(idTime)+"  "+c.getString(idTemp)+"\n";
			sno++;
		}
		return send;
	}
}
