<?php
header('Content-Type: application/json; charset=utf-8');

$endpoint = $_GET['endpoint'] ?? 'profile';
$uniqueId = isset($_GET['uniqueId']) ? trim($_GET['uniqueId']) : '';
$name = isset($_GET['name']) ? trim($_GET['name']) : '';
$hotel = isset($_GET['hotel']) ? strtolower(trim($_GET['hotel'])) : 'br';
$includePreviousNames = isset($_GET['includePreviousNames']) ? strtolower(trim((string) $_GET['includePreviousNames'])) : 'false';
$figureString = isset($_GET['figureString']) ? trim($_GET['figureString']) : '';
$page = isset($_GET['page']) ? (int) $_GET['page'] : 1;
$limit = isset($_GET['limit']) ? (int) $_GET['limit'] : 100;

if ($page < 1) {
    $page = 1;
}

if ($limit < 1) {
    $limit = 100;
}

if ($limit > 100) {
    $limit = 100;
}

if (!preg_match('/^[a-z]{2}$/', $hotel)) {
    $hotel = 'br';
}

$includePreviousNamesValue = in_array($includePreviousNames, ['1', 'true', 'yes', 'on'], true) ? 'true' : 'false';

if ($uniqueId === '' && $name === '' && $figureString === '') {
    echo json_encode([
        'ok' => false,
        'error' => 'Informe uniqueId, name ou figureString.'
    ], JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES);
    exit;
}

if ($endpoint === 'habbos-suggest') {
    if ($name === '') {
        echo json_encode([
            'ok' => false,
            'error' => 'Informe name.'
        ], JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES);
        exit;
    }

    $url = 'https://habbodex.com/api/v1/habboinfo/habbos?name=' . rawurlencode($name) . '&includePreviousNames=' . $includePreviousNamesValue . '&hotel=' . rawurlencode($hotel);
} elseif ($endpoint === 'from-figure-string') {
    if ($figureString === '') {
        echo json_encode([
            'ok' => false,
            'error' => 'Informe figureString.'
        ], JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES);
        exit;
    }

    $url = 'https://habbodex.com/api/v1/furnidex/furni/from-figure-string?figureString=' . rawurlencode($figureString);
} elseif ($name !== '') {
    $url = 'https://habbodex.com/api/v1/habboinfo/br/habbo?name=' . rawurlencode($name);
} else {
    $url = 'https://habbodex.com/api/v1/habboinfo/' . rawurlencode($uniqueId);

    if ($endpoint !== 'profile') {
        $url .= '/' . rawurlencode($endpoint) . '?page=' . $page . '&limit=' . $limit;
    }
}

$context = stream_context_create([
    'http' => [
        'method' => 'GET',
        'header' => implode("\r\n", [
            'Accept: application/json',
            'User-Agent: Toxic/1.0'
        ]),
        'timeout' => 20,
        'ignore_errors' => true
    ]
]);

$response = @file_get_contents($url, false, $context);

if ($response === false) {
    echo json_encode([
        'ok' => false,
        'error' => 'Falha ao consultar.',
        'url' => $url
    ], JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES);
    exit;
}

$statusCode = 0;
if (isset($http_response_header) && is_array($http_response_header)) {
    foreach ($http_response_header as $headerLine) {
        if (preg_match('#HTTP/\S+\s+(\d{3})#', $headerLine, $matches)) {
            $statusCode = (int) $matches[1];
            break;
        }
    }
}

$data = json_decode($response, true);

if ($statusCode >= 400 || $data === null) {
    echo json_encode([
        'ok' => false,
        'error' => 'Resposta inválida',
        'status' => $statusCode,
        'url' => $url,
        'raw' => $response
    ], JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES);
    exit;
}

echo json_encode([
    'ok' => true,
    'data' => $data
], JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES);
