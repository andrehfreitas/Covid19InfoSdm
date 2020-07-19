package br.edu.ifsp.scl.covid19infosdm.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import br.edu.ifsp.scl.covid19infosdm.model.Covid19Service
import br.edu.ifsp.scl.covid19infosdm.model.dataclass.ByCountryResponseList
import br.edu.ifsp.scl.covid19infosdm.model.dataclass.DayOneResponseList
import java.util.*

class Covid19ViewModel(context: Context): ViewModel() {
    private val model = Covid19Service(context)

    fun fetchCountries() = model.callGetCountries()

    fun fetchSummary() = model.getSummaryGlobal()

    fun fetchCountryData(countryCode: String) = model.getSummaryCountry(countryCode)

//    fun fetchCountriesSummary() = model.getCountry()

    fun fetchDayOne(countryName: String, status: String) = model.callService(
        countryName, status.toLowerCase(Locale.getDefault()),
        DayOneResponseList::class.java
    )

    fun fetchByCountry(countryName: String, status: String) = model.callService(
        countryName, status.toLowerCase(Locale.getDefault()),
        ByCountryResponseList::class.java
    )
}