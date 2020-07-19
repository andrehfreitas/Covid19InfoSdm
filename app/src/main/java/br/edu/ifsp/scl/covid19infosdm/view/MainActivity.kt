package br.edu.ifsp.scl.covid19infosdm.view

import android.annotation.SuppressLint
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

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var viewModel: Covid19ViewModel
    private lateinit var countryAdapter: ArrayAdapter<String>
    private lateinit var countryNameSlugMap: MutableMap<String, String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onResume() {
        super.onResume()
        viewModel = Covid19ViewModel(this)

        countryAdapterPanelInit()

        spnCountryPanel.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>?, view: View?, position: Int, l: Long) {
                if (position == 0) {
                    initGlobalData()
                    initData()
                } else {
                    viewCountryData()
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
            }
        }

        btnDetalheCasos.setOnClickListener {
            val intent = DetalheCasosActivity.getStartIntent(this)
            this.startActivity(intent)
        }

    }

    private fun countryAdapterPanelInit() {
        /* Preenchido por Web Service */
        countryAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1)
        countryAdapter.add("Dados globais")
        countryNameSlugMap = mutableMapOf()
        spnCountryPanel.adapter = countryAdapter
        viewModel.fetchCountries().observe(
            this,
            Observer { countryList ->
                countryList.sortedBy { it.country }.forEach { countryListItem ->
                    if (countryListItem.country.isNotEmpty()) {
                        countryAdapter.add(countryListItem.country)
                        countryNameSlugMap[countryListItem.country] = countryListItem.slug
                    }
                }
            }
        )
    }

    @SuppressLint("SetTextI18n")
    fun initData() {
        viewModel.fetchSummaryDate().observe(
            this,
            Observer {data ->
                val dataRecebida = data
                val indexT = dataRecebida.indexOf('T')
                val indexZ = dataRecebida.indexOf('Z')
                val parteData = dataRecebida.substring(0, indexT)

                txtDataAtualizacao.text = parteData + ' ' + dataRecebida.substring(indexT + 1, indexZ)
                // txtDataAtualizacao.text = dataRecebida
            }
        )
    }

    @SuppressLint("SetTextI18n")
    fun initGlobalData() {
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

    private fun viewCountryData() {
        // Carrefa TextViews com 0 por padrao
        txtNewCases24h.text = formatNumber(0)
        txtTotalCases.text = formatNumber(0)
        txtNewDeaths24h.text = formatNumber(0)
        txtTotalDeaths.text = formatNumber(0)
        txtNewRecovered24h.text = formatNumber(0)
        txtTotalRecovered.text = formatNumber(0)

        val country = spnCountryPanel.selectedItem.toString()

        /* Muda os TextViews de zero para os dados do pais selecionado no Spinner
           caso existam dados no WebService */
        viewModel.fetchCountryData(country).observe(
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
}

// Formataçao do numero
fun formatNumber(number: Int): String {
    val f = NumberFormat.getInstance(Locale("pt", "br"))
    val numeroFormatado = f.format(number)
    return numeroFormatado
}


