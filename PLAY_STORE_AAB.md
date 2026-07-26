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
versionCode 35
versionName '1.2.3'
```

O `versionCode` precisa sempre aumentar. O `versionName` é o texto exibido para você/usuário.

3. Gere outro AAB pelo mesmo workflow **2 - Build Signed Android AAB**.
4. Use sempre a mesma chave/secrets.

## 6. Ative a renovação automática dos banners no AdMob

O aplicativo tenta carregar novamente um banner quando ocorre falta de preenchimento
ou erro de rede, começando após 30 segundos e aumentando gradualmente até 5 minutos.
Quando um anúncio está carregado, a troca periódica do criativo é controlada pelo
AdMob.

No painel do AdMob, abra cada uma das cinco unidades de banner usadas pelo aplicativo
e deixe a opção de atualização automática em **Otimizada pelo Google**. Essa é a
configuração recomendada para manter banners visíveis recebendo novos anúncios sem
criar solicitações duplicadas no código.
