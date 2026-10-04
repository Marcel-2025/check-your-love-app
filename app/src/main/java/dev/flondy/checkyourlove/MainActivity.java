package dev.flondy.checkyourlove;

import android.app.Activity;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

public class MainActivity extends Activity {

	 /** The view to show the ad. */
    private AdView adView ;

    /* Your ad unit id. Replace with your actual ad unit id. */
    private static final String AD_UNIT_ID = "ca-app-pub-5414395840028991/4122761666" ;

	DbHelper dbHelper;
    SQLiteDatabase db;
    
    //GIBT AN OB NACHRICHTEN FÜR DEN ENTWICKLER GEZEIGT WERDEN SOLLEN
    boolean showDeveloperMessages = true;
 	
	//DIE STRINGS FÜR BEIDE NAMEN AUS DEN TEXTFELDERN
	String firstName, secondName;
	// DIE VALUES FÜR DIE NAMEN
	int wert1, wert2;
	
	//ENDQUOTE
	int quote = 0;
	
	EditText FirstNameET;
	EditText SecondNameET;
	TextView FeldquoteTV;
	Button GenerateBTN;
	Button MoreBTN;
	
	private static String DATABASE_NAME = "CheckYourLove.db";
	
	boolean loading = false;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
	{
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        //Führt meine Methode aus um das main layout zu initialisieren
       	SettingUpMainLay();
       	
       	//DROP/DELETE OLD DB 4 testing/coding (fresh db 4 every launch now)
       	this.deleteDatabase(DATABASE_NAME);
       	
       	//GETTING NEW DB
       	dbHelper = new DbHelper(this, showDeveloperMessages);
        db = dbHelper.getWritableDatabase();
	}
    
    public void SettingUpMainLay(){
    	
    	// Findet die jeweiligen Elemente per findViewById
    	FirstNameET = (EditText)findViewById(R.id.ename1);
    	SecondNameET = (EditText)findViewById(R.id.ename2);
    	FeldquoteTV = (TextView)findViewById(R.id.equote);
    	GenerateBTN = (Button)findViewById(R.id.generateButton);
    	MoreBTN = (Button)findViewById(R.id.moreButton);
    	
       	ScrollView adsscroll = (ScrollView) findViewById(R.id.adscrollview);
    	
        // Setzt bei die Texte in jeweiliger Sprache zu ...
       	FirstNameET.setHint(R.string.name1);
       	SecondNameET.setHint(R.string.name2);
       	FeldquoteTV.setText("");
       	GenerateBTN.setText(R.string.generate);
     		
//     		//Setze Zeichen & Zahlen Filter im ersten Textfeld
//       	FirstNameET.setFilters(new InputFilter[] {
//     				    new InputFilter() {
//     				        public CharSequence filter(CharSequence src, int start,
//     				                int end, Spanned dst, int dstart, int dend) {
//     				            if(src.equals("")){ // for backspace
//     				                return src;
//     				            }
//     				            if(src.toString().matches("[a-zA-ZäÄöÖüÜß ]+")){
//     				                return src;
//     				            }
//     				            return "";  
//     				        }
//     				    }
//     				});
//     		  
//     		//Setze Zeichen & Zahlen Filter im zweiten Textfeld
//       	SecondNameET.setFilters(new InputFilter[] {
//     				    new InputFilter() {
//     				        public CharSequence filter(CharSequence src, int start,
//     				                int end, Spanned dst, int dstart, int dend) {
//     				            if(src.equals("")){ // for backspace
//     				                return src;
//     				            }
//     				            if(src.toString().matches("[a-zA-ZäÄöÖüÜß ]+")){
//     				                return src;
//     				            }
//     				            return "";  
//     				        }
//     				    }
//     				});
     		  
  			getLayoutInflater().inflate(R.layout.adcontent, adsscroll);
  			
  		    // Create an ad.
  		    adView = new AdView(this );
  		    adView.setAdSize(AdSize.SMART_BANNER);
  		    adView.setAdUnitId( AD_UNIT_ID);

  		    // Add the AdView to the view hierarchy. The view will have no size
  		    // until the ad is loaded.
  		    LinearLayout layout = (LinearLayout) findViewById(R.id.adsviewing);
  		    layout.addView( adView);
  		    
  		    // Create an ad request. Check logcat output for the hashed device ID to
  		    // get test ads on a physical device.
  		    AdRequest adRequest = new AdRequest.Builder()
  		        .addTestDevice(AdRequest. DEVICE_ID_EMULATOR)
  		        .addTestDevice( "INSERT_YOUR_HASHED_DEVICE_ID_HERE" )
  		        .build();

  		    // Start loading the ad in the background.
  		    adView.loadAd(adRequest);
    		}
		
		  @Override
		  public void onResume() {
		    super.onResume();
		    if (adView != null) {
		      adView.resume();
		    }
		  }

		  @Override
		  public void onPause() {
		    if (adView != null) {
		      adView.pause();
		    }
		    super.onPause();
		  }

		  /** Called before the activity is destroyed. */
		  @Override
		  public void onDestroy() {
		    // Destroy the AdView.
		    if (adView != null) {
		      adView.destroy();
		    }
		    super.onDestroy();
		  }
	  
	public static int myRandom(int low, int high){
	return (int) (Math.random() * (high - low) + low);
	}
			
	public void generatequote (View view) 
	{
		//WENN NOCH LÄD TUE NICHTS
		if(loading){
			return;
		}
		Message.showToast(this, "----------------- HERZ GEDRÜCKT -----------------", "d");
		//KANN NICHT GEDRÜCKT WERDEN BIS FERTIG GELADEN
		loading = true;
		//VERSTECKE VORHERIGER WERT
		FeldquoteTV.setText("");
		//ZEIGE LABEL AUF BUTTON
       	GenerateBTN.setText(R.string.generate);
		//GET NAMES
		firstName = FirstNameET.getText().toString();
		secondName = SecondNameET.getText().toString();
		
		//WENN NUR DAS ERSTE TEXTFELD LEER IST
				if (firstName.length() == 0 && secondName.length() > 0) {
					Message.showToast(this, "Schreibe einen Namen in das erste Textfeld", "t");
					return;
				}
				//WENN NUR DAS ZWEITE TEXTFELD LEER IST
				if (firstName.length() > 0 && secondName.length() == 0) {
					Message.showToast(this, "Schreibe einen Namen in das zweite Textfeld", "t");
					return;
				}
				//WENN BEIDE TEXTFELDER LEER SIND
				if (firstName.length() == 0 && secondName.length() == 0){
					Message.showToast(this, "Beide Namensfelder sind leer bitte trage zwei Namen ein", "t");
					return;
			    }
				//WENN TEXTFELDER NICHT LEER SIND
				else
				{
					//SETZT BEIDE WERTE 
					SetValue(firstName, wert1);
					//KURZE LADEZEIT WEGEN ERSTEN TOAST
					//SONST WIRD DER NICHT GEZEIGT
					Handler handler = new Handler();
					handler.postDelayed(new Runnable() {
					    public void run() {
					     // Actions to do after seconds
							SetValue(secondName, wert2);
							//ZEIGE ERGEBNIS
							quote = wert1 + wert2;
							FeldquoteTV.setText("" + quote);
							//VERSTECKE LABEL AUF BUTTON
							GenerateBTN.setText("");
							//TODO ANIMATION EINFÜGEN STATT DEM TETX
							//ALSO TEXT GANZ AUSBLENDEN
							loading = false;
					    }
					}, 3000);
				}
		
		//SETZTE WERT DER QUOTE (FALSCHE WERT WEGEN TIMER)
//		quote = wert1 + wert2;
		//WENN WERT KLEINER ALS 0 ODER GRÖßER ALS 100
		if (quote < 0 || quote > 100) 
			{
			Message.showToast(this, "Ungültiger Wert", "t");
			return;
			}
		//GIBT WERT IM TEXTLABEL AUS
//		FeldquoteTV.setText(""+quote);
		//WENN STRING NICHT LEER IST DANN VERSTECKE LABEL AUF BUTTON
		if(FeldquoteTV.length() > 0)
		{
				GenerateBTN.setText("");
		}

	}
	
	public void SetValue(String name, int wert)
	{
			//WENN NAME IN DATENBANK VORHANDEN
			if(dbHelper.doesNameExistInDb(name, db))
				// TODO FEHLER HIER FRÄGT FALSCHEN INDEX AN
			{
				Message.showToast(this, "Name in Datenbank hole Wert", "d");
				//SETZT WERT... ELSE IF UM HERAUSZUFINDEN WELCHER NAME ÜBERGEBEN WURDE
				if(name == firstName) {
					wert1 = dbHelper.getValueOfName(name, db);
				}
				else if(name == secondName) {

					wert2 = dbHelper.getValueOfName(name, db);
				}
				else {

					Message.showToast(this, "What sääääh Fag", "t");
				}
			}
			//WENN NAME NICHT EXISTIERT DANN ERSTELLE NEUEN ZUFALLSWERT UND ERSTELLE NEUE REIHE IN DATENBANK
			else
			{
				Message.showToast(this, "Name NICHT in Datenbank setze Wert", "d");
				//ERZEUGT NEUE ZUFALLSZAHL ZWISCHEN 0 und 50 UND WEIST SIE ZU
				wert = myRandom(0, 50);
				//WEISE WERT ZU... ELSE IF UM HERAUSZUFINDEN WELCHER NAME ÜBERGEBEN WURDE
				if(name == firstName) {
					wert1 = wert;
				}
				else if(name == secondName) {

					wert2 = wert;
				}
				else {

					Message.showToast(this, "What sääääh Fag", "t");
				}
				//ERSTELLT NEUE REIHE FÜR DEN NAMEN UND ÜBERGIBT DIE WERTE
				dbHelper.createNewEntryForName(name, wert, db);
			}
	}
		
	
//  boolean check1;
//	boolean check2;
//	boolean check3;
//	boolean check4;
//	boolean check5;
//	boolean check6;
//	boolean check7;
//	boolean check8;
	
//	// hate eine Indexspannweite von 0 bis arrayindex1 -1
//	char [] zeichenkette1 = new char[arrayindex1];
//	
//	// hat eine Indexspannweite von 0 bis arryindex2 - 1
//	char [] zeichenkette2 = new char[arrayindex2];
//	
//	// 29 Buchstaben also insgesamt 59 und das array hat eine Spannweite von 0 bis 59 -1 ALSO 58
//	char [] buchstabenarray = {'A','a', 'B','b', 'C','c', 'D','d', 'E','e', 'F','f', 'G','g', 'H','h', 'I','i', 'J','j', 'K','k', 'L','l', 'M','m', 'N','n', 'O','o', 'P','p', 'Q','q', 'R','r', 'S','s', 'T','t', 'U','u', 'V','v', 'J','j', 'X','x', 'Y','y', 'Z','z',		'Ä','ä',		'Ö','ö',		'Ü','ü',		'ß'};
//	
//	// 10 Zahlen also hat das Array eine Indexspannweite von 0 bis 10 -1 ALSO 9
//	int [] zahlenarry = {1,2,3,4,5,6,7,8,9,10};
	
//		//Übergibt den Arrays die Werte für die Zeichenanzahl
//		arrayindex1 = erstername.length();
//		arrayindex2 = zweitername.length();
//		
//		//Übergibt den String in ein Char Array
//		zeichenkette1 = erstername.toCharArray();
//		zeichenkette2 = zweitername.toCharArray();
		
		
//		//FOR SCHLAUFE ZEICHENKETTE 1
//		
//		for(int i = 0; i < arrayindex1; i++){
//		
////		//Wenn Zahl eingegeben wurde dann ...
////		if(zeichenkette1 [i] == 0 || zeichenkette1 [i] < 0 || zeichenkette1 [i] > 0) {
////		Toast.makeText(MainActivity.this, "Zahlen sind hier fehl am Platz :D", Toast.LENGTH_SHORT).show();
////		check1 = false;
////		}
//		
//		// Wenn Zeichen im Array an der Stelle mit dem Index i entweder 'a' oder 'A' ist dann setze 
//		if(zeichenkette1 [i] == buchstabenarray[0] || zeichenkette1 [i] == buchstabenarray[1] ) {
//			wert1 += zahlenarry[0]; }
//		
//		// Wenn Zeichen im Array an der Stelle mit dem Index i entweder 'b' oder 'B' ist dann setze 
//		if(zeichenkette1 [i] == buchstabenarray[2] || zeichenkette1 [i] == buchstabenarray[3] ) {
//			wert1 += zahlenarry[1]; }
//		
//		// Wenn Zeichen im Array an der Stelle mit dem Index i entweder 'c' oder 'C' ist dann setze 
//		if(zeichenkette1 [i] == buchstabenarray[4] || zeichenkette1 [i] == buchstabenarray[5] ) {
//			wert1 += zahlenarry[2]; }
//				
//		// Wenn Zeichen im Array an der Stelle mit dem Index i entweder 'd' oder 'D' ist dann setze 
//		if(zeichenkette1 [i] == buchstabenarray[6] || zeichenkette1 [i] == buchstabenarray[7] ) {
//			wert1 += zahlenarry[3]; }
//				
//		// Wenn Zeichen im Array an der Stelle mit dem Index i entweder 'e' oder 'E' ist dann setze 
//		if(zeichenkette1 [i] == buchstabenarray[8] || zeichenkette1 [i] == buchstabenarray[9] ) {
//			wert1 += zahlenarry[4]; }
//				
//		// Wenn Zeichen im Array an der Stelle mit dem Index i entweder 'f' oder 'F' ist dann setze 
//		if(zeichenkette1 [i] == buchstabenarray[10] || zeichenkette1 [i] == buchstabenarray[11] ) {
//			wert1 += zahlenarry[5]; }
//				
//		// Wenn Zeichen im Array an der Stelle mit dem Index i entweder 'g' oder 'G' ist dann setze 
//		if(zeichenkette1 [i] == buchstabenarray[12] || zeichenkette1 [i] == buchstabenarray[13] ) {
//			wert1 += zahlenarry[6]; }
//				
//		// Wenn Zeichen im Array an der Stelle mit dem Index i entweder 'h' oder 'H' ist dann setze 
//		if(zeichenkette1 [i] == buchstabenarray[14] || zeichenkette1 [i] == buchstabenarray[15] ) {
//			wert1 += zahlenarry[7]; }
//				
//		// Wenn Zeichen im Array an der Stelle mit dem Index i entweder 'i' oder 'I' ist dann setze 
//		if(zeichenkette1 [i] == buchstabenarray[16] || zeichenkette1 [i] == buchstabenarray[17] ) {
//			wert1 += zahlenarry[8]; }
//				
//		// Wenn Zeichen im Array an der Stelle mit dem Index i entweder 'j' oder 'J' ist dann setze 
//		if(zeichenkette1 [i] == buchstabenarray[18] || zeichenkette1 [i] == buchstabenarray[19] ) {
//			wert1 += zahlenarry[9]; }
//				
//		// Wenn Zeichen im Array an der Stelle mit dem Index i entweder 'k' oder 'K' ist dann setze 
//		if(zeichenkette1 [i] == buchstabenarray[20] || zeichenkette1 [i] == buchstabenarray[21] ) {
//			wert1 += zahlenarry[1]; }
//				
//		// Wenn Zeichen im Array an der Stelle mit dem Index i entweder 'l' oder 'L' ist dann setze 
//		if(zeichenkette1 [i] == buchstabenarray[22] || zeichenkette1 [i] == buchstabenarray[23] ) {
//			wert1 += zahlenarry[2]; }
//				
//		// Wenn Zeichen im Array an der Stelle mit dem Index i entweder 'm' oder 'M' ist dann setze 
//		if(zeichenkette1 [i] == buchstabenarray[24] || zeichenkette1 [i] == buchstabenarray[25] ) {
//			wert1 += zahlenarry[3]; }
//				
//		// Wenn Zeichen im Array an der Stelle mit dem Index i entweder 'n' oder 'N' ist dann setze 
//		if(zeichenkette1 [i] == buchstabenarray[26] || zeichenkette1 [i] == buchstabenarray[27] ) {
//			wert1 += zahlenarry[4]; }
//				
//		// Wenn Zeichen im Array an der Stelle mit dem Index i entweder 'o' oder 'O' ist dann setze 
//		if(zeichenkette1 [i] == buchstabenarray[28] || zeichenkette1 [i] == buchstabenarray[29] ) {
//			wert1 += zahlenarry[5]; }
//				
//		// Wenn Zeichen im Array an der Stelle mit dem Index i entweder 'p' oder 'P' ist dann setze 
//		if(zeichenkette1 [i] == buchstabenarray[30] || zeichenkette1 [i] == buchstabenarray[31] ) {
//			wert1 += zahlenarry[6]; }
//				
//		// Wenn Zeichen im Array an der Stelle mit dem Index i entweder 'q' oder 'Q' ist dann setze 
//		if(zeichenkette1 [i] == buchstabenarray[32] || zeichenkette1 [i] == buchstabenarray[33] ) {
//			wert1 += zahlenarry[7]; }
//				
//		// Wenn Zeichen im Array an der Stelle mit dem Index i entweder 'r' oder 'R' ist dann setze 
//		if(zeichenkette1 [i] == buchstabenarray[34] || zeichenkette1 [i] == buchstabenarray[35] ) {
//			wert1 += zahlenarry[8]; }
//				
//		// Wenn Zeichen im Array an der Stelle mit dem Index i entweder 's' oder 'S' ist dann setze 
//		if(zeichenkette1 [i] == buchstabenarray[36] || zeichenkette1 [i] == buchstabenarray[37] ) {
//			wert1 += zahlenarry[9]; }
//				
//		// Wenn Zeichen im Array an der Stelle mit dem Index i entweder 't' oder 'T' ist dann setze 
//		if(zeichenkette1 [i] == buchstabenarray[38] || zeichenkette1 [i] == buchstabenarray[39] ) {
//			wert1 += zahlenarry[1]; }
//				
//		// Wenn Zeichen im Array an der Stelle mit dem Index i entweder 'u' oder 'U' ist dann setze 
//		if(zeichenkette1 [i] == buchstabenarray[40] || zeichenkette1 [i] == buchstabenarray[41] ) {
//			wert1 += zahlenarry[2]; }
//				
//		// Wenn Zeichen im Array an der Stelle mit dem Index i entweder 'v' oder 'V' ist dann setze 
//		if(zeichenkette1 [i] == buchstabenarray[42] || zeichenkette1 [i] == buchstabenarray[43] ) {
//			wert1 += zahlenarry[3]; }
//				
//		// Wenn Zeichen im Array an der Stelle mit dem Index i entweder 'j' oder 'J' ist dann setze 
//		if(zeichenkette1 [i] == buchstabenarray[44] || zeichenkette1 [i] == buchstabenarray[45] ) {
//			wert1 += zahlenarry[4]; }
//				
//		// Wenn Zeichen im Array an der Stelle mit dem Index i entweder 'x' oder 'X' ist dann setze 
//		if(zeichenkette1 [i] == buchstabenarray[46] || zeichenkette1 [i] == buchstabenarray[47] ) {
//			wert1 += zahlenarry[5]; }
//				
//		// Wenn Zeichen im Array an der Stelle mit dem Index i entweder 'y' oder 'Y' ist dann setze 
//		if(zeichenkette1 [i] == buchstabenarray[48] || zeichenkette1 [i] == buchstabenarray[49] ) {
//			wert1 += zahlenarry[6]; }
//				
//		// Wenn Zeichen im Array an der Stelle mit dem Index i entweder 'z' oder 'Z' ist dann setze 
//		if(zeichenkette1 [i] == buchstabenarray[50] || zeichenkette1 [i] == buchstabenarray[51] ) {
//			wert1 += zahlenarry[7]; }
//				
//		// Wenn Zeichen im Array an der Stelle mit dem Index i entweder 'ä' oder 'Ä' ist dann setze 
//		if(zeichenkette1 [i] == buchstabenarray[52] || zeichenkette1 [i] == buchstabenarray[53] ) {
//			wert1 += zahlenarry[8]; }
//				
//		// Wenn Zeichen im Array an der Stelle mit dem Index i entweder 'ö' oder 'Ö' ist dann setze 
//		if(zeichenkette1 [i] == buchstabenarray[54] || zeichenkette1 [i] == buchstabenarray[55] ) {
//			wert1 += zahlenarry[9]; }
//				
//		// Wenn Zeichen im Array an der Stelle mit dem Index i entweder 'ü' oder 'ü' ist dann setze 
//		if(zeichenkette1 [i] == buchstabenarray[56] || zeichenkette1 [i] == buchstabenarray[57] ) {
//			wert1 += zahlenarry[1]; }
//				
//		// Wenn Zeichen im Array an der Stelle mit dem Index i entweder 'ß' ist dann setze 
//		if(zeichenkette1 [i] == buchstabenarray[58] ) {
//			wert1 += zahlenarry[2]; }
//		
//		
////		else{Toast.makeText(MainActivity.this, "Zeichen ist kein Buchstabe des Alphabets", Toast.LENGTH_SHORT).show();
////		check2 = false;
////		}
//		
//		
//		}
//		//FOR SCHLEIFE ENDE
//		
//		
//		
//		
//		//FOR SCHLAUFE ZEICHENKETTE 2
//		
//		for(int i1 = 0; i1 < arrayindex1; i1++){
//			//WEISE TEMPORÄRER VARIABLE DAS CHAR ELEMENT ZU
//			char tempChar = zeichenkette2[i1];
//			//WENN CHAR IS DIGIT ALSO ZAHL IST DANN ...
////			if(tempChar.isDigit) {
////			check3 = false;
////			Toast.makeText(MainActivity.this, "Zahlen sind hier fehl am Platz :D", Toast.LENGTH_SHORT).show();
////			}
//			
//			// Wenn Zeichen im Array an der Stelle mit dem Index i entweder 'a' oder 'A' ist dann setze 
//			if(zeichenkette2 [i1] == buchstabenarray[0] || zeichenkette2 [i1] == buchstabenarray[1] ) {
//				wert2 += zahlenarry[0]; }
//			
//			// Wenn Zeichen im Array an der Stelle mit dem Index i entweder 'b' oder 'B' ist dann setze 
//			if(zeichenkette2 [i1] == buchstabenarray[2] || zeichenkette2 [i1] == buchstabenarray[3] ) {
//				wert2 += zahlenarry[1]; }
//			
//			// Wenn Zeichen im Array an der Stelle mit dem Index i entweder 'c' oder 'C' ist dann setze 
//			if(zeichenkette2 [i1] == buchstabenarray[4] || zeichenkette2 [i1] == buchstabenarray[5] ) {
//				wert2 += zahlenarry[2]; }
//					
//			// Wenn Zeichen im Array an der Stelle mit dem Index i entweder 'd' oder 'D' ist dann setze 
//			if(zeichenkette2 [i1] == buchstabenarray[6] || zeichenkette2 [i1] == buchstabenarray[7] ) {
//				wert2 += zahlenarry[3]; }
//					
//			// Wenn Zeichen im Array an der Stelle mit dem Index i entweder 'e' oder 'E' ist dann setze 
//			if(zeichenkette2 [i1] == buchstabenarray[8] || zeichenkette2 [i1] == buchstabenarray[9] ) {
//				wert2 += zahlenarry[4]; }
//					
//			// Wenn Zeichen im Array an der Stelle mit dem Index i entweder 'f' oder 'F' ist dann setze 
//			if(zeichenkette2 [i1] == buchstabenarray[10] || zeichenkette2 [i1] == buchstabenarray[11] ) {
//				wert2 += zahlenarry[5]; }
//					
//			// Wenn Zeichen im Array an der Stelle mit dem Index i entweder 'g' oder 'G' ist dann setze 
//			if(zeichenkette2 [i1] == buchstabenarray[12] || zeichenkette2 [i1] == buchstabenarray[13] ) {
//				wert2 += zahlenarry[6]; }
//					
//			// Wenn Zeichen im Array an der Stelle mit dem Index i entweder 'h' oder 'H' ist dann setze 
//			if(zeichenkette2 [i1] == buchstabenarray[14] || zeichenkette2 [i1] == buchstabenarray[15] ) {
//				wert2 += zahlenarry[7]; }
//					
//			// Wenn Zeichen im Array an der Stelle mit dem Index i entweder 'i' oder 'I' ist dann setze 
//			if(zeichenkette2 [i1] == buchstabenarray[16] || zeichenkette2 [i1] == buchstabenarray[17] ) {
//				wert2 += zahlenarry[8]; }
//					
//			// Wenn Zeichen im Array an der Stelle mit dem Index i entweder 'j' oder 'J' ist dann setze 
//			if(zeichenkette2 [i1] == buchstabenarray[18] || zeichenkette2 [i1] == buchstabenarray[19] ) {
//				wert2 += zahlenarry[9]; }
//					
//			// Wenn Zeichen im Array an der Stelle mit dem Index i entweder 'k' oder 'K' ist dann setze 
//			if(zeichenkette2 [i1] == buchstabenarray[20] || zeichenkette2 [i1] == buchstabenarray[21] ) {
//				wert2 += zahlenarry[1]; }
//					
//			// Wenn Zeichen im Array an der Stelle mit dem Index i entweder 'l' oder 'L' ist dann setze 
//			if(zeichenkette2 [i1] == buchstabenarray[22] || zeichenkette2 [i1] == buchstabenarray[23] ) {
//				wert2 += zahlenarry[2]; }
//					
//			// Wenn Zeichen im Array an der Stelle mit dem Index i entweder 'm' oder 'M' ist dann setze 
//			if(zeichenkette2 [i1] == buchstabenarray[24] || zeichenkette2 [i1] == buchstabenarray[25] ) {
//				wert2 += zahlenarry[3]; }
//					
//			// Wenn Zeichen im Array an der Stelle mit dem Index i entweder 'n' oder 'N' ist dann setze 
//			if(zeichenkette2 [i1] == buchstabenarray[26] || zeichenkette2 [i1] == buchstabenarray[27] ) {
//				wert2 += zahlenarry[4]; }
//					
//			// Wenn Zeichen im Array an der Stelle mit dem Index i entweder 'o' oder 'O' ist dann setze 
//			if(zeichenkette2 [i1] == buchstabenarray[28] || zeichenkette2 [i1] == buchstabenarray[29] ) {
//				wert2 += zahlenarry[5]; }
//					
//			// Wenn Zeichen im Array an der Stelle mit dem Index i entweder 'p' oder 'P' ist dann setze 
//			if(zeichenkette2 [i1] == buchstabenarray[30] || zeichenkette2 [i1] == buchstabenarray[31] ) {
//				wert2 += zahlenarry[6]; }
//					
//			// Wenn Zeichen im Array an der Stelle mit dem Index i entweder 'q' oder 'Q' ist dann setze 
//			if(zeichenkette2 [i1] == buchstabenarray[32] || zeichenkette2 [i1] == buchstabenarray[33] ) {
//				wert2 += zahlenarry[7]; }
//					
//			// Wenn Zeichen im Array an der Stelle mit dem Index i entweder 'r' oder 'R' ist dann setze 
//			if(zeichenkette2 [i1] == buchstabenarray[34] || zeichenkette2 [i1] == buchstabenarray[35] ) {
//				wert2 += zahlenarry[8]; }
//					
//			// Wenn Zeichen im Array an der Stelle mit dem Index i entweder 's' oder 'S' ist dann setze 
//			if(zeichenkette2 [i1] == buchstabenarray[36] || zeichenkette2 [i1] == buchstabenarray[37] ) {
//				wert2 += zahlenarry[9]; }
//					
//			// Wenn Zeichen im Array an der Stelle mit dem Index i entweder 't' oder 'T' ist dann setze 
//			if(zeichenkette2 [i1] == buchstabenarray[38] || zeichenkette2 [i1] == buchstabenarray[39] ) {
//				wert2 += zahlenarry[1]; }
//					
//			// Wenn Zeichen im Array an der Stelle mit dem Index i entweder 'u' oder 'U' ist dann setze 
//			if(zeichenkette2 [i1] == buchstabenarray[40] || zeichenkette2 [i1] == buchstabenarray[41] ) {
//				wert2 += zahlenarry[2]; }
//					
//			// Wenn Zeichen im Array an der Stelle mit dem Index i entweder 'v' oder 'V' ist dann setze 
//			if(zeichenkette2 [i1] == buchstabenarray[42] || zeichenkette2 [i1] == buchstabenarray[43] ) {
//				wert2 += zahlenarry[3]; }
//					
//			// Wenn Zeichen im Array an der Stelle mit dem Index i entweder 'j' oder 'J' ist dann setze 
//			if(zeichenkette2 [i1] == buchstabenarray[44] || zeichenkette2 [i1] == buchstabenarray[45] ) {
//				wert2 += zahlenarry[4]; }
//					
//			// Wenn Zeichen im Array an der Stelle mit dem Index i entweder 'x' oder 'X' ist dann setze 
//			if(zeichenkette2 [i1] == buchstabenarray[46] || zeichenkette2 [i1] == buchstabenarray[47] ) {
//				wert2 += zahlenarry[5]; }
//					
//			// Wenn Zeichen im Array an der Stelle mit dem Index i entweder 'y' oder 'Y' ist dann setze 
//			if(zeichenkette2 [i1] == buchstabenarray[48] || zeichenkette2 [i1] == buchstabenarray[49] ) {
//				wert2 += zahlenarry[6]; }
//					
//			// Wenn Zeichen im Array an der Stelle mit dem Index i entweder 'z' oder 'Z' ist dann setze 
//			if(zeichenkette2 [i1] == buchstabenarray[50] || zeichenkette2 [i1] == buchstabenarray[51] ) {
//				wert2 += zahlenarry[7]; }
//					
//			// Wenn Zeichen im Array an der Stelle mit dem Index i entweder 'ä' oder 'Ä' ist dann setze 
//			if(zeichenkette2 [i1] == buchstabenarray[52] || zeichenkette2 [i1] == buchstabenarray[53] ) {
//				wert2 += zahlenarry[8]; }
//					
//			// Wenn Zeichen im Array an der Stelle mit dem Index i entweder 'ö' oder 'Ö' ist dann setze 
//			if(zeichenkette2 [i1] == buchstabenarray[54] || zeichenkette2 [i1] == buchstabenarray[55] ) {
//				wert2 += zahlenarry[9]; }
//					
//			// Wenn Zeichen im Array an der Stelle mit dem Index i entweder 'ü' oder 'ü' ist dann setze 
//			if(zeichenkette2 [i1] == buchstabenarray[56] || zeichenkette2 [i1] == buchstabenarray[57] ) {
//				wert2 += zahlenarry[1]; }
//					
//			// Wenn Zeichen im Array an der Stelle mit dem Index i entweder 'ß' ist dann setze 
//			if(zeichenkette2 [i1] == buchstabenarray[58] ) {
//				wert2 += zahlenarry[2]; }
//			
//			
////			else{
////			Toast.makeText(MainActivity.this, "Zeichen ist kein Buchstabe des Alphabets", Toast.LENGTH_SHORT).show();
////			check4 = false;
////			}
//			
//			
//			}
//			//FOR SCHLEIFE ENDE
//}
	
	// Flondy - More und Lizenzscreen
	 public void dev (View view) {
		   setContentView(R.layout.flondyscreen);
		   ScrollView devscroll = (ScrollView) findViewById(R.id.flondyscrollview );
		   getLayoutInflater().inflate(R.layout.flondycontent, devscroll);    
		   
		// DEKLARIERT UND FINDET DEN ZURÜCKBUTTON AUF DEM 'FLONDYSCREEN.XML'
		   Button flondyScreenBackButton = (Button)findViewById(R.id.flondyScreenBackButton);
		   
		   
		// DEKLARIERT UND FINDET ALLE TEXTVIEWS AUF DEM 'FLONDYCONTENT.XML'
		   TextView Hgreen1 = (TextView)findViewById(R.id.Hgreen1);
		   TextView Abschnitt1 = (TextView)findViewById(R.id.Abschnitt1);
		   TextView Hgreen2 = (TextView)findViewById(R.id.Hgreen2);
		   TextView Abschnitt2 = (TextView)findViewById(R.id.Abschnitt2);
		   TextView Abschnitt3 = (TextView)findViewById(R.id.Abschnitt3);
		   TextView Abschnitt4 = (TextView)findViewById(R.id.Abschnitt4);
		   TextView Hgreen3 = (TextView)findViewById(R.id.Hgreen3);
		   TextView Abschnitt5 = (TextView)findViewById(R.id.Abschnitt5);
		   TextView Abschnitt6 = (TextView)findViewById(R.id.Abschnitt6);
		   TextView Abschnitt7 = (TextView)findViewById(R.id.Abschnitt7);
		   TextView Playstoresignature = (TextView)findViewById(R.id.Playstoresignature);
		   TextView Appstoresignature = (TextView)findViewById(R.id.Appstoresignature);
		   TextView Flondysignature = (TextView)findViewById(R.id.Flondystoresignature);
		   
		   
		   
		   //SETZT DIE TEXTE ...
		   flondyScreenBackButton.setText(R.string.back);
		   
		   Hgreen1.setText(R.string.hgreen1text);
		   Abschnitt1.setText(R.string.abschnitt1text);
		   Hgreen2.setText(R.string.hgreen2text);
		   Abschnitt2.setText(R.string.abschnitt2text);
		   Abschnitt3.setText(R.string.abschnitt3text);
		   Abschnitt4.setText(R.string.abschnitt4text);
		   Hgreen3.setText(R.string.hgreen3text);
		   Abschnitt5.setText(R.string.abschnitt5text);
		   Abschnitt6.setText(R.string.abschnitt6text);
		   Abschnitt7.setText(R.string.abschnitt7text);
		   Playstoresignature.setText(R.string.playstoresignaturetext);
		   Appstoresignature.setText(R.string.appstoresignaturetext);
		   Flondysignature.setText(R.string.flondystoresignaturetext);
		   }
	 
    public void GotoTW (View view) {
          goToUrl ( "https://twitter.com/Flondy_Dev" );
   }

    public void GotoFB (View view) {
          goToUrl ( "https://m.facebook.com/profile.php?id=703595876370289" );
   }

    public void GotoFD (View view) {
          goToUrl ( "http://flondy.com");
   }

    public void GotoPlayStore  (View view) {
     goToUrl ( "https://play.google.com/store/apps/developer?id=Flondy");
   }
    
    public void GotoAppStore  (View view) {
        goToUrl ( "https://itunes.apple.com/de/artist/Flondy");
    }

    public void GotoFlondyStore  (View view) {
     goToUrl ( "https://play.google.com/store/apps/details?id=dev.flondy.app");
 }
    
    public void GotoDonateUs  (View view) {
        goToUrl ( "https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=TAKC8KEF3MV6N");
    }
   
    public void EmailUsGmail (View view) {
          Intent sendIntent;
          sendIntent = new Intent(Intent.ACTION_SEND);
          sendIntent.putExtra(Intent. EXTRA_EMAIL  , new String[]{"flondy.dev@gmail.com" });
          sendIntent.putExtra(Intent. EXTRA_SUBJECT, "Hello Flondy it´s about:");
          sendIntent.putExtra(Intent. EXTRA_TEXT, "Hello Flondy it´s about: ");
          // [Android Studio Migration] leeren file://-Anhang entfernt (ab targetSdk 24 Absturz: FileUriExposedException)
          // sendIntent.putExtra(Intent. EXTRA_STREAM, Uri.parse( "file://" + ""));
          sendIntent.setType( "image/jpeg");
          startActivity(Intent. createChooser(sendIntent, "Send Mail"));
          }
   
    public void GotoEmailUsFlondymail (View view) {
    	   Intent sendIntent;
		   sendIntent = new Intent(Intent.ACTION_SEND);
		   sendIntent.putExtra(Intent. EXTRA_EMAIL  , new String[]{"contact@flondy.com" });
		   sendIntent.putExtra(Intent. EXTRA_SUBJECT, "Hello Flondy it´s about:");
		   sendIntent.putExtra(Intent. EXTRA_TEXT, "Hello Flondy it´s about: " );
		   // [Android Studio Migration] leeren file://-Anhang entfernt (ab targetSdk 24 Absturz: FileUriExposedException)
		   // sendIntent.putExtra(Intent. EXTRA_STREAM, Uri.parse( "file://" + "" ));
		   sendIntent.setType( "image/jpeg");
		   startActivity(Intent. createChooser(sendIntent, "Send Mail"));
		   }
   
    public void lizenz (View view) 
    {
    	   setContentView(R.layout.lizenzscreen);
    	   ScrollView lizscroll = (ScrollView) findViewById(R.id.lizenzscrollview );
    	   getLayoutInflater().inflate(R.layout.lizenzcontent, lizscroll);
    	   
    	   // DEKLARIERT UND FINDET DEN ZURÜCKBUTTON AUF DEM 'LIZENZSCREEN.XML'
    	   Button lizenzScreenBackButton = (Button)findViewById(R.id.lizenzScreenBackButton);
    	   
    	   // DEKLARIERT UND FINDET DEN LIZENZTITLE UND TEXT DER ALLGEMEINEN LIZENZBEDINGUNGEN AUF DEM 'LIZENZCONTENT.XML'
    	   TextView lizenzTitle = (TextView)findViewById(R.id.lizenzTitle);
    	   TextView generalLizenzText = (TextView)findViewById(R.id.generalLizenzText);
    	   
    	   // DEKLARIERT UND FINDET DIE 'LIZENZ' TEXTVIEWS DER JEWEILIGEN ARTISTS AUF DEM 'LIZENZCONTENT.XML'
    	   TextView lizenzText1 = (TextView)findViewById(R.id.LizenzText1);
    	   TextView lizenzText2 = (TextView)findViewById(R.id.LizenzText2);
    	   TextView lizenzText3 = (TextView)findViewById(R.id.LizenzText3);
    	   TextView lizenzText4 = (TextView)findViewById(R.id.LizenzText4);
    	   TextView lizenzText5 = (TextView)findViewById(R.id.LizenzText5);
    	   TextView lizenzText6 = (TextView)findViewById(R.id.LizenzText6);
    	   
    	   // DEKLARIERT UND FINDET DIE LIZENZART TEXTVIEWS DER JEWEILIGEN ARTISTS AUF DEM 'LIZENZCONTENT.XML'
    	   TextView lizenzKindText1 = (TextView)findViewById(R.id.LizenzKindText1);
    	   TextView lizenzKindText2 = (TextView)findViewById(R.id.LizenzKindText2);
    	   TextView lizenzKindText3 = (TextView)findViewById(R.id.LizenzKindText3);
    	   TextView lizenzKindText4 = (TextView)findViewById(R.id.LizenzKindText4);
    	   TextView lizenzKindText5 = (TextView)findViewById(R.id.LizenzKindText5);
    	   TextView lizenzKindText6 = (TextView)findViewById(R.id.LizenzKindText6);
    	    
    	   // DEKLARIERT UND FINDET DIE BUTTONS DER JEWEILIGEN ARTISTS AUF DEM 'LIZENZCONTENT.XML'
    	   Button GotoArtist1Button = (Button)findViewById(R.id.GotoArtist1Button);
    	   Button GotoArtist2Button = (Button)findViewById(R.id.GotoArtist2Button);
    	   Button GotoArtist3Button = (Button)findViewById(R.id.GotoArtist3Button);
    	   Button GotoArtist4Button = (Button)findViewById(R.id.GotoArtist4Button);
    	   Button GotoArtist5Button = (Button)findViewById(R.id.GotoArtist5Button);
    	   Button GotoArtist6Button = (Button)findViewById(R.id.GotoArtist6Button);
    	   
    	   //SETZT DIE TEXTE ...
    	   lizenzScreenBackButton.setText(R.string.back);
    	  
    	   lizenzTitle.setText(R.string.lizenzbedingtitle);
    	   generalLizenzText.setText(R.string.alleinhalte);
    	   
    	   lizenzText1.setText(R.string.lizenz);
    	   lizenzText2.setText(R.string.lizenz);
    	   lizenzText3.setText(R.string.lizenz);
    	   lizenzText4.setText(R.string.lizenz);
    	   lizenzText5.setText(R.string.lizenz);
    	   lizenzText6.setText(R.string.lizenz);
    	   
    	   lizenzKindText1.setText(R.string.lizenzkind1);
    	   lizenzKindText2.setText(R.string.lizenzkind2);
    	   lizenzKindText3.setText(R.string.lizenzkind3);
    	   lizenzKindText4.setText(R.string.lizenzkind4);
    	   lizenzKindText5.setText(R.string.lizenzkind5);
    	   lizenzKindText6.setText(R.string.lizenzkind6);
    	   
    	   GotoArtist1Button.setText(R.string.gotoArtist1ButtonText);
    	   GotoArtist2Button.setText(R.string.gotoArtist2ButtonText);
    	   GotoArtist3Button.setText(R.string.gotoArtist3ButtonText);
    	   GotoArtist4Button.setText(R.string.gotoArtist4ButtonText);
    	   GotoArtist5Button.setText(R.string.gotoArtist5ButtonText);
    	   GotoArtist6Button.setText(R.string.gotoArtist6ButtonText);
    }
    
    public void GotoArtist1 (View view) {
    	 goToUrl(this.getResources().getString(R.string.gotoArtist1URL));
 }
    
    
    public void GotoArtist2 (View view) {
        goToUrl (this.getResources().getString(R.string.gotoArtist2URL));
 }
    
    
    public void GotoArtist3 (View view) {
        goToUrl (this.getResources().getString(R.string.gotoArtist3URL));
 }
    
    
    public void GotoArtist4 (View view) {
        goToUrl (this.getResources().getString(R.string.gotoArtist4URL));
 }
    
    
    public void GotoArtist5 (View view) {
        goToUrl (this.getResources().getString(R.string.gotoArtist5URL));
 }
    
    
    public void GotoArtist6 (View view) {
        goToUrl (this.getResources().getString(R.string.gotoArtist6URL));
 }
    
   public void more (View view) {
	   setContentView(R.layout.morescreen);
	   
	   // DEKLARIERT UND FINDET DEN ZURÜCKBUTTON UND FLONDYBUTTON AUF DEM 'MORESCREEN.XML'
	   Button moreScreenLizenzButton = (Button)findViewById(R.id.moreScreenLizenzButton);
	   Button moreScreenBackButton = (Button)findViewById(R.id.moreScreenBackButton);
	   
	   //SETZT DIE TEXTE ...
	   moreScreenLizenzButton.setText(R.string.lizenz);
	   moreScreenBackButton.setText(R.string.back);
	   }
   
    public void BackTo(View view){
    setContentView(R.layout.main);
    SettingUpMainLay();
    }
    
    public void showklick (View view) {
    }
  
    private void goToUrl (String url) {
    Uri uriUrl = Uri. parse(url);
    Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
    startActivity(launchBrowser);
}
    // Flondy - More und Lizenzscreen Ende                          
}



