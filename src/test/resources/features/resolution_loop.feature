# language: pt
@resolution_loop
Funcionalidade: Loop de Resolução Automática
  Como um gestor de marca
  Eu quero que as reclamações públicas sejam resolvidas em um chat privado
  E que uma resposta pública de encerramento seja gerada automaticamente para proteger a reputação da marca

  Contexto:
    Dado que o sistema de análise de mídia social está rodando

  Cenário: Resolução completa de uma reclamação do Instagram
    Dado que uma marca "BetaCorp" existe
    E o usuário "maria_silva" postou uma reclamação no "Instagram" com "Meu pedido #999 não chegou"
    Quando a IA processa a mensagem
    E eu resolvo o caso com a nota "Pedido localizado e entrega agendada para amanhã"
    Então o status da análise deve ser "RESOLVED"
    E uma mensagem pública de encerramento deve ser gerada pela IA
