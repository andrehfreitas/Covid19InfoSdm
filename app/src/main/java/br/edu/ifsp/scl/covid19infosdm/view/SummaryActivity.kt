package br.edu.ifsp.scl.covid19infosdm.view

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
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

        countryAdapterPanelInit()

        spnCountryPanel.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>?, view: View?, position: Int, l: Long) {
                if (position != 0){
                    viewCountryData()
                } else {
                    initGlobalData()
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }
    }

    private fun countryAdapterPanelInit() {
        /* Preenchido por Web Service */
        countryAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1)
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
        val country = spnCountryPanel.selectedItem.toString()
        viewModel.fetchCountryData(country).observe(
            this,
            Observer { summaryCountry ->
                txtRegion.text = summaryCountry.country
                txtNewCases24h.text = formatNumber(summaryCountry.newConfirmed)
                txtTotalCases.text = formatNumber(summaryCountry.totalConfirmed)
                txtNewDeaths24h.text = formatNumber(summaryCountry.newDeaths)
                txtTotalDeaths.text = formatNumber(summaryCountry.totalDeaths)
                txtNewRecovered24h.text = formatNumber(summaryCountry.newRecovered)
                txtTotalRecovered.text = formatNumber(summaryCountry.totalRecovered)
                //Toast.makeText(this, summaryCountry.country, Toast.LENGTH_SHORT).show()
                Toast.makeText(this, country, Toast.LENGTH_SHORT).show()
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