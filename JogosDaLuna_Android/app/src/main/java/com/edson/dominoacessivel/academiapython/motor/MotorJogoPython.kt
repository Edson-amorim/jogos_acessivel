package com.edson.dominoacessivel.academiapython.motor

import android.content.Context
import kotlin.random.Random

/**
 * Motor do Jogo Academia Python da Luna:
 * Gerencia pontuação (XP), Patentes de Programador, Progresso salvo no celular,
 * Comentários da Professora Luna e o Minijogo da Forca Python (baseado em forca.py).
 */
class MotorJogoPython(context: Context) {

    private val prefs = context.getSharedPreferences("academia_python_edson", Context.MODE_PRIVATE)

    var xpTotal: Int
        get() = prefs.getInt("xp_total", 0)
        private set(value) = prefs.edit().putInt("xp_total", value).apply()

    fun adicionarXp(pontos: Int) {
        xpTotal += pontos
    }

    fun marcarModuloConcluido(numeroModulo: Int, acertos: Int, total: Int) {
        val chave = "modulo_${numeroModulo}_melhor"
        val atual = prefs.getInt(chave, 0)
        if (acertos > atual) {
            prefs.edit().putInt(chave, acertos).apply()
        }
    }

    fun melhorPontuacaoModulo(numeroModulo: Int): Int {
        return prefs.getInt("modulo_${numeroModulo}_melhor", 0)
    }

    fun obterPatenteAtual(): String {
        val xp = xpTotal
        return when {
            xp < 100 -> "Iniciante do Código"
            xp < 250 -> "Aprendiz da Luna"
            xp < 500 -> "Programador Python Júnior"
            xp < 800 -> "Especialista em Dados e SQLite"
            xp < 1200 -> "Engenheiro de Testes Pytest"
            else -> "Mestre Supremo do Python (Nível Edson)"
        }
    }

    val elogiosLuna = listOf(
        "Brilhante, %s! Seu raciocínio lógico está afiadíssimo!",
        "Na mosca! Nem minha inferência bayesiana faria melhor!",
        "Código limpo, resposta certa! Mais XP para a sua patente!",
        "Excelente! Você domina esse conceito de Python!",
        "Perfeito! O interpretador Python aprovou sua resposta sem nenhum bug!"
    )

    val incentivosErroLuna = listOf(
        "Quase lá, %s! Todo grande programador aprende depurando erros. Escute a explicação!",
        "Opa, deu uma pequena exceção nessa resposta! Vamos revisar o conceito juntos!",
        "Não desanime! Até nos melhores sistemas aparecem bugs antes do commit final!"
    )

    fun sortearElogio(nome: String = "Edson"): String {
        val frase = elogiosLuna[Random.nextInt(elogiosLuna.size)]
        return if (frase.contains("%s")) String.format(frase, nome) else frase
    }
    fun sortearIncentivo(nome: String = "Edson"): String {
        val frase = incentivosErroLuna[Random.nextInt(incentivosErroLuna.size)]
        return if (frase.contains("%s")) String.format(frase, nome) else frase
    }
}

/**
 * Motor do Minijogo da Forca Python (fiel ao arquivo C:\edson_python\forca.py),
 * enriquecido com dicas didáticas sobre cada termo de programação!
 */
class PartidaForcaPython {
    data class PalavraComDica(val palavra: String, val dica: String)

    companion object {
        val bancoPalavras = listOf(
            PalavraComDica("python", "Linguagem de programação criada por Guido van Rossum que estamos estudando."),
            PalavraComDica("programacao", "Arte de escrever instruções e códigos para o computador executar."),
            PalavraComDica("computador", "Máquina que processa dados e executa nossos programas."),
            PalavraComDica("teclado", "Dispositivo de entrada usado com a função input()."),
            PalavraComDica("acessibilidade", "Qualidade que permite que qualquer pessoa use jogos e sistemas com leitor de tela."),
            PalavraComDica("leitor", "Software que transforma o texto da tela em voz, como NVDA e TalkBack."),
            PalavraComDica("algoritmo", "Sequência lógica de passos para resolver um problema, como o cálculo do CPF."),
            PalavraComDica("variavel", "Caixinha na memória onde guardamos valores como valor1 e salario."),
            PalavraComDica("funcao", "Bloco de código reutilizável criado com a palavra def."),
            PalavraComDica("biblioteca", "Módulo importado com import, como random, json e sqlite3.")
        )
    }

    private val sorteada = bancoPalavras[Random.nextInt(bancoPalavras.size)]
    val palavraSecreta: String = sorteada.palavra
    val dica: String = sorteada.dica
    val letrasCertas = mutableSetOf<Char>()
    val letrasErradas = mutableSetOf<Char>()
    var tentativasRestantes: Int = 6
        private set

    val alfabeto: List<Char> = ('a'..'z').toList()

    fun mostrarProgressoVisual(): String {
        return palavraSecreta.map { c ->
            if (c in letrasCertas) c.uppercaseChar().toString() else "_"
        }.joinToString(" ")
    }

    fun mostrarProgressoFalado(): String {
        return palavraSecreta.map { c ->
            if (c in letrasCertas) "letra ${c.uppercaseChar()}" else "traço"
        }.joinToString(", ")
    }

    fun venceu(): Boolean = palavraSecreta.all { it in letrasCertas }
    fun perdeu(): Boolean = tentativasRestantes <= 0 && !venceu()

    fun tentarLetra(letra: Char): String {
        val c = letra.lowercaseChar()
        if (c in letrasCertas || c in letrasErradas) {
            return "Você já tentou a letra ${c.uppercaseChar()}. Escolha outra letra!"
        }
        return if (c in palavraSecreta) {
            letrasCertas.add(c)
            if (venceu()) {
                "Parabéns! A letra ${c.uppercaseChar()} completou a palavra: ${palavraSecreta.uppercase()}! Significado: $dica"
            } else {
                "Boa! A letra ${c.uppercaseChar()} está na palavra! Progresso: ${mostrarProgressoFalado()}."
            }
        } else {
            letrasErradas.add(c)
            tentativasRestantes--
            if (perdeu()) {
                "A letra ${c.uppercaseChar()} não está na palavra. Suas tentativas acabaram! A palavra era ${palavraSecreta.uppercase()}."
            } else {
                "A letra ${c.uppercaseChar()} não está na palavra. Restam $tentativasRestantes tentativas."
            }
        }
    }
}
