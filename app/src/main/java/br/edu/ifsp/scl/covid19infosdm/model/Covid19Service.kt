package br.edu.ifsp.scl.covid19infosdm.model

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import br.edu.ifsp.scl.covid19infosdm.R
import br.edu.ifsp.scl.covid19infosdm.model.Covid19Api.BASE_URL
import br.edu.ifsp.scl.covid19infosdm.model.Covid19Api.COUNTRIES_ENDPOINT
import br.edu.ifsp.scl.covid19infosdm.model.dataclass.ByCountryResponseList
import br.edu.ifsp.scl.covid19infosdm.model.dataclass.CountryList
import br.edu.ifsp.scl.covid19infosdm.model.dataclass.DayOneResponseList
import com.android.volley.Request
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class Covid19Service(val context: Context) {
    private val requestQueue = Volley.newRequestQueue(context)
    private val gson = Gson()


    /* Acesso ao Web Service usando Volley */
    fun callGetCountries(): MutableLiveData<CountryList>{
        val url = "${BASE_URL}${COUNTRIES_ENDPOINT}"

        val countriesListLd = MutableLiveData<CountryList>()

        val request = JsonArrayRequest(
            Request.Method.GET,
            url,
            null,
            { countriesList ->
                countriesListLd.value = gson.fromJson(countriesList.toString(), CountryList::class.java)
            },
            { error -> Log.e("Covid19InfoSdm", "${error.message}")}
        )
        requestQueue.add(request)

        return countriesListLd
    }


    /* Implementação da Interface RetrofitServices usando um objeto retrofit */
    private val retrofitServices = with (Retrofit.Builder()){
        baseUrl(BASE_URL)
        addConverterFactory(GsonConverterFactory.create())
        build()
    }.create(Covid19Api.RetrofitServices::class.java)


    /*
    Acesso ao Web Service usando Retrofit. Como os serviços retornam o mesmo tipo de resposta foram
    aglutinados em uma mesma função
     */
    fun <T> callService (countryName: String, status: String, tipoResposta: Class<T>): MutableLiveData<T> {
        val responseList = MutableLiveData<T>()

        /* Callback usado pelos serviços que retornam JSON */
        val callback = object: Callback<T> {

            override fun onResponse(call: Call<T>, response: Response<T>) {
                if (response.isSuccessful){
                    responseList.value = response.body()
                }
            }

            override fun onFailure(call: Call<T>, t: Throwable) {
                Log.e(context.getString(R.string.app_name), "Falha no acesso ao serviço")
            }
        }

        /* O serviço correto é chamado */
        when (tipoResposta) {
            DayOneResponseList::class.java -> {
                @Suppress("UNCHECKED_CAST")
                retrofitServices.getDayOne(countryName, status).enqueue(callback as Callback<DayOneResponseList>)
            }
            ByCountryResponseList::class.java -> {
                @Suppress("UNCHECKED_CAST")
                retrofitServices.getByCountry(countryName, status).enqueue(callback as Callback<ByCountryResponseList>)
            }
        }

        return responseList
    }
}