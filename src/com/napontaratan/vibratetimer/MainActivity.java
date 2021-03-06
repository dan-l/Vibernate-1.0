package com.napontaratan.vibratetimer;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

import com.google.analytics.tracking.android.EasyTracker;
import com.napontaratan.vibratetimer.controller.VibrateTimerController;
import com.napontaratan.vibratetimer.model.VibrateTimer;

import android.os.Bundle;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class MainActivity extends Activity {

	private List<VibrateTimer> vibrateTimers;
	private String[] days = new String[]{"Su ","Mo ","Tu ","We ","Th ","Fr ","Sa "};
	private VibrateTimerController controller;
	private static String SELECTED_TIMER = "selected_timer";
	VibrateArrayAdapter vaa;
	private ListView listViewVibrateTimers;
	

	/**
	 * Google Analytics code
	 */
	@Override
	public void onStart() {
		super.onStart();
		EasyTracker.getInstance(this).activityStart(this);
	}
	
	@Override
	public void onStop(){
		super.onStop();
		EasyTracker.getInstance(this).activityStop(this);
	}
	
	/**
	 * Main interface for the app
	 * @author Daniel
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		controller = new VibrateTimerController(this.getBaseContext());

		vibrateTimers = controller.getVibrateTimers();
		if(vibrateTimers == null) // no existing timers
			vibrateTimers = new ArrayList<VibrateTimer>();

		System.out.println(vibrateTimers.size());
		listViewVibrateTimers = (ListView) findViewById(R.id.vibrates);
		registerForContextMenu(listViewVibrateTimers);
		vaa = new VibrateArrayAdapter(this, R.layout.vibrate, vibrateTimers);
		listViewVibrateTimers.setAdapter(vaa);
		setListViewMultiChoiceMode();
		

		System.out.println("number of alarms: " + vibrateTimers.size());

		ListView listOfVibrates = (ListView) findViewById(R.id.vibrates);
		vaa = new VibrateArrayAdapter(this, R.layout.vibrate, vibrateTimers);
		listOfVibrates.setAdapter(vaa);
		registerForContextMenu(listOfVibrates);

		/**
		 * If the list is empty, display a message
		 * !!! needs polishing
		 * @author Napon
		 */
		TextView emptyView = new TextView(this);
		((ViewGroup) listOfVibrates.getParent()).addView(emptyView);
		emptyView.setText("Click the + button to add new timers!");
		listOfVibrates.setEmptyView(emptyView);

		final Context currentActivity = this; // to pass into item click intent

		listOfVibrates.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int index,long arg3) {
				// passing the vibrate object that has been clicked
				System.out.println("clicked on " + index);
				Intent i = new Intent(currentActivity, SetTimerActivity.class);
				try {
					i.putExtra(SELECTED_TIMER, VibrateTimer.serialize(vibrateTimers.get(index)));
					startActivity(i);
				} catch (IOException e) {
					System.out.println("IOException caught in MainActivity");
				}
			}
		});
		
	}

	
//	/**
//	 * Create a pop up menu when the user long-presses an alarm entry.
//	 * Menu items: Modify, Delete
//	 * @author Napon
//	 */
//	@Override
//	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo){
//		super.onCreateContextMenu(menu, v, menuInfo);
//		if(v.getId() == R.id.vibrates){
//			menu.add("Modify");
//		    menu.add("Delete");
//		}
//	}
//	
//	/**
//	 * Clicking on 'Modify' will launch a SetTimerActivity with the selected alarm
//	 * Clicking on 'Delete' will delete the alarm from the database and update the ListView
//	 * @author Napon
//	 */
//	@Override
//	public boolean onContextItemSelected(MenuItem item) {
//		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
//		if(item.getTitle().equals("Modify")){
//			Intent i = new Intent(getApplicationContext(), SetTimerActivity.class);
//			try {
//				i.putExtra(SELECTED_TIMER, VibrateTimer.serialize(vibrateTimers.get(info.position)));
//				startActivity(i);
//				return true;
//			} catch (IOException e) {
//				System.out.println("IOException caught in onContextItemSelected in MainActivity");
//			}
//		}else {
//			controller.cancelAlarm(vibrateTimers.get(info.position), getApplicationContext());
//			vaa.clear();
//			List<VibrateTimer> lov = controller.getVibrateTimers();
//			for(VibrateTimer vt : lov){
//				vaa.add(vt);
//			}
//			vaa.notifyDataSetChanged();
//			return true;
//		}
//		return false;
//	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	/**
	 * Contextual Action Bar - only available api > 11 
	 * Edit multiple items at once
	 * @author daniel
	 */
	@TargetApi(11)
	private void setListViewMultiChoiceMode(){
		listViewVibrateTimers.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
		listViewVibrateTimers.setMultiChoiceModeListener(new MultiChoiceModeListener() {
			private List<Integer> positions; // list of selected VibrateTimer with their respective position
			@Override
			public boolean onPrepareActionMode(ActionMode mode, Menu item) {
				// TODO Auto-generated method stub
				return false;
			}
	
			@Override
			public void onDestroyActionMode(ActionMode mode) {
				// TODO Auto-generated method stub
			}
			
			@Override
			public boolean onCreateActionMode(ActionMode mode, Menu menu) {
				positions = new ArrayList<Integer>();
				MenuInflater menuInflater = mode.getMenuInflater();
				menuInflater.inflate(R.menu.multiselectmode, menu);
				mode.setTitle(listViewVibrateTimers.getCheckedItemCount() + " timer selected");
				return true;
			}
			
			@Override
			public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
				switch(item.getItemId()){
					case R.id.action_delete:
						cancelAlarm(positions);
						mode.finish();
						return true;
					default:
						return this.onActionItemClicked(mode,item);
				}
			}
			
			@Override
			public void onItemCheckedStateChanged(ActionMode mode, int position,
					long id, boolean checked) {
				if(checked) positions.add(position);
				else positions.remove(position);
				mode.setTitle(listViewVibrateTimers.getCheckedItemCount() + " timer selected");
			}
		});
		
	}
	

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle presses on the action bar items
		switch (item.getItemId()) {
		case R.id.action_addVibrate:
			addVibrateTimer();
			return true;
		case R.id.action_settings: // go to about page
			Intent i = new Intent(this, About.class);
			startActivity(i);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * add a new VibrateTimer, user will be taken to setTimerActivity page 
	 */
	public void addVibrateTimer(){
		Intent i = new Intent(this, SetTimerActivity.class);
		startActivity(i);
	}

	/**
	 * List Adapter for our custom VibrateTimer Object
	 * @author Daniel
	 *
	 */
	private class VibrateArrayAdapter extends ArrayAdapter<VibrateTimer> {
		private List<VibrateTimer> listOfVibrateTimers;
		private int resourceId; // id for a single item view 
		private Context context;

		public VibrateArrayAdapter(Context context, int customViewResourceId,
				List<VibrateTimer> vibrateTimers) {
			super(context, customViewResourceId, vibrateTimers);
			this.context = context;
			this.listOfVibrateTimers = vibrateTimers;
			resourceId = customViewResourceId;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View itemView = inflater.inflate(resourceId, parent, false); 
			// get all the info needed to show a timer on UI 
			VibrateTimer timer = listOfVibrateTimers.get(position);
			String startTime = getStartTimeInFormat(timer, "HH:mm");
			String endTime = getEndTimeInFormat(timer, "HH:mm");
			boolean[] daysOn = timer.getDays();
			String dayString = "";
			// determines which days the timer are active and log it as String 
			for(int day = 0; day < daysOn.length; day++){
				if(daysOn[day])
					dayString += days[day];
			}
			// setting the view 
			TextView timeStart = (TextView) itemView.findViewById(R.id.time_start);
			timeStart.setText(startTime);
			TextView timeEnd = (TextView) itemView.findViewById(R.id.time_end);
			timeEnd.setText(endTime);
			TextView day = (TextView) itemView.findViewById(R.id.day);
			// note: cast int position to string bcuz not doing so throws string resource not found err
			day.setText(dayString);
			return itemView;	
		}
	} 




	// ========  HELPER METHODS ==========================

	/**
	 * Convert startTime as Calendar into String with proper dateFormat
	 * @param sDateFormat
	 * @return String - startTime after applying sDateFormat
	 * @author Paul, Amelia
	 */
	public String getStartTimeInFormat (VibrateTimer vt , String sDateFormat) {
		String startTest = null;
		SimpleDateFormat sDateTest = new SimpleDateFormat(sDateFormat, Locale.getDefault());
		if (vt.getStartTime() != null) {
			startTest = sDateTest.format(vt.getStartTime().getTime());  
		}
		return startTest;

	}

	/**
	 * Convert endTime as Calendar into String with proper dateFormat
	 * @param eDateFormat
	 * @return String
	 * @author Paul, Amelia
	 */
	public String getEndTimeInFormat (VibrateTimer vt , String eDateFormat) {
		String endTest = null; 
		//SimpleDateFormat sDateTest = new SimpleDateFormat(eDateFormat, Locale.getDefault());
		SimpleDateFormat sDateTest = new SimpleDateFormat(eDateFormat, Locale.getDefault());
		if (vt.getEndTime() != null) {
			endTest = sDateTest.format(vt.getEndTime().getTime());  
		}
		return endTest;
	}
	
	
	/**
	 * @param positions VibrateTimer's position in the list 
	 * @author napon 
	 */
	
	private void cancelAlarm(List<Integer>positions){
		for(int pos: positions)
			controller.cancelAlarm(vibrateTimers.get(pos), getApplicationContext());
		vaa.clear();
		List<VibrateTimer> lov = controller.getVibrateTimers();
		for(VibrateTimer vt : lov){
			vaa.add(vt);
		}
		vaa.notifyDataSetChanged();
	}
	

} 
