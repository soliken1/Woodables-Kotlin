package com.intprog.woodablesapp

class Listing {
    @JvmField
    var companyName: String? = null
    @JvmField
    var jobTitle: String? = null
    @JvmField
    var payRange: String? = null
    @JvmField
    var details: String? = null
    @JvmField
    var requirements1: String? = null
    @JvmField
    var requirements2: String? = null
    @JvmField
    var requirements3: String? = null
    @JvmField
    var hasBenefits: String? = null // New field

    constructor()
    constructor(
        companyName: String?,
        jobTitle: String?,
        payRange: String?,
        details: String?,
        requirements1: String?,
        requirements2: String?,
        requirements3: String?,
        hasBenefits: String?
    ) {
        this.companyName = companyName
        this.jobTitle = jobTitle
        this.payRange = payRange
        this.details = details
        this.requirements1 = requirements1
        this.requirements2 = requirements2
        this.requirements3 = requirements3
        this.hasBenefits = hasBenefits // Initialize the new field
    }
}