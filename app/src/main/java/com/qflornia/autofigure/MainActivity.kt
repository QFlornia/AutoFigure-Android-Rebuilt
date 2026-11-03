package com.qflornia.autofigure

import android.os.Bundle
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.lifecycleScope
import com.qflornia.autofigure.engine.AiConfig
import com.qflornia.autofigure.engine.FigureEngine
import com.qflornia.autofigure.engine.DocumentTools
import com.qflornia.autofigure.engine.ExportFormats
import com.qflornia.autofigure.data.ProjectStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); setContent { AutoFigureScreen() } }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AutoFigureScreen() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("provider", 0) }
    var base by remember { mutableStateOf(prefs.getString("base", "https://openrouter.ai/api/v1") ?: "") }
    var key by remember { mutableStateOf(prefs.getString("key", "") ?: "") }
    var model by remember { mutableStateOf(prefs.getString("model", "google/gemini-3.1-pro-preview") ?: "") }
    var instruction by remember { mutableStateOf("") }
    var pdfName by remember { mutableStateOf("") }
    var referenceImage by remember { mutableStateOf<ByteArray?>(null) }
    var referenceImageName by remember { mutableStateOf("") }
    var enhancementPrompt by remember { mutableStateOf("") }
    var showHistory by remember { mutableStateOf(false) }
    val store = remember { ProjectStore(context) }
    var history by remember { mutableStateOf(store.list()) }
    var topic by remember { mutableStateOf("paper") }
    var iterations by remember { mutableFloatStateOf(3f) }
    var threshold by remember { mutableFloatStateOf(8.5f) }
    var busy by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf("配置模型 API 后，输入科研绘图要求即可开始。") }
    var svg by remember { mutableStateOf("") }
    var logs by remember { mutableStateOf(listOf<String>()) }
    val engine = remember { FigureEngine() }
    val pdfLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            busy = true; status = "正在提取 PDF 方法学文本…"
            (context as ComponentActivity).lifecycleScope.launch {
                try { val extracted = withContext(Dispatchers.IO) { DocumentTools.extractPdfText(context, uri) }; instruction = extracted; pdfName = uri.lastPathSegment ?: "paper.pdf"; status = "PDF 文本提取完成，已填入绘图任务（最多 120,000 字符）" }
                catch (e: Exception) { status = "PDF 提取失败：${e.message}" } finally { busy = false }
            }
        }
    }
    val imageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            try {
                referenceImage = context.contentResolver.openInputStream(uri)?.use { stream ->
                    val bitmap = BitmapFactory.decodeStream(stream) ?: error("无法解码图片")
                    ByteArrayOutputStream().use { out -> bitmap.compress(Bitmap.CompressFormat.JPEG, 88, out); out.toByteArray() }
                }
                referenceImageName = uri.lastPathSegment ?: "reference-image"
                status = if (referenceImage != null) "参考图已载入；生成时会随提示词发送给支持视觉输入的模型。" else "参考图读取失败"
            } catch (e: Exception) { status = "参考图读取失败：${e.message}" }
        }
    }
    val saveLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("image/svg+xml")) { uri ->
        if (uri != null && svg.isNotBlank()) runCatching { val payload = if ((uri.lastPathSegment ?: "").endsWith("xml", true)) ExportFormats.svgToDrawio(svg) else svg; context.contentResolver.openOutputStream(uri)?.use { it.write(payload.toByteArray()) } }
            .onSuccess { status = "SVG 已保存" }.onFailure { status = "保存失败：${it.message}" }
    }
    MaterialTheme(colorScheme = lightColorScheme()) {
        Scaffold(topBar = { TopAppBar(title = { Text("AutoFigure · Scientific Illustrator") }) }) { pad ->
            Column(Modifier.padding(pad).fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("模型 API 配置", style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(base, { base = it }, label = { Text("API Base URL") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(key, { key = it }, label = { Text("API Key") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(model, { model = it }, label = { Text("模型名称") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                Button(onClick = { prefs.edit().putString("base", base).putString("key", key).putString("model", model).apply(); status = "配置已保存在本机" }, modifier = Modifier.fillMaxWidth()) { Text("保存 API 配置") }
                HorizontalDivider()
                Text("绘图任务", style = MaterialTheme.typography.titleMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(onClick = { pdfLauncher.launch(arrayOf("application/pdf")) }, enabled = !busy) { Text("导入论文 PDF") }
                    OutlinedButton(onClick = { imageLauncher.launch(arrayOf("image/*")) }, enabled = !busy) { Text("添加参考图") }
                    OutlinedButton(onClick = { showHistory = !showHistory; history = store.list() }) { Text("项目历史 (${history.size})") }
                }
                if (pdfName.isNotBlank()) Text("已导入 PDF：$pdfName", style = MaterialTheme.typography.bodySmall)
                if (referenceImageName.isNotBlank()) Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Text("参考图：$referenceImageName", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
                    TextButton(onClick = { referenceImage = null; referenceImageName = "" }) { Text("移除") }
                }
                if (showHistory) {
                    history.forEach { item ->
                        OutlinedButton(onClick = { instruction = item.instruction; svg = item.svg; status = "已载入 ${item.name} · 评分 ${item.score}" }, modifier = Modifier.fillMaxWidth()) { Text("${item.name} · ${"%.1f".format(item.score)}/10") }
                    }
                }
                OutlinedTextField(instruction, { instruction = it }, label = { Text("描述你要绘制的科研图 / 粘贴方法学内容") }, modifier = Modifier.fillMaxWidth().heightIn(min = 140.dp), minLines = 5)
                Text("内容类型")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { listOf("paper", "survey", "blog", "textbook").forEach { FilterChip(selected = topic == it, onClick = { topic = it }, label = { Text(it) }) } }
                Text("最大迭代次数：${iterations.toInt()}")
                Slider(value = iterations, onValueChange = { iterations = it }, valueRange = 1f..5f, steps = 3)
                Text("质量阈值：${"%.1f".format(threshold)}/10")
                Slider(value = threshold, onValueChange = { threshold = it }, valueRange = 6f..10f)
                Button(enabled = !busy && instruction.isNotBlank(), onClick = {
                    prefs.edit().putString("base", base).putString("key", key).putString("model", model).apply()
                    busy = true; status = "正在生成初稿并执行评价—修改循环…"; logs = emptyList(); svg = ""
                    (context as ComponentActivity).lifecycleScope.launch {
                        try {
                            val result = withContext(Dispatchers.IO) { engine.generate(AiConfig(base, key, model), instruction, topic, iterations.toInt(), threshold.toDouble(), referenceImage) }
                            svg = result.svg; logs = result.log
                            withContext(Dispatchers.IO) { store.save(instruction, svg, result.score) }
                            history = store.list(); status = "完成：质量评分 ${"%.1f".format(result.score)}/10，迭代 ${result.iterations} 轮；已保存到本机历史"
                        } catch (e: Exception) { status = "生成失败：${e.message}" } finally { busy = false }
                    }
                }, modifier = Modifier.fillMaxWidth()) { Text(if (busy) "处理中…" else "生成科研插图") }
                Text(status, style = MaterialTheme.typography.bodyMedium)
                if (logs.isNotEmpty()) { Text("流程日志", style = MaterialTheme.typography.titleMedium); logs.forEach { Text("• $it", style = MaterialTheme.typography.bodySmall) } }
                if (svg.isNotBlank()) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { saveLauncher.launch("AutoFigure.svg") }) { Text("导出 SVG") }
                        OutlinedButton(onClick = { saveLauncher.launch("AutoFigure.drawio.xml") }) { Text("导出 draw.io") }
                        OutlinedButton(onClick = { status = "SVG 源码已生成，可导出后用矢量编辑器继续编辑。" }) { Text("查看说明") }
                    }
                    OutlinedButton(onClick = {
                        busy = true
                        (context as ComponentActivity).lifecycleScope.launch {
                            try { enhancementPrompt = withContext(Dispatchers.IO) { engine.buildEnhancementPrompt(AiConfig(base, key, model), svg, "clean BioRender-like vector scientific illustration", topic) }; status = "图像增强提示词已生成，可复制到图像生成模型使用" }
                            catch (e: Exception) { status = "提示词生成失败：${e.message}" } finally { busy = false }
                        }
                    }, enabled = !busy) { Text("生成图像美化提示词") }
                    if (enhancementPrompt.isNotBlank()) Text(enhancementPrompt, style = MaterialTheme.typography.bodySmall)
                    Text("矢量预览", style = MaterialTheme.typography.titleMedium)
                    AndroidView(factory = { ctx -> WebView(ctx).apply { settings.javaScriptEnabled = false; webViewClient = WebViewClient() } }, update = { it.loadDataWithBaseURL(null, svg, "image/svg+xml", "UTF-8", null) }, modifier = Modifier.fillMaxWidth().height(420.dp))
                    Text("SVG 源码（可直接编辑）", style = MaterialTheme.typography.titleMedium)
                    OutlinedTextField(value = svg, onValueChange = { svg = it }, modifier = Modifier.fillMaxWidth().heightIn(min = 180.dp), minLines = 8, textStyle = MaterialTheme.typography.bodySmall)
                }
                Text("参考图会以多模态输入发送；所选模型必须支持视觉输入。当前仍未集成 SAM3/RMBG 分割、原生 mxGraph 图元重建、节点级拖拽编辑和高分辨率 PNG/PDF 导出。", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
