package com.hilbing.mybands.widget;

import android.appwidget.AppWidgetManager;
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
        return 0;
    }

    @Override
    public RemoteViews getViewAt(int i) {
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
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 0;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    private List<Event> fetchingEventList(final String currentBandIdPref){
        eventReference.child(currentBandIdPref).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                events.clear();

                for (DataSnapshot songSnapshot : dataSnapshot.getChildren()) {
                    Event event = songSnapshot.getValue(Event.class);
                    events.add(event);
                }

                Log.d("W I D G E T", events.toArray().toString());

            //    EventAdapter adapter = new EventAdapter(ListRemoteViewsFactory.this, events, currentBandIdPref);
                // songsLV.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        return events;
    }





    //RemoteViewsService.RemoteViewsFactory, SharedPreferences.OnSharedPreferenceChangeListener {

    /*private static final int count = 10;
    private List<Event> events = new ArrayList<>();
    private Context context;
    private int appWidgetId;

    private String currentBandIdPref = "oC92I2DpCdQJ25CueMGjW4UVX83310-January-202012:38";

    private DatabaseReference eventsReference;


    public ListRemoteViewsFactory(Context context){
        this.context = context;
    //    appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        currentBandIdPref = sharedPreferences.getString("currentBandIPref", "");
        fetchingEventList(currentBandIdPref);
    }




    @Override
    public void onCreate() {
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
        eventsReference = FirebaseDatabase.getInstance().getReference().child("Events");

    }

    @Override
    public void onDataSetChanged() {

    }

    @Override
    public void onDestroy() {
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public int getCount() {
        return (null == events) ? 0 : events.size();
    }

    @Override
    public RemoteViews getViewAt(int i) {
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.item_event);
        final Event event = events.get(i);
        final String eventName = event.getmName();
        remoteViews.setTextViewText(R.id.tv_event, eventName);
        return null;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        if(s.equals("currentBandIdPref")){
            currentBandIdPref = sharedPreferences.getString("currentBandIdPref", "");
            fetchingEventList(currentBandIdPref);
        }
    }

    private void fetchingEventList(final String currentBandIdPref){
        eventsReference.child(currentBandIdPref).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                events.clear();

                for (DataSnapshot songSnapshot : dataSnapshot.getChildren()) {
                    Event event = songSnapshot.getValue(Event.class);
                    events.add(event);
                }

                Log.d("W I D G E T", events.toArray().toString());

                EventAdapter adapter = new EventAdapter(ListRemoteViewsFactory.this, events, currentBandIdPref);
               // songsLV.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }*/
}
