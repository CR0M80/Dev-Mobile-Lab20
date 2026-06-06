<?php
class Contact {
    public $id;
    public $name;
    public $phone;
    public $source;
    public $created_at;

    public function __construct($id = null, $name = null, $phone = null, $source = "mobile", $created_at = null) {
        $this->id = $id;
        $this->name = $name;
        $this->phone = $phone;
        $this->source = $source;
        $this->created_at = $created_at;
    }
}
?>
