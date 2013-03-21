package org.xbmc.android.remote.presentation.activity;

import org.xbmc.android.remote.R;
import org.xbmc.android.remote.presentation.controller.VoiceRecognitionController;
import org.xbmc.api.business.IEventClientManager;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.speech.RecognizerIntent;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
//import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;


public class VoiceRecognitionActivity extends Activity {
	private static final int REQUEST_CODE = 1234;
	private static final String TAG = "VoiceRecognitionActivity";
	private ListView voxRecResultsList;
	private TextView emptyList;
	IEventClientManager mEventClientManager;
	private VoiceRecognitionController mVoiceRecognitionController;
	private boolean bVoiceRecognizerPresent = false;
	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.voicerecview);
        mVoiceRecognitionController = new VoiceRecognitionController(this, getApplicationContext());

        // remove nasty top fading edge
		FrameLayout topFrame = (FrameLayout)findViewById(android.R.id.content);
		topFrame.setForeground(null);
		
		// Set title
		TextView title = (TextView)findViewById(R.id.titlebar_text);
		title.setText("XBMC Remote Voice Control");
		
		
        //ImageButton speakButton = (ImageButton) findViewById(R.id.voxRecButton);
        voxRecResultsList = (ListView) findViewById(R.id.voxRecResultsList);
        voxRecResultsList.setEmptyView(findViewById(R.id.voxRecResultsListEmpty));
        
        // Determine if recognition service is present
        PackageManager pm = getPackageManager();
        List<ResolveInfo> activities = pm.queryIntentActivities(new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
        if (activities.size() == 0)
        {   
        	bVoiceRecognizerPresent = false;
        	//Disable button if no recognition service
        	//speakButton.setEnabled(false);
        	Toast.makeText(getApplicationContext(), "Speech recognizer not present", Toast.LENGTH_SHORT).show();        	
        }
        else 
        {
        	bVoiceRecognizerPresent = true;
        }
        
        showVoiceInstructions(mVoiceRecognitionController);

    }
    
    private void showVoiceInstructions(VoiceRecognitionController controller) {
    	emptyList = (TextView) findViewById(R.id.voxRecResultsListEmpty);
    	ArrayList<String> instructions = controller.buildVoiceInstructionList();
    	emptyList.setMovementMethod(new ScrollingMovementMethod());
    	emptyList.setText(getString(R.string.vox_supported_commands)+"\n");
    	for (String lString : instructions) {
			emptyList.append(" - "+lString+"\n");
		}
    }
    
    /**
     * Handle the action of the button being clicked
     */
    public void speakButtonClicked(View v)
    {
    	Log.d(TAG, "speakButtonClicked...");
        startVoiceRecognitionActivity();
    }
    
    
    /**
     * Fire an intent to start the voice recognition activity.
     */
    private void startVoiceRecognitionActivity()
    {
    	if (bVoiceRecognizerPresent)
    	{
    		Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
    		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
    		intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "XBMC Commands...");
    		startActivityForResult(intent, REQUEST_CODE);
    	}
    	else
    	{
    		//No voice recognizer, so fake it for testing purposes  
    		ArrayList<String> commands = new ArrayList<String>();
    		commands.add("play movie best");
    		commands.add("play song take");
    		commands.add("play");
    		commands.add("stop");
    		commands.add("pause");
            Log.d(TAG, "Found matches:" + commands.toString());
            voxRecResultsList.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, commands));
            boolean res = mVoiceRecognitionController.parseAndAct(commands, this.getApplicationContext());
    		Log.d(TAG, "Result of parseAndAct was: " +res);
    		if(!res) Toast.makeText(this.getApplicationContext(), "No command successful", Toast.LENGTH_SHORT).show();
    	}
    }
	
    /**
     * Handle the results from the voice recognition activity.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK)
        {
            // Populate the wordsList with the String values the recognition engine thought it heard
        	Log.d(TAG, "Recognizer Results: "+RecognizerIntent.EXTRA_RESULTS.toString());
            ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            Log.d(TAG, "Found matches:" + matches.toString());
            voxRecResultsList.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, matches));
            boolean res = mVoiceRecognitionController.parseAndAct(matches, this.getApplicationContext());
            Log.d(TAG, "Result of parseAndAct was: " +res);
        }
        super.onActivityResult(requestCode, resultCode, data);
        
    }
}
