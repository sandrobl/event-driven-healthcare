function InsulinForm({ correlationId }) {
    const [carbs, setCarbs] = useState("");
    const [insulinRatio, setInsulinRatio] = useState("");

    const handleSubmit = (e) => {
        e.preventDefault();
        const payload = {
            carbs: parseFloat(carbs),
            insulinRatio: parseFloat(insulinRatio)
        };

        fetch(`/api/dashboard/processes/${correlationId}/insulinForm/submit`, {
            method: "POST",
            headers: {"Content-Type": "application/json"},
            body: JSON.stringify(payload)
        })

     };

    return (
        <div className="border p-3 rounded">
            <h5>Enter Additional Data (Insulin Calculation)</h5>
            <form onSubmit={handleSubmit}>
                <div className="mb-3">
                    <label className="form-label">Carbs (grams)</label>
                    <input
                        type="number"
                        step="0.1"
                        className="form-control"
                        value={carbs}
                        onChange={(e) => setCarbs(e.target.value)}
                        required
                    />
                </div>
                <div className="mb-3">
                    <label className="form-label">Insulin Ratio (units per 10g carbs)</label>
                    <input
                        type="number"
                        step="0.1"
                        className="form-control"
                        value={insulinRatio}
                        onChange={(e) => setInsulinRatio(e.target.value)}
                        required
                    />
                </div>
                <button type="submit" className="btn btn-success">Submit</button>
            </form>
        </div>
    );
}