INSERT OR IGNORE INTO patients
(patientID, name, firstname, nfcID, address, city, plz, dateOfBirth)
VALUES
    (1, 'Müller',  'Anna',    '04DAF28AB45780',
     'Bahnhofstrasse 1',   'Zürich',      '8001', '1985-03-15'),
    (2, 'Meier',   'Lukas',   '08DAC28BB44223',
     'Hauptstrasse 45',    'Bern',        '3001', '1990-11-02'),
    (3, 'Dubois',  'Sophie',  '06DCE18BA42067',
     'Rue du Rhône 10',    'Genève',      '1204', '1978-07-28');