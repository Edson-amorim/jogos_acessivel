"""
Gera os arquivos de som (.wav) do jogo de domino, sem precisar
baixar nada da internet. Cria a pasta "sons" ao lado deste
script e preenche com: batida.wav, navegar.wav, erro.wav,
passar.wav, vitoria.wav, derrota.wav, campeao.wav.

Basta rodar: python gerar_sons.py
"""

import wave
import struct
import math
import random
import os

TAXA = 44100  # amostras por segundo


def _salvar_wav(caminho, amostras):
    """Recebe uma lista de floats entre -1.0 e 1.0 e grava um wav mono 16 bits."""
    with wave.open(caminho, "w") as arq:
        arq.setnchannels(1)
        arq.setsampwidth(2)
        arq.setframerate(TAXA)
        dados = b"".join(
            struct.pack("<h", int(max(-1.0, min(1.0, a)) * 32000))
            for a in amostras
        )
        arq.writeframes(dados)


def _envelope(n, ataque=0.05, solta=0.3):
    """Gera uma envoltoria simples: sobe rapido, desce suave (evita clique feio)."""
    n_ataque = max(1, int(n * ataque))
    n_solta = max(1, int(n * solta))
    env = []
    for i in range(n):
        if i < n_ataque:
            env.append(i / n_ataque)
        elif i > n - n_solta:
            env.append((n - i) / n_solta)
        else:
            env.append(1.0)
    return env


def tom(freq, duracao, volume=0.5, ataque=0.05, solta=0.4):
    n = int(TAXA * duracao)
    env = _envelope(n, ataque, solta)
    return [
        volume * env[i] * math.sin(2 * math.pi * freq * i / TAXA)
        for i in range(n)
    ]


def sequencia_tons(notas, gap=0.02):
    """notas: lista de (freq, duracao, volume). Concatena com um pequeno silencio entre elas."""
    amostras = []
    silencio = [0.0] * int(TAXA * gap)
    for freq, dur, vol in notas:
        amostras.extend(tom(freq, dur, vol))
        amostras.extend(silencio)
    return amostras


def batida_domino():
    """Batida seca de peca de domino (simula o impacto num material rigido)."""
    dur = 0.14
    n = int(TAXA * dur)
    amostras = [0.0] * n

    # Transiente de impacto: rajada curtissima de ruido (o "clique" do choque)
    n_transiente = max(1, int(TAXA * 0.004))
    for i in range(n_transiente):
        amostras[i] += random.uniform(-1, 1) * (1 - i / n_transiente)

    # Ressonancias do material: cada peca de domino "toca" varias frequencias
    # ao mesmo tempo, decaindo em velocidades diferentes. Isso e o que da
    # o timbre de peca rigida (madeira/plastico), em vez de um bipe puro.
    modos = [
        (3400, 0.008, 0.55),
        (5100, 0.005, 0.35),
        (1900, 0.014, 0.45),
        (7200, 0.003, 0.20),
    ]  # (frequencia em Hz, tempo de decaimento em segundos, amplitude)

    for freq, tau, amp in modos:
        tau_amostras = TAXA * tau
        for i in range(n):
            amostras[i] += amp * math.exp(-i / tau_amostras) * math.sin(2 * math.pi * freq * i / TAXA)

    pico = max(abs(x) for x in amostras) or 1.0
    return [x / pico * 0.9 for x in amostras]


def navegar():
    return tom(700, 0.06, volume=0.35, ataque=0.1, solta=0.5)


def erro():
    return sequencia_tons([(180, 0.12, 0.5), (140, 0.16, 0.5)])


def passar():
    return sequencia_tons([(320, 0.09, 0.4), (260, 0.09, 0.4)])


def vitoria():
    return sequencia_tons([
        (523, 0.12, 0.45), (659, 0.12, 0.45),
        (784, 0.12, 0.45), (1046, 0.25, 0.5),
    ])


def derrota():
    return sequencia_tons([(300, 0.18, 0.45), (250, 0.18, 0.45), (190, 0.28, 0.45)])


def campeao():
    return sequencia_tons([
        (659, 0.13, 0.45), (784, 0.13, 0.45),
        (988, 0.13, 0.45), (1318, 0.35, 0.55),
    ])


def main():
    pasta = os.path.join(os.path.dirname(os.path.abspath(__file__)), "sons")
    os.makedirs(pasta, exist_ok=True)

    sons = {
        "batida.wav": batida_domino(),
        "navegar.wav": navegar(),
        "erro.wav": erro(),
        "passar.wav": passar(),
        "vitoria.wav": vitoria(),
        "derrota.wav": derrota(),
        "campeao.wav": campeao(),
    }

    for nome_arquivo, amostras in sons.items():
        caminho = os.path.join(pasta, nome_arquivo)
        _salvar_wav(caminho, amostras)
        print(f"Criado: {caminho}")

    print("\nPronto! Os sons foram gerados na pasta 'sons'.")


if __name__ == "__main__":
    main()