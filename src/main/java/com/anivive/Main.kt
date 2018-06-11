package com.anivive

import com.anivive.process.Download
import com.anivive.util.WebUtil
import com.anivive.util.initConfig
import com.anivive.util.initLog4J
import org.apache.commons.net.ftp.FTPClient
import org.apache.log4j.Logger
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream

object Main {
    private val logger = Logger.getLogger(Main::class.java)

    init {
        initLog4J("log4j.properties")
        initConfig("config.properties") //CAN ALSO GO IN ENVIRONMENT VARIABLES
    }

    @JvmStatic
    fun main(args: Array<String>) {

        /*val filename = "pubchem/Bioassay/CSV/Data/0001001_0002000.zip"
        val client = FTPClient()
        val os: OutputStream = FileOutputStream(filename)
        try {
            client.connect("ftp.ncbi.nlm.nih.gov")
            client.login("jwyner@anivive.com", "password")

            val status = client.retrieveFile(filename, os)
            println("status: $status")
            println("reply: ${client.reply}")
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            try {
                client.disconnect()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }*/
        //Download.downloadFiles("/pubchem/Bioassay/CSV/Data/", "/Users/jwyner/Applications/Programs/ftpFiles/0000001_0001000")//, "0000001_0001000.zip")
        //Download.downloadFiles("/Users/jwyner/Desktop/0020001_0021000.zip")
        Download.getUrls("Data")
    }
}