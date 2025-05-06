package com.eventdriven.healthcare.patientcheckin.repository;

import com.eventdriven.healthcare.patientcheckin.model.Patient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Repository
public class PatientRepository {

    @Autowired
    private DataSource dataSource;

    public List<Patient> getPatientList() {
        List<Patient> patients = new ArrayList<>();
        String sql = """
            SELECT patientID, name, firstname, nfcID,
                   address, city, plz, dateOfBirth
              FROM patients
            """;

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                patients.add(Patient.builder()
                        .patientID(   rs.getInt("patientID") )
                        .name(        rs.getString("name")    )
                        .firstname(   rs.getString("firstname"))
                        .nfcID(       rs.getString("nfcID")   )
                        .address(     rs.getString("address") )
                        .city(        rs.getString("city")    )
                        .plz(         rs.getString("plz")     )
                        .dateOfBirth( LocalDate.parse(rs.getString("dateOfBirth")) )
                        .build()
                );
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching patients", e);
        }
        return patients;
    }

    public Patient addPatient(Patient patient) {
        String sql = """
            INSERT INTO patients
              (patientID, name, firstname, nfcID,
               address, city, plz, dateOfBirth)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, patient.getPatientID());
            pstmt.setString(2, patient.getName());
            pstmt.setString(3, patient.getFirstname());
            pstmt.setString(4, patient.getNfcID());
            pstmt.setString(5, patient.getAddress());
            pstmt.setString(6, patient.getCity());
            pstmt.setString(7, patient.getPlz());
            pstmt.setString(8, patient.getDateOfBirth().toString());

            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error inserting patient", e);
        }
        return patient;
    }

    public Patient updatePatient(Patient patient) {
        String sql = """
            UPDATE patients SET
                name = ?, firstname = ?, nfcID = ?,
                address = ?, city = ?, plz = ?, dateOfBirth = ?
              WHERE patientID = ?
            """;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, patient.getName());
            pstmt.setString(2, patient.getFirstname());
            pstmt.setString(3, patient.getNfcID());
            pstmt.setString(4, patient.getAddress());
            pstmt.setString(5, patient.getCity());
            pstmt.setString(6, patient.getPlz());
            pstmt.setString(7, patient.getDateOfBirth().toString());
            pstmt.setInt(8, patient.getPatientID());

            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating patient", e);
        }
        return patient;
    }

    public Patient getPatientById(int id) {
        String sql = """
            SELECT patientID, name, firstname, nfcID,
                   address, city, plz, dateOfBirth
              FROM patients
             WHERE patientID = ?
            """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Patient.builder()
                            .patientID(   rs.getInt("patientID") )
                            .name(        rs.getString("name")    )
                            .firstname(   rs.getString("firstname"))
                            .nfcID(       rs.getString("nfcID")   )
                            .address(     rs.getString("address") )
                            .city(        rs.getString("city")    )
                            .plz(         rs.getString("plz")     )
                            .dateOfBirth( LocalDate.parse(rs.getString("dateOfBirth")) )
                            .build();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching patient by ID", e);
        }
        return null;
    }

    public Patient getPatientByNfcId(String nfcId) {
        String sql = """
            SELECT patientID, name, firstname, nfcID,
                   address, city, plz, dateOfBirth
              FROM patients
             WHERE nfcID = ?
            """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, nfcId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Patient.builder()
                            .patientID(   rs.getInt("patientID") )
                            .name(        rs.getString("name")    )
                            .firstname(   rs.getString("firstname"))
                            .nfcID(       rs.getString("nfcID")   )
                            .address(     rs.getString("address") )
                            .city(        rs.getString("city")    )
                            .plz(         rs.getString("plz")     )
                            .dateOfBirth( LocalDate.parse(rs.getString("dateOfBirth")) )
                            .build();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching patient by NFC ID", e);
        }
        return null;
    }
}