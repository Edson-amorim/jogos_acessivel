package com.edson.academiapython

import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.edson.academiapython.audio.GerenciadorAudioVoz
import com.edson.academiapython.modelo.ModuloEnsino
import com.edson.academiapython.modelo.QuestaoDesafio
import com.edson.academiapython.modulos.RepositorioModulos
import com.edson.academiapython.motor.MotorJogoPython
import com.edson.academiapython.motor.PartidaForcaPython
import com.edson.academiapython.ui.BarraNavegacaoAcessivel
import com.edson.academiapython.ui.BlocoCodigoPython
import com.edson.academiapython.ui.BotaoAcessivelGrande
import com.edson.academiapython.ui.CorAmareloDestaque
import com.edson.academiapython.ui.CorAzulBotao
import com.edson.academiapython.ui.CorCartao
import com.edson.academiapython.ui.CorFundoEscuro
import com.edson.academiapython.ui.CorRoxoLuna
import com.edson.academiapython.ui.CorVerdeSucesso
import com.edson.academiapython.ui.CorVermelhoErro

enum class TelaAcademia {
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

data class ItemMenu(val id: Int, val titulo: String, val fala: String)

class MainActivity : ComponentActivity() {

    private lateinit var audioVoz: GerenciadorAudioVoz
    private lateinit var motorJogo: MotorJogoPython

    private var telaAtual by mutableStateOf(TelaAcademia.MENU_PRINCIPAL)
    private var ultimaFalaLuna by mutableStateOf(
        "Bem-vindo à Academia Python da Luna, Edson! Todos os seus códigos da pasta edson_python transformados em um curso e jogo interativo!"
    )

    private var indiceMenuPrincipal by mutableIntStateOf(0)
    private var indiceModuloSelecionado by mutableIntStateOf(0)
    private var indiceHubModulo by mutableIntStateOf(0)

    // Índices internos do Módulo
    private var indiceTopicoTeoria by mutableIntStateOf(0)
    private var indiceArquivoCodigo by mutableIntStateOf(0)
    private var indiceLinhaExplicada by mutableIntStateOf(0)
    private var indiceSimulador by mutableIntStateOf(0)

    // Estado do Quiz do Módulo e Maratona
    private var listaQuestoesAtivas by mutableStateOf<List<QuestaoDesafio>>(emptyList())
    private var indiceQuestaoAtual by mutableIntStateOf(0)
    private var indiceOpcaoSelecionada by mutableIntStateOf(0)
    private var questaoJaRespondida by mutableStateOf(false)
    private var acertouUltimaQuestao by mutableStateOf(false)
    private var acertosSessao by mutableIntStateOf(0)
    private var vidasMaratona by mutableIntStateOf(3)

    // Estado do Detetive de Bugs
    private var indiceBugAtual by mutableIntStateOf(0)
    private var indiceOpcaoBug by mutableIntStateOf(0)
    private var bugJaRespondido by mutableStateOf(false)

    // Estado do Minijogo da Forca Python
    private var partidaForca by mutableStateOf(PartidaForcaPython())
    private var indiceLetraForca by mutableIntStateOf(0)

    private val opcoesMenuPrincipal = listOf(
        ItemMenu(
            1,
            "1 - Trilha Completa de Aulas (8 Módulos de Python)",
            "Opção 1: Trilha Completa de Aulas com 8 Módulos baseados nos seus códigos da pasta edson python."
        ),
        ItemMenu(
            2,
            "2 - Show do Python contra a Luna (Desafio Geral)",
            "Opção 2: Show do Python contra a Luna. Maratona de perguntas valendo XP e Patentes!"
        ),
        ItemMenu(
            3,
            "3 - Detetive de Bugs Reais (Depurando seus Códigos)",
            "Opção 3: Detetive de Bugs Reais. Descubra e conserte os erros reais de erro despensa, empréstimo e pytest!"
        ),
        ItemMenu(
            4,
            "4 - Minijogo da Forca Python (baseado no seu forca.py)",
            "Opção 4: Jogar o Minijogo da Forca Python com termos de programação e acessibilidade."
        ),
        ItemMenu(
            5,
            "5 - Alterar Velocidade da Voz da Professora Luna",
            "Opção 5: Alterar a velocidade da voz da Professora Luna."
        ),
        ItemMenu(
            6,
            "6 - Ajuda e Como Estudar com Acessibilidade",
            "Opção 6: Ajuda e instruções de uso."
        ),
        ItemMenu(
            7,
            "7 - Sair da Academia Python",
            "Opção 7: Sair do aplicativo."
        )
    )

    private val opcoesHubModulo = listOf(
        ItemMenu(1, "1 - Aula Teórica Explicada pela Luna", "Opção 1: Ouvir e ler a Aula Teórica passo a passo com dicas da Luna."),
        ItemMenu(2, "2 - Seus Códigos Explicados Linha por Linha", "Opção 2: Inspecionar os seus arquivos Python originais explicados linha por linha."),
        ItemMenu(3, "3 - Laboratório Prático (Simular Execução)", "Opção 3: Laboratório Prático. Veja e ouça a simulação do código rodando com dados reais."),
        ItemMenu(4, "4 - Desafio Quiz do Módulo (Valendo XP)", "Opção 4: Responder o Desafio Quiz deste módulo para ganhar pontos de experiência."),
        ItemMenu(5, "5 - Voltar para a Lista dos 8 Módulos", "Opção 5: Voltar para a lista de módulos.")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        motorJogo = MotorJogoPython(this)
        audioVoz = GerenciadorAudioVoz(this) {
            anunciarMenuPrincipal()
        }

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = CorFundoEscuro
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        CabecalhoStatusAluno()
                        Spacer(modifier = Modifier.height(8.dp))
                        when (telaAtual) {
                            TelaAcademia.MENU_PRINCIPAL -> RenderMenuPrincipal()
                            TelaAcademia.LISTA_MODULOS -> RenderListaModulos()
                            TelaAcademia.HUB_MODULO -> RenderHubModulo()
                            TelaAcademia.AULA_TEORIA -> RenderAulaTeoria()
                            TelaAcademia.CODIGO_LINHA_A_LINHA -> RenderCodigoLinhaALinha()
                            TelaAcademia.LABORATORIO_SIMULADOR -> RenderLaboratorioSimulador()
                            TelaAcademia.QUIZ_MODULO -> RenderQuiz(ehMaratona = false)
                            TelaAcademia.MARATONA_LUNA -> RenderQuiz(ehMaratona = true)
                            TelaAcademia.DETETIVE_BUGS -> RenderDetetiveBugs()
                            TelaAcademia.JOGO_FORCA_PYTHON -> RenderJogoForca()
                            TelaAcademia.AJUDA -> RenderAjuda()
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        audioVoz.liberar()
        super.onDestroy()
    }

    private fun falarEAtualizarPainel(textoVisual: String, textoFalado: String = textoVisual, interromper: Boolean = true) {
        ultimaFalaLuna = textoVisual
        audioVoz.falar(textoFalado, interromper = interromper)
    }

    private fun moduloAtual(): ModuloEnsino {
        return RepositorioModulos.todosOsModulos[indiceModuloSelecionado]
    }

    private fun anunciarMenuPrincipal() {
        val op = opcoesMenuPrincipal[indiceMenuPrincipal]
        val patente = motorJogo.obterPatenteAtual()
        falarEAtualizarPainel(
            textoVisual = "Professora Luna: Bem-vindo, Edson! Patente: $patente (${motorJogo.xpTotal} XP). ${op.titulo}",
            textoFalado = "Academia Python da Luna. Sua patente atual é $patente, com ${motorJogo.xpTotal} pontos de experiência. ${op.fala}"
        )
    }

    @Composable
    private fun CabecalhoStatusAluno() {
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
                    text = "Aluno: Edson | Patente: ${motorJogo.obterPatenteAtual()} | XP: ${motorJogo.xpTotal}",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 2.dp)
                )
                Text(
                    text = ultimaFalaLuna,
                    color = Color(0xFFBAE6FD),
                    fontSize = 15.sp,
                    modifier = Modifier
                        .padding(top = 6.dp)
                        .semantics { liveRegion = LiveRegionMode.Polite }
                )
            }
        }
    }

    @Composable
    private fun RenderMenuPrincipal() {
        Text(
            text = "Menu Principal (Toque ou navegue com Anterior / Próximo)",
            color = CorAmareloDestaque,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 6.dp)
        )

        opcoesMenuPrincipal.forEachIndexed { idx, item ->
            BotaoAcessivelGrande(
                texto = item.titulo,
                descricaoAcessivel = "${item.titulo}. Toque duas vezes para entrar diretamente.",
                selecionado = idx == indiceMenuPrincipal,
                aoClicar = {
                    indiceMenuPrincipal = idx
                    audioVoz.tocarConfirmar()
                    executarOpcaoMenuPrincipal(item.id)
                }
            )
        }

        BarraNavegacaoAcessivel(
            aoAnterior = {
                indiceMenuPrincipal = (indiceMenuPrincipal - 1 + opcoesMenuPrincipal.size) % opcoesMenuPrincipal.size
                audioVoz.tocarNavegar()
                falarEAtualizarPainel(opcoesMenuPrincipal[indiceMenuPrincipal].titulo, opcoesMenuPrincipal[indiceMenuPrincipal].fala)
            },
            aoProximo = {
                indiceMenuPrincipal = (indiceMenuPrincipal + 1) % opcoesMenuPrincipal.size
                audioVoz.tocarNavegar()
                falarEAtualizarPainel(opcoesMenuPrincipal[indiceMenuPrincipal].titulo, opcoesMenuPrincipal[indiceMenuPrincipal].fala)
            },
            aoConfirmar = null,
            aoOuvir = {
                anunciarMenuPrincipal()
            },
            aoVoltar = {
                audioVoz.falar("Você já está no menu principal.", interromper = true)
            }
        )
    }

    private fun executarOpcaoMenuPrincipal(id: Int) {
        when (id) {
            1 -> {
                telaAtual = TelaAcademia.LISTA_MODULOS
                val mod = moduloAtual()
                falarEAtualizarPainel(
                    "Escolha um dos 8 Módulos de Python. Selecionado: ${mod.titulo}",
                    "Lista dos 8 Módulos de Python. Selecionado: ${mod.resumoFalado}"
                )
            }
            2 -> iniciarMaratonaLuna()
            3 -> {
                indiceBugAtual = 0
                indiceOpcaoBug = 0
                bugJaRespondido = false
                telaAtual = TelaAcademia.DETETIVE_BUGS
                anunciarBugAtual()
            }
            4 -> {
                partidaForca = PartidaForcaPython()
                indiceLetraForca = 0
                telaAtual = TelaAcademia.JOGO_FORCA_PYTHON
                falarEAtualizarPainel(
                    "Jogo da Forca Python iniciado! A palavra tem ${partidaForca.palavraSecreta.length} letras. Dica: ${partidaForca.dica}",
                    "Jogo da Forca Python iniciado! A palavra tem ${partidaForca.palavraSecreta.length} letras. Dica da Luna: ${partidaForca.dica}. Letra selecionada: A."
                )
            }
            5 -> {
                val novaVel = audioVoz.alternarVelocidadeFala()
                ultimaFalaLuna = "Velocidade da voz ajustada para $novaVel."
            }
            6 -> {
                telaAtual = TelaAcademia.AJUDA
                falarEAtualizarPainel(
                    "Ajuda da Academia Python da Luna. Todos os 28 arquivos da pasta C:\\edson_python foram organizados em 8 módulos didáticos.",
                    "Ajuda da Academia Python da Luna. Você pode tocar diretamente em qualquer botão ou usar os botões Anterior, Próximo, Confirmar e Ouvir na parte inferior da tela. No leitor de códigos, você pode ouvir cada linha de Python explicada pela Luna!"
                )
            }
            7 -> {
                audioVoz.falar("Até a próxima aula de Python, Edson!", interromper = true)
                finish()
            }
        }
    }

    @Composable
    private fun RenderListaModulos() {
        val modulos = RepositorioModulos.todosOsModulos
        Text(
            text = "Trilha de Ensino (8 Módulos da pasta C:\\edson_python)",
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
                descricaoAcessivel = "${mod.resumoFalado}. Melhor pontuação: $melhor de $totalQ.",
                selecionado = idx == indiceModuloSelecionado,
                aoClicar = {
                    indiceModuloSelecionado = idx
                    indiceHubModulo = 0
                    audioVoz.tocarConfirmar()
                    telaAtual = TelaAcademia.HUB_MODULO
                    anunciarHubModulo()
                }
            )
        }

        BarraNavegacaoAcessivel(
            aoAnterior = {
                indiceModuloSelecionado = (indiceModuloSelecionado - 1 + modulos.size) % modulos.size
                audioVoz.tocarNavegar()
                val mod = moduloAtual()
                falarEAtualizarPainel(mod.titulo, mod.resumoFalado)
            },
            aoProximo = {
                indiceModuloSelecionado = (indiceModuloSelecionado + 1) % modulos.size
                audioVoz.tocarNavegar()
                val mod = moduloAtual()
                falarEAtualizarPainel(mod.titulo, mod.resumoFalado)
            },
            aoConfirmar = null,
            aoOuvir = {
                val mod = moduloAtual()
                falarEAtualizarPainel(mod.titulo, mod.resumoFalado)
            },
            aoVoltar = {
                telaAtual = TelaAcademia.MENU_PRINCIPAL
                anunciarMenuPrincipal()
            }
        )
    }

    private fun anunciarHubModulo() {
        val mod = moduloAtual()
        val op = opcoesHubModulo[indiceHubModulo]
        falarEAtualizarPainel(
            "${mod.titulo} — ${mod.subtitulo}",
            "${mod.titulo}. Arquivos estudados: ${mod.arquivosOrigem.joinToString(", ")}. ${op.fala}"
        )
    }

    @Composable
    private fun RenderHubModulo() {
        val mod = moduloAtual()
        Text(
            text = mod.titulo,
            color = CorAmareloDestaque,
            fontSize = 19.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Arquivos de C:\\edson_python: ${mod.arquivosOrigem.joinToString(", ")}",
            color = CorVerdeSucesso,
            fontSize = 15.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        opcoesHubModulo.forEachIndexed { idx, item ->
            BotaoAcessivelGrande(
                texto = item.titulo,
                descricaoAcessivel = item.fala,
                selecionado = idx == indiceHubModulo,
                aoClicar = {
                    indiceHubModulo = idx
                    audioVoz.tocarConfirmar()
                    executarOpcaoHubModulo(item.id)
                }
            )
        }

        BarraNavegacaoAcessivel(
            aoAnterior = {
                indiceHubModulo = (indiceHubModulo - 1 + opcoesHubModulo.size) % opcoesHubModulo.size
                audioVoz.tocarNavegar()
                falarEAtualizarPainel(opcoesHubModulo[indiceHubModulo].titulo, opcoesHubModulo[indiceHubModulo].fala)
            },
            aoProximo = {
                indiceHubModulo = (indiceHubModulo + 1) % opcoesHubModulo.size
                audioVoz.tocarNavegar()
                falarEAtualizarPainel(opcoesHubModulo[indiceHubModulo].titulo, opcoesHubModulo[indiceHubModulo].fala)
            },
            aoConfirmar = null,
            aoOuvir = { anunciarHubModulo() },
            aoVoltar = {
                telaAtual = TelaAcademia.LISTA_MODULOS
                falarEAtualizarPainel("Lista de Módulos. Selecionado: ${mod.titulo}", mod.resumoFalado)
            }
        )
    }

    private fun executarOpcaoHubModulo(id: Int) {
        val mod = moduloAtual()
        when (id) {
            1 -> {
                indiceTopicoTeoria = 0
                telaAtual = TelaAcademia.AULA_TEORIA
                anunciarTopicoTeoria()
            }
            2 -> {
                indiceArquivoCodigo = 0
                indiceLinhaExplicada = 0
                telaAtual = TelaAcademia.CODIGO_LINHA_A_LINHA
                anunciarLinhaCodigo()
            }
            3 -> {
                indiceSimulador = 0
                telaAtual = TelaAcademia.LABORATORIO_SIMULADOR
                anunciarSimulador()
            }
            4 -> {
                listaQuestoesAtivas = mod.questoes
                indiceQuestaoAtual = 0
                indiceOpcaoSelecionada = 0
                questaoJaRespondida = false
                acertosSessao = 0
                telaAtual = TelaAcademia.QUIZ_MODULO
                anunciarQuestaoAtual()
            }
            5 -> {
                telaAtual = TelaAcademia.LISTA_MODULOS
                falarEAtualizarPainel("Voltando para a lista dos 8 Módulos.", mod.resumoFalado)
            }
        }
    }

    private fun anunciarTopicoTeoria() {
        val mod = moduloAtual()
        val topico = mod.topicosTeoria[indiceTopicoTeoria]
        falarEAtualizarPainel(
            "${topico.titulo} (${indiceTopicoTeoria + 1}/${mod.topicosTeoria.size})",
            "${topico.explicacaoFalada} ${topico.dicaDaLuna}"
        )
    }

    @Composable
    private fun RenderAulaTeoria() {
        val mod = moduloAtual()
        val topicos = mod.topicosTeoria
        val topico = topicos[indiceTopicoTeoria]

        Card(
            colors = CardDefaults.cardColors(containerColor = CorCartao),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "${mod.titulo} — Tópico ${indiceTopicoTeoria + 1} de ${topicos.size}",
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
            rotuloAnterior = "◀ Tópico Anterior",
            rotuloProximo = "Próximo Tópico ▶",
            rotuloConfirmar = "💡 Ouvir Dica",
            rotuloOuvir = "🔊 Repetir Aula",
            aoAnterior = {
                if (indiceTopicoTeoria > 0) {
                    indiceTopicoTeoria--
                    audioVoz.tocarNavegar()
                    anunciarTopicoTeoria()
                } else {
                    audioVoz.falar("Você já está no primeiro tópico deste módulo.", interromper = true)
                }
            },
            aoProximo = {
                if (indiceTopicoTeoria < topicos.lastIndex) {
                    indiceTopicoTeoria++
                    audioVoz.tocarNavegar()
                    anunciarTopicoTeoria()
                } else {
                    motorJogo.adicionarXp(15)
                    audioVoz.tocarAcerto()
                    falarEAtualizarPainel(
                        "Parabéns! Você concluiu a teoria deste módulo e ganhou 15 XP! Vamos ver seus códigos linha por linha!",
                        "Parabéns, Edson! Você concluiu todos os tópicos teóricos deste módulo e ganhou 15 pontos de experiência! Abrindo agora seus códigos explicados linha por linha."
                    )
                    indiceArquivoCodigo = 0
                    indiceLinhaExplicada = 0
                    telaAtual = TelaAcademia.CODIGO_LINHA_A_LINHA
                }
            },
            aoConfirmar = {
                audioVoz.tocarDica()
                audioVoz.falar(topico.dicaDaLuna, interromper = true)
            },
            aoOuvir = { anunciarTopicoTeoria() },
            aoVoltar = {
                telaAtual = TelaAcademia.HUB_MODULO
                anunciarHubModulo()
            }
        )
    }

    private fun anunciarLinhaCodigo() {
        val mod = moduloAtual()
        val arq = mod.codigosOriginais[indiceArquivoCodigo]
        val linha = arq.linhasExplicadas[indiceLinhaExplicada]
        falarEAtualizarPainel(
            "Arquivo ${arq.nomeArquivo} — Passo ${indiceLinhaExplicada + 1}/${arq.linhasExplicadas.size}: ${linha.explicacaoDidatica}",
            "Arquivo ${arq.nomeArquivo}. ${linha.leituraFalada} Explicação da Luna: ${linha.explicacaoDidatica}"
        )
    }

    @Composable
    private fun RenderCodigoLinhaALinha() {
        val mod = moduloAtual()
        val codigos = mod.codigosOriginais
        val arq = codigos[indiceArquivoCodigo]
        val linha = arq.linhasExplicadas[indiceLinhaExplicada]

        if (codigos.size > 1) {
            BotaoAcessivelGrande(
                texto = "📂 Trocar Arquivo (${indiceArquivoCodigo + 1}/${codigos.size}): ${arq.nomeArquivo}",
                descricaoAcessivel = "Trocar entre os arquivos Python deste módulo. Arquivo atual: ${arq.nomeArquivo}",
                corFundo = CorRoxoLuna,
                aoClicar = {
                    indiceArquivoCodigo = (indiceArquivoCodigo + 1) % codigos.size
                    indiceLinhaExplicada = 0
                    audioVoz.tocarNavegar()
                    anunciarLinhaCodigo()
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
                    titulo = "Código Completo Original (C:\\edson_python\\${arq.nomeArquivo}):",
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
                            text = "🔍 Explicação Linha por Linha (${indiceLinhaExplicada + 1} de ${arq.linhasExplicadas.size}):",
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
                if (indiceLinhaExplicada > 0) {
                    indiceLinhaExplicada--
                    audioVoz.tocarNavegar()
                    anunciarLinhaCodigo()
                } else {
                    audioVoz.falar("Já está na primeira linha explicada deste arquivo.", interromper = true)
                }
            },
            aoProximo = {
                if (indiceLinhaExplicada < arq.linhasExplicadas.lastIndex) {
                    indiceLinhaExplicada++
                    audioVoz.tocarNavegar()
                    anunciarLinhaCodigo()
                } else {
                    motorJogo.adicionarXp(15)
                    audioVoz.tocarAcerto()
                    audioVoz.falar("Você revisou todas as linhas deste código e ganhou 15 pontos de experiência!", interromper = true)
                }
            },
            aoConfirmar = {
                indiceArquivoCodigo = (indiceArquivoCodigo + 1) % codigos.size
                indiceLinhaExplicada = 0
                audioVoz.tocarNavegar()
                anunciarLinhaCodigo()
            },
            aoOuvir = { anunciarLinhaCodigo() },
            aoVoltar = {
                telaAtual = TelaAcademia.HUB_MODULO
                anunciarHubModulo()
            }
        )
    }

    private fun anunciarSimulador() {
        val mod = moduloAtual()
        val sim = mod.cenariosSimulador[indiceSimulador]
        falarEAtualizarPainel(
            sim.titulo,
            "Laboratório Prático. ${sim.titulo}. Entradas simuladas: ${sim.entradasSimuladas}. Saída na tela: ${sim.saidaConsole}. Explicação da Luna: ${sim.explicacaoExecucao}"
        )
    }

    @Composable
    private fun RenderLaboratorioSimulador() {
        val mod = moduloAtual()
        val cenarios = mod.cenariosSimulador
        val sim = cenarios[indiceSimulador]

        Card(
            colors = CardDefaults.cardColors(containerColor = CorCartao),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "🧪 Laboratório Prático (${indiceSimulador + 1}/${cenarios.size})",
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
            rotuloAnterior = "◀ Cenário Anterior",
            rotuloProximo = "Próximo Cenário ▶",
            rotuloConfirmar = "🎯 Ir para o Quiz",
            rotuloOuvir = "🔊 Ouvir Simulação",
            aoAnterior = {
                indiceSimulador = (indiceSimulador - 1 + cenarios.size) % cenarios.size
                audioVoz.tocarNavegar()
                anunciarSimulador()
            },
            aoProximo = {
                indiceSimulador = (indiceSimulador + 1) % cenarios.size
                audioVoz.tocarNavegar()
                anunciarSimulador()
            },
            aoConfirmar = {
                listaQuestoesAtivas = mod.questoes
                indiceQuestaoAtual = 0
                indiceOpcaoSelecionada = 0
                questaoJaRespondida = false
                acertosSessao = 0
                telaAtual = TelaAcademia.QUIZ_MODULO
                anunciarQuestaoAtual()
            },
            aoOuvir = { anunciarSimulador() },
            aoVoltar = {
                telaAtual = TelaAcademia.HUB_MODULO
                anunciarHubModulo()
            }
        )
    }

    private fun iniciarMaratonaLuna() {
        listaQuestoesAtivas = RepositorioModulos.todasAsQuestoes().shuffled().take(10)
        indiceQuestaoAtual = 0
        indiceOpcaoSelecionada = 0
        questaoJaRespondida = false
        acertosSessao = 0
        vidasMaratona = 3
        telaAtual = TelaAcademia.MARATONA_LUNA
        val q = listaQuestoesAtivas[0]
        falarEAtualizarPainel(
            "Show do Python contra a Luna! 10 questões sorteadas, 3 vidas! Questão 1: ${q.enunciado}",
            "Show do Python contra a Luna iniciado! Você tem 3 vidas para encarar 10 desafios de todos os módulos! Questão 1: ${q.enunciado}. Opção 1: ${q.opcoes[0]}"
        )
    }

    private fun anunciarQuestaoAtual() {
        val q = listaQuestoesAtivas[indiceQuestaoAtual]
        val op = q.opcoes[indiceOpcaoSelecionada]
        falarEAtualizarPainel(
            "Questão ${indiceQuestaoAtual + 1}/${listaQuestoesAtivas.size}: ${q.enunciado}",
            "Questão ${indiceQuestaoAtual + 1} de ${listaQuestoesAtivas.size}. ${q.enunciado}. Alternativa ${indiceOpcaoSelecionada + 1}: $op"
        )
    }

    private fun responderQuestao(indiceEscolhido: Int, ehMaratona: Boolean) {
        val q = listaQuestoesAtivas[indiceQuestaoAtual]
        indiceOpcaoSelecionada = indiceEscolhido
        questaoJaRespondida = true

        if (indiceEscolhido == q.indiceCorreto) {
            acertouUltimaQuestao = true
            acertosSessao++
            motorJogo.adicionarXp(q.pontosXp)
            audioVoz.tocarAcerto()
            val elogio = motorJogo.sortearElogio()
            falarEAtualizarPainel(
                "✅ CORRETO! +${q.pontosXp} XP! ${q.explicacaoResposta}",
                "Resposta correta! $elogio Você ganhou ${q.pontosXp} pontos de experiência! ${q.explicacaoResposta}"
            )
        } else {
            acertouUltimaQuestao = false
            if (ehMaratona) vidasMaratona--
            audioVoz.tocarErro()
            val incentivo = motorJogo.sortearIncentivo()
            val correta = q.opcoes[q.indiceCorreto]
            falarEAtualizarPainel(
                "❌ Resposta incorreta. A correta era: $correta. ${q.explicacaoResposta}",
                "$incentivo A resposta correta era: $correta. Explicação: ${q.explicacaoResposta}"
            )
        }
    }

    private fun avancarProximaQuestao(ehMaratona: Boolean) {
        if (ehMaratona && vidasMaratona <= 0) {
            audioVoz.tocarDerrota()
            falarEAtualizarPainel(
                "Fim do Show do Python! Suas vidas acabaram com $acertosSessao acertos.",
                "Suas 3 vidas acabaram no Show do Python! Você acertou $acertosSessao questões. Continue estudando os módulos para dar o troco na Luna!"
            )
            telaAtual = TelaAcademia.MENU_PRINCIPAL
            return
        }

        if (indiceQuestaoAtual < listaQuestoesAtivas.lastIndex) {
            indiceQuestaoAtual++
            indiceOpcaoSelecionada = 0
            questaoJaRespondida = false
            anunciarQuestaoAtual()
        } else {
            if (!ehMaratona) {
                val mod = moduloAtual()
                motorJogo.marcarModuloConcluido(mod.numero, acertosSessao, listaQuestoesAtivas.size)
            }
            audioVoz.tocarCampeao()
            val msgFinal = "Parabéns, Edson! Você completou o desafio acertando $acertosSessao de ${listaQuestoesAtivas.size} questões! Sua patente agora é ${motorJogo.obterPatenteAtual()} com ${motorJogo.xpTotal} XP!"
            falarEAtualizarPainel(msgFinal, msgFinal)
            telaAtual = if (ehMaratona) TelaAcademia.MENU_PRINCIPAL else TelaAcademia.HUB_MODULO
        }
    }

    @Composable
    private fun RenderQuiz(ehMaratona: Boolean) {
        val q = listaQuestoesAtivas[indiceQuestaoAtual]

        Card(
            colors = CardDefaults.cardColors(containerColor = CorCartao),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                val cabecalho = if (ehMaratona) {
                    "🏆 Show do Python (${indiceQuestaoAtual + 1}/${listaQuestoesAtivas.size}) | ❤️ Vidas: $vidasMaratona | Acertos: $acertosSessao"
                } else {
                    "🎯 Desafio Módulo ${q.moduloNumero} (${indiceQuestaoAtual + 1}/${listaQuestoesAtivas.size}) | Acertos: $acertosSessao"
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
                !questaoJaRespondida -> CorAzulBotao
                idx == q.indiceCorreto -> Color(0xFF15803D)
                idx == indiceOpcaoSelecionada -> CorVermelhoErro
                else -> Color(0xFF334155)
            }
            BotaoAcessivelGrande(
                texto = "${idx + 1}) $textoOpcao",
                descricaoAcessivel = "Alternativa ${idx + 1}: $textoOpcao",
                selecionado = idx == indiceOpcaoSelecionada,
                corFundo = corBotao,
                aoClicar = {
                    if (!questaoJaRespondida) {
                        responderQuestao(idx, ehMaratona)
                    } else {
                        avancarProximaQuestao(ehMaratona)
                    }
                }
            )
        }

        if (questaoJaRespondida) {
            BotaoAcessivelGrande(
                texto = "▶ Avançar para a Próxima Questão",
                descricaoAcessivel = "Avançar para a próxima questão",
                corFundo = Color(0xFF15803D),
                aoClicar = { avancarProximaQuestao(ehMaratona) }
            )
        }

        BarraNavegacaoAcessivel(
            rotuloAnterior = "◀ Opção Anterior",
            rotuloProximo = "Próxima Opção ▶",
            rotuloConfirmar = if (questaoJaRespondida) "▶ Próxima Questão" else "✔ Responder",
            rotuloOuvir = "🔊 Ouvir Pergunta",
            aoAnterior = {
                indiceOpcaoSelecionada = (indiceOpcaoSelecionada - 1 + q.opcoes.size) % q.opcoes.size
                audioVoz.tocarNavegar()
                audioVoz.falar("Alternativa ${indiceOpcaoSelecionada + 1}: ${q.opcoes[indiceOpcaoSelecionada]}", interromper = true)
            },
            aoProximo = {
                indiceOpcaoSelecionada = (indiceOpcaoSelecionada + 1) % q.opcoes.size
                audioVoz.tocarNavegar()
                audioVoz.falar("Alternativa ${indiceOpcaoSelecionada + 1}: ${q.opcoes[indiceOpcaoSelecionada]}", interromper = true)
            },
            aoConfirmar = {
                if (!questaoJaRespondida) {
                    responderQuestao(indiceOpcaoSelecionada, ehMaratona)
                } else {
                    avancarProximaQuestao(ehMaratona)
                }
            },
            aoOuvir = { anunciarQuestaoAtual() },
            aoVoltar = {
                telaAtual = if (ehMaratona) TelaAcademia.MENU_PRINCIPAL else TelaAcademia.HUB_MODULO
                if (ehMaratona) anunciarMenuPrincipal() else anunciarHubModulo()
            }
        )
    }

    private fun anunciarBugAtual() {
        val caso = RepositorioModulos.todosOsBugs[indiceBugAtual]
        falarEAtualizarPainel(
            "🕵️ ${caso.titulo} (${caso.arquivoOrigem})",
            "Detetive de Bugs. ${caso.titulo}, do arquivo ${caso.arquivoOrigem}. História: ${caso.historiaBug} Pergunta: ${caso.pergunta}. Alternativa 1: ${caso.opcoes[0]}"
        )
    }

    @Composable
    private fun RenderDetetiveBugs() {
        val bugs = RepositorioModulos.todosOsBugs
        val caso = bugs[indiceBugAtual]

        Card(
            colors = CardDefaults.cardColors(containerColor = CorCartao),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "🕵️ Detetive de Bugs (${indiceBugAtual + 1}/${bugs.size}) — ${caso.arquivoOrigem}",
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
                if (bugJaRespondido) {
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
                selecionado = idx == indiceOpcaoBug,
                corFundo = if (bugJaRespondido && idx == caso.indiceCorreto) Color(0xFF15803D) else CorAzulBotao,
                aoClicar = {
                    indiceOpcaoBug = idx
                    if (!bugJaRespondido) {
                        bugJaRespondido = true
                        if (idx == caso.indiceCorreto) {
                            motorJogo.adicionarXp(caso.pontosXp)
                            audioVoz.tocarAcerto()
                            falarEAtualizarPainel(
                                "✅ Bug Resolvido! +${caso.pontosXp} XP! ${caso.licaoMoral}",
                                "Exato, Detetive Edson! Você resolveu o bug e ganhou ${caso.pontosXp} pontos de experiência! ${caso.licaoMoral}"
                            )
                        } else {
                            audioVoz.tocarErro()
                            falarEAtualizarPainel(
                                "❌ Quase! A causa real era: ${caso.opcoes[caso.indiceCorreto]}. ${caso.licaoMoral}",
                                "Quase lá! A resposta certa era: ${caso.opcoes[caso.indiceCorreto]}. ${caso.licaoMoral}"
                            )
                        }
                    } else {
                        indiceBugAtual = (indiceBugAtual + 1) % bugs.size
                        indiceOpcaoBug = 0
                        bugJaRespondido = false
                        anunciarBugAtual()
                    }
                }
            )
        }

        BarraNavegacaoAcessivel(
            rotuloAnterior = "◀ Caso Anterior",
            rotuloProximo = "Próximo Caso ▶",
            rotuloConfirmar = "✔ Investigar Opção",
            rotuloOuvir = "🔊 Ouvir Caso",
            aoAnterior = {
                indiceBugAtual = (indiceBugAtual - 1 + bugs.size) % bugs.size
                indiceOpcaoBug = 0
                bugJaRespondido = false
                audioVoz.tocarNavegar()
                anunciarBugAtual()
            },
            aoProximo = {
                indiceBugAtual = (indiceBugAtual + 1) % bugs.size
                indiceOpcaoBug = 0
                bugJaRespondido = false
                audioVoz.tocarNavegar()
                anunciarBugAtual()
            },
            aoConfirmar = {
                indiceOpcaoBug = (indiceOpcaoBug + 1) % caso.opcoes.size
                audioVoz.tocarNavegar()
                audioVoz.falar("Alternativa ${indiceOpcaoBug + 1}: ${caso.opcoes[indiceOpcaoBug]}", interromper = true)
            },
            aoOuvir = { anunciarBugAtual() },
            aoVoltar = {
                telaAtual = TelaAcademia.MENU_PRINCIPAL
                anunciarMenuPrincipal()
            }
        )
    }

    @Composable
    private fun RenderJogoForca() {
        val letraAtual = partidaForca.alfabeto[indiceLetraForca]

        Card(
            colors = CardDefaults.cardColors(containerColor = CorCartao),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "🎮 Jogo da Forca Python (baseado em C:\\edson_python\\forca.py)",
                    color = CorVerdeSucesso,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Text(
                    text = "Palavra (${partidaForca.palavraSecreta.length} letras): ${partidaForca.mostrarProgressoVisual()}",
                    color = CorAmareloDestaque,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                Text(
                    text = "💡 Dica da Luna: ${partidaForca.dica}",
                    color = Color.White,
                    fontSize = 16.sp
                )
                Text(
                    text = "❤️ Tentativas restantes: ${partidaForca.tentativasRestantes} | Letras erradas: ${partidaForca.letrasErradas.joinToString(", ").uppercase()}",
                    color = Color(0xFFFCA5A5),
                    fontSize = 15.sp,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        BotaoAcessivelGrande(
            texto = "🔤 Chutar Letra Selecionada: ${letraAtual.uppercaseChar()}",
            descricaoAcessivel = "Chutar a letra ${letraAtual.uppercaseChar()}",
            selecionado = true,
            corFundo = Color(0xFF15803D),
            aoClicar = { chutarLetraForca(letraAtual) }
        )

        BotaoAcessivelGrande(
            texto = "🔄 Sortear Nova Palavra de Python",
            descricaoAcessivel = "Sortear uma nova palavra no jogo da forca",
            corFundo = CorRoxoLuna,
            aoClicar = {
                partidaForca = PartidaForcaPython()
                indiceLetraForca = 0
                audioVoz.tocarConfirmar()
                falarEAtualizarPainel(
                    "Nova palavra sorteada com ${partidaForca.palavraSecreta.length} letras! Dica: ${partidaForca.dica}",
                    "Nova palavra sorteada com ${partidaForca.palavraSecreta.length} letras! Dica: ${partidaForca.dica}"
                )
            }
        )

        BarraNavegacaoAcessivel(
            rotuloAnterior = "◀ Letra Anterior",
            rotuloProximo = "Próxima Letra ▶",
            rotuloConfirmar = "✔ Tentar ${letraAtual.uppercaseChar()}",
            rotuloOuvir = "🔊 Ouvir Progresso",
            aoAnterior = {
                indiceLetraForca = (indiceLetraForca - 1 + partidaForca.alfabeto.size) % partidaForca.alfabeto.size
                audioVoz.tocarNavegar()
                val l = partidaForca.alfabeto[indiceLetraForca].uppercaseChar()
                audioVoz.falar("Letra $l", interromper = true)
            },
            aoProximo = {
                indiceLetraForca = (indiceLetraForca + 1) % partidaForca.alfabeto.size
                audioVoz.tocarNavegar()
                val l = partidaForca.alfabeto[indiceLetraForca].uppercaseChar()
                audioVoz.falar("Letra $l", interromper = true)
            },
            aoConfirmar = { chutarLetraForca(letraAtual) },
            aoOuvir = {
                audioVoz.falar(
                    "A palavra tem ${partidaForca.palavraSecreta.length} letras. Progresso: ${partidaForca.mostrarProgressoFalado()}. Tentativas restantes: ${partidaForca.tentativasRestantes}. Dica: ${partidaForca.dica}",
                    interromper = true
                )
            },
            aoVoltar = {
                telaAtual = TelaAcademia.MENU_PRINCIPAL
                anunciarMenuPrincipal()
            }
        )
    }

    private fun chutarLetraForca(letra: Char) {
        if (partidaForca.venceu() || partidaForca.perdeu()) {
            partidaForca = PartidaForcaPython()
            indiceLetraForca = 0
            falarEAtualizarPainel(
                "Iniciando nova partida! Palavra de ${partidaForca.palavraSecreta.length} letras. Dica: ${partidaForca.dica}",
                "Iniciando nova partida! Palavra de ${partidaForca.palavraSecreta.length} letras. Dica: ${partidaForca.dica}"
            )
            return
        }
        val resultado = partidaForca.tentarLetra(letra)
        if (partidaForca.venceu()) {
            motorJogo.adicionarXp(50)
            audioVoz.tocarCampeao()
        } else if (letra in partidaForca.palavraSecreta) {
            audioVoz.tocarAcerto()
        } else {
            audioVoz.tocarErro()
        }
        falarEAtualizarPainel(resultado, resultado)
    }

    @Composable
    private fun RenderAjuda() {
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
                    text = "• Todos os seus códigos de C:\\edson_python foram organizados em 8 Módulos progressivos:\n" +
                        "  1. Variáveis, input, float e f-strings (soma.py, ex11.py)\n" +
                        "  2. Porcentagem e Operador Resto % (ex12.py, ex13.py, calculadora_do_bando.py)\n" +
                        "  3. Condicionais if, elif, else e Ternário (calc.py, calculadora.py, loja_da_luna.py)\n" +
                        "  4. Lógica Financeira e Regra dos 30% (financiamento.py, emprestimo)\n" +
                        "  5. Funções def, while, for, list e set (forca.py)\n" +
                        "  6. Dicionários, JSON, try/except e CPF válido (controli.py, gerar_cadastro.py)\n" +
                        "  7. Banco de Dados SQLite, CRUD, ALTER TABLE e WhatsApp (cadastro.py, estoque.py)\n" +
                        "  8. Testes Automatizados com Pytest e Módulos (test_soma.py, funcao_soma.py)\n\n" +
                        "• Acessibilidade Total: Use o TalkBack tocando nos cartões ou use a barra inferior com Anterior, Próximo, Confirmar e Ouvir!",
                    color = Color.White,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }

        BotaoAcessivelGrande(
            texto = "↩ Voltar ao Menu Principal",
            descricaoAcessivel = "Voltar ao menu principal",
            corFundo = CorRoxoLuna,
            aoClicar = {
                telaAtual = TelaAcademia.MENU_PRINCIPAL
                anunciarMenuPrincipal()
            }
        )
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_BACK, KeyEvent.KEYCODE_ESCAPE -> {
                if (telaAtual != TelaAcademia.MENU_PRINCIPAL) {
                    telaAtual = when (telaAtual) {
                        TelaAcademia.AULA_TEORIA,
                        TelaAcademia.CODIGO_LINHA_A_LINHA,
                        TelaAcademia.LABORATORIO_SIMULADOR,
                        TelaAcademia.QUIZ_MODULO -> TelaAcademia.HUB_MODULO
                        TelaAcademia.HUB_MODULO -> TelaAcademia.LISTA_MODULOS
                        else -> TelaAcademia.MENU_PRINCIPAL
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
                audioVoz.alternarVelocidadeFala()
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }
}
