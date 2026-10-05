package dev.flondy.checkyourlove

import android.app.Activity
import android.app.Application
import android.os.Bundle
import java.lang.ref.WeakReference

/** Merkt sich die sichtbare Activity für SDKs, die eine Activity brauchen (Kauf, Vollbildwerbung). */
object CurrentActivity : Application.ActivityLifecycleCallbacks {
    private var ref: WeakReference<Activity>? = null

    fun get(): Activity? = ref?.get()?.takeUnless { it.isFinishing || it.isDestroyed }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) { ref = WeakReference(activity) }
    override fun onActivityStarted(activity: Activity) { ref = WeakReference(activity) }
    override fun onActivityResumed(activity: Activity) { ref = WeakReference(activity) }
    override fun onActivityPaused(activity: Activity) = Unit
    override fun onActivityStopped(activity: Activity) = Unit
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
    override fun onActivityDestroyed(activity: Activity) { if (ref?.get() === activity) ref = null }
}
