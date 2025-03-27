function InsulinCalculated({ insulinDoseInformation }) {

    const roundedDose = insulinDoseInformation < 1 ? 1 : Math.round(insulinDoseInformation);

    return (
        <div className="alert alert-info mt-4 shadow-sm rounded">
            <h4 className="alert-heading">Insulin Dose Ready</h4>
            <p className="mb-2">
                The required insulin dose has been calculated.
            </p>
            <hr />
            <p className="mb-3">
                <strong>Dose:</strong>{' '}
                {insulinDoseInformation ? (
                    <span className="badge bg-primary fs-5">{roundedDose} ml</span>
                ) : (
                    <span className="badge bg-secondary fs-5">?</span>
                )}
            </p>
            <p className="mb-0">
                A scale is now being reserved so you can fill the syringe and measure the correct dose.
                Please wait for further instructions before proceeding.
            </p>
        </div>
    );
}