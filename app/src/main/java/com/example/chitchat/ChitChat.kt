package com.example.chitchat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.BufferedReader
import java.io.IOException

var Username = "client=Phillip.bell@mymail.champlain.edu"
var Key = "key=a7dcde13-a277-452a-bdef-0875debfc984"

class ChitChat : Fragment(R.layout.chit_chat_fragment)  {
    lateinit var viewModel: ViewModel
    //var recycler: RecyclerView? = null
    lateinit var recycler: RecyclerView
    private var adapter: Adapter = Adapter(emptyList())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.chit_chat_fragment, container, false)
        val provider = ViewModelProvider(this)
        viewModel = provider.get(ViewModel::class.java)
        recycler = view?.findViewById(R.id.Posts) as RecyclerView
        recycler.layoutManager = LinearLayoutManager(view.context)
        recycler.adapter = adapter

        updateUI(0)
        return view
    }

    //inner class for the indivisual chit chat views.
    private inner class SaveViewHolder(cellView: View) : RecyclerView.ViewHolder(cellView){
        private lateinit var post: Chat
        private var chat: View = cellView.findViewById(R.id.ChatFrag)
        private val postText: TextView = itemView.findViewById(R.id.chat)
        private val like: Button = itemView.findViewById(R.id.like)
        private val dislike: Button = itemView.findViewById(R.id.dislike)
        private val likeText: TextView = itemView.findViewById(R.id.likeText)
        private val dislikeText: TextView = itemView.findViewById(R.id.dislikeText)

        init{
            postText.text = post.message
            likeText.text = post.likes.toString()
            dislikeText.text = post.dislikes.toString()

            //TODO wire up dis/like buttons
        }

        //gives the individual element a post to update its contents
        fun setup(chat: Chat){
            this.post = chat
            postText.text = chat.message
        }
    }

    //handles our recycler view
    private inner class Adapter(var model: List<Chat>): RecyclerView.Adapter<SaveViewHolder>(){
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SaveViewHolder {
            val inflater:LayoutInflater = LayoutInflater.from(parent.context)
            val cellView: View = inflater.inflate(R.layout.chit_chat_fragment, parent, false)
            return SaveViewHolder(cellView)
        }

        //setup view holder
        override fun onBindViewHolder(holder: SaveViewHolder, position: Int) {
            //give the element contents
            holder.setup(model[position])
            //TODO properly update ui based on if they are at the end of the list
            if (position == model.size-1){
                updateUI(position)
            }
        }

        //to know when to stop making recycler elements
        override fun getItemCount(): Int {
            return model.size
        }
    }

    //get the current messages and update the adapter
    private fun updateUI(position: Int){
        val next = viewModel.getNext(position)
        if (next != null){
            this.adapter.model = next.messages
            recycler?.adapter = adapter
        }
    }
}
