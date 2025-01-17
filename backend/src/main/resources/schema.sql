CREATE TABLE IF NOT EXISTS InvoiceEntity (
    invoice_id UUID PRIMARY KEY,
    original_file_save_path VARCHAR(255),
    generated_file_save_path VARCHAR(255),
    file_format VARCHAR(50),
    xml_format VARCHAR(50),
    seller_name VARCHAR(255),
    invoice_reference VARCHAR(255),
    invoice_type_code INTEGER,
    issued_date DATE,
    total_sum DECIMAL(19, 2)
);
