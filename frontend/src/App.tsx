import React from "react";
import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import Navbar from "./components/custom/navbar";
import Import from "./pages/import";
import Dashboard from "./pages/dashboard";

function App() {
  return (
    <Router>
      {/* Fixed Navbar */}
      <div className="fixed top-0 left-0 w-full z-50">
        <Navbar />
      </div>

      {/* Main Content */}
      <div>
        <Routes>
          <Route path="/" element={<Import />} />
          <Route path="/dashboard" element={<Dashboard />} />
        </Routes>
      </div>
    </Router>
  );
}

export default App;
