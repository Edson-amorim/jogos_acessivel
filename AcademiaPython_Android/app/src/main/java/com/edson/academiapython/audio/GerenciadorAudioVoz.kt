package com.edson.academiapython.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.view.accessibility.AccessibilityManager
import com.edson.academiapython.R
import java.util.Locale

/**
 * Módulo responsável pela síntese de voz (TextToSpeech) em Português do Brasil,
 * leitura adaptada de símbolos de código Python para acessibilidade e efeitos sonoros.
 */
class GerenciadorAudioVoz(
    context: Context,
    private val aoPronto: () -> Unit = {}
) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var ttsPronto = false
    private val filaPendente = mutableListOf<Pair<String, Boolean>>()
    private val mainHandler = Handler(Looper.getMainLooper())
    private var falaImediataRunnable: Runnable? = null
    private val accessibilityManager =
        context.getSystemService(Context.ACCESSIBILITY_SERVICE) as? AccessibilityManager

    private val soundPool: SoundPool
    private val somBatida: Int
    private val somNavegar: Int
    private val somErro: Int
    private val somPassar: Int
    private val somVitoria: Int
    private val somDerrota: Int
    private val somCampeao: Int

    var velocidadeFala: Float = 1.0f
        private set

    private val speechAttributes: AudioAttributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_ASSISTANCE_ACCESSIBILITY)
        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
        .build()
    private var contadorUtterance = 0L

    init {
        tts = TextToSpeech(context.applicationContext, this)
        tts?.setAudioAttributes(speechAttributes)

        val sfxAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(2)
            .setAudioAttributes(sfxAttributes)
            .build()

        somBatida = soundPool.load(context, R.raw.batida, 1)
        somNavegar = soundPool.load(context, R.raw.navegar, 1)
        somErro = soundPool.load(context, R.raw.erro, 1)
        somPassar = soundPool.load(context, R.raw.passar, 1)
        somVitoria = soundPool.load(context, R.raw.vitoria, 1)
        somDerrota = soundPool.load(context, R.raw.derrota, 1)
        somCampeao = soundPool.load(context, R.raw.campeao, 1)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val localePtBr = Locale("pt", "BR")
            val res = tts?.setLanguage(localePtBr)
            if (res == TextToSpeech.LANG_MISSING_DATA || res == TextToSpeech.LANG_NOT_SUPPORTED) {
                tts?.setLanguage(Locale.getDefault())
            }
            tts?.setAudioAttributes(speechAttributes)
            tts?.setSpeechRate(velocidadeFala)

            ttsPronto = true
            synchronized(filaPendente) {
                for ((texto, interromper) in filaPendente) {
                    falar(texto, interromper)
                }
                filaPendente.clear()
            }
            aoPronto()
        }
    }

    fun alternarVelocidadeFala(): String {
        velocidadeFala = when {
            velocidadeFala < 1.0f -> 1.0f
            velocidadeFala == 1.0f -> 1.25f
            velocidadeFala == 1.25f -> 1.5f
            else -> 0.85f
        }
        tts?.setSpeechRate(velocidadeFala)
        val descricao = when (velocidadeFala) {
            0.85f -> "pausada (0.85x)"
            1.0f -> "normal (1.0x)"
            1.25f -> "rápida (1.25x)"
            else -> "muito rápida (1.5x)"
        }
        falar("Velocidade da voz da Luna ajustada para $descricao.", interromper = true)
        return descricao
    }

    fun falar(texto: String, interromper: Boolean = true) {
        if (texto.isBlank()) return
        if (!ttsPronto) {
            synchronized(filaPendente) {
                if (interromper) filaPendente.clear()
                filaPendente.add(texto to interromper)
            }
            return
        }
        falaImediataRunnable?.let { mainHandler.removeCallbacks(it) }
        val idFala = "f_${++contadorUtterance}"
        val leitorAtivo = accessibilityManager?.isEnabled == true && accessibilityManager.isTouchExplorationEnabled
        if (interromper && leitorAtivo) {
            val runnable = Runnable {
                tts?.speak(texto, TextToSpeech.QUEUE_FLUSH, null, idFala)
            }
            falaImediataRunnable = runnable
            mainHandler.postDelayed(runnable, 90L)
        } else {
            val modoFila = if (interromper) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
            tts?.speak(texto, modoFila, null, idFala)
        }
    }

    /**
     * Converte trechos de código Python em leitura clara para leitores de tela,
     * pronunciando operadores e pontuações importantes.
     */
    fun converterCodigoParaFala(codigo: String): String {
        return codigo
            .replace("==", " igual a ")
            .replace("!=", " diferente de ")
            .replace(">=", " maior ou igual a ")
            .replace("<=", " menor ou igual a ")
            .replace("+=", " recebe ele mesmo mais ")
            .replace("-=", " recebe ele mesmo menos ")
            .replace("=", " recebe ")
            .replace("f\"", "f-string abre aspas ")
            .replace("f'", "f-string abre aspas ")
            .replace("(", " abre parênteses ")
            .replace(")", " fecha parênteses ")
            .replace("[", " abre colchetes ")
            .replace("]", " fecha colchetes ")
            .replace("{", " abre chaves ")
            .replace("}", " fecha chaves ")
            .replace(":", " dois pontos ")
            .replace("%", " resto da divisão por ")
            .replace("*", " vezes ")
            .replace("/", " dividido por ")
            .replace("_", " underline ")
    }

    fun tocarConfirmar() {
        soundPool.play(somBatida, 1f, 1f, 1, 0, 1f)
    }

    fun tocarNavegar() {
        soundPool.play(somNavegar, 0.9f, 0.9f, 1, 0, 1f)
    }

    fun tocarErro() {
        soundPool.play(somErro, 1f, 1f, 1, 0, 1f)
    }

    fun tocarDica() {
        soundPool.play(somPassar, 1f, 1f, 1, 0, 1f)
    }

    fun tocarAcerto() {
        soundPool.play(somVitoria, 1f, 1f, 1, 0, 1f)
    }

    fun tocarDerrota() {
        soundPool.play(somDerrota, 1f, 1f, 1, 0, 1f)
    }

    fun tocarCampeao() {
        soundPool.play(somCampeao, 1f, 1f, 1, 0, 1f)
    }

    fun liberar() {
        tts?.stop()
        tts?.shutdown()
        soundPool.release()
    }
}
