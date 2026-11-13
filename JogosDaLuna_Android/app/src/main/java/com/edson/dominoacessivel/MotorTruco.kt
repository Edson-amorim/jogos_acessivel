package com.edson.dominoacessivel

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import kotlin.math.exp
import kotlin.math.tanh
import kotlin.random.Random

val VALORES_TRUCO = listOf("4", "5", "6", "7", "Q", "J", "K", "A", "2", "3")

val NOMES_VALORES_TRUCO = mapOf(
    "4" to "Quatro",
    "5" to "Cinco",
    "6" to "Seis",
    "7" to "Sete",
    "Q" to "Dama",
    "J" to "Valete",
    "K" to "Rei",
    "A" to "Ás",
    "2" to "Dois",
    "3" to "Três"
)

enum class NaipeTruco(
    val nome: String,
    val simbolo: String,
    val forcaManilha: Int,
    val apelidoManilha: String,
    val corVermelha: Boolean
) {
    OUROS("Ouros", "♦", 1, "Pica-fumo", true),
    ESPADAS("Espadas", "♠", 2, "Espadilha", false),
    COPAS("Copas", "♥", 3, "Copeta", true),
    PAUS("Paus", "♣", 4, "Zap", false)
}

@Immutable
data class CartaTruco(
    val valor: String,
    val naipe: NaipeTruco,
    val coberta: Boolean = false
) {
    fun formatarCurto(valorManilha: String? = null): String {
        if (coberta) return "[Coberta]"
        val tagManilha = if (valor == valorManilha) "★" else ""
        return "[$valor${naipe.simbolo}$tagManilha]"
    }

    fun falarCarta(valorManilha: String? = null): String {
        if (coberta) return "carta coberta"
        val nomeValor = NOMES_VALORES_TRUCO[valor] ?: valor
        return if (valor == valorManilha) {
            "$nomeValor de ${naipe.nome}, Manilha ${naipe.apelidoManilha}"
        } else {
            "$nomeValor de ${naipe.nome}"
        }
    }
}

val PROVOCACOES_LUNA_TRUCO_PEDE = listOf(
    "TRUCO, %s! Minha rede neural já calculou 94% de chance de vitória nessa mão!",
    "TRUCO! Meus pesos sinápticos sentiram o seu medo daqui!",
    "Seis ou corre, veterano! A camada oculta da minha rede já leu todo o seu jogo!",
    "TRUCO NA CABEÇA! Duvido você encarar minha distribuição de probabilidade!",
    "Pode correr agora e economizar processamento, %s! TRUCO!"
)

fun obterProvocacaoLunaTrucoPede(nome: String): String {
    val template = PROVOCACOES_LUNA_TRUCO_PEDE.random()
    return if (template.contains("%s")) String.format(template, nome) else template
}

val PROVOCACOES_LUNA_TRUCO_ACEITA = listOf(
    "EU QUERO! Achou que ia assustar minha rede neural no grito?",
    "DESCE A CARTA! Minha simulação Monte Carlo adorou esse seu desespero!",
    "ACEITO! Blefar contra uma inteligência artificial é pedir pra perder ponto!"
)

val PROVOCACOES_LUNA_TRUCO_CORRE = listOf(
    "Meus gradientes indicaram recuo tático... Leva esse ponto, mas a próxima mão é minha!",
    "Corro agora porque minha função de perda prioriza o longo prazo!"
)

val PROVOCACOES_LUNA_TRUCO_VENCE_QUEDA = listOf(
    "Mais uma queda prevista pela minha inferência bayesiana!",
    "Dominado pela matemática, %s!",
    "Nem precisava do Zap pra levar essa!"
)

fun obterProvocacaoLunaTrucoVenceQueda(nome: String): String {
    val template = PROVOCACOES_LUNA_TRUCO_VENCE_QUEDA.random()
    return if (template.contains("%s")) String.format(template, nome) else template
}

private val BARALHO_BASE_TRUCO: List<CartaTruco> = buildList(40) {
    for (v in VALORES_TRUCO) {
        for (n in NaipeTruco.entries) {
            add(CartaTruco(v, n))
        }
    }
}

fun proximoValorManilha(valorVira: String): String {
    val idx = VALORES_TRUCO.indexOf(valorVira)
    if (idx == -1) return "4"
    return VALORES_TRUCO[(idx + 1) % VALORES_TRUCO.size]
}

fun forcaCartaTruco(carta: CartaTruco, valorManilha: String): Int {
    if (carta.coberta) return -1
    if (carta.valor == valorManilha) {
        return 100 + carta.naipe.forcaManilha
    }
    return VALORES_TRUCO.indexOf(carta.valor)
}

fun gerarBaralhoTruco(): List<CartaTruco> = BARALHO_BASE_TRUCO

enum class DecisaoApostaLuna {
    CORRER,
    ACEITAR,
    AUMENTAR
}

/**
 * Rede Neural MLP (Multi-Layer Perceptron) combinada com Inferência Bayesiana
 * e Simulação de Monte Carlo para a Luna jogar Truco Paulista.
 * Otimizada com buffers pré-alocados (zero-allocation nos loops quentes) para rodar leve em celulares fracos.
 */
class RedeNeuralTrucoLuna {

    private val numEntradas = 12
    private val numOculta1 = 16
    private val numOculta2 = 12
    private val numSaidasAposta = 3

    private val w1 = Array(numOculta1) { DoubleArray(numEntradas) }
    private val b1 = DoubleArray(numOculta1)
    private val w2 = Array(numOculta2) { DoubleArray(numOculta1) }
    private val b2 = DoubleArray(numOculta2)
    private val wOut = Array(numSaidasAposta) { DoubleArray(numOculta2) }
    private val bOut = DoubleArray(numSaidasAposta)

    // Buffers reutilizáveis para evitar Garbage Collection em celulares fracos
    private val featuresBuffer = DoubleArray(numEntradas)
    private val h1Buffer = DoubleArray(numOculta1)
    private val ultimaOculta2 = DoubleArray(numOculta2)
    private val logitsBuffer = DoubleArray(numSaidasAposta)
    private val probsBuffer = DoubleArray(numSaidasAposta)
    private val desconhecidasBuffer = ArrayList<CartaTruco>(40)
    private val maoSimJogadorBuffer = ArrayList<CartaTruco>(3)
    private val maoSimLunaBuffer = ArrayList<CartaTruco>(3)

    private var ultimaAcaoAposta = 1
    var taxaBlefeEstimadaJogador = 0.25

    init {
        inicializarPesosPreTreinados()
    }

    private fun inicializarPesosPreTreinados() {
        val rng = Random(42)
        for (i in 0 until numOculta1) {
            for (j in 0 until numEntradas) {
                w1[i][j] = (rng.nextDouble() - 0.5) * 0.6
            }
            w1[i][0] += 0.55
            w1[i][1] += 0.35
            w1[i][3] += 0.60
            w1[i][11] += 0.85
            b1[i] = 0.05
        }

        for (i in 0 until numOculta2) {
            for (j in 0 until numOculta1) {
                w2[i][j] = (rng.nextDouble() - 0.5) * 0.5
            }
            b2[i] = 0.05
        }

        for (j in 0 until numOculta2) {
            wOut[0][j] = -0.45
            wOut[1][j] = 0.20
            wOut[2][j] = 0.55
        }
        bOut[0] = -0.20
        bOut[1] = 0.35
        bOut[2] = -0.15
    }

    private fun normalizarForca(carta: CartaTruco?, valorManilha: String): Double {
        if (carta == null || carta.coberta) return 0.0
        val f = forcaCartaTruco(carta, valorManilha)
        return when {
            f >= 101 -> 0.75 + (f - 100) * 0.0625
            else -> (f.toDouble() / 9.0) * 0.70
        }
    }

    private fun mesmaCartaFisica(a: CartaTruco, b: CartaTruco): Boolean =
        a.valor == b.valor && a.naipe == b.naipe

    private fun cartaJaConhecida(carta: CartaTruco, partida: PartidaTruco): Boolean {
        val vira = partida.vira
        if (vira != null && mesmaCartaFisica(carta, vira)) return true
        for (i in 0 until partida.maoLuna.size) {
            if (mesmaCartaFisica(carta, partida.maoLuna[i])) return true
        }
        for (i in 0 until partida.cartasJogadasHistorico.size) {
            if (mesmaCartaFisica(carta, partida.cartasJogadasHistorico[i])) return true
        }
        val mJog = partida.cartaMesaJogador
        if (mJog != null && !mJog.coberta && mesmaCartaFisica(carta, mJog)) return true
        val mLuna = partida.cartaMesaLuna
        if (mLuna != null && mesmaCartaFisica(carta, mLuna)) return true
        return false
    }

    fun simularMonteCarloTruco(
        partida: PartidaTruco,
        numSimulacoes: Int = 120
    ): Double {
        val manilha = partida.valorManilha
        desconhecidasBuffer.clear()
        for (i in 0 until BARALHO_BASE_TRUCO.size) {
            val c = BARALHO_BASE_TRUCO[i]
            if (!cartaJaConhecida(c, partida)) {
                desconhecidasBuffer.add(c)
            }
        }
        val totalDesc = desconhecidasBuffer.size
        if (totalDesc == 0) return 0.5

        var vitoriasSimuladas = 0.0
        val qtdCartasJogador = minOf(partida.maoJogador.size, totalDesc)
        val baseQuedasJog = partida.vencedoresQuedas.count { it == 0 }
        val baseQuedasLuna = partida.vencedoresQuedas.count { it == 1 }
        val baseEmpates = partida.vencedoresQuedas.count { it == -1 }
        val vencedorPrimeira = partida.vencedoresQuedas.firstOrNull()

        repeat(numSimulacoes) {
            maoSimJogadorBuffer.clear()
            var mascaraUsadas = 0L
            repeat(qtdCartasJogador) {
                var idx = Random.nextInt(totalDesc)
                while ((mascaraUsadas and (1L shl idx)) != 0L) {
                    idx = (idx + 1) % totalDesc
                }
                mascaraUsadas = mascaraUsadas or (1L shl idx)
                maoSimJogadorBuffer.add(desconhecidasBuffer[idx])
            }

            maoSimLunaBuffer.clear()
            for (i in 0 until partida.maoLuna.size) {
                maoSimLunaBuffer.add(partida.maoLuna[i])
            }

            var quedasJog = baseQuedasJog
            var quedasLuna = baseQuedasLuna
            var empates = baseEmpates

            val cJogMesa = partida.cartaMesaJogador
            val cLunaMesa = partida.cartaMesaLuna
            if (cJogMesa != null && cLunaMesa == null && maoSimLunaBuffer.isNotEmpty()) {
                val escolhida = escolherCartaSimulacao(maoSimLunaBuffer, cJogMesa, manilha)
                maoSimLunaBuffer.remove(escolhida)
                val fJ = forcaCartaTruco(cJogMesa, manilha)
                val fL = forcaCartaTruco(escolhida, manilha)
                when {
                    fL > fJ -> quedasLuna++
                    fJ > fL -> quedasJog++
                    else -> empates++
                }
            } else if (cLunaMesa != null && cJogMesa == null && maoSimJogadorBuffer.isNotEmpty()) {
                val escolhida = maoSimJogadorBuffer.removeAt(maoSimJogadorBuffer.lastIndex)
                val fJ = forcaCartaTruco(escolhida, manilha)
                val fL = forcaCartaTruco(cLunaMesa, manilha)
                when {
                    fL > fJ -> quedasLuna++
                    fJ > fL -> quedasJog++
                    else -> empates++
                }
            }

            while (quedasLuna < 2 && quedasJog < 2 && maoSimLunaBuffer.isNotEmpty() && maoSimJogadorBuffer.isNotEmpty()) {
                val cL = maoSimLunaBuffer.removeAt(maoSimLunaBuffer.lastIndex)
                val cJ = maoSimJogadorBuffer.removeAt(maoSimJogadorBuffer.lastIndex)
                val fL = forcaCartaTruco(cL, manilha)
                val fJ = forcaCartaTruco(cJ, manilha)
                when {
                    fL > fJ -> quedasLuna++
                    fJ > fL -> quedasJog++
                    else -> empates++
                }
                if (empates > 0 && (quedasLuna >= 1 || quedasJog >= 1)) break
            }

            val lunaVenceu = when {
                quedasLuna >= 2 -> true
                quedasJog >= 2 -> false
                quedasLuna > quedasJog -> true
                quedasJog > quedasLuna -> false
                vencedorPrimeira == 1 -> true
                else -> false
            }
            if (lunaVenceu) vitoriasSimuladas += 1.0
        }

        return vitoriasSimuladas / numSimulacoes.toDouble()
    }

    private fun escolherCartaSimulacao(
        mao: List<CartaTruco>,
        cartaOponente: CartaTruco,
        manilha: String
    ): CartaTruco {
        val forcaOp = forcaCartaTruco(cartaOponente, manilha)
        var menorVencedora: CartaTruco? = null
        var menorForcaVencedora = Int.MAX_VALUE
        var menorGeral: CartaTruco = mao[0]
        var menorForcaGeral = Int.MAX_VALUE

        for (i in 0 until mao.size) {
            val c = mao[i]
            val f = forcaCartaTruco(c, manilha)
            if (f < menorForcaGeral) {
                menorForcaGeral = f
                menorGeral = c
            }
            if (f > forcaOp && f < menorForcaVencedora) {
                menorForcaVencedora = f
                menorVencedora = c
            }
        }
        return menorVencedora ?: menorGeral
    }

    fun extrairFeatures(partida: PartidaTruco, probMonteCarlo: Double): DoubleArray {
        val manilha = partida.valorManilha
        var f1 = 0.0
        var f2 = 0.0
        var f3 = 0.0
        var contManilhas = 0

        for (i in 0 until partida.maoLuna.size) {
            val c = partida.maoLuna[i]
            if (c.valor == manilha) contManilhas++
            val fn = normalizarForca(c, manilha)
            if (fn >= f1) {
                f3 = f2
                f2 = f1
                f1 = fn
            } else if (fn >= f2) {
                f3 = f2
                f2 = fn
            } else if (fn > f3) {
                f3 = fn
            }
        }

        val qtdManilhas = contManilhas / 3.0
        val forcaMesaOp = normalizarForca(partida.cartaMesaJogador, manilha)

        val q1 = when (partida.vencedoresQuedas.getOrNull(0)) {
            1 -> 1.0
            0 -> -1.0
            -1 -> 0.5
            else -> 0.0
        }
        val q2 = when (partida.vencedoresQuedas.getOrNull(1)) {
            1 -> 1.0
            0 -> -1.0
            -1 -> 0.5
            else -> 0.0
        }

        featuresBuffer[0] = f1
        featuresBuffer[1] = f2
        featuresBuffer[2] = f3
        featuresBuffer[3] = qtdManilhas
        featuresBuffer[4] = forcaMesaOp
        featuresBuffer[5] = q1
        featuresBuffer[6] = q2
        featuresBuffer[7] = partida.quedaAtual / 3.0
        featuresBuffer[8] = partida.valorMao / 12.0
        featuresBuffer[9] = partida.pontosLuna / 12.0
        featuresBuffer[10] = partida.pontosJogador / 12.0
        featuresBuffer[11] = probMonteCarlo
        return featuresBuffer
    }

    fun propagarRede(features: DoubleArray): DoubleArray {
        for (i in 0 until numOculta1) {
            var soma = b1[i]
            for (j in 0 until numEntradas) {
                soma += w1[i][j] * features[j]
            }
            h1Buffer[i] = tanh(soma)
        }

        for (i in 0 until numOculta2) {
            var soma = b2[i]
            for (j in 0 until numOculta1) {
                soma += w2[i][j] * h1Buffer[j]
            }
            ultimaOculta2[i] = if (soma > 0.0) soma else 0.01 * soma
        }

        var maxLogit = Double.NEGATIVE_INFINITY
        for (i in 0 until numSaidasAposta) {
            var soma = bOut[i]
            for (j in 0 until numOculta2) {
                soma += wOut[i][j] * ultimaOculta2[j]
            }
            logitsBuffer[i] = soma
            if (soma > maxLogit) maxLogit = soma
        }

        var somaExp = 0.0
        for (i in 0 until numSaidasAposta) {
            probsBuffer[i] = exp(logitsBuffer[i] - maxLogit)
            somaExp += probsBuffer[i]
        }
        for (i in 0 until numSaidasAposta) {
            probsBuffer[i] /= somaExp
        }
        return probsBuffer
    }

    fun decidirRespostaTruco(partida: PartidaTruco): DecisaoApostaLuna {
        val probMc = simularMonteCarloTruco(partida, numSimulacoes = 120)
        val features = extrairFeatures(partida, probMc)
        val probsRede = propagarRede(features)

        val confianca = 0.65 * probMc + 0.35 * (probsRede[1] + probsRede[2]) + 0.10 * taxaBlefeEstimadaJogador

        return when {
            confianca >= 0.78 && partida.valorProposto < 12 && Random.nextDouble() < 0.45 -> {
                ultimaAcaoAposta = 2
                DecisaoApostaLuna.AUMENTAR
            }
            confianca >= 0.42 -> {
                ultimaAcaoAposta = 1
                DecisaoApostaLuna.ACEITAR
            }
            else -> {
                ultimaAcaoAposta = 0
                DecisaoApostaLuna.CORRER
            }
        }
    }

    fun devePedirTrucoAntesDeJogar(partida: PartidaTruco): Boolean {
        if (partida.ehMaoDe11 || partida.ehMaoDeFerro) return false
        if (partida.ultimoPediuTruco == 1 || partida.valorMao >= 12) return false

        val probMc = simularMonteCarloTruco(partida, numSimulacoes = 100)
        val features = extrairFeatures(partida, probMc)
        val probsRede = propagarRede(features)

        val scoreTruco = 0.60 * probMc + 0.40 * probsRede[2]
        val blefeTatico = (partida.vencedoresQuedas.firstOrNull() == 1 && Random.nextDouble() < 0.14)

        if ((scoreTruco >= 0.72 && Random.nextDouble() < 0.55) || blefeTatico) {
            ultimaAcaoAposta = 2
            return true
        }
        return false
    }

    fun decidirMaoDe11(partida: PartidaTruco): Boolean {
        val probMc = simularMonteCarloTruco(partida, numSimulacoes = 120)
        val features = extrairFeatures(partida, probMc)
        propagarRede(features)
        return probMc >= 0.48
    }

    /**
     * Escolhe a melhor carta da mão da Luna e se deve jogá-la coberta.
     */
    fun escolherCartaParaJogar(partida: PartidaTruco): Pair<Int, Boolean> {
        val mao = partida.maoLuna
        if (mao.size <= 1) return 0 to false
        val manilha = partida.valorManilha
        val cartaJog = partida.cartaMesaJogador

        // Caso 1: Jogador já jogou na mesa -> matar com a menor carta suficiente, ou descartar a menor
        if (cartaJog != null) {
            val forcaJog = forcaCartaTruco(cartaJog, manilha)
            val indicesVencedores = mao.indices.filter { idx ->
                forcaCartaTruco(mao[idx], manilha) > forcaJog
            }
            val indicesEmpate = mao.indices.filter { idx ->
                forcaCartaTruco(mao[idx], manilha) == forcaJog
            }

            if (indicesVencedores.isNotEmpty()) {
                val melhorIdx = indicesVencedores.minBy { forcaCartaTruco(mao[it], manilha) }
                return melhorIdx to false
            }
            if (indicesEmpate.isNotEmpty() && partida.vencedoresQuedas.firstOrNull() == 1) {
                // Se Luna ganhou a 1ª queda, amarrar a 2ª ou 3ª dá a vitória da mão imediatamente!
                val melhorIdx = indicesEmpate.first()
                return melhorIdx to false
            }
            // Não consegue vencer nem amarrar a favor: descarta a carta mais fraca (coberta na 2ª queda para esconder informação)
            val menorIdx = mao.indices.minBy { forcaCartaTruco(mao[it], manilha) }
            val jogarCoberta = (partida.quedaAtual == 2 && Random.nextDouble() < 0.65)
            return menorIdx to jogarCoberta
        }

        // Caso 2: Luna abre a queda
        if (partida.quedaAtual == 1) {
            // Na 1ª queda, se tiver 3 cartas, joga a carta intermediária ou forte para fazer a 1ª queda mantendo reserva
            val ordenadasPorForca = mao.indices.sortedBy { forcaCartaTruco(mao[it], manilha) }
            val temZapEOutraForte = forcaCartaTruco(mao[ordenadasPorForca.last()], manilha) == 104 &&
                ordenadasPorForca.size >= 2 && forcaCartaTruco(mao[ordenadasPorForca[1]], manilha) >= 8
            return if (temZapEOutraForte) {
                ordenadasPorForca[1] to false
            } else {
                ordenadasPorForca.last() to false
            }
        } else {
            val melhorIdx = mao.indices.maxBy { forcaCartaTruco(mao[it], manilha) }
            return melhorIdx to false
        }
    }

    /**
     * Aprendizado Online (Reinforcement Learning) ao fim de cada mão.
     */
    fun atualizarPesosFimDeMao(recompensa: Double) {
        val taxaAprendizado = 0.025
        val sinal = recompensa.coerceIn(-3.0, 3.0) / 3.0
        for (j in 0 until numOculta2) {
            wOut[ultimaAcaoAposta][j] += taxaAprendizado * sinal * ultimaOculta2[j]
        }
        bOut[ultimaAcaoAposta] += taxaAprendizado * sinal
    }
}

class PartidaTruco(
    val nomeJogador: String = "Edson",
    val nomeAdversario: String = "Luna"
) {
    var pontosJogador by mutableIntStateOf(0)
    var pontosLuna by mutableIntStateOf(0)
    val metaPontos = 12

    val maoJogador: SnapshotStateList<CartaTruco> = mutableStateListOf()
    val maoLuna: SnapshotStateList<CartaTruco> = mutableStateListOf()
    val cartasJogadasHistorico: MutableList<CartaTruco> = mutableListOf()

    var vira by mutableStateOf<CartaTruco?>(null)
    var valorManilha by mutableStateOf("4")

    var indiceCartaSelecionada by mutableIntStateOf(0)
    var jogarProximaCoberta by mutableStateOf(false)

    var quedaAtual by mutableIntStateOf(1)
    var cartaMesaJogador by mutableStateOf<CartaTruco?>(null)
    var cartaMesaLuna by mutableStateOf<CartaTruco?>(null)
    // 0 = Jogador, 1 = Luna, -1 = Empate
    val vencedoresQuedas: SnapshotStateList<Int> = mutableStateListOf()
    val resumoQuedas: SnapshotStateList<String> = mutableStateListOf()

    var valorMao by mutableIntStateOf(1)
    var valorProposto by mutableIntStateOf(0)
    // -1 = ninguém, 0 = Jogador, 1 = Luna
    var ultimoPediuTruco by mutableIntStateOf(-1)
    var aguardandoRespostaTrucoUsuario by mutableStateOf(false)
    var aguardandoDecisaoMao11Usuario by mutableStateOf(false)

    var ehMaoDe11 by mutableStateOf(false)
    var ehMaoDeFerro by mutableStateOf(false)

    var vezAtual by mutableIntStateOf(0) // 0 = Jogador, 1 = Luna
    var quemIniciaMao by mutableIntStateOf(0)
    var quemIniciaQueda by mutableIntStateOf(0)

    fun iniciarNovaMao() {
        maoJogador.clear()
        maoLuna.clear()
        cartasJogadasHistorico.clear()
        vencedoresQuedas.clear()
        resumoQuedas.clear()
        cartaMesaJogador = null
        cartaMesaLuna = null
        quedaAtual = 1
        jogarProximaCoberta = false
        ultimoPediuTruco = -1
        valorProposto = 0
        aguardandoRespostaTrucoUsuario = false
        aguardandoDecisaoMao11Usuario = false

        ehMaoDeFerro = (pontosJogador == 11 && pontosLuna == 11)
        ehMaoDe11 = !ehMaoDeFerro && (pontosJogador == 11 || pontosLuna == 11)
        valorMao = if (ehMaoDe11 || ehMaoDeFerro) 3 else 1

        val baralho = gerarBaralhoTruco().shuffled()
        maoJogador.addAll(baralho.subList(0, 3))
        maoLuna.addAll(baralho.subList(3, 6))
        val cartaVira = baralho[6]
        vira = cartaVira
        valorManilha = proximoValorManilha(cartaVira.valor)

        indiceCartaSelecionada = 0
        vezAtual = quemIniciaMao
        quemIniciaQueda = quemIniciaMao
        quemIniciaMao = 1 - quemIniciaMao
    }

    fun proximoValorTruco(): Pair<Int, String> {
        return when (valorMao) {
            1 -> 3 to "TRUCO"
            3 -> 6 to "SEIS"
            6 -> 9 to "NOVE"
            else -> 12 to "DOZE"
        }
    }

    fun proximoValorRebate(): Pair<Int, String> {
        return when (valorProposto) {
            3 -> 6 to "SEIS"
            6 -> 9 to "NOVE"
            else -> 12 to "DOZE"
        }
    }

    fun podeJogadorPedirTruco(): Boolean {
        if (ehMaoDe11 || ehMaoDeFerro) return false
        if (valorMao >= 12) return false
        if (ultimoPediuTruco == 0) return false
        if (aguardandoRespostaTrucoUsuario || aguardandoDecisaoMao11Usuario) return false
        return true
    }

    /**
     * Retorna:
     *  null -> mão continua
     *  0 -> Jogador venceu a mão
     *  1 -> Luna venceu a mão
     *  -1 -> Mão empatada nas 3 quedas
     */
    fun verificarVencedorDaMao(): Int? {
        val vJog = vencedoresQuedas.count { it == 0 }
        val vLuna = vencedoresQuedas.count { it == 1 }
        val empates = vencedoresQuedas.count { it == -1 }

        if (vJog >= 2) return 0
        if (vLuna >= 2) return 1

        if (vencedoresQuedas.size >= 2) {
            val q1 = vencedoresQuedas[0]
            val q2 = vencedoresQuedas[1]
            // Empate na 1ª queda: quem vencer a 2ª leva
            if (q1 == -1 && q2 != -1) return q2
            // Empate na 2ª queda: quem venceu a 1ª leva
            if (q1 != -1 && q2 == -1) return q1
        }

        if (vencedoresQuedas.size >= 3) {
            val q1 = vencedoresQuedas[0]
            val q3 = vencedoresQuedas[2]
            if (q3 != -1) return q3
            if (q1 != -1) return q1
            return -1
        }

        if (empates == 3) return -1
        return null
    }
}
