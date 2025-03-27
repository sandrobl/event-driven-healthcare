function InsulinForm({ correlationId, patientInsulinSensitivityFactor }) {
    const [nextMealCarbohydrates, setNextMealCarbohydrates] = useState("");
    const [insulinToCarbohydrateRatio, setInsulinToCarbohydrateRatio] = useState("");
    const [targetBloodGlucoseLevel, setTargetBloodGlucoseLevel] = useState("");
    const [bloodGlucose, setBloodGlucose] = useState("");
    const [errors, setErrors] = useState({});

    const validateForm = () => {
        const newErrors = {};
        if (!nextMealCarbohydrates) newErrors.nextMealCarbohydrates = "Please enter Next Meal Carbohydrates.";
        if (!insulinToCarbohydrateRatio) newErrors.insulinToCarbohydrateRatio = "Please enter Insulin-to-Carbohydrate Ratio.";
        if (!targetBloodGlucoseLevel) newErrors.targetBloodGlucoseLevel = "Target Blood Glucose Level required.";
        if (!bloodGlucose) newErrors.bloodGlucose = "Current Blood Glucose Level required.";
        setErrors(newErrors);
        return Object.keys(newErrors).length === 0;
    };


    const handleSubmit = (e) => {
        e.preventDefault();
        if (!validateForm()) return;
        const payload = {
            nextMealCarbohydrates: parseFloat(nextMealCarbohydrates),
            insulinToCarbohydrateRatio: parseFloat(insulinToCarbohydrateRatio),
            targetBloodGlucoseLevel: parseFloat(targetBloodGlucoseLevel),
            bloodGlucose: parseFloat(bloodGlucose),
            patientInsulinSensitivityFactor: parseFloat(patientInsulinSensitivityFactor)
        };

        fetch(`/api/dashboard/processes/${correlationId}/insulinForm/submit`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(payload)
        });
    };

    return (
        <div className="border p-3 rounded">
            <div className="alert alert-info mt-4 shadow-sm rounded">
                <h4 className="alert-heading">Successfully Registered</h4>
                <p>Please remove your NFC card from the reader and provide additional information.</p>
            </div>
            <h5>Enter Additional Data (Needed for Insulin Calculation)</h5>
            <form onSubmit={handleSubmit} noValidate>
                <div className="mb-3">
                    <label className="form-label">Next Meal Carbohydrates (grams)</label>
                    <input
                        type="number"
                        step="0.1"
                        className={`form-control ${errors.nextMealCarbohydrates ? "is-invalid" : ""}`}
                        value={nextMealCarbohydrates}
                        onChange={(e) => setNextMealCarbohydrates(e.target.value)}
                        required
                    />
                    {errors.nextMealCarbohydrates && (
                        <div className="invalid-feedback">{errors.nextMealCarbohydrates}</div>
                    )}
                </div>
                <div className="mb-3">
                    <label className="form-label">Insulin-to-Carbohydrate Ratio (grams/unit)</label>
                    <input
                        type="number"
                        step="0.1"
                        className={`form-control ${errors.insulinToCarbohydrateRatio ? "is-invalid" : ""}`}
                        value={insulinToCarbohydrateRatio}
                        onChange={(e) => setInsulinToCarbohydrateRatio(e.target.value)}
                        required
                    />
                    {errors.insulinToCarbohydrateRatio && (
                        <div className="invalid-feedback">{errors.insulinToCarbohydrateRatio}</div>
                    )}
                </div>
                <div className="mb-3">
                    <label className="form-label">Target Blood Glucose Level (mg/dL)</label>
                    <input
                        type="number"
                        step="0.1"
                        className={`form-control ${errors.targetBloodGlucoseLevel ? "is-invalid" : ""}`}
                        value={targetBloodGlucoseLevel}
                        onChange={(e) => setTargetBloodGlucoseLevel(e.target.value)}
                        required
                    />
                    {errors.targetBloodGlucoseLevel && (
                        <div className="invalid-feedback">{errors.targetBloodGlucoseLevel}</div>
                    )}
                </div>
                <div className="mb-3">
                    <label className="form-label">Current Blood Glucose Level (mg/dL)</label>
                    <input
                        type="number"
                        step="0.1"
                        className={`form-control ${errors.bloodGlucose ? "is-invalid" : ""}`}
                        value={bloodGlucose}
                        onChange={(e) => setBloodGlucose(e.target.value)}
                        required
                    />
                    {errors.bloodGlucose && (
                        <div className="invalid-feedback">{errors.bloodGlucose}</div>
                    )}
                </div>
                <button type="submit" className="btn btn-success">Submit</button>
            </form>
        </div>
    );
}