package com.edson.dominoacessivel

import android.view.KeyEvent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class SubTelaYugioh {
    ESCOLHA_DECK,
    PERSONALIZAR_CARTAS,
    BLUETOOTH_SALA,
    ARENA_DUELO,
    FIM_DUELO
}

class EstadoYugiohLuna {
    var subTela by mutableStateOf(SubTelaYugioh.ESCOLHA_DECK)
    var indiceDeckJogador by mutableIntStateOf(0)
    var indiceDeckOponente by mutableIntStateOf(3) // Padrão: enfrentar Luna (Deck 4)
    var vitoriasYugioh by mutableIntStateOf(0)
    var torneiosVencidosYugioh by mutableIntStateOf(0)
    var bancoDados: BancoDadosJogosLuna? = null
    var modoJogo by mutableStateOf(ModoJogoYugioh.DUELO_AVULSO)
    var dadosTorneio by mutableStateOf(DadosTorneioYugioh())
    var versao by mutableIntStateOf(0)

    val idsCartasDeckPersonalizado = mutableStateListOf<Int>().apply {
        addAll(CatalogoYugiohLuna.decksPredefinidos[0].idsCartasPadrao)
    }

    var partidaAtual by mutableStateOf<PartidaYugiohLuna?>(null)
    var ultimaNarracao by mutableStateOf(
        "Bem-vindo ao Yu-Gi-Oh! Duelo Acessível da Luna! Escolha seu Deck para duelar."
    )
    var jobTurnoIa: Job? = null
    var gerenciadorBluetooth by mutableStateOf<GerenciadorBluetoothDuelo?>(null)

    fun abrirTelaBluetooth(context: android.content.Context, audioVoz: GerenciadorAudioVoz) {
        if (gerenciadorBluetooth == null) {
            gerenciadorBluetooth = GerenciadorBluetoothDuelo(context).apply {
                aoReceberComando = { cmd -> processarComandoBluetooth(cmd, audioVoz) }
            }
        }
        gerenciadorBluetooth?.atualizarDispositivosPareados()
        subTela = SubTelaYugioh.BLUETOOTH_SALA
        val msg = "Modo Duelo Bluetooth! Crie uma sala para esperar seu oponente ou conecte ao celular pareado."
        ultimaNarracao = msg
        audioVoz.falar(msg, interromper = true)
        versao++
    }

    fun iniciarDueloBluetooth(nomeOponenteRemoto: String, idDeckRemoto: Int, audioVoz: GerenciadorAudioVoz, nomeJogador: String) {
        modoJogo = ModoJogoYugioh.DUELO_BLUETOOTH
        val decks = CatalogoYugiohLuna.decksPredefinidos
        val deckJogInfo = decks[indiceDeckJogador]
        val deckOpInfo = decks.firstOrNull { it.id == idDeckRemoto } ?: decks[0]

        val cartasJog = idsCartasDeckPersonalizado.map { CatalogoYugiohLuna.buscarCartaPorId(it) }
        val cartasOp = deckOpInfo.idsCartasPadrao.map { CatalogoYugiohLuna.buscarCartaPorId(it) }

        val nova = PartidaYugiohLuna(
            nomeJogador = nomeJogador,
            nomeOponente = nomeOponenteRemoto,
            nomeDeckJogador = deckJogInfo.nomeDeck,
            nomeDeckOponente = deckOpInfo.nomeDeck,
            cartasIniciaisJogador = cartasJog,
            cartasIniciaisOponente = cartasOp
        )
        partidaAtual = nova
        subTela = SubTelaYugioh.ARENA_DUELO
        versao++

        val msg = "📡 Duelo Bluetooth Conectado! $nomeJogador contra $nomeOponenteRemoto! " +
            "Turno 1: Sua vez, $nomeJogador! " + obterTextoMaoAtualizada(nova)
        ultimaNarracao = msg
        audioVoz.tocarConfirmar()
        audioVoz.falar(msg, interromper = true)
    }

    fun processarComandoBluetooth(cmd: String, audioVoz: GerenciadorAudioVoz) {
        val partes = cmd.split(":")
        when (partes.getOrNull(0)) {
            "START" -> {
                val nomeOp = partes.getOrNull(1) ?: "Oponente Bluetooth"
                val deckOpId = partes.getOrNull(2)?.toIntOrNull() ?: 1
                iniciarDueloBluetooth(nomeOp, deckOpId, audioVoz, "Duelista")
            }
            "JOGAR" -> {
                val idCarta = partes.getOrNull(1)?.toIntOrNull() ?: 0
                val atk = partes.getOrNull(2)?.toBooleanStrictOrNull() ?: true
                partidaAtual?.let { p ->
                    val carta = CatalogoYugiohLuna.buscarCartaPorId(idCarta)
                    if (carta.tipo == TipoCartaYugioh.MONSTRO) {
                        p.monstrosOponente.add(MonstroEmCampoYugioh(carta, carta.atk, carta.def, atk, !atk))
                        audioVoz.tocarConfirmar()
                        val pos = if (atk) "em Posição de Ataque" else "em Posição de Defesa"
                        val msg = "${p.nomeOponente} invocou ${carta.nome} $pos via Bluetooth!"
                        ultimaNarracao = msg
                        audioVoz.falar(msg, interromper = true)
                    } else if (carta.tipo == TipoCartaYugioh.ARMADILHA) {
                        p.armadilhaOponente = carta
                        audioVoz.tocarConfirmar()
                        val msg = "${p.nomeOponente} colocou uma Armadilha virada para baixo."
                        ultimaNarracao = msg
                        audioVoz.falar(msg, interromper = true)
                    }
                    versao++
                }
            }
            "ATACAR" -> {
                partidaAtual?.let { p ->
                    val mOp = p.monstrosOponente.firstOrNull()
                    if (mOp != null) {
                        val mJog = p.monstrosJogador.firstOrNull()
                        if (mJog == null) {
                            p.lpJogador = (p.lpJogador - mOp.atkAtual).coerceAtLeast(0)
                            audioVoz.tocarDerrota()
                            val msg = "${p.nomeOponente} atacou seus Pontos de Vida diretamente com ${mOp.cartaBase.nome} causando ${mOp.atkAtual} de dano!"
                            ultimaNarracao = msg
                            audioVoz.falar(msg, interromper = true)
                        } else if (mOp.atkAtual > mJog.atkAtual) {
                            val dif = mOp.atkAtual - mJog.atkAtual
                            p.lpJogador = (p.lpJogador - dif).coerceAtLeast(0)
                            p.monstrosJogador.remove(mJog)
                            p.cemiterioJogador.add(mJog.cartaBase)
                            audioVoz.tocarDerrota()
                            val msg = "${mOp.cartaBase.nome} destruiu seu ${mJog.cartaBase.nome} causando $dif de dano!"
                            ultimaNarracao = msg
                            audioVoz.falar(msg, interromper = true)
                        }
                        checarVitoriaOuDerrota(p, audioVoz)
                        versao++
                    }
                }
            }
            "PASSAR" -> {
                partidaAtual?.let { p ->
                    p.turnoNumero++
                    p.vezDoJogador = true
                    p.invocacaoNormalUsadaNoTurno = false
                    val c = p.comprarCarta(paraJogador = true)
                    val txt = c?.let { "Você comprou ${it.nome}." } ?: ""
                    val msg = "${p.nomeOponente} encerrou a vez. Turno ${p.turnoNumero}: Sua vez! $txt"
                    ultimaNarracao = msg
                    audioVoz.tocarConfirmar()
                    audioVoz.falar(msg, interromper = true)
                    versao++
                }
            }
        }
    }

    fun selecionarDeckBase(indice: Int, audioVoz: GerenciadorAudioVoz, nomeJogador: String) {
        val decks = CatalogoYugiohLuna.decksPredefinidos
        indiceDeckJogador = indice.coerceIn(0, decks.lastIndex)
        idsCartasDeckPersonalizado.clear()
        idsCartasDeckPersonalizado.addAll(decks[indiceDeckJogador].idsCartasPadrao)
        val deck = decks[indiceDeckJogador]
        val msg = "Deck selecionado para $nomeJogador: ${deck.nomeDeck}. ${deck.descricao} Total de ${idsCartasDeckPersonalizado.size} cartas."
        ultimaNarracao = msg
        audioVoz.tocarConfirmar()
        audioVoz.falar(msg, interromper = true)
        versao++
    }

    fun alternarOponente(audioVoz: GerenciadorAudioVoz) {
        val decks = CatalogoYugiohLuna.decksPredefinidos
        indiceDeckOponente = (indiceDeckOponente + 1) % decks.size
        val op = decks[indiceDeckOponente]
        val msg = "Oponente alterado para ${op.personagem} usando ${op.nomeDeck}."
        ultimaNarracao = msg
        audioVoz.tocarNavegar()
        audioVoz.falar(msg, interromper = true)
        versao++
    }

    fun contarCopiasNoDeck(idCarta: Int): Int {
        return idsCartasDeckPersonalizado.count { it == idCarta }
    }

    fun adicionarCartaAoDeck(carta: CartaYugioh, audioVoz: GerenciadorAudioVoz) {
        val copias = contarCopiasNoDeck(carta.id)
        if (copias >= 3) {
            audioVoz.tocarErro()
            val msg = "Limite máximo de 3 cópias de ${carta.nome} atingido."
            ultimaNarracao = msg
            audioVoz.falar(msg, interromper = true)
            return
        }
        if (idsCartasDeckPersonalizado.size >= 30) {
            audioVoz.tocarErro()
            val msg = "Seu Deck já está com o limite máximo de 30 cartas. Remova uma carta antes."
            ultimaNarracao = msg
            audioVoz.falar(msg, interromper = true)
            return
        }
        idsCartasDeckPersonalizado.add(carta.id)
        audioVoz.tocarConfirmar()
        val msg = "Adicionada 1 cópia de ${carta.nome}. Agora você tem ${copias + 1} cópia(s) no Deck (${idsCartasDeckPersonalizado.size} cartas no total)."
        ultimaNarracao = msg
        audioVoz.falar(msg, interromper = true)
        versao++
    }

    fun removerCartaDoDeck(carta: CartaYugioh, audioVoz: GerenciadorAudioVoz) {
        val copias = contarCopiasNoDeck(carta.id)
        if (copias <= 0) {
            audioVoz.tocarErro()
            val msg = "Você não tem cópias de ${carta.nome} no Deck."
            ultimaNarracao = msg
            audioVoz.falar(msg, interromper = true)
            return
        }
        if (idsCartasDeckPersonalizado.size <= 12) {
            audioVoz.tocarErro()
            val msg = "O Deck precisa ter pelo menos 12 cartas para iniciar o duelo."
            ultimaNarracao = msg
            audioVoz.falar(msg, interromper = true)
            return
        }
        idsCartasDeckPersonalizado.remove(carta.id)
        audioVoz.tocarNavegar()
        val msg = "Removida 1 cópia de ${carta.nome}. Restam ${copias - 1} cópia(s) no Deck (${idsCartasDeckPersonalizado.size} cartas no total)."
        ultimaNarracao = msg
        audioVoz.falar(msg, interromper = true)
        versao++
    }

    fun iniciarDuelo(nomeJogador: String, audioVoz: GerenciadorAudioVoz) {
        jobTurnoIa?.cancel()
        modoJogo = ModoJogoYugioh.DUELO_AVULSO
        val decks = CatalogoYugiohLuna.decksPredefinidos
        val deckJogInfo = decks[indiceDeckJogador]
        val deckOpInfo = decks[indiceDeckOponente]

        val cartasJog = idsCartasDeckPersonalizado.map { CatalogoYugiohLuna.buscarCartaPorId(it) }
        val ehLuna = deckOpInfo.personagem.contains("Luna", ignoreCase = true)
        val cartasOp = if (ehLuna) {
            val aprendidas = bancoDados?.obterCartasFavoritasJogador(limite = 4) ?: emptyList()
            CatalogoYugiohLuna.gerarDeckAleatorioLuna(aprendidas)
        } else {
            deckOpInfo.idsCartasPadrao.map { CatalogoYugiohLuna.buscarCartaPorId(it) }
        }

        val nova = PartidaYugiohLuna(
            nomeJogador = nomeJogador,
            nomeOponente = deckOpInfo.personagem,
            nomeDeckJogador = deckJogInfo.nomeDeck,
            nomeDeckOponente = deckOpInfo.nomeDeck,
            cartasIniciaisJogador = cartasJog,
            cartasIniciaisOponente = cartasOp
        )
        partidaAtual = nova
        subTela = SubTelaYugioh.ARENA_DUELO
        versao++

        if (nova.checarVitoriaExodia()) {
            checarVitoriaOuDerrota(nova, audioVoz)
            return
        }

        val msg = "É hora do Duelo! Duelista $nomeJogador (${deckJogInfo.personagem}) contra ${deckOpInfo.personagem}! " +
            "Ambos começam com 4000 Pontos de Vida e 5 cartas na mão. " +
            "Turno 1: Sua vez, $nomeJogador! " + obterTextoMaoAtualizada(nova)
        ultimaNarracao = msg
        audioVoz.tocarConfirmar()
        audioVoz.falar(msg, interromper = true)
    }

    fun iniciarTorneio(nomeJogador: String, audioVoz: GerenciadorAudioVoz) {
        jobTurnoIa?.cancel()
        modoJogo = ModoJogoYugioh.TORNEIO

        // Sorteia 2 oponentes distintos para Quartas e Semifinal
        val todosDecks = CatalogoYugiohLuna.decksPredefinidos
        val rivaisPossiveis = todosDecks.filterIndexed { idx, _ -> idx != 3 && idx != indiceDeckJogador }
        val rivaisSorteados = rivaisPossiveis.shuffled(kotlin.random.Random(System.nanoTime()))
        val opQuartas = rivaisSorteados.getOrElse(0) { todosDecks[0] }
        val opSemis = rivaisSorteados.getOrElse(1) { todosDecks[1] }

        // Gera o Deck dinâmico da Luna para a Grande Final, incorporando as táticas aprendidas do duelista
        val aprendidas = bancoDados?.obterCartasFavoritasJogador(limite = 4) ?: emptyList()
        val cartasLuna = CatalogoYugiohLuna.gerarDeckAleatorioLuna(aprendidas)
        val titulosLuna = listOf(
            "Deck Surpresa Imprevisível da Luna",
            "Deck Caos Mágico da Luna",
            "Deck Lendário Secreto da Luna",
            "Deck Desafio Extremo da Luna",
            "Deck Poder Cósmico da Luna"
        )
        val nomeDeckLuna = titulosLuna.random(kotlin.random.Random(System.nanoTime()))

        dadosTorneio = DadosTorneioYugioh(
            faseAtual = 1,
            oponenteQuartas = opQuartas,
            oponenteSemifinal = opSemis,
            deckLunaFinalCartas = cartasLuna,
            nomeDeckLunaFinal = nomeDeckLuna
        )

        val deckJogInfo = todosDecks[indiceDeckJogador]
        val cartasJog = idsCartasDeckPersonalizado.map { CatalogoYugiohLuna.buscarCartaPorId(it) }
        val cartasOp = opQuartas.idsCartasPadrao.map { CatalogoYugiohLuna.buscarCartaPorId(it) }

        val nova = PartidaYugiohLuna(
            nomeJogador = nomeJogador,
            nomeOponente = opQuartas.personagem,
            nomeDeckJogador = deckJogInfo.nomeDeck,
            nomeDeckOponente = opQuartas.nomeDeck,
            cartasIniciaisJogador = cartasJog,
            cartasIniciaisOponente = cartasOp
        )
        partidaAtual = nova
        subTela = SubTelaYugioh.ARENA_DUELO
        versao++

        if (nova.checarVitoriaExodia()) {
            checarVitoriaOuDerrota(nova, audioVoz)
            return
        }

        val msg = "🏆 Bem-vindo ao Grande Torneio da Luna! São 3 fases eliminatórias: Quartas de Final, Semifinal e a Grande Final contra Luna, a Campeã Invicta! " +
            "Fase 1: Quartas de Final! Seu primeiro oponente sorteado é ${opQuartas.personagem} usando ${opQuartas.nomeDeck}! " +
            "Ambos começam com 4000 Pontos de Vida. Turno 1: Sua vez, $nomeJogador! " + obterTextoMaoAtualizada(nova)
        ultimaNarracao = msg
        audioVoz.tocarConfirmar()
        audioVoz.falar(msg, interromper = true)
    }

    fun avancarFaseTorneio(nomeJogador: String, audioVoz: GerenciadorAudioVoz) {
        jobTurnoIa?.cancel()
        val todosDecks = CatalogoYugiohLuna.decksPredefinidos
        val deckJogInfo = todosDecks[indiceDeckJogador]
        val cartasJog = idsCartasDeckPersonalizado.map { CatalogoYugiohLuna.buscarCartaPorId(it) }

        if (dadosTorneio.faseAtual == 1) {
            dadosTorneio.faseAtual = 2
            val opSemis = dadosTorneio.oponenteSemifinal ?: todosDecks[1]
            val cartasOp = opSemis.idsCartasPadrao.map { CatalogoYugiohLuna.buscarCartaPorId(it) }

            val nova = PartidaYugiohLuna(
                nomeJogador = nomeJogador,
                nomeOponente = opSemis.personagem,
                nomeDeckJogador = deckJogInfo.nomeDeck,
                nomeDeckOponente = opSemis.nomeDeck,
                cartasIniciaisJogador = cartasJog,
                cartasIniciaisOponente = cartasOp
            )
            partidaAtual = nova
            subTela = SubTelaYugioh.ARENA_DUELO
            versao++

            val msg = "⚔️ SEMIFINAL DO TORNEIO DA LUNA! Duelista $nomeJogador contra ${opSemis.personagem} (${opSemis.nomeDeck})! " +
                "Quem vencer avança para a Grande Final contra a Luna! Turno 1: Sua vez, $nomeJogador! " + obterTextoMaoAtualizada(nova)
            ultimaNarracao = msg
            audioVoz.tocarConfirmar()
            audioVoz.falar(msg, interromper = true)
        } else if (dadosTorneio.faseAtual == 2) {
            dadosTorneio.faseAtual = 3
            val cartasLuna = dadosTorneio.deckLunaFinalCartas
            val nomeDeckLuna = dadosTorneio.nomeDeckLunaFinal

            val nova = PartidaYugiohLuna(
                nomeJogador = nomeJogador,
                nomeOponente = "Luna (A Grande Finalista)",
                nomeDeckJogador = deckJogInfo.nomeDeck,
                nomeDeckOponente = nomeDeckLuna,
                cartasIniciaisJogador = cartasJog,
                cartasIniciaisOponente = cartasLuna
            )
            partidaAtual = nova
            subTela = SubTelaYugioh.ARENA_DUELO
            versao++

            val msg = "👑 GRANDE FINAL DO TORNEIO DA LUNA! Duelista $nomeJogador contra a Pequena LUNA! " +
                "A Luna é uma adorável criança prodígio de 8 anos, cheia de imaginação, que montou seu $nomeDeckLuna de forma divertida e aleatória para esta Final! " +
                "O troféu dourado do torneio está em jogo! Turno 1: Sua vez, $nomeJogador! " + obterTextoMaoAtualizada(nova)
            ultimaNarracao = msg
            audioVoz.tocarConfirmar()
            audioVoz.falar(msg, interromper = true)
        }
    }

    fun reiniciarTorneio(nomeJogador: String, audioVoz: GerenciadorAudioVoz) {
        iniciarTorneio(nomeJogador, audioVoz)
    }

    fun obterTextoMaoAtualizada(p: PartidaYugiohLuna): String {
        if (p.maoJogador.isEmpty()) {
            return "Sua mão agora está vazia."
        }
        val cartasTexto = p.maoJogador.mapIndexed { i, c ->
            val sel = if (i == p.indiceCartaMaoSelecionada) " [selecionada]" else ""
            "Carta ${i + 1}$sel: ${c.nome}"
        }.joinToString(", ")
        val cartaAtual = p.maoJogador.getOrNull(p.indiceCartaMaoSelecionada)?.let { " Carta pronta para jogar: ${it.falarCompleta()}" } ?: ""
        return "Sua mão atualizada tem ${p.maoJogador.size} carta(s): $cartasTexto.$cartaAtual"
    }

    fun narrarMaoAtualizada(audioVoz: GerenciadorAudioVoz) {
        val p = partidaAtual ?: return
        val msg = obterTextoMaoAtualizada(p)
        ultimaNarracao = msg
        audioVoz.tocarNavegar()
        audioVoz.falar(msg, interromper = true)
        versao++
    }

    fun navegarMaoAnterior(audioVoz: GerenciadorAudioVoz) {
        val p = partidaAtual ?: return
        if (p.maoJogador.isEmpty()) {
            audioVoz.tocarErro()
            audioVoz.falar("Sua mão está vazia.", interromper = true)
            return
        }
        p.indiceCartaMaoSelecionada = (p.indiceCartaMaoSelecionada - 1 + p.maoJogador.size) % p.maoJogador.size
        val c = p.maoJogador[p.indiceCartaMaoSelecionada]
        audioVoz.tocarNavegar()
        val msg = "Carta ${p.indiceCartaMaoSelecionada + 1} de ${p.maoJogador.size}: ${c.falarCompleta()}"
        ultimaNarracao = msg
        audioVoz.falar(msg, interromper = true)
        versao++
    }

    fun navegarMaoProxima(audioVoz: GerenciadorAudioVoz) {
        val p = partidaAtual ?: return
        if (p.maoJogador.isEmpty()) {
            audioVoz.tocarErro()
            audioVoz.falar("Sua mão está vazia.", interromper = true)
            return
        }
        p.indiceCartaMaoSelecionada = (p.indiceCartaMaoSelecionada + 1) % p.maoJogador.size
        val c = p.maoJogador[p.indiceCartaMaoSelecionada]
        audioVoz.tocarNavegar()
        val msg = "Carta ${p.indiceCartaMaoSelecionada + 1} de ${p.maoJogador.size}: ${c.falarCompleta()}"
        ultimaNarracao = msg
        audioVoz.falar(msg, interromper = true)
        versao++
    }

    fun selecionarCartaMaoDireto(indice: Int, audioVoz: GerenciadorAudioVoz) {
        val p = partidaAtual ?: return
        if (indice !in p.maoJogador.indices) return
        p.indiceCartaMaoSelecionada = indice
        val c = p.maoJogador[indice]
        audioVoz.tocarNavegar()
        val msg = "Selecionada carta ${indice + 1}: ${c.falarCompleta()}"
        ultimaNarracao = msg
        audioVoz.falar(msg, interromper = true)
        versao++
    }

    fun tocarEJogarCartaMao(indice: Int, audioVoz: GerenciadorAudioVoz) {
        val p = partidaAtual ?: return
        if (indice !in p.maoJogador.indices) return
        p.indiceCartaMaoSelecionada = indice
        jogarCartaSelecionada(emAtaque = true, audioVoz = audioVoz)
    }

    fun jogarCartaSelecionada(emAtaque: Boolean, audioVoz: GerenciadorAudioVoz) {
        val p = partidaAtual ?: return
        if (!p.vezDoJogador || p.dueloEncerrado) {
            audioVoz.tocarErro()
            audioVoz.falar("Aguarde a sua vez de jogar.", interromper = true)
            return
        }
        if (p.maoJogador.isEmpty()) {
            audioVoz.tocarErro()
            audioVoz.falar("Você não tem cartas na mão.", interromper = true)
            return
        }
        val idx = p.indiceCartaMaoSelecionada.coerceIn(0, p.maoJogador.lastIndex)
        val carta = p.maoJogador[idx]

        when (carta.tipo) {
            TipoCartaYugioh.MONSTRO -> invocarMonstroJogador(p, idx, carta, emAtaque, audioVoz)
            TipoCartaYugioh.MAGIA -> ativarMagiaJogador(p, idx, carta, audioVoz)
            TipoCartaYugioh.ARMADILHA -> armarArmadilhaJogador(p, idx, carta, audioVoz)
        }
    }

    private fun invocarMonstroJogador(
        p: PartidaYugiohLuna,
        idxMao: Int,
        carta: CartaYugioh,
        emAtaque: Boolean,
        audioVoz: GerenciadorAudioVoz
    ) {
        if (p.invocacaoNormalUsadaNoTurno) {
            audioVoz.tocarErro()
            val msg = "Você já realizou uma Invocação Normal neste turno. Ataque com seu monstro ou passe o turno."
            ultimaNarracao = msg
            audioVoz.falar(msg, interromper = true)
            return
        }

        val tributos = carta.tributosNecessarios
        var txtTributo = ""
        if (tributos > 0) {
            if (p.monstrosJogador.size < tributos) {
                audioVoz.tocarErro()
                val msg = "Tributo insuficiente! ${carta.nome} é de Nível ${carta.nivel} e exige $tributos monstro(s) em campo como Tributo! Seu campo tem apenas ${p.monstrosJogador.size} monstro(s) no momento. Invoque primeiro monstros de nível 1 a 4 para acumular os tributos necessários."
                ultimaNarracao = msg
                audioVoz.falar(msg, interromper = true)
                return
            }
            val sacrificados = mutableListOf<String>()
            repeat(tributos) {
                val sacrificado = p.monstrosJogador.removeAt(0)
                p.cemiterioJogador.add(sacrificado.cartaBase)
                sacrificados.add(sacrificado.cartaBase.nome)
            }
            txtTributo = "Tributo de $tributos monstro(s) realizado com sucesso! Você ofereceu ${sacrificados.joinToString(" e ")} como Tributo na arena! "
        } else if (p.monstrosJogador.size >= 3) {
            // Limite de até 3 monstros no campo (regras oficiais de Speed Duel). Substitui o mais antigo.
            val antigo = p.monstrosJogador.removeAt(0)
            p.cemiterioJogador.add(antigo.cartaBase)
            txtTributo = "${antigo.cartaBase.nome} foi enviado ao cemitério para abrir espaço na arena. "
        }

        p.maoJogador.removeAt(idxMao)
        if (p.maoJogador.isNotEmpty()) {
            p.indiceCartaMaoSelecionada = p.indiceCartaMaoSelecionada.coerceIn(0, p.maoJogador.lastIndex)
        } else {
            p.indiceCartaMaoSelecionada = 0
        }

        val novoMonstro = MonstroEmCampoYugioh(
            cartaBase = carta,
            atkAtual = carta.atk,
            defAtual = carta.def,
            emAtaque = emAtaque,
            oculto = !emAtaque,
            mudouPosicaoNesteTurno = true // Regra oficial: não pode mudar de posição no turno em que foi invocado
        )

        var txtEfeito = ""
        when (carta.codigoEfeito) {
            "DEUS_SLIFER" -> {
                val poderMao = maxOf(1000, p.maoJogador.size * 1000)
                novoMonstro.atkAtual = poderMao
                novoMonstro.defAtual = poderMao
                p.monstrosOponente.forEach { it.atkAtual = (it.atkAtual - 1000).coerceAtLeast(0) }
                txtEfeito = " O Poder Divino de Slifer, o Dragão Celeste! Canalizou as ${p.maoJogador.size} cartas na sua mão para atingir $poderMao de Ataque e sua Boca do Segundo Ataque enfraqueceu os monstros de ${p.nomeOponente}!"
                audioVoz.tocarAplausos()
            }
            "DEUS_OBELISCO" -> {
                novoMonstro.atkAtual = 4000
                novoMonstro.defAtual = 4000
                var destruidoTxt = ""
                if (p.monstrosOponente.isNotEmpty()) {
                    val destruido = p.monstrosOponente.removeAt(0)
                    p.cemiterioOponente.add(destruido.cartaBase)
                    destruidoTxt = " O Punho da Destruição Divina aniquilou ${destruido.cartaBase.nome} de ${p.nomeOponente}!"
                }
                txtEfeito = " Obelisco, o Atormentador desce dos céus com 4000 ATK e 4000 DEF colossais!$destruidoTxt"
                audioVoz.tocarAplausos()
            }
            "DRAGAO_RA" -> {
                p.lpJogador += 1000
                txtEfeito = " O Poder Divino do Dragão Alado de Rá! Restaurou 1000 Pontos de Vida (Total: ${p.lpJogador} LP) com 4000 de Ataque!"
                audioVoz.tocarAplausos()
            }
            "MAGA_NEGRA" -> {
                novoMonstro.atkAtual += 300
                txtEfeito = " Efeito da Pequena Maga Negra ativado: ganhou 300 de Ataque (Total: ${novoMonstro.atkAtual} ATK)!"
            }
            "KURIBOH" -> {
                p.lpJogador += 500
                txtEfeito = " Efeito do Kuriboh Alado: você recuperou 500 Pontos de Vida (Total: ${p.lpJogador} LP)!"
            }
            "SENHOR_DRAGOES" -> {
                p.lpJogador += 400
                novoMonstro.defAtual += 400
                txtEfeito = " Efeito do Senhor dos Dragões: recuperou 400 LP e ganhou 400 de Defesa!"
            }
            "CHAMA" -> {
                p.lpOponente = (p.lpOponente - 400).coerceAtLeast(0)
                txtEfeito = " Efeito do Espadachim da Chama: causou 400 de dano direto a ${p.nomeOponente}!"
            }
            "HARPIA_IRMAS" -> {
                novoMonstro.atkAtual += 350
                txtEfeito = " Efeito das Irmãs de Harpia: ganhou 350 de Ataque (Total: ${novoMonstro.atkAtual} ATK)!"
            }
            "AMAZONA" -> {
                novoMonstro.atkAtual += 300
                txtEfeito = " Efeito da Espadachim Amazona: ganhou 300 de Ataque (Total: ${novoMonstro.atkAtual} ATK)!"
            }
            "PALHACO_PETEN" -> {
                p.lpJogador += 400
                txtEfeito = " Efeito do Peten o Palhaço Negro: você recuperou 400 Pontos de Vida (Total: ${p.lpJogador} LP)!"
            }
            "TOON_DRAGON" -> {
                p.lpOponente = (p.lpOponente - 400).coerceAtLeast(0)
                txtEfeito = " Efeito surpresa do Dragão Toon: causou 400 de dano direto a ${p.nomeOponente}!"
            }
            "TOON_MAGA" -> {
                novoMonstro.atkAtual += 300
                p.lpJogador += 300
                txtEfeito = " Efeito da Pequena Maga Toon: +300 de Ataque e recuperou 300 Pontos de Vida!"
            }
            "RELINQUISHED" -> {
                novoMonstro.atkAtual = 2200
                novoMonstro.defAtual = 2200
                txtEfeito = " Ritual de Ilusão do Renunciado: absorveu o poder das trevas para 2200 de Ataque e 2200 de Defesa!"
            }
            "LAVA_GOLEM" -> {
                p.lpOponente = (p.lpOponente - 800).coerceAtLeast(0)
                txtEfeito = " Efeito Vulcânico do Lava Golem: chamas causaram 800 de dano direto a ${p.nomeOponente}!"
            }
            "HELPOEMER" -> {
                p.lpOponente = (p.lpOponente - 400).coerceAtLeast(0)
                p.lpJogador += 400
                txtEfeito = " Efeito do Helpoemer: drenou 400 LP de ${p.nomeOponente} para você!"
            }
            "NEWDORIA" -> {
                novoMonstro.atkAtual += 400
                txtEfeito = " Efeito de Newdoria: ganhou 400 de Ataque (Total: ${novoMonstro.atkAtual} ATK)!"
            }
            "NECROFEAR" -> {
                p.monstroOponente?.let {
                    it.atkAtual = (it.atkAtual - 600).coerceAtLeast(0)
                    txtEfeito = " Maldição de Necrofear: reduziu o ataque de ${it.cartaBase.nome} em 600 pontos!"
                }
            }
            "DIABOUND" -> {
                novoMonstro.atkAtual += 500
                txtEfeito = " Efeito de Diabound: roubou força e ganhou 500 de Ataque (Total: ${novoMonstro.atkAtual} ATK)!"
            }
            "REVOLVER_DRAGON" -> {
                p.lpOponente = (p.lpOponente - 500).coerceAtLeast(0)
                txtEfeito = " Roleta Russa do Dragão Revólver: canhões dispararam e causaram 500 de dano direto a ${p.nomeOponente}!"
            }
            "HEROI_NEOS" -> {
                novoMonstro.atkAtual += 400
                txtEfeito = " Efeito do Herói Elemental Neos: energia de Neo Space concedeu +400 ATK (Total: ${novoMonstro.atkAtual} ATK)!"
            }
        }

        p.monstrosJogador.add(novoMonstro)
        p.invocacaoNormalUsadaNoTurno = true
        audioVoz.tocarConfirmar()

        val tipoAcao = if (carta.tributosNecessarios > 0) "INVOCACAO_TRIBUTO" else "MONSTRO_INVOCADO"
        bancoDados?.registrarJogadaJogador(tipoAcao, carta.id, carta.nome, p.turnoNumero)

        val modoTxt = if (emAtaque) {
            "invocado em Posição de Ataque com ${novoMonstro.atkAtual} pontos"
        } else {
            "colocado oculto em Posição de Defesa com ${novoMonstro.defAtual} pontos"
        }

        val msg = "$txtTributo${carta.nome} foi $modoTxt!$txtEfeito " + obterTextoMaoAtualizada(p)
        ultimaNarracao = msg
        audioVoz.falar(msg, interromper = true)
        checarVitoriaOuDerrota(p, audioVoz)
        versao++
    }

    private fun ativarMagiaJogador(
        p: PartidaYugiohLuna,
        idxMao: Int,
        carta: CartaYugioh,
        audioVoz: GerenciadorAudioVoz
    ) {
        var msgEfeito = ""
        when (carta.codigoEfeito) {
            "MAGIA_BURACO_NEGRO" -> {
                if (p.monstroJogador == null && p.monstroOponente == null) {
                    audioVoz.tocarErro()
                    audioVoz.falar("Não há monstros no campo para destruir com Buraco Negro.", interromper = true)
                    return
                }
                p.monstroJogador?.let { p.cemiterioJogador.add(it.cartaBase) }
                p.monstroOponente?.let { p.cemiterioOponente.add(it.cartaBase) }
                p.monstroJogador = null
                p.monstroOponente = null
                msgEfeito = "Magia Buraco Negro ativada! Todos os monstros da arena foram destruídos!"
            }
            "MAGIA_RAIGEKI" -> {
                val mOp = p.monstroOponente
                if (mOp == null) {
                    audioVoz.tocarErro()
                    audioVoz.falar("${p.nomeOponente} não tem monstro em campo para destruir com Raigeki.", interromper = true)
                    return
                }
                p.cemiterioOponente.add(mOp.cartaBase)
                p.monstroOponente = null
                msgEfeito = "Magia Raigeki ativada! O relâmpago destruiu ${mOp.cartaBase.nome} de ${p.nomeOponente}!"
            }
            "MAGIA_RENASCIMENTO" -> {
                val todosCemiterio = (p.cemiterioJogador + p.cemiterioOponente).filter { it.tipo == TipoCartaYugioh.MONSTRO }
                if (todosCemiterio.isEmpty()) {
                    audioVoz.tocarErro()
                    audioVoz.falar("Ainda não há monstros nos cemitérios para reviver.", interromper = true)
                    return
                }
                val melhor = todosCemiterio.maxBy { it.atk }
                if (!p.cemiterioJogador.remove(melhor)) {
                    p.cemiterioOponente.remove(melhor)
                }
                p.monstroJogador?.let { p.cemiterioJogador.add(it.cartaBase) }
                p.monstroJogador = MonstroEmCampoYugioh(
                    cartaBase = melhor,
                    atkAtual = melhor.atk,
                    defAtual = melhor.def,
                    emAtaque = true,
                    oculto = false
                )
                msgEfeito = "Magia Renascimento do Monstro ativada! Você reviveu ${melhor.nome} com ${melhor.atk} de Ataque!"
            }
            "MAGIA_POTE" -> {
                val c1 = p.comprarCarta(paraJogador = true)
                val c2 = p.comprarCarta(paraJogador = true)
                val nomes = listOfNotNull(c1?.nome, c2?.nome).joinToString(" e ")
                msgEfeito = "Pote da Ganância ativado! Você comprou 2 cartas: $nomes."
                bancoDados?.registrarJogadaJogador("COMPRA_RAPIDA", 24, "Pote da Ganância", p.turnoNumero)
                if (p.checarVitoriaExodia() || p.dueloEncerrado) {
                    checarVitoriaOuDerrota(p, audioVoz)
                    return
                }
            }
            "MAGIA_ESPADAS" -> {
                p.turnosEspadasLuzJogador = 2
                var revTxt = ""
                p.monstroOponente?.let {
                    if (it.oculto) {
                        it.oculto = false
                        revTxt = " O monstro oculto de ${p.nomeOponente} foi revelado: ${it.cartaBase.nome}!"
                    }
                }
                msgEfeito = "Espadas da Luz Reveladora ativadas!$revTxt ${p.nomeOponente} está impedido de atacar por 2 turnos!"
            }
            "MAGIA_HINOTAMA" -> {
                p.lpOponente = (p.lpOponente - 600).coerceAtLeast(0)
                msgEfeito = "Chama Final de Hinotama ativada! Causou 600 de dano direto a ${p.nomeOponente}! LP de ${p.nomeOponente}: ${p.lpOponente}."
            }
            "MAGIA_EQUIP_ATK" -> {
                val mJog = p.monstroJogador
                if (mJog == null) {
                    audioVoz.tocarErro()
                    audioVoz.falar("Você precisa ter um monstro no seu campo para equipar esta magia.", interromper = true)
                    return
                }
                mJog.atkAtual += 500
                mJog.defAtual += 500
                msgEfeito = "Magia de Equipamento ativada! ${mJog.cartaBase.nome} ganhou 500 pontos e agora tem ${mJog.atkAtual} de Ataque e ${mJog.defAtual} de Defesa!"
            }
            "MAGIA_CURA" -> {
                p.lpJogador += 1000
                msgEfeito = "Poção Suprema da Luna ativada! Você recuperou 1000 Pontos de Vida! Seus LP: ${p.lpJogador}."
            }
            "MAGIA_CONTROLE_KAIBA" -> {
                val mOp = p.monstroOponente
                if (mOp == null) {
                    audioVoz.tocarErro()
                    audioVoz.falar("${p.nomeOponente} não tem monstro em campo para controlar.", interromper = true)
                    return
                }
                mOp.atkAtual = (mOp.atkAtual - 600).coerceAtLeast(0)
                msgEfeito = "Controlador de Inimigos de Kaiba ativado! O Ataque de ${mOp.cartaBase.nome} foi reduzido em 600 pontos (Novo ATK: ${mOp.atkAtual})!"
            }
            "MAGIA_MUNDO_TOON" -> {
                p.lpJogador += 800
                p.monstroJogador?.let { it.atkAtual += 300 }
                msgEfeito = "Mundo Toon aberto! Você recuperou 800 Pontos de Vida e seu monstro em campo ganhou 300 de Ataque!"
            }
            "MAGIA_PESADELO_MARIK" -> {
                p.lpOponente = (p.lpOponente - 800).coerceAtLeast(0)
                msgEfeito = "Pesadelo de Marik ativado! Chamas das trevas causaram 800 de dano direto a ${p.nomeOponente}! LP de ${p.nomeOponente}: ${p.lpOponente}."
            }
            "MAGIA_SKYSCRAPER" -> {
                val mJog = p.monstroJogador
                if (mJog == null) {
                    audioVoz.tocarErro()
                    audioVoz.falar("Você precisa ter um monstro no seu campo para lutar no Arranha-Céu.", interromper = true)
                    return
                }
                mJog.atkAtual += 600
                mJog.defAtual += 600
                msgEfeito = "Arranha-Céu da Cidade ativado! ${mJog.cartaBase.nome} recebeu 600 de Ataque e Defesa (Total: ${mJog.atkAtual} ATK / ${mJog.defAtual} DEF)!"
            }
        }

        p.maoJogador.removeAt(idxMao)
        p.cemiterioJogador.add(carta)
        bancoDados?.registrarJogadaJogador("MAGIA_USADA", carta.id, carta.nome, p.turnoNumero)
        if (p.maoJogador.isNotEmpty()) {
            p.indiceCartaMaoSelecionada = p.indiceCartaMaoSelecionada.coerceIn(0, p.maoJogador.lastIndex)
        } else {
            p.indiceCartaMaoSelecionada = 0
        }

        audioVoz.tocarAcerto()
        val narracaoMagia = "$msgEfeito " + obterTextoMaoAtualizada(p)
        ultimaNarracao = narracaoMagia
        audioVoz.falar(narracaoMagia, interromper = true)
        checarVitoriaOuDerrota(p, audioVoz)
        versao++
    }

    private fun armarArmadilhaJogador(
        p: PartidaYugiohLuna,
        idxMao: Int,
        carta: CartaYugioh,
        audioVoz: GerenciadorAudioVoz
    ) {
        p.armadilhaJogador?.let { p.cemiterioJogador.add(it) }
        p.armadilhaJogador = carta
        p.maoJogador.removeAt(idxMao)
        bancoDados?.registrarJogadaJogador("ARMADILHA_POSTA", carta.id, carta.nome, p.turnoNumero)
        if (p.maoJogador.isNotEmpty()) {
            p.indiceCartaMaoSelecionada = p.indiceCartaMaoSelecionada.coerceIn(0, p.maoJogador.lastIndex)
        } else {
            p.indiceCartaMaoSelecionada = 0
        }
        audioVoz.tocarConfirmar()
        val msg = "Armadilha ${carta.nome} colocada virada para baixo no seu campo! " + obterTextoMaoAtualizada(p)
        ultimaNarracao = msg
        audioVoz.falar(msg, interromper = true)
        versao++
    }

    fun mudarPosicaoMonstroJogador(audioVoz: GerenciadorAudioVoz) {
        val p = partidaAtual ?: return
        if (!p.vezDoJogador || p.dueloEncerrado) return
        val m = p.monstroJogador
        if (m == null) {
            audioVoz.tocarErro()
            audioVoz.falar("Você não tem monstro em campo para mudar de posição.", interromper = true)
            return
        }
        if (m.mudouPosicaoNesteTurno) {
            audioVoz.tocarErro()
            audioVoz.falar("Regra oficial de Yu-Gi-Oh: ${m.cartaBase.nome} foi invocado ou já mudou de posição neste turno e não pode alterar sua posição.", interromper = true)
            return
        }
        if (m.jaAtacouNesteTurno) {
            audioVoz.tocarErro()
            audioVoz.falar("Regra oficial de Yu-Gi-Oh: ${m.cartaBase.nome} já atacou neste turno e não pode mudar para Posição de Defesa.", interromper = true)
            return
        }
        m.emAtaque = !m.emAtaque
        m.oculto = false
        m.mudouPosicaoNesteTurno = true
        audioVoz.tocarNavegar()
        val novaPos = if (m.emAtaque) "Posição de Ataque (${m.atkAtual} ATK)" else "Posição de Defesa (${m.defAtual} DEF)"
        val msg = "${m.cartaBase.nome} mudou para $novaPos."
        ultimaNarracao = msg
        audioVoz.falar(msg, interromper = true)
        versao++
    }

    fun atacarComMonstroJogador(audioVoz: GerenciadorAudioVoz) {
        val p = partidaAtual ?: return
        if (!p.vezDoJogador || p.dueloEncerrado) return
        val mJog = p.monstroJogador
        if (mJog == null) {
            audioVoz.tocarErro()
            audioVoz.falar("Você não tem monstro no seu campo para atacar.", interromper = true)
            return
        }
        if (!mJog.emAtaque) {
            audioVoz.tocarErro()
            audioVoz.falar("${mJog.cartaBase.nome} está em Posição de Defesa. Mude para Ataque antes de atacar.", interromper = true)
            return
        }
        if (p.turnosEspadasLuzOponente > 0) {
            audioVoz.tocarErro()
            val msg = "Ataque bloqueado! As Espadas da Luz Reveladora de ${p.nomeOponente} estão ativas por mais ${p.turnosEspadasLuzOponente} turno(s)."
            ultimaNarracao = msg
            audioVoz.falar(msg, interromper = true)
            return
        }
        if (p.turnoNumero == 1) {
            audioVoz.tocarErro()
            val msg = "Regra oficial de Yu-Gi-Oh: Não é permitido atacar no primeiro turno do duelo! Encerre seu turno para passar a vez."
            ultimaNarracao = msg
            audioVoz.falar(msg, interromper = true)
            return
        }
        if (mJog.jaAtacouNesteTurno) {
            audioVoz.tocarErro()
            audioVoz.falar("${mJog.cartaBase.nome} já atacou neste turno.", interromper = true)
            return
        }

        mJog.jaAtacouNesteTurno = true

        // Verificar armadilha do oponente
        val armOp = p.armadilhaOponente
        if (armOp != null) {
            p.armadilhaOponente = null
            p.cemiterioOponente.add(armOp)
            when (armOp.codigoEfeito) {
                "ARM_FORCA_ESPELHO" -> {
                    p.cemiterioJogador.add(mJog.cartaBase)
                    p.monstroJogador = null
                    audioVoz.tocarDerrota()
                    val msg = "${p.nomeOponente} ativou a Armadilha Força Espelho! O ataque de ${mJog.cartaBase.nome} foi refletido e seu monstro foi destruído!"
                    ultimaNarracao = msg
                    audioVoz.falar(msg, interromper = true)
                    versao++
                    return
                }
                "ARM_CILINDRO" -> {
                    val dano = mJog.atkAtual
                    p.lpJogador = (p.lpJogador - dano).coerceAtLeast(0)
                    audioVoz.tocarDerrota()
                    val msg = "${p.nomeOponente} ativou Cilindro Mágico! Seu ataque foi desviado e você sofreu $dano de dano direto! Seus LP: ${p.lpJogador}."
                    ultimaNarracao = msg
                    audioVoz.falar(msg, interromper = true)
                    checarVitoriaOuDerrota(p, audioVoz)
                    versao++
                    return
                }
                "ARM_BURACO" -> {
                    if (mJog.atkAtual >= 1000) {
                        p.cemiterioJogador.add(mJog.cartaBase)
                        p.monstroJogador = null
                        audioVoz.tocarDerrota()
                        val msg = "${p.nomeOponente} ativou Buraco Armadilha! ${mJog.cartaBase.nome} caiu na armadilha e foi destruído!"
                        ultimaNarracao = msg
                        audioVoz.falar(msg, interromper = true)
                        versao++
                        return
                    }
                }
                "ARM_VIRUS_KAIBA" -> {
                    if (mJog.atkAtual >= 1500) {
                        p.cemiterioJogador.add(mJog.cartaBase)
                        p.monstroJogador = null
                        p.lpJogador = (p.lpJogador - 500).coerceAtLeast(0)
                        audioVoz.tocarDerrota()
                        val msg = "${p.nomeOponente} ativou o Vírus Esmagador de Cartas! Seu ${mJog.cartaBase.nome} foi infectado e destruído, causando 500 de dano a você! Seus LP: ${p.lpJogador}."
                        ultimaNarracao = msg
                        audioVoz.falar(msg, interromper = true)
                        checarVitoriaOuDerrota(p, audioVoz)
                        versao++
                        return
                    }
                }
                "ARM_DESTINO_BAKURA" -> {
                    p.lpJogador = (p.lpJogador - 500).coerceAtLeast(0)
                    audioVoz.tocarDerrota()
                    val msg = "${p.nomeOponente} ativou o Tabuleiro do Destino de Bakura! O ataque foi bloqueado e 500 LP seus foram drenados! Seus LP: ${p.lpJogador}."
                    ultimaNarracao = msg
                    audioVoz.falar(msg, interromper = true)
                    checarVitoriaOuDerrota(p, audioVoz)
                    versao++
                    return
                }
                "ARM_PAREDE" -> {
                    mJog.atkAtual /= 2
                }
            }
        }

        val mOp = p.monstroOponente
        if (mOp == null) {
            // Ataque direto!
            val dano = mJog.atkAtual
            p.lpOponente = (p.lpOponente - dano).coerceAtLeast(0)
            audioVoz.tocarVitoria()
            val msg = "Ataque Direto! ${mJog.cartaBase.nome} atacou ${p.nomeOponente} diretamente causando $dano de dano! Pontos de Vida de ${p.nomeOponente}: ${p.lpOponente}."
            ultimaNarracao = msg
            audioVoz.falar(msg, interromper = true)
            checarVitoriaOuDerrota(p, audioVoz)
            versao++
            return
        }

        mOp.oculto = false
        if (mOp.emAtaque) {
            when {
                mJog.atkAtual > mOp.atkAtual -> {
                    val dif = mJog.atkAtual - mOp.atkAtual
                    p.lpOponente = (p.lpOponente - dif).coerceAtLeast(0)
                    p.cemiterioOponente.add(mOp.cartaBase)
                    p.monstroOponente = null
                    audioVoz.tocarVitoria()
                    val msg = "${mJog.cartaBase.nome} (${mJog.atkAtual} ATK) destruiu ${mOp.cartaBase.nome} (${mOp.atkAtual} ATK) e causou $dif de dano a ${p.nomeOponente}! LP de ${p.nomeOponente}: ${p.lpOponente}."
                    ultimaNarracao = msg
                    audioVoz.falar(msg, interromper = true)
                }
                mJog.atkAtual < mOp.atkAtual -> {
                    val dif = mOp.atkAtual - mJog.atkAtual
                    p.lpJogador = (p.lpJogador - dif).coerceAtLeast(0)
                    p.cemiterioJogador.add(mJog.cartaBase)
                    p.monstroJogador = null
                    audioVoz.tocarDerrota()
                    val msg = "${mJog.cartaBase.nome} (${mJog.atkAtual} ATK) foi superado por ${mOp.cartaBase.nome} (${mOp.atkAtual} ATK)! Você perdeu seu monstro e sofreu $dif de dano! Seus LP: ${p.lpJogador}."
                    ultimaNarracao = msg
                    audioVoz.falar(msg, interromper = true)
                }
                else -> {
                    p.cemiterioJogador.add(mJog.cartaBase)
                    p.cemiterioOponente.add(mOp.cartaBase)
                    p.monstroJogador = null
                    p.monstroOponente = null
                    audioVoz.tocarConfirmar()
                    val msg = "Choque de forças iguais (${mJog.atkAtual} ATK)! Ambos os monstros se destruíram!"
                    ultimaNarracao = msg
                    audioVoz.falar(msg, interromper = true)
                }
            }
        } else {
            when {
                mJog.atkAtual > mOp.defAtual -> {
                    p.cemiterioOponente.add(mOp.cartaBase)
                    p.monstroOponente = null
                    audioVoz.tocarVitoria()
                    val msg = "${mJog.cartaBase.nome} (${mJog.atkAtual} ATK) despedaçou a defesa de ${mOp.cartaBase.nome} (${mOp.defAtual} DEF)!"
                    ultimaNarracao = msg
                    audioVoz.falar(msg, interromper = true)
                }
                mJog.atkAtual < mOp.defAtual -> {
                    val dif = mOp.defAtual - mJog.atkAtual
                    p.lpJogador = (p.lpJogador - dif).coerceAtLeast(0)
                    audioVoz.tocarDerrota()
                    val msg = "A defesa de ${mOp.cartaBase.nome} (${mOp.defAtual} DEF) era maior que o ataque de ${mJog.cartaBase.nome} (${mJog.atkAtual} ATK)! Você sofreu $dif de recuo! Seus LP: ${p.lpJogador}."
                    ultimaNarracao = msg
                    audioVoz.falar(msg, interromper = true)
                }
                else -> {
                    audioVoz.tocarConfirmar()
                    val msg = "Ataque igual à defesa (${mOp.defAtual} pontos)! Nenhum monstro foi destruído."
                    ultimaNarracao = msg
                    audioVoz.falar(msg, interromper = true)
                }
            }
        }

        checarVitoriaOuDerrota(p, audioVoz)
        versao++
    }

    fun encerrarTurnoJogador(scope: CoroutineScope, audioVoz: GerenciadorAudioVoz) {
        val p = partidaAtual ?: return
        if (p.checarVitoriaExodia() || p.dueloEncerrado) {
            checarVitoriaOuDerrota(p, audioVoz)
            return
        }
        if (!p.vezDoJogador) return

        if (p.turnosEspadasLuzOponente > 0) {
            p.turnosEspadasLuzOponente--
        }

        p.vezDoJogador = false
        versao++

        jobTurnoIa?.cancel()
        jobTurnoIa = scope.launch {
            try {
                val ehLuna = p.nomeOponente.contains("Luna", ignoreCase = true)
                val eventosIa = mutableListOf<String>()
                if (ehLuna) {
                    eventosIa.add("Luna (8 anos): É a minha vez! Puxei uma cartinha super legal!")
                } else {
                    eventosIa.add("Você encerrou seu turno. Vez de ${p.nomeOponente}!")
                }

                p.comprarCarta(paraJogador = false)
                if (p.checarVitoriaExodia() || p.dueloEncerrado) {
                    if (checarVitoriaOuDerrota(p, audioVoz)) {
                        versao++
                        return@launch
                    }
                }

                // 1. IA usa magia se tiver na mão
                val magiaIa = p.maoOponente.firstOrNull { it.tipo == TipoCartaYugioh.MAGIA }
                if (magiaIa != null) {
                    when (magiaIa.codigoEfeito) {
                        "MAGIA_RAIGEKI" -> if (p.monstroJogador != null) {
                            val mJ = p.monstroJogador!!
                            p.maoOponente.remove(magiaIa)
                            p.cemiterioOponente.add(magiaIa)
                            p.cemiterioJogador.add(mJ.cartaBase)
                            p.monstroJogador = null
                            eventosIa.add("${p.nomeOponente} ativou Raigeki e destruiu seu ${mJ.cartaBase.nome}!")
                        }
                        "MAGIA_HINOTAMA" -> {
                            p.maoOponente.remove(magiaIa)
                            p.cemiterioOponente.add(magiaIa)
                            p.lpJogador = (p.lpJogador - 600).coerceAtLeast(0)
                            eventosIa.add("${p.nomeOponente} lançou Chama Final de Hinotama causando 600 de dano a você!")
                        }
                        "MAGIA_CURA" -> if (p.lpOponente <= 3000) {
                            p.maoOponente.remove(magiaIa)
                            p.cemiterioOponente.add(magiaIa)
                            p.lpOponente += 1000
                            eventosIa.add("${p.nomeOponente} usou Poção Suprema e recuperou 1000 Pontos de Vida!")
                        }
                        "MAGIA_POTE" -> {
                            p.maoOponente.remove(magiaIa)
                            p.cemiterioOponente.add(magiaIa)
                            p.comprarCarta(paraJogador = false)
                            p.comprarCarta(paraJogador = false)
                            eventosIa.add("${p.nomeOponente} ativou Pote da Ganância e comprou 2 cartas!")
                        }
                        "MAGIA_ESPADAS" -> {
                            p.maoOponente.remove(magiaIa)
                            p.cemiterioOponente.add(magiaIa)
                            p.turnosEspadasLuzOponente = 2
                            eventosIa.add("${p.nomeOponente} ativou Espadas da Luz Reveladora e impediu você de atacar por 2 turnos!")
                        }
                        "MAGIA_CONTROLE_KAIBA" -> if (p.monstroJogador != null) {
                            p.maoOponente.remove(magiaIa)
                            p.cemiterioOponente.add(magiaIa)
                            val mJ = p.monstroJogador!!
                            mJ.atkAtual = (mJ.atkAtual - 600).coerceAtLeast(0)
                            eventosIa.add("${p.nomeOponente} usou Controlador de Inimigos e reduziu o ataque de seu ${mJ.cartaBase.nome} em 600 pontos!")
                        }
                        "MAGIA_MUNDO_TOON" -> {
                            p.maoOponente.remove(magiaIa)
                            p.cemiterioOponente.add(magiaIa)
                            p.lpOponente += 800
                            p.monstroOponente?.let { it.atkAtual += 300 }
                            eventosIa.add("${p.nomeOponente} abriu o Mundo Toon, recuperou 800 Pontos de Vida e fortaleceu o ataque!")
                        }
                        "MAGIA_PESADELO_MARIK" -> {
                            p.maoOponente.remove(magiaIa)
                            p.cemiterioOponente.add(magiaIa)
                            p.lpJogador = (p.lpJogador - 800).coerceAtLeast(0)
                            eventosIa.add("${p.nomeOponente} ativou Pesadelo de Marik causando 800 de dano direto a você!")
                        }
                        "MAGIA_SKYSCRAPER" -> if (p.monstroOponente != null) {
                            p.maoOponente.remove(magiaIa)
                            p.cemiterioOponente.add(magiaIa)
                            val mOp = p.monstroOponente!!
                            mOp.atkAtual += 600
                            mOp.defAtual += 600
                            eventosIa.add("${p.nomeOponente} ativou a arena Arranha-Céu aumentando o poder de ${mOp.cartaBase.nome} em 600 pontos!")
                        }
                    }
                }

                if (checarVitoriaOuDerrota(p, audioVoz)) {
                    versao++
                    return@launch
                }

            // 2. IA arma armadilha se não tiver
            if (p.armadilhaOponente == null) {
                val armIa = p.maoOponente.firstOrNull { it.tipo == TipoCartaYugioh.ARMADILHA }
                if (armIa != null) {
                    p.maoOponente.remove(armIa)
                    p.armadilhaOponente = armIa
                    eventosIa.add("${p.nomeOponente} colocou uma carta de Armadilha virada para baixo.")
                }
            }

            // 3. IA invoca monstro
            val monstrosMaoIa = p.maoOponente.filter { it.tipo == TipoCartaYugioh.MONSTRO }
            val monstroForteTributo = monstrosMaoIa.filter { it.tributosNecessarios in 1..3 && p.monstrosOponente.size >= it.tributosNecessarios }.maxByOrNull { it.atk }
            val monstroSemTributo = monstrosMaoIa.filter { it.tributosNecessarios == 0 }.maxByOrNull { it.atk }

            if (monstroForteTributo != null) {
                val tribs = monstroForteTributo.tributosNecessarios
                val sacrificados = mutableListOf<String>()
                repeat(tribs) {
                    if (p.monstrosOponente.isNotEmpty()) {
                        val m = p.monstrosOponente.removeAt(0)
                        p.cemiterioOponente.add(m.cartaBase)
                        sacrificados.add(m.cartaBase.nome)
                    }
                }
                p.maoOponente.remove(monstroForteTributo)
                val mInvocado = MonstroEmCampoYugioh(
                    cartaBase = monstroForteTributo,
                    atkAtual = monstroForteTributo.atk,
                    defAtual = monstroForteTributo.def,
                    emAtaque = true,
                    oculto = false
                )
                p.monstrosOponente.add(mInvocado)
                if (ehLuna) {
                    eventosIa.add("Luna: Sacrifiquei ${sacrificados.joinToString(" e ")} e chamei o grandioso ${monstroForteTributo.nome} com ${monstroForteTributo.atk} de Ataque!")
                } else {
                    eventosIa.add("${p.nomeOponente} ofereceu ${sacrificados.joinToString(" e ")} como Tributo e invocou ${monstroForteTributo.nome} com ${monstroForteTributo.atk} de Ataque!")
                }
            } else if (monstroSemTributo != null && p.monstrosOponente.size < 3) {
                p.maoOponente.remove(monstroSemTributo)
                val atkMaiorQueJogador = p.monstroJogador == null || !p.monstroJogador!!.emAtaque || monstroSemTributo.atk >= p.monstroJogador!!.atkAtual
                val mInvocado = MonstroEmCampoYugioh(
                    cartaBase = monstroSemTributo,
                    atkAtual = monstroSemTributo.atk,
                    defAtual = monstroSemTributo.def,
                    emAtaque = atkMaiorQueJogador,
                    oculto = !atkMaiorQueJogador
                )
                p.monstrosOponente.add(mInvocado)
                if (ehLuna) {
                    if (atkMaiorQueJogador) {
                        eventosIa.add("Luna: Olha só que lindo! Chamei o meu amiguinho ${monstroSemTributo.nome} em Ataque!")
                    } else {
                        eventosIa.add("Luna: Botei uma cartinha escondida virada pra baixo em Defesa!")
                    }
                } else if (atkMaiorQueJogador) {
                    eventosIa.add("${p.nomeOponente} invocou ${monstroSemTributo.nome} em Ataque (${monstroSemTributo.atk} ATK)!")
                } else {
                    eventosIa.add("${p.nomeOponente} colocou um monstro oculto em Posição de Defesa.")
                }
            }

            // 4. Fase de Batalha da IA
            if (p.turnosEspadasLuzJogador > 0) {
                p.turnosEspadasLuzJogador--
                eventosIa.add("As Espadas da Luz Reveladora impediram ${p.nomeOponente} de atacar neste turno!")
            } else if (p.monstroOponente != null && p.monstroOponente!!.emAtaque) {
                val mIa = p.monstroOponente!!
                val armJog = p.armadilhaJogador
                if (armJog != null) {
                    p.armadilhaJogador = null
                    p.cemiterioJogador.add(armJog)
                    when (armJog.codigoEfeito) {
                        "ARM_FORCA_ESPELHO" -> {
                            p.cemiterioOponente.add(mIa.cartaBase)
                            p.monstroOponente = null
                            eventosIa.add("${mIa.cartaBase.nome} atacou, mas sua Força Espelho refletiu o ataque e destruiu o monstro de ${p.nomeOponente}!")
                        }
                        "ARM_CILINDRO" -> {
                            val dano = mIa.atkAtual
                            p.lpOponente = (p.lpOponente - dano).coerceAtLeast(0)
                            eventosIa.add("${mIa.cartaBase.nome} atacou, mas seu Cilindro Mágico devolveu $dano de dano direto a ${p.nomeOponente}!")
                        }
                        "ARM_BURACO" -> {
                            p.cemiterioOponente.add(mIa.cartaBase)
                            p.monstroOponente = null
                            eventosIa.add("Seu Buraco Armadilha ativou e destruiu ${mIa.cartaBase.nome} de ${p.nomeOponente}!")
                        }
                        "ARM_VIRUS_KAIBA" -> {
                            if (mIa.atkAtual >= 1500) {
                                p.cemiterioOponente.add(mIa.cartaBase)
                                p.monstroOponente = null
                                p.lpOponente = (p.lpOponente - 500).coerceAtLeast(0)
                                eventosIa.add("Seu Vírus Esmagador de Cartas ativou! O monstro ${mIa.cartaBase.nome} de ${p.nomeOponente} foi destruído e causou 500 de dano a ele!")
                            }
                        }
                        "ARM_DESTINO_BAKURA" -> {
                            p.lpOponente = (p.lpOponente - 500).coerceAtLeast(0)
                            p.lpJogador += 500
                            p.monstroOponente = null
                            eventosIa.add("Seu Tabuleiro do Destino ativou! O ataque foi selado e 500 LP de ${p.nomeOponente} foram drenados para você!")
                        }
                        "ARM_PAREDE" -> {
                            mIa.atkAtual /= 2
                            eventosIa.add("Sua Parede de Espelhos cortou o Ataque de ${mIa.cartaBase.nome} pela metade (${mIa.atkAtual} ATK)!")
                        }
                    }
                }

                // Se o monstro da IA ainda estiver vivo após armadilhas
                val mIaVivo = p.monstroOponente
                if (mIaVivo != null && mIaVivo.emAtaque && armJog?.codigoEfeito != "ARM_CILINDRO") {
                    val mJog = p.monstroJogador
                    if (mJog == null) {
                        val dano = mIaVivo.atkAtual
                        p.lpJogador = (p.lpJogador - dano).coerceAtLeast(0)
                        eventosIa.add("${mIaVivo.cartaBase.nome} atacou você diretamente causando $dano de dano! Seus LP: ${p.lpJogador}.")
                    } else if (mJog.emAtaque && mIaVivo.atkAtual >= mJog.atkAtual) {
                        val dif = mIaVivo.atkAtual - mJog.atkAtual
                        p.lpJogador = (p.lpJogador - dif).coerceAtLeast(0)
                        p.cemiterioJogador.add(mJog.cartaBase)
                        p.monstroJogador = null
                        if (dif == 0) {
                            p.cemiterioOponente.add(mIaVivo.cartaBase)
                            p.monstroOponente = null
                            eventosIa.add("${mIaVivo.cartaBase.nome} empatou em força com ${mJog.cartaBase.nome} e ambos foram destruídos!")
                        } else {
                            eventosIa.add("${mIaVivo.cartaBase.nome} (${mIaVivo.atkAtual} ATK) destruiu seu ${mJog.cartaBase.nome} e causou $dif de dano! Seus LP: ${p.lpJogador}.")
                        }
                    } else if (!mJog.emAtaque && mIaVivo.atkAtual > mJog.defAtual) {
                        p.cemiterioJogador.add(mJog.cartaBase)
                        p.monstroJogador = null
                        eventosIa.add("${mIaVivo.cartaBase.nome} destruiu seu ${mJog.cartaBase.nome} que estava em Defesa!")
                    }
                }
            }

            delay(650L)
            if (checarVitoriaOuDerrota(p, audioVoz)) {
                versao++
                return@launch
            }

            // 5. Passa a vez de volta para o Jogador
            p.turnoNumero++
            p.vezDoJogador = true
            p.invocacaoNormalUsadaNoTurno = false
            p.monstroJogador?.let {
                it.jaAtacouNesteTurno = false
                it.mudouPosicaoNesteTurno = false
            }

            val comprada = p.comprarCarta(paraJogador = true)
            if (p.checarVitoriaExodia() || p.dueloEncerrado) {
                if (checarVitoriaOuDerrota(p, audioVoz)) {
                    versao++
                    return@launch
                }
            }

            val txtCompra = comprada?.let { "Você comprou: ${it.nome} (${it.resumoCurto()})." } ?: "Seu Deck acabou!"
            eventosIa.add("Turno ${p.turnoNumero}: Sua vez, ${p.nomeJogador}! $txtCompra " + obterTextoMaoAtualizada(p))

            val narracaoCompleta = eventosIa.joinToString(" ")
            ultimaNarracao = narracaoCompleta
            audioVoz.tocarConfirmar()
            if (ehLuna) {
                audioVoz.falarComoLuna(narracaoCompleta, interromper = true)
            } else {
                audioVoz.falar(narracaoCompleta, interromper = true)
            }
            versao++
        } catch (e: Exception) {
            p.vezDoJogador = true
            versao++
        }
    }
}

    private fun checarVitoriaOuDerrota(p: PartidaYugiohLuna, audioVoz: GerenciadorAudioVoz): Boolean {
        if (!p.verificarFimDeDuelo()) return false
        subTela = SubTelaYugioh.FIM_DUELO
        val motivo = p.motivoFimDuelo ?: ""
        val venceu = p.vencedorDuelo == p.nomeJogador
        val porExodia = motivo.contains("EXODIA", ignoreCase = true)
        val oponenteEhLuna = p.nomeOponente.contains("Luna", ignoreCase = true)

        if (porExodia) {
            audioVoz.tocarAplausos()
        }

        if (modoJogo == ModoJogoYugioh.TORNEIO) {
            if (venceu) {
                val tipoVitoria = if (dadosTorneio.faseAtual == 3) "CAMPEAO_TORNEIO" else "FASE_TORNEIO"
                bancoDados?.salvarVitoria("YUGIOH", p.nomeJogador, modoJogo.name, tipoVitoria, p.nomeOponente)
                bancoDados?.let {
                    vitoriasYugioh = it.obterTotalVitorias("YUGIOH", p.nomeJogador)
                    torneiosVencidosYugioh = it.obterTotalTorneiosYugioh(p.nomeJogador)
                } ?: run {
                    vitoriasYugioh++
                    if (dadosTorneio.faseAtual == 3) torneiosVencidosYugioh++
                }

                if (dadosTorneio.faseAtual == 1) {
                    audioVoz.tocarConfirmar()
                    val msg = "Vitória nas Quartas de Final do Torneio! Parabéns, Duelista ${p.nomeJogador}! Você derrotou ${p.nomeOponente}! $motivo Total: $vitoriasYugioh vitórias salvas. Você avançou para a SEMIFINAL!"
                    ultimaNarracao = msg
                    audioVoz.falar(msg, interromper = true)
                } else if (dadosTorneio.faseAtual == 2) {
                    audioVoz.tocarConfirmar()
                    val msg = "Vitória na Semifinal do Torneio! Sensacional, Duelista ${p.nomeJogador}! Você venceu ${p.nomeOponente}! $motivo Total: $vitoriasYugioh vitórias salvas. Você é o grande finalista e enfrentará a pequena LUNA!"
                    ultimaNarracao = msg
                    audioVoz.falar(msg, interromper = true)
                } else {
                    audioVoz.tocarCampeao()
                    audioVoz.tocarAplausos()
                    val msg = "🏆 É CAMPEÃO DO TORNEIO DA LUNA! PARABÉNS, DUELISTA ${p.nomeJogador}! Você venceu a Grande Final contra a doce Luna! $motivo Total no banco: $vitoriasYugioh vitórias e $torneiosVencidosYugioh torneios conquistados!"
                    ultimaNarracao = msg
                    audioVoz.falar(msg, interromper = true)
                    if (oponenteEhLuna) {
                        audioVoz.falarComoLuna("Parabéns ${p.nomeJogador}! Você jogou muito bem e é o grande campeão! Foi demais brincar com você!", interromper = false)
                    }
                }
            } else {
                audioVoz.tocarDerrota()
                val msg = "Fim do Torneio! ${p.nomeOponente} venceu a partida na ${dadosTorneio.nomeFaseCurto()}. $motivo Você foi eliminado do Torneio da Luna. Tente novamente!"
                ultimaNarracao = msg
                audioVoz.falar(msg, interromper = true)
                if (oponenteEhLuna) {
                    audioVoz.falarComoLuna("Ebaaa! Eu ganhei! Brincar de Yu-Gi-Oh com você é muito divertido!", interromper = false)
                }
            }
        } else {
            if (venceu) {
                val tipoVitoria = when {
                    porExodia -> "EXODIA"
                    motivo.contains("Deck Out", ignoreCase = true) -> "DECK_OUT"
                    else -> "LP_ZERO"
                }
                bancoDados?.salvarVitoria("YUGIOH", p.nomeJogador, modoJogo.name, tipoVitoria, p.nomeOponente)
                bancoDados?.let {
                    vitoriasYugioh = it.obterTotalVitorias("YUGIOH", p.nomeJogador)
                    torneiosVencidosYugioh = it.obterTotalTorneiosYugioh(p.nomeJogador)
                } ?: run {
                    vitoriasYugioh++
                }

                audioVoz.tocarCampeao()
                audioVoz.tocarAplausos()
                val msg = "Vitória no Duelo! Parabéns, Duelista ${p.nomeJogador}! $motivo Seu total acumulado é de $vitoriasYugioh vitórias salvas no banco."
                ultimaNarracao = msg
                audioVoz.falar(msg, interromper = true)
                if (oponenteEhLuna) {
                    audioVoz.falarComoLuna("Puxa vida, você joga muito bem! Foi super divertido brincar com você!", interromper = false)
                }
            } else {
                audioVoz.tocarDerrota()
                val msg = "Fim do Duelo! ${p.nomeOponente} venceu esta partida. $motivo Prepare seu Deck para a revanche, ${p.nomeJogador}!"
                ultimaNarracao = msg
                audioVoz.falar(msg, interromper = true)
                if (oponenteEhLuna) {
                    audioVoz.falarComoLuna("Ebaaa! Eu venci! Vamos jogar outra vez?", interromper = false)
                }
            }
        }
        return true
    }

    fun tratarTecla(
        keyCode: Int,
        scope: CoroutineScope,
        audioVoz: GerenciadorAudioVoz,
        nomeJogador: String,
        aoVoltarMenuLuna: () -> Unit
    ): Boolean {
        when (subTela) {
            SubTelaYugioh.ESCOLHA_DECK -> {
                when (keyCode) {
                    KeyEvent.KEYCODE_1 -> { selecionarDeckBase(0, audioVoz, nomeJogador); return true }
                    KeyEvent.KEYCODE_2 -> { selecionarDeckBase(1, audioVoz, nomeJogador); return true }
                    KeyEvent.KEYCODE_3 -> { selecionarDeckBase(2, audioVoz, nomeJogador); return true }
                    KeyEvent.KEYCODE_4 -> { selecionarDeckBase(3, audioVoz, nomeJogador); return true }
                    KeyEvent.KEYCODE_5 -> { selecionarDeckBase(4, audioVoz, nomeJogador); return true }
                    KeyEvent.KEYCODE_6 -> { selecionarDeckBase(5, audioVoz, nomeJogador); return true }
                    KeyEvent.KEYCODE_7 -> { selecionarDeckBase(6, audioVoz, nomeJogador); return true }
                    KeyEvent.KEYCODE_8 -> { selecionarDeckBase(7, audioVoz, nomeJogador); return true }
                    KeyEvent.KEYCODE_9 -> { selecionarDeckBase(8, audioVoz, nomeJogador); return true }
                    KeyEvent.KEYCODE_SPACE, KeyEvent.KEYCODE_ENTER -> { iniciarDuelo(nomeJogador, audioVoz); return true }
                    KeyEvent.KEYCODE_ESCAPE, KeyEvent.KEYCODE_BACK -> { aoVoltarMenuLuna(); return true }
                }
            }
            SubTelaYugioh.PERSONALIZAR_CARTAS -> {
                if (keyCode == KeyEvent.KEYCODE_ESCAPE || keyCode == KeyEvent.KEYCODE_BACK) {
                    subTela = SubTelaYugioh.ESCOLHA_DECK
                    audioVoz.falar("Voltando para a escolha de Deck.", interromper = true)
                    return true
                }
            }
            SubTelaYugioh.BLUETOOTH_SALA -> {
                if (keyCode == KeyEvent.KEYCODE_ESCAPE || keyCode == KeyEvent.KEYCODE_BACK) {
                    subTela = SubTelaYugioh.ESCOLHA_DECK
                    audioVoz.falar("Voltando para a escolha de Deck.", interromper = true)
                    return true
                }
            }
            SubTelaYugioh.ARENA_DUELO -> {
                when (keyCode) {
                    KeyEvent.KEYCODE_DPAD_LEFT -> { navegarMaoAnterior(audioVoz); return true }
                    KeyEvent.KEYCODE_DPAD_RIGHT -> { navegarMaoProxima(audioVoz); return true }
                    KeyEvent.KEYCODE_SPACE -> { jogarCartaSelecionada(emAtaque = true, audioVoz = audioVoz); return true }
                    KeyEvent.KEYCODE_D -> { jogarCartaSelecionada(emAtaque = false, audioVoz = audioVoz); return true }
                    KeyEvent.KEYCODE_A -> { atacarComMonstroJogador(audioVoz); return true }
                    KeyEvent.KEYCODE_P -> { mudarPosicaoMonstroJogador(audioVoz); return true }
                    KeyEvent.KEYCODE_M, KeyEvent.KEYCODE_H -> { narrarMaoAtualizada(audioVoz); return true }
                    KeyEvent.KEYCODE_E, KeyEvent.KEYCODE_DEL -> { encerrarTurnoJogador(scope, audioVoz); return true }
                    KeyEvent.KEYCODE_ENTER, KeyEvent.KEYCODE_R -> {
                        partidaAtual?.let {
                            val r = it.falarResumoCampo()
                            ultimaNarracao = r
                            audioVoz.falar(r, interromper = true)
                        }
                        return true
                    }
                    KeyEvent.KEYCODE_ESCAPE -> {
                        jobTurnoIa?.cancel()
                        subTela = SubTelaYugioh.ESCOLHA_DECK
                        audioVoz.falar("Saindo da arena e voltando para a seleção de Decks.", interromper = true)
                        return true
                    }
                }
            }
            SubTelaYugioh.FIM_DUELO -> {
                when (keyCode) {
                    KeyEvent.KEYCODE_S, KeyEvent.KEYCODE_SPACE, KeyEvent.KEYCODE_ENTER -> {
                        if (modoJogo == ModoJogoYugioh.TORNEIO) {
                            val p = partidaAtual
                            val venceu = p?.vencedorDuelo == nomeJogador
                            if (venceu && dadosTorneio.faseAtual < 3) {
                                avancarFaseTorneio(nomeJogador, audioVoz)
                            } else {
                                reiniciarTorneio(nomeJogador, audioVoz)
                            }
                        } else {
                            iniciarDuelo(nomeJogador, audioVoz)
                        }
                        return true
                    }
                    KeyEvent.KEYCODE_N, KeyEvent.KEYCODE_ESCAPE -> {
                        subTela = SubTelaYugioh.ESCOLHA_DECK
                        audioVoz.falar("Voltando para a escolha de Deck.", interromper = true)
                        return true
                    }
                }
            }
        }
        return false
    }
}

@Composable
fun TelaYugiohLuna(
    estado: EstadoYugiohLuna,
    nomeJogador: String,
    audioVoz: GerenciadorAudioVoz,
    scope: CoroutineScope,
    aoAbrirTelaNome: () -> Unit,
    aoVoltarParaJogosDaLuna: () -> Unit
) {
    val tick = estado.versao
    if (tick < 0) return

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0B1021))
            .padding(14.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Cabeçalho com Nome do Duelista e vitórias
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF17213A)),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, Color(0xFFFFD54F), RoundedCornerShape(14.dp))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "🐉 YU-GI-OH! DUELO ACESSÍVEL DA LUNA",
                    color = Color(0xFFFFD54F),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "Duelista: $nomeJogador  |  Vitórias: ${estado.vitoriasYugioh}  |  🏆 Torneios: ${estado.torneiosVencidosYugioh}",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // Painel do Locutor / Juiz da Arena (LiveRegion Polite)
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .semantics(mergeDescendants = true) {
                    liveRegion = LiveRegionMode.Polite
                    contentDescription = "Narrador do Duelo: ${estado.ultimaNarracao}. Toque duas vezes para ouvir novamente."
                    role = Role.Button
                    onClick(label = "Ouvir narração novamente") {
                        audioVoz.falar(estado.ultimaNarracao, interromper = true)
                        true
                    }
                }
        ) {
            Text(
                text = "🔊 ${estado.ultimaNarracao}",
                color = Color(0xFFFEF08A),
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(12.dp)
            )
        }

        when (estado.subTela) {
            SubTelaYugioh.ESCOLHA_DECK -> SecaoEscolhaDeckYugioh(
                estado = estado,
                nomeJogador = nomeJogador,
                audioVoz = audioVoz,
                aoAbrirTelaNome = aoAbrirTelaNome,
                aoVoltarParaJogosDaLuna = aoVoltarParaJogosDaLuna
            )
            SubTelaYugioh.PERSONALIZAR_CARTAS -> SecaoPersonalizarDeckYugioh(
                estado = estado,
                audioVoz = audioVoz
            )
            SubTelaYugioh.BLUETOOTH_SALA -> SecaoBluetoothYugioh(
                estado = estado,
                nomeJogador = nomeJogador,
                audioVoz = audioVoz
            )
            SubTelaYugioh.ARENA_DUELO -> SecaoArenaDueloYugioh(
                estado = estado,
                audioVoz = audioVoz,
                scope = scope
            )
            SubTelaYugioh.FIM_DUELO -> SecaoFimDueloYugioh(
                estado = estado,
                nomeJogador = nomeJogador,
                audioVoz = audioVoz
            )
        }
    }
}

@Composable
private fun SecaoEscolhaDeckYugioh(
    estado: EstadoYugiohLuna,
    nomeJogador: String,
    audioVoz: GerenciadorAudioVoz,
    aoAbrirTelaNome: () -> Unit,
    aoVoltarParaJogosDaLuna: () -> Unit
) {
    val decks = CatalogoYugiohLuna.decksPredefinidos
    val deckAtual = decks[estado.indiceDeckJogador]
    val oponenteAtual = decks[estado.indiceDeckOponente]

    Button(
        onClick = aoAbrirTelaNome,
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F766E), contentColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .semantics(mergeDescendants = true) {
                contentDescription = "Alterar nome do Duelista. Nome atual: $nomeJogador. Toque duas vezes para digitar seu nome."
                role = Role.Button
            }
    ) {
        Text("👤 Alterar Nome do Duelista (Atual: $nomeJogador)", fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }

    Text(
        text = "1. ESCOLHA O SEU DECK:",
        color = Color(0xFFFFD54F),
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold
    )

    decks.forEachIndexed { idx, deck ->
        val selecionado = idx == estado.indiceDeckJogador
        val corBotao = if (selecionado) Color(0xFF1D4ED8) else Color(0xFF1E293B)
        val prefixo = if (selecionado) "✅ [SELECIONADO] " else ""
        Button(
            onClick = { estado.selecionarDeckBase(idx, audioVoz, nomeJogador) },
            colors = ButtonDefaults.buttonColors(containerColor = corBotao, contentColor = Color.White),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = if (selecionado) 2.5.dp else 1.dp,
                    color = if (selecionado) Color(0xFFFFD54F) else Color(0xFF475569),
                    shape = RoundedCornerShape(12.dp)
                )
                .semantics(mergeDescendants = true) {
                    contentDescription = "${if (selecionado) "Selecionado: " else ""}${deck.nomeDeck}. ${deck.descricao}"
                    stateDescription = if (selecionado) "Deck selecionado" else "Toque duas vezes para escolher este Deck"
                    role = Role.Button
                }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "$prefixo${deck.nomeDeck}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (selecionado) Color(0xFFFFD54F) else Color.White
                )
                Text(
                    text = deck.descricao,
                    fontSize = 13.sp,
                    color = Color(0xFFCBD5E1)
                )
            }
        }
    }

    // Botão para escolher o Oponente
    Button(
        onClick = { estado.alternarOponente(audioVoz) },
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6D28D9), contentColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .semantics(mergeDescendants = true) {
                contentDescription = "Oponente atual: ${oponenteAtual.personagem} (${oponenteAtual.nomeDeck}). Toque duas vezes para trocar de oponente."
                role = Role.Button
            }
    ) {
        Text(
            text = "⚔️ Oponente: ${oponenteAtual.personagem} (Toque p/ trocar)",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }

    // Botão para Personalizar Cartas Individuais do Deck
    Button(
        onClick = {
            estado.subTela = SubTelaYugioh.PERSONALIZAR_CARTAS
            val msg = "Personalizador de Cartas do ${deckAtual.nomeDeck}. Seu Deck tem ${estado.idsCartasDeckPersonalizado.size} cartas. Toque em qualquer uma das 32 cartas para adicionar ou remover cópias."
            estado.ultimaNarracao = msg
            audioVoz.falar(msg, interromper = true)
        },
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0369A1), contentColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .semantics(mergeDescendants = true) {
                contentDescription = "Personalizar Cartas do Deck (${estado.idsCartasDeckPersonalizado.size} cartas). Adicione ou remova monstros, magias e armadilhas."
                role = Role.Button
            }
    ) {
        Text(
            text = "🃏 Personalizar Cartas do Deck (${estado.idsCartasDeckPersonalizado.size} cartas)",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }

    // Botão de Destaque: INICIAR TORNEIO DA LUNA!
    Button(
        onClick = { estado.iniciarTorneio(nomeJogador, audioVoz) },
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB45309), contentColor = Color.White),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(68.dp)
            .border(2.5.dp, Color(0xFFFFD54F), RoundedCornerShape(14.dp))
            .semantics(mergeDescendants = true) {
                contentDescription = "Iniciar Torneio da Luna! Disputa em 3 fases eliminatórias: Quartas de Final e Semifinal contra adversários sorteados aleatoriamente, e a Grande Final contra Luna usando um Deck montado aleatoriamente como ela quiser!"
                role = Role.Button
            }
    ) {
        Text(
            text = "🏆 INICIAR TORNEIO DA LUNA (Final c/ Luna!)",
            fontSize = 17.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFFFFD54F)
        )
    }

    // Botão Secundário: DUELO RÁPIDO AVULSO
    Button(
        onClick = { estado.iniciarDuelo(nomeJogador, audioVoz) },
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF15803D), contentColor = Color.White),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .border(1.5.dp, Color(0xFF86EFAC), RoundedCornerShape(14.dp))
            .semantics(mergeDescendants = true) {
                contentDescription = "Iniciar Duelo Rápido Avulso como $nomeJogador usando ${deckAtual.nomeDeck} contra ${oponenteAtual.personagem}."
                role = Role.Button
            }
    ) {
        Text(
            text = "⚔️ INICIAR DUELO RÁPIDO AVULSO",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }

    // Botão DUELO BLUETOOTH (2 CELULARES)
    val contextBt = LocalContext.current
    Button(
        onClick = { estado.abrirTelaBluetooth(contextBt, audioVoz) },
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7), contentColor = Color.White),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .border(2.dp, Color(0xFF38BDF8), RoundedCornerShape(14.dp))
            .semantics(mergeDescendants = true) {
                contentDescription = "Duelar por Bluetooth com 2 Celulares. Crie uma sala ou conecte a outro celular pareado."
                role = Role.Button
            }
    ) {
        Text(
            text = "📡 DUELAR POR BLUETOOTH (2 CELULARES)",
            fontSize = 16.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White
        )
    }

    Button(
        onClick = aoVoltarParaJogosDaLuna,
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF991B1B), contentColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .semantics(mergeDescendants = true) {
                contentDescription = "Voltar ao menu principal dos Jogos da Luna"
                role = Role.Button
            }
    ) {
        Text("Voltar aos Jogos da Luna", fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun SecaoPersonalizarDeckYugioh(
    estado: EstadoYugiohLuna,
    audioVoz: GerenciadorAudioVoz
) {
    Text(
        text = "PERSONALIZAR CARTAS DO DECK (${estado.idsCartasDeckPersonalizado.size} cartas)",
        color = Color(0xFFFFD54F),
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold
    )

    Button(
        onClick = {
            estado.subTela = SubTelaYugioh.ESCOLHA_DECK
            val msg = "Deck salvo com ${estado.idsCartasDeckPersonalizado.size} cartas. Pronto para iniciar o duelo!"
            estado.ultimaNarracao = msg
            audioVoz.falar(msg, interromper = true)
        },
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF15803D), contentColor = Color.White),
        modifier = Modifier.fillMaxWidth().height(54.dp)
    ) {
        Text("✅ Concluir e Voltar para Iniciar Duelo", fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }

    CatalogoYugiohLuna.todasAsCartas.forEach { carta ->
        val copias = estado.contarCopiasNoDeck(carta.id)
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = if (copias > 0) 1.5.dp else 1.dp,
                    color = if (copias > 0) Color(0xFFFFD54F) else Color(0xFF334155),
                    shape = RoundedCornerShape(12.dp)
                )
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "${carta.resumoCurto()}  —  No Deck: $copias cópia(s)",
                    color = if (copias > 0) Color(0xFFFFD54F) else Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = carta.descricao,
                    color = Color(0xFFCBD5E1),
                    fontSize = 14.sp
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { estado.adicionarCartaAoDeck(carta, audioVoz) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF15803D)),
                        modifier = Modifier
                            .weight(1f)
                            .semantics(mergeDescendants = true) {
                                contentDescription = "Adicionar 1 cópia de ${carta.nome}. Atualmente $copias no Deck. ${carta.falarCompleta()}"
                                role = Role.Button
                            }
                    ) {
                        Text("+1 no Deck", fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { estado.removerCartaDoDeck(carta, audioVoz) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF991B1B)),
                        modifier = Modifier
                            .weight(1f)
                            .semantics(mergeDescendants = true) {
                                contentDescription = "Remover 1 cópia de ${carta.nome}. Atualmente $copias no Deck."
                                role = Role.Button
                            }
                    ) {
                        Text("-1 do Deck", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun obterIdRecursoCarta(context: android.content.Context, idCarta: Int): Int {
    val nomeRecurso = "carta_$idCarta"
    val resId = context.resources.getIdentifier(nomeRecurso, "drawable", context.packageName)
    return if (resId != 0) resId else R.drawable.carta_1
}

@Composable
fun CardMonstroVisual(
    carta: CartaYugioh,
    emCampo: Boolean = false,
    emAtaque: Boolean = true,
    oculto: Boolean = false,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val corBorda = when (carta.tipo) {
        TipoCartaYugioh.MONSTRO -> if (carta.nivel >= 10) Color(0xFFFFD700) else Color(0xFFF59E0B)
        TipoCartaYugioh.MAGIA -> Color(0xFF10B981)
        TipoCartaYugioh.ARMADILHA -> Color(0xFFA855F7)
    }
    val corFundo = when (carta.tipo) {
        TipoCartaYugioh.MONSTRO -> Color(0xFF1E293B)
        TipoCartaYugioh.MAGIA -> Color(0xFF064E3B)
        TipoCartaYugioh.ARMADILHA -> Color(0xFF4A044E)
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = corFundo),
        shape = RoundedCornerShape(10.dp),
        modifier = modifier
            .border(2.dp, corBorda, RoundedCornerShape(10.dp))
            .padding(2.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(4.dp)
        ) {
            Image(
                painter = painterResource(id = obterIdRecursoCarta(context, carta.id)),
                contentDescription = carta.nome,
                modifier = Modifier
                    .size(if (emCampo) 115.dp else 56.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .border(1.5.dp, Color(0xFFFFD54F), RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = carta.nome,
                color = Color.White,
                fontSize = if (emCampo) 12.sp else 10.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            if (carta.tipo == TipoCartaYugioh.MONSTRO) {
                Text(
                    text = "★".repeat(carta.nivel.coerceIn(1, 10)),
                    color = Color(0xFFFFD54F),
                    fontSize = 9.sp
                )
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(text = "⚔️${carta.atk}", color = Color(0xFFFCA5A5), fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                    Text(text = "🛡️${carta.def}", color = Color(0xFF93C5FD), fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                }
            }
        }
    }
}

@Composable
private fun SecaoBluetoothYugioh(
    estado: EstadoYugiohLuna,
    nomeJogador: String,
    audioVoz: GerenciadorAudioVoz
) {
    val bt = estado.gerenciadorBluetooth

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, Color(0xFF38BDF8), RoundedCornerShape(14.dp))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "📡 DUELO VIA BLUETOOTH (2 CELULARES)",
                color = Color(0xFF38BDF8),
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = "Status: ${bt?.mensagemStatus ?: "Bluetooth pronto"}",
                color = Color(0xFFFEF08A),
                fontSize = 14.sp
            )

            // Botão Criar Sala (Servidor)
            Button(
                onClick = {
                    bt?.iniciarServidor {
                        audioVoz.tocarConfirmar()
                        audioVoz.falar("Duelista conectado via Bluetooth! Iniciando o duelo!", interromper = true)
                    }
                    val msg = "Aguardando outro duelista se conectar à sua sala Bluetooth..."
                    audioVoz.falar(msg, interromper = true)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1D4ED8)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
            ) {
                Text("📡 CRIAR SALA BLUETOOTH (Esperar Celular)", fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }

            // Botão Atualizar Aparelhos
            Button(
                onClick = {
                    bt?.atualizarDispositivosPareados()
                    val msg = "Dispositivos pareados atualizados: ${bt?.dispositivosPareados?.size ?: 0} encontrados."
                    audioVoz.falar(msg, interromper = true)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text("🔄 Atualizar Aparelhos Pareados", fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }

            Text(
                text = "APARELHOS PAREADOS (Toque para Conectar):",
                color = Color(0xFFFFD54F),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )

            val pareados = bt?.dispositivosPareados ?: emptyList()
            if (pareados.isEmpty()) {
                Text(
                    text = "Nenhum celular pareado encontrado. Pareie os dois celulares nas Configurações de Bluetooth do Android antes de duelar.",
                    color = Color(0xFF94A3B8),
                    fontSize = 13.sp
                )
            } else {
                pareados.forEach { dev ->
                    val nomeDev = try { dev.name ?: "Celular Pareado" } catch (_: Exception) { "Celular" }
                    Button(
                        onClick = {
                            bt?.conectarADispositivo(dev) {
                                bt.enviarComando("START:$nomeJogador:${estado.indiceDeckJogador + 1}")
                                audioVoz.tocarConfirmar()
                                audioVoz.falar("Conectado ao duelo com $nomeDev!", interromper = true)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().height(52.dp)
                    ) {
                        Text("📲 Conectar a $nomeDev", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Button(
                onClick = {
                    estado.subTela = SubTelaYugioh.ESCOLHA_DECK
                    audioVoz.falar("Voltando para a seleção de Decks.", interromper = true)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF475569)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text("Voltar", fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun SecaoArenaDueloYugioh(
    estado: EstadoYugiohLuna,
    audioVoz: GerenciadorAudioVoz,
    scope: CoroutineScope
) {
    val tick = estado.versao
    val p = estado.partidaAtual ?: return
    val idxSeguro = if (p.maoJogador.isEmpty()) -1 else p.indiceCartaMaoSelecionada.coerceIn(0, p.maoJogador.lastIndex)
    val cartaSelecionada = if (idxSeguro >= 0) p.maoJogador[idxSeguro] else null
    val falaCartaSel = cartaSelecionada?.falarCompleta() ?: "Nenhuma carta na mão"
    val falaCartaCurta = cartaSelecionada?.let { "${it.nome} (${it.resumoCurto()})" } ?: "Nenhuma" 

    // 0. Banner da Fase do Torneio (se ativo)
    if (estado.modoJogo == ModoJogoYugioh.TORNEIO) {
        val finalissima = estado.dadosTorneio.faseAtual == 3
        Card(
            colors = CardDefaults.cardColors(containerColor = if (finalissima) Color(0xFF831843) else Color(0xFF1E1B4B)),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, if (finalissima) Color(0xFFF472B6) else Color(0xFFFFD54F), RoundedCornerShape(10.dp))
                .semantics(mergeDescendants = true) {
                    contentDescription = "Fase do Torneio: ${estado.dadosTorneio.nomeFase()}"
                }
        ) {
            Text(
                text = if (finalissima)
                    "👑 GRANDE FINAL: CONTRA LUNA (DECK SURPRESA ALEATÓRIO)"
                else
                    "🏆 TORNEIO DA LUNA: ${estado.dadosTorneio.nomeFaseCurto().uppercase()}",
                color = Color(0xFFFFD54F),
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp, horizontal = 10.dp)
            )
        }
    }

    // 1. Placar de Pontos de Vida e Resumo da Arena (Toque para ouvir)
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF172554)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.5.dp, Color(0xFF38BDF8), RoundedCornerShape(12.dp))
            .semantics(mergeDescendants = true) {
                contentDescription = p.falarResumoCampo() + " Toque duas vezes para ouvir o resumo completo da arena."
                role = Role.Button
                onClick(label = "Ouvir resumo completo da arena") {
                    val r = p.falarResumoCampo()
                    estado.ultimaNarracao = r
                    audioVoz.falar(r, interromper = true)
                    true
                }
            }
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "❤️ ${p.nomeJogador}: ${p.lpJogador} LP   ⚡   ${p.nomeOponente}: ${p.lpOponente} LP",
                color = Color(0xFFFFD54F),
                fontSize = 17.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = "🛡️ Campo de ${p.nomeOponente}: ${p.monstroOponente?.falarStatus(false) ?: "Vazio"} | Armadilha: ${if (p.armadilhaOponente != null) "1 Oculta" else "Nenhuma"}",
                color = Color(0xFFFCA5A5),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "⚔️ Seu Campo (${p.nomeJogador}): ${p.monstroJogador?.falarStatus(true) ?: "Vazio"} | Armadilha: ${p.armadilhaJogador?.nome ?: "Nenhuma"}",
                color = Color(0xFF86EFAC),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Turno ${p.turnoNumero} (Toque aqui p/ ouvir o estado completo da arena)",
                color = Color(0xFFCBD5E1),
                fontSize = 13.sp
            )

            // Ilustrações dos Monstros do Oponente em Campo
            if (p.monstrosOponente.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Monstro(s) de ${p.nomeOponente} na Arena:",
                    color = Color(0xFFFCA5A5),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    p.monstrosOponente.forEach { m ->
                        CardMonstroVisual(
                            carta = m.cartaBase,
                            emCampo = true,
                            emAtaque = m.emAtaque,
                            oculto = m.oculto,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Ilustrações dos Seus Monstros em Campo
            if (p.monstrosJogador.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Seus Monstro(s) na Arena:",
                    color = Color(0xFF86EFAC),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    p.monstrosJogador.forEach { m ->
                        CardMonstroVisual(
                            carta = m.cartaBase,
                            emCampo = true,
                            emAtaque = m.emAtaque,
                            oculto = m.oculto,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }

    // 2. Painel Interativo da Mão com LiveRegion para Leitores de Tela (TalkBack)
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.5.dp, Color(0xFFFFD54F), RoundedCornerShape(12.dp))
            .semantics(mergeDescendants = true) {
                liveRegion = LiveRegionMode.Polite
                contentDescription = estado.obterTextoMaoAtualizada(p) + " Toque duas vezes para ouvir toda a sua mão atualizada."
                stateDescription = "Carta selecionada: $falaCartaCurta"
                role = Role.Button
                onClick(label = "Ouvir mão atualizada") {
                    estado.narrarMaoAtualizada(audioVoz)
                    true
                }
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "🃏 SUA MÃO ATUALIZADA (${p.maoJogador.size} cartas):",
                    color = Color(0xFFFFD54F),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = if (p.maoJogador.isNotEmpty()) {
                        p.maoJogador.mapIndexed { idx, c ->
                            val s = if (idx == p.indiceCartaMaoSelecionada) "👉 " else ""
                            "$s${c.nome}"
                        }.joinToString("  •  ")
                    } else {
                        "Sua mão está vazia"
                    },
                    color = Color(0xFFE2E8F0),
                    fontSize = 13.sp,
                    maxLines = 2
                )
            }
            Button(
                onClick = { estado.narrarMaoAtualizada(audioVoz) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1D4ED8)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.padding(start = 6.dp)
            ) {
                Text("🔊 Ouvir Mão", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            onClick = { estado.navegarMaoAnterior(audioVoz) },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E3A8A)),
            modifier = Modifier
                .weight(1f)
                .height(52.dp)
                .semantics(mergeDescendants = true) {
                    contentDescription = "Carta anterior. Carta selecionada: $falaCartaSel"
                    stateDescription = "Carta selecionada: $falaCartaCurta"
                    role = Role.Button
                }
        ) {
            Text("◀ Carta Anterior", fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }
        Button(
            onClick = { estado.navegarMaoProxima(audioVoz) },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E3A8A)),
            modifier = Modifier
                .weight(1f)
                .height(52.dp)
                .semantics(mergeDescendants = true) {
                    contentDescription = "Próxima carta. Carta selecionada: $falaCartaSel"
                    stateDescription = "Carta selecionada: $falaCartaCurta"
                    role = Role.Button
                }
        ) {
            Text("Próxima Carta ▶", fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }
    }

    // Lista de botões para cada carta da mão (acesso direto e toque imediato para jogar)
    p.maoJogador.forEachIndexed { idx, carta ->
        val sel = idx == idxSeguro
        val tributosExigidos = carta.tributosNecessarios
        val monstrosEmCampo = p.monstrosJogador.size
        val statusTributoTxt = when {
            carta.tipo != TipoCartaYugioh.MONSTRO -> ""
            tributosExigidos > 0 && monstrosEmCampo >= tributosExigidos -> " 👑 [Tributo pronto: $monstrosEmCampo/$tributosExigidos]"
            tributosExigidos > 0 -> " 🔒 [Exige $tributosExigidos Tributos - Campo tem $monstrosEmCampo]"
            else -> ""
        }
        val exodiaTag = if (carta.id in setOf(60, 61, 62, 63, 64)) " 🧩 [Exodia: ${p.contarPecasExodiaNaMao(true)}/5]" else ""
        val acaoToqueTxt = when (carta.tipo) {
            TipoCartaYugioh.MAGIA -> "Toque duas vezes para ATIVAR MAGIA imediatamente"
            TipoCartaYugioh.ARMADILHA -> "Toque duas vezes para ARMAR virada para baixo imediatamente"
            TipoCartaYugioh.MONSTRO -> if (tributosExigidos > 0) {
                if (monstrosEmCampo >= tributosExigidos) "Toque duas vezes para SACRIFICAR $tributosExigidos monstro(s) e invocar por Tributo imediatamente em Ataque"
                else "Exige $tributosExigidos Tributos. Não pode ser jogada pois você tem apenas $monstrosEmCampo monstro(s) em campo"
            } else {
                "Toque duas vezes para INVOCAR em Posição de Ataque imediatamente"
            }
        }

        key(carta.id, idx, sel, p.maoJogador.size) {
            Button(
                onClick = { estado.tocarEJogarCartaMao(idx, audioVoz) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (sel) Color(0xFF1D4ED8) else Color(0xFF1E293B),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = if (sel) 2.dp else 1.dp,
                        color = if (sel) Color(0xFFFFD54F) else Color(0xFF475569),
                        shape = RoundedCornerShape(10.dp)
                    )
                    .semantics(mergeDescendants = true) {
                        contentDescription = "Carta ${idx + 1} de ${p.maoJogador.size}: ${carta.falarCompleta()}.$statusTributoTxt$exodiaTag $acaoToqueTxt."
                        stateDescription = if (sel) "Selecionada" else ""
                        selected = sel
                        role = Role.Button
                    }
            ) {
                val contextCard = LocalContext.current
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Image(
                        painter = painterResource(id = obterIdRecursoCarta(contextCard, carta.id)),
                        contentDescription = null,
                        modifier = Modifier
                            .size(54.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.5.dp, Color(0xFFFFD54F), RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.Start) {
                        Text(
                            text = "${if (sel) "👉 " else ""}${carta.resumoCurto()}$statusTributoTxt$exodiaTag",
                            fontSize = 15.sp,
                            fontWeight = if (sel) FontWeight.ExtraBold else FontWeight.Medium,
                            color = if (sel) Color(0xFFFFD54F) else Color.White
                        )
                        Text(
                            text = "⚡ Toque para jogar agora",
                            fontSize = 12.sp,
                            color = Color(0xFF93C5FD),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }

    // 3. Botões de Ação para a Carta Selecionada (com Opção de Tributo Acessível)
    if (cartaSelecionada != null) {
        // Exibição visual destacada da Carta Selecionada
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            CardMonstroVisual(
                carta = cartaSelecionada,
                emCampo = true,
                emAtaque = true,
                modifier = Modifier.width(200.dp)
            )
        }

        when (cartaSelecionada.tipo) {
            TipoCartaYugioh.MONSTRO -> {
                val tribs = cartaSelecionada.tributosNecessarios
                if (tribs > 0) {
                    val temMonstrosSuficientes = p.monstrosJogador.size >= tribs
                    if (temMonstrosSuficientes) {
                        val sacrificadosNomes = p.monstrosJogador.take(tribs).joinToString(" e ") { it.cartaBase.nome }
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF451A03)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(2.dp, Color(0xFFF59E0B), RoundedCornerShape(12.dp))
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = if (tribs == 3) "⚡ INVOCACÃO DIVINA (3 TRIBUTOS):" else "👑 OPÇÃO DE TRIBUTO ACESSÍVEL ($tribs TRIBUTOS):",
                                    color = Color(0xFFFCD34D),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = "Sacrificar $sacrificadosNomes para invocar ${cartaSelecionada.nome} (${cartaSelecionada.atk} ATK)!",
                                    color = Color.White,
                                    fontSize = 13.sp
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = { estado.jogarCartaSelecionada(emAtaque = true, audioVoz = audioVoz) },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(58.dp)
                                            .semantics(mergeDescendants = true) {
                                                contentDescription = "Invocar por Tributo: Sacrificar $sacrificadosNomes para invocar ${cartaSelecionada.nome} em Posição de Ataque com ${cartaSelecionada.atk} de Ataque"
                                                role = Role.Button
                                            }
                                    ) {
                                        Text("👑 Sacrificar p/ ATK (${cartaSelecionada.atk})", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Button(
                                        onClick = { estado.jogarCartaSelecionada(emAtaque = false, audioVoz = audioVoz) },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(58.dp)
                                            .semantics(mergeDescendants = true) {
                                                contentDescription = "Invocar por Tributo: Sacrificar $sacrificadosNomes para colocar ${cartaSelecionada.nome} em Posição de Defesa com ${cartaSelecionada.def} de Defesa"
                                                role = Role.Button
                                            }
                                    ) {
                                        Text("🛡️ Sacrificar p/ DEF (${cartaSelecionada.def})", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    } else {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF450A0A)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.5.dp, Color(0xFFEF4444), RoundedCornerShape(12.dp))
                                .semantics(mergeDescendants = true) {
                                    contentDescription = "Opção de Tributo Bloqueada: ${cartaSelecionada.nome} é de Nível ${cartaSelecionada.nivel} e exige $tribs monstros em campo como Tributo, mas seu campo tem apenas ${p.monstrosJogador.size}. Invoque primeiro monstros de nível 1 a 4."
                                    role = Role.Button
                                    onClick(label = "Ouvir aviso de tributo") {
                                        audioVoz.falar("Opção de Tributo Bloqueada: ${cartaSelecionada.nome} exige $tribs monstros no campo como Tributo, mas você tem apenas ${p.monstrosJogador.size}.", interromper = true)
                                        true
                                    }
                                }
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("🔒 OPÇÃO DE TRIBUTO BLOQUEADA", color = Color(0xFFFCA5A5), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text("${cartaSelecionada.nome} (Nível ${cartaSelecionada.nivel}) exige $tribs monstro(s) como Tributo, mas seu campo tem apenas ${p.monstrosJogador.size}. Invoque antes monstros nível 1 a 4 para ter tributos suficientes.", color = Color.White, fontSize = 13.sp)
                            }
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { estado.jogarCartaSelecionada(emAtaque = true, audioVoz = audioVoz) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF15803D)),
                            modifier = Modifier
                                .weight(1f)
                                .height(58.dp)
                                .semantics(mergeDescendants = true) {
                                    contentDescription = "Invocar ${cartaSelecionada.nome} em Posição de Ataque com ${cartaSelecionada.atk} pontos"
                                    role = Role.Button
                                }
                        ) {
                            Text("🗡️ Invocar em ATK (${cartaSelecionada.atk})", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = { estado.jogarCartaSelecionada(emAtaque = false, audioVoz = audioVoz) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0369A1)),
                            modifier = Modifier
                                .weight(1f)
                                .height(58.dp)
                                .semantics(mergeDescendants = true) {
                                    contentDescription = "Colocar ${cartaSelecionada.nome} em Posição de Defesa com ${cartaSelecionada.def} pontos"
                                    role = Role.Button
                                }
                        ) {
                            Text("🛡️ Colocar em DEF (${cartaSelecionada.def})", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            TipoCartaYugioh.MAGIA -> {
                Button(
                    onClick = { estado.jogarCartaSelecionada(emAtaque = true, audioVoz = audioVoz) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF047857)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp)
                        .semantics(mergeDescendants = true) {
                            contentDescription = "Ativar Carta de Magia ${cartaSelecionada.nome}. ${cartaSelecionada.descricao}"
                            role = Role.Button
                        }
                ) {
                    Text("✨ ATIVAR MAGIA: ${cartaSelecionada.nome}", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
            TipoCartaYugioh.ARMADILHA -> {
                Button(
                    onClick = { estado.jogarCartaSelecionada(emAtaque = false, audioVoz = audioVoz) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7E22CE)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp)
                        .semantics(mergeDescendants = true) {
                            contentDescription = "Armar Carta de Armadilha ${cartaSelecionada.nome} virada para baixo. ${cartaSelecionada.descricao}"
                            role = Role.Button
                        }
                ) {
                    Text("🪤 ARMAR ARMADILHA: ${cartaSelecionada.nome}", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    // 4. Botões de Combate e Turno
    Text(
        text = "AÇÕES DE CAMPO E TURNO:",
        color = Color(0xFFFFD54F),
        fontSize = 17.sp,
        fontWeight = FontWeight.Bold
    )

    Button(
        onClick = { estado.atacarComMonstroJogador(audioVoz) },
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB91C1C), contentColor = Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .semantics(mergeDescendants = true) {
                val m = p.monstroJogador
                contentDescription = when {
                    p.turnoNumero == 1 -> "Atacar Bloqueado: No primeiro turno do duelo não é permitido atacar pelas regras oficiais de Yu-Gi-Oh."
                    m != null -> "Atacar com seu monstro ${m.cartaBase.nome} (${m.atkAtual} de Ataque)"
                    else -> "Atacar com Monstro (Você ainda não tem monstro no campo)"
                }
                role = Role.Button
            }
    ) {
        val m = p.monstroJogador
        val txtAtk = if (m != null) "⚔️ ATACAR COM ${m.cartaBase.nome.uppercase()} (${m.atkAtual} ATK)" else "⚔️ ATACAR COM MONSTRO"
        Text(txtAtk, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            onClick = { estado.mudarPosicaoMonstroJogador(audioVoz) },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4338CA)),
            modifier = Modifier
                .weight(1f)
                .height(54.dp)
                .semantics(mergeDescendants = true) {
                    contentDescription = "Mudar posição do seu monstro entre Ataque e Defesa"
                    role = Role.Button
                }
        ) {
            Text("🔄 Mudar ATK/DEF", fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
        Button(
            onClick = { estado.encerrarTurnoJogador(scope, audioVoz) },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
            modifier = Modifier
                .weight(1f)
                .height(54.dp)
                .semantics(mergeDescendants = true) {
                    contentDescription = "Passar Turno e encerrar sua vez"
                    role = Role.Button
                }
        ) {
            Text("⏭️ PASSAR TURNO", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
        }
    }

    Button(
        onClick = {
            estado.jobTurnoIa?.cancel()
            estado.subTela = SubTelaYugioh.ESCOLHA_DECK
            val msg = "Saindo do duelo. Voltando para a seleção de Decks."
            estado.ultimaNarracao = msg
            audioVoz.falar(msg, interromper = true)
        },
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF475569)),
        modifier = Modifier.fillMaxWidth().height(48.dp)
    ) {
        Text("Trocar de Deck / Sair da Arena", fontSize = 15.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun SecaoFimDueloYugioh(
    estado: EstadoYugiohLuna,
    nomeJogador: String,
    audioVoz: GerenciadorAudioVoz
) {
    val p = estado.partidaAtual
    val venceu = p?.vencedorDuelo == nomeJogador

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = if (venceu) "🏆 VITÓRIA DO DUELISTA $nomeJogador!" else "⚡ FIM DO DUELO",
                color = Color(0xFFFFD54F),
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )
            Text(
                text = estado.ultimaNarracao,
                color = Color.White,
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            )
            if (estado.modoJogo == ModoJogoYugioh.TORNEIO) {
                if (venceu) {
                    if (estado.dadosTorneio.faseAtual < 3) {
                        val proxNome = if (estado.dadosTorneio.faseAtual == 1) "A SEMIFINAL" else "A GRANDE FINAL CONTRA LUNA"
                        Button(
                            onClick = { estado.avancarFaseTorneio(nomeJogador, audioVoz) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB45309)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp)
                                .border(2.dp, Color(0xFFFFD54F), RoundedCornerShape(12.dp))
                                .semantics(mergeDescendants = true) {
                                    contentDescription = "Avançar para $proxNome do Torneio da Luna."
                                    role = Role.Button
                                }
                        ) {
                            Text("➡️ AVANÇAR PARA $proxNome!", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFFFD54F))
                        }
                    } else {
                        Button(
                            onClick = { estado.reiniciarTorneio(nomeJogador, audioVoz) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF15803D)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp)
                                .border(2.dp, Color(0xFFFFD54F), RoundedCornerShape(12.dp))
                                .semantics(mergeDescendants = true) {
                                    contentDescription = "Jogar novo Torneio da Luna para defender o título!"
                                    role = Role.Button
                                }
                        ) {
                            Text("🏆 JOGAR NOVO TORNEIO DA LUNA", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                } else {
                    Button(
                        onClick = { estado.reiniciarTorneio(nomeJogador, audioVoz) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB91C1C)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(58.dp)
                            .semantics(mergeDescendants = true) {
                                contentDescription = "Tentar o Torneio da Luna novamente desde o início."
                                role = Role.Button
                            }
                    ) {
                        Text("🔄 TENTAR O TORNEIO NOVAMENTE", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Button(
                    onClick = {
                        estado.subTela = SubTelaYugioh.ESCOLHA_DECK
                        audioVoz.falar("Voltando para a escolha de Deck.", interromper = true)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) {
                    Text("🃏 VOLTAR AO MENU DE DECKS", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                Button(
                    onClick = { estado.iniciarDuelo(nomeJogador, audioVoz) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF15803D)),
                    modifier = Modifier.fillMaxWidth().height(58.dp)
                ) {
                    Text("🔥 JOGAR REVANCHE COM ESTE DECK", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = {
                        estado.subTela = SubTelaYugioh.ESCOLHA_DECK
                        audioVoz.falar("Escolha seu Deck para o próximo duelo.", interromper = true)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1D4ED8)),
                    modifier = Modifier.fillMaxWidth().height(54.dp)
                ) {
                    Text("🃏 ESCOLHER OUTRO DECK", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
