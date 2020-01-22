package com.hilbing.mybands.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.hilbing.mybands.MainActivity;
import com.hilbing.mybands.R;
import com.hilbing.mybands.adapters.EventAdapter;
import com.hilbing.mybands.models.Band;
import com.hilbing.mybands.models.Event;

/**
 * Implementation of App Widget functionality.
 */
public class EventAppWidgetProvider extends AppWidgetProvider {

    public static final String TOAST_ACTION  = "com.hilbing.mybands.widget.TOAST_ACTION";
    public static final String EXTRA_ITEM = "com.hilbing.mybands.widget.EXTRA_ITEM";
    public String currentBandIdPref;

    /*public void onUpdate(Context context, AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {
        // update each of the app widgets with the remote adapter
        for (int i = 0; i < appWidgetIds.length; ++i) {

            // Set up the intent that starts the StackViewService, which will
            // provide the views for this collection.
            Intent intent = new Intent(context, ListWidgetService.class);
            // Add the app widget ID to the intent extras.
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[i]);
            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
            // Instantiate the RemoteViews object for the app widget layout.
            RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
            // Set up the RemoteViews object to use a RemoteViews adapter.
            // This adapter connects
            // to a RemoteViewsService  through the specified intent.
            // This is how you populate the data.
            rv.setRemoteAdapter(R.id.widget_listView_LV, intent);

            // The empty view is displayed when the collection has no items.
            // It should be in the same layout used to instantiate the RemoteViews
            // object above.
            rv.setEmptyView(R.id.widget_listView_LV, R.id.widget_empty_view_TV);

            //
            // Do additional processing specific to this app widget...
            //

            appWidgetManager.updateAppWidget(appWidgetIds[i], rv);
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }*/

    // Called when the BroadcastReceiver receives an Intent broadcast.
    // Checks to see whether the intent's action is TOAST_ACTION. If it is, the app widget
    // displays a Toast message for the current item.
    @Override
    public void onReceive(Context context, Intent intent) {
        AppWidgetManager mgr = AppWidgetManager.getInstance(context);
        if (intent.getAction().equals(TOAST_ACTION)) {
            int appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
            int viewIndex = intent.getIntExtra(EXTRA_ITEM, 0);
            Toast.makeText(context, "Touched view " + viewIndex, Toast.LENGTH_SHORT).show();
        }
        super.onReceive(context, intent);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // update each of the app widgets with the remote adapter
        for (int appWidgetId : appWidgetIds) {

            // Sets up the intent that points to the StackViewService that will
            // provide the views for this collection.
            Intent intent = new Intent(context, MainActivity.class);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[appWidgetId]);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

            final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            currentBandIdPref = sharedPreferences.getString("currentBandIPref", "");

            Intent serviceIntent = new Intent(context, ListWidgetService.class);
            serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            serviceIntent.setData(Uri.parse(serviceIntent.toUri(Intent.URI_INTENT_SCHEME)));
            // When intents are compared, the extras are ignored, so we need to embed the extras
            // into the data so that the extras will not be ignored.

            RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
            rv.setRemoteAdapter(R.id.widget_listView_LV, serviceIntent);

            // The empty view is displayed when the collection has no items. It should be a sibling
            // of the collection view.
            rv.setEmptyView(R.id.widget_listView_LV, R.id.widget_empty_view_TV);

            // This section makes it possible for items to have individualized behavior.
            // It does this by setting up a pending intent template. Individuals items of a collection
            // cannot set up their own pending intents. Instead, the collection as a whole sets
            // up a pending intent template, and the individual items set a fillInIntent
            // to create unique behavior on an item-by-item basis.
            Intent toastIntent = new Intent(context, EventAppWidgetProvider.class);
            // Set the action for the intent.
            // When the user touches a particular view, it will have the effect of
            // broadcasting TOAST_ACTION.
            toastIntent.setAction(EventAppWidgetProvider.TOAST_ACTION);
            toastIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[appWidgetId]);
            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
            PendingIntent toastPendingIntent = PendingIntent.getBroadcast(context, 0, toastIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            rv.setPendingIntentTemplate(R.id.widget_listView_LV, toastPendingIntent);

            appWidgetManager.updateAppWidget(appWidgetIds[appWidgetId], rv);
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }









    /*private static String currentBandIdPref = "oC92I2DpCdQJ25CueMGjW4UVX83310-January-202012:38";

    public static void updateAllWidgets(final Context context, final Event event){
       *//* final SharedPreferences preferences = getSharedPreferences("SHARED_PREFS", Context.MODE_PRIVATE);
        currentBandIdPref = preferences.getString("currentBandIdPref", "");*//*
       final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
       sharedPreferences.edit().putString("currentBandIdPref", currentBandIdPref).apply();
       final Class<EventAppWidgetProvider> widgetProviderClass = EventAppWidgetProvider.class;
       final Intent updateWidgetIntent = new Intent(context, EventAppWidgetProvider.class);
       updateWidgetIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
       final int[] appWidgetIds = AppWidgetManager.getInstance(context).getAppWidgetIds(new ComponentName(context, widgetProviderClass));
       updateWidgetIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
       context.sendBroadcast(updateWidgetIntent);

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        if(action.equals(AppWidgetManager.ACTION_APPWIDGET_UPDATE)){
            final AppWidgetManager widgetManager = AppWidgetManager.getInstance(context);
            final ComponentName componentName = new ComponentName(context, EventAppWidgetProvider.class);
            widgetManager.notifyAppWidgetViewDataChanged(widgetManager.getAppWidgetIds(componentName), R.id.widget_listView_LV);
        }
        super.onReceive(context, intent);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them

        for (int appWidgetId : appWidgetIds) {

            final RemoteViews remoteViews = getRemoteViews(context);
            if(null != currentBandIdPref){
                showEventsList(remoteViews, context, appWidgetId, currentBandIdPref);
            } else {
                showEmptyMessage(remoteViews);
            }
            appWidgetManager.updateAppWidget(appWidgetId, remoteViews);

        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    private void showEmptyMessage(RemoteViews remoteViews){
       // remoteViews.setViewVisibility(R.id.tv_widget_title, View.GONE);
        remoteViews.setViewVisibility(R.id.widget_listView_LV, View.GONE);
        remoteViews.setViewVisibility(R.id.widget_empty_view, View.VISIBLE);
    }

    private void showEventsList(RemoteViews remoteViews, Context context, int appWidgetId, Event event){
        remoteViews.setViewVisibility(R.id.widget_empty_view, View.GONE);
        remoteViews.setViewVisibility(R.id.widget_listView_LV, View.VISIBLE);
        final Intent serviceIntent = new Intent(context, ListWidgetService.class);
        serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        serviceIntent.setData(Uri.parse(serviceIntent.toUri(Intent.URI_INTENT_SCHEME)));
        remoteViews.setRemoteAdapter(R.id.widget_listView_LV, serviceIntent);
        remoteViews.setEmptyView(R.id.widget_listView_LV, R.id.widget_empty_view);

        Intent toastIntent = new Intent(context, EventAppWidgetProvider.class);
        toastIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        serviceIntent.setData(Uri.parse(serviceIntent.toUri(Intent.URI_INTENT_SCHEME)));
        PendingIntent toastPendingIntent = PendingIntent.getBroadcast(context, 0, toastIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        remoteViews.setPendingIntentTemplate(R.id.widget_listView_LV, toastPendingIntent);
    }

    private RemoteViews getRemoteViews(Context context){
        return new RemoteViews(context.getPackageName(), R.layout.widget_layout);

    }

    private Event getEvents(Context context){
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        final String currentBandId = sharedPreferences.getString("currentBandIdPref", null);
       // return (null == currentBandId) ? null :;
    }*/


}

