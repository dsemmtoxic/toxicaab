TOXIC — ATUALIZAÇÃO 1.3.10 (versionCode 52)

Esta entrega contém apenas o código-fonte. Nenhum APK ou AAB foi compilado.

CORREÇÕES

- O título "Buscando —" voltou à fonte padrão anterior; a fonte Ubuntu Habbo
  continua somente no campo de inserção do nick, conforme solicitado.
- A busca de perfis banidos repete automaticamente a resolução por nome, HHID,
  perfil completo e confirmação de banimento. São até três tentativas com
  pausas curtas antes de exibir "não encontrado".
- O backend também repete internamente o fluxo temporário do HabboWidgets e
  confirma que a página final já contém os dados do perfil antes de responder.
- O mini perfil não carrega mais uma imagem `headonly` ampliada enquanto busca
  os dados. Ele mantém `pre_load.png` e troca diretamente para o avatar completo.
- A rota real do HabboWidgets agora resolve nome para HHID antes de recuperar
  perfis banidos que não aparecem mais na API oficial do Habbo.
- Perfil privado/fechado e perfil banido foram separados: o cadeado e a
  existência do usuário na API oficial sempre impedem o falso selo Banido.
- A rotação da tela não recria a Activity, preservando o perfil aberto, a aba,
  os diálogos, o editor e a posição atual da interface.
- `toxic_top_logo` deixou de ser usado no topo. A área agora mostra diretamente
  "Buscando —" com a bandeira do hotel, e o bloco de pesquisa foi elevado.
- O campo de nick usa a fonte Ubuntu Habbo.
- O primeiro banner possui o mesmo espaçamento de 12 dp acima e abaixo; o texto
  de status vazio não reserva mais altura invisível entre o anúncio e o conteúdo.
- Os tutoriais calculam os limites globais visíveis dos componentes reais, em
  vez de coordenadas fixas, e se reajustam depois de rotação ou mudança de tela.

BACKEND

O arquivo backend/api.php está na versão 1.4.1 e deve substituir o api.php do
servidor. A resolução de HHID usa cookie somente na memória durante a requisição;
perfis e sessões continuam sem cache persistente.

COMPILAÇÃO

Use os workflows existentes na pasta .github/workflows ou o seu fluxo habitual
no GitHub. As credenciais de assinatura continuam sendo lidas pelos secrets já
descritos em PLAY_STORE_AAB.md.
