function ScaleReservedInstructions({ insulinDoseInformation }) {
    return (
        <div className="alert alert-info mt-4 shadow-sm rounded">
            <h4 className="alert-heading">Scale Reserved</h4>
            <p>The scale has been reserved for your process. Please follow these steps:</p>
            <ol>
                <li>Fill the syringe with the calculated insulin dose.</li>
                <li>Place the filled syringe on the scale.</li>
                <li>Wait for the system to validate the measurement.</li>
            </ol>
            <p className="mb-3">
                <strong>Dose Required:</strong>{' '}
                {insulinDoseInformation ? (
                    <span className="badge bg-primary fs-5">{insulinDoseInformation} ml</span>
                ) : (
                    <span className="badge bg-secondary fs-5">?</span>
                )}
            </p>
            <p>Once validated, you will be prompted to confirm your injection.</p>
        </div>
    );
}
