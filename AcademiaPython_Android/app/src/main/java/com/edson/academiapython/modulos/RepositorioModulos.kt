package com.edson.academiapython.modulos

import com.edson.academiapython.modelo.CasoBugReal
import com.edson.academiapython.modelo.ModuloEnsino
import com.edson.academiapython.modelo.QuestaoDesafio

/**
 * Repositório Central de Módulos da Academia Python da Luna.
 * Para dar manutenção ou adicionar um novo módulo no futuro,
 * basta criar o arquivo Modulo09...kt e adicioná-lo na lista abaixo!
 */
object RepositorioModulos {

    val todosOsModulos: List<ModuloEnsino> = listOf(
        Modulo01VariaveisEntrada.modulo,
        Modulo02OperadoresPorcentagem.modulo,
        Modulo03Condicionais.modulo,
        Modulo04LogicaFinanceira.modulo,
        Modulo05LacosFuncoesForca.modulo,
        Modulo06DicionariosJsonMassa.modulo,
        Modulo07BancoDadosSqlite.modulo,
        Modulo08TestesPytest.modulo
    )

    val todosOsBugs: List<CasoBugReal> = ModuloDetetiveBugs.casosDeBug

    fun todasAsQuestoes(): List<QuestaoDesafio> {
        return todosOsModulos.flatMap { it.questoes }
    }
}
