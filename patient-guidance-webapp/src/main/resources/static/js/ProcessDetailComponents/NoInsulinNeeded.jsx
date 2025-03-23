function NoInsulinNeeded() {
    return (
        <div className="alert alert-success mt-4 shadow-sm rounded">
            <h4 className="alert-heading">No Insulin Required</h4>
            <p className="mb-2">
                Based on your current measurements, no insulin needs to be injected at this time.
            </p>
            <hr />
            <p className="mb-0">
                You're all set! You may leave or wait for further instructions from the staff if needed.
            </p>
        </div>
    );
}