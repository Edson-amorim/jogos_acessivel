package com.edson.dominoacessivel

import android.view.KeyEvent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.edson.dominoacessivel.academiapython.modelo.ModuloEnsino
import com.edson.dominoacessivel.academiapython.modelo.QuestaoDesafio
import com.edson.dominoacessivel.academiapython.modulos.RepositorioModulos
import com.edson.dominoacessivel.academiapython.motor.MotorJogoPython
import com.edson.dominoacessivel.academiapython.motor.PartidaForcaPython
import com.edson.dominoacessivel.academiapython.ui.BarraNavegacaoAcessivel
import com.edson.dominoacessivel.academiapython.ui.BlocoCodigoPython
import com.edson.dominoacessivel.academiapython.ui.BotaoAcessivelGrande
import com.edson.dominoacessivel.academiapython.ui.CorAmareloDestaque
import com.edson.dominoacessivel.academiapython.ui.CorAzulBotao
import com.edson.dominoacessivel.academiapython.ui.CorCartao
import com.edson.dominoacessivel.academiapython.ui.CorFundoEscuro
import com.edson.dominoacessivel.academiapython.ui.CorRoxoLuna
import com.edson.dominoacessivel.academiapython.ui.CorVerdeSucesso
import com.edson.dominoacessivel.academiapython.ui.CorVermelhoErro

enum class SubTelaAcademia {
    MENU_PRINCIPAL,
    LISTA_MODULOS,
    HUB_MODULO,
    AULA_TEORIA,
    CODIGO_LINHA_A_LINHA,
    LABORATORIO_SIMULADOR,
    QUIZ_MODULO,
    MARATONA_LUNA,
    DETETIVE_BUGS,
    JOGO_FORCA_PYTHON,
    AJUDA
}

data class ItemMenuAcademia(val id: Int, val titulo: String, val fala: String)

class EstadoAcademiaPython {
    var nomeJogador by mutableStateOf("Edson")
    var telaAtual by mutableStateOf(SubTelaAcademia.MENU_PRINCIPAL)
    var ultimaFalaLuna by mutableStateOf(
        "Bem-vindo à Academia Python da Luna! 8 módulos interativos com desafios, códigos linha por linha, quiz e jogo da forca!"
    )

    var indiceMenuPrincipal by mutableIntStateOf(0)
    var indiceModuloSelecionado by mutableIntStateOf(0)
    var indiceHubModulo by mutableIntStateOf(0)

    // Índices internos do Módulo
    var indiceTopicoTeoria by mutableIntStateOf(0)
    var indiceArquivoCodigo by mutableIntStateOf(0)
    var indiceLinhaExplicada by mutableIntStateOf(0)
    var indiceSimulador by mutableIntStateOf(0)

    // Estado do Quiz e Maratona
    var listaQuestoesAtivas by mutableStateOf<List<QuestaoDesafio>>(emptyList())
    var indiceQuestaoAtual by mutableIntStateOf(0)
    var indiceOpcaoSelecionada by mutableIntStateOf(0)
    var questaoJaRespondida by mutableStateOf(false)
    var acertouUltimaQuestao by mutableStateOf(false)
    var acertosSessao by mutableIntStateOf(0)
    var vidasMaratona by mutableIntStateOf(3)

    // Estado do Detetive de Bugs
    var indiceBugAtual by mutableIntStateOf(0)
    var indiceOpcaoBug by mutableIntStateOf(0)
    var bugJaRespondido by mutableStateOf(false)

    // Estado do Minijogo da Forca Python
    var partidaForca by mutableStateOf(PartidaForcaPython())
    var indiceLetraForca by mutableIntStateOf(0)

    fun falarEAtualizarPainel(
        audioVoz: GerenciadorAudioVoz,
        textoVisual: String,
        textoFalado: String = textoVisual,
        interromper: Boolean = true
    ) {
        ultimaFalaLuna = textoVisual
        audioVoz.falar(textoFalado, interromper = interromper)
    }

    fun moduloAtual(): ModuloEnsino {
        return RepositorioModulos.todosOsModulos[indiceModuloSelecionado]
    }

    fun anunciarMenuPrincipal(audioVoz: GerenciadorAudioVoz, motorJogo: MotorJogoPython) {
        val op = opcoesMenuPrincipal[indiceMenuPrincipal]
        val patente = motorJogo.obterPatenteAtual()
        falarEAtualizarPainel(
            audioVoz,
            textoVisual = "Professora Luna: Patente $patente (${motorJogo.xpTotal} XP). ${op.titulo}",
            textoFalado = "Academia Python da Luna. Patente: $patente, com ${motorJogo.xpTotal} pontos de experiência. ${op.fala}"
        )
    }

    fun tratarTecla(
        keyCode: Int,
        audioVoz: GerenciadorAudioVoz,
        motorJogo: MotorJogoPython,
        aoSairParaMenuPrincipal: () -> Unit
    ): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_BACK, KeyEvent.KEYCODE_ESCAPE -> {
                if (telaAtual == SubTelaAcademia.MENU_PRINCIPAL) {
                    audioVoz.tocarNavegar()
                    audioVoz.falar("Voltando para a seleção de jogos.", interromper = true)
                    aoSairParaMenuPrincipal()
                    return true
                } else {
                    telaAtual = when (telaAtual) {
                        SubTelaAcademia.AULA_TEORIA,
                        SubTelaAcademia.CODIGO_LINHA_A_LINHA,
                        SubTelaAcademia.LABORATORIO_SIMULADOR,
                        SubTelaAcademia.QUIZ_MODULO -> SubTelaAcademia.HUB_MODULO
                        SubTelaAcademia.HUB_MODULO -> SubTelaAcademia.LISTA_MODULOS
                        else -> SubTelaAcademia.MENU_PRINCIPAL
                    }
                    audioVoz.tocarNavegar()
                    audioVoz.falar("Voltando para a tela anterior.", interromper = true)
                    return true
                }
            }
            KeyEvent.KEYCODE_R -> {
                audioVoz.falar(ultimaFalaLuna, interromper = true)
                return true
            }
            KeyEvent.KEYCODE_V -> {
                val vel = audioVoz.alternarVelocidadeFala()
                ultimaFalaLuna = "Velocidade da voz ajustada para $vel."
                return true
            }
        }
        return false
    }

    val opcoesMenuPrincipal = listOf(
        ItemMenuAcademia(
            1,
            "1 - Trilha Completa de Aulas (8 Módulos de Python)",
            "Opção 1: Trilha Completa de Aulas com 8 Módulos baseados nos seus códigos de Python."
        ),
        ItemMenuAcademia(
            2,
            "2 - Show do Python contra a Luna (Desafio Geral)",
            "Opção 2: Show do Python contra a Luna. Maratona de perguntas valendo XP e Patentes!"
        ),
        ItemMenuAcademia(
            3,
            "3 - Detetive de Bugs Reais (Depurando seus Códigos)",
            "Opção 3: Detetive de Bugs Reais. Descubra e conserte os erros reais de sintaxe, tipos e testes!"
        ),
        ItemMenuAcademia(
            4,
            "4 - Minijogo da Forca Python (Termos de Programação)",
            "Opção 4: Jogar o Minijogo da Forca Python com termos e sintaxe de programação acessível."
        ),
        ItemMenuAcademia(
            5,
            "5 - Alterar Velocidade da Voz da Professora Luna",
            "Opção 5: Alterar a velocidade da voz da Professora Luna."
        ),
        ItemMenuAcademia(
            6,
            "6 - Ajuda e Como Estudar com Acessibilidade",
            "Opção 6: Ajuda e instruções de navegação da Academia Python."
        ),
        ItemMenuAcademia(
            7,
            "7 - Voltar para a Seleção de Jogos da Luna",
            "Opção 7: Voltar ao menu principal dos Jogos da Luna (Dominó e Truco)."
        )
    )

    val opcoesHubModulo = listOf(
        ItemMenuAcademia(1, "1 - Aula Teórica Explicada pela Luna", "Opção 1: Ouvir e ler a Aula Teórica passo a passo com dicas da Luna."),
        ItemMenuAcademia(2, "2 - Seus Códigos Explicados Linha por Linha", "Opção 2: Inspecionar os seus arquivos Python originais explicados linha por linha."),
        ItemMenuAcademia(3, "3 - Laboratório Prático (Simular Execução)", "Opção 3: Laboratório Prático. Veja e ouça a simulação do código rodando com dados reais."),
        ItemMenuAcademia(4, "4 - Desafio Quiz do Módulo (Valendo XP)", "Opção 4: Responder o Desafio Quiz deste módulo para ganhar pontos de experiência."),
        ItemMenuAcademia(5, "5 - Voltar para a Lista dos 8 Módulos", "Opção 5: Voltar para a lista de módulos.")
    )
}

@Composable
fun TelaAcademiaPython(
    estado: EstadoAcademiaPython,
    motorJogo: MotorJogoPython,
    audioVoz: GerenciadorAudioVoz,
    nomeJogador: String = "Edson",
    aoVoltarParaJogosDaLuna: () -> Unit
) {
    LaunchedEffect(nomeJogador) {
        estado.nomeJogador = nomeJogador
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CorFundoEscuro)
            .padding(12.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Cabeçalho de Status
        Card(
            colors = CardDefaults.cardColors(containerColor = CorCartao),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, CorAmareloDestaque, RoundedCornerShape(12.dp))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "🐍 ACADEMIA PYTHON DA LUNA",
                    color = CorAmareloDestaque,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "Jogador: $nomeJogador | Patente: ${motorJogo.obterPatenteAtual()} | XP: ${motorJogo.xpTotal}",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 2.dp)
                )
                Text(
                    text = estado.ultimaFalaLuna,
                    color = Color(0xFFBAE6FD),
                    fontSize = 15.sp,
                    modifier = Modifier
                        .padding(top = 6.dp)
                        .semantics { liveRegion = LiveRegionMode.Polite }
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        when (estado.telaAtual) {
            SubTelaAcademia.MENU_PRINCIPAL -> RenderMenuPrincipal(estado, motorJogo, audioVoz, aoVoltarParaJogosDaLuna)
            SubTelaAcademia.LISTA_MODULOS -> RenderListaModulos(estado, motorJogo, audioVoz)
            SubTelaAcademia.HUB_MODULO -> RenderHubModulo(estado, motorJogo, audioVoz)
            SubTelaAcademia.AULA_TEORIA -> RenderAulaTeoria(estado, motorJogo, audioVoz)
            SubTelaAcademia.CODIGO_LINHA_A_LINHA -> RenderCodigoLinhaALinha(estado, motorJogo, audioVoz)
            SubTelaAcademia.LABORATORIO_SIMULADOR -> RenderLaboratorioSimulador(estado, motorJogo, audioVoz)
            SubTelaAcademia.QUIZ_MODULO -> RenderQuiz(estado, motorJogo, audioVoz, ehMaratona = false)
            SubTelaAcademia.MARATONA_LUNA -> RenderQuiz(estado, motorJogo, audioVoz, ehMaratona = true)
            SubTelaAcademia.DETETIVE_BUGS -> RenderDetetiveBugs(estado, motorJogo, audioVoz)
            SubTelaAcademia.JOGO_FORCA_PYTHON -> RenderJogoForca(estado, motorJogo, audioVoz)
            SubTelaAcademia.AJUDA -> RenderAjuda(estado, audioVoz)
        }
    }
}

@Composable
private fun RenderMenuPrincipal(
    estado: EstadoAcademiaPython,
    motorJogo: MotorJogoPython,
    audioVoz: GerenciadorAudioVoz,
    aoVoltarParaJogosDaLuna: () -> Unit
) {
    Text(
        text = "Menu da Academia Python (Toque ou use Anterior / Próximo):",
        color = CorAmareloDestaque,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(vertical = 6.dp)
    )

    estado.opcoesMenuPrincipal.forEachIndexed { idx, item ->
        BotaoAcessivelGrande(
            texto = item.titulo,
            descricaoAcessivel = "${item.titulo}. Toque duas vezes para entrar diretamente.",
            selecionado = idx == estado.indiceMenuPrincipal,
            aoClicar = {
                estado.indiceMenuPrincipal = idx
                audioVoz.tocarConfirmar()
                executarOpcaoMenuPrincipal(item.id, estado, motorJogo, audioVoz, aoVoltarParaJogosDaLuna)
            }
        )
    }

    BarraNavegacaoAcessivel(
        aoAnterior = {
            estado.indiceMenuPrincipal = (estado.indiceMenuPrincipal - 1 + estado.opcoesMenuPrincipal.size) % estado.opcoesMenuPrincipal.size
            audioVoz.tocarNavegar()
            estado.falarEAtualizarPainel(audioVoz, estado.opcoesMenuPrincipal[estado.indiceMenuPrincipal].titulo, estado.opcoesMenuPrincipal[estado.indiceMenuPrincipal].fala)
        },
        aoProximo = {
            estado.indiceMenuPrincipal = (estado.indiceMenuPrincipal + 1) % estado.opcoesMenuPrincipal.size
            audioVoz.tocarNavegar()
            estado.falarEAtualizarPainel(audioVoz, estado.opcoesMenuPrincipal[estado.indiceMenuPrincipal].titulo, estado.opcoesMenuPrincipal[estado.indiceMenuPrincipal].fala)
        },
        aoConfirmar = null,
        aoOuvir = {
            estado.anunciarMenuPrincipal(audioVoz, motorJogo)
        },
        aoVoltar = {
            audioVoz.tocarNavegar()
            audioVoz.falar("Voltando aos Jogos da Luna.", interromper = true)
            aoVoltarParaJogosDaLuna()
        }
    )
}

private fun executarOpcaoMenuPrincipal(
    id: Int,
    estado: EstadoAcademiaPython,
    motorJogo: MotorJogoPython,
    audioVoz: GerenciadorAudioVoz,
    aoVoltarParaJogosDaLuna: () -> Unit
) {
    when (id) {
        1 -> {
            estado.telaAtual = SubTelaAcademia.LISTA_MODULOS
            val mod = estado.moduloAtual()
            estado.falarEAtualizarPainel(
                audioVoz,
                "Escolha um dos 8 Módulos de Python. Selecionado: ${mod.titulo}",
                "Lista dos 8 Módulos de Python. Selecionado: ${mod.resumoFalado}"
            )
        }
        2 -> {
            estado.listaQuestoesAtivas = RepositorioModulos.todasAsQuestoes().shuffled().take(10)
            estado.indiceQuestaoAtual = 0
            estado.indiceOpcaoSelecionada = 0
            estado.questaoJaRespondida = false
            estado.acertosSessao = 0
            estado.vidasMaratona = 3
            estado.telaAtual = SubTelaAcademia.MARATONA_LUNA
            val q = estado.listaQuestoesAtivas[0]
            estado.falarEAtualizarPainel(
                audioVoz,
                "Show do Python contra a Luna! 10 questões sorteadas, 3 vidas! Questão 1: ${q.enunciado}",
                "Show do Python contra a Luna iniciado! Você tem 3 vidas para encarar 10 desafios de todos os módulos! Questão 1: ${q.enunciado}. Opção 1: ${q.opcoes[0]}"
            )
        }
        3 -> {
            estado.indiceBugAtual = 0
            estado.indiceOpcaoBug = 0
            estado.bugJaRespondido = false
            estado.telaAtual = SubTelaAcademia.DETETIVE_BUGS
            val caso = RepositorioModulos.todosOsBugs[0]
            estado.falarEAtualizarPainel(
                audioVoz,
                "🕵️ ${caso.titulo} (${caso.arquivoOrigem})",
                "Detetive de Bugs. ${caso.titulo}, do arquivo ${caso.arquivoOrigem}. História: ${caso.historiaBug} Pergunta: ${caso.pergunta}. Alternativa 1: ${caso.opcoes[0]}"
            )
        }
        4 -> {
            estado.partidaForca = PartidaForcaPython()
            estado.indiceLetraForca = 0
            estado.telaAtual = SubTelaAcademia.JOGO_FORCA_PYTHON
            estado.falarEAtualizarPainel(
                audioVoz,
                "Jogo da Forca Python iniciado! A palavra tem ${estado.partidaForca.palavraSecreta.length} letras. Dica: ${estado.partidaForca.dica}",
                "Jogo da Forca Python iniciado! A palavra tem ${estado.partidaForca.palavraSecreta.length} letras. Dica da Luna: ${estado.partidaForca.dica}. Letra selecionada: A."
            )
        }
        5 -> {
            val novaVel = audioVoz.alternarVelocidadeFala()
            estado.ultimaFalaLuna = "Velocidade da voz ajustada para $novaVel."
        }
        6 -> {
            estado.telaAtual = SubTelaAcademia.AJUDA
            estado.falarEAtualizarPainel(
                audioVoz,
                "Ajuda da Academia Python da Luna. Todos os arquivos de Python foram organizados em 8 módulos didáticos com voz e quiz.",
                "Ajuda da Academia Python da Luna. Você pode tocar diretamente em qualquer botão ou usar os botões Anterior, Próximo, Confirmar e Ouvir. No leitor de códigos, você pode ouvir cada linha de Python explicada pela Luna!"
            )
        }
        7 -> {
            audioVoz.falar("Voltando aos Jogos da Luna.", interromper = true)
            aoVoltarParaJogosDaLuna()
        }
    }
}

@Composable
private fun RenderListaModulos(
    estado: EstadoAcademiaPython,
    motorJogo: MotorJogoPython,
    audioVoz: GerenciadorAudioVoz
) {
    val modulos = RepositorioModulos.todosOsModulos
    Text(
        text = "Trilha de Ensino (8 Módulos de Python):",
        color = CorAmareloDestaque,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(vertical = 6.dp)
    )

    modulos.forEachIndexed { idx, mod ->
        val melhor = motorJogo.melhorPontuacaoModulo(mod.numero)
        val totalQ = mod.questoes.size
        val selo = if (melhor > 0) " ⭐ ($melhor/$totalQ)" else ""
        BotaoAcessivelGrande(
            texto = "${mod.titulo}$selo\n📂 Arquivos: ${mod.arquivosOrigem.joinToString(", ")}",
            descricaoAcessivel = "${mod.titulo}. ${mod.resumoFalado}. Toque duas vezes para entrar neste módulo.",
            selecionado = idx == estado.indiceModuloSelecionado,
            aoClicar = {
                estado.indiceModuloSelecionado = idx
                estado.indiceHubModulo = 0
                audioVoz.tocarConfirmar()
                estado.telaAtual = SubTelaAcademia.HUB_MODULO
                anunciarHubModulo(estado, audioVoz)
            }
        )
    }

    BarraNavegacaoAcessivel(
        aoAnterior = {
            estado.indiceModuloSelecionado = (estado.indiceModuloSelecionado - 1 + modulos.size) % modulos.size
            audioVoz.tocarNavegar()
            val mod = estado.moduloAtual()
            estado.falarEAtualizarPainel(audioVoz, mod.titulo, mod.resumoFalado)
        },
        aoProximo = {
            estado.indiceModuloSelecionado = (estado.indiceModuloSelecionado + 1) % modulos.size
            audioVoz.tocarNavegar()
            val mod = estado.moduloAtual()
            estado.falarEAtualizarPainel(audioVoz, mod.titulo, mod.resumoFalado)
        },
        aoConfirmar = null,
        aoOuvir = {
            val mod = estado.moduloAtual()
            estado.falarEAtualizarPainel(audioVoz, mod.titulo, mod.resumoFalado)
        },
        aoVoltar = {
            estado.telaAtual = SubTelaAcademia.MENU_PRINCIPAL
            estado.anunciarMenuPrincipal(audioVoz, motorJogo)
        }
    )
}

private fun anunciarHubModulo(estado: EstadoAcademiaPython, audioVoz: GerenciadorAudioVoz) {
    val mod = estado.moduloAtual()
    val op = estado.opcoesHubModulo[estado.indiceHubModulo]
    estado.falarEAtualizarPainel(
        audioVoz,
        "${mod.titulo} — ${mod.subtitulo}",
        "${mod.titulo}. Arquivos estudados: ${mod.arquivosOrigem.joinToString(", ")}. ${op.fala}"
    )
}

@Composable
private fun RenderHubModulo(
    estado: EstadoAcademiaPython,
    motorJogo: MotorJogoPython,
    audioVoz: GerenciadorAudioVoz
) {
    val mod = estado.moduloAtual()
    Text(
        text = mod.titulo,
        color = CorAmareloDestaque,
        fontSize = 19.sp,
        fontWeight = FontWeight.Bold
    )
    Text(
        text = "Arquivos: ${mod.arquivosOrigem.joinToString(", ")}",
        color = CorVerdeSucesso,
        fontSize = 15.sp,
        modifier = Modifier.padding(bottom = 8.dp)
    )

    estado.opcoesHubModulo.forEachIndexed { idx, item ->
        BotaoAcessivelGrande(
            texto = item.titulo,
            descricaoAcessivel = "${item.titulo}. Toque duas vezes para entrar diretamente.",
            selecionado = idx == estado.indiceHubModulo,
            aoClicar = {
                estado.indiceHubModulo = idx
                audioVoz.tocarConfirmar()
                executarOpcaoHubModulo(item.id, estado, audioVoz)
            }
        )
    }

    BarraNavegacaoAcessivel(
        aoAnterior = {
            estado.indiceHubModulo = (estado.indiceHubModulo - 1 + estado.opcoesHubModulo.size) % estado.opcoesHubModulo.size
            audioVoz.tocarNavegar()
            estado.falarEAtualizarPainel(audioVoz, estado.opcoesHubModulo[estado.indiceHubModulo].titulo, estado.opcoesHubModulo[estado.indiceHubModulo].fala)
        },
        aoProximo = {
            estado.indiceHubModulo = (estado.indiceHubModulo + 1) % estado.opcoesHubModulo.size
            audioVoz.tocarNavegar()
            estado.falarEAtualizarPainel(audioVoz, estado.opcoesHubModulo[estado.indiceHubModulo].titulo, estado.opcoesHubModulo[estado.indiceHubModulo].fala)
        },
        aoConfirmar = null,
        aoOuvir = { anunciarHubModulo(estado, audioVoz) },
        aoVoltar = {
            estado.telaAtual = SubTelaAcademia.LISTA_MODULOS
            estado.falarEAtualizarPainel(audioVoz, "Lista de Módulos. Selecionado: ${mod.titulo}", mod.resumoFalado)
        }
    )
}

private fun executarOpcaoHubModulo(
    id: Int,
    estado: EstadoAcademiaPython,
    audioVoz: GerenciadorAudioVoz
) {
    val mod = estado.moduloAtual()
    when (id) {
        1 -> {
            estado.indiceTopicoTeoria = 0
            estado.telaAtual = SubTelaAcademia.AULA_TEORIA
            val topico = mod.topicosTeoria[0]
            estado.falarEAtualizarPainel(
                audioVoz,
                "${topico.titulo} (1/${mod.topicosTeoria.size})",
                "${topico.explicacaoFalada} ${topico.dicaDaLuna}"
            )
        }
        2 -> {
            estado.indiceArquivoCodigo = 0
            estado.indiceLinhaExplicada = 0
            estado.telaAtual = SubTelaAcademia.CODIGO_LINHA_A_LINHA
            anunciarLinhaCodigo(estado, audioVoz)
        }
        3 -> {
            estado.indiceSimulador = 0
            estado.telaAtual = SubTelaAcademia.LABORATORIO_SIMULADOR
            anunciarSimulador(estado, audioVoz)
        }
        4 -> {
            estado.listaQuestoesAtivas = mod.questoes
            estado.indiceQuestaoAtual = 0
            estado.indiceOpcaoSelecionada = 0
            estado.questaoJaRespondida = false
            estado.acertosSessao = 0
            estado.telaAtual = SubTelaAcademia.QUIZ_MODULO
            anunciarQuestaoAtual(estado, audioVoz)
        }
        5 -> {
            estado.telaAtual = SubTelaAcademia.LISTA_MODULOS
            estado.falarEAtualizarPainel(audioVoz, "Voltando para a lista dos 8 Módulos.", mod.resumoFalado)
        }
    }
}

private fun anunciarLinhaCodigo(estado: EstadoAcademiaPython, audioVoz: GerenciadorAudioVoz) {
    val mod = estado.moduloAtual()
    val arq = mod.codigosOriginais[estado.indiceArquivoCodigo]
    val linha = arq.linhasExplicadas[estado.indiceLinhaExplicada]
    estado.falarEAtualizarPainel(
        audioVoz,
        "Arquivo ${arq.nomeArquivo} — Passo ${estado.indiceLinhaExplicada + 1}/${arq.linhasExplicadas.size}: ${linha.explicacaoDidatica}",
        "Arquivo ${arq.nomeArquivo}. ${linha.leituraFalada} Explicação da Luna: ${linha.explicacaoDidatica}"
    )
}

private fun anunciarSimulador(estado: EstadoAcademiaPython, audioVoz: GerenciadorAudioVoz) {
    val mod = estado.moduloAtual()
    val sim = mod.cenariosSimulador[estado.indiceSimulador]
    estado.falarEAtualizarPainel(
        audioVoz,
        sim.titulo,
        "Laboratório Prático. ${sim.titulo}. Entradas simuladas: ${sim.entradasSimuladas}. Saída na tela: ${sim.saidaConsole}. Explicação da Luna: ${sim.explicacaoExecucao}"
    )
}

private fun anunciarQuestaoAtual(estado: EstadoAcademiaPython, audioVoz: GerenciadorAudioVoz) {
    val q = estado.listaQuestoesAtivas[estado.indiceQuestaoAtual]
    val op = q.opcoes[estado.indiceOpcaoSelecionada]
    estado.falarEAtualizarPainel(
        audioVoz,
        "Questão ${estado.indiceQuestaoAtual + 1}/${estado.listaQuestoesAtivas.size}: ${q.enunciado}",
        "Questão ${estado.indiceQuestaoAtual + 1} de ${estado.listaQuestoesAtivas.size}. ${q.enunciado}. Alternativa ${estado.indiceOpcaoSelecionada + 1}: $op"
    )
}

@Composable
private fun RenderAulaTeoria(
    estado: EstadoAcademiaPython,
    motorJogo: MotorJogoPython,
    audioVoz: GerenciadorAudioVoz
) {
    val mod = estado.moduloAtual()
    val topicos = mod.topicosTeoria
    val topico = topicos[estado.indiceTopicoTeoria]

    Card(
        colors = CardDefaults.cardColors(containerColor = CorCartao),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "${mod.titulo} — Tópico ${estado.indiceTopicoTeoria + 1} de ${topicos.size}",
                color = CorVerdeSucesso,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = topico.titulo,
                color = CorAmareloDestaque,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 6.dp)
            )
            Text(
                text = topico.explicacaoVisual,
                color = Color.White,
                fontSize = 17.sp,
                modifier = Modifier.padding(bottom = 10.dp)
            )
            BlocoCodigoPython(
                titulo = "Exemplo de Código Python:",
                codigo = topico.exemploCodigo,
                aoOuvirCodigo = {
                    val falado = audioVoz.converterCodigoParaFala(topico.exemploCodigo)
                    audioVoz.falar("Leitura simbólica do código: $falado", interromper = true)
                }
            )
            Card(
                colors = CardDefaults.cardColors(containerColor = CorRoxoLuna.copy(alpha = 0.35f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                Text(
                    text = "💡 ${topico.dicaDaLuna}",
                    color = CorAmareloDestaque,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(10.dp)
                )
            }
        }
    }

    BarraNavegacaoAcessivel(
        rotuloAnterior = "◀ Anterior",
        rotuloProximo = "Próximo ▶",
        rotuloConfirmar = "💡 Ouvir Dica",
        rotuloOuvir = "🔊 Repetir Aula",
        aoAnterior = {
            if (estado.indiceTopicoTeoria > 0) {
                estado.indiceTopicoTeoria--
                audioVoz.tocarNavegar()
                val t = topicos[estado.indiceTopicoTeoria]
                estado.falarEAtualizarPainel(audioVoz, "${t.titulo} (${estado.indiceTopicoTeoria + 1}/${topicos.size})", "${t.explicacaoFalada} ${t.dicaDaLuna}")
            } else {
                audioVoz.falar("Você já está no primeiro tópico deste módulo.", interromper = true)
            }
        },
        aoProximo = {
            if (estado.indiceTopicoTeoria < topicos.lastIndex) {
                estado.indiceTopicoTeoria++
                audioVoz.tocarNavegar()
                val t = topicos[estado.indiceTopicoTeoria]
                estado.falarEAtualizarPainel(audioVoz, "${t.titulo} (${estado.indiceTopicoTeoria + 1}/${topicos.size})", "${t.explicacaoFalada} ${t.dicaDaLuna}")
            } else {
                motorJogo.adicionarXp(15)
                audioVoz.tocarAcerto()
                estado.falarEAtualizarPainel(
                    audioVoz,
                    "Parabéns! Você concluiu a teoria deste módulo e ganhou 15 XP! Vamos ver seus códigos linha por linha!",
                    "Parabéns, ${estado.nomeJogador}! Você concluiu todos os tópicos teóricos deste módulo e ganhou 15 pontos de experiência! Abrindo agora seus códigos explicados linha por linha."
                )
                estado.indiceArquivoCodigo = 0
                estado.indiceLinhaExplicada = 0
                estado.telaAtual = SubTelaAcademia.CODIGO_LINHA_A_LINHA
            }
        },
        aoConfirmar = {
            audioVoz.tocarDica()
            audioVoz.falar(topico.dicaDaLuna, interromper = true)
        },
        aoOuvir = {
            estado.falarEAtualizarPainel(audioVoz, "${topico.titulo} (${estado.indiceTopicoTeoria + 1}/${topicos.size})", "${topico.explicacaoFalada} ${topico.dicaDaLuna}")
        },
        aoVoltar = {
            estado.telaAtual = SubTelaAcademia.HUB_MODULO
            anunciarHubModulo(estado, audioVoz)
        }
    )
}

@Composable
private fun RenderCodigoLinhaALinha(
    estado: EstadoAcademiaPython,
    motorJogo: MotorJogoPython,
    audioVoz: GerenciadorAudioVoz
) {
    val mod = estado.moduloAtual()
    val codigos = mod.codigosOriginais
    val arq = codigos[estado.indiceArquivoCodigo]
    val linha = arq.linhasExplicadas[estado.indiceLinhaExplicada]

    if (codigos.size > 1) {
        BotaoAcessivelGrande(
            texto = "📂 Trocar Arquivo (${estado.indiceArquivoCodigo + 1}/${codigos.size}): ${arq.nomeArquivo}",
            descricaoAcessivel = "Trocar entre os arquivos Python deste módulo. Arquivo atual: ${arq.nomeArquivo}",
            corFundo = CorRoxoLuna,
            aoClicar = {
                estado.indiceArquivoCodigo = (estado.indiceArquivoCodigo + 1) % codigos.size
                estado.indiceLinhaExplicada = 0
                audioVoz.tocarNavegar()
                anunciarLinhaCodigo(estado, audioVoz)
            }
        )
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = CorCartao),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "📄 ${arq.nomeArquivo} — ${arq.titulo}",
                color = CorAmareloDestaque,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = arq.descricaoContexto,
                color = Color.White,
                fontSize = 15.sp,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            BlocoCodigoPython(
                titulo = "Código Original (${arq.nomeArquivo}):",
                codigo = arq.codigoCompleto,
                aoOuvirCodigo = {
                    val falado = audioVoz.converterCodigoParaFala(arq.codigoCompleto)
                    audioVoz.falar("Código completo de ${arq.nomeArquivo}: $falado", interromper = true)
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, CorVerdeSucesso, RoundedCornerShape(10.dp))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "🔍 Explicação Linha por Linha (${estado.indiceLinhaExplicada + 1} de ${arq.linhasExplicadas.size}):",
                        color = CorVerdeSucesso,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = linha.codigo,
                        color = CorAmareloDestaque,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 6.dp)
                    )
                    Text(
                        text = "🗣️ Como ler: ${linha.leituraFalada}",
                        color = Color(0xFFBAE6FD),
                        fontSize = 15.sp
                    )
                    Text(
                        text = "👩‍🏫 O que faz: ${linha.explicacaoDidatica}",
                        color = Color.White,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
            }
        }
    }

    BarraNavegacaoAcessivel(
        rotuloAnterior = "◀ Linha Anterior",
        rotuloProximo = "Próxima Linha ▶",
        rotuloConfirmar = "📂 Outro Arquivo",
        rotuloOuvir = "🔊 Ouvir Linha",
        aoAnterior = {
            if (estado.indiceLinhaExplicada > 0) {
                estado.indiceLinhaExplicada--
                audioVoz.tocarNavegar()
                anunciarLinhaCodigo(estado, audioVoz)
            } else {
                audioVoz.falar("Já está na primeira linha explicada deste arquivo.", interromper = true)
            }
        },
        aoProximo = {
            if (estado.indiceLinhaExplicada < arq.linhasExplicadas.lastIndex) {
                estado.indiceLinhaExplicada++
                audioVoz.tocarNavegar()
                anunciarLinhaCodigo(estado, audioVoz)
            } else {
                motorJogo.adicionarXp(15)
                audioVoz.tocarAcerto()
                audioVoz.falar("Você revisou todas as linhas deste código e ganhou 15 pontos de experiência!", interromper = true)
            }
        },
        aoConfirmar = {
            estado.indiceArquivoCodigo = (estado.indiceArquivoCodigo + 1) % codigos.size
            estado.indiceLinhaExplicada = 0
            audioVoz.tocarNavegar()
            anunciarLinhaCodigo(estado, audioVoz)
        },
        aoOuvir = { anunciarLinhaCodigo(estado, audioVoz) },
        aoVoltar = {
            estado.telaAtual = SubTelaAcademia.HUB_MODULO
            anunciarHubModulo(estado, audioVoz)
        }
    )
}

@Composable
private fun RenderLaboratorioSimulador(
    estado: EstadoAcademiaPython,
    motorJogo: MotorJogoPython,
    audioVoz: GerenciadorAudioVoz
) {
    val mod = estado.moduloAtual()
    val cenarios = mod.cenariosSimulador
    val sim = cenarios[estado.indiceSimulador]

    Card(
        colors = CardDefaults.cardColors(containerColor = CorCartao),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "🧪 Laboratório Prático (${estado.indiceSimulador + 1}/${cenarios.size})",
                color = CorVerdeSucesso,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = sim.titulo,
                color = CorAmareloDestaque,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 6.dp)
            )
            Text(
                text = "⌨️ Entradas Digitadas:\n${sim.entradasSimuladas}",
                color = Color.White,
                fontSize = 16.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            BlocoCodigoPython(
                titulo = "🖥️ Saída no Console do Python:",
                codigo = sim.saidaConsole,
                aoOuvirCodigo = {
                    audioVoz.falar("Saída do console: ${sim.saidaConsole}", interromper = true)
                }
            )
            Text(
                text = "👩‍🏫 Passo a Passo da Execução:\n${sim.explicacaoExecucao}",
                color = Color(0xFFBAE6FD),
                fontSize = 16.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }

    BarraNavegacaoAcessivel(
        rotuloAnterior = "◀ Anterior",
        rotuloProximo = "Próximo ▶",
        rotuloConfirmar = "🎯 Ir para o Quiz",
        rotuloOuvir = "🔊 Ouvir Simulação",
        aoAnterior = {
            estado.indiceSimulador = (estado.indiceSimulador - 1 + cenarios.size) % cenarios.size
            audioVoz.tocarNavegar()
            anunciarSimulador(estado, audioVoz)
        },
        aoProximo = {
            estado.indiceSimulador = (estado.indiceSimulador + 1) % cenarios.size
            audioVoz.tocarNavegar()
            anunciarSimulador(estado, audioVoz)
        },
        aoConfirmar = {
            estado.listaQuestoesAtivas = mod.questoes
            estado.indiceQuestaoAtual = 0
            estado.indiceOpcaoSelecionada = 0
            estado.questaoJaRespondida = false
            estado.acertosSessao = 0
            estado.telaAtual = SubTelaAcademia.QUIZ_MODULO
            anunciarQuestaoAtual(estado, audioVoz)
        },
        aoOuvir = { anunciarSimulador(estado, audioVoz) },
        aoVoltar = {
            estado.telaAtual = SubTelaAcademia.HUB_MODULO
            anunciarHubModulo(estado, audioVoz)
        }
    )
}

@Composable
private fun RenderQuiz(
    estado: EstadoAcademiaPython,
    motorJogo: MotorJogoPython,
    audioVoz: GerenciadorAudioVoz,
    ehMaratona: Boolean
) {
    val q = estado.listaQuestoesAtivas[estado.indiceQuestaoAtual]

    Card(
        colors = CardDefaults.cardColors(containerColor = CorCartao),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            val cabecalho = if (ehMaratona) {
                "🏆 Show do Python (${estado.indiceQuestaoAtual + 1}/${estado.listaQuestoesAtivas.size}) | ❤️ Vidas: ${estado.vidasMaratona} | Acertos: ${estado.acertosSessao}"
            } else {
                "🎯 Desafio Módulo ${q.moduloNumero} (${estado.indiceQuestaoAtual + 1}/${estado.listaQuestoesAtivas.size}) | Acertos: ${estado.acertosSessao}"
            }
            Text(
                text = cabecalho,
                color = CorVerdeSucesso,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
            Text(
                text = q.enunciado,
                color = CorAmareloDestaque,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            if (q.codigoApoio.isNotBlank()) {
                BlocoCodigoPython(
                    titulo = "Trecho de Código:",
                    codigo = q.codigoApoio,
                    aoOuvirCodigo = {
                        audioVoz.falar(audioVoz.converterCodigoParaFala(q.codigoApoio), interromper = true)
                    }
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(6.dp))

    q.opcoes.forEachIndexed { idx, textoOpcao ->
        val corBotao = when {
            !estado.questaoJaRespondida -> CorAzulBotao
            idx == q.indiceCorreto -> Color(0xFF15803D)
            idx == estado.indiceOpcaoSelecionada -> CorVermelhoErro
            else -> Color(0xFF334155)
        }
        BotaoAcessivelGrande(
            texto = "${idx + 1}) $textoOpcao",
            descricaoAcessivel = "Alternativa ${idx + 1}: $textoOpcao. Toque duas vezes para responder.",
            selecionado = idx == estado.indiceOpcaoSelecionada,
            corFundo = corBotao,
            aoClicar = {
                if (!estado.questaoJaRespondida) {
                    responderQuestao(idx, estado, motorJogo, audioVoz, ehMaratona)
                } else {
                    avancarProximaQuestao(estado, motorJogo, audioVoz, ehMaratona)
                }
            }
        )
    }

    if (estado.questaoJaRespondida) {
        BotaoAcessivelGrande(
            texto = "▶ Avançar para a Próxima Questão",
            descricaoAcessivel = "Avançar para a próxima questão. Toque duas vezes para avançar.",
            corFundo = Color(0xFF15803D),
            aoClicar = { avancarProximaQuestao(estado, motorJogo, audioVoz, ehMaratona) }
        )
    }

    BarraNavegacaoAcessivel(
        rotuloAnterior = "◀ Opção Anterior",
        rotuloProximo = "Próxima Opção ▶",
        aoAnterior = {
            estado.indiceOpcaoSelecionada = (estado.indiceOpcaoSelecionada - 1 + q.opcoes.size) % q.opcoes.size
            audioVoz.tocarNavegar()
            audioVoz.falar("Alternativa ${estado.indiceOpcaoSelecionada + 1}: ${q.opcoes[estado.indiceOpcaoSelecionada]}", interromper = true)
        },
        aoProximo = {
            estado.indiceOpcaoSelecionada = (estado.indiceOpcaoSelecionada + 1) % q.opcoes.size
            audioVoz.tocarNavegar()
            audioVoz.falar("Alternativa ${estado.indiceOpcaoSelecionada + 1}: ${q.opcoes[estado.indiceOpcaoSelecionada]}", interromper = true)
        },
        aoConfirmar = null,
        aoOuvir = { anunciarQuestaoAtual(estado, audioVoz) },
        aoVoltar = {
            estado.telaAtual = if (ehMaratona) SubTelaAcademia.MENU_PRINCIPAL else SubTelaAcademia.HUB_MODULO
            if (ehMaratona) estado.anunciarMenuPrincipal(audioVoz, motorJogo) else anunciarHubModulo(estado, audioVoz)
        }
    )
}

private fun responderQuestao(
    indiceEscolhido: Int,
    estado: EstadoAcademiaPython,
    motorJogo: MotorJogoPython,
    audioVoz: GerenciadorAudioVoz,
    ehMaratona: Boolean
) {
    val q = estado.listaQuestoesAtivas[estado.indiceQuestaoAtual]
    estado.indiceOpcaoSelecionada = indiceEscolhido
    estado.questaoJaRespondida = true

    if (indiceEscolhido == q.indiceCorreto) {
        estado.acertouUltimaQuestao = true
        estado.acertosSessao++
        motorJogo.adicionarXp(q.pontosXp)
        audioVoz.tocarAcerto()
        val elogio = motorJogo.sortearElogio(estado.nomeJogador)
        estado.falarEAtualizarPainel(
            audioVoz,
            "✅ CORRETO! +${q.pontosXp} XP! ${q.explicacaoResposta}",
            "Resposta correta! $elogio Você ganhou ${q.pontosXp} pontos de experiência! ${q.explicacaoResposta}"
        )
    } else {
        estado.acertouUltimaQuestao = false
        if (ehMaratona) estado.vidasMaratona--
        audioVoz.tocarErro()
        val incentivo = motorJogo.sortearIncentivo(estado.nomeJogador)
        val correta = q.opcoes[q.indiceCorreto]
        estado.falarEAtualizarPainel(
            audioVoz,
            "❌ Resposta incorreta. A correta era: $correta. ${q.explicacaoResposta}",
            "$incentivo A resposta correta era: $correta. Explicação: ${q.explicacaoResposta}"
        )
    }
}

private fun avancarProximaQuestao(
    estado: EstadoAcademiaPython,
    motorJogo: MotorJogoPython,
    audioVoz: GerenciadorAudioVoz,
    ehMaratona: Boolean
) {
    if (ehMaratona && estado.vidasMaratona <= 0) {
        audioVoz.tocarDerrota()
        estado.falarEAtualizarPainel(
            audioVoz,
            "Fim do Show do Python! Suas vidas acabaram com ${estado.acertosSessao} acertos.",
            "Suas 3 vidas acabaram no Show do Python! Você acertou ${estado.acertosSessao} questões. Continue praticando para dar o troco na Luna!"
        )
        estado.telaAtual = SubTelaAcademia.MENU_PRINCIPAL
        return
    }

    if (estado.indiceQuestaoAtual < estado.listaQuestoesAtivas.lastIndex) {
        estado.indiceQuestaoAtual++
        estado.indiceOpcaoSelecionada = 0
        estado.questaoJaRespondida = false
        anunciarQuestaoAtual(estado, audioVoz)
    } else {
        if (!ehMaratona) {
            val mod = estado.moduloAtual()
            motorJogo.marcarModuloConcluido(mod.numero, estado.acertosSessao, estado.listaQuestoesAtivas.size)
        }
        audioVoz.tocarCampeao()
        val msgFinal = "Parabéns, ${estado.nomeJogador}! Você completou o desafio acertando ${estado.acertosSessao} de ${estado.listaQuestoesAtivas.size} questões! Patente atual: ${motorJogo.obterPatenteAtual()} com ${motorJogo.xpTotal} XP!"
        estado.falarEAtualizarPainel(audioVoz, msgFinal, msgFinal)
        estado.telaAtual = if (ehMaratona) SubTelaAcademia.MENU_PRINCIPAL else SubTelaAcademia.HUB_MODULO
    }
}

@Composable
private fun RenderDetetiveBugs(
    estado: EstadoAcademiaPython,
    motorJogo: MotorJogoPython,
    audioVoz: GerenciadorAudioVoz
) {
    val bugs = RepositorioModulos.todosOsBugs
    val caso = bugs[estado.indiceBugAtual]

    Card(
        colors = CardDefaults.cardColors(containerColor = CorCartao),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "🕵️ Detetive de Bugs (${estado.indiceBugAtual + 1}/${bugs.size}) — ${caso.arquivoOrigem}",
                color = CorVerdeSucesso,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
            Text(
                text = caso.titulo,
                color = CorAmareloDestaque,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 4.dp)
            )
            Text(
                text = caso.historiaBug,
                color = Color.White,
                fontSize = 16.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            BlocoCodigoPython(
                titulo = "🐛 Código com Bug:",
                codigo = caso.codigoComBug,
                aoOuvirCodigo = {
                    audioVoz.falar("Código com bug: ${audioVoz.converterCodigoParaFala(caso.codigoComBug)}", interromper = true)
                }
            )
            if (estado.bugJaRespondido) {
                BlocoCodigoPython(
                    titulo = "✅ Código Corrigido:",
                    codigo = caso.codigoCorrigido,
                    aoOuvirCodigo = {
                        audioVoz.falar("Código corrigido: ${audioVoz.converterCodigoParaFala(caso.codigoCorrigido)}", interromper = true)
                    }
                )
            }
            Text(
                text = "❓ ${caso.pergunta}",
                color = CorAmareloDestaque,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 6.dp)
            )
        }
    }

    caso.opcoes.forEachIndexed { idx, op ->
        BotaoAcessivelGrande(
            texto = "${idx + 1}) $op",
            descricaoAcessivel = "Alternativa ${idx + 1}: $op",
            selecionado = idx == estado.indiceOpcaoBug,
            corFundo = if (estado.bugJaRespondido && idx == caso.indiceCorreto) Color(0xFF15803D) else CorAzulBotao,
            aoClicar = {
                estado.indiceOpcaoBug = idx
                if (!estado.bugJaRespondido) {
                    estado.bugJaRespondido = true
                    if (idx == caso.indiceCorreto) {
                        motorJogo.adicionarXp(caso.pontosXp)
                        audioVoz.tocarAcerto()
                        estado.falarEAtualizarPainel(
                            audioVoz,
                            "✅ Bug Resolvido! +${caso.pontosXp} XP! ${caso.licaoMoral}",
                            "Exato, Detetive ${estado.nomeJogador}! Você resolveu o bug e ganhou ${caso.pontosXp} pontos de experiência! ${caso.licaoMoral}"
                        )
                    } else {
                        audioVoz.tocarErro()
                        estado.falarEAtualizarPainel(
                            audioVoz,
                            "❌ Quase! A causa real era: ${caso.opcoes[caso.indiceCorreto]}. ${caso.licaoMoral}",
                            "Quase lá! A resposta certa era: ${caso.opcoes[caso.indiceCorreto]}. ${caso.licaoMoral}"
                        )
                    }
                } else {
                    estado.indiceBugAtual = (estado.indiceBugAtual + 1) % bugs.size
                    estado.indiceOpcaoBug = 0
                    estado.bugJaRespondido = false
                    val c = bugs[estado.indiceBugAtual]
                    estado.falarEAtualizarPainel(
                        audioVoz,
                        "🕵️ ${c.titulo} (${c.arquivoOrigem})",
                        "Detetive de Bugs. ${c.titulo}, do arquivo ${c.arquivoOrigem}. Pergunta: ${c.pergunta}."
                    )
                }
            }
        )
    }

    BarraNavegacaoAcessivel(
        rotuloAnterior = "◀ Anterior",
        rotuloProximo = "Próximo ▶",
        rotuloConfirmar = "✔ Investigar",
        rotuloOuvir = "🔊 Ouvir Caso",
        aoAnterior = {
            estado.indiceBugAtual = (estado.indiceBugAtual - 1 + bugs.size) % bugs.size
            estado.indiceOpcaoBug = 0
            estado.bugJaRespondido = false
            audioVoz.tocarNavegar()
            val c = bugs[estado.indiceBugAtual]
            estado.falarEAtualizarPainel(audioVoz, "🕵️ ${c.titulo} (${c.arquivoOrigem})", "Detetive de Bugs. ${c.titulo}.")
        },
        aoProximo = {
            estado.indiceBugAtual = (estado.indiceBugAtual + 1) % bugs.size
            estado.indiceOpcaoBug = 0
            estado.bugJaRespondido = false
            audioVoz.tocarNavegar()
            val c = bugs[estado.indiceBugAtual]
            estado.falarEAtualizarPainel(audioVoz, "🕵️ ${c.titulo} (${c.arquivoOrigem})", "Detetive de Bugs. ${c.titulo}.")
        },
        aoConfirmar = {
            estado.indiceOpcaoBug = (estado.indiceOpcaoBug + 1) % caso.opcoes.size
            audioVoz.tocarNavegar()
            audioVoz.falar("Alternativa ${estado.indiceOpcaoBug + 1}: ${caso.opcoes[estado.indiceOpcaoBug]}", interromper = true)
        },
        aoOuvir = {
            estado.falarEAtualizarPainel(audioVoz, "🕵️ ${caso.titulo} (${caso.arquivoOrigem})", "Detetive de Bugs. ${caso.titulo}, do arquivo ${caso.arquivoOrigem}. Pergunta: ${caso.pergunta}.")
        },
        aoVoltar = {
            estado.telaAtual = SubTelaAcademia.MENU_PRINCIPAL
            estado.anunciarMenuPrincipal(audioVoz, motorJogo)
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RenderJogoForca(
    estado: EstadoAcademiaPython,
    motorJogo: MotorJogoPython,
    audioVoz: GerenciadorAudioVoz
) {
    val letraAtual = estado.partidaForca.alfabeto[estado.indiceLetraForca]

    Card(
        colors = CardDefaults.cardColors(containerColor = CorCartao),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "🎮 Minijogo da Forca Python (baseado em forca.py)",
                color = CorVerdeSucesso,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
            Text(
                text = "Palavra (${estado.partidaForca.palavraSecreta.length} letras): ${estado.partidaForca.mostrarProgressoVisual()}",
                color = CorAmareloDestaque,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            Text(
                text = "💡 Dica da Luna: ${estado.partidaForca.dica}",
                color = Color.White,
                fontSize = 16.sp
            )
            Text(
                text = "❤️ Tentativas restantes: ${estado.partidaForca.tentativasRestantes} | Letras erradas: ${estado.partidaForca.letrasErradas.joinToString(", ").uppercase()}",
                color = Color(0xFFFCA5A5),
                fontSize = 15.sp,
                modifier = Modifier.padding(top = 6.dp)
            )
        }
    }

    Spacer(modifier = Modifier.height(8.dp))

    Text(
        text = "Teclado de Letras (Toque duas vezes para chutar diretamente):",
        color = CorAmareloDestaque,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
    )

    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        estado.partidaForca.alfabeto.forEach { letraChar ->
            val foiTentada = letraChar in estado.partidaForca.letrasCertas || letraChar in estado.partidaForca.letrasErradas
            val estaCerta = letraChar in estado.partidaForca.letrasCertas
            val corFundo = when {
                estaCerta -> Color(0xFF15803D)
                foiTentada -> Color(0xFF7F1D1D)
                else -> Color(0xFF1D4ED8)
            }
            Button(
                onClick = {
                    chutarLetra(letraChar, estado, motorJogo, audioVoz)
                },
                enabled = !foiTentada && !estado.partidaForca.venceu() && !estado.partidaForca.perdeu(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = corFundo,
                    disabledContainerColor = if (estaCerta) Color(0xFF166534) else Color(0xFF334155)
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .size(width = 44.dp, height = 48.dp)
                    .semantics {
                        contentDescription = when {
                            estaCerta -> "Letra ${letraChar.uppercaseChar()}, já acertada"
                            foiTentada -> "Letra ${letraChar.uppercaseChar()}, já errada"
                            else -> "Letra ${letraChar.uppercaseChar()}. Toque duas vezes para chutar."
                        }
                        role = Role.Button
                        onClick(label = "Chutar letra ${letraChar.uppercaseChar()}") {
                            chutarLetra(letraChar, estado, motorJogo, audioVoz)
                            true
                        }
                    }
            ) {
                Text(
                    text = letraChar.uppercase(),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(10.dp))

    BotaoAcessivelGrande(
        texto = "🔄 Sortear Nova Palavra de Python",
        descricaoAcessivel = "Sortear uma nova palavra no jogo da forca. Toque duas vezes para sortear.",
        corFundo = CorRoxoLuna,
        aoClicar = {
            estado.partidaForca = PartidaForcaPython()
            estado.indiceLetraForca = 0
            audioVoz.tocarConfirmar()
            estado.falarEAtualizarPainel(
                audioVoz,
                "Nova palavra sorteada com ${estado.partidaForca.palavraSecreta.length} letras! Dica: ${estado.partidaForca.dica}",
                "Nova palavra sorteada com ${estado.partidaForca.palavraSecreta.length} letras! Dica: ${estado.partidaForca.dica}"
            )
        }
    )

    BarraNavegacaoAcessivel(
        rotuloOuvir = "🔊 Ouvir Palavra",
        aoAnterior = {
            estado.indiceLetraForca = (estado.indiceLetraForca - 1 + estado.partidaForca.alfabeto.size) % estado.partidaForca.alfabeto.size
            audioVoz.tocarNavegar()
            val l = estado.partidaForca.alfabeto[estado.indiceLetraForca].uppercaseChar()
            audioVoz.falar("Letra $l", interromper = true)
        },
        aoProximo = {
            estado.indiceLetraForca = (estado.indiceLetraForca + 1) % estado.partidaForca.alfabeto.size
            audioVoz.tocarNavegar()
            val l = estado.partidaForca.alfabeto[estado.indiceLetraForca].uppercaseChar()
            audioVoz.falar("Letra $l", interromper = true)
        },
        aoConfirmar = null,
        aoOuvir = {
            audioVoz.falar(
                "A palavra tem ${estado.partidaForca.palavraSecreta.length} letras. Progresso: ${estado.partidaForca.mostrarProgressoFalado()}. Tentativas restantes: ${estado.partidaForca.tentativasRestantes}. Dica: ${estado.partidaForca.dica}",
                interromper = true
            )
        },
        aoVoltar = {
            estado.telaAtual = SubTelaAcademia.MENU_PRINCIPAL
            estado.anunciarMenuPrincipal(audioVoz, motorJogo)
        }
    )
}

private fun chutarLetra(
    letra: Char,
    estado: EstadoAcademiaPython,
    motorJogo: MotorJogoPython,
    audioVoz: GerenciadorAudioVoz
) {
    if (estado.partidaForca.venceu() || estado.partidaForca.perdeu()) {
        estado.partidaForca = PartidaForcaPython()
        estado.indiceLetraForca = 0
        estado.falarEAtualizarPainel(
            audioVoz,
            "Iniciando nova partida! Palavra de ${estado.partidaForca.palavraSecreta.length} letras. Dica: ${estado.partidaForca.dica}",
            "Iniciando nova partida! Palavra de ${estado.partidaForca.palavraSecreta.length} letras. Dica: ${estado.partidaForca.dica}"
        )
        return
    }
    val resultado = estado.partidaForca.tentarLetra(letra)
    if (estado.partidaForca.venceu()) {
        motorJogo.adicionarXp(50)
        audioVoz.tocarCampeao()
    } else if (letra in estado.partidaForca.palavraSecreta) {
        audioVoz.tocarAcerto()
    } else {
        audioVoz.tocarErro()
    }
    estado.falarEAtualizarPainel(audioVoz, resultado, resultado)
}

@Composable
private fun RenderAjuda(
    estado: EstadoAcademiaPython,
    audioVoz: GerenciadorAudioVoz
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CorCartao),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "📖 Sobre a Academia Python da Luna",
                color = CorAmareloDestaque,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "• 8 Módulos didáticos completos com explicações em voz pela Luna:\n" +
                    "  1. Variáveis, input, float e f-strings\n" +
                    "  2. Porcentagem e Operador Resto %\n" +
                    "  3. Condicionais if, elif, else e Ternário\n" +
                    "  4. Lógica Financeira e Regra dos 30%\n" +
                    "  5. Funções def, while, for, list e set (forca.py)\n" +
                    "  6. Dicionários, JSON e validações\n" +
                    "  7. Banco de Dados SQLite, CRUD e WhatsApp\n" +
                    "  8. Testes Automatizados com Pytest\n\n" +
                    "• Acessibilidade: Navegue por toque ou use os botões Anterior, Próximo, Confirmar e Ouvir. No teclado físico ou TalkBack, use as setas ou Enter.",
                color = Color.White,
                fontSize = 16.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }

    Spacer(modifier = Modifier.height(10.dp))

    BotaoAcessivelGrande(
        texto = "↩ Voltar ao Menu da Academia",
        descricaoAcessivel = "Voltar ao menu da academia python",
        corFundo = CorRoxoLuna,
        aoClicar = {
            estado.telaAtual = SubTelaAcademia.MENU_PRINCIPAL
        }
    )
}
