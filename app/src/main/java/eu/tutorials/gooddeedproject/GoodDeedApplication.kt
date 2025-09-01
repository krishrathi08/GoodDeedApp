package eu.tutorials.gooddeedproject

import android.app.Application
import com.google.firebase.FirebaseApp

class GoodDeedApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
}
