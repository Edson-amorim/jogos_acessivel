package com.edson.dominoacessivel

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.io.PrintWriter
import java.util.UUID

enum class JogoBluetooth(val titulo: String, val icone: String, val descricao: String) {
    DOMINO("Dominó Acessível", "🀥", "Duelo de Dominó para 2 jogadores em 2 celulares"),
    TRUCO("Truco Paulista", "🃏", "Partida de Truco Paulista com tentos e manilhas"),
    PYTHON("Academia Python", "🐍", "Duelo de Conhecimento e Quiz de Programação"),
    YUGIOH("Yu-Gi-Oh! Duelo Acessível", "⚔️", "Duelo com Decks, Monstros e Magias")
}

enum class EstadoBluetoothDuelo {
    DESCONECTADO,
    AGUARDANDO_CONEXAO, // Modo Host / Servidor
    CONECTANDO,        // Modo Cliente
    CONECTADO
}

class GerenciadorBluetoothDuelo(private val context: Context) {

    companion object {
        private const val NOME_SERVICO = "DueloJogosDaLuna"
        private val UUID_DUELO: UUID = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66")
    }

    var estado by mutableStateOf(EstadoBluetoothDuelo.DESCONECTADO)
    var nomeDispositivoConectado by mutableStateOf<String?>(null)
    var mensagemStatus by mutableStateOf("Bluetooth pronto para conectar.")
    var souHost by mutableStateOf(false)
    var jogoSelecionado by mutableStateOf(JogoBluetooth.DOMINO)

    val dispositivosPareados = mutableStateListOf<BluetoothDevice>()

    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager?.adapter ?: BluetoothAdapter.getDefaultAdapter()

    private var serverSocket: BluetoothServerSocket? = null
    private var clientSocket: BluetoothSocket? = null
    private var writer: PrintWriter? = null
    private var reader: BufferedReader? = null

    private var threadServidor: Thread? = null
    private var threadComunicacao: Thread? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    var aoReceberComando: ((String) -> Unit)? = null
    var aoConectar: ((Boolean) -> Unit)? = null
    var aoDesconectar: (() -> Unit)? = null

    fun bluetoothDisponivel(): Boolean = bluetoothAdapter != null

    fun bluetoothAtivado(): Boolean = bluetoothAdapter?.isEnabled == true

    @SuppressLint("MissingPermission")
    fun atualizarDispositivosPareados() {
        dispositivosPareados.clear()
        try {
            bluetoothAdapter?.bondedDevices?.let { bonded ->
                dispositivosPareados.addAll(bonded)
            }
        } catch (_: Exception) {}
    }

    @SuppressLint("MissingPermission")
    fun iniciarServidor(aoSucesso: () -> Unit = {}) {
        fecharConexoes()
        souHost = true
        estado = EstadoBluetoothDuelo.AGUARDANDO_CONEXAO
        mensagemStatus = "Aguardando outro duelista se conectar via Bluetooth..."

        threadServidor = Thread {
            try {
                serverSocket = bluetoothAdapter?.listenUsingRfcommWithServiceRecord(NOME_SERVICO, UUID_DUELO)
                val socket = serverSocket?.accept()
                serverSocket?.close()
                serverSocket = null
                if (socket != null) {
                    clientSocket = socket
                    configurarComunicacao(socket)
                    mainHandler.post {
                        nomeDispositivoConectado = try { socket.remoteDevice.name ?: "Duelista Conectado" } catch (_: Exception) { "Duelista Conectado" }
                        estado = EstadoBluetoothDuelo.CONECTADO
                        mensagemStatus = "Conectado com sucesso com $nomeDispositivoConectado!"
                        aoConectar?.invoke(true)
                        aoSucesso()
                    }
                }
            } catch (e: Exception) {
                mainHandler.post {
                    estado = EstadoBluetoothDuelo.DESCONECTADO
                    mensagemStatus = "Erro ao aguardar conexão Bluetooth: ${e.message}"
                }
            }
        }.apply { start() }
    }

    @SuppressLint("MissingPermission")
    fun conectarADispositivo(device: BluetoothDevice, aoSucesso: () -> Unit = {}) {
        fecharConexoes()
        souHost = false
        estado = EstadoBluetoothDuelo.CONECTANDO
        val nomeDev = try { device.name ?: "Dispositivo" } catch (_: Exception) { "Dispositivo" }
        mensagemStatus = "Conectando ao celular de $nomeDev..."

        threadServidor = Thread {
            try {
                bluetoothAdapter?.cancelDiscovery()
                val socket = device.createRfcommSocketToServiceRecord(UUID_DUELO)
                socket.connect()
                clientSocket = socket
                configurarComunicacao(socket)
                mainHandler.post {
                    nomeDispositivoConectado = nomeDev
                    estado = EstadoBluetoothDuelo.CONECTADO
                    mensagemStatus = "Conectado ao duelo com $nomeDev!"
                    aoConectar?.invoke(false)
                    aoSucesso()
                }
            } catch (e: Exception) {
                mainHandler.post {
                    estado = EstadoBluetoothDuelo.DESCONECTADO
                    mensagemStatus = "Falha ao conectar via Bluetooth: ${e.message}"
                }
            }
        }.apply { start() }
    }

    private fun configurarComunicacao(socket: BluetoothSocket) {
        writer = PrintWriter(OutputStreamWriter(socket.outputStream, Charsets.UTF_8), true)
        reader = BufferedReader(InputStreamReader(socket.inputStream, Charsets.UTF_8))

        threadComunicacao = Thread {
            try {
                while (estado == EstadoBluetoothDuelo.CONECTADO && socket.isConnected) {
                    val linha = reader?.readLine() ?: break
                    mainHandler.post {
                        aoReceberComando?.invoke(linha)
                    }
                }
            } catch (_: Exception) {
            } finally {
                mainHandler.post {
                    desconectar()
                }
            }
        }.apply { start() }
    }

    fun enviarComando(comando: String) {
        Thread {
            try {
                writer?.println(comando)
            } catch (_: Exception) {}
        }.start()
    }

    fun desconectar() {
        fecharConexoes()
        estado = EstadoBluetoothDuelo.DESCONECTADO
        nomeDispositivoConectado = null
        mensagemStatus = "Duelo Bluetooth desconectado."
        aoDesconectar?.invoke()
    }

    private fun fecharConexoes() {
        try { writer?.close() } catch (_: Exception) {}
        try { reader?.close() } catch (_: Exception) {}
        try { clientSocket?.close() } catch (_: Exception) {}
        try { serverSocket?.close() } catch (_: Exception) {}
        clientSocket = null
        serverSocket = null
        writer = null
        reader = null
    }
}
