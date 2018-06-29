package com.anivive.Model

import com.anivive.util.neo.NeoQueryBaseBuilder
import com.anivive.util.neo.annotations.*
import org.json.JSONObject

data class Compound(var cid: String? = null, var outcome: String? = null, var activity: String? = null, var url: String? = null, var comment: String? = null, var extra: List<JSONObject>? = null)

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
        var compounds: List<Compound>? = emptyList()) : NeoNodeModel() {
    @NeoMergeOn("aid")
    val matchName: Int?
        get() = id

    override fun <R> massInsertLogic(builder: NeoQueryBaseBuilder<R>): NeoQueryBaseBuilder<R> {
        return when {
            !compounds!!.isEmpty() -> {
                val compoundValues = this.compounds!!.map {
                    mapOf(Pair("cid", it.cid), Pair("properties", mapOf(Pair("outcome", it.outcome), Pair("comment", it.comment),
                            Pair("url", it.url), Pair("activity", it.activity), Pair("extra_json", it.extra.toString()))))
                }
                builder.manualQuery("WITH $neoReferenceBase, $neoReferenceBaseUnwind ")
                        .manualQuery("UNWIND ${builder.param(compoundValues)} as id ")
                        .manualQuery("MERGE (compound:Compound {cid: id.cid}) ")
                        .manualQuery("MERGE ($neoReferenceBase)-[r:bioassy_cid]->(compound) ")
                        .manualQuery("SET r += id.properties")
            }
            else -> builder
        }
    }
}