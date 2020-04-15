package br.edu.ifsp.scl.covid19infosdm.model.dataclass

import com.google.gson.annotations.SerializedName

/* Omiti os campos City, CityCode e Province que estavam na implementação do Pedro,
mas que não vi no Gson de resposta a API
Analisar se a resposta se dará de forma adequada
*/

data class ByCountryResponseListItem (
    @SerializedName("Cases")
    val cases: Int,
    @SerializedName("Country")
    val country: String,
    @SerializedName ("City")
    val city: String?,
    @SerializedName("Province")
    val province: String?,
    @SerializedName("Date")
    val date: String,
    @SerializedName("Lat")
    val lat: Double,
    @SerializedName("Lon")
    val lon: Double,
    @SerializedName("CountryCode")
    val countryCode: String,
    @SerializedName("Status")
    val status: String
)