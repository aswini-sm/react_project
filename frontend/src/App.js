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

const API = process.env.REACT_APP_API_URL;

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
      console.log("API:", API);
      const res = await axios.get(`${API}/students`);
      console.log("DATA:", res.data);

      if (!res.data) {
        setStudents([]);
        return;
      }

      setStudents(res.data);
    } catch (error) {
      console.error("FETCH ERROR:", error);
    } finally {
      setLoading(false);
    }
  }, [user]);

  const markAttendance = async (id) => {
    try {
      await axios.put(`${API}/students/${id}/status`, {
        status: "Present"
      });

      fetchStudents(); // refresh UI
    } catch (error) {
      console.error("UPDATE ERROR:", error);
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
              <p>No data available</p>
            ) : (
              <div className="student-list">
                {students.map((s) => {
                  const percentage = getPercentage(s.presentCount, s.totalDays);
                  return (
                    <div key={s.id} className="student-row">
                      <div className="student-info">
                        <h3>{s.name}</h3>
                        <p>Present: {s.presentCount}</p>
                        <p>Total: {s.totalDays}</p>
                        <p>Attendance: {percentage}%</p>
                        {/* We use existing badge for aesthetics as well */}
                        <span
                          className="student-badge"
                          style={{ backgroundColor: computeColor(percentage), color: '#fff' }}
                        >
                          Status
                        </span>
                      </div>
                      <div className="student-actions">
                        <button className="btn-present" onClick={() => markAttendance(s.id)}>
                          Mark Present
                        </button>
                      </div>
                    </div>
                  );
                })}
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
                    data={students.map(s => ({...s, percentage: getPercentage(s.presentCount, s.totalDays)}))}
                    margin={{ top: 20, right: 30, left: 20, bottom: 5 }}
                  >
                    <CartesianGrid strokeDasharray="3 3" />
                    <XAxis dataKey="name" />
                    <YAxis domain={[0, 100]} />
                    <Tooltip />
                    <Legend />
                    <Bar dataKey="percentage" name="Attendance %">
                      {students.map((s, index) => {
                        const percentage = getPercentage(s.presentCount, s.totalDays);
                        return <Cell key={`cell-${index}`} fill={computeColor(percentage)} />;
                      })}
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