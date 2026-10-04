package dev.flondy.checkyourlove;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DbHelper extends SQLiteOpenHelper {
	
	boolean showDeveloperMessages;
	
	private Context context;
	
	private static String DATABASE_NAME = "CheckYourLove.db";
	private static int DATABASE_VERSION = 1;
	
	private static String COLUMN_NAME = "Names";
	private static String COLUMN_VALUE = "Value";
	
	private static String TABLE_NAME = "NameValuePairs";
	
	private static String CREATE_TABLE = "CREATE TABLE "+TABLE_NAME+" ("+COLUMN_NAME+" VARCHAR(255), "+COLUMN_VALUE+" INTEGER);";
	private static String DROP_TRABLE ="DROP TABLE IF EXISTS "+TABLE_NAME;
	
	private static String selName;//DER NAME FÜR DEN MAN DIE VALUE WILL (MUSS ERST ZUGEWIESEN WERDEN BEVOR MAN QUERY AUSFÜHRT)
	private static String GET_ROW_OF_NAME ="SELECT * FROM "+TABLE_NAME+" WHERE "+ COLUMN_NAME+"="+selName+"";
	
	public DbHelper(Context context, boolean showDeveloperMessages) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		this.context = context;
		this.showDeveloperMessages = showDeveloperMessages;
		// TODO Auto-generated constructor stub
		if(showDeveloperMessages)
		{
			Message.showToast(context,  "Konstruktor ausgeführt", "d");
		}
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
try {
	db.execSQL(CREATE_TABLE);
} catch (SQLException e) {
	// TODO Auto-generated catch block
	Message.showToast(context,  e.toString(), "d");
}
if(showDeveloperMessages)
{
	Message.showToast(context,  "onCreate ausgeführt", "d");
}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
try {
	db.execSQL(DROP_TRABLE);
	onCreate(db);
} catch (SQLException e) {
	// TODO Auto-generated catch block
	Message.showToast(context,  e.toString(), "d");
}
if(showDeveloperMessages)
{
	Message.showToast(context,  "onUpgrade ausgeführt", "d");
}
	}
	
	public boolean doesNameExistInDb(String name, SQLiteDatabase db)
	{
		
		//FÜGT DEN RICHTIGEN WERT IN DIE QUERY EIN
		selName = name;
		Cursor mCursor = null;
		try {
			//TODO hier 
			//HOLT ALLE VERFÜGBAREN DATEN AUS DB
			mCursor = db.rawQuery("SELECT * FROM " + TABLE_NAME, null);
			//WENN DB EH NOCH LEER
			if(!mCursor.moveToFirst()){
				Message.showToast(context,  "DB noch leer", "d");
				return false;
		}
			//WENN NICHT LEER
			else {
				int index = 0;
				//CHECK OB DIESER NAME IN DB IST BEREITS
				while (mCursor.moveToNext()) {
					Message.showToast(context, "Cursor Iterate Inhalt: " + mCursor.getString(index), "d");
					//CHECKE OB INHALT ÜBEREINSTIMMT
					if(mCursor.toString() == selName){
						return true;
					}
					//VIELLEICHT NOCH EIN ELEMENT 
					index++;
				}
				//NICHT IN DB
				//TODO was passiert ohne Rückgabe? auch false?
				return false;
			}
		}
		
			catch (Exception e) {
			// TODO Auto-generated catch block
			Message.showToast(context, e.toString(), "d");
		}
		if(mCursor != null)
		{
			if(showDeveloperMessages)
			{
				Message.showToast(context,  "Cursor ist nicht gleich null", "d");
			}
			return true;
		}
		else
		{
			if(showDeveloperMessages)
			{
				Message.showToast(context,  "Cursor ist gleich null", "d");
			}
			return false;
		}
		}
	
	//FUNKTIONIERT NUR WENN NAME SCHON IN DB VORHANDEN
	public int getValueOfName(String name, SQLiteDatabase db)
	{
		//SETZT DEN RICHTIGEN NAMEN IN DIE QUERY
		selName = name;
		Cursor cursor = null;
		try {
				cursor = db.rawQuery(GET_ROW_OF_NAME, null);
				int valueColIndex = cursor.getColumnIndex(COLUMN_VALUE);
				int value = cursor.getInt(valueColIndex);
				if(showDeveloperMessages)
				{
					Message.showToast(context, "Value of Name erfolgreich ausgelesen : "+value, "d");
				}
				return value;
			}
			catch (Exception e) {
			// TODO Auto-generated catch block
			Message.showToast(context, e.toString(), "d");
		}
		return 0;
	}
	
	public void createNewEntryForName(String name, int wert, SQLiteDatabase db)
	{
		try {
			ContentValues cv = new ContentValues();
			cv.put(COLUMN_NAME, name);
			cv.put(COLUMN_VALUE, wert);
			db.insert(TABLE_NAME, null, cv);
			if(showDeveloperMessages)
			{
				Message.showToast(context, "Neue Reihe für "+name+" mit dem Wert "+wert+" erfolgreich eingefügt !", "d");
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			Message.showToast(context, e.toString(), "d");
		}
	}
}
