package org.xbmc.android.remote.presentation.controller;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Locale;

import org.xbmc.android.remote.business.ManagerFactory;
import org.xbmc.android.remote.presentation.controller.ListController;
import org.xbmc.android.util.NameOptionsSplitter;
import org.xbmc.api.business.DataResponse;
import org.xbmc.api.business.IControlManager;
import org.xbmc.api.business.IEventClientManager;
import org.xbmc.api.business.IInfoManager;
import org.xbmc.api.business.IMusicManager;
import org.xbmc.api.business.IVideoManager;
import org.xbmc.api.info.GuiSettings;
import org.xbmc.api.object.Album;
import org.xbmc.api.object.Song;
import org.xbmc.api.object.Movie;
import org.xbmc.api.presentation.INotifiableController;
import org.xbmc.eventclient.ButtonCodes;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;

public class VoiceRecognitionController extends ListController  implements INotifiableController{
	/**
	 * 
	 */
	public static final String TAG = "VoiceRecognitionController";
	
	private static final long serialVersionUID = 1L;
	IEventClientManager mEventClientManager;
	IInfoManager mInfoManager;
	IControlManager mControl;
	
	private LinkedHashMap<String, String> singleCommandList;
	private static final String playOptionsCommand = "play"; //TODO move to Strings.xml to allow for localization?
	private LinkedHashMap<String, String> playOptionsList;
	
	/*private ArrayList<String> supportedCommands;
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
	*/
	final SharedPreferences prefs;

	private IMusicManager mMusicManager;
	private IVideoManager mVideoManager;
	private IControlManager mControlManager;
	
	public void onCreate(Activity activity, Handler handler, AbsListView list) {
		super.onCreate(activity, handler, list);
		Log.d(TAG, "mActivity: "+mActivity.toString());
		mActivity = activity;
		Log.d(TAG, "mActivity: "+mActivity.toString());
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
		
		//Build list of available single word commands and the ButtonCode for the command
		//TODO move key words to Strings.xml to allow for localization?
		singleCommandList = new LinkedHashMap<String, String>();
		singleCommandList.put("pause", ButtonCodes.REMOTE_PAUSE);
		singleCommandList.put("play", ButtonCodes.REMOTE_PLAY);
		singleCommandList.put("rewind", ButtonCodes.REMOTE_REVERSE);
		singleCommandList.put("forward", ButtonCodes.REMOTE_FORWARD);
		singleCommandList.put("stop", ButtonCodes.REMOTE_STOP);
		singleCommandList.put("next", ButtonCodes.REMOTE_SKIP_PLUS);
		singleCommandList.put("previous", ButtonCodes.REMOTE_SKIP_MINUS);
		singleCommandList.put("up", ButtonCodes.REMOTE_UP);
		singleCommandList.put("down", ButtonCodes.REMOTE_DOWN);
		singleCommandList.put("left", ButtonCodes.REMOTE_LEFT);
		singleCommandList.put("right", ButtonCodes.REMOTE_RIGHT);
		singleCommandList.put("select", ButtonCodes.REMOTE_ENTER);
		singleCommandList.put("title", ButtonCodes.REMOTE_TITLE);
		singleCommandList.put("info", ButtonCodes.REMOTE_INFO);
		singleCommandList.put("menu", ButtonCodes.REMOTE_MENU);
		singleCommandList.put("back", ButtonCodes.REMOTE_BACK);
		singleCommandList.put("video", ButtonCodes.REMOTE_MY_VIDEOS);
		singleCommandList.put("music", ButtonCodes.REMOTE_MY_MUSIC);
		singleCommandList.put("images", ButtonCodes.REMOTE_MY_PICTURES);
		singleCommandList.put("tv", ButtonCodes.REMOTE_MY_TV);

		//List of commands that can occur after the play command
		playOptionsList = new LinkedHashMap<String, String>();
		playOptionsList.put("album", "playAlbum");
		playOptionsList.put("song", "playSong");
		playOptionsList.put("artist", "playArtist");
		playOptionsList.put("playlist", "playPlaylist");
		playOptionsList.put("movie", "playMovie");
		playOptionsList.put("latest", "playLatest");
		playOptionsList.put("next", "playNext");
		
		/*
		 * 
			display/goto? (insert show here) / season 
		 */
		
		}

	public boolean parseAndAct(ArrayList<String> pMatches, Context context){
		Log.d(TAG, "parseAndAct all voice result: "+pMatches);
		//Loop through each voice result until we get a match
		for ( String lMatch : pMatches)  {
			//remove trailing and leading space and make lower case
			lMatch = lMatch.toLowerCase(Locale.US).trim();
			//First check to see if there is just one 'word'
			String[] matchedWords = lMatch.split(" ");
			if (matchedWords.length == 1)
			{
				//If we found a valid and successful command return, else keep trying
				if (runSingleCommand(singleCommandList.get(matchedWords[0]))) return true;
			}
			//Maybe first word is play
			else if (matchedWords.length > 1 && playOptionsCommand.equalsIgnoreCase(matchedWords[0]))
			{
				Log.d(TAG, "Multi-word play command");
				//Check what to play, song album etc....
				if (playOptionsList.containsKey(matchedWords[1]))
				{
					Log.d(TAG, "Play options function: "+ playOptionsList.get(matchedWords[1]));
					StringBuilder searchTerm = new StringBuilder();
					for (int i = 2; i < matchedWords.length; i++)
					{
						searchTerm.append(matchedWords[i]+" ");
					}
					//If we found a valid and successful command return, else keep trying
					if (playWithOptions(playOptionsList.get(matchedWords[1]), searchTerm.toString(), context)) return true;
				}
			}
			//Maybe first word is garbage, and second word is actual command>
			else
			{	
				//TODO
			}
		}
		//No valid commands found
		return false;
	}
	
	/**
	 * Function to manage redirecting to correct function to play the relevant media
	 * 
	 * @param method - which function to redirect to 
	 * @param searchTerm - search term
	 * @param context - application context
	 * @return - true if played successfully, false otherwise
	 */
	private boolean playWithOptions(String method, String searchTerm, Context context) {
		Log.d(TAG, "playWithOptions: "+method +" 2: "+searchTerm);
		if (method.equalsIgnoreCase("playSong")) { return runPlaySong(searchTerm, context); }
		else if (method.equalsIgnoreCase("playAlbum")) { return runPlayAlbum(searchTerm, context); }
		else if (method.equalsIgnoreCase("playMovie")) { return runPlayMovie(searchTerm, context); }
		else if (method.equalsIgnoreCase("playArtist")) { return runPlayArtist(searchTerm, context); }
		else if (method.equalsIgnoreCase("playPlaylist")) { return runPlayPlaylist(searchTerm, context); }
		else if (method.equalsIgnoreCase("playLatest")) { return runPlayLatest(searchTerm, context); }
		else if (method.equalsIgnoreCase("playNext")) { return runPlayNext(searchTerm, context); }

		return false;
	}
	
	/**
	 * Helper function to send a simple key press to xbmc instance
	 * @param command - command to send; must be a valid ButtonCodes command
	 * @return - true if a valid command was sent to xbmc else false 
	 */
	private boolean runSingleCommand(String command)
	{
		if (singleCommandList.containsKey(command))
		{
			//Found a valid command, so run it and we're done
			Log.d(TAG, "runSingleCommand matched on:"+ command);
			mEventClientManager.sendButton("R1", singleCommandList.get(command), false, true, true, (short)0, (byte)0);
			return true;
		}
		//No matching command in this result
		Log.d(TAG, "No command matches: "+command);
		return false;
	}
	
	/**
	 * Function to play or queue an Album 
	 * @param searchTerm string to find in Album name
	 * @param context
	 * @return true if successfully found and played an album else false
	 */
	private boolean runPlayAlbum(String searchTerm, Context context) {
		Log.d(TAG, "in runPlayAlbum");
		//Search for matching album titles as supplied
		ArrayList<Album> lAlbumList = mMusicManager.getAlbums( searchTerm.toLowerCase(Locale.US), context);
		
		//Try replacing numbers with roman numerals
		if (lAlbumList == null || lAlbumList.isEmpty()) {
			//Replace integers with roman numerals and try again.
			NameOptionsSplitter lNameOptionsSplitter = new NameOptionsSplitter();
			String commandParameterwithRomanNumerals = lNameOptionsSplitter.replaceIntwithRN(searchTerm);
			if (commandParameterwithRomanNumerals != null) {
				lAlbumList = mMusicManager.getAlbums(commandParameterwithRomanNumerals.toLowerCase(Locale.US), context);
			}
		}
		
		//If we have found some albums - play them
		//TODO use play / queue preference 
		//TODO validate play / queue works
		//TODO check how the loop works - play one at a time?
		if (lAlbumList != null) {
			Log.d(TAG, "lAlbumList "+lAlbumList.toString());
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
	
	private boolean runPlaySong(String searchTerm, Context context) {
		Log.d(TAG, "in runPlaySong");
		//Search for matching song titles as supplied
		ArrayList<Song> lSongList = mMusicManager.getSongs( searchTerm.toLowerCase(Locale.US), context);
		
		if (lSongList == null || lSongList.isEmpty()) {
			//Found no songs try replacing numbers with roman numerals
			Log.d(TAG, "songList isEmpty");
			//Replace integers with roman numerals and try again.
			NameOptionsSplitter lNameOptionsSplitter = new NameOptionsSplitter();
			String commandParameterwithRomanNumerals = lNameOptionsSplitter.replaceIntwithRN(searchTerm);
			if (commandParameterwithRomanNumerals != null) {
				lSongList = mMusicManager.getSongs(commandParameterwithRomanNumerals.toLowerCase(Locale.US), context);
			}
		}
		
		//If we have found some songs - play them
		//TODO use play / queue preference 
		//TODO validate play / queue works
		if (lSongList != null) {
			for (Song lSong : lSongList) {
				Log.d(TAG, "Play song: "+ lSong.title);
				mMusicManager.play(new QueryResponse(
							mActivity, 
							"Playing song " + lSong.artist + "-" + lSong.title + "...", 
							"Error playing song!",
							true
						), lSong, mActivity.getApplicationContext());
				return true;
			}
		}
		//No songs found
		return false;
	}

	private boolean runPlayMovie(String searchTerm, Context context) {
		Log.d(TAG, "in runPlayMovie");
		//Search for matching movie titles as supplied
		ArrayList<Movie> lMovieList = mVideoManager.getMovies(searchTerm.toLowerCase(Locale.US), context);
		
		//Try replacing numbers with roman numerals
		if (lMovieList == null || lMovieList.isEmpty()) {
			//Replace integers with roman numerals and try again.
			NameOptionsSplitter lNameOptionsSplitter = new NameOptionsSplitter();
			String commandParameterwithRomanNumerals = lNameOptionsSplitter.replaceIntwithRN(searchTerm);
			if (commandParameterwithRomanNumerals != null) {
				lMovieList = 
					 mVideoManager.getMovies(commandParameterwithRomanNumerals.toLowerCase(Locale.US), context);
			}
		}
		
		//If we have found some movies - play them
		//TODO use play / queue preference 
		//TODO validate play / queue works
		if (lMovieList != null) {
			Log.d(TAG, "lMovieList "+lMovieList.toString());
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
	
	/**
	 * Play next unwatched episode in a tv show
	 * @param searchTerm term to search for tv show name
	 * @param context
	 * @return
	 */
	private boolean runPlayNext(String searchTerm, Context context) {
		// TODO Auto-generated method stub
		return false;
	}
	
	/**
	 * Play latest episode in a tv show
	 * @param searchTerm term to search for tv show name
	 * @param context
	 * @return
	 */
	private boolean runPlayLatest(String searchTerm, Context context) {
		// TODO Auto-generated method stub
		return false;
	}
	
	/**
	 * Function to play playlists that match search term 
	 * @param searchTerm 
	 * @param context
	 * @return
	 */
	private boolean runPlayPlaylist(String searchTerm, Context context) {
		// TODO Auto-generated method stub
		return false;
	}
	
	
	private boolean runPlayArtist(String searchTerm, Context context) {
		// TODO Auto-generated method stub
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
	
	/**
	 * Helper function to dynamically generate a list of understood commands
	 * @return A String containing all understood commands
	 */
	public String buildVoiceInstructionList() {
		StringBuilder sb = new StringBuilder();
		//Build the single word commands into a string
		for (String lString : singleCommandList.keySet()) {
			sb.append(lString);
			sb.append("\n");
		}
		//Add the play options
		for (String lString : playOptionsList.keySet()) {
			sb.append("play "+lString+"\n");
		}
		return sb.toString();
	}
}
