package com.example.cauliflower.ready2walk.UI

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.example.cauliflower.ready2walk.Database.Sessions
import com.example.cauliflower.ready2walk.R
import com.example.cauliflower.ready2walk.UI.FilesFragmentDirections
import kotlinx.android.synthetic.main.session_layout.view.*

class SessionAdapter(private val listSessions : List<Sessions>) : RecyclerView.Adapter<SessionAdapter.SessionViewHolder>() {
    class SessionViewHolder(val view: View):RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SessionViewHolder {
        return SessionViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.session_layout, parent, false)
        )
    }

    // function to get number of sessions
    override fun getItemCount()= listSessions.size

    // function to populate session layout
    override fun onBindViewHolder(holder: SessionViewHolder, position: Int) {
        holder.view.text_view_title.text = "Session Date:"
        holder.view.text_view_note.text = listSessions[position].id.toString()

        holder.view.setOnClickListener{
            // button action to move into the session summary
            val action = FilesFragmentDirections.actionFilesFragmentToSessionView()
            action.sessions = listSessions[position]
            Navigation.findNavController(it).navigate(action)
        }
    }
}