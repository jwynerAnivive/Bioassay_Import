package com.anivive.process

import com.anivive.Model.Assay
import com.anivive.Model.PCAssayDescription
import com.anivive.util.xml.XMLIterator2
import com.anivive.util.xml.KotlinBuilder
import org.apache.log4j.Logger
import java.io.PrintWriter
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardOpenOption

class XML {
    //Logger please engage
    private val logger = Logger.getLogger(XML::class.java)

    //Just a small sample of what one can expect from a typical archetype-resources.src.main.kotlin.com.anivive.XML file
    //This data is very, very true to life. Treat carefully.
    companion object {
        const val exampleXML = ""
    }

    fun generateModel() {
        //Let's write it into an actual xml file
        val tempFile = Files.write(Paths.get("tempFileXML"), exampleXML.toByteArray(), StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)
        //This brings the xml structure into some static variables
        KotlinBuilder(tempFile).buildStructure("ExampleStructure")
        //This gets a string representing the kotlin code, as shown below
        val fileText = KotlinBuilder.buildFile("packageName")
        //This is how we write that kotlin code into a file
        val tempFile1 = Files.write(Paths.get("tempFileKotlin.kt"), fileText.toByteArray(), StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)
        val tempFile2 = Paths.get("exampleValues.txt")
        //This gives us a list of the variables we found, along with some examples of their values
        KotlinBuilder.writeTypesWithValuesAndFiles(tempFile2)
        //Clean up is important
        Files.delete(tempFile)
        Files.deleteIfExists(tempFile1)
        Files.delete(tempFile2)
    }

    fun nasXmlInsert(xmlList: List<String>, pmid: String) {
        xmlList.forEach { it ->
            val printWriter: PrintWriter = PrintWriter("/Users/jwyner/Desktop/Anivive/ShareDriveMount/pubmed_xmlFiles/file$pmid.XML")
            printWriter.print(it)
            printWriter.close()
        }
    }

    fun readDescription(xmlPage: String) {
        val tempFile = Files.write(Paths.get("tempDescription"), xmlPage.toByteArray(), StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)
        XMLIterator2(tempFile, PCAssayDescription::class.java).forEach {
            val id = it.pCAssayDescriptionAid!!.pCID!!.pCIDId!!.toInt()
            val name = it.pCAssayDescriptionName
            val descriptionE = it.pCAssayDescriptionDescription?.pCAssayDescriptionDescriptionE?.fold("") { a, b -> "$a,$b" }
            val commentE = it.pCAssayDescriptionComment?.pCAssayDescriptionCommentE
            val burl = it.pCAssayDescriptionXref?.pCAnnotatedXRef?.map {
                it.pCAnnotatedXRefXref?.pCXRefData?.pCXRefDataDburl!! }
            val pmid = it.pCAssayDescriptionXref?.pCAnnotatedXRef?.map {
                it.pCAnnotatedXRefXref?.pCXRefData?.pCXRefDataPmid!!.toInt() }
            val xAid = it.pCAssayDescriptionXref?.pCAnnotatedXRef?.map {
                it.pCAnnotatedXRefXref?.pCXRefData?.pCXRefDataAid!!.toInt() }
            val protein = it.pCAssayDescriptionXref?.pCAnnotatedXRef?.map {
                it.pCAnnotatedXRefXref?.pCXRefData?.pCXRefDataProteinGi!!.toInt() }
            val taxonomy = it.pCAssayDescriptionXref?.pCAnnotatedXRef?.map {
                it.pCAnnotatedXRefXref?.pCXRefData?.pCXRefDataTaxonomy!!.toInt() }
            val mmdb = it.pCAssayDescriptionXref?.pCAnnotatedXRef?.map {
                it.pCAnnotatedXRefXref?.pCXRefData?.pCXRefDataMmdbId!!.toInt() }
            val gene = it.pCAssayDescriptionXref?.pCAnnotatedXRef?.map {
                it.pCAnnotatedXRefXref?.pCXRefData?.pCXRefDataGene!!.toInt() }
            val protocol = it.PCAssayDescriptionProtocol?.pcAssayDescriptionProtocolE
            val trackingName = it.pCAssayDescriptionAidSource?.pCSource?.pCSourceDb?.pCDBTracking?.pCDBTrackingName
            val sourceId = it.pCAssayDescriptionAidSource?.pCSource?.pCSourceDb?.pCDBTracking?.pCDBTrackingSourceId?.objectId?.objectIdStr
            val version = it.pCAssayDescriptionAid?.pCID?.pCIDVersion!!.toInt()
            val outcome = it.pCAssayDescriptionDescription?.pcAssayDescriptionActivityOutcomeMethod
            val assay: Assay = Assay(id = id, name = name, descriptionE = descriptionE, commentE = commentE, burl = burl, pmid = pmid, xAid = xAid, protein = protein,
                    taxonomy = taxonomy, mmdb = mmdb, gene = gene, protocol = protocol, trackingName = trackingName, idStr = sourceId, version = version, outcome = outcome)
        }
    }

    fun readXML(xmlPage: String) {
        val tempFile = Files.write(Paths.get("tempFileXML"), xmlPage.toByteArray(), StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)
        //So, if we just want to read it straight at a nested level, we can totally do that
        XMLIterator2(tempFile, MidLevel::class.java).forEach {
            logger.info("item0: ${it.item0}")
            logger.info("item1: ${it.item1}")
        }
        //Or, if we want to read it from the top into a comprehensive object, we also have this power
        XMLIterator2(tempFile, TopLevel::class.java).forEach {
            it.midLevel?.forEach {
                logger.info("item0: ${it.item0}")
                logger.info("item1: ${it.item1}")
            }
        }
        //Clean up and stuff
        Files.delete(tempFile)
    }

    class TopLevel {
        var midLevel: List<MidLevel>? = null
    }

    class MidLevel {
        var item0: String? = null
        var item1: String? = null
    }


    //Generated model:
    /*
        @file:Suppress("unused")

        package packageName

        class ExampleStructure {
            var topLevel: archetype-resources.src.main.kotlin.com.anivive.TopLevel? = null
        }

        class archetype-resources.src.main.kotlin.com.anivive.TopLevel {
            var midLevel: List<archetype-resources.src.main.kotlin.com.anivive.MidLevel>? = null
        }

        class archetype-resources.src.main.kotlin.com.anivive.MidLevel {
            var item0: String? = null
            var item1: String? = null
        }
     */

    //Types with values:
    /*
        mid-level.item0
            tempFileXML
                Yoooo
                Words and all
        mid-level.item1
            tempFileXML
                Can you believe all the words here?
                Another item has arriiiived
     */
}