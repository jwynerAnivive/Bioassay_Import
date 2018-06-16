package com.anivive

import com.anivive.Neo.Insert
import com.anivive.process.Download
import com.anivive.process.Parse
import com.anivive.process.XML
import com.anivive.util.initConfig
import com.anivive.util.initLog4J
import com.univocity.parsers.common.processor.ColumnProcessor
import com.univocity.parsers.csv.CsvParser
import com.univocity.parsers.csv.CsvParserSettings
import org.apache.log4j.Logger
import java.io.File
import java.nio.file.Paths

object Main {
    private val logger = Logger.getLogger(Main::class.java)

    init {
        initLog4J("log4j.properties")
        initConfig("config.properties") //CAN ALSO GO IN ENVIRONMENT VARIABLES
    }

    @JvmStatic
    fun main(args: Array<String>) {
        //val files = Download.getUrls("Description")
        //files.forEach { println(it) }
        //Download.downloadAndUnzipFiles("Data")
        // can probly use list taken from kafka to use as parameter
        //Download.extractContent(listOf("0000001_0001000"), "Data")
        val br = File(Paths.get("example/XmlFiles/0000001_0001000/1.descr.xml").toString()).bufferedReader()
        val xmlPage = br.use { it.readText() }
        val br2 = File(Paths.get("example/CsvFiles/0001001_0002000/1986.csv").toString()).bufferedReader()
        val csvPage = br2.use { it.readText() }
        val csvPath = "example/CsvFiles/0001001_0002000/1986.csv"

        /*val parserSettings: CsvParserSettings = CsvParserSettings()
        parserSettings.format.setLineSeparator("\n") // maybe comma
        parserSettings.isHeaderExtractionEnabled = true

        val rowProcessor = ColumnProcessor()
        parserSettings.setProcessor(rowProcessor)

        val parser = CsvParser(parserSettings)
        val file: File = File(csvPath)
        parser.parse(file)

        val columnValues = rowProcessor.columnValuesAsMapOfNames
        println()*/

        //Parse.readData(csvPage, "1986")

        Download.sendToProcess()

        Insert.closeGateway()
        //Parse.readData(csvPage)


        /*
        get needed filenames (should be same for data and description, so only run on one?)
        Download.getUrls("Data) -> fileList

        download files from site and unzip them
        Download.downloadAndUnzipFiles("Description")
        Download.downloadAndUnzipFiles("Data")

        extract XMLs (description)
        Download.extractContent(fileList, "Description") -> assay object

        extract CSVs (Data)
        Download.extractContent(fileList, "Data") -> data object

        create one assay object: add data object to assay object created from description
        ForEach item in directory {
            val dataObjects: List<Assay> = Parse.readData(filename)
            val assayObjects: List<> = Parse.readDescription(filename)
        }
        add data object properties to assay object

        insert into neo
        massInsert()
         */
    }
}