package com.invinciboll.database;

import com.invinciboll.entities.InvoiceEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

@Repository
public class InvoiceDaoImpl implements InvoiceDao {

    private final JdbcTemplate jdbcTemplate;

    public InvoiceDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void save(InvoiceEntity invoice) {
        String sql = "INSERT INTO InvoiceEntity (invoice_id, file_hash, original_file_save_path, generated_file_save_path, " +
                     "file_format, xml_format, seller_name, invoice_reference, invoice_type_code, issued_date, total_sum) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql,
                invoice.getInvoiceId(),
                invoice.getFileHash(),
                invoice.getOriginalFileSavePath(),
                invoice.getGeneratedFileSavePath(),
                invoice.getFileFormat(),
                invoice.getXmlFormat(),
                invoice.getSellerName(),
                invoice.getInvoiceReference(),
                invoice.getInvoiceTypeCode(),
                invoice.getIssuedDate(),
                invoice.getTotalSum());
    }

    @Override
    public InvoiceEntity findById(UUID invoiceId) {
        String sql = "SELECT * FROM InvoiceEntity WHERE invoice_id = ?";
        return jdbcTemplate.queryForObject(sql, new InvoiceRowMapper(), invoiceId);
    }

    @Override
    public List<InvoiceEntity> findAll() {
        String sql = "SELECT * FROM InvoiceEntity";
        return jdbcTemplate.query(sql, new InvoiceRowMapper());
    }

    @Override
    public void deleteById(UUID invoiceId) {
        String sql = "DELETE FROM InvoiceEntity WHERE invoice_id = ?";
        jdbcTemplate.update(sql, invoiceId);
    }

    @Override
    public boolean existsByFileHash(String fileHash) {
        String sql = "SELECT COUNT(*) FROM InvoiceEntity WHERE file_hash = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, fileHash);
        return count != null && count > 0;
    }

    @Override
    public List<String> findDistinctSellers() {
        String sql = "SELECT DISTINCT seller_name FROM InvoiceEntity";
        return jdbcTemplate.queryForList(sql, String.class);
    }

    private static class InvoiceRowMapper implements RowMapper<InvoiceEntity> {
        @Override
        public InvoiceEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new InvoiceEntity(
                    UUID.fromString(rs.getString("invoice_id")),
                    rs.getString("file_hash"),
                    rs.getString("original_file_save_path"),
                    rs.getString("generated_file_save_path"),
                    rs.getString("file_format"),
                    rs.getString("xml_format"),
                    rs.getString("seller_name"),
                    rs.getString("invoice_reference"),
                    rs.getInt("invoice_type_code"),
                    rs.getDate("issued_date").toLocalDate(),
                    rs.getBigDecimal("total_sum")
            );
        }
    }
}
