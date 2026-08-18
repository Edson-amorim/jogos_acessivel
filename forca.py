import random

def escolher_palavra():
    palavras = [
        "python", "programacao", "computador", "teclado",
        "acessibilidade", "leitor", "algoritmo", "variavel",
        "funcao", "biblioteca"
    ]
    return random.choice(palavras)

def mostrar_progresso(palavra, letras_certas):
    progresso = ""
    for letra in palavra:
        if letra in letras_certas:
            progresso += letra + " "
        else:
            progresso += "_ "
    return progresso.strip()

def jogar():
    palavra = escolher_palavra()
    letras_certas = set()
    letras_erradas = set()
    tentativas_restantes = 6

    print("Bem-vindo ao jogo da forca!")
    print(f"A palavra tem {len(palavra)} letras.")
    print("Você tem 6 tentativas erradas permitidas.")
    print()

    while tentativas_restantes > 0:
        print(f"Palavra: {mostrar_progresso(palavra, letras_certas)}")
        print(f"Tentativas restantes: {tentativas_restantes}")

        if letras_erradas:
            print(f"Letras erradas já tentadas: {', '.join(sorted(letras_erradas))}")

        chute = input("Digite uma letra: ").strip().lower()

        if len(chute) != 1 or not chute.isalpha():
            print("Por favor, digite apenas uma letra válida.")
            print()
            continue

        if chute in letras_certas or chute in letras_erradas:
            print("Você já tentou essa letra. Tente outra.")
            print()
            continue

        if chute in palavra:
            letras_certas.add(chute)
            print(f"Boa! A letra '{chute}' está na palavra.")
        else:
            letras_erradas.add(chute)
            tentativas_restantes -= 1
            print(f"A letra '{chute}' não está na palavra.")

        print()

        if all(letra in letras_certas for letra in palavra):
            print(f"Parabéns! Você acertou a palavra: {palavra}")
            return

    print(f"Suas tentativas acabaram. A palavra era: {palavra}")

def main():
    jogar()
    while True:
        continuar = input("Deseja jogar novamente? (s/n): ").strip().lower()
        if continuar == "s":
            print()
            jogar()
        else:
            print("Obrigado por jogar!")
            break

if __name__ == "__main__":
    main()