const { useState, useEffect } = React;

function App() {
    const [processes, setProcesses] = useState([]);
    const [selectedProcess, setSelectedProcess] = useState(null);

    // On mount, open SSE connection & fetch initial list
    useEffect(() => {
        // 1) Fetch all processes
        fetch("/api/dashboard/processes")
            .then(res => res.json())
            .then(data => setProcesses(data));

        // 2) Setup SSE connection
        const eventSource = new EventSource("/api/dashboard/sse");

        eventSource.addEventListener("processUpdate", event => {
            const updatedProcess = JSON.parse(event.data);

            setProcesses(prev => {
                const existingIndex = prev.findIndex(p => p.correlationId === updatedProcess.correlationId);
                if (existingIndex >= 0) {
                    const newList = [...prev];
                    newList[existingIndex] = updatedProcess;
                    return newList;
                } else {
                    return [...prev, updatedProcess];
                }
            });

            if (selectedProcess && selectedProcess.correlationId === updatedProcess.correlationId) {
                setSelectedProcess(updatedProcess);
            }
        });

        eventSource.onerror = err => {
            console.error("SSE error:", err);
        };

        return () => {
            eventSource.close();
        };
    }, [selectedProcess]);

    // When user clicks a process in the overview table, fetch details
    const handleSelectProcess = (corrId) => {
        fetch(`/api/dashboard/processes/${corrId}`)
            .then(res => res.json())
            .then(data => setSelectedProcess(data));
    };

    const handleBack = () => {
        setSelectedProcess(null);
    };

    return (
        <div className="container py-4">
            <h1 className="mb-4">Patient Insulin Guidance</h1>
            {!selectedProcess ? (
                <Overview processes={processes} onSelect={handleSelectProcess} />
            ) : (
                <ProcessDetail process={selectedProcess} onBack={handleBack} />
            )}
        </div>
    );
}

ReactDOM.render(<App />, document.getElementById("root"));