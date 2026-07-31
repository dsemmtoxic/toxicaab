BACKEND DO TOXIC

O aplicativo está configurado para usar:

https://atoxic.com.br/api.php

O arquivo backend/api.php desta versão é a API de compatibilidade da Toxic.
Dados atuais usam a API pública oficial do Habbo; históricos são usados apenas
como complemento. A API não mantém respostas em cache.

PUBLICAÇÃO

1. Envie backend/api.php para public_html/api.php no atoxic.com.br.
2. Use PHP 8.0 ou superior, com as extensões cURL e DOM habilitadas.
3. Permita que o PHP crie a pasta cache ao lado do arquivo. Ela guarda somente
   bloqueios de concorrência e o histórico de observações, nunca respostas de API.
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

O endpoint também mantém compatibilidade com o formato antigo por query string
e com o modo gateway usando os parâmetros path e query.
