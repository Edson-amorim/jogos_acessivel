package com.edson.academiapython.modulos

import com.edson.academiapython.modelo.CenarioSimulador
import com.edson.academiapython.modelo.CodigoOriginalEdson
import com.edson.academiapython.modelo.LinhaExplicada
import com.edson.academiapython.modelo.ModuloEnsino
import com.edson.academiapython.modelo.QuestaoDesafio
import com.edson.academiapython.modelo.TopicoTeoria

/**
 * Módulo 2: Operadores Aritméticos, Porcentagem e Resto da Divisão (%)
 * Baseado nos arquivos: ex12.py, ex13.py, calculadora_do_bando.py e maquina_de_recompença.py
 */
object Modulo02OperadoresPorcentagem {

    val modulo = ModuloEnsino(
        numero = 2,
        titulo = "Módulo 2: Porcentagem e Divisão de Ouro",
        subtitulo = "Descontos, Salários e o Resto da Capitã (ex12.py, ex13.py, calculadora_do_bando.py)",
        arquivosOrigem = listOf("ex12.py", "ex13.py", "calculadora_do_bando.py", "maquina_de_recompença.py"),
        resumoFalado = "Módulo 2: Operadores Aritméticos, Porcentagem e Resto da Divisão. Aprenda como calcular descontos, aumentos salariais e a sobra do ouro da Capitã Luna com o operador porcentagem.",
        topicosTeoria = listOf(
            TopicoTeoria(
                titulo = "1. Calculando Porcentagens em Python (15% = 0.15)",
                explicacaoVisual = "Em Python, não existe um botão `%` para calcular porcentagem direta como na calculadora comum.\n" +
                    "Para calcular 15% de um valor, multiplicamos por `0.15` (que é 15 dividido por 100).\n" +
                    "• **Desconto (ex12.py):** `novo_preco = valor - (valor * 0.15)`\n" +
                    "• **Aumento (ex13.py):** `novo_salario = salario + (salario * 0.15)`",
                explicacaoFalada = "Tópico 1: Calculando Porcentagens em Python. Para calcular 15 por cento de um valor em Python, multiplicamos esse valor por 0 ponto 15. No seu arquivo ex12.py, você subtraiu o desconto do preço original. Já no arquivo ex13.py, você somou o reajuste ao salário!",
                exemploCodigo = "desconto = valor_do_produto * 0.15\nnovo_preco = valor_do_produto - desconto",
                dicaDaLuna = "Dica da Luna: Multiplicar um preço por 0.85 dá exatamente o mesmo resultado que tirar 15% de desconto, porque 100% menos 15% é igual a 85% (0.85)!"
            ),
            TopicoTeoria(
                titulo = "2. O Operador Módulo (%) — A Sobra da Capitã!",
                explicacaoVisual = "Em Python, o símbolo `%` entre dois números é o **Operador Módulo (Resto da Divisão)**!\n" +
                    "No seu código `calculadora_do_bando.py`, quando o bando chega com ouro:\n" +
                    "• `valor / homens` faz a divisão normal.\n" +
                    "• `valor % homens` calcula **quanto sobra** da divisão inteira (a sobra da Capitã!).",
                explicacaoFalada = "Tópico 2: O Operador Módulo, representado pelo símbolo de porcentagem. Em Python, colocar o símbolo de porcentagem entre dois números calcula o RESTO da divisão! Foi assim que no seu código calculadora do bando você calculou a variável sobra da capitan igual a valor módulo homens!",
                exemploCodigo = "pagamento = valor / homens\nsobra_da_capitan = valor % homens",
                dicaDaLuna = "Dica da Luna: Se você quiser que cada homem receba apenas moedas inteiras sem quebrar nenhuma moeda ao meio, você também pode usar duas barras (valor // homens), que faz a divisão inteira!"
            ),
            TopicoTeoria(
                titulo = "3. Formatando Dinheiro com Duas Casas Decimais (:.2f)",
                explicacaoVisual = "Quando trabalhamos com dinheiro (Reais e centavos), números como `85.0` ou `12.3456` ficam feios.\n" +
                    "Dentro das chaves da f-string, colocamos `:.2f` após o nome da variável:\n" +
                    "`f\"R\${novo_preco:.2f}\"` -> Mostra sempre 2 casas após o ponto (ex: `R\$85.00`).",
                explicacaoFalada = "Tópico 3: Formatando dinheiro com dois pontos, ponto, 2, f. Dentro da chave da f-string, escrever dois pontos ponto 2 f formata o número decimal com exatamente duas casas depois da vírgula, perfeito para valores em Reais como você fez em ex12.py e ex13.py!",
                exemploCodigo = "print(f\"O novo salário é R\${novo_salario:.2f}\")",
                dicaDaLuna = "Dica da Luna: O '2' indica duas casas decimais e o 'f' vem de float (ponto flutuante)!"
            )
        ),
        codigosOriginais = listOf(
            CodigoOriginalEdson(
                nomeArquivo = "ex12.py",
                titulo = "Loja da Luna - Desconto de 15%",
                descricaoContexto = "Seu código em C:\\edson_python\\ex12.py que lê o preço de um produto, calcula 15% de desconto e exibe o preço final com duas casas decimais.",
                codigoCompleto = """print("Bem-vindo à loja da Luna!")
valor_do_produto = float(input("Digite o valor do produto: "))
desconto = valor_do_produto * 0.15
novo_preco = valor_do_produto - desconto
print(f"O valor do produto com desconto de 15% é: R${'$'}{novo_preco:.2f}")""",
                linhasExplicadas = listOf(
                    LinhaExplicada(1, "print(\"Bem-vindo à loja da Luna!\")", "Linha 1: print Bem-vindo à loja da Luna.", "Saúda o cliente na abertura da loja."),
                    LinhaExplicada(2, "valor_do_produto = float(input(\"Digite o valor do produto: \"))", "Linha 2: valor do produto recebe float de input Digite o valor do produto.", "Lê o preço original digitado e converte para número decimal."),
                    LinhaExplicada(3, "desconto = valor_do_produto * 0.15", "Linha 3: desconto recebe valor do produto vezes 0 ponto 15.", "Calcula 15% do valor do produto."),
                    LinhaExplicada(4, "novo_preco = valor_do_produto - desconto", "Linha 4: novo preço recebe valor do produto menos desconto.", "Subtrai o valor do desconto para achar o preço final a pagar."),
                    LinhaExplicada(5, "print(f\"O valor do produto com desconto de 15% é: R\${novo_preco:.2f}\")", "Linha 5: print com f-string exibindo novo preço formatado com duas casas decimais.", "Exibe o resultado em Reais com duas casas decimais usando dois pontos ponto 2 f.")
                )
            ),
            CodigoOriginalEdson(
                nomeArquivo = "ex13.py",
                titulo = "Calculadora de Reajuste Salarial (15%)",
                descricaoContexto = "Seu código em C:\\edson_python\\ex13.py que calcula um aumento salarial de 15%.",
                codigoCompleto = """salario = float(input("digite o salário! "))
reajuste = salario * 0.15
novo_salario = salario + reajuste
print(f"o novo salário é R${'$'}{novo_salario:.2f}")""",
                linhasExplicadas = listOf(
                    LinhaExplicada(1, "salario = float(input(\"digite o salário! \"))", "Linha 1: salario recebe float de input digite o salário.", "Lê o salário atual do trabalhador."),
                    LinhaExplicada(2, "reajuste = salario * 0.15", "Linha 2: reajuste recebe salario vezes 0 ponto 15.", "Calcula o valor do aumento de 15%."),
                    LinhaExplicada(3, "novo_salario = salario + reajuste", "Linha 3: novo salario recebe salario mais reajuste.", "Soma o salário antigo com o reajuste de 15%."),
                    LinhaExplicada(4, "print(f\"o novo salário é R\${novo_salario:.2f}\")", "Linha 4: print com f-string mostrando o novo salário com duas casas decimais.", "Mostra na tela o salário reajustado formatado em Reais.")
                )
            ),
            CodigoOriginalEdson(
                nomeArquivo = "calculadora_do_bando.py",
                titulo = "Divisão de Ouro do Bando e Sobra da Capitã",
                descricaoContexto = "Seu código em C:\\edson_python\\calculadora_do_bando.py (e maquina_de_recompença.py) que divide o ouro entre os homens do bando e calcula o resto da divisão para a Capitã.",
                codigoCompleto = """print("o bando chegou com ouro")
valor = float(input("informe a quantidade de ouro: "))
homens = int(input("informe a quantidade de homens: "))
pagamento = valor / homens
sobra_da_capitan = valor % homens
print(f"cada homem recebeu {pagamento} de ouro")
print(f"a sobra da capitan e de {sobra_da_capitan} de ouro")""",
                linhasExplicadas = listOf(
                    LinhaExplicada(1, "print(\"o bando chegou com ouro\")", "Linha 1: print o bando chegou com ouro.", "Anuncia a chegada do bando com o tesouro."),
                    LinhaExplicada(2, "valor = float(input(\"informe a quantidade de ouro: \"))", "Linha 2: valor recebe float de input informe a quantidade de ouro.", "Lê o total de ouro conquistado."),
                    LinhaExplicada(3, "homens = int(input(\"informe a quantidade de homens: \"))", "Linha 3: homens recebe int de input informe a quantidade de homens.", "Usa int() porque quantidade de pessoas é sempre um número inteiro!"),
                    LinhaExplicada(4, "pagamento = valor / homens", "Linha 4: pagamento recebe valor dividido por homens.", "Calcula a divisão do ouro entre os membros do bando."),
                    LinhaExplicada(5, "sobra_da_capitan = valor % homens", "Linha 5: sobra da capitan recebe valor módulo homens.", "Usa o operador % para descobrir o resto da divisão inteira, que fica para a Capitã!"),
                    LinhaExplicada(6, "print(f\"cada homem recebeu {pagamento} de ouro\")", "Linha 6: print mostrando quanto cada homem recebeu.", "Exibe a parte de cada membro do bando."),
                    LinhaExplicada(7, "print(f\"a sobra da capitan e de {sobra_da_capitan} de ouro\")", "Linha 7: print mostrando a sobra da capitã.", "Exibe o resto da divisão que ficou com a Capitã.")
                )
            )
        ),
        cenariosSimulador = listOf(
            CenarioSimulador(
                titulo = "Simulação 1: Produto de R$ 200,00 na Loja da Luna (ex12.py)",
                entradasSimuladas = "valor_do_produto = 200.00",
                saidaConsole = "Bem-vindo à loja da Luna!\nDigite o valor do produto: 200\nO valor do produto com desconto de 15% é: R$170.00",
                explicacaoExecucao = "15% de 200 é 30.0 (200 * 0.15). Subtraindo 200 - 30, o novo_preco é 170.0, exibido como R$170.00 graças ao :.2f."
            ),
            CenarioSimulador(
                titulo = "Simulação 2: 103 Moedas de Ouro para 10 Homens (calculadora_do_bando.py)",
                entradasSimuladas = "valor = 103 | homens = 10",
                saidaConsole = "o bando chegou com ouro\ninforme a quantidade de ouro: 103\ninforme a quantidade de homens: 10\ncada homem recebeu 10.3 de ouro\na sobra da capitan e de 3.0 de ouro",
                explicacaoExecucao = "Na divisão inteira de 103 por 10, 10 vezes 10 dá 100 e sobram 3! Por isso 103 % 10 resulta em 3.0 de sobra para a Capitã."
            )
        ),
        questoes = listOf(
            QuestaoDesafio(
                id = "m2_q1",
                moduloNumero = 2,
                enunciado = "No código calculadora_do_bando.py, para que serve o símbolo % na linha 'sobra_da_capitan = valor % homens'?",
                codigoApoio = "sobra_da_capitan = valor % homens",
                opcoes = listOf(
                    "Calcula o resto da divisão de 'valor' por 'homens'.",
                    "Calcula 100% da soma dos dois números.",
                    "Multiplica o valor pela quantidade de homens.",
                    "Transforma o número em texto."
                ),
                indiceCorreto = 0,
                explicacaoResposta = "Perfeito! O operador % (módulo) retorna o resto de uma divisão. Se dividirmos 17 moedas para 5 homens, 5x3=15 e sobram 2 (17 % 5 == 2)."
            ),
            QuestaoDesafio(
                id = "m2_q2",
                moduloNumero = 2,
                enunciado = "Em calculadora_do_bando.py, por que usamos int(input(...)) para a variável 'homens' e float(input(...)) para 'valor'?",
                codigoApoio = "valor = float(input(\"informe a quantidade de ouro: \"))\nhomens = int(input(\"informe a quantidade de homens: \"))",
                opcoes = listOf(
                    "Porque a quantidade de pessoas é um número inteiro (int), enquanto o ouro pode ter casas decimais (float).",
                    "Porque o Python proíbe usar float duas vezes no mesmo arquivo.",
                    "Porque int significa internet e float significa flutuante.",
                    "Não há nenhuma diferença entre int e float em Python."
                ),
                indiceCorreto = 0,
                explicacaoResposta = "Exatamente! Pessoas são contadas em números inteiros (1, 2, 3...), logo usamos int(). Valores ou pesos de ouro podem ter frações, então usamos float()."
            ),
            QuestaoDesafio(
                id = "m2_q3",
                moduloNumero = 2,
                enunciado = "No código ex13.py, um funcionário ganha R$ 1.000,00 e recebe 15% de aumento (reajuste = salario * 0.15). Qual será o novo_salario?",
                codigoApoio = "reajuste = 1000 * 0.15\nnovo_salario = 1000 + reajuste",
                opcoes = listOf(
                    "R$ 1150.00",
                    "R$ 850.00",
                    "R$ 1015.00",
                    "R$ 1500.00"
                ),
                indiceCorreto = 0,
                explicacaoResposta = "Isso mesmo! 1000 * 0.15 é igual a 150. Somando 1000 + 150, temos R$ 1150.00."
            ),
            QuestaoDesafio(
                id = "m2_q4",
                moduloNumero = 2,
                enunciado = "O que o código :.2f faz dentro de print(f\"R\${novo_preco:.2f}\") nos arquivos ex12.py e ex13.py?",
                codigoApoio = "print(f\"O valor é: R\${novo_preco:.2f}\")",
                opcoes = listOf(
                    "Formata o número para exibir exatamente 2 casas decimais após o ponto.",
                    "Multiplica o preço por 2 antes de imprimir.",
                    "Divide o número por 2.",
                    "Apaga os dois últimos números do preço."
                ),
                indiceCorreto = 0,
                explicacaoResposta = "Exato! O sufixo :.2f formata um número ponto flutuante (float) arredondando e exibindo sempre duas casas decimais, padrão monetário."
            )
        )
    )
}
