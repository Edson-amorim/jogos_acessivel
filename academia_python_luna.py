"""
Academia Python da Luna - Jogo Educativo Acessível (Versão Modular PC / Console)
Baseado em todos os códigos da pasta C:\edson_python.
Para Android, instale o aplicativo C:\jogos_acessivels\AcademiaPythonLuna.apk!
"""

import sys
import os

sys.path.insert(0, os.path.join(os.path.dirname(os.path.abspath(__file__)), "academia_python"))

from modulos.voz_sons import falar, som_acerto, som_erro, som_navegar, som_campeao
from modulos import (
    modulo_01_variaveis,
    modulo_02_operadores,
    modulo_03_condicionais,
    modulo_04_financiamento,
    modulo_05_lacos_forca,
    modulo_06_json_massa,
    modulo_07_sqlite_zap,
    modulo_08_pytest,
)

TODOS_MODULOS = [
    modulo_01_variaveis.MODULO,
    modulo_02_operadores.MODULO,
    modulo_03_condicionais.MODULO,
    modulo_04_financiamento.MODULO,
    modulo_05_lacos_forca.MODULO,
    modulo_06_json_massa.MODULO,
    modulo_07_sqlite_zap.MODULO,
    modulo_08_pytest.MODULO,
]


def exibir_e_falar(texto, interromper=True):
    print(texto)
    falar(texto, interromper=interromper)


def estudar_modulo(mod):
    som_navegar()
    exibir_e_falar(f"\n=== {mod['titulo']} ===")
    exibir_e_falar(f"Arquivos originais: {', '.join(mod['arquivos'])}", interromper=False)
    exibir_e_falar(f"\nExplicação da Professora Luna:\n{mod['explicacao']}", interromper=False)

    for cod in mod["codigos"]:
        input("\nPressione ENTER para ouvir o código explicado...")
        exibir_e_falar(f"\nArquivo: {cod['arquivo']}\n{cod['codigo']}")
        for linha in cod["explicacao_linhas"]:
            exibir_e_falar(f"- {linha}", interromper=False)

    input("\nPressione ENTER para iniciar o Desafio Quiz deste módulo...")
    acertos = 0
    for idx, q in enumerate(mod["questoes"], 1):
        exibir_e_falar(f"\nPergunta {idx}: {q['pergunta']}")
        for i, op in enumerate(q["opcoes"], 1):
            exibir_e_falar(f"{i} - {op}", interromper=False)
        resp = input("Sua resposta (1, 2 ou 3): ").strip()
        if resp.isdigit() and int(resp) - 1 == q["correta"]:
            som_acerto()
            acertos += 1
            exibir_e_falar(f"Correto! {q['explicacao']}")
        else:
            som_erro()
            exibir_e_falar(f"Quase! A resposta certa era a opção {q['correta'] + 1}. {q['explicacao']}")
    som_campeao()
    exibir_e_falar(f"\nMódulo concluído! Você acertou {acertos} de {len(mod['questoes'])} questões!")


def main():
    exibir_e_falar("Bem-vindo à Academia Python da Luna! Jogo educativo baseado nos seus códigos da pasta edson_python.")
    while True:
        print("\n--- ESCOLHA UM MÓDULO (1 a 8) OU 0 PARA SAIR ---")
        for m in TODOS_MODULOS:
            print(f"{m['numero']} - {m['titulo']} ({', '.join(m['arquivos'])})")
        print("0 - Sair")
        op = input("Opção: ").strip()
        if op == "0":
            exibir_e_falar("Até a próxima aula de Python!")
            break
        elif op.isdigit() and 1 <= int(op) <= len(TODOS_MODULOS):
            estudar_modulo(TODOS_MODULOS[int(op) - 1])
        else:
            som_erro()
            exibir_e_falar("Opção inválida. Escolha de 1 a 8 ou 0 para sair.")


if __name__ == "__main__":
    main()
