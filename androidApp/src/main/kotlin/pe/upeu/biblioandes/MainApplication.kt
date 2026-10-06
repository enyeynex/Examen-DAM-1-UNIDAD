package pe.upeu.biblioandes

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import pe.upeu.biblioandes.di.initKoin

/**
 * Arranca Koin una sola vez, antes que cualquier Activity. Se declara en el
 * manifiesto con android:name=".MainApplication".
 */
class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        initKoin {
            androidLogger()
            androidContext(this@MainApplication)
        }
    }
}
