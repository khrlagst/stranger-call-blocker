package com.strangerblocker

import android.app.Application

class StrangerBlockerApp : Application() {

    /** Lazily-initialized singleton database. Access via db. */
    val db: data.AppDatabase by lazy {
        data.AppDatabase.getInstance(this)
    }
}
