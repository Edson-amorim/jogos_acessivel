package com.edson.dominoacessivel

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/**
 * Banco de Dados SQLite nativo para persistência de vitórias de todos os jogos
 * e memória tática de aprendizado da Luna no Yu-Gi-Oh e outros duelos.
 */
class BancoDadosJogosLuna(context: Context) : SQLiteOpenHelper(
    context.applicationContext,
    NOME_BANCO,
    null,
    VERSAO_BANCO
) {

    companion object {
        private const val NOME_BANCO = "jogos_da_luna.db"
        private const val VERSAO_BANCO = 1

        @Volatile
        private var instancia: BancoDadosJogosLuna? = null

        fun obterInstancia(context: Context): BancoDadosJogosLuna {
            return instancia ?: synchronized(this) {
                instancia ?: BancoDadosJogosLuna(context).also { instancia = it }
            }
        }
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS vitorias (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                jogo TEXT NOT NULL,
                nome_jogador TEXT NOT NULL,
                modo_jogo TEXT NOT NULL,
                tipo_vitoria TEXT NOT NULL,
                oponente TEXT NOT NULL,
                data_hora INTEGER NOT NULL
            );
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS aprendizado_luna_yugioh (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                tipo_acao TEXT NOT NULL,
                id_carta INTEGER NOT NULL,
                nome_carta TEXT NOT NULL,
                turno_usado INTEGER NOT NULL,
                data_hora INTEGER NOT NULL
            );
            """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Upgrade futuro seguro
    }

    // ========================================================================
    // PERSISTÊNCIA DE VITÓRIAS
    // ========================================================================

    fun salvarVitoria(
        jogo: String,
        nomeJogador: String,
        modoJogo: String,
        tipoVitoria: String,
        oponente: String
    ): Long {
        return try {
            val db = writableDatabase
            val valores = ContentValues().apply {
                put("jogo", jogo)
                put("nome_jogador", nomeJogador)
                put("modo_jogo", modoJogo)
                put("tipo_vitoria", tipoVitoria)
                put("oponente", oponente)
                put("data_hora", System.currentTimeMillis())
            }
            db.insert("vitorias", null, valores)
        } catch (_: Exception) {
            -1L
        }
    }

    fun obterTotalVitorias(jogo: String, nomeJogador: String): Int {
        return try {
            val db = readableDatabase
            val cursor = db.rawQuery(
                "SELECT COUNT(*) FROM vitorias WHERE jogo = ? AND nome_jogador = ?",
                arrayOf(jogo, nomeJogador)
            )
            cursor.use {
                if (it.moveToFirst()) it.getInt(0) else 0
            }
        } catch (_: Exception) {
            0
        }
    }

    fun obterTotalTorneiosYugioh(nomeJogador: String): Int {
        return try {
            val db = readableDatabase
            val cursor = db.rawQuery(
                "SELECT COUNT(*) FROM vitorias WHERE jogo = 'YUGIOH' AND tipo_vitoria LIKE '%TORNEIO%' AND nome_jogador = ?",
                arrayOf(nomeJogador)
            )
            cursor.use {
                if (it.moveToFirst()) it.getInt(0) else 0
            }
        } catch (_: Exception) {
            0
        }
    }

    // ========================================================================
    // APRENDIZADO TÁTICO DA LUNA (IA IMITA E CRIA TÉCNICAS)
    // ========================================================================

    fun registrarJogadaJogador(
        tipoAcao: String,
        idCarta: Int,
        nomeCarta: String,
        turnoUsado: Int
    ) {
        try {
            val db = writableDatabase
            val valores = ContentValues().apply {
                put("tipo_acao", tipoAcao)
                put("id_carta", idCarta)
                put("nome_carta", nomeCarta)
                put("turno_usado", turnoUsado)
                put("data_hora", System.currentTimeMillis())
            }
            db.insert("aprendizado_luna_yugioh", null, valores)
        } catch (_: Exception) {
        }
    }

    /**
     * Retorna os IDs das cartas mais usadas pelo adversário contra a Luna,
     * para que a Luna possa copiar as táticas do jogador no deck dela.
     */
    fun obterCartasFavoritasJogador(limite: Int = 6): List<Int> {
        val lista = mutableListOf<Int>()
        try {
            val db = readableDatabase
            val cursor = db.rawQuery(
                """
                SELECT id_carta, COUNT(*) as qtd 
                FROM aprendizado_luna_yugioh 
                GROUP BY id_carta 
                ORDER BY qtd DESC 
                LIMIT ?
                """.trimIndent(),
                arrayOf(limite.toString())
            )
            cursor.use {
                while (it.moveToNext()) {
                    lista.add(it.getInt(0))
                }
            }
        } catch (_: Exception) {
        }
        return lista
    }

    /**
     * Identifica se o jogador costuma acelerar o deck com compra rápida (ex: Pote da Ganância).
     */
    fun jogadorUsaAceleracaoCompra(): Boolean {
        return try {
            val db = readableDatabase
            val cursor = db.rawQuery(
                "SELECT COUNT(*) FROM aprendizado_luna_yugioh WHERE id_carta = 24", // Pote da Ganância
                null
            )
            cursor.use {
                if (it.moveToFirst()) it.getInt(0) >= 2 else false
            }
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Identifica se o jogador usa estratégias de invocação pesada por tributo.
     */
    fun jogadorUsaMonstrosTributo(): Boolean {
        return try {
            val db = readableDatabase
            val cursor = db.rawQuery(
                "SELECT COUNT(*) FROM aprendizado_luna_yugioh WHERE tipo_acao = 'INVOCACAO_TRIBUTO'",
                null
            )
            cursor.use {
                if (it.moveToFirst()) it.getInt(0) >= 2 else false
            }
        } catch (_: Exception) {
            false
        }
    }
}
