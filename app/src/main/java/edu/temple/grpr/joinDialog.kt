package edu.temple.grpr

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import org.json.JSONObject

class joinDialog: DialogFragment() {
    internal lateinit var listener: joinInterface

    override fun onAttach(context: Context) {
        super.onAttach(context)
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            listener = context as joinInterface
        } catch (e: ClassCastException) {
            // The activity doesn't implement the interface, throw exception
            throw ClassCastException((context.toString() +
                    " must implement NoticeDialogListener"))
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let{
            val alertDialog = AlertDialog.Builder(it)
            val diaView = requireActivity().layoutInflater.inflate(R.layout.join_dialog, null)
            alertDialog.setView(diaView).setTitle("JOIN GROUP")
                .setPositiveButton(R.string.ok, DialogInterface.OnClickListener{dialog, it ->
                    val group = diaView.findViewById<TextView>(R.id.editTextGroupID).text.toString()
                    Helper.api.joinGroup(requireContext(), Helper.user.get(requireContext()), Helper.user.getSessionKey(requireContext())!!, group, object: Helper.api.Response {
                        override fun processResponse(response: JSONObject) {
                            if (Helper.api.isSuccess(response)) {
                                Helper.user.saveGroupId(requireContext(), group)
                                listener.onJoinSuccess(group)
                            } else {
                                val error = Helper.api.getErrorMessage(response)
                                listener.onJoinFailure(error)
                            }
                        }
                    })
                })
                .setNegativeButton(R.string.cancel, null)
            alertDialog.create()
        }!!
    }

    interface joinInterface{
        fun onJoinSuccess(group: String)
        fun onJoinFailure(error: String)
    }
}