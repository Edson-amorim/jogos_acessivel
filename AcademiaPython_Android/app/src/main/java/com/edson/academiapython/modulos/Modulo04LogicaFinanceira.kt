package com.edson.academiapython.modulos

import com.edson.academiapython.modelo.CenarioSimulador
import com.edson.academiapython.modelo.CodigoOriginalEdson
import com.edson.academiapython.modelo.LinhaExplicada
import com.edson.academiapython.modelo.ModuloEnsino
import com.edson.academiapython.modelo.QuestaoDesafio
import com.edson.academiapython.modelo.TopicoTeoria

/**
 * Módulo 4: Lógica Financeira e Caça aos Bugs Reais
 * Baseado nos arquivos: financiamento.py, emprestimo, emprestimo.py.py e banco_da_luna.py
 */
object Modulo04LogicaFinanceira {

    val modulo = ModuloEnsino(
        numero = 4,
        titulo = "Módulo 4: Financiamento e Caça aos Bugs",
        subtitulo = "Banco da Luna: Regra dos 30% e Depuração (financiamento.py e emprestimo)",
        arquivosOrigem = listOf("financiamento.py", "emprestimo", "emprestimo.py.py", "banco_da_luna.py"),
        resumoFalado = "Módulo 4: Lógica Financeira e Caça aos Bugs. Aprenda a regra bancária dos 30 por cento do salário no seu arquivo financiamento.py e veja como você corrigiu os bugs dos arquivos emprestimo!",
        topicosTeoria = listOf(
            TopicoTeoria(
                titulo = "1. A Matemática do Financiamento Imobiliário",
                explicacaoVisual = "No seu programa `financiamento.py`, o comprador informa 3 dados:\n" +
                    "1. `casa`: Valor total do imóvel.\n" +
                    "2. `salario`: Renda mensal do comprador.\n" +
                    "3. `anos`: Em quantos anos pretende pagar.\n\n" +
                    "Como as parcelas são mensais, primeiro convertemos anos em meses:\n" +
                    "`parcelas = anos * 12`\n" +
                    "`parcela_mensal = casa / parcelas`\n" +
                    "`limites_parcela = salario * 0.30` (30% do salário)",
                explicacaoFalada = "Tópico 1: A Matemática do Financiamento Imobiliário. Como o pagamento é mensal, multiplicamos a quantidade de anos por 12 meses. Depois dividimos o valor da casa pelo número de parcelas e calculamos o limite de 30 por cento do salário multiplicando o salário por 0 ponto 30.",
                exemploCodigo = "parcelas = anos * 12\nparcela_mensal = casa / parcelas\nlimites_parcela = salario * 0.30",
                dicaDaLuna = "Dica da Luna: Na vida real, os bancos também usam essa regra de ouro: a prestação não pode comprometer mais do que 30% da renda mensal!"
            ),
            TopicoTeoria(
                titulo = "2. A Grande Lição de Bug Lógico: emprestimo vs financiamento.py",
                explicacaoVisual = "Olhando seus arquivos na pasta `c:\\edson_python`, vemos uma evolução incrível de aprendizado!\n" +
                    "• Na primeira tentativa (`emprestimo`), estava escrito:\n" +
                    "`if salario < limites_parcela:`\n" +
                    "Por que isso era um **Bug Lógico**? Porque `limites_parcela` é 30% do salário, e o salário inteiro NUNCA é menor que 30% dele mesmo! O empréstimo era sempre autorizado!\n" +
                    "• Já em `financiamento.py`, você consertou perfeitamente:\n" +
                    "`if parcela_mensal > limites_parcela:` -> Agora sim compara a **parcela da casa** com o **limite de 30%**!",
                explicacaoFalada = "Tópico 2: A Grande Lição de Bug Lógico! No seu primeiro arquivo chamado emprestimo, o if comparava se o salário era menor que 30 por cento do próprio salário, o que nunca acontecia! Já no arquivo financiamento.py, você corrigiu o bug comparando se parcela mensal é maior que limites parcela! Isso é pensar como programador profissional!",
                exemploCodigo = "# ERRADO (arquivo emprestimo):\nif salario < limites_parcela:\n\n# CORRETO (arquivo financiamento.py):\nif parcela_mensal > limites_parcela:",
                dicaDaLuna = "Dica da Luna: Erro de lógica é quando o Python não reclama de erro de digitação, mas a fórmula testa a variável errada. Testar com valores extremos ajuda a pegar esses bugs!"
            ),
            TopicoTeoria(
                titulo = "3. Erro de Sintaxe (SyntaxError) em emprestimo.py.py",
                explicacaoVisual = "No arquivo `emprestimo.py.py`, a última linha ficou com dois comandos `print` grudados:\n" +
                    "`print(f\"Valor...\")print(f\"Quantidade...\")`\n" +
                    "Em Python, cada comando deve ficar na sua própria linha! Quando grudamos dois comandos assim, o Python gera um **SyntaxError** (Erro de Sintaxe) e nem começa a rodar.",
                explicacaoFalada = "Tópico 3: Erro de Sintaxe no arquivo emprestimo ponto py ponto py. Na última linha daquele arquivo, dois comandos print ficaram colados na mesma linha sem quebra de linha. Isso gera um SyntaxError, porque o Python exige cada instrução em uma linha separada.",
                exemploCodigo = "# Gera SyntaxError:\nprint(\"Linha 1\")print(\"Linha 2\")\n\n# Correto:\nprint(\"Linha 1\")\nprint(\"Linha 2\")",
                dicaDaLuna = "Dica da Luna: Sempre olhe o número da linha que o Python aponta no SyntaxError!"
            )
        ),
        codigosOriginais = listOf(
            CodigoOriginalEdson(
                nomeArquivo = "financiamento.py",
                titulo = "Aprovação de Financiamento Imobiliário (Versão Corrigida)",
                descricaoContexto = "Seu código corrigido em C:\\edson_python\\financiamento.py que aprova ou nega a compra da casa com base no limite de 30% do salário.",
                codigoCompleto = """casa = float(input("informe o valor da casa! "))
salario = float(input("informe o seu salario! "))
anos = int(input("informe a quantidade de anos! "))
parcelas = anos * 12
parcela_mensal = casa / parcelas
limites_parcela = salario * 0.30
print(f"\nValor da casa: R${'$'} {casa}")
print(f"Valor da parcela mensal: R${'$'} {parcela_mensal:.2f}")
print(f"Quantidade de parcelas: {parcelas} meses")
if parcela_mensal > limites_parcela:
    print("emprestimo negado!")
else:
    print("emprestimo altorizado.")""",
                linhasExplicadas = listOf(
                    LinhaExplicada(1, "casa = float(input(\"informe o valor da casa! \"))", "Linha 1: casa recebe float do input valor da casa.", "Lê o valor do imóvel."),
                    LinhaExplicada(2, "salario = float(input(\"informe o seu salario! \"))", "Linha 2: salario recebe float do input salário.", "Lê o salário mensal do comprador."),
                    LinhaExplicada(3, "anos = int(input(\"informe a quantidade de anos! \"))", "Linha 3: anos recebe int do input quantidade de anos.", "Lê em quantos anos o financiamento será pago."),
                    LinhaExplicada(4, "parcelas = anos * 12", "Linha 4: parcelas recebe anos vezes 12.", "Converte o prazo de anos para meses."),
                    LinhaExplicada(5, "parcela_mensal = casa / parcelas", "Linha 5: parcela mensal recebe casa dividido por parcelas.", "Calcula quanto custará cada prestação mensal."),
                    LinhaExplicada(6, "limites_parcela = salario * 0.30", "Linha 6: limites parcela recebe salario vezes 0 ponto 30.", "Calcula o teto máximo permitido para a parcela (30% do salário)."),
                    LinhaExplicada(7, "if parcela_mensal > limites_parcela:", "Linha 10: se parcela mensal for maior que limites parcela, imprime empréstimo negado, senão autorizado.", "Toma a decisão bancária comparando a prestação com o limite salarial.")
                )
            )
        ),
        cenariosSimulador = listOf(
            CenarioSimulador(
                titulo = "Simulação 1: Financiamento Aprovado (Casa R$ 120.000, Salário R$ 4.000, 10 anos)",
                entradasSimuladas = "casa = 120000 | salario = 4000 | anos = 10",
                saidaConsole = "Valor da casa: R$ 120000.0\nValor da parcela mensal: R$ 1000.00\nQuantidade de parcelas: 120 meses\nemprestimo altorizado.",
                explicacaoExecucao = "Em 10 anos são 120 meses. 120.000 / 120 = R$ 1.000,00 por mês. Como 30% de R$ 4.000 é R$ 1.200,00, a parcela (1.000) não ultrapassa o limite (1.200), então é autorizado!"
            ),
            CenarioSimulador(
                titulo = "Simulação 2: Financiamento Negado (Casa R$ 240.000, Salário R$ 3.000, 10 anos)",
                entradasSimuladas = "casa = 240000 | salario = 3000 | anos = 10",
                saidaConsole = "Valor da casa: R$ 240000.0\nValor da parcela mensal: R$ 2000.00\nQuantidade de parcelas: 120 meses\nemprestimo negado!",
                explicacaoExecucao = "A parcela ficou em R$ 2.000,00, mas 30% do salário de R$ 3.000 é apenas R$ 900,00. Como 2000 > 900 é verdadeiro, o empréstimo é negado!"
            )
        ),
        questoes = listOf(
            QuestaoDesafio(
                id = "m4_q1",
                moduloNumero = 4,
                enunciado = "No arquivo financiamento.py, por que multiplicamos a variável 'anos' por 12?",
                codigoApoio = "parcelas = anos * 12\nparcela_mensal = casa / parcelas",
                opcoes = listOf(
                    "Para converter a quantidade de anos no número total de parcelas mensais (já que 1 ano tem 12 meses).",
                    "Para cobrar 12% de juros ao mês.",
                    "Porque todo financiamento dura no máximo 12 meses.",
                    "Para dividir o salário por 12."
                ),
                indiceCorreto = 0,
                explicacaoResposta = "Exato! Como o salário é mensal e as prestações são pagas todo mês, precisamos saber quantos meses existem no prazo escolhido."
            ),
            QuestaoDesafio(
                id = "m4_q2",
                moduloNumero = 4,
                enunciado = "Qual era o bug lógico na primeira versão do arquivo 'emprestimo' que você corrigiu depois em 'financiamento.py'?",
                codigoApoio = "# Versão antiga:\nif salario < limites_parcela:\n\n# Versão corrigida:\nif parcela_mensal > limites_parcela:",
                opcoes = listOf(
                    "A versão antiga comparava o salário com 30% do próprio salário em vez de comparar a parcela mensal com o limite.",
                    "A versão antiga somava o salário com o valor da casa.",
                    "A versão antiga não tinha o comando print.",
                    "A versão antiga multiplicava anos por 365."
                ),
                indiceCorreto = 0,
                explicacaoResposta = "Perfeito! Em 'financiamento.py' você comparou corretamente 'parcela_mensal > limites_parcela'."
            ),
            QuestaoDesafio(
                id = "m4_q3",
                moduloNumero = 4,
                enunciado = "Se uma pessoa ganha R$ 5.000,00 por mês, qual será o valor máximo permitido para a parcela mensal (limites_parcela = salario * 0.30)?",
                codigoApoio = "limites_parcela = 5000 * 0.30",
                opcoes = listOf(
                    "R$ 1.500,00",
                    "R$ 3.000,00",
                    "R$ 500,00",
                    "R$ 150,00"
                ),
                indiceCorreto = 0,
                explicacaoResposta = "Certíssimo! 30% de 5.000 (5000 * 0.30) é igual a R$ 1.500,00."
            ),
            QuestaoDesafio(
                id = "m4_q4",
                moduloNumero = 4,
                enunciado = "No arquivo emprestimo.py.py, dois comandos print ficaram na mesma linha: print(...)print(...). Que tipo de erro o Python acusa nesse caso?",
                codigoApoio = "print(f\"Valor: {parcela_mensal:.2f}\")print(f\"Parcelas: {parcelas}\")",
                opcoes = listOf(
                    "SyntaxError (Erro de Sintaxe), pois falta quebra de linha entre os dois comandos.",
                    "ZeroDivisionError (Erro de divisão por zero).",
                    "FileNotFoundError (Arquivo não encontrado).",
                    "Nenhum erro, o Python aceita comandos colados sem espaço."
                ),
                indiceCorreto = 0,
                explicacaoResposta = "Muito bem! Colar dois comandos na mesma linha viola a gramática da linguagem Python, disparando um SyntaxError."
            )
        )
    )
}
