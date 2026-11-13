package com.edson.dominoacessivel.academiapython.modulos

import com.edson.dominoacessivel.academiapython.modelo.CenarioSimulador
import com.edson.dominoacessivel.academiapython.modelo.CodigoOriginalEdson
import com.edson.dominoacessivel.academiapython.modelo.LinhaExplicada
import com.edson.dominoacessivel.academiapython.modelo.ModuloEnsino
import com.edson.dominoacessivel.academiapython.modelo.QuestaoDesafio
import com.edson.dominoacessivel.academiapython.modelo.TopicoTeoria

/**
 * Módulo 7: Banco de Dados SQLite, CRUD Completo, Migração Dinâmica e Automação WhatsApp
 * Baseado nos arquivos: cadastro.py, cadastro.db, estoque.py, despensa.db e erro_despensa.txt
 */
object Modulo07BancoDadosSqlite {

    val modulo = ModuloEnsino(
        numero = 7,
        titulo = "Módulo 7: Banco de Dados SQLite e Automação",
        subtitulo = "CRUD Completo, Auto-Correção de Tabela e WhatsApp (cadastro.py e estoque.py)",
        arquivosOrigem = listOf("cadastro.py", "cadastro.db", "estoque.py", "despensa.db", "erro_despensa.txt"),
        resumoFalado = "Módulo 7: Banco de Dados SQLite, CRUD Completo e Automação com WhatsApp. Estude seus sistemas profissionais cadastro.py e estoque.py, incluindo como você resolveu o erro de coluna registrado em erro_despensa.txt!",
        topicosTeoria = listOf(
            TopicoTeoria(
                titulo = "1. O que é SQLite e o que significa CRUD? (cadastro.py)",
                explicacaoVisual = "O **SQLite** (`import sqlite3`) é um banco de dados relacional que já vem embutido no Python!\n" +
                    "No seu arquivo `cadastro.py`, você implementou todas as 4 operações fundamentais de um sistema comercial (**CRUD**):\n" +
                    "• **C (Create / Criar):** `INSERT INTO cadastro (nome, idade, telefone) VALUES (?, ?, ?)`\n" +
                    "• **R (Read / Ler):** `SELECT * FROM cadastro` e busca com `WHERE nome LIKE ?`\n" +
                    "• **U (Update / Atualizar):** `UPDATE cadastro SET nome = ?, idade = ?, telefone = ? WHERE id = ?`\n" +
                    "• **D (Delete / Deletar):** `DELETE FROM cadastro WHERE id = ?`",
                explicacaoFalada = "Tópico 1: O que é SQLite e o que significa CRUD. No seu arquivo cadastro.py, você construiu um CRUD completo usando sqlite3: Create com INSERT INTO, Read com SELECT, Update com UPDATE SET, e Delete com DELETE FROM!",
                exemploCodigo = "conn = sqlite3.connect('cadastro.db')\ncursor = conn.cursor()\ncursor.execute('INSERT INTO cadastro (nome, idade, telefone) VALUES (?, ?, ?)', (nome, idade, telefone))\nconn.commit()",
                dicaDaLuna = "Dica da Luna: Usar pontos de interrogação (?) nos comandos SQL em vez de f-strings protege seu banco de dados contra ataques de SQL Injection!"
            ),
            TopicoTeoria(
                titulo = "2. O Caso Real de erro_despensa.txt e a Auto-Correção com PRAGMA e ALTER TABLE!",
                explicacaoVisual = "Na sua pasta `c:\\edson_python`, o arquivo `erro_despensa.txt` guardou um erro real que aconteceu:\n" +
                    "`sqlite3.OperationalError: table estoque has no column named minimo`\n" +
                    "**Por que aconteceu?** O banco `despensa.db` tinha sido criado antes sem a coluna `minimo`. E o `CREATE TABLE IF NOT EXISTS` não altera uma tabela que já existe!\n" +
                    "**Como você resolveu em `estoque.py`?** Com uma **Migração Dinâmica** genial:\n" +
                    "1. Consultou as colunas atuais com `cursor.execute(\"PRAGMA table_info(estoque)\")`\n" +
                    "2. Verificou `if 'minimo' not in colunas:`\n" +
                    "3. Adicionou a coluna sem perder os dados com `ALTER TABLE estoque ADD COLUMN minimo INTEGER DEFAULT 1`!",
                explicacaoFalada = "Tópico 2: O Caso Real de erro despensa ponto txt e a Auto-Correção de Banco! O arquivo erro despensa ponto txt mostrava que a tabela estoque não tinha a coluna minimo. Em estoque.py, você criou uma solução de nível sênior: usou PRAGMA table info para checar as colunas existentes e executou ALTER TABLE estoque ADD COLUMN minimo automaticamente!",
                exemploCodigo = "cursor.execute(\"PRAGMA table_info(estoque)\")\ncolunas = [coluna[1] for coluna in cursor.fetchall()]\nif 'minimo' not in colunas:\n    cursor.execute(\"ALTER TABLE estoque ADD COLUMN minimo INTEGER DEFAULT 1\")",
                dicaDaLuna = "Dica da Luna: Essa técnica de atualizar a estrutura do banco sem apagar os dados dos clientes se chama Migração de Banco de Dados (Database Migration)!"
            ),
            TopicoTeoria(
                titulo = "3. Automação de Lista de Compras no WhatsApp e Log de Erros (estoque.py)",
                explicacaoVisual = "No `estoque.py`, quando a quantidade é menor ou igual ao mínimo (`if q <= m:`), o produto entra na `lista_compras`.\n" +
                    "Depois, na função `enviar_zap()`:\n" +
                    "• Codifica o texto para link da internet com `urllib.parse.quote(mensagem)`\n" +
                    "• Toca um aviso sonoro com `winsound.Beep(1000, 500)`\n" +
                    "• Abre o WhatsApp Web já com o número e a lista pronta com `webbrowser.open(link)`!\n" +
                    "• E se der qualquer erro, grava o rastreamento completo em `erro_despensa.txt` usando `traceback.format_exc()`!",
                explicacaoFalada = "Tópico 3: Automação de WhatsApp e Log de Erros. Em estoque.py, você filtrou os itens com estoque baixo, codificou a mensagem com urllib.parse.quote, deu um bip com winsound e abriu o WhatsApp Web com webbrowser.open!",
                exemploCodigo = "texto_codificado = urllib.parse.quote(mensagem)\nlink = f\"https://web.whatsapp.com/send?phone={FONE_ESPOSA}&text={texto_codificado}\"\nwebbrowser.open(link)",
                dicaDaLuna = "Dica da Luna: Gravar erros em um arquivo de log com traceback.format_exc() é essencial para descobrir o que aconteceu mesmo quando você não está olhando para a tela!"
            )
        ),
        codigosOriginais = listOf(
            CodigoOriginalEdson(
                nomeArquivo = "cadastro.py",
                titulo = "Sistema CRUD Completo com SQLite",
                descricaoContexto = "Seu sistema de clientes em C:\\edson_python\\cadastro.py com criação de tabela, inserção, listagem, busca por LIKE, atualização e exclusão.",
                codigoCompleto = """import sqlite3

def criar_tabela():
    conn = sqlite3.connect('cadastro.db')
    cursor = conn.cursor()
    cursor.execute('''
        CREATE TABLE IF NOT EXISTS cadastro (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            nome TEXT NOT NULL,
            idade INTEGER NOT NULL,
            telefone TEXT NOT NULL
        )
    ''')
    conn.commit()
    conn.close()

def buscar_cadastro():
    termo = input("Digite o nome ou telefone para buscar: ")
    conn = sqlite3.connect('cadastro.db')
    cursor = conn.cursor()
    cursor.execute('''
        SELECT * FROM cadastro
        WHERE nome LIKE ? OR telefone LIKE ?
    ''', (f'%{termo}%', f'%{termo}%'))
    resultados = cursor.fetchall()
    conn.close()""",
                linhasExplicadas = listOf(
                    LinhaExplicada(1, "conn = sqlite3.connect('cadastro.db')", "Conecta ao arquivo de banco de dados cadastro ponto db.", "Abre a conexão com o banco SQLite (e cria o arquivo se não existir)."),
                    LinhaExplicada(2, "id INTEGER PRIMARY KEY AUTOINCREMENT", "Coluna id inteira, chave primária com auto incremento.", "Gera automaticamente um número único (1, 2, 3...) para cada pessoa cadastrada."),
                    LinhaExplicada(3, "WHERE nome LIKE ? OR telefone LIKE ?", "Filtro SQL usando o operador LIKE com curingas de porcentagem.", "Permite encontrar um cliente digitando apenas parte do nome ou parte do telefone!"),
                    LinhaExplicada(4, "conn.commit() e conn.close()", "Confirma as alterações no disco com commit e fecha a conexão.", "Garante que os dados inseridos, atualizados ou deletados sejam salvos permanentemente.")
                )
            ),
            CodigoOriginalEdson(
                nomeArquivo = "estoque.py",
                titulo = "Gestor de Despensa com Migração SQLite e WhatsApp",
                descricaoContexto = "Seu sistema avançado em C:\\edson_python\\estoque.py que corrige o banco automaticamente e envia a lista de compras pelo WhatsApp.",
                codigoCompleto = """def conectar():
    conn = sqlite3.connect(DB_NAME)
    cursor = conn.cursor()
    cursor.execute('''
        CREATE TABLE IF NOT EXISTS estoque (
            produto TEXT PRIMARY KEY,
            quantidade INTEGER,
            minimo INTEGER
        )
    ''')
    cursor.execute("PRAGMA table_info(estoque)")
    colunas = [coluna[1] for coluna in cursor.fetchall()]
    if 'minimo' not in colunas:
        print(">>> Corrigindo estrutura do banco de dados...")
        cursor.execute("ALTER TABLE estoque ADD COLUMN minimo INTEGER DEFAULT 1")
    conn.commit()
    return conn""",
                linhasExplicadas = listOf(
                    LinhaExplicada(1, "produto TEXT PRIMARY KEY", "Define o nome do produto como Chave Primária da tabela estoque.", "Garante que não existam dois produtos com o mesmo nome; ao usar INSERT OR REPLACE, atualiza o produto existente!"),
                    LinhaExplicada(2, "cursor.execute(\"PRAGMA table_info(estoque)\")", "Consulta os metadados das colunas da tabela estoque.", "Retorna a lista de colunas que realmente existem no arquivo despensa.db."),
                    LinhaExplicada(3, "if 'minimo' not in colunas: cursor.execute(\"ALTER TABLE...\")", "Se a coluna minimo não existir, executa ALTER TABLE para criá-la.", "Resolve definitivamente o OperationalError registrado em erro_despensa.txt!")
                )
            )
        ),
        cenariosSimulador = listOf(
            CenarioSimulador(
                titulo = "Simulação 1: Auto-Correção do Banco e Alerta de Compras (estoque.py)",
                entradasSimuladas = "Banco antigo sem coluna 'minimo' -> Adiciona Arroz (qtd: 1, mín: 2) e Feijão (qtd: 5, mín: 2)",
                saidaConsole = ">>> Corrigindo estrutura do banco de dados...\n--- STATUS DA DESPENSA ---\nArroz: 1 un. (Mín: 2) [PRECISA COMPRAR]\nFeijao: 5 un. [OK]\n>>> Abrindo o WhatsApp Web com: *Lista de Compras - Despensa* - Arroz (Saldo: 1)",
                explicacaoExecucao = "Ao conectar, o PRAGMA detectou a falta da coluna 'minimo' e rodou o ALTER TABLE. Depois, identificou que Arroz (1 <= 2) precisava de reposição e montou o link do WhatsApp!"
            )
        ),
        questoes = listOf(
            QuestaoDesafio(
                id = "m7_q1",
                moduloNumero = 7,
                enunciado = "No arquivo cadastro.py, para que serve o comando 'conn.commit()' após um INSERT, UPDATE ou DELETE no SQLite?",
                codigoApoio = "cursor.execute('INSERT INTO cadastro VALUES (?, ?, ?)', dados)\nconn.commit()",
                opcoes = listOf(
                    "Confirma e grava permanentemente as alterações feitas na transação dentro do arquivo do banco de dados.",
                    "Apaga todas as tabelas do banco de dados.",
                    "Fecha o programa imediatamente.",
                    "Serve apenas para imprimir os dados na tela."
                ),
                indiceCorreto = 0,
                explicacaoResposta = "Exato! Sem chamar conn.commit(), as alterações de INSERT, UPDATE ou DELETE ficam apenas temporárias e são perdidas ao fechar a conexão."
            ),
            QuestaoDesafio(
                id = "m7_q2",
                moduloNumero = 7,
                enunciado = "Qual comando SQL você usou em estoque.py para adicionar a coluna 'minimo' em uma tabela que já existia, resolvendo o erro de erro_despensa.txt?",
                codigoApoio = "if 'minimo' not in colunas:\n    cursor.execute(\"___________________________\")",
                opcoes = listOf(
                    "ALTER TABLE estoque ADD COLUMN minimo INTEGER DEFAULT 1",
                    "CREATE TABLE estoque minimo",
                    "DELETE FROM estoque WHERE minimo",
                    "SELECT minimo FROM estoque"
                ),
                indiceCorreto = 0,
                explicacaoResposta = "Perfeito! O comando ALTER TABLE ... ADD COLUMN adiciona uma nova coluna à tabela existente preservando todos os dados já salvos."
            ),
            QuestaoDesafio(
                id = "m7_q3",
                moduloNumero = 7,
                enunciado = "No arquivo cadastro.py, por que usamos f'%{termo}%' junto com o operador LIKE na busca?",
                codigoApoio = "SELECT * FROM cadastro WHERE nome LIKE ?\n# parâmetro: f'%{termo}%'",
                opcoes = listOf(
                    "Porque o símbolo % no LIKE funciona como curinga, encontrando o termo em qualquer parte do nome (início, meio ou fim).",
                    "Para calcular o resto da divisão do nome.",
                    "Para dar 100% de desconto ao cliente.",
                    "Para buscar somente se o usuário digitar o nome completo idêntico."
                ),
                indiceCorreto = 0,
                explicacaoResposta = "Certíssimo! No SQL, o '%' dentro do LIKE representa qualquer sequência de caracteres antes ou depois do texto buscado."
            ),
            QuestaoDesafio(
                id = "m7_q4",
                moduloNumero = 7,
                enunciado = "Em estoque.py, para que serve a função urllib.parse.quote(mensagem) antes de abrir o link do WhatsApp Web?",
                codigoApoio = "texto_codificado = urllib.parse.quote(mensagem)\nlink = f\"https://web.whatsapp.com/send?phone={FONE}&text={texto_codificado}\"",
                opcoes = listOf(
                    "Converte espaços, quebras de linha e acentos para o formato seguro de URL (como %20 e %0A) para o link funcionar no navegador.",
                    "Traduz a mensagem do português para o inglês.",
                    "Soma os preços dos produtos da despensa.",
                    "Desconecta a internet do computador."
                ),
                indiceCorreto = 0,
                explicacaoResposta = "Muito bem! URLs não podem conter espaços ou quebras de linha soltos; o urllib.parse.quote codifica tudo perfeitamente para o WhatsApp Web."
            )
        )
    )
}
