"""
Menu Principal de Jogos da Luna Acessíveis
--------------------------------------------
Menu interativo para o usuário escolher qual jogo deseja jogar.
Suporta: Domino, Truco (via navegador), Calculadora Luna e Forca.

Comandos:
  Use as setas para navegar (no terminal) ou digite o número.
  Espaço para selecionar.
  Esc para sair.
"""

import subprocess
import sys
import os

def limpar_console():
    """Limpa o console dependendo do SO."""
    os.system('cls' if os.name == 'nt' else 'clear')

def falar(texto):
    """Tenta usar o motor de voz disponível."""
    try:
        import winsound
        # Apenas um beep curto como confirmação auditiva
        winsound.Beep(800, 100)
    except Exception:
        pass

def exibir_menu():
    """Exibe o menu principal de jogos."""
    opcoes = [
        "1 - Domino Acessível (vs Argentina ou Luna IA)",
        "2 - Truco (abrir no navegador)",
        "3 - Calculadora Luna",
        "4 - Jogo da Forca",
        "5 - Academia Python da Luna (Aprenda Programação com Áudio e Quiz)",
        "6 - Sair"
    ]
    
    while True:
        limpar_console()
        print("="*50)
        print("      MENU PRINCIPAL - JOGOS DA LUNA")
        print("="*50)
        for opcao in opcoes:
            print(opcao)
        print("="*50)
        
        try:
            escolha = input("\nEscolha um jogo (1-6): ").strip()
            if escolha == '1':
                falar("Iniciando domino acessível")
                subprocess.run([sys.executable, "domino.py"], check=True)
            elif escolha == '2':
                falar("Iniciando truco")
                # Tenta abrir o truco no navegador padrão
                truco_path = os.path.abspath("..\\truco\\index.html")
                if os.path.exists(truco_path):
                    subprocess.run([sys.executable, "-m", "webbrowser", truco_path], check=True)
                else:
                    print("Arquivo de truco não encontrado em C:\\truco\\index.html")
                    input("Pressione Enter para continuar...")
            elif escolha == '3':
                falar("Iniciando calculadora luna")
                subprocess.run([sys.executable, "luna\\luna_calc.py.py"], check=True)
            elif escolha == '4':
                falar("Iniciando forca")
                subprocess.run([sys.executable, "forca.py"], check=True)
            elif escolha == '5':
                falar("Iniciando academia python da luna")
                subprocess.run([sys.executable, "academia_python_luna.py"], check=True)
            elif escolha == '6':
                print("\nSaindo dos jogos. Até mais!")
                falar("Até mais!")
                break
            else:
                print("Opção inválida! Digite um número de 1 a 6.")
                input("Pressione Enter para tentar novamente.")
        except KeyboardInterrupt:
            print("\n\nSaindo pelo usuário.")
            falar("Saindo do sistema.")
            break
        except Exception as e:
            print(f"\nErro ao executar jogo: {e}")
            try:
                input("Pressione Enter para continuar...")
            except EOFError:
                pass

if __name__ == "__main__":
    exibir_menu()