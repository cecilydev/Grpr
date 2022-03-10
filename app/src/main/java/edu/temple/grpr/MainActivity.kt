package edu.temple.grpr

import android.Manifest
import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.*
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.*
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import com.google.android.gms.maps.model.LatLng
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedInputStream
import java.io.File
import java.io.IOException
import java.net.URL
import java.util.*

class MainActivity : AppCompatActivity(), DashboardFragment.DashboardInterface, GroupFragment.GroupControlInterface {

    private var serviceIntent: Intent? = null
    private val grprViewModel : GrPrViewModel by lazy {
        ViewModelProvider(this).get(GrPrViewModel::class.java)
    }

    private val messagesViewModel: VoiceMessagesViewModel by lazy{
        ViewModelProvider(this).get(VoiceMessagesViewModel::class.java)
    }

    var audioQueue: Queue<String> = LinkedList<String>()
    private var player: MediaPlayer? = null

    // Update ViewModel with location data whenever received from LocationService
    private var locationHandler = object : Handler(Looper.myLooper()!!) {
        override fun handleMessage(msg: Message) {
            grprViewModel.setLocation(msg.obj as LatLng)
        }
    }

    // Receiver to get data of group participants from FCM
    private val groupBroadcastReceiver = object: BroadcastReceiver () {
        override fun onReceive(p0: Context?, p1: Intent?) {
            val participantArray = JSONArray(p1!!.getStringExtra(FCMService.UPDATE_KEY))
            val group = Group()
            var participantObject: JSONObject
            for (i in 0 until participantArray.length()) {
                participantObject = participantArray.getJSONObject(i)
                group.addParticipant(
                    Participant(
                        participantObject.getString("username"),
                        LatLng(
                            participantObject.getDouble("latitude"),
                            participantObject.getDouble("longitude")
                        )
                    )
                )
            }

            grprViewModel.setGroup(group)
        }
    }

    private val messageBroadcastReceiver = object: BroadcastReceiver (){
        override fun onReceive(p0: Context?, p1: Intent?) {
            val messageDetail = JSONObject(p1?.getStringExtra(FCMService.MESSAGE_KEY)!!)
            val group = Helper.user.getGroupId(this@MainActivity)
            val user = messageDetail.getString("username")
            val time = System.currentTimeMillis()

            val filepath = time.toString() + "_" + user + ".3gp"
            val file = File(getDir(group, MODE_PRIVATE), filepath)

            //download
            DownloadAudio(this@MainActivity, audioQueue).execute(messageDetail.getString("message_url"), file.absolutePath)

            //update view model
            messagesViewModel.addVM(VoiceMessage(user, time, filepath))
        }

    }

    private var serviceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(componentName: ComponentName, iBinder: IBinder) {

            // Provide service with handler
            (iBinder as LocationService.LocationBinder).setHandler(locationHandler)
        }

        override fun onServiceDisconnected(componentName: ComponentName) {}
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        createNotificationChannel()
        serviceIntent = Intent(this, LocationService::class.java)

        grprViewModel.getGroupId().observe(this) {
            if (!it.isNullOrEmpty())
                supportActionBar?.title = "GRPR - $it"
            else
                supportActionBar?.title = "GRPR"
        }

        Helper.user.getGroupId(this)?.run {
            grprViewModel.setGroupId(this)
            startLocationService()
        }

        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED  &&
            checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.RECORD_AUDIO
                ), 1
            )
        }

    }

    override fun onResume() {
        super.onResume()
        registerReceiver(groupBroadcastReceiver, IntentFilter(FCMService.UPDATE_ACTION))
        registerReceiver(messageBroadcastReceiver, IntentFilter(FCMService.UPDATE_MESSAGE))
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(groupBroadcastReceiver)
        unregisterReceiver(messageBroadcastReceiver)
    }

    private fun createNotificationChannel() {
        val channel =
            NotificationChannel("default", "Active Convoy", NotificationManager.IMPORTANCE_HIGH)
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    override fun createGroup() {
        Helper.api.createGroup(this, Helper.user.get(this), Helper.user.getSessionKey(this)!!, object: Helper.api.Response {
            override fun processResponse(response: JSONObject) {
                if (Helper.api.isSuccess(response)) {
                    grprViewModel.setGroupId(response.getString("group_id"))
                    Helper.user.saveGroupId(this@MainActivity, grprViewModel.getGroupId().value!!)
                    startLocationService()
                } else {
                    Toast.makeText(this@MainActivity, Helper.api.getErrorMessage(response), Toast.LENGTH_SHORT).show()
                }
            }

        })
    }

    override fun endGroup() {
        AlertDialog.Builder(this).setTitle("Close Group")
            .setMessage("Are you sure you want to close the group?")
            .setPositiveButton("Yes"
            ) { _, _ -> Helper.api.closeGroup(
                this,
                Helper.user.get(this),
                Helper.user.getSessionKey(this)!!,
                grprViewModel.getGroupId().value!!,
                object: Helper.api.Response {
                    override fun processResponse(response: JSONObject) {
                        if (Helper.api.isSuccess(response)) {
                            grprViewModel.setGroupId("")
                            Helper.user.clearGroupId(this@MainActivity)
                            stopLocationService()
                        } else
                            Toast.makeText(this@MainActivity, Helper.api.getErrorMessage(response), Toast.LENGTH_SHORT).show()
                    }

                }
            )}
            .setNegativeButton("Cancel") { p0, _ -> p0.cancel() }
            .show()
    }

    override fun joinGroup() {
        Navigation.findNavController(findViewById(R.id.fragmentContainerView))
            .navigate(R.id.action_dashboardFragment_to_groupFragment, Bundle().apply {
                putBoolean("JOIN_ACTION", true)
            })
    }

    override fun leaveGroup() {
        Navigation.findNavController(findViewById(R.id.fragmentContainerView))
            .navigate(R.id.action_dashboardFragment_to_groupFragment, Bundle().apply {
                putBoolean("JOIN_ACTION", false)
            })
    }

    override fun logout() {
        Helper.user.clearSessionData(this)
        Navigation.findNavController(findViewById(R.id.fragmentContainerView))
            .navigate(R.id.action_dashboardFragment_to_loginFragment)
    }

    override fun play(filename: String) {
        Log.d("filename", filename)
        audioQueue.add(filename)
        if (audioQueue.size==1){
            //play audio
            startPlaying(filename)
        }

    }

    private fun startLocationService() {
        bindService(serviceIntent, serviceConnection, BIND_AUTO_CREATE)
        startService(serviceIntent)
    }

    private fun stopLocationService() {
        unbindService(serviceConnection)
        stopService(serviceIntent)
    }

    override fun joinGroupFlow(groupId: String) {
        Helper.api.joinGroup(
            this,
            Helper.user.get(this),
            Helper.user.getSessionKey(this)!!,
            groupId,
            object: Helper.api.Response {
                override fun processResponse(response: JSONObject) {
                    Helper.user.saveGroupId(this@MainActivity, groupId)
                    startLocationService()
                    // Refresh action bar menu items
                    invalidateOptionsMenu()
                }

            }
        )
    }

    override fun leaveGroupFlow(groupId: String) {
        Helper.api.leaveGroup(
            this,
            Helper.user.get(this),
            Helper.user.getSessionKey(this)!!,
            Helper.user.getGroupId(this)!!,
            object: Helper.api.Response {
                override fun processResponse(response: JSONObject) {
                    Helper.user.clearGroupId(this@MainActivity)
                    stopLocationService()

                    // Refresh action bar menu items
                    invalidateOptionsMenu()
                }

            }
        )
    }

    private fun startPlaying(filename:String) {
        val path = getDir(Helper.user.getGroupId(this), MODE_PRIVATE).toString() + "/" + filename
        player = MediaPlayer().apply {
            try {
                setDataSource(path)
                prepare()
                start()
            } catch (e: IOException) {
                Log.d("ERROR", "prepare() failed")
            }
        }
        player?.setOnCompletionListener {
            audioQueue.remove()
            if (audioQueue.size!=0){
                startPlaying(audioQueue.element())
            }
        }
    }


    // CLASS BELOW TAKEN FROM STACKOVERFLOW: https://stackoverflow.com/questions/58142655/kotlin-how-to-download-mp3-file-and-save-to-internal-storage
    class DownloadAudio(val context: Context, val audio:Queue<String>): AsyncTask<String, String, String>() {
        override fun doInBackground(vararg p0: String?): String {
            val url  = URL(p0[0])
            val connection = url.openConnection()
            connection.connect()
            val inputStream = BufferedInputStream(url.openStream())
            val outputStream = context.openFileOutput(p0[1], Context.MODE_PRIVATE)
            val data = ByteArray(1024)
            var count = inputStream.read(data)
            var total = count
            while (count != -1) {
                outputStream.write(data, 0, count)
                count = inputStream.read(data)
                total += count
            }
            outputStream.flush()
            outputStream.close()
            inputStream.close()
            Log.d("download", "success")

            //upon success, add to queue
            audio.add(p0[1])
            return "Success"
        }
    }
}

