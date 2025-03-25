function InsulinForm({ correlationId, patientInsulinSensitivityFactor }) {
    const [nextMealCarbohydrates, setNextMealCarbohydrates] = useState("");
    const [insulinToCarbohydrateRatio, setInsulinToCarbohydrateRatio] = useState("");
    const [targetBloodGlucoseLevel, setTargetBloodGlucoseLevel] = useState("");
    const [bloodGlucose, setBloodGlucose] = useState("");

    const handleSubmit = (e) => {
        e.preventDefault();
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
            <h5>Enter Additional Data (Needed for Insulin Calculation)</h5>
            <form onSubmit={handleSubmit}>
                <div className="mb-3">
                    <label className="form-label">Next Meal Carbohydrates (grams)</label>
                    <input
                        type="number"
                        step="0.1"
                        className="form-control"
                        value={nextMealCarbohydrates}
                        onChange={(e) => setNextMealCarbohydrates(e.target.value)}
                        required
                    />
                </div>
                <div className="mb-3">
                    <label className="form-label">Insulin-to-Carbohydrate Ratio (grams/unit)</label>
                    <input
                        type="number"
                        step="0.1"
                        className="form-control"
                        value={insulinToCarbohydrateRatio}
                        onChange={(e) => setInsulinToCarbohydrateRatio(e.target.value)}
                        required
                    />
                </div>
                <div className="mb-3">
                    <label className="form-label">Target Blood Glucose Level (mmol/L)</label>
                    <input
                        type="number"
                        step="0.1"
                        className="form-control"
                        value={targetBloodGlucoseLevel}
                        onChange={(e) => setTargetBloodGlucoseLevel(e.target.value)}
                        required
                    />
                </div>
                <div className="mb-3">
                    <label className="form-label">Current Blood Glucose Level (mmol/L)</label>
                    <input
                        type="number"
                        step="0.1"
                        className="form-control"
                        value={bloodGlucose}
                        onChange={(e) => setBloodGlucose(e.target.value)}
                        required
                    />
                </div>
                <button type="submit" className="btn btn-success">Submit</button>
            </form>
        </div>
    );
}