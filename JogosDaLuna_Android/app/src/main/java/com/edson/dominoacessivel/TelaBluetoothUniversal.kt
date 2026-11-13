package com.edson.dominoacessivel

import android.annotation.SuppressLint
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@SuppressLint("MissingPermission")
@Composable
fun TelaBluetoothUniversal(
    gerenciadorBt: GerenciadorBluetoothDuelo,
    nomeJogador: String,
    audioVoz: GerenciadorAudioVoz,
    aoIniciarJogoBluetooth: (JogoBluetooth) -> Unit,
    aoVoltar: () -> Unit
) {
    val scroll = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .padding(16.dp)
            .verticalScroll(scroll),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "📡 DUELOS VIA BLUETOOTH",
            color = Color(0xFFFFD54F),
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center
        )

        Text(
            text = "Jogue entre 2 celulares pareados sem precisar de internet!",
            color = Color(0xFFE2E8F0),
            fontSize = 15.sp,
            textAlign = TextAlign.Center
        )

        // Card de Status
        Card(
            colors = CardDefaults.cardColors(
                containerColor = when (gerenciadorBt.estado) {
                    EstadoBluetoothDuelo.CONECTADO -> Color(0xFF14532D)
                    EstadoBluetoothDuelo.AGUARDANDO_CONEXAO, EstadoBluetoothDuelo.CONECTANDO -> Color(0xFF713F12)
                    else -> Color(0xFF1E293B)
                }
            ),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, Color(0xFF38BDF8), RoundedCornerShape(14.dp))
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "STATUS DA CONEXÃO:",
                    color = Color(0xFF38BDF8),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = gerenciadorBt.mensagemStatus,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
                if (gerenciadorBt.nomeDispositivoConectado != null) {
                    Text(
                        text = "📱 Oponente: ${gerenciadorBt.nomeDispositivoConectado}",
                        color = Color(0xFFFEF08A),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // 1. Escolha do Jogo
        Text(
            text = "1. ESCOLHA O JOGO PARA DUELAR:",
            color = Color(0xFFFFD54F),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.Start)
        )

        JogoBluetooth.entries.forEach { jogo ->
            val selecionado = gerenciadorBt.jogoSelecionado == jogo
            val corFundo = if (selecionado) Color(0xFF1D4ED8) else Color(0xFF1E293B)
            val corBorda = if (selecionado) Color(0xFFFFD54F) else Color(0xFF475569)

            Button(
                onClick = {
                    gerenciadorBt.jogoSelecionado = jogo
                    val msg = "${jogo.titulo} selecionado para o duelo via Bluetooth. ${jogo.descricao}"
                    audioVoz.falar(msg, interromper = true)
                    if (gerenciadorBt.estado == EstadoBluetoothDuelo.CONECTADO) {
                        gerenciadorBt.enviarComando("SELECIONAR_JOGO:${jogo.name}")
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = corFundo, contentColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(if (selecionado) 2.5.dp else 1.dp, corBorda, RoundedCornerShape(12.dp))
                    .semantics(mergeDescendants = true) {
                        contentDescription = "${if (selecionado) "Selecionado: " else ""}${jogo.titulo}. ${jogo.descricao}"
                        role = Role.Button
                    }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = "${jogo.icone} ${jogo.titulo} ${if (selecionado) "✅" else ""}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (selecionado) Color(0xFFFFD54F) else Color.White
                    )
                    Text(
                        text = jogo.descricao,
                        fontSize = 13.sp,
                        color = Color(0xFFCBD5E1)
                    )
                }
            }
        }

        // Se já estiver conectado, mostra botão de iniciar o jogo escolhido!
        if (gerenciadorBt.estado == EstadoBluetoothDuelo.CONECTADO) {
            Button(
                onClick = {
                    val j = gerenciadorBt.jogoSelecionado
                    audioVoz.tocarConfirmar()
                    audioVoz.falar("Iniciando ${j.titulo} via Bluetooth!", interromper = true)
                    gerenciadorBt.enviarComando("INICIAR_PARTIDA:${j.name}")
                    aoIniciarJogoBluetooth(j)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF15803D), contentColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .border(2.dp, Color(0xFFFFD54F), RoundedCornerShape(12.dp))
                    .semantics(mergeDescendants = true) {
                        contentDescription = "Iniciar ${gerenciadorBt.jogoSelecionado.titulo} via Bluetooth agora!"
                        role = Role.Button
                    }
            ) {
                Text(
                    text = "🔥 INICIAR ${gerenciadorBt.jogoSelecionado.titulo.uppercase()} AGORA!",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFFFFD54F)
                )
            }
        }

        // 2. Ações de Conexão
        Text(
            text = "2. CONEXÃO BLUETOOTH:",
            color = Color(0xFFFFD54F),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.Start)
        )

        Button(
            onClick = {
                gerenciadorBt.iniciarServidor {
                    audioVoz.tocarConfirmar()
                    audioVoz.falar("Amigo conectado via Bluetooth com sucesso!", interromper = true)
                }
                val msg = "Aguardando o celular do seu amigo se conectar à sua sala..."
                audioVoz.falar(msg, interromper = true)
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .semantics(mergeDescendants = true) {
                    contentDescription = "Criar Sala Bluetooth para o outro celular se conectar."
                    role = Role.Button
                }
        ) {
            Text("📡 CRIAR SALA (Aguardar Conexão)", fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }

        Button(
            onClick = {
                gerenciadorBt.atualizarDispositivosPareados()
                val qtd = gerenciadorBt.dispositivosPareados.size
                val msg = "Aparelhos pareados atualizados. $qtd encontrados."
                audioVoz.falar(msg, interromper = true)
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text("🔄 Atualizar Aparelhos Pareados", fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }

        // 3. Lista de Aparelhos Pareados
        Text(
            text = "3. CELULARES PAREADOS (Toque para Conectar):",
            color = Color(0xFFFFD54F),
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.Start)
        )

        val pareados = gerenciadorBt.dispositivosPareados
        if (pareados.isEmpty()) {
            Text(
                text = "Nenhum celular pareado encontrado. Pareie os celulares nas Configurações Bluetooth do Android antes de duelar.",
                color = Color(0xFF94A3B8),
                fontSize = 13.sp
            )
        } else {
            pareados.forEach { dev ->
                val nomeDev = try { dev.name ?: "Celular" } catch (_: Exception) { "Celular" }
                Button(
                    onClick = {
                        audioVoz.falar("Conectando ao celular de $nomeDev...", interromper = true)
                        gerenciadorBt.conectarADispositivo(dev) {
                            audioVoz.tocarConfirmar()
                            audioVoz.falar("Conectado ao celular de $nomeDev com sucesso!", interromper = true)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E3A8A)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .semantics(mergeDescendants = true) {
                            contentDescription = "Conectar ao celular de $nomeDev"
                            role = Role.Button
                        }
                ) {
                    Text("📲 Conectar a $nomeDev", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = aoVoltar,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF475569)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .semantics(mergeDescendants = true) {
                    contentDescription = "Voltar ao Menu Principal"
                    role = Role.Button
                }
        ) {
            Text("⬅️ Voltar ao Menu Principal", fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }
    }
}
