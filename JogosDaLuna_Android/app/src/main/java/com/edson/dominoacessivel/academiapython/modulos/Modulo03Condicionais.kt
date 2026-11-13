package com.edson.dominoacessivel.academiapython.modulos

import com.edson.dominoacessivel.academiapython.modelo.CenarioSimulador
import com.edson.dominoacessivel.academiapython.modelo.CodigoOriginalEdson
import com.edson.dominoacessivel.academiapython.modelo.LinhaExplicada
import com.edson.dominoacessivel.academiapython.modelo.ModuloEnsino
import com.edson.dominoacessivel.academiapython.modelo.QuestaoDesafio
import com.edson.dominoacessivel.academiapython.modelo.TopicoTeoria

/**
 * Módulo 3: Estruturas Condicionais (if, elif, else)
 * Baseado nos arquivos: calc.py, calculadora.py, cac.py e loja_da_luna.py
 */
object Modulo03Condicionais {

    val modulo = ModuloEnsino(
        numero = 3,
        titulo = "Módulo 3: Tomada de Decisão (if, elif, else)",
        subtitulo = "Calculadora da Luna e Sistema de Cupons (calc.py, calculadora.py, loja_da_luna.py)",
        arquivosOrigem = listOf("calc.py", "calculadora.py", "cac.py", "loja_da_luna.py"),
        resumoFalado = "Módulo 3: Tomada de Decisão com if, elif e else. Aprenda como o Python escolhe caminhos diferentes na Calculadora da Luna e no Sistema de Cupons da Loja da Luna.",
        topicosTeoria = listOf(
            TopicoTeoria(
                titulo = "1. Como funcionam if, elif e else?",
                explicacaoVisual = "As estruturas condicionais permitem que o programa tome decisões:\n" +
                    "• `if` (SE): Testa a primeira condição. Se for verdadeira, executa o bloco indentado.\n" +
                    "• `elif` (SENÃO SE): Testado apenas se o `if` anterior for falso. Você pode ter vários `elif`!\n" +
                    "• `else` (SENÃO): Executado quando NENHUMA das condições anteriores foi verdadeira.",
                explicacaoFalada = "Tópico 1: Como funcionam if, elif e else. O comando if significa SE. O comando elif significa SENÃO SE, permitindo testar várias opções como mais, menos, vezes e dividir. E o comando else significa SENÃO, capturando qualquer opção inválida!",
                exemploCodigo = "if operacao == \"+\":\n    print(valor1 + valor2)\nelif operacao == \"-\":\n    print(valor1 - valor2)\nelse:\n    print(\"Operação inválida!\")",
                dicaDaLuna = "Dica da Luna: Reparou nos dois sinais de igual (==)? Em Python, um igual (=) guarda valor na variável, e dois iguais (==) COMPARAM se dois valores são iguais!"
            ),
            TopicoTeoria(
                titulo = "2. Protegendo contra a Divisão por Zero (!= 0)",
                explicacaoVisual = "Na matemática e na computação, dividir qualquer número por zero é impossível (gera o erro `ZeroDivisionError`).\n" +
                    "Você resolveu isso de duas formas brilhantes nos seus arquivos:\n" +
                    "1. Em `calc.py`: Usando um `if valor2 != 0:` dentro do `elif operacao == \"/\":`.\n" +
                    "2. Em `calculadora.py`: Usando o **if ternário** em uma única linha:\n" +
                    "`div = valor1 / valor2 if valor2 != 0 else \"Não é possível dividir por zero\"`",
                explicacaoFalada = "Tópico 2: Protegendo contra a divisão por zero. Dividir por zero trava o programa com ZeroDivisionError. Em calc.py, você usou if valor2 diferente de zero, escrito com exclamação igual. E em calculadora.py, você usou uma expressão condicional ternária em uma única linha!",
                exemploCodigo = "div = valor1 / valor2 if valor2 != 0 else \"Não é possível dividir por zero\"",
                dicaDaLuna = "Dica da Luna: O símbolo != significa DIFERENTE DE. Então 'valor2 != 0' lê-se: 'valor2 é diferente de zero'!"
            ),
            TopicoTeoria(
                titulo = "3. Comparando Textos: Cupons da Loja da Luna",
                explicacaoVisual = "Em `loja_da_luna.py`, você usou `if / elif / else` para validar cupons de desconto:\n" +
                    "• `\"DESCONTO10\"` -> multiplica por `0.90` (tira 10%)\n" +
                    "• `\"DESCONTO20\"` -> multiplica por `0.80` (tira 20%)\n" +
                    "• `\"SEM_DESCONTO\"` -> mantém o `preco_original`\n" +
                    "• `else:` -> avisa que o cupom é inválido e mantém o preço original!",
                explicacaoFalada = "Tópico 3: Comparando textos nos Cupons da Loja da Luna. No arquivo loja da luna ponto py, você comparou o texto digitado com DESCONTO10, DESCONTO20 e SEM DESCONTO. Se o cliente digitar um cupom inventado, o programa cai no else e avisa que o cupom é inválido!",
                exemploCodigo = "if codigo_cupom == \"DESCONTO10\":\n    preco_final = preco_original * 0.90\nelif codigo_cupom == \"DESCONTO20\":\n    preco_final = preco_original * 0.80",
                dicaDaLuna = "Dica da Luna: Ao comparar strings em Python, letras maiúsculas e minúsculas são diferentes! Uma boa prática é usar .upper() no input para transformar tudo em maiúsculas automaticamente."
            )
        ),
        codigosOriginais = listOf(
            CodigoOriginalEdson(
                nomeArquivo = "calculadora.py",
                titulo = "Calculadora Completa da Luna (com Ternário)",
                descricaoContexto = "Seu código em C:\\edson_python\\calculadora.py (e calc.py / cac.py) que realiza as 4 operações matemáticas e protege contra divisão por zero.",
                codigoCompleto = """print("Bem-vindo à calculadora da Luna!")
valor1 = float(input("Informe o valor 1: "))
operacao = input("Informe a operação (+, -, *, /): ")
valor2 = float(input("Informe o valor 2: "))

soma = valor1 + valor2
mut = valor1 * valor2
sub = valor1 - valor2
div = valor1 / valor2 if valor2 != 0 else "Não é possível dividir por zero"

if operacao == "+":
    print(f"A soma de {valor1} + {valor2} é {soma}")
elif operacao == "-":
    print(f"A subtração de {valor1} - {valor2} é {sub}")
elif operacao == "*":
    print(f"A multiplicação de {valor1} * {valor2} é {mut}")
elif operacao == "/":
    print(f"A divisão de {valor1} / {valor2} é {div}")
else:
    print("Operação inválida!")""",
                linhasExplicadas = listOf(
                    LinhaExplicada(1, "print(\"Bem-vindo à calculadora da Luna!\")", "Linha 1: print Bem-vindo à calculadora da Luna!", "Exibe o título da calculadora."),
                    LinhaExplicada(2, "valor1 = float(input(\"Informe o valor 1: \"))", "Linha 2: valor1 recebe float de input Informe o valor 1.", "Lê o primeiro número decimal."),
                    LinhaExplicada(3, "operacao = input(\"Informe a operação (+, -, *, /): \")", "Linha 3: operacao recebe input Informe a operação.", "Lê o símbolo da operação desejada como texto (string)."),
                    LinhaExplicada(4, "valor2 = float(input(\"Informe o valor 2: \"))", "Linha 4: valor2 recebe float de input Informe o valor 2.", "Lê o segundo número decimal."),
                    LinhaExplicada(5, "div = valor1 / valor2 if valor2 != 0 else \"Não é possível dividir por zero\"", "Linha 5: div recebe valor1 dividido por valor2 se valor2 diferente de zero, senão mensagem de erro.", "Usa o operador ternário para só dividir quando valor2 não for zero!"),
                    LinhaExplicada(6, "if operacao == \"+\": ... elif ... else:", "Bloco condicional if, elif e else testando a operação.", "Verifica qual símbolo foi digitado e imprime apenas o resultado correspondente ou 'Operação inválida!'.")
                )
            ),
            CodigoOriginalEdson(
                nomeArquivo = "loja_da_luna.py",
                titulo = "Sistema de Cupons da Loja da Luna",
                descricaoContexto = "Seu código em C:\\edson_python\\loja_da_luna.py que aplica descontos de 10%, 20% ou sem desconto conforme o cupom digitado.",
                codigoCompleto = """preco_original = float(input("Informe o preço original do produto: "))
codigo_cupom = input("Informe o código do cupom (DESCONTO10, DESCONTO20, SEM_DESCONTO): ")

if codigo_cupom == "DESCONTO10":
    preco_final = preco_original * 0.90
elif codigo_cupom == "DESCONTO20":
    preco_final = preco_original * 0.80
elif codigo_cupom == "SEM_DESCONTO":
    preco_final = preco_original
else:
    preco_final = preco_original
    print(f"Cupom '{codigo_cupom}' inválido. Nenhum desconto aplicado.")

print(f"{preco_final:.2f}")""",
                linhasExplicadas = listOf(
                    LinhaExplicada(1, "preco_original = float(input(\"Informe o preço original do produto: \"))", "Linha 1: preco original recebe float do input.", "Lê o preço cheio do produto."),
                    LinhaExplicada(2, "codigo_cupom = input(\"Informe o código do cupom...\")", "Linha 2: codigo cupom recebe o texto digitado no input.", "Lê o nome do cupom promocional."),
                    LinhaExplicada(3, "if codigo_cupom == \"DESCONTO10\": preco_final = preco_original * 0.90", "Se codigo cupom igual a DESCONTO10, multiplica por 0.90.", "Aplica 10% de desconto (mantém 90% do preço)."),
                    LinhaExplicada(4, "elif codigo_cupom == \"DESCONTO20\": preco_final = preco_original * 0.80", "Senão se codigo cupom igual a DESCONTO20, multiplica por 0.80.", "Aplica 20% de desconto (mantém 80% do preço)."),
                    LinhaExplicada(5, "else: preco_final = preco_original", "Senão, mantém o preço original e avisa que o cupom é inválido.", "Trata qualquer código desconhecido com segurança."),
                    LinhaExplicada(6, "print(f\"{preco_final:.2f}\")", "Imprime o preço final com duas casas decimais.", "Exibe o valor final formatado.")
                )
            )
        ),
        cenariosSimulador = listOf(
            CenarioSimulador(
                titulo = "Simulação 1: Tentativa de Divisão por Zero (calculadora.py)",
                entradasSimuladas = "valor1 = 50 | operacao = \"/\" | valor2 = 0",
                saidaConsole = "Bem-vindo à calculadora da Luna!\nA divisão de 50.0 / 0.0 é Não é possível dividir por zero",
                explicacaoExecucao = "Graças à condição 'if valor2 != 0', o programa não travou! Ele detectou que valor2 era 0 e exibiu a mensagem amigável."
            ),
            CenarioSimulador(
                titulo = "Simulação 2: Cupom DESCONTO20 em produto de R$ 150,00 (loja_da_luna.py)",
                entradasSimuladas = "preco_original = 150.00 | codigo_cupom = \"DESCONTO20\"",
                saidaConsole = "120.00",
                explicacaoExecucao = "O primeiro if (DESCONTO10) deu falso. O elif (DESCONTO20) deu verdadeiro! Multiplicou 150.00 por 0.80 e imprimiu 120.00."
            )
        ),
        questoes = listOf(
            QuestaoDesafio(
                id = "m3_q1",
                moduloNumero = 3,
                enunciado = "Qual é a diferença entre '=' e '==' em Python, como usado em 'if operacao == \"+\":'?",
                codigoApoio = "operacao = \"+\"\nif operacao == \"+\":",
                opcoes = listOf(
                    "'=' atribui (guarda) um valor em uma variável, enquanto '==' compara se dois valores são iguais.",
                    "'==' serve para somar dois números e '=' serve para subtrair.",
                    "Não existe diferença, ambos fazem a mesma coisa.",
                    "'==' só pode ser usado com números inteiros."
                ),
                indiceCorreto = 0,
                explicacaoResposta = "Exatamente! Um sinal de igual (=) é atribuição; dois sinais de igual (==) fazem uma comparação lógica que retorna True ou False."
            ),
            QuestaoDesafio(
                id = "m3_q2",
                moduloNumero = 3,
                enunciado = "No arquivo loja_da_luna.py, por que multiplicamos preco_original por 0.80 quando o cupom é DESCONTO20?",
                codigoApoio = "elif codigo_cupom == \"DESCONTO20\":\n    preco_final = preco_original * 0.80",
                opcoes = listOf(
                    "Porque tirar 20% de 100% deixa 80% do valor original, e 80% em decimal é 0.80.",
                    "Porque 0.80 significa 80% de desconto.",
                    "Porque o Python soma 80 reais ao produto.",
                    "Porque 0.80 arredonda o número para cima."
                ),
                indiceCorreto = 0,
                explicacaoResposta = "Perfeito! 100% - 20% = 80%, que equivale a multiplicar por 0.80."
            ),
            QuestaoDesafio(
                id = "m3_q3",
                moduloNumero = 3,
                enunciado = "O que significa o operador '!=' usado na linha 'if valor2 != 0:' em calc.py e calculadora.py?",
                codigoApoio = "if valor2 != 0:",
                opcoes = listOf(
                    "Diferente de (verifica se valor2 não é igual a zero).",
                    "Maior ou igual a zero.",
                    "Dividido por zero.",
                    "Obrigatoriamente igual a zero."
                ),
                indiceCorreto = 0,
                explicacaoResposta = "Isso mesmo! O ponto de exclamação seguido de igual (!=) significa 'diferente de' em Python."
            ),
            QuestaoDesafio(
                id = "m3_q4",
                moduloNumero = 3,
                enunciado = "Quando o bloco 'else:' é executado em uma estrutura if / elif / else?",
                codigoApoio = "if condicao1:\n    ...\nelif condicao2:\n    ...\nelse:\n    print(\"Operação inválida!\")",
                opcoes = listOf(
                    "Apenas quando TODAS as condições anteriores (if e elif) forem falsas.",
                    "Sempre que o programa começa.",
                    "Quando a primeira condição do if for verdadeira.",
                    "Apenas quando ocorre um erro de digitação no código."
                ),
                indiceCorreto = 0,
                explicacaoResposta = "Correto! O else é o caminho padrão de segurança, executado somente se nenhum if ou elif anterior tiver sido verdadeiro."
            )
        )
    )
}
