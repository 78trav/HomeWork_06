package otus.homework.reactivecats

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class CatsViewModel(
    private val catsService: CatsService,
    private val localCatFactsGenerator: LocalCatFactsGenerator,
    context: Context
) : ViewModel() {

    private val _catsLiveData = MutableLiveData<Result>()
    val catsLiveData: LiveData<Result> = _catsLiveData

    private var fact = catsService.getCatFact()
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .onErrorResumeWith { error -> _catsLiveData.postValue(Error(error.toString())) }
        .subscribe { fact -> _catsLiveData.postValue(Success(fact)) }

    init {
        getFacts()
    }

//    init {
//
//        catsService.getCatFact().enqueue(object : Callback<Fact> {
//            override fun onResponse(call: Call<Fact>, response: Response<Fact>) {
//                if (response.isSuccessful && response.body() != null) {
//                    _catsLiveData.value = Success(response.body()!!)
//                } else {
//                    _catsLiveData.value = Error(
//                        response.errorBody()?.string() ?: context.getString(
//                            R.string.default_error_text
//                        )
//                    )
//                }
//            }
//
//            override fun onFailure(call: Call<Fact>, t: Throwable) {
//                _catsLiveData.value = ServerError
//            }
//        })
//    }

    fun getFacts() {
        fact = Observable.interval(2, TimeUnit.SECONDS)
            .flatMapSingle {
                catsService.getCatFact()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .onErrorResumeNext { localCatFactsGenerator.generateCatFact() }
            }
            .subscribe { fact -> _catsLiveData.postValue(Success(fact)) }
    }

    override fun onCleared() {
        super.onCleared()
        fact.dispose()
    }
}

class CatsViewModelFactory(
    private val catsRepository: CatsService,
    private val localCatFactsGenerator: LocalCatFactsGenerator,
    private val context: Context
) :
    ViewModelProvider.NewInstanceFactory() {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        CatsViewModel(catsRepository, localCatFactsGenerator, context) as T
}

sealed class Result
data class Success(val fact: Fact) : Result()
data class Error(val message: String) : Result()
object ServerError : Result()
