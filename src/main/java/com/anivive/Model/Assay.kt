package com.anivive.Model

import com.anivive.util.neo.NeoQueryBaseBuilder
import com.anivive.util.neo.annotations.*
import org.json.JSONObject

data class DataObject(var outcome: String? = null, var score: String? = null, var url: String? = null, var comment: String? = null, var extra: JSONObject? = null)

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
        var burl: List<String>? = null,
        @NeoSet("xref_pmid")
        var pmid: List<Int>? = null,
        @NeoSet("xref_aid")
        var xAid: List<Int>? = null,
        @NeoSet("xref_protein_gi")
        var protein: List<Int>? = null,
        @NeoSet("xref_tax_id")
        var taxonomy: List<Int>? = null,
        @NeoSet("mmdb_id")
        var mmdb: List<Int>? = null,
        @NeoSet("gene")
        var gene: List<Int>? = null,
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
        @NeoProperty("comment")
        var dataComment: String? = null) : NeoNodeModel() {
    @NeoMergeOn("aid")
    val matchName: Int?
        get() = id

    override fun massInsertLogic(builder: NeoQueryBaseBuilder): NeoQueryBaseBuilder {
        val outcomeEdge = this.outcomeEdge
        val activity = this.activity
        val activityUrl = this.activityUrl
        val dataComment = this.dataComment
        val pmid = this.pmid
        //return builder.manualQuery("WITH $neoReferenceBase")

        return super.massInsertLogic(builder)
    }
}