package com.edson.academiapython.modulos

import com.edson.academiapython.modelo.CenarioSimulador
import com.edson.academiapython.modelo.CodigoOriginalEdson
import com.edson.academiapython.modelo.LinhaExplicada
import com.edson.academiapython.modelo.ModuloEnsino
import com.edson.academiapython.modelo.QuestaoDesafio
import com.edson.academiapython.modelo.TopicoTeoria

/**
 * Módulo 6: Dicionários, Arquivos JSON, Tratamento de Exceções e Gerador de Massa de Testes
 * Baseado nos arquivos: controli.py, estoque.json, gerar_cadastro.py, gerador_cadatro.py, maça_de_testes.py e massa_teste_cadastro.txt
 */
object Modulo06DicionariosJsonMassa {

    val modulo = ModuloEnsino(
        numero = 6,
        titulo = "Módulo 6: Dicionários, JSON e Gerador de CPF",
        subtitulo = "Estoque em JSON e Massa de Testes com CPF Válido (controli.py e gerar_cadastro.py)",
        arquivosOrigem = listOf("controli.py", "estoque.json", "gerar_cadastro.py", "maça_de_testes.py", "massa_teste_cadastro.txt"),
        resumoFalado = "Módulo 6: Dicionários, Arquivos JSON, Tratamento de Erros e Gerador de Massa de Testes com CPF válido. Baseado nos seus códigos controli.py e gerar_cadastro.py!",
        topicosTeoria = listOf(
            TopicoTeoria(
                titulo = "1. Dicionários em Python ({chave: valor}) no Controle de Estoque",
                explicacaoVisual = "No arquivo `controli.py`, você usou um **Dicionário** (`estoque = {}`) para relacionar o **nome do produto** (chave) com a sua **quantidade** (valor):\n" +
                    "• `estoque[nome] = quantidade` -> Cadastra um novo produto.\n" +
                    "• `estoque[nome] += quantidade` -> Soma novas unidades a um produto que já existe.\n" +
                    "• `for nome, quantidade in estoque.items():` -> Percorre todos os pares de produto e quantidade!",
                explicacaoFalada = "Tópico 1: Dicionários em Python no Controle de Estoque. Um dicionário guarda pares de chave e valor entre chaves. No seu arquivo controli.py, a chave é o nome do produto e o valor é a quantidade no estoque! Com estoque ponto items, você lista todos os produtos e suas quantidades.",
                exemploCodigo = "estoque = {\"arroz\": 10, \"feijao\": 5}\nif nome in estoque:\n    estoque[nome] += quantidade\nelse:\n    estoque[nome] = quantidade",
                dicaDaLuna = "Dica da Luna: O operador 'in' em um dicionário verifica rapidamente se aquela chave (nome do produto) já está cadastrada!"
            ),
            TopicoTeoria(
                titulo = "2. Salvando Dados em JSON e Tratando Erros (try / except)",
                explicacaoVisual = "Se o programa fechar, as variáveis da memória somem. Para não perder os dados, em `controli.py` você usou a biblioteca `json`:\n" +
                    "• `json.dump(estoque, arquivo, indent=4)` -> Salva o dicionário no arquivo `estoque.json`.\n" +
                    "• `estoque = json.load(arquivo)` -> Carrega os dados de volta quando o programa abre.\n" +
                    "E se o arquivo ainda não existir ou o usuário digitar texto na quantidade? Você usou **`try / except`** (`FileNotFoundError`, `json.JSONDecodeError`, `ValueError`) para o programa nunca travar!",
                explicacaoFalada = "Tópico 2: Salvando dados em JSON e tratando erros com try e except. Com json.dump você grava o dicionário no arquivo estoque.json, e com json.load você carrega de volta! E usando try e except, seu programa captura erros como FileNotFoundError e ValueError sem fechar na cara do usuário!",
                exemploCodigo = "try:\n    with open(\"estoque.json\", \"r\") as arquivo:\n        estoque = json.load(arquivo)\nexcept FileNotFoundError:\n    estoque = {}",
                dicaDaLuna = "Dica da Luna: O bloco 'with open(...) as arquivo:' fecha o arquivo automaticamente assim que termina a leitura ou gravação!"
            ),
            TopicoTeoria(
                titulo = "3. Gerador de Massa de Testes e Algoritmo Real do CPF (gerar_cadastro.py)",
                explicacaoVisual = "Em `gerar_cadastro.py` (e `maça_de_testes.py`), você criou uma ferramenta digna de Engenheiro de Testes (QA)!\n" +
                    "• **Compreensão de Lista:** `cpf = [random.randint(0, 9) for _ in range(9)]` gera os 9 primeiros dígitos.\n" +
                    "• **Cálculo dos Dígitos Verificadores com `zip`:**\n" +
                    "`soma = sum(a * b for a, b in zip(cpf, range(10, 1, -1)))`\n" +
                    "`digito_1 = (soma * 10 % 11) % 10`\n" +
                    "• **Gerador de Senhas:** Usa `string.ascii_letters + string.digits` e grava uma tabela alinhada com `.ljust(25)` em `massa_teste_cadastro.txt`!",
                explicacaoFalada = "Tópico 3: Gerador de Massa de Testes e Algoritmo do CPF. No seu código gerar cadastro ponto py, você gerou CPFs matematicamente válidos calculando os dois dígitos verificadores com zip e range decrescente, gerou e-mails da UFRPE, senhas alfanuméricas de 8 dígitos e salvou tudo alinhado em massa teste cadastro ponto txt!",
                exemploCodigo = "cpf = [random.randint(0, 9) for _ in range(9)]\nsoma = sum(a * b for a, b in zip(cpf, range(10, 1, -1)))\ndigito_1 = (soma * 10 % 11) % 10",
                dicaDaLuna = "Dica da Luna: A função zip() junta duas sequências par a par: o primeiro dígito do CPF multiplica por 10, o segundo por 9, até o nono multiplicar por 2!"
            )
        ),
        codigosOriginais = listOf(
            CodigoOriginalEdson(
                nomeArquivo = "controli.py",
                titulo = "Sistema de Controle de Estoque com JSON",
                descricaoContexto = "Seu sistema completo em C:\\edson_python\\controli.py com menu interativo, dicionário e persistência no arquivo estoque.json.",
                codigoCompleto = """import json

ARQUIVO_ESTOQUE = "estoque.json"
estoque = {}

def carregar_estoque():
    global estoque
    try:
        with open(ARQUIVO_ESTOQUE, 'r') as arquivo:
            estoque = json.load(arquivo)
        print("Estoque carregado com sucesso.")
    except FileNotFoundError:
        print("Arquivo de estoque não encontrado. Iniciando com estoque vazio.")
    except json.JSONDecodeError:
        print("Erro ao decodificar o arquivo de estoque. Iniciando com estoque vazio.")

def salvar_estoque():
    with open(ARQUIVO_ESTOQUE, 'w') as arquivo:
        json.dump(estoque, arquivo, indent=4)
    print("Estoque salvo com sucesso!")

def adicionar_produto(nome, quantidade):
    if nome in estoque:
        estoque[nome] += quantidade
    else:
        estoque[nome] = quantidade
    salvar_estoque()""",
                linhasExplicadas = listOf(
                    LinhaExplicada(1, "import json", "Linha 1: import json.", "Importa o módulo nativo do Python para trabalhar com arquivos JSON."),
                    LinhaExplicada(2, "estoque = {}", "Declaração do dicionário vazio estoque.", "Cria o dicionário que guardará cada produto e sua quantidade."),
                    LinhaExplicada(3, "with open(ARQUIVO_ESTOQUE, 'r') as arquivo: estoque = json.load(arquivo)", "Abre o arquivo em modo leitura 'r' e carrega o JSON.", "Lê os dados salvos no disco para dentro do dicionário Python."),
                    LinhaExplicada(4, "except FileNotFoundError:", "Captura a exceção caso o arquivo ainda não exista.", "Evita que o programa trave na primeira vez que é aberto."),
                    LinhaExplicada(5, "with open(ARQUIVO_ESTOQUE, 'w') as arquivo: json.dump(estoque, arquivo, indent=4)", "Abre o arquivo em modo escrita 'w' e salva com json ponto dump.", "Grava o dicionário atualizado no arquivo com recuo de 4 espaços para ficar legível.")
                )
            ),
            CodigoOriginalEdson(
                nomeArquivo = "gerar_cadastro.py",
                titulo = "Gerador de Massa de Testes (CPF, E-mail UFRPE, Matrícula e Senha)",
                descricaoContexto = "Seu gerador automático de cadastros para testes de software em C:\\edson_python\\gerar_cadastro.py.",
                codigoCompleto = """import random
import string

def gerar_cpf_valido():
    cpf = [random.randint(0, 9) for _ in range(9)]
    soma = sum(a * b for a, b in zip(cpf, range(10, 1, -1)))
    digito_1 = (soma * 10 % 11) % 10
    cpf.append(digito_1)

    soma = sum(a * b for a, b in zip(cpf, range(11, 1, -1)))
    digito_2 = (soma * 10 % 11) % 10
    cpf.append(digito_2)
    return ''.join(map(str, cpf))

def gerar_senha(tamanho=8):
    caracteres = string.ascii_letters + string.digits
    return ''.join(random.choice(caracteres) for _ in range(tamanho))""",
                linhasExplicadas = listOf(
                    LinhaExplicada(1, "cpf = [random.randint(0, 9) for _ in range(9)]", "List comprehension gerando 9 números aleatórios de 0 a 9.", "Cria uma lista com os 9 primeiros dígitos do CPF."),
                    LinhaExplicada(2, "soma = sum(a * b for a, b in zip(cpf, range(10, 1, -1)))", "Soma a multiplicação de cada dígito pelos pesos de 10 até 2.", "Implementa a regra oficial da Receita Federal para o primeiro dígito verificador."),
                    LinhaExplicada(3, "digito_1 = (soma * 10 % 11) % 10", "Calcula o primeiro dígito verificador usando resto da divisão por 11 e por 10.", "Garante que se o resto por 11 der 10, o dígito vira 0."),
                    LinhaExplicada(4, "return ''.join(map(str, cpf))", "Converte os 11 números da lista para texto e junta tudo sem espaços.", "Retorna o CPF completo como uma string de 11 dígitos."),
                    LinhaExplicada(5, "caracteres = string.ascii_letters + string.digits", "Junta todas as letras maiúsculas, minúsculas e dígitos de 0 a 9.", "Prepara o alfabeto para sortear senhas seguras de 8 caracteres.")
                )
            )
        ),
        cenariosSimulador = listOf(
            CenarioSimulador(
                titulo = "Simulação 1: Adicionando e Vendendo no Estoque JSON (controli.py)",
                entradasSimuladas = "1. Adicionar 'cafe' (20 un.) | 2. Vender 'cafe' (5 un.)",
                saidaConsole = "Produto 'cafe' adicionado ao estoque com 20 unidade(s).\nEstoque salvo com sucesso!\nVenda de 5 unidade(s) de 'cafe' realizada. Restam: 15.\nConteúdo de estoque.json: {\n    \"cafe\": 15\n}",
                explicacaoExecucao = "O dicionário gravou estoque['cafe'] = 20, subtraiu 5 na venda deixando 15, e o json.dump salvou o estado atualizado no arquivo estoque.json."
            ),
            CenarioSimulador(
                titulo = "Simulação 2: Massa de Testes Gerada (massa_teste_cadastro.txt)",
                entradasSimuladas = "Quantidade de registros: 2",
                saidaConsole = "Nome Completo | Email | CPF | Matricula | Senha\n------------------------------------------------------------------------------------------\nAna Oliveira Pereira      | ana.pereira@ufrpe.br      | 03807484108 | 202025485 | Of5PQYeG\nGisele Pereira Pereira    | gisele.pereira@ufrpe.br   | 61342744705 | 202191916 | qFpNDGio",
                explicacaoExecucao = "Este é o conteúdo real gerado pelo seu script em C:\\edson_python\\massa_teste_cadastro.txt! Veja como .ljust(25) alinhou perfeitamente as colunas de Nome e Email."
            )
        ),
        questoes = listOf(
            QuestaoDesafio(
                id = "m6_q1",
                moduloNumero = 6,
                enunciado = "No arquivo controli.py, qual função da biblioteca json é usada para SALVAR o dicionário 'estoque' dentro do arquivo estoque.json?",
                codigoApoio = "with open(ARQUIVO_ESTOQUE, 'w') as arquivo:\n    json.____(estoque, arquivo, indent=4)",
                opcoes = listOf(
                    "json.dump(estoque, arquivo, indent=4)",
                    "json.load(arquivo)",
                    "json.input(estoque)",
                    "json.print(estoque)"
                ),
                indiceCorreto = 0,
                explicacaoResposta = "Exato! json.dump() despeja (salva) os dados no arquivo, enquanto json.load() carrega (lê) os dados do arquivo."
            ),
            QuestaoDesafio(
                id = "m6_q2",
                moduloNumero = 6,
                enunciado = "Por que o seu código controli.py usa 'try ... except ValueError:' quando pede a quantidade do produto ao usuário?",
                codigoApoio = "try:\n    quantidade = int(input(\"Quantidade a adicionar: \"))\nexcept ValueError:\n    print(\"Por favor, insira um número válido.\")",
                opcoes = listOf(
                    "Para evitar que o programa trave caso o usuário digite letras ou palavras em vez de um número inteiro.",
                    "Para apagar o arquivo estoque.json automaticamente.",
                    "Porque o int() só funciona dentro de um bloco try.",
                    "Para multiplicar a quantidade por 10."
                ),
                indiceCorreto = 0,
                explicacaoResposta = "Perfeito! Se alguém digitar 'dez' em int(input()), o Python lança um ValueError. Com o try/except, seu programa mostra um aviso amigável e continua funcionando!"
            ),
            QuestaoDesafio(
                id = "m6_q3",
                moduloNumero = 6,
                enunciado = "No seu gerador de massa de testes (gerar_cadastro.py), o que faz a expressão '[random.randint(0, 9) for _ in range(9)]'?",
                codigoApoio = "cpf = [random.randint(0, 9) for _ in range(9)]",
                opcoes = listOf(
                    "Cria uma lista contendo 9 números inteiros sorteados aleatoriamente entre 0 e 9.",
                    "Cria um único número igual a 9.",
                    "Apaga os 9 primeiros dígitos do CPF.",
                    "Ordena o CPF em ordem crescente."
                ),
                indiceCorreto = 0,
                explicacaoResposta = "Isso mesmo! Essa estrutura se chama List Comprehension (Compreensão de Lista) e gera os 9 dígitos base do CPF em uma única linha elegante."
            ),
            QuestaoDesafio(
                id = "m6_q4",
                moduloNumero = 6,
                enunciado = "No arquivo gerar_cadastro.py, para que serve o método .ljust(25) em 'nome_completo.ljust(25)' ao gravar o arquivo texto?",
                codigoApoio = "linha = f\"{nome_completo.ljust(25)} | {email.ljust(25)} | {cpf}\"",
                opcoes = listOf(
                    "Alinha o texto à esquerda preenchendo com espaços até completar 25 caracteres, deixando a tabela alinhada.",
                    "Corta o nome para ter apenas 2 letras.",
                    "Converte o nome para letras maiúsculas.",
                    "Criptografa o nome com 25 senhas."
                ),
                indiceCorreto = 0,
                explicacaoResposta = "Certíssimo! O método ljust (left justify) preenche com espaços à direita para que todas as colunas da tabela fiquem retas no arquivo .txt!"
            )
        )
    )
}
