package com.edson.academiapython.modulos

import com.edson.academiapython.modelo.CenarioSimulador
import com.edson.academiapython.modelo.CodigoOriginalEdson
import com.edson.academiapython.modelo.LinhaExplicada
import com.edson.academiapython.modelo.ModuloEnsino
import com.edson.academiapython.modelo.QuestaoDesafio
import com.edson.academiapython.modelo.TopicoTeoria

/**
 * Módulo 1: Primeiros Passos, Variáveis, Entrada e Saída
 * Baseado nos arquivos: soma.py e ex11.py da pasta C:\edson_python
 */
object Modulo01VariaveisEntrada {

    val modulo = ModuloEnsino(
        numero = 1,
        titulo = "Módulo 1: Variáveis, Entrada e Saída",
        subtitulo = "Máquina de Soma da Luna e Pintura de Parede (soma.py e ex11.py)",
        arquivosOrigem = listOf("soma.py", "ex11.py"),
        resumoFalado = "Módulo 1: Variáveis, Entrada e Saída. Aprenda print, input, float e f-strings usando os seus códigos soma.py e ex11.py.",
        topicosTeoria = listOf(
            TopicoTeoria(
                titulo = "1. O que são Variáveis em Python?",
                explicacaoVisual = "Pense em uma variável como uma caixinha etiquetada na memória do computador.\n" +
                    "Quando você escreve `valor1 = 10`, o sinal de igual `=` significa RECEBE (atribuição).\n" +
                    "Você está guardando o número 10 dentro da caixinha chamada `valor1`.",
                explicacaoFalada = "Tópico 1: O que são Variáveis em Python? Pense em uma variável como uma caixinha etiquetada na memória do computador. Quando você escreve valor1 igual a 10, o sinal de igual significa RECEBE. Você guarda o número 10 dentro da variável valor1.",
                exemploCodigo = "valor1 = 10\nvalor2 = 5.5\nsoma = valor1 + valor2",
                dicaDaLuna = "Dica da Luna: Em Python, não precisamos declarar o tipo antes do nome da variável. O Python descobre sozinho se é número inteiro (int), número com vírgula (float) ou texto (str)!"
            ),
            TopicoTeoria(
                titulo = "2. Lendo dados do teclado com input() e convertendo com float()",
                explicacaoVisual = "A função `input()` mostra uma pergunta na tela e espera o usuário digitar.\n" +
                    "ATENÇÃO: O `input()` SEMPRE devolve um texto (`str`).\n" +
                    "Se você quiser fazer contas matemáticas, precisa converter esse texto usando `float()` (números reais/decimais) ou `int()` (números inteiros).",
                explicacaoFalada = "Tópico 2: Lendo dados com input e convertendo com float. A função input espera o usuário digitar, mas sempre devolve um texto. Por isso, no seu código soma.py, você envolveu o input com float abre parênteses, transformando o texto digitado em número decimal para poder somar!",
                exemploCodigo = "valor1 = float(input(\"Informe o valor 1: \"))\nvalor2 = float(input(\"Informe o valor 2: \"))",
                dicaDaLuna = "Dica da Luna: Se você somar dois inputs sem usar float() ou int(), o Python junta os textos! Por exemplo, '2' + '3' vira '23' em vez de 5!"
            ),
            TopicoTeoria(
                titulo = "3. Exibindo resultados com print() e f-strings",
                explicacaoVisual = "A função `print()` exibe mensagens na tela.\n" +
                    "Quando colocamos a letra `f` antes das aspas (`f\"...\"`), criamos uma **f-string** (string formatada).\n" +
                    "Tudo o que estiver entre chaves `{}` dentro da f-string é substituído pelo valor da variável!",
                explicacaoFalada = "Tópico 3: Exibindo resultados com print e f-strings. Quando colocamos a letra f antes das aspas no print, criamos uma f-string. Qualquer variável colocada entre chaves, como abre chaves som fecha chaves, tem seu valor calculado e inserido direto na frase!",
                exemploCodigo = "som = valor1 + valor2\nprint(f\"A soma de {valor1} + {valor2} é {som}\")",
                dicaDaLuna = "Dica da Luna: Você usou f-strings perfeitamente tanto em soma.py quanto em ex11.py para falar a área da parede e os litros de tinta!"
            )
        ),
        codigosOriginais = listOf(
            CodigoOriginalEdson(
                nomeArquivo = "soma.py",
                titulo = "Máquina de Soma da Luna",
                descricaoContexto = "Seu programa em C:\\edson_python\\soma.py que pede dois números ao usuário, calcula a soma e exibe o resultado formatado.",
                codigoCompleto = """print("máquina de soma de luna!")
valor1 = float(input("informe o valor 1! "))
valor2 = float(input("informe o valor 2! "))
som = (valor1 + valor2)
print(f"a soma de {valor1} + {valor2} é {som}")""",
                linhasExplicadas = listOf(
                    LinhaExplicada(
                        1,
                        "print(\"máquina de soma de luna!\")",
                        "Linha 1: print abre parênteses e aspas, máquina de soma de luna!, fecha aspas e parênteses.",
                        "Exibe a mensagem de boas-vindas do programa na tela."
                    ),
                    LinhaExplicada(
                        2,
                        "valor1 = float(input(\"informe o valor 1! \"))",
                        "Linha 2: valor1 recebe float de input informe o valor 1.",
                        "Pede o primeiro número ao usuário pelo teclado, converte de texto para número decimal (float) e guarda na variável valor1."
                    ),
                    LinhaExplicada(
                        3,
                        "valor2 = float(input(\"informe o valor 2! \"))",
                        "Linha 3: valor2 recebe float de input informe o valor 2.",
                        "Pede o segundo número ao usuário, converte para float e guarda na variável valor2."
                    ),
                    LinhaExplicada(
                        4,
                        "som = (valor1 + valor2)",
                        "Linha 4: som recebe valor1 mais valor2.",
                        "Soma os valores guardados em valor1 e valor2 e armazena o resultado na variável som."
                    ),
                    LinhaExplicada(
                        5,
                        "print(f\"a soma de {valor1} + {valor2} é {som}\")",
                        "Linha 5: print com f-string: a soma de chave valor1 mais chave valor2 é chave som.",
                        "Usa uma f-string para mostrar na tela os dois números digitados e o resultado final da soma."
                    )
                )
            ),
            CodigoOriginalEdson(
                nomeArquivo = "ex11.py",
                titulo = "Calculadora de Área de Parede e Tinta",
                descricaoContexto = "Seu exercício C:\\edson_python\\ex11.py que calcula a área de uma parede (largura x altura) e quantos litros de tinta são necessários sabendo que 1 litro pinta 2 metros quadrados.",
                codigoCompleto = """largura = float(input("informe a largura da parede! "))
altura = float(input("informe a altura da parede!"))
aria = largura * altura
print(f"a ária da parede é {aria}! ")
tinta = aria / 2
print(f"para pintar essa parede você precisará de {tinta}! ")""",
                linhasExplicadas = listOf(
                    LinhaExplicada(
                        1,
                        "largura = float(input(\"informe a largura da parede! \"))",
                        "Linha 1: largura recebe float de input informe a largura da parede.",
                        "Lê a largura da parede em metros (aceitando casas decimais como 3.5) e guarda em largura."
                    ),
                    LinhaExplicada(
                        2,
                        "altura = float(input(\"informe a altura da parede!\"))",
                        "Linha 2: altura recebe float de input informe a altura da parede.",
                        "Lê a altura da parede em metros e guarda na variável altura."
                    ),
                    LinhaExplicada(
                        3,
                        "aria = largura * altura",
                        "Linha 3: aria recebe largura vezes altura.",
                        "Multiplica a largura pela altura usando o operador asterisco (*) para encontrar a área em metros quadrados."
                    ),
                    LinhaExplicada(
                        4,
                        "print(f\"a ária da parede é {aria}! \")",
                        "Linha 4: print com f-string mostrando a área calculada.",
                        "Mostra para o usuário quantos metros quadrados a parede possui."
                    ),
                    LinhaExplicada(
                        5,
                        "tinta = aria / 2",
                        "Linha 5: tinta recebe aria dividido por 2.",
                        "Divide a área por 2 usando a barra (/), pois cada litro de tinta rende 2 metros quadrados."
                    ),
                    LinhaExplicada(
                        6,
                        "print(f\"para pintar essa parede você precisará de {tinta}! \")",
                        "Linha 6: print com f-string mostrando a quantidade de tinta.",
                        "Exibe o total de litros de tinta necessários para pintar toda a parede."
                    )
                )
            )
        ),
        cenariosSimulador = listOf(
            CenarioSimulador(
                titulo = "Simulação 1: Máquina de Soma da Luna (soma.py)",
                entradasSimuladas = "valor1 = 15.5 | valor2 = 4.5",
                saidaConsole = "máquina de soma de luna!\ninforme o valor 1! 15.5\ninforme o valor 2! 4.5\na soma de 15.5 + 4.5 é 20.0",
                explicacaoExecucao = "O Python leu 15.5 e 4.5, converteu ambos com float(), somou 15.5 + 4.5 obtendo 20.0 na variável som, e imprimiu a f-string completa."
            ),
            CenarioSimulador(
                titulo = "Simulação 2: Pintando uma Parede de 4m x 2.5m (ex11.py)",
                entradasSimuladas = "largura = 4.0 | altura = 2.5",
                saidaConsole = "informe a largura da parede! 4.0\ninforme a altura da parede! 2.5\na ária da parede é 10.0!\npara pintar essa parede você precisará de 5.0!",
                explicacaoExecucao = "Multiplicou 4.0 por 2.5 resultando em 10.0 metros quadrados de área. Em seguida, dividiu 10.0 por 2, concluindo que são necessários 5.0 litros de tinta."
            )
        ),
        questoes = listOf(
            QuestaoDesafio(
                id = "m1_q1",
                moduloNumero = 1,
                enunciado = "No seu arquivo soma.py, por que usamos float(input(...)) em vez de apenas input(...)?",
                codigoApoio = "valor1 = float(input(\"informe o valor 1! \"))",
                opcoes = listOf(
                    "Porque o input() retorna texto (string) e o float() converte para número decimal para podermos somar.",
                    "Porque sem o float() o Python não mostra a mensagem na tela.",
                    "Porque o float() serve para deixar as letras maiúsculas.",
                    "Porque o input() só funciona dentro de bancos de dados."
                ),
                indiceCorreto = 0,
                explicacaoResposta = "Exato! A função input() sempre retorna uma string (texto). O float() converte esse texto para número real para que o operador + faça uma adição matemática em vez de juntar textos."
            ),
            QuestaoDesafio(
                id = "m1_q2",
                moduloNumero = 1,
                enunciado = "No seu código ex11.py, se a largura da parede for 6 e a altura for 3, qual será o valor das variáveis 'aria' e 'tinta'?",
                codigoApoio = "aria = largura * altura\ntinta = aria / 2",
                opcoes = listOf(
                    "aria será 18.0 e tinta será 9.0",
                    "aria será 9.0 e tinta será 18.0",
                    "aria será 12.0 e tinta será 6.0",
                    "aria será 18.0 e tinta será 36.0"
                ),
                indiceCorreto = 0,
                explicacaoResposta = "Perfeito! A área é largura vezes altura (6 * 3 = 18.0) e a tinta é a área dividida por 2 (18.0 / 2 = 9.0 litros)."
            ),
            QuestaoDesafio(
                id = "m1_q3",
                moduloNumero = 1,
                enunciado = "Qual letra colocamos imediatamente antes das aspas no print para que o Python substitua {som} pelo valor da variável?",
                codigoApoio = "print(_\"a soma é {som}\")",
                opcoes = listOf(
                    "A letra f (criando uma f-string)",
                    "A letra s (criando uma s-string)",
                    "A letra p (de print)",
                    "O símbolo @ antes das aspas"
                ),
                indiceCorreto = 0,
                explicacaoResposta = "Muito bem! A letra f antes das aspas cria uma f-string (formatted string literal), permitindo colocar variáveis e expressões dentro de chaves {}."
            ),
            QuestaoDesafio(
                id = "m1_q4",
                moduloNumero = 1,
                enunciado = "O que acontece se alguém escrever x = input() e digitar '5', e y = input() e digitar '5', sem usar float() nem int(), e fizer print(x + y)?",
                codigoApoio = "x = \"5\"\ny = \"5\"\nprint(x + y)",
                opcoes = listOf(
                    "O Python imprime 55, pois concatena (junta) os dois textos.",
                    "O Python imprime 10 normalmente.",
                    "O computador desliga imediatamente.",
                    "O Python imprime 25."
                ),
                indiceCorreto = 0,
                explicacaoResposta = "Certíssimo! Quando usamos o sinal de + entre duas strings ('5' + '5'), o Python faz concatenação de texto, resultando em '55'."
            )
        )
    )
}
