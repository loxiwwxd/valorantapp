import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface ValorantApi {
    @GET("valorant/v3/matches/{region}/{name}/{tag}")
    fun getMatchHistory(
        @Path("region") region: String,
        @Path("name") name: String,
        @Path("tag") tag: String
    ): Call<Any>  // Any çünkü JSON dönecek, sonra parse edeceğiz

    @GET("valorant/v1/store-featured")
    fun getFeaturedStore(): Call<Any>
}