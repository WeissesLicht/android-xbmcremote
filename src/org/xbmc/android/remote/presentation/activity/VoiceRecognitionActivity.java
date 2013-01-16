package org.xbmc.android.remote.presentation.activity;

import org.xbmc.android.remote.R;
import org.xbmc.android.remote.business.ManagerFactory;
import org.xbmc.android.remote.presentation.controller.NowPlayingController;
import org.xbmc.android.remote.presentation.controller.RemoteController;
import org.xbmc.android.remote.presentation.controller.VoiceRecognitionController;
import org.xbmc.android.remote.presentation.widget.JewelView;
import org.xbmc.android.util.KeyTracker;
import org.xbmc.android.util.KeyTracker.Stage;
import org.xbmc.android.util.OnLongPressBackKeyTracker;
import org.xbmc.api.business.IEventClientManager;
import org.xbmc.api.object.Song;
import org.xbmc.api.type.ThumbSize;
import org.xbmc.eventclient.ButtonCodes;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;


public class VoiceRecognitionActivity extends Activity {
	private static final int REQUEST_CODE = 1234;
	private ListView voxRecResultsList;
	private TextView voxRecIntructionsView;
	IEventClientManager mEventClientManager;
	private VoiceRecognitionController mVoiceRecognitionController;
	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.voicerecview);
        mVoiceRecognitionController = new VoiceRecognitionController(this, getApplicationContext());
        //mEventClientManager = ManagerFactory.getEventClientManager(this);
        Button speakButton = (Button) findViewById(R.id.voxRecButton);
        
        voxRecResultsList = (ListView) findViewById(R.id.voxRecResultsList);
        voxRecIntructionsView = (TextView) findViewById(R.id.voxRecIntructionsView);
        
        // Disable Button if no recognition service is present
        PackageManager pm = getPackageManager();
        List<ResolveInfo> activities = pm.queryIntentActivities(
                new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
        if (activities.size() == 0)
        {   speakButton.setEnabled(false);
        	speakButton.setText("Recognizer not present");
        
        	//Disable if no recognition service
        	
        }
        showVoiceInstructions(mVoiceRecognitionController);
        /*//Test
        String match = "play movie men";
       //String match = "pause";
       ArrayList<String> matches = new ArrayList<String>();
        matches.add(match);
       Log.i("VoiceRecognitionActivity", match);
        mVoiceRecognitionController.parseAndAct(matches, this.getApplicationContext()); */
    }
    
    private void showVoiceInstructions(VoiceRecognitionController controller) {
    	voxRecIntructionsView.append(controller.buildVoiceInstructionList());
    	
    	
    }
    /**
     * Handle the action of the button being clicked
     */
    public void speakButtonClicked(View v)
    {
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
            ArrayList<String> matches = data.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS);
            voxRecResultsList.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
                    matches));
            mVoiceRecognitionController.parseAndAct(matches, this.getApplicationContext());
        }
        super.onActivityResult(requestCode, resultCode, data);
        
    }
}
