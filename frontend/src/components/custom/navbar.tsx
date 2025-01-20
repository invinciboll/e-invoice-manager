import { useState, useEffect } from "react";
import { NavigationMenu, NavigationMenuItem, NavigationMenuList } from "@/components/ui/navigation-menu";
import { Link, useLocation, useNavigate } from "react-router-dom"; // Import Link from React Router
import { SunIcon, MoonIcon } from "@heroicons/react/24/solid"; // Import icons from Heroicons
import { Toggle } from "@/components/ui/toggle";
import { useTranslation } from "react-i18next"; // Import translation hook
import { useTheme } from "@/components/theme-provider"

const Navbar = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { setTheme } = useTheme()

  const handleImportClick = () => {
    if (location.pathname === "/") {
      console.log("Resetting the page");
      // Trigger reset only if already on the Import page
      navigate(location.pathname, { state: { reset: true } });
    } else {
      // Navigate to the Import page
      navigate("/");
    }
  };

  
  const { i18n } = useTranslation(); // Hook to handle language change
  const { t } = useTranslation(); 
  const [darkMode, setDarkMode] = useState(() => {
    // Initialize darkMode state from localStorage
    return localStorage.getItem("dark") === "true";
  });
  const [language, setLanguage] = useState(() => {
    // Initialize language state from localStorage or default to "en"
    return localStorage.getItem("language") || "en";
  });

  useEffect(() => {
    // Handle dark mode changes
    if (darkMode) {
      setTheme("dark");
      localStorage.setItem("dark", "true");
    } else {
      setTheme("light");
      localStorage.setItem("dark", "false");
    }
  }, [darkMode]);

  useEffect(() => {
    // Handle language changes
    i18n.changeLanguage(language);
    localStorage.setItem("language", language);
  }, [language, i18n]);

  const toggleDarkMode = () => {
    setDarkMode((prev) => !prev);
  };

  const toggleLanguage = () => {
    setLanguage((prev) => (prev === "en" ? "de" : "en"));
  };

  return (
    <nav className="border-b bg-white dark:bg-black">
      <div className="container mx-auto flex items-center justify-between py-4">
        {/* Logo Section */}
        <div>
          <img src="src/assets/logo.png" alt="MyApp Logo" className="h-10 w-auto" />
        </div>

        {/* Navigation Menu */}
        <div className="flex items-center space-x-6">
          <NavigationMenu>
            <NavigationMenuList className="flex space-x-4 pr-4">
              <NavigationMenuItem>
              <button
                  onClick={handleImportClick}
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
                  {t("navbar.import")}
                </button>
              </NavigationMenuItem>
              <NavigationMenuItem>
                <Link
                  to="/invoices"
                  className="text-gray-700 dark:text-gray-300 hover:text-gray-900 dark:hover:text-white"
                >
                  {t("navbar.invoices")}
                </Link>
              </NavigationMenuItem>
              <NavigationMenuItem>
                <Link
                  to="/dashboard"
                  className="text-gray-700 dark:text-gray-300 hover:text-gray-900 dark:hover:text-white"
                >
                  {t("navbar.dashboard")}
                </Link>
              </NavigationMenuItem>
            </NavigationMenuList>
          </NavigationMenu>

          {/* Dark Mode and Language Toggles */}
          <div className="flex items-center space-x-4">
            {/* Dark Mode Toggle */}
            <Toggle
              aria-label="Toggle Dark Mode"
              pressed={darkMode}
              onClick={toggleDarkMode}
              className="flex items-center"
            >
              {darkMode ? (
                <SunIcon className="h-4 w-4 text-yellow-400" />
              ) : (
                <MoonIcon className="h-4 w-4 text-gray-700 dark:text-gray-300" />
              )}
            </Toggle>

            {/* Language Toggle */}
            <Toggle
              aria-label="Toggle Language"
              onClick={toggleLanguage}
              className="flex items-center"
            >
              <span className="text-gray-700 dark:text-gray-300">{language.toUpperCase()}</span>
            </Toggle>
          </div>
        </div>
      </div>
    </nav>
  );
};

export default Navbar;
