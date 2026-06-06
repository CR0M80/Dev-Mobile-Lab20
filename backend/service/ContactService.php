<?php
require_once __DIR__ . '/../config/Database.php';

class ContactService {
    private $conn;
    private $table = "contact";

    public function __construct() {
        $database = new Database();
        $this->conn = $database->getConnection();
    }

    public function insert($name, $phone, $source = "mobile") {
        if (!$this->conn) return false;
        $sql = "INSERT INTO " . $this->table . " (name, phone, source) VALUES (:name, :phone, :source)";
        $stmt = $this->conn->prepare($sql);
        return $stmt->execute([
            ':name' => $name,
            ':phone' => $phone,
            ':source' => $source
        ]);
    }

    public function getAll() {
        if (!$this->conn) return [];
        $sql = "SELECT * FROM " . $this->table . " ORDER BY name ASC";
        $stmt = $this->conn->prepare($sql);
        $stmt->execute();
        return $stmt->fetchAll(PDO::FETCH_ASSOC);
    }

    public function search($keyword) {
        if (!$this->conn) return [];
        $sql = "SELECT * FROM " . $this->table . "
                WHERE name LIKE :keyword OR phone LIKE :keyword
                ORDER BY name ASC";
        $stmt = $this->conn->prepare($sql);
        $stmt->execute([':keyword' => '%' . $keyword . '%']);
        return $stmt->fetchAll(PDO::FETCH_ASSOC);
    }
}
?>
