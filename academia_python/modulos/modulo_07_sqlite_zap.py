"""
Módulo 7: Banco de Dados SQLite, CRUD, Migração Dinâmica e Automação WhatsApp
Baseado em: cadastro.py, estoque.py, erro_despensa.txt
"""

MODULO = {
    "numero": 7,
    "titulo": "Módulo 7: Banco de Dados SQLite e Automação WhatsApp",
    "arquivos": ["cadastro.py", "cadastro.db", "estoque.py", "despensa.db", "erro_despensa.txt"],
    "explicacao": (
        "Em cadastro.py você implementou o CRUD completo no SQLite (CREATE, INSERT, SELECT com LIKE, UPDATE e DELETE). "
        "E em estoque.py você resolveu o erro de erro_despensa.txt ('no column named minimo') usando PRAGMA table_info "
        "e ALTER TABLE ADD COLUMN, além de enviar a lista de compras automaticamente pelo WhatsApp Web!"
    ),
    "codigos": [
        {
            "arquivo": "estoque.py",
            "codigo": (
                'cursor.execute("PRAGMA table_info(estoque)")\n'
                'colunas = [coluna[1] for coluna in cursor.fetchall()]\n'
                'if "minimo" not in colunas:\n'
                '    cursor.execute("ALTER TABLE estoque ADD COLUMN minimo INTEGER DEFAULT 1")'
            ),
            "explicacao_linhas": [
                "Inspeciona as colunas da tabela estoque e adiciona a coluna minimo dinamicamente sem perder dados!"
            ]
        }
    ],
    "questoes": [
        {
            "pergunta": "Qual comando SQL adicionou a coluna 'minimo' na tabela existente em estoque.py?",
            "opcoes": [
                "ALTER TABLE estoque ADD COLUMN minimo INTEGER DEFAULT 1",
                "DELETE FROM estoque",
                "SELECT * FROM cadastro"
            ],
            "correta": 0,
            "explicacao": "Perfeito! ALTER TABLE ADD COLUMN atualiza a estrutura da tabela preservando os dados."
        }
    ]
}
