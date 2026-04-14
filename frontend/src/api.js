import axios from 'axios';

const BASE_URL = "https://react-project-5-nl1p.onrender.com";

// Setup an axios instance if we want, but simple functions are fine too.

export const fetchAllStudents = async (token) => {
  const response = await axios.get(`${BASE_URL}/students`, {
    headers: { Authorization: `Bearer ${token}` }
  });
  return response.data;
};

export const createStudent = async (name, token) => {
  const response = await axios.post(`${BASE_URL}/students`, 
    { name: name, status: "Present" },
    { headers: { Authorization: `Bearer ${token}` } }
  );
  return response.data;
};

export const updateStudentStatus = async (id, newStatus, token) => {
  const response = await axios.put(`${BASE_URL}/students/${id}/status`, 
    { status: newStatus },
    { headers: { Authorization: `Bearer ${token}` } }
  );
  return response.data;
};
