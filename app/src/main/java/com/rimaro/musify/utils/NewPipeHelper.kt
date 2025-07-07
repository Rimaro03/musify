package com.rimaro.musify.utils

import android.util.Log
import org.schabi.newpipe.extractor.NewPipe
import org.schabi.newpipe.extractor.ServiceList
import org.schabi.newpipe.extractor.stream.AudioStream
import org.schabi.newpipe.extractor.stream.StreamInfo
import javax.inject.Singleton

@Singleton
class NewPipeHelper {
    init {
        NewPipe.init(MyDownloader())
    }

    fun getVideoUrl(query: String) : String? {
        val youtube = ServiceList.YouTube
        val searchExtractor = youtube.getSearchExtractor(query)
        searchExtractor.fetchPage()

        val videoURL = searchExtractor.initialPage.items.firstOrNull()?.url
        Log.d("NewPipeHelper", "${searchExtractor.initialPage.items}")

        return videoURL
    }

    fun getAudioStream(url: String) : AudioStream? {
        val streamInfo = try {
            //get service from url
            StreamInfo.getInfo(NewPipe.getServiceByUrl(url), url)
        } catch (e: Exception) {
            null
        }
        val audioStream: AudioStream? = streamInfo?.audioStreams?.firstOrNull()

        return audioStream
    }
}