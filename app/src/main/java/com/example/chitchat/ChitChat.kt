package com.example.chitchat

import android.os.AsyncTask
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.gson.Gson
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody


val LIKE = "like"
val DISLIKE = "dislike"

class ChitChat : Fragment(R.layout.chit_chat_fragment)  {

//main chitchat fragment
    //Contains everything
    private var client: OkHttpClient? = null
    private var swiped = false
    private var swipeRefresh: SwipeRefreshLayout? = null
    private lateinit var send: Button
    lateinit var text: TextView
    lateinit var recycler: RecyclerView
    private var adapter: Adapter = Adapter(emptyList())
    private val gson = Gson()
    private var username = "client=phillip.bell@mymail.champlain.edu"
    private var key = "key=a7dcde13-a277-452a-bdef-0875debfc984"


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //swipeRefresh, text, client, messages, and recycler setup here
        val view = inflater.inflate(R.layout.chit_chat_fragment, container, false)

        swipeRefresh = view.findViewById(R.id.Refresh) as SwipeRefreshLayout
        text = view.findViewById(R.id.yourText)
        client = OkHttpClient()

        recycler = view?.findViewById(R.id.Posts) as RecyclerView
        recycler.layoutManager = LinearLayoutManager(view.context)
        recycler.adapter = adapter

        //wire up the refresh
        swipeRefresh!!.setOnRefreshListener {
            swiped = true
            MessageProvider().execute(0)
        }
        MessageProvider().execute(0)

        //wire up the send button
        send = view.findViewById(R.id.send) as Button
        send.setOnClickListener {
            var text = text.text
            text = text.toString()
            if (text == ""){
                Toast.makeText(view.context, "You need to input something first", Toast.LENGTH_SHORT).show()
            } else {
                MessageSender().execute(text as String)
            }
        }

        return view
    }

    //handles getting https messages
    private inner class MessageProvider : AsyncTask<Int, Void?, String>(){
        protected override fun doInBackground(vararg params: Int?): String {
            val request = Request.Builder()
                .url("https://www.stepoutnyc.com/chitchat?$key&$username&skip=${params[0]}")
                .build()
            try {
                client!!.newCall(request)
                    .execute()
                    .use{ response ->
                    return response.body!!.string()
                    }

            } catch (e: Exception){
                e.printStackTrace()
                return "Error with request"
            }
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            var chat = gson.fromJson(result, Chats::class.java)
            if (swiped){
                adapter.model = chat.messages
            } else {
                adapter.model = adapter.model + chat.messages
            }
            updateUI()
        }
    }

    //Sends the messages
    private inner class MessageSender : AsyncTask<String, Void?, String>(){
        protected override fun doInBackground(vararg params: String?): String {
            val body = FormBody.Builder()
                .add("message", "test")
                .build()
            val request = Request.Builder()
                .url("https://www.stepoutnyc.com/chitchat?$key&$username&message=${params[0]}")
                .post(body)
                .build()

            try {
                client!!.newCall(request)
                    .execute()
                    return "Success"

            } catch (e: Exception){
                e.printStackTrace()
                return "Error with request"
            }
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            swiped = true
            MessageProvider().execute(0)
        }
    }

    //update the adapter
    private fun updateUI(){
        recycler.adapter = adapter
        swipeRefresh!!.isRefreshing = false
        swiped = false
    }

    //inner class for the individual chit chat views.
    private inner class SaveViewHolder(cellView: View) : RecyclerView.ViewHolder(cellView){
        private lateinit var post: Chat
        private val postText: TextView = itemView.findViewById(R.id.chat)
        private val like: Button = itemView.findViewById(R.id.like)
        private val dislike: Button = itemView.findViewById(R.id.dislike)
        private val likeText: TextView = itemView.findViewById(R.id.likeText)
        private val dislikeText: TextView = itemView.findViewById(R.id.dislikeText)

        //manages the button counter
        private var disCounter = 1
        private var likCounter = 1

        init {
            like.setOnClickListener {
                ratings().execute(LIKE)
                likeText.text = (likeText.text.toString().toInt() + 1 * likCounter).toString()
                likCounter *= -1
            }
            dislike.setOnClickListener {
                ratings().execute(DISLIKE)
                dislikeText.text = (dislikeText.text.toString().toInt() + 1 * disCounter).toString()
                disCounter *= -1
            }
        }

        //gives the individual element a post to update its contents
        fun setup(chat: Chat){
            this.post = chat
            postText.text = post.message
            likeText.text = post.likes.toString()
            dislikeText.text = post.dislikes.toString()
            postText.text = chat.message
        }

        //Responsible for sending post signals for the buttons
        private inner class ratings : AsyncTask<String, Void?, String>(){
            protected override fun doInBackground(vararg params: String?): String {
                val url = "https://www.stepoutnyc.com/chitchat/${params[0]}/${post._id}?$key&$username"

                val request = Request.Builder()
                    .url(url)
                    .build()

                try {
                    client!!.newCall(request)
                        .execute()
                        .use {
                                response -> return response.body.toString()
                        }

                } catch (e: Exception){
                    e.printStackTrace()
                    return "Error"
                }
            }

            override fun onPostExecute(result: String?) {
                super.onPostExecute(result)
            }
        }
    }

    //handles our recycler view
    private inner class Adapter(var model: List<Chat>): RecyclerView.Adapter<SaveViewHolder>(){
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SaveViewHolder {
            val inflater:LayoutInflater = LayoutInflater.from(parent.context)
            val cellView: View = inflater.inflate(R.layout.post, parent, false)
            return SaveViewHolder(cellView)
        }

        //setup view holder
        override fun onBindViewHolder(holder: SaveViewHolder, position: Int) {
            //give the element contents
            holder.setup(model[position])
            if (position == model.size-1){
                MessageProvider().execute(position)
            }
        }

        //to know when to stop making recycler elements
        override fun getItemCount(): Int {
            return model.size
        }
    }


}
