package com.qflornia.autofigure.engine
import android.util.Base64
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.*
import java.util.concurrent.TimeUnit

data class ModelProfile(val baseUrl:String,val apiKey:String,val model:String,val provider:String="openai-compatible")
class ProviderRouter {
 private val client=OkHttpClient.Builder().connectTimeout(30,TimeUnit.SECONDS).readTimeout(240,TimeUnit.SECONDS).build()
 fun chat(p:ModelProfile,system:String,user:String,image:ByteArray?=null):String {
  require(p.baseUrl.isNotBlank() && p.apiKey.isNotBlank() && p.model.isNotBlank()){"请填写 API 地址、Key 和模型名"}
  val content:Any=if(image==null) user else JSONArray()
   .put(JSONObject().put("type","text").put("text",user))
   .put(JSONObject().put("type","image_url").put("image_url",JSONObject().put("url","data:image/jpeg;base64,"+Base64.encodeToString(image,Base64.NO_WRAP))))
  val body=JSONObject().put("model",p.model).put("temperature",0.2).put("messages",JSONArray()
   .put(JSONObject().put("role","system").put("content",system)).put(JSONObject().put("role","user").put("content",content)))
  val req=Request.Builder().url(p.baseUrl.trimEnd('/')+"/chat/completions").header("Authorization","Bearer ${p.apiKey}")
   .post(body.toString().toRequestBody("application/json".toMediaType())).build()
  client.newCall(req).execute().use { r ->
   val raw=r.body?.string().orEmpty()
   if(!r.isSuccessful) error("API HTTP ${r.code}: ${raw.take(1000)}")
   return JSONObject(raw).getJSONArray("choices").getJSONObject(0).getJSONObject("message").optString("content").also { require(it.isNotBlank()){"模型返回空内容"} }
  }
 }
}
