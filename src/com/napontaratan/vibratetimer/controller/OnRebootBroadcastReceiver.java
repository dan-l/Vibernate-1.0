package com.napontaratan.vibratetimer.controller;

import java.util.List;

import com.napontaratan.vibratetimer.database.VibrateTimerDB;
import com.napontaratan.vibratetimer.model.VibrateTimer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class OnRebootBroadcastReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		VibrateTimerDB datastore = new VibrateTimerDB(context);
		VibrateTimerController controller = new VibrateTimerController(context);
		System.out.println("reboot: setting back alarms");
		List<VibrateTimer> timers = datastore.getAllVibrateTimers();
		for(VibrateTimer vt : timers) {
			controller.setAlarm(vt,context);
		}
	}
}