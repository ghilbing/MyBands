package com.hilbing.mybands.widget;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import androidx.annotation.NonNull;

import com.google.android.gms.common.util.CollectionUtils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hilbing.mybands.R;
import com.hilbing.mybands.SongActivity;
import com.hilbing.mybands.adapters.EventAdapter;
import com.hilbing.mybands.adapters.SongAdapter;
import com.hilbing.mybands.models.Event;
import com.hilbing.mybands.models.Song;

import java.util.ArrayList;
import java.util.List;

public class ListRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory{

    private static final int count = 10;
    private List<Event> events = new ArrayList<>();
    private Context context;
    private int appWidgetId;
    private String currentBandIdPref;

    private DatabaseReference eventReference;

    ListRemoteViewsFactory(Context context, Intent intent){
        this.context = context;
        appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
    }


    @Override
    public void onCreate() {
        // In onCreate() you setup any connections / cursors to your data source. Heavy lifting,
        // for example downloading or creating content etc, should be deferred to onDataSetChanged()
        // or getViewAt(). Taking more than 20 seconds in this call will result in an ANR.

        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        currentBandIdPref = sharedPreferences.getString("currentBandIPref", "");
        eventReference = FirebaseDatabase.getInstance().getReference().child("Events");
        eventReference.keepSynced(true);

        fetchingEventList(currentBandIdPref);

    }

    @Override
    public void onDataSetChanged() {

    }

    @Override
    public void onDestroy() {

    }

    @Override
    public int getCount() {
        return events.size();
    }

    @Override
    public RemoteViews getViewAt(int i) {

        if(CollectionUtils.isEmpty(events)){
            return null;
        }
        // Construct a remote views item based on the app widget item XML file,
        // and set the text based on the position.
        RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.item_event);
        rv.setTextViewText(R.id.widget_item_event_TV, events.get(i).getmName());

        // Next, set a fill-intent, which will be used to fill in the pending intent template
        // that is set on the collection view in StackWidgetProvider.
        Bundle extras = new Bundle();
        extras.putInt(EventAppWidgetProvider.EXTRA_ITEM, i);
        Intent fillInIntent = new Intent();
        fillInIntent.putExtras(extras);
        // Make it possible to distinguish the individual on-click
        // action of a given item
        rv.setOnClickFillInIntent(R.id.widget_item_event_TV, fillInIntent);

        // Return the remote views object.
        return rv;
    }

    @Override
    public RemoteViews getLoadingView() {
        return new RemoteViews(context.getPackageName(), R.layout.item_event);
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public boolean hasStableIds() {
        //for more eficiency
        return true;
    }

    private List<Event> fetchingEventList(final String currentBandIdPref){
        eventReference.child(currentBandIdPref).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                events.clear();

                for (DataSnapshot eventSnapshot : dataSnapshot.getChildren()) {
                    Event event = eventSnapshot.getValue(Event.class);
                    events.add(event);
                }
                Log.d("W I D G E T", Integer.toString(events.size()));
                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
                ComponentName widget = new ComponentName(context, EventAppWidgetProvider.class);
                appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetManager.getAppWidgetIds(widget), R.id.widget_listView_LV);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        return events;

    }

}
