package br.edu.ifsp.scl.covid19infosdm.view

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import br.edu.ifsp.scl.covid19infosdm.R
import br.edu.ifsp.scl.covid19infosdm.viewmodel.Covid19ViewModel
import kotlinx.android.synthetic.main.activity_main.*
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.time.format.ResolverStyle
import java.util.*

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
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

            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }

        btnCasesDayday.setOnClickListener {
            val intent = DetalheCasosActivity.getStartIntent(this)
            this.startActivity(intent)
        }

    }

    private fun countryAdapterPanelInit() {
        /* Preenchido por Web Service */
        countryAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1)
        countryAdapter.add("DADOS GLOBAIS")
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

    @SuppressLint("SetTextI18n", "SimpleDateFormat")
    fun initData() {
        viewModel.fetchSummaryDate().observe(
            this,
            Observer {data ->
                // Formatação da Data para formato usado no Brasil
                val indexT = data.indexOf('T')
                val indexZ = data.indexOf('Z')
                val parteData = data.substring(0, indexT)
                val formatoOrigem = SimpleDateFormat("yyyy-MM-dd")
                val dataAplicada = formatoOrigem.parse(parteData)
                formatoOrigem.applyPattern("dd/MM/yyyy")
                val dataFormatada = formatoOrigem.format(dataAplicada)
                val diaDataFormatada = dataFormatada.substring(0, 2)
                val mesAnoDataFormatada = dataFormatada.substring(2, 10)

                /* Ajuste da data e da hora para o fuso horário do Brasil, data retornada do WebService
                 está 3 horas a frente do Brasil */
                val horaNova = when (val horaOriginal =
                                   data.substring(indexT + 1, indexT + 3).toInt()) {
                    0 -> 21 and diaDataFormatada.toInt() - 1
                    1 -> 22 and diaDataFormatada.toInt() - 1
                    2 -> 23 and diaDataFormatada.toInt() - 1
                    else -> horaOriginal - 3
                }
                val horaAjustada = String.format("%02d", horaNova) + data.substring(indexT + 3, indexZ)

                // Exibição da data formatada e hora ajustada no TextView
                txtDataAtualizacao.text = "$diaDataFormatada$mesAnoDataFormatada $horaAjustada"
            }
        )
    }

    @SuppressLint("SetTextI18n")
    fun initGlobalData() {
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
        // Carrefa TextViews com 0 por padrao e caso WebService nao retorne dados
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


