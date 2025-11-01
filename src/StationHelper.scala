package org.aprsdroid.app

import _root_.android.content.res.Configuration
import _root_.android.view.{Menu, MenuItem}

class StationHelper(title_id : Int) extends LoadingListActivity {
	lazy val targetcall = {
		val call = getIntent().getDataString()
		if (call == null) "" else call
	}

	override def onResume() = {
		super.onResume()
		setLongTitle(title_id, targetcall)
	}

	override def onConfigurationChanged(c : Configuration) = {
		super.onConfigurationChanged(c)
		setLongTitle(title_id, targetcall)
	}

	override def onCreateOptionsMenu(menu : Menu) : Boolean = {
		getMenuInflater().inflate(R.menu.context_call, menu);
		true
	}

	override def onOptionsItemSelected(mi : MenuItem) : Boolean = {
		callsignAction(mi.getItemId, targetcall)
	}

}
