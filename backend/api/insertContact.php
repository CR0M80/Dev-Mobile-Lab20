<?php
header("Content-Type: application/json");
require_once __DIR__ . '/../service/ContactService.php';

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $data = json_decode(file_get_contents("php://input"), true);

    if (!isset($data['name']) || !isset($data['phone'])) {
        echo json_encode(["success" => false, "message" => "Champs name et phone requis"]);
        exit;
    }

    $service = new ContactService();
    $ok = $service->insert($data['name'], $data['phone']);

    echo json_encode([
        "success" => $ok,
        "message" => $ok ? "Contact synchronisé avec succès" : "Erreur lors de l'insertion"
    ]);
} else {
    echo json_encode(["success" => false, "message" => "Méthode non autorisée"]);
}
?>
