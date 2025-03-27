function IncorrectDoseInstructions({ doseDifference, insulinDoseInformation }) {
    const roundedDose = insulinDoseInformation < 1 ? 1 : Math.round(insulinDoseInformation);

    return (
        <div className="alert alert-warning mt-4 shadow-sm rounded">
            <h4 className="alert-heading">Incorrect Dose</h4>
            <p>
                The measured dose does not match the expected value.
                {doseDifference > 0
                    ? ` Your syringe contains ${doseDifference} ml too much.`
                    : ` Your syringe is short by ${Math.abs(doseDifference)} ml.`}
            </p>
            <p className="mb-3">
                <strong>Expected Dose:</strong>{' '}
                {roundedDose ? (
                    <span className="badge bg-primary fs-5">{roundedDose} ml</span>
                ) : (
                    <span className="badge bg-secondary fs-5">?</span>
                )}
            </p>
            <p>Please adjust your syringe accordingly and re-measure.</p>
        </div>
    );
}
