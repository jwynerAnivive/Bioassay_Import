@file:Suppress("unused")

package com.anivive.Model

class PCAssayDescription {
    var pCAssayDescriptionAid: PCAssayDescriptionAid? = null
    var pCAssayDescriptionAidSource: PCAssayDescriptionAidSource? = null
    var pCAssayDescriptionName: String? = null
    var pCAssayDescriptionDescription: PCAssayDescriptionDescription? = null
    var pCAssayDescriptionComment: PCAssayDescriptionComment? = null
    var pCAssayDescriptionXref: PCAssayDescriptionXref? = null
    var pCAssayDescriptionResults: PCAssayDescriptionResults? = null
    var pCAssayDescriptionRevision: String? = null
    var pCAssayDescriptionProjectCategory: PCAssayDescriptionProjectCategory? = null
    var PCAssayDescriptionProtocol: PCAssayDescriptionProtocol? = null
    var pCAssayDescriptionAssayGroup: PCAssayDescriptionAssayGroup? = null
    var pCAssayDescriptionCategorizedComment: PCAssayDescriptionCategorizedComment? = null
    var xmlns: String? = null
}

class PCAssayDescriptionAid {
    var pCID: PCID? = null
}

class PCID {
    var pCIDId: String? = null
    var pCIDVersion: String? = null
}

class PCAssayDescriptionAidSource {
    var pCSource: PCSource? = null
}

class PCSource {
    var pCSourceDb: PCSourceDb? = null
}

class PCSourceDb {
    var pCDBTracking: PCDBTracking? = null
}

class PCDBTracking {
    var pCDBTrackingName: String? = null
    var pCDBTrackingSourceId: PCDBTrackingSourceId? = null
}

class PCDBTrackingSourceId {
    var objectId: ObjectId? = null
}

class ObjectId {
    var objectIdStr: String? = null
}

class PCAssayDescriptionDescription {
    var pCAssayDescriptionDescriptionE: List<String>? = null
    var pcAssayDescriptionActivityOutcomeMethod: String? = null
}

class PCAssayDescriptionComment {
    var pCAssayDescriptionCommentE: List<String>? = null
}

class PCAssayDescriptionXref {
    var pCAnnotatedXRef: List<PCAnnotatedXRef>? = null
}

class PCAnnotatedXRef {
    var pCAnnotatedXRefXref: PCAnnotatedXRefXref? = null
    var pCAnnotatedXRefType: PCAnnotatedXRefType? = null
}

class PCAnnotatedXRefXref {
    var pCXRefData: PCXRefData? = null
}

class PCXRefData {
    var pCXRefDataPmid: String? = null
    var pCXRefDataDburl: String? = null
    var pCXRefDataAsurl: String? = null
    var pCXRefDataProteinGi: String? = null
    var pCXRefDataTaxonomy: String? = null
    var pCXRefDataMmdbId: String? = null
    var pCXRefDataAid: String? = null
    var pCXRefDataGene: String? = null
}

class PCAnnotatedXRefType {
    var value: String? = null
    var stringValue: String? = null
}

class PCAssayDescriptionResults {
    var pCResultType: List<PCResultType>? = null
}

class PCResultType {
    var pCResultTypeTid: String? = null
    var pCResultTypeName: String? = null
    var pCResultTypeDescription: PCResultTypeDescription? = null
    var pCResultTypeType: PCResultTypeType? = null
    var pCResultTypeSunit: String? = null
}

class PCResultTypeDescription {
    var pCResultTypeDescriptionE: String? = null
}

class PCResultTypeType {
    var value: String? = null
    var stringValue: String? = null
}

class PCAssayDescriptionProjectCategory {
    var value: String? = null
    var stringValue: String? = null
}

class PCAssayDescriptionProtocol {
    var pcAssayDescriptionProtocolE: List<String>? = null
}

class PCAssayDescriptionAssayGroup {
    var pCAssayDescriptionAssayGroupE: String? = null
}

class PCAssayDescriptionCategorizedComment {
    var pCCategorizedComment: List<PCCategorizedComment>? = null
}

class PCCategorizedComment {
    var pCCategorizedCommentTitle: String? = null
    var pCCategorizedCommentComment: PCCategorizedCommentComment? = null
}

class PCCategorizedCommentComment {
    var pCCategorizedCommentCommentE: String? = null
}

data class DescriptionObject(var id: Int? = null, var name: String? = null, var na: Long? = null, var descriptionE: String? = null, var commentE: List<String>? = null,
                             var burl: List<String>? = null, var pmid: List<Int>? = null, var xAid: List<Int>? = null, var protein: List<Int>? = null,
                             var taxonomy: List<Int>? = null, var mmdb: List<Int>? = null, var gene: List<Int>? = null, var protocol: List<String>? = null,
                             var trackingName: String? = null, var idStr: String? = null, var version: Int? = null, var outcome: String? = null)