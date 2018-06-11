package com.anivive.process

import com.anivive.Model.Assay
import com.anivive.Model.DataObject
import com.anivive.util.CSVObjectIterator
import org.apache.log4j.Logger
import org.json.JSONObject
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardOpenOption

class CSV {

    private val logger = Logger.getLogger(CSV::class.java)

    private val ec50QualifierDesc = "\'>\', \'=\',  or \'<\'"
    private val ec50Desc = "the concentration whereupon perceived activity reaches 50% of the maximum"
    private val ec50StandardErrorDesc = "the standard error for the calculated EC50 value"
    private val s0Desc = "the fitted activity level at zero concentration"
    private val sInfDesc = "the fitted activity level at infinite concentration"
    private val hillSlopeDesc = "the slope at EC50"
    private val numPointsDesc = "the number of data points included in the plot"
    private val maxActivityDesc = "the maximum activity value observed"
    private val activityAt010uMDesc = "The average measured activity of all accepted replicates at the specified concentration"
    private val activityAt019uMDesc = activityAt010uMDesc
    private val activityAt038uMDesc = activityAt010uMDesc
    private val activityAt075uMDesc = activityAt010uMDesc
    private val activityAt080uMDesc = activityAt010uMDesc
    private val activityAt150uMDesc = activityAt010uMDesc
    private val activityAt160uMDesc = activityAt010uMDesc
    private val activityAt300uMDesc = activityAt010uMDesc
    private val activityAt600uMDesc = activityAt010uMDesc
    private val activityAt1200uMDesc = activityAt010uMDesc
    private var ec50QualifierUnit = ""
    private val ec50Unit = "MICROMOLAR"
    private val ec50StandardErrorUnit = "MICROMOLAR"
    private val s0Unit = "PERCENT"
    private val sInfUnit = "PERCENT"
    private val hillSlopeUnit = "NONE"
    private val numPointsUnit = "NONE"
    private val maxActivityUnit = "PERCENT"
    private val activityAt010uMUnit = "PERCENT"
    private val activityAt019uMUnit = activityAt010uMUnit
    private val activityAt038uMUnit = activityAt010uMUnit
    private val activityAt075uMUnit = activityAt010uMUnit
    private val activityAt080uMUnit = activityAt010uMUnit
    private val activityAt150uMUnit = activityAt010uMUnit
    private val activityAt160uMUnit = activityAt010uMUnit
    private val activityAt300uMUnit = activityAt010uMUnit
    private val activityAt600uMUnit = activityAt010uMUnit
    private val activityAt1200uMUnit = activityAt010uMUnit

    fun readData(csvText: String): DataObject {
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

        val br = File(Paths.get("tempFileCSV.csv").toString()).bufferedReader()
        val csvPage = br.use { it.readText() }
        val dataObject = DataObject()
        val tempFile = Files.write(Paths.get("tempFileCSV.csv"), csvPage.toByteArray()) // Will be csvText Parameter
        CSVObjectIterator(tempFile, CSVClass::class.java).use { iterator ->
            iterator.forEach {
                println(it.pubchemResultTag)
                println(it.pubchemSid)
                println(it.pubchemCid)
                println(it.pubchemActivityOutcome)
                println(it.pubchemActivityScore)
                println(it.pubchemActivityUrl)
                println(it.pubchemAssaydataComment)
                val json = JSONObject().apply { put("property", "EC50 Qulaifier"); put("Value", it.ec50Qualifier); put("description", ec50QualifierDesc); put("unit", ec50QualifierUnit)
                    put("EC50", it.ec50);
                    put("property", "EC50 Standard Error"); put("Value", it.ec50StandardError); put("description", ec50StandardErrorDesc); put("unit", ec50Unit)
                    put("property", "s0"); put("Value", it.s0); put("description", s0Desc); put("unit", s0Unit)
                    put("property", "sInf"); put("Value", it.sInf); put("description", sInfDesc); put("unit", sInfUnit)
                    put("property", "Hill Slope"); put("Value", it.hillSlope); put("description", hillSlopeDesc); put("unit", hillSlopeUnit)
                    put("property", "Num. Points"); put("Value", it.numPoints); put("description", numPointsDesc); put("unit", numPointsUnit)
                    put("property", "Max. Activity"); put("Value", it.maxActivity); put("description", maxActivityDesc); put("unit", maxActivityUnit)
                    put("property", "Activity at 0.10uM"); put("Value", it.activityAt010uM); put("description", activityAt010uMDesc); put("unit", activityAt010uMUnit)
                    put("property", "Activity at 0.19uM"); put("Value", it.activityAt019uM); put("description", activityAt010uMDesc); put("unit", activityAt010uMUnit)
                    put("property", "Activity at 0.38uM"); put("Value", it.activityAt038uM); put("description", activityAt010uMDesc); put("unit", activityAt010uMUnit)
                    put("property", "Activity at 0.75uM"); put("Value", it.activityAt075uM); put("description", activityAt010uMDesc); put("unit", activityAt010uMUnit)
                    put("property", "Activity at 0.80uM"); put("Value", it.activityAt080uM); put("description", activityAt010uMDesc); put("unit", activityAt010uMUnit)
                    put("property", "Activity at 1.50uM"); put("Value", it.activityAt150uM); put("description", activityAt010uMDesc); put("unit", activityAt010uMUnit)
                    put("property", "Activity at 1.60uM"); put("Value", it.activityAt160uM); put("description", activityAt010uMDesc); put("unit", activityAt010uMUnit)
                    put("property", "Activity at 300uM"); put("Value", it.activityAt300uM); put("description", activityAt010uMDesc); put("unit", activityAt010uMUnit)
                    put("property", "Activity at 600uM"); put("Value", it.activityAt600uM); put("description", activityAt010uMDesc); put("unit", activityAt010uMUnit)
                    put("property", "Activity at 1200uM"); put("Value", it.activityAt1200uM); put("description", activityAt010uMDesc); put("unit", activityAt010uMUnit)
                    put("property", "s0"); put("Value", it.s0); put("description", s0Desc); put("unit", s0Unit)
                    put("Activity at 0.75uM", it.activityAt075uM)
                    put("Activity at 0.80uM", it.activityAt080uM)
                    put("Activity at 1.50uM", it.activityAt150uM)
                    put("Activity at 1.60uM", it.activityAt160uM)
                    put("Activity at 300uM", it.activityAt300uM)
                    put("Activity at 600uM", it.activityAt600uM)
                    put("Activity at 1200uM", it.activityAt1200uM); }
                dataObject.outcome = it.pubchemActivityOutcome; dataObject.score = it.pubchemActivityScore
                dataObject.url = it.pubchemActivityUrl; dataObject.comment = it.pubchemAssaydataComment; dataObject.extra = json
            }
        }
        return dataObject
    }
}