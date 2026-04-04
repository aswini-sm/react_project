import axios from "axios";
import { useEffect, useState } from "react";

const API = "http://localhost:8080/students";

function App() {
  const [students, setStudents] = useState([]);
  const [name, setName] = useState("");

  const fetchStudents = () => {
    axios.get(API).then(res => setStudents(res.data));
  };

  useEffect(() => {
    fetchStudents();
  }, []);

  const addStudent = () => {
    if (!name) return;
    axios.post(API, { name, status: "Absent" })
      .then(() => {
        setName("");
        fetchStudents();
      });
  };

  const markPresent = (id) => {
    axios.put(`${API}/${id}`, { status: "Present" })
      .then(fetchStudents);
  };

  return (
    <div style={{ padding: "30px", fontFamily: "Arial" }}>
      <h1 style={{ textAlign: "center" }}>📚 SmartAttend</h1>

      <div style={{ textAlign: "center", marginBottom: "20px" }}>
        <input
          value={name}
          placeholder="Enter student name"
          onChange={e => setName(e.target.value)}
          style={{ padding: "10px", width: "200px" }}
        />
        <button
          onClick={addStudent}
          style={{
            marginLeft: "10px",
            padding: "10px",
            background: "green",
            color: "white",
            border: "none"
          }}
        >
          Add
        </button>
      </div>

      <div style={{ maxWidth: "500px", margin: "auto" }}>
        {students.map(s => (
          <div
            key={s.id}
            style={{
              display: "flex",
              justifyContent: "space-between",
              padding: "10px",
              marginBottom: "10px",
              border: "1px solid #ccc",
              borderRadius: "8px"
            }}
          >
            <span>{s.name}</span>
            <span>{s.status}</span>
            <button
              onClick={() => markPresent(s.id)}
              style={{
                background: "blue",
                color: "white",
                border: "none",
                padding: "5px 10px"
              }}
            >
              Present
            </button>
          </div>
        ))}
      </div>
    </div>
  );
}

export default App;