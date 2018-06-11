package com.anivive.Neo

import com.anivive.Model.Assay
import com.anivive.util.StandardProperty
import com.anivive.util.neo.NeoGatewayBoltImpl
import com.anivive.util.neo.NeoNode
import com.anivive.util.neo.annotations.massInsert
import org.apache.log4j.Logger

object Insert {
    //Always make yer logger
    private val logger = Logger.getLogger(Insert::class.java)
    //Get yerself a gateway to archetype-resources.src.main.kotlin.com.anivive.Insert
    private val gateway = NeoGatewayBoltImpl(StandardProperty.NEO_SERVER.value!!, StandardProperty.NEO_USER.value!!, StandardProperty.NEO_PASSWORD.value!!)
    private val builder = gateway.builder()

    // will check filename property on BIOASSAY UpdatedAt Node and check if the date passed in is after the stored date
    fun checkDate(filename: String, date: Long): Boolean {
        val result = gateway.executeQuery(builder.manualQuery("MATCH (u:UpdatedAt{match_name: 'BIOASSAY'}) RETURN u.$filename").build())
        val previous = if (result.hasNext()) result.peek()[0].toString().replace("\"", "") else "0"
        return date > previous.toLong()
    }

    fun updateDate(filename: String, date: Long) {
        val node = NeoNode("UpdatedAt", mapOf(Pair("match_name", "BIOASSAY")), "upd")
        val builder = gateway.builder().mergeNode(node).set(node.ref, mapOf(Pair(filename, date)))
        gateway.executeQuery(builder.build())
    }

    // maybe take a list of assays and insert them with massInsert
    fun insert(assayObject: Assay) {
        val builder = listOf(assayObject).massInsert(gateway.builder())
        gateway.executeQuery(builder.build())
        logger.info("Inserted Assay ${assayObject.id}")
    }

    fun closeGateway() = gateway.close()
}