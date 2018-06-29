package com.anivive.process

import com.anivive.Model.Assay
import com.anivive.Model.Compound
import com.anivive.Model.PCAssayDescription
import com.anivive.util.*
import com.anivive.util.xml.XMLIterator2
import org.json.JSONObject
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardOpenOption

object Parse {

    /**
     * In the description folders are the XML files that contain the data to put on the assay nodes
     */

    fun readDescription(xmlPage: String): Assay {
        val tempFile = Files.write(Paths.get("tempDescription"), xmlPage.toByteArray(), StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)
        XMLIterator2(tempFile, PCAssayDescription::class.java).forEach {
            val id = it.pCAssayDescriptionAid!!.pCID!!.pCIDId!!.toInt()
            val name = it.pCAssayDescriptionName
            val descriptionE = it.pCAssayDescriptionDescription?.pCAssayDescriptionDescriptionE?.fold("") { a, b -> "$a,$b" } // often comes as array, we just want a string
            val commentE = it.pCAssayDescriptionComment?.pCAssayDescriptionCommentE
            val burl = it.pCAssayDescriptionXref?.pCAnnotatedXRef?.mapNotNull {
                it.pCAnnotatedXRefXref?.pCXRefData?.pCXRefDataDburl }
            val pmid = it.pCAssayDescriptionXref?.pCAnnotatedXRef?.mapNotNull {
                it.pCAnnotatedXRefXref?.pCXRefData?.pCXRefDataPmid }
            val xAid = it.pCAssayDescriptionXref?.pCAnnotatedXRef?.mapNotNull {
                it.pCAnnotatedXRefXref?.pCXRefData?.pCXRefDataAid }
            val protein = it.pCAssayDescriptionXref?.pCAnnotatedXRef?.mapNotNull {
                it.pCAnnotatedXRefXref?.pCXRefData?.pCXRefDataProteinGi }
            val taxonomy = it.pCAssayDescriptionXref?.pCAnnotatedXRef?.mapNotNull {
                it.pCAnnotatedXRefXref?.pCXRefData?.pCXRefDataTaxonomy }
            val mmdb = it.pCAssayDescriptionXref?.pCAnnotatedXRef?.mapNotNull {
                it.pCAnnotatedXRefXref?.pCXRefData?.pCXRefDataMmdbId }
            val gene = it.pCAssayDescriptionXref?.pCAnnotatedXRef?.mapNotNull {
                it.pCAnnotatedXRefXref?.pCXRefData?.pCXRefDataGene }
            val protocol = it.pCAssayDescriptionProtocol?.pcAssayDescriptionProtocolE
            val trackingName = it.pCAssayDescriptionAidSource?.pCSource?.pCSourceDb?.pCDBTracking?.pCDBTrackingName
            val sourceId = it.pCAssayDescriptionAidSource?.pCSource?.pCSourceDb?.pCDBTracking?.pCDBTrackingSourceId?.objectId?.objectIdStr
            val version = it.pCAssayDescriptionAid?.pCID?.pCIDVersion?.toInt()
            val outcome = it.pCAssayDescriptionDescription?.pcAssayDescriptionActivityOutcomeMethod
            Files.delete(tempFile)
            //Creating the assay the assay object that will eventually be inserted
            return Assay(id = id, name = name, descriptionE = descriptionE, commentE = commentE, burl = burl?.filterNot { it.isNullOrBlank() }.takeUnless { it!!.isEmpty() }, pmid = pmid?.filter { it.isAllDigits() }?.map { it.toInt() }.takeUnless { it!!.isEmpty() },
                    xAid = xAid?.map { it.toInt() }.takeUnless { it!!.isEmpty() }, protein = protein?.map { it.toInt() }.takeUnless { it!!.isEmpty() },
                    taxonomy = taxonomy?.map { it.toInt() }.takeUnless { it!!.isEmpty() }, mmdb = mmdb?.map { it.toInt() }.takeUnless { it!!.isEmpty() },
                    gene = gene?.map { it.toInt() }.takeUnless { it!!.isEmpty() }, protocol = protocol, trackingName = trackingName, idStr = sourceId, version = version, outcome = outcome)
        }
        Files.delete(tempFile)
        return Assay()
    }

    /**
     * Reading the csv files that contain the data about all of the compounds that the assay is related to
     * The CID is the compound id that we are connecting to the assay
     * Columns containing the outcome , activity, url, and comment will be stored as a property on the edge
     * Every column outside of the 'notExtras' set will put put in a JSON object and stored as an array of objects on the edge
     * JSONObjects contain the name of the property(column header), the description of the property, the units it is measured in, and the value
     * All descriptions are listed in one of the first few rows before the compounds are listed, and are kept track of in their own hashmaps, to be referenced when each compound is processed
     */

    fun readData(csvPage: String): List<Compound> {
        class CSVClass // declaring base class for objectIterator, even though we don't actually use it

        val returnList = mutableListOf<Compound>()
        val tempFile = Files.write(Paths.get("tempFileCSV.csv"), csvPage.toByteArray(), StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING) // Will be csvText Parameter
        CSVObjectIterator(tempFile, CSVClass::class.java, CSVUtil.defaultSettings().apply { maxColumns = 1000 }).use { iterator ->
            val rowsToRead = setOf("RESULT_DESCR", "RESULT_UNIT")
            val notExtras = setOf("PUBCHEM_RESULT_TAG", "PUBCHEM_SID", "PUBCHEM_CID", "PUBCHEM_ACTIVITY_OUTCOME", "PUBCHEM_ACTIVITY_SCORE", "PUBCHEM_ACTIVITY_URL", "PUBCHEM_ASSAYDATA_COMMENT")
            val descriptions = HashMap<String, Any?>()
            val units = HashMap<String, Any?>()
            while (iterator.hasNext()) {
                val dataObject = Compound()
                val extras = mutableListOf<JSONObject>()
                val row: MutableMap<String?, String?> = iterator.nextMap()!!
                val rowLabel = row["PUBCHEM_RESULT_TAG"]
                if (rowLabel in rowsToRead || rowLabel.isAllDigits()) {
                    row.forEach { key, value ->
                        val extraObject = JSONObject()
                        if (rowLabel == "RESULT_DESCR" && key.isNotNullOrBlank() && key !in notExtras) {
                            descriptions.apply { put(key!!, value) }
                        }
                        else if (rowLabel == "RESULT_UNIT" && key.isNotNullOrBlank()) {
                            units.apply { put(key!!, value) }
                        }
                        else {
                            if (value.isNotNullOrBlank()) {
                                when {
                                    key == "PUBCHEM_CID" -> dataObject.cid = value
                                    key == "PUBCHEM_ACTIVITY_SCORE" -> dataObject.activity = value
                                    key == "PUBCHEM_ACTIVITY_URL" -> dataObject.url = value
                                    key == "PUBCHEM_ASSAYDATA_COMMENT" -> dataObject.comment = value
                                    key == "PUBCHEM_ACTIVITY_OUTCOME" -> dataObject.outcome = value
                                    key !in notExtras && !key.isNullOrEmptyString() -> extraObject.run {
                                        put("property", key)
                                        if (!descriptions[key].isNullOrEmptyString()) put("description", descriptions[key])
                                        if (!units[key].isNullOrEmptyString()) put("unit", units[key])
                                        put("value", value)
                                    }
                                }
                            }
                        }
                        if (extraObject.length() != 0) extras.add(extraObject)
                    }
                }
                if (dataObject.cid != null) {
                    dataObject.extra = extras
                    returnList.add(dataObject)
                }
            }
        }
         return returnList
    }
}