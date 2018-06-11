package com.anivive.process

import com.anivive.util.WebUtil
import org.apache.log4j.Logger

class Json {
    //Guess what this is? It's a logger. We should log that somewhere.
    private val logger = Logger.getLogger(Json::class.java)

    //This is the model for our deserializationing
    data class TestData(var userId: Int, var id: Int, var title: String, var body: String)

    fun readJson() {
        //We're gonna hit our lovely test API that we found online, and deserialize it into a TestData object all in one go!
        val testData = WebUtil.get<TestData>("https://jsonplaceholder.typicode.com/posts/1")
        logger.info(testData)
    }
}