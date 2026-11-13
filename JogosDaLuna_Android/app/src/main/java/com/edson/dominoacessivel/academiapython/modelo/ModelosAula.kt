package com.edson.dominoacessivel.academiapython.modelo

/**
 * Modelos de dados modulares da Academia Python da Luna.
 * Cada módulo de ensino é composto por:
 * 1. Tópicos de Teoria (com explicações didáticas e dicas da Professora Luna)
 * 2. Códigos Originais da pasta C:\edson_python explicados linha por linha
 * 3. Cenários de Laboratório/Simulador (execução passo a passo simulada do código)
 * 4. Questões de Desafio (Quiz interativo com feedback imediato)
 */

data class TopicoTeoria(
    val titulo: String,
    val explicacaoVisual: String,
    val explicacaoFalada: String,
    val exemploCodigo: String,
    val dicaDaLuna: String
)

data class LinhaExplicada(
    val numeroLinha: Int,
    val codigo: String,
    val leituraFalada: String,
    val explicacaoDidatica: String
)

data class CodigoOriginalEdson(
    val nomeArquivo: String,
    val titulo: String,
    val descricaoContexto: String,
    val codigoCompleto: String,
    val linhasExplicadas: List<LinhaExplicada>
)

data class CenarioSimulador(
    val titulo: String,
    val entradasSimuladas: String,
    val saidaConsole: String,
    val explicacaoExecucao: String
)

data class QuestaoDesafio(
    val id: String,
    val moduloNumero: Int,
    val enunciado: String,
    val codigoApoio: String = "",
    val opcoes: List<String>,
    val indiceCorreto: Int,
    val explicacaoResposta: String,
    val pontosXp: Int = 30
)

data class CasoBugReal(
    val id: Int,
    val arquivoOrigem: String,
    val titulo: String,
    val historiaBug: String,
    val codigoComBug: String,
    val codigoCorrigido: String,
    val pergunta: String,
    val opcoes: List<String>,
    val indiceCorreto: Int,
    val licaoMoral: String,
    val pontosXp: Int = 40
)

data class ModuloEnsino(
    val numero: Int,
    val titulo: String,
    val subtitulo: String,
    val arquivosOrigem: List<String>,
    val resumoFalado: String,
    val topicosTeoria: List<TopicoTeoria>,
    val codigosOriginais: List<CodigoOriginalEdson>,
    val cenariosSimulador: List<CenarioSimulador>,
    val questoes: List<QuestaoDesafio>
)
