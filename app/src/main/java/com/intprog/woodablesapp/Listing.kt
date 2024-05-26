package com.intprog.woodablesapp

class Listing {
    var companyName: String? = null
    var jobTitle: String? = null
    var payRange: String? = null
    var details: String? = null
    var requirements1: String? = null
    var requirements2: String? = null
    var requirements3: String? = null
    var hasBenefits: String? = null
    var userId: String? = null // New field

    constructor()
    constructor(
        companyName: String?,
        jobTitle: String?,
        payRange: String?,
        details: String?,
        requirements1: String?,
        requirements2: String?,
        requirements3: String?,
        hasBenefits: String?,
        userId: String?
    ) {
        this.companyName = companyName
        this.jobTitle = jobTitle
        this.payRange = payRange
        this.details = details
        this.requirements1 = requirements1
        this.requirements2 = requirements2
        this.requirements3 = requirements3
        this.hasBenefits = hasBenefits
        this.userId = userId
    }
}