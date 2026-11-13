"""
Módulo 5: Funções (def), Laços (while / for), Listas e Conjuntos (forca.py)
"""

MODULO = {
    "numero": 5,
    "titulo": "Módulo 5: Funções, Laços e Jogo da Forca",
    "arquivos": ["forca.py"],
    "explicacao": (
        "No arquivo forca.py você estruturou um jogo completo usando funções (def escolher_palavra, def mostrar_progresso, def jogar), "
        "listas para sortear palavras com random.choice(), conjuntos set() para guardar letras certas e erradas sem repetição, "
        "e laços while e for para controlar as tentativas e revelar as letras!"
    ),
    "codigos": [
        {
            "arquivo": "forca.py",
            "codigo": (
                'def escolher_palavra():\n'
                '    palavras = ["python", "programacao", "acessibilidade", "leitor"]\n'
                '    return random.choice(palavras)\n\n'
                'def mostrar_progresso(palavra, letras_certas):\n'
                '    progresso = ""\n'
                '    for letra in palavra:\n'
                '        if letra in letras_certas:\n'
                '            progresso += letra + " "\n'
                '        else:\n'
                '            progresso += "_ "\n'
                '    return progresso.strip()'
            ),
            "explicacao_linhas": [
                "def cria uma função reutilizável e return devolve o valor calculado.",
                "random.choice(palavras) sorteia uma palavra da lista.",
                "for letra in palavra percorre cada caractere da palavra secreta."
            ]
        }
    ],
    "questoes": [
        {
            "pergunta": "Em forca.py, por que usamos set() para letras_certas e letras_erradas?",
            "opcoes": [
                "Porque conjuntos (set) não permitem elementos duplicados e verificam rapidamente se uma letra já foi usada.",
                "Porque set() sorteia números automaticamente.",
                "Porque set() apaga a palavra secreta."
            ],
            "correta": 0,
            "explicacao": "Perfeito! O tipo set() é ideal para guardar palpites únicos sem repetição."
        }
    ]
}
