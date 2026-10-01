# 📦 MysteryBoxes - Plugin de Caixas Misteriosas (Minecraft 1.21.x)

Um plugin completo, moderno e seguro de **Caixas Misteriosas (Crates / Mystery Boxes)** para servidores de Minecraft **1.21.x** (Spigot, Paper, Purpur) rodando em **Java 21**.

---

## ✨ Características Principais

- 🔑 **Sistema Seguro de Chaves (Anti-Fraude):**
  - As chaves utilizam o `PersistentDataContainer` (NBT Moderno do Spigot).
  - **Impossível clonar ou forjar** renomeando itens na bigorna.
  - Compatível com itens da versão 1.21 como `TRIAL_KEY`, `OMINOUS_TRIAL_KEY` e a nova `MACE` (Maça).

- 🎰 **Animação Estilo Roleta (CS:GO / Cassino):**
  - Interface gráfica interativa (GUI de 27 slots) com trilha de rolagem dos prêmios.
  - Desaceleração suave e realista da roleta.
  - Efeitos sonoros sincronizados a cada rolagem (`BLOCK_NOTE_BLOCK_PLING`).
  - Destaque visual no item vencedor com painéis luminosos, sons de vitória e fogos de artifício.

- 🛡️ **Proteção Anti-Exploit e Conexão:**
  - Impossível retirar itens decorativos ou da roleta durante o giro.
  - Caso o jogador feche a janela ou desconecte durante o sorteio, a recompensa é **entregue automaticamente** ao seu inventário ou ao chão (se cheio), garantindo que ninguém perca seus itens.

- 👁️ **Menu de Prévia das Recompensas:**
  - Clique com o botão esquerdo no bloco da caixa para abrir um menu que mostra todos os prêmios e a porcentagem exata de probabilidade de cada um.

- 🧱 **Caixas Físicas no Mundo & Caixas Virtuais:**
  - Associe qualquer bloco (Baú, Baú do Fim, Âncora de Renascimento, etc.) a uma caixa misteriosa usando `/caixa setcrate <id>`.
  - Partículas ambientes decorativas saindo da caixa no mundo.
  - Proteção contra quebra acidental de blocos configurados como caixas misteriosas.
  - Suporte também para abertura virtual via comando `/caixa open <id>`.

---

## 📋 Comandos e Permissões

### Comandos de Jogador
| Comando | Descrição | Permissão |
| :--- | :--- | :--- |
| `/caixa preview <id>` | Abre o menu de prévia com chances e recompensas | `mysteryboxes.preview` (padrão) |
| `/caixa open <id>` | Abre uma caixa misteriosa virtualmente com sua chave | `mysteryboxes.open` (padrão) |
| `/caixa list` | Lista todas as caixas cadastradas no servidor | `mysteryboxes.use` (padrão) |
| `/caixa help` | Mostra o menu de ajuda do plugin | `mysteryboxes.use` (padrão) |

*Aliases aceitos:* `/mysterybox`, `/crate`, `/caixas`, `/chaves`

### Comandos de Administrador
| Comando | Descrição | Permissão |
| :--- | :--- | :--- |
| `/caixa givekey <jogador> <caixa> [quantidade]` | Dá uma ou mais chaves da caixa para o jogador | `mysteryboxes.admin` (OP) |
| `/caixa setcrate <id>` | Define o bloco para o qual você está olhando como uma Caixa | `mysteryboxes.admin` (OP) |
| `/caixa removecrate` | Remove a Caixa do bloco para o qual você está olhando | `mysteryboxes.admin` (OP) |
| `/caixa reload` | Recarrega todos os arquivos de configuração (`config.yml`, `crates.yml`, `locations.yml`) | `mysteryboxes.admin` (OP) |

---

## 🛠️ Como Compilar o Projeto

Este projeto utiliza o **Apache Maven** e **Java 21**.

1. Certifique-se de ter o **Java 21** e o **Maven** instalados no computador.
2. Abra o terminal na pasta raiz deste projeto:
   ```bash
   mvn clean package
   ```
3. O arquivo final compilado estará na pasta `target/`:
   ```
   target/MysteryBoxes-1.0.0.jar
   ```
4. Coloque o `.jar` na pasta `plugins/` do seu servidor Spigot/Paper 1.21.x e reinicie o servidor.

---

## ⚙️ Como Configurar Novas Caixas (`crates.yml`)

O arquivo `crates.yml` permite criar quantas caixas desejar. Exemplo de estrutura:

```yaml
crates:
  comum:
    name: "&a&lCaixa Comum"
    key:
      material: TRIPWIRE_HOOK
      name: "&a&lChave Comum"
      lore:
        - "&7Use esta chave para abrir"
        - "&7uma &aCaixa Comum&7!"
      glowing: true
    rewards:
      diamantes:
        chance: 20.0 # Porcentagem ou peso de probabilidade
        display:
          material: DIAMOND
          amount: 5
          name: "&b5x Diamantes"
        items:
          - material: DIAMOND
            amount: 5
      premio_especial:
        chance: 5.0
        display:
          material: NETHERITE_SWORD
          amount: 1
          name: "&4Espada do Dragão"
          enchantments:
            - "SHARPNESS:5"
            - "FIRE_ASPECT:2"
        items:
          - material: NETHERITE_SWORD
            amount: 1
            name: "&4Espada do Dragão"
            enchantments:
              - "SHARPNESS:5"
              - "FIRE_ASPECT:2"
        broadcast: true
        broadcast-message: "&6&l[CAIXA] &f%player% &etirou uma &4Espada do Dragão &ena Caixa!"
      vip_comando:
        chance: 2.0
        display:
          material: EMERALD_BLOCK
          amount: 1
          name: "&a&lVIP Bronze (30 Dias)"
        commands:
          - "lp user %player% parent addtemp vip 30d"
        broadcast: true
        broadcast-message: "&a&l[PARABÉNS] &f%player% &aganhou um VIP na Caixa Misteriosa!"
```

### Placeholders Suportados nos Comandos e Broadcast:
- `%player%` - Nome do jogador que abriu a caixa.
- `%crate%` - Nome de exibição da caixa.
- `%reward%` - Nome de exibição do prêmio ganho.

---

## 🎮 Guia Rápido de Uso no Servidor

1. Coloque um bloco de **Baú do Fim (Ender Chest)** ou outro bloco no chão do seu Spawn.
2. Mire a mira no bloco e digite:
   ```
   /caixa setcrate lendaria
   ```
3. O bloco agora emitirá partículas e está protegido.
4. Dê uma chave para você mesmo testar:
   ```
   /caixa givekey seu_nick lendaria 1
   ```
5. Segure a chave na mão e **clique com o botão direito** no bloco para iniciar a roleta!
6. Se quiser ver os prêmios antes de abrir, basta **clicar com o botão esquerdo** no bloco da caixa.
