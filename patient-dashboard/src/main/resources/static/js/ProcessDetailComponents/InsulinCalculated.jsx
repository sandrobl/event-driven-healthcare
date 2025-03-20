function InsulinCalculated({insulinDoseInformation}) {
    console.log(insulinDoseInformation);
    return (
        <div className="alert alert-info mt-3">
            Insulin calculated.
            insulin.dose: {insulinDoseInformation}
        </div>
    );
}