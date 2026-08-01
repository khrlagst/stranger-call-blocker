package com.strangerblocker.service

import android.telecom.Call
import android.telecom.CallScreeningService
import com.strangerblocker.StrangerBlockerApp
import com.strangerblocker.engine.CallDecision

/**
 * Thin adapter: all screening logic lives in [com.strangerblocker.engine.SbEngine].
 */
class CallBlockerService : CallScreeningService() {

    override fun onScreenCall(details: Call.Details) {
        val number = details.handle?.schemeSpecificPart
        val engine = (applicationContext as StrangerBlockerApp).engine

        val decision = engine.shouldBlockCall(number)
        if (decision == CallDecision.ALLOW) {
            respondToCall(details, CallScreeningService.CallResponse.Builder().build())
            return
        }

        respondToCall(
            details,
            CallScreeningService.CallResponse.Builder()
                .setDisallowCall(true)
                .setRejectCall(true)
                .setSkipCallLog(false)
                .setSkipNotification(false)
                .build(),
        )
        if (number != null && decision != CallDecision.BLOCK_PRIVATE) {
            engine.recordBlockedCall(number)
        }
    }
}
