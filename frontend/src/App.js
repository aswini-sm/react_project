import React, { useEffect, useState, useCallback } from "react";
import Login from "./Login";
import { auth, db } from "./firebase";
import { onAuthStateChanged, signOut } from "firebase/auth";
import { collection, getDocs, addDoc, updateDoc, doc } from "firebase/firestore";
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  BarElement,
  Title,
  Tooltip,
  Legend,
} from 'chart.js';
import { Bar } from 'react-chartjs-2';
import './App.css';

// Register ChartJS plugins
ChartJS.register(
  CategoryScale,
  LinearScale,
  BarElement,
  Title,
  Tooltip,
  Legend
);

function App() {
  const [user, setUser] = useState(null);
  const [students, setStudents] = useState([]);
  const [name, setName] = useState("");
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

  const computeColor = (percentage) => {
    if (percentage >= 75) return "rgba(34, 197, 94, 0.8)"; // Green
    if (percentage >= 50) return "rgba(249, 115, 22, 0.8)"; // Orange
    return "rgba(239, 68, 68, 0.8)"; // Red
  };

  const getPercentage = (present, total) => {
    return total === 0 ? 0 : Math.round((present / total) * 100);
  };

  const fetchStudents = useCallback(async () => {
    if (!user) return;
    try {
      setLoading(true);
      const querySnapshot = await getDocs(collection(db, "students"));
      const studentsList = querySnapshot.docs.map(docSnap => {
        const data = docSnap.data();
        let p = data.totalpresent !== undefined ? data.totalpresent : (data.present || 0);
        let t = data.totaldays !== undefined ? data.totaldays : (data.total || 0);

        // Legacy normalization from old structure {status: "Present"/"Absent"}
        if (data.status) {
          if (data.status === "Present" && p === 0 && t === 0) {
            p = 1; t = 1;
          } else if (data.status === "Absent" && p === 0 && t === 0) {
            p = 0; t = 1;
          }
        }
        
        return {
          id: docSnap.id,
          name: data.name,
          present: p,
          total: t,
          percentage: getPercentage(p, t),
        };
      });
      setStudents(studentsList);
    } catch (err) {
      console.error("GET ERROR:", err);
      alert("Failed to fetch students. Check permissions.");
    } finally {
      setLoading(false);
    }
  }, [user]);

  const addStudent = async () => {
    if (!name.trim()) return alert("Enter a student name");

    try {
      setLoading(true);
      await addDoc(collection(db, "students"), {
        name,
        totalpresent: 0,
        totaldays: 0,
        attendancepercent: "0%"
      });
      setName("");
      fetchStudents();
    } catch (err) {
      console.error("ADD ERROR:", err);
      alert("Failed to add student.");
    } finally {
      setLoading(false);
    }
  };

  const handleAttendance = async (student, isPresent) => {
    try {
      const newPresent = student.present + (isPresent ? 1 : 0);
      const newTotal = student.total + 1;
      const newPercentage = getPercentage(newPresent, newTotal);
      
      // Optimistic UI Update
      setStudents(prev => prev.map(s => 
        s.id === student.id 
          ? { ...s, present: newPresent, total: newTotal, percentage: newPercentage }
          : s
      ));

      const docRef = doc(db, "students", student.id);
      await updateDoc(docRef, {
        totalpresent: newPresent,
        totaldays: newTotal,
        attendancepercent: newPercentage + "%"
      });
    } catch (err) {
      console.error("UPDATE ERROR:", err);
      alert("Failed to update attendance.");
      // Revert optimism on error
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

  // Setup Chart Data
  const chartData = {
    labels: students.map(s => s.name),
    datasets: [
      {
        label: 'Attendance %',
        data: students.map(s => s.percentage),
        backgroundColor: students.map(s => computeColor(s.percentage)),
        borderRadius: 6,
      }
    ]
  };

  const chartOptions = {
    responsive: true,
    maintainAspectRatio: false,
    scales: {
      y: {
        beginAtZero: true,
        max: 100,
        ticks: { stepSize: 20 }
      }
    },
    plugins: {
      legend: { display: false },
      title: { display: true, text: 'Student Attendance Overview' }
    }
  };

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
        {/* Left Column: Student Management */}
        <section className="student-management">
          <div className="add-student-card">
            <h2>Add New Student</h2>
            <div className="input-group">
              <input
                value={name}
                placeholder="Enter student name"
                onChange={e => setName(e.target.value)}
              />
              <button onClick={addStudent} className="btn-add">Add Student</button>
            </div>
          </div>

          <div className="student-list-card">
            <h2>Student Roster</h2>
            {loading && students.length === 0 ? (
              <p className="loading-text">Loading students...</p>
            ) : students.length === 0 ? (
              <p className="empty-text">No students found. Add one above!</p>
            ) : (
              <div className="student-list">
                {students.map(s => (
                  <div key={s.id} className="student-row">
                    <div className="student-info">
                      <span className="student-name">{s.name}</span>
                      <span 
                        className="student-badge"
                        style={{ backgroundColor: computeColor(s.percentage) }}
                      >
                        {s.percentage}% ({s.present}/{s.total})
                      </span>
                    </div>
                    <div className="student-actions">
                      <button className="btn-present" onClick={() => handleAttendance(s, true)}>+ Present</button>
                      <button className="btn-absent" onClick={() => handleAttendance(s, false)}>+ Absent</button>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </section>

        {/* Right Column: Analytics */}
        <section className="analytics">
          <div className="chart-card">
            <div className="chart-wrapper">
              {students.length > 0 ? (
                <Bar options={chartOptions} data={chartData} />
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