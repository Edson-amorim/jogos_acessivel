package com.edson.academiapython.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val CorFundoEscuro = Color(0xFF0D1B2A)
val CorCartao = Color(0xFF1B263B)
val CorCodigoFundo = Color(0xFF0A0F18)
val CorAmareloDestaque = Color(0xFFFFD166)
val CorVerdeSucesso = Color(0xFF2EC4B6)
val CorAzulBotao = Color(0xFF1D4ED8)
val CorRoxoLuna = Color(0xFF6D28D9)
val CorVermelhoErro = Color(0xFFDC2626)

@Composable
fun BotaoAcessivelGrande(
    texto: String,
    descricaoAcessivel: String = texto,
    selecionado: Boolean = false,
    corFundo: Color = CorAzulBotao,
    aoClicar: () -> Unit
) {
    val corReal = if (selecionado) Color(0xFFD97706) else corFundo
    val borda = if (selecionado) 3.dp else 1.dp
    val corBorda = if (selecionado) CorAmareloDestaque else Color.White.copy(alpha = 0.35f)

    Button(
        onClick = aoClicar,
        colors = ButtonDefaults.buttonColors(containerColor = corReal),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .border(borda, corBorda, RoundedCornerShape(12.dp))
            .semantics {
                contentDescription = descricaoAcessivel
                role = Role.Button
                stateDescription = if (selecionado) "Selecionado" else ""
            }
    ) {
        Text(
            text = texto,
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Start,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp)
        )
    }
}

@Composable
fun BarraNavegacaoAcessivel(
    rotuloAnterior: String = "◀ Anterior",
    rotuloProximo: String = "Próximo ▶",
    rotuloConfirmar: String = "✔ Confirmar",
    rotuloOuvir: String = "🔊 Ouvir",
    aoAnterior: () -> Unit,
    aoProximo: () -> Unit,
    aoConfirmar: (() -> Unit)? = null,
    aoOuvir: () -> Unit,
    aoVoltar: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = aoAnterior,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                modifier = Modifier
                    .weight(1f)
                    .semantics { contentDescription = rotuloAnterior }
            ) {
                Text(rotuloAnterior, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
            Button(
                onClick = aoProximo,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                modifier = Modifier
                    .weight(1f)
                    .semantics { contentDescription = rotuloProximo }
            ) {
                Text(rotuloProximo, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (aoConfirmar != null) {
                Button(
                    onClick = aoConfirmar,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF15803D)),
                    modifier = Modifier
                        .weight(1f)
                        .semantics { contentDescription = rotuloConfirmar }
                ) {
                    Text(rotuloConfirmar, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
            Button(
                onClick = aoOuvir,
                colors = ButtonDefaults.buttonColors(containerColor = CorRoxoLuna),
                modifier = Modifier
                    .weight(1f)
                    .semantics { contentDescription = rotuloOuvir }
            ) {
                Text(rotuloOuvir, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
            Button(
                onClick = aoVoltar,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF991B1B)),
                modifier = Modifier
                    .weight(1f)
                    .semantics { contentDescription = "Voltar para a tela anterior" }
            ) {
                Text("↩ Voltar", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }
    }
}

@Composable
fun BlocoCodigoPython(
    titulo: String,
    codigo: String,
    aoOuvirCodigo: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CorCodigoFundo),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CorAmareloDestaque.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
            .padding(vertical = 4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = titulo,
                color = CorAmareloDestaque,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
            Text(
                text = codigo,
                color = Color(0xFFE2E8F0),
                fontFamily = FontFamily.Monospace,
                fontSize = 15.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CorCodigoFundo)
                    .padding(vertical = 8.dp)
            )
            Button(
                onClick = aoOuvirCodigo,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F766E)),
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics {
                        contentDescription = "Ouvir leitura falada do código Python com símbolos explicados"
                    }
            ) {
                Text(
                    "🔊 Ouvir Código Python (Leitura Simbólica)",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
