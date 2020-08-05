package br.edu.ifsp.scl.covid19infosdm.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Switch
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import br.edu.ifsp.scl.covid19infosdm.R
import br.edu.ifsp.scl.covid19infosdm.model.dataclass.ByCountryResponseList
import br.edu.ifsp.scl.covid19infosdm.model.dataclass.ByCountryResponseListItem
import br.edu.ifsp.scl.covid19infosdm.model.dataclass.DayOneResponseList
import br.edu.ifsp.scl.covid19infosdm.model.dataclass.DayOneResponseListItem
import br.edu.ifsp.scl.covid19infosdm.viewmodel.Covid19ViewModel
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import kotlinx.android.synthetic.main.activity_detalhe_casos.*
import java.text.SimpleDateFormat
import java.util.*

class DetalheCasosActivity : AppCompatActivity() {
    private lateinit var viewModel: Covid19ViewModel
    private lateinit var countryAdapter: ArrayAdapter<String>
    private lateinit var countryNameSlugMap: MutableMap<String, String>


    /* Classe para os serviços que serão acessados */
    private enum class Information(val type: String){
        DAY_ONE("Dia um"),
        BY_COUNTRY("Por país")
    }


    /* Classe para o status que será buscado no serviço */
    private enum class Status(val type: String){
        CONFIRMED("Confirmados"),
        DEATHS("Mortos"),
        RECOVERED("Recuperados")
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalhe_casos)

        viewModel = Covid19ViewModel(this)

        countryAdapterInit()
        informationAdapterInit()
        statusAdapterInit()
    }


    private fun countryAdapterInit() {
        /* Preenchido por Web Service */
        countryAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1)
        countryAdapter.add("Selecione... ")
        countryNameSlugMap = mutableMapOf()
        countrySp.adapter = countryAdapter
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


    private fun informationAdapterInit() {
        val informationList = arrayListOf<String>()
        Information.values().forEach { informationList.add(it.type) }

        infoSp.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, informationList)
        infoSp.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun  onNothingSelected(parent: AdapterView<*>?) { }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                when (position){
                    Information.DAY_ONE.ordinal -> {
                        viewModeTv.visibility = View.VISIBLE
                        viewModeRg.visibility = View.VISIBLE
                    }
                    Information.BY_COUNTRY.ordinal -> {
                        viewModeTv.visibility = View.GONE
                        viewModeRg.visibility = View.GONE
                    }
                }
            }
        }
    }


    private fun statusAdapterInit() {
        val statusList = arrayListOf<String>()
        Status.values().forEach { statusList.add(it.type) }

        statusSp.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, statusList)
    }


    // Faz a passagem do status selecionado para a lingua inglesa para que o webService "entenda"
    private fun convertStatus(): String {
        val statusIngles = when (statusSp.selectedItem.toString()) {
            Status.CONFIRMED.type -> "confirmed"
            Status.DEATHS.type    -> "deaths"
            Status.RECOVERED.type -> "recovered"
            else ->  "Erro: Status inválido"
        }
        return statusIngles
    }


    /* Função chamada quando é clicado o botão de busca, verificando se o país foi selecionado */
    fun onRetrieveClick(view: View){
                if (countrySp.selectedItemPosition == 0){
                    val builder = AlertDialog.Builder(this@DetalheCasosActivity)
                    builder.setTitle("Opss")
                    builder.setMessage("Faltou selecionar um país!")
                    builder.setNeutralButton("OK", null)
                    val dialog: AlertDialog = builder.create()
                    dialog.show()
                } else {
                    when (infoSp.selectedItem.toString()) {
                        Information.DAY_ONE.type -> {
                            fetchDayOne()
                        }
                        Information.BY_COUNTRY.type -> {
                            fetchByCountry()
                        }
                    }
                }
    }


    private fun fetchDayOne(){
        val countrySlug = countryNameSlugMap[countrySp.selectedItem.toString()]!!

        viewModel.fetchDayOne(countrySlug, convertStatus()).observe(
            this,
            Observer { casesList ->
                if (viewModeTextRb.isChecked) {
                    /* Exibição dos dados em modo texto */
                    modoGrafico(ligado = false)
                    resultTv.text = casesListToString(casesList)
                }
                else {
                    /* Exibição dos dados em modo gráfico */
                    modoGrafico (ligado = true)
                    resultGv.removeAllSeries()
                    resultGv.gridLabelRenderer.resetStyles()

                    /* Preparando pontos */
                    val pointsArrayList = arrayListOf<DataPoint>()
                    casesList.forEach {
                        val date = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(it.date.substring(0,10))
                        val point = DataPoint(date, it.cases.toDouble())
                        pointsArrayList.add(point)
                    }
                    val pointsSeries = LineGraphSeries(pointsArrayList.toTypedArray())
                    resultGv.addSeries(pointsSeries)

                    /* Formatando o gráfico */
                    resultGv.gridLabelRenderer.setHumanRounding(false)
                    resultGv.gridLabelRenderer.labelFormatter = DateAsXAxisLabelFormatter(this)

                    resultGv.gridLabelRenderer.numHorizontalLabels = 4
                    val primeiraData = Date(pointsArrayList.first().x.toLong())
                    val ultimaData = Date(pointsArrayList.last().x.toLong())
                    resultGv.viewport.setMinX(primeiraData.time.toDouble())
                    resultGv.viewport.setMaxX(ultimaData.time.toDouble())
                    resultGv.viewport.isXAxisBoundsManual = true

                    resultGv.gridLabelRenderer.numVerticalLabels = 4
                    resultGv.viewport.setMinY(pointsArrayList.first().y)
                    resultGv.viewport.setMaxY(pointsArrayList.last().y)
                    resultGv.viewport.isYAxisBoundsManual = true
                }
            }
        )
    }


    private fun fetchByCountry(){
        val countrySlug = countryNameSlugMap[countrySp.selectedItem.toString()]!!
        modoGrafico(ligado = false)

        viewModel.fetchByCountry(countrySlug, convertStatus()).observe(
            this,
            Observer {casesList ->
                    resultTv.text = casesListToString(casesList)
            }
        )
    }


    private fun modoGrafico(ligado: Boolean){
        if (ligado){
            resultTv.visibility = View.GONE
            resultGv.visibility = View.VISIBLE
        }
        else{
            resultTv.visibility = View.VISIBLE
            resultGv.visibility = View.GONE
        }
    }


    private inline fun <reified T: ArrayList<*>> casesListToString(responseList: T): String {
        val resultSb = StringBuffer()

        /* Usando class.java para não ser necessário adicionar bibliotecas de reflexão kotlin */
        responseList.forEach {
            when(T::class.java){
                DayOneResponseList::class.java -> {
                    with(it as DayOneResponseListItem) {
                        resultSb.append("Casos: ${this.cases}\n")
                        resultSb.append("Data: ${this.date.substring(0, 10)}\n\n")
                    }
                }
                ByCountryResponseList::class.java -> {
                    with (it as ByCountryResponseListItem) {
                        this.province.takeIf { !this.province.isNullOrEmpty() }?.let { province ->
                            resultSb.append("Estado/Província: ${province}\n")
                        }
                        this.city.takeIf { !this.province.isNullOrEmpty() }?.let {city ->
                            resultSb.append("Cidade: ${city}\n")
                        }

                        resultSb.append("Casos: ${this.cases}\n")
                        resultSb.append("Data: ${this.date.substring(0,10)}\n\n")
                    }
                }

            }
        }
        return resultSb.toString()
    }

    /* Retorno de Intent para a activity origem (pode ser qualquer activity do projeto) que chamar
    a Detailhe Casos Activity */
    companion object {
        fun getStartIntent (context: Context): Intent {
            return Intent (context, DetalheCasosActivity::class.java)
        }
    }


}
