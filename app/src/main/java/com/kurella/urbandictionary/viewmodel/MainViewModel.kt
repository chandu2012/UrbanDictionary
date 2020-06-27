package com.kurella.urbandictionary.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kurella.urbandictionary.model.BASE_URL
import com.kurella.urbandictionary.model.MeaningData
import com.kurella.urbandictionary.model.UrbanDictApi
import com.kurella.urbandictionary.model.json_data_classes.ListDataItem
import com.kurella.urbandictionary.model.json_data_classes.UrbanDictionaryResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainViewModel : ViewModel() {

    private var isUpVoteSelected = MutableLiveData(true)
    private var definitionListLiveData = MutableLiveData<List<MeaningData>>()
    private var shouldShowSpinnerLiveData = MutableLiveData(false)

    fun getShouldShowSpinnerLiveData() = shouldShowSpinnerLiveData as LiveData<Boolean>

    fun getDefinitionLiveData() = definitionListLiveData as LiveData<List<MeaningData>>

    fun search(query: String) {
        Log.v("Search query", query)

        shouldShowSpinnerLiveData.value = true

        val definitionList = ArrayList<MeaningData>()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val response = retrofit.create(UrbanDictApi::class.java)

        val callUrbanDictionary = response.userEntry(query)

        callUrbanDictionary.enqueue(object : Callback<UrbanDictionaryResponse> {
            override fun onFailure(call: Call<UrbanDictionaryResponse>, t: Throwable) {
                Log.e("Retrofit Call", t.message)
            }

            override fun onResponse(
                call: Call<UrbanDictionaryResponse>,
                response: Response<UrbanDictionaryResponse>
            ) {
                val unSortedList = response.body()?.listDataItem
                var sortedList: List<ListDataItem>? = null

                isUpVoteSelected.value?.also {isUpVote ->
                    sortedList = if (isUpVote){
                        unSortedList?.sortedByDescending{ it.thumbs_up }
                    }else{
                        unSortedList?.sortedByDescending{ it.thumbs_down }
                    }
                }

                sortedList?.forEach {
                    definitionList.add(
                        MeaningData(
                            it.definition,
                            it.thumbs_up,
                            it.thumbs_down
                        )
                    )
                }

                definitionListLiveData.value = definitionList
                shouldShowSpinnerLiveData.value = false
            }
        })
    }

    fun updateIsUpVote() {
        isUpVoteSelected.value = isUpVoteSelected.value?.not()
    }

    fun getIsUpVoteSelectedLiveData() = isUpVoteSelected as LiveData<Boolean>
}