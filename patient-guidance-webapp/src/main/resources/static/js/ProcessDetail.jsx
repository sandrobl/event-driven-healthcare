function ProcessDetail({ process, onBack }) {
    const { correlationId, patient, currentStep, insulinCalculatedEvent, errorMessage, doseDifference, confirmationMessage } = process;

    return (
        <div className="card">
            <div className="card-header d-flex justify-content-between">
                <h2>Process Detail ({correlationId})</h2>
                <button className="btn btn-secondary" onClick={onBack}>Back</button>
            </div>
            <div className="card-body">
                {patient ? (
                    <div className="mb-3">
                        <h5>Patient Info</h5>
                        <p><strong>ID:</strong> {patient.patientID}</p>
                        <p><strong>Name:</strong> {patient.firstname} {patient.name}</p>
                        <p><strong>Height:</strong> {patient.height} cm</p>
                        <p><strong>Weight:</strong> {patient.weight} kg</p>
                        <p><strong>NFC ID:</strong> {patient.nfcID}</p>
                        <p><strong>Insulin sensitivity factor:</strong> {patient.insulinSensitivityFactor} mg/dL</p>
                    </div>
                ) : (
                    <p>No patient data available.</p>
                )}

                <div className="mb-3">
                    <p><strong>Current Step:</strong> {currentStep}</p>
                </div>

                {currentStep === "INFORMATION_NEEDED" && (
                    <InsulinForm correlationId={correlationId} patientInsulinSensitivityFactor={patient.insulinSensitivityFactor} />
                )}
                {currentStep === "FORM_SUBMITTED" && (
                    <InsulinFormSubmittedInfo />
                )}
                {currentStep === "INSULIN_CALCULATED" && insulinCalculatedEvent && (
                    <InsulinCalculated insulinDoseInformation={insulinCalculatedEvent.insulinDoses} />
                )}
                {currentStep === "NO_INSULIN_NEEDED" && (
                    <NoInsulinNeeded />
                )}
                {currentStep === "ERROR" && errorMessage && (
                    <ErrorAlert message={errorMessage}/>
                )}
                {currentStep === "SCALE_RESERVED" && (
                    <ScaleReservedInstructions insulinDoseInformation={insulinCalculatedEvent.insulinDoses}/>
                )}
                {currentStep === "INCORRECT_DOSE" && (
                    <IncorrectDoseInstructions doseDifference={doseDifference} insulinDoseInformation={insulinCalculatedEvent.insulinDoses} />
                )}
                {currentStep === "AWAITING_CONFIRMATION" && (
                    <InjectionConfirmation  correlationId={correlationId} confirmationMessage={confirmationMessage}/>
                )}
                {currentStep === "CONFIRMED" && (
                    <InjectionConfirmed />
                )}

            </div>
        </div>
    );
}