<?php
// Cópia pronta para publicação junto ao projeto Android.
declare(strict_types=1);

/*
 * Toxic Profile Compatibility API
 *
 * Envie este arquivo para https://atoxic.com.br/api.php.
 * No aplicativo, a base compatível com as rotas do HabboDex passa a ser:
 *
 *     https://atoxic.com.br/api.php
 *
 * O arquivo aceita:
 *   - /api.php/habboinfo/br/habbo?name=Refresh
 *   - /api.php/habboinfo/hhbr-.../friends?page=1&limit=100&hotel=br
 *   - /api.php/furnidex/furni/from-figure-string?figureString=...&hotel=br
 *   - /api.php?path=habboinfo%2F...&query=... (gateway legado)
 *   - o formato antigo por query string documentado no projeto.
 *
 * Requisitos: PHP 8.0+, cURL e DOM. O diretório deste arquivo precisa aceitar
 * a criação de cache/habbonews_rarity para o índice técnico de raridades.
 * Dados atuais vêm da API pública oficial; a fonte histórica é usada somente
 * como complemento. Datas históricas são datas de detecção. Nenhum nome de
 * roupa do HabboNews é usado: os nomes localizados continuam no HabboWidgets.
 */

const TOXIC_API_VERSION = '1.4.0';
const TOXIC_USER_AGENT = 'ToxicSearchTool/1.4.0 (+https://atoxic.com.br)';
const HABBOWIDGETS_BASE = 'https://www.habbowidgets.com';
const CACHE_ROOT = __DIR__ . '/cache/habbowidgets_api';
const HABBONEWS_IFRAME_URL = 'https://lite.habbonews.net/iframes/iframe-clothing2-temp.php?nick=&direcao=2&genero=3&tutorial=3';
const HABBONEWS_RARITY_CACHE_ROOT = __DIR__ . '/cache/habbonews_rarity';
const HABBONEWS_RARITY_CACHE_FILE = HABBONEWS_RARITY_CACHE_ROOT . '/index.json';
const HABBONEWS_RARITY_LOCK_FILE = HABBONEWS_RARITY_CACHE_ROOT . '/refresh.lock';
const HABBONEWS_RARITY_CACHE_TTL = 21_600;
const HABBONEWS_RARITY_MIN_ITEMS = 2_500;
const HABBONEWS_TRANSPARENT_ICON = 'OuxYRCz';
const HABBONEWS_RARITY_SEED_GZIP_BASE64 = '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';
// Perfis e históricos continuam stateless. Somente o pequeno índice técnico de
// raridades do HabboNews é persistido no cache separado definido acima.
const ENABLE_API_CACHE = false;
const PROFILE_CACHE_TTL = 600;
const HTTP_CACHE_TTL = 600;
const HTTP_STALE_TTL = 2_592_000;
const PROFILE_STALE_TTL = 7_776_000;
const CLOSET_CACHE_TTL = 2_592_000;
const MAX_HTML_BYTES = 24_000_000;
const MAX_HABBONEWS_HTML_BYTES = 2_000_000;
const MAX_JSON_BYTES = 8_000_000;
const MAX_HISTORY_ITEMS = 1_000;
const UPSTREAM_MIN_INTERVAL_MS = 300;

final class ApiProblem extends RuntimeException
{
    public int $httpStatus;
    public string $apiCode;
    public array $details;

    public function __construct(
        int $httpStatus,
        string $apiCode,
        string $message,
        array $details = []
    ) {
        parent::__construct($message);
        $this->httpStatus = $httpStatus;
        $this->apiCode = $apiCode;
        $this->details = $details;
    }
}

function main(): void
{
    sendCommonHeaders();

    if (($_SERVER['REQUEST_METHOD'] ?? 'GET') === 'OPTIONS') {
        http_response_code(204);
        exit;
    }

    if (($_SERVER['REQUEST_METHOD'] ?? 'GET') !== 'GET') {
        sendFailure(new ApiProblem(405, 'method_not_allowed', 'Use o método GET.'), false);
    }

    $gatewayMode = isset($_GET['path']);
    try {
        purgeLegacyStorage();
        [$path, $params, $gatewayMode] = parseIncomingRequest();
        [$payload, $cacheHit] = dispatch($path, $params);
        sendSuccess($payload, $gatewayMode, $cacheHit);
    } catch (ApiProblem $problem) {
        sendFailure($problem, $gatewayMode);
    } catch (Throwable $error) {
        sendFailure(
            new ApiProblem(
                500,
                'internal_error',
                'Falha interna ao montar a resposta.',
                ['detail' => safeErrorDetail($error)]
            ),
            $gatewayMode
        );
    }
}

function sendCommonHeaders(): void
{
    header('Access-Control-Allow-Origin: *');
    header('Access-Control-Allow-Methods: GET, OPTIONS');
    header('Access-Control-Allow-Headers: Accept, Content-Type, X-Toxic-App');
    header('Content-Type: application/json; charset=utf-8');
    header('X-Content-Type-Options: nosniff');
    header('X-Toxic-API-Version: ' . TOXIC_API_VERSION);
    header('Referrer-Policy: no-referrer');
    header('Cache-Control: no-store, no-cache, must-revalidate, max-age=0');
    header('Pragma: no-cache');
    header('Expires: 0');
}

function sendSuccess(mixed $payload, bool $gatewayMode, bool $cacheHit): void
{
    http_response_code(200);
    $payload = sanitizePublicPayload($payload);
    if ($gatewayMode) {
        $payload = [
            'ok' => true,
            'cached' => false,
            'source' => 'toxic',
            'data' => $payload,
        ];
    }
    echo encodeJson($payload);
    exit;
}

function sendFailure(ApiProblem $problem, bool $gatewayMode): void
{
    http_response_code($problem->httpStatus);
    $payload = sanitizePublicPayload([
        'error' => $problem->getMessage(),
        'code' => $problem->apiCode,
    ]);
    if ($problem->details !== []) {
        $payload += sanitizePublicPayload($problem->details);
    }
    if ($gatewayMode) {
        $payload = ['ok' => false] + $payload;
    }
    echo encodeJson($payload);
    exit;
}

function sanitizePublicPayload(mixed $value, string $key = ''): mixed
{
    if (is_array($value)) {
        $out = [];
        foreach ($value as $childKey => $childValue) {
            $normalizedKey = strtolower((string) $childKey);
            $publicKey = str_contains($normalizedKey, 'habbowidgets')
                ? 'sourceCounts'
                : $childKey;
            $out[$publicKey] = sanitizePublicPayload($childValue, (string) $publicKey);
        }
        return $out;
    }
    if (!is_string($value)) {
        return $value;
    }
    if (in_array(strtolower($key), ['sourceurl', 'closeturl'], true)) {
        return '';
    }
    $clean = preg_replace(
        '/\s*[-–|]\s*(?:Habbo\s+(?:Guarda[- ]Roupa|Closet).*|Habbo\s*Widgets(?:\.com)?.*)$/iu',
        '',
        $value
    ) ?? $value;
    $clean = preg_replace('/Habbo\s*Widgets(?:\.com)?/iu', 'Toxic', $clean) ?? $clean;
    $clean = str_ireplace(
        ['atoxic-Toxic', 'toxic-Toxic', 'Toxic-closet', 'Toxic-ticker'],
        ['toxic', 'toxic', 'toxic', 'toxic'],
        $clean
    );
    return trim($clean);
}

function encodeJson(mixed $value): string
{
    $json = json_encode(
        $value,
        JSON_UNESCAPED_UNICODE
        | JSON_UNESCAPED_SLASHES
        | JSON_INVALID_UTF8_SUBSTITUTE
    );
    if (!is_string($json)) {
        throw new ApiProblem(500, 'json_encode_failed', 'Não foi possível codificar a resposta.');
    }
    return $json;
}

function safeErrorDetail(Throwable $error): string
{
    $message = trim($error->getMessage());
    return mbSubstrSafe($message, 0, 240);
}

/**
 * Carrega o índice técnico usado somente para ligar "tipo-ID" ao ícone do
 * HabboNews e identificar peças padrão. A primeira consulta após seis horas
 * atualiza o cache; se a origem falhar, a última cópia válida permanece ativa.
 */
function habbonewsRarityIndex(): array
{
    static $memory = null;
    if (is_array($memory)) {
        return $memory;
    }

    $cached = readHabbonewsRarityCache();
    if (
        isValidHabbonewsRarityIndex($cached)
        && (time() - (int) ($cached['updatedAt'] ?? 0)) < HABBONEWS_RARITY_CACHE_TTL
    ) {
        $memory = habbonewsRarityIndexWithMeta($cached, true, false, 'disk-cache');
        return $memory;
    }

    $fallback = isValidHabbonewsRarityIndex($cached)
        ? $cached
        : embeddedHabbonewsRarityIndex();
    $lockHandle = null;
    $ownsLock = false;

    if (ensureHabbonewsRarityCacheDirectory()) {
        $lockHandle = @fopen(HABBONEWS_RARITY_LOCK_FILE, 'c+');
        if (is_resource($lockHandle)) {
            $ownsLock = @flock($lockHandle, LOCK_EX | LOCK_NB);
            if (!$ownsLock && isValidHabbonewsRarityIndex($fallback)) {
                @fclose($lockHandle);
                $memory = habbonewsRarityIndexWithMeta(
                    $fallback,
                    true,
                    true,
                    isValidHabbonewsRarityIndex($cached) ? 'disk-cache' : 'embedded-seed'
                );
                return $memory;
            }
        }
    }

    try {
        // Outro processo pode ter concluído a atualização enquanto este esperava.
        if ($ownsLock) {
            $newerCache = readHabbonewsRarityCache();
            if (
                isValidHabbonewsRarityIndex($newerCache)
                && (time() - (int) ($newerCache['updatedAt'] ?? 0))
                    < HABBONEWS_RARITY_CACHE_TTL
            ) {
                $memory = habbonewsRarityIndexWithMeta(
                    $newerCache,
                    true,
                    false,
                    'disk-cache'
                );
                return $memory;
            }
        }

        $response = cachedHttpRequest(
            'GET',
            HABBONEWS_IFRAME_URL,
            [],
            0,
            MAX_HABBONEWS_HTML_BYTES,
            'pt-BR,pt;q=0.9,en;q=0.7'
        );
        $fresh = parseHabbonewsRarityIndex((string) $response['body']);
        if (!isValidHabbonewsRarityIndex($fresh)) {
            throw new ApiProblem(
                502,
                'rarity_index_invalid',
                'O índice de raridades recebido é inválido.'
            );
        }
        writeHabbonewsRarityCache($fresh);
        $memory = habbonewsRarityIndexWithMeta($fresh, false, false, 'live-refresh');
        return $memory;
    } catch (Throwable $error) {
        if (isValidHabbonewsRarityIndex($fallback)) {
            $memory = habbonewsRarityIndexWithMeta(
                $fallback,
                isValidHabbonewsRarityIndex($cached),
                true,
                isValidHabbonewsRarityIndex($cached) ? 'disk-cache' : 'embedded-seed'
            );
            return $memory;
        }
        $memory = habbonewsRarityIndexWithMeta(
            ['v' => 1, 'updatedAt' => 0, 'items' => []],
            false,
            true,
            'unavailable'
        );
        return $memory;
    } finally {
        if (is_resource($lockHandle)) {
            if ($ownsLock) {
                @flock($lockHandle, LOCK_UN);
            }
            @fclose($lockHandle);
        }
    }
}

function readHabbonewsRarityCache(): ?array
{
    $decoded = readJsonFile(HABBONEWS_RARITY_CACHE_FILE);
    return isValidHabbonewsRarityIndex($decoded) ? $decoded : null;
}

function isValidHabbonewsRarityIndex(mixed $index): bool
{
    return is_array($index)
        && (int) ($index['v'] ?? 0) === 1
        && is_array($index['items'] ?? null)
        && count($index['items']) >= HABBONEWS_RARITY_MIN_ITEMS;
}

function embeddedHabbonewsRarityIndex(): ?array
{
    $compressed = base64_decode(HABBONEWS_RARITY_SEED_GZIP_BASE64, true);
    if (!is_string($compressed) || !function_exists('gzdecode')) {
        return null;
    }
    $json = @gzdecode($compressed);
    if (!is_string($json) || $json === '') {
        return null;
    }
    $decoded = json_decode($json, true);
    return isValidHabbonewsRarityIndex($decoded) ? $decoded : null;
}

function habbonewsRarityIndexWithMeta(
    array $index,
    bool $cacheHit,
    bool $stale,
    string $loadedFrom
): array {
    $updatedAt = (int) ($index['updatedAt'] ?? 0);
    $index['_meta'] = [
        'provider' => 'habbonews-iframe',
        'loadedFrom' => $loadedFrom,
        'cacheHit' => $cacheHit,
        'stale' => $stale,
        'updatedAt' => $updatedAt > 0 ? gmdate('c', $updatedAt) : '',
        'ageSeconds' => $updatedAt > 0 ? max(0, time() - $updatedAt) : null,
        'refreshIntervalSeconds' => HABBONEWS_RARITY_CACHE_TTL,
        'entries' => count($index['items'] ?? []),
    ];
    return $index;
}

function ensureHabbonewsRarityCacheDirectory(): bool
{
    if (is_dir(HABBONEWS_RARITY_CACHE_ROOT)) {
        return is_writable(HABBONEWS_RARITY_CACHE_ROOT);
    }
    return @mkdir(HABBONEWS_RARITY_CACHE_ROOT, 0775, true)
        && is_dir(HABBONEWS_RARITY_CACHE_ROOT);
}

function writeHabbonewsRarityCache(array $index): bool
{
    if (!isValidHabbonewsRarityIndex($index) || !ensureHabbonewsRarityCacheDirectory()) {
        return false;
    }
    $json = encodeJson($index);
    try {
        $suffix = bin2hex(random_bytes(6));
    } catch (Throwable $error) {
        $suffix = str_replace('.', '', uniqid('', true));
    }
    $temporary = HABBONEWS_RARITY_CACHE_FILE . '.tmp-' . getmypid() . '-' . $suffix;
    $written = @file_put_contents($temporary, $json, LOCK_EX);
    if (!is_int($written) || $written !== strlen($json)) {
        @unlink($temporary);
        return false;
    }
    @chmod($temporary, 0664);
    if (!@rename($temporary, HABBONEWS_RARITY_CACHE_FILE)) {
        @unlink($temporary);
        return false;
    }
    return true;
}

function parseHabbonewsRarityIndex(string $html): array
{
    $arraySource = extractJavascriptArrayAfter($html, 'var setsJSON');
    $json = quoteJavascriptNumericObjectKeys($arraySource);
    try {
        $groups = json_decode($json, true, 512, JSON_THROW_ON_ERROR);
    } catch (JsonException $error) {
        throw new ApiProblem(
            502,
            'rarity_index_parse_failed',
            'Não foi possível interpretar o índice de raridades.'
        );
    }
    if (!is_array($groups)) {
        throw new ApiProblem(502, 'rarity_index_missing', 'Índice de raridades ausente.');
    }

    $items = [];
    foreach ($groups as $group) {
        if (!is_array($group)) {
            continue;
        }
        $type = strtolower(trim((string) ($group['type'] ?? '')));
        $sets = $group['sets'] ?? null;
        if (preg_match('/^[a-z]{2}$/', $type) !== 1 || !is_array($sets)) {
            continue;
        }
        foreach ($sets as $id => $metadata) {
            $id = (string) $id;
            if ($id === '9999' || preg_match('/^\d+$/', $id) !== 1 || !is_array($metadata)) {
                continue;
            }
            $iconCode = trim((string) ($metadata['raridade'] ?? ''));
            if (
                $iconCode !== ''
                && preg_match('/^[A-Za-z0-9]{5,12}$/', $iconCode) !== 1
            ) {
                $iconCode = '';
            }
            $sourceName = trim(html_entity_decode(
                (string) ($metadata['mobi'] ?? ''),
                ENT_QUOTES | ENT_HTML5,
                'UTF-8'
            ));
            $hidden = $sourceName === '' && $iconCode === HABBONEWS_TRANSPARENT_ICON;
            $code = $type . '-' . $id;
            if ($hidden) {
                $items[$code] = ['h' => 1];
            } elseif ($iconCode !== '' && $iconCode !== HABBONEWS_TRANSPARENT_ICON) {
                $items[$code] = ['i' => $iconCode];
            } else {
                // Registro conhecido, mas sem ícone visível. O nome não é salvo.
                $items[$code] = ['i' => ''];
            }
        }
    }
    ksort($items, SORT_STRING);
    if (count($items) < HABBONEWS_RARITY_MIN_ITEMS) {
        throw new ApiProblem(
            502,
            'rarity_index_incomplete',
            'O índice de raridades recebido está incompleto.'
        );
    }
    return [
        'v' => 1,
        'updatedAt' => time(),
        'generatedAt' => gmdate('c'),
        'sourceHash' => hash('sha256', $arraySource),
        'items' => $items,
    ];
}

function extractJavascriptArrayAfter(string $source, string $marker): string
{
    $markerPosition = strpos($source, $marker);
    if ($markerPosition === false) {
        throw new ApiProblem(502, 'rarity_index_marker_missing', 'Marcador do índice ausente.');
    }
    $begin = strpos($source, '[', $markerPosition);
    if ($begin === false) {
        throw new ApiProblem(502, 'rarity_index_array_missing', 'Array do índice ausente.');
    }

    $depth = 0;
    $quote = '';
    $escaped = false;
    $length = strlen($source);
    for ($position = $begin; $position < $length; $position++) {
        $character = $source[$position];
        if ($quote !== '') {
            if ($escaped) {
                $escaped = false;
            } elseif ($character === '\\') {
                $escaped = true;
            } elseif ($character === $quote) {
                $quote = '';
            }
            continue;
        }
        if ($character === '"' || $character === "'") {
            $quote = $character;
            continue;
        }
        if ($character === '[') {
            $depth++;
        } elseif ($character === ']') {
            $depth--;
            if ($depth === 0) {
                return substr($source, $begin, $position - $begin + 1);
            }
        }
    }
    throw new ApiProblem(502, 'rarity_index_unclosed', 'Array do índice incompleto.');
}

function quoteJavascriptNumericObjectKeys(string $source): string
{
    $output = '';
    $quote = '';
    $escaped = false;
    $length = strlen($source);
    for ($position = 0; $position < $length; $position++) {
        $character = $source[$position];
        if ($quote !== '') {
            $output .= $character;
            if ($escaped) {
                $escaped = false;
            } elseif ($character === '\\') {
                $escaped = true;
            } elseif ($character === $quote) {
                $quote = '';
            }
            continue;
        }
        if ($character === '"' || $character === "'") {
            $quote = $character;
            $output .= $character;
            continue;
        }
        if (ctype_digit($character)) {
            $previous = $position - 1;
            while ($previous >= 0 && ctype_space($source[$previous])) {
                $previous--;
            }
            if ($previous >= 0 && ($source[$previous] === '{' || $source[$previous] === ',')) {
                $digitsEnd = $position;
                while ($digitsEnd < $length && ctype_digit($source[$digitsEnd])) {
                    $digitsEnd++;
                }
                $colon = $digitsEnd;
                while ($colon < $length && ctype_space($source[$colon])) {
                    $colon++;
                }
                if ($colon < $length && $source[$colon] === ':') {
                    $output .= '"' . substr($source, $position, $digitsEnd - $position) . '"';
                    $output .= substr($source, $digitsEnd, $colon - $digitsEnd + 1);
                    $position = $colon;
                    continue;
                }
            }
        }
        $output .= $character;
    }
    return $output;
}

function habbonewsClothingClassification(string $code): array
{
    $code = strtolower(trim($code));
    $index = habbonewsRarityIndex();
    $record = $index['items'][$code] ?? null;
    $known = is_array($record);
    $hidden = $known && !empty($record['h']);
    $iconCode = $known ? trim((string) ($record['i'] ?? '')) : '';
    if (
        $iconCode === HABBONEWS_TRANSPARENT_ICON
        || preg_match('/^[A-Za-z0-9]{5,12}$/', $iconCode) !== 1
    ) {
        $iconCode = '';
    }
    return [
        'known' => $known,
        'hidden' => $hidden,
        'iconCode' => $iconCode,
        'iconUrl' => habbonewsRarityIconUrl($iconCode),
        'source' => $known ? 'habbonews-iframe' : 'unclassified',
    ];
}

function habbonewsRarityIconUrl(string $iconCode): string
{
    if (
        $iconCode === ''
        || $iconCode === HABBONEWS_TRANSPARENT_ICON
        || preg_match('/^[A-Za-z0-9]{5,12}$/', $iconCode) !== 1
    ) {
        return '';
    }
    return 'https://i.imgur.com/' . rawurlencode($iconCode) . '.gif';
}

function habbonewsRarityPublicMeta(): array
{
    $index = habbonewsRarityIndex();
    $meta = is_array($index['_meta'] ?? null) ? $index['_meta'] : [];
    return [
        'provider' => 'habbonews-iframe',
        'cacheHit' => (bool) ($meta['cacheHit'] ?? false),
        'stale' => (bool) ($meta['stale'] ?? false),
        'updatedAt' => (string) ($meta['updatedAt'] ?? ''),
        'ageSeconds' => $meta['ageSeconds'] ?? null,
        'refreshIntervalSeconds' => HABBONEWS_RARITY_CACHE_TTL,
        'entries' => count($index['items'] ?? []),
    ];
}

function parseIncomingRequest(): array
{
    $params = $_GET;
    $gatewayMode = isset($params['path']);
    $path = '';

    if ($gatewayMode) {
        $path = trim((string) ($params['path'] ?? ''));
        $embedded = [];
        $rawQuery = trim((string) ($params['query'] ?? ''));
        if ($rawQuery !== '') {
            parse_str($rawQuery, $embedded);
        }
        unset($params['path'], $params['query']);
        $params = array_merge($embedded, $params);
    } else {
        $pathInfo = (string) ($_SERVER['PATH_INFO'] ?? '');
        if ($pathInfo !== '') {
            $path = $pathInfo;
        } else {
            $requestPath = (string) parse_url(
                (string) ($_SERVER['REQUEST_URI'] ?? ''),
                PHP_URL_PATH
            );
            $scriptName = (string) ($_SERVER['SCRIPT_NAME'] ?? '/api.php');
            if ($scriptName !== '' && str_starts_with($requestPath, $scriptName . '/')) {
                $path = substr($requestPath, strlen($scriptName) + 1);
            } elseif (preg_match('#/(?:api/v1|api\.php)/(.*)$#', $requestPath, $match)) {
                $path = $match[1];
            }
        }
    }

    $path = rawurldecode(trim($path, '/'));
    if (str_starts_with($path, 'api/v1/')) {
        $path = substr($path, strlen('api/v1/'));
    }
    if (
        $path !== ''
        && (
            str_contains($path, '..')
            || !preg_match('#^[A-Za-z0-9._/-]+$#', $path)
        )
    ) {
        throw new ApiProblem(400, 'invalid_path', 'Caminho de API inválido.');
    }

    $legacyQueryMode = !$gatewayMode
        && $path === ''
        && (
            isset($params['endpoint'])
            || isset($params['name'])
            || isset($params['uniqueId'])
            || isset($params['figureString'])
        );

    if ($path === '') {
        $path = legacyPathFromQuery($params);
    }

    // Os clientes antigos do site consultam api.php?endpoint=... e esperam o
    // envelope {ok,data}. As rotas modernas continuam devolvendo o JSON cru.
    return [$path, $params, $gatewayMode || $legacyQueryMode];
}

function legacyPathFromQuery(array $params): string
{
    $endpoint = trim((string) ($params['endpoint'] ?? ''));
    $name = trim((string) ($params['name'] ?? ''));
    $uniqueId = trim((string) ($params['uniqueId'] ?? ''));

    if ($endpoint === 'habbos-suggest') {
        return 'habboinfo/habbos';
    }
    if ($endpoint === 'from-figure-string' || isset($params['figureString'])) {
        return 'furnidex/furni/from-figure-string';
    }
    if ($name !== '') {
        return 'habboinfo/' . normalizeHotel((string) ($params['hotel'] ?? 'br')) . '/habbo';
    }
    if ($uniqueId !== '') {
        return 'habboinfo/' . $uniqueId
            . ($endpoint !== '' && $endpoint !== 'profile' ? '/' . $endpoint : '');
    }
    return '';
}

function dispatch(string $path, array $params): array
{
    if ($path === '') {
        return [[
            'name' => 'Toxic Profile Compatibility API',
            'version' => TOXIC_API_VERSION,
            'status' => 'ok',
            'provider' => 'toxic',
            'examples' => [
                '/api.php/habboinfo/br/habbo?name=Refresh',
                '/api.php/habboinfo/hhbr-...?hotel=br&complementOnly=true',
                '/api.php/habboinfo/hhbr-.../friends?hotel=br&page=1&limit=100',
                '/api.php/furnidex/furni/from-figure-string?figureString=hr-828-1346.hd-209-28&hotel=br',
            ],
        ], false];
    }

    if ($path === 'habboinfo/habbos') {
        $name = validateName((string) ($params['name'] ?? ''));
        $hotel = normalizeHotel((string) ($params['hotel'] ?? 'br'));
        [$items, $cacheHit] = suggestProfiles($name, $hotel);
        return [paginated($items, 'habbos', 1, 100), $cacheHit];
    }

    if ($path === 'furnidex/furni/from-figure-string') {
        $figure = validateFigure((string) ($params['figureString'] ?? ''));
        $hotel = normalizeHotel((string) ($params['hotel'] ?? 'br'));
        [$clothing, $cacheHit] = clothingFromFigure($figure, $hotel);
        return [$clothing, $cacheHit];
    }

    if (preg_match('#^habboinfo/([^/]+)/habbo$#i', $path, $match)) {
        $hotel = normalizeHotel($match[1]);
        $name = validateName((string) ($params['name'] ?? ''));
        $complementOnly = filter_var(
            $params['complementOnly'] ?? false,
            FILTER_VALIDATE_BOOLEAN
        );
        $loaded = $complementOnly
            ? loadComplementProfile($hotel, $name, '')
            : loadProfile($hotel, $name, '');
        return [publicProfilePayload($loaded['profile']), (bool) $loaded['cacheHit']];
    }

    if (preg_match('#^habboinfo/([^/]+)(?:/([^/]+))?$#i', $path, $match)) {
        $uniqueId = validateUniqueId($match[1]);
        $hotel = normalizeHotel(
            (string) ($params['hotel'] ?? inferHotelFromUniqueId($uniqueId))
        );
        $endpoint = strtolower(trim((string) ($match[2] ?? '')));
        $complementOnly = filter_var(
            $params['complementOnly'] ?? false,
            FILTER_VALIDATE_BOOLEAN
        );
        $loaded = $complementOnly
            ? loadComplementProfile($hotel, '', $uniqueId)
            : loadProfile($hotel, '', $uniqueId);
        $profile = $loaded['profile'];

        if ($endpoint === '') {
            return [publicProfilePayload($profile), (bool) $loaded['cacheHit']];
        }

        $page = max(1, (int) ($params['page'] ?? 1));
        $limit = min(250, max(1, (int) ($params['limit'] ?? 100)));
        $payload = profileSection($profile, $endpoint, $page, $limit, $params);
        return [$payload, (bool) $loaded['cacheHit']];
    }

    throw new ApiProblem(404, 'route_not_found', 'Rota não encontrada.');
}

function profileSection(
    array $profile,
    string $endpoint,
    int $page,
    int $limit,
    array $params
): array {
    $map = [
        'photos' => ['photos', 'photos'],
        'previous-names' => ['previousNames', 'previousNames'],
        'previous-mottos' => ['previousMottos', 'mottos'],
        'selected-badges' => ['selectedBadges', 'badges'],
        'previous-badges' => ['previousBadges', 'badges'],
        'previous-styles' => ['previousStyles', 'styles'],
        'friends' => ['friends', 'friends'],
        'previous-friends' => ['previousFriends', 'friends'],
        'rooms' => ['rooms', 'rooms'],
        'previous-rooms' => ['previousRooms', 'rooms'],
        'groups' => ['groups', 'groups'],
        'previous-groups' => ['previousGroups', 'groups'],
        'ticker' => ['ticker', 'ticker'],
        'clothing' => ['clothing', 'clothing'],
    ];

    if ($endpoint === 'badges') {
        $items = is_array($profile['badges'] ?? null) ? $profile['badges'] : [];
        $hideAchievements = filter_var(
            $params['hideAchievements'] ?? false,
            FILTER_VALIDATE_BOOLEAN
        );
        if ($hideAchievements) {
            $items = array_values(array_filter(
                $items,
                static fn(array $badge): bool => !($badge['isAchievement'] ?? false)
            ));
        }
        return paginated($items, 'badges', $page, $limit, $profile['_meta'] ?? []);
    }

    if (!isset($map[$endpoint])) {
        throw new ApiProblem(
            404,
            'section_not_found',
            'Seção de perfil não suportada.',
            ['endpoint' => $endpoint]
        );
    }

    [$profileKey, $responseKey] = $map[$endpoint];
    $items = is_array($profile[$profileKey] ?? null) ? $profile[$profileKey] : [];
    if ($profileKey === 'previousStyles') {
        $items = deduplicatePreviousStyles($items);
    }
    return paginated($items, $responseKey, $page, $limit, $profile['_meta'] ?? []);
}

function paginated(
    array $items,
    string $primaryKey,
    int $page,
    int $limit,
    array $sourceMeta = []
): array {
    $total = count($items);
    $totalPages = max(1, (int) ceil($total / max(1, $limit)));
    $page = min(max(1, $page), $totalPages);
    $offset = ($page - 1) * $limit;
    $slice = array_values(array_slice($items, $offset, $limit));
    $nextPage = $page < $totalPages ? $page + 1 : 0;

    return [
        $primaryKey => $slice,
        'result' => $slice,
        'items' => $slice,
        'total' => $total,
        'totalItems' => $total,
        'page' => $page,
        'limit' => $limit,
        'totalPages' => $totalPages,
        'pages' => $totalPages,
        'next' => $nextPage > 0 ? ['page' => $nextPage] : null,
        'pagination' => [
            'page' => $page,
            'limit' => $limit,
            'total' => $total,
            'totalItems' => $total,
            'totalPages' => $totalPages,
            'pages' => $totalPages,
            'nextPage' => $nextPage,
        ],
        'meta' => [
            'provider' => 'toxic',
            'datesAreDetectionDates' => true,
            'sourceUrl' => '',
        ],
    ];
}

function publicProfilePayload(array $profile): array
{
    return $profile;
}

function normalizeHotel(string $hotel): string
{
    $hotel = strtolower(trim($hotel));
    $hotel = str_replace(['www.habbo.', 'habbo.', '.'], ['', '', ''], $hotel);
    $aliases = [
        'us' => 'com',
        'com' => 'com',
        'br' => 'br',
        'combr' => 'br',
        'es' => 'es',
        'de' => 'de',
        'fr' => 'fr',
        'fi' => 'fi',
        'it' => 'it',
        'nl' => 'nl',
        'tr' => 'tr',
        'comtr' => 'tr',
    ];
    return $aliases[$hotel] ?? 'br';
}

function hotelConfig(string $hotel): array
{
    $hotel = normalizeHotel($hotel);
    $configs = [
        'br' => ['widget' => 'com.br', 'domain' => 'www.habbo.com.br', 'prefix' => 'hhbr', 'language' => 'pt-BR,pt;q=0.9,en;q=0.7'],
        'com' => ['widget' => 'com', 'domain' => 'www.habbo.com', 'prefix' => 'hhus', 'language' => 'en-US,en;q=0.9'],
        'es' => ['widget' => 'es', 'domain' => 'www.habbo.es', 'prefix' => 'hhes', 'language' => 'es-ES,es;q=0.9,en;q=0.7'],
        'de' => ['widget' => 'de', 'domain' => 'www.habbo.de', 'prefix' => 'hhde', 'language' => 'de-DE,de;q=0.9,en;q=0.7'],
        'fr' => ['widget' => 'fr', 'domain' => 'www.habbo.fr', 'prefix' => 'hhfr', 'language' => 'fr-FR,fr;q=0.9,en;q=0.7'],
        'fi' => ['widget' => 'fi', 'domain' => 'www.habbo.fi', 'prefix' => 'hhfi', 'language' => 'fi-FI,fi;q=0.9,en;q=0.7'],
        'it' => ['widget' => 'it', 'domain' => 'www.habbo.it', 'prefix' => 'hhit', 'language' => 'it-IT,it;q=0.9,en;q=0.7'],
        'nl' => ['widget' => 'nl', 'domain' => 'www.habbo.nl', 'prefix' => 'hhnl', 'language' => 'nl-NL,nl;q=0.9,en;q=0.7'],
        'tr' => ['widget' => 'com.tr', 'domain' => 'www.habbo.com.tr', 'prefix' => 'hhtr', 'language' => 'tr-TR,tr;q=0.9,en;q=0.7'],
    ];
    return ['key' => $hotel] + $configs[$hotel];
}

function inferHotelFromUniqueId(string $uniqueId): string
{
    $prefix = strtolower((string) strtok($uniqueId, '-'));
    $map = [
        'hhbr' => 'br',
        'hhus' => 'com',
        'hhes' => 'es',
        'hhde' => 'de',
        'hhfr' => 'fr',
        'hhfi' => 'fi',
        'hhit' => 'it',
        'hhnl' => 'nl',
        'hhtr' => 'tr',
    ];
    return $map[$prefix] ?? 'br';
}

function validateName(string $name): string
{
    $name = trim($name);
    $length = mbLengthSafe($name);
    if (
        $name === ''
        || $length > 48
        || preg_match('/[\x00-\x1F\x7F\/\\\\]/u', $name)
    ) {
        throw new ApiProblem(400, 'invalid_name', 'Informe um nome Habbo válido.');
    }
    return $name;
}

function validateUniqueId(string $uniqueId): string
{
    $uniqueId = strtolower(trim($uniqueId));
    if (!preg_match('/^hh[a-z]{2}-[a-z0-9]{20,64}$/i', $uniqueId)) {
        throw new ApiProblem(400, 'invalid_unique_id', 'Identificador Habbo inválido.');
    }
    return $uniqueId;
}

function validateFigure(string $figure): string
{
    $figure = trim($figure);
    if (
        $figure === ''
        || strlen($figure) > 2_048
        || !preg_match('/^[A-Za-z0-9._-]+$/', $figure)
    ) {
        throw new ApiProblem(400, 'invalid_figure', 'Figure string inválida.');
    }
    return $figure;
}

function mbLengthSafe(string $value): int
{
    return function_exists('mb_strlen') ? mb_strlen($value, 'UTF-8') : strlen($value);
}

function mbSubstrSafe(string $value, int $start, int $length): string
{
    return function_exists('mb_substr')
        ? mb_substr($value, $start, $length, 'UTF-8')
        : substr($value, $start, $length);
}

function ensureCacheDirectories(): void
{
    // Compatibilidade com instalações anteriores: não cria diretórios.
    purgeLegacyStorage();
}

function purgeLegacyStorage(): void
{
    if (ENABLE_API_CACHE) {
        return;
    }
    // Remove cache, histórico e locks deixados por versões anteriores.
    deleteCacheTree(CACHE_ROOT);
}

function deleteCacheTree(string $path): void
{
    if (!is_dir($path)) {
        return;
    }
    $items = @scandir($path);
    if (!is_array($items)) {
        return;
    }
    foreach ($items as $item) {
        if ($item === '.' || $item === '..') {
            continue;
        }
        $target = $path . '/' . $item;
        if (is_dir($target) && !is_link($target)) {
            deleteCacheTree($target);
        } else {
            @unlink($target);
        }
    }
    @rmdir($path);
}

function profileCacheFile(string $hotel, string $identifier): string
{
    $key = strtolower(normalizeHotel($hotel) . '|' . trim($identifier));
    return CACHE_ROOT . '/profiles/' . hash('sha256', $key) . '.json';
}

function readProfileCache(
    string $hotel,
    string $identifier,
    bool $freshOnly
): ?array {
    return null;
}

function writeProfileCache(string $hotel, string $identifier, array $profile): void
{
    // API stateless: mantida somente para compatibilidade interna.
}

function loadProfile(string $hotel, string $name, string $uniqueId): array
{
    $hotel = normalizeHotel($hotel);
    $profile = buildProfile($hotel, $name, $uniqueId);
    $profile['_meta']['cacheHit'] = false;
    return ['profile' => $profile, 'cacheHit' => false];
}

function loadComplementProfile(string $hotel, string $name, string $uniqueId): array
{
    $hotel = normalizeHotel($hotel);
    $result = fetchAndParseHabbowidgetsProfile(
        hotelConfig($hotel),
        $name,
        $uniqueId
    );
    if (($result['valid'] ?? false) !== true || !is_array($result['profile'] ?? null)) {
        throw new ApiProblem(404, 'profile_not_found', 'Perfil complementar não encontrado.');
    }

    $profile = $result['profile'];
    $profile['hotel'] = $hotel;
    $profile['privateProfile'] = !firstBool(
        $profile,
        ['profileVisible', 'isProfileVisible', 'visible'],
        true
    );
    $profile['_meta'] = [
        'provider' => 'toxic-complement',
        'apiVersion' => TOXIC_API_VERSION,
        'hotel' => $hotel,
        'sources' => ['toxic-history'],
        'sourceUrl' => '',
        'fetchedAt' => gmdate('c'),
        'datesAreDetectionDates' => true,
        'stale' => false,
        'warnings' => [],
    ];
    sortProfileLists($profile);
    return ['profile' => $profile, 'cacheHit' => false];
}

function buildProfile(string $hotel, string $requestedName, string $requestedId): array
{
    $config = hotelConfig($hotel);
    $warnings = [];
    $sources = [];
    $officialUser = null;
    $officialProfile = null;
    $officialPhotos = [];
    $resolvedName = $requestedName;
    $resolvedId = $requestedId;

    if ($requestedName !== '') {
        try {
            $officialUser = fetchOfficialUserByName($requestedName, $config);
            if (is_array($officialUser)) {
                $resolvedName = firstString($officialUser, ['name', 'username']) ?: $resolvedName;
                $resolvedId = firstString($officialUser, ['uniqueId', 'id']) ?: $resolvedId;
                $sources[] = 'habbo-public-user';
            }
        } catch (Throwable $error) {
            $warnings[] = 'A busca oficial por nome não respondeu; a fonte complementar foi usada como alternativa.';
        }
    }

    if ($resolvedId !== '') {
        try {
            $officialProfile = fetchOfficialProfile($resolvedId, $config);
            if (is_array($officialProfile)) {
                $sources[] = 'habbo-public-profile';
                $nestedUser = is_array($officialProfile['user'] ?? null)
                    ? $officialProfile['user']
                    : null;
                if ($nestedUser !== null) {
                    $officialUser = mergeRecord($officialUser ?? [], $nestedUser);
                    $resolvedName = firstString($officialUser, ['name', 'username']) ?: $resolvedName;
                }
            }
        } catch (Throwable $error) {
            $warnings[] = 'O perfil oficial completo não está público ou não respondeu.';
        }
    }

    $widgetResult = null;
    try {
        $widgetResult = fetchAndParseHabbowidgetsProfile(
            $config,
            $resolvedName !== '' ? $resolvedName : $requestedName,
            $resolvedId
        );
        if (($widgetResult['valid'] ?? false) === true) {
            $sources[] = 'toxic-history';
            $widgetData = $widgetResult['profile'];
            $resolvedName = firstString($widgetData, ['name']) ?: $resolvedName;
            $resolvedId = firstString($widgetData, ['uniqueId']) ?: $resolvedId;
        }
    } catch (Throwable $error) {
        $warnings[] = 'A fonte histórica não respondeu nesta atualização.';
    }

    if ($officialUser === null && $resolvedName !== '') {
        try {
            $officialUser = fetchOfficialUserByName($resolvedName, $config);
            if (is_array($officialUser)) {
                $resolvedId = firstString($officialUser, ['uniqueId', 'id']) ?: $resolvedId;
                $sources[] = 'habbo-public-user';
            }
        } catch (Throwable $ignored) {
        }
    }

    if ($officialProfile === null && $resolvedId !== '') {
        try {
            $officialProfile = fetchOfficialProfile($resolvedId, $config);
            if (is_array($officialProfile)) {
                $sources[] = 'habbo-public-profile';
                $nestedUser = is_array($officialProfile['user'] ?? null)
                    ? $officialProfile['user']
                    : null;
                if ($nestedUser !== null) {
                    $officialUser = mergeRecord($officialUser ?? [], $nestedUser);
                }
            }
        } catch (Throwable $ignored) {
        }
    }

    if ($resolvedId !== '') {
        try {
            $officialPhotos = fetchOfficialPhotos($resolvedId, $config);
            if ($officialPhotos !== []) {
                $sources[] = 'habbo-public-photos';
            }
        } catch (Throwable $ignored) {
        }
    }

    $widgetProfile = is_array($widgetResult['profile'] ?? null)
        ? $widgetResult['profile']
        : [];
    if (
        $widgetProfile === []
        && !is_array($officialUser)
        && !is_array($officialProfile)
    ) {
        throw new ApiProblem(404, 'profile_not_found', 'Perfil Habbo não encontrado.');
    }

    $profile = mergeCurrentAndHistoricalData(
        $hotel,
        $widgetProfile,
        $officialUser ?? [],
        $officialProfile ?? [],
        $officialPhotos
    );

    if (
        $requestedName !== ''
        && ($profile['name'] ?? '') !== ''
        && normalizeKey($requestedName) !== normalizeKey((string) $profile['name'])
    ) {
        $profile['previousNames'] = mergeLists(
            $profile['previousNames'] ?? [],
            [[
                'name' => $requestedName,
                'changedAt' => (string) ($profile['lastChangeAt'] ?? ''),
                'source' => 'toxic-history',
                'datePrecision' => 'observed',
            ]],
            ['name']
        );
    }

    $profile['_meta'] = [
        'provider' => 'toxic-profile-api',
        'apiVersion' => TOXIC_API_VERSION,
        'hotel' => $hotel,
        'sources' => array_values(array_unique($sources)),
        'sourceUrl' => '',
        'fetchedAt' => gmdate('c'),
        'datesAreDetectionDates' => true,
        'stale' => (bool) ($widgetResult['stale'] ?? false),
        'warnings' => array_values(array_unique($warnings)),
        'availability' => [
            'previousFriends' => 'fonte histórica disponível na consulta atual',
            'previousRooms' => 'bloco histórico de removidos disponível na consulta atual',
            'previousBadges' => 'bloco histórico de emblemas removidos',
            'previousGroups' => 'bloco histórico de grupos removidos',
            'previousNames' => 'fonte histórica disponível na consulta atual',
            'previousStyles' => 'fonte histórica',
            'previousMottos' => 'fonte histórica',
            'photos' => 'fonte histórica + API pública oficial do Habbo quando disponível',
        ],
    ];

    $reliability = is_array($widgetResult['reliability'] ?? null)
        ? $widgetResult['reliability']
        : [];
    $profile = updateObservedHistory($profile, $reliability);
    sortProfileLists($profile);

    return $profile;
}

function fetchOfficialUserByName(string $name, array $config): ?array
{
    $url = 'https://' . $config['domain']
        . '/api/public/users?name=' . rawurlencode($name);
    $result = fetchJsonUrl($url, 300, (string) $config['language']);
    return is_array($result['data']) ? $result['data'] : null;
}

function fetchOfficialProfile(string $uniqueId, array $config): ?array
{
    $url = 'https://' . $config['domain']
        . '/api/public/users/' . rawurlencode($uniqueId) . '/profile';
    $result = fetchJsonUrl($url, 300, (string) $config['language']);
    return is_array($result['data']) ? $result['data'] : null;
}

function fetchOfficialPhotos(string $uniqueId, array $config): array
{
    $url = 'https://' . $config['domain']
        . '/extradata/public/users/' . rawurlencode($uniqueId) . '/photos';
    $result = fetchJsonUrl($url, 300, (string) $config['language']);
    $data = $result['data'];
    if (!is_array($data)) {
        return [];
    }
    if (isListArray($data)) {
        return array_values(array_filter($data, 'is_array'));
    }
    foreach (['photos', 'items', 'result', 'data'] as $key) {
        if (is_array($data[$key] ?? null)) {
            return array_values(array_filter($data[$key], 'is_array'));
        }
    }
    return [];
}

function fetchJsonUrl(string $url, int $ttl, string $language): array
{
    $response = cachedHttpRequest('GET', $url, [], $ttl, MAX_JSON_BYTES, $language);
    $decoded = json_decode($response['body'], true);
    if (!is_array($decoded)) {
        throw new ApiProblem(
            502,
            'invalid_upstream_json',
            'A API oficial do Habbo devolveu uma resposta inválida.'
        );
    }
    return ['data' => $decoded, 'cacheHit' => $response['cacheHit']];
}

function fetchAndParseHabbowidgetsProfile(
    array $config,
    string $name,
    string $uniqueId
): array {
    $response = null;
    $lastError = null;

    if ($uniqueId !== '') {
        $url = HABBOWIDGETS_BASE . '/habinfo/' . rawurlencode($uniqueId);
        if ($name !== '') {
            $url .= '?name=' . rawurlencode($name);
        }
        try {
            $response = cachedHttpRequest(
                'GET',
                $url,
                [],
                HTTP_CACHE_TTL,
                MAX_HTML_BYTES,
                (string) $config['language']
            );
        } catch (Throwable $error) {
            $lastError = $error;
        }
    }

    if ($response === null && $name !== '') {
        try {
            // A página por nome cria uma sessão e o endpoint usado pelo próprio
            // HabboWidgets devolve o HHID histórico, inclusive para banidos que
            // já não existem na API pública oficial.
            $response = fetchHabbowidgetsProfileByNameSession($config, $name);
        } catch (Throwable $error) {
            $lastError = $error;
        }
    }

    if ($response === null && $name !== '') {
        try {
            $response = cachedHttpRequest(
                'POST',
                HABBOWIDGETS_BASE . '/habinfo/submit',
                ['habbo' => $name, 'hotel' => $config['widget']],
                HTTP_CACHE_TTL,
                MAX_HTML_BYTES,
                (string) $config['language']
            );
        } catch (Throwable $error) {
            $lastError = $error;
        }
    }

    if ($response === null) {
        if ($lastError instanceof Throwable) {
            throw $lastError;
        }
        throw new ApiProblem(502, 'history_source_unavailable', 'Fonte histórica indisponível.');
    }

    $parsed = parseHabbowidgetsHtml(
        $response['body'],
        (string) $response['effectiveUrl'],
        $config
    );
    $parsed['stale'] = (bool) $response['stale'];
    return $parsed;
}

function fetchHabbowidgetsProfileByNameSession(array $config, string $name): ?array
{
    if (!function_exists('curl_init')) {
        throw new ApiProblem(
            500,
            'curl_missing',
            'A extensão cURL não está habilitada no servidor.'
        );
    }

    $widgetHotel = (string) ($config['widget'] ?? 'com.br');
    $language = (string) ($config['language'] ?? 'en-US,en;q=0.9');
    $landingUrl = HABBOWIDGETS_BASE
        . '/habinfo/' . rawurlencode($widgetHotel)
        . '/' . rawurlencode($name);
    $extractUrl = HABBOWIDGETS_BASE . '/habinfo-extract';
    assertAllowedUpstreamUrl($landingUrl);
    assertAllowedUpstreamUrl($extractUrl);

    $ch = curl_init();
    if ($ch === false) {
        throw new ApiProblem(502, 'history_source_unavailable', 'Fonte histórica indisponível.');
    }

    try {
        $common = [
            CURLOPT_FOLLOWLOCATION => true,
            CURLOPT_MAXREDIRS => 5,
            CURLOPT_CONNECTTIMEOUT => 12,
            CURLOPT_TIMEOUT => 35,
            CURLOPT_ENCODING => '',
            CURLOPT_RETURNTRANSFER => true,
            CURLOPT_COOKIEFILE => '',
            CURLOPT_USERAGENT => TOXIC_USER_AGENT,
        ];
        if (defined('CURLOPT_PROTOCOLS') && defined('CURLPROTO_HTTPS')) {
            $common[CURLOPT_PROTOCOLS] = CURLPROTO_HTTPS;
        }
        if (defined('CURLOPT_REDIR_PROTOCOLS') && defined('CURLPROTO_HTTPS')) {
            $common[CURLOPT_REDIR_PROTOCOLS] = CURLPROTO_HTTPS;
        }
        curl_setopt_array($ch, $common);

        politeThrottle((string) parse_url($landingUrl, PHP_URL_HOST));
        curl_setopt_array($ch, [
            CURLOPT_URL => $landingUrl,
            CURLOPT_HTTPGET => true,
            CURLOPT_HTTPHEADER => [
                'Accept: text/html,application/xhtml+xml',
                'Accept-Language: ' . $language,
                'Referer: ' . HABBOWIDGETS_BASE . '/',
                'X-Toxic-App: ' . TOXIC_API_VERSION,
            ],
        ]);
        $landingBody = curl_exec($ch);
        $landingStatus = (int) curl_getinfo($ch, CURLINFO_RESPONSE_CODE);
        if (
            !is_string($landingBody)
            || $landingStatus < 200
            || $landingStatus >= 300
            || strlen($landingBody) > MAX_HTML_BYTES
        ) {
            return null;
        }

        politeThrottle((string) parse_url($extractUrl, PHP_URL_HOST));
        $form = http_build_query([
            'hotel' => $widgetHotel,
            'habbo' => $name,
            'hhid' => '',
            'type' => 'extract',
        ], '', '&', PHP_QUERY_RFC3986);
        curl_setopt_array($ch, [
            CURLOPT_URL => $extractUrl,
            CURLOPT_HTTPGET => false,
            CURLOPT_POST => true,
            CURLOPT_POSTFIELDS => $form,
            CURLOPT_HTTPHEADER => [
                'Accept: application/json, text/javascript, */*; q=0.01',
                'Accept-Language: ' . $language,
                'Content-Type: application/x-www-form-urlencoded; charset=UTF-8',
                'Origin: ' . HABBOWIDGETS_BASE,
                'Referer: ' . $landingUrl,
                'X-Requested-With: XMLHttpRequest',
                'X-Toxic-App: ' . TOXIC_API_VERSION,
            ],
        ]);
        $extractBody = curl_exec($ch);
        $extractStatus = (int) curl_getinfo($ch, CURLINFO_RESPONSE_CODE);
        if (!is_string($extractBody) || $extractStatus < 200 || $extractStatus >= 300) {
            return null;
        }
        $decoded = json_decode($extractBody, true);
        if (!is_array($decoded)) {
            return null;
        }
        $resolvedId = firstString($decoded, ['hhid', 'uniqueId', 'id']);
        if ($resolvedId === '') {
            return null;
        }
        try {
            $resolvedId = validateUniqueId($resolvedId);
        } catch (Throwable $ignored) {
            return null;
        }
    } finally {
        curl_close($ch);
    }

    return cachedHttpRequest(
        'GET',
        HABBOWIDGETS_BASE . '/habinfo/' . rawurlencode($resolvedId),
        [],
        HTTP_CACHE_TTL,
        MAX_HTML_BYTES,
        $language
    );
}

function cachedHttpRequest(
    string $method,
    string $url,
    array $form,
    int $ttl,
    int $maxBytes,
    string $language
): array {
    assertAllowedUpstreamUrl($url);
    $method = strtoupper($method);
    $upstreamHost = strtolower((string) parse_url($url, PHP_URL_HOST));
    $referer = $upstreamHost === 'lite.habbonews.net'
        ? 'https://www.habbonews.net/visuais'
        : HABBOWIDGETS_BASE . '/';
    if (!function_exists('curl_init')) {
        throw new ApiProblem(
            500,
            'curl_missing',
            'A extensão cURL não está habilitada no servidor.'
        );
    }

    politeThrottle((string) parse_url($url, PHP_URL_HOST));
    $body = '';
    $tooLarge = false;
    $ch = curl_init($url);
    $options = [
        CURLOPT_FOLLOWLOCATION => true,
        CURLOPT_MAXREDIRS => 5,
        CURLOPT_CONNECTTIMEOUT => 12,
        CURLOPT_TIMEOUT => 35,
        CURLOPT_ENCODING => '',
        CURLOPT_USERAGENT => TOXIC_USER_AGENT,
        CURLOPT_HTTPHEADER => [
            'Accept: text/html,application/json;q=0.9,*/*;q=0.7',
            'Accept-Language: ' . $language,
            'Referer: ' . $referer,
            'X-Toxic-App: ' . TOXIC_API_VERSION,
        ],
        CURLOPT_WRITEFUNCTION => static function ($curl, string $chunk) use (
            &$body,
            &$tooLarge,
            $maxBytes
        ): int {
            if (strlen($body) + strlen($chunk) > $maxBytes) {
                $tooLarge = true;
                return 0;
            }
            $body .= $chunk;
            return strlen($chunk);
        },
    ];
    if (defined('CURLOPT_PROTOCOLS') && defined('CURLPROTO_HTTPS')) {
        $options[CURLOPT_PROTOCOLS] = CURLPROTO_HTTPS;
    }
    if (defined('CURLOPT_REDIR_PROTOCOLS') && defined('CURLPROTO_HTTPS')) {
        $options[CURLOPT_REDIR_PROTOCOLS] = CURLPROTO_HTTPS;
    }
    if ($method === 'POST') {
        $options[CURLOPT_POST] = true;
        $options[CURLOPT_POSTFIELDS] = http_build_query($form, '', '&', PHP_QUERY_RFC3986);
        $options[CURLOPT_HTTPHEADER][] = 'Content-Type: application/x-www-form-urlencoded';
    }
    curl_setopt_array($ch, $options);
    $ok = curl_exec($ch);
    $status = (int) curl_getinfo($ch, CURLINFO_RESPONSE_CODE);
    $effectiveUrl = (string) curl_getinfo($ch, CURLINFO_EFFECTIVE_URL);
    $curlError = curl_error($ch);
    curl_close($ch);

    if ($tooLarge) {
        throw new ApiProblem(
            502,
            'upstream_too_large',
            'A resposta externa excedeu o limite seguro.'
        );
    }
    if ($ok === false || $status < 200 || $status >= 300 || $body === '') {
        throw new ApiProblem(
            in_array($status, [408, 425, 429], true) || $status >= 500 ? 503 : 502,
            'upstream_failed',
            'O serviço externo recusou ou não concluiu a consulta.',
            [
                'upstreamStatus' => $status,
                'detail' => mbSubstrSafe($curlError ?: strip_tags($body), 0, 180),
            ]
        );
    }

    return [
        'body' => $body,
        'status' => $status,
        'effectiveUrl' => $effectiveUrl !== '' ? $effectiveUrl : $url,
        'cacheHit' => false,
        'stale' => false,
    ];
}

function assertAllowedUpstreamUrl(string $url): void
{
    $host = strtolower((string) parse_url($url, PHP_URL_HOST));
    $allowed = [
        'www.habbowidgets.com',
        'habbowidgets.com',
        'www.habbo.com.br',
        'www.habbo.com',
        'www.habbo.es',
        'www.habbo.de',
        'www.habbo.fr',
        'www.habbo.fi',
        'www.habbo.it',
        'www.habbo.nl',
        'www.habbo.com.tr',
        'lite.habbonews.net',
    ];
    if (!in_array($host, $allowed, true)) {
        throw new ApiProblem(400, 'upstream_not_allowed', 'Destino externo não permitido.');
    }
}

function readHttpCache(string $bodyFile, string $metaFile): ?array
{
    return null;
}

function politeThrottle(string $host): void
{
    // Limite somente na memória da requisição atual; não cria lock em disco.
    static $lastRequestByHost = [];
    $key = strtolower($host);
    $now = microtime(true);
    $last = (float) ($lastRequestByHost[$key] ?? 0.0);
    $wait = (UPSTREAM_MIN_INTERVAL_MS / 1000) - ($now - $last);
    if ($wait > 0) {
        usleep((int) ceil($wait * 1_000_000));
    }
    $lastRequestByHost[$key] = microtime(true);
}

function parseHabbowidgetsHtml(
    string $html,
    string $sourceUrl,
    array $config
): array {
    if (!class_exists('DOMDocument') || !class_exists('DOMXPath')) {
        throw new ApiProblem(
            500,
            'dom_missing',
            'A extensão DOM do PHP não está habilitada no servidor.'
        );
    }

    $document = new DOMDocument('1.0', 'UTF-8');
    $previous = libxml_use_internal_errors(true);
    $loaded = $document->loadHTML(
        '<?xml encoding="UTF-8">' . $html,
        LIBXML_NONET | LIBXML_NOERROR | LIBXML_NOWARNING
    );
    libxml_clear_errors();
    libxml_use_internal_errors($previous);
    if (!$loaded) {
        throw new ApiProblem(502, 'invalid_history_html', 'Não foi possível ler a resposta da fonte histórica.');
    }

    $xpath = new DOMXPath($document);
    $summary = firstXpathNode($xpath, '//*[@id="habinfo-summary-habbo"]');
    if (!$summary instanceof DOMNode) {
        $pageText = normalizeText((string) ($document->textContent ?? ''));
        if (preg_match('/not found|não encontrado|niet gevonden|nicht gefunden|introuvable/i', $pageText)) {
            throw new ApiProblem(404, 'profile_not_found', 'Perfil não encontrado na fonte histórica.');
        }
        throw new ApiProblem(
            502,
            'unexpected_history_page',
            'A fonte histórica devolveu uma página sem os dados esperados.'
        );
    }

    $uniqueId = '';
    $path = (string) parse_url($sourceUrl, PHP_URL_PATH);
    if (preg_match('#/habinfo/(hh[a-z]{2}-[a-z0-9]{20,64})#i', $path, $match)) {
        $uniqueId = strtolower($match[1]);
    }
    $name = nodeText(firstXpathNode(
        $xpath,
        './/h1[contains(concat(" ", normalize-space(@class), " "), " habinfo-summary-heading ")]',
        $summary
    ));
    $avatar = firstXpathNode($xpath, './/img[@id="closet-url"]', $summary);
    $avatarUrl = imageUrlFromNode($avatar);
    $figure = extractFigureFromImageUrl($avatarUrl);
    $motto = extractMotto($xpath, $summary);
    $summaryText = nodeText($summary);
    $pageText = normalizeText((string) ($document->textContent ?? ''));
    $privateProfile = (bool) preg_match(
        '/perfil fechado|private profile|closed profile|privates profil|profil fermé|perfil cerrado|profiel gesloten|profilo chiuso|gizli profil|yksityinen profiili/i',
        $summaryText . ' ' . $pageText
    );
    if (!$privateProfile) {
        $privateProfile = firstXpathNode(
            $xpath,
            './/a[contains(concat(" ", normalize-space(@class), " "), " btn-warning ")][.//*[contains(concat(" ", normalize-space(@class), " "), " glyphicon-lock ")]]',
            $summary
        ) instanceof DOMNode;
    }
    $bannedMarker = firstXpathNode($xpath, '//*[@id="extract-banned"]') instanceof DOMNode
        || (bool) preg_match('/\bthis habbo is banned\b/i', $pageText);
    $bannedButton = firstXpathNode(
        $xpath,
        './/a[contains(concat(" ", normalize-space(@class), " "), " btn-danger ")][.//*[contains(concat(" ", normalize-space(@class), " "), " glyphicon-remove ")]]',
        $summary
    ) instanceof DOMNode;
    // O HabboWidgets reutiliza extract-banned dentro do aviso de perfil
    // fechado. O estado privado/cadeado é, portanto, mais específico.
    $banned = !$privateProfile && ($bannedButton || $bannedMarker);
    $lastChangeAt = nodeAttribute(
        firstXpathNode($xpath, './/time[@datetime][1]', $summary),
        'datetime'
    );

    $previousMottos = parsePreviousMottos($xpath, $summary);
    $visibleStyles = parsePreviousStyles($xpath, $summary);
    $ticker = parseTicker($xpath);
    $tickerStyles = [];
    foreach ($ticker as $event) {
        if (($event['type'] ?? '') === 'look' && ($event['figureString'] ?? '') !== '') {
            $tickerStyles[] = [
                'figureString' => $event['figureString'],
                'figure' => $event['figureString'],
                'look' => $event['figureString'],
                'changedAt' => (string) ($event['date'] ?? ''),
                'imageUrl' => (string) ($event['imageUrl'] ?? ''),
                'source' => 'toxic-history',
                'datePrecision' => 'observed',
            ];
        }
    }
    $previousStyles = mergeListsCompound(
        $tickerStyles,
        $visibleStyles,
        ['figureString', 'changedAt']
    );
    $previousStyles = array_values(array_filter(
        $previousStyles,
        static fn(array $item): bool =>
            ($item['figureString'] ?? '') !== ''
            && ($item['figureString'] ?? '') !== $figure
    ));

    $selectedBadges = [];
    foreach (xpathNodes(
        $xpath,
        '//*[@id="habinfo-summary-badges"]//*[contains(concat(" ", normalize-space(@class), " "), " media ")]'
    ) as $node) {
        $badge = parseBadgeNode($xpath, $node, true);
        if (($badge['code'] ?? '') !== '') {
            $selectedBadges[] = $badge;
        }
    }

    $badges = [];
    foreach (xpathNodes(
        $xpath,
        '//*[@id="active-badges"]/*[contains(concat(" ", normalize-space(@class), " "), " badge-holder ")]'
    ) as $node) {
        $badge = parseBadgeNode($xpath, $node, false);
        if (($badge['code'] ?? '') !== '') {
            $badges[] = $badge;
        }
    }
    if ($badges === []) {
        foreach (xpathNodes(
            $xpath,
            '//*[@id="habbo-badges-block"]//*[contains(concat(" ", normalize-space(@class), " "), " badge-holder ")][not(ancestor::*[@id="removed-badges"])]'
        ) as $node) {
            $badge = parseBadgeNode($xpath, $node, false);
            if (($badge['code'] ?? '') !== '') {
                $badges[] = $badge;
            }
        }
    }
    $previousBadges = [];
    foreach (xpathNodes(
        $xpath,
        '//*[@id="removed-badges"]/*[contains(concat(" ", normalize-space(@class), " "), " badge-holder ")]/*[contains(concat(" ", normalize-space(@class), " "), " badge-holder ")]'
    ) as $node) {
        $badge = parseBadgeNode($xpath, $node, false);
        if (($badge['code'] ?? '') !== '') {
            $badge['removed'] = true;
            $previousBadges[] = $badge;
        }
    }
    $badges = mergeLists($badges, $selectedBadges, ['code']);
    $selectedBadges = enrichSelectedBadges($selectedBadges, $badges);

    $friends = [];
    foreach (xpathNodes(
        $xpath,
        '//*[@id="habbo-friends-block"]//*[contains(concat(" ", normalize-space(@class), " "), " friend-holder ")][not(ancestor::*[@id="removed-friends"])]'
    ) as $node) {
        $friend = parseFriendNode($xpath, $node, false);
        if (($friend['name'] ?? '') !== '') {
            $friends[] = $friend;
        }
    }

    $previousFriends = [];
    foreach (xpathNodes(
        $xpath,
        '//*[@id="removed-friends"]//*[contains(concat(" ", normalize-space(@class), " "), " friend-holder ")]'
    ) as $node) {
        $friend = parseFriendNode($xpath, $node, true);
        if (($friend['name'] ?? '') !== '') {
            $previousFriends[] = $friend;
        }
    }

    $rooms = [];
    foreach (xpathNodes(
        $xpath,
        '//*[@id="habbo-rooms-block"]//*[contains(concat(" ", normalize-space(@class), " "), " room-holder ")][not(ancestor::*[@id="removed-rooms"])]'
    ) as $node) {
        $room = parseRoomNode($xpath, $node);
        if (($room['name'] ?? '') !== '') {
            $rooms[] = $room;
        }
    }
    $directPreviousRooms = [];
    foreach (xpathNodes(
        $xpath,
        '//*[@id="removed-rooms"]//*[contains(concat(" ", normalize-space(@class), " "), " room-holder ")]'
    ) as $node) {
        $room = parseRoomNode($xpath, $node);
        if (($room['name'] ?? '') !== '') {
            $room['removed'] = true;
            $directPreviousRooms[] = $room;
        }
    }

    $groups = [];
    foreach (xpathNodes(
        $xpath,
        '//*[@id="habbo-groups-block"]//*[contains(concat(" ", normalize-space(@class), " "), " group-holder ")][not(ancestor::*[@id="removed-groups"])]'
    ) as $node) {
        $group = parseGroupNode($xpath, $node);
        if (($group['name'] ?? '') !== '' || ($group['badgeUrl'] ?? '') !== '') {
            $groups[] = $group;
        }
    }
    $previousGroups = [];
    foreach (xpathNodes(
        $xpath,
        '//*[@id="removed-groups"]//*[contains(concat(" ", normalize-space(@class), " "), " group-holder ")]'
    ) as $node) {
        $group = parseGroupNode($xpath, $node);
        if (($group['name'] ?? '') !== '' || ($group['badgeUrl'] ?? '') !== '') {
            $group['removed'] = true;
            $previousGroups[] = $group;
        }
    }

    $photos = parseHabbowidgetsPhotos($xpath);
    $tickerPreviousRooms = [];
    $previousNames = [];
    foreach ($ticker as $event) {
        if (($event['type'] ?? '') === 'room_removed') {
            $tickerPreviousRooms[] = [
                'id' => (string) ($event['roomId'] ?? ''),
                'roomId' => (string) ($event['roomId'] ?? ''),
                'name' => (string) ($event['title'] ?? 'Quarto anterior'),
                'roomName' => (string) ($event['title'] ?? 'Quarto anterior'),
                'description' => (string) ($event['message'] ?? ''),
                'removedAt' => (string) ($event['date'] ?? ''),
                'date' => (string) ($event['date'] ?? ''),
                'url' => (string) ($event['url'] ?? ''),
                'thumbnailUrl' => (string) ($event['imageUrl'] ?? ''),
                'source' => 'toxic-history',
                'datePrecision' => 'observed',
            ];
        }
        if (($event['type'] ?? '') === 'name' && ($event['previousName'] ?? '') !== '') {
            $previousNames[] = [
                'name' => (string) $event['previousName'],
                'changedAt' => (string) ($event['date'] ?? ''),
                'source' => 'toxic-history',
                'datePrecision' => 'observed',
            ];
        }
    }
    $previousRooms = mergeHistoricalLists(
        'previousRooms',
        $directPreviousRooms,
        $tickerPreviousRooms
    );

    $clothing = parseClosetRows($xpath, $config);
    foreach ($clothing as $item) {
        saveClosetMetadata($config['key'], $item);
    }

    $countNodes = [
        'badges' => firstXpathNode($xpath, '//*[@id="habinfo-badge-count"]'),
        'friends' => firstXpathNode($xpath, '//*[@id="habinfo-friend-count"]'),
        'groups' => firstXpathNode($xpath, '//*[@id="habinfo-group-count"]'),
        'rooms' => firstXpathNode($xpath, '//*[@id="habinfo-room-count"]'),
        'photos' => firstXpathNode($xpath, '//*[@id="habinfo-photo-count"]'),
    ];
    $counts = [
        'badges' => integerFromNode($countNodes['badges']),
        'friends' => integerFromNode($countNodes['friends']),
        'groups' => integerFromNode($countNodes['groups']),
        'rooms' => integerFromNode($countNodes['rooms']),
        'photos' => integerFromNode($countNodes['photos']),
        'ticker' => count($ticker),
    ];
    if ($counts['badges'] <= 0) {
        $counts['badges'] = count($badges);
    }
    if ($counts['photos'] <= 0 && $photos !== []) {
        $counts['photos'] = count($photos);
    }
    $levelValues = integersFromNode(firstXpathNode(
        $xpath,
        '//*[@id="counter-level-xp" or @id="counter-level"]'
    ));
    $starValues = integersFromNode(firstXpathNode($xpath, '//*[@id="counter-star-gem"]'));

    $profile = [
        'uniqueId' => $uniqueId,
        'id' => $uniqueId,
        'habboId' => $uniqueId,
        'name' => $name,
        'username' => $name,
        'habboName' => $name,
        'figureString' => $figure,
        'figure' => $figure,
        'figure_string' => $figure,
        'avatarUrl' => $avatarUrl,
        'motto' => $motto,
        'mission' => $motto,
        'isBanned' => $banned,
        'banned' => $banned,
        'profileVisible' => !$privateProfile,
        'isProfileVisible' => !$privateProfile,
        'visible' => !$privateProfile,
        'lastChangeAt' => normalizeDate($lastChangeAt),
        'currentLevel' => (string) ($levelValues[0] ?? ''),
        'level' => (string) ($levelValues[0] ?? ''),
        'experience' => (string) ($levelValues[1] ?? ''),
        'xp' => (string) ($levelValues[1] ?? ''),
        'starGemCount' => (string) ($starValues[0] ?? ''),
        'starGems' => (string) ($starValues[0] ?? ''),
        'totalBadges' => (string) $counts['badges'],
        'badgeCount' => $counts['badges'],
        'friendCount' => $counts['friends'],
        'groupCount' => $counts['groups'],
        'roomCount' => $counts['rooms'],
        'photoCount' => $counts['photos'],
        'selectedBadges' => $selectedBadges,
        'badges' => $badges,
        'previousBadges' => $previousBadges,
        'previousNames' => $previousNames,
        'previousMottos' => $previousMottos,
        'previousStyles' => $previousStyles,
        'friends' => mergeLists($friends, [], ['uniqueId', 'name']),
        'previousFriends' => mergeLists($previousFriends, [], ['uniqueId', 'name']),
        'rooms' => mergeLists($rooms, [], ['id', 'name']),
        'previousRooms' => array_values($previousRooms),
        'groups' => array_values($groups),
        'previousGroups' => array_values($previousGroups),
        'photos' => $photos,
        'ticker' => $ticker,
        'clothing' => array_values($clothing),
        'sourceCounts' => $counts,
    ];

    return [
        'valid' => $name !== '' || $uniqueId !== '',
        'profile' => $profile,
        'sourceUrl' => $sourceUrl,
        'reliability' => [
            'friends' => listMatchesCount($friends, $counts['friends'], $countNodes['friends'] instanceof DOMNode),
            'rooms' => listMatchesCount($rooms, $counts['rooms'], $countNodes['rooms'] instanceof DOMNode),
            'groups' => listMatchesCount($groups, $counts['groups'], $countNodes['groups'] instanceof DOMNode),
            'badges' => listMatchesCount($badges, $counts['badges'], $countNodes['badges'] instanceof DOMNode),
            'photos' => listMatchesCount($photos, $counts['photos'], $countNodes['photos'] instanceof DOMNode),
        ],
    ];
}

function xpathNodes(
    DOMXPath $xpath,
    string $query,
    ?DOMNode $context = null
): array {
    $list = $xpath->query($query, $context);
    if (!$list instanceof DOMNodeList) {
        return [];
    }
    $nodes = [];
    foreach ($list as $node) {
        $nodes[] = $node;
    }
    return $nodes;
}

function firstXpathNode(
    DOMXPath $xpath,
    string $query,
    ?DOMNode $context = null
): ?DOMNode {
    $nodes = xpathNodes($xpath, $query, $context);
    return $nodes[0] ?? null;
}

function nodeAttribute(?DOMNode $node, string $name): string
{
    if (!$node instanceof DOMElement || !$node->hasAttribute($name)) {
        return '';
    }
    return trim(html_entity_decode(
        $node->getAttribute($name),
        ENT_QUOTES | ENT_HTML5,
        'UTF-8'
    ));
}

function nodeText(?DOMNode $node): string
{
    return $node instanceof DOMNode ? normalizeText((string) $node->textContent) : '';
}

function normalizeText(string $text): string
{
    $text = html_entity_decode($text, ENT_QUOTES | ENT_HTML5, 'UTF-8');
    $text = str_replace(["\xC2\xA0", "\r", "\n", "\t"], [' ', ' ', ' ', ' '], $text);
    return trim((string) preg_replace('/\s+/u', ' ', $text));
}

function imageUrlFromNode(?DOMNode $node): string
{
    if (!$node instanceof DOMElement) {
        return '';
    }
    $url = nodeAttribute($node, 'data-original');
    if ($url === '') {
        $url = nodeAttribute($node, 'src');
    }
    return absoluteHabbowidgetsUrl($url);
}

function absoluteHabbowidgetsUrl(string $url): string
{
    $url = trim($url);
    if ($url === '') {
        return '';
    }
    if (str_starts_with($url, '//')) {
        return 'https:' . $url;
    }
    if (str_starts_with($url, '/')) {
        return HABBOWIDGETS_BASE . $url;
    }
    return $url;
}

function extractFigureFromImageUrl(string $url): string
{
    if ($url === '') {
        return '';
    }
    $query = (string) parse_url(html_entity_decode($url, ENT_QUOTES | ENT_HTML5), PHP_URL_QUERY);
    $params = [];
    parse_str($query, $params);
    $figure = trim((string) ($params['figure'] ?? ''));
    return preg_match('/^[A-Za-z0-9._-]+$/', $figure) ? $figure : '';
}

function extractMotto(DOMXPath $xpath, DOMNode $summary): string
{
    $paragraph = firstXpathNode(
        $xpath,
        './/*[contains(concat(" ", normalize-space(@class), " "), " summary ")]/p[1]',
        $summary
    );
    if (!$paragraph instanceof DOMNode) {
        return '';
    }
    $parts = [];
    foreach ($paragraph->childNodes as $child) {
        if ($child instanceof DOMElement && strtolower($child->tagName) === 'br') {
            break;
        }
        if ($child instanceof DOMElement && strtolower($child->tagName) === 'a') {
            continue;
        }
        $parts[] = (string) $child->textContent;
    }
    return normalizeText(implode(' ', $parts));
}

function parsePreviousMottos(DOMXPath $xpath, DOMNode $summary): array
{
    $items = [];
    foreach (xpathNodes($xpath, './/*[@data-content]', $summary) as $node) {
        $title = strtolower(
            nodeAttribute($node, 'data-original-title')
            . ' '
            . nodeAttribute($node, 'title')
        );
        $content = nodeAttribute($node, 'data-content');
        if (
            !preg_match('/miss|motto|mission|devise|lema/i', $title)
            || $content === ''
        ) {
            continue;
        }
        if (!preg_match_all('#<p[^>]*>(.*?)</p>#si', $content, $matches)) {
            continue;
        }
        foreach ($matches[1] as $raw) {
            $line = normalizeText(strip_tags((string) $raw));
            if (!preg_match('/^(.+?)\s+[—–]\s+(.+)$/u', $line, $parts)) {
                continue;
            }
            $items[] = [
                'text' => trim($parts[2]),
                'motto' => trim($parts[2]),
                'changedAt' => normalizeDate(trim($parts[1])),
                'date' => normalizeDate(trim($parts[1])),
                'source' => 'toxic-history',
                'datePrecision' => 'observed',
            ];
        }
    }
    return array_values($items);
}

function parsePreviousStyles(DOMXPath $xpath, DOMNode $summary): array
{
    $items = [];
    foreach (xpathNodes(
        $xpath,
        './/*[contains(concat(" ", normalize-space(@class), " "), " habinfo-previous-looks ")]//img',
        $summary
    ) as $node) {
        $url = imageUrlFromNode($node);
        $figure = extractFigureFromImageUrl($url);
        if ($figure === '') {
            continue;
        }
        $date = normalizeDate(nodeAttribute($node, 'data-content'));
        $items[] = [
            'figureString' => $figure,
            'figure' => $figure,
            'look' => $figure,
            'changedAt' => $date,
            'date' => $date,
            'imageUrl' => $url,
            'source' => 'toxic-history',
            'datePrecision' => 'observed',
        ];
    }
    return $items;
}

function parseBadgeNode(DOMXPath $xpath, DOMNode $node, bool $selected): array
{
    $code = nodeAttribute($node, 'data-original-title');
    $imageNode = firstXpathNode($xpath, './/img[1]', $node);
    if ($code === '') {
        $code = nodeAttribute($node, 'title');
    }
    if ($code === '') {
        $code = nodeAttribute($imageNode, 'data-original-title')
            ?: nodeAttribute($imageNode, 'title');
    }
    $imageUrl = imageUrlFromNode($imageNode);
    if ($code === '' && $imageUrl !== '') {
        $basename = pathinfo((string) parse_url($imageUrl, PHP_URL_PATH), PATHINFO_FILENAME);
        $code = trim($basename);
    }
    $name = nodeAttribute($imageNode, 'alt');
    if ($name === '') {
        $name = nodeText(firstXpathNode($xpath, './/h4[1]', $node));
    }
    $content = nodeAttribute($node, 'data-content');
    if ($content === '') {
        $content = nodeAttribute($imageNode, 'data-content');
    }
    $description = '';
    if (preg_match_all('#<p[^>]*>(.*?)</p>#si', $content, $paragraphs)) {
        foreach ($paragraphs[1] as $raw) {
            if (stripos((string) $raw, '<time') !== false) {
                continue;
            }
            $candidate = normalizeText(strip_tags((string) $raw));
            if ($candidate !== '') {
                $description = $candidate;
                break;
            }
        }
    }
    $createdAt = '';
    $removedAt = '';
    if (preg_match_all('/datetime=[\'"]([^\'"]+)[\'"]/i', $content, $dateMatches)) {
        $dates = array_values($dateMatches[1]);
        $createdAt = normalizeDate((string) ($dates[0] ?? ''));
        if (
            count($dates) > 1
            && preg_match('/removed|removido|retirado|entfernt|supprim|eliminad|rimosso|verwijderd|silin|poist/i', strip_tags($content))
        ) {
            $removedAt = normalizeDate((string) $dates[count($dates) - 1]);
        }
    }
    $owners = null;
    if (preg_match('/\((\d[\d.,\s]*)\)\s*<\/a>/i', $content, $ownerMatch)) {
        $owners = (int) preg_replace('/\D+/', '', $ownerMatch[1]);
    }
    $class = strtolower(nodeAttribute($node, 'class'));
    $isAchievement = str_contains($class, 'badge-achievement')
        || str_starts_with(strtoupper($code), 'ACH_');
    $isNft = str_starts_with(strtoupper($code), 'NFT')
        || preg_match('/\bnft\b|colecion[aá]vel|collectible/i', $name . ' ' . $description);
    $isRare = str_contains($class, 'badge-is-rare');
    $rarity = $isNft ? 'nft' : ($isRare ? 'rare' : 'generic');

    return [
        'code' => $code,
        'badgeCode' => $code,
        'name' => $name !== '' ? $name : $code,
        'title' => $name !== '' ? $name : $code,
        'description' => $description,
        'desc' => $description,
        'creationTime' => $createdAt,
        'createdAt' => $createdAt,
        'date' => $createdAt,
        'removedAt' => $removedAt,
        'totalOwners' => $owners,
        'imageUrl' => $imageUrl,
        'badgeUrl' => $imageUrl,
        'selected' => $selected,
        'isAchievement' => $isAchievement,
        'isRare' => $isRare,
        'isNft' => (bool) $isNft,
        'rarity' => $rarity,
        'source' => 'toxic-history',
        'datePrecision' => 'observed',
    ];
}

function enrichSelectedBadges(array $selected, array $all): array
{
    $byCode = [];
    foreach ($all as $badge) {
        $code = strtoupper((string) ($badge['code'] ?? ''));
        if ($code !== '') {
            $byCode[$code] = $badge;
        }
    }
    foreach ($selected as &$badge) {
        $code = strtoupper((string) ($badge['code'] ?? ''));
        if ($code !== '' && isset($byCode[$code])) {
            $badge = mergeRecord($badge, $byCode[$code]);
            $badge['selected'] = true;
        }
    }
    unset($badge);
    return $selected;
}

function parseFriendNode(DOMXPath $xpath, DOMNode $node, bool $removed): array
{
    $strong = firstXpathNode($xpath, './/strong[1]', $node);
    $name = nodeAttribute($strong, 'title') ?: nodeText($strong);
    $link = firstXpathNode($xpath, './/a[contains(@href, "/habinfo/")][1]', $node);
    $href = nodeAttribute($link, 'href');
    $uniqueId = '';
    if (preg_match('#/habinfo/(hh[a-z]{2}-[a-z0-9]{20,64})#i', $href, $match)) {
        $uniqueId = strtolower($match[1]);
    }
    $image = firstXpathNode(
        $xpath,
        './/img['
            . 'contains(concat(" ", normalize-space(@class), " "), " avatar ")'
            . ' or contains(concat(" ", normalize-space(@class), " "), " head ")'
            . ' or contains(@src, "habbo-imaging")'
            . ' or contains(@data-src, "habbo-imaging")'
        . '][1]',
        $node
    ) ?: firstXpathNode($xpath, './/img[1]', $node);
    $headUrl = imageUrlFromNode($image);
    $dateNodes = xpathNodes($xpath, './/time[@datetime]', $node);
    $dateNode = $removed && $dateNodes !== []
        ? $dateNodes[count($dateNodes) - 1]
        : ($dateNodes[0] ?? null);
    $rawDate = nodeAttribute($dateNode, 'datetime');
    if ($rawDate === '') {
        foreach ($node->childNodes as $child) {
            if ($child->nodeType !== XML_TEXT_NODE) {
                continue;
            }
            $candidate = normalizeText((string) $child->textContent);
            if ($candidate !== '' && preg_match('/\d{4}|\d{1,2}[\/.-]\d{1,2}/u', $candidate)) {
                $rawDate = $candidate;
                break;
            }
        }
    }
    $dateHasTime = dateValueHasTime($rawDate);
    $date = normalizeDate($rawDate);
    $friend = [
        'uniqueId' => $uniqueId,
        'id' => $uniqueId,
        'habboId' => $uniqueId,
        'name' => $name,
        'username' => $name,
        'habboName' => $name,
        'figureString' => '',
        'headUrl' => $headUrl,
        'avatarUrl' => $headUrl,
        'online' => false,
        'isOnline' => false,
        'source' => 'toxic-history',
        'datePrecision' => $date === '' ? 'unknown' : ($dateHasTime ? 'datetime' : 'date'),
        'dateHasTime' => $dateHasTime,
    ];
    if ($removed) {
        $friend['removedAt'] = $date;
        $friend['date'] = $date;
    } else {
        $friend['creationTime'] = $date;
        $friend['friendSince'] = $date;
        $friend['createdAt'] = $date;
        $friend['date'] = $date;
    }
    return $friend;
}

function parseHabbowidgetsPhotos(DOMXPath $xpath): array
{
    $photos = [];
    foreach (xpathNodes(
        $xpath,
        '//*[@id="habbo-photos-block"]//*[contains(concat(" ", normalize-space(@class), " "), " thumbnail ")]'
    ) as $node) {
        $photoLink = firstXpathNode(
            $xpath,
            './/a[contains(concat(" ", normalize-space(@class), " "), " photo-link ")][1]',
            $node
        );
        $imageUrl = absoluteHabbowidgetsUrl(nodeAttribute($photoLink, 'data-image'));
        if ($imageUrl === '') {
            $imageUrl = imageUrlFromNode(firstXpathNode($xpath, './/img[1]', $node));
        }
        if ($imageUrl === '') {
            continue;
        }

        $roomLink = firstXpathNode($xpath, './/a[contains(@href, "/room/")][1]', $node);
        $roomUrl = trim(nodeAttribute($roomLink, 'href'));
        $roomId = '';
        if (preg_match('#/room/(\d+)#', $roomUrl, $roomMatch)) {
            $roomId = $roomMatch[1];
        }

        $habboLink = firstXpathNode($xpath, './/a[contains(@href, "/photo/")][1]', $node);
        $habboUrl = trim(nodeAttribute($habboLink, 'href'));
        $photoId = '';
        if (preg_match('#/photo/([A-Za-z0-9-]+)#', $habboUrl, $photoMatch)) {
            $photoId = $photoMatch[1];
        }
        if ($photoId === '' && preg_match('#/([^/?#]+)\.(?:png|jpe?g|webp)(?:\?|$)#i', $imageUrl, $imageMatch)) {
            $photoId = $imageMatch[1];
        }

        $infoParagraph = firstXpathNode($xpath, './p[1]', $node);
        $infoText = nodeText($infoParagraph);
        $createdAt = normalizeDate($infoText);
        $likesNode = firstXpathNode($xpath, './p[1]//a[@data-content][1]', $node);
        $likerNames = [];
        $likerContent = nodeAttribute($likesNode, 'data-content');
        if ($likerContent !== '') {
            foreach (preg_split('/<br\s*\/?>/i', $likerContent) ?: [] as $liker) {
                $liker = normalizeText(strip_tags((string) $liker));
                if ($liker !== '' && !in_array($liker, $likerNames, true)) {
                    $likerNames[] = $liker;
                }
            }
        }
        $likesCount = count($likerNames);
        if ($likesCount === 0 && preg_match('/(\d[\d.,]*)\s+(?:likes?|curtidas?|me gusta|j.?aime)/iu', $infoText, $likesMatch)) {
            $likesCount = (int) preg_replace('/\D+/', '', $likesMatch[1]);
        }

        $photos[] = [
            'id' => $photoId,
            'photoId' => $photoId,
            'previewUrl' => $imageUrl,
            'url' => $imageUrl,
            'imageUrl' => $imageUrl,
            'photoUrl' => $imageUrl,
            'creationTime' => $createdAt,
            'createdAt' => $createdAt,
            'time' => $createdAt,
            'formatted_time' => $createdAt,
            'roomId' => $roomId,
            'room_id' => $roomId,
            'roomUrl' => $roomUrl,
            'habboUrl' => $habboUrl,
            'likerNames' => $likerNames,
            'likes' => $likerNames,
            'likers' => $likerNames,
            'likesCount' => $likesCount,
            'source' => 'toxic-history',
            'datePrecision' => 'observed',
        ];
    }
    return $photos;
}

function parseRoomNode(DOMXPath $xpath, DOMNode $node): array
{
    $name = nodeText(firstXpathNode($xpath, './/h3[1]', $node));
    $imageNode = firstXpathNode($xpath, './/img[contains(concat(" ", normalize-space(@class), " "), " room-preview ")][1]', $node)
        ?? firstXpathNode($xpath, './/img[1]', $node);
    $imageUrl = imageUrlFromNode($imageNode);
    $link = firstXpathNode($xpath, './/a[contains(@href, "/room/")][1]', $node);
    $url = absoluteHabbowidgetsUrl(nodeAttribute($link, 'href'));
    $roomId = '';
    if (preg_match('#/room/(\d+)#', $url, $match)) {
        $roomId = $match[1];
    }
    if ($roomId === '' && preg_match('#/(\d+)\.(?:png|gif|jpe?g|webp)(?:\?|$)#i', $imageUrl, $match)) {
        $roomId = $match[1];
    }
    $paragraphs = xpathNodes($xpath, './p', $node);
    $description = isset($paragraphs[0]) ? nodeText($paragraphs[0]) : '';
    $labels = xpathNodes($xpath, './/span[contains(concat(" ", normalize-space(@class), " "), " label ")]', $node);
    $score = isset($labels[0]) ? integerFromNode($labels[0]) : 0;
    $capacity = isset($labels[1]) ? integerFromNode($labels[1]) : 0;
    $dates = dateValuesAfterStrong($xpath, $node);
    $createdAt = normalizeDate($dates[0] ?? '');
    $updatedAt = normalizeDate($dates[1] ?? '');
    $removedAt = nodeAttribute(
        firstXpathNode(
            $xpath,
            './/*[contains(concat(" ", normalize-space(@class), " "), " text-danger ")]//time[@datetime][1]',
            $node
        ),
        'datetime'
    );
    $removedAt = normalizeDate($removedAt);

    return [
        'id' => $roomId,
        'roomId' => $roomId,
        'room_id' => $roomId,
        'name' => $name,
        'roomName' => $name,
        'caption' => $name,
        'title' => $name,
        'description' => $description,
        'desc' => $description,
        'score' => $score,
        'rating' => $score,
        'maximumVisitors' => $capacity,
        'capacity' => $capacity,
        'creationTime' => $createdAt,
        'createdAt' => $createdAt,
        'date' => $createdAt,
        'updatedAt' => $updatedAt,
        'removedAt' => $removedAt,
        'removed' => $removedAt !== '',
        'thumbnailUrl' => $imageUrl,
        'url' => $imageUrl,
        'roomUrl' => $url,
        'source' => 'toxic-history',
        'datePrecision' => 'observed',
    ];
}

function parseGroupNode(DOMXPath $xpath, DOMNode $node): array
{
    $name = nodeText(firstXpathNode($xpath, './/h3[1]', $node));
    $image = firstXpathNode($xpath, './/img[1]', $node);
    $badgeUrl = imageUrlFromNode($image);
    $badgeCode = '';
    if (preg_match('~/habbo-imaging/badge/([^/?#]+?)(?:\.gif)?(?:\?|$)~i', $badgeUrl, $badgeMatch)) {
        $badgeCode = rawurldecode($badgeMatch[1]);
    }
    $paragraphs = xpathNodes($xpath, './p', $node);
    $description = isset($paragraphs[0]) ? nodeText($paragraphs[0]) : '';
    $dates = dateValuesAfterStrong($xpath, $node);
    $createdAt = normalizeDate($dates[0] ?? '');
    $updatedAt = count($dates) > 2 ? normalizeDate($dates[1]) : '';
    $joinedAt = normalizeDate($dates[count($dates) > 2 ? 2 : 1] ?? '');
    $leftAt = normalizeDate(nodeAttribute(
        firstXpathNode(
            $xpath,
            './/*[contains(concat(" ", normalize-space(@class), " "), " text-danger ")]//time[@datetime][1]',
            $node
        ),
        'datetime'
    ));
    $link = firstXpathNode($xpath, './/a[@href][last()]', $node);
    $url = absoluteHabbowidgetsUrl(nodeAttribute($link, 'href'));
    $roomId = '';
    $query = (string) parse_url($url, PHP_URL_QUERY);
    $queryParams = [];
    parse_str($query, $queryParams);
    if (isset($queryParams['room'])) {
        $roomId = trim((string) $queryParams['room']);
    }
    $colors = [];
    foreach (xpathNodes(
        $xpath,
        './/*[contains(concat(" ", normalize-space(@class), " "), " group-color ")]',
        $node
    ) as $colorNode) {
        if (preg_match('/background-color\s*:\s*([^;]+)/i', nodeAttribute($colorNode, 'style'), $match)) {
            $colors[] = trim($match[1]);
        }
    }
    $admin = firstXpathNode(
        $xpath,
        './/*[contains(concat(" ", normalize-space(@class), " "), " text-warning ")]',
        $node
    ) instanceof DOMNode;

    return [
        'id' => $roomId,
        'groupId' => $roomId,
        'name' => $name,
        'groupName' => $name,
        'description' => $description,
        'desc' => $description,
        'badgeCode' => $badgeCode,
        'code' => $badgeCode,
        'badgeUrl' => $badgeUrl,
        'imageUrl' => $badgeUrl,
        'url' => $badgeUrl,
        'roomId' => $roomId,
        'groupRoomId' => $roomId,
        'groupRoomUrl' => $url,
        'createdAt' => $createdAt,
        'creationTime' => $createdAt,
        'date' => $createdAt,
        'updatedAt' => $updatedAt,
        'joinedAt' => $joinedAt,
        'leftAt' => $leftAt,
        'removedAt' => $leftAt,
        'removed' => $leftAt !== '',
        'isAdmin' => $admin,
        'primaryColour' => (string) ($colors[0] ?? ''),
        'secondaryColour' => (string) ($colors[1] ?? ''),
        'colors' => $colors,
        'source' => 'toxic-history',
        'datePrecision' => 'observed',
    ];
}

function parseGroupFragmentsFromHtml(string $html): array
{
    if (!preg_match_all(
        '#<div\b[^>]*class\s*=\s*["\'][^"\']*\bgroup-holder\b[^"\']*["\'][^>]*>(.*?)</div>#si',
        $html,
        $matches
    )) {
        return [];
    }
    $groups = [];
    foreach ($matches[1] as $fragment) {
        $name = '';
        if (preg_match('#<h3\b[^>]*>(.*?)</h3>#si', (string) $fragment, $match)) {
            $name = normalizeText(strip_tags($match[1]));
        }
        $badgeUrl = '';
        if (preg_match('/\bdata-original\s*=\s*["\']([^"\']+)["\']/i', (string) $fragment, $match)) {
            $badgeUrl = absoluteHabbowidgetsUrl(html_entity_decode($match[1], ENT_QUOTES | ENT_HTML5, 'UTF-8'));
        } elseif (preg_match('/<img\b[^>]*\bsrc\s*=\s*["\']([^"\']+)["\']/i', (string) $fragment, $match)) {
            $badgeUrl = absoluteHabbowidgetsUrl(html_entity_decode($match[1], ENT_QUOTES | ENT_HTML5, 'UTF-8'));
        }
        $description = '';
        if (preg_match('#<p\b[^>]*>(.*?)</p>#si', (string) $fragment, $match)) {
            $description = normalizeText(strip_tags($match[1]));
        }
        $dateValues = [];
        if (preg_match_all(
            '#<strong\b[^>]*>.*?</strong>\s*([^<]+)#si',
            (string) $fragment,
            $dateMatches
        )) {
            foreach ($dateMatches[1] as $candidate) {
                $candidate = normalizeText((string) $candidate);
                if ($candidate !== '' && preg_match('/\d/', $candidate)) {
                    $dateValues[] = $candidate;
                }
            }
        }
        $urls = [];
        if (preg_match_all('/<a\b[^>]*\bhref\s*=\s*["\']([^"\']+)["\']/i', (string) $fragment, $linkMatches)) {
            foreach ($linkMatches[1] as $href) {
                $urls[] = absoluteHabbowidgetsUrl(html_entity_decode((string) $href, ENT_QUOTES | ENT_HTML5, 'UTF-8'));
            }
        }
        $url = (string) ($urls[count($urls) - 1] ?? '');
        $roomId = '';
        $query = (string) parse_url($url, PHP_URL_QUERY);
        $params = [];
        parse_str($query, $params);
        if (isset($params['room'])) {
            $roomId = trim((string) $params['room']);
        }
        $colors = [];
        if (preg_match_all(
            '/\bbackground-color\s*:\s*([^;"\']+)/i',
            (string) $fragment,
            $colorMatches
        )) {
            $colors = array_values(array_map('trim', $colorMatches[1]));
        }
        $createdAt = normalizeDate($dateValues[0] ?? '');
        $joinedAt = normalizeDate($dateValues[1] ?? '');
        $groups[] = [
            'id' => $roomId,
            'groupId' => $roomId,
            'name' => $name,
            'groupName' => $name,
            'description' => $description,
            'desc' => $description,
            'badgeCode' => '',
            'badgeUrl' => $badgeUrl,
            'imageUrl' => $badgeUrl,
            'url' => $badgeUrl,
            'roomId' => $roomId,
            'groupRoomUrl' => $url,
            'createdAt' => $createdAt,
            'creationTime' => $createdAt,
            'date' => $createdAt,
            'joinedAt' => $joinedAt,
            'isAdmin' => stripos((string) $fragment, 'text-warning') !== false,
            'primaryColour' => (string) ($colors[0] ?? ''),
            'secondaryColour' => (string) ($colors[1] ?? ''),
            'colors' => $colors,
            'source' => 'toxic-history',
            'datePrecision' => 'observed',
        ];
    }
    return $groups;
}

function dateValuesAfterStrong(DOMXPath $xpath, DOMNode $context): array
{
    $values = [];
    foreach (xpathNodes($xpath, './/strong', $context) as $strong) {
        $value = nextTextUntilBreak($strong);
        if ($value === '' || !preg_match('/\d/u', $value)) {
            continue;
        }
        $values[] = $value;
    }
    return $values;
}

function nextTextUntilBreak(DOMNode $node): string
{
    $parts = [];
    $current = $node->nextSibling;
    while ($current instanceof DOMNode) {
        if ($current instanceof DOMElement && strtolower($current->tagName) === 'br') {
            break;
        }
        $parts[] = (string) $current->textContent;
        $current = $current->nextSibling;
    }
    return normalizeText(implode(' ', $parts));
}

function parseTicker(DOMXPath $xpath): array
{
    $events = [];
    foreach (xpathNodes(
        $xpath,
        '//*[@id="habbo-ticker-block"]//*[contains(concat(" ", normalize-space(@class), " "), " page ")]'
    ) as $node) {
        $message = nodeText(firstXpathNode($xpath, './p[1]', $node));
        $title = nodeText(firstXpathNode($xpath, './h3[1]', $node));
        $dateNode = firstXpathNode($xpath, './/em[@title][1]', $node)
            ?? firstXpathNode($xpath, './/time[@datetime][1]', $node);
        $date = normalizeDate(
            nodeAttribute($dateNode, 'title') ?: nodeAttribute($dateNode, 'datetime')
        );
        $link = firstXpathNode($xpath, './/a[@href][1]', $node);
        $url = absoluteHabbowidgetsUrl(nodeAttribute($link, 'href'));
        $imageUrl = '';
        $figure = '';
        foreach (xpathNodes($xpath, './/img', $node) as $image) {
            $candidate = imageUrlFromNode($image);
            $candidateFigure = extractFigureFromImageUrl($candidate);
            if ($candidateFigure !== '') {
                $imageUrl = $candidate;
                $figure = $candidateFigure;
                break;
            }
            if ($imageUrl === '' && !str_contains($candidate, '/flags/')) {
                $imageUrl = $candidate;
            }
        }
        $combined = normalizeText($message . ' ' . $title);
        $type = inferTickerType($combined, $figure);
        $roomId = '';
        if (preg_match('#(?:/room/|[?&]room=)([^&/\s]+)#i', $url, $match)) {
            $roomId = trim($match[1]);
        }
        $previousName = '';
        if ($type === 'name') {
            if (preg_match(
                '/(?:from|de|von|da|van)\s+["“]?([^"”]+?)["”]?\s+(?:to|para|a|zu|naar)\s+/iu',
                $combined,
                $match
            )) {
                $previousName = trim($match[1]);
            }
        }
        $events[] = [
            'type' => $type,
            'message' => $message,
            'title' => $title,
            'date' => $date,
            'createdAt' => $date,
            'figureString' => $figure,
            'imageUrl' => $imageUrl,
            'url' => $url,
            'roomId' => $roomId,
            'previousName' => $previousName,
            'source' => 'toxic-history',
            'datePrecision' => 'observed',
        ];
    }
    return $events;
}

function inferTickerType(string $text, string $figure): string
{
    $low = strtolower(removeAccents($text));
    if ($figure !== '' || preg_match('/look|visual|aussehen|apparence|aspetto|uiterlijk|gorunum/', $low)) {
        return 'look';
    }
    $roomWord = preg_match('/room|quarto|raum|chambre|habitacion|stanza|kamer|oda|huone/', $low);
    $removed = preg_match('/remov|delet|exclu|apag|geloscht|supprim|borrad|eliminat|verwijderd|silin|poist/', $low);
    if ($roomWord && $removed) {
        return 'room_removed';
    }
    if ($roomWord) {
        return 'room';
    }
    if (preg_match('/name|nome|nick|naam|nombre|nom\b|isim/', $low)) {
        return 'name';
    }
    if (preg_match('/motto|missao|mission|devise|lema/', $low)) {
        return 'motto';
    }
    if (preg_match('/badge|emblema|abzeichen|insigne|distintivo|rozet/', $low)) {
        return 'badge';
    }
    if (preg_match('/friend|amig|freund|ami|vriend|arkadas|ystav/', $low)) {
        return $removed ? 'friend_removed' : 'friend';
    }
    return 'event';
}

function parseClosetRows(DOMXPath $xpath, array $config): array
{
    $items = [];
    foreach (xpathNodes($xpath, '//*[@id="closet-modal"]//tr') as $row) {
        $link = firstXpathNode($xpath, './/a[contains(@href, "/habbo/closet/")][1]', $row);
        $href = nodeAttribute($link, 'href');
        $code = '';
        if (preg_match('#/habbo/closet/[^/]+/([A-Za-z]{2}-\d+)#', $href, $match)) {
            $code = strtolower($match[1]);
        }
        if ($code === '') {
            continue;
        }
        $category = nodeText(firstXpathNode($xpath, './th[1]', $row));
        $cells = xpathNodes($xpath, './td', $row);
        $name = isset($cells[0]) ? nodeText($cells[0]) : '';
        if ($name === '') {
            $name = $code;
        }
        $classification = habbonewsClothingClassification($code);
        if ((bool) ($classification['hidden'] ?? false)) {
            continue;
        }
        $rowContext = nodeText($row);
        $scientificName = extractClothingScientificName($rowContext . ' ' . $href);
        $slot = strtolower((string) strtok($code, '-'));
        $items[$slot] = clothingRecord(
            $code,
            $name,
            $category,
            $slot,
            (string) $config['key'],
            absoluteHabbowidgetsUrl($href),
            $scientificName,
            $classification
        );
    }
    return $items;
}

function clothingRecord(
    string $code,
    string $name,
    string $category,
    string $slot,
    string $hotel,
    string $closetUrl = '',
    string $scientificName = '',
    array $classification = []
): array {
    $name = sanitizeClothingName($name, $code);
    $category = trim($category);
    if ($category === '') {
        $category = clothingSlotName($slot, $hotel);
    }
    if ($classification === []) {
        $classification = habbonewsClothingClassification($code);
    }
    $iconCode = trim((string) ($classification['iconCode'] ?? ''));
    $resolvedIconUrl = trim((string) ($classification['iconUrl'] ?? ''));
    $hidden = (bool) ($classification['hidden'] ?? false);
    $known = (bool) ($classification['known'] ?? false);

    return [
        'code' => $code,
        'classname' => $code,
        'className' => $code,
        'scientificName' => $scientificName,
        'id' => $code,
        'name' => $name,
        'publicName' => $name,
        'furniName' => $name,
        'localeNames' => [
            localeKeyForHotel($hotel) => $name,
        ],
        'lineCode' => $category,
        'category' => $category,
        '_slot' => $slot,
        'slot' => $slot,
        'rarity' => '',
        'rarityType' => '',
        'rarityLevel' => 0,
        'rarityKey' => $iconCode,
        'rarityCode' => $iconCode,
        'rarityIconCode' => $iconCode,
        'habbonewsIconCode' => $iconCode,
        'rarityLabel' => '',
        'rarityDescription' => '',
        'raritySource' => (string) ($classification['source'] ?? 'unclassified'),
        'rarityConfidence' => $known ? 'exact' : 'unknown',
        'rarityKnown' => $known,
        'isDefaultClothing' => $hidden,
        'hidden' => $hidden,
        'isRare' => false,
        'isNft' => false,
        'iconUrl' => $resolvedIconUrl,
        'imageUrl' => $resolvedIconUrl,
        'rarityIconUrl' => $resolvedIconUrl,
        'closetUrl' => '',
        'source' => 'toxic',
    ];
}

function sanitizeClothingName(string $name, string $code): string
{
    $clean = normalizeText($name);
    $clean = preg_replace(
        '/\s*[-–|]\s*(?:Habbo\s+(?:Guarda[- ]Roupa|Closet).*|Habbo\s*Widgets(?:\.com)?.*)$/iu',
        '',
        $clean
    ) ?? $clean;
    $clean = preg_replace('/\s*Habbo\s*Widgets(?:\.com)?\s*/iu', ' ', $clean) ?? $clean;
    $clean = normalizeText($clean);
    if (
        $clean === ''
        || preg_match('/^(?:de|from)\s+(?:rosto\s*&\s*corpo|face\s*&\s*body|cabelo|hair|roupas?|clothes?)$/iu', $clean)
    ) {
        return $code;
    }
    return $clean;
}

function isCompleteClothingName(string $name, string $code, string $category = ''): bool
{
    $name = sanitizeClothingName($name, $code);
    $nameIdentity = preg_replace('/[^a-z0-9]+/', '', strtolower(removeAccents($name))) ?? '';
    $codeIdentity = preg_replace('/[^a-z0-9]+/', '', strtolower(removeAccents($code))) ?? '';
    $categoryIdentity = preg_replace('/[^a-z0-9]+/', '', strtolower(removeAccents($category))) ?? '';
    if ($name === '' || ($codeIdentity !== '' && $nameIdentity === $codeIdentity)) {
        return false;
    }
    if ($categoryIdentity !== '' && $nameIdentity === $categoryIdentity) {
        return false;
    }
    return !isTechnicalClothingName($name);
}

function isTechnicalClothingName(string $name): bool
{
    $clean = normalizeText($name);
    if ($clean === '') {
        return true;
    }
    $plain = strtolower(removeAccents($clean));
    $technical = preg_replace('/[^a-z0-9]+/', '_', $plain) ?? '';
    $technical = trim(preg_replace('/_+/', '_', $technical) ?? $technical, '_');
    $slots = '(?:hd|hr|ch|lg|sh|ha|he|ea|fa|cp|ca|cc|wa|pt|mc)';

    if (preg_match('/^\d+$/', $technical) === 1) {
        return true;
    }
    if (preg_match('/^' . $slots . '_?\d+(?:_\d+)*(?:_[a-z0-9]+)*$/', $technical) === 1) {
        return true;
    }
    if (preg_match('/^(?:nft|kld)_?\d+(?:_\d+)*(?:_name)?$/', $technical) === 1) {
        return true;
    }
    if (preg_match('/^(?:clothing|figure|avatar|look|furni)(?:_[a-z0-9]+)+$/', $technical) === 1) {
        return true;
    }
    if (preg_match(
        '/^(?:rosto_corpo|face_body|cabelo|hair|camisas?|shirts?|calcas?|trousers?|pants?|sapatos?|shoes?|chapeus?|hats?|acessorios_de_cabeca|head_accessories|acessorios_faciais|face_accessories|estampas|prints|casacos|coats|acessorios_de_peito|chest_accessories|acessorios_de_orelha|ear_accessories|acessorios_de_mao|hand_accessories|cintura|waist)$/',
        $technical
    ) === 1) {
        return true;
    }

    $technicalSeparators = preg_match('/[_.:\/]/', $clean) === 1
        || (!str_contains($clean, ' ') && str_contains($clean, '-'));
    return $technicalSeparators
        && preg_match(
            '/^(?:hair|hairstyle|shirt|top|trousers?|pants?|shoes?|hat|head|face|coat|jacket|accessory|accessories|belt|waist|chest|ear|hand)(?:_[a-z0-9]+)+$/',
            $technical
        ) === 1;
}

function clothingSlotName(string $slot, string $hotel): string
{
    $pt = [
        'hr' => 'Cabelo',
        'hd' => 'Rosto & Corpo',
        'ch' => 'Camisas',
        'lg' => 'Calças',
        'sh' => 'Sapatos',
        'ha' => 'Chapéus',
        'he' => 'Acessórios de cabeça',
        'fa' => 'Acessórios faciais',
        'cp' => 'Estampas',
        'ca' => 'Casacos',
        'cc' => 'Acessórios de peito',
        'ea' => 'Acessórios de orelha',
        'mc' => 'Acessórios de mão',
        'pt' => 'Cintura',
        'wa' => 'Cintura',
    ];
    $en = [
        'hr' => 'Hair',
        'hd' => 'Face & Body',
        'ch' => 'Shirts',
        'lg' => 'Trousers',
        'sh' => 'Shoes',
        'ha' => 'Hats',
        'he' => 'Head accessories',
        'fa' => 'Face accessories',
        'cp' => 'Prints',
        'ca' => 'Coats',
        'cc' => 'Chest accessories',
        'ea' => 'Ear accessories',
        'mc' => 'Hand accessories',
        'pt' => 'Waist',
        'wa' => 'Waist',
    ];
    $map = normalizeHotel($hotel) === 'br' ? $pt : $en;
    return (string) ($map[strtolower($slot)] ?? strtoupper($slot));
}

function extractClothingScientificName(string $html): string
{
    if (preg_match('/\b(clothing_[a-z0-9_]+)\b/i', $html, $match)) {
        return strtolower($match[1]);
    }
    return '';
}

function localeKeyForHotel(string $hotel): string
{
    $hotel = normalizeHotel($hotel);
    return $hotel === 'com' ? 'us' : $hotel;
}

function listMatchesCount(array $items, int $count, bool $counterPresent = false): bool
{
    if ($counterPresent) {
        return count($items) >= $count;
    }
    return $count > 0 && count($items) >= $count;
}

function integerFromNode(?DOMNode $node): int
{
    $text = nodeText($node);
    if (preg_match('/\d[\d.,\s]*/', $text, $match)) {
        return (int) preg_replace('/\D+/', '', $match[0]);
    }
    return 0;
}

function integersFromNode(?DOMNode $node): array
{
    $text = nodeText($node);
    if (!preg_match_all('/\d[\d.,]*/', $text, $matches)) {
        return [];
    }
    $values = [];
    foreach ($matches[0] as $raw) {
        $digits = preg_replace('/\D+/', '', (string) $raw);
        if ($digits !== '') {
            $values[] = (int) $digits;
        }
    }
    return $values;
}

function dateValueHasTime(string $raw): bool
{
    $raw = normalizeText($raw);
    if ($raw === '') {
        return false;
    }
    return preg_match('/(?:T|\s|,)(\d{1,2}):(\d{2})(?::\d{2})?/u', $raw) === 1;
}

function normalizeDate(string $raw): string
{
    $raw = normalizeText($raw);
    if ($raw === '') {
        return '';
    }
    if (preg_match('/^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}(?:\.\d+)?(?:Z|[+-]\d{2}:\d{2})$/', $raw)) {
        return $raw;
    }
    if (preg_match('/^(\d{4}-\d{2}-\d{2})[ T](\d{2}:\d{2}:\d{2})$/', $raw, $match)) {
        return $match[1] . 'T' . $match[2] . '+00:00';
    }
    if (preg_match('/^(\d{4}-\d{2}-\d{2})$/', $raw)) {
        return $raw;
    }

    $normalized = strtolower(removeAccents($raw));
    $months = [
        'january' => 1, 'januar' => 1, 'janvier' => 1, 'janeiro' => 1,
        'enero' => 1, 'gennaio' => 1, 'januari' => 1, 'tammikuu' => 1,
        'tammikuuta' => 1, 'ocak' => 1,
        'february' => 2, 'februar' => 2, 'fevrier' => 2, 'fevereiro' => 2,
        'febrero' => 2, 'febbraio' => 2, 'februari' => 2, 'helmikuu' => 2,
        'helmikuuta' => 2, 'subat' => 2,
        'march' => 3, 'marz' => 3, 'mars' => 3, 'marco' => 3,
        'marzo' => 3, 'maart' => 3, 'maaliskuu' => 3, 'maaliskuuta' => 3, 'mart' => 3,
        'april' => 4, 'avril' => 4, 'abril' => 4, 'aprile' => 4,
        'huhtikuu' => 4, 'huhtikuuta' => 4, 'nisan' => 4,
        'may' => 5, 'mai' => 5, 'maio' => 5, 'mayo' => 5, 'maggio' => 5,
        'mei' => 5, 'toukokuu' => 5, 'toukokuuta' => 5, 'mayis' => 5,
        'june' => 6, 'juni' => 6, 'juin' => 6, 'junho' => 6, 'junio' => 6,
        'giugno' => 6, 'kesakuu' => 6, 'kesakuuta' => 6, 'haziran' => 6,
        'july' => 7, 'juli' => 7, 'juillet' => 7, 'julho' => 7, 'julio' => 7,
        'luglio' => 7, 'heinakuu' => 7, 'heinakuuta' => 7, 'temmuz' => 7,
        'august' => 8, 'aout' => 8, 'agosto' => 8, 'augustus' => 8,
        'elokuu' => 8, 'elokuuta' => 8, 'agustos' => 8,
        'september' => 9, 'septembre' => 9, 'setembro' => 9, 'septiembre' => 9,
        'settembre' => 9, 'syyskuu' => 9, 'syyskuuta' => 9, 'eylul' => 9,
        'october' => 10, 'oktober' => 10, 'octobre' => 10, 'outubro' => 10,
        'octubre' => 10, 'ottobre' => 10, 'lokakuu' => 10, 'lokakuuta' => 10, 'ekim' => 10,
        'november' => 11, 'novembre' => 11, 'novembro' => 11, 'noviembre' => 11,
        'marraskuu' => 11, 'marraskuuta' => 11, 'kasim' => 11,
        'december' => 12, 'dezember' => 12, 'decembre' => 12, 'dezembro' => 12,
        'diciembre' => 12, 'dicembre' => 12, 'joulukuu' => 12, 'joulukuuta' => 12, 'aralik' => 12,
    ];
    foreach ($months as $word => $month) {
        if (!preg_match('/\b' . preg_quote($word, '/') . '\b/u', $normalized)) {
            continue;
        }
        $time = '';
        if (preg_match('/\b(\d{1,2}):(\d{2})(?::(\d{2}))?\b/', $normalized, $timeMatch)) {
            $time = sprintf(
                '%02d:%02d:%02d',
                (int) $timeMatch[1],
                (int) $timeMatch[2],
                (int) ($timeMatch[3] ?? 0)
            );
        }
        $withoutTime = preg_replace('/\b\d{1,2}:\d{2}(?::\d{2})?\b/', '', $normalized);
        if (preg_match('/\b(\d{1,2})\D+' . preg_quote($word, '/') . '\D+(\d{4})\b/u', (string) $withoutTime, $parts)) {
            $date = sprintf('%04d-%02d-%02d', (int) $parts[2], $month, (int) $parts[1]);
            return $time !== '' ? $date . 'T' . $time . '+00:00' : $date;
        }
        if (preg_match('/\b' . preg_quote($word, '/') . '\D+(\d{1,2})\D+(\d{4})\b/u', (string) $withoutTime, $parts)) {
            $date = sprintf('%04d-%02d-%02d', (int) $parts[2], $month, (int) $parts[1]);
            return $time !== '' ? $date . 'T' . $time . '+00:00' : $date;
        }
    }
    return $raw;
}

function removeAccents(string $value): string
{
    if (function_exists('transliterator_transliterate')) {
        $converted = transliterator_transliterate('Any-Latin; Latin-ASCII', $value);
        if (is_string($converted)) {
            return $converted;
        }
    }
    if (function_exists('iconv')) {
        $converted = @iconv('UTF-8', 'ASCII//TRANSLIT//IGNORE', $value);
        if (is_string($converted)) {
            return $converted;
        }
    }
    return strtr($value, [
        'á' => 'a', 'à' => 'a', 'ã' => 'a', 'â' => 'a', 'ä' => 'a',
        'é' => 'e', 'è' => 'e', 'ê' => 'e', 'ë' => 'e',
        'í' => 'i', 'ì' => 'i', 'î' => 'i', 'ï' => 'i',
        'ó' => 'o', 'ò' => 'o', 'õ' => 'o', 'ô' => 'o', 'ö' => 'o',
        'ú' => 'u', 'ù' => 'u', 'û' => 'u', 'ü' => 'u',
        'ç' => 'c', 'ñ' => 'n', 'ş' => 's', 'ğ' => 'g',
    ]);
}

function mergeCurrentAndHistoricalData(
    string $hotel,
    array $widget,
    array $officialUser,
    array $officialProfile,
    array $officialPhotos
): array {
    $nestedUser = is_array($officialProfile['user'] ?? null)
        ? $officialProfile['user']
        : [];
    $officialUser = mergeRecord($officialUser, $nestedUser);

    $uniqueId = firstString($officialUser, ['uniqueId', 'id'])
        ?: firstString($widget, ['uniqueId', 'id']);
    $name = firstString($officialUser, ['name', 'username'])
        ?: firstString($widget, ['name', 'username']);
    $figure = firstString($officialUser, ['figureString', 'figure', 'figure_string'])
        ?: firstString($widget, ['figureString', 'figure', 'figure_string']);
    $motto = firstString($officialUser, ['motto', 'mission'])
        ?: firstString($widget, ['motto', 'mission']);

    $officialVisible = firstNullableBool(
        $officialUser,
        ['profileVisible', 'isProfileVisible', 'visible']
    );
    if ($officialVisible === null) {
        $officialVisible = firstNullableBool(
            $officialProfile,
            ['profileVisible', 'isProfileVisible', 'visible']
        );
    }
    $widgetVisible = firstNullableBool(
        $widget,
        ['profileVisible', 'isProfileVisible', 'visible']
    );
    $visible = $officialVisible ?? $widgetVisible ?? true;

    $officialSelected = normalizeOfficialBadges(
        extractArrayFromKeys($officialUser, ['selectedBadges'])
    );
    $widgetSelected = extractArrayFromKeys($widget, ['selectedBadges']);
    $selectedBadges = mergeLists($officialSelected, $widgetSelected, ['code']);

    $widgetBadges = extractArrayFromKeys($widget, ['badges']);
    $badges = mergeLists($widgetBadges, $selectedBadges, ['code']);
    $selectedBadges = enrichSelectedBadges($selectedBadges, $badges);

    $officialFriendsAuthoritative = array_key_exists('friends', $officialProfile)
        && is_array($officialProfile['friends']);
    $officialFriends = normalizeOfficialFriends(
        extractArrayFromKeys($officialProfile, ['friends'])
    );
    $widgetFriends = extractArrayFromKeys($widget, ['friends']);
    $friends = $officialFriendsAuthoritative
        ? enrichPrimaryList($officialFriends, $widgetFriends, ['uniqueId', 'name'])
        : $widgetFriends;

    $officialRooms = normalizeOfficialRooms(
        extractArrayFromKeys($officialProfile, ['rooms'])
    );
    $widgetRooms = extractArrayFromKeys($widget, ['rooms']);
    $rooms = mergeLists($widgetRooms, $officialRooms, ['id', 'name']);

    $officialGroups = normalizeOfficialGroups(
        extractArrayFromKeys($officialProfile, ['groups'])
    );
    $widgetGroups = extractArrayFromKeys($widget, ['groups']);
    $groups = mergeListsPreservePrimary($widgetGroups, $officialGroups, ['id', 'name']);

    $widgetPhotos = extractArrayFromKeys($widget, ['photos']);
    $photos = mergeListsPreservePrimary(
        $widgetPhotos,
        normalizeOfficialPhotos($officialPhotos),
        ['id', 'url']
    );
    $totalBadges = firstString(
        $widget,
        ['totalBadges', 'badgeCount', 'badgesCount', 'badgesTotal']
    );
    if ($totalBadges === '') {
        $totalBadges = firstString(
            $officialUser,
            ['totalBadges', 'badgeCount', 'badgesCount', 'badgesTotal']
        );
    }
    if ($totalBadges === '') {
        $totalBadges = (string) count($badges);
    }
    $currentLevel = firstString($officialUser, ['currentLevel', 'level']);
    if ($currentLevel === '') {
        $currentLevel = firstString($widget, ['currentLevel', 'level']);
    }
    $experience = firstString($officialUser, ['experience', 'xp']);
    if ($experience === '') {
        $experience = firstString($widget, ['experience', 'xp']);
    }
    $starGems = firstString($officialUser, ['starGemCount', 'starGems']);
    if ($starGems === '') {
        $starGems = firstString($widget, ['starGemCount', 'starGems']);
    }
    $widgetCounts = is_array($widget['sourceCounts'] ?? null)
        ? $widget['sourceCounts']
        : [];
    $banned = $officialUser === []
        && $officialProfile === []
        && firstBool($widget, ['isBanned', 'banned', 'is_banned'], false);

    return [
        'uniqueId' => $uniqueId,
        'id' => $uniqueId,
        'habboId' => $uniqueId,
        'hotel' => normalizeHotel($hotel),
        'name' => $name,
        'username' => $name,
        'habboName' => $name,
        'figureString' => $figure,
        'figure' => $figure,
        'figure_string' => $figure,
        'avatarUrl' => firstString($widget, ['avatarUrl']),
        'motto' => $motto,
        'mission' => $motto,
        'online' => firstBool($officialUser, ['online', 'isOnline'], false),
        'isOnline' => firstBool($officialUser, ['online', 'isOnline'], false),
        'isBanned' => $banned,
        'banned' => $banned,
        'profileVisible' => $visible,
        'isProfileVisible' => $visible,
        'visible' => $visible,
        'privateProfile' => !$visible,
        'memberSince' => firstString(
            $officialUser,
            ['memberSince', 'creationTime', 'createdAt', 'registeredAt']
        ),
        'creationTime' => firstString(
            $officialUser,
            ['memberSince', 'creationTime', 'createdAt', 'registeredAt']
        ),
        'lastAccessTime' => firstString(
            $officialUser,
            ['lastAccessTime', 'lastLoginTime', 'lastOnline', 'lastVisit']
        ),
        'lastChangeAt' => firstString($widget, ['lastChangeAt']),
        'currentLevel' => $currentLevel,
        'level' => $currentLevel,
        'experience' => $experience,
        'xp' => $experience,
        'starGemCount' => $starGems,
        'starGems' => $starGems,
        'totalBadges' => $totalBadges,
        'badgeCount' => (int) $totalBadges,
        'badgesCount' => (int) $totalBadges,
        'friendCount' => $officialFriendsAuthoritative
            ? count($friends)
            : (int) ($widgetCounts['friends'] ?? count($friends)),
        'groupCount' => (int) ($widgetCounts['groups'] ?? count($groups)),
        'roomCount' => (int) ($widgetCounts['rooms'] ?? count($rooms)),
        'photoCount' => (int) ($widgetCounts['photos'] ?? count($photos)),
        'selectedBadges' => $selectedBadges,
        'badges' => $badges,
        'previousBadges' => extractArrayFromKeys($widget, ['previousBadges']),
        'previousNames' => extractArrayFromKeys($widget, ['previousNames']),
        'previousMottos' => extractArrayFromKeys($widget, ['previousMottos']),
        'previousStyles' => extractArrayFromKeys($widget, ['previousStyles']),
        'friends' => $friends,
        'previousFriends' => extractArrayFromKeys($widget, ['previousFriends']),
        'rooms' => $rooms,
        'previousRooms' => extractArrayFromKeys($widget, ['previousRooms']),
        'groups' => $groups,
        'previousGroups' => extractArrayFromKeys($widget, ['previousGroups']),
        'photos' => $photos,
        'ticker' => extractArrayFromKeys($widget, ['ticker']),
        'clothing' => extractArrayFromKeys($widget, ['clothing']),
        'sourceCounts' => $widgetCounts,
    ];
}

function normalizeOfficialBadges(array $badges): array
{
    $out = [];
    foreach ($badges as $badge) {
        if (!is_array($badge)) {
            continue;
        }
        $code = firstString($badge, ['code', 'badgeCode']);
        if ($code === '') {
            continue;
        }
        $name = firstString($badge, ['name', 'title']);
        $description = firstString($badge, ['description', 'desc']);
        $imageUrl = firstString($badge, ['imageUrl', 'badgeUrl', 'url']);
        if ($imageUrl === '') {
            $imageUrl = 'https://images.habbo.com/c_images/album1584/'
                . rawurlencode($code) . '.png';
        }
        $out[] = mergeRecord([
            'code' => $code,
            'badgeCode' => $code,
            'name' => $name !== '' ? $name : $code,
            'title' => $name !== '' ? $name : $code,
            'description' => $description,
            'desc' => $description,
            'imageUrl' => $imageUrl,
            'badgeUrl' => $imageUrl,
            'selected' => true,
            'isAchievement' => str_starts_with(strtoupper($code), 'ACH_'),
            'isNft' => str_starts_with(strtoupper($code), 'NFT'),
            'rarity' => str_starts_with(strtoupper($code), 'NFT') ? 'nft' : 'generic',
            'source' => 'habbo-public-api',
        ], $badge);
    }
    return $out;
}

function normalizeOfficialFriends(array $friends): array
{
    $out = [];
    foreach ($friends as $friend) {
        if (!is_array($friend)) {
            continue;
        }
        $id = firstString($friend, ['uniqueId', 'id', 'habboId']);
        $name = firstString($friend, ['name', 'username', 'habboName']);
        if ($id === '' && $name === '') {
            continue;
        }
        $figure = firstString($friend, ['figureString', 'figure', 'look']);
        $out[] = mergeRecord([
            'uniqueId' => $id,
            'id' => $id,
            'habboId' => $id,
            'name' => $name,
            'username' => $name,
            'habboName' => $name,
            'figureString' => $figure,
            'figure' => $figure,
            'online' => firstBool($friend, ['online', 'isOnline'], false),
            'isOnline' => firstBool($friend, ['online', 'isOnline'], false),
            'source' => 'habbo-public-api',
        ], $friend);
    }
    return $out;
}

function normalizeOfficialRooms(array $rooms): array
{
    $out = [];
    foreach ($rooms as $room) {
        if (!is_array($room)) {
            continue;
        }
        $id = firstString($room, ['id', 'roomId', 'room_id']);
        $name = firstString($room, ['name', 'roomName', 'caption', 'title']);
        if ($id === '' && $name === '') {
            continue;
        }
        $description = firstString($room, ['description', 'desc']);
        $thumbnail = firstString($room, ['thumbnailUrl', 'imageUrl', 'url']);
        $out[] = mergeRecord([
            'id' => $id,
            'roomId' => $id,
            'room_id' => $id,
            'name' => $name,
            'roomName' => $name,
            'caption' => $name,
            'title' => $name,
            'description' => $description,
            'desc' => $description,
            'thumbnailUrl' => $thumbnail,
            'url' => $thumbnail,
            'source' => 'habbo-public-api',
        ], $room);
    }
    return $out;
}

function normalizeOfficialGroups(array $groups): array
{
    $out = [];
    foreach ($groups as $group) {
        if (!is_array($group)) {
            continue;
        }
        $id = firstString($group, ['id', 'groupId']);
        $name = firstString($group, ['name', 'groupName']);
        if ($id === '' && $name === '') {
            continue;
        }
        $badgeCode = firstString($group, ['badgeCode', 'code']);
        $badgeUrl = firstString($group, ['badgeUrl', 'imageUrl', 'url']);
        $out[] = mergeRecord([
            'id' => $id,
            'groupId' => $id,
            'name' => $name,
            'groupName' => $name,
            'badgeCode' => $badgeCode,
            'badgeUrl' => $badgeUrl,
            'imageUrl' => $badgeUrl,
            'source' => 'habbo-public-api',
        ], $group);
    }
    return $out;
}

function normalizeOfficialPhotos(array $photos): array
{
    $out = [];
    foreach ($photos as $photo) {
        if (!is_array($photo)) {
            continue;
        }
        $url = firstString($photo, ['previewUrl', 'url', 'imageUrl', 'photoUrl']);
        if ($url === '') {
            $url = findUrlDeep($photo);
        }
        $time = firstString($photo, ['creationTime', 'time', 'createdAt']);
        $out[] = mergeRecord([
            'previewUrl' => $url,
            'url' => $url,
            'imageUrl' => $url,
            'creationTime' => $time,
            'time' => $time,
            'source' => 'habbo-public-api',
        ], $photo);
    }
    return $out;
}

function findUrlDeep(mixed $value, int $depth = 0): string
{
    if ($depth > 6) {
        return '';
    }
    if (is_string($value) && preg_match('#^https://#i', $value)) {
        return $value;
    }
    if (!is_array($value)) {
        return '';
    }
    foreach ($value as $nested) {
        $url = findUrlDeep($nested, $depth + 1);
        if ($url !== '') {
            return $url;
        }
    }
    return '';
}

function extractArrayFromKeys(array $data, array $keys): array
{
    foreach ($keys as $key) {
        if (is_array($data[$key] ?? null)) {
            return array_values(array_filter($data[$key], 'is_array'));
        }
    }
    return [];
}

function firstString(array $data, array $keys): string
{
    foreach ($keys as $key) {
        if (!array_key_exists($key, $data) || is_array($data[$key]) || is_object($data[$key])) {
            continue;
        }
        $value = trim((string) $data[$key]);
        if ($value !== '' && strtolower($value) !== 'null') {
            return $value;
        }
    }
    return '';
}

function firstBool(array $data, array $keys, bool $fallback): bool
{
    return firstNullableBool($data, $keys) ?? $fallback;
}

function firstNullableBool(array $data, array $keys): ?bool
{
    foreach ($keys as $key) {
        if (!array_key_exists($key, $data)) {
            continue;
        }
        $value = $data[$key];
        if (is_bool($value)) {
            return $value;
        }
        if (is_int($value) || is_float($value)) {
            return $value !== 0;
        }
        if (is_string($value)) {
            $parsed = filter_var($value, FILTER_VALIDATE_BOOLEAN, FILTER_NULL_ON_FAILURE);
            if ($parsed !== null) {
                return $parsed;
            }
        }
    }
    return null;
}

function mergeRecord(array $primary, array $secondary): array
{
    foreach ($secondary as $key => $value) {
        if (!array_key_exists($key, $primary) || isMissingValue($primary[$key])) {
            $primary[$key] = $value;
            continue;
        }
        if (is_array($primary[$key]) && is_array($value) && !isListArray($primary[$key]) && !isListArray($value)) {
            $primary[$key] = mergeRecord($primary[$key], $value);
        }
    }
    return $primary;
}

function isMissingValue(mixed $value): bool
{
    return $value === null || $value === '' || $value === [];
}

function mergeLists(array $primary, array $secondary, array $keys = []): array
{
    $out = [];
    $positions = [];
    foreach ([$primary, $secondary] as $list) {
        foreach ($list as $item) {
            if (!is_array($item)) {
                continue;
            }
            $key = listItemKey($item, $keys);
            if ($key !== '' && isset($positions[$key])) {
                $index = $positions[$key];
                $out[$index] = mergeRecord($out[$index], $item);
                continue;
            }
            $out[] = $item;
            if ($key !== '') {
                $positions[$key] = array_key_last($out);
            }
        }
    }
    return array_values($out);
}

function mergeListsPreservePrimary(array $primary, array $secondary, array $keys): array
{
    $out = array_values(array_filter($primary, 'is_array'));
    $positions = [];
    foreach ($out as $index => $item) {
        $key = listItemKey($item, $keys);
        if ($key !== '' && !isset($positions[$key])) {
            $positions[$key] = $index;
        }
    }
    foreach ($secondary as $item) {
        if (!is_array($item)) {
            continue;
        }
        $key = listItemKey($item, $keys);
        if ($key !== '' && isset($positions[$key])) {
            $index = $positions[$key];
            $out[$index] = mergeRecord($out[$index], $item);
            continue;
        }
        $out[] = $item;
        if ($key !== '') {
            $positions[$key] = array_key_last($out);
        }
    }
    return array_values($out);
}

function mergeListsCompound(array $primary, array $secondary, array $keys): array
{
    $out = [];
    $positions = [];
    foreach ([$primary, $secondary] as $list) {
        foreach ($list as $item) {
            if (!is_array($item)) {
                continue;
            }
            $parts = [];
            foreach ($keys as $key) {
                $parts[] = normalizeKey(firstString($item, [$key]));
            }
            $compound = implode('|', $parts);
            if ($compound !== str_repeat('|', max(0, count($keys) - 1)) && isset($positions[$compound])) {
                $index = $positions[$compound];
                $out[$index] = mergeRecord($out[$index], $item);
                continue;
            }
            $out[] = $item;
            if ($compound !== str_repeat('|', max(0, count($keys) - 1))) {
                $positions[$compound] = array_key_last($out);
            }
        }
    }
    return array_values($out);
}

/**
 * Completa somente os registros que já existem na lista principal.
 *
 * A lista oficial de amigos é autoritativa: um registro histórico ausente
 * nela não pode ser recolocado como amigo atual.
 */
function enrichPrimaryList(array $primary, array $supplement, array $keys): array
{
    $out = array_values(array_filter($primary, 'is_array'));
    $positions = [];

    foreach ($out as $index => $item) {
        $identity = listItemKey($item, $keys);
        if ($identity !== '' && !isset($positions[$identity])) {
            $positions[$identity] = $index;
        }
    }

    foreach ($supplement as $item) {
        if (!is_array($item)) {
            continue;
        }
        $identity = listItemKey($item, $keys);
        if ($identity !== '' && isset($positions[$identity])) {
            $index = $positions[$identity];
            $out[$index] = mergeRecord($out[$index], $item);
        }
    }

    return array_values($out);
}

/**
 * Remove repetições do mesmo visual observadas no mesmo minuto.
 *
 * A fonte histórica pode publicar o mesmo evento em blocos diferentes (lista
 * de visuais e ticker) e representar o instante com formatos ou segundos
 * distintos. Como a interface exibe data e hora até os minutos, esses registros
 * representam a mesma ocorrência para o usuário.
 */
function deduplicatePreviousStyles(array $items): array
{
    $out = [];
    $positions = [];

    foreach ($items as $item) {
        if (!is_array($item)) {
            continue;
        }

        $figure = firstString($item, ['figureString', 'figure', 'look']);
        if ($figure === '') {
            continue;
        }

        $date = firstString($item, ['changedAt', 'date']);
        $dateKey = previousStyleMinuteKey($date);

        // Sem uma data não é seguro concluir que duas aparições são o mesmo evento.
        if ($dateKey === '') {
            $out[] = $item;
            continue;
        }

        $key = normalizeKey($figure) . '|' . $dateKey;
        if (isset($positions[$key])) {
            $index = $positions[$key];
            $out[$index] = mergeRecord($out[$index], $item);
            continue;
        }

        $out[] = $item;
        $positions[$key] = array_key_last($out);
    }

    return array_values($out);
}

function previousStyleMinuteKey(string $date): string
{
    $date = trim($date);
    if ($date === '') {
        return '';
    }

    $timestamp = strtotime($date);
    if ($timestamp !== false) {
        return gmdate('Y-m-d\\TH:i', $timestamp);
    }

    return normalizeKey($date);
}

function listItemKey(array $item, array $keys): string
{
    if ($keys === []) {
        $keys = [
            'uniqueId', 'id', 'code', 'badgeCode', 'figureString',
            'name', 'text', 'url',
        ];
    }
    foreach ($keys as $key) {
        $value = firstString($item, [$key]);
        if ($value !== '') {
            return strtolower($key . ':' . normalizeKey($value));
        }
    }
    return '';
}

function normalizeKey(string $value): string
{
    return strtolower(trim(removeAccents($value)));
}

function isListArray(array $array): bool
{
    if ($array === []) {
        return true;
    }
    return array_keys($array) === range(0, count($array) - 1);
}

function updateObservedHistory(array $profile, array $reliability): array
{
    // Preserva somente o histórico recebido na consulta atual.
    return $profile;
}

function filterHabbowidgetsRecords(array $items): array
{
    return array_values(array_filter(
        $items,
        static function (mixed $item): bool {
            if (!is_array($item)) {
                return false;
            }
            $source = strtolower(firstString($item, ['source']));
            return $source === ''
                || $source === 'toxic-history'
                || str_contains($source, 'habbowidgets');
        }
    ));
}

function mergeHistoricalLists(string $listKey, array $primary, array $secondary): array
{
    $out = [];
    $positions = [];
    foreach ([$primary, $secondary] as $list) {
        foreach ($list as $item) {
            if (!is_array($item)) {
                continue;
            }
            $key = historicalItemKey($listKey, $item);
            if ($key !== '' && isset($positions[$key])) {
                $index = $positions[$key];
                $out[$index] = mergeRecord($out[$index], $item);
                continue;
            }
            $out[] = $item;
            if ($key !== '') {
                $positions[$key] = array_key_last($out);
            }
        }
    }
    return array_values($out);
}

function historicalItemKey(string $listKey, array $item): string
{
    $identity = '';
    $date = '';
    if ($listKey === 'previousNames') {
        $identity = firstString($item, ['name', 'oldName', 'username']);
        $date = firstString($item, ['changedAt', 'date']);
    } elseif ($listKey === 'previousMottos') {
        $identity = firstString($item, ['text', 'motto']);
        $date = firstString($item, ['changedAt', 'date']);
    } elseif ($listKey === 'previousStyles') {
        $identity = firstString($item, ['figureString', 'figure', 'look']);
        $date = firstString($item, ['changedAt', 'date']);
    } elseif ($listKey === 'previousFriends') {
        $identity = firstString($item, ['uniqueId', 'id', 'name']);
        $date = firstString($item, ['removedAt', 'date']);
    } elseif ($listKey === 'previousRooms') {
        $identity = firstString($item, ['id', 'roomId', 'name']);
        $date = firstString($item, ['removedAt', 'date']);
    } elseif ($listKey === 'previousBadges') {
        $identity = firstString($item, ['code', 'badgeCode', 'name']);
        $date = firstString($item, ['removedAt', 'date']);
    } elseif ($listKey === 'previousGroups') {
        $identity = firstString($item, ['id', 'groupId', 'badgeCode', 'name']);
        $date = firstString($item, ['leftAt', 'removedAt', 'date']);
    }
    if ($identity === '') {
        return '';
    }
    return normalizeKey($identity) . '|' . normalizeKey($date);
}

function mapObservedRecords(array $items, array $keys): array
{
    $mapped = [];
    foreach ($items as $item) {
        if (!is_array($item)) {
            continue;
        }
        $key = '';
        foreach ($keys as $candidate) {
            $value = firstString($item, [$candidate]);
            if ($value !== '') {
                $key = normalizeKey($candidate . ':' . $value);
                break;
            }
        }
        if ($key !== '') {
            $mapped[$key] = $item;
        }
    }
    return $mapped;
}

function sortProfileLists(array &$profile): void
{
    if (is_array($profile['previousStyles'] ?? null)) {
        $profile['previousStyles'] = deduplicatePreviousStyles($profile['previousStyles']);
    }

    $dateKeys = [
        'previousNames' => ['changedAt', 'date'],
        'previousMottos' => ['changedAt', 'date'],
        'previousStyles' => ['changedAt', 'date'],
        'friends' => ['creationTime', 'friendSince', 'addedAt', 'createdAt', 'date'],
        'previousFriends' => [
            'removedAt', 'leftAt', 'date', 'creationTime', 'friendSince', 'addedAt', 'createdAt'
        ],
        'rooms' => ['creationTime', 'createdAt', 'date', 'updatedAt'],
        'previousRooms' => ['removedAt', 'date'],
        'previousBadges' => ['removedAt', 'date'],
        'previousGroups' => ['leftAt', 'removedAt', 'date'],
        'badges' => ['creationTime', 'createdAt', 'date'],
    ];
    foreach ($dateKeys as $listKey => $keys) {
        if (!is_array($profile[$listKey] ?? null)) {
            continue;
        }
        usort($profile[$listKey], static function (array $a, array $b) use ($keys): int {
            $aDate = firstString($a, $keys);
            $bDate = firstString($b, $keys);
            $aTime = strtotime($aDate) ?: 0;
            $bTime = strtotime($bDate) ?: 0;
            return $bTime <=> $aTime;
        });
    }
}

function closetCacheFile(string $hotel, string $code): string
{
    return CACHE_ROOT . '/closet/' . hash(
        'sha256',
        normalizeHotel($hotel) . '|' . strtolower($code)
    ) . '.json';
}

function saveClosetMetadata(string $hotel, array $item): void
{
    // API stateless: metadados do visual não são salvos.
}

function readClosetMetadata(string $hotel, string $code): ?array
{
    return null;
}

function clothingFromFigure(string $figure, string $hotel): array
{
    $hotel = normalizeHotel($hotel);
    $parts = explode('.', $figure);
    $items = [];
    $allFromCache = true;

    foreach ($parts as $part) {
        if (!preg_match('/^([a-z]{2})-(\d+)/i', $part, $match)) {
            continue;
        }
        $slot = strtolower($match[1]);
        $code = $slot . '-' . $match[2];
        $classification = habbonewsClothingClassification($code);
        // Peças padrão são descartadas pelo código antes mesmo da consulta do
        // nome. Isso impede que "hd-600", "ch-240" etc. reapareçam na resposta.
        if ((bool) ($classification['hidden'] ?? false)) {
            continue;
        }
        $cached = readClosetMetadata($hotel, $code);
        if ($cached !== null) {
            $cached['_slot'] = $slot;
            $cached['slot'] = $slot;
            $items[$slot] = $cached;
            continue;
        }

        $allFromCache = false;
        try {
            $items[$slot] = fetchClosetMetadata($hotel, $code, $slot);
            saveClosetMetadata($hotel, $items[$slot]);
        } catch (Throwable $error) {
            $items[$slot] = clothingRecord(
                $code,
                $code,
                '',
                $slot,
                $hotel,
                HABBOWIDGETS_BASE . '/habbo/closet/'
                    . rawurlencode((string) hotelConfig($hotel)['widget'])
                    . '/' . rawurlencode($code),
                '',
                $classification
            );
        }
    }

    $namedItems = array_filter(
        $items,
        static fn (array $item): bool => isCompleteClothingName(
            (string) ($item['name'] ?? ''),
            (string) ($item['code'] ?? ''),
            (string) ($item['category'] ?? $item['lineCode'] ?? '')
        )
    );
    $result = array_values($namedItems);
    // As chaves por slot também precisam conter somente peças com nome público.
    // O app prioriza essas chaves antes de "result"; manter códigos técnicos
    // aqui fazia roupas padrão reaparecerem mesmo com "result" filtrado.
    $payload = $namedItems;
    $payload['result'] = $result;
    $payload['items'] = $result;
    $payload['total'] = count($result);
    $payload['meta'] = [
        'provider' => 'toxic',
        'figureString' => $figure,
        'hotel' => $hotel,
        'rarityIndex' => habbonewsRarityPublicMeta(),
        'note' => 'Nomes localizados: HabboWidgets. Ícone e filtro de peças padrão: índice tipo-ID do HabboNews.',
    ];
    return [$payload, $allFromCache];
}

function fetchClosetMetadata(string $hotel, string $code, string $slot): array
{
    $config = hotelConfig($hotel);
    $url = HABBOWIDGETS_BASE . '/habbo/closet/'
        . rawurlencode((string) $config['widget'])
        . '/' . rawurlencode($code);
    $response = cachedHttpRequest(
        'GET',
        $url,
        [],
        CLOSET_CACHE_TTL,
        MAX_HTML_BYTES,
        (string) $config['language']
    );
    return parseClosetMetadataHtml(
        (string) $response['body'],
        $hotel,
        $code,
        $slot,
        (string) ($response['effectiveUrl'] ?? $url)
    );
}

function parseClosetMetadataHtml(
    string $html,
    string $hotel,
    string $code,
    string $slot,
    string $closetUrl
): array {
    $document = new DOMDocument('1.0', 'UTF-8');
    $previous = libxml_use_internal_errors(true);
    $document->loadHTML(
        '<?xml encoding="UTF-8">' . $html,
        LIBXML_NONET | LIBXML_NOERROR | LIBXML_NOWARNING
    );
    libxml_clear_errors();
    libxml_use_internal_errors($previous);
    $xpath = new DOMXPath($document);
    $name = nodeText(firstXpathNode(
        $xpath,
        '//*[contains(concat(" ", normalize-space(@class), " "), " text-center ")]/h4[1]'
    ));
    $pageTitle = nodeText(firstXpathNode($xpath, '//title[1]'));
    if ($name === '') {
        $name = preg_replace('/\s+-\s+Habbo Closet.*$/i', '', $pageTitle) ?: $code;
    }
    if (preg_match('/^' . preg_quote($code, '/') . '\s+from\s+/i', $name)) {
        $name = $code;
    }
    $name = sanitizeClothingName($name, $code);
    $category = '';
    if (preg_match('/\bfrom\s+(.+?)\s+-\s+Habbo Closet/i', $pageTitle, $match)) {
        $category = normalizeText($match[1]);
    }
    $scientificName = extractClothingScientificName($html);
    $classification = habbonewsClothingClassification($code);
    return clothingRecord(
        $code,
        $name,
        $category,
        $slot,
        $hotel,
        $closetUrl,
        $scientificName,
        $classification
    );
}

function suggestProfiles(string $query, string $hotel): array
{
    $items = [];

    try {
        $official = fetchOfficialUserByName($query, hotelConfig($hotel));
        if (is_array($official)) {
            $suggestion = suggestionFromProfile($official);
            if (firstString($suggestion, ['uniqueId', 'name']) !== '') {
                $items[] = $suggestion;
            }
        }
    } catch (Throwable $ignored) {
    }

    // A sugestão é intencionalmente oficial e leve; o histórico só é carregado
    // depois que o usuário abre um perfil.
    return [$items, false];
}

function suggestionFromProfile(array $profile): array
{
    return [
        'uniqueId' => firstString($profile, ['uniqueId', 'id']),
        'id' => firstString($profile, ['uniqueId', 'id']),
        'name' => firstString($profile, ['name', 'username']),
        'username' => firstString($profile, ['name', 'username']),
        'figureString' => firstString($profile, ['figureString', 'figure']),
        'figure' => firstString($profile, ['figureString', 'figure']),
        'motto' => firstString($profile, ['motto', 'mission']),
        'online' => firstBool($profile, ['online', 'isOnline'], false),
        'profileVisible' => firstBool(
            $profile,
            ['profileVisible', 'isProfileVisible', 'visible'],
            true
        ),
        'previousNames' => extractArrayFromKeys($profile, ['previousNames']),
    ];
}

function readJsonFile(string $file): ?array
{
    if (!is_file($file)) {
        return null;
    }
    $body = @file_get_contents($file);
    if (!is_string($body) || $body === '') {
        return null;
    }
    $decoded = json_decode($body, true);
    return is_array($decoded) ? $decoded : null;
}

function writeJsonAtomic(string $file, array $value): void
{
    // API stateless: nenhuma gravação em arquivo é permitida.
}

function writeFileAtomic(string $file, string $contents): void
{
    // API stateless: nenhuma gravação em arquivo é permitida.
}

if (!defined('TOXIC_API_NO_MAIN')) {
    main();
}
