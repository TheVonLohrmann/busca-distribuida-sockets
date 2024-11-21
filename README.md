# Sistema de Busca Distribuída com Sockets

Este projeto é uma implementação de um sistema de busca distribuída utilizando **Java 17** e sockets, desenvolvido como parte da disciplina *Programação Concorrente e Distribuída (GPE02M0609)*. 

## Objetivo
Implementar um sistema que:
- Recebe uma substring de um cliente.
- Realiza uma busca distribuída em dois servidores que processam arquivos JSON contendo dados de artigos do arXiv.
- Retorna os resultados ao cliente.

## Arquitetura do Sistema
O sistema é composto por:
- **Cliente**: Envia a substring e recebe os resultados.
- **Servidor A**: Coordena a busca, processa metade dos dados e solicita a busca ao Servidor B.
- **Servidor B**: Realiza a busca na outra metade dos dados.

### Diagrama de Comunicação
![Diagrama de Comunicação](docs/diagrama_comunicacao.png) ( ainda sera adicionado )

1. O Cliente envia uma substring para o Servidor A.
2. O Servidor A:
   - Processa metade dos dados localmente.
   - Envia a substring para o Servidor B.
3. O Servidor B processa sua parte e envia o resultado para o Servidor A.
4. O Servidor A combina os resultados e retorna ao Cliente.

## Como Executar
### Pré-requisitos
- Java 17 instalado
- Maven configurado
- Clone deste repositório:
  ```bash
  git clone [[https://github.com/TheVonLohrmann/busca-distribuida-sockets.git
  cd busca-distribuida-sockets]
