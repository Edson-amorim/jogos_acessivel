package com.edson.academiapython.modulos

import com.edson.academiapython.modelo.CenarioSimulador
import com.edson.academiapython.modelo.CodigoOriginalEdson
import com.edson.academiapython.modelo.LinhaExplicada
import com.edson.academiapython.modelo.ModuloEnsino
import com.edson.academiapython.modelo.QuestaoDesafio
import com.edson.academiapython.modelo.TopicoTeoria

/**
 * Módulo 5: Funções (def), Laços (while / for), Listas e Conjuntos (set)
 * Baseado no arquivo: forca.py da pasta C:\edson_python
 */
object Modulo05LacosFuncoesForca {

    val modulo = ModuloEnsino(
        numero = 5,
        titulo = "Módulo 5: Funções, Laços e Jogo da Forca",
        subtitulo = "def, while, for, list, set e random (forca.py)",
        arquivosOrigem = listOf("forca.py"),
        resumoFalado = "Módulo 5: Funções, Laços de Repetição, Listas e Conjuntos. Aprenda a estruturar programas profissionais estudando o seu Jogo da Forca em forca.py!",
        topicosTeoria = listOf(
            TopicoTeoria(
                titulo = "1. Criando Funções com def e return",
                explicacaoVisual = "Funções dividem um programa grande em partes menores e organizadas!\n" +
                    "Em `forca.py`, você criou 4 funções com a palavra-chave `def`:\n" +
                    "• `def escolher_palavra():` -> Sorteia e devolve (`return`) uma palavra.\n" +
                    "• `def mostrar_progresso(palavra, letras_certas):` -> Monta os tracinhos `_` e letras descobertas.\n" +
                    "• `def jogar():` -> Controla a partida.\n" +
                    "• `def main():` -> Pergunta se o jogador quer jogar novamente.",
                explicacaoFalada = "Tópico 1: Criando Funções com def e return. No seu código forca.py, você dividiu o jogo em 4 funções usando a palavra def: escolher palavra, mostrar progresso, jogar e main. O comando return devolve o resultado da função para quem a chamou!",
                exemploCodigo = "def escolher_palavra():\n    palavras = [\"python\", \"acessibilidade\", \"leitor\"]\n    return random.choice(palavras)",
                dicaDaLuna = "Dica da Luna: Dividir o código em funções facilita encontrar erros e reaproveitar código sem precisar copiar e colar!"
            ),
            TopicoTeoria(
                titulo = "2. Listas [] vs Conjuntos set() no Jogo da Forca",
                explicacaoVisual = "No `forca.py`, você usou duas estruturas de dados poderosas:\n" +
                    "• **Lista (`[...]`):** Guarda as palavras para sorteio com `random.choice(palavras)`.\n" +
                    "• **Conjunto (`set()`):** Guarda `letras_certas = set()` e `letras_erradas = set()`.\n" +
                    "Por que usar `set()`? Porque conjuntos **não permitem itens duplicados** e verificam instantaneamente se uma letra já foi tentada usando `if chute in letras_certas:`!",
                explicacaoFalada = "Tópico 2: Listas versus Conjuntos set. Para guardar as letras certas e erradas no forca.py, você usou set abre e fecha parênteses. O conjunto set é perfeito para o jogo da forca porque adiciona letras com ponto add e nunca duplica letras repetidas!",
                exemploCodigo = "letras_certas = set()\nletras_certas.add(chute)\nif chute in letras_certas:\n    print(\"Você já tentou essa letra.\")",
                dicaDaLuna = "Dica da Luna: Para verificar se TODAS as letras da palavra já estão no conjunto letras_certas, você usou a função all(letra in letras_certas for letra in palavra)!"
            ),
            TopicoTeoria(
                titulo = "3. Laços while e for, continue e break",
                explicacaoVisual = "• **`while tentativas_restantes > 0:`** -> Repete as rodadas ENQUANTO o jogador ainda tiver vidas.\n" +
                    "• **`for letra in palavra:`** -> Percorre letra por letra da palavra secreta para montar o progresso.\n" +
                    "• **`continue`** -> Pula para a próxima volta do `while` se o usuário digitar algo inválido.\n" +
                    "• **`break`** -> Sai do laço `while True:` quando o jogador escolhe não jogar mais.",
                explicacaoFalada = "Tópico 3: Laços while e for, continue e break. O laço while repete o turno enquanto tentativas restantes for maior que zero. E o laço for percorre cada letra da palavra secreta para revelar as letras descobertas ou mostrar sublinhado!",
                exemploCodigo = "for letra in palavra:\n    if letra in letras_certas:\n        progresso += letra + \" \"\n    else:\n        progresso += \"_ \"",
                dicaDaLuna = "Dica da Luna: Você também pode jogar o Jogo da Forca interativo direto aqui no aplicativo pelo menu!"
            )
        ),
        codigosOriginais = listOf(
            CodigoOriginalEdson(
                nomeArquivo = "forca.py",
                titulo = "Jogo da Forca Acessível Completo",
                descricaoContexto = "Seu jogo clássico em C:\\edson_python\\forca.py com sorteio de palavras de tecnologia e acessibilidade.",
                codigoCompleto = """import random

def escolher_palavra():
    palavras = [
        "python", "programacao", "computador", "teclado",
        "acessibilidade", "leitor", "algoritmo", "variavel",
        "funcao", "biblioteca"
    ]
    return random.choice(palavras)

def mostrar_progresso(palavra, letras_certas):
    progresso = ""
    for letra in palavra:
        if letra in letras_certas:
            progresso += letra + " "
        else:
            progresso += "_ "
    return progresso.strip()

def jogar():
    palavra = escolher_palavra()
    letras_certas = set()
    letras_erradas = set()
    tentativas_restantes = 6

    while tentativas_restantes > 0:
        print(f"Palavra: {mostrar_progresso(palavra, letras_certas)}")
        chute = input("Digite uma letra: ").strip().lower()
        if len(chute) != 1 or not chute.isalpha():
            print("Por favor, digite apenas uma letra válida.")
            continue
        if chute in palavra:
            letras_certas.add(chute)
        else:
            letras_erradas.add(chute)
            tentativas_restantes -= 1
        if all(letra in letras_certas for letra in palavra):
            print(f"Parabéns! Você acertou a palavra: {palavra}")
            return""",
                linhasExplicadas = listOf(
                    LinhaExplicada(1, "import random", "Linha 1: import random.", "Importa a biblioteca padrão do Python para sorteios aleatórios."),
                    LinhaExplicada(2, "def escolher_palavra(): ... return random.choice(palavras)", "Função escolher palavra que retorna random ponto choice de palavras.", "Cria uma lista com 10 palavras sobre programação e acessibilidade e sorteia uma delas."),
                    LinhaExplicada(3, "def mostrar_progresso(palavra, letras_certas):", "Função mostrar progresso recebendo palavra e letras certas.", "Percorre cada letra da palavra com for: se foi acertada, mostra a letra; senão, mostra um traço."),
                    LinhaExplicada(4, "letras_certas = set() / letras_erradas = set()", "Criação dos conjuntos letras certas e letras erradas.", "Cria conjuntos vazios para registrar os palpites sem repetição."),
                    LinhaExplicada(5, "chute = input(\"Digite uma letra: \").strip().lower()", "Lê a letra, remove espaços com strip e passa para minúscula com lower.", "Garante que digitar 'A' maiúsculo ou com espaço funcione perfeitamente."),
                    LinhaExplicada(6, "if len(chute) != 1 or not chute.isalpha(): continue", "Valida se digitou exatamente 1 letra do alfabeto.", "Impede que números ou várias letras descontem tentativas do jogador."),
                    LinhaExplicada(7, "if all(letra in letras_certas for letra in palavra):", "Verifica se todas as letras da palavra estão em letras certas.", "Se o jogador descobriu todas as letras, anuncia a vitória e encerra a partida com return!")
                )
            )
        ),
        cenariosSimulador = listOf(
            CenarioSimulador(
                titulo = "Simulação: Rodada do Jogo da Forca com a palavra 'python'",
                entradasSimuladas = "Palavra sorteada: 'python' | Chutes: 'p', 'y', 'z', 't', 'h', 'o', 'n'",
                saidaConsole = "Bem-vindo ao jogo da forca!\nA palavra tem 6 letras.\nPalavra: _ _ _ _ _ _\nBoa! A letra 'p' está na palavra.\nPalavra: p _ _ _ _ _\nA letra 'z' não está na palavra. Tentativas restantes: 5\n...\nParabéns! Você acertou a palavra: python",
                explicacaoExecucao = "A cada chute certo, letras_certas.add(chute) adiciona a letra e mostrar_progresso revela sua posição. No chute errado ('z'), tentativas_restantes diminui de 6 para 5. Quando todas as letras são descobertas, all(...) retorna True!"
            )
        ),
        questoes = listOf(
            QuestaoDesafio(
                id = "m5_q1",
                moduloNumero = 5,
                enunciado = "No arquivo forca.py, qual comando da biblioteca random é usado para sortear uma palavra da lista 'palavras'?",
                codigoApoio = "import random\npalavras = [\"python\", \"leitor\", \"algoritmo\"]",
                opcoes = listOf(
                    "random.choice(palavras)",
                    "random.sortear(palavras)",
                    "random.randint(palavras)",
                    "random.input(palavras)"
                ),
                indiceCorreto = 0,
                explicacaoResposta = "Exato! A função random.choice() escolhe aleatoriamente um elemento de uma sequência (como uma lista)."
            ),
            QuestaoDesafio(
                id = "m5_q2",
                moduloNumero = 5,
                enunciado = "Para que servem os métodos .strip().lower() aplicados ao input em 'chute = input(\"Digite uma letra: \").strip().lower()'?",
                codigoApoio = "chute = input(\"Digite uma letra: \").strip().lower()",
                opcoes = listOf(
                    ".strip() remove espaços extras nas pontas e .lower() converte a letra para minúscula.",
                    ".strip() apaga a palavra inteira e .lower() diminui o volume do som.",
                    ".strip() converte para número inteiro e .lower() arredonda para baixo.",
                    "Servem para fechar o jogo imediatamente."
                ),
                indiceCorreto = 0,
                explicacaoResposta = "Perfeito! Assim, se o usuário digitar ' P ' (com espaço e letra maiúscula), vira 'p' limpo e minúsculo."
            ),
            QuestaoDesafio(
                id = "m5_q3",
                moduloNumero = 5,
                enunciado = "O que faz a verificação 'if not chute.isalpha():' no seu código forca.py?",
                codigoApoio = "if len(chute) != 1 or not chute.isalpha():",
                opcoes = listOf(
                    "Verifica se o que foi digitado NÃO é uma letra do alfabeto (rejeitando números e símbolos).",
                    "Verifica se a letra é a letra 'a'.",
                    "Sorteia uma nova palavra em ordem alfabética.",
                    "Encerra o jogo com vitória."
                ),
                indiceCorreto = 0,
                explicacaoResposta = "Muito bem! O método .isalpha() retorna True apenas se todos os caracteres forem letras."
            ),
            QuestaoDesafio(
                id = "m5_q4",
                moduloNumero = 5,
                enunciado = "O que a linha 'tentativas_restantes -= 1' faz quando o jogador erra uma letra?",
                codigoApoio = "tentativas_restantes -= 1",
                opcoes = listOf(
                    "Diminui 1 do valor atual de tentativas_restantes (equivale a tentativas_restantes = tentativas_restantes - 1).",
                    "Transforma tentativas_restantes no número negativo -1.",
                    "Soma 1 tentativa extra para o jogador.",
                    "Compara se tentativas_restantes é igual a 1."
                ),
                indiceCorreto = 0,
                explicacaoResposta = "Certíssimo! O operador -= subtrai o valor à direita da própria variável e guarda o novo resultado nela."
            )
        )
    )
}
