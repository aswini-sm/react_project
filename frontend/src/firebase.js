// Import Firebase
import { initializeApp } from "firebase/app";
import { getAuth, GoogleAuthProvider } from "firebase/auth";

// Your config (from Firebase console)
const firebaseConfig = {
  apiKey: "AIzaSyBkAmhPme7RXz1Py0zDwa5W6mdtDs4NEBc",
  authDomain: "twelvefirebase.firebaseapp.com",
  projectId: "twelvefirebase",
  appId: "1:304744848836:web:6aabd44d56a65319df1891"
};

// Initialize Firebase
const app = initializeApp(firebaseConfig);

// Auth
export const auth = getAuth(app);
export const googleProvider = new GoogleAuthProvider();