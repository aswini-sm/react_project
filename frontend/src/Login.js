import React, { useState } from 'react';
import {
  createUserWithEmailAndPassword,
  signInWithEmailAndPassword,
  signInWithPopup
} from "firebase/auth";

import { auth, googleProvider } from "./firebase";



const Login = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [isRegistering, setIsRegistering] = useState(false);
  const [error, setError] = useState(null);
  const [token, setToken] = useState(null);

  // Helper method to extract token and save it
  const handleAuthSuccess = async (userCredential) => {
    try {
      const user = userCredential.user;
      // Get the Firebase ID Token (JWT)

      const token = await user.getIdToken();
      console.log(token);

      const idToken = await user.getIdToken(true);
      setToken(idToken);

      // We will send this token in standard API calls:
      // axios.get('/students', { headers: { Authorization: `Bearer ${idToken}` } })

      console.log("🔥 Successfully logged in!");
      console.log("Token retrieved: ", idToken);
      setError(null);
    } catch (error) {
      console.error("Error retrieving token: ", error);
    }
  };

  const handleEmailAuth = async (e) => {
    e.preventDefault();
    try {
      let userCredential;
      if (isRegistering) {
        userCredential = await createUserWithEmailAndPassword(auth, email, password);
      } else {
        userCredential = await signInWithEmailAndPassword(auth, email, password);
      }
      await handleAuthSuccess(userCredential);
    } catch (err) {
      setError(err.message);
    }
  };

  const handleGoogleSignIn = async () => {
    try {
      const userCredential = await signInWithPopup(auth, googleProvider);
      await handleAuthSuccess(userCredential);
    } catch (err) {
      setError(err.message);
    }
  };

  return (
    <div style={{ maxWidth: '400px', margin: '50px auto', padding: '20px', border: '1px solid #ccc', borderRadius: '8px' }}>
      <h2>{isRegistering ? 'Sign Up' : 'Login'} with Firebase</h2>

      {error && <p style={{ color: 'red' }}>{error}</p>}

      <form onSubmit={handleEmailAuth} style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
        <input
          type="email"
          placeholder="Email address"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          required
          style={{ padding: '8px' }}
        />
        <input
          type="password"
          placeholder="Password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          required
          style={{ padding: '8px' }}
        />
        <button type="submit" style={{ padding: '10px', background: '#007BFF', color: 'white', border: 'none', borderRadius: '4px' }}>
          {isRegistering ? 'Sign Up' : 'Login'}
        </button>
      </form>

      <p style={{ textAlign: 'center' }}>OR</p>

      <button onClick={handleGoogleSignIn} style={{ width: '100%', padding: '10px', background: '#DB4437', color: 'white', border: 'none', borderRadius: '4px' }}>
        Sign In with Google
      </button>

      <p style={{ marginTop: '20px', textAlign: 'center', cursor: 'pointer', color: '#007BFF' }} onClick={() => setIsRegistering(!isRegistering)}>
        {isRegistering ? 'Already have an account? Login' : "Don't have an account? Sign Up"}
      </p>

      {token && (
        <div style={{ marginTop: '20px', background: '#e9ffe9', padding: '10px', wordBreak: 'break-all' }}>
          <strong>✅ Authentication Successful!</strong>
          <p>JWT Token retrieved. Ready to send to Spring Boot!</p>
        </div>
      )}
    </div>
  );
};

export default Login;
