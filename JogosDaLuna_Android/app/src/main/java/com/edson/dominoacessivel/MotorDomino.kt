package com.edson.dominoacessivel



import androidx.compose.runtime.Immutable

import androidx.compose.runtime.getValue

import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf

import androidx.compose.runtime.mutableStateListOf

import androidx.compose.runtime.setValue

import androidx.compose.runtime.snapshots.SnapshotStateList

import kotlin.random.Random



@Immutable

data class Pedra(val a: Int, val b: Int) {

    val soma: Int get() = a + b

    val ehCarroca: Boolean get() = a == b



    fun formatar(): String = "[$a|$b]"



    fun falarJuiz(): String {

        return if (a == b) {

            "carroça de $a"

        } else {

            "$a e $b"

        }

    }

}



val CRAQUES_SELECAO = listOf(

    "Pelé", "Garrincha", "Zico", "Romário", "Ronaldo", "Ronaldinho",

    "Rivaldo", "Kaká", "Cafu", "Roberto Carlos", "Bebeto", "Taffarel",

    "Sócrates", "Falcão", "Vampeta", "Neymar"

)



val CRAQUES_ARGENTINA = listOf(

    "Maradona", "Messi", "Riquelme", "Di María", "Batistuta",

    "Kempes", "Passarella", "Agüero", "Tevez", "Verón",

    "Mascherano", "Crespo", "Caniggia", "Aimar"

)



val PROVOCACOES_LUNA_JOGADA = listOf(
    "Achou mesmo que tinha alguma chance contra minha inferência bayesiana, %s?",
    "Simulei essa sua derrota em 500 cenários Monte Carlo antes de mover essa pedra!",
    "Sentiu o peso da matemática? Essa vai doer na sua pontuação!",
    "Sua probabilidade de vitória acabou de cair para zero!",
    "Pode chorar pro celular, veterano. Essa mesa é minha!",
    "Patético... Eu já sabia que você não tinha a resposta pra essa pedra!"
)

fun obterProvocacaoLunaJogada(nome: String): String {
    val template = PROVOCACOES_LUNA_JOGADA.random()
    return if (template.contains("%s")) String.format(template, nome) else template
}

val PROVOCACOES_LUNA_PASSA = listOf(
    "Passou a vez de novo? Que vergonha, %s! Treina mais!",
    "Entrou em colapso? Compra do monte e chora em silêncio!",
    "Adoro ver você completamente travado! Minha distribuição de probabilidade acertou em cheio!",
    "Qual a sensação de ser dominado por poucas linhas de código?",
    "Pode ir passando a vez... Só estou saboreando a sua derrota!"
)

fun obterProvocacaoLunaPassa(nome: String): String {
    val template = PROVOCACOES_LUNA_PASSA.random()
    return if (template.contains("%s")) String.format(template, nome) else template
}



val NOMES_NIVEL = listOf(

    "Fácil", "Médio", "Difícil", "Muito Difícil", "SÁDICA (Monte Carlo & Bayes)"

)



private val TODAS_PEDRAS_BASE: List<Pedra> = buildList(28) {

    for (i in 0..6) {

        for (j in i..6) {

            add(Pedra(i, j))

        }

    }

}



fun gerarPedras(): List<Pedra> = TODAS_PEDRAS_BASE



fun extremidades(tabuleiro: List<Pedra>): Pair<Int?, Int?> {

    if (tabuleiro.isEmpty()) return null to null

    return tabuleiro.first().a to tabuleiro.last().b

}



fun falarPontasJuiz(tabuleiro: List<Pedra>): String {

    if (tabuleiro.isEmpty()) return "Mesa vazia"

    val (esq, dir) = extremidades(tabuleiro)

    return if (esq == dir) {

        "$esq em ambos os lados"

    } else {

        "$esq e $dir"

    }

}



fun pedraEncaixa(pedra: Pedra, valor: Int?): Boolean {

    if (valor == null) return true

    return pedra.a == valor || pedra.b == valor

}



fun orientarParaEsquerda(pedra: Pedra, valorEsquerdo: Int): Pedra {

    return if (pedra.b == valorEsquerdo) pedra else Pedra(pedra.b, pedra.a)

}



fun orientarParaDireita(pedra: Pedra, valorDireito: Int): Pedra {

    return if (pedra.a == valorDireito) pedra else Pedra(pedra.b, pedra.a)

}



fun pedrasJogaveis(mao: List<Pedra>, tabuleiro: List<Pedra>): List<Pedra> {

    val (esquerda, direita) = extremidades(tabuleiro)

    if (esquerda == null || direita == null) return mao.toList()

    return mao.filter { pedraEncaixa(it, esquerda) || pedraEncaixa(it, direita) }

}



fun temJogadaValida(mao: List<Pedra>, tabuleiro: List<Pedra>): Boolean {

    val (esquerda, direita) = extremidades(tabuleiro)

    if (esquerda == null || direita == null) return mao.isNotEmpty()

    for (i in mao.indices) {

        val p = mao[i]

        if (p.a == esquerda || p.b == esquerda || p.a == direita || p.b == direita) return true

    }

    return false

}



fun contarPontos(mao: List<Pedra>): Int {

    var soma = 0

    for (i in mao.indices) {

        soma += mao[i].soma

    }

    return soma

}



class MotorLunaBayesMonteCarlo {

    private val todasPedras = TODAS_PEDRAS_BASE



    // Buffers reutilizáveis para evitar alocação (Garbage Collector) em celulares fracos

    private val maoSimEdson = ArrayList<Pedra>(14)

    private val maoSimLuna = ArrayList<Pedra>(14)

    private val indicesJogaveisBuffer = IntArray(14)



    private fun normalizarPedra(p: Pedra): Pedra =

        if (p.a <= p.b) p else Pedra(p.b, p.a)



    fun atualizarCrencasBayesianas(

        jogo: JogoDomino,

        idxLuna: Int

    ): Pair<DoubleArray, List<Pedra>> {

        val idxEdson = if (idxLuna != 0) 0 else 1

        val maoLuna = jogo.maos[idxLuna] ?: emptyList()

        val desconhecidas = ArrayList<Pedra>(21)



        for (i in todasPedras.indices) {

            val candidata = todasPedras[i]

            var conhecida = false

            for (j in jogo.tabuleiro.indices) {

                val t = normalizarPedra(jogo.tabuleiro[j])

                if (t.a == candidata.a && t.b == candidata.b) {

                    conhecida = true

                    break

                }

            }

            if (!conhecida) {

                for (j in maoLuna.indices) {

                    val m = normalizarPedra(maoLuna[j])

                    if (m.a == candidata.a && m.b == candidata.b) {

                        conhecida = true

                        break

                    }

                }

            }

            if (!conhecida) {

                desconhecidas.add(candidata)

            }

        }



        val passagens = jogo.passagensJogadores[idxEdson] ?: emptySet()

        val pesos = DoubleArray(desconhecidas.size)

        var totalProb = 0.0



        for (i in desconhecidas.indices) {

            val p = desconhecidas[i]

            var prob = 1.0

            for (numTravado in passagens) {

                if (p.a == numTravado || p.b == numTravado) {

                    prob = 0.001

                    break

                }

            }

            pesos[i] = prob

            totalProb += prob

        }



        if (totalProb > 0.0) {

            for (i in pesos.indices) {

                pesos[i] /= totalProb

            }

        }



        return pesos to desconhecidas

    }



    private fun preencherMaoAmostrada(

        pedrasOcultas: List<Pedra>,

        pesosBase: DoubleArray,

        quantidade: Int,

        destino: ArrayList<Pedra>

    ) {

        destino.clear()

        val n = pedrasOcultas.size

        val meta = minOf(quantidade, n)

        if (meta <= 0) return



        // Máscara booleana na pilha/registrador (até 28 bits em um único Int!)

        var mascaraUsadas = 0

        var somaRestante = 0.0

        for (i in 0 until n) somaRestante += pesosBase[i]



        repeat(meta) {

            if (somaRestante <= 1e-9) {

                // Fallback uniforme entre as não usadas

                for (i in 0 until n) {

                    if ((mascaraUsadas and (1 shl i)) == 0) {

                        mascaraUsadas = mascaraUsadas or (1 shl i)

                        destino.add(pedrasOcultas[i])

                        break

                    }

                }

            } else {

                var r = Random.nextDouble() * somaRestante

                var escolhido = -1

                for (i in 0 until n) {

                    if ((mascaraUsadas and (1 shl i)) == 0) {

                        r -= pesosBase[i]

                        if (r <= 0.0) {

                            escolhido = i

                            break

                        }

                    }

                }

                if (escolhido == -1) {

                    for (i in n - 1 downTo 0) {

                        if ((mascaraUsadas and (1 shl i)) == 0) {

                            escolhido = i

                            break

                        }

                    }

                }

                if (escolhido != -1) {

                    mascaraUsadas = mascaraUsadas or (1 shl escolhido)

                    somaRestante -= pesosBase[escolhido]

                    destino.add(pedrasOcultas[escolhido])

                }

            }

        }

    }



    fun simularMonteCarlo(

        jogo: JogoDomino,

        idxLuna: Int,

        pedra: Pedra,

        lado: String,

        pesosEdson: DoubleArray,

        pedrasOcultas: List<Pedra>,

        numSimulacoes: Int = 120

    ): Double {

        if (pedrasOcultas.isEmpty()) return 0.0

        var vitorias = 0.0

        val idxEdson = if (idxLuna != 0) 0 else 1

        val qtdEdson = jogo.maos[idxEdson]?.size ?: 0

        val maoLunaOriginal = jogo.maos[idxLuna] ?: emptyList()

        val (esqInicial, dirInicial) = extremidades(jogo.tabuleiro)



        // Calcula as pontas iniciais após a jogada candidata apenas uma vez

        val esqAposJogada: Int

        val dirAposJogada: Int

        if (esqInicial == null || dirInicial == null) {

            esqAposJogada = pedra.a

            dirAposJogada = pedra.b

        } else if (lado == "ESQUERDA") {

            val orientada = orientarParaEsquerda(pedra, esqInicial)

            esqAposJogada = orientada.a

            dirAposJogada = dirInicial

        } else {

            val orientada = orientarParaDireita(pedra, dirInicial)

            esqAposJogada = esqInicial

            dirAposJogada = orientada.b

        }



        repeat(numSimulacoes) {

            preencherMaoAmostrada(pedrasOcultas, pesosEdson, qtdEdson, maoSimEdson)

            maoSimLuna.clear()

            var removida = false

            for (i in maoLunaOriginal.indices) {

                val p = maoLunaOriginal[i]

                if (!removida && p == pedra) {

                    removida = true

                } else {

                    maoSimLuna.add(p)

                }

            }



            var esqSim = esqAposJogada

            var dirSim = dirAposJogada

            var vez = idxEdson

            var bloqueios = 0



            while (maoSimLuna.isNotEmpty() && maoSimEdson.isNotEmpty() && bloqueios < 2) {

                val mAtual = if (vez == idxEdson) maoSimEdson else maoSimLuna

                var qtdValidas = 0

                for (i in 0 until mAtual.size) {

                    val c = mAtual[i]

                    if (c.a == esqSim || c.b == esqSim || c.a == dirSim || c.b == dirSim) {

                        indicesJogaveisBuffer[qtdValidas++] = i

                    }

                }



                if (qtdValidas > 0) {

                    val idxEscolhido = indicesJogaveisBuffer[Random.nextInt(qtdValidas)]

                    val pJogada = mAtual.removeAt(idxEscolhido)

                    if (pJogada.a == esqSim) {

                        esqSim = pJogada.b

                    } else if (pJogada.b == esqSim) {

                        esqSim = pJogada.a

                    } else if (pJogada.a == dirSim) {

                        dirSim = pJogada.b

                    } else {

                        dirSim = pJogada.a

                    }

                    bloqueios = 0

                } else {

                    bloqueios++

                }

                vez = 1 - vez

            }



            when {

                maoSimLuna.isEmpty() -> vitorias += 3.0

                maoSimEdson.isEmpty() -> vitorias -= 3.0

                else -> {

                    val ptsLuna = contarPontos(maoSimLuna)

                    val ptsEdson = contarPontos(maoSimEdson)

                    if (ptsLuna < ptsEdson) vitorias += 1.5 else vitorias -= 1.5

                }

            }

        }

        return vitorias

    }



    fun escolherMelhorJogada(

        jogo: JogoDomino,

        idxLuna: Int,

        validas: List<Pedra>

    ): Pair<Pedra, String?> {

        val (esqAtual, dirAtual) = extremidades(jogo.tabuleiro)

        // Calcula crenças bayesianas apenas 1 vez por turno!

        val (pesosEdson, pedrasOcultas) = atualizarCrencasBayesianas(jogo, idxLuna)



        var melhorPontuacao = Double.NEGATIVE_INFINITY

        var melhorPedra = validas.first()

        var melhorLado: String? = null



        for (i in validas.indices) {

            val p = validas[i]

            val testaEsquerda = (esqAtual == null || pedraEncaixa(p, esqAtual))

            val testaDireita = (esqAtual != null && dirAtual != null && dirAtual != esqAtual && pedraEncaixa(p, dirAtual))



            if (testaEsquerda) {

                val scoreMc = simularMonteCarlo(jogo, idxLuna, p, "ESQUERDA", pesosEdson, pedrasOcultas, numSimulacoes = 120)

                val scoreHeuristico = p.soma * 2.0 + if (p.ehCarroca) 15.0 else 0.0

                val totalScore = scoreMc + scoreHeuristico

                if (totalScore > melhorPontuacao) {

                    melhorPontuacao = totalScore

                    melhorPedra = p

                    melhorLado = "ESQUERDA"

                }

            }

            if (testaDireita) {

                val scoreMc = simularMonteCarlo(jogo, idxLuna, p, "DIREITA", pesosEdson, pedrasOcultas, numSimulacoes = 120)

                val scoreHeuristico = p.soma * 2.0 + if (p.ehCarroca) 15.0 else 0.0

                val totalScore = scoreMc + scoreHeuristico

                if (totalScore > melhorPontuacao) {

                    melhorPontuacao = totalScore

                    melhorPedra = p

                    melhorLado = "DIREITA"

                }

            }

        }

        return melhorPedra to melhorLado

    }

}



class JogoDomino(

    val modo: Int,

    nomeJogador: String,

    nomeParceiro: String? = null,

    historicoAdversarios: List<String> = emptyList()

) {

    val nomes: List<String>

    val advPrincipal: String

    val maos: MutableMap<Int, SnapshotStateList<Pedra>> = mutableMapOf()

    val monte: SnapshotStateList<Pedra> = mutableStateListOf()

    val numJogadores: Int

    val tabuleiro: SnapshotStateList<Pedra> = mutableStateListOf()

    var indiceSelecionado by mutableIntStateOf(0)

    val passagensJogadores: MutableMap<Int, MutableSet<Int>> = mutableMapOf()

    var jogadorAtual by mutableIntStateOf(0)

    var pedraInicial: Pedra? by mutableStateOf(null)



    init {

        val pedras = gerarPedras().shuffled()

        when (modo) {

            1 -> {

                var opcoesAdv = CRAQUES_ARGENTINA.filter { it !in historicoAdversarios }

                if (opcoesAdv.isEmpty()) opcoesAdv = CRAQUES_ARGENTINA

                val adv = opcoesAdv.random()

                advPrincipal = adv

                nomes = listOf(nomeJogador, adv)

                maos[0] = mutableStateListOf<Pedra>().apply { addAll(pedras.subList(0, 7)) }

                maos[1] = mutableStateListOf<Pedra>().apply { addAll(pedras.subList(7, 14)) }

                monte.addAll(pedras.subList(14, 28))

                numJogadores = 2

            }

            3 -> {
                advPrincipal = "Luna"
                nomes = listOf(nomeJogador, "Luna")
                maos[0] = mutableStateListOf<Pedra>().apply { addAll(pedras.subList(0, 7)) }
                maos[1] = mutableStateListOf<Pedra>().apply { addAll(pedras.subList(7, 14)) }
                monte.addAll(pedras.subList(14, 28))
                numJogadores = 2
            }
            4 -> {
                val amigo = nomeParceiro ?: "Amigo Bluetooth"
                advPrincipal = amigo
                nomes = listOf(nomeJogador, amigo)
                maos[0] = mutableStateListOf<Pedra>().apply { addAll(pedras.subList(0, 7)) }
                maos[1] = mutableStateListOf<Pedra>().apply { addAll(pedras.subList(7, 14)) }
                monte.addAll(pedras.subList(14, 28))
                numJogadores = 2
            }

            else -> {

                val parceiro = nomeParceiro ?: CRAQUES_SELECAO.first()

                var opcoesAdv = CRAQUES_SELECAO.filter { it != parceiro && it !in historicoAdversarios }

                if (opcoesAdv.size < 2) {

                    opcoesAdv = CRAQUES_SELECAO.filter { it != parceiro }

                }

                val escolhidos = opcoesAdv.shuffled().take(2)

                advPrincipal = escolhidos[0]

                nomes = listOf(nomeJogador, escolhidos[0], parceiro, escolhidos[1])

                maos[0] = mutableStateListOf<Pedra>().apply { addAll(pedras.subList(0, 7)) }

                maos[1] = mutableStateListOf<Pedra>().apply { addAll(pedras.subList(7, 14)) }

                maos[2] = mutableStateListOf<Pedra>().apply { addAll(pedras.subList(14, 21)) }

                maos[3] = mutableStateListOf<Pedra>().apply { addAll(pedras.subList(21, 28)) }

                numJogadores = 4

            }

        }



        for (i in 0 until numJogadores) {

            passagensJogadores[i] = mutableSetOf()

        }

        jogadorAtual = decidirQuemComeca()

        if (jogadorAtual == 0 && pedraInicial != null) {

            val idx = maos[0]?.indexOf(pedraInicial) ?: -1

            if (idx != -1) indiceSelecionado = idx

        }

    }



    private fun decidirQuemComeca(): Int {

        // Regra clássica do dominó: inicia quem tem a maior carroça (de (6,6) até (0,0))

        for (v in 6 downTo 0) {

            val carroca = Pedra(v, v)

            for (idx in 0 until numJogadores) {

                if (maos[idx]?.contains(carroca) == true) {

                    pedraInicial = carroca

                    return idx

                }

            }

        }



        // Se ninguém tem carroça (ex: todas no monte em 1x1), inicia quem tem a maior pedra

        var maiorSoma = -1

        var maiorPonta = -1

        var donoMaior = 0

        var maiorPedra: Pedra? = null

        for (idx in 0 until numJogadores) {

            for (pedra in maos[idx] ?: emptyList()) {

                val ponta = maxOf(pedra.a, pedra.b)

                if (pedra.soma > maiorSoma || (pedra.soma == maiorSoma && ponta > maiorPonta)) {

                    maiorSoma = pedra.soma

                    maiorPonta = ponta

                    donoMaior = idx

                    maiorPedra = pedra

                }

            }

        }

        pedraInicial = maiorPedra

        return donoMaior

    }



    fun removerPedra(pedra: Pedra, idxJogador: Int) {

        val mao = maos[idxJogador] ?: return

        mao.remove(pedra)

        if (idxJogador == 0) {

            indiceSelecionado = if (mao.isEmpty()) 0 else indiceSelecionado.coerceIn(0, mao.lastIndex)

        }

    }



    fun pescarDoMonte(idxJogador: Int): Pedra? {

        if (monte.isEmpty()) return null

        val pedra = monte.removeAt(monte.lastIndex)

        maos[idxJogador]?.add(pedra)

        return pedra

    }



    fun estaBloqueado(): Boolean {

        if ((modo == 1 || modo == 3) && monte.isNotEmpty()) return false

        for (i in 0 until numJogadores) {

            if (temJogadaValida(maos[i] ?: emptyList(), tabuleiro)) return false

        }

        return true

    }

}

