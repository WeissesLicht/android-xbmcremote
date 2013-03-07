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
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageButton;
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
		
		
        ImageButton speakButton = (ImageButton) findViewById(R.id.voxRecButton);
        voxRecResultsList = (ListView) findViewById(R.id.voxRecResultsList);
        voxRecResultsList.setEmptyView(findViewById(R.id.voxRecResultsListEmpty));
        
        // Disable Button if no recognition service is present
        PackageManager pm = getPackageManager();
        List<ResolveInfo> activities = pm.queryIntentActivities(new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
        
        //Check if recognition service is available
        if (activities.size() == 0)
        {   
        	//Disable button if no recognition service
        	speakButton.setEnabled(false);
        	Toast.makeText(getApplicationContext(), "Speech recognizer not present", Toast.LENGTH_SHORT).show();        	
        }
        
        showVoiceInstructions(mVoiceRecognitionController);

    }
    
    private void showVoiceInstructions(VoiceRecognitionController controller) {
    	emptyList = (TextView) findViewById(R.id.voxRecResultsListEmpty);
    	String preface = getString(R.string.vox_supported_commands);
    	String instructions = controller.buildVoiceInstructionList();
    	emptyList.setText(preface + "\n" + instructions);
    }
    
    /**
     * Handle the action of the button being clicked
     */
    public void speakButtonClicked(View v)
    {
    	Log.d(TAG, "speakButtonClicked...");
    	emptyList.setText("");
    	voxRecResultsList.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, new ArrayList<String>()));
    	emptyList.invalidate();
    	voxRecResultsList.invalidate();
        startVoiceRecognitionActivity();
    }
 
    /**
     * Fire an intent to start the voice recognition activity.
     */
    private void startVoiceRecognitionActivity()
    {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "XBMC Commands...");
        startActivityForResult(intent, REQUEST_CODE);
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
            ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            Log.d(TAG, "Found matches:" + matches.toString());
            voxRecResultsList.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, matches));
            mVoiceRecognitionController.parseAndAct(matches, this.getApplicationContext());
        }
        super.onActivityResult(requestCode, resultCode, data);
        
    }
}
