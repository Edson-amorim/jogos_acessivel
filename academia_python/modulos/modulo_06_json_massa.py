"""
Módulo 6: Dicionários, JSON, Tratamento de Exceções e Gerador de Massa de Testes
Baseado em: controli.py, estoque.json, gerar_cadastro.py, maça_de_testes.py
"""

MODULO = {
    "numero": 6,
    "titulo": "Módulo 6: Dicionários, JSON e Gerador de CPF",
    "arquivos": ["controli.py", "estoque.json", "gerar_cadastro.py", "maça_de_testes.py"],
    "explicacao": (
        "Em controli.py você usou um dicionário (estoque = {}) para mapear nome do produto -> quantidade, "
        "salvando em disco com json.dump() e lendo com json.load() dentro de blocos try/except. "
        "Já em gerar_cadastro.py você criou um gerador de massa de testes com cálculo matemático real de CPF válido!"
    ),
    "codigos": [
        {
            "arquivo": "controli.py",
            "codigo": (
                'with open(ARQUIVO_ESTOQUE, "w") as arquivo:\n'
                '    json.dump(estoque, arquivo, indent=4)'
            ),
            "explicacao_linhas": [
                "Abre estoque.json em modo escrita ('w') e salva o dicionário formatado com indent=4."
            ]
        },
        {
            "arquivo": "gerar_cadastro.py",
            "codigo": (
                'cpf = [random.randint(0, 9) for _ in range(9)]\n'
                'soma = sum(a * b for a, b in zip(cpf, range(10, 1, -1)))\n'
                'digito_1 = (soma * 10 % 11) % 10\n'
                'cpf.append(digito_1)'
            ),
            "explicacao_linhas": [
                "Gera 9 dígitos aleatórios com list comprehension e calcula o dígito verificador com zip()."
            ]
        }
    ],
    "questoes": [
        {
            "pergunta": "Qual função grava o dicionário Python dentro do arquivo JSON em controli.py?",
            "opcoes": [
                "json.dump(estoque, arquivo, indent=4)",
                "json.load(arquivo)",
                "json.input(estoque)"
            ],
            "correta": 0,
            "explicacao": "Exato! json.dump grava no arquivo e json.load lê do arquivo."
        }
    ]
}
