package com.edson.dominoacessivel.academiapython.modulos

import com.edson.dominoacessivel.academiapython.modelo.CenarioSimulador
import com.edson.dominoacessivel.academiapython.modelo.CodigoOriginalEdson
import com.edson.dominoacessivel.academiapython.modelo.LinhaExplicada
import com.edson.dominoacessivel.academiapython.modelo.ModuloEnsino
import com.edson.dominoacessivel.academiapython.modelo.QuestaoDesafio
import com.edson.dominoacessivel.academiapython.modelo.TopicoTeoria

/**
 * Módulo 8: Testes Automatizados com Pytest e Modularização
 * Baseado nos arquivos: test_soma.py, funcao_soma.py e funcao_mut.py
 */
object Modulo08TestesPytest {

    val modulo = ModuloEnsino(
        numero = 8,
        titulo = "Módulo 8: Testes Automatizados com Pytest",
        subtitulo = "Garantindo Qualidade com assert e Módulos (test_soma.py e funcao_soma.py)",
        arquivosOrigem = listOf("test_soma.py", "funcao_soma.py", "funcao_mut.py"),
        resumoFalado = "Módulo 8: Testes Automatizados com Pytest e Modularização. Aprenda como testar suas funções automaticamente com assert e pytest usando o seu arquivo test_soma.py!",
        topicosTeoria = listOf(
            TopicoTeoria(
                titulo = "1. Separando o Código em Módulos (from funcao_soma import soma)",
                explicacaoVisual = "Na primeira linha de `test_soma.py`, você escreveu:\n" +
                    "`from funcao_soma import soma`\n" +
                    "O que isso significa?\n" +
                    "Em Python, cada arquivo `.py` é um **módulo**! Quando você cria a função `def soma(a, b): return a + b` dentro do arquivo `funcao_soma.py`, qualquer outro arquivo pode importá-la e usá-la!\n" +
                    "Curiosidade da sua pasta `c:\\edson_python`: O arquivo `funcao_soma.py` tinha ficado vazio (0 bytes) depois que você rodou o pytest, gerando um `ImportError` salvo no cache do `.pytest_cache`! Aqui nós restauramos o seu `funcao_soma.py` e `funcao_mut.py`!",
                explicacaoFalada = "Tópico 1: Separando o código em módulos. Em Python, cada arquivo ponto py é um módulo. Quando você escreve from funcao soma import soma no arquivo test soma ponto py, o Python busca a função soma dentro do arquivo funcao soma ponto py!",
                exemploCodigo = "# Arquivo funcao_soma.py:\ndef soma(a, b):\n    return a + b\n\n# Arquivo test_soma.py:\nfrom funcao_soma import soma",
                dicaDaLuna = "Dica da Luna: É exatamente por isso que dividir um sistema grande em módulos facilita tanto a manutenção e os testes!"
            ),
            TopicoTeoria(
                titulo = "2. Como funciona o Pytest e a palavra-chave assert?",
                explicacaoVisual = "O **pytest** é a ferramenta mais usada no mundo Python para testes automatizados.\n" +
                    "Regras do pytest que você aplicou em `test_soma.py`:\n" +
                    "1. O nome do arquivo começa com `test_` (`test_soma.py`).\n" +
                    "2. O nome de cada função de teste começa com `def test_...()`.\n" +
                    "3. Dentro do teste, usamos o comando **`assert`** (afirmar/garantir):\n" +
                    "   `assert soma(2, 3) == 5`\n" +
                    "   • Se a comparação for `True`, o teste **PASSA (PASSED)**!\n" +
                    "   • Se for `False`, o pytest aponta exatamente qual teste **FALHOU (AssertionError)**!",
                explicacaoFalada = "Tópico 2: Como funciona o Pytest e o comando assert. No pytest, criamos funções começando com test underline e usamos a palavra assert para afirmar qual deve ser o resultado. Por exemplo, assert soma de 2 e 3 igual igual a 5. Se der 5, o teste passa! Se der diferente, o pytest avisa na hora!",
                exemploCodigo = "def test_soma_numeros_positivos():\n    assert soma(2, 3) == 5",
                dicaDaLuna = "Dica da Luna: Um teste automatizado roda em milissegundos e substitui ter que digitar valores manualmente no input toda vez que você altera o código!"
            ),
            TopicoTeoria(
                titulo = "3. Testando Casos de Borda (Positivos, Negativos, Grandes e Opostos)",
                explicacaoVisual = "Veja que aula de Engenharia de Software você deu em `test_soma.py`! Você não testou apenas `2 + 3`, mas criou **5 cenários diferentes**:\n" +
                    "1. **Positivos:** `soma(2, 3) == 5`\n" +
                    "2. **Negativos:** `soma(-3, -5) == -8`\n" +
                    "3. **Positivo e Negativo:** `soma(8, -3) == 5`\n" +
                    "4. **Números Grandes:** `soma(10000, 20000) == 30000`\n" +
                    "5. **Números Opostos (resultado zero):** `soma(100, -100) == 0`",
                explicacaoFalada = "Tópico 3: Testando Casos de Borda. No seu arquivo test soma ponto py, você criou cinco testes cobrindo números positivos, números negativos, mistura de positivo com negativo, números grandes e números opostos que resultam em zero! Isso se chama cobertura de casos de borda!",
                exemploCodigo = "def test_soma_opostos():\n    assert soma(100, -100) == 0",
                dicaDaLuna = "Dica da Luna: Testar números negativos, zero e números grandes é o segredo para garantir que uma função matemática nunca falhe em produção!"
            )
        ),
        codigosOriginais = listOf(
            CodigoOriginalEdson(
                nomeArquivo = "test_soma.py",
                titulo = "Suíte de Testes Automatizados com Pytest",
                descricaoContexto = "Seu arquivo de testes unitários em C:\\edson_python\\test_soma.py junto com o módulo funcao_soma.py.",
                codigoCompleto = """# Módulo: funcao_soma.py
def soma(a, b):
    return a + b

# Módulo: test_soma.py
from funcao_soma import soma

def test_soma_numeros_positivos():
    assert soma(2, 3) == 5

def test_soma_numeros_negativos():
    assert soma(-3, -5) == -8

def test_soma_numero_positivo_e_negativo():
    assert soma(8, -3) == 5

def test_soma_numeros_grandes():
    assert soma(10000, 20000) == 30000

def test_soma_opostos():
    assert soma(100, -100) == 0""",
                linhasExplicadas = listOf(
                    LinhaExplicada(1, "def soma(a, b): return a + b", "Função soma no módulo funcao_soma.py.", "Recebe dois parâmetros (a e b) e retorna a soma deles."),
                    LinhaExplicada(2, "from funcao_soma import soma", "Importa a função soma do arquivo funcao_soma.py.", "Conecta o arquivo de teste ao módulo onde está a lógica."),
                    LinhaExplicada(3, "def test_soma_numeros_positivos(): assert soma(2, 3) == 5", "Teste de números positivos: garante que 2 mais 3 retorna 5.", "Primeiro teste unitário da bateria."),
                    LinhaExplicada(4, "def test_soma_numeros_negativos(): assert soma(-3, -5) == -8", "Teste de números negativos: garante que menos 3 com menos 5 dá menos 8.", "Verifica se a regra de sinais funciona corretamente."),
                    LinhaExplicada(5, "def test_soma_opostos(): assert soma(100, -100) == 0", "Teste de números opostos: garante que 100 mais menos 100 resulta em zero.", "Valida o elemento neutro/anulação na soma.")
                )
            )
        ),
        cenariosSimulador = listOf(
            CenarioSimulador(
                titulo = "Simulação: Executando 'pytest' no Terminal",
                entradasSimuladas = "Comando: pytest test_soma.py -v",
                saidaConsole = "============================= test session starts =============================\ncollected 5 items\n\ntest_soma.py::test_soma_numeros_positivos PASSED                        [ 20%]\ntest_soma.py::test_soma_numeros_negativos PASSED                        [ 40%]\ntest_soma.py::test_soma_numero_positivo_e_negativo PASSED               [ 60%]\ntest_soma.py::test_soma_numeros_grandes PASSED                          [ 80%]\ntest_soma.py::test_soma_opostos PASSED                                  [100%]\n\n============================== 5 passed in 0.03s ==============================",
                explicacaoExecucao = "O pytest descobriu automaticamente todas as 5 funções que começam com 'test_', executou cada 'assert' e confirmou 100% de aprovação em 0.03 segundos!"
            )
        ),
        questoes = listOf(
            QuestaoDesafio(
                id = "m8_q1",
                moduloNumero = 8,
                enunciado = "No arquivo test_soma.py, qual palavra-chave do Python é usada dentro de cada função de teste para verificar se o resultado está correto?",
                codigoApoio = "def test_soma_numeros_positivos():\n    ______ soma(2, 3) == 5",
                opcoes = listOf(
                    "assert",
                    "input",
                    "while",
                    "break"
                ),
                indiceCorreto = 0,
                explicacaoResposta = "Exato! A palavra-chave 'assert' verifica se a condição a seguir é verdadeira. Se for falsa, levanta um AssertionError acusando falha no teste."
            ),
            QuestaoDesafio(
                id = "m8_q2",
                moduloNumero = 8,
                enunciado = "Qual convenção de nomes o pytest exige para descobrir as funções de teste automaticamente?",
                codigoApoio = "def test_soma_opostos():\n    assert soma(100, -100) == 0",
                opcoes = listOf(
                    "Os nomes dos arquivos e das funções de teste devem começar com o prefixo 'test_'.",
                    "Todas as funções devem se chamar apenas 'main'.",
                    "Os arquivos devem terminar com a extensão .txt.",
                    "As funções precisam ter números aleatórios no nome."
                ),
                indiceCorreto = 0,
                explicacaoResposta = "Perfeito! O pytest procura arquivos 'test_*.py' e funções 'def test_*()' automaticamente."
            ),
            QuestaoDesafio(
                id = "m8_q3",
                moduloNumero = 8,
                enunciado = "O que faz a linha 'from funcao_soma import soma' no início do arquivo test_soma.py?",
                codigoApoio = "from funcao_soma import soma",
                opcoes = listOf(
                    "Importa a função 'soma' que está definida dentro do módulo (arquivo) funcao_soma.py.",
                    "Cria um banco de dados chamado funcao_soma.",
                    "Apaga a função soma do computador.",
                    "Instala o Python na internet."
                ),
                indiceCorreto = 0,
                explicacaoResposta = "Isso mesmo! 'from modulo import funcao' permite trazer funções de outros arquivos .py, mantendo o projeto organizado em módulos."
            ),
            QuestaoDesafio(
                id = "m8_q4",
                moduloNumero = 8,
                enunciado = "Em test_soma.py, se o arquivo funcao_soma.py estiver vazio (0 bytes, sem 'def soma'), qual erro o pytest mostra ao tentar importar?",
                codigoApoio = "from funcao_soma import soma\n# E funcao_soma.py está em branco!",
                opcoes = listOf(
                    "ImportError: cannot import name 'soma' from 'funcao_soma'",
                    "ZeroDivisionError: division by zero",
                    "sqlite3.OperationalError",
                    "5 passed in 0.01s"
                ),
                indiceCorreto = 0,
                explicacaoResposta = "Na mosca! Inclusive esse foi exatamente o erro que ficou salvo no .pytest_cache da sua pasta c:\\edson_python quando o arquivo funcao_soma.py ficou vazio!"
            )
        )
    )
}
