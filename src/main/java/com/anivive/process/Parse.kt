package com.anivive.process

import com.anivive.Model.Assay
import com.anivive.Model.DataObject
import com.anivive.Model.PCAssayDescription
import com.anivive.util.*
import com.anivive.util.xml.XMLIterator2
import com.univocity.parsers.common.processor.ColumnProcessor
import com.univocity.parsers.csv.CsvParser
import com.univocity.parsers.csv.CsvParserSettings
import org.apache.log4j.Logger
import org.json.JSONObject
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardOpenOption

object Parse {

    private val logger = Logger.getLogger(Parse::class.java)

    private val ec50QualifierDesc = "\'>\', \'=\',  or \'<\'"
    private val ec50Desc = "the concentration whereupon perceived activity reaches 50% of the maximum"
    private val ec50StandardErrorDesc = "the standard error for the calculated EC50 value"
    private val s0Desc = "the fitted activity level at zero concentration"
    private val sInfDesc = "the fitted activity level at infinite concentration"
    private val hillSlopeDesc = "the slope at EC50"
    private val numPointsDesc = "the number of data points included in the plot"
    private val maxActivityDesc = "the maximum activity value observed"
    private val activityAtDesc = "The average measured activity of all accepted replicates at the specified concentration"
    private var ec50QualifierUnit = ""
    private val ec50Unit = "MICROMOLAR"
    private val ec50StandardErrorUnit = "MICROMOLAR"
    private val s0Unit = "PERCENT"
    private val sInfUnit = "PERCENT"
    private val hillSlopeUnit = "NONE"
    private val numPointsUnit = "NONE"
    private val maxActivityUnit = "PERCENT"
    private val activityAtUnit = "PERCENT"

    fun readDescription(xmlPage: String): Assay {
        val tempFile = Files.write(Paths.get("tempDescription"), xmlPage.toByteArray(), StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)
        XMLIterator2(tempFile, PCAssayDescription::class.java).forEach {
            val id = it.pCAssayDescriptionAid!!.pCID!!.pCIDId!!.toInt()
            val name = it.pCAssayDescriptionName
            val descriptionE = it.pCAssayDescriptionDescription?.pCAssayDescriptionDescriptionE?.fold("") { a, b -> "$a,$b" }
            val commentE = it.pCAssayDescriptionComment?.pCAssayDescriptionCommentE
            val burl = it.pCAssayDescriptionXref?.pCAnnotatedXRef?.map {
                it.pCAnnotatedXRefXref?.pCXRefData?.pCXRefDataDburl }
            val pmid = it.pCAssayDescriptionXref?.pCAnnotatedXRef?.map {
                it.pCAnnotatedXRefXref?.pCXRefData?.pCXRefDataPmid }
            val xAid = it.pCAssayDescriptionXref?.pCAnnotatedXRef?.map {
                it.pCAnnotatedXRefXref?.pCXRefData?.pCXRefDataAid }
            val protein = it.pCAssayDescriptionXref?.pCAnnotatedXRef?.map {
                it.pCAnnotatedXRefXref?.pCXRefData?.pCXRefDataProteinGi }
            val taxonomy = it.pCAssayDescriptionXref?.pCAnnotatedXRef?.map {
                it.pCAnnotatedXRefXref?.pCXRefData?.pCXRefDataTaxonomy }
            val mmdb = it.pCAssayDescriptionXref?.pCAnnotatedXRef?.map {
                it.pCAnnotatedXRefXref?.pCXRefData?.pCXRefDataMmdbId }
            val gene = it.pCAssayDescriptionXref?.pCAnnotatedXRef?.map {
                it.pCAnnotatedXRefXref?.pCXRefData?.pCXRefDataGene }
            val protocol = it.PCAssayDescriptionProtocol?.pcAssayDescriptionProtocolE
            val trackingName = it.pCAssayDescriptionAidSource?.pCSource?.pCSourceDb?.pCDBTracking?.pCDBTrackingName
            val sourceId = it.pCAssayDescriptionAidSource?.pCSource?.pCSourceDb?.pCDBTracking?.pCDBTrackingSourceId?.objectId?.objectIdStr
            val version = it.pCAssayDescriptionAid?.pCID?.pCIDVersion?.toInt()
            val outcome = it.pCAssayDescriptionDescription?.pcAssayDescriptionActivityOutcomeMethod
            return Assay(id = id, name = name, descriptionE = descriptionE, commentE = commentE, burl = burl?.filterNot { it.isNullOrBlank() }, pmid = pmid?.filter { it.isAllDigits() }?.map { it?.toInt() }, xAid = xAid?.filter { it.isAllDigits() }?.map { it?.toInt() }, protein = protein?.filter { it.isAllDigits() }?.map { it?.toInt() },
                    taxonomy = taxonomy?.filter { it.isAllDigits() }?.map { it?.toInt() }, mmdb = mmdb?.filter { it.isAllDigits() }?.map { it?.toInt() }, gene = gene?.filter { it.isAllDigits() }?.map { it?.toInt() }, protocol = protocol, trackingName = trackingName, idStr = sourceId, version = version, outcome = outcome)
        }
        return Assay()
    }

    fun readData(csvPage: String, aid: String): List<DataObject> {
        class CSVClass {
            var pubchemResultTag: String? = null
            var pubchemSid: String? = null
            var pubchemCid: String? = null
            var pubchemActivityOutcome: String? = null
            var pubchemActivityScore: String? = null
            var pubchemActivityUrl: String? = null
            var pubchemAssaydataComment: String? = null
            var ec50Qualifier: String? = null
            var ec50StandardError: String? = null
            var ec50: String? = null
            var s0: String? = null
            var sInf: String? = null
            var hillSlope: String? = null
            var numPoints: String? = null
            var maxActivity: String? = null
            var activityAt010uM: String? = null
            var activityAt019uM: String? = null
            var activityAt038uM: String? = null
            var activityAt075uM: String? = null
            var activityAt080uM: String? = null
            var activityAt150uM: String? = null
            var activityAt160uM: String? = null
            var activityAt300uM: String? = null
            var activityAt600uM: String? = null
            var activityAt1200uM: String? = null
        }


        val returnList = mutableListOf<DataObject>()
        /*val extraObject = JSONObject()
        val extraHeaders = mutableListOf<String>()
        val extraTypes = mutableListOf<String>()
        val extraDescr = mutableListOf<String>()
        val extraUnits = mutableListOf<String>()*/

        //figure out values for extra data
        val firstRow = csvPage.substringBefore("\n")
        val firstRowSplit = firstRow.split(",")
        if (firstRowSplit.size > 400) { println("more than 400 columns"); return emptyList() }
        /*var start = false
        firstRowSplit.forEachIndexed { i, column ->
            if (start) {
                extraHeaders.add(column)
                val e = 0
                /*when (i) {
                    0 -> extraHeaders.add(column)
                    1 -> extraTypes.add(column)
                    2 -> extraDescr.add(column)
                    3 -> extraUnits.add(column)
                }*/
            }
            if (column == "PUBCHEM_ASSAYDATA_COMMENT") start = true
        }*/

        //filter out punctuation and change capitalization to match CSVObjectIterator
        /*val temps = extraHeaders.map {
            val a = it.replace(Regex("\\p{P}"), "").replace(it[0], it[0].toLowerCase())
            val chars = a.toMutableList()
            val newChars = chars.mapIndexed { i, letter ->
                if (i < chars.size - 1) {
                    if (letter.isUpperCase()) {
                        chars[i + 1] = chars[i + 1].toLowerCase()
                    }
                }
                if (i > 0) {
                    if (letter.isDigit()) {
                        chars[i - 1] = chars[i - 1].toLowerCase()
                    }
                }
            }
            chars.fold("") { a, b-> "$a$b" }
        }*/

        //val splitPage = csvPage.split("\n")
        /*splitPage.forEachIndexed { i, row ->
            val splitRow = row.split(",")
            var start = false
            splitPage.forEach { column ->
                if (column == "PUBCHEM_ASSAYDATA_COMMENT") start = true
                if (start) {
                    when (i) {
                        0 -> extraHeaders.add(column)
                        1 -> extraTypes.add(column)
                        2 -> extraDescr.add(column)
                        3 -> extraUnits.add(column)
                    }
                }
            }
            start = false
        }*/
        //val br = File(Paths.get("tempFileit.csv").toString()).bufferedReader()
        //val csvPage = br.use { it.readText() }
        val dataObject = DataObject()
        val tempFile = Files.write(Paths.get("tempFileCSV.csv"), csvPage.toByteArray(), StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING) // Will be csvText Parameter
        CSVObjectIterator(tempFile, CSVClass::class.java).use { iterator ->
            iterator.forEach {
                //val row = JsonUtil.serialize(it)
                /*println(it.pubchemResultTag)
            println(it.pubchemSid)
            println(it.pubchemCid)
            println(it.pubchemActivityOutcome)
            println(it.pubchemActivityScore)
            println(it.pubchemActivityUrl)
            println(it.pubchemAssaydataComment)*/
                //val work = it.getFieldValues()
                /*val json = JSONObject().apply { put("property", "EC50 Qualifier"); put("Value", it.ec50Qualifier); put("description", ec50QualifierDesc); put("unit", ec50QualifierUnit)
                put("EC50", it.ec50)
                put("property", "EC50 Standard Error"); put("Value", it.ec50StandardError); put("description", ec50StandardErrorDesc); put("unit", ec50StandardErrorUnit)
                put("property", "s0"); put("Value", it.s0); put("description", s0Desc); put("unit", s0Unit)
                put("property", "sInf"); put("Value", it.sInf); put("description", sInfDesc); put("unit", sInfUnit)
                put("property", "Hill Slope"); put("Value", it.hillSlope); put("description", hillSlopeDesc); put("unit", hillSlopeUnit)
                put("property", "Num. Points"); put("Value", it.numPoints); put("description", numPointsDesc); put("unit", numPointsUnit)
                put("property", "Max. Activity"); put("Value", it.maxActivity); put("description", maxActivityDesc); put("unit", maxActivityUnit)
                put("property", "Activity at 0.10uM"); put("Value", it.activityAt010uM); put("description", activityAtDesc); put("unit", activityAtUnit)
                put("property", "Activity at 0.19uM"); put("Value", it.activityAt019uM); put("description", activityAtDesc); put("unit", activityAtUnit)
                put("property", "Activity at 0.38uM"); put("Value", it.activityAt038uM); put("description", activityAtDesc); put("unit", activityAtUnit)
                put("property", "Activity at 0.75uM"); put("Value", it.activityAt075uM); put("description", activityAtDesc); put("unit", activityAtUnit)
                put("property", "Activity at 0.80uM"); put("Value", it.activityAt080uM); put("description", activityAtDesc); put("unit", activityAtUnit)
                put("property", "Activity at 1.50uM"); put("Value", it.activityAt150uM); put("description", activityAtDesc); put("unit", activityAtUnit)
                put("property", "Activity at 1.60uM"); put("Value", it.activityAt160uM); put("description", activityAtDesc); put("unit", activityAtUnit)
                put("property", "Activity at 300uM"); put("Value", it.activityAt300uM); put("description", activityAtDesc); put("unit", activityAtUnit)
                put("property", "Activity at 600uM"); put("Value", it.activityAt600uM); put("description", activityAtDesc); put("unit", activityAtUnit)
                put("property", "Activity at 1200uM"); put("Value", it.activityAt1200uM); put("description", activityAtDesc); put("unit", activityAtUnit)
                put("property", "s0"); put("Value", it.s0); put("description", s0Desc); put("unit", s0Unit)
                put("Activity at 0.75uM", it.activityAt075uM)
                put("Activity at 0.80uM", it.activityAt080uM)
                put("Activity at 1.50uM", it.activityAt150uM)
                put("Activity at 1.60uM", it.activityAt160uM)
                put("Activity at 300uM", it.activityAt300uM)
                put("Activity at 600uM", it.activityAt600uM)
                put("Activity at 1200uM", it.activityAt1200uM) }*/
                val dataObj: DataObject = DataObject(cid = it.pubchemCid, outcome = it.pubchemActivityOutcome, score = it.pubchemActivityScore, url = it.pubchemActivityUrl,
                        comment = it.pubchemAssaydataComment)
                if (it.pubchemCid != null)
                    returnList.add(dataObj)
                //dataObject.outcome = it.pubchemActivityOutcome; dataObject.score = it.pubchemActivityScore
                //dataObject.url = it.pubchemActivityUrl; dataObject.comment = it.pubchemAssaydataComment; dataObject.extra = json
            }
        }

        return returnList
    }
}