import React from "react";
import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import Navbar from "@/components/custom/navbar";
import Import from "@/pages/import";
import Dashboard from "@/pages/dashboard";
import "@/i18n"; // Import i18n configuration
import Invoices from "./pages/invoices";
import { ThemeProvider } from "./components/theme-provider";

function App() {
  return (
    <ThemeProvider>
    <Router>
      {/* Fixed Navbar */}
      <div className="fixed top-0 left-0 w-full z-50 shadow-md">
        <Navbar />
      </div>

      {/* Main Content */}
      <div className="flex flex-col items-center w-full h-full">
        <Routes>
          <Route path="/" element={<Import />} />
          <Route path="/invoices" element={<Invoices />} />
          <Route path="/dashboard" element={<Dashboard />} />
        </Routes>
      </div> 
    </Router>
    </ThemeProvider>
  );
}

export default App;

