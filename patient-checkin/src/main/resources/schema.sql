CREATE TABLE IF NOT EXISTS patients (
    patientID INTEGER PRIMARY KEY,
    name TEXT NOT NULL,
    firstname TEXT NOT NULL,
    height REAL NOT NULL,
    weight REAL NOT NULL,
    bloodGlucose REAL NOT NULL,
    nfcID TEXT UNIQUE NOT NULL,
    insulinSensitivityFactor REAL NOT NULL
);