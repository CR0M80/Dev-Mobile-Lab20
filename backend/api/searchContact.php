<?php
header("Content-Type: application/json");
require_once __DIR__ . '/../service/ContactService.php';

$keyword = isset($_GET['keyword']) ? $_GET['keyword'] : '';

if (empty($keyword)) {
    echo json_encode([]);
    exit;
}

$service = new ContactService();
echo json_encode($service->search($keyword));
?>
