"""
Módulo 8: Testes Automatizados com Pytest e Modularização
Baseado em: test_soma.py, funcao_soma.py, funcao_mut.py
"""

MODULO = {
    "numero": 8,
    "titulo": "Módulo 8: Testes Automatizados com Pytest",
    "arquivos": ["test_soma.py", "funcao_soma.py", "funcao_mut.py"],
    "explicacao": (
        "Em test_soma.py você importou a função soma do módulo funcao_soma.py e criou 5 testes automatizados "
        "cobrindo números positivos, negativos, mistos, grandes e opostos usando a palavra-chave assert!"
    ),
    "codigos": [
        {
            "arquivo": "test_soma.py",
            "codigo": (
                'from funcao_soma import soma\n'
                'def test_soma_numeros_positivos():\n'
                '    assert soma(2, 3) == 5\n'
                'def test_soma_opostos():\n'
                '    assert soma(100, -100) == 0'
            ),
            "explicacao_linhas": [
                "Importa a função soma e usa assert para garantir automaticamente que o retorno está correto."
            ]
        }
    ],
    "questoes": [
        {
            "pergunta": "Qual palavra-chave usamos dentro das funções test_* para validar o resultado esperado no pytest?",
            "opcoes": [
                "assert",
                "input",
                "while"
            ],
            "correta": 0,
            "explicacao": "Exato! O comando assert verifica se a expressão é verdadeira."
        }
    ]
}
