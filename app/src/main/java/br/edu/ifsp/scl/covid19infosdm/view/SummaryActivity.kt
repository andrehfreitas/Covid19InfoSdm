package br.edu.ifsp.scl.covid19infosdm.view

import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import br.edu.ifsp.scl.covid19infosdm.R
import br.edu.ifsp.scl.covid19infosdm.viewmodel.Covid19ViewModel
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_summary.*
import java.text.NumberFormat
import java.util.*

class SummaryActivity : AppCompatActivity(){
    private lateinit var viewModel: Covid19ViewModel
    private lateinit var countryAdapter: ArrayAdapter<String>
    private lateinit var countryNameSlugMap: MutableMap<String, String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_summary)

        viewModel = Covid19ViewModel(this)

        initGlobalData()
        countryAdapterPanelInit()
    }

    private fun countryAdapterPanelInit() {
        /* Preenchido por Web Service */
        countryAdapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1)
        countryAdapter.add("Selecione o pais ... ")
        countryNameSlugMap = mutableMapOf()
        spnCountryPanel.adapter = countryAdapter
        viewModel.fetchCountries().observe(
            this,
            Observer { countryList ->
                countryList.sortedBy { it.country }.forEach { countryListItem ->
                    if ( countryListItem.country.isNotEmpty()) {
                        countryAdapter.add(countryListItem.country)
                        countryNameSlugMap[countryListItem.country] = countryListItem.slug
                    }
                }
            }
        )
    }

    fun initGlobalData(){
        val f = NumberFormat.getInstance(Locale("pt", "br"))
        viewModel.fetchSummary().observe(
            this,
            Observer { summaryGlobal ->
                txtNewCases24h.text = formatNumber(summaryGlobal.newConfirmed)
                txtTotalCases.text = formatNumber(summaryGlobal.totalConfirmed)
                txtNewDeaths24h.text = formatNumber(summaryGlobal.newDeaths)
                txtTotalDeaths.text = formatNumber(summaryGlobal.totalDeaths)
                txtNewRecovered24h.text = formatNumber(summaryGlobal.newRecovered)
                txtTotalRecovered.text = formatNumber(summaryGlobal.totalRecovered)
            }
        )
    }

    private fun viewCountryData(){
        val countrySlug = countryNameSlugMap[spnCountryPanel.selectedItem.toString()]!!


        viewModel.fetchCountryData(countrySlug).observe(
            this,
            Observer { summaryCountry ->
                txtNewCases24h.text = formatNumber(summaryCountry.newConfirmed)
                txtTotalCases.text = formatNumber(summaryCountry.totalConfirmed)
                txtNewDeaths24h.text = formatNumber(summaryCountry.newDeaths)
                txtTotalDeaths.text = formatNumber(summaryCountry.totalDeaths)
                txtNewRecovered24h.text = formatNumber(summaryCountry.newRecovered)
                txtTotalRecovered.text = formatNumber(summaryCountry.totalRecovered)
            }
        )
    }

    // Formataçao do numero
    fun formatNumber(number: Int): String {
        val f = NumberFormat.getInstance(Locale("pt", "br"))
        val numeroFormatado = f.format(number)
        return numeroFormatado
    }
}