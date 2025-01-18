import { useState, useEffect } from "react";
import { NavigationMenu, NavigationMenuItem, NavigationMenuList } from "@/components/ui/navigation-menu";
import { Link } from "react-router-dom"; // Import Link from React Router
import { SunIcon, MoonIcon } from "@heroicons/react/24/solid"; // Import icons from Heroicons
import { Toggle } from "@/components/ui/toggle";

const Navbar = () => {
  const [darkMode, setDarkMode] = useState(false);

  useEffect(() => {
    if (darkMode) {
      document.documentElement.classList.add("dark");
    } else {
      document.documentElement.classList.remove("dark");
    }
  }, [darkMode]);

  const toggleDarkMode = () => {
    setDarkMode((prev) => !prev);
  };

  return (
    <nav className="border-b bg-white dark:bg-gray-800">
      <div className="container mx-auto flex items-center justify-between py-4">
        {/* Logo Section */}
        <div>
          <Link to="/">
            <img src="src/assets/logo.png" alt="MyApp Logo" className="h-10 w-auto" />
          </Link>
        </div>

        {/* Navigation Menu */}
        <div className="flex items-center space-x-6">
          <NavigationMenu>
            <NavigationMenuList className="flex space-x-4">
              <NavigationMenuItem>
                <Link
                  to="/"
                  className="flex items-center bg-yellow-400 hover:bg-yellow-500 text-black px-4 py-2 rounded"
                >
                  <svg
                    className="w-4 h-4 mr-2"
                    fill="none"
                    stroke="currentColor"
                    viewBox="0 0 24 24"
                    xmlns="http://www.w3.org/2000/svg"
                  >
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M12 4v16m8-8H4" />
                  </svg>
                  Import
                </Link>
              </NavigationMenuItem>
              <NavigationMenuItem>
                <Link
                  to="/dashboard"
                  className="text-gray-700 dark:text-gray-300 hover:text-gray-900 dark:hover:text-white"
                >
                  Dashboard
                </Link>
              </NavigationMenuItem>
            </NavigationMenuList>
          </NavigationMenu>

          {/* Dark Mode Toggle */}
          <Toggle
            aria-label="Toggle Dark Mode"
            pressed={darkMode}
            onClick={toggleDarkMode}
            className="flex items-center"
          >
            {darkMode ? <SunIcon className="h-4 w-4 text-yellow-400" /> : <MoonIcon className="h-4 w-4 text-gray-700 dark:text-gray-300" />}
          </Toggle>
        </div>
      </div>
    </nav>
  );
};

export default Navbar;
