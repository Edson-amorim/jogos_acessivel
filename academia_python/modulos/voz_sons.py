"""
Módulo de Acessibilidade e Efeitos Sonoros para a versão Python da Academia Python da Luna.
Compatível com leitor de telas nativo (NVDA via accessible_output2 ou console direto) e arquivos .wav.
A síntese por SAPI foi removida para não atropelar a tecnologia assistiva do sistema.
"""

import os
import sys

try:
    import winsound
except ImportError:
    winsound = None

try:
    from accessible_output2.outputs.auto import Auto
    _falador = Auto()
except Exception:
    _falador = None

PASTA_BASE = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
PASTA_SONS = os.path.join(os.path.dirname(PASTA_BASE), "sons")


def falar(texto, interromper=True):
    """Envia o texto para o leitor de telas do sistema se disponível."""
    if _falador:
        try:
            _falador.speak(texto, interrupt=interromper)
            return
        except Exception:
            pass


def _tocar_som(nome_arquivo, freqs_fallback):
    if not winsound:
        return
    caminho = os.path.join(PASTA_SONS, nome_arquivo)
    if os.path.exists(caminho):
        try:
            winsound.PlaySound(caminho, winsound.SND_FILENAME | winsound.SND_ASYNC)
            return
        except Exception:
            pass
    for freq, dur in freqs_fallback:
        try:
            winsound.Beep(freq, dur)
        except Exception:
            pass


def som_navegar():
    _tocar_som("navegar.wav", [(800, 30)])


def som_confirmar():
    _tocar_som("batida.wav", [(1200, 60)])


def som_acerto():
    _tocar_som("vitoria.wav", [(1000, 100), (1400, 150)])


def som_erro():
    _tocar_som("erro.wav", [(400, 200)])


def som_conclusao():
    _tocar_som("campeao.wav", [(800, 100), (1000, 100), (1200, 100), (1600, 300)])
