"""
Módulo 1: Variáveis, Entrada e Saída (soma.py e ex11.py)
"""

MODULO = {
    "numero": 1,
    "titulo": "Módulo 1: Variáveis, Entrada e Saída",
    "arquivos": ["soma.py", "ex11.py"],
    "explicacao": (
        "Neste módulo você aprende que variáveis são caixinhas na memória onde guardamos dados. "
        "A função input() lê o que o usuário digita sempre como texto (string). "
        "Por isso, usamos float() para converter em número decimal antes de somar ou multiplicar, "
        "e usamos print(f'...') com f-strings para exibir o valor das variáveis dentro de chaves {}."
    ),
    "codigos": [
        {
            "arquivo": "soma.py",
            "codigo": (
                'print("máquina de soma de luna!")\n'
                'valor1 = float(input("informe o valor 1! "))\n'
                'valor2 = float(input("informe o valor 2! "))\n'
                'som = (valor1 + valor2)\n'
                'print(f"a soma de {valor1} + {valor2} é {som}")'
            ),
            "explicacao_linhas": [
                "Linha 1: print exibe o título da máquina de soma da Luna.",
                "Linha 2: lê o primeiro valor pelo teclado e converte para número decimal com float().",
                "Linha 3: lê o segundo valor e converte para float.",
                "Linha 4: soma valor1 + valor2 e guarda na variável som.",
                "Linha 5: usa uma f-string para mostrar os números e o resultado final."
            ]
        },
        {
            "arquivo": "ex11.py",
            "codigo": (
                'largura = float(input("informe a largura da parede! "))\n'
                'altura = float(input("informe a altura da parede!"))\n'
                'aria = largura * altura\n'
                'print(f"a ária da parede é {aria}! ")\n'
                'tinta = aria / 2\n'
                'print(f"para pintar essa parede você precisará de {tinta}! ")'
            ),
            "explicacao_linhas": [
                "Linhas 1 e 2: leem a largura e a altura da parede como float.",
                "Linha 3: calcula a área multiplicando largura * altura.",
                "Linha 5: divide a área por 2 para descobrir quantos litros de tinta são necessários."
            ]
        }
    ],
    "questoes": [
        {
            "pergunta": "No arquivo soma.py, por que usamos float(input(...)) em vez de apenas input(...)?",
            "opcoes": [
                "Porque input() devolve texto e float() converte para número decimal para somar.",
                "Porque sem float() o teclado fica bloqueado.",
                "Porque float() serve para apagar variáveis."
            ],
            "correta": 0,
            "explicacao": "Exato! Sem float(), o sinal + juntaria os dois textos (ex: '2' + '3' viraria '23')."
        },
        {
            "pergunta": "Em ex11.py, se uma parede tem largura 4 e altura 3, qual será a área e os litros de tinta?",
            "opcoes": [
                "Área 12.0 e tinta 6.0 litros",
                "Área 7.0 e tinta 3.5 litros",
                "Área 12.0 e tinta 24.0 litros"
            ],
            "correta": 0,
            "explicacao": "Perfeito! 4 * 3 = 12.0 metros quadrados, e 12.0 / 2 = 6.0 litros de tinta."
        }
    ]
}
