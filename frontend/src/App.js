import React, { useEffect, useState, useCallback } from "react";
import Login from "./Login";
import { auth } from "./firebase";
import { onAuthStateChanged, signOut } from "firebase/auth";
import axios from "axios";
import {
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ResponsiveContainer,
  Cell
} from "recharts";
import './App.css';

const API = "https://react-project-5-nl1p.onrender.com";

function App() {
  const [user, setUser] = useState(null);
  const [students, setStudents] = useState([]);
  const [loading, setLoading] = useState(false);

  // Monitor Firebase Auth State
  useEffect(() => {
    const unsubscribe = onAuthStateChanged(auth, (currentUser) => {
      setUser(currentUser);
    });
    return () => unsubscribe();
  }, []);

  const handleLogout = async () => {
    await signOut(auth);
  };

  const getPercentage = (present, total) => {
    return total === 0 ? 0 : Math.round((present / total) * 100);
  };

  const computeColor = (percentage) => {
    if (percentage >= 75) return "#22c55e"; // Green
    if (percentage >= 50) return "#f97316"; // Orange
    return "#ef4444"; // Red
  };

  const fetchStudents = useCallback(async () => {
    if (!user) return;
    try {
      setLoading(true);
      console.log("Fetching students from Firebase Realtime DB...");
      
      const response = await axios.get("https://twelvefirebase-default-rtdb.asia-southeast1.firebasedatabase.app/students.json");
      console.log("Raw Data from Firebase:", response.data);
      
      let studentsList = [];
      if (response.data) {
        studentsList = Object.keys(response.data).map(key => {
          const student = response.data[key];
          // Determine the total field based on what is available (totalDays or totalClasses from backend)
          const total = student.totalDays !== undefined ? student.totalDays : (student.totalClasses || 0);
          const present = student.presentCount || 0;
          return {
            id: key,
            name: student.name || "Unknown",
            present: present,
            total: total,
            percentage: getPercentage(present, total),
          };
        });
      }
      
      console.log("Processed Students Array:", studentsList);
      setStudents(studentsList);
    } catch (err) {
      console.error("GET ERROR:", err.response || err);
      alert("Failed to fetch students. Open console for details.");
    } finally {
      setLoading(false);
    }
  }, [user]);

  const handleAttendance = async (studentId, isPresent) => {
    try {
      // Find the specific student we are updating safely
      const studentToUpdate = students.find(s => s.id === studentId);
      if (!studentToUpdate) {
         console.warn("Student ID not found in current UI state.");
         return; 
      }

      // Calculate newly updated metrics
      const newPresent = studentToUpdate.present + (isPresent ? 1 : 0);
      const newTotal = studentToUpdate.total + 1;
      const newPercentage = getPercentage(newPresent, newTotal);

      console.log(`[DEBUG] Preparing to update ${studentToUpdate.name} (${studentId})...`);
      console.log(`[DEBUG] Old Stats -> Present: ${studentToUpdate.present}, Total: ${studentToUpdate.total}`);
      console.log(`[DEBUG] New Stats -> Present: ${newPresent}, Total: ${newTotal}`);

      // Optimistic UI Update so user gets instant visual feedback
      setStudents(prev => prev.map(s => {
        if (s.id === studentId) {
           return {
              ...s,
              present: newPresent,
              total: newTotal,
              percentage: newPercentage
           };
        }
        return s;
      }));

      // Firebase PATCH expects just the explicit fields you wish to mutate.
      const payload = {
         presentCount: newPresent,
         totalDays: newTotal
      };
      
      console.log("[DEBUG] Sending PATCH payload to Firebase:", payload);
      
      // Grab Firebase Authentication JWT (in case Realtime DB rules require auth)
      let authParam = "";
      if (user) {
         const authToken = await user.getIdToken();
         authParam = `?auth=${authToken}`;
      }

      // Call Realtime Database REST API directly using PATCH method on exactly the student node
      const firebaseEndpoint = `https://twelvefirebase-default-rtdb.asia-southeast1.firebasedatabase.app/students/${studentId}.json${authParam}`;
      const response = await axios.patch(firebaseEndpoint, payload);
      
      console.log("[DEBUG] Firebase PATCH Response Success:", response.data);

      // Reload fresh data globally after success to enforce synchronization
      fetchStudents();

    } catch (err) {
      console.error("[ERROR] FIREBASE UPDATE ERROR:", err.response || err);
      alert(`Failed to mark attendance. ${err.response?.status === 401 ? 'Permission Denied! Ensure Firebase Rules permit write operations.' : ''}`);
      
      // Rollback optimistic frontend update by refetching genuine DB state
      fetchStudents();
    }
  };

  useEffect(() => {
    if (user) {
      fetchStudents();
    }
  }, [user, fetchStudents]);

  if (!user) {
    return <Login />;
  }

  return (
    <div className="dashboard-container">
      <header className="dashboard-header">
        <h1>📊 SmartAttend Dashboard</h1>
        <div className="user-controls">
          <span className="user-email">{user.email}</span>
          <button onClick={handleLogout} className="btn-logout">Logout</button>
        </div>
      </header>

      <main className="dashboard-content">
        <section className="student-management">
          <div className="student-list-card">
            <h2>Student Roster</h2>
            {loading && students.length === 0 ? (
              <p className="loading-text">Loading students...</p>
            ) : students.length === 0 ? (
              <p className="empty-text">No students found.</p>
            ) : (
              <div className="student-list">
                {students.map(s => (
                  <div key={s.id} className="student-row">
                    <div className="student-info">
                      <span className="student-name">{s.name}</span>
                      <span 
                        className="student-badge"
                        style={{ backgroundColor: computeColor(s.percentage), color: '#fff' }}
                      >
                        {s.percentage}% ({s.present}/{s.total})
                      </span>
                    </div>
                    <div className="student-actions">
                      <button className="btn-present" onClick={() => handleAttendance(s.id, true)}>+ Present</button>
                      <button className="btn-absent" onClick={() => handleAttendance(s.id, false)}>+ Absent</button>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </section>

        <section className="analytics">
          <div className="chart-card">
            <div className="chart-wrapper" style={{ height: '400px' }}>
              {students.length > 0 ? (
                <ResponsiveContainer width="100%" height="100%">
                  <BarChart
                    data={students}
                    margin={{ top: 20, right: 30, left: 20, bottom: 5 }}
                  >
                    <CartesianGrid strokeDasharray="3 3" />
                    <XAxis dataKey="name" />
                    <YAxis domain={[0, 100]} />
                    <Tooltip />
                    <Legend />
                    <Bar dataKey="percentage" name="Attendance %">
                      {students.map((entry, index) => (
                        <Cell key={`cell-${index}`} fill={computeColor(entry.percentage)} />
                      ))}
                    </Bar>
                  </BarChart>
                </ResponsiveContainer>
              ) : (
                <div className="empty-chart">Not enough data to display chart.</div>
              )}
            </div>
          </div>
        </section>
      </main>
    </div>
  );
}

export default App;