package com.anivive.Neo

import com.anivive.Model.Assay
import com.anivive.util.StandardProperty
import com.anivive.util.neo.NeoGatewayBoltImpl
import com.anivive.util.neo.annotations.massInsert
import com.google.common.collect.Lists
import org.apache.log4j.Logger

object Insert {

    private val logger = Logger.getLogger(Insert::class.java)

    private val gateway = NeoGatewayBoltImpl(StandardProperty.NEO_SERVER.value!!, StandardProperty.NEO_USER.value!!, StandardProperty.NEO_PASSWORD.value!!)
    private val builder = gateway.builder()

    /**
     * will check filename property on BIOASSAY UpdatedAt Node and check if the date passed in is after the stored date
     * setting properties with preceding 'f' because neo properties cannot start with a number
    */
    fun checkDate(filename: String, date: Long): Boolean {
        builder.clear()
        val result = gateway.executeQuery(builder.manualQuery("MATCH (u:UpdatedAt{match_name: 'BIOASSAY'}) RETURN u.f${filename.substringBefore(".zip")}").build())
        var previous = if (result.hasNext()) result.peek()[0].toString().replace("\"", "") else "0"
        if (previous == "NULL") previous = "0"
        logger.info("$filename updated = ${date > previous.toLong()}")
        return date > previous.toLong()
    }

    /**
     * items are given in strings in the form of <filename>*<date>
     * dates are stored on an UpdatedAt node under match_name 'BIOASSAY'
     * each file has it's own property - f<filename>: <date> (epoch long)
     */

    fun updateDate(items: List<String>) { // probably should integrate unwind :|
        val fileMap = items.map {
            mapOf(Pair("f${it.substringBefore(".zip")}", it.substringAfter("*").toLong()))
        }
        builder.manualQuery("MERGE (u:UpdatedAt {match_name: 'BIOASSAY'}) WITH u")
                .manualQuery("UNWIND ${builder.param(fileMap)} as map ")
                .manualQuery("SET u += map")

        gateway.executeQuery(builder.build())
        builder.clear()
        logger.info("dates updated: ${items.map { it.substringBefore("*") }}")
    }

    /**
     * Some assays have hundreds of thousands of compounds to merge on, which is too much for one insertion
     * if the assay has more than 100,000 compounds then the compounds are merged on in separate queries of 10,000 compounds
     * compounds values are on assay object as data class. They are mapped out from the data class so that they can be unwound
     * compounds are merged on their CID, with all additional information stored on the edge
     * if there are less than 100,000 compounds, the entire insertion is done in one query using massInsert()
     */

    fun insert(assay: Assay) {
        if (assay.compounds!!.size > 10000) {
            // splitting the list into smaller lists, compunds will be merged in chunks of 10,000
            val splitList = Lists.partition(assay.compounds!!, 10000)
            assay.compounds = emptyList()
            val builder = listOf(assay).massInsert(gateway.builder())
            builder.manualQuery("WITH ${assay.id} as ID")
            splitList.forEach { compoundList ->
                val compoundValues = compoundList.map {
                    mapOf(Pair("cid", it.cid), Pair("properties", mapOf(Pair("outcome", it.outcome), Pair("comment", it.comment),
                            Pair("url", it.url), Pair("activity", it.activity), Pair("extra_json", it.extra.toString()))))
                }
                builder.manualQuery("MATCH (assay:Assay {aid: ${builder.param(assay.id!!)}}) ")
                        .manualQuery("UNWIND ${builder.param(compoundValues)} as id ")
                        .manualQuery("MERGE (compound:Compound {cid: id.cid}) ")
                        .manualQuery("MERGE (assay)-[r:bioassy_cid]->(compound) ")
                        .manualQuery("SET r += id.properties")
                gateway.executeQuery(builder.build())
                builder.clear()
            }

        } else {
            val builder = listOf(assay).massInsert(gateway.builder())
            gateway.executeQuery(builder.build())
        }
        logger.info("Inserted Assay ${assay.id}")
    }

    fun closeGateway() = gateway.close()
}