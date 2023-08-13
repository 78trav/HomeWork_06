package otus.homework.reactivecats

import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import retrofit2.Call
import retrofit2.http.GET

interface CatsService {

    @GET("fact")
    fun getCatFact(): Observable<Fact>
}
