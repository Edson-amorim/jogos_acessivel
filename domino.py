"""
Domino Acessivel para Windows - Modos Individual, Duplas e Desafio Luna (IA Mestre Bayesiana/Monte Carlo)

Comandos:
  Setas (Esquerda / Direita): navega entre as pedras da sua mao
  Barra de Espaco: joga a pedra selecionada
  Tab / C: compra uma pedra do monte (no modo 1x1) quando nao houver jogada valida
  Backspace: passa a vez quando nao houver jogada valida (ou quando o monte acabar)
  T / Enter / J: le APENAS as pontas da mesa (ex: "3 e 5")
  Esc: sai do jogo
"""

import msvcrt
import winsound
import random
import sys
import os
import time
import threading
import queue
import ctypes
import math

try:
    from accessible_output2.outputs.auto import Auto
    _falador = Auto()
except Exception:
    _falador = None

try:
    import tkinter as tk
    TABULEIRO_VISUAL_DISPONIVEL = True
except ImportError:
    TABULEIRO_VISUAL_DISPONIVEL = False

_fila_visual = queue.Queue()

# --- LISTA DE JOGADORES BRASILEIROS (PARA O MODO DUPLAS) ---
CRAQUES_SELECAO = [
    "Pelé", "Garrincha", "Zico", "Romário", "Ronaldo", "Ronaldinho", 
    "Rivaldo", "Kaká", "Cafu", "Roberto Carlos", "Bebeto", "Taffarel",
    "Sócrates", "Falcão", "Vampeta", "Neymar"
]

# --- LISTA DE JOGADORES ARGENTINOS (PARA O MODO 1x1) ---
CRAQUES_ARGENTINA = [
    "Maradona", "Messi", "Riquelme", "Di María", "Batistuta", 
    "Kempes", "Passarella", "Agüero", "Tevez", "Verón", 
    "Mascherano", "Crespo", "Caniggia", "Aimar"
]

# --- PROVOCAÇÕES SÁDICAS E AGRESSIVAS DA LUNA ---
PROVOCACOES_LUNA_JOGADA = [
    "Achou mesmo que tinha alguma chance contra minha inferência bayesiana, {nome}?",
    "Simulei essa sua derrota em 500 cenários Monte Carlo antes de mover essa pedra!",
    "Sentiu o peso da matemática? Essa vai doer na sua pontuação!",
    "Sua probabilidade de vitória acabou de cair para zero!",
    "Pode chorar pro teclado, veterano. Essa mesa é minha!",
    "Patético... Eu já sabia que você não tinha a resposta pra essa pedra!"
]

PROVOCACOES_LUNA_PASSA = [
    "Passou a vez de novo? Que vergonha, {nome}! Treina mais!",
    "Entrou em colapso? Compra do monte e chora em silêncio!",
    "Adoro ver você completamente travado! Minha distribuição de probabilidade acertou em cheio!",
    "Qual a sensação de ser dominado por poucas linhas de código?",
    "Pode ir passando a vez... Só estou saboreando a sua derrota!"
]

NOMES_NIVEL = ["Fácil", "Médio", "Difícil", "Muito Difícil", "SÁDICA (Monte Carlo & Bayes)"]

if getattr(sys, 'frozen', False):
    DIRETORIO_BASE = os.path.dirname(sys.executable)
else:
    DIRETORIO_BASE = os.path.dirname(os.path.abspath(__file__))

PASTA_SONS = os.path.join(DIRETORIO_BASE, "sons")

PAUSA_ENTRE_FALAS = 1.5
PAUSA_JOGADA = 2.0


# ---------- Motor de Inferência Bayesiana e Monte Carlo ----------

class MotorLunaBayesMonteCarlo:
    """Motor de inteligência extrema combinando estatística Bayesiana com Simulação de Monte Carlo."""

    def __init__(self):
        self.todas_pedras = [(i, j) for i in range(7) for j in range(i, 7)]

    def atualizar_crencas_bayesianas(self, jogo, idx_luna):
        idx_edson = 0 if idx_luna != 0 else 1
        pedras_conhecidas = set(jogo.tabuleiro + jogo.maos[idx_luna])
        pedras_desconhecidas = [p for p in self.todas_pedras if p not in pedras_conhecidas]
        
        passagens = jogo.passagens_jogadores[idx_edson]
        probabilidades = {}

        for p in pedras_desconhecidas:
            prob = 1.0
            # Se Edson passou em pontas que continham o número de 'p', P(p na mão do Edson) vai a zero.
            for num_travado in passagens:
                if p[0] == num_travado or p[1] == num_travado:
                    prob = 0.001
                    break
            probabilidades[p] = prob

        total_prob = sum(probabilidades.values())
        if total_prob > 0:
            for p in probabilidades:
                probabilidades[p] /= total_prob

        return probabilidades, pedras_desconhecidas

    def simular_monte_carlo(self, jogo, idx_luna, acao_proposta, num_simulacoes=250):
        pedra, lado = acao_proposta
        vitorias = 0
        idx_edson = 0 if idx_luna != 0 else 1
        probs_edson, pedras_ocultas = self.atualizar_crencas_bayesianas(jogo, idx_luna)

        if not pedras_ocultas:
            return 0

        for _ in range(num_simulacoes):
            # Amostragem condicional do estado hipotético do Edson
            qtd_edson = len(jogo.maos[idx_edson])
            
            # Escolhe pedras prováveis para o Edson baseado na inferência Bayesiana
            pesos = [probs_edson[p] for p in pedras_ocultas]
            if sum(pesos) == 0:
                mao_simulada_edson = random.sample(pedras_ocultas, min(qtd_edson, len(pedras_ocultas)))
            else:
                # Amostragem ponderada sem reposição simples
                amostra = set()
                candidatos = list(pedras_ocultas)
                c_pesos = list(pesos)
                while len(amostra) < min(qtd_edson, len(candidatos)):
                    escolhida = random.choices(candidatos, weights=c_pesos, k=1)[0]
                    amostra.add(escolhida)
                    idx_rem = candidatos.index(escolhida)
                    candidatos.pop(idx_rem)
                    c_pesos.pop(idx_rem)
                mao_simulada_edson = list(amostra)

            # Executa simulação leve de jogo até o fim
            tabuleiro_sim = list(jogo.tabuleiro)
            esq, dir_ = extremidades(tabuleiro_sim)

            if esq is None:
                tabuleiro_sim.append(pedra)
            elif lado == 'ESQUERDA':
                tabuleiro_sim.insert(0, orientar_para_esquerda(pedra, esq))
            else:
                tabuleiro_sim.append(orientar_para_direita(pedra, dir_))

            mao_luna_sim = [p for p in jogo.maos[idx_luna] if p != pedra]
            mao_edson_sim = list(mao_simulada_edson)

            vez = idx_edson
            bloqueios = 0
            while len(mao_luna_sim) > 0 and len(mao_edson_sim) > 0 and bloqueios < 2:
                m_atual = mao_edson_sim if vez == idx_edson else mao_luna_sim
                validas_sim = pedras_jogaveis(m_atual, tabuleiro_sim)

                if validas_sim:
                    p_jogada = random.choice(validas_sim)
                    e_s, d_s = extremidades(tabuleiro_sim)
                    if pedra_encaixa(p_jogada, e_s):
                        tabuleiro_sim.insert(0, orientar_para_esquerda(p_jogada, e_s))
                    else:
                        tabuleiro_sim.append(orientar_para_direita(p_jogada, d_s))
                    m_atual.remove(p_jogada)
                    bloqueios = 0
                else:
                    bloqueios += 1

                vez = 1 - vez

            # Avaliação de Resultado do Rollout
            if len(mao_luna_sim) == 0:
                vitorias += 3.0
            elif len(mao_edson_sim) == 0:
                vitorias -= 3.0
            else:
                pts_luna = sum(a + b for a, b in mao_luna_sim)
                pts_edson = sum(a + b for a, b in mao_edson_sim)
                if pts_luna < pts_edson:
                    vitorias += 1.5
                else:
                    vitorias -= 1.5

        return vitorias

    def escolher_melhor_jogada(self, jogo, idx_luna, validas):
        esq_atual, dir_atual = extremidades(jogo.tabuleiro)
        melhor_pontuacao = -float('inf')
        melhor_pedra = validas[0]
        melhor_lado = None

        for p in validas:
            lados = []
            if esq_atual is None or pedra_encaixa(p, esq_atual): lados.append('ESQUERDA')
            if esq_atual is not None and pedra_encaixa(p, dir_atual): lados.append('DIREITA')

            for lado in lados:
                # Avaliação por Monte Carlo Tree Search Rollouts
                score_mc = self.simular_monte_carlo(jogo, idx_luna, (p, lado), num_simulacoes=200)

                # Heurísticas Agressivas Adicionais
                score_heuristico = (p[0] + p[1]) * 2.0
                if p[0] == p[1]:
                    score_heuristico += 15.0 # Desfaz rápido das carroças

                total_score = score_mc + score_heuristico

                if total_score > melhor_pontuacao:
                    melhor_pontuacao = total_score
                    melhor_pedra = p
                    melhor_lado = lado

        return melhor_pedra, melhor_lado

motor_luna = MotorLunaBayesMonteCarlo()


# ---------- Sons Estilo Domino / Interface ----------

def _tocar_wav_ou_beep(nome_arquivo, freqs_fallback):
    caminho = os.path.join(PASTA_SONS, nome_arquivo)
    if os.path.exists(caminho):
        try:
            winsound.PlaySound(caminho, winsound.SND_FILENAME | winsound.SND_ASYNC)
        except Exception:
            for freq, dur in freqs_fallback:
                winsound.Beep(freq, dur)
    else:
        for freq, dur in freqs_fallback:
            try:
                winsound.Beep(freq, dur)
            except Exception:
                pass

def som_batida_domino():
    _tocar_wav_ou_beep("batida.wav", [(180, 20), (120, 45)])

def som_navegar():
    _tocar_wav_ou_beep("navegar.wav", [(800, 30)])

def som_erro():
    _tocar_wav_ou_beep("erro.wav", [(150, 150)])

def som_passar():
    _tocar_wav_ou_beep("passar.wav", [(300, 100)])

def som_vitoria():
    _tocar_wav_ou_beep("vitoria.wav", [(523, 100), (659, 100), (784, 100), (1046, 100)])

def som_derrota():
    _tocar_wav_ou_beep("derrota.wav", [(300, 150), (250, 150), (200, 150)])

def som_campeao():
    _tocar_wav_ou_beep("campeao.wav", [(659, 120), (784, 120), (988, 120), (1318, 220)])


# ---------- Formatacao Visual / Falada (Estilo Juiz) ----------

def formatar_pedra(pedra):
    return f"[{pedra[0]}|{pedra[1]}]"

def falar_pedra_juiz(pedra):
    if pedra[0] == pedra[1]:
        return f"carroça de {pedra[0]}"
    return f"{pedra[0]} e {pedra[1]}"

def falar_pontas_juiz(tabuleiro):
    if not tabuleiro:
        return "Mesa vazia"
    esq, dir_ = extremidades(tabuleiro)
    if esq == dir_:
        return f"{esq} em ambos os lados"
    return f"{esq} e {dir_}"


# ---------- Tabuleiro Visual ----------

PONTOS_PADRAO = {
    0: [],
    1: [(1, 1)],
    2: [(0, 0), (2, 2)],
    3: [(0, 0), (1, 1), (2, 2)],
    4: [(0, 0), (2, 0), (0, 2), (2, 2)],
    5: [(0, 0), (2, 0), (1, 1), (0, 2), (2, 2)],
    6: [(0, 0), (2, 0), (0, 1), (2, 1), (0, 2), (2, 2)],
}

COR_FUNDO = "#0b3d24"
COR_PEDRA = "#f5f0e6"
COR_BORDA_PEDRA = "#222222"
COR_PONTO = "#111111"
COR_DESTAQUE = "#ffd700"
COR_TEXTO = "#f5f0e6"


def _desenhar_metade_pedra(canvas, x0, y0, x1, y1, valor):
    passo_x = (x1 - x0) / 3
    passo_y = (y1 - y0) / 3
    raio = min(passo_x, passo_y) * 0.30
    for coluna, linha in PONTOS_PADRAO.get(valor, []):
        cx = x0 + passo_x * (coluna + 0.5)
        cy = y0 + passo_y * (linha + 0.5)
        canvas.create_oval(cx - raio, cy - raio, cx + raio, cy + raio,
                            fill=COR_PONTO, outline="")


def _desenhar_pedra(canvas, x, y, valor_esq, valor_dir, lado=34, destaque=False):
    largura = lado * 2
    altura = lado
    if destaque:
        canvas.create_rectangle(x - 4, y - 4, x + largura + 4, y + altura + 4,
                                 outline=COR_DESTAQUE, width=3)
    canvas.create_rectangle(x, y, x + largura, y + altura,
                             fill=COR_PEDRA, outline=COR_BORDA_PEDRA, width=2)
    canvas.create_line(x + lado, y, x + lado, y + altura, fill=COR_BORDA_PEDRA, width=2)
    _desenhar_metade_pedra(canvas, x, y, x + lado, y + altura, valor_esq)
    _desenhar_metade_pedra(canvas, x + lado, y, x + largura, y + altura, valor_dir)


def _quebrar_em_linhas(itens, largura_janela, lado_pedra, margem):
    largura_pedra = lado_pedra * 2 + 10
    por_linha = max(1, (largura_janela - margem * 2) // largura_pedra)
    linhas = []
    for i in range(0, len(itens), por_linha):
        linhas.append(itens[i:i + por_linha])
    return linhas


def _desenhar_estado(canvas, estado):
    canvas.delete("all")
    largura = canvas.winfo_width() or 1000
    altura_janela = canvas.winfo_height() or 600
    margem = 30

    canvas.create_text(largura / 2, 20, text="Dominó Acessível - Mesa",
                        fill=COR_TEXTO, font=("Segoe UI", 16, "bold"))

    y = 55
    canvas.create_text(margem, y, anchor="w",
                        text=f"Vez de: {estado['jogador_da_vez']}",
                        fill=COR_TEXTO, font=("Segoe UI", 12))
    y += 30
    if estado.get("monte", 0) > 0:
        canvas.create_text(margem, y, anchor="w",
                            text=f"Monte: {estado['monte']} pedra(s)",
                            fill=COR_TEXTO, font=("Segoe UI", 11))
        y += 22

    canvas.create_text(margem, y, anchor="w", text="Mesa:",
                        fill=COR_TEXTO, font=("Segoe UI", 12, "bold"))
    y += 25
    linhas_mesa = _quebrar_em_linhas(estado["tabuleiro"], largura, 34, margem)
    if not linhas_mesa:
        canvas.create_text(margem, y, anchor="w", text="(vazia)",
                            fill=COR_TEXTO, font=("Segoe UI", 11))
        y += 45
    for linha in linhas_mesa:
        x = margem
        for pedra in linha:
            _desenhar_pedra(canvas, x, y, pedra[0], pedra[1], lado=34)
            x += 34 * 2 + 10
        y += 34 + 15

    y += 15
    canvas.create_text(margem, y, anchor="w", text="Sua mão:",
                        fill=COR_TEXTO, font=("Segoe UI", 12, "bold"))
    y += 25
    linhas_mao = _quebrar_em_linhas(estado["mao_jogador"], largura, 34, margem)
    for linha in linhas_mao:
        x = margem
        for pedra in linha:
            selecionada = (pedra == estado["pedra_selecionada"])
            _desenhar_pedra(canvas, x, y, pedra[0], pedra[1], lado=34, destaque=selecionada)
            x += 34 * 2 + 10
        y += 34 + 15

    y = altura_janela - 30 * len(estado["outros"]) - 20
    canvas.create_text(margem, y, anchor="w", text="Outros jogadores:",
                        fill=COR_TEXTO, font=("Segoe UI", 12, "bold"))
    y += 25
    for nome, qtd in estado["outros"]:
        canvas.create_text(margem, y, anchor="w",
                            text=f"{nome}: {qtd} pedra(s)",
                            fill=COR_TEXTO, font=("Segoe UI", 11))
        y += 22


_janela_root = None

def _evitar_roubo_de_foco(janela):
    try:
        janela.update_idletasks()
        hwnd = janela.winfo_id()
        GWL_EXSTYLE = -20
        WS_EX_NOACTIVATE = 0x08000000
        estilo_atual = ctypes.windll.user32.GetWindowLongW(hwnd, GWL_EXSTYLE)
        ctypes.windll.user32.SetWindowLongW(hwnd, GWL_EXSTYLE, estilo_atual | WS_EX_NOACTIVATE)
    except Exception:
        pass

def _focar_console():
    try:
        hwnd_console = ctypes.windll.kernel32.GetConsoleWindow()
        if hwnd_console:
            ctypes.windll.user32.SetForegroundWindow(hwnd_console)
    except Exception:
        pass

def iniciar_tabuleiro_visual():
    global _janela_root
    if not TABULEIRO_VISUAL_DISPONIVEL:
        return
    _janela_root = tk.Tk()
    _janela_root.title("Dominó Acessível - Tabuleiro")
    _janela_root.geometry("1000x650")
    _janela_root.configure(bg=COR_FUNDO)
    
    canvas = tk.Canvas(_janela_root, bg=COR_FUNDO, highlightthickness=0)
    canvas.pack(fill="both", expand=True)

    _evitar_roubo_de_foco(_janela_root)
    _focar_console()

    def checar_fila():
        estado_mais_recente = None
        try:
            while True:
                estado_mais_recente = _fila_visual.get_nowait()
        except queue.Empty:
            pass
        if estado_mais_recente is not None:
            _desenhar_estado(canvas, estado_mais_recente)
        _janela_root.after(120, checar_fila)

    checar_fila()


def atualizar_tabuleiro_visual(jogo):
    if not TABULEIRO_VISUAL_DISPONIVEL:
        return
    pedra_selecionada = None
    if jogo.maos.get(0) and 0 <= jogo.indice_selecionado < len(jogo.maos[0]):
        pedra_selecionada = jogo.maos[0][jogo.indice_selecionado]
    outros = [(jogo.nomes[i], len(jogo.maos[i])) for i in range(1, jogo.num_jogadores)]
    estado = {
        "tabuleiro": list(jogo.tabuleiro),
        "mao_jogador": list(jogo.maos.get(0, [])),
        "pedra_selecionada": pedra_selecionada,
        "outros": outros,
        "jogador_da_vez": jogo.nomes[jogo.jogador_atual],
        "monte": len(jogo.monte),
    }
    _fila_visual.put(estado)


# ---------- Logica Geral ----------

def gerar_pedras():
    return [(i, j) for i in range(7) for j in range(i, 7)]

def extremidades(tabuleiro):
    if not tabuleiro:
        return None, None
    return tabuleiro[0][0], tabuleiro[-1][1]

def pedra_encaixa(pedra, valor):
    return pedra[0] == valor or pedra[1] == valor

def orientar_para_esquerda(pedra, valor_esquerdo):
    a, b = pedra
    if b == valor_esquerdo:
        return (a, b)
    return (b, a)

def orientar_para_direita(pedra, valor_direito):
    a, b = pedra
    if a == valor_direito:
        return (a, b)
    return (b, a)

def pedras_jogaveis(mao, tabuleiro):
    esquerda, direita = extremidades(tabuleiro)
    if esquerda is None:
        return list(mao)
    return [p for p in mao if pedra_encaixa(p, esquerda) or pedra_encaixa(p, direita)]

def tem_jogada_valida(mao, tabuleiro):
    return len(pedras_jogaveis(mao, tabuleiro)) > 0

def contar_pontos(mao):
    return sum(a + b for a, b in mao)


# ---------- Leitura do Teclado ----------

_erro_teclado_avisado = False

def ler_tecla():
    global _erro_teclado_avisado
    try:
        tecla = msvcrt.getch()
    except Exception as erro:
        if not _erro_teclado_avisado:
            _erro_teclado_avisado = True
            print(f"Erro ao ler teclado: {erro}")
        return None

    if tecla in (b'\x00', b'\xe0'):
        try:
            tecla2 = msvcrt.getch()
        except Exception:
            return None
        if tecla2 == b'K':
            return 'ESQUERDA'
        if tecla2 == b'M':
            return 'DIREITA'
        return None
    if tecla == b' ':
        return 'ESPACO'
    if tecla == b'\t':
        return 'TAB'
    if tecla == b'\x08':
        return 'BACKSPACE'
    if tecla == b'\x1b':
        return 'ESC'
    if tecla == b'\r':
        return 'ENTER'
    if tecla.lower() in (b'j', b't'):
        return 'PONTAS'
    if tecla.lower() == b'c':
        return 'COMPRAR'
    if tecla.lower() == b's':
        return 'SIM'
    if tecla.lower() == b'n':
        return 'NAO'
    if tecla in (b'1', b'2', b'3', b'4', b'5'):
        return tecla.decode('utf-8')
    return None

def anunciar(texto_visual, texto_falado=None, interromper=True, pausar_depois=False, pausa=None):
    mensagem = texto_falado if texto_falado is not None else texto_visual
    print(texto_visual, flush=True)
    if _falador:
        try:
            _falador.speak(mensagem, interrupt=interromper)
        except Exception:
            pass
    if pausar_depois:
        time.sleep(pausa if pausa is not None else PAUSA_ENTRE_FALAS)


# ---------- Estado do Jogo ----------

class JogoDomino:
    def __init__(self, modo, nome_jogador, nome_parceiro=None, historico_adversarios=None):
        self.modo = modo
        pedras = gerar_pedras()
        random.shuffle(pedras)
        
        if historico_adversarios is None:
            historico_adversarios = []
        
        if self.modo == 1:
            opcoes_adv = [c for c in CRAQUES_ARGENTINA if c not in historico_adversarios]
            if not opcoes_adv:
                opcoes_adv = CRAQUES_ARGENTINA
            
            adv = random.choice(opcoes_adv)
            self.adv_principal = adv
            self.nomes = [nome_jogador, adv]
            self.maos = {
                0: pedras[0:7],
                1: pedras[7:14]
            }
            self.monte = pedras[14:]
            self.num_jogadores = 2
        elif self.modo == 3: # Modo Desafio Luna Sádica
            self.adv_principal = "Luna"
            self.nomes = [nome_jogador, "Luna"]
            self.maos = {
                0: pedras[0:7],
                1: pedras[7:14]
            }
            self.monte = pedras[14:]
            self.num_jogadores = 2
        else:
            opcoes_adv = [c for c in CRAQUES_SELECAO if c != nome_parceiro and c not in historico_adversarios]
            if len(opcoes_adv) < 2:
                opcoes_adv = [c for c in CRAQUES_SELECAO if c != nome_parceiro]
            
            adv1, adv2 = random.sample(opcoes_adv, 2)
            self.adv_principal = adv1
            self.nomes = [nome_jogador, adv1, nome_parceiro, adv2]
            self.maos = {
                0: pedras[0:7],
                1: pedras[7:14],
                2: pedras[14:21],
                3: pedras[21:28]
            }
            self.monte = []
            self.num_jogadores = 4

        self.tabuleiro = []
        self.indice_selecionado = 0
        self.passagens_jogadores = {i: set() for i in range(self.num_jogadores)}
        self.jogador_atual = self.decidir_quem_comeca()

    def decidir_quem_comeca(self):
        donos_por_valor_de_carroca = {}
        for idx in range(self.num_jogadores):
            for pedra in self.maos[idx]:
                if pedra[0] == pedra[1]:
                    donos_por_valor_de_carroca[pedra[0]] = idx

        if donos_por_valor_de_carroca:
            maior_valor = max(donos_por_valor_de_carroca)
            return donos_por_valor_de_carroca[maior_valor]

        return random.randrange(self.num_jogadores)


# ---------- Acoes de Jogo ----------

def remover_pedra(jogo, pedra, idx_jogador):
    mao = jogo.maos[idx_jogador]
    mao.remove(pedra)
    if idx_jogador == 0 and mao and jogo.indice_selecionado >= len(mao):
        jogo.indice_selecionado = len(mao) - 1

def pescar_do_monte(jogo, idx_jogador):
    if not jogo.monte:
        return None
    pedra = jogo.monte.pop()
    jogo.maos[idx_jogador].append(pedra)
    return pedra


def tentar_jogar(jogo, pedra, idx_jogador, lado_forcado=None):
    esquerda, direita = extremidades(jogo.tabuleiro)
    nome = jogo.nomes[idx_jogador]
    
    if esquerda is None:
        jogo.tabuleiro.append(pedra)
        remover_pedra(jogo, pedra, idx_jogador)
        som_batida_domino()
        atualizar_tabuleiro_visual(jogo)
        anunciar(f"{nome} jogou {formatar_pedra(pedra)}", f"{nome} jogou {falar_pedra_juiz(pedra)}",
                 interromper=False, pausar_depois=True, pausa=PAUSA_JOGADA)
        return True

    encaixa_esquerda = pedra_encaixa(pedra, esquerda)
    encaixa_direita = pedra_encaixa(pedra, direita)

    if not encaixa_esquerda and not encaixa_direita:
        if idx_jogador == 0:
            som_erro()
            anunciar("Nao encaixa.", interromper=True)
        return False

    lado = None
    if lado_forcado in ('ESQUERDA', 'DIREITA'):
        lado = lado_forcado
    elif encaixa_esquerda and encaixa_direita:
        if idx_jogador == 0:
            anunciar("Escolha o lado: Esquerda, Direita ou Esc.", interromper=True)
            while lado is None:
                tecla = ler_tecla()
                if tecla == 'ESQUERDA':
                    lado = 'ESQUERDA'
                elif tecla == 'DIREITA':
                    lado = 'DIREITA'
                elif tecla == 'ESC':
                    anunciar("Cancelado.", interromper=True)
                    return False
        else:
            lado = 'ESQUERDA' if random.choice([True, False]) else 'DIREITA'
    elif encaixa_esquerda:
        lado = 'ESQUERDA'
    else:
        lado = 'DIREITA'

    if lado == 'ESQUERDA':
        pedra_orientada = orientar_para_esquerda(pedra, esquerda)
        jogo.tabuleiro.insert(0, pedra_orientada)
    else:
        pedra_orientada = orientar_para_direita(pedra, direita)
        jogo.tabuleiro.append(pedra_orientada)

    remover_pedra(jogo, pedra, idx_jogador)
    som_batida_domino()
    atualizar_tabuleiro_visual(jogo)

    fala_juiz = f"{nome}: {falar_pedra_juiz(pedra)} na {lado.lower()}"
    anunciar(f"{nome} jogou {formatar_pedra(pedra)} na {lado.lower()}", fala_juiz,
             interromper=False, pausar_depois=True, pausa=PAUSA_JOGADA)

    # Provocações Sádicas da Luna ao jogar
    if nome == "Luna" and random.random() < 0.8:
        nome_alvo = jogo.nomes[0] if hasattr(jogo, "nomes") and jogo.nomes else "jogador"
        provocacao = random.choice(PROVOCACOES_LUNA_JOGADA).format(nome=nome_alvo)
        anunciar(f'Luna diz: "{provocacao}"', f'Luna diz: {provocacao}', interromper=False, pausar_depois=True)

    return True


# ---------- IA Sádica da Luna e Execução de Turno ----------

def escolher_jogada_ia(jogo, idx_jogador, nivel=0):
    validas = pedras_jogaveis(jogo.maos[idx_jogador], jogo.tabuleiro)
    if not validas:
        return None, None

    nome_jogador = jogo.nomes[idx_jogador]

    # Ativa o motor Bayesiano / Monte Carlo se for a Luna ou Nível SÁDICO
    if nome_jogador == "Luna" or nivel >= 4:
        return motor_luna.escolher_melhor_jogada(jogo, idx_jogador, validas)

    # Níveis básicos de fallback
    carrocas = [p for p in validas if p[0] == p[1]]
    if carrocas:
        return max(carrocas, key=sum), None
    return max(validas, key=sum), None

def turno_ia(jogo, idx_jogador, nivel=0):
    nome = jogo.nomes[idx_jogador]

    while not tem_jogada_valida(jogo.maos[idx_jogador], jogo.tabuleiro):
        if jogo.modo in (1, 3) and jogo.monte:
            pescar_do_monte(jogo, idx_jogador)
            atualizar_tabuleiro_visual(jogo)
            anunciar(f"{nome} nao tinha jogada e comprou uma pedra do monte.",
                     interromper=False, pausar_depois=True, pausa=PAUSA_JOGADA)
        else:
            if jogo.tabuleiro:
                esq, dir_ = extremidades(jogo.tabuleiro)
                jogo.passagens_jogadores[idx_jogador].add(esq)
                jogo.passagens_jogadores[idx_jogador].add(dir_)
            anunciar(f"{nome} passou a vez.", interromper=False, pausar_depois=True, pausa=PAUSA_JOGADA)
            som_passar()
            return

    pedra, lado = escolher_jogada_ia(jogo, idx_jogador, nivel)
    if pedra:
        tentar_jogar(jogo, pedra, idx_jogador, lado)


# ---------- Turno do Jogador Principal ----------

def turno_jogador(jogo):
    if jogo.maos[0]:
        jogo.indice_selecionado = min(jogo.indice_selecionado, len(jogo.maos[0]) - 1)
    
    msg_vez = "Sua vez. Mesa: " + falar_pontas_juiz(jogo.tabuleiro)
    anunciar(msg_vez, msg_vez, interromper=False, pausar_depois=True, pausa=PAUSA_JOGADA)

    while True:
        tecla = ler_tecla()
        
        if tecla == 'ESQUERDA':
            if jogo.maos[0]:
                jogo.indice_selecionado = (jogo.indice_selecionado - 1) % len(jogo.maos[0])
                som_navegar()
                pedra = jogo.maos[0][jogo.indice_selecionado]
                atualizar_tabuleiro_visual(jogo)
                anunciar(formatar_pedra(pedra), falar_pedra_juiz(pedra), interromper=True)

        elif tecla == 'DIREITA':
            if jogo.maos[0]:
                jogo.indice_selecionado = (jogo.indice_selecionado + 1) % len(jogo.maos[0])
                som_navegar()
                pedra = jogo.maos[0][jogo.indice_selecionado]
                atualizar_tabuleiro_visual(jogo)
                anunciar(formatar_pedra(pedra), falar_pedra_juiz(pedra), interromper=True)

        elif tecla == 'ESPACO':
            if not jogo.maos[0]:
                som_erro()
                anunciar("Sem pedras.", interromper=True)
                continue
            pedra = jogo.maos[0][jogo.indice_selecionado]
            if tentar_jogar(jogo, pedra, 0):
                return

        elif tecla in ('ENTER', 'PONTAS'):
            pontas = falar_pontas_juiz(jogo.tabuleiro)
            anunciar("Pontas: " + pontas, pontas, interromper=True)

        elif tecla in ('TAB', 'COMPRAR'):
            if tem_jogada_valida(jogo.maos[0], jogo.tabuleiro):
                som_erro()
                anunciar("Voce tem jogada.", interromper=True)
            elif jogo.modo in (1, 3) and jogo.monte:
                pedra_comprada = pescar_do_monte(jogo, 0)
                jogo.indice_selecionado = len(jogo.maos[0]) - 1
                atualizar_tabuleiro_visual(jogo)
                anunciar(f"Sem jogada. Comprou {formatar_pedra(pedra_comprada)} do monte.",
                         f"Comprou {falar_pedra_juiz(pedra_comprada)}",
                         interromper=True, pausar_depois=True, pausa=PAUSA_JOGADA)
                if tem_jogada_valida(jogo.maos[0], jogo.tabuleiro):
                    anunciar("Agora voce tem jogada.", interromper=False, pausar_depois=True)
                else:
                    anunciar(f"Ainda sem jogada. Restam {len(jogo.monte)} pedra(s) no monte.",
                             interromper=False, pausar_depois=True)
            elif jogo.modo in (1, 3) and not jogo.monte:
                som_erro()
                anunciar("Monte vazio. Pressione Backspace para passar a vez.", interromper=True)
            else:
                som_erro()
                anunciar("Nao ha monte neste modo de jogo.", interromper=True)

        elif tecla == 'BACKSPACE':
            if tem_jogada_valida(jogo.maos[0], jogo.tabuleiro):
                som_erro()
                anunciar("Voce tem jogada.", interromper=True)
            elif jogo.modo in (1, 3) and jogo.monte:
                som_erro()
                anunciar("Ainda ha pedras no monte. Pressione Tab para comprar.", interromper=True)
            else:
                if jogo.tabuleiro:
                    esq, dir_ = extremidades(jogo.tabuleiro)
                    jogo.passagens_jogadores[0].add(esq)
                    jogo.passagens_jogadores[0].add(dir_)
                som_passar()
                anunciar("Passou a vez.", interromper=False, pausar_depois=True, pausa=PAUSA_JOGADA)
                
                # Provocação Sádica da Luna ao passar a vez
                if jogo.adv_principal == "Luna":
                    nome_alvo = jogo.nomes[0] if hasattr(jogo, "nomes") and jogo.nomes else "jogador"
                    provocacao = random.choice(PROVOCACOES_LUNA_PASSA).format(nome=nome_alvo)
                    anunciar(f'Luna diz: "{provocacao}"', f'Luna diz: {provocacao}', interromper=False, pausar_depois=True)
                return

        elif tecla == 'ESC':
            anunciar("Saindo do jogo.", interromper=True)
            sys.exit()


# ---------- Menu e Configuracao Inicial ----------

def escolher_opcao(mensagem, opcoes_validas):
    while True:
        print("\n" + mensagem, flush=True)
        try:
            resposta = input("> ").strip().lower()
        except EOFError:
            resposta = ""
        if resposta in opcoes_validas:
            return resposta
        print("Opcao invalida. Tente novamente.", flush=True)


def exibir_ajuda():
    texto_ajuda = (
        "\n--- AJUDA ---"
        "\nSetas Esquerda/Direita: navegam pelas pedras."
        "\nBarra de Espaco: joga a pedra."
        "\nTab ou C: compra do monte (no modo 1 vs 1 ou Luna)."
        "\nBackspace: passa a vez."
        "\nT, J ou Enter: le as pontas da mesa."
        "\nEsc: sai do jogo.\n"
    )
    print(texto_ajuda, flush=True)


def selecionar_modo_e_jogadores():
    while True:
        prompt_menu = (
            "==============================================\n"
            "             DOMINÓ ACESSÍVEL                 \n"
            "==============================================\n"
            "1 - Jogar Modo Individual vs Argentina (1 vs 1)\n"
            "2 - Jogar Modo Duplas (2 vs 2)\n"
            "3 - Jogar Modo Desafio  com Luna (Bayes/Monte Carlo)\n"
            "4 - Sair\n"
            "5 - Ajuda\n"
            "Digite a opção desejada (1, 2, 3, 4 ou 5) e pressione Enter:"
        )
        opcao = escolher_opcao(prompt_menu, ('1', '2', '3', '4', '5'))
        
        if opcao == '1':
            modo = 1
            print("\nModo Individual selecionado.", flush=True)
            break
        elif opcao == '2':
            modo = 2
            print("\nModo Duplas selecionado.", flush=True)
            break
        elif opcao == '3':
            modo = 3
            print("\nmodo luna.", flush=True)
            break
        elif opcao == '4':
            print("Saindo do jogo.", flush=True)
            sys.exit()
        elif opcao == '5':
            exibir_ajuda()

    print("\nInforme seu nome de jogador:")
    try:
        nome_dig = input("Nome (pressione Enter para manter Edson): ").strip()
        nome_usuario = nome_dig if nome_dig else "Edson"
    except Exception:
        nome_usuario = "Edson"
    print(f"Jogador registrado: {nome_usuario}", flush=True)

    parceiro_nome = None
    if modo == 2:
        prompt_parceiro = "Escolha seu parceiro da Seleção Brasileira:\n"
        for i, nome in enumerate(CRAQUES_SELECAO, start=1):
            prompt_parceiro += f"{i} - {nome}\n"
        prompt_parceiro += "Digite o número do parceiro e pressione Enter:"
        
        numeros_validos = [str(i) for i in range(1, len(CRAQUES_SELECAO) + 1)]
        escolha = escolher_opcao(prompt_parceiro, numeros_validos)
        parceiro_nome = CRAQUES_SELECAO[int(escolha) - 1]
        print(f"Parceiro selecionado: {parceiro_nome}", flush=True)

    return modo, nome_usuario, parceiro_nome


# ---------- Loop da Partida ----------

def jogo_bloqueado(jogo):
    if jogo.modo in (1, 3) and jogo.monte:
        return False
    for i in range(jogo.num_jogadores):
        if tem_jogada_valida(jogo.maos[i], jogo.tabuleiro):
            return False
    return True

def executar_partida(modo, nome_usuario, parceiro_nome, nivel_atual, vitorias_jogador, historico_adversarios):
    jogo = JogoDomino(modo, nome_usuario, parceiro_nome, historico_adversarios)
    
    if modo != 3:
        historico_adversarios.append(jogo.adv_principal)
        if len(historico_adversarios) > 5:
            historico_adversarios.pop(0)

    nome_nivel = NOMES_NIVEL[nivel_atual]
    atualizar_tabuleiro_visual(jogo)

    if modo == 1:
        anunciar(f"Partida Individual iniciada! {jogo.nomes[0]} versus {jogo.nomes[1]}.", interromper=False, pausar_depois=True)
    elif modo == 3:
        anunciar(f"Desafio Sádico Iniciado! {jogo.nomes[0]} versus Luna (Inferência Bayesiana e Monte Carlo).", interromper=False, pausar_depois=True)
    else:
        anunciar(f"Partida em Duplas iniciada! Sua dupla: {jogo.nomes[0]} e {jogo.nomes[2]}.", interromper=False, pausar_depois=True)
        anunciar(f"Dupla adversária: {jogo.nomes[1]} e {jogo.nomes[3]}.", interromper=False, pausar_depois=True)

    anunciar(f"Quem inicia a partida: {jogo.nomes[jogo.jogador_atual]}", interromper=False, pausar_depois=True)

    while True:
        for idx in range(jogo.num_jogadores):
            if not jogo.maos[idx]:
                vencedor = jogo.nomes[idx]
                anunciar(f"{vencedor} bateu!", interromper=False, pausar_depois=True)
                
                if modo in (1, 3):
                    if idx == 0:
                        anunciar("Você venceu a partida!", interromper=True)
                        som_vitoria()
                        return True, False
                    else:
                        anunciar(f"{vencedor} venceu a partida.", interromper=True)
                        som_derrota()
                        return False, False
                else:
                    if idx in (0, 2):
                        anunciar(f"Sua dupla ({jogo.nomes[0]} e {jogo.nomes[2]}) venceu!", interromper=True)
                        som_vitoria()
                        return True, False
                    else:
                        anunciar(f"A dupla adversária ({jogo.nomes[1]} e {jogo.nomes[3]}) venceu.", interromper=True)
                        som_derrota()
                        return False, False

        curr = jogo.jogador_atual
        if curr == 0:
            turno_jogador(jogo)
        else:
            turno_ia(jogo, curr, nivel_atual)

        if jogo_bloqueado(jogo):
            if modo in (1, 3):
                p_user = contar_pontos(jogo.maos[0])
                p_adv = contar_pontos(jogo.maos[1])
                anunciar(f"Jogo fechado! Seus pontos: {p_user}. {jogo.nomes[1]}: {p_adv} pontos.", interromper=False, pausar_depois=True)
                if p_user < p_adv:
                    anunciar("Você venceu por menor pontuação!", interromper=True)
                    som_vitoria()
                    return True, False
                elif p_user > p_adv:
                    anunciar(f"{jogo.nomes[1]} venceu por menor pontuação.", interromper=True)
                    som_derrota()
                    return False, False
                else:
                    anunciar("Empate!", interromper=True)
                    return False, True
            else:
                p_sua_dupla = contar_pontos(jogo.maos[0]) + contar_pontos(jogo.maos[2])
                p_adv_dupla = contar_pontos(jogo.maos[1]) + contar_pontos(jogo.maos[3])
                anunciar(f"Jogo fechado! Sua dupla: {p_sua_dupla} pontos. Dupla adversária: {p_adv_dupla} pontos.", interromper=False, pausar_depois=True)
                if p_sua_dupla < p_adv_dupla:
                    anunciar("Sua dupla venceu por menor pontuação!", interromper=True)
                    som_vitoria()
                    return True, False
                elif p_sua_dupla > p_adv_dupla:
                    anunciar("A dupla adversária venceu por menor pontuação.", interromper=True)
                    som_derrota()
                    return False, False
                else:
                    anunciar("Empate!", interromper=True)
                    return False, True

        jogo.jogador_atual = (jogo.jogador_atual + 1) % jogo.num_jogadores

def processar_resultado(vitorias_jogador, nivel_atual, venceu, empatou):
    if empatou or not venceu:
        return vitorias_jogador, nivel_atual

    vitorias_jogador += 1
    anunciar(f"Total de vitórias: {vitorias_jogador}.", interromper=False, pausar_depois=True)
    return vitorias_jogador, 4

def loop_jogo_logica():
    vitorias_jogador = 0
    nivel_atual = 4
    historico_adversarios = []

    while True:
        modo, nome_usuario, parceiro_nome = selecionar_modo_e_jogadores()
        
        jogando = True
        while jogando:
            venceu, empatou = executar_partida(
                modo, nome_usuario, parceiro_nome, nivel_atual, vitorias_jogador, historico_adversarios
            )
            vitorias_jogador, nivel_atual = processar_resultado(vitorias_jogador, nivel_atual, venceu, empatou)

            if venceu:
                anunciar("Preparando a próxima partida...", interromper=False, pausar_depois=True)
                continue

            resposta = escolher_opcao(
                "Jogar novamente nesta mesma modalidade? Digite S para sim ou N para não, e pressione Enter:",
                ('s', 'n')
            )
            if resposta == 'n':
                jogando = False

        resposta = escolher_opcao(
            "Deseja voltar ao menu principal? Digite S para sim ou N para não, e pressione Enter:",
            ('s', 'n')
        )
        if resposta == 'n':
            anunciar("Saindo do jogo.", interromper=True)
            if _janela_root:
                _janela_root.quit()
            sys.exit()

def main():
    iniciar_tabuleiro_visual()
    
    thread_jogo = threading.Thread(target=loop_jogo_logica, daemon=True)
    thread_jogo.start()
    
    if TABULEIRO_VISUAL_DISPONIVEL and _janela_root:
        _janela_root.mainloop()

if __name__ == "__main__":
    main()