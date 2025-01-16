import React from "react";
import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import Navbar from "./components/custom/navbar";
import Import from "./pages/import";
import Dashboard from "./pages/dashboard";

function App() {
  return (
    <Router>
      {/* Fixed Navbar */}
      <div className="fixed top-0 left-0 w-full z-50 bg-gray-100">
        <Navbar />
      </div>

      {/* Main Content */}
      <div className="flex flex-col items-center w-full h-full pt-[6rem] bg-gray-100">
        <Routes>
          <Route path="/" element={<Import />} />
          <Route path="/dashboard" element={<Dashboard />} />
        </Routes>
      </div> 
    </Router>
  );
}

export default App;

