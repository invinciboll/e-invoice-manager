CREATE TABLE IF NOT EXISTS InvoiceEntity (
    invoice_id UUID PRIMARY KEY,
    file_hash VARCHAR(512),
    original_file_save_path VARCHAR(512),
    generated_file_save_path VARCHAR(512),
    file_format VARCHAR(50),
    xml_format VARCHAR(50),
    seller_name VARCHAR(512),
    invoice_reference VARCHAR(512),
    invoice_type_code INTEGER,
    issued_date DATE,
    total_sum DECIMAL(19, 2)
);
