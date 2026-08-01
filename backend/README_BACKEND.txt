BACKEND DO TOXIC — VERSÃO 1.3.5

O aplicativo está configurado para usar:

https://atoxic.com.br/api.php

O arquivo backend/api.php desta versão é a API de compatibilidade da Toxic.
Dados atuais usam a API pública oficial do Habbo; históricos são usados apenas
como complemento.

VISUAIS E RARIDADES

- O nome localizado de cada roupa continua vindo do HabboWidgets, de acordo com
  o hotel selecionado.
- O código da roupa é conferido no iframe do HabboNews para obter exatamente o
  ícone usado por ele e para identificar roupas padrão que devem ser ocultadas.
- A API não inventa nível nem usa cabide amarelo como fallback. Se a peça não
  existir no índice, ela permanece sem ícone.
- O índice técnico não armazena os nomes em português do iframe.

CACHE AUTOMÁTICO DO HABBONEWS

A atualização é automática e não precisa de cron. Na primeira consulta de
visuais após seis horas, a API tenta baixar novamente o iframe. Um bloqueio evita
atualizações simultâneas. Se o site estiver temporariamente indisponível, a última
cópia válida continua em uso; na primeira instalação há também uma cópia técnica
embutida como segurança.

Somente o pequeno mapa "tipo-ID -> ícone/roupa padrão" é persistido em:

cache/habbonews_rarity/index.json

Perfis, históricos, nomes e imagens não são armazenados nesse cache.

PUBLICAÇÃO

1. Envie backend/api.php para public_html/api.php no atoxic.com.br.
2. Use PHP 8.0 ou superior, com cURL, DOM e zlib habilitados.
3. Permita que o PHP crie e grave a pasta cache/habbonews_rarity ao lado de
   api.php.
4. Confirme que a hospedagem aceita PATH_INFO nas URLs de api.php.

TESTE

https://atoxic.com.br/api.php/habboinfo/br/habbo?name=Refresh

ROTAS PRINCIPAIS

- /api.php/habboinfo/br/habbo?name=NICK
- /api.php/habboinfo/UNIQUE_ID?hotel=br
- /api.php/habboinfo/UNIQUE_ID/photos?page=1&limit=100&hotel=br
- /api.php/habboinfo/UNIQUE_ID/previous-mottos?page=1&limit=100&hotel=br
- /api.php/habboinfo/UNIQUE_ID/selected-badges?page=1&limit=100&hotel=br
- /api.php/habboinfo/UNIQUE_ID/badges?page=1&limit=100&hotel=br
- /api.php/habboinfo/UNIQUE_ID/previous-badges?page=1&limit=100&hotel=br
- /api.php/habboinfo/UNIQUE_ID/previous-styles?page=1&limit=100&hotel=br
- /api.php/habboinfo/UNIQUE_ID/friends?page=1&limit=100&hotel=br
- /api.php/habboinfo/UNIQUE_ID/previous-friends?page=1&limit=100&hotel=br
- /api.php/habboinfo/UNIQUE_ID/rooms?page=1&limit=100&hotel=br
- /api.php/habboinfo/UNIQUE_ID/previous-rooms?page=1&limit=100&hotel=br
- /api.php/habboinfo/UNIQUE_ID/groups?page=1&limit=100&hotel=br
- /api.php/habboinfo/UNIQUE_ID/previous-groups?page=1&limit=100&hotel=br
- /api.php/furnidex/furni/from-figure-string?figureString=FIGURE&hotel=br

O endpoint mantém compatibilidade com o formato antigo por query string e com
o modo gateway usando os parâmetros path e query.
