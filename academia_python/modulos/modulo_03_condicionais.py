"""
Módulo 3: Condicionais if, elif, else (calc.py, calculadora.py, cac.py, loja_da_luna.py)
"""

MODULO = {
    "numero": 3,
    "titulo": "Módulo 3: Tomada de Decisão (if, elif, else)",
    "arquivos": ["calc.py", "calculadora.py", "cac.py", "loja_da_luna.py"],
    "explicacao": (
        "As condicionais permitem que o programa escolha caminhos diferentes. "
        "Usamos 'if' para a primeira condição, 'elif' para testar outras possibilidades e 'else' quando nenhuma anterior for verdadeira. "
        "Também aprendemos a comparar igualdade com '==' e diferença com '!=', protegendo contra a divisão por zero!"
    ),
    "codigos": [
        {
            "arquivo": "calculadora.py",
            "codigo": (
                'div = valor1 / valor2 if valor2 != 0 else "Não é possível dividir por zero"\n'
                'if operacao == "+":\n'
                '    print(f"A soma de {valor1} + {valor2} é {soma}")\n'
                'elif operacao == "/":\n'
                '    print(f"A divisão de {valor1} / {valor2} é {div}")\n'
                'else:\n'
                '    print("Operação inválida!")'
            ),
            "explicacao_linhas": [
                "Usa expressão condicional ternária para evitar ZeroDivisionError quando valor2 é 0.",
                "Testa qual operação foi escolhida usando if, elif e else."
            ]
        },
        {
            "arquivo": "loja_da_luna.py",
            "codigo": (
                'if codigo_cupom == "DESCONTO10":\n'
                '    preco_final = preco_original * 0.90\n'
                'elif codigo_cupom == "DESCONTO20":\n'
                '    preco_final = preco_original * 0.80\n'
                'else:\n'
                '    preco_final = preco_original'
            ),
            "explicacao_linhas": [
                "Compara o texto do cupom com DESCONTO10 (multiplica por 0.90) e DESCONTO20 (multiplica por 0.80)."
            ]
        }
    ],
    "questoes": [
        {
            "pergunta": "Qual operador usamos em Python para verificar se dois valores são IGUAIS dentro de um if?",
            "opcoes": [
                "Dois sinais de igual (==)",
                "Um sinal de igual (=)",
                "Exclamação igual (!=)"
            ],
            "correta": 0,
            "explicacao": "Exato! '=' atribui valor, enquanto '==' compara se dois valores são iguais."
        },
        {
            "pergunta": "Em calc.py, por que verificamos 'if valor2 != 0:' antes de dividir?",
            "opcoes": [
                "Para evitar o erro de divisão por zero (ZeroDivisionError).",
                "Porque o Python não divide números pares.",
                "Para somar zero ao resultado."
            ],
            "correta": 0,
            "explicacao": "Perfeito! Não existe divisão por zero na matemática, então validamos antes de dividir."
        }
    ]
}
