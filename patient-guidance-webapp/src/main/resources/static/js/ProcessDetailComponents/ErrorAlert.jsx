function ErrorAlert({ message }) {
    return (
        <div className="alert alert-danger mt-4 shadow-sm rounded">
            <h4 className="alert-heading">Error</h4>
            <p className="mb-2">
                An error has occurred while processing your request.
            </p>
            <hr />
            <p className="mb-0">
                <strong>Details:</strong> {message}
            </p>
        </div>
    );
}