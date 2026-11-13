package com.edson.dominoacessivel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import kotlin.random.Random

enum class TipoCartaYugioh {
    MONSTRO,
    MAGIA,
    ARMADILHA
}

data class CartaYugioh(
    val id: Int,
    val nome: String,
    val tipo: TipoCartaYugioh,
    val nivel: Int,
    val atk: Int,
    val def: Int,
    val codigoEfeito: String? = null,
    val descricaoEfeito: String = ""
) {
    val descricao: String get() = descricaoEfeito
    val tributosNecessarios: Int
        get() = when {
            tipo != TipoCartaYugioh.MONSTRO -> 0
            nivel <= 4 -> 0
            nivel in 5..6 -> 1
            nivel in 7..8 -> 2
            else -> 3
        }

    fun falarCompleta(): String {
        return when (tipo) {
            TipoCartaYugioh.MONSTRO -> {
                val tribTxt = when (tributosNecessarios) {
                    0 -> "Monstro de nível $nivel sem exigência de tributo."
                    1 -> "Monstro de nível $nivel exige 1 tributo na arena."
                    2 -> "Monstro lendário de nível $nivel exige 2 tributos na arena."
                    else -> "Monstro supremo de nível $nivel exige 3 tributos na arena."
                }
                val efTxt = if (descricaoEfeito.isNotBlank()) " Efeito: $descricaoEfeito." else ""
                "$nome. $tribTxt Ataque $atk, Defesa $def.$efTxt"
            }
            TipoCartaYugioh.MAGIA -> "Carta de Magia: $nome. Efeito: $descricaoEfeito"
            TipoCartaYugioh.ARMADILHA -> "Carta de Armadilha: $nome. Efeito: $descricaoEfeito"
        }
    }

    fun resumoCurto(): String {
        return when (tipo) {
            TipoCartaYugioh.MONSTRO -> "$nome ($atk ATK / $def DEF)"
            TipoCartaYugioh.MAGIA -> "Magia: $nome"
            TipoCartaYugioh.ARMADILHA -> "Armadilha: $nome"
        }
    }
}

data class MonstroEmCampoYugioh(
    val cartaBase: CartaYugioh,
    var atkAtual: Int,
    var defAtual: Int,
    var emAtaque: Boolean = true,
    var oculto: Boolean = false,
    var jaAtacouNesteTurno: Boolean = false,
    var mudouPosicaoNesteTurno: Boolean = false
) {
    fun falarStatus(visivelParaJogador: Boolean): String {
        if (oculto && !visivelParaJogador) {
            return "Monstro oculto virado para baixo em Posição de Defesa"
        }
        val pos = if (emAtaque) "Ataque com $atkAtual pontos" else "Defesa com $defAtual pontos"
        val desc = if (oculto) " (Defesa oculta)" else ""
        return "${cartaBase.nome} em $pos$desc"
    }
}

data class DeckPredefinidoYugioh(
    val id: Int,
    val nomeDeck: String,
    val personagem: String,
    val descricao: String,
    val idsCartasPadrao: List<Int>
)

object CatalogoYugiohLuna {

    val todasAsCartas: List<CartaYugioh> = listOf(
        // Monstros Clássicos de Yugi e Joey
        CartaYugioh(1, "Mago Negro", TipoCartaYugioh.MONSTRO, 7, 2500, 2100, "MAGO_NEGRO", "O mago definitivo em termos de ataque e defesa."),
        CartaYugioh(2, "Feiticeira Negra", TipoCartaYugioh.MONSTRO, 6, 2000, 1700, "FEITICEIRA", "Ganha 300 de ataque se o Mago Negro estiver no cemitério."),
        CartaYugioh(3, "Buster Blader", TipoCartaYugioh.MONSTRO, 7, 2600, 2300, "BUSTER", "O destruidor de espadas e matador de dragões."),
        CartaYugioh(4, "Rei Caveira", TipoCartaYugioh.MONSTRO, 6, 2500, 1200, null, "Monstro com poderes das trevas e ataque devastador."),
        CartaYugioh(5, "Kuriboh", TipoCartaYugioh.MONSTRO, 1, 300, 200, "KURIBOH", "Ao receber ataque direto, anula todo o dano da batalha."),
        CartaYugioh(6, "Elfo Místico", TipoCartaYugioh.MONSTRO, 4, 800, 2000, null, "Defesa élfica ancestral quase impenetrável."),
        CartaYugioh(7, "Soldado Gigante de Pedra", TipoCartaYugioh.MONSTRO, 3, 1300, 2000, null, "Muralha rochosa intransponível."),
        CartaYugioh(8, "Dragão Negro de Olhos Vermelhos", TipoCartaYugioh.MONSTRO, 7, 2400, 2000, null, "Um dragão feroz de olhos escarlates e fogo negro."),
        CartaYugioh(9, "Espadachim das Chamas", TipoCartaYugioh.MONSTRO, 5, 1800, 1600, null, "Mestre das espadas de fogo ardente."),
        CartaYugioh(10, "Mago do Tempo", TipoCartaYugioh.MONSTRO, 2, 500, 400, "MAGO_TEMPO", "Gira a roleta do tempo: se acertar, destrói todos os monstros inimigos!"),
        CartaYugioh(11, "Gearfried, o Cavaleiro de Ferro", TipoCartaYugioh.MONSTRO, 4, 1800, 1600, null, "Guerreiro com couraça de ferro indestrutível."),
        CartaYugioh(12, "Jinzo", TipoCartaYugioh.MONSTRO, 6, 2400, 1500, "JINZO", "Anula completamente todas as armadilhas da arena!"),

        // Monstros de Kaiba
        CartaYugioh(13, "Dragão Branco de Olhos Azuis", TipoCartaYugioh.MONSTRO, 8, 3000, 2500, null, "O lendário dragão supremo de destruição pura!"),
        CartaYugioh(14, "Senhor dos Dragões", TipoCartaYugioh.MONSTRO, 4, 1200, 1100, null, "Controlador supremo das bestas aladas."),
        CartaYugioh(15, "Vorse Raider", TipoCartaYugioh.MONSTRO, 4, 1900, 1200, null, "Besta guerreira impiedosa e veloz."),
        CartaYugioh(16, "La Jinn, o Gênio Místico da Lâmpada", TipoCartaYugioh.MONSTRO, 4, 1800, 1000, null, "Gênio sombrio que ataca das sombras."),

        // Monstros de Mai Valentine e Marik
        CartaYugioh(17, "Dama Harpia", TipoCartaYugioh.MONSTRO, 4, 1300, 1400, null, "Guerreira alada ágil com garras cortantes."),
        CartaYugioh(18, "Irmãs de Harpia", TipoCartaYugioh.MONSTRO, 6, 1950, 2100, null, "Trio de harpies com ataque aéreo conjunto."),
        CartaYugioh(19, "Giga Tech Wolf", TipoCartaYugioh.MONSTRO, 4, 1200, 1400, null, "Lobo cibernético de patrulha."),
        CartaYugioh(20, "Lava Golem", TipoCartaYugioh.MONSTRO, 8, 3000, 2500, "LAVA_GOLEM", "Gigante vulcânico que queima 500 LP do oponente a cada turno!"),

        // Magias Lendárias
        CartaYugioh(21, "Buraco Negro", TipoCartaYugioh.MAGIA, 0, 0, 0, "MAGIA_BURACO_NEGRO", "Destrói todos os monstros na arena instantaneamente!"),
        CartaYugioh(22, "Raigeki", TipoCartaYugioh.MAGIA, 0, 0, 0, "MAGIA_RAIGEKI", "Destrói o monstro do oponente com um raio devastador!"),
        CartaYugioh(23, "Renascimento do Monstro", TipoCartaYugioh.MAGIA, 0, 0, 0, "MAGIA_RENASCIMENTO", "Ressuscita o monstro mais forte de qualquer cemitério para a sua arena!"),
        CartaYugioh(24, "Pote da Ganância", TipoCartaYugioh.MAGIA, 0, 0, 0, "MAGIA_POTE", "Compre 2 cartas do seu baralho imediatamente!"),
        CartaYugioh(25, "Espadas da Luz Reveladora", TipoCartaYugioh.MAGIA, 0, 0, 0, "MAGIA_ESPADAS", "Revela monstros ocultos e impede o oponente de atacar por 2 turnos!"),
        CartaYugioh(26, "Tributo Torrencial (Magia de Inundação)", TipoCartaYugioh.MAGIA, 0, 0, 0, "MAGIA_HINOTAMA", "Lança Chama Final de Hinotama causando 600 de dano direto!"),
        CartaYugioh(27, "Poção Suprema de Cura", TipoCartaYugioh.MAGIA, 0, 0, 0, "MAGIA_CURA", "Recupera 1000 Pontos de Vida imediatamente!"),

        // Armadilhas Clássicas
        CartaYugioh(28, "Força Espelho (Mirror Force)", TipoCartaYugioh.ARMADILHA, 0, 0, 0, "ARM_FORCA_ESPELHO", "Quando o oponente ataca, reflete o golpe e destrói o monstro atacante!"),
        CartaYugioh(29, "Cilindro Mágico", TipoCartaYugioh.ARMADILHA, 0, 0, 0, "ARM_CILINDRO", "Bloqueia o ataque inimigo e devolve o valor de ataque como dano direto ao oponente!"),
        CartaYugioh(30, "Buraco Armadilha (Trap Hole)", TipoCartaYugioh.ARMADILHA, 0, 0, 0, "ARM_BURACO", "Destrói o monstro inimigo no momento em que ele ataca!"),
        CartaYugioh(31, "Parede de Espelhos", TipoCartaYugioh.ARMADILHA, 0, 0, 0, "ARM_PAREDE", "Corta o poder de ataque do monstro atacante pela metade!"),

        // Monstros do Pegasus e Toon World
        CartaYugioh(32, "Mundo Toon", TipoCartaYugioh.MAGIA, 0, 0, 0, "MAGIA_MUNDO_TOON", "Abre o Mundo Toon, recupera 800 LP e fortalece seus monstros Toon!"),
        CartaYugioh(33, "Manga Ryu-Ran Toon", TipoCartaYugioh.MONSTRO, 7, 2200, 2600, null, "Dragão Toon divertido que salta do livro ilustrado."),
        CartaYugioh(34, "Crânio Invocado Toon", TipoCartaYugioh.MONSTRO, 6, 2500, 1200, null, "Versão Toon animada com raio relâmpago."),
        CartaYugioh(35, "Saggi, o Palhaço das Trevas", TipoCartaYugioh.MONSTRO, 3, 600, 1500, null, "Servo fiel de Kaiba."),

        // Monstros de Bakura (Ocultismo e Destino)
        CartaYugioh(36, "Cavaleiro Sem Cabeça", TipoCartaYugioh.MONSTRO, 4, 1450, 1700, null, "Guerreiro sombrio errante."),
        CartaYugioh(37, "Necrofear das Trevas", TipoCartaYugioh.MONSTRO, 8, 2200, 2800, "NECROFEAR", "Alma maligna que amaldiçoa e reduz em 600 pontos o ataque do adversário."),
        CartaYugioh(38, "Diabound Kernel", TipoCartaYugioh.MONSTRO, 5, 1800, 1200, "DIABOUND", "Espírito ladrão milenar que rouba 500 de poder de combate!"),
        CartaYugioh(39, "Tabuleiro do Destino (Destiny Board)", TipoCartaYugioh.ARMADILHA, 0, 0, 0, "ARM_DESTINO_BAKURA", "Armadilha sombria que sela o ataque inimigo e drena 500 LP!"),

        // Monstros e Magias de Marik (Tormento das Trevas)
        CartaYugioh(40, "Dragão Revólver", TipoCartaYugioh.MONSTRO, 7, 2600, 2200, "REVOLVER_DRAGON", "Dispara canhões de fogo e causa 500 de dano direto imediato!"),
        CartaYugioh(41, "Máscara da Fraqueza", TipoCartaYugioh.ARMADILHA, 0, 0, 0, "ARM_PAREDE", "Reduz o ataque do inimigo pela metade com feitiço sombrio."),
        CartaYugioh(42, "O Dragão Alado de Rá", TipoCartaYugioh.MONSTRO, 10, 4000, 4000, "DEUS_EGIPCIO", "O Deus Egípcio dourado do Sol! Ataque e Defesa colossais de 4000 pontos!"),
        CartaYugioh(43, "Pesadelo de Marik", TipoCartaYugioh.MAGIA, 0, 0, 0, "MAGIA_PESADELO_MARIK", "Invoca sombras do Reino das Trevas e causa 800 de dano direto!"),

        // Monstros e Magias de Kaiba (Dragões e Controle)
        CartaYugioh(44, "Dragão Branco Supremo (Fusão)", TipoCartaYugioh.MONSTRO, 12, 4500, 3800, null, "A fusão suprema dos três Dragões Brancos! Poder colossal incomparável!"),
        CartaYugioh(45, "Controlador de Inimigos (Enemy Controller)", TipoCartaYugioh.MAGIA, 0, 0, 0, "MAGIA_CONTROLE_KAIBA", "Comando de joystick que reduz em 600 o ataque do monstro rival!"),
        CartaYugioh(46, "Vírus Esmagador de Cartas (Crush Card Virus)", TipoCartaYugioh.ARMADILHA, 0, 0, 0, "ARM_VIRUS_KAIBA", "Armadilha mortal que aniquila qualquer monstro atacante de 1500+ ATK e queima 500 LP!"),

        // Monstros de Chazz Princeton e Zane Truesdale (Yu-Gi-Oh! GX)
        CartaYugioh(47, "Ojama Amarelo", TipoCartaYugioh.MONSTRO, 2, 0, 1000, null, "Criaturinha bizarra e cômica do Chazz!"),
        CartaYugioh(48, "Ojama Preto", TipoCartaYugioh.MONSTRO, 2, 0, 1000, null, "Irmão Ojama que confunde o adversário com palhaçadas."),
        CartaYugioh(49, "Ojama Verde", TipoCartaYugioh.MONSTRO, 2, 0, 1000, null, "Terceiro irmão Ojama de cueca branca."),
        CartaYugioh(50, "Rei Ojama", TipoCartaYugioh.MONSTRO, 6, 0, 3000, null, "Muralha colossal com 3000 de Defesa pura que bloqueia a arena!"),
        CartaYugioh(51, "Dragão Cibernético (Cyber Dragon)", TipoCartaYugioh.MONSTRO, 5, 2100, 1600, null, "A máquina prateada de duelo de elite de Zane Truesdale!"),
        CartaYugioh(52, "Dragão Cibernético Final (Cyber End Dragon)", TipoCartaYugioh.MONSTRO, 10, 4000, 2800, null, "A fusão tríplice suprema de metal com 4000 de Ataque implacável!"),
        CartaYugioh(53, "Limiter Removal (Remoção de Limitador)", TipoCartaYugioh.MAGIA, 0, 0, 0, "MAGIA_CURA", "Injeta energia em máquinas e recupera 1000 Pontos de Vida de apoio."),

        // Monstros de Jaden Yuki (Heróis Elementais - Yu-Gi-Oh! GX)
        CartaYugioh(54, "Herói Elemental Neos", TipoCartaYugioh.MONSTRO, 7, 2500, 2000, "HEROI_NEOS", "O guerreiro cósmico de Neo Space que amplia a força para 2900 de Ataque!"),
        CartaYugioh(55, "Herói Elemental Sparkman", TipoCartaYugioh.MONSTRO, 4, 1600, 1400, null, "Herói guerreiro que dispara eletricidade pura."),
        CartaYugioh(56, "Herói Elemental Clayman", TipoCartaYugioh.MONSTRO, 4, 800, 2000, null, "Herói com couraça de barro sólido e 2000 de Defesa."),
        CartaYugioh(57, "Arranha-Céu (Skyscraper)", TipoCartaYugioh.MAGIA, 0, 0, 0, "MAGIA_SKYSCRAPER", "Arena de heróis que amplia o poder do seu monstro em 600 pontos de Ataque e Defesa!"),

        // Deuses Egípcios Adicionais
        CartaYugioh(58, "Slifer, o Dragão Celeste", TipoCartaYugioh.MONSTRO, 10, 4000, 4000, "DEUS_EGIPCIO", "O lendário Dragão Divino vermelho do Faraó Atem! Poder de combate colossal!"),
        CartaYugioh(59, "Obelisco, o Atormentador", TipoCartaYugioh.MONSTRO, 10, 4000, 4000, "DEUS_EGIPCIO", "O Deus Divino da força pura e punho esmagador! 4000 de Ataque!"),

        // As Cinco Partes Proibidas do Exodia
        CartaYugioh(60, "Exodia, o Proibido", TipoCartaYugioh.MONSTRO, 3, 1000, 1000, "EXODIA_CABECA", "A cabeça do guardião ancestral selado por correntes mágicas milenares."),
        CartaYugioh(61, "Braço Direito de O Proibido", TipoCartaYugioh.MONSTRO, 1, 200, 300, "EXODIA_PARTE", "O braço direito selado do Proibido."),
        CartaYugioh(62, "Braço Esquerdo de O Proibido", TipoCartaYugioh.MONSTRO, 1, 200, 300, "EXODIA_PARTE", "O braço esquerdo selado do Proibido."),
        CartaYugioh(63, "Perna Direita de O Proibido", TipoCartaYugioh.MONSTRO, 1, 200, 300, "EXODIA_PARTE", "A perna direita selada do Proibido."),
        CartaYugioh(64, "Perna Esquerda de O Proibido", TipoCartaYugioh.MONSTRO, 1, 200, 300, "EXODIA_PARTE", "A perna esquerda selada do Proibido.")
    )

    val decksPredefinidos: List<DeckPredefinidoYugioh> = listOf(
        DeckPredefinidoYugioh(
            id = 1,
            nomeDeck = "Deck 1: Yugi Muto (Magia das Trevas)",
            personagem = "Yugi Muto",
            descricao = "Deck equilibrado focado no Mago Negro, Feiticeira Negra e magias de controle como Buraco Negro e Espadas da Luz Reveladora.",
            idsCartasPadrao = listOf(1, 2, 4, 5, 6, 7, 11, 21, 22, 23, 24, 25, 27, 28, 29, 30, 31, 6, 7, 5)
        ),
        DeckPredefinidoYugioh(
            id = 2,
            nomeDeck = "Deck 2: Seto Kaiba (Poder dos Dragões)",
            personagem = "Seto Kaiba",
            descricao = "Deck ofensivo devastador com o temido Dragão Branco de Olhos Azuis (3000 ATK), Senhor dos Dragões e Vorse Raider.",
            idsCartasPadrao = listOf(13, 13, 14, 15, 15, 16, 16, 21, 22, 23, 24, 28, 29, 30, 31, 15, 16, 13, 14, 25)
        ),
        DeckPredefinidoYugioh(
            id = 3,
            nomeDeck = "Deck 3: Joey Wheeler (Guerreiros e Sorte)",
            personagem = "Joey Wheeler",
            descricao = "Deck veloz e imprevisível com Dragão Negro de Olhos Vermelhos, Jinzo, Espadachim das Chamas e o Mago do Tempo.",
            idsCartasPadrao = listOf(8, 9, 10, 11, 12, 19, 11, 21, 23, 24, 26, 27, 28, 29, 30, 31, 9, 11, 12, 25)
        ),
        DeckPredefinidoYugioh(
            id = 4,
            nomeDeck = "Deck 4: Luna (Magia Acessível & Duelo)",
            personagem = "Luna (8 anos)",
            descricao = "Deck carinhoso e cheio de surpresas preparado especialmente pela pequena Luna, combinando suas cartas favoritas e táticas de duelo.",
            idsCartasPadrao = listOf(2, 5, 6, 7, 10, 17, 21, 23, 24, 25, 27, 28, 29, 30, 31, 5, 6, 2, 24, 27)
        ),
        DeckPredefinidoYugioh(
            id = 5,
            nomeDeck = "Deck 5: Mai Valentine (Asas das Harpias)",
            personagem = "Mai Valentine",
            descricao = "Deck ágil focado nas Irmãs de Harpia e controle aéreo com ataques rápidos e armadilhas defensivas como Força Espelho.",
            idsCartasPadrao = listOf(17, 17, 18, 18, 19, 11, 6, 21, 22, 23, 24, 25, 27, 28, 29, 30, 31, 17, 18, 28)
        ),
        DeckPredefinidoYugioh(
            id = 6,
            nomeDeck = "Deck 6: Maximillion Pegasus (Reino dos Toons)",
            personagem = "Maximillion Pegasus",
            descricao = "Deck mágico extravagante focado na magia contínua Mundo Toon, Crânio Invocado Toon e Manga Ryu-Ran Toon.",
            idsCartasPadrao = listOf(32, 32, 33, 34, 35, 35, 6, 7, 21, 23, 24, 25, 27, 28, 29, 30, 31, 33, 34, 32)
        ),
        DeckPredefinidoYugioh(
            id = 7,
            nomeDeck = "Deck 7: Yami Bakura (Ocultismo do Destino)",
            personagem = "Yami Bakura",
            descricao = "Deck sombrio com Necrofear das Trevas, Diabound Kernel, Cavaleiro Sem Cabeça e o temido Tabuleiro do Destino.",
            idsCartasPadrao = listOf(36, 36, 37, 38, 39, 39, 4, 6, 21, 23, 24, 25, 27, 28, 29, 30, 31, 37, 38, 39)
        ),
        DeckPredefinidoYugioh(
            id = 8,
            nomeDeck = "Deck 8: Yami Marik (Tormento das Trevas)",
            personagem = "Yami Marik",
            descricao = "Deck implacável contendo o Deus Egípcio O Dragão Alado de Rá (4000 ATK/DEF), Lava Golem, Dragão Revólver e Pesadelo de Marik.",
            idsCartasPadrao = listOf(42, 20, 40, 41, 43, 43, 15, 16, 21, 22, 23, 24, 25, 27, 28, 29, 30, 31, 40, 42)
        ),
        DeckPredefinidoYugioh(
            id = 9,
            nomeDeck = "Deck 9: Jaden Yuki (Heróis Elementais - Yu-Gi-Oh! GX)",
            personagem = "Jaden Yuki",
            descricao = "Deck heroico da Academia de Duelos liderado pelo lendário Herói do Elemental Neos (2500 ATK), Sparkman, Clayman e a arena Arranha-Céu.",
            idsCartasPadrao = listOf(54, 54, 55, 55, 56, 56, 57, 57, 14, 15, 6, 9, 21, 23, 24, 25, 27, 28, 29, 30)
        ),
        DeckPredefinidoYugioh(
            id = 10,
            nomeDeck = "Deck 10: Yugi Muto (O Selo Supremo do Exodia)",
            personagem = "Yugi Muto & Solomon",
            descricao = "O lendário deck milenar com as 5 partes proibidas do Exodia! Reúna as 5 peças na mão com Pote da Ganância e proteja seus pontos com Espadas da Luz para a vitória instantânea: Exodia, Obliterar!",
            idsCartasPadrao = listOf(60, 61, 62, 63, 64, 24, 24, 25, 25, 7, 7, 5, 6, 28, 29, 30, 31, 23, 22, 21)
        ),
        DeckPredefinidoYugioh(
            id = 11,
            nomeDeck = "Deck 11: Faraó Atem (A Fúria dos Três Deuses Egípcios)",
            personagem = "Faraó Atem (Yami Yugi)",
            descricao = "Deck divino supremo contendo os Três Deuses Egípcios: Slifer o Dragão Celeste, Obelisco o Atormentador e O Dragão Alado de Rá! Exige 3 tributos de monstros na arena para a Invocação Divina!",
            idsCartasPadrao = listOf(58, 59, 42, 1, 2, 5, 5, 6, 6, 9, 10, 21, 22, 23, 24, 25, 27, 28, 29, 30)
        )
    )

    fun buscarCartaPorId(id: Int): CartaYugioh {
        return todasAsCartas.firstOrNull { it.id == id } ?: todasAsCartas.first()
    }

    /**
     * Gera dinamicamente um Deck para a Luna na Grande Final do Torneio ou duelos avançados.
     * Ela aprende e copia as técnicas de quem joga contra ela (inserindo cópias de cartas aprendidas)
     * e combina com seu próprio arsenal tático para ficar mais desafiadora e inteligente.
     */
    fun gerarDeckAleatorioLuna(cartasAprendidas: List<Int> = emptyList()): List<CartaYugioh> {
        val rng = Random(System.nanoTime())
        val semTributo = todasAsCartas.filter { it.tipo == TipoCartaYugioh.MONSTRO && it.tributosNecessarios == 0 }
        val comTributo = todasAsCartas.filter { it.tipo == TipoCartaYugioh.MONSTRO && it.tributosNecessarios > 0 }
        val magias = todasAsCartas.filter { it.tipo == TipoCartaYugioh.MAGIA }
        val armadilhas = todasAsCartas.filter { it.tipo == TipoCartaYugioh.ARMADILHA }

        val cartasDeck = mutableListOf<CartaYugioh>()

        // 1. A Luna copia cartas e técnicas aprendidas do jogador (até 4 cartas)
        for (idCarta in cartasAprendidas.take(4)) {
            val cartaCopiada = todasAsCartas.firstOrNull { it.id == idCarta }
            if (cartaCopiada != null) {
                cartasDeck.add(cartaCopiada)
                // Se for Pote da Ganância ou Espadas da Luz, ela adiciona reforço da técnica
                if (cartaCopiada.id in listOf(24, 25)) {
                    cartasDeck.add(cartaCopiada)
                }
            }
        }

        fun sortearComLimite(listaOrigem: List<CartaYugioh>, qtd: Int) {
            val disponiveis = listaOrigem.toMutableList()
            var inseridas = 0
            var tentativas = 0
            while (inseridas < qtd && tentativas < 200) {
                tentativas++
                val candidata = disponiveis.random(rng)
                val count = cartasDeck.count { it.id == candidata.id }
                if (count < 3) {
                    cartasDeck.add(candidata)
                    inseridas++
                }
            }
        }

        // 2. Complementa com as táticas fortes da própria Luna
        sortearComLimite(semTributo, 7)
        sortearComLimite(comTributo, 5)
        sortearComLimite(magias, 4)
        sortearComLimite(armadilhas, 3)

        // Se faltar alguma para 20 cartas devido a limites, completa com monstros sem tributo
        while (cartasDeck.size < 20) {
            cartasDeck.add(semTributo.random(rng))
        }

        return cartasDeck.shuffled(rng)
    }
}

enum class ModoJogoYugioh {
    DUELO_AVULSO,
    TORNEIO,
    DUELO_BLUETOOTH
}

data class DadosTorneioYugioh(
    var faseAtual: Int = 1, // 1 = Quartas de Final, 2 = Semifinal, 3 = Grande Final contra Luna
    var oponenteQuartas: DeckPredefinidoYugioh? = null,
    var oponenteSemifinal: DeckPredefinidoYugioh? = null,
    var deckLunaFinalCartas: List<CartaYugioh> = emptyList(),
    var nomeDeckLunaFinal: String = "Deck Surpresa da Luna"
) {
    fun nomeFase(): String = when (faseAtual) {
        1 -> "Quartas de Final (Fase 1 de 3)"
        2 -> "Semifinal (Fase 2 de 3)"
        else -> "Grande Final contra Luna (Fase 3 de 3)"
    }

    fun nomeFaseCurto(): String = when (faseAtual) {
        1 -> "Quartas de Final"
        2 -> "Semifinal"
        else -> "Grande Final"
    }
}

class PartidaYugiohLuna(
    val nomeJogador: String,
    val nomeOponente: String,
    val nomeDeckJogador: String,
    val nomeDeckOponente: String,
    cartasIniciaisJogador: List<CartaYugioh>,
    cartasIniciaisOponente: List<CartaYugioh>
) {
    var lpJogador: Int by mutableIntStateOf(4000)
    var lpOponente: Int by mutableIntStateOf(4000)
    var turnoNumero: Int by mutableIntStateOf(1)
    var vezDoJogador: Boolean by mutableStateOf(true)
    var invocacaoNormalUsadaNoTurno: Boolean by mutableStateOf(false)
    var turnosEspadasLuzJogador: Int by mutableIntStateOf(0) // Impede oponente de atacar se > 0
    var turnosEspadasLuzOponente: Int by mutableIntStateOf(0) // Impede jogador de atacar se > 0
    var dueloEncerrado: Boolean by mutableStateOf(false)
    var vencedorDuelo: String? by mutableStateOf(null)
    var motivoFimDuelo: String? by mutableStateOf(null)

    val deckJogador: MutableList<CartaYugioh> = cartasIniciaisJogador.shuffled(Random(System.nanoTime())).toMutableList()
    val deckOponente: MutableList<CartaYugioh> = cartasIniciaisOponente.shuffled(Random(System.nanoTime() + 7)).toMutableList()

    val maoJogador: SnapshotStateList<CartaYugioh> = mutableStateListOf()
    val maoOponente: SnapshotStateList<CartaYugioh> = mutableStateListOf()

    val cemiterioJogador: SnapshotStateList<CartaYugioh> = mutableStateListOf()
    val cemiterioOponente: SnapshotStateList<CartaYugioh> = mutableStateListOf()

    val monstrosJogador: SnapshotStateList<MonstroEmCampoYugioh> = mutableStateListOf()
    val monstrosOponente: SnapshotStateList<MonstroEmCampoYugioh> = mutableStateListOf()

    var monstroJogador: MonstroEmCampoYugioh?
        get() = monstrosJogador.firstOrNull()
        set(value) {
            monstrosJogador.clear()
            if (value != null) monstrosJogador.add(value)
        }

    var monstroOponente: MonstroEmCampoYugioh?
        get() = monstrosOponente.firstOrNull()
        set(value) {
            monstrosOponente.clear()
            if (value != null) monstrosOponente.add(value)
        }

    var armadilhaJogador: CartaYugioh? by mutableStateOf(null)
    var armadilhaOponente: CartaYugioh? by mutableStateOf(null)

    var indiceCartaMaoSelecionada: Int by mutableIntStateOf(0)

    init {
        repeat(5) {
            comprarCarta(paraJogador = true)
            comprarCarta(paraJogador = false)
        }
        checarVitoriaExodia()
    }

    fun checarVitoriaExodia(): Boolean {
        if (dueloEncerrado && motivoFimDuelo?.contains("EXODIA", ignoreCase = true) == true) return true
        val pecasExodia = setOf(60, 61, 62, 63, 64)
        val maoJogIds = maoJogador.map { it.id }.toSet()
        if (pecasExodia.all { it in maoJogIds }) {
            dueloEncerrado = true
            vencedorDuelo = nomeJogador
            motivoFimDuelo = "💥 VITÓRIA SUPREMA POR EXODIA! Você reuniu as cinco peças proibidas na sua mão: Exodia o Proibido, Braço Direito, Braço Esquerdo, Perna Direita e Perna Esquerda! EXODIA, OBLITERAR!"
            return true
        }
        val maoOpIds = maoOponente.map { it.id }.toSet()
        if (pecasExodia.all { it in maoOpIds }) {
            dueloEncerrado = true
            vencedorDuelo = nomeOponente
            motivoFimDuelo = "💥 VITÓRIA DE $nomeOponente POR EXODIA! $nomeOponente reuniu todas as cinco peças proibidas do Exodia na mão! EXODIA, OBLITERAR!"
            return true
        }
        return false
    }

    fun contarPecasExodiaNaMao(paraJogador: Boolean): Int {
        val pecasExodia = setOf(60, 61, 62, 63, 64)
        val mao = if (paraJogador) maoJogador else maoOponente
        val ids = mao.map { it.id }.toSet()
        return pecasExodia.count { it in ids }
    }

    fun comprarCarta(paraJogador: Boolean): CartaYugioh? {
        val baralho = if (paraJogador) deckJogador else deckOponente
        val mao = if (paraJogador) maoJogador else maoOponente
        if (baralho.isEmpty()) {
            dueloEncerrado = true
            vencedorDuelo = if (paraJogador) nomeOponente else nomeJogador
            motivoFimDuelo = if (paraJogador) {
                "Derrota por Deck Out: Suas cartas acabaram e você não pôde comprar!"
            } else {
                "Vitória por Deck Out: As cartas do Deck de $nomeOponente acabaram!"
            }
            return null
        }
        val carta = baralho.removeAt(0)
        mao.add(carta)
        checarVitoriaExodia()
        return carta
    }

    fun verificarFimDeDuelo(): Boolean {
        if (dueloEncerrado) return true
        if (checarVitoriaExodia()) return true
        if (lpOponente <= 0) {
            lpOponente = 0
            dueloEncerrado = true
            vencedorDuelo = nomeJogador
            motivoFimDuelo = "Você zerou os Pontos de Vida de $nomeOponente!"
            return true
        }
        if (lpJogador <= 0) {
            lpJogador = 0
            dueloEncerrado = true
            vencedorDuelo = nomeOponente
            motivoFimDuelo = "$nomeOponente zerou seus Pontos de Vida!"
            return true
        }
        return false
    }

    fun falarResumoCampo(): String {
        val mOp = if (monstrosOponente.isEmpty()) "nenhum monstro" else {
            monstrosOponente.mapIndexed { i, m -> "Monstro ${i + 1}: ${m.falarStatus(visivelParaJogador = false)}" }.joinToString(", ")
        }
        val armOp = if (armadilhaOponente != null) "1 armadilha oculta armada" else "sem armadilhas"
        val mJog = if (monstrosJogador.isEmpty()) "nenhum monstro" else {
            monstrosJogador.mapIndexed { i, m -> "Monstro ${i + 1}: ${m.falarStatus(visivelParaJogador = true)}" }.joinToString(", ")
        }
        val armJog = armadilhaJogador?.let { "Armadilha armada: ${it.nome}" } ?: "sem armadilhas"
        val espadasTxt = if (turnosEspadasLuzJogador > 0) " Espadas da Luz Reveladora ativas impedindo o oponente de atacar por $turnosEspadasLuzJogador turno(s)." else ""
        val espadasOpTxt = if (turnosEspadasLuzOponente > 0) " Espadas da Luz Reveladora de $nomeOponente impedindo você de atacar por $turnosEspadasLuzOponente turno(s)." else ""
        val exodiaContador = contarPecasExodiaNaMao(paraJogador = true)
        val exodiaTxt = if (exodiaContador > 0) " Peças do Exodia na sua mão: $exodiaContador de 5." else ""
        return "Turno $turnoNumero. Pontos de Vida: $nomeJogador $lpJogador contra $lpOponente de $nomeOponente. " +
            "Campo de $nomeOponente (${monstrosOponente.size}/3 monstros): $mOp, $armOp. " +
            "Seu campo (${monstrosJogador.size}/3 monstros): $mJog, $armJog.$espadasTxt$espadasOpTxt$exodiaTxt " +
            "Você tem ${maoJogador.size} carta(s) na mão e ${deckJogador.size} no Deck."
    }
}
