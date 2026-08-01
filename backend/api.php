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
 * Requisitos: PHP 8.0+, cURL e DOM. Não precisa de permissão de escrita.
 * Dados atuais vêm da API pública oficial; a fonte histórica é usada somente
 * como complemento. Datas históricas são datas de detecção.
 */

const TOXIC_API_VERSION = '1.3.3';
const TOXIC_USER_AGENT = 'ToxicSearchTool/1.3.3 (+https://atoxic.com.br)';
const HABBOWIDGETS_BASE = 'https://www.habbowidgets.com';
const HABBOWIDGETS_KLD1_ICON = HABBOWIDGETS_BASE . '/images/kld1.gif';
const HABBOWIDGETS_KLD2_ICON = HABBOWIDGETS_BASE . '/images/kld2.gif';
const CACHE_ROOT = __DIR__ . '/cache/habbowidgets_api';
// A API opera de forma totalmente stateless. Não altere para true em hospedagens
// com pouco espaço: nenhum perfil, histórico, resposta ou lock é persistido.
const ENABLE_API_CACHE = false;
const PROFILE_CACHE_TTL = 600;
const HTTP_CACHE_TTL = 600;
const HTTP_STALE_TTL = 2_592_000;
const PROFILE_STALE_TTL = 7_776_000;
const CLOSET_CACHE_TTL = 2_592_000;
const MAX_HTML_BYTES = 24_000_000;
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
        if (preg_match('#^rarity-icon/(?:level/)?(\d{1,2})$#i', $path, $iconMatch)) {
            sendRarityIcon((int) $iconMatch[1]);
        }
        if (preg_match('#^rarity-icon/(generic|rare|nft)$#i', $path, $iconMatch)) {
            sendRarityIcon(strtolower($iconMatch[1]));
        }
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
            if (in_array($normalizedKey, ['isbanned', 'banned', 'ban', 'is_banned'], true)) {
                continue;
            }
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

function sendRarityIcon(string|int $rarity): void
{
    $level = is_int($rarity)
        ? $rarity
        : rarityLevelFromLegacy((string) $rarity);
    $level = max(1, min(14, $level));
    $encoded = rarityIconPngBase64($level);
    if ($encoded === '') {
        throw new ApiProblem(404, 'rarity_icon_not_found', 'Ícone de raridade não encontrado.');
    }
    $binary = base64_decode($encoded, true);
    if (!is_string($binary)) {
        throw new ApiProblem(500, 'rarity_icon_invalid', 'Ícone de raridade inválido.');
    }

    header_remove('Content-Type');
    header('Content-Type: image/png');
    header('Cache-Control: public, max-age=604800, immutable');
    header('Content-Length: ' . strlen($binary));
    echo $binary;
    exit;
}

function rarityIconPngBase64(int $level): string
{
    // Cores alinhadas à legenda de cabides usada por fã-sites. Alguns níveis
    // compartilham o mesmo cabide e são diferenciados pelo nível e tooltip.
    $icons = [
        1 => 'iVBORw0KGgoAAAANSUhEUgAAABEAAAAPCAYAAAACsSQRAAAAwUlEQVR42p2TwQ3DIAxFTVQ1e7QHRsgAVRboGBmoY7AAygCMkEOyB7m4hwJ1iA0olnIx5ufxPyg0DrhS7wHzHhqnuNmuVaDYz0noIG7Tv//4iEQsCRXY1/kk2HScWPs6Qz8uUKubtEDxKVGVhE0kHIMS5XOdtOCtBtymROCtFgNQaNyh4a2G+/N18ITr0aQUACCXCGcoFaOeJZEokBtaij/OJhH6l5aitMnYflyKMRbvT7j2ePVD434k0uusehP2fQHxBn4QmxntRgAAAABJRU5ErkJggg==',
        2 => 'iVBORw0KGgoAAAANSUhEUgAAABEAAAAPCAYAAAACsSQRAAAAvUlEQVR42pVTuxHDIAwVviyQXlu4SJsNdM6gcN7ALQVb0DOCUsTidPzz7lRYyI/He2DYemjBfF5c9th605rdVgmG/VKJHkxEuf88z66iphJNcIVQES4dR3CFAEeMMMOjt6Dla0VTJS3j5BhaUTm39RYcIiSirMAh9pO602Eph8iJiBMRO8RuT4qtB3N/VIm0DHWI8N73yrNMIgSloaP4ZTaT6F1WoNVmY48YhzEO709p7L/F1v+U9F7nDPLfF/u0h6Qbz1rBAAAAAElFTkSuQmCC',
        3 => 'iVBORw0KGgoAAAANSUhEUgAAABEAAAAPCAYAAAACsSQRAAAAwUlEQVR42p2TwQ3DIAxFTVQ1e7QHRsgAVRboGBmoY7AAygCMkEOyB7m4hwJ1iA0olnIx5ufxPyg0DrhS7wHzHhqnuNmuVaDYz0noIG7Tv//4iEQsCRXY1/kk2HScWPs6Qz8uUKubtEDxKVGVhE0kHIMS5XOdtOCtBtymROCtFgNQaNyh4a2G+/N18ITr0aQUACCXCGcoFaOeJZEokBtaij/OJhH6l5aitMnYflyKMRbvT7j2ePVD434k0uusehP2fQHxBn4QmxntRgAAAABJRU5ErkJggg==',
        4 => 'iVBORw0KGgoAAAANSUhEUgAAABEAAAAPCAYAAAACsSQRAAAAvUlEQVR42pVTuxHDIAwVviyQXlu4SJsNdM6gcN7ALQVb0DOCUsTidPzz7lRYyI/He2DYemjBfF5c9th605rdVgmG/VKJHkxEuf88z66iphJNcIVQES4dR3CFAEeMMMOjt6Dla0VTJS3j5BhaUTm39RYcIiSirMAh9pO602Eph8iJiBMRO8RuT4qtB3N/VIm0DHWI8N73yrNMIgSloaP4ZTaT6F1WoNVmY48YhzEO709p7L/F1v+U9F7nDPLfF/u0h6Qbz1rBAAAAAElFTkSuQmCC',
        5 => 'iVBORw0KGgoAAAANSUhEUgAAABEAAAAPCAYAAAACsSQRAAAAwUlEQVR42pVTQQ6EIAwsxj+t4WL0CX6EhIds4kf6BIwXI6/qHta6DVBwm/RAaSfDTDGEEUphlhelNcJoSr3dU4BqPWUiG9H/Zpa3URkVmUiA/dwywEfP4djPDdZjhlb02oWkLxk1mZSE42dIRmlfp104GwA93QycDbpTlzvE6Wwg9EToiZwNao2TMIK5DpkjJUGdDTAOU6bZDcIAqaA1+7m3l+qPw1TdB82tW9j1mKs2VvcnFfbfJIxfJtrvbAXPfQDa+4yhxAKkSQAAAABJRU5ErkJggg==',
        6 => 'iVBORw0KGgoAAAANSUhEUgAAABEAAAAPCAYAAAACsSQRAAAAwUlEQVR42pVTQQ6EIAwsxj+t4WL0CX6EhIds4kf6BIwXI6/qHta6DVBwm/RAaSfDTDGEEUphlhelNcJoSr3dU4BqPWUiG9H/Zpa3URkVmUiA/dwywEfP4djPDdZjhlb02oWkLxk1mZSE42dIRmlfp104GwA93QycDbpTlzvE6Wwg9EToiZwNao2TMIK5DpkjJUGdDTAOU6bZDcIAqaA1+7m3l+qPw1TdB82tW9j1mKs2VvcnFfbfJIxfJtrvbAXPfQDa+4yhxAKkSQAAAABJRU5ErkJggg==',
        7 => 'iVBORw0KGgoAAAANSUhEUgAAABEAAAAPCAYAAAACsSQRAAAAwklEQVR42p1TMQ7DIAw0Ub6SJcyon2DKmCmvyCP6ik6MTPkEYk6WPMYdWpBDbYhqieUwp/OdUegDcKWmB5YY+qC43u4uQRUvldDG5Vwz/hqeoiJWCSWI8fghvDVOqhgP2OcNWtVLF1Q+VdRUwhmXxqCKyr5OutDOwnKuWYF2VgxAoQ8XQDsLxowXTziMJqUAALlEOEMpGfUskySC0tBa/Km3p+4bM1b3QUorG7vPWzXG6v581x7/PejDR4n0O1uV3r0BUXZ8+3RqTGMAAAAASUVORK5CYII=',
        8 => 'iVBORw0KGgoAAAANSUhEUgAAABEAAAAPCAYAAAACsSQRAAAAwElEQVR42pVTsQ3DIBAEy9OwQNooTpM2g9Ayg1sPQktjLLcs4HUuRQC/CA/OSy9Zx/l9f4clbBC1ku8bSgw2yBp3uDqgiZdKKBHanfjyYhVVldAB27r/DLy0Tqpt3cV0zKJXI3dA5VNFXSXVROIaVFHJG7gDr4yAdlmBV4ZPKqaD1F4ZQDtAO3hlWCx1SvcEGGLtAxSX8SHvXhraij9xR+r+43lv3gcurWzsdMzNGJv3pzT234YNXyXc39n1Jr73AQdPnpCnfIlyAAAAAElFTkSuQmCC',
        9 => 'iVBORw0KGgoAAAANSUhEUgAAABEAAAAPCAYAAAACsSQRAAAAv0lEQVR42pVT0QnDIBDVkAUcpnQD6UcDpVM4UqaQgPmQbiAZJiO8fjRnD+tpenAgz/N8955q+KRqoZ9XlBh80rXa4WyDJl4y4YU2fs+8blpkVGXCG2zL+tPw1DgU27KqfZ5UL0Zpg9PnjLpMasLRGJxRWTdIG8YFZSMyA+OC7NThDiiNC7ARsBEwLogYJbmbAamwdgHH9bHIs5eCtuyn2pGrf3ncm+9BcisLu89T08bm+ymF/Tfh04eJ9Dt7QefelkSbp3PJpcIAAAAASUVORK5CYII=',
        10 => 'iVBORw0KGgoAAAANSUhEUgAAABEAAAAPCAYAAAACsSQRAAAAvklEQVR42pVTMQ7DIAw0KBPfSaRO3Sqx9zN5A59hr5QtExL+TlZ3KFBKsEMtWUqOwzmfHUU+QC/U80YtRj6oHlePFpBwLREJbUmpUFdJLgAAgNv+8z7cTg7cdljWA65i4g7U/DoVHFLSnUhqo1bU8jR3EJ0BQlsURGf4SaU9oZzRGSK0RGgpOsNiOfOefQGG2PtAjav0UHpvDZXGn7lT7f78uIv7wE2rGLushzhGcX9aY/9N8uGjhPs7L71J997ASJ4cvt4KngAAAABJRU5ErkJggg==',
        11 => 'iVBORw0KGgoAAAANSUhEUgAAABEAAAAPCAYAAAACsSQRAAAAxUlEQVR42p2Tyw3EIAxEh5BWqCE9+GIpTaSNbYMmkLjQAyWkGu9hF+QlkM9a4jI4w2NMjISMXpl1kVaTkE2vd7prcKq3JLrRe1/1bduGRF0SbZBSOhjeuk6plBJijLiqebSh8TXRJUkvuHINTdT2TaMNZob3vhIw83AAcyswM4jokAkRgYh+NLMuIiEbA0B6E+kFqg/QmVWTYtAGejb+0ltN9Cl3StNaAC8A2Pcd1lo45x4ZlCeML81fS0L+jHj0d15V+e4NAfx6IlyulGUAAAAASUVORK5CYII=',
        12 => 'iVBORw0KGgoAAAANSUhEUgAAABEAAAAPCAYAAAACsSQRAAAAv0lEQVR42pVT0QnDIBDVkAUcpnQD6UcDpVM4UqaQgPmQbiAZJiO8fjRnD+tpenAgz/N8955q+KRqoZ9XlBh80rXa4WyDJl4y4YU2fs+8blpkVGXCG2zL+tPw1DgU27KqfZ5UL0Zpg9PnjLpMasLRGJxRWTdIG8YFZSMyA+OC7NThDiiNC7ARsBEwLogYJbmbAamwdgHH9bHIs5eCtuyn2pGrf3ncm+9BcisLu89T08bm+ymF/Tfh04eJ9Dt7QefelkSbp3PJpcIAAAAASUVORK5CYII=',
        13 => 'iVBORw0KGgoAAAANSUhEUgAAABEAAAAPCAYAAAACsSQRAAAAvUlEQVR42pVTuxHDIAwVviyQXlu4SJsNdM6gcN7ALQVb0DOCUsTidPzz7lRYyI/He2DYemjBfF5c9th605rdVgmG/VKJHkxEuf88z66iphJNcIVQES4dR3CFAEeMMMOjt6Dla0VTJS3j5BhaUTm39RYcIiSirMAh9pO602Eph8iJiBMRO8RuT4qtB3N/VIm0DHWI8N73yrNMIgSloaP4ZTaT6F1WoNVmY48YhzEO709p7L/F1v+U9F7nDPLfF/u0h6Qbz1rBAAAAAElFTkSuQmCC',
        14 => 'iVBORw0KGgoAAAANSUhEUgAAABEAAAAPCAYAAAACsSQRAAAAwElEQVR42p2TsRHDIBAED9mlkH7sEshdCwW4AGohpwRiulBKATiwYV74Ecg3o+R5nZa7kSo+QpJ6Pko/Kz4qaXdbNTid9yR80Vrb5s65IZFIwg1CCD+GS9epCiEgpYSZ7qMDjs+JpiRScPUanKjf20YHRARrbSMgomEBqvh4GBARjDGHTKQZb0oBKFIjUqDcjGfWTKpBH+hZ/XW3mfCvrIjT3gC8AGDfd+ScobW+ZFCDwZfmr6f4+Kl49HfOVN97A9vJfWF5JMoRAAAAAElFTkSuQmCC',
    ];
    return (string) ($icons[$level] ?? '');
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

    if ($path === '') {
        $path = legacyPathFromQuery($params);
    }

    return [$path, $params, $gatewayMode];
}

function legacyPathFromQuery(array $params): string
{
    $endpoint = trim((string) ($params['endpoint'] ?? ''));
    $name = trim((string) ($params['name'] ?? ''));
    $uniqueId = trim((string) ($params['uniqueId'] ?? ''));

    if (isset($params['rarityIconLevel'])) {
        return 'rarity-icon/level/' . (int) $params['rarityIconLevel'];
    }

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
            'Referer: ' . HABBOWIDGETS_BASE . '/',
            'X-Toxic-App: 1.3.0',
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
    $privateProfile = (bool) preg_match(
        '/perfil fechado|private profile|closed profile|privates profil|profil fermé|perfil cerrado|profiel gesloten|profilo chiuso|gizli profil|yksityinen profiili/i',
        $summaryText
    );
    if (!$privateProfile) {
        $privateProfile = firstXpathNode(
            $xpath,
            './/a[contains(concat(" ", normalize-space(@class), " "), " btn-warning ")][.//*[contains(concat(" ", normalize-space(@class), " "), " glyphicon-lock ")]]',
            $summary
        ) instanceof DOMNode;
    }
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
    $image = firstXpathNode($xpath, './/img[1]', $node);
    $headUrl = imageUrlFromNode($image);
    $date = nodeAttribute(firstXpathNode($xpath, './/time[@datetime][1]', $node), 'datetime');
    if ($date === '') {
        foreach ($node->childNodes as $child) {
            if ($child->nodeType !== XML_TEXT_NODE) {
                continue;
            }
            $candidate = normalizeText((string) $child->textContent);
            if ($candidate !== '' && preg_match('/\d{4}|\d{1,2}[\/.-]\d{1,2}/u', $candidate)) {
                $date = $candidate;
                break;
            }
        }
    }
    $date = normalizeDate($date);
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
        'datePrecision' => 'observed',
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
        $rarityNode = firstXpathNode(
            $xpath,
            './/img[contains(translate(@src, "KLD", "kld"), "kld1.gif") or contains(translate(@src, "KLD", "kld"), "kld2.gif")][1]',
            $row
        );
        $icon = imageUrlFromNode($rarityNode);
        $rowContext = nodeText($row);
        $scientificName = extractClothingScientificName($rowContext . ' ' . $href);
        $rarityDetails = clothingRarityProfile($code, $name, $icon, $rowContext . ' ' . $scientificName);
        $slot = strtolower((string) strtok($code, '-'));
        $items[$slot] = clothingRecord(
            $code,
            $name,
            $category,
            $slot,
            (string) $rarityDetails['legacy'],
            $icon,
            (string) $config['key'],
            absoluteHabbowidgetsUrl($href),
            $rarityDetails,
            $scientificName
        );
    }
    return $items;
}

function clothingRecord(
    string $code,
    string $name,
    string $category,
    string $slot,
    string $rarity,
    string $iconUrl,
    string $hotel,
    string $closetUrl = '',
    array $rarityDetails = [],
    string $scientificName = ''
): array {
    $name = sanitizeClothingName($name, $code);
    $category = trim($category);
    if ($category === '') {
        $category = clothingSlotName($slot, $hotel);
    }

    if ($rarityDetails === []) {
        $rarityDetails = clothingRarityProfile(
            $code,
            $name,
            $iconUrl,
            $scientificName . ' ' . $category
        );
    }
    $level = (int) ($rarityDetails['level'] ?? 0);
    $legacyRarity = (string) ($rarityDetails['legacy'] ?? $rarity);
    $resolvedIconUrl = $level > 0 ? rarityIconUrlByLevel($level) : '';

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
        'rarity' => $legacyRarity,
        'rarityType' => (string) ($rarityDetails['key'] ?? ''),
        'rarityLevel' => $level,
        'rarityKey' => (string) ($rarityDetails['key'] ?? ''),
        'rarityLabel' => (string) ($rarityDetails['label'] ?? ''),
        'rarityDescription' => (string) ($rarityDetails['description'] ?? ''),
        'raritySource' => (string) ($rarityDetails['source'] ?? ''),
        'rarityConfidence' => (string) ($rarityDetails['confidence'] ?? ''),
        'isRare' => in_array($level, [2, 4, 8, 13], true),
        'isNft' => $level === 13,
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

function isCompleteClothingName(string $name, string $code): bool
{
    $name = sanitizeClothingName($name, $code);
    $nameIdentity = preg_replace('/[^a-z0-9]+/', '', strtolower(removeAccents($name))) ?? '';
    $codeIdentity = preg_replace('/[^a-z0-9]+/', '', strtolower(removeAccents($code))) ?? '';
    if ($name === '' || ($codeIdentity !== '' && $nameIdentity === $codeIdentity)) {
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
    if (preg_match('/^' . $slots . '_?\d+(?:_\d+)*(?:_name)?$/', $technical) === 1) {
        return true;
    }
    if (preg_match('/^(?:nft|kld)_?\d+(?:_\d+)*(?:_name)?$/', $technical) === 1) {
        return true;
    }
    if (preg_match('/^(?:clothing|figure|avatar|look|furni)(?:_[a-z0-9]+)+$/', $technical) === 1) {
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

function rarityLevelDefinitions(): array
{
    return [
        1 => ['key' => 'catalog_permanent', 'label' => 'Permanente no catálogo', 'description' => 'Disponível permanentemente no catálogo.'],
        2 => ['key' => 'activity_rare', 'label' => 'Raro de atividade', 'description' => 'Entregue em atividade e não vendido no catálogo.'],
        3 => ['key' => 'normal_returnable', 'label' => 'Normal', 'description' => 'Peça normal que pode retornar ao catálogo.'],
        4 => ['key' => 'rare', 'label' => 'Raro', 'description' => 'Peça rara, normalmente sem retorno à Habbo Loja.'],
        5 => ['key' => 'pack_exclusive', 'label' => 'Exclusivo de pack', 'description' => 'Disponibilizado exclusivamente em um pack.'],
        6 => ['key' => 'offer_exclusive', 'label' => 'Exclusivo de oferta', 'description' => 'Disponibilizado exclusivamente em uma oferta.'],
        7 => ['key' => 'clickable_normal', 'label' => 'Mobi clicável', 'description' => 'Obtido por meio de um mobi clicável normal.'],
        8 => ['key' => 'clickable_rare', 'label' => 'Mobi clicável raro', 'description' => 'Obtido por meio de um mobi clicável raro.'],
        9 => ['key' => 'crafting_recipe', 'label' => 'Mesa de criações', 'description' => 'Obtido por receita de uma mesa de criações.'],
        10 => ['key' => 'hc_gift', 'label' => 'Presente HC', 'description' => 'Presente de assinatura Habbo Club de anos anteriores.'],
        11 => ['key' => 'unreleased', 'label' => 'Nunca lançado', 'description' => 'Nunca foi disponibilizado ou vendido no jogo.'],
        12 => ['key' => 'activity_promotion', 'label' => 'Atividade ou promoção', 'description' => 'Entregue em atividade ou promoção oficial.'],
        13 => ['key' => 'collectible', 'label' => 'Colecionável', 'description' => 'Exclusivo de uma edição colecionável, LTD ou NFT.'],
        14 => ['key' => 'unavailable', 'label' => 'Indisponível', 'description' => 'Classificado como indisponível.'],
    ];
}

function rarityLevelFromLegacy(string $rarity): int
{
    $rarity = strtolower(trim($rarity));
    if (str_contains($rarity, 'nft') || str_contains($rarity, 'collect')) {
        return 13;
    }
    if (str_contains($rarity, 'rare') || str_contains($rarity, 'raro')) {
        return 4;
    }
    return 3;
}

function legacyRarityForLevel(int $level): string
{
    if ($level === 13) {
        return 'nft';
    }
    if (in_array($level, [2, 4, 8], true)) {
        return 'rare';
    }
    return 'generic';
}

function publicApiBaseUrl(): string
{
    $forwardedProto = trim((string) ($_SERVER['HTTP_X_FORWARDED_PROTO'] ?? ''));
    $scheme = $forwardedProto !== ''
        ? strtolower((string) strtok($forwardedProto, ','))
        : ((!empty($_SERVER['HTTPS']) && $_SERVER['HTTPS'] !== 'off') ? 'https' : 'http');
    $host = trim((string) ($_SERVER['HTTP_HOST'] ?? ''));
    $script = (string) ($_SERVER['SCRIPT_NAME'] ?? '/api.php');
    if (preg_match('#^(.*?\.php)(?:/.*)?$#i', $script, $match) === 1) {
        $script = $match[1];
    }
    if ($host === '') {
        return '/api.php';
    }
    return $scheme . '://' . $host . $script;
}

function rarityIconUrlByLevel(int $level): string
{
    if ($level < 1 || $level > 14) {
        return '';
    }
    return rtrim(publicApiBaseUrl(), '/') . '?rarityIconLevel=' . $level;
}

function clothingRarity(string $code, string $name, string $iconUrl): string
{
    return (string) clothingRarityProfile($code, $name, $iconUrl)['legacy'];
}

function clothingRarityProfile(
    string $code,
    string $name,
    string $iconUrl = '',
    string $context = '',
    array $hints = []
): array {
    $definitions = rarityLevelDefinitions();
    $combined = strtolower(removeAccents(
        $code . ' ' . $name . ' ' . $iconUrl . ' ' . $context . ' '
        . implode(' ', array_map('strval', $hints))
    ));
    $level = 0;
    $source = 'unknown';
    $confidence = 'unknown';

    $explicitLevel = (int) ($hints['rarityLevel'] ?? 0);
    if ($explicitLevel >= 1 && $explicitLevel <= 14) {
        $level = $explicitLevel;
        $source = (string) ($hints['source'] ?? 'mapping');
        $confidence = 'exact';
    } elseif (preg_match('/(?:raridade|rarity|nivel|level)\s*[:#-]?\s*(1[0-4]|[1-9])\b/i', $context, $m)) {
        $level = (int) $m[1];
        $source = 'source-page';
        $confidence = 'exact';
    } elseif (preg_match('/\b(?:indisponivel|unavailable|disabled clothing)\b/', $combined)) {
        $level = 14;
        $source = 'metadata';
        $confidence = 'high';
    } elseif (preg_match('/\b(?:nft|collectible|collectable|colecionavel|limited edition|clothing_ltd|ltd_)\b/', $combined)) {
        $level = 13;
        $source = 'metadata';
        $confidence = 'high';
    } elseif (preg_match('/\b(?:never released|unreleased|nao lancad|nunca (?:foi )?(?:disponibilizad|lancad|vendid))\b/', $combined)) {
        $level = 11;
        $source = 'metadata';
        $confidence = 'high';
    } elseif (preg_match('/\b(?:hc gift|habbo club gift|presente hc|monthly hc gift)\b/', $combined)) {
        $level = 10;
        $source = 'metadata';
        $confidence = 'high';
    } elseif (preg_match('/\b(?:craft|crafting|recipe|receita|mesa de criac)\b/', $combined)) {
        $level = 9;
        $source = 'metadata';
        $confidence = 'high';
    } elseif (preg_match('/\b(?:clickable|mobi clicavel|click furni)\b/', $combined)) {
        $level = (str_contains($combined, 'kld2') || preg_match('/\brare|raro\b/', $combined)) ? 8 : 7;
        $source = 'metadata';
        $confidence = 'high';
    } elseif (preg_match('/\b(?:pack|bundle)\b/', $combined)) {
        $level = 5;
        $source = 'metadata';
        $confidence = 'high';
    } elseif (preg_match('/\b(?:offer|oferta)\b/', $combined)) {
        $level = 6;
        $source = 'metadata';
        $confidence = 'high';
    } elseif (
        preg_match('/\b(?:activity|atividade|event reward|recompensa)\b/', $combined)
        && (str_contains($combined, 'kld2') || preg_match('/\brare|raro\b/', $combined))
    ) {
        $level = 2;
        $source = 'metadata';
        $confidence = 'medium';
    } elseif (preg_match('/\b(?:promotion|promo|promocao|campaign reward)\b/', $combined)) {
        $level = 12;
        $source = 'metadata';
        $confidence = 'medium';
    } elseif (preg_match('/\b(?:permanent|permanente|always in catalog|catalogo permanente)\b/', $combined)) {
        $level = 1;
        $source = 'metadata';
        $confidence = 'medium';
    } elseif (str_contains(strtolower($iconUrl), 'kld2.gif')) {
        $level = 4;
        $source = 'habbowidgets';
        $confidence = 'exact';
    } elseif (str_contains(strtolower($iconUrl), 'kld1.gif')) {
        $level = 3;
        $source = 'habbowidgets';
        $confidence = 'exact';
    } elseif (preg_match('/\bclothing_r\d{2}[_-]/', $combined)) {
        $level = 4;
        $source = 'scientific-name';
        $confidence = 'medium';
    }

    if ($level === 0) {
        return [
            'level' => 0,
            'key' => '',
            'label' => '',
            'description' => '',
            'legacy' => 'generic',
            'source' => 'unknown',
            'confidence' => 'unknown',
        ];
    }

    $definition = $definitions[$level];
    return [
        'level' => $level,
        'key' => $definition['key'],
        'label' => $definition['label'],
        'description' => $definition['description'],
        'legacy' => legacyRarityForLevel($level),
        'source' => $source,
        'confidence' => $confidence,
    ];
}

function rarityIconUrl(string $rarity): string
{
    return rarityIconUrlByLevel(rarityLevelFromLegacy($rarity));
}

function normalizeRarityIconUrl(string $iconUrl, string $rarity): string
{
    $profile = clothingRarityProfile('', '', $iconUrl, $rarity);
    return rarityIconUrlByLevel((int) ($profile['level'] ?? 0));
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
        $time = '00:00:00';
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
            return sprintf('%04d-%02d-%02dT%s+00:00', (int) $parts[2], $month, (int) $parts[1], $time);
        }
        if (preg_match('/\b' . preg_quote($word, '/') . '\D+(\d{1,2})\D+(\d{4})\b/u', (string) $withoutTime, $parts)) {
            return sprintf('%04d-%02d-%02dT%s+00:00', (int) $parts[2], $month, (int) $parts[1], $time);
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

    $officialFriends = normalizeOfficialFriends(
        extractArrayFromKeys($officialProfile, ['friends'])
    );
    $widgetFriends = extractArrayFromKeys($widget, ['friends']);
    $friends = mergeLists($widgetFriends, $officialFriends, ['uniqueId', 'name']);

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
        'friendCount' => (int) ($widgetCounts['friends'] ?? count($friends)),
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
    $dateKeys = [
        'previousNames' => ['changedAt', 'date'],
        'previousMottos' => ['changedAt', 'date'],
        'previousStyles' => ['changedAt', 'date'],
        'previousFriends' => ['removedAt', 'date'],
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
                'generic',
                '',
                $hotel,
                HABBOWIDGETS_BASE . '/habbo/closet/'
                    . rawurlencode((string) hotelConfig($hotel)['widget'])
                    . '/' . rawurlencode($code)
            );
        }
    }

    $namedItems = array_filter(
        $items,
        static fn (array $item): bool => isCompleteClothingName(
            (string) ($item['name'] ?? ''),
            (string) ($item['code'] ?? '')
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
        'rarityIcons' => array_map(
            static fn (int $level): string => rarityIconUrlByLevel($level),
            array_keys(rarityLevelDefinitions())
        ),
        'rarityLevels' => rarityLevelDefinitions(),
        'note' => 'A raridade é vinculada à peça e usa os 14 níveis adotados por fã-sites.',
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
    $nameNode = firstXpathNode(
        $xpath,
        '//*[contains(concat(" ", normalize-space(@class), " "), " text-center ")]/h4[1]'
    );
    $itemContainer = $nameNode !== null
        ? firstXpathNode(
            $xpath,
            'ancestor::*[(self::div or self::section or self::article) and .//img[contains(translate(@src, "KLD", "kld"), "kld1.gif") or contains(translate(@src, "KLD", "kld"), "kld2.gif")]][1]',
            $nameNode
        )
        : null;
    $rarityNode = $itemContainer !== null
        ? firstXpathNode(
            $xpath,
            './/img[contains(translate(@src, "KLD", "kld"), "kld1.gif") or contains(translate(@src, "KLD", "kld"), "kld2.gif")][1]',
            $itemContainer
        )
        : null;
    $rarityIcon = imageUrlFromNode($rarityNode);
    $scientificName = extractClothingScientificName($html);
    $context = normalizeText(
        ($itemContainer !== null ? nodeText($itemContainer) : '')
        . ' ' . $pageTitle . ' ' . $scientificName
    );
    $rarityDetails = clothingRarityProfile($code, $name, $rarityIcon, $context);
    return clothingRecord(
        $code,
        $name,
        $category,
        $slot,
        (string) $rarityDetails['legacy'],
        $rarityIcon,
        $hotel,
        $closetUrl,
        $rarityDetails,
        $scientificName
    );
}

function suggestProfiles(string $query, string $hotel): array
{
    $queryKey = normalizeKey($query);
    $items = [];
    $seen = [];
    $cacheHit = false;

    $files = [];
    $checked = 0;
    foreach ($files as $file) {
        if ($checked++ >= 1_000) {
            break;
        }
        $record = readJsonFile($file);
        $profile = is_array($record['profile'] ?? null) ? $record['profile'] : null;
        if ($profile === null || normalizeHotel((string) ($profile['hotel'] ?? 'br')) !== $hotel) {
            continue;
        }
        $current = normalizeKey(firstString($profile, ['name', 'username']));
        $previousMatch = false;
        foreach (extractArrayFromKeys($profile, ['previousNames']) as $previous) {
            if (normalizeKey(firstString($previous, ['name', 'oldName', 'username'])) === $queryKey) {
                $previousMatch = true;
                break;
            }
        }
        if (!str_starts_with($current, $queryKey) && !$previousMatch) {
            continue;
        }
        $suggestion = suggestionFromProfile($profile);
        $key = firstString($suggestion, ['uniqueId', 'name']);
        if ($key !== '' && !isset($seen[$key])) {
            $seen[$key] = true;
            $items[] = $suggestion;
        }
        if (count($items) >= 8) {
            break;
        }
    }

    try {
        $official = fetchOfficialUserByName($query, hotelConfig($hotel));
        if (is_array($official)) {
            $cacheHit = false;
            $id = firstString($official, ['uniqueId', 'id']);
            $loaded = loadProfile($hotel, $query, $id);
            $suggestion = suggestionFromProfile($loaded['profile']);
            $key = firstString($suggestion, ['uniqueId', 'name']);
            if ($key !== '' && !isset($seen[$key])) {
                array_unshift($items, $suggestion);
            }
        }
    } catch (Throwable $ignored) {
    }

    return [array_slice($items, 0, 8), $cacheHit];
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
