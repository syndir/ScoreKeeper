package com.dancingweasels.scorekeeper;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.app.Activity;
import android.app.Application;
import android.widget.Toast;

public class ToastHelper
{
	public static Context context;

	ToastHelper(Context context) { ToastHelper.context = context; }


	public static void Toast(@NonNull Application application, String message)
	{
		Toast.makeText(application.getApplicationContext(), message, Toast.LENGTH_SHORT).show();
	}

	public static void Toast(@NonNull Activity activity, String message)
	{
		Toast.makeText(activity.getApplicationContext(), message, Toast.LENGTH_SHORT).show();
	}

	public static void Toast(String message)
	{
		Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
	}
}