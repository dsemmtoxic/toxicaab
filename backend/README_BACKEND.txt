BACKEND DO TOXIC HABBO

O app Android nativo NÃO importa PHP/CSS para dentro do APK.
Ele usa o seu site como backend.

Arquivo obrigatório no site:
- /api.php

Esse arquivo é o proxy JSON que consulta o Habbodex e devolve respostas no formato:
{"ok":true,"data":...}

Endpoints usados pelo app:
- /api.php?name=NICK
- /api.php?endpoint=habbos-suggest&name=NICK&includePreviousNames=true&hotel=br
- /api.php?uniqueId=ID
- /api.php?uniqueId=ID&endpoint=previous-mottos&page=1&limit=100
- /api.php?uniqueId=ID&endpoint=previous-styles&page=1&limit=100
- /api.php?uniqueId=ID&endpoint=photos&page=1&limit=100
- /api.php?uniqueId=ID&endpoint=friends&page=1&limit=100
- /api.php?uniqueId=ID&endpoint=previous-friends&page=1&limit=100
- /api.php?uniqueId=ID&endpoint=rooms&page=1&limit=100
- /api.php?uniqueId=ID&endpoint=previous-rooms&page=1&limit=100
- /api.php?uniqueId=ID&endpoint=groups&page=1&limit=100
- /api.php?endpoint=from-figure-string&figureString=FIGURE

Se quiser mudar o domínio, edite MainActivity.java:
private static final String API = "https://atoxic.com.br/api.php";
