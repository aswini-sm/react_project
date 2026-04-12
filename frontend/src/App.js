import { useEffect, useState } from "react";
import Login from "./Login";
import { auth } from "./firebase";
import { onAuthStateChanged, signOut } from "firebase/auth";
import { fetchAllStudents, createStudent, updateStudentStatus } from "./api";

function App() {
  const [user, setUser] = useState(null);
  const [token, setToken] = useState(null);

  // App core state
  const [students, setStudents] = useState([]);
  const [name, setName] = useState("");
  const [loading, setLoading] = useState(false);

  // Monitor Firebase Auth State
  useEffect(() => {
    const unsubscribe = onAuthStateChanged(auth, async (currentUser) => {
      setUser(currentUser);
      if (currentUser) {
        // Retrieve Firebase ID token after login
        const idToken = await currentUser.getIdToken(true);
        setToken(idToken);
      } else {
        setToken(null);
      }
    });
    return () => unsubscribe();
  }, []);

  const handleLogout = async () => {
    await signOut(auth);
  };

  // Add a new student to Firestore via Spring Boot Backend
  const addStudent = async () => {
    if (!name.trim()) return alert("Enter a student name");
    if (!token) return alert("You must be logged in to add a student.");

    try {
      setLoading(true);
      await createStudent(name, token);
      setName("");
      fetchStudents(); // Refresh the list after adding
    } catch (err) {
      console.error("POST ERROR:", err);
      // Helpful error alert for debugging
      if (err.response && err.response.status === 401) {
        alert("Unauthorized: Your Firebase token is invalid or expired.");
      } else {
        alert("Failed to add student. Check backend console logs.");
      }
    } finally {
      setLoading(false);
    }
  };

  // Fetch all students
  const fetchStudents = async () => {
    if (!token) return;
    try {
      setLoading(true);
      const data = await fetchAllStudents(token);
      setStudents(data);
    } catch (err) {
      console.error("GET ERROR:", err);
      if (err.response && err.response.status === 401) {
        alert("Unauthorized to fetch students.");
      } else {
        alert("Failed to fetch students. Check backend or network.");
      }
    } finally {
      setLoading(false);
    }
  };
  // Toggle student presence status safely over Firebase Auth
  const toggleAttendance = async (id, currentStatus) => {
    if (!token) return alert("You must be logged in to update a student.");

    const newStatus = currentStatus === "Present" ? "Absent" : "Present";

    console.log("\n--- [FRONTEND DEBUG] SENDING UPDATE ---");
    console.log("Target Document ID:", id);
    console.log("New Status to send:", newStatus);

    try {
      // Optimistic UI update (Instant feedback to user)
      setStudents(prev => prev.map(s => s.id === id ? { ...s, status: newStatus } : s));

      const data = await updateStudentStatus(id, newStatus, token);

      console.log("✅ API SUCCESS RESPONSE:", data);
      console.log("---------------------------------------\n");
    } catch (err) {
      console.error("❌ PUT ERROR:", err);
      console.error("Failed Response Data:", err.response?.data);

      if (err.response && err.response.status === 401) {
        alert("Unauthorized: Your Firebase token is invalid or expired.");
      } else if (err.response && err.response.status === 404) {
        alert(`Error 404: Document with ID ${id} was not found in Firestore!`);
      } else {
        alert("Failed to update status. Reverting changes.");
      }
      fetchStudents(); // Revert local state to truth on failure
    }
  };

  useEffect(() => {
    if (user && token) {
      fetchStudents();
    }
  }, [user, token]);

  // If no user is logged in, show Login Screen
  if (!user) {
    return <Login />;
  }

  // If logged in, show App
  return (
    <div style={{ padding: "30px", fontFamily: "Arial" }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <h1 style={{ textAlign: "center" }}>📚 SmartAttend</h1>
        <button onClick={handleLogout} style={{ padding: '8px', background: 'red', color: 'white', border: 'none', borderRadius: '4px' }}>Logout</button>
      </div>

      <p style={{ textAlign: 'center', color: '#666' }}>Logged in as: {user.email}</p>

      <div style={{ textAlign: "center", marginBottom: "20px" }}>
        <input
          value={name}
          placeholder="Enter student name"
          onChange={e => setName(e.target.value)}
          style={{ padding: "10px", width: "200px" }}
        />
        <button
          onClick={addStudent}
          style={{ marginLeft: "10px", padding: "10px", background: "green", color: "white", border: "none" }}
        >
          Add
        </button>
      </div>

      {loading ? <p style={{ textAlign: "center" }}>Loading students...</p> : null}

      <div style={{ maxWidth: "500px", margin: "auto" }}>
        {students.map(s => (
          <div key={s.id} style={{ display: "flex", justifyContent: "space-between", alignItems: "center", padding: "10px", marginBottom: "10px", border: "1px solid #ccc", borderRadius: "8px" }}>
            <span style={{ fontWeight: "bold" }}>{s.name}</span>
            <span style={{ color: s.status === "Present" ? "green" : "red", fontWeight: "bold" }}>{s.status}</span>
            <button
              onClick={() => toggleAttendance(s.id, s.status)}
              style={{
                background: s.status === "Present" ? "orange" : "blue",
                color: "white",
                border: "none",
                padding: "8px",
                borderRadius: "4px",
                cursor: "pointer"
              }}
            >
              Mark {s.status === "Present" ? "Absent" : "Present"}
            </button>
          </div>
        ))}
      </div>
    </div>
  );
}

export default App;