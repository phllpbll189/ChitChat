package com.example.chitchat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import kotlinx.coroutines.launch
import okhttp3.*
import java.io.IOException

class ViewModel : ViewModel() {
    private var client = OkHttpClient()
    private val gson = Gson()

    fun getNext(position: Int): Chats? {
        var returnVal: Chats? = null

            val url = "https://www.stepoutnyc.com/chitchat?$Key&$Username&skip=$position"
            val request = Request.Builder()
                .url(url)
                .build()


            try {
                val res: Response = client.newCall(request).execute()
                val string = res.body?.string()
                returnVal = gson.fromJson(string, Chats::class.java)!!
                } catch (e: IOException) {
            }
        return returnVal
    }

        //Then return a list of posts made from the returned json
}