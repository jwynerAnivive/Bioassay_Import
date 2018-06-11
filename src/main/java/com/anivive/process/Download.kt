package com.anivive.process

import com.anivive.Neo.Insert
import com.anivive.util.*
import io.netty.handler.logging.LogLevel
import org.apache.commons.io.IOUtils
import org.apache.log4j.Logger
import java.io.File
import java.io.InputStream
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
    private val folder = "/pubchem/Bioassay/CSV/Data/" //"/Users/jwyner/Applications/Programs/ftpFiles/0001001_0002000"
    private val pattern = Pattern.compile("(?:US)([^-]*)-(\\d{4})[^.]*\\.")
    private val toExtractFilePattern = Pattern.compile("US[^-]*-(\\d{4})(\\d{2})(\\d{2})[.-][\\d\\w.]+")
    private val unzipToFolder = Paths.get(folder)
    private const val DOWNLOAD_PATH = "/Users/jwyner/Desktop"//Applications/Programs/ftpFiles/"//"/Users/jwyner/Documents/ftpFiles"
    private const val EXTRACTED_PATH = "/Applications/Programs/ftpFiles2/Extracted"
    private const val FTP_EMAIL = "jwyner@anivive.com"
    private val spaceDelimiter = Regex("\\s+")

    fun getUrls(type: String) {
        val urlList = mutableListOf<String>()
        val doc = WebUtil.get("ftp://ftp.ncbi.nlm.nih.gov/pubchem/Bioassay/CSV/$type/")
        val split = doc.split("\n") // line containing data for each file
        split.forEach { line ->
            val info = line.split(spaceDelimiter)
            val date = dateProcess(info)
            val url = info[8].substringBefore(".zip")
            if (Insert.checkDate(url, date)) {
                urlList.add(url)
            }
        }
        println(doc)
    }

    fun dateProcess(info: List<String>): Long {
        val month = monthSet[info[5].toLowerCase()] ?: "01"
        val day = if (info[6].length == 1) "0${info[6]}" else info[6]
        val year = info[7]
        return ("$year$month$day").toMillis()
    }

    private val monthSet = HashMap<String, String>().apply {
        put("jan", "01");put("feb", "02");put("mar", "03")
        put("apr", "04"); put("may", "05"); put("jun", "06"); put("jul", "07")
        put("aug", "08"); put("sep", "09"); put("oct", "10")
        put("nov", "11"); put("dec", "12")
    }

    fun download(urlPath: String): List<Collection<Path>> {
        //val url = URL(urlPath)
        val file: Path = unzipToFolder.resolve(DownloadUtil.getFileName(urlPath))
        val results = HashSet<Path>().map { extractZip(file) }
        return results
    }

    fun downloadFiles(folderName: String, endPath: String) {
        FTPAniClient("ftp.ncbi.nlm.nih.gov", "anonymous", FTP_EMAIL).use { client ->
            client.listFiles(folderName).filter { it.name.endsWith(".zip") }.forEach { it ->
                val downloadPath = Paths.get(DOWNLOAD_PATH, it.name)
                //tryRepeat(3) {
                    println("inside repeat")
                    client.downloadFile("$folderName${it.name}", downloadPath)
                    //DownloadUtil.extractGz(downloadPath, Paths.get(EXTRACTED_PATH, it.name.substringBefore(".zip")))
                    val e = extractZip2(Paths.get(folderName), Paths.get(endPath))
                println()
                //}
            }
        }
    }

    fun download2() {
        ZipFile(folder).use { zip ->
            zip.entries().asSequence().forEach { entry ->
                zip.getInputStream(entry).use { input ->
                    File(entry.name).outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
            }
        }
    }

    @JvmStatic
    fun extractZip2(from: Path, to: Path) {
        val buffer = ByteArray(1024)
        ZipInputStream(Files.newInputStream(from)).use { inStream ->
            Files.newOutputStream(to).use { outStream ->
                var len = inStream.read(buffer)
                while (len > 0) {
                    outStream.write(buffer, 0, len)
                    len = inStream.read(buffer)
                }
            }
        }
        logger.info("Extracted $from")
    }

    fun extractZip(file: Path): Collection<Path> {
        val resultSet = HashSet<Path>()
        ZipInputStream(Files.newInputStream(file)).use { zipInputStream ->
            var zipEntry: ZipEntry? = null
            while ({ zipEntry = zipInputStream.nextEntry; zipEntry }() != null) { //Code for extracting each thing in zip file
                logger.debug("Zip entry name: ${zipEntry!!.name}")
                //if (!zipEntry!!.isDirectory && weCareAbout(zipEntry!!.name)) {
                    //resultSet.addAll(extractFiles(zipEntry!!.name, zipInputStream))
                DownloadUtil.extractFile(zipInputStream, Paths.get("/Users/jwyner/Applications/Programs/ftpFiles/"))
                //}
            }
        }
        return resultSet
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