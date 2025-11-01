package org.aprsdroid.app

import _root_.android.app.ListActivity
import _root_.android.content._
import _root_.android.database.Cursor
import _root_.android.os.{Bundle, Handler}
import _root_.android.util.Log
import _root_.android.view.View
import _root_.android.widget.ListView

class HubActivity extends MainListActivity("hub", R.id.hub) {
	val TAG = "APRSdroid.Hub"

	lazy val mycall = prefs.getCallSsid()
	lazy val pla = new StationListAdapter(this, prefs, mycall, mycall, StationListAdapter.NEIGHBORS)

	override def onCreate(savedInstanceState: Bundle) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.main)

		onContentViewLoaded()

		getListView().setOnCreateContextMenuListener(this);

		onStartLoading()
		setListAdapter(pla)
		getListView().setTextFilterEnabled(true)
	}

	override def onDestroy() {
		super.onDestroy()
		pla.onDestroy()
	}


	override def onListItemClick(l : ListView, v : View, position : Int, id : Long) {
		//super.onListItemClick(l, v, position, id)
		try {
			val item = getListView().getItemAtPosition(position)
			if (item == null) {
				Log.d(TAG, "Null item clicked at position: " + position)
				return
			}

			val c = item.asInstanceOf[Cursor]
			if (c == null || c.isAfterLast() || c.isBeforeFirst()) {
				Log.d(TAG, "Invalid cursor at position: " + position)
				return
			}

			val call = c.getString(StorageDatabase.Station.COLUMN_CALL)
			if (call == null || call.trim().isEmpty()) {
				Log.d(TAG, "Null or empty callsign at position: " + position)
				return
			}

			openDetails(call)
		} catch {
			case e: Exception =>
				Log.e(TAG, "Error in onListItemClick", e)
		}
	}

}
