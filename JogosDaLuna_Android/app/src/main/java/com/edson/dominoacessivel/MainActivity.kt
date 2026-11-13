package com.edson.dominoacessivel

import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random
import com.edson.dominoacessivel.academiapython.motor.MotorJogoPython

enum class TelaJogo {
    INFORMAR_NOME,
    SELECAO_JOGO,
    MENU_PRINCIPAL,
    ESCOLHA_PARCEIRO,
    AJUDA,
    PARTIDA,
    FIM_PARTIDA,
    PARTIDA_TRUCO,
    FIM_PARTIDA_TRUCO,
    ACADEMIA_PYTHON,
    YUGIOH_LUNA,
    BLUETOOTH_SALA
}

data class OpcaoMenu(val id: Int, val titulo: String, val fala: String)

class MainActivity : ComponentActivity() {

    private lateinit var audioVoz: GerenciadorAudioVoz
    private lateinit var gerenciadorBt: GerenciadorBluetoothDuelo
    private lateinit var motorJogoPython: MotorJogoPython
    private val estadoAcademiaPython = EstadoAcademiaPython()
    private val estadoYugiohLuna = EstadoYugiohLuna()
    private val motorLuna = MotorLunaBayesMonteCarlo()
    private val redeNeuralTruco = RedeNeuralTrucoLuna()

    private var telaAtual by mutableStateOf(TelaJogo.SELECAO_JOGO)
    private var telaRetornoAposNome by mutableStateOf(TelaJogo.SELECAO_JOGO)
    private var indiceSelecaoJogo by mutableIntStateOf(0)
    private var indiceMenu by mutableIntStateOf(0)
    private var indiceParceiro by mutableIntStateOf(0)

    private var modoSelecionado by mutableIntStateOf(1)
    private var nomeUsuario by mutableStateOf("Edson")
    private var rascunhoNome by mutableStateOf("")
    private var parceiroSelecionado by mutableStateOf<String?>(null)
    private var vitoriasJogador by mutableIntStateOf(0)
    private var vitoriasTrucoJogador by mutableIntStateOf(0)
    private val nivelAtual = 4
    private val historicoAdversarios = mutableListOf<String>()

    private var jogoAtual by mutableStateOf<JogoDomino?>(null)
    private var partidaTrucoAtual by mutableStateOf<PartidaTruco?>(null)
    private var versaoEstado by mutableIntStateOf(0)
    private var aguardandoJogadaUsuario by mutableStateOf(false)
    private var aguardandoJogadaTrucoUsuario by mutableStateOf(false)
    private var pedraPendenteEscolhaLado by mutableStateOf<Pedra?>(null)
    private var ultimaMensagemJuiz by mutableStateOf("Bem-vindo aos Jogos da Luna!")
    private val historicoMensagens = mutableStateListOf<String>()
    private var textoResultadoPartida by mutableStateOf("")

    private var jobPartida: Job? = null

    private val opcoesSelecaoJogo = listOf(
        OpcaoMenu(
            1,
            "1 - Dominó Acessível (Individual, Duplas e Desafio Luna)",
            "Opção 1: Jogar Dominó Acessível, com modos Individual, Duplas e Desafio contra Luna"
        ),
        OpcaoMenu(
            2,
            "2 - Truco Paulista contra Luna (Rede Neural e Monte Carlo)",
            "Opção 2: Jogar Truco Paulista contra a Rede Neural da Luna"
        ),
        OpcaoMenu(
            3,
            "3 - Academia Python da Luna (Aprenda Programação com Áudio)",
            "Opção 3: Academia Python da Luna. Aprenda programação com 8 módulos, desafios, códigos e jogo da forca com voz"
        ),
        OpcaoMenu(
            4,
            "4 - Yu-Gi-Oh! Duelo Acessível (Escolha de Decks vs Luna)",
            "Opção 4: Jogar Yu-Gi-Oh! Duelo Acessível. Escolha seu Deck de Yugi, Kaiba, Joey ou Luna e duele na arena"
        ),
        OpcaoMenu(
            5,
            "5 - Informar / Alterar Nome do Jogador",
            "Opção 5: Informar ou alterar o nome do jogador"
        ),
        OpcaoMenu(
            6,
            "6 - Ajuda e Comandos (Dominó, Truco, Python e Yu-Gi-Oh!)",
            "Opção 6: Ajuda e instruções de uso do Dominó, Truco, Academia Python e Yu-Gi-Oh!"
        ),
        OpcaoMenu(
            8,
            "7 - Jogar via Bluetooth (Dominó, Truco, Python e Yu-Gi-Oh)",
            "Opção 7: Duelo via Bluetooth entre 2 celulares pareados. Escolha Dominó, Truco, Python ou Yu-Gi-Oh!"
        ),
        OpcaoMenu(
            7,
            "8 - Sair do Aplicativo",
            "Opção 8: Sair do aplicativo"
        )
    )

    private val opcoesMenuPrincipal = listOf(
        OpcaoMenu(1, "1 - Modo Individual vs Argentina (1 vs 1)", "Opção 1: Jogar Modo Individual versus Argentina, 1 contra 1"),
        OpcaoMenu(2, "2 - Modo Duplas com Seleção Brasileira (2 vs 2)", "Opção 2: Jogar Modo Duplas, 2 contra 2"),
        OpcaoMenu(3, "3 - Desafio com Luna (IA Bayes e Monte Carlo)", "Opção 3: Jogar Modo Desafio com Luna, Inteligência Bayesiana e Monte Carlo"),
        OpcaoMenu(7, "4 - Duelo de Dominó via Bluetooth (2 Celulares)", "Opção 4: Jogar Dominó via Bluetooth contra um amigo em outro celular"),
        OpcaoMenu(5, "5 - Ajuda e Comandos", "Opção 5: Ajuda e instruções de uso"),
        OpcaoMenu(6, "6 - Voltar à Escolha de Jogo (Dominó / Truco)", "Opção 6: Voltar ao menu de escolha de jogo"),
        OpcaoMenu(4, "7 - Sair do Jogo", "Opção 7: Sair do jogo")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences("jogos_da_luna_prefs", MODE_PRIVATE)
        val nomeSalvo = prefs.getString("nome_jogador", null)
        if (!nomeSalvo.isNullOrBlank()) {
            nomeUsuario = nomeSalvo
            rascunhoNome = nomeSalvo
            telaAtual = TelaJogo.SELECAO_JOGO
        } else {
            nomeUsuario = "Edson"
            rascunhoNome = ""
            telaAtual = TelaJogo.INFORMAR_NOME
        }

        motorJogoPython = MotorJogoPython(this)
        gerenciadorBt = GerenciadorBluetoothDuelo(this).apply {
            aoReceberComando = { cmd -> processarComandoBluetoothUniversal(cmd) }
        }

        val banco = BancoDadosJogosLuna.obterInstancia(this)
        estadoYugiohLuna.bancoDados = banco
        estadoYugiohLuna.vitoriasYugioh = banco.obterTotalVitorias("YUGIOH", nomeUsuario)
        estadoYugiohLuna.torneiosVencidosYugioh = banco.obterTotalTorneiosYugioh(nomeUsuario)
        vitoriasJogador = banco.obterTotalVitorias("DOMINO", nomeUsuario)
        vitoriasTrucoJogador = banco.obterTotalVitorias("TRUCO", nomeUsuario)
        audioVoz = GerenciadorAudioVoz(this) {
            if (telaAtual == TelaJogo.INFORMAR_NOME) {
                audioVoz.falar(
                    "Bem-vindo aos Jogos da Luna! Informe o seu nome de jogador na caixa de texto e toque em Confirmar Nome para começar.",
                    interromper = true
                )
            } else {
                anunciarSelecaoJogo()
            }
        }

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = CorFundoMesa
                ) {
                    val tick = versaoEstado
                    if (tick >= 0) {
                        when (telaAtual) {
                            TelaJogo.INFORMAR_NOME -> TelaInformarNome()
                            TelaJogo.SELECAO_JOGO -> TelaSelecaoJogo()
                            TelaJogo.MENU_PRINCIPAL -> TelaMenuPrincipal()
                            TelaJogo.ESCOLHA_PARCEIRO -> TelaEscolhaParceiro()
                            TelaJogo.AJUDA -> TelaAjuda()
                            TelaJogo.PARTIDA -> TelaPartida()
                            TelaJogo.FIM_PARTIDA -> TelaFimPartida()
                            TelaJogo.PARTIDA_TRUCO -> TelaPartidaTruco()
                            TelaJogo.FIM_PARTIDA_TRUCO -> TelaFimPartidaTruco()
                            TelaJogo.ACADEMIA_PYTHON -> TelaAcademiaPython(
                                estado = estadoAcademiaPython,
                                motorJogo = motorJogoPython,
                                audioVoz = audioVoz,
                                nomeJogador = nomeUsuario,
                                aoVoltarParaJogosDaLuna = {
                                    telaAtual = TelaJogo.SELECAO_JOGO
                                    anunciarSelecaoJogo()
                                }
                            )
                            TelaJogo.YUGIOH_LUNA -> TelaYugiohLuna(
                                estado = estadoYugiohLuna,
                                nomeJogador = nomeUsuario,
                                audioVoz = audioVoz,
                                scope = lifecycleScope,
                                aoAbrirTelaNome = {
                                    abrirTelaInformarNome(TelaJogo.YUGIOH_LUNA)
                                },
                                aoVoltarParaJogosDaLuna = {
                                    estadoYugiohLuna.jobTurnoIa?.cancel()
                                    telaAtual = TelaJogo.SELECAO_JOGO
                                    anunciarSelecaoJogo()
                                }
                            )
                            TelaJogo.BLUETOOTH_SALA -> TelaBluetoothUniversal(
                                gerenciadorBt = gerenciadorBt,
                                nomeJogador = nomeUsuario,
                                audioVoz = audioVoz,
                                aoIniciarJogoBluetooth = { jogo ->
                                    iniciarJogoPorBluetooth(jogo)
                                },
                                aoVoltar = {
                                    telaAtual = TelaJogo.SELECAO_JOGO
                                    anunciarSelecaoJogo()
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    private fun abrirTelaInformarNome(retorno: TelaJogo) {
        telaRetornoAposNome = retorno
        rascunhoNome = nomeUsuario
        telaAtual = TelaJogo.INFORMAR_NOME
        audioVoz.tocarNavegar()
        audioVoz.falar(
            "Informar nome do jogador. Nome atual: $nomeUsuario. Digite o seu nome na caixa de texto e toque em Confirmar Nome.",
            interromper = true
        )
    }

    private fun salvarNomeJogador(novoNome: String) {
        val limpo = novoNome.trim().ifEmpty { "Edson" }
        nomeUsuario = limpo
        rascunhoNome = limpo
        getSharedPreferences("jogos_da_luna_prefs", MODE_PRIVATE)
            .edit()
            .putString("nome_jogador", limpo)
            .apply()

        val banco = BancoDadosJogosLuna.obterInstancia(this)
        estadoYugiohLuna.vitoriasYugioh = banco.obterTotalVitorias("YUGIOH", limpo)
        estadoYugiohLuna.torneiosVencidosYugioh = banco.obterTotalTorneiosYugioh(limpo)
        vitoriasJogador = banco.obterTotalVitorias("DOMINO", limpo)
        vitoriasTrucoJogador = banco.obterTotalVitorias("TRUCO", limpo)
        audioVoz.tocarConfirmar()
        telaAtual = telaRetornoAposNome
        if (telaAtual == TelaJogo.YUGIOH_LUNA) {
            val msg = "Nome do Duelista confirmado: $nomeUsuario! Escolha seu Deck para iniciar o duelo."
            estadoYugiohLuna.ultimaNarracao = msg
            audioVoz.falar(msg, interromper = true)
        } else {
            audioVoz.falar(
                "Olá, $nomeUsuario! Nome confirmado nos Jogos da Luna. Escolha o jogo: Dominó, Truco Paulista, Academia Python ou Yu-Gi-Oh! Duelo Acessível.",
                interromper = true
            )
        }
    }

    override fun onDestroy() {
        jobPartida?.cancel()
        estadoYugiohLuna.jobTurnoIa?.cancel()
        audioVoz.liberar()
        super.onDestroy()
    }

    private fun registrarMensagem(msg: String) {
        ultimaMensagemJuiz = msg
        historicoMensagens.add(0, msg)
        if (historicoMensagens.size > 6) {
            historicoMensagens.removeAt(historicoMensagens.lastIndex)
        }
        versaoEstado++
    }

    private suspend fun anunciarPausado(
        textoVisual: String,
        textoFalado: String = textoVisual,
        interromper: Boolean = false,
        pausaFixaMs: Long? = null
    ) {
        registrarMensagem(textoVisual)
        audioVoz.falar(textoFalado, interromper = interromper)
        val espera = pausaFixaMs ?: audioVoz.duracaoEstimadaMs(textoFalado)
        delay(espera)
    }

    private fun anunciarImediato(textoVisual: String, textoFalado: String = textoVisual, interromper: Boolean = true) {
        registrarMensagem(textoVisual)
        audioVoz.falar(textoFalado, interromper = interromper)
    }

    private fun anunciarSelecaoJogo() {
        val op = opcoesSelecaoJogo[indiceSelecaoJogo]
        audioVoz.falar(
            "Jogos da Luna. Jogador: $nomeUsuario. Escolha o jogo: Dominó, Truco Paulista, Academia Python ou Yu-Gi-Oh! Duelo Acessível. ${op.fala}",
            interromper = true
        )
    }

    private fun navegarSelecaoJogoAnterior() {
        indiceSelecaoJogo = (indiceSelecaoJogo - 1 + opcoesSelecaoJogo.size) % opcoesSelecaoJogo.size
        audioVoz.tocarNavegar()
        audioVoz.falar(opcoesSelecaoJogo[indiceSelecaoJogo].fala, interromper = true)
    }

    private fun navegarSelecaoJogoProximo() {
        indiceSelecaoJogo = (indiceSelecaoJogo + 1) % opcoesSelecaoJogo.size
        audioVoz.tocarNavegar()
        audioVoz.falar(opcoesSelecaoJogo[indiceSelecaoJogo].fala, interromper = true)
    }

    private fun confirmarSelecaoJogo(idOpcao: Int = opcoesSelecaoJogo[indiceSelecaoJogo].id) {
        when (idOpcao) {
            1 -> {
                indiceMenu = 0
                telaAtual = TelaJogo.MENU_PRINCIPAL
                anunciarMenuPrincipal()
            }
            2 -> {
                iniciarNovaPartidaTruco()
            }
            3 -> {
                telaAtual = TelaJogo.ACADEMIA_PYTHON
                estadoAcademiaPython.telaAtual = SubTelaAcademia.MENU_PRINCIPAL
                estadoAcademiaPython.anunciarMenuPrincipal(audioVoz, motorJogoPython)
            }
            4 -> {
                telaAtual = TelaJogo.YUGIOH_LUNA
                estadoYugiohLuna.subTela = SubTelaYugioh.ESCOLHA_DECK
                val deckSel = CatalogoYugiohLuna.decksPredefinidos[estadoYugiohLuna.indiceDeckJogador]
                val msg = "Yu-Gi-Oh! Duelo Acessível da Luna! Bem-vindo, Duelista $nomeUsuario! Deck atual selecionado: ${deckSel.nomeDeck}. Toque em um dos 9 Decks lendários para escolher ou em Iniciar Duelo Agora."
                estadoYugiohLuna.ultimaNarracao = msg
                audioVoz.falar(msg, interromper = true)
            }
            5 -> {
                abrirTelaInformarNome(TelaJogo.SELECAO_JOGO)
            }
            6 -> {
                telaAtual = TelaJogo.AJUDA
                audioVoz.falar(
                    "Ajuda dos Jogos da Luna para $nomeUsuario. Opção 1: Dominó Acessível. " +
                        "Opção 2: Truco Paulista contra a Rede Neural da Luna. " +
                        "Opção 3: Academia Python da Luna. " +
                        "Opção 4: Yu-Gi-Oh! Duelo Acessível, com escolha de 9 Decks clássicos da franquia, personalização de cartas, opção de tributo acessível e cartas jogadas com um toque.",
                    interromper = true
                )
            }
            7 -> {
                audioVoz.falar("Saindo do aplicativo.", interromper = true)
                finish()
            }
            8 -> {
                abrirTelaBluetoothUniversal()
            }
        }
    }

    fun abrirTelaBluetoothUniversal(jogo: JogoBluetooth = JogoBluetooth.DOMINO) {
        gerenciadorBt.jogoSelecionado = jogo
        gerenciadorBt.atualizarDispositivosPareados()
        telaAtual = TelaJogo.BLUETOOTH_SALA
        val msg = "Sala de Duelos via Bluetooth! Jogo selecionado: ${jogo.titulo}. Crie uma sala ou conecte a um celular pareado."
        audioVoz.falar(msg, interromper = true)
    }

    private fun iniciarJogoPorBluetooth(jogo: JogoBluetooth) {
        when (jogo) {
            JogoBluetooth.DOMINO -> {
                modoSelecionado = 4
                val amigo = gerenciadorBt.nomeDispositivoConectado ?: "Amigo Bluetooth"
                parceiroSelecionado = amigo
                if (gerenciadorBt.souHost) {
                    val novoJogo = JogoDomino(
                        modo = 4,
                        nomeJogador = nomeUsuario,
                        nomeParceiro = amigo
                    )
                    jogoAtual = novoJogo
                    telaAtual = TelaJogo.PARTIDA
                    audioVoz.tocarEmbaralharDomino()
                    val pedrasCliente = novoJogo.maos[1]?.joinToString(";") { "${it.a},${it.b}" } ?: ""
                    val pIni = novoJogo.pedraInicial
                    val pIniStr = if (pIni != null) "${pIni.a},${pIni.b}" else "-1,-1"
                    gerenciadorBt.enviarComando("DOM_START:${novoJogo.jogadorAtual}:$pIniStr:$nomeUsuario:$pedrasCliente")
                    jobPartida = lifecycleScope.launch {
                        anunciarPausado("Duelo de Dominó via Bluetooth iniciado contra $amigo!", interromper = true)
                        continuarLoopPartida()
                    }
                } else {
                    audioVoz.tocarEmbaralharDomino()
                    audioVoz.falar("Aguardando o celular do amigo iniciar as pedras do dominó...", interromper = true)
                }
            }
            JogoBluetooth.TRUCO -> {
                val amigo = gerenciadorBt.nomeDispositivoConectado ?: "Amigo Bluetooth"
                if (gerenciadorBt.souHost) {
                    val novaPartida = PartidaTruco(nomeJogador = nomeUsuario, nomeAdversario = amigo)
                    novaPartida.iniciarNovaMao()
                    partidaTrucoAtual = novaPartida
                    telaAtual = TelaJogo.PARTIDA_TRUCO
                    versaoEstado++
                    val maosAmigo = novaPartida.maoLuna.joinToString(";") { "${it.valor},${it.naipe.name}" }
                    val vira = novaPartida.vira
                    val viraStr = if (vira != null) "${vira.valor},${vira.naipe.name}" else "4,OUROS"
                    gerenciadorBt.enviarComando("TRU_START:$viraStr:$nomeUsuario:$maosAmigo:${novaPartida.quemIniciaMao}")
                    jobPartida = lifecycleScope.launch {
                        anunciarPausado("Truco Paulista via Bluetooth iniciado contra $amigo!", interromper = true)
                        executarNovaMaoTruco()
                    }
                } else {
                    audioVoz.falar("Aguardando o amigo distribuir as cartas de Truco...", interromper = true)
                }
            }
            JogoBluetooth.PYTHON -> {
                telaAtual = TelaJogo.ACADEMIA_PYTHON
                estadoAcademiaPython.telaAtual = SubTelaAcademia.QUIZ_MODULO
                audioVoz.falar("Duelo da Academia Python via Bluetooth iniciado!", interromper = true)
            }
            JogoBluetooth.YUGIOH -> {
                telaAtual = TelaJogo.YUGIOH_LUNA
                estadoYugiohLuna.abrirTelaBluetooth(this, audioVoz)
            }
        }
    }

    private fun processarComandoBluetoothUniversal(cmd: String) {
        when {
            cmd.startsWith("SELECIONAR_JOGO:") -> {
                val nomeJogo = cmd.removePrefix("SELECIONAR_JOGO:")
                try {
                    val j = JogoBluetooth.valueOf(nomeJogo)
                    gerenciadorBt.jogoSelecionado = j
                    audioVoz.falar("O amigo selecionou o jogo ${j.titulo}.", interromper = true)
                } catch (_: Exception) {}
            }
            cmd.startsWith("INICIAR_PARTIDA:") -> {
                val nomeJogo = cmd.removePrefix("INICIAR_PARTIDA:")
                try {
                    val j = JogoBluetooth.valueOf(nomeJogo)
                    gerenciadorBt.jogoSelecionado = j
                    iniciarJogoPorBluetooth(j)
                } catch (_: Exception) {}
            }
            cmd.startsWith("DOM_START:") -> {
                val partes = cmd.removePrefix("DOM_START:").split(":")
                val quemInicia = partes.getOrNull(0)?.toIntOrNull() ?: 0
                val pIniStr = partes.getOrNull(1) ?: "-1,-1"
                val nomeHost = partes.getOrNull(2) ?: "Amigo Bluetooth"
                val pedrasStr = partes.getOrNull(3) ?: ""

                val pedrasCliente = pedrasStr.split(";").mapNotNull { par ->
                    val ab = par.split(",")
                    if (ab.size == 2) {
                        val a = ab[0].toIntOrNull()
                        val b = ab[1].toIntOrNull()
                        if (a != null && b != null) Pedra(a, b) else null
                    } else null
                }

                modoSelecionado = 4
                parceiroSelecionado = nomeHost
                val novoJogo = JogoDomino(
                    modo = 4,
                    nomeJogador = nomeUsuario,
                    nomeParceiro = nomeHost
                )
                novoJogo.maos[0]?.clear()
                novoJogo.maos[0]?.addAll(pedrasCliente)
                novoJogo.jogadorAtual = if (quemInicia == 1) 0 else 1
                val pIniParts = pIniStr.split(",")
                val pA = pIniParts.getOrNull(0)?.toIntOrNull() ?: -1
                val pB = pIniParts.getOrNull(1)?.toIntOrNull() ?: -1
                if (pA >= 0 && pB >= 0) {
                    novoJogo.pedraInicial = Pedra(pA, pB)
                }

                jogoAtual = novoJogo
                telaAtual = TelaJogo.PARTIDA
                versaoEstado++
                audioVoz.tocarEmbaralharDomino()
                jobPartida = lifecycleScope.launch {
                    anunciarPausado("Duelo de Dominó via Bluetooth iniciado contra $nomeHost!", interromper = true)
                    continuarLoopPartida()
                }
            }
            cmd.startsWith("DOM_JOGAR:") -> {
                val partes = cmd.removePrefix("DOM_JOGAR:").split(":")
                val a = partes.getOrNull(0)?.toIntOrNull() ?: return
                val b = partes.getOrNull(1)?.toIntOrNull() ?: return
                val lado = partes.getOrNull(2) ?: "ESQUERDA"
                val jogo = jogoAtual ?: return
                val pedra = Pedra(a, b)
                val maoAdv = jogo.maos[1]
                if (maoAdv != null && !maoAdv.contains(pedra)) {
                    maoAdv.add(pedra)
                }
                jobPartida?.cancel()
                jobPartida = lifecycleScope.launch {
                    aplicarJogadaNaMesa(jogo, pedra, 1, lado)
                    jogo.jogadorAtual = 0
                    versaoEstado++
                    continuarLoopPartida()
                }
            }
            cmd.startsWith("DOM_PASSAR") -> {
                val jogo = jogoAtual ?: return
                jobPartida?.cancel()
                jobPartida = lifecycleScope.launch {
                    audioVoz.tocarPassar()
                    anunciarPausado("${jogo.nomes[1]} passou a vez.", interromper = false)
                    jogo.jogadorAtual = 0
                    versaoEstado++
                    continuarLoopPartida()
                }
            }
            cmd.startsWith("DOM_COMPRA:") -> {
                val partes = cmd.removePrefix("DOM_COMPRA:").split(":")
                val a = partes.getOrNull(0)?.toIntOrNull() ?: return
                val b = partes.getOrNull(1)?.toIntOrNull() ?: return
                val jogo = jogoAtual ?: return
                val p = Pedra(a, b)
                jogo.maos[0]?.add(p)
                audioVoz.tocarEmbaralharDomino()
                versaoEstado++
                anunciarImediato("Você comprou do monte a pedra [${p.a}|${p.b}].", interromper = true)
            }
            cmd.startsWith("TRU_START:") -> {
                val partes = cmd.removePrefix("TRU_START:").split(":")
                val viraStr = partes.getOrNull(0) ?: "4,OUROS"
                val nomeHost = partes.getOrNull(1) ?: "Amigo Bluetooth"
                val cartasStr = partes.getOrNull(2) ?: ""
                val novaPartida = PartidaTruco(nomeJogador = nomeUsuario, nomeAdversario = nomeHost)
                novaPartida.iniciarNovaMao()
                val vParts = viraStr.split(",")
                val naipeVira = try { NaipeTruco.valueOf(vParts.getOrElse(1) { "OUROS" }) } catch (_: Exception) { NaipeTruco.OUROS }
                novaPartida.vira = CartaTruco(vParts.getOrElse(0) { "4" }, naipeVira)
                novaPartida.valorManilha = proximoValorManilha(novaPartida.vira?.valor ?: "4")

                novaPartida.maoJogador.clear()
                cartasStr.split(";").forEach { c ->
                    val cp = c.split(",")
                    if (cp.size == 2) {
                        val n = try { NaipeTruco.valueOf(cp[1]) } catch (_: Exception) { NaipeTruco.OUROS }
                        novaPartida.maoJogador.add(CartaTruco(cp[0], n))
                    }
                }
                partidaTrucoAtual = novaPartida
                telaAtual = TelaJogo.PARTIDA_TRUCO
                versaoEstado++
                audioVoz.falar("Truco Paulista via Bluetooth iniciado contra $nomeHost!", interromper = true)
            }
            cmd.startsWith("TRU_JOGAR:") -> {
                val partes = cmd.removePrefix("TRU_JOGAR:").split(":")
                val valC = partes.getOrNull(0) ?: "4"
                val naipeC = try { NaipeTruco.valueOf(partes.getOrElse(1) { "OUROS" }) } catch (_: Exception) { NaipeTruco.OUROS }
                val coberta = partes.getOrNull(2)?.toBoolean() ?: false
                val partida = partidaTrucoAtual ?: return
                partida.cartaMesaLuna = CartaTruco(valC, naipeC, coberta)
                audioVoz.tocarConfirmar()
                versaoEstado++
                anunciarImediato("${partida.nomeAdversario} jogou ${partida.cartaMesaLuna?.falarCarta(partida.valorManilha)}", interromper = false)
            }
            cmd.startsWith("TRU_PEDIR:") -> {
                val valor = cmd.removePrefix("TRU_PEDIR:").toIntOrNull() ?: 3
                val partida = partidaTrucoAtual ?: return
                partida.valorProposto = valor
                partida.aguardandoRespostaTrucoUsuario = true
                audioVoz.tocarAplausos()
                anunciarImediato("${partida.nomeAdversario} GRITOU TRUCO valendo $valor tentos! Pressione S para aceitar, N para correr ou T para retrucar.", interromper = true)
            }
            cmd.startsWith("TRU_RESP:") -> {
                val resp = cmd.removePrefix("TRU_RESP:")
                val partida = partidaTrucoAtual ?: return
                anunciarImediato("${partida.nomeAdversario} respondeu ao truco: $resp!", interromper = true)
            }
            cmd.startsWith("YUG_") -> {
                estadoYugiohLuna.processarComandoBluetooth(cmd, audioVoz)
            }
        }
    }

    private fun anunciarMenuPrincipal() {
        val op = opcoesMenuPrincipal[indiceMenu]
        audioVoz.falar(
            "Menu do Dominó Acessível. Toque em uma opção ou use os botões Anterior, Próxima e Confirmar. ${op.fala}",
            interromper = true
        )
    }

    private fun navegarMenuAnterior() {
        indiceMenu = (indiceMenu - 1 + opcoesMenuPrincipal.size) % opcoesMenuPrincipal.size
        audioVoz.tocarNavegar()
        audioVoz.falar(opcoesMenuPrincipal[indiceMenu].fala, interromper = true)
    }

    private fun navegarMenuProximo() {
        indiceMenu = (indiceMenu + 1) % opcoesMenuPrincipal.size
        audioVoz.tocarNavegar()
        audioVoz.falar(opcoesMenuPrincipal[indiceMenu].fala, interromper = true)
    }

    private fun confirmarOpcaoMenu(idOpcao: Int = opcoesMenuPrincipal[indiceMenu].id) {
        when (idOpcao) {
            1 -> {
                modoSelecionado = 1
                parceiroSelecionado = null
                iniciarNovaPartida()
            }
            2 -> {
                modoSelecionado = 2
                indiceParceiro = 0
                telaAtual = TelaJogo.ESCOLHA_PARCEIRO
                val nome = CRAQUES_SELECAO[indiceParceiro]
                audioVoz.falar(
                    "Modo Duplas selecionado. Escolha seu parceiro da Seleção Brasileira. $nome",
                    interromper = true
                )
            }
            3 -> {
                modoSelecionado = 3
                parceiroSelecionado = null
                iniciarNovaPartida()
            }
            7 -> {
                abrirTelaBluetoothUniversal(JogoBluetooth.DOMINO)
            }
            5 -> {
                telaAtual = TelaJogo.AJUDA
                audioVoz.falar(
                    "Ajuda. Na partida de Dominó, use os botões Pedra Anterior e Próxima Pedra para navegar pela sua mão. " +
                        "No Truco, use Carta Anterior, Próxima Carta, Jogar Carta e Pedir Truco.",
                    interromper = true
                )
            }
            6 -> {
                telaAtual = TelaJogo.SELECAO_JOGO
                anunciarSelecaoJogo()
            }
            4 -> {
                audioVoz.falar("Saindo do jogo.", interromper = true)
                finish()
            }
        }
    }

    private fun navegarParceiroAnterior() {
        indiceParceiro = (indiceParceiro - 1 + CRAQUES_SELECAO.size) % CRAQUES_SELECAO.size
        audioVoz.tocarNavegar()
        audioVoz.falar(CRAQUES_SELECAO[indiceParceiro], interromper = true)
    }

    private fun navegarParceiroProximo() {
        indiceParceiro = (indiceParceiro + 1) % CRAQUES_SELECAO.size
        audioVoz.tocarNavegar()
        audioVoz.falar(CRAQUES_SELECAO[indiceParceiro], interromper = true)
    }

    private fun confirmarParceiro(nome: String = CRAQUES_SELECAO[indiceParceiro]) {
        parceiroSelecionado = nome
        iniciarNovaPartida()
    }

    private fun iniciarNovaPartida() {
        jobPartida?.cancel()
        pedraPendenteEscolhaLado = null
        aguardandoJogadaUsuario = false
        historicoMensagens.clear()

        val novoJogo = JogoDomino(
            modo = modoSelecionado,
            nomeJogador = nomeUsuario,
            nomeParceiro = parceiroSelecionado,
            historicoAdversarios = historicoAdversarios
        )
        if (modoSelecionado != 3) {
            historicoAdversarios.add(novoJogo.advPrincipal)
            if (historicoAdversarios.size > 5) {
                historicoAdversarios.removeAt(0)
            }
        }

        jogoAtual = novoJogo
        telaAtual = TelaJogo.PARTIDA
        versaoEstado++
        audioVoz.tocarEmbaralharDomino()

        jobPartida = lifecycleScope.launch {
            when (modoSelecionado) {
                1 -> anunciarPausado(
                    "Partida Individual iniciada! ${novoJogo.nomes[0]} versus ${novoJogo.nomes[1]}.",
                    interromper = true
                )
                3 -> anunciarPausado(
                    "Desafio Sádico Iniciado! ${novoJogo.nomes[0]} versus Luna (Inferência Bayesiana e Monte Carlo).",
                    interromper = true
                )
                4 -> anunciarPausado(
                    "Duelo de Dominó via Bluetooth! ${novoJogo.nomes[0]} versus ${novoJogo.nomes[1]}.",
                    interromper = true
                )
                else -> {
                    anunciarPausado(
                        "Partida em Duplas iniciada! Sua dupla: ${novoJogo.nomes[0]} e ${novoJogo.nomes[2]}.",
                        interromper = true
                    )
                    anunciarPausado(
                        "Dupla adversária: ${novoJogo.nomes[1]} e ${novoJogo.nomes[3]}.",
                        interromper = false
                    )
                }
            }

            val pIni = novoJogo.pedraInicial
            val descIni = if (pIni != null) {
                if (pIni.ehCarroca) " com a carroça de ${pIni.a}." else " com a maior pedra ${pIni.falarJuiz()}."
            } else "."
            anunciarPausado(
                "Quem inicia a partida: ${novoJogo.nomes[novoJogo.jogadorAtual]}$descIni",
                interromper = false
            )

            continuarLoopPartida()
        }
    }

    private suspend fun continuarLoopPartida() {
        val jogo = jogoAtual ?: return

        while (true) {
            for (idx in 0 until jogo.numJogadores) {
                if (jogo.maos[idx].isNullOrEmpty()) {
                    val vencedor = jogo.nomes[idx]
                    anunciarPausado("$vencedor bateu!", interromper = false)

                    if (jogo.modo == 1 || jogo.modo == 3 || jogo.modo == 4) {
                        if (idx == 0) {
                            audioVoz.tocarBaterMesa()
                            audioVoz.tocarAplausos()
                            audioVoz.tocarVitoria()
                            finalizarPartida(venceu = true, empatou = false, "Você bateu e venceu a partida!")
                        } else {
                            audioVoz.tocarBaterMesa()
                            audioVoz.tocarDerrota()
                            finalizarPartida(venceu = false, empatou = false, "$vencedor venceu a partida.")
                        }
                    } else {
                        if (idx == 0 || idx == 2) {
                            audioVoz.tocarBaterMesa()
                            audioVoz.tocarAplausos()
                            audioVoz.tocarVitoria()
                            finalizarPartida(
                                venceu = true,
                                empatou = false,
                                "Sua dupla (${jogo.nomes[0]} e ${jogo.nomes[2]}) venceu!"
                            )
                        } else {
                            audioVoz.tocarBaterMesa()
                            audioVoz.tocarDerrota()
                            finalizarPartida(
                                venceu = false,
                                empatou = false,
                                "A dupla adversária (${jogo.nomes[1]} e ${jogo.nomes[3]}) venceu."
                            )
                        }
                    }
                    return
                }
            }

            if (jogo.estaBloqueado()) {
                if (jogo.modo == 1 || jogo.modo == 3 || jogo.modo == 4) {
                    val pUser = contarPontos(jogo.maos[0] ?: emptyList())
                    val pAdv = contarPontos(jogo.maos[1] ?: emptyList())
                    anunciarPausado(
                        "Jogo fechado! Seus pontos: $pUser. ${jogo.nomes[1]}: $pAdv pontos.",
                        interromper = false
                    )
                    when {
                        pUser < pAdv -> {
                            audioVoz.tocarBaterMesa()
                            audioVoz.tocarAplausos()
                            audioVoz.tocarVitoria()
                            finalizarPartida(venceu = true, empatou = false, "Você venceu por menor pontuação ($pUser a $pAdv)!")
                        }
                        pUser > pAdv -> {
                            audioVoz.tocarBaterMesa()
                            audioVoz.tocarDerrota()
                            finalizarPartida(venceu = false, empatou = false, "${jogo.nomes[1]} venceu por menor pontuação ($pAdv a $pUser).")
                        }
                        else -> {
                            audioVoz.tocarBaterMesa()
                            finalizarPartida(venceu = false, empatou = true, "Empate por pontuação ($pUser pontos)!")
                        }
                    }
                } else {
                    val pSuaDupla = contarPontos(jogo.maos[0] ?: emptyList()) + contarPontos(jogo.maos[2] ?: emptyList())
                    val pAdvDupla = contarPontos(jogo.maos[1] ?: emptyList()) + contarPontos(jogo.maos[3] ?: emptyList())
                    anunciarPausado(
                        "Jogo fechado! Sua dupla: $pSuaDupla pontos. Dupla adversária: $pAdvDupla pontos.",
                        interromper = false
                    )
                    when {
                        pSuaDupla < pAdvDupla -> {
                            audioVoz.tocarBaterMesa()
                            audioVoz.tocarAplausos()
                            audioVoz.tocarVitoria()
                            finalizarPartida(venceu = true, empatou = false, "Sua dupla venceu por menor pontuação ($pSuaDupla a $pAdvDupla)!")
                        }
                        pSuaDupla > pAdvDupla -> {
                            audioVoz.tocarBaterMesa()
                            audioVoz.tocarDerrota()
                            finalizarPartida(venceu = false, empatou = false, "A dupla adversária venceu por menor pontuação ($pAdvDupla a $pSuaDupla).")
                        }
                        else -> {
                            audioVoz.tocarBaterMesa()
                            finalizarPartida(venceu = false, empatou = true, "Empate por pontuação ($pSuaDupla pontos)!")
                        }
                    }
                }
                return
            }

            val curr = jogo.jogadorAtual
            if (curr == 0) {
                val mao = jogo.maos[0] ?: emptyList()
                if (mao.isNotEmpty()) {
                    jogo.indiceSelecionado = jogo.indiceSelecionado.coerceIn(0, mao.lastIndex)
                }
                val pedraAtual = mao.getOrNull(jogo.indiceSelecionado)
                val infoPedra = pedraAtual?.let { ". Pedra selecionada: ${it.falarJuiz()}" } ?: ""
                val msgVez = "Sua vez. Mesa: ${falarPontasJuiz(jogo.tabuleiro)}$infoPedra"
                anunciarPausado(msgVez, msgVez, interromper = false, pausaFixaMs = 1600L)
                aguardandoJogadaUsuario = true
                versaoEstado++
                return
            } else if (jogo.modo == 4) {
                aguardandoJogadaUsuario = false
                val msgVez = "Vez de ${jogo.nomes[1]}. Aguardando jogada via Bluetooth..."
                anunciarPausado(msgVez, msgVez, interromper = false, pausaFixaMs = 1200L)
                versaoEstado++
                return
            } else {
                aguardandoJogadaUsuario = false
                executarTurnoIa(jogo, curr)
                jogo.jogadorAtual = (jogo.jogadorAtual + 1) % jogo.numJogadores
                versaoEstado++
            }
        }
    }

    private suspend fun executarTurnoIa(jogo: JogoDomino, idxJogador: Int) {
        val nome = jogo.nomes[idxJogador]
        val mao = jogo.maos[idxJogador] ?: return

        while (!temJogadaValida(mao, jogo.tabuleiro)) {
            if ((jogo.modo == 1 || jogo.modo == 3) && jogo.monte.isNotEmpty()) {
                jogo.pescarDoMonte(idxJogador)
                versaoEstado++
                anunciarPausado(
                    "$nome não tinha jogada e comprou uma pedra do monte.",
                    interromper = false,
                    pausaFixaMs = 1800L
                )
            } else {
                if (jogo.tabuleiro.isNotEmpty()) {
                    val (esq, dir) = extremidades(jogo.tabuleiro)
                    if (esq != null) jogo.passagensJogadores[idxJogador]?.add(esq)
                    if (dir != null) jogo.passagensJogadores[idxJogador]?.add(dir)
                }
                audioVoz.tocarPassar()
                anunciarPausado("$nome passou a vez.", interromper = false, pausaFixaMs = 1800L)
                return
            }
        }

        val validas = pedrasJogaveis(mao, jogo.tabuleiro)
        if (validas.isEmpty()) return

        val (pedraEscolhida, ladoEscolhido) = withContext(Dispatchers.Default) {
            if (jogo.tabuleiro.isEmpty()) {
                val pIni = jogo.pedraInicial ?: (mao.find { it.ehCarroca } ?: mao.maxBy { it.soma })
                pIni to null
            } else if (nome == "Luna" || nivelAtual >= 4) {
                motorLuna.escolherMelhorJogada(jogo, idxJogador, validas)
            } else {
                val carrocas = validas.filter { it.ehCarroca }
                val p = if (carrocas.isNotEmpty()) {
                    carrocas.maxBy { it.soma }
                } else {
                    validas.maxBy { it.soma }
                }
                p to null
            }
        }

        aplicarJogadaNaMesa(jogo, pedraEscolhida, idxJogador, ladoEscolhido)
    }

    private suspend fun aplicarJogadaNaMesa(
        jogo: JogoDomino,
        pedra: Pedra,
        idxJogador: Int,
        ladoForcado: String? = null
    ) {
        val (esquerda, direita) = extremidades(jogo.tabuleiro)
        val nome = jogo.nomes[idxJogador]

        if (esquerda == null || direita == null) {
            jogo.tabuleiro.add(pedra)
            jogo.removerPedra(pedra, idxJogador)
            audioVoz.tocarPedraDomino()
            versaoEstado++
            if (jogo.modo == 4 && idxJogador == 0) {
                gerenciadorBt.enviarComando("DOM_JOGAR:${pedra.a}:${pedra.b}:CENTRO")
            }
            anunciarPausado(
                "$nome jogou ${pedra.formatar()}",
                "$nome jogou ${pedra.falarJuiz()}",
                interromper = false
            )
            return
        }

        val encaixaEsquerda = pedraEncaixa(pedra, esquerda)
        val encaixaDireita = pedraEncaixa(pedra, direita)

        val lado = when {
            ladoForcado == "ESQUERDA" || ladoForcado == "DIREITA" -> ladoForcado
            encaixaEsquerda && encaixaDireita -> if (Random.nextBoolean()) "ESQUERDA" else "DIREITA"
            encaixaEsquerda -> "ESQUERDA"
            else -> "DIREITA"
        }

        if (lado == "ESQUERDA") {
            jogo.tabuleiro.add(0, orientarParaEsquerda(pedra, esquerda))
        } else {
            jogo.tabuleiro.add(orientarParaDireita(pedra, direita))
        }

        jogo.removerPedra(pedra, idxJogador)
        audioVoz.tocarPedraDomino()
        versaoEstado++
        if (jogo.modo == 4 && idxJogador == 0) {
            gerenciadorBt.enviarComando("DOM_JOGAR:${pedra.a}:${pedra.b}:$lado")
        }

        val ladoTexto = lado.lowercase()
        anunciarPausado(
            "$nome jogou ${pedra.formatar()} na $ladoTexto",
            "$nome jogou ${pedra.falarJuiz()} na $ladoTexto",
            interromper = false,
            pausaFixaMs = 2000L
        )

        if (nome == "Luna" && Random.nextDouble() < 0.8) {
            val provocacao = obterProvocacaoLunaJogada(nomeUsuario)
            anunciarPausado(
                "Luna diz: \"$provocacao\"",
                "Luna diz: $provocacao",
                interromper = false,
                pausaFixaMs = 2400L
            )
        }
    }

    private suspend fun finalizarPartida(venceu: Boolean, empatou: Boolean, mensagemFinal: String) {
        aguardandoJogadaUsuario = false
        textoResultadoPartida = mensagemFinal
        anunciarPausado(mensagemFinal, mensagemFinal, interromper = true)

        if (venceu && !empatou) {
            val banco = BancoDadosJogosLuna.obterInstancia(this)
            banco.salvarVitoria("DOMINO", nomeUsuario, "MODO_$modoSelecionado", "PONTOS", parceiroSelecionado ?: "Argentina")
            vitoriasJogador = banco.obterTotalVitorias("DOMINO", nomeUsuario)
            anunciarPausado("Total de vitórias salvas no banco: $vitoriasJogador.", interromper = false)
            anunciarPausado("Preparando a próxima partida...", interromper = false)
            iniciarNovaPartida()
        } else {
            telaAtual = TelaJogo.FIM_PARTIDA
            audioVoz.falar(
                "Deseja jogar novamente nesta mesma modalidade ou voltar ao menu principal?",
                interromper = false
            )
        }
    }

    // --- Ações do Jogador na Partida ---

    private fun acaoPedraAnterior() {
        val jogo = jogoAtual ?: return
        if (!aguardandoJogadaUsuario) return
        val mao = jogo.maos[0] ?: return
        if (mao.isEmpty()) return

        jogo.indiceSelecionado = (jogo.indiceSelecionado - 1 + mao.size) % mao.size
        audioVoz.tocarNavegar()
        val pedra = mao[jogo.indiceSelecionado]
        versaoEstado++
        anunciarImediato(
            "Selecionada: ${pedra.formatar()} (${pedra.falarJuiz()})",
            "Pedra selecionada: ${pedra.falarJuiz()}",
            interromper = true
        )
    }

    private fun acaoProximaPedra() {
        val jogo = jogoAtual ?: return
        if (!aguardandoJogadaUsuario) return
        val mao = jogo.maos[0] ?: return
        if (mao.isEmpty()) return

        jogo.indiceSelecionado = (jogo.indiceSelecionado + 1) % mao.size
        audioVoz.tocarNavegar()
        val pedra = mao[jogo.indiceSelecionado]
        versaoEstado++
        anunciarImediato(
            "Selecionada: ${pedra.formatar()} (${pedra.falarJuiz()})",
            "Pedra selecionada: ${pedra.falarJuiz()}",
            interromper = true
        )
    }

    private fun acaoSelecionarPedraDireto(indice: Int) {
        val jogo = jogoAtual ?: return
        if (!aguardandoJogadaUsuario) return
        val mao = jogo.maos[0] ?: return
        if (indice !in mao.indices) return

        jogo.indiceSelecionado = indice
        audioVoz.tocarNavegar()
        val pedra = mao[indice]
        versaoEstado++
        anunciarImediato(
            "Selecionada: ${pedra.formatar()} (${pedra.falarJuiz()})",
            "Pedra selecionada: ${pedra.falarJuiz()}",
            interromper = true
        )
    }

    private fun acaoJogarPedraDireto(indice: Int) {
        val jogo = jogoAtual ?: return
        if (!aguardandoJogadaUsuario) {
            audioVoz.tocarErro()
            anunciarImediato("Aguarde a sua vez de jogar.", interromper = true)
            return
        }
        val mao = jogo.maos[0] ?: return
        if (indice !in mao.indices) return

        jogo.indiceSelecionado = indice
        val pedra = mao[indice]
        val (esquerda, direita) = extremidades(jogo.tabuleiro)

        if (esquerda == null || direita == null) {
            // Mesa vazia: joga imediatamente
            executarJogadaConfirmadaUsuario(pedra, null)
            return
        }

        val encaixaEsquerda = pedraEncaixa(pedra, esquerda)
        val encaixaDireita = pedraEncaixa(pedra, direita)

        if (!encaixaEsquerda && !encaixaDireita) {
            audioVoz.tocarErro()
            anunciarImediato(
                "A pedra ${pedra.formatar()} não encaixa na mesa.",
                "A pedra ${pedra.falarJuiz()} não encaixa na mesa. As pontas são $esquerda e $direita.",
                interromper = true
            )
            return
        }

        if (encaixaEsquerda && encaixaDireita && esquerda != direita) {
            // Encaixa em duas pontas diferentes: solicita apenas o lado
            pedraPendenteEscolhaLado = pedra
            anunciarImediato(
                "Em qual lado jogar ${pedra.formatar()}? Toque na Esquerda ($esquerda) ou Direita ($direita).",
                "Em qual lado jogar ${pedra.falarJuiz()}? Toque duas vezes em Esquerda ponta $esquerda ou Direita ponta $direita.",
                interromper = true
            )
            return
        }

        val ladoUnico = if (encaixaEsquerda) "ESQUERDA" else "DIREITA"
        executarJogadaConfirmadaUsuario(pedra, ladoUnico)
    }

    private fun acaoOuvirPontas() {
        val jogo = jogoAtual ?: return
        val pontas = falarPontasJuiz(jogo.tabuleiro)
        anunciarImediato("Pontas: $pontas", pontas, interromper = true)
    }

    private fun acaoJogarPedraSelecionada() {
        val jogo = jogoAtual ?: return
        if (!aguardandoJogadaUsuario) return
        val mao = jogo.maos[0] ?: return
        if (mao.isEmpty()) {
            audioVoz.tocarErro()
            anunciarImediato("Sem pedras.", interromper = true)
            return
        }

        val idxSeguro = jogo.indiceSelecionado.coerceIn(0, mao.lastIndex)
        jogo.indiceSelecionado = idxSeguro
        val pedra = mao[idxSeguro]
        val (esquerda, direita) = extremidades(jogo.tabuleiro)

        if (esquerda == null || direita == null) {
            val pEsperada = jogo.pedraInicial
            if (pEsperada != null && pedra != pEsperada && Pedra(pedra.b, pedra.a) != pEsperada) {
                audioVoz.tocarErro()
                val nomePedra = if (pEsperada.ehCarroca) "a maior carroça (${pEsperada.formatar()})" else "a maior pedra (${pEsperada.formatar()})"
                val falaPedra = if (pEsperada.ehCarroca) "a carroça de ${pEsperada.a}" else "a maior pedra ${pEsperada.falarJuiz()}"
                anunciarImediato(
                    "Regra de início: você deve jogar $nomePedra.",
                    "Regra de início: você deve jogar $falaPedra.",
                    interromper = true
                )
                return
            }
            executarJogadaConfirmadaUsuario(pedra, null)
            return
        }

        val encaixaEsquerda = pedraEncaixa(pedra, esquerda)
        val encaixaDireita = pedraEncaixa(pedra, direita)

        if (!encaixaEsquerda && !encaixaDireita) {
            audioVoz.tocarErro()
            anunciarImediato(
                "A pedra ${pedra.formatar()} não encaixa.",
                "A pedra ${pedra.falarJuiz()} não encaixa.",
                interromper = true
            )
            return
        }

        if (encaixaEsquerda && encaixaDireita && esquerda != direita) {
            pedraPendenteEscolhaLado = pedra
            anunciarImediato(
                "Escolha o lado para jogar ${pedra.formatar()}: Esquerda ($esquerda), Direita ($direita) ou Cancelar.",
                "Escolha o lado para jogar ${pedra.falarJuiz()}: Esquerda, Direita ou Cancelar.",
                interromper = true
            )
            return
        }

        val ladoUnico = if (encaixaEsquerda) "ESQUERDA" else "DIREITA"
        executarJogadaConfirmadaUsuario(pedra, ladoUnico)
    }

    private fun executarJogadaConfirmadaUsuario(pedra: Pedra, ladoForcado: String?) {
        val jogo = jogoAtual ?: return
        pedraPendenteEscolhaLado = null
        aguardandoJogadaUsuario = false

        jobPartida?.cancel()
        jobPartida = lifecycleScope.launch {
            aplicarJogadaNaMesa(jogo, pedra, 0, ladoForcado)
            jogo.jogadorAtual = (jogo.jogadorAtual + 1) % jogo.numJogadores
            versaoEstado++
            continuarLoopPartida()
        }
    }

    private fun acaoComprarDoMonte() {
        val jogo = jogoAtual ?: return
        if (!aguardandoJogadaUsuario) return
        val mao = jogo.maos[0] ?: return

        if (temJogadaValida(mao, jogo.tabuleiro)) {
            audioVoz.tocarErro()
            anunciarImediato("Você tem jogada válida na mão.", interromper = true)
            return
        }

        if ((jogo.modo == 1 || jogo.modo == 3 || jogo.modo == 4) && jogo.monte.isNotEmpty()) {
            val pedraComprada = jogo.pescarDoMonte(0) ?: return
            jogo.indiceSelecionado = mao.lastIndex
            versaoEstado++
            audioVoz.tocarEmbaralharDomino()

            jobPartida?.cancel()
            jobPartida = lifecycleScope.launch {
                anunciarPausado(
                    "Comprou ${pedraComprada.formatar()} (${pedraComprada.falarJuiz()}) do monte.",
                    "Comprou ${pedraComprada.falarJuiz()}. Pedra selecionada: ${pedraComprada.falarJuiz()}",
                    interromper = true
                )
                if (temJogadaValida(mao, jogo.tabuleiro)) {
                    anunciarPausado("Agora você tem jogada.", interromper = false)
                } else if (jogo.monte.isEmpty()) {
                    anunciarPausado(
                        "Ainda sem jogada e o monte acabou. O botão Passar a Jogada foi habilitado.",
                        interromper = false
                    )
                } else {
                    anunciarPausado(
                        "Ainda sem jogada. Restam ${jogo.monte.size} pedra(s) no monte.",
                        interromper = false
                    )
                }
            }
        } else if (jogo.modo == 1 || jogo.modo == 3) {
            audioVoz.tocarErro()
            anunciarImediato("Monte vazio. Use o botão Passar a Jogada.", interromper = true)
        } else {
            audioVoz.tocarErro()
            anunciarImediato("Não há monte neste modo de jogo.", interromper = true)
        }
    }

    private fun acaoPassarVez() {
        val jogo = jogoAtual ?: return
        if (!aguardandoJogadaUsuario) return
        val mao = jogo.maos[0] ?: return

        if (jogo.monte.isNotEmpty()) {
            audioVoz.tocarErro()
            anunciarImediato("Ainda há pedras no monte. Compre do monte antes de passar a jogada.", interromper = true)
            return
        }

        if (temJogadaValida(mao, jogo.tabuleiro)) {
            audioVoz.tocarErro()
            anunciarImediato("Você tem jogada válida na mão.", interromper = true)
            return
        }

        if (jogo.tabuleiro.isNotEmpty()) {
            val (esq, dir) = extremidades(jogo.tabuleiro)
            if (esq != null) jogo.passagensJogadores[0]?.add(esq)
            if (dir != null) jogo.passagensJogadores[0]?.add(dir)
        }

        aguardandoJogadaUsuario = false
        audioVoz.tocarPassar()
        if (jogo.modo == 4) {
            gerenciadorBt.enviarComando("DOM_PASSAR")
        }

        jobPartida?.cancel()
        jobPartida = lifecycleScope.launch {
            anunciarPausado("Você passou a jogada.", interromper = true)
            if (jogo.advPrincipal == "Luna") {
                val provocacao = obterProvocacaoLunaPassa(nomeUsuario)
                anunciarPausado(
                    "Luna diz: \"$provocacao\"",
                    "Luna diz: $provocacao",
                    interromper = false
                )
            }
            jogo.jogadorAtual = (jogo.jogadorAtual + 1) % jogo.numJogadores
            versaoEstado++
            continuarLoopPartida()
        }
    }

    // --- Lógica e Ações da Partida de Truco Paulista contra a Rede Neural da Luna ---

    private fun iniciarNovaPartidaTruco() {
        jobPartida?.cancel()
        aguardandoJogadaTrucoUsuario = false
        historicoMensagens.clear()

        val novaPartida = PartidaTruco(nomeJogador = nomeUsuario, nomeAdversario = "Luna")
        partidaTrucoAtual = novaPartida
        telaAtual = TelaJogo.PARTIDA_TRUCO
        versaoEstado++

        jobPartida = lifecycleScope.launch {
            anunciarPausado(
                "Truco Paulista iniciado! $nomeUsuario versus Luna (Rede Neural MLP e Monte Carlo). Partida até 12 tentos.",
                interromper = true
            )
            executarNovaMaoTruco()
        }
    }

    private suspend fun executarNovaMaoTruco() {
        val partida = partidaTrucoAtual ?: return

        if (partida.pontosJogador >= partida.metaPontos || partida.pontosLuna >= partida.metaPontos) {
            val venceu = partida.pontosJogador >= partida.metaPontos
            if (venceu) {
                val banco = BancoDadosJogosLuna.obterInstancia(this@MainActivity)
                banco.salvarVitoria("TRUCO", nomeUsuario, "TRUCO_PAULISTA", "QUEDA", "Luna")
                vitoriasTrucoJogador = banco.obterTotalVitorias("TRUCO", nomeUsuario)
                audioVoz.tocarVitoria()
                textoResultadoPartida = "Parabéns! Você venceu a Luna no Truco por ${partida.pontosJogador} a ${partida.pontosLuna}! Total acumulado: $vitoriasTrucoJogador vitórias salvas."
            } else {
                audioVoz.tocarDerrota()
                textoResultadoPartida = "A Rede Neural da Luna venceu a partida de Truco por ${partida.pontosLuna} a ${partida.pontosJogador}."
            }
            aguardandoJogadaTrucoUsuario = false
            anunciarPausado(textoResultadoPartida, textoResultadoPartida, interromper = true)
            telaAtual = TelaJogo.FIM_PARTIDA_TRUCO
            audioVoz.falar("Deseja jogar outra partida de Truco contra a Luna ou voltar ao menu?", interromper = false)
            return
        }

        partida.iniciarNovaMao()
        versaoEstado++

        val nomeManilha = NOMES_VALORES_TRUCO[partida.valorManilha] ?: partida.valorManilha
        val viraFala = partida.vira?.falarCarta() ?: ""
        anunciarPausado(
            "Nova mão! Placar: $nomeUsuario ${partida.pontosJogador} a ${partida.pontosLuna} Luna. Vira: $viraFala. Manilha: $nomeManilha.",
            interromper = false
        )

        if (partida.ehMaoDeFerro) {
            anunciarPausado(
                "Mão de Ferro! 11 a 11! Cartas no escuro valendo a partida!",
                interromper = false
            )
        } else if (partida.ehMaoDe11) {
            if (partida.pontosJogador == 11) {
                val descricaoCartas = partida.maoJogador.joinToString(", ") { it.falarCarta(partida.valorManilha) }
                partida.aguardandoDecisaoMao11Usuario = true
                aguardandoJogadaTrucoUsuario = false
                versaoEstado++
                anunciarPausado(
                    "Mão de 11 para você! Suas cartas: $descricaoCartas. Deseja jogar valendo 3 tentos ou correr e dar 1 tento para Luna?",
                    interromper = false
                )
                return
            } else if (partida.pontosLuna == 11) {
                val lunaAceita = withContext(Dispatchers.Default) {
                    redeNeuralTruco.decidirMaoDe11(partida)
                }
                if (lunaAceita) {
                    partida.valorMao = 3
                    anunciarPausado(
                        "Mão de 11 da Luna! A rede neural analisou as cartas e decidiu jogar valendo 3 tentos!",
                        interromper = false
                    )
                } else {
                    partida.pontosJogador += 1
                    versaoEstado++
                    anunciarPausado(
                        "Mão de 11 da Luna! A rede neural calculou baixo retorno e correu. Você ganhou 1 tento!",
                        interromper = false
                    )
                    executarNovaMaoTruco()
                    return
                }
            }
        }

        continuarFluxoQuedaTruco()
    }

    private suspend fun continuarFluxoQuedaTruco() {
        val partida = partidaTrucoAtual ?: return

        while (true) {
            val cJog = partida.cartaMesaJogador
            val cLuna = partida.cartaMesaLuna

            if (cJog != null && cLuna != null) {
                aguardandoJogadaTrucoUsuario = false
                resolverQuedaTruco(partida, cJog, cLuna)

                val vencedorMao = partida.verificarVencedorDaMao()
                if (vencedorMao != null) {
                    finalizarMaoTruco(partida, vencedorMao)
                    return
                }

                partida.quedaAtual += 1
                partida.cartaMesaJogador = null
                partida.cartaMesaLuna = null
                partida.jogarProximaCoberta = false
                partida.vezAtual = partida.quemIniciaQueda
                versaoEstado++
                continue
            }

            if (partida.vezAtual == 0) {
                if (partida.maoJogador.isNotEmpty()) {
                    partida.indiceCartaSelecionada = partida.indiceCartaSelecionada.coerceIn(0, partida.maoJogador.lastIndex)
                }
                val cartaSel = partida.maoJogador.getOrNull(partida.indiceCartaSelecionada)
                val falaCarta = if (partida.ehMaoDeFerro) {
                    "Carta no escuro ${partida.indiceCartaSelecionada + 1} de ${partida.maoJogador.size}"
                } else {
                    cartaSel?.falarCarta(partida.valorManilha) ?: "nenhuma"
                }
                val infoMesa = partida.cartaMesaLuna?.let {
                    ". Luna jogou ${it.falarCarta(partida.valorManilha)}"
                } ?: ""
                val msg = "Sua vez (${partida.quedaAtual}ª queda)$infoMesa. Carta selecionada: $falaCarta"
                anunciarPausado(msg, msg, interromper = false, pausaFixaMs = 900L)
                aguardandoJogadaTrucoUsuario = true
                versaoEstado++
                return
            } else {
                aguardandoJogadaTrucoUsuario = false
                val pedirTruco = withContext(Dispatchers.Default) {
                    redeNeuralTruco.devePedirTrucoAntesDeJogar(partida)
                }
                if (pedirTruco) {
                    val (novoValor, nomeGrito) = partida.proximoValorTruco()
                    partida.valorProposto = novoValor
                    partida.ultimoPediuTruco = 1
                    partida.aguardandoRespostaTrucoUsuario = true
                    versaoEstado++
                    val provocacao = obterProvocacaoLunaTrucoPede(nomeUsuario)
                    anunciarPausado(
                        "Luna pediu $nomeGrito (valendo $novoValor tentos)! \"$provocacao\" Escolha: Aceitar, Correr ou Aumentar.",
                        "Luna pediu $nomeGrito valendo $novoValor tentos! $provocacao. Escolha: Aceitar, Correr ou Aumentar.",
                        interromper = true
                    )
                    return
                }

                executarJogadaCartaLuna(partida)
                if (partida.cartaMesaJogador == null) {
                    partida.vezAtual = 0
                }
                versaoEstado++
            }
        }
    }

    private suspend fun executarJogadaCartaLuna(partida: PartidaTruco) {
        if (partida.maoLuna.isEmpty()) return
        val (idxEscolhido, jogarCoberta) = withContext(Dispatchers.Default) {
            redeNeuralTruco.escolherCartaParaJogar(partida)
        }
        val idxSeguro = idxEscolhido.coerceIn(0, partida.maoLuna.lastIndex)
        val cartaOriginal = partida.maoLuna.removeAt(idxSeguro)
        val cobertaFinal = jogarCoberta && partida.quedaAtual >= 2
        val cartaJogada = cartaOriginal.copy(coberta = cobertaFinal)
        partida.cartaMesaLuna = cartaJogada
        if (!cobertaFinal) {
            partida.cartasJogadasHistorico.add(cartaOriginal)
        }
        audioVoz.tocarConfirmar()
        versaoEstado++
        anunciarPausado(
            "Luna jogou ${cartaJogada.formatarCurto(partida.valorManilha)} (${cartaJogada.falarCarta(partida.valorManilha)})",
            "Luna jogou ${cartaJogada.falarCarta(partida.valorManilha)}",
            interromper = false
        )
    }

    private suspend fun resolverQuedaTruco(
        partida: PartidaTruco,
        cartaJog: CartaTruco,
        cartaLuna: CartaTruco
    ) {
        val fJog = forcaCartaTruco(cartaJog, partida.valorManilha)
        val fLuna = forcaCartaTruco(cartaLuna, partida.valorManilha)
        val numQueda = partida.quedaAtual

        when {
            fJog > fLuna -> {
                partida.vencedoresQuedas.add(0)
                partida.resumoQuedas.add("${numQueda}ª: $nomeUsuario")
                partida.quemIniciaQueda = 0
                versaoEstado++
                anunciarPausado(
                    "Você venceu a ${numQueda}ª queda com ${cartaJog.falarCarta(partida.valorManilha)}!",
                    interromper = false
                )
            }
            fLuna > fJog -> {
                partida.vencedoresQuedas.add(1)
                partida.resumoQuedas.add("${numQueda}ª: Luna")
                partida.quemIniciaQueda = 1
                versaoEstado++
                anunciarPausado(
                    "Luna venceu a ${numQueda}ª queda com ${cartaLuna.falarCarta(partida.valorManilha)}!",
                    interromper = false
                )
                if (Random.nextDouble() < 0.55) {
                    val prov = obterProvocacaoLunaTrucoVenceQueda(nomeUsuario)
                    anunciarPausado("Luna diz: \"$prov\"", "Luna diz: $prov", interromper = false)
                }
            }
            else -> {
                partida.vencedoresQuedas.add(-1)
                partida.resumoQuedas.add("${numQueda}ª: Empate")
                versaoEstado++
                anunciarPausado(
                    "${numQueda}ª queda empatou (amarrou)!",
                    interromper = false
                )
            }
        }
    }

    private suspend fun finalizarMaoTruco(partida: PartidaTruco, vencedorMao: Int) {
        val tentos = partida.valorMao
        when (vencedorMao) {
            0 -> {
                partida.pontosJogador = (partida.pontosJogador + tentos).coerceAtMost(partida.metaPontos)
                redeNeuralTruco.atualizarPesosFimDeMao(-tentos.toDouble())
                audioVoz.tocarVitoria()
                versaoEstado++
                anunciarPausado(
                    "Você venceu a mão e ganhou $tentos tento(s)! Placar: $nomeUsuario ${partida.pontosJogador} a ${partida.pontosLuna} Luna.",
                    interromper = false
                )
            }
            1 -> {
                partida.pontosLuna = (partida.pontosLuna + tentos).coerceAtMost(partida.metaPontos)
                redeNeuralTruco.atualizarPesosFimDeMao(tentos.toDouble())
                audioVoz.tocarDerrota()
                versaoEstado++
                anunciarPausado(
                    "Luna venceu a mão e ganhou $tentos tento(s)! Placar: Luna ${partida.pontosLuna} a ${partida.pontosJogador} $nomeUsuario.",
                    interromper = false
                )
            }
            else -> {
                versaoEstado++
                anunciarPausado(
                    "As três quedas empataram! Ninguém marcou ponto nesta mão.",
                    interromper = false
                )
            }
        }
        executarNovaMaoTruco()
    }

    private fun acaoCartaAnteriorTruco() {
        val partida = partidaTrucoAtual ?: return
        if (!aguardandoJogadaTrucoUsuario) return
        val mao = partida.maoJogador
        if (mao.isEmpty()) return

        partida.indiceCartaSelecionada = (partida.indiceCartaSelecionada - 1 + mao.size) % mao.size
        audioVoz.tocarNavegar()
        val carta = mao[partida.indiceCartaSelecionada]
        versaoEstado++
        val fala = if (partida.ehMaoDeFerro) {
            "Carta no escuro ${partida.indiceCartaSelecionada + 1} de ${mao.size}"
        } else {
            carta.falarCarta(partida.valorManilha)
        }
        anunciarImediato(
            "Selecionada: $fala",
            "Carta selecionada: $fala",
            interromper = true
        )
    }

    private fun acaoProximaCartaTruco() {
        val partida = partidaTrucoAtual ?: return
        if (!aguardandoJogadaTrucoUsuario) return
        val mao = partida.maoJogador
        if (mao.isEmpty()) return

        partida.indiceCartaSelecionada = (partida.indiceCartaSelecionada + 1) % mao.size
        audioVoz.tocarNavegar()
        val carta = mao[partida.indiceCartaSelecionada]
        versaoEstado++
        val fala = if (partida.ehMaoDeFerro) {
            "Carta no escuro ${partida.indiceCartaSelecionada + 1} de ${mao.size}"
        } else {
            carta.falarCarta(partida.valorManilha)
        }
        anunciarImediato(
            "Selecionada: $fala",
            "Carta selecionada: $fala",
            interromper = true
        )
    }

    private fun acaoSelecionarCartaDiretoTruco(indice: Int) {
        val partida = partidaTrucoAtual ?: return
        if (!aguardandoJogadaTrucoUsuario) return
        val mao = partida.maoJogador
        if (indice !in mao.indices) return

        partida.indiceCartaSelecionada = indice
        audioVoz.tocarNavegar()
        val carta = mao[indice]
        versaoEstado++
        val fala = if (partida.ehMaoDeFerro) {
            "Carta no escuro ${indice + 1} de ${mao.size}"
        } else {
            carta.falarCarta(partida.valorManilha)
        }
        anunciarImediato(
            "Selecionada: $fala",
            "Carta selecionada: $fala",
            interromper = true
        )
    }

    private fun acaoJogarCartaDiretoTruco(indice: Int) {
        val partida = partidaTrucoAtual ?: return
        if (!aguardandoJogadaTrucoUsuario) {
            audioVoz.tocarErro()
            anunciarImediato("Aguarde a sua vez de jogar.", interromper = true)
            return
        }
        val mao = partida.maoJogador
        if (indice !in mao.indices) return

        partida.indiceCartaSelecionada = indice
        acaoJogarCartaSelecionadaTruco()
    }

    private fun acaoAlternarCartaCobertaTruco() {
        val partida = partidaTrucoAtual ?: return
        if (!aguardandoJogadaTrucoUsuario) return
        if (partida.quedaAtual < 2) {
            audioVoz.tocarErro()
            anunciarImediato("Na primeira queda não é permitido jogar carta coberta.", interromper = true)
            return
        }
        partida.jogarProximaCoberta = !partida.jogarProximaCoberta
        versaoEstado++
        val estado = if (partida.jogarProximaCoberta) "ATIVADO (a carta será jogada virada para baixo)" else "DESATIVADO (a carta será jogada aberta)"
        anunciarImediato("Modo carta coberta: $estado", interromper = true)
    }

    private fun acaoJogarCartaSelecionadaTruco() {
        val partida = partidaTrucoAtual ?: return
        if (!aguardandoJogadaTrucoUsuario) return
        val mao = partida.maoJogador
        if (mao.isEmpty()) return

        val idxSeguro = partida.indiceCartaSelecionada.coerceIn(0, mao.lastIndex)
        val cartaOriginal = mao.removeAt(idxSeguro)
        val cobertaFinal = partida.jogarProximaCoberta && partida.quedaAtual >= 2
        val cartaJogada = cartaOriginal.copy(coberta = cobertaFinal)
        partida.jogarProximaCoberta = false
        partida.cartaMesaJogador = cartaJogada
        if (!cobertaFinal) {
            partida.cartasJogadasHistorico.add(cartaOriginal)
        }
        if (mao.isNotEmpty()) {
            partida.indiceCartaSelecionada = partida.indiceCartaSelecionada.coerceIn(0, mao.lastIndex)
        } else {
            partida.indiceCartaSelecionada = 0
        }

        aguardandoJogadaTrucoUsuario = false
        audioVoz.tocarConfirmar()
        versaoEstado++

        jobPartida?.cancel()
        jobPartida = lifecycleScope.launch {
            anunciarPausado(
                "Você jogou ${cartaJogada.formatarCurto(partida.valorManilha)} (${cartaJogada.falarCarta(partida.valorManilha)})",
                "Você jogou ${cartaJogada.falarCarta(partida.valorManilha)}",
                interromper = true
            )
            if (partida.cartaMesaLuna == null) {
                partida.vezAtual = 1
            }
            continuarFluxoQuedaTruco()
        }
    }

    private fun acaoPedirTrucoUsuario() {
        val partida = partidaTrucoAtual ?: return
        if (!aguardandoJogadaTrucoUsuario || !partida.podeJogadorPedirTruco()) {
            audioVoz.tocarErro()
            anunciarImediato("Não é possível pedir truco neste momento.", interromper = true)
            return
        }

        aguardandoJogadaTrucoUsuario = false
        val (valorAlvo, nomeGrito) = partida.proximoValorTruco()
        partida.valorProposto = valorAlvo
        partida.ultimoPediuTruco = 0
        redeNeuralTruco.taxaBlefeEstimadaJogador = (redeNeuralTruco.taxaBlefeEstimadaJogador * 0.85 + 0.15 * 0.45).coerceIn(0.1, 0.7)
        versaoEstado++

        jobPartida?.cancel()
        jobPartida = lifecycleScope.launch {
            anunciarPausado(
                "Você pediu $nomeGrito (valendo $valorAlvo tentos)!",
                "Você pediu $nomeGrito valendo $valorAlvo tentos!",
                interromper = true
            )
            val decisaoLuna = withContext(Dispatchers.Default) {
                redeNeuralTruco.decidirRespostaTruco(partida)
            }
            when (decisaoLuna) {
                DecisaoApostaLuna.CORRER -> {
                    val frase = PROVOCACOES_LUNA_TRUCO_CORRE.random()
                    anunciarPausado("Luna correu! \"$frase\"", "Luna correu! $frase", interromper = false)
                    finalizarMaoTruco(partida, vencedorMao = 0)
                }
                DecisaoApostaLuna.ACEITAR -> {
                    partida.valorMao = valorAlvo
                    partida.valorProposto = 0
                    versaoEstado++
                    val frase = PROVOCACOES_LUNA_TRUCO_ACEITA.random()
                    anunciarPausado(
                        "Luna aceitou! A mão agora vale ${partida.valorMao} tentos! \"$frase\"",
                        "Luna aceitou! A mão agora vale ${partida.valorMao} tentos! $frase",
                        interromper = false
                    )
                    aguardandoJogadaTrucoUsuario = true
                    versaoEstado++
                }
                DecisaoApostaLuna.AUMENTAR -> {
                    partida.valorMao = valorAlvo
                    val (novoRebate, nomeRebate) = partida.proximoValorRebate()
                    partida.valorProposto = novoRebate
                    partida.ultimoPediuTruco = 1
                    partida.aguardandoRespostaTrucoUsuario = true
                    versaoEstado++
                    val frase = obterProvocacaoLunaTrucoPede(nomeUsuario)
                    anunciarPausado(
                        "Luna rebateu pedindo $nomeRebate (valendo $novoRebate tentos)! \"$frase\" Escolha: Aceitar, Correr ou Aumentar.",
                        "Luna rebateu pedindo $nomeRebate valendo $novoRebate tentos! $frase. Escolha: Aceitar, Correr ou Aumentar.",
                        interromper = false
                    )
                }
            }
        }
    }

    private fun acaoResponderTrucoUsuario(decisao: DecisaoApostaLuna) {
        val partida = partidaTrucoAtual ?: return
        if (!partida.aguardandoRespostaTrucoUsuario) return

        partida.aguardandoRespostaTrucoUsuario = false
        versaoEstado++

        jobPartida?.cancel()
        jobPartida = lifecycleScope.launch {
            when (decisao) {
                DecisaoApostaLuna.CORRER -> {
                    audioVoz.tocarPassar()
                    anunciarPausado(
                        "Você correu! Luna ganha ${partida.valorMao} tento(s).",
                        interromper = true
                    )
                    finalizarMaoTruco(partida, vencedorMao = 1)
                }
                DecisaoApostaLuna.ACEITAR -> {
                    partida.valorMao = partida.valorProposto
                    partida.valorProposto = 0
                    versaoEstado++
                    anunciarPausado(
                        "Você aceitou! A mão agora vale ${partida.valorMao} tentos!",
                        interromper = true
                    )
                    if (partida.vezAtual == 1 && partida.cartaMesaLuna == null) {
                        executarJogadaCartaLuna(partida)
                        if (partida.cartaMesaJogador == null) {
                            partida.vezAtual = 0
                        }
                    }
                    continuarFluxoQuedaTruco()
                }
                DecisaoApostaLuna.AUMENTAR -> {
                    partida.valorMao = partida.valorProposto
                    val (novoValor, nomeGrito) = partida.proximoValorRebate()
                    partida.valorProposto = novoValor
                    partida.ultimoPediuTruco = 0
                    versaoEstado++
                    anunciarPausado(
                        "Você aumentou pedindo $nomeGrito (valendo $novoValor tentos)!",
                        interromper = true
                    )
                    val respostaLuna = withContext(Dispatchers.Default) {
                        redeNeuralTruco.decidirRespostaTruco(partida)
                    }
                    if (respostaLuna == DecisaoApostaLuna.CORRER) {
                        val frase = PROVOCACOES_LUNA_TRUCO_CORRE.random()
                        anunciarPausado("Luna correu do seu $nomeGrito! \"$frase\"", interromper = false)
                        finalizarMaoTruco(partida, vencedorMao = 0)
                    } else {
                        partida.valorMao = novoValor
                        partida.valorProposto = 0
                        versaoEstado++
                        val frase = PROVOCACOES_LUNA_TRUCO_ACEITA.random()
                        anunciarPausado(
                            "Luna aceitou $nomeGrito! A mão agora vale ${partida.valorMao} tentos! \"$frase\"",
                            interromper = false
                        )
                        if (partida.vezAtual == 1 && partida.cartaMesaLuna == null) {
                            executarJogadaCartaLuna(partida)
                            if (partida.cartaMesaJogador == null) {
                                partida.vezAtual = 0
                            }
                        }
                        continuarFluxoQuedaTruco()
                    }
                }
            }
        }
    }

    private fun acaoResponderMao11Usuario(aceitar: Boolean) {
        val partida = partidaTrucoAtual ?: return
        if (!partida.aguardandoDecisaoMao11Usuario) return

        partida.aguardandoDecisaoMao11Usuario = false
        versaoEstado++

        jobPartida?.cancel()
        jobPartida = lifecycleScope.launch {
            if (aceitar) {
                partida.valorMao = 3
                anunciarPausado("Você aceitou a Mão de 11! Mão valendo 3 tentos!", interromper = true)
                continuarFluxoQuedaTruco()
            } else {
                partida.pontosLuna += 1
                versaoEstado++
                anunciarPausado("Você correu da Mão de 11. Luna ganhou 1 tento.", interromper = true)
                executarNovaMaoTruco()
            }
        }
    }

    private fun acaoOuvirMesaTruco() {
        val partida = partidaTrucoAtual ?: return
        val nomeManilha = NOMES_VALORES_TRUCO[partida.valorManilha] ?: partida.valorManilha
        val viraTxt = partida.vira?.falarCarta() ?: "nenhuma"
        val cLunaTxt = partida.cartaMesaLuna?.falarCarta(partida.valorManilha) ?: "ainda não jogou"
        val cJogTxt = partida.cartaMesaJogador?.falarCarta(partida.valorManilha) ?: "ainda não jogou"
        val resumo = "Placar: Você ${partida.pontosJogador}, Luna ${partida.pontosLuna}. " +
            "${partida.quedaAtual}ª queda, mão valendo ${partida.valorMao} tento(s). " +
            "Vira: $viraTxt, Manilha: $nomeManilha. " +
            "Na mesa: Luna $cLunaTxt; Você $cJogTxt."
        anunciarImediato(resumo, resumo, interromper = true)
    }

    // --- Suporte a Teclado Físico / Bluetooth (igual ao PC) ---

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        when (telaAtual) {
            TelaJogo.INFORMAR_NOME -> {
                if (keyCode == KeyEvent.KEYCODE_ESCAPE) {
                    salvarNomeJogador(rascunhoNome)
                    return true
                }
            }
            TelaJogo.SELECAO_JOGO -> {
                when (keyCode) {
                    KeyEvent.KEYCODE_DPAD_LEFT -> { navegarSelecaoJogoAnterior(); return true }
                    KeyEvent.KEYCODE_DPAD_RIGHT -> { navegarSelecaoJogoProximo(); return true }
                    KeyEvent.KEYCODE_SPACE, KeyEvent.KEYCODE_ENTER -> { confirmarSelecaoJogo(); return true }
                    KeyEvent.KEYCODE_1 -> { confirmarSelecaoJogo(1); return true }
                    KeyEvent.KEYCODE_2 -> { confirmarSelecaoJogo(2); return true }
                    KeyEvent.KEYCODE_3 -> { confirmarSelecaoJogo(3); return true }
                    KeyEvent.KEYCODE_4 -> { confirmarSelecaoJogo(4); return true }
                    KeyEvent.KEYCODE_5 -> { confirmarSelecaoJogo(5); return true }
                    KeyEvent.KEYCODE_6 -> { confirmarSelecaoJogo(6); return true }
                    KeyEvent.KEYCODE_7 -> { confirmarSelecaoJogo(7); return true }
                    KeyEvent.KEYCODE_8 -> { confirmarSelecaoJogo(8); return true }
                }
            }
            TelaJogo.YUGIOH_LUNA -> {
                if (estadoYugiohLuna.tratarTecla(
                    keyCode = keyCode,
                    scope = lifecycleScope,
                    audioVoz = audioVoz,
                    nomeJogador = nomeUsuario,
                    aoVoltarMenuLuna = {
                        estadoYugiohLuna.jobTurnoIa?.cancel()
                        telaAtual = TelaJogo.SELECAO_JOGO
                        anunciarSelecaoJogo()
                    }
                )) {
                    return true
                }
            }
            TelaJogo.ACADEMIA_PYTHON -> {
                if (estadoAcademiaPython.tratarTecla(
                    keyCode = keyCode,
                    audioVoz = audioVoz,
                    motorJogo = motorJogoPython,
                    aoSairParaMenuPrincipal = {
                        telaAtual = TelaJogo.SELECAO_JOGO
                        anunciarSelecaoJogo()
                    }
                )) {
                    return true
                }
            }
            TelaJogo.MENU_PRINCIPAL -> {
                when (keyCode) {
                    KeyEvent.KEYCODE_DPAD_LEFT -> { navegarMenuAnterior(); return true }
                    KeyEvent.KEYCODE_DPAD_RIGHT -> { navegarMenuProximo(); return true }
                    KeyEvent.KEYCODE_SPACE, KeyEvent.KEYCODE_ENTER -> { confirmarOpcaoMenu(); return true }
                    KeyEvent.KEYCODE_1 -> { confirmarOpcaoMenu(1); return true }
                    KeyEvent.KEYCODE_2 -> { confirmarOpcaoMenu(2); return true }
                    KeyEvent.KEYCODE_3 -> { confirmarOpcaoMenu(3); return true }
                    KeyEvent.KEYCODE_4 -> { confirmarOpcaoMenu(4); return true }
                    KeyEvent.KEYCODE_5 -> { confirmarOpcaoMenu(5); return true }
                    KeyEvent.KEYCODE_6, KeyEvent.KEYCODE_ESCAPE -> { confirmarOpcaoMenu(6); return true }
                }
            }
            TelaJogo.ESCOLHA_PARCEIRO -> {
                when (keyCode) {
                    KeyEvent.KEYCODE_DPAD_LEFT -> { navegarParceiroAnterior(); return true }
                    KeyEvent.KEYCODE_DPAD_RIGHT -> { navegarParceiroProximo(); return true }
                    KeyEvent.KEYCODE_SPACE, KeyEvent.KEYCODE_ENTER -> { confirmarParceiro(); return true }
                    KeyEvent.KEYCODE_ESCAPE -> {
                        telaAtual = TelaJogo.MENU_PRINCIPAL
                        anunciarMenuPrincipal()
                        return true
                    }
                }
            }
            TelaJogo.AJUDA -> {
                if (keyCode == KeyEvent.KEYCODE_ESCAPE || keyCode == KeyEvent.KEYCODE_SPACE || keyCode == KeyEvent.KEYCODE_ENTER) {
                    telaAtual = TelaJogo.SELECAO_JOGO
                    anunciarSelecaoJogo()
                    return true
                }
            }
            TelaJogo.PARTIDA -> {
                val pendente = pedraPendenteEscolhaLado
                if (pendente != null) {
                    when (keyCode) {
                        KeyEvent.KEYCODE_DPAD_LEFT -> { executarJogadaConfirmadaUsuario(pendente, "ESQUERDA"); return true }
                        KeyEvent.KEYCODE_DPAD_RIGHT -> { executarJogadaConfirmadaUsuario(pendente, "DIREITA"); return true }
                        KeyEvent.KEYCODE_ESCAPE -> {
                            pedraPendenteEscolhaLado = null
                            anunciarImediato("Cancelado.", interromper = true)
                            return true
                        }
                    }
                    return super.onKeyDown(keyCode, event)
                }

                when (keyCode) {
                    KeyEvent.KEYCODE_DPAD_LEFT -> { acaoPedraAnterior(); return true }
                    KeyEvent.KEYCODE_DPAD_RIGHT -> { acaoProximaPedra(); return true }
                    KeyEvent.KEYCODE_SPACE -> { acaoJogarPedraSelecionada(); return true }
                    KeyEvent.KEYCODE_ENTER, KeyEvent.KEYCODE_T, KeyEvent.KEYCODE_J -> { acaoOuvirPontas(); return true }
                    KeyEvent.KEYCODE_TAB, KeyEvent.KEYCODE_C -> { acaoComprarDoMonte(); return true }
                    KeyEvent.KEYCODE_DEL -> { acaoPassarVez(); return true }
                    KeyEvent.KEYCODE_ESCAPE -> {
                        jobPartida?.cancel()
                        telaAtual = TelaJogo.MENU_PRINCIPAL
                        anunciarMenuPrincipal()
                        return true
                    }
                }
            }
            TelaJogo.FIM_PARTIDA -> {
                when (keyCode) {
                    KeyEvent.KEYCODE_S -> { iniciarNovaPartida(); return true }
                    KeyEvent.KEYCODE_N, KeyEvent.KEYCODE_ESCAPE -> {
                        telaAtual = TelaJogo.MENU_PRINCIPAL
                        anunciarMenuPrincipal()
                        return true
                    }
                }
            }
            TelaJogo.PARTIDA_TRUCO -> {
                val partida = partidaTrucoAtual
                if (partida?.aguardandoDecisaoMao11Usuario == true) {
                    when (keyCode) {
                        KeyEvent.KEYCODE_S, KeyEvent.KEYCODE_SPACE, KeyEvent.KEYCODE_ENTER -> { acaoResponderMao11Usuario(true); return true }
                        KeyEvent.KEYCODE_N, KeyEvent.KEYCODE_DEL -> { acaoResponderMao11Usuario(false); return true }
                    }
                    return super.onKeyDown(keyCode, event)
                }
                if (partida?.aguardandoRespostaTrucoUsuario == true) {
                    when (keyCode) {
                        KeyEvent.KEYCODE_S, KeyEvent.KEYCODE_SPACE, KeyEvent.KEYCODE_ENTER -> { acaoResponderTrucoUsuario(DecisaoApostaLuna.ACEITAR); return true }
                        KeyEvent.KEYCODE_N, KeyEvent.KEYCODE_DEL -> { acaoResponderTrucoUsuario(DecisaoApostaLuna.CORRER); return true }
                        KeyEvent.KEYCODE_T -> {
                            if (partida.valorProposto < 12) {
                                acaoResponderTrucoUsuario(DecisaoApostaLuna.AUMENTAR)
                                return true
                            }
                        }
                    }
                    return super.onKeyDown(keyCode, event)
                }
                when (keyCode) {
                    KeyEvent.KEYCODE_DPAD_LEFT -> { acaoCartaAnteriorTruco(); return true }
                    KeyEvent.KEYCODE_DPAD_RIGHT -> { acaoProximaCartaTruco(); return true }
                    KeyEvent.KEYCODE_SPACE -> { acaoJogarCartaSelecionadaTruco(); return true }
                    KeyEvent.KEYCODE_T -> { acaoPedirTrucoUsuario(); return true }
                    KeyEvent.KEYCODE_C -> { acaoAlternarCartaCobertaTruco(); return true }
                    KeyEvent.KEYCODE_ENTER, KeyEvent.KEYCODE_J -> { acaoOuvirMesaTruco(); return true }
                    KeyEvent.KEYCODE_ESCAPE -> {
                        jobPartida?.cancel()
                        telaAtual = TelaJogo.SELECAO_JOGO
                        anunciarSelecaoJogo()
                        return true
                    }
                }
            }
            TelaJogo.FIM_PARTIDA_TRUCO -> {
                when (keyCode) {
                    KeyEvent.KEYCODE_S -> { iniciarNovaPartidaTruco(); return true }
                    KeyEvent.KEYCODE_N, KeyEvent.KEYCODE_ESCAPE -> {
                        telaAtual = TelaJogo.SELECAO_JOGO
                        anunciarSelecaoJogo()
                        return true
                    }
                }
            }
            TelaJogo.BLUETOOTH_SALA -> {
                if (keyCode == KeyEvent.KEYCODE_ESCAPE || keyCode == KeyEvent.KEYCODE_BACK) {
                    telaAtual = TelaJogo.SELECAO_JOGO
                    anunciarSelecaoJogo()
                    return true
                }
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    // --- Telas em Jetpack Compose ---

    @Composable
    private fun TelaInformarNome() {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "IDENTIFICAÇÃO DO JOGADOR",
                color = CorDestaque,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Informe o seu nome para jogar Dominó, Truco Paulista, Academia Python e Yu-Gi-Oh! Duelo Acessível:",
                color = CorTextoClaro,
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            )

            OutlinedTextField(
                value = rascunhoNome,
                onValueChange = { rascunhoNome = it },
                label = { Text("Digite o seu nome de jogador", color = CorDestaque) },
                placeholder = { Text("Ex: Edson", color = Color(0xFF94A3B8)) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = CorDestaque,
                    unfocusedBorderColor = Color(0xFF64748B),
                    cursorColor = CorDestaque
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics {
                        contentDescription = "Caixa de edição do nome do jogador. Valor atual: ${rascunhoNome.ifBlank { "vazio" }}. Toque duas vezes para digitar seu nome."
                    }
            )

            Button(
                onClick = { salvarNomeJogador(rascunhoNome) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF15803D),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .border(2.dp, CorDestaque, RoundedCornerShape(14.dp))
                    .semantics(mergeDescendants = true) {
                        val nomeFinal = rascunhoNome.trim().ifEmpty { "Edson" }
                        contentDescription = "Confirmar Nome do Jogador como $nomeFinal e entrar nos Jogos da Luna."
                        role = Role.Button
                    }
            ) {
                val nomeFinal = rascunhoNome.trim().ifEmpty { "Edson" }
                Text(
                    text = "✅ CONFIRMAR NOME: $nomeFinal",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Button(
                onClick = { rascunhoNome = "" },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF475569),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .semantics(mergeDescendants = true) {
                        contentDescription = "Limpar caixa de texto para digitar outro nome"
                        role = Role.Button
                    }
            ) {
                Text("Limpar Caixa de Texto", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }

    @Composable
    private fun TelaSelecaoJogo() {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "JOGOS DA LUNA",
                color = CorDestaque,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Jogador: $nomeUsuario  |  Dominó: $vitoriasJogador  |  Truco: $vitoriasTrucoJogador  |  Yu-Gi-Oh!: ${estadoYugiohLuna.vitoriasYugioh} (🏆 ${estadoYugiohLuna.torneiosVencidosYugioh})",
                color = CorTextoClaro,
                fontSize = 15.sp,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Toque duas vezes no jogo para entrar diretamente:",
                color = CorDestaque,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )

            opcoesSelecaoJogo.forEachIndexed { idx, opcao ->
                val corBotao = when (opcao.id) {
                    1 -> Color(0xFF1E3A8A) // Azul dominó
                    2 -> Color(0xFF15803D) // Verde truco
                    3 -> Color(0xFF6D28D9) // Roxo python
                    4 -> Color(0xFFB45309) // Dourado escuro Yu-Gi-Oh!
                    5 -> Color(0xFF0F766E) // Turquesa informar/alterar nome
                    6 -> Color(0xFF0369A1) // Azul ajuda
                    8 -> Color(0xFF0284C7) // Azul bluetooth
                    else -> Color(0xFF991B1B) // Vermelho sair
                }
                Button(
                    onClick = {
                        indiceSelecaoJogo = idx
                        confirmarSelecaoJogo(opcao.id)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = corBotao,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .border(1.5.dp, CorDestaque.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                        .semantics(mergeDescendants = true) {
                            contentDescription = "${opcao.titulo}. Toque duas vezes para entrar diretamente."
                            role = Role.Button
                            onClick(label = "Entrar no jogo ${opcao.titulo}") {
                                indiceSelecaoJogo = idx
                                confirmarSelecaoJogo(opcao.id)
                                true
                            }
                        }
                ) {
                    Text(
                        text = opcao.titulo,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }

    @Composable
    private fun TelaMenuPrincipal() {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "DOMINÓ ACESSÍVEL",
                color = CorDestaque,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Jogador: $nomeUsuario | Vitórias: $vitoriasJogador",
                color = CorTextoClaro,
                fontSize = 16.sp
            )

            Text(
                text = "Toque duas vezes na opção para jogar diretamente:",
                color = CorDestaque,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )

            opcoesMenuPrincipal.forEachIndexed { idx, opcao ->
                Button(
                    onClick = {
                        indiceMenu = idx
                        confirmarOpcaoMenu(opcao.id)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (opcao.id == 4) Color(0xFF991B1B) else CorPainelMesa,
                        contentColor = CorTextoClaro
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .border(1.dp, CorDestaque.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                        .semantics(mergeDescendants = true) {
                            contentDescription = "${opcao.fala}. Toque duas vezes para entrar diretamente."
                            role = Role.Button
                            onClick(label = "Entrar na opção ${opcao.titulo}") {
                                indiceMenu = idx
                                confirmarOpcaoMenu(opcao.id)
                                true
                            }
                        }
                ) {
                    Text(
                        text = opcao.titulo,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }

    @OptIn(ExperimentalLayoutApi::class)
    @Composable
    private fun TelaEscolhaParceiro() {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "ESCOLHA SEU PARCEIRO",
                color = CorDestaque,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Toque duas vezes no craque para iniciar a partida em duplas:",
                color = CorTextoClaro,
                fontSize = 15.sp,
                textAlign = TextAlign.Center
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                CRAQUES_SELECAO.forEachIndexed { idx, nome ->
                    Button(
                        onClick = {
                            indiceParceiro = idx
                            confirmarParceiro(nome)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF15803D),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .semantics(mergeDescendants = true) {
                                contentDescription = "Jogar com o craque $nome. Toque duas vezes para iniciar a partida."
                                role = Role.Button
                                onClick(label = "Iniciar partida com $nome") {
                                    indiceParceiro = idx
                                    confirmarParceiro(nome)
                                    true
                                }
                            }
                    ) {
                        Text("⚽ $nome", fontSize = 17.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = {
                    telaAtual = TelaJogo.MENU_PRINCIPAL
                    anunciarMenuPrincipal()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF991B1B)),
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text("Voltar ao Menu Principal", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }

    @Composable
    private fun TelaAjuda() {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "AJUDA E COMANDOS",
                color = CorDestaque,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = CorPainelMesa),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("1. Como jogar Dominó Acessível:", color = CorDestaque, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                    Text("• Botões ◀ Pedra Anterior / Próxima Pedra ▶: navegam pelas pedras da sua mão falando o valor de cada uma.", color = CorTextoClaro, fontSize = 15.sp)
                    Text("• Botão JOGAR PEDRA: joga a pedra selecionada na mesa. Se ela encaixar nos dois lados, o jogo perguntará se prefere Esquerda ou Direita.", color = CorTextoClaro, fontSize = 15.sp)
                    Text("• Botão OUVIR PONTAS: lê apenas as duas pontas abertas da mesa (ex: '3 e 5').", color = CorTextoClaro, fontSize = 15.sp)
                    Text("• Botão COMPRAR DO MONTE: pesca uma pedra nos modos 1 vs 1 e Desafio Luna quando você não tiver jogada.", color = CorTextoClaro, fontSize = 15.sp)
                    Text("• Botão PASSAR A JOGADA: habilitado somente quando não houver pedras no monte.", color = CorTextoClaro, fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("2. Como jogar Truco Paulista contra a Luna (Rede Neural):", color = CorDestaque, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                    Text("• Botões ◀ Carta Anterior / Próxima Carta ▶: navegam pelas cartas da sua mão informando o naipe e se é Manilha (Pica-fumo, Espadilha, Copeta ou Zap).", color = CorTextoClaro, fontSize = 15.sp)
                    Text("• Botão JOGAR CARTA: desce a carta selecionada na mesa.", color = CorTextoClaro, fontSize = 15.sp)
                    Text("• Botão PEDIR TRUCO / SEIS / NOVE / DOZE: desafia a Rede Neural da Luna aumentando o valor da mão.", color = CorTextoClaro, fontSize = 15.sp)
                    Text("• Botão CARTA COBERTA: disponível a partir da 2ª queda para jogar a carta virada para baixo.", color = CorTextoClaro, fontSize = 15.sp)
                    Text("• Botão OUVIR MESA: anuncia o placar, a carta Vira, a Manilha e as cartas jogadas na queda atual.", color = CorTextoClaro, fontSize = 15.sp)
                }
            }

            Button(
                onClick = {
                    telaAtual = TelaJogo.SELECAO_JOGO
                    anunciarSelecaoJogo()
                },
                colors = ButtonDefaults.buttonColors(containerColor = CorDestaque, contentColor = Color.Black),
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text("VOLTAR À ESCOLHA DE JOGO", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }

    @OptIn(ExperimentalLayoutApi::class)
    @Composable
    private fun TelaPartida() {
        val tick = versaoEstado
        if (tick < 0) return
        val jogo = jogoAtual ?: return
        val maoJogador = jogo.maos[0] ?: emptyList()
        val indiceSeguro = if (maoJogador.isEmpty()) 0 else jogo.indiceSelecionado.coerceIn(0, maoJogador.lastIndex)
        val pedraSelecionada = maoJogador.getOrNull(indiceSeguro)
        val falaPedraSelecionada = pedraSelecionada?.falarJuiz() ?: "nenhuma pedra selecionada"
        val pendenteLado = pedraPendenteEscolhaLado
        val qtdMonte = jogo.monte.size
        val monteVazio = jogo.monte.isEmpty()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Cabeçalho e Status da Partida
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Vez de: ${jogo.nomes[jogo.jogadorAtual]}",
                        color = CorDestaque,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Button(
                        onClick = {
                            jobPartida?.cancel()
                            telaAtual = TelaJogo.SELECAO_JOGO
                            anunciarSelecaoJogo()
                        },
                        modifier = Modifier.semantics(mergeDescendants = true) {
                            contentDescription = "Voltar ao menu de escolha de jogo"
                            role = Role.Button
                        }
                    ) {
                        Text("Jogos", fontSize = 14.sp)
                    }
                }

                // Placar de pedras dos outros jogadores e monte
                Card(
                    colors = CardDefaults.cardColors(containerColor = CorPainelMesa),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        val infoOutros = (1 until jogo.numJogadores).joinToString("  |  ") { i ->
                            "${jogo.nomes[i]}: ${jogo.maos[i]?.size ?: 0} pç"
                        }
                        Text(text = infoOutros, color = CorTextoClaro, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        if (jogo.modo == 1 || jogo.modo == 3) {
                            Text(
                                text = "Monte: ${jogo.monte.size} pedra(s)  |  Vitórias: $vitoriasJogador",
                                color = CorTextoClaro,
                                fontSize = 13.sp
                            )
                        } else {
                            Text(
                                text = "Vitórias: $vitoriasJogador",
                                color = CorTextoClaro,
                                fontSize = 13.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Juiz: $ultimaMensagemJuiz",
                            color = CorDestaque,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.semantics {
                                liveRegion = LiveRegionMode.Polite
                            }
                        )
                    }
                }

                // Mesa (Tabuleiro Visual)
                Card(
                    colors = CardDefaults.cardColors(containerColor = CorPainelMesa),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "Mesa (${falarPontasJuiz(jogo.tabuleiro)}):",
                            color = CorTextoClaro,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        if (jogo.tabuleiro.isEmpty()) {
                            Text("(Mesa vazia — primeira jogada)", color = CorTextoClaro, fontSize = 14.sp)
                        } else {
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                jogo.tabuleiro.forEachIndexed { idxMesa, pedraMesa ->
                                    key(idxMesa, pedraMesa.a, pedraMesa.b) {
                                        PecaDominoVisual(pedra = pedraMesa, destaque = false, ladoDp = 32.dp)
                                    }
                                }
                            }
                        }
                    }
                }

                // Mão do Jogador
                Card(
                    colors = CardDefaults.cardColors(containerColor = CorPainelMesa),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        val maoCard = jogo.maos[0] ?: emptyList()
                        val idxCard = if (maoCard.isEmpty()) 0 else jogo.indiceSelecionado.coerceIn(0, maoCard.lastIndex)
                        val pedraCard = maoCard.getOrNull(idxCard)
                        val txtSelecionada = pedraCard?.let { "${it.formatar()} (${it.falarJuiz()})" } ?: "Nenhuma"
                        val falaSelecionadaCard = pedraCard?.falarJuiz() ?: "nenhuma pedra selecionada"
                        Text(
                            text = "Sua mão (${maoCard.size}) — Selecionada: $txtSelecionada",
                            color = CorTextoClaro,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.semantics {
                                contentDescription = "Sua mão com ${maoCard.size} pedras. Pedra selecionada: $falaSelecionadaCard"
                                stateDescription = "Pedra selecionada: $falaSelecionadaCard"
                                liveRegion = LiveRegionMode.Polite
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            maoCard.forEachIndexed { idx, pedraMao ->
                                val estaSelecionada = (idx == idxCard)
                                key(idx, pedraMao.a, pedraMao.b, estaSelecionada) {
                                    PecaDominoVisual(
                                        pedra = pedraMao,
                                        destaque = estaSelecionada,
                                        ladoDp = 36.dp,
                                        posicaoNaMao = idx + 1,
                                        totalNaMao = maoCard.size,
                                        aoClicar = { acaoJogarPedraDireto(idx) }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Painel de Controles Acessíveis Grandes na Parte Inferior
            if (pendenteLado != null) {
                val (esq, dir) = extremidades(jogo.tabuleiro)
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1A472A)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(2.dp, CorDestaque, RoundedCornerShape(12.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Em qual lado jogar ${pendenteLado.formatar()} (${pendenteLado.falarJuiz()})?",
                            color = CorDestaque,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { executarJogadaConfirmadaUsuario(pendenteLado, "ESQUERDA") },
                                colors = ButtonDefaults.buttonColors(containerColor = CorDestaque, contentColor = Color.Black),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp)
                                    .semantics(mergeDescendants = true) {
                                        contentDescription = "Jogar ${pendenteLado.falarJuiz()} na esquerda, ponta $esq"
                                        role = Role.Button
                                    }
                            ) {
                                Text("◀ ESQUERDA ($esq)", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = { executarJogadaConfirmadaUsuario(pendenteLado, "DIREITA") },
                                colors = ButtonDefaults.buttonColors(containerColor = CorDestaque, contentColor = Color.Black),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp)
                                    .semantics(mergeDescendants = true) {
                                        contentDescription = "Jogar ${pendenteLado.falarJuiz()} na direita, ponta $dir"
                                        role = Role.Button
                                    }
                            ) {
                                Text("DIREITA ($dir) ▶", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Button(
                            onClick = {
                                pedraPendenteEscolhaLado = null
                                anunciarImediato("Cancelado.", interromper = true)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .semantics(mergeDescendants = true) {
                                    contentDescription = "Cancelar escolha de lado"
                                    role = Role.Button
                                }
                        ) {
                            Text("Cancelar", fontSize = 16.sp)
                        }
                    }
                }
            } else {
                val podeComprarMonte = aguardandoJogadaUsuario && !monteVazio
                val podePassarJogada = aguardandoJogadaUsuario && monteVazio
                val pontasAtuais = falarPontasJuiz(jogo.tabuleiro)

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { acaoPedraAnterior() },
                            enabled = aguardandoJogadaUsuario && maoJogador.isNotEmpty(),
                            modifier = Modifier
                                .weight(1f)
                                .height(54.dp)
                                .semantics(mergeDescendants = true) {
                                    contentDescription = "Pedra anterior. Pedra selecionada: $falaPedraSelecionada"
                                    stateDescription = "Pedra selecionada: $falaPedraSelecionada"
                                    role = Role.Button
                                    if (aguardandoJogadaUsuario && maoJogador.isNotEmpty()) {
                                        onClick(label = "Selecionar pedra anterior") {
                                            acaoPedraAnterior()
                                            true
                                        }
                                    }
                                }
                        ) {
                            Text("◀ Pedra Anterior", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = { acaoProximaPedra() },
                            enabled = aguardandoJogadaUsuario && maoJogador.isNotEmpty(),
                            modifier = Modifier
                                .weight(1f)
                                .height(54.dp)
                                .semantics(mergeDescendants = true) {
                                    contentDescription = "Próxima pedra. Pedra selecionada: $falaPedraSelecionada"
                                    stateDescription = "Pedra selecionada: $falaPedraSelecionada"
                                    role = Role.Button
                                    if (aguardandoJogadaUsuario && maoJogador.isNotEmpty()) {
                                        onClick(label = "Selecionar próxima pedra") {
                                            acaoProximaPedra()
                                            true
                                        }
                                    }
                                }
                        ) {
                            Text("Próxima Pedra ▶", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Button(
                        onClick = { acaoJogarPedraSelecionada() },
                        enabled = aguardandoJogadaUsuario && pedraSelecionada != null,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CorDestaque,
                            contentColor = Color.Black
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .semantics(mergeDescendants = true) {
                                contentDescription = if (pedraSelecionada != null) {
                                    "Jogar pedra selecionada: $falaPedraSelecionada"
                                } else {
                                    "Jogar pedra: nenhuma pedra selecionada"
                                }
                                stateDescription = "Pedra selecionada: $falaPedraSelecionada"
                                role = Role.Button
                                if (aguardandoJogadaUsuario && pedraSelecionada != null) {
                                    onClick(label = "Jogar pedra $falaPedraSelecionada") {
                                        acaoJogarPedraSelecionada()
                                        true
                                    }
                                }
                            }
                    ) {
                        val labelPedra = pedraSelecionada?.let { "${it.formatar()} (${it.falarJuiz()})" } ?: ""
                        Text(
                            text = "JOGAR PEDRA $labelPedra",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { acaoOuvirPontas() },
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp)
                                .semantics(mergeDescendants = true) {
                                    contentDescription = "Ouvir pontas da mesa. Pontas atuais: $pontasAtuais"
                                    stateDescription = "Mesa: $pontasAtuais"
                                    role = Role.Button
                                    onClick(label = "Ouvir pontas da mesa") {
                                        acaoOuvirPontas()
                                        true
                                    }
                                }
                        ) {
                            Text("Ouvir Pontas", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        }
                        if (jogo.modo == 1 || jogo.modo == 3) {
                            Button(
                                onClick = { acaoComprarDoMonte() },
                                enabled = podeComprarMonte,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp)
                                    .semantics(mergeDescendants = true) {
                                        contentDescription = if (!monteVazio) {
                                            "Comprar pedra do monte. Restam $qtdMonte pedras no monte"
                                        } else {
                                            "Comprar pedra do monte. Monte vazio, botão desabilitado"
                                        }
                                        stateDescription = when {
                                            !aguardandoJogadaUsuario -> "Desabilitado, aguardando sua vez"
                                            !monteVazio -> "Habilitado, $qtdMonte pedras no monte"
                                            else -> "Desabilitado, monte vazio"
                                        }
                                        role = Role.Button
                                        if (podeComprarMonte) {
                                            onClick(label = "Comprar pedra do monte") {
                                                acaoComprarDoMonte()
                                                true
                                            }
                                        }
                                    }
                            ) {
                                Text("Comprar ($qtdMonte)", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }

                    Button(
                        onClick = { acaoPassarVez() },
                        enabled = podePassarJogada,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .semantics(mergeDescendants = true) {
                                contentDescription = if (monteVazio) {
                                    "Passar a jogada. Botão habilitado, não há pedras no monte"
                                } else {
                                    "Passar a jogada. Botão desabilitado porque ainda há $qtdMonte pedras no monte"
                                }
                                stateDescription = when {
                                    !aguardandoJogadaUsuario -> "Desabilitado, aguardando sua vez"
                                    !monteVazio -> "Desabilitado, há $qtdMonte pedras no monte"
                                    else -> "Habilitado, sem pedras no monte"
                                }
                                role = Role.Button
                                if (podePassarJogada) {
                                    onClick(label = "Passar a jogada") {
                                        acaoPassarVez()
                                        true
                                    }
                                }
                            }
                    ) {
                        val textoPassar = if (monteVazio) {
                            "Passar a Jogada"
                        } else {
                            "Passar a Jogada (Monte: $qtdMonte)"
                        }
                        Text(textoPassar, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    @Composable
    private fun TelaFimPartida() {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "FIM DA PARTIDA",
                color = CorDestaque,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = textoResultadoPartida,
                color = CorTextoClaro,
                fontSize = 20.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { iniciarNovaPartida() },
                colors = ButtonDefaults.buttonColors(containerColor = CorDestaque, contentColor = Color.Black),
                modifier = Modifier.fillMaxWidth().height(60.dp)
            ) {
                Text("JOGAR NOVAMENTE", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = {
                    telaAtual = TelaJogo.SELECAO_JOGO
                    anunciarSelecaoJogo()
                },
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text("VOLTAR À ESCOLHA DE JOGO", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }

    @OptIn(ExperimentalLayoutApi::class)
    @Composable
    private fun TelaPartidaTruco() {
        val tick = versaoEstado
        if (tick < 0) return
        val partida = partidaTrucoAtual ?: return
        val maoJog = partida.maoJogador
        val idxSeguro = if (maoJog.isEmpty()) 0 else partida.indiceCartaSelecionada.coerceIn(0, maoJog.lastIndex)
        val cartaSel = maoJog.getOrNull(idxSeguro)
        val falaCartaSel = when {
            cartaSel == null -> "nenhuma carta selecionada"
            partida.ehMaoDeFerro -> "Carta no escuro ${idxSeguro + 1} de ${maoJog.size}"
            else -> cartaSel.falarCarta(partida.valorManilha)
        }
        val nomeManilha = NOMES_VALORES_TRUCO[partida.valorManilha] ?: partida.valorManilha

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Topo: Título + Botão Voltar à Escolha de Jogo
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "TRUCO: $nomeUsuario ${partida.pontosJogador} x ${partida.pontosLuna} Luna",
                        color = CorDestaque,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.semantics {
                            contentDescription = "Placar do Truco: $nomeUsuario ${partida.pontosJogador} tentos, Luna ${partida.pontosLuna} tentos"
                        }
                    )
                    Button(
                        onClick = {
                            jobPartida?.cancel()
                            telaAtual = TelaJogo.SELECAO_JOGO
                            anunciarSelecaoJogo()
                        },
                        modifier = Modifier.semantics(mergeDescendants = true) {
                            contentDescription = "Voltar ao menu de escolha de jogo"
                            role = Role.Button
                        }
                    ) {
                        Text("Jogos", fontSize = 14.sp)
                    }
                }

                // Painel de Status da Mão, Vira, Manilha e Juiz
                Card(
                    colors = CardDefaults.cardColors(containerColor = CorPainelMesa),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val viraTxt = partida.vira?.let { "${it.formatarCurto()} (${it.falarCarta()})" } ?: "-"
                        val quedasTxt = if (partida.resumoQuedas.isEmpty()) {
                            "Nenhuma queda concluída"
                        } else {
                            partida.resumoQuedas.joinToString(" | ")
                        }
                        Text(
                            text = "${partida.quedaAtual}ª Queda  |  Mão vale: ${partida.valorMao} tento(s)  |  Manilha: $nomeManilha (${partida.valorManilha})",
                            color = CorTextoClaro,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Vira: $viraTxt  |  Quedas: $quedasTxt",
                            color = CorTextoClaro,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "Juiz: $ultimaMensagemJuiz",
                            color = CorDestaque,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.semantics {
                                liveRegion = LiveRegionMode.Polite
                            }
                        )
                    }
                }

                // Mesa de Truco (Vira, Carta da Luna e Sua Carta)
                Card(
                    colors = CardDefaults.cardColors(containerColor = CorPainelMesa),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "Mesa da ${partida.quedaAtual}ª Queda:",
                            color = CorTextoClaro,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            partida.vira?.let { cartaVira ->
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Vira", color = CorDestaque, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    CartaTrucoVisual(
                                        carta = cartaVira,
                                        rotuloContexto = "Carta Vira na mesa"
                                    )
                                }
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Luna", color = CorTextoClaro, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                val cLuna = partida.cartaMesaLuna
                                if (cLuna != null) {
                                    CartaTrucoVisual(
                                        carta = cLuna,
                                        valorManilha = partida.valorManilha,
                                        rotuloContexto = "Carta jogada por Luna"
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .border(1.dp, CorTextoClaro, RoundedCornerShape(10.dp))
                                            .padding(horizontal = 14.dp, vertical = 34.dp)
                                            .semantics {
                                                contentDescription = "Luna ainda não jogou carta nesta queda"
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("Aguardando", color = CorTextoClaro, fontSize = 11.sp)
                                    }
                                }
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(nomeUsuario, color = CorTextoClaro, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                val cJog = partida.cartaMesaJogador
                                if (cJog != null) {
                                    CartaTrucoVisual(
                                        carta = cJog,
                                        valorManilha = partida.valorManilha,
                                        rotuloContexto = "Carta jogada por você"
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .border(1.dp, CorTextoClaro, RoundedCornerShape(10.dp))
                                            .padding(horizontal = 14.dp, vertical = 34.dp)
                                            .semantics {
                                                contentDescription = "Você ainda não jogou carta nesta queda"
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("Sua jogada", color = CorTextoClaro, fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                // Mão do Jogador no Truco
                Card(
                    colors = CardDefaults.cardColors(containerColor = CorPainelMesa),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        val txtSel = when {
                            cartaSel == null -> "Nenhuma"
                            partida.ehMaoDeFerro -> "Carta no escuro ${idxSeguro + 1}"
                            else -> "${cartaSel.formatarCurto(partida.valorManilha)} (${cartaSel.falarCarta(partida.valorManilha)})"
                        }
                        Text(
                            text = "Suas Cartas (${maoJog.size}) — Selecionada: $txtSel",
                            color = CorTextoClaro,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.semantics {
                                contentDescription = "Sua mão com ${maoJog.size} cartas. Carta selecionada: $falaCartaSel"
                                stateDescription = "Carta selecionada: $falaCartaSel"
                                liveRegion = LiveRegionMode.Polite
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            maoJog.forEachIndexed { idx, carta ->
                                val selecionada = (idx == idxSeguro)
                                key(idx, carta.valor, carta.naipe.name, selecionada, partida.ehMaoDeFerro) {
                                    CartaTrucoVisual(
                                        carta = carta,
                                        valorManilha = partida.valorManilha,
                                        destaque = selecionada,
                                        ocultarFace = partida.ehMaoDeFerro,
                                        posicaoNaMao = idx + 1,
                                        totalNaMao = maoJog.size,
                                        aoClicar = { acaoJogarCartaDiretoTruco(idx) }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Painel Inferior de Decisão (Mão de 11, Resposta a Truco da Luna ou Controles de Jogada)
            when {
                partida.aguardandoDecisaoMao11Usuario -> {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A472A)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(2.dp, CorDestaque, RoundedCornerShape(12.dp))
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "MÃO DE 11! Jogar valendo 3 tentos ou correr?",
                                color = CorDestaque,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = { acaoResponderMao11Usuario(true) },
                                    colors = ButtonDefaults.buttonColors(containerColor = CorDestaque, contentColor = Color.Black),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(56.dp)
                                        .semantics(mergeDescendants = true) {
                                            contentDescription = "Aceitar Mão de 11 e jogar valendo 3 tentos"
                                            role = Role.Button
                                        }
                                ) {
                                    Text("JOGAR (VALE 3)", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                }
                                Button(
                                    onClick = { acaoResponderMao11Usuario(false) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(56.dp)
                                        .semantics(mergeDescendants = true) {
                                            contentDescription = "Correr da Mão de 11 e dar 1 tento para Luna"
                                            role = Role.Button
                                        }
                                ) {
                                    Text("CORRER (-1)", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
                partida.aguardandoRespostaTrucoUsuario -> {
                    val valorProp = partida.valorProposto
                    val (valorRebate, nomeRebate) = partida.proximoValorRebate()
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF4A1C1C)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(2.dp, CorDestaque, RoundedCornerShape(12.dp))
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "LUNA PEDIU ${valorProp} TENTOS! Sua resposta:",
                                color = CorDestaque,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { acaoResponderTrucoUsuario(DecisaoApostaLuna.ACEITAR) },
                                    colors = ButtonDefaults.buttonColors(containerColor = CorDestaque, contentColor = Color.Black),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(54.dp)
                                        .semantics(mergeDescendants = true) {
                                            contentDescription = "Aceitar pedido da Luna valendo $valorProp tentos"
                                            role = Role.Button
                                        }
                                ) {
                                    Text("ACEITAR ($valorProp)", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                }
                                Button(
                                    onClick = { acaoResponderTrucoUsuario(DecisaoApostaLuna.CORRER) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(54.dp)
                                        .semantics(mergeDescendants = true) {
                                            contentDescription = "Correr e entregar ${partida.valorMao} tento(s) para Luna"
                                            role = Role.Button
                                        }
                                ) {
                                    Text("CORRER", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            if (valorProp < 12) {
                                Button(
                                    onClick = { acaoResponderTrucoUsuario(DecisaoApostaLuna.AUMENTAR) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD84315), contentColor = Color.White),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(52.dp)
                                        .semantics(mergeDescendants = true) {
                                            contentDescription = "Aumentar aposta pedindo $nomeRebate, valendo $valorRebate tentos"
                                            role = Role.Button
                                        }
                                ) {
                                    Text("PEDIR $nomeRebate! (VALE $valorRebate)", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
                else -> {
                    val (proxValorTruco, proxNomeTruco) = partida.proximoValorTruco()
                    val podePedirTruco = aguardandoJogadaTrucoUsuario && partida.podeJogadorPedirTruco()
                    val podeCoberta = aguardandoJogadaTrucoUsuario && partida.quedaAtual >= 2

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { acaoCartaAnteriorTruco() },
                                enabled = aguardandoJogadaTrucoUsuario && maoJog.isNotEmpty(),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(54.dp)
                                    .semantics(mergeDescendants = true) {
                                        contentDescription = "Carta anterior. Carta selecionada: $falaCartaSel"
                                        stateDescription = "Carta selecionada: $falaCartaSel"
                                        role = Role.Button
                                        if (aguardandoJogadaTrucoUsuario && maoJog.isNotEmpty()) {
                                            onClick(label = "Selecionar carta anterior") {
                                                acaoCartaAnteriorTruco()
                                                true
                                            }
                                        }
                                    }
                            ) {
                                Text("◀ Carta Anterior", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = { acaoProximaCartaTruco() },
                                enabled = aguardandoJogadaTrucoUsuario && maoJog.isNotEmpty(),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(54.dp)
                                    .semantics(mergeDescendants = true) {
                                        contentDescription = "Próxima carta. Carta selecionada: $falaCartaSel"
                                        stateDescription = "Carta selecionada: $falaCartaSel"
                                        role = Role.Button
                                        if (aguardandoJogadaTrucoUsuario && maoJog.isNotEmpty()) {
                                            onClick(label = "Selecionar próxima carta") {
                                                acaoProximaCartaTruco()
                                                true
                                            }
                                        }
                                    }
                            ) {
                                Text("Próxima Carta ▶", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        val modoCobertaTxt = if (partida.jogarProximaCoberta) " [COBERTA]" else ""
                        Button(
                            onClick = { acaoJogarCartaSelecionadaTruco() },
                            enabled = aguardandoJogadaTrucoUsuario && cartaSel != null,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CorDestaque,
                                contentColor = Color.Black
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .semantics(mergeDescendants = true) {
                                    contentDescription = if (cartaSel != null) {
                                        "Jogar carta selecionada: $falaCartaSel$modoCobertaTxt"
                                    } else {
                                        "Jogar carta: nenhuma carta selecionada"
                                    }
                                    stateDescription = "Carta selecionada: $falaCartaSel"
                                    role = Role.Button
                                    if (aguardandoJogadaTrucoUsuario && cartaSel != null) {
                                        onClick(label = "Jogar carta $falaCartaSel") {
                                            acaoJogarCartaSelecionadaTruco()
                                            true
                                        }
                                    }
                                }
                        ) {
                            val labelCarta = when {
                                cartaSel == null -> ""
                                partida.ehMaoDeFerro -> "NO ESCURO"
                                else -> "${cartaSel.formatarCurto(partida.valorManilha)} (${cartaSel.falarCarta(partida.valorManilha)})"
                            }
                            Text(
                                text = "JOGAR CARTA $labelCarta$modoCobertaTxt",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { acaoPedirTrucoUsuario() },
                                enabled = podePedirTruco,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFD84315),
                                    contentColor = Color.White
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp)
                                    .semantics(mergeDescendants = true) {
                                        contentDescription = if (podePedirTruco) {
                                            "Pedir $proxNomeTruco, aumentando a mão para $proxValorTruco tentos"
                                        } else {
                                            "Pedir $proxNomeTruco desabilitado neste momento"
                                        }
                                        stateDescription = if (podePedirTruco) "Habilitado" else "Desabilitado"
                                        role = Role.Button
                                    }
                            ) {
                                Text("PEDIR $proxNomeTruco ($proxValorTruco)", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = { acaoAlternarCartaCobertaTruco() },
                                enabled = podeCoberta,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp)
                                    .semantics(mergeDescendants = true) {
                                        val statusCob = if (partida.jogarProximaCoberta) "ativada" else "desativada"
                                        contentDescription = if (podeCoberta) {
                                            "Alternar carta coberta. Atualmente $statusCob"
                                        } else {
                                            "Carta coberta desabilitada na primeira queda"
                                        }
                                        stateDescription = if (partida.jogarProximaCoberta) "Carta coberta ativada" else "Carta aberta normal"
                                        role = Role.Button
                                    }
                            ) {
                                val txtCob = if (partida.jogarProximaCoberta) "Coberta: SIM" else "Coberta: NÃO"
                                Text(txtCob, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }

                        Button(
                            onClick = { acaoOuvirMesaTruco() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .semantics(mergeDescendants = true) {
                                    contentDescription = "Ouvir resumo da mesa, placar, vira, manilha e cartas jogadas"
                                    role = Role.Button
                                }
                        ) {
                            Text("Ouvir Mesa, Vira e Placar", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun TelaFimPartidaTruco() {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "FIM DO TRUCO",
                color = CorDestaque,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = textoResultadoPartida,
                color = CorTextoClaro,
                fontSize = 20.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { iniciarNovaPartidaTruco() },
                colors = ButtonDefaults.buttonColors(containerColor = CorDestaque, contentColor = Color.Black),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .semantics(mergeDescendants = true) {
                        contentDescription = "Jogar Truco novamente contra a Rede Neural da Luna"
                        role = Role.Button
                    }
            ) {
                Text("JOGAR TRUCO NOVAMENTE", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = {
                    telaAtual = TelaJogo.SELECAO_JOGO
                    anunciarSelecaoJogo()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .semantics(mergeDescendants = true) {
                        contentDescription = "Voltar ao menu de escolha de jogo"
                        role = Role.Button
                    }
            ) {
                Text("VOLTAR À ESCOLHA DE JOGO", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
