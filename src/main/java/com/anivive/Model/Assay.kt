package com.anivive.Model

import com.anivive.util.neo.NeoNode
import com.anivive.util.neo.NeoQueryBaseBuilder
import com.anivive.util.neo.annotations.*
import org.json.JSONObject

data class DataObject(var cid: String? = null, var outcome: String? = null, var score: String? = null, var url: String? = null, var comment: String? = null, var extra: JSONObject? = null)

@NeoNodeClass(nodeLabel = "Assay", reference = "a")
data class Assay(
        @NeoSet("aid")
        var id: Int? = null,
        @NeoSet("name")
        var name: String? = null,
        @NeoSet("description")
        var descriptionE: String? = null,
        @NeoSet("comments")
        var commentE: List<String>? = null,
        @NeoSet("xref_url")
        var burl: List<String?>? = null,
        @NeoSet("xref_pmid")
        var pmid: List<Int?>? = null,
        @NeoSet("xref_aid")
        var xAid: List<Int?>? = null,
        @NeoSet("xref_protein_gi")
        var protein: List<Int?>? = null,
        @NeoSet("xref_tax_id")
        var taxonomy: List<Int?>? = null,
        @NeoSet("mmdb_id")
        var mmdb: List<Int?>? = null,
        @NeoSet("gene")
        var gene: List<Int?>? = null,
        @NeoSet("protocol")
        var protocol: List<String>? = null,
        @NeoSet("source_db")
        var trackingName: String? = null,
        @NeoSet("source_id")
        var idStr: String? = null,
        @NeoSet("version")
        var version: Int? = null,
        @NeoSet("outcome")
        var outcome: String? = null,
        @NeoProperty("outcome") // Properties come from CSV data
        var outcomeEdge: String? = null,
        @NeoProperty("activity")
        var activity: String? = null,
        @NeoProperty("url")
        var activityUrl: String? = null,
        @NeoProperty("compounds")
        var compounds: List<DataObject>? = emptyList(),
        @NeoProperty("cids")
        var cids: List<String>? = emptyList(),
        @NeoProperty("comment")
        var dataComment: String? = null) : NeoNodeModel() {
    @NeoMergeOn("aid")
    val matchName: Int?
        get() = id

    override fun massInsertLogic(builder: NeoQueryBaseBuilder): NeoQueryBaseBuilder {
        //val cid = Assay::class.getPropertyNameForField("cids")
        //val cids = this.dataObjects!!.map { it.cid }
        /*val compoundMap = HashMap<String, HashMap<String, Any?>>()
        dataObjects?.forEach { dataObj ->
            compoundMap.apply {
                put(dataObj.cid!!, HashMap<String, Any?>().apply {
                    put("outcome", dataObj.outcome); put("activity", dataObj.score); put("url", dataObj.url); put("comment", dataObj.comment); put("extra_data", dataObj.extra)
                })
            }
        }
        val outcomeEdge = this.outcomeEdge
        val activity = this.activity
        val activityUrl = this.activityUrl
        val dataComment = this.dataComment
        val pmid = this.pmid*/

        //val dataObjects = this.compounds // might not need this line?
        compounds?.forEachIndexed { i, it ->
            val compundNode = NeoNode("Compound", mapOf(Pair("cid", it.cid)), "comp$i")
            val edgeProperties = mapOf(Pair("outcome", it.outcome), Pair("activity", it.score), Pair("url", it.url),
                    Pair("comment", it.comment), Pair("extra_data", it.extra)).filter { it.value != null }
            builder.mergeNode(compundNode).mergeRelationship(neoReferenceBase, "bioassay_cid", compundNode.ref, edgeProperties)
        }

        return builder/*.manualQuery("WITH $neoReferenceBase, $neoReferenceBaseUnwind ")
                .manualQuery("UNWIND $neoReferenceBaseUnwind.$cids as id ")
                .manualQuery("MERGE (compound:Compound {cid: id}) ")
                .manualQuery("MERGE ($neoReferenceBase)-[r:bioassy_cid]->(compound) ")
                .manualQuery("SET r.outcome = '${compoundMap[]}' SET r.activity = '$activity' SET r.url = '$url' ")
                .manualQuery("SET r.comment = '$comment' SET r.extra_data = '$extraData'")*/
    }


    /*
    @NeoNodeClass(nodeLabel = "Compound", reference = "c")
data class Compound(
        @NeoSet("cid")
        var id: Int? = null,
        @NeoProperty("outcome")
        var outcome: String? = null,
        @NeoProperty("activity")
        var score: String? = null,
        @NeoProperty("url")
        var url: String? = null,
        @NeoProperty("comment")
        var comment: String? = null,
        @NeoProperty("extra_data")
        var extraData: JSONObject? = null) : NeoNodeModel() {
    @NeoMergeOn("cid")
    val cid: Int?
    get() = id

    override fun massInsertLogic(builder: NeoQueryBaseBuilder): NeoQueryBaseBuilder {
        val id = this.id
        val outcome = this.outcome
        val activity = this.score
        val url = this.url
        val comment = this.comment
        val extraData = this.extraData
        return builder
                .manualQuery("WITH $neoReferenceBase, $neoReferenceBaseUnwind ")
                .manualQuery("MATCH (assay:Assay {aid: '$id'} )")
                .manualQuery("MERGE ($neoReferenceBase)<-[r:bioassy_cid]-(assay) ")
                .manualQuery("SET r.outcome = '$outcome' SET r.activity = '$activity' SET r.url = '$url' ")
                .manualQuery("SET r.comment = '$comment' SET r.extra_data = '$extraData'")
    }
}

     */
}