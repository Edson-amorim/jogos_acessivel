package com.edson.dominoacessivel

import android.app.Activity
import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityManager

/**
 * Gerenciador de Áudio e Acessibilidade dos Jogos da Luna.
 * O sintetizador de voz interno customizado (TTS com pitch) foi removido
 * para tornar o aplicativo mais leve e permitir que o leitor de telas nativo
 * do sistema operacional (TalkBack / leitor do sistema) leia diretamente
 * todas as mensagens, botões e ações sem sobreposição de vozes.
 */
class GerenciadorAudioVoz(
    private val context: Context,
    private val aoPronto: () -> Unit = {}
) {

    private val mainHandler = Handler(Looper.getMainLooper())
    private val accessibilityManager =
        context.getSystemService(Context.ACCESSIBILITY_SERVICE) as? AccessibilityManager

    var velocidadeFala: Float = 1.0f
        private set

    private val soundPool: SoundPool
    private val somBatida: Int
    private val somNavegar: Int
    private val somErro: Int
    private val somPassar: Int
    private val somVitoria: Int
    private val somDerrota: Int
    private val somCampeao: Int
    private val somAplausos: Int
    private val somPedraDomino: Int
    private val somEmbaralharDomino: Int
    private val somBaterMesa: Int

    init {
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
        somAplausos = soundPool.load(context, R.raw.aplausos, 1)
        somPedraDomino = soundPool.load(context, R.raw.pedra_domino, 1)
        somEmbaralharDomino = soundPool.load(context, R.raw.embaralhar_domino, 1)
        somBaterMesa = soundPool.load(context, R.raw.bater_mesa, 1)

        // Inicialização imediata sem bloqueio de carregamento de motor TTS
        mainHandler.post {
            aoPronto()
        }
    }

    /**
     * Envia o texto como um anúncio de acessibilidade para o leitor de telas
     * nativo do sistema (ex: TalkBack), usando a voz e velocidade oficiais configuradas pelo usuário.
     */
    fun falar(texto: String, interromper: Boolean = true, comoCrianca: Boolean = false) {
        if (texto.isBlank()) return
        mainHandler.post {
            try {
                if (interromper) {
                    accessibilityManager?.interrupt()
                }
                val activity = context as? Activity
                val decorView = activity?.window?.decorView ?: activity?.findViewById<View>(android.R.id.content)
                if (decorView != null) {
                    decorView.announceForAccessibility(texto)
                } else if (accessibilityManager?.isEnabled == true) {
                    @Suppress("DEPRECATION")
                    val event = AccessibilityEvent.obtain(AccessibilityEvent.TYPE_ANNOUNCEMENT).apply {
                        this.text.add(texto)
                        this.packageName = context.packageName
                        this.className = javaClass.name
                    }
                    accessibilityManager.sendAccessibilityEvent(event)
                }
            } catch (_: Exception) {
                // Tratamento silencioso caso o serviço de acessibilidade esteja indisponível
            }
        }
    }

    /**
     * Anuncia mensagem pelo leitor de tela do sistema.
     */
    fun falarComoLuna(texto: String, interromper: Boolean = true) {
        falar(texto, interromper = interromper, comoCrianca = false)
    }

    /**
     * Tempo de intervalo entre eventos do jogo para que o leitor de tela do sistema possa anunciar.
     */
    fun duracaoEstimadaMs(texto: String): Long {
        var palavras = 0
        var dentroPalavra = false
        for (i in 0 until texto.length) {
            if (texto[i].isWhitespace()) {
                dentroPalavra = false
            } else if (!dentroPalavra) {
                dentroPalavra = true
                palavras++
            }
        }
        val qtd = if (palavras < 1) 1 else palavras
        val segundos = qtd / (150.0 / 60.0)
        return maxOf(1400L, minOf(4500L, (segundos * 1000).toLong() + 500L))
    }

    fun tocarBatidaDomino() {
        soundPool.play(somBatida, 1f, 1f, 1, 0, 1f)
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

    fun tocarPassar() {
        soundPool.play(somPassar, 1f, 1f, 1, 0, 1f)
    }

    fun tocarDica() {
        soundPool.play(somPassar, 1f, 1f, 1, 0, 1f)
    }

    fun tocarVitoria() {
        soundPool.play(somVitoria, 1f, 1f, 1, 0, 1f)
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

    fun tocarAplausos() {
        soundPool.play(somAplausos, 1f, 1f, 1, 0, 1f)
    }

    fun tocarPedraDomino() {
        soundPool.play(somPedraDomino, 1f, 1f, 1, 0, 1f)
    }

    fun tocarEmbaralharDomino() {
        soundPool.play(somEmbaralharDomino, 1f, 1f, 1, 0, 1f)
    }

    fun tocarBaterMesa() {
        soundPool.play(somBaterMesa, 1f, 1f, 1, 0, 1f)
    }

    fun alternarVelocidadeFala(): String {
        falar("A velocidade e a voz são ajustadas diretamente nas configurações do seu leitor de tela (TalkBack).", interromper = true)
        return "controlada pelo leitor do sistema"
    }

    /**
     * Converte trechos de código Python em leitura clara para leitores de tela.
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

    fun liberar() {
        soundPool.release()
    }
}
