package com.edson.dominoacessivel

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val CorFundoMesa = Color(0xFF0B3D24)
val CorPainelMesa = Color(0xFF082B19)
val CorPedra = Color(0xFFF5F0E6)
val CorBordaPedra = Color(0xFF222222)
val CorPonto = Color(0xFF111111)
val CorDestaque = Color(0xFFFFD700)
val CorTextoClaro = Color(0xFFF5F0E6)

private val PONTOS_PADRAO: Map<Int, List<Pair<Int, Int>>> = mapOf(
    0 to emptyList(),
    1 to listOf(1 to 1),
    2 to listOf(0 to 0, 2 to 2),
    3 to listOf(0 to 0, 1 to 1, 2 to 2),
    4 to listOf(0 to 0, 2 to 0, 0 to 2, 2 to 2),
    5 to listOf(0 to 0, 2 to 0, 1 to 1, 0 to 2, 2 to 2),
    6 to listOf(0 to 0, 2 to 0, 0 to 1, 2 to 1, 0 to 2, 2 to 2)
)

private fun DrawScope.desenharMetadePedra(
    x0: Float,
    y0: Float,
    x1: Float,
    y1: Float,
    valor: Int
) {
    val passoX = (x1 - x0) / 3f
    val passoY = (y1 - y0) / 3f
    val raio = minOf(passoX, passoY) * 0.28f
    for ((coluna, linha) in PONTOS_PADRAO[valor] ?: emptyList()) {
        val cx = x0 + passoX * (coluna + 0.5f)
        val cy = y0 + passoY * (linha + 0.5f)
        drawCircle(
            color = CorPonto,
            radius = raio,
            center = Offset(cx, cy)
        )
    }
}

@Composable
fun PecaDominoVisual(
    pedra: Pedra,
    destaque: Boolean = false,
    ladoDp: Dp = 38.dp,
    posicaoNaMao: Int? = null,
    totalNaMao: Int? = null,
    aoClicar: (() -> Unit)? = null
) {
    val larguraDp = ladoDp * 2
    val alturaDp = ladoDp
    val infoPosicao = if (posicaoNaMao != null && totalNaMao != null) {
        ", pedra $posicaoNaMao de $totalNaMao"
    } else {
        ""
    }
    val descricao = if (aoClicar != null) {
        "Pedra ${pedra.falarJuiz()}$infoPosicao. Toque duas vezes para jogar na mesa."
    } else {
        "Pedra na mesa: ${pedra.falarJuiz()}"
    }
    val estadoSelecao = if (destaque) {
        "Selecionada: ${pedra.falarJuiz()}"
    } else {
        ""
    }

    val mod = Modifier
        .size(width = larguraDp, height = alturaDp)
        .then(
            if (aoClicar != null) {
                Modifier.clickable(
                    onClickLabel = "Jogar pedra ${pedra.falarJuiz()} na mesa",
                    onClick = { aoClicar() }
                )
            } else {
                Modifier
            }
        )
        .semantics(mergeDescendants = true) {
            contentDescription = descricao
            if (aoClicar != null) {
                stateDescription = estadoSelecao
                selected = destaque
                role = Role.Button
            }
        }

    Canvas(modifier = mod) {
        val pad = 4.dp.toPx()
        val x = pad
        val y = pad
        val w = size.width - pad * 2
        val h = size.height - pad * 2
        val metade = w / 2f
        val canto = 6.dp.toPx()

        if (destaque) {
            drawRoundRect(
                color = CorDestaque,
                topLeft = Offset(1.dp.toPx(), 1.dp.toPx()),
                size = Size(size.width - 2.dp.toPx(), size.height - 2.dp.toPx()),
                cornerRadius = CornerRadius(canto + 2f, canto + 2f),
                style = Stroke(width = 3.5.dp.toPx())
            )
        }

        drawRoundRect(
            color = CorPedra,
            topLeft = Offset(x, y),
            size = Size(w, h),
            cornerRadius = CornerRadius(canto, canto)
        )

        drawRoundRect(
            color = CorBordaPedra,
            topLeft = Offset(x, y),
            size = Size(w, h),
            cornerRadius = CornerRadius(canto, canto),
            style = Stroke(width = 2.dp.toPx())
        )

        drawLine(
            color = CorBordaPedra,
            start = Offset(x + metade, y + 3.dp.toPx()),
            end = Offset(x + metade, y + h - 3.dp.toPx()),
            strokeWidth = 2.dp.toPx()
        )

        desenharMetadePedra(x, y, x + metade, y + h, pedra.a)
        desenharMetadePedra(x + metade, y, x + w, y + h, pedra.b)
    }
}

@Composable
fun CartaTrucoVisual(
    carta: CartaTruco,
    valorManilha: String? = null,
    destaque: Boolean = false,
    ocultarFace: Boolean = false,
    posicaoNaMao: Int? = null,
    totalNaMao: Int? = null,
    rotuloContexto: String? = null,
    aoClicar: (() -> Unit)? = null
) {
    val mostrarCoberta = carta.coberta || ocultarFace
    val falaCarta = if (mostrarCoberta) {
        "Carta coberta"
    } else {
        carta.falarCarta(valorManilha)
    }
    val infoPosicao = if (posicaoNaMao != null && totalNaMao != null) {
        ", carta $posicaoNaMao de $totalNaMao"
    } else {
        ""
    }
    val prefixo = rotuloContexto?.let { "$it: " } ?: ""
    val descricao = when {
        aoClicar != null -> "${prefixo}$falaCarta$infoPosicao. Toque duas vezes para jogar na mesa."
        else -> "$prefixo$falaCarta"
    }
    val estadoSelecao = if (destaque) {
        "Selecionada: $falaCarta"
    } else {
        ""
    }

    val ehManilha = !mostrarCoberta && valorManilha != null && carta.valor == valorManilha
    val corFundoCarta = when {
        mostrarCoberta -> Color(0xFF1B3B6F)
        ehManilha -> Color(0xFFFFF8DC)
        else -> CorPedra
    }
    val corTextoNaipe = when {
        mostrarCoberta -> CorDestaque
        carta.naipe.corVermelha -> Color(0xFFC62828)
        else -> Color(0xFF111111)
    }
    val corBorda = when {
        destaque -> CorDestaque
        ehManilha -> Color(0xFFD4AF37)
        else -> CorBordaPedra
    }
    val espessuraBorda = if (destaque) 3.5.dp else 2.dp

    Box(
        modifier = Modifier
            .size(width = 76.dp, height = 104.dp)
            .background(corFundoCarta, RoundedCornerShape(10.dp))
            .border(espessuraBorda, corBorda, RoundedCornerShape(10.dp))
            .then(
                if (aoClicar != null) {
                    Modifier.clickable(
                        onClickLabel = "Jogar carta $falaCarta na mesa",
                        onClick = { aoClicar() }
                    )
                } else {
                    Modifier
                }
            )
            .semantics(mergeDescendants = true) {
                contentDescription = descricao
                if (aoClicar != null) {
                    stateDescription = estadoSelecao
                    selected = destaque
                    role = Role.Button
                }
            }
            .padding(6.dp),
        contentAlignment = Alignment.Center
    ) {
        if (mostrarCoberta) {
            Text(
                text = "🂠\nCOBERTA",
                color = CorDestaque,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "${carta.valor}${carta.naipe.simbolo}",
                    color = corTextoNaipe,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = carta.naipe.nome,
                    color = corTextoNaipe,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )
                if (ehManilha) {
                    Text(
                        text = "★ ${carta.naipe.apelidoManilha}",
                        color = Color(0xFF8B6508),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
