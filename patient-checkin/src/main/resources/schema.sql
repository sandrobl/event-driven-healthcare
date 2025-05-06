CREATE TABLE IF NOT EXISTS patients (
    patientID         INTEGER PRIMARY KEY,
    name              TEXT    NOT NULL,
    firstname         TEXT    NOT NULL,
    nfcID             TEXT    UNIQUE NOT NULL,
    address           TEXT    NOT NULL,
    city              TEXT    NOT NULL,
    plz               TEXT    NOT NULL,
    dateOfBirth       DATE    NOT NULL
);