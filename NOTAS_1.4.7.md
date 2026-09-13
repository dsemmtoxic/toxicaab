# Toxic 1.4.7 — atualizações

`versionCode`: **91** · Android mínimo: **6.0 (API 23)** · alvo: **API 36**.

## Online de amigos e removidos

O indicador dos cartões consulta o perfil público atual, separadamente dos registros de amizade e remoção. As datas históricas continuam sendo exibidas.

- Consulta apenas cartões visíveis, com até três pedidos simultâneos.
- Mantém o resultado em memória por até 30 segundos na página atual. Revalida ao voltar ao aplicativo e ao mudar de página ou aba.
- Durante a consulta, ou se a resposta falhar/não informar presença, o estado é desconhecido: nenhum ponto verde é mantido com base no histórico.
- Ao sair do perfil ou colocar o app em segundo plano, cancela as tarefas da área. Respostas de uma pesquisa anterior não atualizam os novos cartões.
- As chamadas JSON usam limites de tempo e tamanho de resposta e respeitam pausas informadas pelo servidor em erros de limitação ou indisponibilidade.

Isso não transforma a presença em uma conexão em tempo real: ela continua dependendo da informação disponibilizada pelo serviço e da conexão do aparelho.

## Grupos

Os textos visíveis de acesso e função foram substituídos pelos arquivos enviados:

| Informação | Arquivo |
| --- | --- |
| Aberto | `open.png` |
| Solicitação | `exclusive.gif` |
| Fechado | `closed.gif` |
| Membro | `member.png` |
| Admin | `admin.png` |
| Dono | `owner.png` |

Os rótulos traduzidos permanecem nas descrições de acessibilidade dos ícones. A seleção depende dos dados do grupo, independentemente do idioma da interface; o ícone de dono exige a confirmação de propriedade que o aplicativo já utilizava.

Os seis anexos recebidos contêm imagens JPEG estáticas, apesar dos nomes `.png`/`.gif`. Foram integrados sem alterar seus bytes, em `app/src/main/res/raw/`, com decodificação automática. Portanto, os dois GIFs não terão animação nesta entrega. Os arquivos podem ser substituídos nesse mesmo diretório pelos originais transparentes/animados, mantendo os nomes; o carregador já aceita GIF animado.

## Estabilidade e interface

- Substituição da dependência de `CompletableFuture` por uma implementação com APIs disponíveis no Android 6.
- Tratamento de Voltar pelo mecanismo moderno no Android 13 ou superior, incluindo o alvo Android 16, com suporte à navegação anterior nas versões antigas.
- Tarefas separadas por responsabilidade, cancelamento de pesquisas anteriores e proteção contra verificações simultâneas de favoritos.
- Atualização dos contêineres das seções que mudaram, preservando os demais cartões e carrosséis. A posição vertical usa uma seção como referência durante alterações.
- Cópias independentes dos dados publicados na interface e descarte de snapshots completos mais antigos que o já exibido.
- Recuperação da referência do perfil, hotel, abas, páginas e posição quando o Android recria a atividade. O conteúdo é consultado novamente; sinais antigos de online não são restaurados. A restauração de páginas tem limite de 20 consultas adicionais e cede à interação do usuário.
- Repetição de páginas sem novos registros interrompe o carregamento automático; erros de paginação em amigos, removidos e emblemas exibem uma ação para tentar novamente na própria seção.
- Transporte JSON, presença, navegação, modelos, snapshots, política de anúncios e armazenamento da autorização de compra foram separados em classes menores. A atividade ainda contém a composição visual e partes das integrações existentes.
- Diagnósticos registram operação, categoria de falha e código HTTP quando disponível, sem incluir nomes de perfil, endereços completos ou tokens de compra nesses novos registros.

## Favoritos em segundo plano

As verificações em primeiro plano continuam com intervalo de 15 segundos e não se sobrepõem. Em segundo plano, o Android agenda uma tarefa periódica com rede disponível, intervalo mínimo de 15 minutos e persistência após reinicialização do aparelho. O receiver antigo apenas encaminha a tarefa ao agendador.

O Android pode adiar a execução conforme bateria e restrições do sistema. As notificações em segundo plano consideram uma transição confirmada de offline para online, sem exigir que o login tenha ocorrido nos últimos três minutos. Sessões curtas entre duas consultas podem não ser detectadas.

## Verificação e compilação

Executado nesta entrega:

- **9 verificações de lógica Java aprovadas**, cobrindo conclusão concorrente, cancelamento, timeout, propagação de falhas, exclusão de tarefas simultâneas, cancelamento da fila e limites das políticas de anúncio.
- Análise sintática dos **26 arquivos Java**.
- Validação dos XMLs, referências de textos/recursos, nomes de tradução e sintaxe dos workflows.
- Conferência de que os seis recursos incorporados são idênticos aos anexos.

Foram adicionados outros oito cenários JUnit para presença, isolamento de dados, preservação de páginas, ordem de snapshots, classificação dos grupos e pausas de requisições. Eles serão executados junto dos testes de lógica pelos workflows de geração de APK e AAB.

**Não foi executada uma compilação Android completa, Android Lint ou teste em emulador/aparelho nesta entrega:** este ambiente não possui Android SDK/Gradle. As verificações de sintaxe e lógica não substituem essas etapas. Os workflows agora executam testes e Lint antes de gerar o aplicativo e disponibilizam os relatórios.

Em uma instalação com JDK 17, Gradle 8.11.1 e o SDK do projeto:

```sh
gradle :app:testDebugUnitTest :app:lintDebug --no-daemon
gradle :app:assembleDebug --no-daemon
```

Para gerar o AAB assinado, use o workflow existente com os mesmos secrets de assinatura. O procedimento está em `PLAY_STORE_AAB.md`.
