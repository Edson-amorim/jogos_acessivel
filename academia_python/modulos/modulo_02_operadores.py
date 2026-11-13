"""
Módulo 2: Operadores Aritméticos, Porcentagem e Resto da Divisão (%)
Baseado em: ex12.py, ex13.py, calculadora_do_bando.py e maquina_de_recompença.py
"""

MODULO = {
    "numero": 2,
    "titulo": "Módulo 2: Porcentagem e Divisão do Ouro (%)",
    "arquivos": ["ex12.py", "ex13.py", "calculadora_do_bando.py", "maquina_de_recompença.py"],
    "explicacao": (
        "Neste módulo aprendemos a calcular porcentagens (multiplicando por 0.15 para 15%), "
        "a formatar valores em Reais com duas casas decimais usando :.2f nas f-strings, "
        "e a usar o operador módulo (%) que calcula o RESTO da divisão inteira — a sobra de ouro da Capitã!"
    ),
    "codigos": [
        {
            "arquivo": "ex12.py e ex13.py",
            "codigo": (
                'valor_do_produto = float(input("Digite o valor do produto: "))\n'
                'desconto = valor_do_produto * 0.15\n'
                'novo_preco = valor_do_produto - desconto\n'
                'print(f"O valor do produto com desconto de 15% é: R${novo_preco:.2f}")'
            ),
            "explicacao_linhas": [
                "Calcula 15% multiplicando por 0.15.",
                "Em ex12.py subtrai o desconto; em ex13.py soma o reajuste salarial.",
                "Usa :.2f para exibir sempre duas casas decimais (centavos)."
            ]
        },
        {
            "arquivo": "calculadora_do_bando.py",
            "codigo": (
                'print("o bando chegou com ouro")\n'
                'valor = float(input("informe a quantidade de ouro: "))\n'
                'homens = int(input("informe a quantidade de homens: "))\n'
                'pagamento = valor / homens\n'
                'sobra_da_capitan = valor % homens\n'
                'print(f"cada homem recebeu {pagamento} de ouro")\n'
                'print(f"a sobra da capitan e de {sobra_da_capitan} de ouro")'
            ),
            "explicacao_linhas": [
                "Usa int() para a quantidade de homens porque pessoas são contadas em números inteiros.",
                "Usa valor / homens para dividir o ouro e valor % homens para descobrir o resto da divisão."
            ]
        }
    ],
    "questoes": [
        {
            "pergunta": "Em calculadora_do_bando.py, o que calcula a linha 'sobra_da_capitan = valor % homens'?",
            "opcoes": [
                "O resto da divisão do ouro pela quantidade de homens.",
                "A multiplicação do ouro pelos homens.",
                "O desconto de 15% da loja."
            ],
            "correta": 0,
            "explicacao": "Certíssimo! O símbolo % em Python é o operador módulo (resto da divisão)."
        },
        {
            "pergunta": "Para que serve :.2f dentro de f'R${novo_salario:.2f}' em ex13.py?",
            "opcoes": [
                "Formata o número decimal com exatamente 2 casas após o ponto.",
                "Multiplica o salário por 2.",
                "Soma 2 reais ao salário."
            ],
            "correta": 0,
            "explicacao": "Perfeito! O formato :.2f garante a exibição correta de centavos."
        }
    ]
}
