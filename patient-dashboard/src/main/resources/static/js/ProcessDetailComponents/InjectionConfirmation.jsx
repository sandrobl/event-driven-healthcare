function InjectionConfirmation({ correlationId, confirmationMessage }) {
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);

    const confirmInjection = async () => {
        setLoading(true);
        setError(null);
        try {
            const response = await fetch(`/api/dashboard/processes/${correlationId}/confirmInjection`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ confirmed: true })
            });
            if (!response.ok) {
                throw new Error('Failed to confirm injection');
            }
        } catch (err) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="alert alert-info mt-4 shadow-sm rounded">
            <h4 className="alert-heading">Confirm Injection</h4>
            <p>{{confirmationMessage}}</p>
            {error && <div className="alert alert-danger">{error}</div>}
            <button className="btn btn-primary" onClick={confirmInjection} disabled={loading}>
                {loading ? 'Confirming...' : 'Confirm Injection'}
            </button>
        </div>
    );
}