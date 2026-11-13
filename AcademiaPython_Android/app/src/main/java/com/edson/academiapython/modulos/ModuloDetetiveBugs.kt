package com.edson.academiapython.modulos

import com.edson.academiapython.modelo.CasoBugReal

/**
 * Módulo Especial: Detetive de Bugs Reais da Pasta C:\edson_python
 * Transforma os erros reais encontrados no histórico de estudos (erro_despensa.txt,
 * emprestimo, emprestimo.py.py, .pytest_cache, calc.py) em missões investigativas!
 */
object ModuloDetetiveBugs {

    val casosDeBug = listOf(
        CasoBugReal(
            id = 1,
            arquivoOrigem = "erro_despensa.txt / estoque.py",
            titulo = "Caso 1: O Mistério da Coluna 'minimo' Sumida",
            historiaBug = "Ao tentar salvar um item na despensa, o sistema gravou no log erro_despensa.txt:\n" +
                "sqlite3.OperationalError: table estoque has no column named minimo.",
            codigoComBug = """cursor.execute('''
    CREATE TABLE IF NOT EXISTS estoque (
        produto TEXT PRIMARY KEY,
        quantidade INTEGER,
        minimo INTEGER
    )
''')
cursor.execute("INSERT OR REPLACE INTO estoque (produto, quantidade, minimo) VALUES (?, ?, ?)", (nome, qtd, minimo))""",
            codigoCorrigido = """cursor.execute("PRAGMA table_info(estoque)")
colunas = [coluna[1] for coluna in cursor.fetchall()]
if 'minimo' not in colunas:
    cursor.execute("ALTER TABLE estoque ADD COLUMN minimo INTEGER DEFAULT 1")""",
            pergunta = "Por que apenas colocar 'minimo INTEGER' dentro do CREATE TABLE IF NOT EXISTS não resolveu quando o arquivo despensa.db já existia?",
            opcoes = listOf(
                "Porque 'IF NOT EXISTS' não altera uma tabela que já foi criada no passado; foi preciso usar ALTER TABLE ADD COLUMN!",
                "Porque o SQLite não aceita números inteiros.",
                "Porque o nome da tabela deveria ser cadastro.",
                "Porque faltou apagar o Windows."
            ),
            indiceCorreto = 0,
            licaoMoral = "Lição da Luna: Quando o banco de dados já existe no disco, CREATE TABLE IF NOT EXISTS é ignorado. Para adicionar colunas novas sem perder dados, usamos ALTER TABLE!"
        ),
        CasoBugReal(
            id = 2,
            arquivoOrigem = "emprestimo vs financiamento.py",
            titulo = "Caso 2: O Banco que Aprovava Todo Mundo",
            historiaBug = "No primeiro arquivo 'emprestimo', mesmo quem tentava comprar uma mansão de 10 milhões ganhando 1.000 reais tinha o empréstimo autorizado!",
            codigoComBug = """parcelas = anos * 12
parcela_mensal = casa / parcelas
limites_parcela = salario * 0.30
if salario < limites_parcela:
    print("emprestimo negado!")
else:
    print("emprestimo altorizado.")""",
            codigoCorrigido = """if parcela_mensal > limites_parcela:
    print("emprestimo negado!")
else:
    print("emprestimo altorizado.")""",
            pergunta = "Qual foi a correção exata feita em financiamento.py para o banco parar de aprovar parcelas maiores que 30% do salário?",
            opcoes = listOf(
                "Trocar 'if salario < limites_parcela:' por 'if parcela_mensal > limites_parcela:'.",
                "Trocar 'anos * 12' por 'anos + 12'.",
                "Mudar o limite de 0.30 para 300.",
                "Apagar o comando else."
            ),
            indiceCorreto = 0,
            licaoMoral = "Lição da Luna: Sempre confira se o 'if' está comparando as duas variáveis certas! Comparar o salário com 30% dele mesmo sempre dava Falso."
        ),
        CasoBugReal(
            id = 3,
            arquivoOrigem = "emprestimo.py.py",
            titulo = "Caso 3: Os Dois Prints Colados",
            historiaBug = "Ao rodar emprestimo.py.py, o Python nem pediu os dados e já parou com SyntaxError na linha 9!",
            codigoComBug = """print(f"Valor da parcela mensal: R${'$'} {parcela_mensal:.2f}")print(f"Quantidade de parcelas: {parcelas} meses")""",
            codigoCorrigido = """print(f"Valor da parcela mensal: R${'$'} {parcela_mensal:.2f}")
print(f"Quantidade de parcelas: {parcelas} meses")""",
            pergunta = "O que causou o SyntaxError nessa linha de emprestimo.py.py?",
            opcoes = listOf(
                "Dois comandos print() foram colocados grudados na mesma linha sem quebra de linha entre eles.",
                "O uso de :.2f dentro da f-string.",
                "A palavra R$ dentro das aspas.",
                "O nome da variável parcelas."
            ),
            indiceCorreto = 0,
            licaoMoral = "Lição da Luna: Em Python, cada instrução deve estar na sua própria linha!"
        ),
        CasoBugReal(
            id = 4,
            arquivoOrigem = "test_soma.py / funcao_soma.py",
            titulo = "Caso 4: O Módulo Vazio no Pytest",
            historiaBug = "No cache do pytest (.pytest_cache) da pasta edson_python, o teste falhou com: ImportError: cannot import name 'soma' from 'funcao_soma'.",
            codigoComBug = """# Arquivo funcao_soma.py estava com 0 bytes (em branco)!

# Arquivo test_soma.py:
from funcao_soma import soma
def test_soma_numeros_positivos():
    assert soma(2, 3) == 5""",
            codigoCorrigido = """# Arquivo funcao_soma.py salvo com o código:
def soma(a, b):
    return a + b""",
            pergunta = "Por que o pytest lançou ImportError ao executar test_soma.py?",
            opcoes = listOf(
                "Porque o arquivo funcao_soma.py estava vazio (0 bytes) ou não tinha sido salvo com 'def soma(a, b): return a + b'.",
                "Porque 2 + 3 não é igual a 5.",
                "Porque o pytest não aceita números positivos.",
                "Porque o nome do arquivo tinha a palavra soma."
            ),
            indiceCorreto = 0,
            licaoMoral = "Lição da Luna: Antes de rodar o pytest, certifique-se de salvar (Ctrl+S) o arquivo do módulo onde a função foi criada!"
        ),
        CasoBugReal(
            id = 5,
            arquivoOrigem = "maquina_de_recompença.py",
            titulo = "Caso 5: As Aspas Duplas Extras na f-string",
            historiaBug = "Em maquina_de_recompença.py, a linha 6 tinha um detalhe curioso nas aspas do print!",
            codigoComBug = """print(f"" "cada homem recebeu {pagamento} de ouro")""",
            codigoCorrigido = """print(f"cada homem recebeu {pagamento} de ouro")""",
            pergunta = "O que acontece quando escrevemos print(f\"\" \"cada homem recebeu {pagamento}\") com aspas vazias logo após o f?",
            opcoes = listOf(
                "O 'f' fica preso apenas nas primeiras aspas vazias (f\"\"), e a segunda string pode deixar de ser f-string ou confundir a leitura!",
                "O ouro é multiplicado por dois.",
                "O Python apaga o arquivo.",
                "A cor da tela fica dourada."
            ),
            indiceCorreto = 0,
            licaoMoral = "Lição da Luna: Em uma f-string, coloque o 'f' colado diretamente na string que contém as chaves: f\"cada homem recebeu {pagamento} de ouro\"!"
        )
    )
}
