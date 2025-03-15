package com.eventdriven.healthcare.patientcheckin.repository;

import com.eventdriven.healthcare.patientcheckin.model.Patient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class PatientRepository {

    @Autowired
    private DataSource dataSource;


    public List<Patient> getPatientList() {
        List<Patient> patients = new ArrayList<>();
        String sql = "SELECT * FROM patients";
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Patient patient = new Patient(
                        rs.getInt("patientID"),
                        rs.getString("name"),
                        rs.getString("firstname"),
                        rs.getFloat("height"),
                        rs.getFloat("weight"),
                        rs.getFloat("bloodGlucose"),
                        rs.getString("nfcID")
                );
                patients.add(patient);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return patients;
    }

    public Patient addPatient(Patient patient) {
        String sql = "INSERT INTO patients(patientID, name, firstname, height, weight, bloodGlucose, nfcID) VALUES(?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, patient.getPatientID());
            pstmt.setString(2, patient.getName());
            pstmt.setString(3, patient.getFirstname());
            pstmt.setFloat(4, patient.getHeight());
            pstmt.setFloat(5, patient.getWeight());
            pstmt.setFloat(6, patient.getBloodGlucose());
            pstmt.setString(7, patient.getNfcID());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return patient;
    }

    public Patient updatePatient(Patient patient) {
        String sql = "UPDATE patients SET name = ?, firstname = ?, height = ?, weight = ?, bloodGlucose = ?, nfcID = ? WHERE patientID = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, patient.getName());
            pstmt.setString(2, patient.getFirstname());
            pstmt.setFloat(3, patient.getHeight());
            pstmt.setFloat(4, patient.getWeight());
            pstmt.setFloat(5, patient.getBloodGlucose());
            pstmt.setString(6, patient.getNfcID());
            pstmt.setInt(7, patient.getPatientID());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return patient;
    }

    public Patient getPatientById(int id) {
        String sql = "SELECT * FROM patients WHERE patientID = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new Patient(
                        rs.getInt("patientID"),
                        rs.getString("name"),
                        rs.getString("firstname"),
                        rs.getFloat("height"),
                        rs.getFloat("weight"),
                        rs.getFloat("bloodGlucose"),
                        rs.getString("nfcID")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Patient getPatientByNfcId(String nfcId) {
        String sql = "SELECT * FROM patients WHERE nfcID = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nfcId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new Patient(
                        rs.getInt("patientID"),
                        rs.getString("name"),
                        rs.getString("firstname"),
                        rs.getFloat("height"),
                        rs.getFloat("weight"),
                        rs.getFloat("bloodGlucose"),
                        rs.getString("nfcID")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
