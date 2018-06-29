package com.anivive.process

import com.anivive.Model.Assay
import com.anivive.Model.Compound
import com.anivive.Neo.Insert
import com.anivive.util.*
import org.apache.log4j.Logger
import java.io.*
import java.nio.file.Files
import java.nio.file.Paths
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField


object Download {
    private val logger = Logger.getLogger(Download::class.java)
    private val DOWNLOAD_PATH_DATA = StringProperty("Download_Path_Data").value!!
    private val DOWNLOAD_PATH_DESCRIPTION = StringProperty("Download_Path_Description").value!!
    private val UNZIP_PATH_DATA = StringProperty("Unzip_Path_Data").value!!
    private val UNZIP_PATH_DESCRIPTION = StringProperty("Unzip_Path_Description").value!!
    private val EXTRACTED_PATH_DATA = StringProperty("Extracted_Path_Data").value!!
    private val EXTRACTED_PATH_DESCRIPTION = StringProperty("Extracted_Path_Description").value!!
    private val FTP_EMAIL = StringProperty("FTP_EMAIL").value!!
    private val spaceDelimiter = Regex("\\s+")

    /**
     * Checks download and extraction folder paths, and created them if they do not exist
     * Connects to the FTP client using arbitrary email and password values
     * Accesses files as a list and iterates through all .zip files
     * Downloads file from ftp server to local folder and unzips it
     */

    fun downloadAndUnzipFiles(category: String, fileList: List<String>) {
        val folderName = "/pubchem/Bioassay/CSV/$category/"
        if (Files.notExists(Paths.get(DOWNLOAD_PATH_DATA))) File(DOWNLOAD_PATH_DATA).mkdir()
        if (Files.notExists(Paths.get(DOWNLOAD_PATH_DESCRIPTION))) File(DOWNLOAD_PATH_DESCRIPTION).mkdir()
        if (Files.notExists(Paths.get(UNZIP_PATH_DATA))) File(UNZIP_PATH_DATA).mkdir()
        if (Files.notExists(Paths.get(UNZIP_PATH_DESCRIPTION))) File(UNZIP_PATH_DESCRIPTION).mkdir()
        FTPAniClient("ftp.ncbi.nlm.nih.gov", "anonymous", FTP_EMAIL).use { client ->
            client.listFiles(folderName).filter { it.name.endsWith(".zip") }.forEach { file ->
                if (file.name in fileList) { // for filtering out files that have not been updated since last insert
                    val typePath = if (category == "Data") DOWNLOAD_PATH_DATA else DOWNLOAD_PATH_DESCRIPTION
                    val unzipPath = if (category == "Data") UNZIP_PATH_DATA else UNZIP_PATH_DESCRIPTION
                    val downloadPath = Paths.get(typePath, file.name)
                    client.downloadFile("$folderName${file.name}", downloadPath) // downloading files from FTP server to local folder
                    logger.debug("${file.name.substringBefore(".zip")} downloaded")
                    DownloadUtil.extractFiles("$unzipPath/", downloadPath)
                    logger.debug("${file.name.substringBefore(".zip")} unzipped")
                }
            }
        }
    }

    /**
     * Unzipped files contain compressed .gz files, which we extract here
     * Create the extraction folder if it does not already exist
     * Iterate through the directory of unzipped folders
     * For each folder iterate through all .xml or .csv files and extract them
     * Create folders to send all extracted csv and xml files
     */

    fun extractContent(category: String) {
        val unzipPath = if (category == "Data") UNZIP_PATH_DATA else UNZIP_PATH_DESCRIPTION
        val extractPath = if (category == "Data") EXTRACTED_PATH_DATA else EXTRACTED_PATH_DESCRIPTION
        if (Files.notExists(Paths.get(extractPath))) File(extractPath).mkdir()
        val dirOut = File(unzipPath)
        val directoryFiles = dirOut.listFiles()
        directoryFiles.forEach { file ->
            val directory = "$unzipPath/${file.name}/${file.name}" //not sure why, but when unzipped, there are two nested folders of the same filename
            if (Files.isDirectory(Paths.get(directory))) {
                val dir = File(directory)
                val directoryListing = dir.listFiles()
                directoryListing?.forEachIndexed { i, it ->
                    val gzDirectory = "$extractPath/${file.name}"
                    if (Files.notExists(Paths.get(gzDirectory))) {
                        File(gzDirectory).mkdirs()
                    }
                    DownloadUtil.extractGz(Paths.get("$directory/${it.name}"), Paths.get("$gzDirectory/${it.name.substringBefore(".gz")}/"))
                }
                logger.debug("folder ${file.name} extracted")
            }
        }
    }

    /**
     * The directories and filenames leading to related csv and xml files are identical, outside of the folder containing them and the extensions of the files
     * Iterate through each folder, iterating through each file inside. Each file name shows the range of assay aids contained
     * Process description (xml) file and store that as the assay object, process data (csv) file and store that as the list of compound objects
     * Compound Lists are stored with their corresponding filenames, so they can be attached to their proper assay (file names match the aid of the assay)
     * After combining the the data and description information into one assay object, the object is inserted into neo. Process and insertion is done one folder at a time
     */

    fun sendToProcess() {
        val assays = mutableListOf<Assay>()
        val compoundLists = HashMap<Int, List<Compound>>()
        val dir = File(EXTRACTED_PATH_DATA)
        val directory = dir.listFiles()
        directory.forEach {
            if (it.name != ".DS_Store") { // might not be an issue in prod?
                val fullPathDesc = "$EXTRACTED_PATH_DESCRIPTION/${it.name}"
                val fullPathData = "$EXTRACTED_PATH_DATA/${it.name}"
                val nextDir = File(fullPathData)
                val files = nextDir.listFiles()
                files.forEach { file ->
                    if (file.name != ".DS_Store") {
                        val brDesc = File(Paths.get("$fullPathData/${file.name}").toString()).bufferedReader()
                        val brData = File(Paths.get("$fullPathDesc/${file.name.substringBefore(".csv")}.descr.xml").toString()).bufferedReader()
                        val pageDesc = brDesc.use { it.readText() }
                        val pageData = brData.use { it.readText() }
                        // sets a hashmap with the aid as the key so it can be matched up with the proper description file
                        assays.add(Parse.readDescription(pageData))
                        compoundLists.apply { put(file.name.substringBefore(".csv").toInt(), Parse.readData(pageDesc)) }
                    }
                }
                assays.forEach { assay ->
                    assay.compounds = compoundLists[assay.id]
                    Insert.insert(assay)
                }
                assays.clear()
                compoundLists.clear()
            }
        }
    }

    /**
     * Data and description files have the same filenames and are updated together, so going through one list will give us the most recent dates
     * Calls the webpage, which presents the filenames and updatedAt dates line by line consistently
     * We split each line into an array and take the filename and date to return them
     */

    fun getUrls(): HashMap<String, Long> {
        val urlList = HashMap<String, Long>() // can be LinkedHashMap if we want to keep order
        val doc = WebUtil.get("ftp://ftp.ncbi.nlm.nih.gov/pubchem/Bioassay/CSV/Data/")
        val split = doc.split("\n") // line containing data for each file
        split.forEach { line ->
            if (line.isNotNullOrBlank()) {
                val info = line.split(spaceDelimiter)
                val date = dateProcess(info)
                val url = info[8]
                if (Insert.checkDate(url, date)) {
                    urlList.apply { put(url, date) }
                }
            }
        }
        return urlList
    }

    /**
     * The dates from the page are all formatted the same
     * Here we convert the date into YYYYMMDD format to pass through toMillis() to return an epoch long version of the date
     */

    private fun dateProcess(info: List<String>): Long {
        if (info.size < 7) logger.trace("SIZE ERROR: $info") // listed dates are in the same format, if the split array is not big enough, a proper date is not there
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
        } catch (e: Throwable) {
            logger.error("Invalid date passed ${e.printStackTrace()}")
            return 0L
        }
    }
}