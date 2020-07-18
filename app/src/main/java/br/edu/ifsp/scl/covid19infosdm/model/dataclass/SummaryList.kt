package br.edu.ifsp.scl.covid19infosdm.model.dataclass

import com.google.gson.annotations.SerializedName

class SummaryList(
    @SerializedName("Global")
    val global: SummaryGlobal,
    @SerializedName("Countries")
    val countries: ArrayList<SummaryCountries>,
    @SerializedName("Date")
    val date: String
)