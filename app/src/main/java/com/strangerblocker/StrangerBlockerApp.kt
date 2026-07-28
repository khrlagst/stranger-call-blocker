package com.strangerblocker

import android.app.Application
import com.strangerblocker.data.AppDatabase

class StrangerBlockerApp : Application() {

    /** Lazily-initialized singleton database. Access via db. */
    val db: AppDatabase by lazy {
        AppDatabase.getInstance(this)
    }
}
