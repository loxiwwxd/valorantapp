package com.example.valorantapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.google.android.material.tabs.TabLayout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import com.example.valorantapp.R

interface ValorantApi {
    @GET("valorant/v2/store-featured")
    fun getFeaturedStore(): Call<Any>

    @GET("valorant/v3/matches/{region}/{name}/{tag}")
    fun getMatchHistory(
        @Path("region") region: String,
        @Path("name") name: String,
        @Path("tag") tag: String
    ): Call<Any>
}

class MainActivity : ComponentActivity() {
    private lateinit var api: ValorantApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.henrikdev.xyz/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        api = retrofit.create(ValorantApi::class.java)
        val editRiotId = findViewById<EditText>(R.id.editRiotId)
        val btnLoad = findViewById<Button>(R.id.btnLoad)
        val tvContent = findViewById<TextView>(R.id.tvContent)
        val tabLayout = findViewById<TabLayout>(R.id.tabLayout)

        // Add tabs
        tabLayout.addTab(tabLayout.newTab().setText("Mağaza"))
        tabLayout.addTab(tabLayout.newTab().setText("Maç Geçmişi"))
        tabLayout.addTab(tabLayout.newTab().setText("Koleksiyon"))

        var name = ""
        var tag = ""
        var region = "eu" // Varsayılan EU, değiştirilebilir
        btnLoad.setOnClickListener {
            val riotId = editRiotId.text.toString()
            if (riotId.contains("#")) {
                name = riotId.split("#")[0]
                tag = riotId.split("#")[1]
                // Şimdi tab'a göre yükle, ama önce veri çek
                loadData(tabLayout.selectedTabPosition, name, tag, region, tvContent)
            } else {
                tvContent.text = "Geçersiz Riot ID! Ör: Player#EUW"
            }
        }
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                loadData(tab?.position ?: 0, name, tag, region, tvContent)
            }
            override fun onTabReselected(tab: TabLayout.Tab?) {}
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
        })
    }
    private fun loadData(tabPosition: Int, name: String, tag: String, region: String, tvContent: TextView) {
        if (name.isEmpty()) {
            tvContent.text = "Önce Riot ID gir!"
            return
        }
        when (tabPosition) {
            0 -> { // Mağaza
                api.getFeaturedStore().enqueue(object : Callback<Any> {
                    override fun onResponse(call: Call<Any>, response: Response<Any>) {
                        if (response.isSuccessful) {
                            val data = response.body() as Map<*, *> // JSON parse et
                            tvContent.text = "Öne Çıkan Mağaza: ${data["FeaturedBundle"]}" // Basit göster, detaylandırabilirsin
                        } else {
                            tvContent.text = "Hata: ${response.code()}"
                        }
                    }
                    override fun onFailure(call: Call<Any>, t: Throwable) {
                        tvContent.text = "Bağlantı hatası: ${t.message}"
                    }
                })
            }
            1 -> { // Maç Geçmişi
                api.getMatchHistory(region, name, tag).enqueue(object : Callback<Any> {
                    override fun onResponse(call: Call<Any>, response: Response<Any>) {
                        if (response.isSuccessful) {
                            val data = response.body() as Map<*, *>
                            val matches = data["data"] as List<*>
                            var text = "Maç Geçmişi:\n"
                            for (match in matches) {
                                val m = match as Map<*, *>
                                val metadata = m["metadata"] as Map<*, *>
                                text += "Harita: ${metadata["map"]}, Skor: ${metadata["has_won"]}\n"
                            }
                            tvContent.text = text
                        } else {
                            tvContent.text = "Hata: ${response.code()}"
                        }
                    }
                    override fun onFailure(call: Call<Any>, t: Throwable) {
                        tvContent.text = "Bağlantı hatası: ${t.message}"
                    }
                })
            }
            2 -> { // Koleksiyon
                tvContent.text = "Koleksiyon burada gösterilecek (henüz tam değil, basit liste ekle)."
                // Buraya unofficial API'de yoksa, statik liste koy veya ileri için bırak.
            }
        }
    }
}