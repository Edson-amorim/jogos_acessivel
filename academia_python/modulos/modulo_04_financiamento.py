"""
Módulo 4: Lógica Financeira e Caça aos Bugs (financiamento.py, emprestimo, emprestimo.py.py)
"""

MODULO = {
    "numero": 4,
    "titulo": "Módulo 4: Financiamento e Regra dos 30%",
    "arquivos": ["financiamento.py", "emprestimo", "emprestimo.py.py", "banco_da_luna.py"],
    "explicacao": (
        "Neste módulo estudamos a aprovação de empréstimo imobiliário no Banco da Luna: "
        "convertemos anos em meses (anos * 12), calculamos a parcela mensal (casa / parcelas) "
        "e verificamos se ela ultrapassa 30% do salário (salario * 0.30). "
        "Também aprendemos a diferença entre o bug lógico de 'emprestimo' e a versão corrigida 'financiamento.py'!"
    ),
    "codigos": [
        {
            "arquivo": "financiamento.py",
            "codigo": (
                'casa = float(input("informe o valor da casa! "))\n'
                'salario = float(input("informe o seu salario! "))\n'
                'anos = int(input("informe a quantidade de anos! "))\n'
                'parcelas = anos * 12\n'
                'parcela_mensal = casa / parcelas\n'
                'limites_parcela = salario * 0.30\n'
                'if parcela_mensal > limites_parcela:\n'
                '    print("emprestimo negado!")\n'
                'else:\n'
                '    print("emprestimo altorizado.")'
            ),
            "explicacao_linhas": [
                "Converte anos para meses multiplicando por 12.",
                "Calcula o teto de 30% do salário com salario * 0.30.",
                "Nega o empréstimo se parcela_mensal > limites_parcela."
            ]
        }
    ],
    "questoes": [
        {
            "pergunta": "Qual condição aprova ou nega corretamente o financiamento em financiamento.py?",
            "opcoes": [
                "if parcela_mensal > limites_parcela: (nega quando a parcela passa de 30% do salário)",
                "if salario < limites_parcela: (compara o salário com 30% dele mesmo)",
                "if anos == 12:"
            ],
            "correta": 0,
            "explicacao": "Muito bem! A parcela mensal não pode superar o limite de 30% do salário."
        }
    ]
}
