package com.anivive

import com.anivive.Neo.Insert
import com.anivive.kafka.Queue
import com.anivive.process.Download
import com.anivive.util.initConfig
import com.anivive.util.initLog4J
import org.apache.commons.io.FileUtils
import org.apache.log4j.Logger
import java.io.File

object Main {
    private val logger = Logger.getLogger(Main::class.java)

    init {
        initLog4J("log4j.properties")
        initConfig("config.properties") //CAN ALSO GO IN ENVIRONMENT VARIABLES
    }

    @JvmStatic
    fun main(args: Array<String>) {
        try {
            if ("get_file_info" in args) {
                Queue.storeFiles()
            }
            if ("download_unzip" in args) {
                Queue.pullFiles()
            }
            if ("extract" in args) {
                Download.extractContent("Description")
                Download.extractContent("Data")
            }
            if ("insert" in args) {
                Download.sendToProcess()
            }
            if ("update" in args) {
                Queue.pullDates()
            }
            if ("delete" in args) {
                FileUtils.deleteDirectory(File("storage"))
            }
        } catch (e: Exception) {
            logger.error("Error running program with arguments ${args.reduce { a, b -> "$a $b" }}", e)
        } finally {
            Insert.closeGateway()
        }
    }
}