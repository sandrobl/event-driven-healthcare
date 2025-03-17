function Overview({ processes, onSelect }) {
    return (
        <div className="card">
            <div className="card-header">
                <h2>All Active Processes</h2>
            </div>
            <div className="card-body p-0">
                {processes.length === 0 ? (
                    <p className="p-3">No active processes yet.</p>
                ) : (
                    <table className="table table-striped mb-0">
                        <thead>
                        <tr>
                            <th>Correlation ID</th>
                            <th>Patient Name</th>
                            <th>Current Step</th>
                            <th></th>
                        </tr>
                        </thead>
                        <tbody>
                        {processes.map(p => (
                            <tr key={p.correlationId}>
                                <td>{p.correlationId}</td>
                                <td>{p.patient ? (p.patient.firstname + " " + p.patient.name) : "?"}</td>
                                <td>{p.currentStep}</td>
                                <td>
                                    <button
                                        className="btn btn-primary btn-sm"
                                        onClick={() => onSelect(p.correlationId)}
                                    >
                                        View
                                    </button>
                                </td>
                            </tr>
                        ))}
                        </tbody>
                    </table>
                )}
            </div>
        </div>
    );
}