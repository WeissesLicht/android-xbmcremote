package org.xbmc.android.remote.presentation.controller;


import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import org.xbmc.android.remote.R;
import org.xbmc.android.remote.business.ManagerFactory;
import org.xbmc.android.remote.presentation.activity.GestureRemoteActivity;
import org.xbmc.android.remote.presentation.activity.NowPlayingActivity;
import org.xbmc.android.remote.presentation.activity.VoiceRecognitionActivity;
import org.xbmc.android.remote.presentation.controller.ListController;

import org.xbmc.android.util.NameOptionsSplitter;
import org.xbmc.android.widget.gestureremote.IGestureListener;
import org.xbmc.api.business.DataResponse;
import org.xbmc.api.business.IControlManager;
import org.xbmc.api.business.IEventClientManager;
import org.xbmc.api.business.IInfoManager;
import org.xbmc.api.business.IMusicManager;
import org.xbmc.api.business.INotifiableManager;
import org.xbmc.api.business.IVideoManager;
import org.xbmc.api.info.GuiSettings;
import org.xbmc.api.object.Album;
import org.xbmc.api.object.Artist;
import org.xbmc.api.object.Song;
import org.xbmc.api.object.Movie;
import org.xbmc.api.presentation.INotifiableController;
import org.xbmc.api.type.SortType;
import org.xbmc.eventclient.ButtonCodes;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;

import android.os.Handler.Callback;

public class VoiceRecognitionController extends ListController  implements INotifiableController{
	/**
	 * 
	 */
	public static final String TAG = "VoiceRecognitionController";
	
	private static final long serialVersionUID = 1L;
	IEventClientManager mEventClientManager;
	IInfoManager mInfoManager;
	IControlManager mControl;
	
	private ArrayList<String> supportedCommands;
	
	public static final String COMMAND_PLAY = "play";
	public static final String COMMAND_PAUSE = "pause";
	public static final String COMMAND_FF = "fast forward";
	public static final String COMMAND_RW = "rewind";
	public static final String COMMAND_STOP = "stop";
	public static final String COMMAND_NEXT = "next"; 
	public static final String COMMAND_PREVIOUS = "previous";
	public static final String COMMAND_UP = "up";
	public static final String COMMAND_DOWN = "down";
	public static final String COMMAND_LEFT = "left";
	public static final String COMMAND_RIGHT = "right";
	public static final String COMMAND_SELECT = "select";
	public static final String COMMAND_TITLE = "title";
	public static final String COMMAND_INFO = "info";
	public static final String COMMAND_MENU = "menu";
	public static final String COMMAND_BACK = "back";
	public static final String COMMAND_VIDEO = "video";
	public static final String COMMAND_MUSIC = "music";
	public static final String COMMAND_IMAGES = "images";
	public static final String COMMAND_TV = "tv";
	public static final String COMMAND_PLAY_SONG = "play song";
	public static final String COMMAND_PLAY_ALBUM = "play album";
	public static final String COMMAND_PLAY_MOVIE = "play movie";
	
	final SharedPreferences prefs;

	private IMusicManager mMusicManager;
	private IVideoManager mVideoManager;
	private IControlManager mControlManager;
	
	public void onCreate(Activity activity, Handler handler, AbsListView list) {
		mActivity = activity;
		
	}
	public VoiceRecognitionController(Activity activity, Context context) {
		prefs = PreferenceManager.getDefaultSharedPreferences(context);
		
		mActivity = activity;
		mControl = ManagerFactory.getControlManager(this);
		mInfoManager = ManagerFactory.getInfoManager(this);
		mEventClientManager = ManagerFactory.getEventClientManager(this);
		mMusicManager = ManagerFactory.getMusicManager(this);
		mVideoManager = ManagerFactory.getVideoManager(this);
		mControlManager = ManagerFactory.getControlManager(this);
		//mVibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
		//mDoVibrate = prefs.getBoolean("setting_vibrate_on_touch", true);
		mInfoManager.getGuiSettingInt(new DataResponse<Integer>() {
//			@Override
//			public void run() {
//				mHandler.post(new Runnable() {
//					public void run() {
//						mEventServerInitialDelay = value;
//						Log.i("RemoteController", "Saving previous value " + GuiSettings.getName(GuiSettings.Services.EVENT_SERVER_INITIAL_DELAY) + " = " + value);
//					}
//				});
//			}
		}, GuiSettings.Services.EVENT_SERVER_INITIAL_DELAY, context);
		populateCommandList();
	}
	private void populateCommandList() {
		supportedCommands = new ArrayList<String>();
		
		supportedCommands.add(COMMAND_PAUSE);
		supportedCommands.add(COMMAND_FF);
		supportedCommands.add(COMMAND_RW);
		supportedCommands.add(COMMAND_STOP);
		supportedCommands.add(COMMAND_NEXT);
		supportedCommands.add(COMMAND_PREVIOUS);
		supportedCommands.add(COMMAND_UP);
		supportedCommands.add(COMMAND_DOWN);
		supportedCommands.add(COMMAND_LEFT);
		supportedCommands.add(COMMAND_RIGHT);
		supportedCommands.add(COMMAND_SELECT);
		supportedCommands.add(COMMAND_TITLE);
		supportedCommands.add(COMMAND_INFO);
		supportedCommands.add(COMMAND_MENU);
		supportedCommands.add(COMMAND_BACK);
		supportedCommands.add(COMMAND_VIDEO);
		supportedCommands.add(COMMAND_MUSIC);
		supportedCommands.add(COMMAND_IMAGES);
		supportedCommands.add(COMMAND_TV);
		supportedCommands.add(COMMAND_PLAY_SONG);
		supportedCommands.add(COMMAND_PLAY_ALBUM);
		supportedCommands.add(COMMAND_PLAY_MOVIE);

		// This needs to be added last for now.
		supportedCommands.add(COMMAND_PLAY);
	}
	
	
	public void showVolume() {
		Log.d(TAG, "in showVolume");
	}

	public boolean parseAndAct(ArrayList<String> pMatches, Context context){
		Log.d(TAG, "parseAndAct matches: "+pMatches);
		for ( String lMatch : pMatches)  {
			lMatch.toLowerCase();
			Log.d(TAG, "parseAndAct lMatch: "+lMatch);
			for (int i = 0; i < supportedCommands.size(); i++ ) {
				if (lMatch.startsWith(supportedCommands.get(i))) {
					if (supportedCommands.get(i).equalsIgnoreCase(COMMAND_PLAY_SONG)){
						Log.d(TAG, "parseAndAct equals PLAY_SONG");
						if (lMatch.length() > supportedCommands.get(i).length() ) {
						lMatch = lMatch.substring(supportedCommands.get(i).length()+ 1);
						lMatch = lMatch.trim();
					
						runPlaySong(supportedCommands.get(i), lMatch, context);	
						}
					} else if (supportedCommands.get(i).equalsIgnoreCase(COMMAND_PLAY_ALBUM)) {
						Log.d(TAG, "parseAndAct equals PLAY_ALBUM");
							if (lMatch.length() > supportedCommands.get(i).length() ) {
								lMatch = lMatch.substring(supportedCommands.get(i).length()+ 1);
								lMatch = lMatch.trim();
						
						
								return runPlayAlbum(supportedCommands.get(i), lMatch, context);	
					
							}
					} else if (supportedCommands.get(i).equalsIgnoreCase(COMMAND_PLAY_MOVIE)) {
						Log.d(TAG, "parseAndAct equals PLAY_MOVIE");
						if (lMatch.length() > supportedCommands.get(i).length() ) {
							lMatch = lMatch.substring(supportedCommands.get(i).length()+ 1);
							lMatch = lMatch.trim();
					
					
							return runPlayMovie(supportedCommands.get(i), lMatch, context);	
				
						}
					}
						else {
							Log.d(TAG, "parseAndAct no match");
						runSimpleCommand(supportedCommands.get(i));
						if (lMatch.length() > supportedCommands.get(i).length()  ){
						lMatch = lMatch.substring(supportedCommands.get(i).length()+ 1);
						lMatch = lMatch.trim();
						boolean lfoundanother = true;
						while ((lMatch.length() > 0) && lfoundanother) {
							//Parse through the rest of the string to get more commands
							String lNextMatch = findMoreCommands(lMatch);
							if (lNextMatch == null ) {
								lfoundanother = false;
							} else {
								runSimpleCommand(lNextMatch);
								if (lMatch.length() >lNextMatch.length() ){
								lMatch = lMatch.substring(lNextMatch.length() + 1);
								lMatch = lMatch.trim();
								} else {
									lfoundanother = false;
								}
							}
						}
						}
					}
					return true;
				}
			}
		}
		return false;
	}
	
	public String findMoreCommands(String pMatch){
		for (int i = 0; i < supportedCommands.size(); i++ ) {
			if (pMatch.startsWith(supportedCommands.get(i))) {
				return supportedCommands.get(i);
			}
		}
		
		return null;
	}
	
	private boolean runSimpleCommand(String command){
		if (command.equalsIgnoreCase(COMMAND_PLAY)) {
			mEventClientManager.sendButton("R1", ButtonCodes.REMOTE_PLAY, false, true, true, (short)0, (byte)0);
			return true;
		} else if (command.equalsIgnoreCase(COMMAND_PAUSE)) {
			mEventClientManager.sendButton("R1", ButtonCodes.REMOTE_PAUSE, false, true, true, (short)0, (byte)0);
			return true;
		} else if (command.equalsIgnoreCase(COMMAND_FF)) {
			mEventClientManager.sendButton("R1", ButtonCodes.REMOTE_FORWARD, false, true, true, (short)0, (byte)0);
			return true;
		} else if (command.equalsIgnoreCase(COMMAND_RW)) {
			mEventClientManager.sendButton("R1", ButtonCodes.REMOTE_REVERSE, false, true, true, (short)0, (byte)0);
			return true;
		} else if (command.equalsIgnoreCase(COMMAND_STOP)) {
			mEventClientManager.sendButton("R1", ButtonCodes.REMOTE_STOP, false, true, true, (short)0, (byte)0);
			return true;
		} else if (command.equalsIgnoreCase(COMMAND_NEXT)) {
			mEventClientManager.sendButton("R1", ButtonCodes.REMOTE_SKIP_PLUS, false, true, true, (short)0, (byte)0);
			return true;
		} else if (command.equalsIgnoreCase(COMMAND_PREVIOUS)) {
			mEventClientManager.sendButton("R1", ButtonCodes.REMOTE_PAUSE, false, true, true, (short)0, (byte)0);
			return true;
		} else if (command.equalsIgnoreCase(COMMAND_UP)) {
			mEventClientManager.sendButton("R1", ButtonCodes.REMOTE_UP, false, true, true, (short)0, (byte)0);
			return true;
		} else if (command.equalsIgnoreCase(COMMAND_DOWN)) {
			mEventClientManager.sendButton("R1", ButtonCodes.REMOTE_DOWN, false, true, true, (short)0, (byte)0);
			return true;
		} else if (command.equalsIgnoreCase(COMMAND_LEFT)) {
			mEventClientManager.sendButton("R1", ButtonCodes.REMOTE_LEFT, false, true, true, (short)0, (byte)0);
			return true;
		} else if (command.equalsIgnoreCase(COMMAND_RIGHT)) {
			mEventClientManager.sendButton("R1", ButtonCodes.REMOTE_RIGHT, false, true, true, (short)0, (byte)0);
			return true;
		} else if (command.equalsIgnoreCase(COMMAND_SELECT)) {
			mEventClientManager.sendButton("R1", ButtonCodes.REMOTE_ENTER, false, true, true, (short)0, (byte)0);
			return true;							
		} else if (command.equalsIgnoreCase(COMMAND_TITLE)) {
			mEventClientManager.sendButton("R1", ButtonCodes.REMOTE_TITLE, false, true, true, (short)0, (byte)0);
			return true;							
		} else if (command.equalsIgnoreCase(COMMAND_INFO)) {
			mEventClientManager.sendButton("R1", ButtonCodes.REMOTE_INFO, false, true, true, (short)0, (byte)0);
			return true;							
		} else if (command.equalsIgnoreCase(COMMAND_MENU)) {
			mEventClientManager.sendButton("R1", ButtonCodes.REMOTE_MENU, false, true, true, (short)0, (byte)0);
			return true;							
		} else if (command.equalsIgnoreCase(COMMAND_BACK)) {
			mEventClientManager.sendButton("R1", ButtonCodes.REMOTE_BACK, false, true, true, (short)0, (byte)0);
			return true;							
		} else if (command.equalsIgnoreCase(COMMAND_VIDEO)) {
			mEventClientManager.sendButton("R1", ButtonCodes.REMOTE_MY_VIDEOS, false, true, true, (short)0, (byte)0);
			return true;							
		} else if (command.equalsIgnoreCase(COMMAND_MUSIC)) {
			mEventClientManager.sendButton("R1", ButtonCodes.REMOTE_MY_MUSIC, false, true, true, (short)0, (byte)0);
			return true;							
		} else if (command.equalsIgnoreCase(COMMAND_IMAGES)) {
			mEventClientManager.sendButton("R1", ButtonCodes.REMOTE_MY_PICTURES, false, true, true, (short)0, (byte)0);
			return true;							
		} else if (command.equalsIgnoreCase(COMMAND_TV)) {
			mEventClientManager.sendButton("R1", ButtonCodes.REMOTE_MY_TV, false, true, true, (short)0, (byte)0);
		} else {
			return false;
		}
		return false;
	}
	
	private boolean runPlayAlbum(String command, String commandParameter, Context context) {
		 ArrayList<Album> lAlbumList = 
				 mMusicManager.getAlbums( commandParameter.toLowerCase(), context);
		 if (lAlbumList.isEmpty()) {
				//Replace integers with roman numerals and try again.
				NameOptionsSplitter lNameOptionsSplitter = new NameOptionsSplitter();
				String commandParameterwithRomanNumerals = lNameOptionsSplitter.replaceIntwithRN(commandParameter);
				if (commandParameterwithRomanNumerals != null) {
					lAlbumList = 
							 mMusicManager.getAlbums(commandParameterwithRomanNumerals.toLowerCase(), context);
				}
			}
		 if (lAlbumList != null) {
		 for (Album lAlbum : lAlbumList) {
					mMusicManager.play(new QueryResponse(
							mActivity, 
							"Playing Album " + lAlbum.artist + "-" + lAlbum.name + "...", 
							"Error playing song!",
							true
						), lAlbum, mActivity.getApplicationContext());
					return true;
				
			}
		 }
		return false;
	}
	
	private boolean runPlaySong(String command, String commandParameter, Context context) {
		 ArrayList<Song> lSongList = 
				 mMusicManager.getSongs( commandParameter.toLowerCase(), context);
		if (lSongList.isEmpty()) {
				//Replace integers with roman numerals and try again.
				NameOptionsSplitter lNameOptionsSplitter = new NameOptionsSplitter();
				String commandParameterwithRomanNumerals = lNameOptionsSplitter.replaceIntwithRN(commandParameter);
				if (commandParameterwithRomanNumerals != null) {
					lSongList = 
							 mMusicManager.getSongs(commandParameterwithRomanNumerals.toLowerCase(), context);
				}
			}
		 if (lSongList != null) {
		 for (Song lSong : lSongList) {
				if (commandParameter.equalsIgnoreCase(lSong.title)) {
					mMusicManager.play(new QueryResponse(
							mActivity, 
							"Playing song " + lSong.artist + "-" + lSong.title + "...", 
							"Error playing song!",
							true
						), lSong, mActivity.getApplicationContext());
					return true;
				}
			}
		 }
		return false;
	}

	private boolean runPlayMovie(String command, String commandParameter, Context context) {
		ArrayList<Movie> lMovieList = 
				 mVideoManager.getMovies(commandParameter.toLowerCase(), context);
		
		if (lMovieList.isEmpty()) {
			//Replace integers with roman numerals and try again.
			NameOptionsSplitter lNameOptionsSplitter = new NameOptionsSplitter();
			String commandParameterwithRomanNumerals = lNameOptionsSplitter.replaceIntwithRN(commandParameter);
			if (commandParameterwithRomanNumerals != null) {
				lMovieList = 
					 mVideoManager.getMovies(commandParameterwithRomanNumerals.toLowerCase(), context);
			}
		}
		 if (lMovieList != null) {	
		 for (Movie lMovie : lMovieList) {
			 //mControlManager.playFile(new DataResponse<Boolean>() {
			 mControlManager.playUrl(new DataResponse<Boolean>() {
					public void run() {
						if (value) {
							//mActivity.startActivity(new Intent(mActivity, NowPlayingActivity.class));
						}
					}
				}, lMovie.getPath(), mActivity.getApplicationContext());
			 
					return true;
				}
			}
		 
		return false;
	}

	
	@Override
	public void onContextItemSelected(MenuItem item) {
		Log.d(TAG, "onContextItemSelected empty stub called");
		// TODO Auto-generated method stub		
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		Log.d(TAG, "onContextItemSelected empty stub called");
		// TODO Auto-generated method stub	
	} 
	
	public String buildVoiceInstructionList() {
		StringBuilder sb = new StringBuilder();
		for (String lString : supportedCommands) {
			if (sb.toString().length() > 0 ) {
				sb.append(" ");
			}
			sb.append(lString);
			
			if ( lString.equalsIgnoreCase(COMMAND_PLAY_ALBUM) ) {
				sb.append(" <ALBUM NAME>");
			} else if (lString.equalsIgnoreCase(COMMAND_PLAY_MOVIE)) {
				sb.append(" <MOVIE NAME>");
			}  else if (lString.equalsIgnoreCase(COMMAND_PLAY_SONG)) {
				sb.append(" <SONG NAME>");
			}
			sb.append("\n");
		}
		return sb.toString();
	}
}
