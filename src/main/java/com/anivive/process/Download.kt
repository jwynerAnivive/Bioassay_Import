package com.anivive.process

import com.anivive.Model.Assay
import com.anivive.Model.DataObject
import com.anivive.Neo.Insert
import com.anivive.util.*
import io.netty.handler.logging.LogLevel
import org.apache.commons.io.IOUtils
import org.apache.log4j.Logger
import java.io.*
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField
import java.util.HashSet
import java.util.LinkedHashSet
import java.util.regex.Pattern
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipInputStream

object Download {
    private val logger = Logger.getLogger(Download::class.java)
    private val dataFolder = "/pubchem/Bioassay/CSV/Data/"
    private val descriptionFolder = "/pubchem/Bioassay/CSV/Description/"
    private const val DOWNLOAD_PATH_Data = "example/DownloadedCsv"
    private const val DOWNLOAD_PATH_Description = "example/DownloadedXml"
    private const val UNZIP_PATH_Data = "example/UnzippedFilesCsv"
    private const val UNZIP_PATH_Description = "example/UnzippedFilesXml"
    private const val EXTRACTED_PATH_Data = "example/CsvFiles"
    private const val EXTRACTED_PATH_Description = "example/XmlFiles"
    private const val FTP_EMAIL = "jwyner@anivive.com"
    private val spaceDelimiter = Regex("\\s+")

    fun downloadAndUnzipFiles(category: String) {//}, fileList: List<String>) {
        val folderName = "/pubchem/Bioassay/CSV/$category/"
        FTPAniClient("ftp.ncbi.nlm.nih.gov", "anonymous", FTP_EMAIL).use { client ->
            client.listFiles(folderName).filter { it.name.endsWith(".zip") }.forEach { file ->
                //if (file.name in fileList) { // for filtering out files that have not been updated since last insert
                val typePath = if (category == "Data") DOWNLOAD_PATH_Data else DOWNLOAD_PATH_Description
                val unzipPath = if (category == "Data") UNZIP_PATH_Data else UNZIP_PATH_Description
                    val downloadPath = Paths.get(typePath, file.name)// may need to take .descr out of description files
                    client.downloadFile("$folderName${file.name}", downloadPath) // downloading files from FTP server to local folder
                    val fileName = file.name.substringBefore(".zip")
                    println("file downloaded")
                    DownloadUtil.extractFiles("$unzipPath/", downloadPath)
                    println("file unzipped")
                //}
            }
        }
    }

    fun extractContent(filenames: List<String>, category: String) {
        val unzipPath = if (category == "Data") UNZIP_PATH_Data else UNZIP_PATH_Description
        val extractPath = if (category == "Data") EXTRACTED_PATH_Data else EXTRACTED_PATH_Description
        filenames.forEach { filename ->
            val directory = "$unzipPath/$filename/$filename" //not sure why, but when extracted, there are two nested folders of the filename
            if (Files.notExists(Paths.get(directory)))
                File(directory).mkdir()
            val dir = File(directory)
            val directoryListing = dir.listFiles()
            directoryListing?.forEachIndexed { i, it ->
                println("count: $i")
                println(it.name)
                val csvDirectory = "$extractPath/$filename"
                if (Files.notExists(Paths.get(csvDirectory)))
                    File(csvDirectory).mkdirs()
                DownloadUtil.extractGz(Paths.get("$directory/${it.name}"), Paths.get("$csvDirectory/${it.name.substringBefore(".gz")}/"))
            }
        }
    }

    fun sendToProcess() {
        val descriptionObjects = mutableListOf<Assay>()
        val dataObjects = HashMap<Int, List<DataObject>>()

        fun helper(type: String) {
            val path = if (type == "Data") EXTRACTED_PATH_Data else EXTRACTED_PATH_Description
            if (Files.notExists(Paths.get(path)))
                File(path).mkdir()
            val dir = File(path)
            val directory = dir.listFiles()
            directory?.forEach {
                if (it.name != ".DS_Store") { // might not be an issue in prod?
                    val fullPath = "$path/${it.name}"
                    val nextDir = File(fullPath)
                    val files = nextDir.listFiles()
                    files.forEach { file ->
                        val br = File(Paths.get("$fullPath/${file.name}").toString()).bufferedReader()
                        val page = br.use { it.readText() }
                        println(file.name)
                        // sets a hashmap with the aid as the key so it can be matched up with the proper description file
                        if (type == "Data") dataObjects.apply { put(file.name.substringBefore(".csv").toInt(), Parse.readData(page, file.name.substringBefore(".csv"))) }
                        else descriptionObjects.add(Parse.readDescription(page))
                    }
                }
            }
        }

        helper("Description")
        helper("Data")

        descriptionObjects.forEach { assay ->
            assay.compounds = dataObjects[assay.id]
            Insert.insert(assay)
        }

        println("processed")

    }

    fun getUrls(type: String): List<String> {
        val urlList = mutableListOf<String>()
        val doc = WebUtil.get("ftp://ftp.ncbi.nlm.nih.gov/pubchem/Bioassay/CSV/$type/")
        val split = doc.split("\n") // line containing data for each file
        split.forEach { line ->
            if (line.isNotNullOrBlank()) {
                val info = line.split(spaceDelimiter)
                val date = dateProcess(info)
                val url = info[8].substringBefore(".zip")
                if (Insert.checkDate(url, date)) {
                    urlList.add(url)
                }
            }
        }
        Insert.closeGateway()
        return urlList
    }

    fun dateProcess(info: List<String>): Long {
        if (info.size < 7) println("SIZE ERROR: $info")
        val month = monthSet[info[5].toLowerCase()] ?: "01"
        val day = if (info[6].length == 1) "0${info[6]}" else info[6]
        var year = info[7]
        if (year.contains(":")) year = "2018"
        return ("$year$month$day").toMillis()
    }

    private val monthSet = HashMap<String, String>().apply {
        put("jan", "01");put("feb", "02");put("mar", "03")
        put("apr", "04"); put("may", "05"); put("jun", "06"); put("jul", "07")
        put("aug", "08"); put("sep", "09"); put("oct", "10")
        put("nov", "11"); put("dec", "12")
    }

    private val dtf = DateTimeFormatterBuilder().appendPattern("yyyyMMdd").parseDefaulting(ChronoField.SECOND_OF_DAY, 0).toFormatter()

    private fun String.toMillis(): Long {
        try {
            if (startsWith("0000") || this.toInt() == 0) return -1L
            val dateString = this.replace("(\\d{4})00(\\d{2})".toRegex(), "$101$2").replace("(\\d{4}\\d{2})00".toRegex(), "$101")
            return LocalDateTime.parse(dateString, dtf).toInstant(ZoneOffset.UTC).toEpochMilli()
        } catch (e: NumberFormatException) {
            com.anivive.util.logger.error("Invalid date passed ${e.printStackTrace()}")
            return 0L
        }
    }
}