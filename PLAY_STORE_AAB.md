# Gerar assinatura e AAB para Google Play pelo GitHub Actions

Este projeto foi preparado para gerar um Android App Bundle assinado (`.aab`) sem precisar abrir o Android Studio.

## 1. Envie estes arquivos para o GitHub

Envie esta versão do projeto para o seu repositório.

Arquivos importantes adicionados/alterados:

- `app/build.gradle`
- `build.gradle`
- `.github/workflows/generate-upload-key.yml`
- `.github/workflows/build-aab-release.yml`
- `.gitignore`

O projeto usa `compileSdk 36` e `targetSdk 36`, compatíveis com a exigência
da Play Store para atualizações enviadas a partir de 31 de agosto de 2026.
O build utiliza Android Gradle Plugin 8.10.1, Gradle 8.11.1 e JDK 17.

## 2. Gere sua chave de upload uma única vez

No GitHub:

1. Abra o repositório.
2. Vá em **Actions**.
3. Rode o workflow **1 - Generate Play Upload Key**.
4. Quando terminar, baixe o artefato **toxic-upload-key-and-github-secrets**.
5. Extraia o `.zip` baixado.
6. Guarde o arquivo `toxic-upload-key.jks` em local seguro.
7. Abra `github-secrets-to-add.txt`.

## 3. Crie os secrets no GitHub

No GitHub:

1. Vá em **Settings**.
2. **Secrets and variables**.
3. **Actions**.
4. **New repository secret**.
5. Crie estes 4 secrets exatamente com estes nomes:

```text
KEYSTORE_BASE64
KEYSTORE_PASSWORD
KEY_ALIAS
KEY_PASSWORD
```

Copie cada valor do arquivo `github-secrets-to-add.txt`.

## 4. Gere o AAB assinado

No GitHub:

1. Vá em **Actions**.
2. Rode o workflow **2 - Build Signed Android AAB**.
3. Baixe o artefato **ToxicSearchTool-release-aab**.
4. Extraia o `.zip`.
5. Envie o arquivo `.aab` para a Play Console.

O caminho interno gerado pelo Gradle é:

```text
app/build/outputs/bundle/release/app-release.aab
```

## 5. Atualizações futuras

Para atualizar o app depois na Play Store:

1. Mantenha o mesmo `applicationId`:

```gradle
applicationId 'com.toxic.search'
```

2. Aumente o `versionCode` em `app/build.gradle`:

```gradle
versionCode 90
versionName '1.4.6'
```

O `versionCode` precisa sempre aumentar. O `versionName` é o texto exibido para você/usuário.

3. Gere outro AAB pelo mesmo workflow **2 - Build Signed Android AAB**.
4. Use sempre a mesma chave/secrets.

## 6. AdMob

Esta versão usa o Google Mobile Ads SDK com o App ID AdMob configurado no
`AndroidManifest.xml`.

- Builds `debug` usam exclusivamente os IDs de teste oficiais do Google.
- Builds `release` usam os blocos de anúncios de produção.
- A tela inicial exibe um anúncio nativo abaixo de Patrocinadores.
- Permanecem os banners de Visuais, Amigos e do topo do guarda-roupa.
- O banner que ficava abaixo das cores no guarda-roupa foi removido.
- O intersticial e o anúncio premiado mantêm os mesmos pontos e regras de exibição
  do aplicativo.
- O intersticial continua respeitando o intervalo mínimo de 2 minutos.
- A lógica Premium/Supporter continua impedindo a exibição de anúncios quando válida.

Antes de publicar, confirme no painel da AdMob que o aplicativo e todos os blocos de
anúncios estão ativos. Também revise as configurações de privacidade/consentimento e
o `app-ads.txt` da aplicação.

## 7. Configure a compra Premium `remove_ads`

O identificador usado pelo aplicativo continua sendo exatamente:

```text
remove_ads
```

Na Play Console, verifique:

1. Abra **Monetizar > Produtos > Produtos únicos**.
2. Confirme que existe um produto com o ID exato `remove_ads`.
3. O produto deve ser do tipo **produto único**, não assinatura.
4. Crie e ative uma opção de compra com preço definido.
5. Confirme que ela está disponível no país da conta usada no teste.
6. Publique as alterações e aguarde a propagação do Google Play.
7. Teste com o aplicativo instalado por uma faixa da Play Store ou com uma conta
   cadastrada como testadora de licença. O pacote precisa continuar sendo
   `com.toxic.search`.

O aplicativo usa Google Play Billing 9.1.0 e registra no Logcat, com a etiqueta
`ToxicBilling`, o código retornado quando o produto não pode ser consultado.
