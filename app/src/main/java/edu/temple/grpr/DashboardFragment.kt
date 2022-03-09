package edu.temple.grpr

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.media.MediaRecorder
import android.media.MediaRecorder.AudioSource.MIC
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.IOException


const val inGroupMapSize= 0.77f

class DashboardFragment : Fragment() {

    lateinit var fab: FloatingActionButton
    lateinit var map: FragmentContainerView
    lateinit var messagesView: RecyclerView
    lateinit var record: ImageButton

    private var isRecording = false
    private var recorder: MediaRecorder? = null
    private var file: File? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Let the system know that this fragment
        // wants to contribute to the app menu
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val layout =  inflater.inflate(R.layout.fragment_dashboard, container, false)

        fab = layout.findViewById(R.id.startFloatingActionButton)
        map = layout.findViewById(R.id.fragmentContainerView2)
        messagesView = layout.findViewById(R.id.messagesView)
        record = layout.findViewById(R.id.recordButton)

        // Query the server for the current Group ID (if available)
        // and use it to close the group
        fab.setOnLongClickListener {
            Helper.api.queryStatus(requireContext(),
            Helper.user.get(requireContext()),
            Helper.user.getSessionKey(requireContext())!!,
            object: Helper.api.Response {
                override fun processResponse(response: JSONObject) {
                    Helper.api.closeGroup(requireContext(),
                        Helper.user.get(requireContext()),
                        Helper.user.getSessionKey(requireContext())!!,
                        response.getString("group_id"),
                        null)
                }
            })
            true
        }

        layout.findViewById<View>(R.id.startFloatingActionButton)
            .setOnClickListener{
                (activity as DashboardInterface).createGroup()
            }

        messagesView.visibility = View.GONE
        record.visibility = View.GONE


        //set record listener
        record.setOnClickListener {
            if (isRecording){
                stopRecording()
                record.setImageResource(R.drawable.outline_mic_black_24dp)
                record.backgroundTintList  = ColorStateList.valueOf(Color.parseColor("#03DAC5"))
                isRecording = false
            } else {
                startRecording()
                record.setImageResource(R.drawable.outline_mic_off_black_24dp)
                record.backgroundTintList  = ColorStateList.valueOf(Color.parseColor("#e91e63"))
                isRecording = true
            }
        }

        return layout
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val onClick : (VoiceMessage) -> Unit = {
                vm: VoiceMessage -> val filename = vm.fileName
            (activity as DashboardInterface).play(filename)
        }

        messagesView.layoutManager = LinearLayoutManager(requireContext())
        messagesView.adapter=VoiceMessagesAdapter(
            ViewModelProvider(requireActivity()).get(VoiceMessagesViewModel::class.java).getVMs(),
            Helper.user.get(requireContext()).username,
            onClick
        )
        if((messagesView.adapter as VoiceMessagesAdapter).itemCount!=0) messagesView.smoothScrollToPosition((messagesView.adapter as VoiceMessagesAdapter).itemCount)

        // Use ViewModel to determine if we're in an active Group
        // Change FloatingActionButton behavior depending on if we're
        // currently in a group
        ViewModelProvider(requireActivity()).get(GrPrViewModel::class.java).getGroupId().observe(requireActivity()) {
            if (it.isNullOrEmpty()) {
                fab.backgroundTintList  = ColorStateList.valueOf(Color.parseColor("#03DAC5"))
                fab.setImageResource(android.R.drawable.ic_input_add)
                fab.setOnClickListener { (activity as DashboardInterface).createGroup()}
                map.updateLayoutParams<ConstraintLayout.LayoutParams> { matchConstraintPercentHeight = 1.0f }
                messagesView.visibility=View.GONE
                record.visibility=View.GONE
            } else {
                fab.backgroundTintList  = ColorStateList.valueOf(Color.parseColor("#e91e63"))
                fab.setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
                fab.setOnClickListener {(activity as DashboardInterface).endGroup()}
                map.updateLayoutParams<ConstraintLayout.LayoutParams> { matchConstraintPercentHeight = inGroupMapSize }
                messagesView.visibility=View.VISIBLE
                record.visibility=View.VISIBLE
            }

        }

        ViewModelProvider(requireActivity()).get(VoiceMessagesViewModel::class.java).getVMsToObserve().observe(requireActivity()) {
            view.apply { (messagesView.adapter as VoiceMessagesAdapter).notifyDataSetChanged() }
            if((messagesView.adapter as VoiceMessagesAdapter).itemCount!=0) messagesView.smoothScrollToPosition((messagesView.adapter as VoiceMessagesAdapter).itemCount)

        }



    }

    // This fragment places a menu item in the app bar
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.dashboard, menu)
        menu.findItem(R.id.action_join_group).isVisible = Helper.user.getGroupId(requireContext()).isNullOrBlank()
        menu.findItem(R.id.action_leave_group).isVisible = !Helper.user.getGroupId(requireContext()).isNullOrBlank()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.action_logout -> {
                (activity as DashboardInterface).logout()
                return true
            }
            R.id.action_join_group ->
            {
                (activity as DashboardInterface).joinGroup()
                return true
            }
            R.id.action_leave_group -> {
                (activity as DashboardInterface).leaveGroup()
                return true
            }
        }
        return false
    }


    fun startRecording(){
        val group = Helper.user.getGroupId(requireContext())
        val user = Helper.user.get(requireContext()).username
        val time = System.currentTimeMillis()

        val filepath = time.toString() + "_" + user + ".3gp"
        file = File(activity?.getDir(group, Context.MODE_PRIVATE), filepath)
        val vm = VoiceMessage(user, time, filepath)

        //add to VM
        ViewModelProvider(requireActivity()).get(VoiceMessagesViewModel::class.java).addVM(vm)

        this.recorder = MediaRecorder().apply {
            setAudioSource(MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setOutputFile(file)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)

            try {
                prepare()
            } catch (e: IOException) {
                Log.d("ERROR", "startRecording prepare() failed")
            }
            start()
        }

    }

    fun stopRecording(){
        recorder?.apply {
            stop()
            reset()
            release()
        }
        recorder = null
    }

    override fun onStop() {
        super.onStop()
        recorder?.release()
        recorder = null
    }

    interface DashboardInterface {
        fun createGroup()
        fun endGroup()
        fun joinGroup()
        fun leaveGroup()
        fun logout()

        fun play(filename: String)
    }

}