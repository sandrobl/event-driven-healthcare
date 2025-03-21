function InsulinCalculated({insulinDoseInformation}) {

    return (
        <div className="alert alert-info mt-3">
            Insulin calculated.
            insulin.dose:
            {insulinDoseInformation ? (
                <span>{insulinDoseInformation}</span>
            ):(
                <span>?</span>
            )}
        </div>
    );
}